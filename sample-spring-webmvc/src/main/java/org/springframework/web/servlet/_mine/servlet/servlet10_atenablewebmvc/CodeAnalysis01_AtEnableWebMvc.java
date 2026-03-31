package org.springframework.web.servlet._mine.servlet.servlet10_atenablewebmvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
 *
 * ## 1. WebMvcConfigurer 通过 @Autowired 注入到 DelegatingWebMvcConfiguration#configurers
 *
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#configurers
 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#setConfigurers(java.util.List)
 */
public class CodeAnalysis01_AtEnableWebMvc {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Documented
	@Import(DelegatingWebMvcConfiguration.class)
	public @interface EnableWebMvc {}

	/**
	 * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
	 */
	// public class DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport { ... }
	@Configuration(proxyBeanMethods = false)
	static class CA01_DelegatingWebMvcConfiguration extends WebMvcConfigurationSupport {
		// private final WebMvcConfigurerComposite configurers = new WebMvcConfigurerComposite();
		private final CA02_WebMvcConfigurerComposite configurers = new CA02_WebMvcConfigurerComposite();

		@Autowired(required = false)
		public void setConfigurers(List<WebMvcConfigurer> configurers) { // important -> go
			if (!CollectionUtils.isEmpty(configurers)) {
				this.configurers.addWebMvcConfigurers(configurers);
			}
		}
	}

	/**
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite
	 */
	// 一个 {@link WebMvcConfigurer}，它将委托给一个或多个其他配置器。
	// static class WebMvcConfigurerComposite implements WebMvcConfigurer {
	static class CA02_WebMvcConfigurerComposite implements WebMvcConfigurer {
		private final List<WebMvcConfigurer> delegates = new ArrayList<>();

		public void addWebMvcConfigurers(List<WebMvcConfigurer> configurers) { // important -> go
			if (!CollectionUtils.isEmpty(configurers)) {
				this.delegates.addAll(configurers);
			}
		}
		// ....
	}

}
