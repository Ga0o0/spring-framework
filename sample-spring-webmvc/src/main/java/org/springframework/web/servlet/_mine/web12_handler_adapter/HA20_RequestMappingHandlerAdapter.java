package org.springframework.web.servlet._mine.web12_handler_adapter;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * RequestMappingHandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
 * @see org.springframework.web.method.HandlerMethod
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#afterPropertiesSet()
 */
public class HA20_RequestMappingHandlerAdapter {

	/**
	 * @see RequestMappingHandlerAdapter#setCustomReturnValueHandlers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getCustomReturnValueHandlers()
	 * @see RequestMappingHandlerAdapter#setArgumentResolvers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getArgumentResolvers()
	 * @see RequestMappingHandlerAdapter#setInitBinderArgumentResolvers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getInitBinderArgumentResolvers()
	 * @see RequestMappingHandlerAdapter#setCustomReturnValueHandlers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getCustomReturnValueHandlers()
	 * @see RequestMappingHandlerAdapter#setReturnValueHandlers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getReturnValueHandlers()
	 * @see RequestMappingHandlerAdapter#setModelAndViewResolvers(java.util.List)
	 * @see RequestMappingHandlerAdapter#getModelAndViewResolvers()
	 * @see RequestMappingHandlerAdapter#setContentNegotiationManager(org.springframework.web.accept.ContentNegotiationManager)
	 * @see RequestMappingHandlerAdapter#setMessageConverters(java.util.List)
	 * @see RequestMappingHandlerAdapter#getMessageConverters()
	 * @see RequestMappingHandlerAdapter#setRequestBodyAdvice(java.util.List)
	 * @see RequestMappingHandlerAdapter#setResponseBodyAdvice(java.util.List)
	 * @see RequestMappingHandlerAdapter#setWebBindingInitializer(org.springframework.web.bind.support.WebBindingInitializer)
	 * @see RequestMappingHandlerAdapter#getWebBindingInitializer()
	 * @see RequestMappingHandlerAdapter#setTaskExecutor(org.springframework.core.task.AsyncTaskExecutor)
	 * @see RequestMappingHandlerAdapter#setAsyncRequestTimeout(long)
	 * @see RequestMappingHandlerAdapter#setCallableInterceptors(java.util.List)
	 * @see RequestMappingHandlerAdapter#setDeferredResultInterceptors(java.util.List)
	 * @see RequestMappingHandlerAdapter#setReactiveAdapterRegistry(org.springframework.core.ReactiveAdapterRegistry)
	 * @see RequestMappingHandlerAdapter#getReactiveAdapterRegistry()
	 * @see RequestMappingHandlerAdapter#setSessionAttributeStore(org.springframework.web.bind.support.SessionAttributeStore)
	 * @see RequestMappingHandlerAdapter#setCacheSecondsForSessionAttributeHandlers(int)
	 * @see RequestMappingHandlerAdapter#setSynchronizeOnSession(boolean)
	 * @see RequestMappingHandlerAdapter#setParameterNameDiscoverer(org.springframework.core.ParameterNameDiscoverer)
	 *
	 * @see RequestMappingHandlerAdapter#INIT_BINDER_METHODS
	 * @see RequestMappingHandlerAdapter#MODEL_ATTRIBUTE_METHODS
	 */
	public static void main(String[] args) {

	}
}
/*
********************************* Class API Docs *********************************
AbstractHandlerMethodAdapter 的扩展，支持 @RequestMapping 注释的 HandlerMethods。

<p>可以通过 RequestMappingHandlerAdapter#setCustomArgumentResolvers() 和
RequestMappingHandlerAdapter#setCustomReturnValueHandlers() 添加对自定义参数和返回值类型的支持，
或者，要重新配置所有参数和返回值类型，请使用 RequestMappingHandlerAdapter#setArgumentResolvers()
和 RequestMappingHandlerAdapter#setReturnValueHandlers()。

********************************* Class Definition *********************************
public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter
		implements BeanFactoryAware, InitializingBean {

	// 与 {@link InitBinder @InitBinder} 方法匹配的 MethodFilter。
	public static final ReflectionUtils.MethodFilter INIT_BINDER_METHODS = method ->
			AnnotatedElementUtils.hasAnnotation(method, InitBinder.class);
	// 与 {@link ModelAttribute @ModelAttribute} 方法匹配的 MethodFilter。
	public static final ReflectionUtils.MethodFilter MODEL_ATTRIBUTE_METHODS = method ->
			(!AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class) &&
					AnnotatedElementUtils.hasAnnotation(method, ModelAttribute.class));
	private static final boolean BEAN_VALIDATION_PRESENT =
			ClassUtils.isPresent("jakarta.validation.Validator", HandlerMethod.class.getClassLoader());

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
	private AsyncTaskExecutor taskExecutor = new RequestMappingHandlerAdapter.MvcSimpleAsyncTaskExecutor();
	private Long asyncRequestTimeout;
	private CallableProcessingInterceptor[] callableInterceptors = new CallableProcessingInterceptor[0];
	private DeferredResultProcessingInterceptor[] deferredResultInterceptors = new DeferredResultProcessingInterceptor[0];
	private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
	private boolean ignoreDefaultModelOnRedirect = true;
	private int cacheSecondsForSessionAttributeHandlers = 0;
	private boolean synchronizeOnSession = false;
	private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	private ConfigurableBeanFactory beanFactory;
	private final Map<Class<?>, SessionAttributesHandler> sessionAttributesHandlerCache = new ConcurrentHashMap<>(64);
	private final Map<Class<?>, Set<Method>> initBinderCache = new ConcurrentHashMap<>(64);
	private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();
	private final Map<Class<?>, Set<Method>> modelAttributeCache = new ConcurrentHashMap<>(64);
	private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();
	// ...
}
**/
