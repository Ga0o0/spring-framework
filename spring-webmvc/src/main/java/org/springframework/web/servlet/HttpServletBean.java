/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * Simple extension of {@link jakarta.servlet.http.HttpServlet} which treats
 * its config parameters ({@code init-param} entries within the
 * {@code servlet} tag in {@code web.xml}) as bean properties.
 *
 * <p>A handy superclass for any type of servlet. Type conversion of config
 * parameters is automatic, with the corresponding setter method getting
 * invoked with the converted value. It is also possible for subclasses to
 * specify required properties. Parameters without matching bean property
 * setter will simply be ignored.
 *
 * <p>This servlet leaves request handling to subclasses, inheriting the default
 * behavior of HttpServlet ({@code doGet}, {@code doPost}, etc).
 *
 * <p>This generic servlet base class has no dependency on the Spring
 * {@link org.springframework.context.ApplicationContext} concept. Simple
 * servlets usually don't load their own context but rather access service
 * beans from the Spring root application context, accessible via the
 * filter's {@link #getServletContext() ServletContext} (see
 * {@link org.springframework.web.context.support.WebApplicationContextUtils}).
 *
 * <p>The {@link FrameworkServlet} class is a more specific servlet base
 * class which loads its own application context. FrameworkServlet serves
 * as direct base class of Spring's full-fledged {@link DispatcherServlet}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #addRequiredProperty
 * @see #initServletBean
 * @see #doGet
 * @see #doPost
 */
