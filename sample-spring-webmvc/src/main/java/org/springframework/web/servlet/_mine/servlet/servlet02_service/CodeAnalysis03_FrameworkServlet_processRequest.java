package org.springframework.web.servlet._mine.servlet.servlet02_service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.server.RequestPath;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Servlet#service(...) - FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.FrameworkServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## FrameworkServlet#processRequest(...)
 *
 * @see org.springframework.web.servlet.FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#doService(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */
public class CodeAnalysis03_FrameworkServlet_processRequest {

	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	static abstract class CA01_FrameworkServlet extends HttpServletBean {
		private static final long serialVersionUID = 1L;
		// 是否将 LocaleContext 和 RequestAttributes 暴露为可继承的子线程？
		private boolean threadContextInheritable = false;
		// 我们是否应该在每个请求结束时发布 ServletRequestHandledEvent？
		private boolean publishEvents = true;
		// 此 servlet 的 WebApplicationContext。
		private WebApplicationContext webApplicationContext;

		/**
		 * @see org.springframework.web.servlet.FrameworkServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
		 */
		// 将 GET 请求委托给 processRequest/doService。
		// <p>也将由 HttpServlet 的 {@code doHead} 默认实现调用，并使用仅捕获内容长度的 {@code NoBodyResponse}。
		@Override
		protected final void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

			processRequest(request, response); // important -> go
		}

		/**
		 * @see org.springframework.web.servlet.FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
		 */
		// 处理此请求，无论结果如何，都发布一个事件。
		// <p>实际的事件处理由抽象 {@link #doService} 模板方法执行。
		protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

			long startTime = System.currentTimeMillis();
			Throwable failureCause = null;

			// 1. build LocaleContext
			// 返回与当前线程关联的 LocaleContext（如果有）。
			LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
			LocaleContext localeContext = buildLocaleContext(request);

			// 2. build ServletRequestAttributes
			// 返回当前绑定到线程的 RequestAttributes。
			RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
			// 为给定请求构建 ServletRequestAttributes（可能还包含对响应的引用），同时考虑预绑定属性（及其类型）。
			ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

