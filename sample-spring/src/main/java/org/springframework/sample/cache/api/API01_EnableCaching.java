package org.springframework.sample.cache.api;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * EnableCaching
 *
 * @see EnableCaching
 */
public class API01_EnableCaching {

	static class MyService {}

	// 启用 Spring 的注解驱动缓存管理功能，类似于 Spring 的 <cache:*> XML 命名空间中的支持。
	// 需与 @Configuration 类一起使用，如下所示：
	@Configuration
	@EnableCaching
	static class AppConfig1 {

		@Bean
		public MyService myService() {
			// 配置并返回一个具有 @Cacheable 方法的类
			return new MyService();
		}

		@Bean
		public CacheManager cacheManager() {
			// 配置并返回一个 Spring CacheManager SPI 的实现
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
			return cacheManager;
		}
	}

	// 作为参考，上述示例可以与以下 Spring XML 配置进行比较：
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

	// 对于那些希望在 @EnableCaching 和要使用的具体缓存管理器 bean 之间建立更直接关系的用户，
	// 可以实现 CachingConfigurer 回调接口。请注意以下带有 @Override 注解的方法：

	@Configuration
	@EnableCaching
	static class AppConfig2 implements CachingConfigurer {

		@Bean
		public MyService myService() {
			// 配置并返回一个具有 @Cacheable 方法的类
			return new MyService();
		}

		@Bean
		@Override
		public CacheManager cacheManager() {
			// 配置并返回 Spring CacheManager SPI 的一个实现
			SimpleCacheManager cacheManager = new SimpleCacheManager();
			cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));
			return cacheManager;
		}

		@Override
		public KeyGenerator keyGenerator() {
			// 配置并返回 Spring KeyGenerator SPI 的一个实现
			return new SimpleKeyGenerator();
		}
	}

}
