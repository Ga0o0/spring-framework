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

import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySource.StubPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;

/**
 * {@link Environment} implementation to be used by {@code Servlet}-based web
 * applications. All web-related (servlet-based) {@code ApplicationContext} classes
 * initialize an instance by default.
 *
 * <p>Contributes {@code ServletConfig}, {@code ServletContext}, and JNDI-based
 * {@link PropertySource} instances. See {@link #customizePropertySources} method
 * documentation for details.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see StandardEnvironment
 */
// {@link Environment} 实现，供基于 {@code Servlet} 的 Web 应用使用。
// 所有与 Web 相关的（基于 Servlet 的）{@code ApplicationContext} 类默认都会初始化一个实例。
//
// <p>提供 {@code ServletConfig}、{@code ServletContext} 和基于 JNDI 的 {@link PropertySource} 实例。
// 详情请参阅 {@link #customizePropertySources} 方法文档。
public class StandardServletEnvironment extends StandardEnvironment implements ConfigurableWebEnvironment {

	/** Servlet context init parameters property source name: {@value}. */
	// Servlet 上下文初始化参数属性源名称：{@value}
	public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams";

	/** Servlet config init parameters property source name: {@value}. */
	// Servlet 配置初始化参数属性源名称：{@value}。
	public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams";

	/** JNDI property source name: {@value}. */
	// JNDI 属性源名称：{@value}。
	public static final String JNDI_PROPERTY_SOURCE_NAME = "jndiProperties";


	// Defensive reference to JNDI API for JDK 9+ (optional java.naming module)
	// --> 译文：对 JDK 9+ 的 JNDI API 的防御性引用（可选的 java.naming 模块）
	private static final boolean jndiPresent = ClassUtils.isPresent(
			"javax.naming.InitialContext", StandardServletEnvironment.class.getClassLoader());


	/**
	 * Create a new {@code StandardServletEnvironment} instance.
	 */
	// 创建一个新的 {@code StandardServletEnvironment} 实例。
	public StandardServletEnvironment() {
	}

	/**
	 * Create a new {@code StandardServletEnvironment} instance with a specific {@link MutablePropertySources} instance.
	 * @param propertySources property sources to use
	 * @since 5.3.4
	 */
	// 创建一个新的 {@code StandardServletEnvironment} 实例，并指定一个特定的 {@link MutablePropertySources} 实例。
	// @param propertySources 要使用的属性源
	protected StandardServletEnvironment(MutablePropertySources propertySources) {
		super(propertySources);
	}


	/**
	 * Customize the set of property sources with those contributed by superclasses as
	 * well as those appropriate for standard servlet-based environments:
	 * <ul>
	 * <li>{@value #SERVLET_CONFIG_PROPERTY_SOURCE_NAME}
	 * <li>{@value #SERVLET_CONTEXT_PROPERTY_SOURCE_NAME}
	 * <li>{@value #JNDI_PROPERTY_SOURCE_NAME}
	 * </ul>
	 * <p>Properties present in {@value #SERVLET_CONFIG_PROPERTY_SOURCE_NAME} will
	 * take precedence over those in {@value #SERVLET_CONTEXT_PROPERTY_SOURCE_NAME}, and
	 * properties found in either of the above take precedence over those found in
	 * {@value #JNDI_PROPERTY_SOURCE_NAME}.
	 * <p>Properties in any of the above will take precedence over system properties and
	 * environment variables contributed by the {@link StandardEnvironment} superclass.
	 * <p>The {@code Servlet}-related property sources are added as
	 * {@link StubPropertySource stubs} at this stage, and will be
	 * {@linkplain #initPropertySources(ServletContext, ServletConfig) fully initialized}
	 * once the actual {@link ServletContext} object becomes available.
	 * <p>Addition of {@value #JNDI_PROPERTY_SOURCE_NAME} can be disabled with
	 * {@link JndiLocatorDelegate#IGNORE_JNDI_PROPERTY_NAME}.
	 * @see StandardEnvironment#customizePropertySources
	 * @see org.springframework.core.env.AbstractEnvironment#customizePropertySources
	 * @see ServletConfigPropertySource
	 * @see ServletContextPropertySource
	 * @see org.springframework.jndi.JndiPropertySource
	 * @see org.springframework.context.support.AbstractApplicationContext#initPropertySources
	 * @see #initPropertySources(ServletContext, ServletConfig)
	 */
	// 使用超类提供的属性源以及适用于基于标准 servlet 的环境的属性源自定义属性源集：
	// <ul>
	// 		<li>{@value #SERVLET_CONFIG_PROPERTY_SOURCE_NAME}
	// 		<li>{@value #SERVLET_CONTEXT_PROPERTY_SOURCE_NAME}
	// 		<li>{@value #JNDI_PROPERTY_SOURCE_NAME}
	// </ul>
	// <p>{@value #SERVLET_CONFIG_PROPERTY_SOURCE_NAME} 中的属性优先于 {@value #SERVLET_CONTEXT_PROPERTY_SOURCE_NAME} 中的属性，
	// 且上述任一属性中的属性优先于 {@value #JNDI_PROPERTY_SOURCE_NAME} 中的属性。
	//
	// <p>上述任何属性均优先于 {@link StandardEnvironment} 超类提供的系统属性和环境变量。
	//
	// <p>在此阶段，与 {@code Servlet} 相关的属性源将作为 {@link StubPropertySource 存根} 添加，
	// 一旦实际的 {@link ServletContext} 对象可用，
	// 它将被 {@linkplain #initPropertySources(ServletContext, ServletConfig) 完全初始化。
	//
	// <p>可以使用 {@link JndiLocatorDelegate#IGNORE_JNDI_PROPERTY_NAME} 禁用 {@value #JNDI_PROPERTY_SOURCE_NAME} 的添加。
	@Override
	protected void customizePropertySources(MutablePropertySources propertySources) {
		propertySources.addLast(new StubPropertySource(SERVLET_CONFIG_PROPERTY_SOURCE_NAME));
		propertySources.addLast(new StubPropertySource(SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
		if (jndiPresent && JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
			propertySources.addLast(new JndiPropertySource(JNDI_PROPERTY_SOURCE_NAME));
		}
		super.customizePropertySources(propertySources);
	}

	@Override
	public void initPropertySources(@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {
		WebApplicationContextUtils.initServletPropertySources(getPropertySources(), servletContext, servletConfig);
	}

}
