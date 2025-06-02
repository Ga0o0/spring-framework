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

package org.springframework.web.servlet.i18n;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

/**
 * {@link LocaleResolver} implementation that looks for a match between locales
 * in the {@code Accept-Language} header and a list of configured supported
 * locales.
 *
 * <p>See {@link #setSupportedLocales(List)} for further details on how
 * supported and requested locales are matched.
 *
 * <p>Note: This implementation does not support {@link #setLocale} since the
 * {@code Accept-Language} header can only be changed by changing the client's
 * locale settings.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 27.02.2003
 * @see jakarta.servlet.http.HttpServletRequest#getLocale()
 */
// {@link LocaleResolver} 实现会在 {@code Accept-Language} 标头中的语言环境与已配置的支持语言环境列表之间查找匹配项。
//
// <p>有关如何匹配支持语言环境和请求语言环境的更多详细信息，请参阅 {@link #setSupportedLocales(List)}。
//
// <p>注意：此实现不支持 {@link #setLocale}，因为 {@code Accept-Language} 标头只能通过更改客户端的语言环境设置来更改。
public class AcceptHeaderLocaleResolver extends AbstractLocaleResolver {

	private final List<Locale> supportedLocales = new ArrayList<>(4);


	/**
	 * Configure the list of supported locales to compare and match against
	 * {@link HttpServletRequest#getLocales() requested locales}.
	 * <p>In order for a supported locale to be considered a match, it must match
	 * on both country and language. If you want to support a language-only match
	 * as a fallback, you must configure the language explicitly as a supported
	 * locale.
	 * <p>For example, if the supported locales are {@code ["de-DE","en-US"]},
	 * then a request for {@code "en-GB"} will not match, and neither will a
	 * request for {@code "en"}. If you want to support additional locales for a
	 * given language such as {@code "en"}, then you must add it to the list of
	 * supported locales.
	 * <p>If there is no match, then the {@link #setDefaultLocale(Locale)
	 * defaultLocale} is used, if configured, or otherwise falling back on
	 * {@link HttpServletRequest#getLocale()}.
	 * @param locales the supported locales
	 * @since 4.3
	 */
	// 配置受支持的语言环境列表，以便与 {@link HttpServletRequest#getLocales() 请求的语言环境} 进行比较和匹配。
	// <p>要使受支持的语言环境被视为匹配，它必须同时匹配国家/地区和语言。如果您想支持仅匹配语言作为后备，则必须将该语言明确配置为受支持的语言环境。
	// <p>例如，如果受支持的语言环境为 {@code ["de-DE","en-US"]}，则对 {@code "en-GB"} 的请求将不匹配，对 {@code "en"} 的请求也不会匹配。
	// 如果您想为给定语言支持其他语言环境（例如 {@code "en"}），则必须将其添加到受支持的语言环境列表中。
	// <p>如果没有匹配，则使用 {@link #setDefaultLocale(Locale) defaultLocale}（如果已配置），
	// 否则将回退到 {@link HttpServletRequest#getLocale()}。@param locales 受支持的语言环境
	public void setSupportedLocales(List<Locale> locales) {
		this.supportedLocales.clear();
		this.supportedLocales.addAll(locales);
	}

	/**
	 * Get the configured list of supported locales.
	 * @since 4.3
	 */
	// 获取已配置的支持语言环境列表。
	public List<Locale> getSupportedLocales() {
		return this.supportedLocales;
	}


	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		Locale defaultLocale = getDefaultLocale();
		if (defaultLocale != null && request.getHeader("Accept-Language") == null) {
			return defaultLocale;
		}
		Locale requestLocale = request.getLocale();
		List<Locale> supportedLocales = getSupportedLocales();
		if (supportedLocales.isEmpty() || supportedLocales.contains(requestLocale)) {
			return requestLocale;
		}
		Locale supportedLocale = findSupportedLocale(request, supportedLocales);
		if (supportedLocale != null) {
			return supportedLocale;
		}
		return (defaultLocale != null ? defaultLocale : requestLocale);
	}

	@Nullable
	private Locale findSupportedLocale(HttpServletRequest request, List<Locale> supportedLocales) {
		Enumeration<Locale> requestLocales = request.getLocales();
		Locale languageMatch = null;
		while (requestLocales.hasMoreElements()) {
			Locale locale = requestLocales.nextElement();
			if (supportedLocales.contains(locale)) {
				if (languageMatch == null || languageMatch.getLanguage().equals(locale.getLanguage())) {
					// Full match: language + country, possibly narrowed from earlier language-only match
					return locale;
				}
			}
			else if (languageMatch == null) {
				// Let's try to find a language-only match as a fallback
				for (Locale supportedLocale : supportedLocales) {
					if (!StringUtils.hasLength(supportedLocale.getCountry()) &&
							supportedLocale.getLanguage().equals(locale.getLanguage())) {
						languageMatch = supportedLocale;
						break;
					}
				}
			}
		}
		return languageMatch;
	}

	@Override
	public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {
		throw new UnsupportedOperationException(
				"Cannot change HTTP Accept-Language header - use a different locale resolution strategy");
	}

}
