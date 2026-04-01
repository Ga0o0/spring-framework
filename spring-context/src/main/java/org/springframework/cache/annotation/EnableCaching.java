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

package org.springframework.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Enables Spring's annotation-driven cache management capability, similar to the
 * support found in Spring's {@code <cache:*>} XML namespace. To be used together
 * with @{@link org.springframework.context.annotation.Configuration Configuration}
 * classes as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * class AppConfig {
 *
 *     &#064;Bean
 *     MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 * }</pre>
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;cache:annotation-driven/&gt;
 *
 *     &lt;bean id="myService" class="com.foo.MyService"/&gt;
 *
 *     &lt;bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager"&gt;
 *         &lt;property name="caches"&gt;
 *             &lt;set&gt;
 *                 &lt;bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean"&gt;
 *                     &lt;property name="name" value="default"/&gt;
 *                 &lt;/bean&gt;
 *             &lt;/set&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * In both of the scenarios above, {@code @EnableCaching} and {@code
 * <cache:annotation-driven/>} are responsible for registering the necessary Spring
 * components that power annotation-driven cache management, such as the
 * {@link org.springframework.cache.interceptor.CacheInterceptor CacheInterceptor} and the
 * proxy- or AspectJ-based advice that weaves the interceptor into the call stack when
 * {@link org.springframework.cache.annotation.Cacheable @Cacheable} methods are invoked.
 *
 * <p>If the JSR-107 API and Spring's JCache implementation are present, the necessary
 * components to manage standard cache annotations are also registered. This creates the
 * proxy- or AspectJ-based advice that weaves the interceptor into the call stack when
 * methods annotated with {@code CacheResult}, {@code CachePut}, {@code CacheRemove} or
 * {@code CacheRemoveAll} are invoked.
 *
 * <p><strong>A bean of type {@link org.springframework.cache.CacheManager CacheManager}
 * must be registered</strong>, as there is no reasonable default that the framework can
 * use as a convention. And whereas the {@code <cache:annotation-driven>} element assumes
 * a bean <em>named</em> "cacheManager", {@code @EnableCaching} searches for a cache
 * manager bean <em>by type</em>. Therefore, naming of the cache manager bean method is
 * not significant.
 *
 * <p>For those that wish to establish a more direct relationship between
 * {@code @EnableCaching} and the exact cache manager bean to be used,
 * the {@link CachingConfigurer} callback interface may be implemented.
 * Notice the {@code @Override}-annotated methods below:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableCaching
 * class AppConfig implements CachingConfigurer {
 *
 *     &#064;Bean
 *     MyService myService() {
 *         // configure and return a class having &#064;Cacheable methods
 *         return new MyService();
 *     }
 *
 *     &#064;Bean
 *     &#064;Override
 *     CacheManager cacheManager() {
 *         // configure and return an implementation of Spring's CacheManager SPI
 *         SimpleCacheManager cacheManager = new SimpleCacheManager();
 *         cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
 *         return cacheManager;
 *     }
 *
 *     &#064;Override
 *     KeyGenerator keyGenerator() {
 *         // configure and return an implementation of Spring's KeyGenerator SPI
 *         return new MyKeyGenerator();
 *     }
 * }</pre>
 *
 * This approach may be desirable simply because it is more explicit, or it may be
 * necessary in order to distinguish between two {@code CacheManager} beans present in the
 * same container.
 *
 * <p>Notice also the {@code keyGenerator} method in the example above. This allows for
 * customizing the strategy for cache key generation, per Spring's {@link
 * org.springframework.cache.interceptor.KeyGenerator KeyGenerator} SPI. Normally,
 * {@code @EnableCaching} will configure Spring's
 * {@link org.springframework.cache.interceptor.SimpleKeyGenerator SimpleKeyGenerator}
 * for this purpose, but when implementing {@code CachingConfigurer}, a custom key
 * generator can be specified.
 *
 * <p>{@link CachingConfigurer} offers additional customization options:
 * see the {@link CachingConfigurer} javadoc for further details.
 *
 * <p>The {@link #mode} attribute controls how advice is applied: If the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior
 * of the proxying. Please note that proxy mode allows for interception of calls through
 * the proxy only; local calls within the same class cannot get intercepted that way.
 *
 * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the
 * value of the {@link #proxyTargetClass} attribute will be ignored. Note also that in
 * this case the {@code spring-aspects} module JAR must be present on the classpath, with
 * compile-time weaving or load-time weaving applying the aspect to the affected classes.
 * There is no proxy involved in such a scenario; local calls will be intercepted as well.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see CachingConfigurer
 * @see CachingConfigurationSelector
 * @see ProxyCachingConfiguration
 * @see org.springframework.cache.aspectj.AspectJCachingConfiguration
 */
// 启用 Spring 的注解驱动缓存管理功能，类似于 Spring 的 <cache:*> XML 命名空间中的支持。需与 @Configuration 类一起使用，如下所示：
//
// @Configuration
// @EnableCaching
// class AppConfig {
//
//   @Bean
//   MyService myService() {
//     // 配置并返回一个具有 @Cacheable 方法的类
//     return new MyService();
//   }
//
//   @Bean
//   CacheManager cacheManager() {
//     // 配置并返回一个 Spring CacheManager SPI 的实现
//     SimpleCacheManager cacheManager = new SimpleCacheManager();
//     cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
//     return cacheManager;
//   }
// }
//
// 作为参考，上述示例可以与以下 Spring XML 配置进行比较：
//
// <beans>
//
//   <cache:annotation-driven/>
//
//   <bean id="myService" class="com.foo.MyService"/>
//
//   <bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
//   	<property name="caches">
//   		<set>
//     			<bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean">
//     				<property name="name" value="default"/>
// 				</bean>
// 			</set>
// 		</property>
// 	 </bean>
//
// </beans>
//
// 在上述两种情况下，@EnableCaching 和 <cache:annotation-driven/> 都负责注册支持注解驱动缓存管理的必要 Spring 组件，
// 例如 CacheInterceptor 以及在调用时将拦截器编织到调用栈中的基于代理或 AspectJ 的通知。 @Cacheable 方法会被调用。
//
// 如果 JSR-107 API 和 Spring 的 JCache 实现存在，则管理标准缓存注解所需的组件也会被注册。这会创建一个基于代理或 AspectJ 的通知，
// 当调用带有 CacheResult、CachePut、CacheRemove 或 CacheRemoveAll 注解的方法时，该通知会将拦截器编织到调用栈中。
//
// 必须注册一个 CacheManager 类型的 bean，因为框架没有合理的默认值可供使用。
// 虽然 <cache:annotation-driven> 元素假定存在一个名为 “cacheManager” 的 bean，
// 但 @EnableCaching 会按类型查找缓存管理器 bean。因此，缓存管理器 bean 方法的命名并不重要。
//
// 对于那些希望在 @EnableCaching 和要使用的具体缓存管理器 bean 之间建立更直接关系的用户，
// 可以实现 CachingConfigurer 回调接口。请注意以下带有 @Override 注解的方法：
//
// @Configuration
// @EnableCaching
// class AppConfig implements CachingConfigurer {
//
// 		@Bean
// 		MyService myService() {
//			// 配置并返回一个具有 @Cacheable 方法的类
//			return new MyService();
// 		}
//
//		@Bean
// 		@Override
// 		CacheManager cacheManager() {
// 			// 配置并返回 Spring CacheManager SPI 的一个实现
// 			SimpleCacheManager cacheManager = new SimpleCacheManager();
// 			cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
// 			return cacheManager;
// 		}
//
//  	@Override
// 		KeyGenerator keyGenerator() {
// 			// 配置并返回 Spring KeyGenerator SPI 的一个实现
// 			return new MyKeyGenerator();
// 		}
// }
//
// 这种方法可能仅仅是因为它更明确，或者为了区分同一容器中存在的两个 CacheManager bean，这种方法可能是必要的。
//
// 另请注意上面示例中的 keyGenerator 方法。这允许根据 Spring 的 org.springframework.cache.interceptor.KeyGenerator
// KeyGenerator SPI 自定义缓存键生成策略。通常，@EnableCaching 注解会为此配置 Spring 的 SimpleKeyGenerator，
// 但在实现 CachingConfigurer 接口时，可以指定自定义键生成器。
//
// CachingConfigurer 提供了更多自定义选项：有关详细信息，请参阅 CachingConfigurer 的 Javadoc。
//
// mode 属性控制通知的应用方式：如果 mode 为 AdviceMode.PROXY（默认值），则其他属性控制代理的行为。
// 请注意，代理模式仅允许拦截通过代理的调用；同一类中的本地调用无法通过这种方式拦截。
//
// 请注意，如果 mode 设置为 AdviceMode.ASPECTJ，则 proxyTargetClass 属性的值将被忽略。
// 另请注意，在这种情况下，spring-aspects 模块 JAR 必须位于类路径中，
// 并且需要使用编译时织入或加载时织入将切面应用到受影响的类。在这种情况下，不涉及代理；本地通话也会被拦截。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CachingConfigurationSelector.class)
public @interface EnableCaching {

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}. <strong>
	 * Applicable only if {@link #mode()} is set to {@link AdviceMode#PROXY}</strong>.
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with {@code @Cacheable}.
	 * For example, other beans marked with Spring's {@code @Transactional} annotation will
	 * be upgraded to subclass proxying at the same time. This approach has no negative
	 * impact in practice unless one is explicitly expecting one type of proxy vs another,
	 * e.g. in tests.
	 */
	// 指示是否要创建基于子类（CGLIB）的代理，而不是基于标准 Java 接口的代理。默认值为 {@code false}。
	// <strong>仅当 {@link #mode()} 设置为 {@link AdviceMode#PROXY}</strong> 时适用。
	//
	// <p>请注意，将此属性设置为 {@code true} 将影响所有需要代理的 Spring 管理 bean，而不仅仅是那些标记了 {@code @Cacheable} 的 bean。
	// 例如，其他标记了 Spring 的 {@code @Transactional} 注解的 bean 也将同时升级为子类代理。
	// 除非明确预期使用某种类型的代理（例如在测试中），否则这种方法在实践中不会产生任何负面影响。
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how caching advice should be applied.
	 * <p><b>The default is {@link AdviceMode#PROXY}.</b>
	 * Please note that proxy mode allows for interception of calls through the proxy
	 * only. Local calls within the same class cannot get intercepted that way;
	 * a caching annotation on such a method within a local call will be ignored
	 * since Spring's interceptor does not even kick in for such a runtime scenario.
	 * For a more advanced mode of interception, consider switching this to
	 * {@link AdviceMode#ASPECTJ}.
	 */
	// 指定缓存建议的应用方式。
	//
	// <p><b>默认值为 {@link AdviceMode#PROXY}。</b>请注意，代理模式仅允许拦截通过代理发起的调用。
	// 同一类内的本地调用无法通过此方式拦截；本地调用中此类方法的缓存注解将被忽略，因为 Spring 的拦截器在这种运行时场景下根本不会生效。
	// 如需更高级的拦截模式，请考虑将其切换为 {@link AdviceMode#ASPECTJ}。
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the ordering of the execution of the caching advisor
	 * when multiple advices are applied at a specific joinpoint.
	 * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
	 */
	// 指定在特定连接点应用多个缓存建议时，缓存建议的执行顺序。
	// <p>默认值为 {@link Ordered#LOWEST_PRECEDENCE}。
	int order() default Ordered.LOWEST_PRECEDENCE;

}
