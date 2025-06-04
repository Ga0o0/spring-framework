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

package org.springframework.web.servlet.handler;

import java.util.Set;
import java.util.function.Predicate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.Ordered;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Abstract base class for {@link HandlerExceptionResolver} implementations.
 *
 * <p>Supports mapped {@linkplain #setMappedHandlers handlers} and
 * {@linkplain #setMappedHandlerClasses handler classes} that the resolver
 * should be applied to and implements the {@link Ordered} interface.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 */
// {@link HandlerExceptionResolver} 实现的抽象基类。
//
// <p>支持解析器应应用到的映射 {@linkplain #setMappedHandlers 处理程序} 和
// {@linkplain #setMappedHandlerClasses 处理程序类}，并实现 {@link Ordered} 接口。
public abstract class AbstractHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

	private static final String HEADER_CACHE_CONTROL = "Cache-Control";


	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	private int order = Ordered.LOWEST_PRECEDENCE;

	@Nullable
	private Predicate<Object> mappedHandlerPredicate;

	@Nullable
	private Set<?> mappedHandlers;

	@Nullable
	private Class<?>[] mappedHandlerClasses;

	@Nullable
	private Log warnLogger;

	private boolean preventResponseCaching = false;


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Use a {@code Predicate} to determine which handlers this exception
	 * resolver applies to, including when the request was not mapped in which
	 * case the handler is {@code null}.
	 * <p>If no handler predicate, nor handlers, nor handler classes are set,
	 * the exception resolver applies to all handlers.
	 * @since 6.1.2
	 */
	public void setMappedHandlerPredicate(Predicate<Object> predicate) {
		this.mappedHandlerPredicate =
				(this.mappedHandlerPredicate != null ? this.mappedHandlerPredicate.and(predicate) : predicate);
	}

	/**
	 * Specify the set of handlers that this exception resolver should apply to.
	 * <p>If no handler predicate, nor handlers, nor handler classes are set,
	 * the exception resolver applies to all handlers.
	 * @see #setMappedHandlerPredicate(Predicate)
	 */
	public void setMappedHandlers(Set<?> mappedHandlers) {
		this.mappedHandlers = mappedHandlers;
	}

	/**
	 * Specify the set of classes that this exception resolver should apply to.
	 * The resolver will only apply to handlers of the specified types; the
	 * specified types may be interfaces or superclasses of handlers as well.
	 * <p>If no handler predicate, nor handlers, nor handler classes are set,
	 * the exception resolver applies to all handlers.
	 * @see #setMappedHandlerPredicate(Predicate)
	 */
	public void setMappedHandlerClasses(Class<?>... mappedHandlerClasses) {
		this.mappedHandlerClasses = mappedHandlerClasses;
	}

	/**
	 * Alternative to {@link #setMappedHandlerClasses(Class[])}.
	 * @since 6.1
	 */
	public void addMappedHandlerClass(Class<?> mappedHandlerClass) {
		this.mappedHandlerClasses = (this.mappedHandlerClasses != null ?
				ObjectUtils.addObjectToArray(this.mappedHandlerClasses, mappedHandlerClass) :
				new Class<?>[] {mappedHandlerClass});
	}

	/**
	 * Return the {@link #setMappedHandlerClasses(Class[]) configured} mapped
	 * handler classes.
	 */
	@Nullable
	protected Class<?>[] getMappedHandlerClasses() {
		return this.mappedHandlerClasses;
	}

	/**
	 * Set the log category for warn logging. The name will be passed to the underlying logger
	 * implementation through Commons Logging, getting interpreted as a log category according
	 * to the logger's configuration. If {@code null} or empty String is passed, warn logging
	 * is turned off.
	 * <p>By default there is no warn logging although subclasses like
	 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}
	 * can change that default. Specify this setting to activate warn logging into a specific
	 * category. Alternatively, override the {@link #logException} method for custom logging.
	 * @see org.apache.commons.logging.LogFactory#getLog(String)
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public void setWarnLogCategory(String loggerName) {
		this.warnLogger = (StringUtils.hasLength(loggerName) ? LogFactory.getLog(loggerName) : null);
	}

	/**
	 * Specify whether to prevent HTTP response caching for any view resolved
	 * by this exception resolver.
	 * <p>Default is {@code false}. Switch this to {@code true} in order to
	 * automatically generate HTTP response headers that suppress response caching.
	 */
	public void setPreventResponseCaching(boolean preventResponseCaching) {
		this.preventResponseCaching = preventResponseCaching;
	}


	/**
	 * Check whether this resolver is supposed to apply (i.e. if the supplied handler
	 * matches any of the configured {@linkplain #setMappedHandlers handlers} or
	 * {@linkplain #setMappedHandlerClasses handler classes}), and then delegate
	 * to the {@link #doResolveException} template method.
	 */
	// 检查该解析器是否应该应用（即，提供的处理程序是否与任何配置的 {@linkplain #setMappedHandlers 处理程序} 或
	// {@linkplain #setMappedHandlerClasses 处理程序类} 匹配），然后委托给 {@link #doResolveException} 模板方法。
	@Override
	@Nullable
	public ModelAndView resolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {

		// 检查此解析器是否应该应用于给定的处理程序。
		if (shouldApplyTo(request, handler)) {
			// 为异常情况准备响应。
			prepareResponse(ex, response);
			// 实际解决处理程序执行期间抛出的给定异常，并在适当的情况下返回表示特定错误页面的 {@link ModelAndView}。
			ModelAndView result = doResolveException(request, response, handler, ex);
			if (result != null) {
				// Print debug message when warn logger is not enabled. --> 译文：当未启用警告记录器时打印调试消息。
				if (logger.isDebugEnabled() && (this.warnLogger == null || !this.warnLogger.isWarnEnabled())) {
					logger.debug(buildLogMessage(ex, request) + (result.isEmpty() ? "" : " to " + result));
				}
				// Explicitly configured warn logger in logException method. --> 在 logException 方法中明确配置警告记录器。
				logException(ex, request);
			}
			return result;
		}
		else {
			return null;
		}
	}

	/**
	 * Check whether this resolver is supposed to apply to the given handler.
	 * <p>The default implementation checks against the configured
	 * {@linkplain #setMappedHandlerPredicate(Predicate) handlerPredicate}
	 * {@linkplain #setMappedHandlers handlers} and
	 * {@linkplain #setMappedHandlerClasses handler classes}, if any.
	 * @param request current HTTP request
	 * @param handler the executed handler, or {@code null} if none chosen
	 * at the time of the exception (for example, if multipart resolution failed)
	 * @return whether this resolved should proceed with resolving the exception
	 * for the given request and handler
	 * @see #setMappedHandlers
	 * @see #setMappedHandlerClasses
	 */
	// 检查此解析器是否应该应用于给定的处理程序。
	// <p>默认实现会检查已配置的
	// {@linkplain #setMappedHandlerPredicate(Predicate) handlerPredicate}
	// {@linkplain #setMappedHandlers handlers} 和
	// {@linkplain #setMappedHandlerClasses handler classes}（如果有）。
	// @param request 当前 HTTP 请求
	// @param handler 执行的处理程序，如果在发生异常时未选择任何处理程序（例如，如果多部分解析失败），则为 {@code null}
	// @return 此已解析是否应继续解析给定请求和处理程序的异常
	protected boolean shouldApplyTo(HttpServletRequest request, @Nullable Object handler) {
		if (this.mappedHandlerPredicate != null) {
			return this.mappedHandlerPredicate.test(handler);
		}
		if (handler != null) {
			if (this.mappedHandlers != null && this.mappedHandlers.contains(handler)) {
				return true;
			}
			if (this.mappedHandlerClasses != null) {
				for (Class<?> handlerClass : this.mappedHandlerClasses) {
					if (handlerClass.isInstance(handler)) {
						return true;
					}
				}
			}
		}
		return !hasHandlerMappings();
	}

	/**
	 * Whether there are any handler mappings registered via
	 * {@link #setMappedHandlers(Set)}, {@link #setMappedHandlerClasses(Class[])}, or
	 * {@link #setMappedHandlerPredicate(Predicate)}.
	 * @since 5.3
	 */
	// 是否有通过 {@link #setMappedHandlers(Set)}、{@link #setMappedHandlerClasses(Class[])} 或 {@link #setMappedHandlerPredicate(Predicate)} 注册的处理程序映射。
	protected boolean hasHandlerMappings() {
		return (this.mappedHandlers != null || this.mappedHandlerClasses != null ||
				this.mappedHandlerPredicate != null);
	}

	/**
	 * Log the given exception at warn level, provided that warn logging has been
	 * activated through the {@link #setWarnLogCategory "warnLogCategory"} property.
	 * <p>Calls {@link #buildLogMessage} in order to determine the concrete message to log.
	 * @param ex the exception that got thrown during handler execution
	 * @param request current HTTP request (useful for obtaining metadata)
	 * @see #setWarnLogCategory
	 * @see #buildLogMessage
	 * @see org.apache.commons.logging.Log#warn(Object, Throwable)
	 */
	// 假设已通过 {@link #setWarnLogCategory "warnLogCategory"} 属性激活警告日志记录，则以警告级别记录给定的异常。
	// <p>调用 {@link #buildLogMessage} 以确定要记录的具体消息。
	// @param ex 处理程序执行期间引发的异常
	// @param request 当前 HTTP 请求（用于获取元数据）
	protected void logException(Exception ex, HttpServletRequest request) {
		if (this.warnLogger != null && this.warnLogger.isWarnEnabled()) {
			this.warnLogger.warn(buildLogMessage(ex, request));
		}
	}

	/**
	 * Build a log message for the given exception, occurred during processing the given request.
	 * @param ex the exception that got thrown during handler execution
	 * @param request current HTTP request (useful for obtaining metadata)
	 * @return the log message to use
	 */
	// 为给定的异常构建一条日志消息，该异常发生在处理给定的请求期间。
	// @param ex 处理程序执行期间抛出的异常
	// @param request 当前 HTTP 请求（用于获取元数据）
	// @return 要使用的日志消息
	protected String buildLogMessage(Exception ex, HttpServletRequest request) {
		return "Resolved [" + LogFormatUtils.formatValue(ex, -1, true) + "]";
	}

	/**
	 * Prepare the response for the exceptional case.
	 * <p>The default implementation prevents the response from being cached,
	 * if the {@link #setPreventResponseCaching "preventResponseCaching"} property
	 * has been set to "true".
	 * @param ex the exception that got thrown during handler execution
	 * @param response current HTTP response
	 * @see #preventCaching
	 */
	// 为异常情况准备响应。
	// <p>如果 {@link #setPreventResponseCaching "preventResponseCaching"} 属性已设置为“true”，则默认实现将阻止响应被缓存。
	// @param ex 处理程序执行期间抛出的异常
	// @param respond 当前 HTTP 响应
	protected void prepareResponse(Exception ex, HttpServletResponse response) {
		if (this.preventResponseCaching) {
			preventCaching(response);
		}
	}

	/**
	 * Prevents the response from being cached, through setting corresponding
	 * HTTP {@code Cache-Control: no-store} header.
	 * @param response current HTTP response
	 */
	// 通过设置相应的 HTTP {@code Cache-Control: no-store} 标头来防止响应被缓存。
	// @param respond 当前 HTTP 响应
	protected void preventCaching(HttpServletResponse response) {
		response.addHeader(HEADER_CACHE_CONTROL, "no-store");
	}


	/**
	 * Actually resolve the given exception that got thrown during handler execution,
	 * returning a {@link ModelAndView} that represents a specific error page if appropriate.
	 * <p>May be overridden in subclasses, in order to apply specific exception checks.
	 * Note that this template method will be invoked <i>after</i> checking whether this
	 * resolver applies ("mappedHandlers" etc), so an implementation may simply proceed
	 * with its actual exception handling.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the executed handler, or {@code null} if none chosen at the time
	 * of the exception (for example, if multipart resolution failed)
	 * @param ex the exception that got thrown during handler execution
	 * @return a corresponding {@code ModelAndView} to forward to,
	 * or {@code null} for default processing in the resolution chain
	 */
	// 实际解决处理程序执行期间抛出的给定异常，并在适当的情况下返回表示特定错误页面的 {@link ModelAndView}。
	// <p>可在子类中重写，以应用特定的异常检查。请注意，此模板方法将在<i>之后</i>检查此解析器（“mappedHandlers”等）是否适用，因此实现可以继续执行其实际的异常处理。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @param handler 执行的处理程序，如果在发生异常时未选择任何处理程序（例如，如果多部分解析失败），则返回 {@code null}
	// @param ex 处理程序执行期间抛出的异常
	// @return 相应的 {@code ModelAndView} 进行转发，或者 {@code null} 用于解析链中的默认处理
	@Nullable
	protected abstract ModelAndView doResolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex);

}
