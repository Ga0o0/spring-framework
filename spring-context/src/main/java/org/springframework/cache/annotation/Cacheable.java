/*
 * Copyright 2002-2023 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;

/**
 * Annotation indicating that the result of invoking a method (or all methods
 * in a class) can be cached.
 *
 * <p>Each time an advised method is invoked, caching behavior will be applied,
 * checking whether the method has been already invoked for the given arguments.
 * A sensible default simply uses the method parameters to compute the key, but
 * a SpEL expression can be provided via the {@link #key} attribute, or a custom
 * {@link org.springframework.cache.interceptor.KeyGenerator} implementation can
 * replace the default one (see {@link #keyGenerator}).
 *
 * <p>If no value is found in the cache for the computed key, the target method
 * will be invoked and the returned value will be stored in the associated cache.
 * Note that {@link java.util.Optional} return types are unwrapped automatically.
 * If an {@code Optional} value is {@linkplain java.util.Optional#isPresent()
 * present}, it will be stored in the associated cache. If an {@code Optional}
 * value is not present, {@code null} will be stored in the associated cache.
 *
 * <p>This annotation may be used as a <em>meta-annotation</em> to create custom
 * <em>composed annotations</em> with attribute overrides.
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 * @see CacheConfig
 */
// 此注解指示可以缓存方法（或类中的所有方法）的调用结果。
//
// <p>每次调用被建议的方法时，都会应用缓存行为，检查该方法是否已针对给定参数调用过。
// 一个合理的默认值是直接使用方法参数计算键，但也可以通过 {@link #key} 属性提供 SpEL 表达式，
// 或者使用自定义的 {@link org.springframework.cache.interceptor.KeyGenerator} 实现替换默认实现（参见 {@link #keyGenerator}）。
//
// <p>如果在缓存中找不到计算出的键对应的值，则会调用目标方法，并将返回值存储在关联的缓存中。请注意，{@link java.util.Optional} 返回类型会自动解包。
// 如果 {@code Optional} 值 {@linkplain java.util.Optional#isPresent() 存在}，则会将其存储在关联的缓存中。
// 如果 {@code Optional} 值不存在，则会在关联的缓存中存储 {@code null}。
//
// <p>此注解可用作 <em>元注解</em>，以创建具有属性覆盖的自定义 <em>组合注解</em>。</p>
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Cacheable {

	/**
	 * Alias for {@link #cacheNames}.
	 */
	@AliasFor("cacheNames")
	String[] value() default {};

	/**
	 * Names of the caches in which method invocation results are stored.
	 * <p>Names may be used to determine the target cache(s), to be resolved via the
	 * configured {@link #cacheResolver()} which typically delegates to
	 * {@link org.springframework.cache.CacheManager#getCache}.
	 * <p>This will usually be a single cache name. If multiple names are specified,
	 * they will be consulted for a cache hit in the order of definition, and they
	 * will all receive a put/evict request for the same newly cached value.
	 * <p>Note that asynchronous/reactive cache access may not fully consult all
	 * specified caches, depending on the target cache. In the case of late-determined
	 * cache misses (e.g. with Redis), further caches will not get consulted anymore.
	 * As a consequence, specifying multiple cache names in an async cache mode setup
	 * only makes sense with early-determined cache misses (e.g. with Caffeine).
	 * @since 4.2
	 * @see #value
	 * @see CacheConfig#cacheNames
	 */
	// 用于存储方法调用结果的缓存名称。
	//
	// <p>名称可用于确定目标缓存，并通过配置的 {@link #cacheResolver()} 进行解析，
	// 该解析器通常会委托给 {@link org.springframework.cache.CacheManager#getCache}。
	//
	// <p>这通常是一个缓存名称。如果指定了多个名称，则会按照定义顺序查询缓存命中，并且所有名称都会收到针对同一新缓存值的 put/evict 请求。
	//
	// <p>请注意，异步/响应式缓存访问可能不会完全查询所有指定的缓存，具体取决于目标缓存。
	// 对于延迟确定的缓存未命中（例如使用 Redis），将不再查询其他缓存。
	// 因此，在异步缓存模式设置中指定多个缓存名称仅在早期确定的缓存未命中（例如使用 Caffeine）的情况下才有意义。
	@AliasFor("value")
	String[] cacheNames() default {};

	/**
	 * Spring Expression Language (SpEL) expression for computing the key dynamically.
	 * <p>Default is {@code ""}, meaning all method parameters are considered as a key,
	 * unless a custom {@link #keyGenerator} has been configured.
	 * <p>The SpEL expression evaluates against a dedicated context that provides the
	 * following meta-data:
	 * <ul>
	 * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
	 * references to the {@link java.lang.reflect.Method method}, target object, and
	 * affected cache(s) respectively.</li>
	 * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
	 * ({@code #root.targetClass}) are also available.
	 * <li>Method arguments can be accessed by index. For instance the second argument
	 * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
	 * can also be accessed by name if that information is available.</li>
	 * </ul>
	 */
	String key() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.interceptor.KeyGenerator}
	 * to use.
	 * <p>Mutually exclusive with the {@link #key} attribute.
	 * @see CacheConfig#keyGenerator
	 */
	String keyGenerator() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.CacheManager} to use to
	 * create a default {@link org.springframework.cache.interceptor.CacheResolver} if none
	 * is set already.
	 * <p>Mutually exclusive with the {@link #cacheResolver}  attribute.
	 * @see org.springframework.cache.interceptor.SimpleCacheResolver
	 * @see CacheConfig#cacheManager
	 */
	String cacheManager() default "";

	/**
	 * The bean name of the custom {@link org.springframework.cache.interceptor.CacheResolver}
	 * to use.
	 * @see CacheConfig#cacheResolver
	 */
	String cacheResolver() default "";

	/**
	 * Spring Expression Language (SpEL) expression used for making the method
	 * caching conditional. Cache the result if the condition evaluates to
	 * {@code true}.
	 * <p>Default is {@code ""}, meaning the method result is always cached.
	 * <p>The SpEL expression evaluates against a dedicated context that provides the
	 * following meta-data:
	 * <ul>
	 * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
	 * references to the {@link java.lang.reflect.Method method}, target object, and
	 * affected cache(s) respectively.</li>
	 * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
	 * ({@code #root.targetClass}) are also available.
	 * <li>Method arguments can be accessed by index. For instance the second argument
	 * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
	 * can also be accessed by name if that information is available.</li>
	 * </ul>
	 */
	String condition() default "";

	/**
	 * Spring Expression Language (SpEL) expression used to veto method caching.
	 * Veto caching the result if the condition evaluates to {@code true}.
	 * <p>Unlike {@link #condition}, this expression is evaluated after the method
	 * has been called and can therefore refer to the {@code result}.
	 * <p>Default is {@code ""}, meaning that caching is never vetoed.
	 * <p>The SpEL expression evaluates against a dedicated context that provides the
	 * following meta-data:
	 * <ul>
	 * <li>{@code #result} for a reference to the result of the method invocation. For
	 * supported wrappers such as {@code Optional}, {@code #result} refers to the actual
	 * object, not the wrapper</li>
	 * <li>{@code #root.method}, {@code #root.target}, and {@code #root.caches} for
	 * references to the {@link java.lang.reflect.Method method}, target object, and
	 * affected cache(s) respectively.</li>
	 * <li>Shortcuts for the method name ({@code #root.methodName}) and target class
	 * ({@code #root.targetClass}) are also available.
	 * <li>Method arguments can be accessed by index. For instance the second argument
	 * can be accessed via {@code #root.args[1]}, {@code #p1} or {@code #a1}. Arguments
	 * can also be accessed by name if that information is available.</li>
	 * </ul>
	 * @since 3.2
	 */
	String unless() default "";

	/**
	 * Synchronize the invocation of the underlying method if several threads are
	 * attempting to load a value for the same key. The synchronization leads to
	 * a couple of limitations:
	 * <ol>
	 * <li>{@link #unless()} is not supported</li>
	 * <li>Only one cache may be specified</li>
	 * <li>No other cache-related operation can be combined</li>
	 * </ol>
	 * This is effectively a hint and the chosen cache provider might not actually
	 * support it in a synchronized fashion. Check your provider documentation for
	 * more details on the actual semantics.
	 * @since 4.3
	 * @see org.springframework.cache.Cache#get(Object, Callable)
	 */
	boolean sync() default false;

}
