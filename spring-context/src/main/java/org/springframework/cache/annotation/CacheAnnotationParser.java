/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.cache.annotation;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for parsing known caching annotation types.
 * {@link AnnotationCacheOperationSource} delegates to such parsers
 * for supporting specific annotation types such as Spring's own
 * {@link Cacheable}, {@link CachePut} and{@link CacheEvict}.
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 3.1
 * @see AnnotationCacheOperationSource
 * @see SpringCacheAnnotationParser
 */
// 用于解析已知缓存注解类型的策略接口。
// {@link AnnotationCacheOperationSource} 委托给此类解析器，
// 以支持特定的注解类型，例如 Spring 自己的 {@link Cacheable}、{@link CachePut} 和 {@link CacheEvict}。
public interface CacheAnnotationParser {

	/**
	 * Determine whether the given class is a candidate for cache operations
	 * in the annotation format of this {@code CacheAnnotationParser}.
	 * <p>If this method returns {@code false}, the methods on the given class
	 * will not get traversed for {@code #parseCacheAnnotations} introspection.
	 * Returning {@code false} is therefore an optimization for non-affected
	 * classes, whereas {@code true} simply means that the class needs to get
	 * fully introspected for each method on the given class individually.
	 * @param targetClass the class to introspect
	 * @return {@code false} if the class is known to have no cache operation
	 * annotations at class or method level; {@code true} otherwise. The default
	 * implementation returns {@code true}, leading to regular introspection.
	 * @since 5.2
	 */
	// 判断给定的类是否符合此 {@code CacheAnnotationParser} 的注解格式，并可进行缓存操作。
	//
	// <p>如果此方法返回 {@code false}，则不会遍历给定类中的方法以进行 {@code #parseCacheAnnotations} 自省。
	// 因此，返回 {@code false} 是对不受影响的类的一种优化；而返回 {@code true} 则表示需要对给定类中的每个方法进行完整的自省。
	// @param targetClass 要进行自省的类
	// @return 如果已知该类在类级别或方法级别上没有缓存操作注解，则返回 {@code false}；否则返回 {@code true}。默认实现返回 {@code true}，从而进行常规自省。
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * Parse the cache definition for the given class,
	 * based on an annotation type understood by this parser.
	 * <p>This essentially parses a known cache annotation into Spring's metadata
	 * attribute class. Returns {@code null} if the class is not cacheable.
	 * @param type the annotated class
	 * @return the configured caching operation, or {@code null} if none found
	 * @see AnnotationCacheOperationSource#findCacheOperations(Class)
	 */
	// 根据解析器能够识别的注解类型，解析给定类的缓存定义。
	// <p>此方法本质上是将已知的缓存注解解析为 Spring 的元数据属性类。如果该类不可缓存，则返回 {@code null}。
	// @param type 被注解的类
	// @return 已配置的缓存操作，如果未找到则返回 {@code null}
	// @see AnnotationCacheOperationSource#findCacheOperations(Class)
	@Nullable
	Collection<CacheOperation> parseCacheAnnotations(Class<?> type);

	/**
	 * Parse the cache definition for the given method,
	 * based on an annotation type understood by this parser.
	 * <p>This essentially parses a known cache annotation into Spring's metadata
	 * attribute class. Returns {@code null} if the method is not cacheable.
	 * @param method the annotated method
	 * @return the configured caching operation, or {@code null} if none found
	 * @see AnnotationCacheOperationSource#findCacheOperations(Method)
	 */
	// 根据此解析器可识别的注解类型，解析给定方法的缓存定义。
	// <p>此方法本质上是将已知的缓存注解解析为 Spring 的元数据属性类。如果该方法不可缓存，则返回 {@code null}。
	// @param method 被注解的方法
	// @return 已配置的缓存操作，如果未找到，则返回 {@code null}
	// @see AnnotationCacheOperationSource#findCacheOperations(Method)
	@Nullable
	Collection<CacheOperation> parseCacheAnnotations(Method method);

}
