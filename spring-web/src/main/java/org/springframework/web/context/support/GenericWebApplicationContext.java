/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.web.context.support;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ServletContextAware;

/**
 * Subclass of {@link GenericApplicationContext}, suitable for web environments.
 *
 * <p>Implements {@link ConfigurableWebApplicationContext}, but is not intended for
 * declarative setup in {@code web.xml}. Instead, it is designed for programmatic setup,
 * for example for building nested contexts or for use within
 * {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializers}.
 *
 * <p>Interprets resource paths as servlet context resources, i.e. as paths beneath
 * the web application root. Absolute paths &mdash; for example, for files outside
 * the web app root &mdash; can be accessed via {@code file:} URLs, as implemented
 * by {@code AbstractApplicationContext}.
 *
 * <p>In addition to the special beans detected by
 * {@link org.springframework.context.support.AbstractApplicationContext AbstractApplicationContext},
 * this class detects a {@link ThemeSource} bean in the context, with the name "themeSource".
 * Theme support is deprecated as of 6.0 with no direct replacement.
 *
 * <p>If you wish to register annotated <em>component classes</em> with a
 * {@code GenericWebApplicationContext}, you can use an
 * {@link org.springframework.context.annotation.AnnotatedBeanDefinitionReader
 * AnnotatedBeanDefinitionReader}, as demonstrated in the following example.
 * Component classes include in particular
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * classes but also plain {@link org.springframework.stereotype.Component @Component}
 * classes as well as JSR-330 compliant classes using {@code jakarta.inject} annotations.
 *
 * <pre class="code">
 * GenericWebApplicationContext context = new GenericWebApplicationContext();
 * AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(context);
 * reader.register(AppConfig.class, UserController.class, UserRepository.class);</pre>
 *
 * <p>If you intend to implement a {@code WebApplicationContext} that reads bean definitions
 * from configuration files, consider deriving from {@link AbstractRefreshableWebApplicationContext},
 * reading the bean definitions in an implementation of the {@code loadBeanDefinitions}
 * method.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @since 1.2
 */
