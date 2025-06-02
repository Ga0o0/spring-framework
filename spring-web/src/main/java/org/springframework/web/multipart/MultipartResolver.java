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

package org.springframework.web.multipart;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A strategy interface for multipart file upload resolution in accordance
 * with <a href="https://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within an application context
 * and standalone.
 *
 * <p>Spring provides the following concrete implementation:
 * <ul>
 * <li>{@link org.springframework.web.multipart.support.StandardServletMultipartResolver}
 * for the Servlet Part API
 * </ul>
 *
 * <p>There is no default resolver implementation used for Spring
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets},
 * as an application might choose to parse its multipart requests itself. To define
 * an implementation, create a bean with the id "multipartResolver" in a
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet's}
 * application context. Such a resolver gets applied to all requests handled
 * by that {@link org.springframework.web.servlet.DispatcherServlet}.
 *
 * <p>If a {@link org.springframework.web.servlet.DispatcherServlet} detects a
 * multipart request, it will resolve it via the configured {@link MultipartResolver}
 * and pass on a wrapped {@link jakarta.servlet.http.HttpServletRequest}. Controllers
 * can then cast their given request to the {@link MultipartHttpServletRequest}
 * interface, which allows for access to any {@link MultipartFile MultipartFiles}.
 * Note that this cast is only supported in case of an actual multipart request.
 *
 * <pre class="code">
 * public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * Instead of direct access, command or form controllers can register a
 * {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}
 * or {@link org.springframework.web.multipart.support.StringMultipartFileEditor}
 * with their data binder, to automatically apply multipart content to form
 * bean properties.
 *
 * <p>As an alternative to using a {@link MultipartResolver} with a
 * {@link org.springframework.web.servlet.DispatcherServlet},
 * a {@link org.springframework.web.multipart.support.MultipartFilter} can be
 * registered in {@code web.xml}. It will delegate to a corresponding
 * {@link MultipartResolver} bean in the root application context. This is mainly
 * intended for applications that do not use Spring's own web MVC framework.
 *
 * <p>Note: There is hardly ever a need to access the {@link MultipartResolver}
 * itself from application code. It will simply do its work behind the scenes,
 * making {@link MultipartHttpServletRequest MultipartHttpServletRequests}
 * available to controllers.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 */
// 符合 <a href="https://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a> 的分段文件上传解析策略接口。
// 其实现通常可在应用程序上下文中使用，也可独立使用。
//
// <p>Spring 提供了以下具体实现：
// <ul>
// <li>针对 Servlet Part API 的 {@link org.springframework.web.multipart.support.StandardServletMultipartResolver}
// </ul>
//
// <p>Spring {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets} 没有默认的解析器实现，因为应用程序可能会选择自行解析其分段请求。
// 要定义实现，请在 {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet} 的应用程序上下文中创建一个 id 为“multipartResolver”的 bean。
// 此类解析器将应用于该 {@link org.springframework.web.servlet.DispatcherServlet} 处理的所有请求。
//
// <p>如果 {@link org.springframework.web.servlet.DispatcherServlet} 检测到多部分请求，它将通过配置的 {@link MultipartResolver} 解析该请求，
// 并传递包装后的 {@link jakarta.servlet.http.HttpServletRequest}。然后，控制器可以将其给定的请求强制转换为 {@link MultipartHttpServletRequest} 接口，
// 该接口允许访问任何 {@link MultipartFile MultipartFiles}。请注意，只有在实际存在多部分请求的情况下才支持此强制转换。
//
// <pre class="code">
// public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse respond) {
// 		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
// 		MultipartFile multipartFile = multipartRequest.getFile("image"); ...
// }
// </pre> 除了直接访问之外，命令控制器或表单控制器还可以向其数据绑定器注册 {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor} 或
// {@link org.springframework.web.multipart.support.StringMultipartFileEditor}，以自动将多部分内容应用于表单 bean 属性。
//
// <p>作为将 {@link MultipartResolver} 与 {@link org.springframework.web.servlet.DispatcherServlet} 一起使用的替代方案，
// 可以在 {@code web.xml} 中注册 {@link org.springframework.web.multipart.support.MultipartFilter}。
// 它将委托给根应用程序上下文中相应的 {@link MultipartResolver} bean。这主要用于不使用 Spring 自己的 Web MVC 框架的应用程序。
//
// <p>注意：几乎不需要从应用程序代码访问 {@link MultipartResolver} 本身。
// 它只会在后台完成其工作，使 {@link MultipartHttpServletRequest MultipartHttpServletRequests} 可供控制器使用。
public interface MultipartResolver {

	/**
	 * Determine if the given request contains multipart content.
	 * <p>Will typically check for content type "multipart/form-data", but the actually
	 * accepted requests might depend on the capabilities of the resolver implementation.
	 * @param request the servlet request to be evaluated
	 * @return whether the request contains multipart content
	 */
	// 判断给定的请求是否包含多部分内容。
	// <p>通常会检查内容类型“multipart/form-data”，但实际接受的请求可能取决于解析器实现的功能。
	// @param request 待评估的 Servlet 请求
	// @return 请求是否包含多部分内容
	boolean isMultipart(HttpServletRequest request);

	/**
	 * Parse the given HTTP request into multipart files and parameters,
	 * and wrap the request inside a
	 * {@link org.springframework.web.multipart.MultipartHttpServletRequest}
	 * object that provides access to file descriptors and makes contained
	 * parameters accessible via the standard ServletRequest methods.
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * @return the wrapped servlet request
	 * @throws MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file size limits)
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see jakarta.servlet.http.HttpServletRequest#getParameter
	 * @see jakarta.servlet.http.HttpServletRequest#getParameterNames
	 * @see jakarta.servlet.http.HttpServletRequest#getParameterMap
	 */
	// 将给定的 HTTP 请求解析为多部分文件和参数，并将请求包装在 {@link org.springframework.web.multipart.MultipartHttpServletRequest} 对象中，
	// 该对象提供对文件描述符的访问，并通过标准 ServletRequest 方法访问其中包含的参数。
	// @param request 要包装的 Servlet 请求（必须为多部分内容类型）
	// @return 包装后的 Servlet 请求
	// 如果 Servlet 请求不是多部分的，或者遇到特定于实现的问题（例如超出文件大小限制），则抛出 MultipartException
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * Clean up any resources used for the multipart handling,
	 * like a storage for the uploaded files.
	 * @param request the request to clean up resources for
	 */
	// 清理用于多部分处理的所有资源，例如上传文件的存储空间。
	// @param request 清理资源的请求
	void cleanupMultipart(MultipartHttpServletRequest request);

}