// 这是 {@link jakarta.servlet.http.HttpServlet} 的一个简单扩展，
// 它将配置参数（{@code web.xml} 中 {@code servlet} 标签内的 {@code init-param} 条目）视为 bean 属性。
//
// <p>这是一个适用于任何类型 servlet 的便捷超类。配置参数的类型转换是自动的，
// 相应的 setter 方法会使用转换后的值进行调用。子类也可以指定所需的属性。没有匹配 bean 属性 setter 的参数将被忽略。
//
// <p>此 servlet 将请求处理留给子类，继承了 HttpServlet 的默认行为（{@code doGet}、{@code doPost} 等）。
//
// <p>这个通用 servlet 基类不依赖于 Spring 的 {@link org.springframework.context.ApplicationContext} 概念。
// 简单的 Servlet 通常不会加载自己的上下文，而是从 Spring 根应用程序上下文中访问服务 bean，
// 可通过过滤器的 {@link #getServletContext() ServletContext} 方法访问
// （参见 `@link org.springframework.web.context.support.WebApplicationContextUtils`）。
//
// {@link FrameworkServlet} 类是一个更具体的 Servlet 基类，它会加载自己的应用程序上下文。
// {@link DispatcherServlet} 是 Spring 功能齐全的 Servlet 的直接基类。`
@SuppressWarnings("serial")
public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private ConfigurableEnvironment environment;

	private final Set<String> requiredProperties = new HashSet<>(4);


	/**
	 * Subclasses can invoke this method to specify that this property
	 * (which must match a JavaBean property they expose) is mandatory,
	 * and must be supplied as a config parameter. This should be called
	 * from the constructor of a subclass.
	 * <p>This method is only relevant in case of traditional initialization
	 * driven by a ServletConfig instance.
	 * @param property name of the required property
	 */
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}

	/**
	 * Set the {@code Environment} that this servlet runs in.
	 * <p>Any environment set here overrides the {@link StandardServletEnvironment}
	 * provided by default.
	 * @throws IllegalArgumentException if environment is not assignable to
	 * {@code ConfigurableEnvironment}
	 */
	@Override
	public void setEnvironment(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "ConfigurableEnvironment required");
		this.environment = (ConfigurableEnvironment) environment;
	}

	/**
	 * Return the {@link Environment} associated with this servlet.
	 * <p>If none specified, a default environment will be initialized via
	 * {@link #createEnvironment()}.
	 */
	// 返回与此 servlet 关联的 {@link Environment}。
	// <p>如果未指定，则将通过 {@link #createEnvironment()} 初始化默认环境。
	@Override
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = createEnvironment();
		}
		return this.environment;
	}

	/**
	 * Create and return a new {@link StandardServletEnvironment}.
	 * <p>Subclasses may override this in order to configure the environment or
	 * specialize the environment type returned.
	 */
	// 创建并返回一个新的 {@link StandardServletEnvironment}。
	// <p>子类可以重写此方法，以配置环境或特化返回的环境类型。
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}

	/**
	 * Map config parameters onto bean properties of this servlet, and
	 * invoke subclass initialization.
	 * @throws ServletException if bean properties are invalid (or required
	 * properties are missing), or if subclass initialization fails.
	 */
	// 将配置参数映射到此 servlet 的 bean 属性上，并调用子类初始化。
	// @throws ServletException 如果 bean 属性无效（或缺少必需属性），或者子类初始化失败
	@Override
	public final void init() throws ServletException {

		// Set bean properties from init parameters. --> 译文：从初始化参数设置 bean 属性。
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties); // 创建新的 ServletConfigPropertyValues。
		if (!pvs.isEmpty()) {
			try {
				// 获取给定目标对象的 BeanWrapper，以 JavaBean 风格访问属性。
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
				// 创建一个新的 ServletContextResourceLoader。
				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
				// 为指定类型的所有属性注册指定的自定义属性编辑器。
				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
				initBeanWrapper(bw); // 此默认实现为空。
				bw.setPropertyValues(pvs, true); // 执行批量更新，更好地控制行为。
			}
			catch (BeansException ex) {
				// 无法在 servlet 上设置 bean 属性
				if (logger.isErrorEnabled()) {
					logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
				}
				throw ex;
			}
		}

		// Let subclasses do whatever initialization they like. --> 译文：让子类做任何它们喜欢的初始化。
		// 子类可以重写此方法以执行自定义初始化。此 servlet 的所有 bean 属性都将在调用此方法之前设置。<p>此默认实现为空。
		initServletBean();
	}

	/**
	 * Initialize the BeanWrapper for this HttpServletBean,
	 * possibly with custom editors.
	 * <p>This default implementation is empty.
	 * @param bw the BeanWrapper to initialize
	 * @throws BeansException if thrown by BeanWrapper methods
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	// 初始化此 HttpServletBean 的 BeanWrapper，可能包含自定义编辑器。
	// <p>此默认实现为空。
	// @param bw 要初始化的 BeanWrapper
	// @throws BeanWrapper 方法抛出的异常（如果抛出 BeansException）
	// @see org.springframework.beans.BeanWrapper#registerCustomEditor 
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}

	/**
	 * Subclasses may override this to perform custom initialization.
	 * All bean properties of this servlet will have been set before this
	 * method is invoked.
	 * <p>This default implementation is empty.
	 * @throws ServletException if subclass initialization fails
	 */
	// 子类可以重写此方法以执行自定义初始化。此 servlet 的所有 bean 属性都将在调用此方法之前设置。
	// <p>此默认实现为空。
	// @throws ServletException 如果子类初始化失败
	protected void initServletBean() throws ServletException {
	}

	/**
	 * Overridden method that simply returns {@code null} when no
	 * ServletConfig set yet.
	 * @see #getServletConfig()
	 */
	@Override
	@Nullable
	public String getServletName() {
		return (getServletConfig() != null ? getServletConfig().getServletName() : null);
	}


	/**
	 * PropertyValues implementation created from ServletConfig init parameters.
	 */
	// 从 ServletConfig 初始化参数创建的 PropertyValues 实现。
	private static class ServletConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new ServletConfigPropertyValues.
		 * @param config the ServletConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 * we can't accept default values
		 * @throws ServletException if any required properties are missing
		 */
		// 创建新的 ServletConfigPropertyValues。
		// @param config 我们将用来获取 PropertyValues 的 ServletConfig。
		// @param requiredProperties 我们需要的属性名称集合，不能接受默认值。
		// @throws ServletException 如果缺少任何必需的属性
		public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties)
				throws ServletException {

			Set<String> missingProps = (!CollectionUtils.isEmpty(requiredProperties) ?
					new HashSet<>(requiredProperties) : null);

			Enumeration<String> paramNames = config.getInitParameterNames();
			while (paramNames.hasMoreElements()) {
				String property = paramNames.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// Fail if we are still missing properties.
			if (!CollectionUtils.isEmpty(missingProps)) {
				throw new ServletException(
						"Initialization from ServletConfig for servlet '" + config.getServletName() +
						"' failed; the following required properties were missing: " +
						StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
