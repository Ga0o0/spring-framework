/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Interface to be implemented in Servlet environments in order to configure the
 * {@link ServletContext} programmatically -- as opposed to (or possibly in conjunction
 * with) the traditional {@code web.xml}-based approach.
 *
 * <p>Implementations of this SPI will be detected automatically by {@link
 * SpringServletContainerInitializer}, which itself is bootstrapped automatically
 * by any Servlet container. See {@linkplain SpringServletContainerInitializer its
 * Javadoc} for details on this bootstrapping mechanism.
 *
 * <h2>Example</h2>
 * <h3>The traditional, XML-based approach</h3>
 * Most Spring users building a web application will need to register Spring's {@code
 * DispatcherServlet}. For reference, in WEB-INF/web.xml, this would typically be done as
 * follows:
 * <pre class="code">
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;
 *     org.springframework.web.servlet.DispatcherServlet
 *   &lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *     &lt;param-value&gt;/WEB-INF/spring/dispatcher-config.xml&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 *
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;</pre>
 *
 * <h3>The code-based approach with {@code WebApplicationInitializer}</h3>
 * Here is the equivalent {@code DispatcherServlet} registration logic,
 * {@code WebApplicationInitializer}-style:
 * <pre class="code">
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      XmlWebApplicationContext appContext = new XmlWebApplicationContext();
 *      appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
 *
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(appContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractDispatcherServletInitializer}.
 *
 * As you can see, thanks to the Servlet container's {@link ServletContext#addServlet}
 * method we're actually registering an <em>instance</em> of the {@code DispatcherServlet},
 * and this means that the {@code DispatcherServlet} can now be treated like any other
 * object -- receiving constructor injection of its application context in this case.
 *
 * <p>This style is both simpler and more concise. There is no concern for dealing with
 * init-params, etc, just normal JavaBean-style properties and constructor arguments. You
 * are free to create and work with your Spring application contexts as necessary before
 * injecting them into the {@code DispatcherServlet}.
 *
 * <p>Most major Spring Web components have been updated to support this style of
 * registration.  You'll find that {@code DispatcherServlet}, {@code FrameworkServlet},
 * {@code ContextLoaderListener} and {@code DelegatingFilterProxy} all now support
 * constructor arguments. Even if a component (e.g. non-Spring, other third party) has not
 * been specifically updated for use within {@code WebApplicationInitializers}, they still
 * may be used in any case. The {@code ServletContext} API allows for setting init-params,
 * context-params, etc programmatically.
 *
 * <h2>A 100% code-based approach to configuration</h2>
 * In the example above, {@code WEB-INF/web.xml} was successfully replaced with code in
 * the form of a {@code WebApplicationInitializer}, but the actual
 * {@code dispatcher-config.xml} Spring configuration remained XML-based.
 * {@code WebApplicationInitializer} is a perfect fit for use with Spring's code-based
 * {@code @Configuration} classes. See @{@link
 * org.springframework.context.annotation.Configuration Configuration} Javadoc for
 * complete details, but the following example demonstrates refactoring to use Spring's
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} in lieu of {@code XmlWebApplicationContext}, and
 * user-defined {@code @Configuration} classes {@code AppConfig} and
 * {@code DispatcherConfig} instead of Spring XML files. This example also goes a bit
 * beyond those above to demonstrate typical configuration of the 'root' application
 * context and registration of the {@code ContextLoaderListener}:
 * <pre class="code">
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      // Create the 'root' Spring application context
 *      AnnotationConfigWebApplicationContext rootContext =
 *        new AnnotationConfigWebApplicationContext();
 *      rootContext.register(AppConfig.class);
 *
 *      // Manage the lifecycle of the root application context
 *      container.addListener(new ContextLoaderListener(rootContext));
 *
 *      // Create the dispatcher servlet's Spring application context
 *      AnnotationConfigWebApplicationContext dispatcherContext =
 *        new AnnotationConfigWebApplicationContext();
 *      dispatcherContext.register(DispatcherConfig.class);
 *
 *      // Register and map the dispatcher servlet
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 *
 * Remember that {@code WebApplicationInitializer} implementations are <em>detected
 * automatically</em> -- so you are free to package them within your application as you
 * see fit.
 *
 * <h2>Ordering {@code WebApplicationInitializer} execution</h2>
 * {@code WebApplicationInitializer} implementations may optionally be annotated at the
 * class level with Spring's @{@link org.springframework.core.annotation.Order Order}
 * annotation or may implement Spring's {@link org.springframework.core.Ordered Ordered}
 * interface. If so, the initializers will be ordered prior to invocation. This provides
 * a mechanism for users to ensure the order in which servlet container initialization
 * occurs. Use of this feature is expected to be rare, as typical applications will likely
 * centralize all container initialization within a single {@code WebApplicationInitializer}.
 *
 * @author Chris Beams
 * @since 3.1
 * @see SpringServletContainerInitializer
 * @see org.springframework.web.context.AbstractContextLoaderInitializer
 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
 */
