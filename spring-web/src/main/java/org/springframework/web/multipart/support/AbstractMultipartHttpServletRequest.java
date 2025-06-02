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

package org.springframework.web.multipart.support;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Abstract base implementation of the {@link MultipartHttpServletRequest} interface.
 * <p>Provides management of pre-generated {@link MultipartFile} instances.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 06.10.2003
 */
// {@link MultipartHttpServletRequest} 接口的抽象基实现。
// <p>提供对预生成的 {@link MultipartFile} 实例的管理。
public abstract class AbstractMultipartHttpServletRequest extends HttpServletRequestWrapper
		implements MultipartHttpServletRequest {

	@Nullable
	private MultiValueMap<String, MultipartFile> multipartFiles;


	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the request to wrap
	 */
	// 将给定的 HttpServletRequest 包装在 MultipartHttpServletRequest 中。
	// @param request 要包装的请求
	protected AbstractMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}


	@Override
	public HttpServletRequest getRequest() {
		return (HttpServletRequest) super.getRequest();
	}

	@Override
	public HttpMethod getRequestMethod() {
		return HttpMethod.valueOf(getRequest().getMethod());
	}

	@Override
	public HttpHeaders getRequestHeaders() {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, Collections.list(getHeaders(headerName)));
		}
		return headers;
	}

	@Override
	public Iterator<String> getFileNames() {
		return getMultipartFiles().keySet().iterator();
	}

	@Override
	@Nullable
	public MultipartFile getFile(String name) {
		return getMultipartFiles().getFirst(name);
	}

	@Override
	public List<MultipartFile> getFiles(String name) {
		List<MultipartFile> multipartFiles = getMultipartFiles().get(name);
		if (multipartFiles != null) {
			return multipartFiles;
		}
		else {
			return Collections.emptyList();
		}
	}

	@Override
	public Map<String, MultipartFile> getFileMap() {
		return getMultipartFiles().toSingleValueMap();
	}

	@Override
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return getMultipartFiles();
	}

	/**
	 * Determine whether the underlying multipart request has been resolved.
	 * @return {@code true} when eagerly initialized or lazily triggered,
	 * {@code false} in case of a lazy-resolution request that got aborted
	 * before any parameters or multipart files have been accessed
	 * @since 4.3.15
	 * @see #getMultipartFiles()
	 */
	// 确定底层多部分请求是否已被解析。
	// @return {@code true} 如果是主动初始化或延迟触发，{@code false} 如果是延迟解析请求，并且在访问任何参数或多部分文件之前中止，则返回此值。
	public boolean isResolved() {
		return (this.multipartFiles != null);
	}


	/**
	 * Set a Map with parameter names as keys and list of MultipartFile objects as values.
	 * To be invoked by subclasses on initialization.
	 */
	// 设置一个 Map，以参数名称作为键，MultipartFile 对象列表作为值。子类初始化时会调用此方法。
	protected final void setMultipartFiles(MultiValueMap<String, MultipartFile> multipartFiles) {
		this.multipartFiles =
				new LinkedMultiValueMap<>(Collections.unmodifiableMap(multipartFiles));
	}

	/**
	 * Obtain the MultipartFile Map for retrieval,
	 * lazily initializing it if necessary.
	 * @see #initializeMultipart()
	 */
	// 获取用于检索的 MultipartFile Map，并在必要时延迟初始化。
	protected MultiValueMap<String, MultipartFile> getMultipartFiles() {
		if (this.multipartFiles == null) {
			initializeMultipart();
		}
		return this.multipartFiles;
	}

	/**
	 * Lazily initialize the multipart request, if possible.
	 * Only called if not already eagerly initialized.
	 */
	// 如果可能，延迟初始化 Multipart 请求。仅在尚未进行即时初始化时调用。
	protected void initializeMultipart() {
		throw new IllegalStateException("Multipart request not initialized");
	}

}
