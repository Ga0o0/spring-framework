/*
 * Copyright 2002-2017 the original author or authors.
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

import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Wrapper for a JSP or other resource within the same web application.
 * Exposes model objects as request attributes and forwards the request to
 * the specified resource URL using a {@link jakarta.servlet.RequestDispatcher}.
 *
 * <p>A URL for this view is supposed to specify a resource within the web
 * application, suitable for RequestDispatcher's {@code forward} or
 * {@code include} method.
 *
 * <p>If operating within an already included request or within a response that
 * has already been committed, this view will fall back to an include instead of
 * a forward. This can be enforced by calling {@code response.flushBuffer()}
 * (which will commit the response) before rendering the view.
 *
 * <p>Typical usage with {@link InternalResourceViewResolver} looks as follows,
 * from the perspective of the DispatcherServlet context definition:
 *
 * <pre class="code">&lt;bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver"&gt;
 *   &lt;property name="prefix" value="/WEB-INF/jsp/"/&gt;
 *   &lt;property name="suffix" value=".jsp"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * Every view name returned from a handler will be translated to a JSP
 * resource (for example: "myView" &rarr; "/WEB-INF/jsp/myView.jsp"), using
 * this view class by default.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see jakarta.servlet.RequestDispatcher#forward
 * @see jakarta.servlet.RequestDispatcher#include
 * @see jakarta.servlet.ServletResponse#flushBuffer
 * @see InternalResourceViewResolver
 * @see JstlView
 */
// 包装同一 Web 应用中的 JSP 或其他资源。
// 将模型对象作为请求属性公开，并使用 {@link jakarta.servlet.RequestDispatcher} 将请求转发到指定的资源 URL。
//
// <p>此视图的 URL 应指定 Web 应用中的资源，适用于 RequestDispatcher 的 {@code forward} 或 {@code include} 方法。
//
// <p>如果在已包含的请求或已提交的响应中进行操作，此视图将回退到包含而不是转发。
// 这可以通过在渲染视图之前调用 {@code respond.flushBuffer()}（这将提交响应）来强制执行。
//
// <p>从 DispatcherServlet 上下文定义的角度来看，{@link InternalResourceViewResolver} 的典型用法如下：
//
// <pre class="code">
// <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
// 		<property name="prefix" value="/WEB-INF/jsp/"/>
// 		<property name="suffix" value=".jsp"/>
// </bean>
// </pre>
//
// 从处理程序返回的每个视图名称都将转换为 JSP 资源（例如：“myView” &rarr; “/WEB-INF/jsp/myView.jsp”），默认情况下使用此视图类。
public class InternalResourceView extends AbstractUrlBasedView {

	private boolean alwaysInclude = false;

	private boolean preventDispatchLoop = false;


	/**
	 * Constructor for use as a bean.
	 * @see #setUrl
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView() {
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 * @see #setAlwaysInclude
	 */
	public InternalResourceView(String url) {
		super(url);
	}

	/**
	 * Create a new InternalResourceView with the given URL.
	 * @param url the URL to forward to
	 * @param alwaysInclude whether to always include the view rather than forward to it
	 */
	public InternalResourceView(String url, boolean alwaysInclude) {
		super(url);
		this.alwaysInclude = alwaysInclude;
	}


	/**
	 * Specify whether to always include the view rather than forward to it.
	 * <p>Default is "false". Switch this flag on to enforce the use of a
	 * Servlet include, even if a forward would be possible.
	 * @see jakarta.servlet.RequestDispatcher#forward
	 * @see jakarta.servlet.RequestDispatcher#include
	 * @see #useInclude(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	/**
	 * Set whether to explicitly prevent dispatching back to the
	 * current handler path.
	 * <p>Default is "false". Switch this to "true" for convention-based
	 * views where a dispatch back to the current handler path is a
	 * definitive error.
	 */
	public void setPreventDispatchLoop(boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}

	/**
	 * An ApplicationContext is not strictly required for InternalResourceView.
	 */
	@Override
	protected boolean isContextRequired() {
		return false;
	}


