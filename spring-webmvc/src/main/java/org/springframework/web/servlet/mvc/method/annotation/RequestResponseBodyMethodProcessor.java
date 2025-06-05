/*
 * Copyright 2002-2023 the original author or authors.
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

/**
 * Resolves method arguments annotated with {@code @RequestBody} and handles return
 * values from methods annotated with {@code @ResponseBody} by reading and writing
 * to the body of the request or response with an {@link HttpMessageConverter}.
 *
 * <p>An {@code @RequestBody} method argument is also validated if it is annotated
 * with any
 * {@linkplain org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationHints
 * annotations that trigger validation}. In case of validation failure,
 * {@link MethodArgumentNotValidException} is raised and results in an HTTP 400
 * response status code if {@link DefaultHandlerExceptionResolver} is configured.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
// 解析带有 {@code @RequestBody} 注解的方法参数，并通过使用 {@link HttpMessageConverter} 读写请求或响应主体来处理带有 {@code @ResponseBody} 注解的方法的返回值。
//
// <p>如果 {@code @RequestBody} 方法参数带有任何 {@linkplain org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationHints 注解，则会对其进行验证。
// 如果配置了 {@link DefaultHandlerExceptionResolver}，则验证失败时会引发 {@link MethodArgumentNotValidException} 异常，并返回 HTTP 400 响应状态码。
public class RequestResponseBodyMethodProcessor extends AbstractMessageConverterMethodProcessor {

	/**
	 * Basic constructor with converters only. Suitable for resolving
	 * {@code @RequestBody}. For handling {@code @ResponseBody} consider also
	 * providing a {@code ContentNegotiationManager}.
	 */
	public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters) {
		super(converters);
	}

	/**
	 * Basic constructor with converters and {@code ContentNegotiationManager}.
	 * Suitable for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody} without {@code Request~} or
	 * {@code ResponseBodyAdvice}.
	 */
	public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager manager) {

		super(converters, manager);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} method arguments.
	 * For handling {@code @ResponseBody} consider also providing a
	 * {@code ContentNegotiationManager}.
	 * @since 4.2
	 */
	public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
			@Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, null, requestResponseBodyAdvice);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody}.
	 */
	public RequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, manager, requestResponseBodyAdvice);
	}


	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestBody.class);
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
				returnType.hasMethodAnnotation(ResponseBody.class));
	}

	/**
	 * Throws MethodArgumentNotValidException if validation fails.
	 * @throws HttpMessageNotReadableException if {@link RequestBody#required()}
	 * is {@code true} and there is no body content or if there is no suitable
	 * converter to read the content with.
	 */
	// 如果验证失败，则抛出 MethodArgumentNotValidException。
	// 如果 {@link RequestBody#required()} 为 {@code true} 并且没有正文内容，
	// 或者没有合适的转换器来读取内容，则抛出 @throws HttpMessageNotReadableException。
	@Override
	@Nullable
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		// （1）RequestResponseBodyMethodProcessor.resolveArgument() 代码逻辑：
		// 		使用 MessageConverters 进行参数转换，并执行 SmartValidator/Validator.validate() 进行校验
		// 		使用 MessageConverters 进行参数转换的代码逻辑：
		// 		1. GenericHttpMessageConverter.canRead()/HttpMessageConverter.canRead()
		// 		2.1. body != null
		// 			RequestBodyAdvice.supports() and RequestBodyAdvice.beforeBodyRead()
		// 			GenericHttpMessageConverter.read()/HttpMessageConverter.read()
		// 			RequestBodyAdvice.supports() and RequestBodyAdvice.afterBodyRead()
		// 		2.2. body == null
		// 			RequestBodyAdvice.supports() and RequestBodyAdvice.handleEmptyBody()
		// 		3. RequestBodyAdvice.supports() and RequestBodyAdvice.handleEmptyBody()

		parameter = parameter.nestedIfOptional();
		// 使用 MessageConverters 进行读取
		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());

		if (binderFactory != null) {
			// 确定给定参数的常规变量名称，同时考虑通用集合类型（如果有）。
			String name = Conventions.getVariableNameForParameter(parameter);
			// 返回指定 {@link MethodParameter} 的 {@code ResolvableType}。
			ResolvableType type = ResolvableType.forMethodParameter(parameter);
			WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name, type);
			if (arg != null) {
				// 如果适用，验证绑定目标。
				validateIfApplicable(binder, parameter);
				// 验证结果处理
				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
					throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
				}
			}
			if (mavContainer != null) {
				mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
			}
		}

		// 如有必要，根据方法参数调整给定参数。
		return adaptArgumentIfNecessary(arg, parameter);
	}

	@Override
	@Nullable
	protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
			Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		// 通过给定的 {@link NativeWebRequest} 创建一个新的 {@link HttpInputMessage}。
		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		// 通过读取给定的 HttpInputMessage 来创建与预期参数类型对应的方法参数值。
		Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
		if (arg == null && checkRequired(parameter)) { // 缺少必需的请求正文；检查 RequestBody.required
			throw new HttpMessageNotReadableException("Required request body is missing: " +
					parameter.getExecutable().toGenericString(), inputMessage);
		}
		return arg;
	}

	protected boolean checkRequired(MethodParameter parameter) {
		RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
		return (requestBody != null && requestBody.required() && !parameter.isOptional());
	}

	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
			throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

		mavContainer.setRequestHandled(true);
		// 通过给定的 {@link NativeWebRequest} 创建一个新的 {@link HttpInputMessage}。
		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

		if (returnValue instanceof ProblemDetail detail) {
			outputMessage.setStatusCode(HttpStatusCode.valueOf(detail.getStatus()));
			if (detail.getInstance() == null) {
				URI path = URI.create(inputMessage.getServletRequest().getRequestURI());
				detail.setInstance(path);
			}
		}

		// 1. genericConverter != null ? GenericHttpMessageConverter.canWrite() : HttpMessageConverter.canWrite()
		// 2. ResponseBodyAdvice.supports() and ResponseBodyAdvice.beforeBodyWrite()
		// 3. body != null
		// 		if genericConverter != null
		//			GenericHttpMessageConverter.write()
		//		else genericConverter == null
		// 			HttpMessageConverter.write()

		// Try even with null return value. ResponseBodyAdvice could get involved.
		// --> 译文：即使返回值为空，也请尝试。ResponseBodyAdvice 可能会参与其中。
		// 将指定的返回类型写入指定的输出消息。
		writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
	}

}
