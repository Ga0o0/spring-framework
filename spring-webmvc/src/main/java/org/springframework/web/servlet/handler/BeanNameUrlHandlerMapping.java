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

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerMapping}
 * interface that maps from URLs to beans with names that start with a slash ("/"),
 * similar to how Struts maps URLs to action names.
 *
 * <p>This is the default implementation used by the
 * {@link org.springframework.web.servlet.DispatcherServlet}, along with
 * {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}.
 * Alternatively, {@link SimpleUrlHandlerMapping} allows for customizing a
 * handler mapping declaratively.
 *
 * <p>The mapping is from URL to bean name. Thus an incoming URL "/foo" would map
 * to a handler named "/foo", or to "/foo /foo2" in case of multiple mappings to
 * a single handler.
 *
 * <p>Supports direct matches (given "/test" -&gt; registered "/test") and "*"
 * matches (given "/test" -&gt; registered "/t*"). For details on the pattern
 * options, see the {@link org.springframework.web.util.pattern.PathPattern}
 * javadoc.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleUrlHandlerMapping
 */
// 实现 {@link org.springframework.web.servlet.HandlerMapping} 接口，
// 将 URL 映射到名称以斜杠 ("/") 开头的 Bean，类似于 Struts 将 URL 映射到操作名称的方式。
//
// <p>这是 {@link org.springframework.web.servlet.DispatcherServlet} 以及
// {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping} 使用的默认实现。
// 或者，{@link SimpleUrlHandlerMapping} 允许以声明方式自定义 handler 映射。
//
// <p>该映射是从 URL 到 Bean 名称的映射。因此，传入的 URL “/foo” 将映射到名为 “/foo” 的 handler ，
// 如果多个 Bean 映射到单个 handler，则将映射到 “/foo /foo2” 。
//
// <p>支持直接匹配（例如，给定 "/test" -> 注册的 "/test”）和 "*" 匹配（例如，给定 "/test" -> 注册的 "/t*"）。
// 有关模式选项的详细信息，请参阅 {@link org.springframework.web.util.pattern.PathPattern} javadoc。
public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	/**
	 * Checks name and aliases of the given bean for URLs, starting with "/".
	 */
	// 检查给定 bean 的名称和别名的 URL，以“/”开头。
	@Override
	protected String[] determineUrlsForHandler(String beanName) {
		List<String> urls = new ArrayList<>();
		if (beanName.startsWith("/")) {
			urls.add(beanName);
		}
		String[] aliases = obtainApplicationContext().getAliases(beanName);
		for (String alias : aliases) {
			if (alias.startsWith("/")) {
				urls.add(alias);
			}
		}
		return StringUtils.toStringArray(urls);
	}

}
