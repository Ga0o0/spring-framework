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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

/**
 * Provides additional methods for dealing with multipart content within a
 * servlet request, allowing to access uploaded files.
 *
 * <p>Implementations also need to override the standard
 * {@link jakarta.servlet.ServletRequest} methods for parameter access, making
 * multipart parameters available.
 *
 * <p>A concrete implementation is
 * {@link org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest}.
 * As an intermediate step,
 * {@link org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest}
 * can be subclassed.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartResolver
 * @see MultipartFile
 * @see jakarta.servlet.http.HttpServletRequest#getParameter
 * @see jakarta.servlet.http.HttpServletRequest#getParameterNames
 * @see jakarta.servlet.http.HttpServletRequest#getParameterMap
 * @see org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
 * @see org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest
 */
// 提供额外的方法用于在 servlet 请求中处理多部分内容，从而允许访问已上传的文件。
//
// <p>实现还需要重写标准的 {@link jakarta.servlet.ServletRequest} 方法以访问参数，从而可以使用多部分参数。
//
// <p>一个具体的实现是 {@link org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest}。
// 作为中间步骤，可以对 {@link org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest} 进行子类化。
public interface MultipartHttpServletRequest extends HttpServletRequest, MultipartRequest {

	/**
	 * Return this request's method as a convenient HttpMethod instance.
	 */
	// 将此请求的方法作为便捷的 HttpMethod 实例返回。
	HttpMethod getRequestMethod();

	/**
	 * Return this request's headers as a convenient HttpHeaders instance.
	 */
	// 将此请求的标头作为便捷的 HttpHeaders 实例返回。
	HttpHeaders getRequestHeaders();

	/**
	 * Return the headers for the specified part of the multipart request.
	 * <p>If the underlying implementation supports access to part headers,
	 * then all headers are returned. Otherwise, e.g. for a file upload, the
	 * returned headers may expose a 'Content-Type' if available.
	 */
	// 返回多部分请求中指定部分的标头。
	// <p>如果底层实现支持访问部分标头，则返回所有标头。否则，例如对于文件上传，返回的标头可能会暴露 “Content-Type”（如果可用）。
	@Nullable
	HttpHeaders getMultipartHeaders(String paramOrFileName);

}
