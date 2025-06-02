/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * Strategy interface for translating an incoming
 * {@link jakarta.servlet.http.HttpServletRequest} into a
 * logical view name when no view name is explicitly supplied.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
// 当没有明确提供视图名称时，用于将传入的 {@link jakarta.servlet.http.HttpServletRequest} 转换为逻辑视图名称的策略接口。
public interface RequestToViewNameTranslator {

	/**
	 * Translate the given {@link HttpServletRequest} into a view name.
	 * @param request the incoming {@link HttpServletRequest} providing
	 * the context from which a view name is to be resolved
	 * @return the view name, or {@code null} if no default found
	 * @throws Exception if view name translation fails
	 */
	// 将给定的 {@link HttpServletRequest} 转换为视图名称。
	// @param request 传入的 {@link HttpServletRequest} 提供要从中解析视图名称的上下文
	// @return 视图名称，如果没有找到默认值，则返回 {@code null}
	// @throws Exception，如果视图名称转换失败
	@Nullable
	String getViewName(HttpServletRequest request) throws Exception;

}
