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

package org.springframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;

/**
 * Extends {@code HandlerInterceptor} with a callback method invoked after the
 * start of asynchronous request handling.
 *
 * <p>When a handler starts an asynchronous request, the {@link DispatcherServlet}
 * exits without invoking {@code postHandle} and {@code afterCompletion} as it
 * normally does for a synchronous request, since the result of request handling
 * (e.g. ModelAndView) is likely not yet ready and will be produced concurrently
 * from another thread. In such scenarios, {@link #afterConcurrentHandlingStarted}
 * is invoked instead, allowing implementations to perform tasks such as cleaning
 * up thread-bound attributes before releasing the thread to the Servlet container.
 *
 * <p>When asynchronous handling completes, the request is dispatched to the
 * container for further processing. At this stage the {@code DispatcherServlet}
 * invokes {@code preHandle}, {@code postHandle}, and {@code afterCompletion}.
 * To distinguish between the initial request and the subsequent dispatch
 * after asynchronous handling completes, interceptors can check whether the
 * {@code jakarta.servlet.DispatcherType} of {@link jakarta.servlet.ServletRequest}
 * is {@code "REQUEST"} or {@code "ASYNC"}.
 *
 * <p>Note that {@code HandlerInterceptor} implementations may need to do work
 * when an async request times out or completes with a network error. For such
 * cases the Servlet container does not dispatch and therefore the
 * {@code postHandle} and {@code afterCompletion} methods will not be invoked.
 * Instead, interceptors can register to track an asynchronous request through
 * the {@code registerCallbackInterceptor} and {@code registerDeferredResultInterceptor}
 * methods on {@link org.springframework.web.context.request.async.WebAsyncManager
 * WebAsyncManager}. This can be done proactively on every request from
 * {@code preHandle} regardless of whether async request processing will start.
 *
 * @author Rossen Stoyanchev
 * @since 3.2
 * @see org.springframework.web.context.request.async.WebAsyncManager
 * @see org.springframework.web.context.request.async.CallableProcessingInterceptor
 * @see org.springframework.web.context.request.async.DeferredResultProcessingInterceptor
 */
// 使用在异步请求处理开始后调用的回调方法扩展 {@code HandlerInterceptor}。
//
// <p>当处理程序启动异步请求时，{@link DispatcherServlet} 会退出，
// 而不会像处理同步请求那样调用 {@code postHandle} 和 {@code afterCompletion}，
// 因为请求处理的结果（例如 ModelAndView）可能尚未准备好，并且会从另一个线程并发生成。
// 在这种情况下，会改为调用 {@link #afterConcurrentHandlingStarted}，允许实现在将线程释放到 Servlet 容器之前执行清理线程绑定属性等任务。
//
// <p>异步处理完成后，请求将被分派到容器进行进一步处理。在此阶段，{@code DispatcherServlet}
// 会调用 {@code preHandle}、{@code postHandle} 和 {@code afterCompletion}。
// 为了区分初始请求和异步处理完成后的后续调度，拦截器可以检查 {@link jakarta.servlet.ServletRequest} 的
// {@code jakarta.servlet.DispatcherType} 是 {@code "REQUEST"} 还是 {@code "ASYNC"}。
//
// <p>请注意，{@code HandlerInterceptor} 实现可能需要在异步请求超时或因网络错误而完成时执行工作。
// 在这种情况下，Servlet 容器不会进行调度，因此 {@code postHandle} 和 {@code afterCompletion} 方法不会被调用。
// 相反，拦截器可以通过 {@link org.springframework.web.context.request.async.WebAsyncManager WebAsyncManager}
// 上的 {@code registerCallbackInterceptor} 和 {@code registerDeferredResultInterceptor} 方法注册以跟踪异步请求。
// 无论异步请求处理是否启动，都可以对来自 {@code preHandle} 的每个请求主动执行此操作。
public interface AsyncHandlerInterceptor extends HandlerInterceptor {

	/**
	 * Called instead of {@code postHandle} and {@code afterCompletion}
	 * when the handler is being executed concurrently.
	 * <p>Implementations may use the provided request and response but should
	 * avoid modifying them in ways that would conflict with the concurrent
	 * execution of the handler. A typical use of this method would be to
	 * clean up thread-local variables.
	 * @param request the current request
	 * @param response the current response
	 * @param handler the handler (or {@link HandlerMethod}) that started async
	 * execution, for type and/or instance examination
	 * @throws Exception in case of errors
	 */
	// 当处理程序并发执行时，调用该方法而不是 {@code postHandle} 和 {@code afterCompletion}。
	// <p>实现可以使用提供的请求和响应，但应避免以与处理程序并发执行冲突的方式修改它们。此方法的典型用途是清理线程局部变量。
	// @param request 当前请求
	// @param respond 当前响应
	// @param handler 启动异步执行的处理程序（或 {@link HandlerMethod}），用于类型和/或实例检查
	// @throws Exception（如果发生错误）
	default void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {
	}

}
