/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Job;

import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.CoroutinesUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods for AOP support code.
 *
 * <p>Mainly for internal use within Spring's AOP support.
 *
 * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

	private static final boolean coroutinesReactorPresent = ClassUtils.isPresent(
			"kotlinx.coroutines.reactor.MonoKt", AopUtils.class.getClassLoader());


	/**
	 * Check whether the given object is a JDK dynamic proxy or a CGLIB proxy.
	 * <p>This method additionally checks if the given object is an instance
	 * of {@link SpringProxy}.
	 * @param object the object to check
	 * @see #isJdkDynamicProxy
	 * @see #isCglibProxy
	 */
	public static boolean isAopProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && (Proxy.isProxyClass(object.getClass()) ||
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)));
	}

	/**
	 * Check whether the given object is a JDK dynamic proxy.
	 * <p>This method goes beyond the implementation of
	 * {@link Proxy#isProxyClass(Class)} by additionally checking if the
	 * given object is an instance of {@link SpringProxy}.
	 * @param object the object to check
	 * @see java.lang.reflect.Proxy#isProxyClass
	 */
	public static boolean isJdkDynamicProxy(@Nullable Object object) {
		return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
	}

	/**
	 * Check whether the given object is a CGLIB proxy.
	 * <p>This method goes beyond the implementation of
	 * {@link ClassUtils#isCglibProxy(Object)} by additionally checking if
	 * the given object is an instance of {@link SpringProxy}.
	 * @param object the object to check
	 * @see ClassUtils#isCglibProxy(Object)
	 */
	public static boolean isCglibProxy(@Nullable Object object) {
		return (object instanceof SpringProxy &&
				object.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR));
	}

	/**
	 * Determine the target class of the given bean instance which might be an AOP proxy.
	 * <p>Returns the target class for an AOP proxy or the plain class otherwise.
	 * @param candidate the instance to check (might be an AOP proxy)
	 * @return the target class (or the plain class of the given object as fallback;
	 * never {@code null})
	 * @see org.springframework.aop.TargetClassAware#getTargetClass()
	 * @see org.springframework.aop.framework.AopProxyUtils#ultimateTargetClass(Object)
	 */
	// 确定给定 bean 实例的目标类，该类可能是 AOP 代理。
	// <p>如果是 AOP 代理，则返回目标类；否则返回普通类。
	// @param candidates 要检查的实例（可能是 AOP 代理）
	// @return 目标类（或给定对象的普通类作为后备；永远不会返回 {@code null}）
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if (candidate instanceof TargetClassAware targetClassAware) {
			result = targetClassAware.getTargetClass();
		}
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	/**
	 * Select an invocable method on the target type: either the given method itself
	 * if actually exposed on the target type, or otherwise a corresponding method
	 * on one of the target type's interfaces or on the target type itself.
	 * @param method the method to check
	 * @param targetType the target type to search methods on (typically an AOP proxy)
	 * @return a corresponding invocable method on the target type
	 * @throws IllegalStateException if the given method is not invocable on the given
	 * target type (typically due to a proxy mismatch)
	 * @since 4.3
	 * @see MethodIntrospector#selectInvocableMethod(Method, Class)
	 */
	// 选择目标类型上的可调用方法：如果实际在目标类型上暴露，则选择给定方法本身，否则选择目标类型的接口之一或目标类型本身上的相应方法。
	// @param method 要检查的方法
	// @param targetType 要在其上搜索方法的目标类型（通常是 AOP 代理）
	// @return 目标类型上相应的可调用方法
	// @throws IllegalStateException 如果给定方法在给定目标类型上不可调用（通常是由于代理不匹配）
	public static Method selectInvocableMethod(Method method, @Nullable Class<?> targetType) {
		if (targetType == null) {
			return method;
		}
		// 选择可调用方法
		Method methodToUse = MethodIntrospector.selectInvocableMethod(method, targetType);
		if (Modifier.isPrivate(methodToUse.getModifiers()) && !Modifier.isStatic(methodToUse.getModifiers()) &&
				SpringProxy.class.isAssignableFrom(targetType)) {
			// 需要调用目标类 “%s” 的代理上的方法 “%s” ，但无法委托给目标 Bean。请将其可见性切换为包或受保护。
			throw new IllegalStateException(String.format(
					"Need to invoke method '%s' found on proxy for target class '%s' but cannot " +
					"be delegated to target bean. Switch its visibility to package or protected.",
					method.getName(), method.getDeclaringClass().getSimpleName()));
		}
		return methodToUse;
	}

	/**
	 * Determine whether the given method is an "equals" method.
	 * @see java.lang.Object#equals
	 */
	// 确定给定的方法是否是 “equals” 方法。
	public static boolean isEqualsMethod(@Nullable Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}

	/**
	 * Determine whether the given method is a "hashCode" method.
	 * @see java.lang.Object#hashCode
	 */
	public static boolean isHashCodeMethod(@Nullable Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}

	/**
	 * Determine whether the given method is a "toString" method.
	 * @see java.lang.Object#toString()
	 */
	public static boolean isToStringMethod(@Nullable Method method) {
		return ReflectionUtils.isToStringMethod(method);
	}

	/**
	 * Determine whether the given method is a "finalize" method.
	 * @see java.lang.Object#finalize()
	 */
	public static boolean isFinalizeMethod(@Nullable Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterCount() == 0);
	}

	/**
	 * Given a method, which may come from an interface, and a target class used
	 * in the current AOP invocation, find the corresponding target method if there
	 * is one. E.g. the method may be {@code IFoo.bar()} and the target class
	 * may be {@code DefaultFoo}. In this case, the method may be
	 * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
	 * <p><b>NOTE:</b> In contrast to {@link org.springframework.util.ClassUtils#getMostSpecificMethod},
	 * this method resolves bridge methods in order to retrieve attributes from
	 * the <i>original</i> method definition.
	 * @param method the method to be invoked, which may come from an interface
	 * @param targetClass the target class for the current invocation
	 * (can be {@code null} or may not even implement the method)
	 * @return the specific target method, or the original method if the
	 * {@code targetClass} does not implement it
	 * @see org.springframework.util.ClassUtils#getMostSpecificMethod
	 * @see org.springframework.core.BridgeMethodResolver#getMostSpecificMethod
	 */
	// 给定一个方法（可能来自接口）和当前 AOP 调用中使用的目标类，如果存在则查找相应的目标方法。
	// 例如，方法可能是 {@code IFoo.bar()}，目标类可能是 {@code DefaultFoo}。
	// 在这种情况下，方法可能是 {@code DefaultFoo.bar()}。这样就可以找到该方法的属性。
	// <p><b>注意：</b>与 {@link org.springframework.util.ClassUtils#getMostSpecificMethod} 不同，
	// 此方法解析桥接方法，以便从<i>原始</i>方法定义中检索属性。
	// @param method 待调用的方法，可能来自接口
	// @param targetClass 当前调用的目标类（可以为 {@code null} 或甚至未实现该方法）
	// @return 具体的目标方法，如果 {@code targetClass} 未实现原始方法，则返回原始方法
	public static Method getMostSpecificMethod(Method method, @Nullable Class<?> targetClass) {
		Class<?> specificTargetClass = (targetClass != null ? ClassUtils.getUserClass(targetClass) : null);
		return BridgeMethodResolver.getMostSpecificMethod(method, specificTargetClass);
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * <p>This is an important test as it can be used to optimize
	 * out a pointcut for a class.
	 * @param pc the static or dynamic pointcut to check
	 * @param targetClass the class to test
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(Pointcut pc, Class<?> targetClass) {
		return canApply(pc, targetClass, false);
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * <p>This is an important test as it can be used to optimize
	 * out a pointcut for a class.
	 * @param pc the static or dynamic pointcut to check
	 * @param targetClass the class to test
	 * @param hasIntroductions whether the advisor chain
	 * for this bean includes any introductions
	 * @return whether the pointcut can apply on any method
	 */
	// 给定的切入点是否适用于给定的类？
	// <p>这是一个重要的测试，因为它可以用来优化类的切入点。
	// @param pc 要检查的静态或动态切入点
	// @param targetClass 要测试的类
	// @param hasIntroductions 此 bean 的 advisor 链是否包含任何引入
	// @return 切入点是否可以应用于任何方法
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		if (methodMatcher == MethodMatcher.TRUE) {
			// No need to iterate the methods if we're matching any method anyway... --> 译文：如果我们匹配任何方法，则无需迭代方法......
			return true;
		}

		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher iamm) {
			introductionAwareMethodMatcher = iamm;
		}

		Set<Class<?>> classes = new LinkedHashSet<>();
		if (!Proxy.isProxyClass(targetClass)) {
			classes.add(ClassUtils.getUserClass(targetClass));
		}
		classes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetClass));

		for (Class<?> clazz : classes) {
			Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
			for (Method method : methods) {
				if (introductionAwareMethodMatcher != null ?
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions) :
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Can the given advisor apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out an advisor for a class.
	 * @param advisor the advisor to check
	 * @param targetClass class we're testing
	 * @return whether the pointcut can apply on any method
	 */
	// 给定的 advisor 是否适用于给定的类？这是一个重要的测试，因为它可以用来优化某个类的 advisor。
	// @param advisor 需要检查的 advisor
	// @param targetClass 我们正在测试的类
	// @return 切入点是否可以应用于任何方法
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}

	/**
	 * Can the given advisor apply at all on the given class?
	 * <p>This is an important test as it can be used to optimize out an advisor for a class.
	 * This version also takes into account introductions (for IntroductionAwareMethodMatchers).
	 * @param advisor the advisor to check
	 * @param targetClass class we're testing
	 * @param hasIntroductions whether the advisor chain for this bean includes
	 * any introductions
	 * @return whether the pointcut can apply on any method
	 */
	// 给定的 advisor 是否适用于给定的类？
	// <p>这是一个重要的测试，因为它可以用来优化类的 advisor。此版本还考虑了引入（用于 IntroductionAwareMethodMatchers）。
	// @param advisor 需要检查的 advisor
	// @param targetClass 我们正在测试的类
	// @param hasIntroductions 此 bean 的 advisor 链是否包含任何引入
	// @return 切入点是否可以应用于任何方法
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		if (advisor instanceof IntroductionAdvisor ia) {
			return ia.getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor pca) {
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// It doesn't have a pointcut so we assume it applies. --> 译文：它没有切入点，因此我们假设它适用。
			return true;
		}
	}

	/**
	 * Determine the sublist of the {@code candidateAdvisors} list
	 * that is applicable to the given class.
	 * @param candidateAdvisors the Advisors to evaluate
	 * @param clazz the target class
	 * @return sublist of Advisors that can apply to an object of the given class
	 * (may be the incoming List as-is)
	 */
	// 确定适用于给定类的 {@code candidatesAdvisors} 列表中的子列表。
	// @param candidatesAdvisors 需要评估的 Advisors
	// @param clazz 目标类
	// @return 可应用于给定类对象的 Advisors 子列表（可以是传入的 List 原样）
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		List<Advisor> eligibleAdvisors = new ArrayList<>();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
				eligibleAdvisors.add(candidate);
			}
		}
		boolean hasIntroductions = !eligibleAdvisors.isEmpty();
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor) {
				// already processed
				continue;
			}
			if (canApply(candidate, clazz, hasIntroductions)) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * Invoke the given target via reflection, as part of an AOP method invocation.
	 * @param target the target object
	 * @param method the method to invoke
	 * @param args the arguments for the method
	 * @return the invocation result, if any
	 * @throws Throwable if thrown by the target method
	 * @throws org.springframework.aop.AopInvocationException in case of a reflection error
	 */
	// 通过反射调用给定目标，作为 AOP 方法调用的一部分。
	// @param target 目标对象
	// @param method 要调用的方法
	// @param args 方法的参数
	// @return 调用结果（如果有）
	// @throws Throwable（如果目标方法抛出）
	// @throws org.springframework.aop.AopInvocationException（如果发生反射错误）
	@Nullable
	public static Object invokeJoinpointUsingReflection(@Nullable Object target, Method method, Object[] args)
			throws Throwable {

		// Use reflection to invoke the method. --> 译文：使用反射来调用该方法。
		try {
			// 查找提供的 bridge Method 的本地原始方法。
			Method originalMethod = BridgeMethodResolver.findBridgedMethod(method);
			ReflectionUtils.makeAccessible(originalMethod);
			return (coroutinesReactorPresent && KotlinDetector.isSuspendingFunction(originalMethod) ?
					// originalMethod.invoke(target, args) -> java.lang.reflect.Method.invoke()
					KotlinDelegate.invokeSuspendingFunction(originalMethod, target, args) : originalMethod.invoke(target, args));
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception.
			// We must rethrow it. The client won't see the interceptor. --> 译文：调用的方法抛出了已检查异常。我们必须重新抛出该异常。客户端将无法看到该拦截器。
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" +
					method + "] on target [" + target + "]", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopInvocationException("Could not access method [" + method + "]", ex);
		}
	}


	/**
	 * Inner class to avoid a hard dependency on Kotlin at runtime.
	 */
	private static class KotlinDelegate {

		public static Object invokeSuspendingFunction(Method method, @Nullable Object target, Object... args) {
			Continuation<?> continuation = (Continuation<?>) args[args.length -1];
			Assert.state(continuation != null, "No Continuation available");
			CoroutineContext context = continuation.getContext().minusKey(Job.Key);
			return CoroutinesUtils.invokeSuspendingFunction(context, method, target, args);
		}
	}

}
