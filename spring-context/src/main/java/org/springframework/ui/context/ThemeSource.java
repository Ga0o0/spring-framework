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

package org.springframework.ui.context;

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects that can resolve {@link Theme Themes}.
 * This enables parameterization and internationalization of messages
 * for a given 'theme'.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see Theme
 * @deprecated as of 6.0 in favor of using CSS, without direct replacement
 */
// 接口由能够解析 {@link Theme Themes} 的对象实现。这可以实现给定“主题”消息的参数化和国际化。
@Deprecated(since = "6.0")
public interface ThemeSource {

	/**
	 * Return the Theme instance for the given theme name.
	 * <p>The returned Theme will resolve theme-specific messages, codes,
	 * file paths, etc (e.g. CSS and image files in a web environment).
	 * @param themeName the name of the theme
	 * @return the corresponding Theme, or {@code null} if none defined.
	 * Note that, by convention, a ThemeSource should at least be able to
	 * return a default Theme for the default theme name "theme" but may also
	 * return default Themes for other theme names.
	 * @see org.springframework.web.servlet.theme.AbstractThemeResolver#ORIGINAL_DEFAULT_THEME_NAME
	 */
	// 返回指定主题名称的 Theme 实例。
	// <p>返回的主题将解析特定于主题的消息、代码、文件路径等（例如，Web 环境中的 CSS 和图像文件）。
	// @param themeName 主题名称
	// @return 相应的主题，如果未定义则返回 {@code null}。
	// 请注意，按照惯例，ThemeSource 至少应该能够为默认主题名称“theme”返回一个默认主题，
	// 但也可以返回其他主题名称的默认主题。
	@Nullable
	Theme getTheme(String themeName);

}
