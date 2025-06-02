/*
 * Copyright 2002-2020 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects that can resolve views by name.
 *
 * <p>View state doesn't change during the running of the application,
 * so implementations are free to cache views.
 *
 * <p>Implementations are encouraged to support internationalization,
 * i.e. localized view resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.view.ContentNegotiatingViewResolver
 * @see org.springframework.web.servlet.view.BeanNameViewResolver
 */
// 接口由能够按名称解析视图的对象实现。
//
// <p>视图状态在应用程序运行期间不会发生变化，因此实现可以自由缓存视图。
//
// <p>鼓励实现支持国际化，即本地化视图解析。
public interface ViewResolver {

	/**
	 * Resolve the given view by name.
	 * <p>Note: To allow for ViewResolver chaining, a ViewResolver should
	 * return {@code null} if a view with the given name is not defined in it.
	 * However, this is not required: Some ViewResolvers will always attempt
	 * to build View objects with the given name, unable to return {@code null}
	 * (rather throwing an exception when View creation failed).
	 * @param viewName name of the view to resolve
	 * @param locale the Locale in which to resolve the view.
	 * ViewResolvers that support internationalization should respect this.
	 * @return the View object, or {@code null} if not found
	 * (optional, to allow for ViewResolver chaining)
	 * @throws Exception if the view cannot be resolved
	 * (typically in case of problems creating an actual View object)
	 */
	// 按名称解析给定的视图。
	// <p>注意：为允许 ViewResolver 链式调用，如果未定义具有给定名称的视图，ViewResolver 应返回 {@code null}。
	// 但这不是必需的：某些 ViewResolver 将始终尝试构建具有给定名称的视图对象，无法返回 {@code null}（而是在视图创建失败时抛出异常）。
	// @param viewName 要解析的视图的名称
	// @param locale 解析视图的语言环境。支持国际化的 ViewResolver 应该尊重这一点。
	// @return View 对象，如果未找到则返回 {@code null}（可选，以允许 ViewResolver 链式调用）
	// @throws Exception 如果无法解析视图（通常在创建实际 View 对象时出现问题）
	@Nullable
	View resolveViewName(String viewName, Locale locale) throws Exception;

}