// 在 Servlet 环境中实现的接口，用于以编程方式配置 {@link ServletContext} —— 与传统的基于 {@code web.xml} 的方法相反（或可能与之结合）。
// <p>此 SPI 的实现将由 {@link SpringServletContainerInitializer} 自动检测，而它本身可由任何 Servlet 容器自动引导。
// 有关此引导机制的详细信息，请参阅 {@linkplain SpringServletContainerInitializer 及其 Javadoc}。
//
// <h2>示例</h2>
//
// <h3>传统的基于 XML 的方法</h3>
//
// 大多数构建 Web 应用程序的 Spring 用户都需要注册 Spring 的 {@code DispatcherServlet}。作为参考，在 WEB-INF/web.xml 中，这通常会按如下方式完成：
//
// <pre class="code">
// <servlet>
//		<servlet-name>dispatcher</servlet-name>
// 		<servlet-class> org.springframework.web.servlet.DispatcherServlet </servlet-class>
// 		<init-param>
// 			<param-name>contextConfigLocation</param-name>
// 			<param-value>/WEB-INF/spring/dispatcher-config.xml</param-value>
// 		</init-param>
// 		<load-on-startup>1</load-on-startup>
// </servlet>
//
// <servlet-mapping>
// 		<servlet-name>dispatcher</servlet-name>
// 		<url-pattern>/</url-pattern>
// </servlet-mapping>
// </pre>
//
// <h3>使用 {@code WebApplicationInitializer} 的基于代码的方法</h3>
//
// 以下是等效的 {@code DispatcherServlet} 注册逻辑，{@code WebApplicationInitializer}-style：
//
// <pre class="code">
// public class MyWebAppInitializer implements WebApplicationInitializer {
// 		@Override
// 		public void onStartup(ServletContext container) {
// 			XmlWebApplicationContext appContext = new XmlWebApplicationContext();
// 			appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");

