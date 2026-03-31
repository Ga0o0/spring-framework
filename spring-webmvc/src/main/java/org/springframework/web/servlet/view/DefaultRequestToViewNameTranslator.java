/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.web.servlet.view;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.util.ServletRequestPathUtils;

/**
 * {@link RequestToViewNameTranslator} that simply transforms the URI of
 * the incoming request into a view name.
 *
 * <p>Can be explicitly defined as the {@code viewNameTranslator} bean in a
 * {@link org.springframework.web.servlet.DispatcherServlet} context.
 * Otherwise, a plain default instance will be used.
 *
 * <p>The default transformation simply strips leading and trailing slashes
 * as well as the file extension of the URI, and returns the result as the
 * view name with the configured {@link #setPrefix prefix} and a
 * {@link #setSuffix suffix} added as appropriate.
 *
 * <p>The stripping of the leading slash and file extension can be disabled
 * using the {@link #setStripLeadingSlash stripLeadingSlash} and
 * {@link #setStripExtension stripExtension} properties, respectively.
 *
 * <p>Find below some examples of request to view name translation.
 * <ul>
 * <li>{@code http://localhost:8080/gamecast/display.html} &raquo; {@code display}</li>
 * <li>{@code http://localhost:8080/gamecast/displayShoppingCart.html} &raquo; {@code displayShoppingCart}</li>
 * <li>{@code http://localhost:8080/gamecast/admin/index.html} &raquo; {@code admin/index}</li>
 * </ul>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.servlet.RequestToViewNameTranslator
 * @see org.springframework.web.servlet.ViewResolver
 */
// {@link RequestToViewNameTranslator} 只是将传入请求的 URI 转换为视图名称。
//
// <p>可以在 {@link org.springframework.web.servlet.DispatcherServlet} 上下文中明确定义为 {@code viewNameTranslator} bean。
// 否则，将使用简单的默认实例。<p>默认转换只是去除 URI 的前导斜杠和尾随斜杠以及文件扩展名，并将结果作为视图名称返回，
// 并根据需要添加配置的 {@link #setPrefix prefix} 和 {@link #setSuffix suffix}。
//
// <p>可以分别使用 {@link #setStripLeadingSlash stripLeadingSlash} 和 {@link #setStripExtension stripExtension} 属性禁用去除前导斜杠和文件扩展名的功能。
//
// <p>以下是一些请求到视图名称转换的示例。
//
// <ul>
// <li>{@code http://localhost:8080/gamecast/display.html} &raquo; {@code display} </li>
// <li>{@code http://localhost:8080/gamecast/displayShoppingCart.html} &raquo; {@code displayShoppingCart} </li>
// <li>{@code http://localhost:8080/gamecast/admin/index.html} &raquo; {@code admin/index} </li>
// </ul>
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {

	private static final String SLASH = "/";


	private String prefix = "";

	private String suffix = "";

	private String separator = SLASH;

	private boolean stripLeadingSlash = true;

	private boolean stripTrailingSlash = true;

	private boolean stripExtension = true;


	/**
	 * Set the prefix to prepend to generated view names.
	 * @param prefix the prefix to prepend to generated view names
	 */
	// 设置要添加到生成的视图名称前的前缀。
	// @param prefix 要添加到生成的视图名称前的前缀
	public void setPrefix(@Nullable String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}

	/**
	 * Set the suffix to append to generated view names.
	 * @param suffix the suffix to append to generated view names
	 */
	// 设置要附加到生成的视图名称的后缀。
	// @param suffix 要附加到生成的视图名称的后缀
	public void setSuffix(@Nullable String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}

	/**
	 * Set the value that will replace '{@code /}' as the separator
	 * in the view name. The default behavior simply leaves '{@code /}'
	 * as the separator.
	 */
	// 设置视图名称中分隔符 / 的替换值。默认情况下，分隔符仍为 /。
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Set whether leading slashes should be stripped from the URI when
	 * generating the view name. Default is "true".
	 */
	// 设置生成视图名称时是否从 URI 中删除前导斜杠。默认值为 true。
	public void setStripLeadingSlash(boolean stripLeadingSlash) {
		this.stripLeadingSlash = stripLeadingSlash;
	}

	/**
	 * Set whether trailing slashes should be stripped from the URI when
	 * generating the view name. Default is "true".
	 */
	// 设置生成视图名称时是否从 URI 中删除尾部斜杠。默认值为 true。
	public void setStripTrailingSlash(boolean stripTrailingSlash) {
		this.stripTrailingSlash = stripTrailingSlash;
	}

	/**
	 * Set whether file extensions should be stripped from the URI when
	 * generating the view name. Default is "true".
	 */
	// 设置生成视图名称时是否从 URI 中去除文件扩展名。默认值为 true。
	public void setStripExtension(boolean stripExtension) {
		this.stripExtension = stripExtension;
	}


	/**
	 * Translates the request URI of the incoming {@link HttpServletRequest}
	 * into the view name based on the configured parameters.
	 * @throws IllegalArgumentException if neither a parsed RequestPath, nor a
	 * String lookupPath have been resolved and cached as a request attribute.
	 * @see ServletRequestPathUtils#getCachedPath(ServletRequest)
	 * @see #transformPath
	 */
	// 根据配置的参数将传入的 {@link HttpServletRequest} 的请求 URI 转换为视图名称。
	// 如果解析的 RequestPath 和 String lookupPath 都未解析并缓存为请求属性，则抛出 @throws IllegalArgumentException。
	@Override
	public String getViewName(HttpServletRequest request) {
		String path = ServletRequestPathUtils.getCachedPathValue(request);
		return (this.prefix + transformPath(path) + this.suffix);
	}

	/**
	 * Transform the request URI (in the context of the webapp) stripping
	 * slashes and extensions, and replacing the separator as required.
	 * @param lookupPath the lookup path for the current request,
	 * as determined by the UrlPathHelper
	 * @return the transformed path, with slashes and extensions stripped
	 * if desired
	 */
	// 转换请求 URI（在 webapp 上下文中），去除斜杠和扩展名，并根据需要替换分隔符。
	// @param lookupPath 当前请求的查找路径，由 UrlPathHelper 确定
	// @return 转换后的路径，如果需要，可以去除斜杠和扩展名
	@Nullable
	protected String transformPath(String lookupPath) {
		String path = lookupPath;
		if (this.stripLeadingSlash && path.startsWith(SLASH)) {
			path = path.substring(1);
		}
		if (this.stripTrailingSlash && path.endsWith(SLASH)) {
			path = path.substring(0, path.length() - 1);
		}
		if (this.stripExtension) {
			path = StringUtils.stripFilenameExtension(path);
		}
		if (!SLASH.equals(this.separator)) {
			path = StringUtils.replace(path, SLASH, this.separator);
		}
		return path;
	}

}
