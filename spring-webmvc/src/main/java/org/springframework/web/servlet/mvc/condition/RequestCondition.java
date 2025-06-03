/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.servlet.mvc.condition;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

/**
 * Contract for request mapping conditions.
 *
 * <p>Request conditions can be combined via {@link #combine(Object)}, matched to
 * a request via {@link #getMatchingCondition(HttpServletRequest)}, and compared
 * to each other via {@link #compareTo(Object, HttpServletRequest)} to determine
 * which is a closer match for a given request.
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 * @param <T> the type of objects that this RequestCondition can be combined
 * with and compared to
 */
// 请求映射条件的契约。
//
// <p>可以通过 {@link #combine(Object)} 组合请求条件，通过 {@link #getMatchingCondition(HttpServletRequest)} 与请求匹配，
// 并通过 {@link #compareTo(Object, HttpServletRequest)} 相互比较，以确定哪个条件与给定请求更匹配。
//
// @param <T> 可以与此 RequestCondition 组合并进行比较的对象类型
public interface RequestCondition<T> {

	/**
	 * Combine this condition with another such as conditions from a
	 * type-level and method-level {@code @RequestMapping} annotation.
	 * @param other the condition to combine with.
	 * @return a request condition instance that is the result of combining
	 * the two condition instances.
	 */
	// 将此条件与另一个条件（例如来自类型级别和方法级别 {@code @RequestMapping} 注释的条件）相结合。
	// @param other 要与之结合的条件。
	// @return 组合两个条件实例的结果的请求条件实例。
	T combine(T other);

	/**
	 * Check if the condition matches the request returning a potentially new
	 * instance created for the current request. For example a condition with
	 * multiple URL patterns may return a new instance only with those patterns
	 * that match the request.
	 * <p>For CORS pre-flight requests, conditions should match to the would-be,
	 * actual request (e.g. URL pattern, query parameters, and the HTTP method
	 * from the "Access-Control-Request-Method" header). If a condition cannot
	 * be matched to a pre-flight request it should return an instance with
	 * empty content thus not causing a failure to match.
	 * @return a condition instance in case of a match or {@code null} otherwise.
	 */
	// 检查条件是否与请求匹配，并返回为当前请求创建的潜在新实例。例如，包含多个 URL 模式的条件可能仅返回与请求匹配的模式的新实例。
	// <p>对于 CORS 预检请求，条件应与实际请求匹配（例如，URL 模式、查询参数以及 “Access-Control-Request-Method” 标头中的 HTTP 方法）。
	// 如果条件无法与预检请求匹配，则应返回一个内容为空的实例，这样才不会导致匹配失败。
	// 如果匹配，则返回一个条件实例；否则，返回 {@code null}。
	@Nullable
	T getMatchingCondition(HttpServletRequest request);

	/**
	 * Compare this condition to another condition in the context of
	 * a specific request. This method assumes both instances have
	 * been obtained via {@link #getMatchingCondition(HttpServletRequest)}
	 * to ensure they have content relevant to current request only.
	 */
	// 将此条件与特定请求上下文中的另一个条件进行比较。
	// 此方法假定两个实例均已通过 {@link #getMatchingCondition(HttpServletRequest)} 获取，以确保它们仅包含与当前请求相关的内容。
	int compareTo(T other, HttpServletRequest request);

}
