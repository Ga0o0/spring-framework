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

package org.springframework.web.servlet.mvc.annotation;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

/**
 * A {@link org.springframework.web.servlet.HandlerExceptionResolver
 * HandlerExceptionResolver} that uses the {@link ResponseStatus @ResponseStatus}
 * annotation to map exceptions to HTTP status codes.
 *
 * <p>This exception resolver is enabled by default in the
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
 * and the MVC Java config and the MVC namespace.
 *
 * <p>As of 4.2 this resolver also looks recursively for {@code @ResponseStatus}
 * present on cause exceptions, and as of 4.2.2 this resolver supports
 * attribute overrides for {@code @ResponseStatus} in custom composed annotations.
 *
 * <p>As of 5.0 this resolver also supports {@link ResponseStatusException}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 3.0
 * @see ResponseStatus
 * @see ResponseStatusException
 */
// {@link org.springframework.web.servlet.HandlerExceptionResolver HandlerExceptionResolver}
// 使用 {@link ResponseStatus @ResponseStatus} 注解将异常映射到 HTTP 状态代码。
//
// <p>此异常解析器在 {@link org.springframework.web.servlet.DispatcherServlet}、MVC Java 配置和 MVC 命名空间中默认启用。
//
// <p>从 4.2 开始，此解析器还会递归查找导致异常时存在的 {@code @ResponseStatus}，并且从 4.2.2 开始，
// 此解析器支持自定义组合注解中 {@code @ResponseStatus} 的属性覆盖。
//
// <p>从 5.0 开始，此解析器还支持 {@link ResponseStatusException}。
public class ResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver implements MessageSourceAware {

	@Nullable
	private MessageSource messageSource;


	@Override
	public void setMessageSource(@Nullable MessageSource messageSource) {
		this.messageSource = messageSource;
	}


	@Override
	@Nullable
	protected ModelAndView doResolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {

		try {
			if (ex instanceof ResponseStatusException rse) {
				// 处理 {@link ResponseStatusException} 的模板方法。
				return resolveResponseStatusException(rse, request, response, handler);
			}

			ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
			if (status != null) {
				// 处理 {@link ResponseStatus @ResponseStatus} 注释的模板方法。
				return resolveResponseStatus(status, request, response, handler, ex);
			}

			if (ex.getCause() instanceof Exception cause) {
				return doResolveException(request, response, handler, cause);
			}
		}
		catch (Exception resolveEx) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failure while trying to resolve exception [" + ex.getClass().getName() + "]", resolveEx);
			}
		}
		return null;
	}

	/**
	 * Template method that handles the {@link ResponseStatus @ResponseStatus} annotation.
	 * <p>The default implementation delegates to {@link #applyStatusAndReason}
	 * with the status code and reason from the annotation.
	 * @param responseStatus the {@code @ResponseStatus} annotation
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 * time of the exception, e.g. if multipart resolution failed
	 * @param ex the exception
	 * @return an empty ModelAndView, i.e. exception resolved
	 */
	// 处理 {@link ResponseStatus @ResponseStatus} 注释的模板方法。
	// <p>默认实现委托给 {@link #applyStatusAndReason}，并使用注释中的状态代码和原因。
	// @param respondStatus {@code @ResponseStatus} 注释
	// @param request 当前 HTTP 请求 @param respond 当前 HTTP 响应
	// @param handler 执行的处理程序，如果在发生异常时未选择任何处理程序，则返回 {@code null}，例如，如果多部分解析失败
	// @param ex 异常
	// @return 一个空的 ModelAndView，即异常已解决
	protected ModelAndView resolveResponseStatus(ResponseStatus responseStatus, HttpServletRequest request,
			HttpServletResponse response, @Nullable Object handler, Exception ex) throws Exception {

		int statusCode = responseStatus.code().value();
		String reason = responseStatus.reason();
		return applyStatusAndReason(statusCode, reason, response);
	}

	/**
	 * Template method that handles an {@link ResponseStatusException}.
	 * <p>The default implementation applies the headers from
	 * {@link ResponseStatusException#getHeaders()} and delegates to
	 * {@link #applyStatusAndReason} with the status code and reason from the
	 * exception.
	 * @param ex the exception
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or {@code null} if none chosen at the
	 * time of the exception, e.g. if multipart resolution failed
	 * @return an empty ModelAndView, i.e. exception resolved
	 * @since 5.0
	 */
	// 处理 {@link ResponseStatusException} 的模板方法。
	// <p>默认实现会应用 {@link ResponseStatusException#getHeaders()} 中的标头，并将异常的状态码和原因委托给 {@link #applyStatusAndReason}。
	// @param ex 异常
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @param handler 执行的处理程序，如果在发生异常时未选择任何处理程序（例如，如果多部分解析失败），则返回 {@code null}。
	// @return 一个空的 ModelAndView，即异常已解决
	protected ModelAndView resolveResponseStatusException(ResponseStatusException ex,
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler) throws Exception {

		ex.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
		return applyStatusAndReason(ex.getStatusCode().value(), ex.getReason(), response);
	}

	/**
	 * Apply the resolved status code and reason to the response.
	 * <p>The default implementation sends a response error using
	 * {@link HttpServletResponse#sendError(int)} or
	 * {@link HttpServletResponse#sendError(int, String)} if there is a reason
	 * and then returns an empty ModelAndView.
	 * @param statusCode the HTTP status code
	 * @param reason the associated reason (may be {@code null} or empty)
	 * @param response current HTTP response
	 * @since 5.0
	 */
	protected ModelAndView applyStatusAndReason(int statusCode, @Nullable String reason, HttpServletResponse response)
			throws IOException {

		if (!StringUtils.hasLength(reason)) {
			response.sendError(statusCode);
		}
		else {
			String resolvedReason = (this.messageSource != null ?
					this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale()) :
					reason);
			response.sendError(statusCode, resolvedReason);
		}
		return new ModelAndView();
	}

}
