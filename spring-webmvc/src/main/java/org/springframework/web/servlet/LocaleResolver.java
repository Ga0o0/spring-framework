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

import org.springframework.lang.Nullable;

/**
 * Interface for web-based locale resolution strategies that allows for
 * both locale resolution via the request and locale modification via
 * request and response.
 *
 * <p>This interface allows for implementations based on request, session,
 * cookies, etc. The default implementation is
 * {@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver},
 * simply using the request's locale provided by the respective HTTP header.
 *
 * <p>Use {@link org.springframework.web.servlet.support.RequestContext#getLocale()}
 * to retrieve the current locale in controllers or views, independent
 * of the actual resolution strategy.
 *
 * <p>Note: As of Spring 4.0, there is an extended strategy interface
 * called {@link LocaleContextResolver}, allowing for resolution of
 * a {@link org.springframework.context.i18n.LocaleContext} object,
 * potentially including associated time zone information. Spring's
 * provided resolver implementations implement the extended
 * {@link LocaleContextResolver} interface wherever appropriate.
 *
 * @author Juergen Hoeller
 * @since 27.02.2003
 * @see LocaleContextResolver
 * @see org.springframework.context.i18n.LocaleContextHolder
 * @see org.springframework.web.servlet.support.RequestContext#getLocale
 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
 */
// 基于 Web 的区域设置解析策略接口，允许通过请求进行区域设置解析，也允许通过请求和响应进行区域设置修改。
//
// <p>此接口允许基于请求、会话、Cookie 等实现。默认实现是 {@link org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}，
// 它仅使用相应 HTTP 标头提供的请求区域设置。
//
// <p>使用 {@link org.springframework.web.servlet.support.RequestContext#getLocale()} 检索控制器或视图中的当前区域设置，与实际的解析策略无关。
//
// <p>注意：从 Spring 4.0 开始，有一个名为 {@link LocaleContextResolver} 的扩展策略接口，
// 允许解析 {@link org.springframework.context.i18n.LocaleContext} 对象，可能包含相关的时区信息。
// Spring 提供的解析器实现在适当的情况下实现了扩展的 {@link LocaleContextResolver} 接口。
public interface LocaleResolver {

	/**
	 * Resolve the current locale via the given request.
	 * <p>Can return a default locale as fallback in any case.
	 * @param request the request to resolve the locale for
	 * @return the current locale (never {@code null})
	 */
	// 通过给定的请求解析当前的语言环境。
	// <p>在任何情况下都可以返回默认语言环境作为后备。
	// @param request 解析语言环境的请求
	// @return 当前语言环境（永不返回 {@code null}）
	Locale resolveLocale(HttpServletRequest request);

	/**
	 * Set the current locale to the given one.
	 * @param request the request to be used for locale modification
	 * @param response the response to be used for locale modification
	 * @param locale the new locale, or {@code null} to clear the locale
	 * @throws UnsupportedOperationException if the LocaleResolver
	 * implementation does not support dynamic changing of the locale
	 */
	// 将当前语言环境设置为指定的语言环境。
	// @param request 用于修改语言环境的请求
	// @param respond 用于修改语言环境的响应
	// @param locale 新的语言环境，或 {@code null} 清除语言环境
	// 如果 LocaleResolver 实现不支持动态更改语言环境，则抛出 UnsupportedOperationException
	void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale);

}
