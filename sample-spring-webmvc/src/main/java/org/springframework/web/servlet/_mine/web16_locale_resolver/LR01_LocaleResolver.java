package org.springframework.web.servlet._mine.web16_locale_resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * LocaleResolver
 *
 * @see org.springframework.web.servlet.LocaleResolver
 */
public class LR01_LocaleResolver {

	/**
	 * @see org.springframework.web.servlet.LocaleResolver#setLocale(HttpServletRequest, HttpServletResponse, Locale)
	 * @see org.springframework.web.servlet.LocaleResolver#resolveLocale(HttpServletRequest)
	 */
	public static void main(String[] args) {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Accept-Language", "zh-CN");

		// LocaleResolver
		LocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

		// LocaleResolver#setLocale(HttpServletRequest, HttpServletResponse, Locale)
		// 将当前语言环境设置为指定的语言环境。
		// @param request 用于修改语言环境的请求
		// @param respond 用于修改语言环境的响应
		// @param locale 新的语言环境，或 {@code null} 清除语言环境
		// 如果 LocaleResolver 实现不支持动态更改语言环境，则抛出 UnsupportedOperationException
		// localeResolver.setLocale(request, response, Locale.CHINA);

		// LocaleResolver#resolveLocale(HttpServletRequest)
		// 通过给定的请求解析当前的语言环境。
		// <p>在任何情况下都可以返回默认语言环境作为后备。
		// @param request 解析语言环境的请求
		// @return 当前语言环境（永不返回 {@code null}）
		Locale locale = localeResolver.resolveLocale(request);
		System.out.println(locale);
	}
}
/*
********************************* Class API Docs *********************************
基于 Web 的区域设置解析策略接口，允许通过请求进行区域设置解析，也允许通过请求和响应进行区域设置修改。

<p>此接口允许基于请求、会话、Cookie 等实现。默认实现是 org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver，
它仅使用相应 HTTP 标头提供的请求区域设置。

<p>使用 org.springframework.web.servlet.support.RequestContext#getLocale() 检索控制器或视图中的当前区域设置，与实际的解析策略无关。

<p>注意：从 Spring 4.0 开始，有一个名为 LocaleContextResolver 的扩展策略接口，
允许解析 org.springframework.context.i18n.LocaleContext 对象，可能包含相关的时区信息。
Spring 提供的解析器实现在适当的情况下实现了扩展的 LocaleContextResolver 接口。

********************************* Class Definition *********************************
public interface LocaleResolver {
	Locale resolveLocale(HttpServletRequest request);
	void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale);
}
**/