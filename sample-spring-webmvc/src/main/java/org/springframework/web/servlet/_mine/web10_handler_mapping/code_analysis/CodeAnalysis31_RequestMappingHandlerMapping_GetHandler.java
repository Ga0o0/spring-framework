package org.springframework.web.servlet._mine.web10_handler_mapping.code_analysis;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * RequestMappingHandlerMapping
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 *
 * RequestMappingHandlerMapping - Get Handler -> RequestMappingHandlerMapping#getHandler(...)
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 1. 查找给定请求的 handler，如果未找到，则返回 null
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerInternal(jakarta.servlet.http.HttpServletRequest)
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#initLookupPath(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod(java.lang.String, jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.method.HandlerMethod#createWithResolvedBean()
 *
 * ## 2. 为给定的 handler 构建一个 HandlerExecutionChain，包括适用的拦截器
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandlerExecutionChain(java.lang.Object, jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerExecutionChain#HandlerExecutionChain(java.lang.Object)
 * @see org.springframework.web.servlet.HandlerExecutionChain#addInterceptor(org.springframework.web.servlet.HandlerInterceptor)
 *
 * ## 3. Cors 处理
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getCorsHandlerExecutionChain(jakarta.servlet.http.HttpServletRequest, org.springframework.web.servlet.HandlerExecutionChain, org.springframework.web.cors.CorsConfiguration)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping.PreFlightHandler#PreFlightHandler(org.springframework.web.cors.CorsConfiguration)
 * @see org.springframework.web.servlet.HandlerExecutionChain.addInterceptor(int, org.springframework.web.servlet.HandlerInterceptor)
 * @see org.springframework.web.servlet.HandlerExecutionChain.HandlerExecutionChain(java.lang.Object, org.springframework.web.servlet.HandlerInterceptor...)
 */
public class CodeAnalysis31_RequestMappingHandlerMapping_GetHandler {

	// public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
	//		implements MatchableHandlerMapping, EmbeddedValueResolverAware { ... }
	static abstract class CA01_RequestMappingHandlerMapping extends CA04_RequestMappingInfoHandlerMapping {
		private final MappingRegistry mappingRegistry = new MappingRegistry();

		// 查找给定请求的处理程序方法。
		@Override
		@Nullable
		protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
			// 初始化用于请求映射的路径。
			String lookupPath = initLookupPath(request);
			this.mappingRegistry.acquireReadLock();
			try {
				// 查找与当前请求最匹配的处理程序方法。如果找到多个匹配项，则选择最佳匹配项。
				HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
				// handlerMethod.createWithResolvedBean() -> 如果提供的实例包含 bean 名称而不是对象实例，则在创建和返回 {@link HandlerMethod} 之前解析 bean 名称。
				return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
			} finally {
				this.mappingRegistry.releaseReadLock();
			}
		}
	}

/*	static class CA04_HandlerMethod {
		// 如果提供的实例包含 bean 名称而不是对象实例，则在创建和返回 {@link HandlerMethod} 之前解析 bean 名称。
		public HandlerMethod createWithResolvedBean() {
			Object handler = this.bean;
			if (this.bean instanceof String beanName) {
				// 没有 BeanFactory 就无法解析 bean 名称
				Assert.state(this.beanFactory != null, "Cannot resolve bean name without BeanFactory");
				handler = this.beanFactory.getBean(beanName);
			}
			Assert.notNull(handler, "No handler instance");
			return new HandlerMethod(this, handler, false);
		}
	}*/

	/**
	 * @see AbstractHandlerMethodMapping
	 */
	// public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean { ... }
	static abstract class CA03_AbstractHandlerMethodMapping<T> extends AbstractHandlerMethodMapping<T> {
		// 一个注册表，用于维护所有到处理程序方法的映射，公开执行查找的方法并提供并发访问。
		//
		// <p>用于测试目的的包私有。
		class MappingRegistry {
			private final Map<T, MappingRegistration<T>> registry = new HashMap<>();
			private final MultiValueMap<String, T> pathLookup = new LinkedMultiValueMap<>();
			private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();
			private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();
			private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
			// 使用 getMappings 和 getMappingsByUrl 时获取读锁。
			public void acquireReadLock() {
				this.readWriteLock.readLock().lock();
			}
			public void releaseReadLock() {
				this.readWriteLock.readLock().unlock();
			}
		}
	}

	/**
	 * RequestMappingInfoHandlerMapping
	 *
	 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
	 */
	// public abstract class RequestMappingInfoHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {
	static abstract class CA04_RequestMappingInfoHandlerMapping extends CA03_AbstractHandlerMethodMapping<RequestMappingInfo> {

	}

	static class MappingRegistration<T> {
		private final T mapping;
		private final HandlerMethod handlerMethod;
		private final Set<String> directPaths;
		private final String mappingName;
		private final boolean corsConfig;

		public MappingRegistration(T mapping, HandlerMethod handlerMethod,
								   @Nullable Set<String> directPaths, @Nullable String mappingName, boolean corsConfig) {

			Assert.notNull(mapping, "Mapping must not be null");
			Assert.notNull(handlerMethod, "HandlerMethod must not be null");
			this.mapping = mapping;
			this.handlerMethod = handlerMethod;
			this.directPaths = (directPaths != null ? directPaths : Collections.emptySet());
			this.mappingName = mappingName;
			this.corsConfig = corsConfig;
		}

		public T getMapping() {
			return this.mapping;
		}

		public HandlerMethod getHandlerMethod() {
			return this.handlerMethod;
		}

		public Set<String> getDirectPaths() {
			return this.directPaths;
		}

		@Nullable
		public String getMappingName() {
			return this.mappingName;
		}

		public boolean hasCorsConfig() {
			return this.corsConfig;
		}
	}

}
