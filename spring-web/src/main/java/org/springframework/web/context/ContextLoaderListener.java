/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.web.context;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Bootstrap listener to start up and shut down Spring's root {@link WebApplicationContext}.
 * Simply delegates to {@link ContextLoader} as well as to {@link ContextCleanupListener}.
 *
 * <p>{@code ContextLoaderListener} supports injecting the root web application
 * context via the {@link #ContextLoaderListener(WebApplicationContext)}
 * constructor, allowing for programmatic configuration in Servlet initializers.
 * See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 17.02.2003
 * @see #setContextInitializers
 * @see org.springframework.web.WebApplicationInitializer
 */
// 引导监听器用于启动和关闭 Spring 的根 {@link WebApplicationContext}。只需委托给 {@link ContextLoader} 和 {@link ContextCleanupListener} 即可。
//
// <p>{@code ContextLoaderListener} 支持通过 {@link #ContextLoaderListener(WebApplicationContext)}
// 构造函数注入根 Web 应用程序上下文，从而允许在 Servlet 初始化程序中进行编程式配置。
// 有关使用示例，请参阅 {@link org.springframework.web.WebApplicationInitializer}。
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

	/**
	 * Create a new {@code ContextLoaderListener} that will create a web application
	 * context based on the "contextClass" and "contextConfigLocation" servlet
	 * context-params. See {@link ContextLoader} superclass documentation for details on
	 * default values for each.
	 * <p>This constructor is typically used when declaring {@code ContextLoaderListener}
	 * as a {@code <listener>} within {@code web.xml}, where a no-arg constructor is
	 * required.
	 * <p>The created application context will be registered into the ServletContext under
	 * the attribute name {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}
	 * and the Spring application context will be closed when the {@link #contextDestroyed}
	 * lifecycle method is invoked on this listener.
	 * @see ContextLoader
	 * @see #ContextLoaderListener(WebApplicationContext)
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	// 创建一个新的 {@code ContextLoaderListener}，它将基于“contextClass”和“contextConfigLocation”servlet 上下文参数创建一个 Web 应用程序上下文。
	// 有关每个参数的默认值的详细信息，请参阅 {@link ContextLoader} 超类文档。
	// <p>此构造函数通常在 {@code web.xml} 中将 {@code ContextLoaderListener} 声明为 {@code <listener>} 时使用，此时需要使用无参数构造函数。
	// <p>创建的应用程序上下文将注册到 ServletContext 中，属性名称为 {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE}，
	// 并且当在此侦听器上调用 {@link #contextDestroyed} 生命周期方法时，Spring 应用程序上下文将关闭。
	public ContextLoaderListener() {
	}

	/**
	 * Create a new {@code ContextLoaderListener} with the given application context. This
	 * constructor is useful in Servlet initializers where instance-based registration of
	 * listeners is possible through the {@link jakarta.servlet.ServletContext#addListener} API.
	 * <p>The context may or may not yet be {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#refresh() refreshed}. If it
	 * (a) is an implementation of {@link ConfigurableWebApplicationContext} and
	 * (b) has <strong>not</strong> already been refreshed (the recommended approach),
	 * then the following will occur:
	 * <ul>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * org.springframework.context.ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #customizeContext} will be called</li>
	 * <li>Any {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer org.springframework.context.ApplicationContextInitializer ApplicationContextInitializers}
	 * specified through the "contextInitializerClasses" init-param will be applied.</li>
	 * <li>{@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * <p>In any case, the given application context will be registered into the
	 * ServletContext under the attribute name {@link
	 * WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} and the Spring
	 * application context will be closed when the {@link #contextDestroyed} lifecycle
	 * method is invoked on this listener.
	 * @param context the application context to manage
	 * @see #contextInitialized(ServletContextEvent)
	 * @see #contextDestroyed(ServletContextEvent)
	 */
	// 使用给定的应用上下文创建一个新的 {@code ContextLoaderListener}。
	// 此构造函数在 Servlet 初始化程序中非常有用，因为可以通过 {@link jakarta.servlet.ServletContext#addListener} API 进行基于实例的监听器注册。
	// <p>上下文可能已刷新，也可能尚未刷新 {@linkplain org.springframework.context.ConfigurableApplicationContext#refresh()}。
	// 如果它 (a) 是 {@link ConfigurableWebApplicationContext} 的实现并且 (b) <strong>尚未</strong>刷新（推荐方法），则会发生以下情况：
	// <ul>
	// <li>如果给定的上下文尚未分配 {@linkplain org.springframework.context.ConfigurableApplicationContext#setId id}，则会为其分配一个</li>
	// <li>{@code ServletContext} 和 {@code ServletConfig} 对象将被委托给应用程序上下文</li>
	// <li>将调用 {@link #customizeContext} </li>
	// <li>将应用通过“contextInitializerClasses”init-param 指定的任何
	// {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer org.springframework.context.ApplicationContextInitializer ApplicationContextInitializers}。</li>
	// <li>{@link org.springframework.context.ConfigurableApplicationContext#refresh refresh()} 将被调用</li>
	// </ul>
	// 如果上下文已经刷新或者没有实现 {@code ConfigurableWebApplicationContext}，则在假设用户已经根据其特定需求执行了这些操作（或没有执行）的情况下，上述任何操作都不会发生。
	// <p>有关使用示例，请参阅 {@link org.springframework.web.WebApplicationInitializer}。
	// <p>无论如何，给定的应用程序上下文将在属性名称 {@link WebApplicationContext#ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE} 下注册到 ServletContext 中，
	// 并且当在此侦听器上调用 {@link #contextDestroyed} 生命周期方法时，Spring 应用程序上下文将关闭。
	// @param context 要管理的应用程序上下文
	public ContextLoaderListener(WebApplicationContext context) {
		super(context);
	}


	/**
	 * Initialize the root web application context.
	 */
	// 初始化根 Web 应用程序上下文。
	@Override
	public void contextInitialized(ServletContextEvent event) {
		initWebApplicationContext(event.getServletContext());
	}


	/**
	 * Close the root web application context.
	 */
	// 关闭根 Web 应用程序上下文。
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		closeWebApplicationContext(event.getServletContext());
		ContextCleanupListener.cleanupAttributes(event.getServletContext());
	}

}
