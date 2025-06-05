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

package org.springframework.web.method.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.KType;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;
import kotlin.reflect.jvm.ReflectJvmMapping;

import org.springframework.context.MessageSource;
import org.springframework.core.CoroutinesUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.method.MethodValidator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * Extension of {@link HandlerMethod} that invokes the underlying method with
 * argument values resolved from the current HTTP request through a list of
 * {@link HandlerMethodArgumentResolver}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @since 3.1
 */
// {@link HandlerMethod} 的扩展，通过 {@link HandlerMethodArgumentResolver} 列表，使用从当前 HTTP 请求解析的参数值来调用底层方法。
public class InvocableHandlerMethod extends HandlerMethod {

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final Class<?>[] EMPTY_GROUPS = new Class<?>[0];


	private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	@Nullable
	private WebDataBinderFactory dataBinderFactory;

	@Nullable
	private MethodValidator methodValidator;

	private Class<?>[] validationGroups = EMPTY_GROUPS;


	/**
	 * Create an instance from a {@code HandlerMethod}.
	 */
	public InvocableHandlerMethod(HandlerMethod handlerMethod) {
		super(handlerMethod);
	}

	/**
	 * Create an instance from a bean instance and a method.
	 */
	// 从 bean 实例和方法创建一个实例。
	public InvocableHandlerMethod(Object bean, Method method) {
		super(bean, method);
	}

	/**
	 * Variant of {@link #InvocableHandlerMethod(Object, Method)} that
	 * also accepts a {@link MessageSource}, for use in subclasses.
	 * @since 5.3.10
	 */
	protected InvocableHandlerMethod(Object bean, Method method, @Nullable MessageSource messageSource) {
		super(bean, method, messageSource);
	}

	/**
	 * Construct a new handler method with the given bean instance, method name and parameters.
	 * @param bean the object bean
	 * @param methodName the method name
	 * @param parameterTypes the method parameter types
	 * @throws NoSuchMethodException when the method cannot be found
	 */
	public InvocableHandlerMethod(Object bean, String methodName, Class<?>... parameterTypes)
			throws NoSuchMethodException {

		super(bean, methodName, parameterTypes);
	}


