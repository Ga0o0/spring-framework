/*
 * Copyright 2002-2018 the original author or authors.
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

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * MVC View for a web interaction. Implementations are responsible for rendering
 * content, and exposing the model. A single view exposes multiple model attributes.
 *
 * <p>This class and the MVC approach associated with it is discussed in Chapter 12 of
 * <a href="https://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>View implementations may differ widely. An obvious implementation would be
 * JSP-based. Other implementations might be XSLT-based, or use an HTML generation library.
 * This interface is designed to avoid restricting the range of possible implementations.
 *
 * <p>Views should be beans. They are likely to be instantiated as beans by a ViewResolver.
 * As this interface is stateless, view implementations should be thread-safe.
 *
 * @author Rod Johnson
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @see org.springframework.web.servlet.view.AbstractView
 * @see org.springframework.web.servlet.view.InternalResourceView
 */
// MVC 视图用于 Web 交互。实现负责渲染内容并公开模型。单个视图公开多个模型属性。
//
// <p>此类及其相关的 MVC 方法在 Rod Johnson 所著的《Expert One-On-One J2EE Design and Development》（Wrox，2002 年）第 12 章中进行了讨论。
//
// <p>视图的实现可能差异很大。一种显而易见的实现是基于 JSP 的。其他实现可能基于 XSLT，或者使用 HTML 生成库。此接口旨在避免限制可能的实现范围。
//
// <p>视图应该是 bean。它们很可能由 ViewResolver 实例化为 bean。由于此接口是无状态的，因此视图实现应该是线程安全的。
public interface View {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the response status code.
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 * @since 3.0
	 */
	// 包含响应状态代码的 {@link HttpServletRequest} 属性的名称。
	// <p>注意：此属性不需要得到所有 View 实现的支持。
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a Map with path variables.
	 * The map consists of String-based URI template variable names as keys and their corresponding
	 * Object-based values -- extracted from segments of the URL and type converted.
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 * @since 3.1
	 */
	// 包含路径变量映射的 {@link HttpServletRequest} 属性的名称。
	// 该映射包含基于字符串的 URI 模板变量名作为键，以及其对应的基于对象的值（从 URL 的各个部分提取并进行类型转换）。
	// <p>注意：并非所有 View 实现都要求支持此属性。
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";

	/**
	 * The {@link org.springframework.http.MediaType} selected during content negotiation,
	 * which may be more specific than the one the View is configured with. For example:
	 * "application/vnd.example-v1+xml" vs "application/*+xml".
	 * @since 3.2
	 */
	// 内容协商期间选择的 {@link org.springframework.http.MediaType}，可能比视图配置的更具体。
	// 例如：“application/vnd.example-v1+xml” vs “application/*+xml”。
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";


	/**
	 * Return the content type of the view, if predetermined.
	 * <p>Can be used to check the view's content type upfront,
	 * i.e. before an actual rendering attempt.
	 * @return the content type String (optionally including a character set),
	 * or {@code null} if not predetermined
	 */
	// 如果已预先确定，则返回视图的内容类型。
	// <p>可用于预先检查视图的内容类型，即在实际渲染尝试之前。
	// @return 内容类型字符串（可选包含字符集），如果未预先确定，则返回 {@code null}
	@Nullable
	default String getContentType() {
		return null;
	}

	/**
	 * Render the view given the specified model.
	 * <p>The first step will be preparing the request: In the JSP case, this would mean
	 * setting model objects as request attributes. The second step will be the actual
	 * rendering of the view, for example including the JSP via a RequestDispatcher.
	 * @param model a Map with name Strings as keys and corresponding model
	 * objects as values (Map can also be {@code null} in case of empty model)
	 * @param request current HTTP request
	 * @param response he HTTP response we are building
	 * @throws Exception if rendering failed
	 */
	// 根据指定的模型渲染视图。
	// <p>第一步是准备请求：在 JSP 中，这意味着将模型对象设置为请求属性。第二步是实际渲染视图，例如通过 RequestDispatcher 包含 JSP。
	// @param model 一个 Map，以名称字符串为键，以对应的模型对象为值（如果模型为空，则 Map 也可以为 {@code null}）
	// @param request 当前 HTTP 请求
	// @param respond 我们正在构建的 HTTP 响应
	// @throws Exception，如果渲染失败
	void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

}
