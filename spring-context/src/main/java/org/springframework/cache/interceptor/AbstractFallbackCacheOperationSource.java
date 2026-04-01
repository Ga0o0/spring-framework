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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Abstract implementation of {@link CacheOperationSource} that caches operations
 * for methods and implements a fallback policy: 1. specific target method;
 * 2. target class; 3. declaring method; 4. declaring class/interface.
 *
 * <p>Defaults to using the target class's declared cache operations if none are
 * associated with the target method. Any cache operations associated with
 * the target method completely override any class-level declarations.
 * If none found on the target class, the interface that the invoked method
 * has been called through (in case of a JDK proxy) will be checked.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractFallbackCacheOperationSource implements CacheOperationSource {

	/**
	 * Canonical value held in cache to indicate no cache operation was
	 * found for this method, and we don't need to look again.
	 */
	private static final Collection<CacheOperation> NULL_CACHING_MARKER = Collections.emptyList();


	/**
	 * Logger available to subclasses.
	 * <p>As this base class is not marked Serializable, the logger will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Cache of CacheOperations, keyed by method on a specific target class.
	 * <p>As this base class is not marked Serializable, the cache will be recreated
	 * after serialization - provided that the concrete subclass is Serializable.
	 */
	// 缓存操作，以特定目标类的方法为键。
	// <p>由于此基类未标记为 Serializable，因此序列化后将重新创建缓存——前提是具体的子类是 Serializable 的。</p>
	private final Map<Object, Collection<CacheOperation>> operationCache = new ConcurrentHashMap<>(1024);


	/**
	 * Determine the cache operations for this method invocation.
	 * <p>Defaults to class-declared metadata if no method-level metadata is found.
	 * @param method the method for the current invocation (never {@code null})
	 * @param targetClass the target class for this invocation (can be {@code null})
	 * @return {@link CacheOperation} for this method, or {@code null} if the method
	 * is not cacheable
	 */
	// 确定此方法调用的缓存操作。
	// <p>如果未找到方法级元数据，则默认使用类声明的元数据。</p>
	// @param method 当前调用的方法（永远不会为 {@code null}）
	// @param targetClass 此调用的目标类（可以为 {@code null}）
	// @return 此方法的 {@link CacheOperation}，如果该方法不可缓存，则返回 {@code null}。
	@Override
	@Nullable
	public Collection<CacheOperation> getCacheOperations(Method method, @Nullable Class<?> targetClass) {
		if (method.getDeclaringClass() == Object.class) {
			return null;
		}

		Object cacheKey = getCacheKey(method, targetClass);
		Collection<CacheOperation> cached = this.operationCache.get(cacheKey);

		if (cached != null) {
			return (cached != NULL_CACHING_MARKER ? cached : null);
		}
		else {
			Collection<CacheOperation> cacheOps = computeCacheOperations(method, targetClass);
			if (cacheOps != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Adding cacheable method '" + method.getName() + "' with operations: " + cacheOps);
				}
				this.operationCache.put(cacheKey, cacheOps);
			}
			else {
				this.operationCache.put(cacheKey, NULL_CACHING_MARKER);
			}
			return cacheOps;
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
	// 为给定的方法和目标类确定缓存键。
	// <p>重载方法不能使用相同的缓存键。同一方法的不同实例必须使用相同的缓存键。
	// @param method 方法（不能为空）
	// @param targetClass 目标类（可以为空）
	// @return 缓存键（不能为空）
	protected Object getCacheKey(Method method, @Nullable Class<?> targetClass) {
		return new MethodClassKey(method, targetClass);
	}

	@Nullable
	private Collection<CacheOperation> computeCacheOperations(Method method, @Nullable Class<?> targetClass) {
		// Don't allow non-public methods, as configured. --> 译文：不允许使用 non-public 方法，按配置执行。
		if (allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {
			return null;
		}

		// The method may be on an interface, but we need metadata from the target class.
		// If the target class is null, the method will be unchanged.
		// --> 译文：该方法可能位于接口中，但我们需要目标类的元数据。如果目标类为空，则该方法将保持不变。
		Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);

		// First try is the method in the target class. --> 译文：首先尝试的是目标类中的方法。
		Collection<CacheOperation> opDef = findCacheOperations(specificMethod);
		if (opDef != null) {
			return opDef;
		}

		// Second try is the caching operation on the target class.--> 译文：第二次尝试是对目标类进行缓存操作。
		opDef = findCacheOperations(specificMethod.getDeclaringClass());
		if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
			return opDef;
		}

		if (specificMethod != method) {
			// Fallback is to look at the original method. --> 译文：退而求其次的方法是查看原始方法。
			opDef = findCacheOperations(method);
			if (opDef != null) {
				return opDef;
			}
			// Last fallback is the class of the original method. --> 译文：最后一种回退机制是使用原始方法的类。
			opDef = findCacheOperations(method.getDeclaringClass());
			if (opDef != null && ClassUtils.isUserLevelMethod(method)) {
				return opDef;
			}
		}

		return null;
	}


	/**
	 * Subclasses need to implement this to return the cache operations for the
	 * given class, if any.
	 * @param clazz the class to retrieve the cache operations for
	 * @return all cache operations associated with this class, or {@code null} if none
	 */
	// 子类需要实现此方法，以返回给定类的缓存操作（如果有的话）。
	// @param clazz 要检索其缓存操作的类
	// @return 与此类相关的所有缓存操作，如果没有则返回 {@code null}
	@Nullable
	protected abstract Collection<CacheOperation> findCacheOperations(Class<?> clazz);

	/**
	 * Subclasses need to implement this to return the cache operations for the
	 * given method, if any.
	 * @param method the method to retrieve the cache operations for
	 * @return all cache operations associated with this method, or {@code null} if none
	 */
	// 子类需要实现此方法以返回给定方法的缓存操作（如果有）。
	// @param method 要检索缓存操作的方法
	// @return 与此方法关联的所有缓存操作，如果没有则返回 {@code null}
	@Nullable
	protected abstract Collection<CacheOperation> findCacheOperations(Method method);

	/**
	 * Should only public methods be allowed to have caching semantics?
	 * <p>The default implementation returns {@code false}.
	 */
	// 是否只有公共方法才允许具有缓存语义？
	// <p>默认实现返回 {@code false}。</p>
	protected boolean allowPublicMethodsOnly() {
		return false;
	}

}
