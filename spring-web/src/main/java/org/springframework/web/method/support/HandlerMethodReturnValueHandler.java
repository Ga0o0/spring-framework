/*
 * Copyright 2002-2022 the original author or authors.
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
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Strategy interface to handle the value returned from the invocation of a
 * handler method.
 *
 * @author Arjen Poutsma
 * @since 3.1
 * @see HandlerMethodArgumentResolver
 */
// 策略接口用于处理处理程序方法调用返回的值。
public interface HandlerMethodReturnValueHandler {

	/**
	 * Whether the given {@linkplain MethodParameter method return type} is
	 * supported by this handler.
	 * @param returnType the method return type to check
	 * @return {@code true} if this handler supports the supplied return type;
	 * {@code false} otherwise
	 */
	// 此处理程序是否支持给定的 {@linkplain MethodParameter 方法返回类型}。
	// @param returnType 要检查的方法返回类型
	// @return 如果此处理程序支持提供的返回类型，则返回 {@code true}；否则返回 {@code false}。
	boolean supportsReturnType(MethodParameter returnType);

	/**
	 * Handle the given return value by adding attributes to the model and
	 * setting a view or setting the
	 * {@link ModelAndViewContainer#setRequestHandled} flag to {@code true}
	 * to indicate the response has been handled directly.
	 * @param returnValue the value returned from the handler method
	 * @param returnType the type of the return value. This type must have
	 * previously been passed to {@link #supportsReturnType} which must
	 * have returned {@code true}.
	 * @param mavContainer the ModelAndViewContainer for the current request
	 * @param webRequest the current request
	 * @throws Exception if the return value handling results in an error
	 */
	// 通过向模型添加属性并设置视图或将 {@link ModelAndViewContainer#setRequestHandled} 标志
	// 设置为 {@code true} 来处理给定的返回值，以指示响应已被直接处理。
	// @param returnValue 处理程序方法的返回值
	// @param returnType 返回值的类型。此类型必须先前已传递给 {@link #supportsReturnType}，而后者必须返回 {@code true}。
	// @param mavContainer 当前请求的 ModelAndViewContainer
	// @param webRequest 当前请求
	// @throws Exception 如果返回值处理导致错误
	void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception;

}
