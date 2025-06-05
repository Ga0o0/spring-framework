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
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpRequest;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * A base class for resolving method argument values by reading from the body of
 * a request with {@link HttpMessageConverter HttpMessageConverters}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
// 通过使用 {@link HttpMessageConverter HttpMessageConverters} 从请求主体中读取来解析方法参数值的基类。
public abstract class AbstractMessageConverterMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final Set<HttpMethod> SUPPORTED_METHODS = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

	private static final Object NO_VALUE = new Object();


	protected final Log logger = LogFactory.getLog(getClass());

	protected final List<HttpMessageConverter<?>> messageConverters;

	private final RequestResponseBodyAdviceChain advice;


	/**
	 * Basic constructor with converters only.
	 */
	public AbstractMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> converters) {
		this(converters, null);
	}

	/**
	 * Constructor with converters and {@code Request~} and {@code ResponseBodyAdvice}.
	 * @since 4.2
	 */
	public AbstractMessageConverterMethodArgumentResolver(List<HttpMessageConverter<?>> converters,
			@Nullable List<Object> requestResponseBodyAdvice) {

		Assert.notEmpty(converters, "'messageConverters' must not be empty");
		this.messageConverters = converters;
		this.advice = new RequestResponseBodyAdviceChain(requestResponseBodyAdvice);
	}


	/**
	 * Return the configured {@link RequestBodyAdvice} and
	 * {@link RequestBodyAdvice} where each instance may be wrapped as a
	 * {@link org.springframework.web.method.ControllerAdviceBean ControllerAdviceBean}.
	 */
	// 返回已配置的 {@link RequestBodyAdvice} 和 {@link RequestBodyAdvice}，
	// 其中每个实例都可以包装为 {@link org.springframework.web.method.ControllerAdviceBean ControllerAdviceBean}。
	RequestResponseBodyAdviceChain getAdvice() {
		return this.advice;
	}

	/**
	 * Create the method argument value of the expected parameter type by
	 * reading from the given request.
	 * @param <T> the expected type of the argument value to be created
	 * @param webRequest the current request
	 * @param parameter the method parameter descriptor (may be {@code null})
	 * @param paramType the type of the argument value to be created
	 * @return the created method argument value
	 * @throws IOException if the reading from the request fails
	 * @throws HttpMediaTypeNotSupportedException if no suitable message converter is found
	 */
	@Nullable
	protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
			Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		HttpInputMessage inputMessage = createInputMessage(webRequest);
		return readWithMessageConverters(inputMessage, parameter, paramType);
	}

	/**
	 * Create the method argument value of the expected parameter type by reading
	 * from the given HttpInputMessage.
	 * @param <T> the expected type of the argument value to be created
	 * @param inputMessage the HTTP input message representing the current request
	 * @param parameter the method parameter descriptor
	 * @param targetType the target type, not necessarily the same as the method
	 * parameter type, e.g. for {@code HttpEntity<String>}.
	 * @return the created method argument value
	 * @throws IOException if the reading from the request fails
	 * @throws HttpMediaTypeNotSupportedException if no suitable message converter is found
	 */
	// 通过读取给定的 HttpInputMessage 来创建与预期参数类型对应的方法参数值。
	// @param <T> 待创建参数值的预期类型
	// @param inputMessage 表示当前请求的 HTTP 输入消息
	// @param parameter 方法参数描述符
	// @param targetType 目标类型，不必与方法参数类型相同，例如 {@code HttpEntity<String>}。
	// @return 创建的方法参数值
	// 如果从请求读取失败，则抛出 IOException 异常
	// 如果未找到合适的消息转换器，则抛出 HttpMediaTypeNotSupportedException 异常
	@Nullable
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		// 返回此方法参数的包含类。
		Class<?> contextClass = parameter.getContainingClass();
		Class<T> targetClass = (targetType instanceof Class clazz ? clazz : null);
		if (targetClass == null) {
			ResolvableType resolvableType = ResolvableType.forMethodParameter(parameter);
			// 将此类型解析为 {@link java.lang.Class}，如果无法解析，则返回 {@code null}。
			targetClass = (Class<T>) resolvableType.resolve();
		}

		MediaType contentType;
		boolean noContentType = false;
		try {
			// 返回正文的 {@linkplain MediaType 媒体类型}，由 {@code Content-Type} 标头指定。
			contentType = inputMessage.getHeaders().getContentType();
		}
		catch (InvalidMediaTypeException ex) {
			throw new HttpMediaTypeNotSupportedException(
					ex.getMessage(), getSupportedMediaTypes(targetClass != null ? targetClass : Object.class));
		}
		if (contentType == null) {
			noContentType = true;
			contentType = MediaType.APPLICATION_OCTET_STREAM;
		}

		HttpMethod httpMethod = (inputMessage instanceof HttpRequest httpRequest ? httpRequest.getMethod() : null);
		Object body = NO_VALUE;

		EmptyBodyCheckingHttpInputMessage message = null;
		try {
			message = new EmptyBodyCheckingHttpInputMessage(inputMessage); // 检查 HttpInputMessage 的空主体
			for (HttpMessageConverter<?> converter : this.messageConverters) {
				Class<HttpMessageConverter<?>> converterType = (Class<HttpMessageConverter<?>>) converter.getClass();
				GenericHttpMessageConverter<?> genericConverter =
						(converter instanceof GenericHttpMessageConverter ghmc ? ghmc : null);
				// GenericHttpMessageConverter.canRead()
				if (genericConverter != null ? genericConverter.canRead(targetType, contextClass, contentType) :
						// HttpMessageConverter.canRead()
						(targetClass != null && converter.canRead(targetClass, contentType))) {
					if (message.hasBody()) { // body != null
						// invoke RequestBodyAdvice.supports() and RequestBodyAdvice.beforeBodyRead()
						HttpInputMessage msgToUse =
								getAdvice().beforeBodyRead(message, parameter, targetType, converterType);
						// invoke GenericHttpMessageConverter.read()
						body = (genericConverter != null ? genericConverter.read(targetType, contextClass, msgToUse) :
								// invoke HttpMessageConverter.read()
								((HttpMessageConverter<T>) converter).read(targetClass, msgToUse));
						// invoke RequestBodyAdvice.supports() and RequestBodyAdvice.afterBodyRead()
						body = getAdvice().afterBodyRead(body, msgToUse, parameter, targetType, converterType);
					}
					else {
						// invoke RequestBodyAdvice.supports() and RequestBodyAdvice.handleEmptyBody()
						body = getAdvice().handleEmptyBody(null, message, parameter, targetType, converterType);
					}
					break;
				}
			}
			if (body == NO_VALUE && noContentType && !message.hasBody()) {
				// invoke RequestBodyAdvice.supports() and RequestBodyAdvice.handleEmptyBody()
				body = getAdvice().handleEmptyBody(
						null, message, parameter, targetType, NoContentTypeHttpMessageConverter.class);
			}
		}
		catch (IOException ex) {
			throw new HttpMessageNotReadableException("I/O error while reading input message", ex, inputMessage);
		}
		finally {
			if (message != null && message.hasBody()) {
				// 如果需要的话，允许关闭主体流，例如多部分请求中的部分流。
				closeStreamIfNecessary(message.getBody());
			}
		}

		if (body == NO_VALUE) {
			if (httpMethod == null || !SUPPORTED_METHODS.contains(httpMethod) || (noContentType && !message.hasBody())) {
				return null;
			}
			throw new HttpMediaTypeNotSupportedException(contentType,
					getSupportedMediaTypes(targetClass != null ? targetClass : Object.class), httpMethod);
		}

		MediaType selectedContentType = contentType;
		Object theBody = body;
		LogFormatUtils.traceDebug(logger, traceOn -> {
			String formatted = LogFormatUtils.formatValue(theBody, !traceOn);
			return "Read \"" + selectedContentType + "\" to [" + formatted + "]";
		});

		return body;
	}

	/**
	 * Create a new {@link HttpInputMessage} from the given {@link NativeWebRequest}.
	 * @param webRequest the web request to create an input message from
	 * @return the input message
	 */
	// 通过给定的 {@link NativeWebRequest} 创建一个新的 {@link HttpInputMessage}。
	// @param webRequest 用于创建输入消息的 Web 请求
	// @return 输入消息
	protected ServletServerHttpRequest createInputMessage(NativeWebRequest webRequest) {
		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Assert.state(servletRequest != null, "No HttpServletRequest");
		return new ServletServerHttpRequest(servletRequest);
	}

	/**
	 * Validate the binding target if applicable.
	 * <p>The default implementation checks for {@code @jakarta.validation.Valid},
	 * Spring's {@link org.springframework.validation.annotation.Validated},
	 * and custom annotations whose name starts with "Valid".
	 * @param binder the DataBinder to be used
	 * @param parameter the method parameter descriptor
	 * @since 4.1.5
	 * @see #isBindExceptionRequired
	 */
	// 如果适用，验证绑定目标。
	// <p>默认实现会检查 {@code @jakarta.validation.Valid}、Spring 的
	// {@link org.springframework.validation.annotation.Validated} 以及名称以“Valid”开头的自定义注解。
	// @param binder 要使用的 DataBinder
	// @param parameter 方法参数描述符
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		Annotation[] annotations = parameter.getParameterAnnotations();
		for (Annotation ann : annotations) {
			// 通过给定的注解确定所有验证提示。
			Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
			if (validationHints != null) {
				// 使用给定的验证提示调用指定的验证器（如果有）。
				binder.validate(validationHints);
				break;
			}
		}
	}

	/**
	 * Whether to raise a fatal bind exception on validation errors.
	 * @param binder the data binder used to perform data binding
	 * @param parameter the method parameter descriptor
	 * @return {@code true} if the next method argument is not of type {@link Errors}
	 * @since 4.1.5
	 */
	// 是否在验证错误时引发致命绑定异常。
	// @param binder 用于执行数据绑定的数据绑定器
	// @param parameter 方法参数描述符
	// 如果下一个方法参数不是 {@link Errors} 类型，则 @return {@code true}
	protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
		int i = parameter.getParameterIndex();
		Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
		boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
		return !hasBindingResult;
	}

	/**
	 * Return the media types supported by all provided message converters sorted
	 * by specificity via {@link MimeTypeUtils#sortBySpecificity(List)}.
	 * @since 5.3.4
	 */
	protected List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
		Set<MediaType> mediaTypeSet = new LinkedHashSet<>();
		for (HttpMessageConverter<?> converter : this.messageConverters) {
			mediaTypeSet.addAll(converter.getSupportedMediaTypes(clazz));
		}
		List<MediaType> result = new ArrayList<>(mediaTypeSet);
		MimeTypeUtils.sortBySpecificity(result);
		return result;
	}

	/**
	 * Adapt the given argument against the method parameter, if necessary.
	 * @param arg the resolved argument
	 * @param parameter the method parameter descriptor
	 * @return the adapted argument, or the original resolved argument as-is
	 * @since 4.3.5
	 */
	// 如有必要，根据方法参数调整给定参数。
	// @param arg 已解析参数
	// @param parameter 方法参数描述符
	// @return 已调整后的参数，或按原样返回原始已解析参数
	@Nullable
	protected Object adaptArgumentIfNecessary(@Nullable Object arg, MethodParameter parameter) {
		if (parameter.getParameterType() == Optional.class) {
			if (arg == null || (arg instanceof Collection<?> collection && collection.isEmpty()) ||
					(arg instanceof Object[] array && array.length == 0)) {
				return Optional.empty();
			}
			else {
				return Optional.of(arg);
			}
		}
		return arg;
	}

	/**
	 * Allow for closing the body stream if necessary,
	 * e.g. for part streams in a multipart request.
	 */
	// 如果需要的话，允许关闭主体流，例如多部分请求中的部分流。
	void closeStreamIfNecessary(InputStream body) {
		// No-op by default: A standard HttpInputMessage exposes the HTTP request stream
		// (ServletRequest#getInputStream), with its lifecycle managed by the container.
		// --> 译文：默认情况下无操作：标准 HttpInputMessage 公开 HTTP 请求流（ServletRequest#getInputStream），其生命周期由容器管理。
	}


	private static class EmptyBodyCheckingHttpInputMessage implements HttpInputMessage {

		private final HttpHeaders headers;

		@Nullable
		private final InputStream body;

		public EmptyBodyCheckingHttpInputMessage(HttpInputMessage inputMessage) throws IOException {
			this.headers = inputMessage.getHeaders();
			InputStream inputStream = inputMessage.getBody();
			if (inputStream.markSupported()) {
				inputStream.mark(1);
				this.body = (inputStream.read() != -1 ? inputStream : null);
				inputStream.reset();
			}
			else {
				PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
				int b = pushbackInputStream.read();
				if (b == -1) {
					this.body = null;
				}
				else {
					this.body = pushbackInputStream;
					pushbackInputStream.unread(b);
				}
			}
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}

		@Override
		public InputStream getBody() {
			return (this.body != null ? this.body : InputStream.nullInputStream());
		}

		public boolean hasBody() {
			return (this.body != null);
		}
	}


	/**
	 * Placeholder HttpMessageConverter type to pass to RequestBodyAdvice if there
	 * is no content-type and no content. In that case, we may not find a converter,
	 * but RequestBodyAdvice have a chance to provide it via handleEmptyBody.
	 */
	private static class NoContentTypeHttpMessageConverter implements HttpMessageConverter<String> {

		@Override
		public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
			return false;
		}

		@Override
		public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
			return false;
		}

		@Override
		public List<MediaType> getSupportedMediaTypes() {
			return Collections.emptyList();
		}

		@Override
		public String read(Class<? extends String> clazz, HttpInputMessage inputMessage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void write(String s, @Nullable MediaType contentType, HttpOutputMessage outputMessage) {
			throw new UnsupportedOperationException();
		}
	}

}
