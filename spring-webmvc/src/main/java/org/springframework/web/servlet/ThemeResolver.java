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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * Interface for web-based theme resolution strategies that allows for
 * both theme resolution via the request and theme modification via
 * request and response.
 *
 * <p>This interface allows for implementations based on session,
 * cookies, etc. The default implementation is
 * {@link org.springframework.web.servlet.theme.FixedThemeResolver},
 * simply using a configured default theme.
 *
 * <p>Note that this resolver is only responsible for determining the
 * current theme name. The Theme instance for the resolved theme name
 * gets looked up by DispatcherServlet via the respective ThemeSource,
 * i.e. the current WebApplicationContext.
 *
 * <p>Use {@link org.springframework.web.servlet.support.RequestContext#getTheme()}
 * to retrieve the current theme in controllers or views, independent
 * of the actual resolution strategy.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 17.06.2003
 * @see org.springframework.ui.context.Theme
 * @see org.springframework.ui.context.ThemeSource
 * @deprecated as of 6.0 in favor of using CSS, without direct replacement
 */
// 基于 Web 的主题解析策略接口，允许通过请求进行主题解析，也允许通过请求和响应进行主题修改。
//
// <p>此接口允许基于会话、Cookie 等实现。默认实现是 {@link org.springframework.web.servlet.theme.FixedThemeResolver}，即使用已配置的默认主题。
//
// <p>请注意，此解析器仅负责确定当前主题名称。DispatcherServlet 通过相应的 ThemeSource（即当前的 WebApplicationContext）查找已解析主题名称的 Theme 实例。
//
// <p>使用 {@link org.springframework.web.servlet.support.RequestContext#getTheme()} 在控制器或视图中检索当前主题，与实际的解析策略无关。
@Deprecated(since = "6.0")
public interface ThemeResolver {

	/**
	 * Resolve the current theme name via the given request.
	 * Should return a default theme as fallback in any case.
	 * @param request the request to be used for resolution
	 * @return the current theme name
	 */
	// 通过给定的请求解析当前主题名称。无论如何都应返回默认主题作为后备。
	// @param request 用于解析的请求
	// @return 当前主题名称
	String resolveThemeName(HttpServletRequest request);

	/**
	 * Set the current theme name to the given one.
	 * @param request the request to be used for theme name modification
	 * @param response the response to be used for theme name modification
	 * @param themeName the new theme name ({@code null} or empty to reset it)
	 * @throws UnsupportedOperationException if the ThemeResolver implementation
	 * does not support dynamic changing of the theme
	 */
	// 将当前主题名称设置为给定名称。
	// @param request 用于修改主题名称的请求
	// @param respond 用于修改主题名称的响应
	// @param themeName 新的主题名称（{@code null} 或为空以重置）
	// 如果 ThemeResolver 实现不支持动态更改主题，则抛出 UnsupportedOperationException
	void setThemeName(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName);

}
