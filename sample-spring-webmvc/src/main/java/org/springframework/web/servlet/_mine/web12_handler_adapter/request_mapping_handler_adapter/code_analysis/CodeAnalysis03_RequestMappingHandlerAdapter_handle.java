package org.springframework.web.servlet._mine.web12_handler_adapter.request_mapping_handler_adapter.code_analysis;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.method.MethodValidator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.AsyncWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.annotation.SessionAttributesHandler;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RequestMappingHandlerAdapter#handle(...)
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#handleInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.method.HandlerMethod)
 *
 * ## 1. 检查给定请求是否支持方法以及所需的会话（如果有）。
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#checkRequest(jakarta.servlet.http.HttpServletRequest)
 *
 *
 * ## 2. 调用 RequestMapping 的 handler 方法准备 ModelAndView
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#invokeHandlerMethod(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.method.HandlerMethod)
 *
 * ### 2.1. 调用该方法并通过已配置的 HandlerMethodReturnValueHandlers 之一处理返回值
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 *
 * #### 2.1.1. 在给定请求的上下文中解析其参数值后调用该方法
 *
 * @see org.springframework.web.method.support.InvocableHandlerMethod#invokeForRequest(org.springframework.web.context.request.NativeWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 *
 * @see org.springframework.web.method.support.InvocableHandlerMethod#getMethodArgumentValues(org.springframework.web.context.request.NativeWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 * @see org.springframework.validation.method.MethodValidator#applyArgumentValidation(java.lang.Object, java.lang.reflect.Method, org.springframework.core.MethodParameter[], java.lang.Object[], java.lang.Class[])
 * @see org.springframework.web.method.support.InvocableHandlerMethod#doInvoke(java.lang.Object...)
 * @see org.springframework.validation.method.MethodValidator#applyReturnValueValidation(java.lang.Object, java.lang.reflect.Method, org.springframework.core.MethodParameter, java.lang.Object, java.lang.Class[])
 *
 * #### 2.1.2. 在给定请求的上下文中解析其参数值后调用该方法
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#setResponseStatus(org.springframework.web.context.request.ServletWebRequest)
 *
 * #### 2.1.3. 在给定请求的上下文中解析其参数值后调用该方法
 *
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite#handleReturnValue(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite#selectHandler(java.lang.Object, org.springframework.core.MethodParameter)
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandler#handleReturnValue(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
 *
 * ### 2.2. 准备 ModelAndView
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getModelAndView(org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.method.annotation.ModelFactory, org.springframework.web.context.request.NativeWebRequest)
 *
 *
 * ## 3. 处理请求头 Cache-Control
 *
 * ### 3.1. 请求头包含 Cache-Control 并且没有通过 @SessionAttributes 注解声明了会话属性
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheSeconds(jakarta.servlet.http.HttpServletResponse, int)
 *
 * ### 3.2. 请求头包含 Cache-Control 并且通过 @SessionAttributes 注解声明了会话属性
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#prepareResponse(jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheControl(jakarta.servlet.http.HttpServletResponse, org.springframework.http.CacheControl)
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheSeconds(jakarta.servlet.http.HttpServletResponse, int)
 */

public class CodeAnalysis03_RequestMappingHandlerAdapter_handle {

	/**
	 * @see RequestMappingHandlerAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
	 * @see AbstractHandlerMethodAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
	 */
	// public abstract class AbstractHandlerMethodAdapter extends WebContentGenerator implements HandlerAdapter, Ordered { ... }
	static abstract class CA01_AbstractHandlerMethodAdapter extends WebContentGenerator implements HandlerAdapter, Ordered {
		// 此实现期望处理程序是 {@link HandlerMethod}。
		@Override
		@Nullable
		public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {

			return handleInternal(request, response, (HandlerMethod) handler);
		}

		// 使用指定的处理程序方法来处理请求。
		@Nullable
		protected abstract ModelAndView handleInternal(HttpServletRequest request,
													   HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;
	}

