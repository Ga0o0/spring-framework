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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;
import org.springframework.web.multipart.support.RequestPartServletServerHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Resolves the following method arguments:
 * <ul>
 * <li>Annotated with @{@link RequestPart}
 * <li>Of type {@link MultipartFile} in conjunction with Spring's {@link MultipartResolver} abstraction
 * <li>Of type {@code jakarta.servlet.http.Part} in conjunction with Servlet multipart requests
 * </ul>
 *
 * <p>When a parameter is annotated with {@code @RequestPart}, the content of the part is
 * passed through an {@link HttpMessageConverter} to resolve the method argument with the
 * 'Content-Type' of the request part in mind. This is analogous to what @{@link RequestBody}
 * does to resolve an argument based on the content of a regular request.
 *
 * <p>When a parameter is not annotated with {@code @RequestPart} or the name of
 * the part is not specified, the request part's name is derived from the name of
 * the method argument.
 *
 * <p>Automatic validation may be applied if the argument is annotated with any
 * {@linkplain org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationHints
 * annotations that trigger validation}. In case of validation failure, a
 * {@link MethodArgumentNotValidException} is raised and a 400 response status code returned if the
 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}
 * is configured.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @author Juergen Hoeller
 * @since 3.1
 */
// 解析以下方法参数：
// <ul>
// <li>使用 @{@link RequestPart} 注解的参数
// <li>类型为 {@link MultipartFile}，并结合 Spring 的 {@link MultipartResolver} 抽象层
// <li>类型为 {@code jakarta.servlet.http.Part}，并结合 Servlet multipart 请求
// </ul>
//
// <p>当参数使用 {@code @RequestPart} 注解时，该部分的内容会通过 {@link HttpMessageConverter} 进行转换，
// 以根据请求部分的“Content-Type”解析方法参数。这类似于 @{@link RequestBody} 根据常规请求的内容解析参数的方式。
//
// <p>当参数未使用 {@code @RequestPart} 注解或未指定部分名称时，请求部分的名称将根据方法参数的名称派生。
//
// <p>如果参数带有任何触发验证的 {@linkplain org.springframework.validation.annotation.ValidationAnnotationUtils#determineValidationHints 注释}，
// 则可以应用自动验证。如果验证失败，则会引发 {@link MethodArgumentNotValidException} 异常，并且如果配置了
// {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}，则会返回 400 响应状态码。
public class RequestPartMethodArgumentResolver extends AbstractMessageConverterMethodArgumentResolver {

	/**
	 * Basic constructor with converters only.
	 */
	public RequestPartMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters) {
		super(messageConverters);
	}

	/**
	 * Constructor with converters and {@code RequestBodyAdvice} and
	 * {@code ResponseBodyAdvice}.
	 */
	public RequestPartMethodArgumentResolver(List<HttpMessageConverter<?>> messageConverters,
			List<Object> requestResponseBodyAdvice) {

		super(messageConverters, requestResponseBodyAdvice);
	}


	/**
	 * Whether the given {@linkplain MethodParameter method parameter} is
	 * supported as multipart. Supports the following method parameters:
	 * <ul>
	 * <li>annotated with {@code @RequestPart}
	 * <li>of type {@link MultipartFile} unless annotated with {@code @RequestParam}
	 * <li>of type {@code jakarta.servlet.http.Part} unless annotated with
	 * {@code @RequestParam}
	 * </ul>
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.hasParameterAnnotation(RequestPart.class)) {
			return true;
		}
		else {
			if (parameter.hasParameterAnnotation(RequestParam.class)) {
				return false;
			}
			return MultipartResolutionDelegate.isMultipartArgument(parameter.nestedIfOptional());
		}
	}

	@Override
	@Nullable
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest request, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
		Assert.state(servletRequest != null, "No HttpServletRequest");

		RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
		boolean isRequired = ((requestPart == null || requestPart.required()) && !parameter.isOptional());

		String name = getPartName(parameter, requestPart);
		parameter = parameter.nestedIfOptional();
		Object arg = null;

		Object mpArg = MultipartResolutionDelegate.resolveMultipartArgument(name, parameter, servletRequest);
		if (mpArg != MultipartResolutionDelegate.UNRESOLVABLE) {
			arg = mpArg;
		}
		else {
			try {
				HttpInputMessage inputMessage = new RequestPartServletServerHttpRequest(servletRequest, name);
				arg = readWithMessageConverters(inputMessage, parameter, parameter.getNestedGenericParameterType());
				if (binderFactory != null) {
					ResolvableType type = ResolvableType.forMethodParameter(parameter);
					WebDataBinder binder = binderFactory.createBinder(request, arg, name, type);
					if (arg != null) {
						validateIfApplicable(binder, parameter);
						if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
							throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
						}
					}
					if (mavContainer != null) {
						mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
					}
				}
			}
			catch (MissingServletRequestPartException | MultipartException ex) {
				if (isRequired) {
					throw ex;
				}
			}
		}

		if (arg == null && isRequired) {
			if (!MultipartResolutionDelegate.isMultipartRequest(servletRequest)) {
				throw new MultipartException("Current request is not a multipart request");
			}
			else {
				throw new MissingServletRequestPartException(name);
			}
		}
		return adaptArgumentIfNecessary(arg, parameter);
	}

	private String getPartName(MethodParameter methodParam, @Nullable RequestPart requestPart) {
		String partName = (requestPart != null ? requestPart.name() : "");
		if (partName.isEmpty()) {
			partName = methodParam.getParameterName();
			if (partName == null) {
				throw new IllegalArgumentException("Request part name for argument type [" +
						methodParam.getNestedParameterType().getName() +
						"] not specified, and parameter name information not found in class file either.");
			}
		}
		return partName;
	}

	@Override
	void closeStreamIfNecessary(InputStream body) {
		// RequestPartServletServerHttpRequest exposes individual part streams,
		// potentially from temporary files -> explicit close call after resolution
		// in order to prevent file descriptor leaks.
		try {
			body.close();
		}
		catch (IOException ex) {
			// ignore
		}
	}

}