	/**
	 * Set {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}
	 * to use for resolving method argument values.
	 */
	// 设置 {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} 用于解析方法参数值。
	public void setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
		this.resolvers = argumentResolvers;
	}

	/**
	 * Set the ParameterNameDiscoverer for resolving parameter names when needed
	 * (e.g. default request attribute name).
	 * <p>Default is a {@link org.springframework.core.DefaultParameterNameDiscoverer}.
	 */
	// 设置 ParameterNameDiscoverer 以便在需要时解析参数名称（例如，默认请求属性名称）。
	// <p>默认值为 {@link org.springframework.core.DefaultParameterNameDiscoverer}。
	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * Set the {@link WebDataBinderFactory} to be passed to argument resolvers allowing them
	 * to create a {@link WebDataBinder} for data binding and type conversion purposes.
	 */
	// 设置 {@link WebDataBinderFactory} 传递给参数解析器，允许它们创建 {@link WebDataBinder} 用于数据绑定和类型转换目的。
	public void setDataBinderFactory(WebDataBinderFactory dataBinderFactory) {
		this.dataBinderFactory = dataBinderFactory;
	}

	/**
	 * Set the {@link MethodValidator} to perform method validation with if the
	 * controller method {@link #shouldValidateArguments()} or
	 * {@link #shouldValidateReturnValue()}.
	 * @since 6.1
	 */
	// 设置 {@link MethodValidator} 以在控制器方法 {@link #shouldValidateArguments()} 或 {@link #shouldValidateReturnValue()} 时执行方法验证。
	public void setMethodValidator(@Nullable MethodValidator methodValidator) {
		this.methodValidator = methodValidator;
		this.validationGroups = (methodValidator != null ?
				methodValidator.determineValidationGroups(getBean(), getBridgedMethod()) : EMPTY_GROUPS);
	}


	/**
	 * Invoke the method after resolving its argument values in the context of the given request.
	 * <p>Argument values are commonly resolved through
	 * {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}.
	 * The {@code providedArgs} parameter however may supply argument values to be used directly,
	 * i.e. without argument resolution. Examples of provided argument values include a
	 * {@link WebDataBinder}, a {@link SessionStatus}, or a thrown exception instance.
	 * Provided argument values are checked before argument resolvers.
	 * <p>Delegates to {@link #getMethodArgumentValues} and calls {@link #doInvoke} with the
	 * resolved arguments.
	 * @param request the current request
	 * @param mavContainer the ModelAndViewContainer for this request
	 * @param providedArgs "given" arguments matched by type, not resolved
	 * @return the raw value returned by the invoked method
	 * @throws Exception raised if no suitable argument resolver can be found,
	 * or if the method raised an exception
	 * @see #getMethodArgumentValues
	 * @see #doInvoke
	 */
	// 在给定请求的上下文中解析其参数值后调用该方法。
	// <p>参数值通常通过 {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} 解析。
	// 但是，{@code providedArgs} 参数可以提供直接使用的参数值，即无需参数解析。
	// 提供的参数值的示例包括 {@link WebDataBinder}、{@link SessionStatus} 或引发的异常实例。提供的参数值在参数解析器之前进行检查。
	// <p>委托给 {@link #getMethodArgumentValues} 并使用解析后的参数调用 {@link #doInvoke}。
	// @param request 当前请求
	// @param mavContainer 此请求的 ModelAndViewContainer
	// @param providedArgs 按类型匹配的“给定”参数，尚未解析
	// @return 调用方法返回的原始值
	// @throws Exception 如果找不到合适的参数解析器或方法引发异常
	@Nullable
	public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		// 获取当前请求的方法参数值 -> INVOKE HandlerMethodArgumentResolver.resolveArgument()
		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isTraceEnabled()) {
			logger.trace("Arguments: " + Arrays.toString(args));
		}

		// 方法参数是否需要进行方法验证
		if (shouldValidateArguments() && this.methodValidator != null) {
			// 应用参数验证 -> MethodValidator.validateArguments()
			this.methodValidator.applyArgumentValidation(
					getBean(), getBridgedMethod(), getMethodParameters(), args, this.validationGroups);
		}

		Object returnValue = doInvoke(args);

		// 方法返回值是否是方法验证的候选值
		if (shouldValidateReturnValue() && this.methodValidator != null) {
			// 验证给定的返回值并返回验证结果。 -> MethodValidator.validateReturnValue()
			this.methodValidator.applyReturnValueValidation(
					getBean(), getBridgedMethod(), getReturnType(), returnValue, this.validationGroups);
		}

		return returnValue;
	}

	/**
	 * Get the method argument values for the current request, checking the provided
	 * argument values and falling back to the configured argument resolvers.
	 * <p>The resulting array will be passed into {@link #doInvoke}.
	 * @since 5.1.2
	 */
	// 获取当前请求的方法参数值，检查提供的参数值并返回到已配置的参数解析器。
	// <p>结果数组将传递到 {@link #doInvoke}。
	protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		// 返回此 {@code AnnotatedMethod} 的方法参数。
		MethodParameter[] parameters = getMethodParameters();
		if (ObjectUtils.isEmpty(parameters)) {
			return EMPTY_ARGS;
		}

		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			args[i] = findProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			if (!this.resolvers.supportsParameter(parameter)) {
				throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
			}
			try {
				// HandlerMethodArgumentResolverComposite.resolveArgument() -> INVOKE HandlerMethodArgumentResolver.resolveArgument()
				args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory);
			}
			catch (Exception ex) {
				// Leave stack trace for later, exception may actually be resolved and handled...
				// --> 译文：保留堆栈跟踪以供稍后使用，异常实际上可能会被解决和处理......
				if (logger.isDebugEnabled()) {
					String exMsg = ex.getMessage();
					if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
						logger.debug(formatArgumentError(parameter, exMsg));
					}
				}
				throw ex;
			}
		}
		return args;
	}

	/**
	 * Invoke the handler method with the given argument values.
	 */
	// 使用给定的参数值调用处理程序方法。
	@Nullable
	protected Object doInvoke(Object... args) throws Exception {
		// 如果被注解的方法是桥接方法，则此方法返回桥接的（用户定义的）方法。
		Method method = getBridgedMethod();
		try {
			// Kotlin 反射是否存在。
			if (KotlinDetector.isKotlinReflectPresent()) {
				if (KotlinDetector.isSuspendingFunction(method)) {
					return invokeSuspendingFunction(method, getBean(), args);
				}
				else if (KotlinDetector.isKotlinType(method.getDeclaringClass())) {
					return KotlinDelegate.invokeFunction(method, getBean(), args);
				}
			}
			// invoke java.lang.reflect.Method.invoke()
			return method.invoke(getBean(), args);
		}
		catch (IllegalArgumentException ex) {
			// 断言目标 Bean 类是声明了指定方法的类的实例。
			assertTargetBean(method, getBean(), args);
			String text = (ex.getMessage() == null || ex.getCause() instanceof NullPointerException) ?
					"Illegal argument" : ex.getMessage();
			throw new IllegalStateException(formatInvokeError(text, args), ex);
		}
		catch (InvocationTargetException ex) {
			// Unwrap for HandlerExceptionResolvers ...
			Throwable targetException = ex.getCause();
			if (targetException instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			else if (targetException instanceof Error error) {
				throw error;
			}
			else if (targetException instanceof Exception exception) {
				throw exception;
			}
			else {
				throw new IllegalStateException(formatInvokeError("Invocation failure", args), targetException);
			}
		}
	}

	/**
	 * Invoke the given Kotlin coroutine suspended function.
	 * <p>The default implementation invokes
	 * {@link CoroutinesUtils#invokeSuspendingFunction(Method, Object, Object...)},
	 * but subclasses can override this method to use
	 * {@link CoroutinesUtils#invokeSuspendingFunction(kotlin.coroutines.CoroutineContext, Method, Object, Object...)}
	 * instead.
	 * @since 6.0
	 */
	protected Object invokeSuspendingFunction(Method method, Object target, Object[] args) {
		return CoroutinesUtils.invokeSuspendingFunction(method, target, args);
	}


	/**
	 * Inner class to avoid a hard dependency on Kotlin at runtime.
	 */
	private static class KotlinDelegate {

		@Nullable
		@SuppressWarnings({"deprecation", "DataFlowIssue"})
		public static Object invokeFunction(Method method, Object target, Object[] args) throws InvocationTargetException, IllegalAccessException {
			KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
			// For property accessors
			if (function == null) {
				return method.invoke(target, args);
			}
			if (method.isAccessible() && !KCallablesJvm.isAccessible(function)) {
				KCallablesJvm.setAccessible(function, true);
			}
			Map<KParameter, Object> argMap = CollectionUtils.newHashMap(args.length + 1);
			int index = 0;
			for (KParameter parameter : function.getParameters()) {
				switch (parameter.getKind()) {
					case INSTANCE -> argMap.put(parameter, target);
					case VALUE, EXTENSION_RECEIVER -> {
						Object arg = args[index];
						if (!(parameter.isOptional() && arg == null)) {
							KType type = parameter.getType();
							if (!(type.isMarkedNullable() && arg == null) && type.getClassifier() instanceof KClass<?> kClass
									&& KotlinDetector.isInlineClass(JvmClassMappingKt.getJavaClass(kClass))) {
								KFunction<?> constructor = KClasses.getPrimaryConstructor(kClass);
								if (!KCallablesJvm.isAccessible(constructor)) {
									KCallablesJvm.setAccessible(constructor, true);
								}
								arg = constructor.call(arg);
							}
							argMap.put(parameter, arg);
						}
						index++;
					}
				}
			}
			Object result = function.callBy(argMap);
			return (result == Unit.INSTANCE ? null : result);
		}
	}

}