			// 3. build WebAsyncManager
			// 获取当前请求的 {@link WebAsyncManager}，如果找不到，则创建并将其与请求关联。
			WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
			// 注册callableInterceptors：{key:FrameworkServlet.class.getName(), val: new RequestBindingInterceptor()}
			// asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new FrameworkServlet.RequestBindingInterceptor());
			asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new CA01_FrameworkServlet.RequestBindingInterceptor());

			// 初始化上下文持有者
			initContextHolders(request, localeContext, requestAttributes);

			try {
				// 子类必须实现这个方法来处理请求，接收GET、POST、PUT和DELETE的集中回调。
				doService(request, response);  // important -> go
			}
			catch (ServletException | IOException ex) {
				failureCause = ex;
				throw ex;
			}
			catch (Throwable ex) {
				failureCause = ex;
				throw new ServletException("Request processing failed: " + ex, ex);
			}

			finally {
				// 重置上下文持有者
				resetContextHolders(request, previousLocaleContext, previousAttributes);
				if (requestAttributes != null) {
					requestAttributes.requestCompleted();
				}
				// logResult(request, response, failureCause, asyncManager); // 日志记录 -> 不管
				// 发布一个事件：ServletRequestHandledEvent
				publishRequestHandledEvent(request, response, startTime, failureCause);
			}
		}

		// 子类必须实现此方法才能处理请求，并接收 GET、POST、PUT 和 DELETE 的集中回调。
		// <p>此约定本质上与 HttpServlet 中常被覆盖的 {@code doGet} 或 {@code doPost} 方法相同。
		// <p>此类会拦截调用，以确保异常处理和事件发布得以进行。
		// @param request 当前 HTTP 请求
		// @param respond 当前 HTTP 响应
		// @throws 任何类型的处理失败时抛出的异常
		protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
				throws Exception;

		// 为给定请求构建 LocaleContext，将请求的主区域设置公开为当前区域设置。
		@Nullable
		protected LocaleContext buildLocaleContext(HttpServletRequest request) {
			return new SimpleLocaleContext(request.getLocale());
		}

		// 为给定请求构建 ServletRequestAttributes（可能还包含对响应的引用），同时考虑预绑定属性（及其类型）。
		@Nullable
		protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
						  @Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {
			if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
				return new ServletRequestAttributes(request, response);
			} else {
				return null;  // preserve the pre-bound RequestAttributes instance
			}
		}

		// CallableProcessingInterceptor 实现，用于初始化和重置 FrameworkServlet 的上下文持有者，即 LocaleContextHolder 和 RequestContextHolder。
		private class RequestBindingInterceptor implements CallableProcessingInterceptor {
			@Override
			public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
				HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
				if (request != null) {
					HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
					initContextHolders(request, buildLocaleContext(request),
							buildRequestAttributes(request, response, null));
				}
			}
			@Override
			public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, @Nullable Object concurrentResult) {
				HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
				if (request != null) {
					resetContextHolders(request, null, null);
				}
			}
		}

		private void initContextHolders(HttpServletRequest request,
										@Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {
			if (localeContext != null) {
				LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
			}
			if (requestAttributes != null) {
				RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
			}
		}

		private void resetContextHolders(HttpServletRequest request,
										 @Nullable LocaleContext prevLocaleContext, @Nullable RequestAttributes previousAttributes) {
			LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
			RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
		}

		private void publishRequestHandledEvent(HttpServletRequest request, HttpServletResponse response,
												long startTime, @Nullable Throwable failureCause) {
			if (this.publishEvents && this.webApplicationContext != null) {
				// Whether or not we succeeded, publish an event.
				long processingTime = System.currentTimeMillis() - startTime;
				this.webApplicationContext.publishEvent(
						new ServletRequestHandledEvent(this,
								request.getRequestURI(), request.getRemoteAddr(),
								request.getMethod(), getServletConfig().getServletName(),
								WebUtils.getSessionId(request), getUsernameForRequest(request),
								processingTime, failureCause, response.getStatus()));  // important -> go
			}
		}

		// 确定给定请求的用户名。
		// <p>默认实现采用 UserPrincipal 的名称（如果有）。可以在子类中重写。
		@Nullable
		protected String getUsernameForRequest(HttpServletRequest request) {
			Principal userPrincipal = request.getUserPrincipal();
			return (userPrincipal != null ? userPrincipal.getName() : null);
		}
	}

	// public class DispatcherServlet extends FrameworkServlet { ... }
	static class CA02_DispatcherServlet extends CA01_FrameworkServlet {
		private static final long serialVersionUID = 1L;
		// static class CA02_DispatcherServlet extends DispatcherServlet {
		// DispatcherServlet 默认策略属性的通用前缀。
		private static final String DEFAULT_STRATEGIES_PREFIX 			= "org.springframework.web.servlet";
		// 请求属性用于保存当前的 Web 应用上下文。否则，只能通过标签等获取全局 Web 应用上下文。
		public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE 	= DispatcherServlet.class.getName() + ".CONTEXT";
		// 请求属性用于保存当前的 LocaleResolver，可通过视图获取。
		public static final String LOCALE_RESOLVER_ATTRIBUTE 			= DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";
		// 请求属性用于保存当前的 ThemeResolver，可通过视图获取。自 6.0 起已弃用，无直接替代品
		public static final @Deprecated String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";
		// 用于保存当前 ThemeSource 的请求属性，可供视图检索。自 6.0 起已弃用，无直接替代品
		public static final @Deprecated String THEME_SOURCE_ATTRIBUTE 	= DispatcherServlet.class.getName() + ".THEME_SOURCE";
		// 请求属性的名称，该属性包含一个只读的 {@code Map<String,?>}，其中包含先前请求保存的“输入”Flash 属性（如果有）。
		public static final String INPUT_FLASH_MAP_ATTRIBUTE 			= DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";
		// 请求属性的名称，该属性包含一个“输出”{@link FlashMap}，其中包含要保存用于后续请求的属性。
		public static final String OUTPUT_FLASH_MAP_ATTRIBUTE 			= DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";
		// 包含 {@link FlashMapManager} 的请求属性名称。
		public static final String FLASH_MAP_MANAGER_ATTRIBUTE 			= DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";

		private WebApplicationContext webApplicationContext;
		// 在包含请求后执行请求属性清理？
		private boolean cleanupAfterInclude = true;
		// 此 servlet 使用的 LocaleResolver。
		private LocaleResolver localeResolver;
		// 此 servlet 使用的 ThemeResolver。
		private @Deprecated ThemeResolver themeResolver;
		// 此 servlet 使用的 FlashMapManager。
		private FlashMapManager flashMapManager;
		private boolean parseRequestPath;

		/**
		 * @see org.springframework.web.servlet.DispatcherServlet#doService(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
		 */
		// 公开 DispatcherServlet 特定的请求属性并委托给 {@link #doDispatch} 进行实际调度。
		@Override
		protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception { // important -> go
			// logRequest(request);

			// Keep a snapshot of the request attributes in case of an include,
			// to be able to restore the original attributes after the include.
			// --> 译文：在 include 的情况下保留请求属性的快照，以便能够在 include 之后恢复原始属性。
			Map<String, Object> attributesSnapshot = null;
			if (WebUtils.isIncludeRequest(request)) { // 判断给定的请求是否为包含请求，即非来自外部的顶级 HTTP 请求。
				attributesSnapshot = new HashMap<>();
				Enumeration<?> attrNames = request.getAttributeNames();
				while (attrNames.hasMoreElements()) {
					String attrName = (String) attrNames.nextElement();
					// DEFAULT_STRATEGIES_PREFIX = "org.springframework.web.servlet"
					if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
						attributesSnapshot.put(attrName, request.getAttribute(attrName));
					}
				}
			}

			// Make framework objects available to handlers and view objects. --> 译文：使框架对象可供处理程序和视图对象使用。
			// WEB_APPLICATION_CONTEXT_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.CONTEXT
			request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
			// LOCALE_RESOLVER_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER
			request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
			// THEME_RESOLVER_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.THEME_RESOLVER
			request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
			// THEME_SOURCE_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.THEME_SOURCE
			request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

			if (this.flashMapManager != null) { // true
				FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
				if (inputFlashMap != null) { // false
					// INPUT_FLASH_MAP_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.INPUT_FLASH_MAP
					request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
				}
				// OUTPUT_FLASH_MAP_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.OUTPUT_FLASH_MAP
				request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
				// FLASH_MAP_MANAGER_ATTRIBUTE = org.springframework.web.servlet.DispatcherServlet.FLASH_MAP_MANAGER
				request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
			}

			RequestPath previousRequestPath = null;
			if (this.parseRequestPath) { // true
				// PATH_ATTRIBUTE = org.springframework.web.util.ServletRequestPathUtils.PATH
				previousRequestPath = (RequestPath) request.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE);
				ServletRequestPathUtils.parseAndCache(request);
			}

			try {
				// 将实际的调度操作传递给处理程序。
				doDispatch(request, response); // important -> go
			}
			finally {
				if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
					// Restore the original attribute snapshot, in case of an include. --> 译文：恢复原始属性快照（如果是 include）。
					if (attributesSnapshot != null) {
						restoreAttributesAfterInclude(request, attributesSnapshot); // 恢复包含后的请求属性。
					}
				}
				if (this.parseRequestPath) {
					// 将缓存的、已解析的 {@code RequestPath} 设置为给定值。
					ServletRequestPathUtils.setParsedRequestPath(previousRequestPath, request);
				}
			}
		}

		// 将实际的调度操作传递给处理程序。
		// <p>处理程序将通过按顺序应用 servlet 的 HandlerMappings 来获取。
		// HandlerAdapter 将通过查询 servlet 已安装的 HandlerAdapters 来获取，以找到第一个支持该处理程序类的 HandlerAdapter。
		// <p>所有 HTTP 方法都由此方法处理。由 HandlerAdapters 或处理程序本身决定哪些方法是可接受的。
		// @param request 当前 HTTP 请求
		// @param respond 当前 HTTP 响应
		// @throws 任何类型的处理失败时抛出的异常
		@SuppressWarnings("deprecation")
		protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
			// ...
		}

		// 返回此 servlet 的 WebApplicationContext。
		public final WebApplicationContext getWebApplicationContext() {
			return this.webApplicationContext;
		}

		// 返回此 servlet 的 ThemeSource（如果有）；否则返回 {@code null}。
		// <p>默认情况下，如果 WebApplicationContext 实现了 ThemeSource 接口，则将其作为 ThemeSource 返回。</p>
		public final @Deprecated ThemeSource getThemeSource() {
			return (getWebApplicationContext() instanceof ThemeSource themeSource ?
					themeSource : null);
		}

		// 恢复包含后的请求属性。
		// @param request 当前 HTTP 请求
		// @param attributeSnapshot 包含前的请求属性快照
		@SuppressWarnings("unchecked")
		private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?, ?> attributesSnapshot) {
			// 需要将其复制到单独的集合中，以避免在删除属性时对枚举产生副作用。
			Set<String> attrsToCheck = new HashSet<>();
			Enumeration<?> attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
					attrsToCheck.add(attrName);
				}
			}

			// 添加可能已被移除的属性
			attrsToCheck.addAll((Set<String>) attributesSnapshot.keySet());

			// 遍历属性进行检查，如果合适，则分别恢复原始值或删除属性。
			for (String attrName : attrsToCheck) {
				Object attrValue = attributesSnapshot.get(attrName);
				if (attrValue == null) {
					request.removeAttribute(attrName);
				}
				else if (attrValue != request.getAttribute(attrName)) {
					request.setAttribute(attrName, attrValue);
				}
			}
		}
	}

}
