/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.web.context.request;

import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;

/**
 * Interface for general web request interception. Allows for being applied
 * to Servlet request by building on the {@link WebRequest} abstraction.
 *
 * <p>This interface assumes MVC-style request processing: A handler gets executed,
 * exposes a set of model objects, then a view gets rendered based on that model.
 * Alternatively, a handler may also process the request completely, with no
 * view to be rendered.
 *
 * <p>In an async processing scenario, the handler may be executed in a separate
 * thread while the main thread exits without rendering or invoking the
 * {@code postHandle} and {@code afterCompletion} callbacks. When concurrent
 * handler execution completes, the request is dispatched back in order to
 * proceed with rendering the model and all methods of this contract are invoked
 * again. For further options and comments see
 * {@code org.springframework.web.context.request.async.AsyncWebRequestInterceptor}
 *
 * <p>This interface is deliberately minimalistic to keep the dependencies of
 * generic request interceptors as minimal as feasible.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ServletWebRequest
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
// 用于通用 Web 请求拦截的接口。允许基于 {@link WebRequest} 抽象构建，应用于 Servlet 请求。
//
// <p>此接口采用 MVC 风格的请求处理方式：执行处理程序，公开一组模型对象，然后基于该模型渲染视图。
// 或者，处理程序也可以完全处理请求，不渲染任何视图。
//
// <p>在异步处理场景中，处理程序可以在单独的线程中执行，而主线程退出时不渲染或调用 {@code postHandle} 和 {@code afterCompletion} 回调。
// 并发处理程序执行完成后，请求将被重新调度以继续渲染模型，并且此契约的所有方法都将再次调用。
// 更多选项和注释，请参阅 {@code org.springframework.web.context.request.async.AsyncWebRequestInterceptor}
//
// <p>此接口刻意简化，以尽可能减少通用请求拦截器的依赖关系。
public interface WebRequestInterceptor {

	/**
	 * Intercept the execution of a request handler <i>before</i> its invocation.
	 * <p>Allows for preparing context resources (such as a Hibernate Session)
	 * and expose them as request attributes or as thread-local objects.
	 * @param request the current web request
	 * @throws Exception in case of errors
	 */
	void preHandle(WebRequest request) throws Exception;

	/**
	 * Intercept the execution of a request handler <i>after</i> its successful
	 * invocation, right before view rendering (if any).
	 * <p>Allows for modifying context resources after successful handler
	 * execution (for example, flushing a Hibernate Session).
	 * @param request the current web request
	 * @param model the map of model objects that will be exposed to the view
	 * (may be {@code null}). Can be used to analyze the exposed model
	 * and/or to add further model attributes, if desired.
	 * @throws Exception in case of errors
	 */
	void postHandle(WebRequest request, @Nullable ModelMap model) throws Exception;

	/**
	 * Callback after completion of request processing, that is, after rendering
	 * the view. Will be called on any outcome of handler execution, thus allows
	 * for proper resource cleanup.
	 * <p>Note: Will only be called if this interceptor's {@code preHandle}
	 * method has successfully completed!
	 * @param request the current web request
	 * @param ex exception thrown on handler execution, if any
	 * @throws Exception in case of errors
	 */
	void afterCompletion(WebRequest request, @Nullable Exception ex) throws Exception;

}