	/**
	 * Render the internal resource given the specified model.
	 * This includes setting the model as request attributes.
	 */
	// 根据指定的模型渲染内部资源。这包括将模型设置为请求属性。
	@Override
	protected void renderMergedOutputModel(
			Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// Expose the model object as request attributes. --> 译文：将模型对象作为请求属性公开。
		exposeModelAsRequestAttributes(model, request);

		// Expose helpers as request attributes, if any. --> 译文：将辅助函数作为请求属性公开（如果有）。
		exposeHelpers(request);

		// Determine the path for the request dispatcher. --> 译文：确定请求调度器的路径。
		String dispatcherPath = prepareForRendering(request, response); // 准备渲染，并确定要转发到（或包含）的请求调度器路径。

		// Obtain a RequestDispatcher for the target resource (typically a JSP). --> 译文：获取目标资源（通常是 JSP）的 RequestDispatcher。
		RequestDispatcher rd = getRequestDispatcher(request, dispatcherPath); // 获取用于转发/包含的 RequestDispatcher。
		if (rd == null) {
			throw new ServletException("Could not get RequestDispatcher for [" + getUrl() +
					"]: Check that the corresponding file exists within your web application archive!");
		}

		// If already included or response already committed, perform include, else forward. --> 译文：如果已经包含或响应已经提交，则执行包含，否则转发。
		if (useInclude(request, response)) { // 是否使用 jakarta.servlet.RequestDispatcher.include()
			response.setContentType(getContentType());
			if (logger.isDebugEnabled()) {
				logger.debug("Including [" + getUrl() + "]");
			}
			rd.include(request, response);
		}

		else {
			// Note: The forwarded resource is supposed to determine the content type itself. --> 译文：注意：转发的资源应该自己确定内容类型。
			if (logger.isDebugEnabled()) {
				logger.debug("Forwarding to [" + getUrl() + "]");
			}
			rd.forward(request, response);
		}
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's contexts etc.
	 * <p>Called by {@link #renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)}.
	 * The default implementation is empty. This method can be overridden to add
	 * custom helpers as request attributes.
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding attributes
	 * @see #renderMergedOutputModel
	 * @see JstlView#exposeHelpers
	 */
	// 为每个渲染操作公开其独有的辅助函数。这是必要的，这样不同的渲染操作就不会覆盖彼此的上下文等。
	// <p>由 {@link #renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)} 调用。默认实现为空。
	// 此方法可以被重写，以将自定义辅助函数添加为请求属性。
	// @param request 当前 HTTP 请求
	// @throws 如果在添加属性时发生致命错误，则抛出异常
	protected void exposeHelpers(HttpServletRequest request) throws Exception {
	}

	/**
	 * Prepare for rendering, and determine the request dispatcher path
	 * to forward to (or to include).
	 * <p>This implementation simply returns the configured URL.
	 * Subclasses can override this to determine a resource to render,
	 * typically interpreting the URL in a different manner.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return the request dispatcher path to use
	 * @throws Exception if preparations failed
	 * @see #getUrl()
	 */
	// 准备渲染，并确定要转发到（或包含）的请求调度器路径。
	// <p>此实现仅返回已配置的 URL。子类可以重写此方法来确定要渲染的资源，通常会以不同的方式解释 URL。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @return 要使用的请求调度器路径
	// @throws 异常（如果准备失败）
	protected String prepareForRendering(HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String path = getUrl();
		Assert.state(path != null, "'url' not set");

		if (this.preventDispatchLoop) {
			String uri = request.getRequestURI();
			if (path.startsWith("/") ? uri.equals(path) : uri.equals(StringUtils.applyRelativePath(uri, path))) {
				throw new ServletException("Circular view path [" + path + "]: would dispatch back " +
						"to the current handler URL [" + uri + "] again. Check your ViewResolver setup! " +
						"(Hint: This may be the result of an unspecified view, due to default view name generation.)");
			}
		}
		return path;
	}

	/**
	 * Obtain the RequestDispatcher to use for the forward/include.
	 * <p>The default implementation simply calls
	 * {@link HttpServletRequest#getRequestDispatcher(String)}.
	 * Can be overridden in subclasses.
	 * @param request current HTTP request
	 * @param path the target URL (as returned from {@link #prepareForRendering})
	 * @return a corresponding RequestDispatcher
	 */
	// 获取用于转发/包含的 RequestDispatcher。
	// <p>默认实现仅调用 {@link HttpServletRequest#getRequestDispatcher(String)}。可在子类中重写。
	// @param request 当前 HTTP 请求
	// @param path 目标 URL（由 {@link #prepareForRendering} 返回）
	// @return 对应的 RequestDispatcher
	@Nullable
	protected RequestDispatcher getRequestDispatcher(HttpServletRequest request, String path) {
		return request.getRequestDispatcher(path);
	}

	/**
	 * Determine whether to use RequestDispatcher's {@code include} or
	 * {@code forward} method.
	 * <p>Performs a check whether an include URI attribute is found in the request,
	 * indicating an include request, and whether the response has already been committed.
	 * In both cases, an include will be performed, as a forward is not possible anymore.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return {@code true} for include, {@code false} for forward
	 * @see jakarta.servlet.RequestDispatcher#forward
	 * @see jakarta.servlet.RequestDispatcher#include
	 * @see jakarta.servlet.ServletResponse#isCommitted
	 * @see org.springframework.web.util.WebUtils#isIncludeRequest
	 */
	// 确定是否使用 RequestDispatcher 的 {@code include} 或 {@code forward} 方法。
	// <p>检查请求中是否存在 include URI 属性（指示包含请求），以及响应是否已提交。在这两种情况下，都会执行包含操作，因为无法再进行转发。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @return {@code true} 表示包含，{@code false} 表示转发
	protected boolean useInclude(HttpServletRequest request, HttpServletResponse response) {
		return (this.alwaysInclude || WebUtils.isIncludeRequest(request) || response.isCommitted());
	}

}
