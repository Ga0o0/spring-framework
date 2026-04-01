package org.springframework.sample.cache._mine.cache01_at_enable_caching;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * EnableCaching：启用缓存注解声明
 *
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.annotation.CachingConfigurationSelector
 *
 * @see org.springframework.sample.cache.api.API01_EnableCaching
 */
public class EC01_AtEnableCaching {

	@EnableCaching(mode = AdviceMode.PROXY, proxyTargetClass = false, order = Ordered.LOWEST_PRECEDENCE)
	static class CacheConfig {
		@Bean
		public CacheManager cacheManager() {
			// 配置并返回一个 Spring CacheManager SPI 的实现
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
			return cacheManager;
		}
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);
		context.registerBean(CacheConfig.class);
		context.refresh();
	}
}

/*
********************************* Class API Docs *********************************
启用 Spring 的注解驱动缓存管理功能，类似于 Spring 的 <cache:*> XML 命名空间中的支持。需与 @Configuration 类一起使用，如下所示：

@Configuration
@EnableCaching
class AppConfig {

  @Bean
  MyService myService() {
    // 配置并返回一个具有 @Cacheable 方法的类
    return new MyService();
  }

  @Bean
  CacheManager cacheManager() {
    // 配置并返回一个 Spring CacheManager SPI 的实现
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
    return cacheManager;
  }
}

作为参考，上述示例可以与以下 Spring XML 配置进行比较：

<beans>

  <cache:annotation-driven/>

  <bean id="myService" class="com.foo.MyService"/>

  <bean id="cacheManager" class="org.springframework.cache.support.SimpleCacheManager">
  	<property name="caches">
  		<set>
			<bean class="org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean">
				<property name="name" value="default"/>
			</bean>
		</set>
	 </property>
  </bean>

</beans>

在上述两种情况下，@EnableCaching 和 <cache:annotation-driven/> 都负责注册支持注解驱动缓存管理的必要 Spring 组件，
例如 CacheInterceptor 以及在调用时将拦截器编织到调用栈中的基于代理或 AspectJ 的通知。 @Cacheable 方法会被调用。

如果 JSR-107 API 和 Spring 的 JCache 实现存在，则管理标准缓存注解所需的组件也会被注册。这会创建一个基于代理或 AspectJ 的通知，
当调用带有 CacheResult、CachePut、CacheRemove 或 CacheRemoveAll 注解的方法时，该通知会将拦截器编织到调用栈中。

必须注册一个 CacheManager 类型的 bean，因为框架没有合理的默认值可供使用。
虽然 <cache:annotation-driven> 元素假定存在一个名为 “cacheManager” 的 bean，
但 @EnableCaching 会按类型查找缓存管理器 bean。因此，缓存管理器 bean 方法的命名并不重要。

对于那些希望在 @EnableCaching 和要使用的具体缓存管理器 bean 之间建立更直接关系的用户，
可以实现 CachingConfigurer 回调接口。请注意以下带有 @Override 注解的方法：

@Configuration
@EnableCaching
class AppConfig implements CachingConfigurer {

		@Bean
		MyService myService() {
			// 配置并返回一个具有 @Cacheable 方法的类
			return new MyService();
		}

		@Bean
		@Override
		CacheManager cacheManager() {
			// 配置并返回 Spring CacheManager SPI 的一个实现
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
			return cacheManager;
		}

 	@Override
		KeyGenerator keyGenerator() {
			// 配置并返回 Spring KeyGenerator SPI 的一个实现
			return new MyKeyGenerator();
		}
}

这种方法可能仅仅是因为它更明确，或者为了区分同一容器中存在的两个 CacheManager bean，这种方法可能是必要的。

另请注意上面示例中的 keyGenerator 方法。这允许根据 Spring 的 org.springframework.cache.interceptor.KeyGenerator
KeyGenerator SPI 自定义缓存键生成策略。通常，@EnableCaching 注解会为此配置 Spring 的 SimpleKeyGenerator，
但在实现 CachingConfigurer 接口时，可以指定自定义键生成器。

CachingConfigurer 提供了更多自定义选项：有关详细信息，请参阅 CachingConfigurer 的 Javadoc。

mode 属性控制通知的应用方式：如果 mode 为 AdviceMode.PROXY（默认值），则其他属性控制代理的行为。
请注意，代理模式仅允许拦截通过代理的调用；同一类中的本地调用无法通过这种方式拦截。

请注意，如果 mode 设置为 AdviceMode.ASPECTJ，则 proxyTargetClass 属性的值将被忽略。
另请注意，在这种情况下，spring-aspects 模块 JAR 必须位于类路径中，
并且需要使用编译时织入或加载时织入将切面应用到受影响的类。在这种情况下，不涉及代理；本地通话也会被拦截。

********************************* Class Definition *********************************
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CachingConfigurationSelector.class)
public @interface EnableCaching {
	// 指示是否要创建基于子类（CGLIB）的代理，而不是基于标准 Java 接口的代理。默认值为 {@code false}。
	// <strong>仅当 {@link #mode()} 设置为 {@link AdviceMode#PROXY}</strong> 时适用。
	//
	// <p>请注意，将此属性设置为 {@code true} 将影响所有需要代理的 Spring 管理 bean，而不仅仅是那些标记了 {@code @Cacheable} 的 bean。
	// 例如，其他标记了 Spring 的 {@code @Transactional} 注解的 bean 也将同时升级为子类代理。
	// 除非明确预期使用某种类型的代理（例如在测试中），否则这种方法在实践中不会产生任何负面影响。
	boolean proxyTargetClass() default false;

	// 指定缓存建议的应用方式。
	//
	// <p><b>默认值为 {@link AdviceMode#PROXY}。</b>请注意，代理模式仅允许拦截通过代理发起的调用。
	// 同一类内的本地调用无法通过此方式拦截；本地调用中此类方法的缓存注解将被忽略，因为 Spring 的拦截器在这种运行时场景下根本不会生效。
	// 如需更高级的拦截模式，请考虑将其切换为 {@link AdviceMode#ASPECTJ}。
	AdviceMode mode() default AdviceMode.PROXY;

	// 指定在特定连接点应用多个缓存建议时，缓存建议的执行顺序。
	// <p>默认值为 {@link Ordered#LOWEST_PRECEDENCE}。
	int order() default Ordered.LOWEST_PRECEDENCE;
}
**/