	/**
	 * @see RequestMappingHandlerAdapter#handleInternal(HttpServletRequest, HttpServletResponse, HandlerMethod) 
	 */
	// public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter
	//		implements BeanFactoryAware, InitializingBean { ... }
	// static class CA01_RequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
	static class CA02_RequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
		private List<HandlerMethodArgumentResolver> customArgumentResolvers;
		private HandlerMethodArgumentResolverComposite argumentResolvers;
		private HandlerMethodArgumentResolverComposite initBinderArgumentResolvers;
		private List<HandlerMethodReturnValueHandler> customReturnValueHandlers;
		private HandlerMethodReturnValueHandlerComposite returnValueHandlers;
		private List<ModelAndViewResolver> modelAndViewResolvers;
		private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();
		private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		private final List<Object> requestResponseBodyAdvice = new ArrayList<>();
		private WebBindingInitializer webBindingInitializer;
		private MethodValidator methodValidator;
		private AsyncTaskExecutor taskExecutor = null;
		private Long asyncRequestTimeout;
		private CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];
		private DeferredResultProcessingInterceptor[] deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];
		private boolean ignoreDefaultModelOnRedirect = true;
		private int cacheSecondsForSessionAttributeHandlers = 0;
		private boolean synchronizeOnSession = false;
		private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();
		private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);
		private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);
		private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();
		private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);
		private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();

		@Override
		@Nullable
		protected ModelAndView handleInternal(HttpServletRequest request,
											  HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

			ModelAndView mav;
			// 1. 检查给定请求是否支持方法以及所需的会话（如果有）。
			checkRequest(request); // important -> go

			// 2. 调用 RequestMapping 的 handler 方法准备 ModelAndView
			// Execute invokeHandlerMethod in synchronized block if required. --> 译文：如果需要，在同步块中执行 invokeHandlerMethod。
			if (this.synchronizeOnSession) {
				HttpSession session = request.getSession(false);
				if (session != null) {
					// 返回给定会话的最佳可用互斥锁：即用于为给定会话进行同步的对象。
					Object mutex = WebUtils.getSessionMutex(session);
					synchronized (mutex) {
						// 如果需要视图解析，则调用 RequestMapping 处理程序方法准备 ModelAndView
						mav = invokeHandlerMethod(request, response, handlerMethod); // important -> go
					}
				}
				else {
					// No HttpSession available -> no mutex necessary --> 译文：没有可用的 HttpSession -> 不需要互斥锁
					mav = invokeHandlerMethod(request, response, handlerMethod); // important -> go
				}
			}
			else {
				// No synchronization on session demanded at all... --> 译文：根本不需要会话同步...
				mav = invokeHandlerMethod(request, response, handlerMethod); // important -> go
			}

			// 3. 处理请求头 Cache-Control
			if (!response.containsHeader(HEADER_CACHE_CONTROL)) { // HEADER_CACHE_CONTROL = "Cache-Control";
				// HandlerMethod 所在类被 @SessionAttributes 注解标注，并且其属性有值
				if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
					// 应用指定的缓存秒数并生成相应的 HTTP 标头
					applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
				}
				else {
					// 根据此生成器的设置准备给定的响应。应用此生成器指定的缓存秒数。
					prepareResponse(response);
				}
			}

			return mav;
		}

		/**
		 * @see RequestMappingHandlerAdapter#invokeHandlerMethod(HttpServletRequest, HttpServletResponse, HandlerMethod)
		 *
		 * @see ServletInvocableHandlerMethod#invokeForRequest(NativeWebRequest, ModelAndViewContainer, Object...)
		 *
		 * @see RequestMappingHandlerAdapter#getModelAndView(ModelAndViewContainer, ModelFactory, NativeWebRequest)
		 */
		// 如果需要视图解析，则调用 {@link RequestMapping} handler 方法准备 {@link ModelAndView}。
		@SuppressWarnings("deprecation")
		@Nullable
		protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
												   HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

			// 获取当前请求的 WebAsyncManager，如果未找到，则创建并将其与请求关联。
			WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
			// 创建一个 AsyncWebRequest 实例。默认情况下，会创建一个 StandardServletAsyncWebRequest 实例。
			AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
			// 设置并发处理完成所需的时间。
			asyncWebRequest.setTimeout(this.asyncRequestTimeout);

			// 配置 AsyncTaskExecutor 以用于并发处理。
			asyncManager.setTaskExecutor(this.taskExecutor);
			// 配置要使用的 AsyncWebRequest。
			asyncManager.setAsyncWebRequest(asyncWebRequest);
			// 注册一个不带键的 CallableProcessingInterceptor。键由类名和哈希码派生而来。
			asyncManager.registerCallableInterceptors(this.callableInterceptors);
			// 注册一个或多个 DeferredResultProcessingInterceptor，无需指定键。默认键由拦截器类名和哈希码派生而来。
			asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

			// Obtain wrapped response to enforce lifecycle rule from Servlet spec, section 2.3.3.4
			// --> 译文：获取包装的响应以执行 Servlet 规范第 2.3.3.4 节中的生命周期规则
			response = asyncWebRequest.getNativeResponse(HttpServletResponse.class); // 如果可用，则返回底层原生响应对象。

			ServletWebRequest webRequest = (asyncWebRequest instanceof ServletWebRequest ?
					(ServletWebRequest) asyncWebRequest : new ServletWebRequest(request, response));

			WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
			ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

			// 根据给定的 HandlerMethod定义创建一个 ServletInvocableHandlerMethod。
			ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
			if (this.argumentResolvers != null) {
				// 设置 {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} 用于解析方法参数值。
				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
			if (this.returnValueHandlers != null) {
				// 注册 {@link HandlerMethodReturnValueHandler} 实例以用于处理返回值。
				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
			}
			invocableMethod.setDataBinderFactory(binderFactory);
			invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
			invocableMethod.setMethodValidator(this.methodValidator);

			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
			// 将所有属性复制到底层模型。
			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
			modelFactory.initModel(webRequest, mavContainer, invocableMethod);
			mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

			// 返回并发处理后是否存在结果值。
			if (asyncManager.hasConcurrentResult()) {
				// 获取并发处理的结果。
				Object result = asyncManager.getConcurrentResult();
				// 获取并发处理开始时保存的附加处理上下文。
				Object[] resultContext = asyncManager.getConcurrentResultContext();
				Assert.state(resultContext != null && resultContext.length > 0, "Missing result context");
				mavContainer = (ModelAndViewContainer) resultContext[0];
				asyncManager.clearConcurrentResult();
				LogFormatUtils.traceDebug(logger, traceOn -> {
					String formatted = LogFormatUtils.formatValue(result, !traceOn);
					return "Resume with async result [" + formatted + "]";
				});
				// invocableMethod = invocableMethod.wrapConcurrentResult(result);
			}

			// 调用该方法并通过已配置的 HandlerMethodReturnValueHandler 之一处理返回值。
			invocableMethod.invokeAndHandle(webRequest, mavContainer);  // important -> go
			// 返回当前请求所选的处理程序是否选择异步处理请求。
			if (asyncManager.isConcurrentHandlingStarted()) {
				return null;
			}

			return getModelAndView(mavContainer, modelFactory, webRequest);  // important -> go
		}

		private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
			Class<?> handlerType = handlerMethod.getBeanType();

			// 处理 @InitBinder 标记的方法
			Set<Method> methods = this.initBinderCache.get(handlerType);
			if (methods == null) {
				methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
				this.initBinderCache.put(handlerType, methods);
			}
			List<InvocableHandlerMethod> initBinderMethods = new ArrayList<>();
			// Global methods first
			this.initBinderAdviceCache.forEach((controllerAdviceBean, methodSet) -> { // 处理 @ControllerAdvice
				// 检查给定的 bean 类型是否应该由此 ControllerAdviceBean 进行通知。
				if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
					// 获取此 ControllerAdviceBean 的 bean 实例，如有必要，通过 BeanFactory 解析 bean 名称。
					Object bean = controllerAdviceBean.resolveBean();
					for (Method method : methodSet) {
						initBinderMethods.add(createInitBinderMethod(bean, method));
					}
				}
			});
			for (Method method : methods) {
				Object bean = handlerMethod.getBean();
				initBinderMethods.add(createInitBinderMethod(bean, method));
			}

			// 用于创建新 InitBinderDataBinderFactory 实例的模板方法。
			DefaultDataBinderFactory factory = createDataBinderFactory(initBinderMethods);
			// 配置标志以指示是否将验证应用于处理程序方法参数
			factory.setMethodValidationApplicable(this.methodValidator != null && handlerMethod.shouldValidateArguments());
			return factory;
		}

		private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
			// 从 bean 实例和方法创建一个实例。
			InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
			if (this.initBinderArgumentResolvers != null) {
				// 设置 HandlerMethodArgumentResolver 用于解析方法参数值。
				binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);
			}
			// 设置 WebDataBinderFactory 传递给参数解析器，允许它们创建 WebDataBinder 用于数据绑定和类型转换目的。
			binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
			// 设置 ParameterNameDiscoverer 以便在需要时解析参数名称（例如，默认请求属性名称）。
			binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
			return binderMethod;
		}

		@Nullable
		private ModelAndView getModelAndView(ModelAndViewContainer mavContainer,
											 ModelFactory modelFactory, NativeWebRequest webRequest) throws Exception {

			// 1. 将以 @SessionAttributes 形式列出的模型属性提升到会话中。在必要时添加 BindingResult 属性。
			modelFactory.updateModel(webRequest, mavContainer);
			if (mavContainer.isRequestHandled()) { // 请求是否已在处理程序内得到完全处理。
				return null;
			}
			ModelMap model = mavContainer.getModel();
			// 2. 给定视图名称、模型和 HTTP 状态，创建一个新的 ModelAndView。
			ModelAndView mav = new ModelAndView(mavContainer.getViewName(), model, mavContainer.getStatus()); // important -> go
			if (!mavContainer.isViewReference()) {
				mav.setView((View) mavContainer.getView());
			}
			if (model instanceof RedirectAttributes redirectAttributes) {
				Map<String, ?> flashAttributes = redirectAttributes.getFlashAttributes();
				HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
				if (request != null) {
					RequestContextUtils.getOutputFlashMap(request).putAll(flashAttributes);
				}
			}
			return mav;
		}

		private ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
			SessionAttributesHandler sessionAttrHandler = getSessionAttributesHandler(handlerMethod);
			Class<?> handlerType = handlerMethod.getBeanType();
			Set<Method> methods = this.modelAttributeCache.get(handlerType);
			if (methods == null) {
				methods = MethodIntrospector.selectMethods(handlerType, MODEL_ATTRIBUTE_METHODS);
				this.modelAttributeCache.put(handlerType, methods);
			}
			List<InvocableHandlerMethod> attrMethods = new ArrayList<>();
			// Global methods first
			this.modelAttributeAdviceCache.forEach((controllerAdviceBean, methodSet) -> {
				if (controllerAdviceBean.isApplicableToBeanType(handlerType)) {
					Object bean = controllerAdviceBean.resolveBean();
					for (Method method : methodSet) {
						attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
					}
				}
			});
			for (Method method : methods) {
				Object bean = handlerMethod.getBean();
				attrMethods.add(createModelAttributeMethod(binderFactory, bean, method));
			}
			// 使用给定的 {@code @ModelAttribute} 方法创建一个新实例。
			return new ModelFactory(attrMethods, binderFactory, sessionAttrHandler);
		}

		private InvocableHandlerMethod createModelAttributeMethod(WebDataBinderFactory factory, Object bean, Method method) {
			InvocableHandlerMethod attrMethod = new InvocableHandlerMethod(bean, method);
			if (this.argumentResolvers != null) {
				attrMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
			}
			attrMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
			attrMethod.setDataBinderFactory(factory);
			return attrMethod;
		}

		// 返回给定处理程序类型的 SessionAttributesHandler 实例（永远不会 null）。
		private SessionAttributesHandler getSessionAttributesHandler(HandlerMethod handlerMethod) {
			return this.sessionAttributesHandlerCache.computeIfAbsent(
					handlerMethod.getBeanType(),
					type -> new SessionAttributesHandler(type, this.sessionAttributeStore));
		}
	}

	/**
	 * @see ServletInvocableHandlerMethod#invokeForRequest(NativeWebRequest, ModelAndViewContainer, Object...)
	 */
	static class CA02_ServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {
		private static final Class<?>[] EMPTY_GROUPS = new Class<?>[0];
		private static final Object[] EMPTY_ARGS = new Object[0];
		private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();
		private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		private HandlerMethodReturnValueHandlerComposite returnValueHandlers;
		private MethodValidator methodValidator;
		private WebDataBinderFactory dataBinderFactory;
		private Class<?>[] validationGroups = EMPTY_GROUPS;

		public CA02_ServletInvocableHandlerMethod(Object handler, Method method) {
			super(handler, method);
		}

		// 调用该方法并通过已配置的 {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers} 之一处理返回值。
		public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
									Object... providedArgs) throws Exception {

			// 1. 在给定请求的上下文中解析其参数值后调用该方法。
			Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs); // important -> go
			// 2. 根据 @ResponseStatus 注解设置响应状态。
			// setResponseStatus(webRequest);

			// ...
			try {
				// 3. 遍历已注册的 HandlerMethodReturnValueHandler 并调用支持它的那个。
				//    执行 HandlerMethodReturnValueHandler#handleReturnValue() 方法
				this.returnValueHandlers.handleReturnValue(
						returnValue, getReturnValueType(returnValue), mavContainer, webRequest); // important -> go
			}
			catch (Exception ex) {
				// ...
				throw ex;
			}
		}

		// 在给定请求的上下文中解析其参数值后调用该方法。
		// <p>参数值通常通过 {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} 解析。
		// 但是，{@code providedArgs} 参数可以提供直接使用的参数值，即无需参数解析。
		// 提供的参数值的示例包括 {@link WebDataBinder}、{@link SessionStatus} 或引发的异常实例。提供的参数值在参数解析器之前进行检查。
		// <p>委托给 {@link #getMethodArgumentValues} 并使用解析后的参数调用 {@link #doInvoke}。
		@Nullable
		public Object invokeForRequest(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
									   Object... providedArgs) throws Exception {

			// 1. 获取当前请求的方法参数值 -> 执行 HandlerMethodArgumentResolver#resolveArgument() 方法
			Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs); // important -> go
			// ...

			// 2. 方法参数是否需要进行方法验证
			if (shouldValidateArguments() && this.methodValidator != null) {
				// 应用参数验证 -> MethodValidator.validateArguments()
				this.methodValidator.applyArgumentValidation(
						getBean(), getBridgedMethod(), getMethodParameters(), args, this.validationGroups); // important -> go
			}

			Object returnValue = doInvoke(args); // important -> go

			// 3. 方法返回值是否是方法验证的候选值
			if (shouldValidateReturnValue() && this.methodValidator != null) {
				// 验证给定的返回值并返回验证结果。 -> MethodValidator.validateReturnValue()
				this.methodValidator.applyReturnValueValidation(
						getBean(), getBridgedMethod(), getReturnType(), returnValue, this.validationGroups); // important -> go
			}

			return returnValue;
		}

		// 获取当前请求的方法参数值，检查提供的参数值并返回到已配置的参数解析器。
		// <p>结果数组将传递到 {@link #doInvoke}。
		protected Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer,
												   Object... providedArgs) throws Exception {

			// 返回此 AnnotatedMethod 的方法参数。
			MethodParameter[] parameters = getMethodParameters();
			if (ObjectUtils.isEmpty(parameters)) {
				return EMPTY_ARGS;
			}

			Object[] args = new Object[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				MethodParameter parameter = parameters[i];
				parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
				args[i] = findProvidedArgument(parameter, providedArgs);
				if (args[i] != null) {
					continue;
				}
				if (!this.resolvers.supportsParameter(parameter)) {
					throw new IllegalStateException(formatArgumentError(parameter, "No suitable resolver"));
				}
				try {
					// 遍历已注册的 HandlerMethodArgumentResolver 并调用支持它的那个。
					// 执行 HandlerMethodArgumentResolver#resolveArgument() 方法
					args[i] = this.resolvers.resolveArgument(parameter, mavContainer, request, this.dataBinderFactory); // important -> go
				}
				catch (Exception ex) {
					// ...
					throw ex;
				}
			}
			return args;
		}

		// 使用给定的参数值调用处理程序方法。
		@Nullable
		protected Object doInvoke(Object... args) throws Exception {
			// 如果被注解的方法是桥接方法，则此方法返回桥接的（用户定义的）方法。
			Method method = getBridgedMethod();
			// ...
			return method.invoke(getBean(), args); // important -> go
			// ...
		}
	}

}
