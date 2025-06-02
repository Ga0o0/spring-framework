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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects that can resolve exceptions thrown during
 * handler mapping or execution, in the typical case to error views. Implementors are
 * typically registered as beans in the application context.
 *
 * <p>Error views are analogous to JSP error pages but can be used with any kind of
 * exception including any checked exception, with potentially fine-grained mappings for
 * specific handlers.
 *
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
// 接口由对象实现，用于解析处理程序映射或执行期间抛出的异常，通常情况下是错误视图。实现者通常在应用程序上下文中注册为 Bean。
//
// <p>错误视图类似于 JSP 错误页面，但可以用于任何类型的异常，包括任何已检查异常，并可能针对特定处理程序进行细粒度的映射。
public interface HandlerExceptionResolver {

	/**
	 * Try to resolve the given exception that got thrown during handler execution,
	 * returning a {@link ModelAndView} that represents a specific error page if appropriate.
	 * <p>The returned {@code ModelAndView} may be {@linkplain ModelAndView#isEmpty() empty}
	 * to indicate that the exception has been resolved successfully but that no view
	 * should be rendered, for instance by setting a status code.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 * time of the exception (for example, if multipart resolution failed)
	 * @param ex the exception that got thrown during handler execution
	 * @return a corresponding {@code ModelAndView} to forward to,
	 * or {@code null} for default processing in the resolution chain
	 */
	// 尝试解决在处理程序执行期间抛出的给定异常，并在适当的情况下返回表示特定错误页面的 {@link ModelAndView}。
	// <p>返回的 {@code ModelAndView} 可能为 {@linkplain ModelAndView#isEmpty() 空}，以指示异常已成功解决，
	// 但不应呈现任何视图，例如通过设置状态代码。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @param handler 执行的处理程序，如果在发生异常时未选择任何处理程序（例如，如果多部分解析失败），则为 {@code null}
	// @param ex 在处理程序执行期间抛出的异常
	// @return 要转发到的相应 {@code ModelAndView}，或 {@code null} 用于解析链中的默认处理
	@Nullable
	ModelAndView resolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex);

}
