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

package org.springframework.web.servlet.handler;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adapter to use the Servlet interface with the generic DispatcherServlet.
 * Calls the Servlet's {@code service} method to handle a request.
 *
 * <p>Last-modified checking is not explicitly supported: This is typically
 * handled by the Servlet implementation itself (usually deriving from
 * the HttpServlet base class).
 *
 * <p>This adapter is not activated by default; it needs to be defined as a
 * bean in the DispatcherServlet context. It will automatically apply to
 * mapped handler beans that implement the Servlet interface then.
 *
 * <p>Note that Servlet instances defined as bean will not receive initialization
 * and destruction callbacks, unless a special post-processor such as
 * SimpleServletPostProcessor is defined in the DispatcherServlet context.
 *
 * <p><b>Alternatively, consider wrapping a Servlet with Spring's
 * ServletWrappingController.</b> This is particularly appropriate for
 * existing Servlet classes, allowing to specify Servlet initialization
 * parameters etc.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see jakarta.servlet.Servlet
 * @see jakarta.servlet.http.HttpServlet
 * @see SimpleServletPostProcessor
 * @see org.springframework.web.servlet.mvc.ServletWrappingController
 */
// 适配器将 Servlet 接口与通用 DispatcherServlet 结合使用。调用 Servlet 的 {@code service} 方法来处理请求。
//
// <p>不明确支持上次修改时间检查：这通常由 Servlet 实现本身处理（通常从 HttpServlet 基类派生）。
//
// <p>此适配器默认不激活；它需要在 DispatcherServlet 上下文中定义为 bean。它将自动应用于实现 Servlet 接口的映射处理程序 bean。
//
// <p>请注意，定义为 bean 的 Servlet 实例将不会接收初始化和销毁回调，除非在 DispatcherServlet 上下文中定义了特殊的后处理器
// （例如 SimpleServletPostProcessor）。
//
// <p><b>或者，考虑使用 Spring 的 ServletWrappingController 包装 Servlet。</b>这尤其适用于现有的 Servlet 类，允许指定 Servlet 初始化参数等。
public class SimpleServletHandlerAdapter implements HandlerAdapter {

	@Override
	public boolean supports(Object handler) {
		return (handler instanceof Servlet);
	}

	@Override
	@Nullable
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		((Servlet) handler).service(request, response);
		return null;
	}

	@Override
	@SuppressWarnings("deprecation")
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

}
