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

package org.springframework.web.servlet;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.lang.Nullable;

/**
 * Extension of {@link LocaleResolver} that adds support for a rich locale context
 * (potentially including locale and time zone information).
 *
 * <p>Also provides {@code default} implementations of {@link #resolveLocale} and
 * {@link #setLocale} which delegate to {@link #resolveLocaleContext} and
 * {@link #setLocaleContext}, respectively.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 * @see org.springframework.context.i18n.LocaleContext
 * @see org.springframework.context.i18n.TimeZoneAwareLocaleContext
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @see org.springframework.web.servlet.support.RequestContext#getTimeZone
 * @see org.springframework.web.servlet.support.RequestContextUtils#getTimeZone
 */
// {@link LocaleResolver} 的扩展，增加了对丰富语言环境上下文的支持（可能包含语言环境和时区信息）。
//
// <p>还提供了 {@code default} 的 {@link #resolveLocale} 和 {@link #setLocale} 实现，
// 分别委托给 {@link #resolveLocaleContext} 和 {@link #setLocaleContext}。
public interface LocaleContextResolver extends LocaleResolver {

	/**
	 * Resolve the current locale context via the given request.
	 * <p>This is primarily intended for framework-level processing; consider using
	 * {@link org.springframework.web.servlet.support.RequestContextUtils} or
	 * {@link org.springframework.web.servlet.support.RequestContext} for
	 * application-level access to the current locale and/or time zone.
	 * <p>The returned context may be a
	 * {@link org.springframework.context.i18n.TimeZoneAwareLocaleContext},
	 * containing a locale with associated time zone information.
	 * Simply apply an {@code instanceof} check and downcast accordingly.
	 * <p>Custom resolver implementations may also return extra settings in
	 * the returned context, which again can be accessed through downcasting.
	 * @param request the request to resolve the locale context for
	 * @return the current locale context (never {@code null}
	 * @see #resolveLocale(HttpServletRequest)
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getTimeZone
	 */
	// 通过给定的请求解析当前的语言环境上下文。
	// <p>这主要用于框架级处理；考虑使用 {@link org.springframework.web.servlet.support.RequestContextUtils} 或
	// {@link org.springframework.web.servlet.support.RequestContext} 进行应用程序级访问当前语言环境和/或时区。
	// <p>返回的上下文可能是 {@link org.springframework.context.i18n.TimeZoneAwareLocaleContext}，包含带有相关时区信息的语言环境。
	// 只需应用 {@code instanceof} 检查并进行相应的向下转换即可。
	// <p>自定义解析器实现还可以在返回的上下文中返回额外的设置，这些设置也可以通过向下转换进行访问。
	// @param request 解析语言环境上下文的请求
	// @return 当前语言环境上下文（永远不会 {@code null}
	LocaleContext resolveLocaleContext(HttpServletRequest request);

	/**
	 * Set the current locale context to the given one,
	 * potentially including a locale with associated time zone information.
	 * @param request the request to be used for locale modification
	 * @param response the response to be used for locale modification
	 * @param localeContext the new locale context, or {@code null} to clear the locale
	 * @throws UnsupportedOperationException if the LocaleResolver implementation
	 * does not support dynamic changing of the locale or time zone
	 * @see #setLocale(HttpServletRequest, HttpServletResponse, Locale)
	 * @see org.springframework.context.i18n.SimpleLocaleContext
	 * @see org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext
	 */
	// 将当前语言环境上下文设置为给定的上下文，可能包含带有相关时区信息的语言环境。
	// @param request 用于修改语言环境的请求
	// @param respond 用于修改语言环境的响应
	// @param localeContext 新的语言环境上下文，或 {@code null} 清除语言环境
	// 如果 LocaleResolver 实现不支持动态更改语言环境或时区，则抛出 UnsupportedOperationException
	void setLocaleContext(HttpServletRequest request, @Nullable HttpServletResponse response,
			@Nullable LocaleContext localeContext);

	/**
	 * Default implementation of {@link LocaleResolver#resolveLocale(HttpServletRequest)}
	 * that delegates to {@link #resolveLocaleContext(HttpServletRequest)}, falling
	 * back to {@link HttpServletRequest#getLocale()} if necessary.
	 * @param request the request to resolve the locale for
	 * @return the current locale (never {@code null})
	 * @since 6.0
	 */
	// {@link LocaleResolver#resolveLocale(HttpServletRequest)} 的默认实现，委托给 {@link #resolveLocaleContext(HttpServletRequest)}，
	// 必要时回退到 {@link HttpServletRequest#getLocale()}。
	// @param request 需要解析语言环境的请求
	// @return 当前语言环境（永不为 {@code null}）
	@Override
	default Locale resolveLocale(HttpServletRequest request) {
		Locale locale = resolveLocaleContext(request).getLocale();
		return (locale != null ? locale : request.getLocale());
	}

	/**
	 * Default implementation of {@link LocaleResolver#setLocale(HttpServletRequest,
	 * HttpServletResponse, Locale)} that delegates to
	 * {@link #setLocaleContext(HttpServletRequest, HttpServletResponse, LocaleContext)},
	 * using a {@link SimpleLocaleContext}.
	 * @param request the request to be used for locale modification
	 * @param response the response to be used for locale modification
	 * @param locale the new locale, or {@code null} to clear the locale
	 * @throws UnsupportedOperationException if the LocaleResolver implementation
	 * does not support dynamic changing of the locale
	 * @since 6.0
	 */
	// {@link LocaleResolver#setLocale(HttpServletRequest, HttpServletResponse, Locale)} 的默认实现，
	// 委托给 {@link #setLocaleContext(HttpServletRequest, HttpServletResponse, LocaleContext)}，使用 {@link SimpleLocaleContext}。
	// @param request 用于修改语言环境的请求
	// @param respond 用于修改语言环境的响应
	// @param locale 新的语言环境，或 {@code null} 清除语言环境
	// 如果 LocaleResolver 实现不支持动态更改语言环境，则抛出 UnsupportedOperationException
	@Override
	default void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
		setLocaleContext(request, response, (locale != null ? new SimpleLocaleContext(locale) : null));
	}

}
