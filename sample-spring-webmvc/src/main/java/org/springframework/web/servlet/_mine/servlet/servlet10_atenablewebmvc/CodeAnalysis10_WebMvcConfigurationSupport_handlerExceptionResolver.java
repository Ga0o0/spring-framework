package org.springframework.web.servlet._mine.servlet.servlet10_atenablewebmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * WebMvcConfigurer 中的配置如何加载到应用中 - 示例 -> 加载 WebMvcConfigurer 实例中的 HandlerExceptionResolver
 *
 * ## 1. WebMvcConfigurationSupport 中 @Bean - WebMvcConfigurer 加载 HandlerExceptionResolver  -> WebMvcConfigurationSupport#handlerExceptionResolver(...)
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#configureHandlerExceptionResolvers(java.util.List)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite#configureHandlerExceptionResolvers(java.util.List)
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#configureHandlerExceptionResolvers(java.util.List)
 */
public class CodeAnalysis10_WebMvcConfigurationSupport_handlerExceptionResolver {

	/**
	 * @see @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
	 */
	// public class WebMvcConfigurationSupport implements ApplicationContextAware, ServletContextAware {
	static class CA03_WebMvcConfigurationSupport extends WebMvcConfigurationSupport {
		// ...
		@Bean
		public HandlerExceptionResolver handlerExceptionResolver(
				@Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager) {
			List<HandlerExceptionResolver> exceptionResolvers = new ArrayList<>();
			// -> DelegatingWebMvcConfiguration#configureHandlerExceptionResolvers(java.util.List)
			configureHandlerExceptionResolvers(exceptionResolvers); // important -> go
			if (exceptionResolvers.isEmpty()) {
				addDefaultHandlerExceptionResolvers(exceptionResolvers, contentNegotiationManager);
			}
			extendHandlerExceptionResolvers(exceptionResolvers);
			HandlerExceptionResolverComposite composite = new HandlerExceptionResolverComposite();
			composite.setOrder(0);
			composite.setExceptionResolvers(exceptionResolvers);
			return composite;
		}
		// ...
	}

	/**
	 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
	 */
	// public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport { ... }
	@Configuration(proxyBeanMethods = false)
	static class CA02_DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
		// private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
		private final CA03_WebMvcConfigurerComposite configurers = new CA03_WebMvcConfigurerComposite();

		@Autowired(required = false)
		public void setConfigurers(List<WebMvcConfigurer> configurers) {
			if (!CollectionUtils.isEmpty(configurers)) {
				this.configurers.addWebMvcConfigurers(configurers);
			}
		}
 		// ...

		@Override
		protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
			this.configurers.configureHandlerExceptionResolvers(exceptionResolvers); // important -> go
		}
		// ...
	}

	/**
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
	 */
	interface WebMvcConfigurer {
		default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {} // important -> go
	}

	/**
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite
	 */
	// 一个 {@link WebMvcConfigurer}，它将委托给一个或多个其他配置器。
	// static class WebMvcConfigurerComposite implements WebMvcConfigurer {
	static class CA03_WebMvcConfigurerComposite implements WebMvcConfigurer {
		private final List<WebMvcConfigurer> delegates = new ArrayList<>();
		public void addWebMvcConfigurers(List<WebMvcConfigurer> configurers) {
			if (!CollectionUtils.isEmpty(configurers)) {
				this.delegates.addAll(configurers);
			}
		}
		// ....
	}

}
