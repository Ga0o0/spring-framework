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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of the
 * {@link org.springframework.web.multipart.MultipartHttpServletRequest}
 * interface. Provides management of pre-generated parameter values.
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 29.09.2003
 * @see org.springframework.web.multipart.MultipartResolver
 */
// {@link org.springframework.web.multipart.MultipartHttpServletRequest} 接口的默认实现。提供对预生成参数值的管理。
public class DefaultMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private static final String CONTENT_TYPE = "Content-Type";

	@Nullable
	private Map<String, String[]> multipartParameters;

	@Nullable
	private Map<String, String> multipartParameterContentTypes;


	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the servlet request to wrap
	 * @param mpFiles a map of the multipart files
	 * @param mpParams a map of the parameters to expose,
	 * with Strings as keys and String arrays as values
	 */
	// 将给定的 HttpServletRequest 封装为 MultipartHttpServletRequest。
	// @param request 需要封装的 Servlet 请求
	// @param mpFiles 多部分文件映射
	// @param mpParams 需要暴露的参数映射，以字符串为键，字符串数组为值
	public DefaultMultipartHttpServletRequest(HttpServletRequest request, MultiValueMap<String, MultipartFile> mpFiles,
			Map<String, String[]> mpParams, Map<String, String> mpParamContentTypes) {

		super(request);
		setMultipartFiles(mpFiles);
		setMultipartParameters(mpParams);
		setMultipartParameterContentTypes(mpParamContentTypes);
	}

	/**
	 * Wrap the given HttpServletRequest in a MultipartHttpServletRequest.
	 * @param request the servlet request to wrap
	 */
	// 将给定的 HttpServletRequest 包装在 MultipartHttpServletRequest 中。
	// @param request 要包装的 servlet 请求
	public DefaultMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}


	@Override
	@Nullable
	public String getParameter(String name) {
		String[] values = getMultipartParameters().get(name);
		if (values != null) {
			return (values.length > 0 ? values[0] : null);
		}
		return super.getParameter(name);
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] parameterValues = super.getParameterValues(name);
		String[] mpValues = getMultipartParameters().get(name);
		if (mpValues == null) {
			return parameterValues;
		}
		if (parameterValues == null || getQueryString() == null) {
			return mpValues;
		}
		else {
			String[] result = new String[mpValues.length + parameterValues.length];
			System.arraycopy(mpValues, 0, result, 0, mpValues.length);
			System.arraycopy(parameterValues, 0, result, mpValues.length, parameterValues.length);
			return result;
		}
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Map<String, String[]> multipartParameters = getMultipartParameters();
		if (multipartParameters.isEmpty()) {
			return super.getParameterNames();
		}

		Set<String> paramNames = new LinkedHashSet<>();
		paramNames.addAll(Collections.list(super.getParameterNames()));
		paramNames.addAll(multipartParameters.keySet());
		return Collections.enumeration(paramNames);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> result = new LinkedHashMap<>();
		Enumeration<String> names = getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, getParameterValues(name));
		}
		return result;
	}

	@Override
	@Nullable
	public String getMultipartContentType(String paramOrFileName) {
		MultipartFile file = getFile(paramOrFileName);
		if (file != null) {
			return file.getContentType();
		}
		else {
			return getMultipartParameterContentTypes().get(paramOrFileName);
		}
	}

	@Override
	@Nullable
	public HttpHeaders getMultipartHeaders(String paramOrFileName) {
		String contentType = getMultipartContentType(paramOrFileName);
		if (contentType != null) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(CONTENT_TYPE, contentType);
			return headers;
		}
		else {
			return null;
		}
	}


	/**
	 * Set a Map with parameter names as keys and String array objects as values.
	 * To be invoked by subclasses on initialization.
	 */
	// 设置一个 Map，其键为参数名称，值是 String 数组对象。子类初始化时会调用此方法。
	protected final void setMultipartParameters(Map<String, String[]> multipartParameters) {
		this.multipartParameters = multipartParameters;
	}

	/**
	 * Obtain the multipart parameter Map for retrieval,
	 * lazily initializing it if necessary.
	 * @see #initializeMultipart()
	 */
	// 获取用于检索的多部分参数 Map，并在必要时进行延迟初始化。
	protected Map<String, String[]> getMultipartParameters() {
		if (this.multipartParameters == null) {
			initializeMultipart();
		}
		return this.multipartParameters;
	}

	/**
	 * Set a Map with parameter names as keys and content type Strings as values.
	 * To be invoked by subclasses on initialization.
	 */
	// 设置一个 Map，其键为参数名称，值是内容类型字符串。子类初始化时会调用此方法。
	protected final void setMultipartParameterContentTypes(Map<String, String> multipartParameterContentTypes) {
		this.multipartParameterContentTypes = multipartParameterContentTypes;
	}

	/**
	 * Obtain the multipart parameter content type Map for retrieval,
	 * lazily initializing it if necessary.
	 * @see #initializeMultipart()
	 */
	// 获取用于检索的多部分参数内容类型 Map，并在必要时进行延迟初始化。
	protected Map<String, String> getMultipartParameterContentTypes() {
		if (this.multipartParameterContentTypes == null) {
			initializeMultipart();
		}
		return this.multipartParameterContentTypes;
	}

}