// {@link GenericApplicationContext} 的子类，适用于 Web 环境。
//
// <p>实现了 {@link ConfigurableWebApplicationContext}，但不适用于在 {@code web.xml} 中进行声明式设置。
// 相反，它旨在通过编程方式进行设置，例如构建嵌套上下文或在 {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializers} 中使用。
//
// <p>将资源路径解释为 Servlet 上下文资源，即 Web 应用程序根目录下的路径。
// 绝对路径（例如，Web 应用程序根目录之外的文件）可以通过 {@code file:} URL 访问，
// 正如 {@code AbstractApplicationContext} 所实现的那样。
//
// <p>除了 {@link org.springframework.context.support.AbstractApplicationContext AbstractApplicationContext} 检测到的
// 特殊 bean 之外，此类还会检测上下文中名为“themeSource”的 {@link ThemeSource} bean。
// 从 6.0 开始，主题支持已弃用，并且没有直接替代品。
//
// <p>如果您希望使用 {@code GenericWebApplicationContext} 注册带注解的<em>组件类</em>，
// 可以使用 {@link org.springframework.context.annotation.AnnotatedBeanDefinitionReader AnnotatedBeanDefinitionReader}，如下例所示。
// 组件类尤其包括 {@link org.springframework.context.annotation.Configuration @Configuration} 类，
// 但也包括普通的 {@link org.springframework.stereotype.Component @Component} 类
// 以及使用 {@code jakarta.inject} 注解的符合 JSR-330 标准的类。
//
// <pre class="code">
// GenericWebApplicationContext context = new GenericWebApplicationContext();
// AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(context);
// reader.register(AppConfig.class, UserController.class, UserRepository.class);
// </pre>
//
// <p>如果您打算实现从配置文件读取 bean 定义的 {@code WebApplicationContext}，
// 请考虑从 {@link AbstractRefreshableWebApplicationContext} 派生，
// 并在 {@code loadBeanDefinitions} 方法的实现中读取 bean 定义。
@SuppressWarnings("deprecation")
public class GenericWebApplicationContext extends GenericApplicationContext
		implements ConfigurableWebApplicationContext, ThemeSource {

	@Nullable
	private ServletContext servletContext;

	@Nullable
	private ThemeSource themeSource;


	/**
	 * Create a new {@code GenericWebApplicationContext}.
	 * @see #setServletContext
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericWebApplicationContext() {
		super();
	}

	/**
	 * Create a new {@code GenericWebApplicationContext} for the given {@link ServletContext}.
	 * @param servletContext the {@code ServletContext} to run in
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericWebApplicationContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Create a new {@code GenericWebApplicationContext} with the given {@link DefaultListableBeanFactory}.
	 * @param beanFactory the {@code DefaultListableBeanFactory} instance to use for this context
	 * @see #setServletContext
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericWebApplicationContext(DefaultListableBeanFactory beanFactory) {
		super(beanFactory);
	}

	/**
	 * Create a new {@code GenericWebApplicationContext} with the given {@link DefaultListableBeanFactory}
	 * and {@link ServletContext}.
	 * @param beanFactory the {@code DefaultListableBeanFactory} instance to use for this context
	 * @param servletContext the {@code ServletContext} to run in
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericWebApplicationContext(DefaultListableBeanFactory beanFactory, ServletContext servletContext) {
		super(beanFactory);
		this.servletContext = servletContext;
	}


	/**
	 * Set the {@link ServletContext} that this {@code WebApplicationContext} runs in.
	 */
	@Override
	public void setServletContext(@Nullable ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	@Nullable
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public String getApplicationName() {
		return (this.servletContext != null ? this.servletContext.getContextPath() : "");
	}

	/**
	 * Create and return a new {@link StandardServletEnvironment}.
	 */
	@Override
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}

	/**
	 * Register request/session scopes, environment beans, a {@link ServletContextAwareProcessor}, etc.
	 */
	@Override
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		if (this.servletContext != null) {
			beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
			beanFactory.ignoreDependencyInterface(ServletContextAware.class);
		}
		WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
		WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext);
	}

	/**
	 * This implementation supports file paths beneath the root of the {@link ServletContext}.
	 * @see ServletContextResource
	 */
	@Override
	protected Resource getResourceByPath(String path) {
		Assert.state(this.servletContext != null, "No ServletContext available");
		return new ServletContextResource(this.servletContext, path);
	}

	/**
	 * This implementation supports pattern matching in unexpanded WARs too.
	 * @see ServletContextResourcePatternResolver
	 */
	@Override
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new ServletContextResourcePatternResolver(this);
	}

	/**
	 * Initialize the theme capability.
	 */
	@Override
	protected void onRefresh() {
		this.themeSource = UiApplicationContextUtils.initThemeSource(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>Replace {@code Servlet}-related property sources.
	 */
	@Override
	protected void initPropertySources() {
		ConfigurableEnvironment env = getEnvironment();
		if (env instanceof ConfigurableWebEnvironment configurableWebEnv) {
			configurableWebEnv.initPropertySources(this.servletContext, null);
		}
	}

	@Override
	@Nullable
	@Deprecated
	public Theme getTheme(String themeName) {
		Assert.state(this.themeSource != null, "No ThemeSource available");
		return this.themeSource.getTheme(themeName);
	}


	// ---------------------------------------------------------------------
	// Pseudo-implementation of ConfigurableWebApplicationContext
	// ---------------------------------------------------------------------

	@Override
	public void setServletConfig(@Nullable ServletConfig servletConfig) {
		// no-op
	}

	@Override
	@Nullable
	public ServletConfig getServletConfig() {
		throw new UnsupportedOperationException(
				"GenericWebApplicationContext does not support getServletConfig()");
	}

	@Override
	public void setNamespace(@Nullable String namespace) {
		// no-op
	}

	@Override
	@Nullable
	public String getNamespace() {
		throw new UnsupportedOperationException(
				"GenericWebApplicationContext does not support getNamespace()");
	}

	@Override
	public void setConfigLocation(String configLocation) {
		if (StringUtils.hasText(configLocation)) {
			throw new UnsupportedOperationException(
					"GenericWebApplicationContext does not support setConfigLocation(). " +
					"Do you still have a 'contextConfigLocation' init-param set?");
		}
	}

	@Override
	public void setConfigLocations(String... configLocations) {
		if (!ObjectUtils.isEmpty(configLocations)) {
			throw new UnsupportedOperationException(
					"GenericWebApplicationContext does not support setConfigLocations(). " +
					"Do you still have a 'contextConfigLocations' init-param set?");
		}
	}

	@Override
	public String[] getConfigLocations() {
		throw new UnsupportedOperationException(
				"GenericWebApplicationContext does not support getConfigLocations()");
	}

}
