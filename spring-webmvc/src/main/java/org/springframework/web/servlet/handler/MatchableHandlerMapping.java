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

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Additional interface that a {@link HandlerMapping} can implement to expose
 * a request matching API aligned with its internal request matching
 * configuration and implementation.
 *
 * @author Rossen Stoyanchev
 * @since 4.3.1
 * @see HandlerMappingIntrospector
 */
// {@link HandlerMapping} 可以实现的附加接口，用于公开与其内部请求匹配配置和实现一致的请求匹配 API。
public interface MatchableHandlerMapping extends HandlerMapping {

	/**
	 * Return the parser of this {@code HandlerMapping}, if configured in which
	 * case pre-parsed patterns are used.
	 * @since 5.3
	 */
	// 如果已配置，则返回此 {@code HandlerMapping} 的解析器，在这种情况下使用预解析模式。
	@Nullable
	default PathPatternParser getPatternParser() {
		return null;
	}

	/**
	 * Determine whether the request matches the given pattern. Use this method
	 * when {@link #getPatternParser()} returns {@code null} which means that the
	 * {@code HandlerMapping} is using String pattern matching.
	 * @param request the current request
	 * @param pattern the pattern to match
	 * @return the result from request matching, or {@code null} if none
	 */
	// 判断请求是否与给定的模式匹配。当 {@link #getPatternParser()} 返回 {@code null} 时使用此方法，
	// 这意味着 {@code HandlerMapping} 正在使用字符串模式匹配。
	// @param request 当前请求
	// @param pattern 要匹配的模式
	// @return 请求匹配的结果，如果没有匹配，则返回 {@code null}
	@Nullable
	RequestMatchResult match(HttpServletRequest request, String pattern);

}
