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

package org.springframework.web.servlet.mvc.method.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;

/**
 * Allows customizing the response after the execution of an {@code @ResponseBody}
 * or a {@code ResponseEntity} controller method but before the body is written
 * with an {@code HttpMessageConverter}.
 *
 * <p>Implementations may be registered directly with
 * {@code RequestMappingHandlerAdapter} and {@code ExceptionHandlerExceptionResolver}
 * or more likely annotated with {@code @ControllerAdvice} in which case they
 * will be auto-detected by both.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 * @param <T> the body type
 */
// 允许在执行 {@code @ResponseBody} 或 {@code ResponseEntity} 控制器方法之后但在使用 {@code HttpMessageConverter} 写入正文之前自定义响应。
//
// <p>实现可以直接使用 {@code RequestMappingHandlerAdapter} 和 {@code ExceptionHandlerExceptionResolver} 注册，
// 或者更有可能使用 {@code @ControllerAdvice} 进行注释，在这种情况下，它们将被两者自动检测。
public interface ResponseBodyAdvice<T> {

	/**
	 * Whether this component supports the given controller method return type
	 * and the selected {@code HttpMessageConverter} type.
	 * @param returnType the return type
	 * @param converterType the selected converter type
	 * @return {@code true} if {@link #beforeBodyWrite} should be invoked;
	 * {@code false} otherwise
	 */
	// 此组件是否支持给定的控制器方法返回类型和所选的 {@code HttpMessageConverter} 类型。
	// @param returnType 返回类型
	// @param converterType 所选转换器类型
	// @return {@code true} 如果需要调用 {@link #beforeBodyWrite}；否则为 {@code false}
	boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType);

	/**
	 * Invoked after an {@code HttpMessageConverter} is selected and just before
	 * its write method is invoked.
	 * @param body the body to be written
	 * @param returnType the return type of the controller method
	 * @param selectedContentType the content type selected through content negotiation
	 * @param selectedConverterType the converter type selected to write to the response
	 * @param request the current request
	 * @param response the current response
	 * @return the body that was passed in or a modified (possibly new) instance
	 */
	// 在选择 {@code HttpMessageConverter} 之后，在其 write 方法调用之前调用。
	// @param body 待写入的 body
	// @param returnType 控制器方法的返回类型
	// @param selectedContentType 通过内容协商选择的内容类型
	// @param selectedConverterType 所选的用于写入响应的转换器类型
	// @param request 当前请求
	// @param respond 当前响应
	// @return 传入的 body 或修改后的（可能是新的）实例
	@Nullable
	T beforeBodyWrite(@Nullable T body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response);

}
