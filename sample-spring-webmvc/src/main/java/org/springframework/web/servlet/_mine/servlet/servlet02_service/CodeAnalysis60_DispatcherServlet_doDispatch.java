package org.springframework.web.servlet._mine.servlet.servlet02_service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.util.WebUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Servlet#service(...) - DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## 1. checkMultipart
 *
 * @see org.springframework.web.servlet.DispatcherServlet#checkMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#isMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#resolveMultipart(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 2. getHandler
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerExecutionChain
 *
 * ## 3. getHandlerAdapter -> 选择一个 HandlerAdapter 来处理当前 Handler
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getHandlerAdapter(java.lang.Object)
 * @see org.springframework.web.servlet.HandlerAdapter#supports(java.lang.Object)
 *
 * ## 4. 调用已注册 interceptors 的 preHandle 方法，所有 preHandle 调用成功完成并返回 true 的 interceptors 都会调用 afterCompletion 回调。
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#applyPreHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#triggerAfterCompletion(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Exception)
 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
 *
 * ## 5. 使用给定的 handler 来处理当前请求
 *
 * @see org.springframework.web.servlet.HandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * ## 6. 视图名称翻译：执行 RequestToViewNameTranslator#getViewName(HttpServletRequest) 方法从 HttpServletRequest 转换 view 名称，并设置给 ModelAndView#view 属性
 *
 * @see org.springframework.web.servlet.DispatcherServlet#applyDefaultViewName(jakarta.servlet.http.HttpServletRequest, org.springframework.web.servlet.ModelAndView)
 * @see org.springframework.web.servlet.ModelAndView#view
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultViewName(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.RequestToViewNameTranslator#getViewName(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 7. 应用已注册 interceptors 的 postHandle 方法；即：HandlerInterceptor#postHandle()。
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#applyPostHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.servlet.ModelAndView)
 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
 */
public class CodeAnalysis60_DispatcherServlet_doDispatch {

	/**
	 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	static class CA01_DispatcherServlet extends DispatcherServlet {
		private static final long serialVersionUID = 1L;

		// 此 servlet 使用的 MultipartResolver。
		private MultipartResolver multipartResolver;
		// 此 servlet 使用的 HandlerMappings 列表。
		private List<HandlerMapping> handlerMappings;
		// 此 servlet 使用的 HandlerAdapters 列表。
		private List<HandlerAdapter> handlerAdapters;
		// 此 servlet 使用的 RequestToViewNameTranslator。
		private RequestToViewNameTranslator viewNameTranslator;
		// 此 servlet 使用的 LocaleResolver。
		private LocaleResolver localeResolver;
		// 此 servlet 使用的 ViewResolver 列表。
		private List<ViewResolver> viewResolvers;

		// 将实际的调度操作传递给处理程序。
		// <p>处理程序将通过按顺序应用 servlet 的 HandlerMappings 来获取。
		// HandlerAdapter 将通过查询 servlet 已安装的 HandlerAdapters 来获取，以找到第一个支持该处理程序类的 HandlerAdapter。
		// <p>所有 HTTP 方法都由此方法处理。由 HandlerAdapters 或处理程序本身决定哪些方法是可接受的。
		@SuppressWarnings("deprecation")
		protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
			HttpServletRequest processedRequest = request;
			HandlerExecutionChain mappedHandler = null;
			boolean multipartRequestParsed = false;

			// 获取当前请求的 {@link WebAsyncManager}，如果找不到，则创建并将其与请求关联。
			WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

			try {
				ModelAndView mv = null;
				Exception dispatchException = null;

				try {
					// 1. 将请求转换为 multipart 请求，并使 multipart 解析程序可用。如果未设置 multipart resolver ，则只需使用现有请求即可。
					//    执行 MultipartResolver#isMultipart(request) 和 MultipartResolver#resolveMultipart(request) 方法
					processedRequest = checkMultipart(request);
					multipartRequestParsed = (processedRequest != request);

					// Determine handler for the current request. -> 译文：确定当前请求的 handler
					// 2. 返回当前请求的 HandlerExecutionChain；按顺序尝试所有 handler mappings。
					// 	  HandlerExecutionChain {Object handler, List<HandlerInterceptor> interceptorList}
					//    执行 HandlerMapping#getHandler(HttpServletRequest) 方法
					mappedHandler = getHandler(processedRequest);
					if (mappedHandler == null) {
						// 未找到 handler -> 设置适当的 HTTP 响应状态
						noHandlerFound(processedRequest, response);
						return;
					}

					// Determine handler adapter for the current request. -> 译文：确定当前请求的 handler adapter
					// 3. 获取 handler 对象的 HandlerAdapter
					//    执行 HandlerAdapter#supports(Object) 方法
					HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

					// Process last-modified header, if supported by the handler. -> 译文：处理 last-modified 标头（如果处理程序支持）。
					String method = request.getMethod();
					boolean isGet = HttpMethod.GET.matches(method);
					if (isGet || HttpMethod.HEAD.matches(method)) {
						long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
						if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
							return;
						}
					}

					// 4. 调用已注册 interceptors 的 preHandle 方法，所有 preHandle 调用成功完成并返回 true 的 interceptors 都会调用 afterCompletion 回调。
					//    执行 HandlerInterceptor#preHandle()/HandlerInterceptor#afterCompletion() 方法
					/*if (!mappedHandler.applyPreHandle(processedRequest, response)) {
						return;
					}*/

					// Actually invoke the handler. --> 译文：实际调用 handler
					// 5. 使用给定的 handler 来处理当前请求
					//    执行 HandlerAdapter#handle() 方法
					mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

					// 返回当前请求所选的 handler 是否选择异步处理请求
					if (asyncManager.isConcurrentHandlingStarted()) {
						return;
					}

					// 6. 视图名称翻译：执行 RequestToViewNameTranslator#getViewName(HttpServletRequest) 方法从 HttpServletRequest 转换 view 名称，并设置给 ModelAndView#view 属性
					applyDefaultViewName(processedRequest, mv);
					// 7. 应用已注册 interceptors 的 postHandle 方法；即：HandlerInterceptor#postHandle()。
					//    执行 HandlerInterceptor#postHandle() 方法
					/*mappedHandler.applyPostHandle(processedRequest, response, mv);*/
				}
				catch (Exception ex) {
					dispatchException = ex;
				}
				catch (Throwable err) {
					// As of 4.3, we're processing Errors thrown from handler methods as well,
					// making them available for @ExceptionHandler methods and other scenarios.
					// --> 译文：从 4.3 开始，我们还处理了处理程序方法引发的 Error，使其可用于 @ExceptionHandler 方法和其他场景。
					dispatchException = new ServletException("Handler dispatch failed: " + err, err);
				}
				// 8. 处理 handler 选择和 handler 调用的结果，该结果要么是 ModelAndView，要么是要解析为 ModelAndView 的异常。
				// processDispatchResult(...) 执行的一些组件方法：
				// 	 1. 执行 HandlerExceptionResolver#resolveException(HttpServletRequest, HttpServletResponse, Object, Exception) 方法
				// 	 2. 执行 RequestToViewNameTranslator#getViewName(HttpServletRequest) 方法
				// 	 3. 执行 LocaleResolver#resolveLocale(HttpServletRequest) 方法
				// 	 4. 执行 ViewResolver#resolveViewName(String, Locale) 方法
				// 	 5. 执行 View#render() 方法
				//   6. 执行 HandlerInterceptor#afterCompletion() 方法
				processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
			}
			catch (Exception ex) {
				// 执行 HandlerInterceptor#afterCompletion() 方法
				triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
			}
			catch (Throwable err) {
				// 执行 HandlerInterceptor#afterCompletion() 方法
				triggerAfterCompletion(processedRequest, response, mappedHandler,
						new ServletException("Handler processing failed: " + err, err));
			}
			finally {
				if (asyncManager.isConcurrentHandlingStarted()) {
					// Instead of postHandle and afterCompletion --> 译文：而不是 postHandle 和 afterCompletion
					if (mappedHandler != null) {
						/*mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);*/
					}
					// 在映射的 AsyncHandlerInterceptors 上执行 ConcurrentHandlerStarted 回调后应用。
					//    执行 AsyncHandlerInterceptor#afterConcurrentHandlingStarted() 方法
					asyncManager.setMultipartRequestParsed(multipartRequestParsed);
				}
				else {
					// Clean up any resources used by a multipart request. --> 译文：清理 multipart 请求使用的所有资源。
					if (multipartRequestParsed || asyncManager.isMultipartRequestParsed()) {
						// 执行 MultipartResolver#cleanupMultipart(MultipartHttpServletRequest) 方法
						cleanupMultipart(processedRequest);
					}
				}
			}
		}

		// 返回此 handler 对象的 HandlerAdapter。
		// @param handler 要为其查找 adapter 的 handler
		// @throws 如果找不到该 handler 的 HandlerAdapter，则抛出 ServletException。这是一个致命错误。
		protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
			if (this.handlerAdapters != null) {
				for (HandlerAdapter adapter : this.handlerAdapters) {
					// 给定一个处理程序实例，返回此 {@code HandlerAdapter} 是否支持该处理程序。
					if (adapter.supports(handler)) {
						return adapter;
					}
				}
			}
			throw new ServletException("No adapter for handler [" + handler +
					"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
		}

		// 返回此请求的 HandlerExecutionChain。
		// <p>按顺序尝试所有 handler mappings。
		// @param request 当前 HTTP 请求
		// @return HandlerExecutionChain，如果未找到处理程序，则返回 {@code null}
		@Nullable
		protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
			if (this.handlerMappings != null) {
				for (HandlerMapping mapping : this.handlerMappings) {
					HandlerExecutionChain handler = mapping.getHandler(request);
					if (handler != null) {
						return handler;
					}
				}
			}
			return null;
		}

		// 处理 handler 选择和 handler 调用的结果，该结果要么是 ModelAndView，要么是要解析为 ModelAndView 的异常。
		private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
										   @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
										   @Nullable Exception exception) throws Exception {

			boolean errorView = false;

			// 1. 异常处理；解析为 ModelAndView 的异常。
			if (exception != null) {
				if (exception instanceof ModelAndViewDefiningException mavDefiningException) {
					// ...
					mv = mavDefiningException.getModelAndView();
				}
				else {
					Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
					// 通过已注册的 HandlerExceptionResolvers 确定错误的 ModelAndView
					mv = processHandlerException(request, response, handler, exception);
					errorView = (mv != null);
				}
			}

			// 2. 渲染给定的 ModelAndView
			// handler 是否返回了要渲染的视图？
			if (mv != null && !mv.wasCleared()) {
				render(mv, request, response);
				if (errorView) {
					// 清除 Servlet 规范的错误属性
					WebUtils.clearErrorRequestAttributes(request);
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("No view rendering, null ModelAndView returned.");
				}
			}

			if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
				// 转发期间启动的并发处理
				return;
			}

			if (mappedHandler != null) {
				// 异常（如果有）已被处理...
				// 执行 HandlerInterceptor#afterCompletion() 方法
				/*mappedHandler.triggerAfterCompletion(request, response, null);*/
			}
		}

		// 渲染给定的 ModelAndView。
		// <p>这是处理请求的最后一个阶段。它可能涉及按名称解析视图。
		// @param mv 要渲染的 ModelAndView
		// @param request 当前 HTTP Servlet 请求
		// @param respond 当前 HTTP Servlet 响应
		// @throws ServletException 如果视图缺失或无法解析，则抛出 ServletException
		// @throws Exception 如果渲染视图时出现问题，则抛出 Exception
		protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
			// Determine locale for request and apply it to the response. --> 译文：确定请求的区域设置并将其应用于响应。
			// 1. 确定请求的区域设置并将其应用于响应。
			//    执行 LocaleResolver#resolveLocale() 方法
			Locale locale = (this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
			response.setLocale(locale);

			// 2. 将视图名称解析为 View
			View view;
			String viewName = mv.getViewName();
			if (viewName != null) {
				// We need to resolve the view name. --> 译文：我们需要解析视图名称。
				// 将给定的视图名称解析为一个视图对象（用于渲染）。
				// 执行 ViewResolver#resolveViewName() 方法
				/*view = resolveViewName(viewName, mv.getModelInternal(), locale, request);*/
				view = resolveViewName(viewName, null, locale, request);
				if (view == null) {
					throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
							"' in servlet with name '" + getServletName() + "'");
				}
			}
			else {
				// No need to lookup: the ModelAndView object contains the actual View object. --> 译文：无需查找：ModelAndView 对象包含实际的 View 对象。
				view = mv.getView();
				if (view == null) {
					throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
							"View object in servlet with name '" + getServletName() + "'");
				}
			}

			// 3. 根据指定的模型渲染视图
			// Delegate to the View object for rendering. --> 译文：委托给 View 对象进行渲染。
			if (logger.isTraceEnabled()) {
				logger.trace("Rendering view [" + view + "] ");
			}
			try {
				// 处理 HTTP 状态
				if (mv.getStatus() != null) {
					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, mv.getStatus());
					response.setStatus(mv.getStatus().value());
				}
				// 根据指定的模型渲染视图。
				/*view.render(mv.getModelInternal(), request, response);*/
				view.render(null, request, response);
			}
			catch (Exception ex) {
				// ...
				throw ex;
			}
		}

		// 将给定的视图名称解析为一个视图对象（用于渲染）。
		// <p>默认实现会请求此调度器的所有视图解析器。可以覆盖自定义解析策略，可能基于特定的模型属性或请求参数。
		@Nullable
		protected View resolveViewName(String viewName, @Nullable Map<String, Object> model,
									   Locale locale, HttpServletRequest request) throws Exception {

			if (this.viewResolvers != null) {
				for (ViewResolver viewResolver : this.viewResolvers) {
					// 执行 ViewResolver#resolveViewName() 方法
					View view = viewResolver.resolveViewName(viewName, locale);
					if (view != null) {
						return view;
					}
				}
			}
			return null;
		}

		// 我们需要 view 名称翻译吗？
		// ModelAndView 不为空并且 ModelAndView#view 属性为空时，执行 RequestToViewNameTranslator#getViewName(HttpServletRequest)
		// 方法将给定的 HttpServletRequest 转换为 view 名称，并设置给 ModelAndView#view 属性
		private void applyDefaultViewName(HttpServletRequest request, @Nullable ModelAndView mv) throws Exception {
			if (mv != null && !mv.hasView()) {
				// 将提供的请求转换为默认视图名称 -> RequestToViewNameTranslator#getViewName(HttpServletRequest)
				String defaultViewName = getDefaultViewName(request);
				if (defaultViewName != null) {
					mv.setViewName(defaultViewName);
				}
			}
		}

		// 将提供的请求转换为默认视图名称。
		@Nullable
		protected String getDefaultViewName(HttpServletRequest request) throws Exception {
			return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
		}

		private static void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
												   @Nullable HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

			if (mappedHandler != null) {
				/*mappedHandler.triggerAfterCompletion(request, response, ex);*/
			}
			throw ex;
		}

		// 将请求转换为 multipart 请求，并使 multipart 解析程序可用。
		// <p>如果未设置 multipart resolver ，则只需使用现有请求即可。
		protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
			// MultipartResolver.isMultipart() -> 确定给定的请求是否包含 multipart 内容。
			if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
				// 如果存在 MultipartHttpServletRequest 类型的请求对象，则返回该对象，并根据需要对给定的请求进行解包。
				if (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null) {
					// ...
				}
				// 检查 jakarta.servlet.error.exception 属性是否存在 multipart 异常
				else if (hasMultipartException(request)) {
					// ...
				}
				else {
					try {
						// 将给定的 HTTP 请求解析为多部分文件和参数，并将请求包装在 org.springframework.web.multipart.MultipartHttpServletRequest 对象中，
						// 该对象提供对文件描述符的访问，并通过标准 ServletRequest 方法访问其中包含的参数。
						return this.multipartResolver.resolveMultipart(request);
					}
					catch (MultipartException ex) {
						// ...
					}
				}
			}
			// If not returned before: return original request. --> 译文：如果之前没有返回：返回原始请求。
			return request;
		}

		// 检查 jakarta.servlet.error.exception 属性是否存在 multipart 异常
		private static boolean hasMultipartException(HttpServletRequest request) {
			Throwable error = (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
			while (error != null) {
				if (error instanceof MultipartException) {
					return true;
				}
				error = error.getCause();
			}
			return false;
		}

	}


	static class CA02_HandlerExecutionChain extends HandlerExecutionChain {
		private final Object handler;
		private final List<HandlerInterceptor> interceptorList = new ArrayList<>();
		private int interceptorIndex = -1;
		public CA02_HandlerExecutionChain(Object handler) {
			super(handler);
			this.handler = handler;
		}
		// 调用已注册 interceptors 的 preHandle 方法。
		// @return {@code true} 如果执行链应该继续执行下一个拦截器或处理程序本身。否则，DispatcherServlet 会假定此拦截器已经处理了响应本身。
		boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
			for (int i = 0; i < this.interceptorList.size(); i++) {
				HandlerInterceptor interceptor = this.interceptorList.get(i);
				if (!interceptor.preHandle(request, response, this.handler)) {
					// 在映射的 HandlerInterceptors 上触发 afterCompletion 回调。所有 preHandle 调用成功完成并返回 true 的 interceptors 都会调用 afterCompletion 回调
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
			return true;
		}

		// 应用已注册 interceptors 的 postHandle 方法。
		void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
				throws Exception {

			for (int i = this.interceptorList.size() - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = this.interceptorList.get(i);
				// invoke HandlerInterceptor.postHandle() -> 成功执行处理程序后的拦截点。
				interceptor.postHandle(request, response, this.handler, mv);
			}
		}

		// 在映射的 HandlerInterceptors 上触发 afterCompletion 回调。所有 preHandle 调用成功完成并返回 true 的 interceptors 都会调用 afterCompletion 回调。
		void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex) {
			for (int i = this.interceptorIndex; i >= 0; i--) {
				HandlerInterceptor interceptor = this.interceptorList.get(i);
				try {
					interceptor.afterCompletion(request, response, this.handler, ex);
				}
				catch (Throwable ex2) {
					// ...
				}
			}
		}
	}

	static class CA03_ModelAndView {
		// 视图实例或视图名称字符串。
		private Object view;
		// 模型映射。
		private ModelMap model;
		// 响应的可选 HTTP 状态。
		private HttpStatusCode status;
		// 指示此实例是否已通过调用 {@link #clear()} 清除。
		private boolean cleared = false;

		// 返回模型映射。可能返回 {@code null}。由 DispatcherServlet 调用以评估模型。
		@Nullable
		protected Map<String, Object> getModelInternal() {
			return this.model;
		}
	}
}