// 			ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher",
// 					new DispatcherServlet(appContext));
// 			dispatcher.setLoadOnStartup(1);
// 			dispatcher.addMapping("/");
// 		}
// }
// </pre>
//
// 作为上述方法的替代方案，您也可以从 {@link org.springframework.web.servlet.support.AbstractDispatcherServletInitializer} 进行扩展。
// 如您所见，得益于 Servlet 容器的 {@link ServletContext#addServlet} 方法，我们实际上是在注册 {@code DispatcherServlet} 的一个<em>实例</em>，
// 这意味着 {@code DispatcherServlet} 现在可以像任何其他对象一样对待 - 在这种情况下，接收其应用程序上下文的构造函数注入。
//
// <p>这种风格更简单、更简洁。无需担心处理 init-params 等，只需处理普通的 JavaBean 风格属性和构造函数参数。
// 在将它们注入 {@code DispatcherServlet} 之前，您可以根据需要自由创建和使用 Spring 应用程序上下文。
//
// <p>大多数主要的 Spring Web 组件都已更新为支持这种注册风格。
// 您会发现 {@code DispatcherServlet}、{@code FrameworkServlet}、{@code ContextLoaderListener} 和 {@code DelegatingFilterProxy} 现在都支持构造函数参数。
// 即使某个组件（例如非 Spring 组件、其他第三方组件）尚未专门更新以用于 {@code WebApplicationInitializers}，它们仍然可以在任何情况下使用。
// {@code ServletContext} API 允许以编程方式设置 init-params、context-params 等。
//
// <h2>100% 基于代码的配置方法</h2>
//
// 在上面的示例中，{@code WEB-INF/web.xml} 已成功替换为 {@code WebApplicationInitializer} 形式的代码，
// 但实际的 {@code dispatcher-config.xml} Spring 配置仍然是基于 XML 的。
// {@code WebApplicationInitializer} 非常适合与 Spring 基于代码的 {@code @Configuration} 类配合使用。
// 有关完整详细信息，请参阅 @{@link org.springframework.context.annotation.Configuration Configuration} Javadoc，
// 但以下示例演示了如何重构使用 Spring 的
// {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext}
// 代替 {@code XmlWebApplicationContext}，并使用用户定义的 {@code @Configuration} 类 {@code AppConfig} 和 {@code DispatcherConfig}
// 代替 Spring XML 文件。此示例还比上述示例更进一步，演示了“根”应用程序上下文的典型配置以及 {@code ContextLoaderListener} 的注册：
//
// <pre class="code">
// 	public class MyWebAppInitializer implements WebApplicationInitializer {
// 		@Override
// 		public void onStartup(ServletContext container) {
// 			// 创建“根”Spring 应用程序上下文
// 			AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
// 			rootContext.register(AppConfig.class);
//
// 			// 管理根应用程序上下文的生命周期
// 			container.addListener(new ContextLoaderListener(rootContext));
//
// 			// 创建调度程序 servlet 的 Spring 应用程序上下文
// 			AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
// 			dispatcherContext.register(DispatcherConfig.class);
//
// 			// 注册并映射调度程序 servlet
// 			ServletRegistration.Dynamic dispatcher = container.addServlet("dispatcher",
// 							new DispatcherServlet(dispatcherContext));
// 			dispatcher.setLoadOnStartup(1);
// 			dispatcher.addMapping("/");
// 		}
// }
// </pre>
//
// 除了上述方法之外，您还可以从 {@link org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer} 扩展。
// 请记住，{@code WebApplicationInitializer} 实现是<em>自动检测</em>的——因此您可以根据需要将其打包到应用程序中。
//
// <h2>排序 {@code WebApplicationInitializer} 执行</h2>
//
// {@code WebApplicationInitializer} 实现可以选择在类级别使用 Spring 的 @{@link org.springframework.core.annotation.Order Order} 注释进行注释，
// 或者可以实现 Spring 的 {@link org.springframework.core.Ordered Ordered} 接口。如果是这样，初始化程序将在调用之前进行排序。
// 这为用户提供了一种机制来确保 servlet 容器初始化发生的顺序。
// 预计此功能的使用很少，因为典型的应用程序可能会将所有容器初始化集中在单个 {@code WebApplicationInitializer} 中。
public interface WebApplicationInitializer {

	/**
	 * Configure the given {@link ServletContext} with any servlets, filters, listeners
	 * context-params and attributes necessary for initializing this web application. See
	 * examples {@linkplain WebApplicationInitializer above}.
	 * @param servletContext the {@code ServletContext} to initialize
	 * @throws ServletException if any call against the given {@code ServletContext}
	 * throws a {@code ServletException}
	 */
	// 使用初始化此 Web 应用所需的所有 servlet、过滤器、监听器上下文参数和属性来配置给定的 {@link ServletContext}。
	// 请参阅上面的 {@linkplain WebApplicationInitializer} 示例。
	// @param servletContext 需要初始化的 {@code ServletContext}
	// @throws ServletException，如果任何针对给定 {@code ServletContext} 的调用引发 {@code ServletException}
	void onStartup(ServletContext servletContext) throws ServletException;

}
