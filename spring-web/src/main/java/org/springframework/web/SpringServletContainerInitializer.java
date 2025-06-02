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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * A Spring-provided {@link ServletContainerInitializer} designed to support code-based
 * configuration of the servlet container using Spring's {@link WebApplicationInitializer}
 * SPI as opposed to (or possibly in combination with) the traditional
 * {@code web.xml}-based approach.
 *
 * <h2>Mechanism of Operation</h2>
 * This class will be loaded and instantiated and have its {@link #onStartup}
 * method invoked by any Servlet-compliant container during container startup assuming
 * that the {@code spring-web} module JAR is present on the classpath. This occurs through
 * the JAR Services API {@link ServiceLoader#load(Class)} method detecting the
 * {@code spring-web} module's {@code META-INF/services/jakarta.servlet.ServletContainerInitializer}
 * service provider configuration file.
 *
 * <h3>In combination with {@code web.xml}</h3>
 * A web application can choose to limit the amount of classpath scanning the Servlet
 * container does at startup either through the {@code metadata-complete} attribute in
 * {@code web.xml}, which controls scanning for Servlet annotations or through an
 * {@code <absolute-ordering>} element also in {@code web.xml}, which controls which
 * web fragments (i.e. jars) are allowed to perform a {@code ServletContainerInitializer}
 * scan. When using this feature, the {@link SpringServletContainerInitializer}
 * can be enabled by adding "spring_web" to the list of named web fragments in
 * {@code web.xml} as follows:
 *
 * <pre class="code">
 * &lt;absolute-ordering&gt;
 *   &lt;name&gt;some_web_fragment&lt;/name&gt;
 *   &lt;name&gt;spring_web&lt;/name&gt;
 * &lt;/absolute-ordering&gt;
 * </pre>
 *
 * <h2>Relationship to Spring's {@code WebApplicationInitializer}</h2>
 * Spring's {@code WebApplicationInitializer} SPI consists of just one method:
 * {@link WebApplicationInitializer#onStartup(ServletContext)}. The signature is intentionally
 * quite similar to {@link ServletContainerInitializer#onStartup(Set, ServletContext)}:
 * simply put, {@code SpringServletContainerInitializer} is responsible for instantiating
 * and delegating the {@code ServletContext} to any user-defined
 * {@code WebApplicationInitializer} implementations. It is then the responsibility of
 * each {@code WebApplicationInitializer} to do the actual work of initializing the
 * {@code ServletContext}. The exact process of delegation is described in detail in the
 * {@link #onStartup onStartup} documentation below.
 *
 * <h2>General Notes</h2>
 * In general, this class should be viewed as <em>supporting infrastructure</em> for
 * the more important and user-facing {@code WebApplicationInitializer} SPI. Taking
 * advantage of this container initializer is also completely <em>optional</em>:
 * while it is true that this initializer will be loaded and invoked under all
 * Servlet runtimes, it remains the user's choice whether to make any
 * {@code WebApplicationInitializer} implementations available on the classpath.
 * If no {@code WebApplicationInitializer} types are detected, this container
 * initializer will have no effect.
 *
 * <p>Note that use of this container initializer and of {@code WebApplicationInitializer}
 * is not in any way "tied" to Spring MVC other than the fact that the types are shipped
 * in the {@code spring-web} module JAR. Rather, they can be considered general-purpose
 * in their ability to facilitate convenient code-based configuration of the
 * {@code ServletContext}. In other words, any servlet, listener, or filter may be
 * registered within a {@code WebApplicationInitializer}, not just Spring MVC-specific
 * components.
 *
 * <p>This class is neither designed for extension nor intended to be extended.
 * It should be considered an internal type, with {@code WebApplicationInitializer}
 * being the public-facing SPI.
 *
 * <h2>See Also</h2>
 * See {@link WebApplicationInitializer} Javadoc for examples and detailed usage
 * recommendations.<p>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see #onStartup(Set, ServletContext)
 * @see WebApplicationInitializer
 */
// Spring 提供的 {@link ServletContainerInitializer} 旨在支持使用 Spring 的 {@link WebApplicationInitializer} SPI 进行基于代码的 servlet 容器配置，
// 而不是（或可能结合）传统的基于 {@code web.xml} 的方法。
//
// <h2>运行机制</h2>
//
// 此类将被加载并实例化，并且任何符合 Servlet 规范的容器在容器启动期间都会调用其 {@link #onStartup} 方法，前提是 {@code spring-web} 模块 JAR 存在于类路径中。
// 这是通过 JAR 服务 API {@link ServiceLoader#load(Class)} 方法检测 {@code spring-web} 模块的
// {@code META-INF/services/jakarta.servlet.ServletContainerInitializer} 服务提供程序配置文件来实现的。
//
// <h3>与 {@code web.xml} 结合</h3>
//
// Web 应用程序可以选择限制 Servlet 容器在启动时执行的类路径扫描量，方法是通过 {@code web.xml} 中的 {@code metadata-complete} 属性
// （控制对 Servlet 注释的扫描）或通过 {@code web.xml} 中的 {@code <absolute-ordering>} 元素（控制允许哪些 Web 片段（即 jar）执行 {@code ServletContainerInitializer} 扫描）。
// 使用此功能时，可以通过将“spring_web”添加到 {@code web.xml} 中的命名 Web 片段列表来启用 {@link SpringServletContainerInitializer}，如下所示：
//
// <pre class="code">
// <absolute-ordering>
// 		<name>some_web_fragment</name>
// 		<name>spring_web</name>
// </absolute-ordering>
// </pre>
//
// <h2>与 Spring 的 {@code WebApplicationInitializer} 的关系</h2>
//
// Spring 的 {@code WebApplicationInitializer} SPI 仅包含一种方法：{@link WebApplicationInitializer#onStartup(ServletContext)}。
// 签名有意与 {@link ServletContainerInitializer#onStartup(Set, ServletContext)} 非常相似：
// 简而言之，{@code SpringServletContainerInitializer} 负责实例化 {@code ServletContext} 并将其委托给任何用户定义的 {@code WebApplicationInitializer} 实现。
// 然后，每个 {@code WebApplicationInitializer} 负责完成初始化 {@code ServletContext} 的实际工作。
// 委托的具体过程在下面的 {@link #onStartup onStartup} 文档中有详细描述。
//
// <h2>一般说明</h2>
//
// 通常，此类应被视为更重要且面向用户的 {@code WebApplicationInitializer} SPI 的<em>支持基础架构</em>。使用此容器初始化程序也是完全<em>可选的</em>：
// 虽然此初始化程序确实会在所有 Servlet 运行时下加载和调用，但用户仍然可以选择是否在类路径上提供任何 {@code WebApplicationInitializer} 实现。
// 如果未检测到 {@code WebApplicationInitializer} 类型，则此容器初始化程序将不起作用。
//
// <p>请注意，此容器初始化器和 {@code WebApplicationInitializer} 的使用与 Spring MVC 没有任何“关联”，除了这些类型包含在 {@code spring-web} 模块 JAR 中。
// 相反，它们可以被视为通用的，因为它们能够方便地基于代码配置 {@code ServletContext}。
// 换句话说，任何 servlet、监听器或过滤器都可以在 {@code WebApplicationInitializer} 中注册，而不仅仅是 Spring MVC 特定的组件。
//
// <p>此类并非为扩展而设计，也不打算被扩展。它应该被视为内部类型，而 {@code WebApplicationInitializer} 是面向公众的 SPI。
//
// <h2>另请参阅</h2>
//
// 有关示例和详细的使用建议，请参阅 {@link WebApplicationInitializer} Javadoc。<p>
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {

	/**
	 * Delegate the {@code ServletContext} to any {@link WebApplicationInitializer}
	 * implementations present on the application classpath.
	 * <p>Because this class declares @{@code HandlesTypes(WebApplicationInitializer.class)},
	 * Servlet containers will automatically scan the classpath for implementations of
	 * Spring's {@code WebApplicationInitializer} interface and provide the set of all
	 * such types to the {@code webAppInitializerClasses} parameter of this method.
	 * <p>If no {@code WebApplicationInitializer} implementations are found on the classpath,
	 * this method is effectively a no-op. An INFO-level log message will be issued notifying
	 * the user that the {@code ServletContainerInitializer} has indeed been invoked but that
	 * no {@code WebApplicationInitializer} implementations were found.
	 * <p>Assuming that one or more {@code WebApplicationInitializer} types are detected,
	 * they will be instantiated (and <em>sorted</em> if the @{@link
	 * org.springframework.core.annotation.Order @Order} annotation is present or
	 * the {@link org.springframework.core.Ordered Ordered} interface has been
	 * implemented). Then the {@link WebApplicationInitializer#onStartup(ServletContext)}
	 * method will be invoked on each instance, delegating the {@code ServletContext} such
	 * that each instance may register and configure servlets such as Spring's
	 * {@code DispatcherServlet}, listeners such as Spring's {@code ContextLoaderListener},
	 * or any other Servlet API features such as filters.
	 * @param webAppInitializerClasses all implementations of
	 * {@link WebApplicationInitializer} found on the application classpath
	 * @param servletContext the servlet context to be initialized
	 * @see WebApplicationInitializer#onStartup(ServletContext)
	 * @see AnnotationAwareOrderComparator
	 */
	// 将 {@code ServletContext} 委托给应用程序类路径中存在的任何 {@link WebApplicationInitializer} 实现。
	// <p>由于此类声明了 @{@code HandlesTypes(WebApplicationInitializer.class)}，
	// Servlet 容器将自动扫描类路径以查找 Spring 的 {@code WebApplicationInitializer} 接口的实现，
	// 并将所有此类类型的集合提供给此方法的 {@code webAppInitializerClasses} 参数。
	// <p>如果在类路径中未找到任何 {@code WebApplicationInitializer} 实现，则此方法实际上为空操作。
	// 将发出 INFO 级别的日志消息，通知用户 {@code ServletContainerInitializer} 确实已被调用，但未找到任何 {@code WebApplicationInitializer} 实现。
	// <p>假设检测到一个或多个 {@code WebApplicationInitializer} 类型，它们将被实例化（如果存在 @{@link org.springframework.core.annotation.Order @Order} 注释
	// 或已实现 {@link org.springframework.core.Ordered Ordered} 接口，则<em>进行排序</em>）。
	// 然后将在每个实例上调用 {@link WebApplicationInitializer#onStartup(ServletContext)} 方法，委托 {@code ServletContext}，
	// 以便每个实例可以注册和配置 servlet（例如 Spring 的 {@code DispatcherServlet}）、监听器（例如 Spring 的 {@code ContextLoaderListener}）
	// 或任何其他 Servlet API 功能（例如过滤器）。
	// @param webAppInitializerClasses 在应用程序类路径上找到的所有 {@link WebApplicationInitializer} 实现
	// @param servletContext 要初始化的 servlet 上下文
	@Override
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = Collections.emptyList();

		if (webAppInitializerClasses != null) {
			initializers = new ArrayList<>(webAppInitializerClasses.size());
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}
