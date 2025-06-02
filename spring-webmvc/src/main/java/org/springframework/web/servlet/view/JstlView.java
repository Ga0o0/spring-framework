/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.servlet.view;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Specialization of {@link InternalResourceView} for JSTL pages,
 * i.e. JSP pages that use the JSP Standard Tag Library.
 *
 * <p>Exposes JSTL-specific request attributes specifying locale
 * and resource bundle for JSTL's formatting and message tags,
 * using Spring's locale and {@link org.springframework.context.MessageSource}.
 *
 * <p>Typical usage with {@link InternalResourceViewResolver} would look as follows,
 * from the perspective of the DispatcherServlet context definition:
 *
 * <pre class="code">
 * <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
 *   <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
 *   <property name="prefix" value="/WEB-INF/jsp/"/>
 *   <property name="suffix" value=".jsp"/>
 * </bean>
 *
 * <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
 *   <property name="basename" value="messages"/>
 * </bean></pre>
 *
 * Every view name returned from a handler will be translated to a JSP
 * resource (for example: "myView" &rarr; "/WEB-INF/jsp/myView.jsp"), using
 * this view class to enable explicit JSTL support.
 *
 * <p>The specified MessageSource loads messages from "messages.properties" etc
 * files in the class path. This will automatically be exposed to views as
 * JSTL localization context, which the JSTL fmt tags (message etc) will use.
 * Consider using Spring's ReloadableResourceBundleMessageSource instead of
 * the standard ResourceBundleMessageSource for more sophistication.
 * Of course, any other Spring components can share the same MessageSource.
 *
 * <p>This is a separate class mainly to avoid JSTL dependencies in
 * {@link InternalResourceView} itself. JSTL has not been part of standard
 * J2EE up until J2EE 1.4, so we can't assume the JSTL API jar to be
 * available on the class path.
 *
 * <p>Hint: Set the {@link #setExposeContextBeansAsAttributes} flag to "true"
 * in order to make all Spring beans in the application context accessible
 * within JSTL expressions (e.g. in a {@code c:out} value expression).
 * This will also make all such beans accessible in plain {@code ${...}}
 * expressions in a JSP 2.0 page.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see org.springframework.web.servlet.support.JstlUtils#exposeLocalizationContext
 * @see InternalResourceViewResolver
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
// {@link InternalResourceView} 专门针对 JSTL 页面，即使用 JSP 标准标签库的 JSP 页面。
//
// <p>使用 Spring 的语言环境和 {@link org.springframework.context.MessageSource}，公开 JSTL 特定的请求属性，
// 指定 JSTL 格式和消息标签的语言环境和资源包。
// 
// <p>从 DispatcherServlet 上下文定义的角度来看，{@link InternalResourceViewResolver} 的典型用法如下：
// 
// <pre class="code"> 
// <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver> 
// 		<property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/> 
// 		<property name="prefix" value="/WEB-INF/jsp/"/> 
// 		<property name="suffix" value=".jsp"/> 
// </bean> 
// <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource"/> 
// 		<property name="basename" value="messages"/>
// </bean>
// </pre>
//
// 每个从处理器返回的视图名称都将转换为 JSP 资源（例如：“myView” &rarr; “/WEB-INF/jsp/myView.jsp”），并使用此视图类来启用显式 JSTL 支持。
//
// <p>指定的 MessageSource 会从类路径下的“messages.properties”等文件加载消息。
// 这将自动作为 JSTL 本地化上下文暴露给视图，JSTL fmt 标签（message 等）将使用该上下文。为了实现更复杂的功能，
// 请考虑使用 Spring 的 ReloadableResourceBundleMessageSource 代替标准的 ResourceBundleMessageSource。
// 当然，任何其他 Spring 组件都可以共享同一个 MessageSource。
//
// <p>这是一个单独的类，主要是为了避免 {@link InternalResourceView} 本身的 JSTL 依赖。
// 在 J2EE 1.4 之前，JSTL 并非 J2EE 标准的一部分，因此我们不能假设 JSTL API jar 文件在类路径中可用。
//
// <p>提示：将 {@link #setExposeContextBeansAsAttributes} 标志设置为“true”，
// 以使应用程序上下文中的所有 Spring bean 都可以在 JSTL 表达式（例如，在 {@code c:out} 值表达式中）中访问。
// 这也会使得所有这些 bean 都可以在 JSP 2.0 页面中的普通 {@code ${...}} 表达式中访问。
public class JstlView extends InternalResourceView {

	@Nullable
	private MessageSource messageSource;


	/**
	 * Constructor for use as a bean.
	 * @see #setUrl
	 */
	public JstlView() {
	}

	/**
	 * Create a new JstlView with the given URL.
	 * @param url the URL to forward to
	 */
	public JstlView(String url) {
		super(url);
	}

	/**
	 * Create a new JstlView with the given URL.
	 * @param url the URL to forward to
	 * @param messageSource the MessageSource to expose to JSTL tags
	 * (will be wrapped with a JSTL-aware MessageSource that is aware of JSTL's
	 * {@code jakarta.servlet.jsp.jstl.fmt.localizationContext} context-param)
	 * @see JstlUtils#getJstlAwareMessageSource
	 */
	public JstlView(String url, MessageSource messageSource) {
		this(url);
		this.messageSource = messageSource;
	}


	/**
	 * Wraps the MessageSource with a JSTL-aware MessageSource that is aware
	 * of JSTL's {@code jakarta.servlet.jsp.jstl.fmt.localizationContext}
	 * context-param.
	 * @see JstlUtils#getJstlAwareMessageSource
	 */
	@Override
	protected void initServletContext(ServletContext servletContext) {
		if (this.messageSource != null) {
			this.messageSource = JstlUtils.getJstlAwareMessageSource(servletContext, this.messageSource);
		}
		super.initServletContext(servletContext);
	}

	/**
	 * Exposes a JSTL LocalizationContext for Spring's locale and MessageSource.
	 * @see JstlUtils#exposeLocalizationContext
	 */
	@Override
	protected void exposeHelpers(HttpServletRequest request) throws Exception {
		if (this.messageSource != null) {
			JstlUtils.exposeLocalizationContext(request, this.messageSource);
		}
		else {
			JstlUtils.exposeLocalizationContext(new RequestContext(request, getServletContext()));
		}
	}

}
