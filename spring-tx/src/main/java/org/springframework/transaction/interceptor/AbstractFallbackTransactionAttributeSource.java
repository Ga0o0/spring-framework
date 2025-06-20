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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;

/**
 * Abstract implementation of {@link TransactionAttributeSource} that caches
 * attributes for methods and implements a fallback policy: 1. specific target
 * method; 2. target class; 3. declaring method; 4. declaring class/interface.
 *
 * <p>Defaults to using the target class's transaction attribute if none is
 * associated with the target method. Any transaction attribute associated with
 * the target method completely overrides a class transaction attribute.
 * If none found on the target class, the interface that the invoked method
 * has been called through (in case of a JDK proxy) will be checked.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
// {@link TransactionAttributeSource} 的抽象实现，用于缓存方法的属性并实现以下回退策略：
// 1. 特定目标方法；2. 目标类；3. 声明方法；4. 声明类/接口。
//
// <p>如果目标方法未关联任何事务属性，则默认使用目标类的事务属性。任何与目标方法关联的事务属性都会完全覆盖类事务属性。
// 如果在目标类中未找到任何事务属性，则将检查调用方法所通过的接口（如果使用 JDK 代理）。
public abstract class AbstractFallbackTransactionAttributeSource
		implements TransactionAttributeSource, EmbeddedValueResolverAware {

	/**
	 * Canonical value held in cache to indicate no transaction attribute was
	 * found for this method, and we don't need to look again.
	 */
	// 缓存中保存的规范值指示未找到此方法的事务属性，我们不需要再次查找。
	@SuppressWarnings("serial")
	private static final TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute() {
		@Override
		public String toString() {
			return "null";
		}
	};


	/**
	 * Logger available to subclasses.
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private transient StringValueResolver embeddedValueResolver;

	/**
	 * Cache of TransactionAttributes, keyed by method on a specific target class.
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	// TransactionAttributes 的缓存，以特定目标类上的方法为键。
	// <p>由于此基类未标记为可序列化，因此缓存将在序列化后重新创建 - 前提是具体子类是可序列化的。
	private final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);


	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}


	/**
	 * Determine the transaction attribute for this method invocation.
	 * <p>Defaults to the class's transaction attribute if no method attribute is found.
	 * @param method the method for the current invocation (never {@code null})
	 * @param targetClass the target class for this invocation (can be {@code null})
	 * @return a TransactionAttribute for this method, or {@code null} if the method
	 * is not transactional
	 */
	// 确定此方法调用的事务属性。
	// <p>如果未找到方法属性，则默认为类的事务属性。
	// @param method 当前调用的方法（永不为 null）
	// @param targetClass 本次调用的目标类（可以为 null）
	// @return 此方法的 TransactionAttribute 属性，如果方法不是事务性的，则返回 null
	@Override
	@Nullable
	public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		if (method.getDeclaringClass() == Object.class) {
			return null;
		}

		// 为给定方法和目标类确定缓存键。 -> MethodClassKey
		Object cacheKey = getCacheKey(method, targetClass);
		TransactionAttribute cached = this.attributeCache.get(cacheKey);

		if (cached != null) {
			return (cached != NULL_TRANSACTION_ATTRIBUTE ? cached : null);
		}
		else {
			TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
			if (txAttr != null) {
				// 返回给定方法的限定名，由完全限定接口/类名 + "." + 方法名组成。
				String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
				if (txAttr instanceof DefaultTransactionAttribute dta) {
					// 为此事务属性设置描述符，例如指示该属性的应用位置。
					dta.setDescriptor(methodIdentification);
					// 解析定义为可解析字符串的属性值：timeoutString、qualifier、labels。
					dta.resolveAttributeStrings(this.embeddedValueResolver);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Adding transactional method '" + methodIdentification + "' with attribute: " + txAttr);
				}
				this.attributeCache.put(cacheKey, txAttr);
			}
			else {
				this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
			}
			return txAttr;
		}
	}

	/**
	 * Determine a cache key for the given method and target class.
	 * <p>Must not produce same key for overloaded methods.
	 * Must produce same key for different instances of the same method.
	 * @param method the method (never {@code null})
	 * @param targetClass the target class (may be {@code null})
	 * @return the cache key (never {@code null})
	 */
	// 为给定方法和目标类确定缓存键。
	// <p>重载方法的键值不能相同。同一方法的不同实例必须生成相同的键值。
	// @param method 方法（不可为 null）
	// @param targetClass 目标类（可以为 null）
	// @return 缓存键（不可为 null）
	protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
		// 为给定方法和目标类创建一个键对象。
		return new MethodClassKey(method, targetClass);
	}

	/**
	 * Same signature as {@link #getTransactionAttribute}, but doesn't cache the result.
	 * {@link #getTransactionAttribute} is effectively a caching decorator for this method.
	 * <p>As of 4.1.8, this method can be overridden.
	 * @since 4.1.8
	 * @see #getTransactionAttribute
	 */
	// 与 {@link #getTransactionAttribute} 签名相同，但不缓存结果。{@link #getTransactionAttribute} 实际上是此方法的缓存装饰器。
	// <p>从 4.1.8 开始，此方法可以被覆盖。
	@Nullable
	protected TransactionAttribute computeTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		// Don't allow non-public methods, as configured. --> 译文：按照配置，不允许非公共方法。
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}

		// The method may be on an interface, but we need attributes from the target class.
		// If the target class is null, the method will be unchanged.
		// --> 译文：该方法可能位于接口上，但我们需要目标类的属性。如果目标类为 null，则该方法将保持不变。
		// 1. 给定一个方法（可能来自接口）和当前 AOP 调用中使用的目标类，如果存在则查找相应的目标方法。
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// First try is the method in the target class. --> 译文：首先尝试的是目标类中的方法。
		TransactionAttribute txAttr = findTransactionAttribute(specificMethod);
		if (txAttr != null) {
			return txAttr;
		}

		// Second try is the transaction attribute on the target class. --> 译文：第二次尝试是目标类上的事务属性。
		txAttr = findTransactionAttribute(specificMethod.getDeclaringClass());
		// ClassUtils.isUserLevelMethod(method) -> 确定给定方法是否由用户声明，或者至少指向用户声明的方法。
		if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
			return txAttr;
		}

		if (specificMethod != method) {
			// Fallback is to look at the original method. --> 译文：Fallback 就是看一下原来的方法。
			txAttr = findTransactionAttribute(method);
			if (txAttr != null) {
				return txAttr;
			}
			// Last fallback is the class of the original method. --> 译文：最后的回退是原始方法的类。
			txAttr = findTransactionAttribute(method.getDeclaringClass());
			if (txAttr != null && ClassUtils.isUserLevelMethod(method)) {
				return txAttr;
			}
		}

		return null;
	}


	/**
	 * Subclasses need to implement this to return the transaction attribute for the
	 * given class, if any.
	 * @param clazz the class to retrieve the attribute for
	 * @return all transaction attribute associated with this class, or {@code null} if none
	 */
	// 子类需要实现此方法，以返回给定类的事务属性（如果有）。
	// @param clazz 要检索属性的类
	// @return 与此类关联的所有事务属性，如果没有，则返回 {@code null}
	@Nullable
	protected abstract TransactionAttribute findTransactionAttribute(Class<?> clazz);

	/**
	 * Subclasses need to implement this to return the transaction attribute for the
	 * given method, if any.
	 * @param method the method to retrieve the attribute for
	 * @return all transaction attribute associated with this method, or {@code null} if none
	 */
	// 子类需要实现此方法，以返回给定方法的事务属性（如果有）。
	// @param method 检索属性的方法
	// @return 与此方法关联的所有事务属性，如果没有，则返回 {@code null}。
	@Nullable
	protected abstract TransactionAttribute findTransactionAttribute(Method method);

	/**
	 * Should only public methods be allowed to have transactional semantics?
	 * <p>The default implementation returns {@code false}.
	 */
	// 是否应该只允许公共方法具有事务语义？
	// <p>默认实现返回 {@code false}。
	protected boolean allowPublicMethodsOnly() {
		return false;
	}

}
