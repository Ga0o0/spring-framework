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

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Plain handler interface for components that process HTTP requests,
 * analogous to a Servlet. Only declares {@link jakarta.servlet.ServletException}
 * and {@link java.io.IOException}, to allow for usage within any
 * {@link jakarta.servlet.http.HttpServlet}. This interface is essentially the
 * direct equivalent of an HttpServlet, reduced to a central handle method.
 *
 * <p>The easiest way to expose an HttpRequestHandler bean in Spring style
 * is to define it in Spring's root web application context and define
 * an {@link org.springframework.web.context.support.HttpRequestHandlerServlet}
 * in {@code web.xml}, pointing to the target HttpRequestHandler bean
 * through its {@code servlet-name} which needs to match the target bean name.
 *
 * <p>Supported as a handler type within Spring's
 * {@link org.springframework.web.servlet.DispatcherServlet}, being able
 * to interact with the dispatcher's advanced mapping and interception
 * facilities. This is the recommended way of exposing an HttpRequestHandler,
 * while keeping the handler implementations free of direct dependencies
 * on a DispatcherServlet environment.
 *
 * <p>Typically implemented to generate binary responses directly,
 * with no separate view resource involved. This differentiates it from a
 * {@link org.springframework.web.servlet.mvc.Controller} within Spring's Web MVC
 * framework. The lack of a {@link org.springframework.web.servlet.ModelAndView}
 * return value gives a clearer signature to callers other than the
 * DispatcherServlet, indicating that there will never be a view to render.
 *
 * <p>Note that HttpRequestHandlers may optionally implement the
 * {@link org.springframework.web.servlet.mvc.LastModified} interface,
 * just like Controllers can, <i>provided that they run within Spring's
 * DispatcherServlet</i>. However, this is usually not necessary, since
 * HttpRequestHandlers typically only support POST requests to begin with.
 * Alternatively, a handler may implement the "If-Modified-Since" HTTP
 * header processing manually within its {@code handle} method.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.context.support.HttpRequestHandlerServlet
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.ModelAndView
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.servlet.mvc.LastModified
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 */
// 用于处理 HTTP 请求的组件的简单处理程序接口，类似于 Servlet。
// 仅声明 {@link jakarta.servlet.ServletException} 和 {@link java.io.IOException}，
// 即可在任何 {@link jakarta.servlet.http.HttpServlet} 中使用。此接口本质上与 HttpServlet 直接等效，但简化为一个核心处理方法。
//
// <p>以 Spring 风格公开 HttpRequestHandler bean 的最简单方法是在 Spring 的根 Web 应用程序上下文中定义它，
// 并在 {@code web.xml} 中定义一个 {@link org.springframework.web.context.support.HttpRequestHandlerServlet}，
// 并通过其 {@code servlet-name} 指向目标 HttpRequestHandler bean（该 servlet-name 需要与目标 bean 名称匹配）。
//
// <p>在 Spring 的 {@link org.springframework.web.servlet.DispatcherServlet} 中作为处理程序类型受支持，
// 能够与调度程序的高级映射和拦截功能进行交互。这是推荐的暴露 HttpRequestHandler 的方式，同时保持处理程序实现不直接依赖于 DispatcherServlet 环境。
//
// <p>通常实现为直接生成二进制响应，不涉及单独的视图资源。这使其有别于 Spring Web MVC 框架中的 {@link org.springframework.web.servlet.mvc.Controller}。
// 缺少 {@link org.springframework.web.servlet.ModelAndView} 返回值，为 DispatcherServlet 以外的调用者提供了更清晰的签名，表明永远不会有视图需要渲染。
//
// <p>请注意，HttpRequestHandler 可以选择实现 {@link org.springframework.web.servlet.mvc.LastModified} 接口，
// 就像 Controller 一样，<i>前提是它们在 Spring 的 DispatcherServlet 中运行</i>。
// 但是，这通常不是必需的，因为 HttpRequestHandler 通常只支持 POST 请求。
// 或者，处理程序可以在其 {@code handle} 方法中手动实现“If-Modified-Since”HTTP 标头处理。
@FunctionalInterface
public interface HttpRequestHandler {

	/**
	 * Process the given request, generating a response.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws ServletException in case of general errors
	 * @throws IOException in case of I/O errors
	 */
	// 处理给定的请求并生成响应。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @throws ServletException（如果发生一般错误）
	// @throws IOException（如果发生 I/O 错误）
	void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException;

}
