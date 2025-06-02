/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.web.method.support;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Strategy interface for resolving method parameters into argument values in
 * the context of a given request.
 *
 * @author Arjen Poutsma
 * @since 3.1
 * @see HandlerMethodReturnValueHandler
 */
// 策略接口，用于在给定请求的上下文中将方法参数解析为参数值。
public interface HandlerMethodArgumentResolver {

	/**
	 * Whether the given {@linkplain MethodParameter method parameter} is
	 * supported by this resolver.
	 * @param parameter the method parameter to check
	 * @return {@code true} if this resolver supports the supplied parameter;
	 * {@code false} otherwise
	 */
	// 此解析器是否支持给定的 {@linkplain MethodParameter 方法参数}。
	// @param parameter 要检查的方法参数
	// @return 如果此解析器支持提供的参数，则返回 {@code true}；否则返回 {@code false}。
	boolean supportsParameter(MethodParameter parameter);

	/**
	 * Resolves a method parameter into an argument value from a given request.
	 * A {@link ModelAndViewContainer} provides access to the model for the
	 * request. A {@link WebDataBinderFactory} provides a way to create
	 * a {@link WebDataBinder} instance when needed for data binding and
	 * type conversion purposes.
	 * @param parameter the method parameter to resolve. This parameter must
	 * have previously been passed to {@link #supportsParameter} which must
	 * have returned {@code true}.
	 * @param mavContainer the ModelAndViewContainer for the current request
	 * @param webRequest the current request
	 * @param binderFactory a factory for creating {@link WebDataBinder} instances
	 * @return the resolved argument value, or {@code null} if not resolvable
	 * @throws Exception in case of errors with the preparation of argument values
	 */
	// 将方法参数解析为给定请求的参数值。{@link ModelAndViewContainer} 提供对请求模型的访问。
	// {@link WebDataBinderFactory} 提供一种在需要进行数据绑定和类型转换时创建 {@link WebDataBinder} 实例的方法。
	// @param parameter 需要解析的方法参数。此参数必须先前已传递给 {@link #supportsParameter}，且必须返回 {@code true}。
	// @param mavContainer 当前请求的 ModelAndViewContainer
	// @param webRequest 当前请求
	// @param binderFactory 用于创建 {@link WebDataBinder} 实例的工厂
	// @return 解析后的参数值，如果无法解析则返回 {@code null}
	// @throws Exception，如果在准备参数值时出现错误
	@Nullable
	Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception;

}
