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

package org.springframework.core.io.support;

import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * Utility class for determining whether a given URL is a resource
 * location that can be loaded via a {@link ResourcePatternResolver}.
 *
 * <p>Callers will usually assume that a location is a relative path
 * if the {@link #isUrl(String)} method returns {@code false}.
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 */
// 用于判断给定 URL 是否为可通过 {@link ResourcePatternResolver} 加载的资源位置的实用程序类。
//
// <p>如果 {@link #isUrl(String)} 方法返回 {@code false}，调用者通常会假定该位置为相对路径。
public abstract class ResourcePatternUtils {

	/**
	 * Return whether the given resource location is a URL: either a
	 * special "classpath" or "classpath*" pseudo URL or a standard URL.
	 * @param resourceLocation the location String to check
	 * @return whether the location qualifies as a URL
	 * @see ResourcePatternResolver#CLASSPATH_ALL_URL_PREFIX
	 * @see org.springframework.util.ResourceUtils#CLASSPATH_URL_PREFIX
	 * @see org.springframework.util.ResourceUtils#isUrl(String)
	 * @see java.net.URL
	 */
	// 返回给定资源位置是否为 URL：可以是特殊的“classpath”或“classpath*”伪 URL，也可以是标准 URL。
	// @param resourceLocation 要检查的位置字符串
	// @return 该位置是否符合 URL 的条件
	public static boolean isUrl(@Nullable String resourceLocation) {
		return (resourceLocation != null &&
				(resourceLocation.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX) ||
						ResourceUtils.isUrl(resourceLocation)));
	}

	/**
	 * Return a default {@link ResourcePatternResolver} for the given {@link ResourceLoader}.
	 * <p>This might be the {@code ResourceLoader} itself, if it implements the
	 * {@code ResourcePatternResolver} extension, or a default
	 * {@link PathMatchingResourcePatternResolver} built on the given {@code ResourceLoader}.
	 * @param resourceLoader the ResourceLoader to build a pattern resolver for
	 * (may be {@code null} to indicate a default ResourceLoader)
	 * @return the ResourcePatternResolver
	 * @see PathMatchingResourcePatternResolver
	 */
	// 为给定的 {@link ResourceLoader} 返回一个默认的 {@link ResourcePatternResolver}。
	// <p>如果 {@code ResourceLoader} 实现了 {@code ResourcePatternResolver} 扩展，则返回该 {@code ResourceLoader} 本身；
	// 如果是，则返回基于给定 {@code ResourceLoader} 构建的默认 {@link PathMatchingResourcePatternResolver}。
	// @param resourceLoader 用于构建模式解析器的 ResourceLoader
	// （可以为 {@code null}，表示使用默认 ResourceLoader）
	// @return ResourcePatternResolver
	public static ResourcePatternResolver getResourcePatternResolver(@Nullable ResourceLoader resourceLoader) {
		if (resourceLoader instanceof ResourcePatternResolver resolver) {
			return resolver;
		}
		else if (resourceLoader != null) {
			return new PathMatchingResourcePatternResolver(resourceLoader);
		}
		else {
			return new PathMatchingResourcePatternResolver();
		}
	}

}
