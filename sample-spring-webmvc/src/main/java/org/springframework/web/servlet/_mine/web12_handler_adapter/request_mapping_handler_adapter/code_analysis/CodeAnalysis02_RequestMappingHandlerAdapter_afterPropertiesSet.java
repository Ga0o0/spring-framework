package org.springframework.web.servlet._mine.web12_handler_adapter.request_mapping_handler_adapter.code_analysis;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.method.MethodValidator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.method.ControllerAdviceBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ErrorsMethodArgumentResolver;
import org.springframework.web.method.annotation.ExpressionValueMethodArgumentResolver;
import org.springframework.web.method.annotation.HandlerMethodValidator;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;
import org.springframework.web.method.annotation.ModelMethodProcessor;
import org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.annotation.SessionStatusMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.mvc.method.annotation.AsyncTaskMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.ContinuationHandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.HttpHeadersReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMapMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.ModelAndViewResolverMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMapMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PrincipalMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RedirectAttributesMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestAttributeMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestPartMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitterReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletResponseMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBodyReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.UriComponentsBuilderMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ViewMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * RequestMappingHandlerAdapter#afterPropertiesSet()
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#afterPropertiesSet()
 *
 * ## 1. 初始化 ControllerAdvice 缓存；在给定的 ApplicationContext 中查找带有 @ControllerAdvice 注解的 Bean，并将它们包装为 ControllerAdviceBean 实例进行缓存
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#initControllerAdviceCache()
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#modelAttributeAdviceCache
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#initBinderAdviceCache
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#requestResponseBodyAdvice
 *
 * @see org.springframework.web.method.ControllerAdviceBean
 * @see org.springframework.web.bind.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.ModelAttribute
 * @see org.springframework.web.bind.annotation.InitBinder
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
 *
 * ## 2. 初始化 MessageConverters；默认为：ByteArrayHttpMessageConverter、StringHttpMessageConverter、AllEncompassingFormHttpMessageConverter
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#initMessageConverters()
 *
 * @see org.springframework.http.converter.ByteArrayHttpMessageConverter
 * @see org.springframework.http.converter.StringHttpMessageConverter
 * @see org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
 *
 * ## 3. 返回要使用的参数解析器列表，包括内置解析器和通过 RequestMappingHandlerAdapter#setCustomArgumentResolvers() 方法提供的自定义解析器。
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getDefaultArgumentResolvers()
 *
 * ## 4. 返回用于 @InitBinder 方法的参数解析器列表，包括内置和自定义解析器。
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getDefaultInitBinderArgumentResolvers()
 *
 * ## 5. 返回要使用的返回值处理程序列表，包括通过 RequestMappingHandlerAdapter#setReturnValueHandlers() 提供的内置 handlers 和自定义 handlers
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getDefaultReturnValueHandlers()
 *
 * ## 6. 静态工厂方法，用于在启用 Bean 验证时创建 HandlerMethodValidator，以便通过 ConfigurableWebBindingInitializer 使用，例如在 Spring MVC 或 WebFlux 配置中。
 *
 * @see org.springframework.web.method.annotation.HandlerMethodValidator#from(org.springframework.web.bind.support.WebBindingInitializer, org.springframework.core.ParameterNameDiscoverer, java.util.function.Predicate, java.util.function.Predicate)
 */
public class CodeAnalysis02_RequestMappingHandlerAdapter_afterPropertiesSet {

	/**
	 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#afterPropertiesSet()
	 */
	// public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter
	//		implements BeanFactoryAware, InitializingBean { ... }
	// static abstract class CA01_RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean {
	static abstract class CA01_RequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
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
		private WebBindingInitializer webBindingInitializer;
		private MethodValidator methodValidator;
		private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		private final Map<ControllerAdviceBean, Set<Method>> initBinderAdviceCache = new LinkedHashMap<>();
		private final Map<ControllerAdviceBean, Set<Method>> modelAttributeAdviceCache = new LinkedHashMap<>();
		private final List<Object> requestResponseBodyAdvice = new ArrayList<>();
		private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		// private AsyncTaskExecutor taskExecutor = new MvcSimpleAsyncTaskExecutor();
		private AsyncTaskExecutor taskExecutor = null;
		private Long asyncRequestTimeout;
		private ConfigurableBeanFactory beanFactory;
		private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
		private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

		@Override
		public void afterPropertiesSet() {
			// Do this first, it may add ResponseBody advice beans --> 译文：首先执行此操作，它可能会添加 ResponseBody 建议 bean
			// 1. 初始化 ControllerAdvice 缓存；在给定的 ApplicationContext 中查找带有 @ControllerAdvice 注解的 Bean，并将它们包装为 ControllerAdviceBean 实例进行缓存
			initControllerAdviceCache(); // important -> go
			// 2. 初始化 MessageConverters；默认为：ByteArrayHttpMessageConverter、StringHttpMessageConverter、AllEncompassingFormHttpMessageConverter
			initMessageConverters(); // important -> go

			if (this.argumentResolvers == null) {
				// 3. 返回要使用的参数解析器列表，包括内置解析器和通过 RequestMappingHandlerAdapter#setCustomArgumentResolvers() 方法提供的自定义解析器。
				List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers(); // important -> go
				this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
			}
			if (this.initBinderArgumentResolvers == null) {
				// 4. 返回用于 @InitBinder 方法的参数解析器列表，包括内置和自定义解析器。
				List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers(); // important -> go
				this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
			}
			if (this.returnValueHandlers == null) {
				// 5. 返回要使用的返回值处理程序列表，包括通过 RequestMappingHandlerAdapter#setReturnValueHandlers() 提供的内置 handlers 和自定义 handlers
				List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers(); // important -> go
				this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
			}
			if (BEAN_VALIDATION_PRESENT) {
				// 返回一个包含解析器的只读列表，或者返回一个空列表。
				List<HandlerMethodArgumentResolver> resolvers = this.argumentResolvers.getResolvers();
				// 6. 静态工厂方法，用于在启用 Bean 验证时创建 HandlerMethodValidator，以便通过 ConfigurableWebBindingInitializer 使用，例如在 Spring MVC 或 WebFlux 配置中。
				this.methodValidator = HandlerMethodValidator.from( // important -> go
						this.webBindingInitializer, this.parameterNameDiscoverer,
						methodParamPredicate(resolvers, ModelAttributeMethodProcessor.class),
						methodParamPredicate(resolvers, RequestParamMethodArgumentResolver.class));
			}
		}

		private void initControllerAdviceCache() {
			if (getApplicationContext() == null) {
				return;
			}

			// 1. 在给定的 ApplicationContext 中查找带有 @ControllerAdvice 注解的 Bean，并将它们包装为 ControllerAdviceBean 实例。
			List<ControllerAdviceBean> adviceBeans = ControllerAdviceBean.findAnnotatedBeans(getApplicationContext()); // important -> go

			List<Object> requestResponseBodyAdviceBeans = new ArrayList<>();

			for (ControllerAdviceBean adviceBean : adviceBeans) {
				Class<?> beanType = adviceBean.getBeanType();
				if (beanType == null) {
					throw new IllegalStateException("Unresolvable type for ControllerAdviceBean: " + adviceBean);
				}
				// 2. 根据过滤器选择给定目标类中被 @RequestMapping + @ModelAttribute 注解的方法
				Set<Method> attrMethods = MethodIntrospector.selectMethods(beanType, MODEL_ATTRIBUTE_METHODS); // important -> go
				if (!attrMethods.isEmpty()) {
					this.modelAttributeAdviceCache.put(adviceBean, attrMethods);
				}
				// 3. 根据过滤器选择给定目标类中被 @InitBinder 注解的方法
				Set<Method> binderMethods = MethodIntrospector.selectMethods(beanType, INIT_BINDER_METHODS); // important -> go
				if (!binderMethods.isEmpty()) {
					this.initBinderAdviceCache.put(adviceBean, binderMethods);
				}
				// 4. 筛选 RequestBodyAdvice 和 ResponseBodyAdvice 的子类
				if (RequestBodyAdvice.class.isAssignableFrom(beanType) || ResponseBodyAdvice.class.isAssignableFrom(beanType)) {
					requestResponseBodyAdviceBeans.add(adviceBean);
				}
			}

			if (!requestResponseBodyAdviceBeans.isEmpty()) {
				this.requestResponseBodyAdvice.addAll(0, requestResponseBodyAdviceBeans);
			}

			// ...
		}

		private void initMessageConverters() {
			if (!this.messageConverters.isEmpty()) {
				return;
			}
			this.messageConverters.add(new ByteArrayHttpMessageConverter());
			this.messageConverters.add(new StringHttpMessageConverter());

			this.messageConverters.add(new AllEncompassingFormHttpMessageConverter());
		}

		// 返回要使用的参数解析器列表，包括内置解析器和通过 {@link #setCustomArgumentResolvers} 提供的自定义解析器。
		private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
			List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(30);

			// Annotation-based argument resolution --> 注解：基于注解的参数解析
			resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
			resolvers.add(new RequestParamMapMethodArgumentResolver());
			resolvers.add(new PathVariableMethodArgumentResolver());
			resolvers.add(new PathVariableMapMethodArgumentResolver());
			resolvers.add(new MatrixVariableMethodArgumentResolver());
			resolvers.add(new MatrixVariableMapMethodArgumentResolver());
			resolvers.add(new ServletModelAttributeMethodProcessor(false));
			resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
			resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
			resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
			resolvers.add(new RequestHeaderMapMethodArgumentResolver());
			resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
			resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
			resolvers.add(new SessionAttributeMethodArgumentResolver());
			resolvers.add(new RequestAttributeMethodArgumentResolver());

			// Type-based argument resolution --> 注解：基于类型的参数解析
			resolvers.add(new ServletRequestMethodArgumentResolver());
			resolvers.add(new ServletResponseMethodArgumentResolver());
			resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
			resolvers.add(new RedirectAttributesMethodArgumentResolver());
			resolvers.add(new ModelMethodProcessor());
			resolvers.add(new MapMethodProcessor());
			resolvers.add(new ErrorsMethodArgumentResolver());
			resolvers.add(new SessionStatusMethodArgumentResolver());
			resolvers.add(new UriComponentsBuilderMethodArgumentResolver());
			if (KotlinDetector.isKotlinPresent()) {
				resolvers.add(new ContinuationHandlerMethodArgumentResolver());
			}

			// Custom arguments  --> 注解：自定义参数
			if (getCustomArgumentResolvers() != null) {
				resolvers.addAll(getCustomArgumentResolvers());
			}

			// Catch-all --> 注解：包罗万象
			resolvers.add(new PrincipalMethodArgumentResolver());
			resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
			resolvers.add(new ServletModelAttributeMethodProcessor(true));

			return resolvers;
		}

		// 返回用于 {@code @InitBinder} 方法的参数解析器列表，包括内置和自定义解析器。
		private List<HandlerMethodArgumentResolver> getDefaultInitBinderArgumentResolvers() {
			List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(20);

			// Annotation-based argument resolution --> 注解：基于注解的参数解析
			resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
			resolvers.add(new RequestParamMapMethodArgumentResolver());
			resolvers.add(new PathVariableMethodArgumentResolver());
			resolvers.add(new PathVariableMapMethodArgumentResolver());
			resolvers.add(new MatrixVariableMethodArgumentResolver());
			resolvers.add(new MatrixVariableMapMethodArgumentResolver());
			resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));
			resolvers.add(new SessionAttributeMethodArgumentResolver());
			resolvers.add(new RequestAttributeMethodArgumentResolver());

			// Type-based argument resolution --> 注解：基于类型的参数解析
			resolvers.add(new ServletRequestMethodArgumentResolver());
			resolvers.add(new ServletResponseMethodArgumentResolver());

			// Custom arguments --> 注解：自定义参数
			if (getCustomArgumentResolvers() != null) {
				resolvers.addAll(getCustomArgumentResolvers());
			}

			// Catch-all --> 注解：包罗万象
			resolvers.add(new PrincipalMethodArgumentResolver());
			resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));

			return resolvers;
		}

		// 返回要使用的返回值处理程序列表，包括通过 RequestMappingHandlerAdapter#setReturnValueHandlers() 提供的内置 handlers 和自定义 handlers
		private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
			List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(20);

			// Single-purpose return value types --> 译文：单一用途返回值类型
			handlers.add(new ModelAndViewMethodReturnValueHandler());
			handlers.add(new ModelMethodProcessor());
			handlers.add(new ViewMethodReturnValueHandler());
			handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters(),
					this.reactiveAdapterRegistry, this.taskExecutor, this.contentNegotiationManager));
			handlers.add(new StreamingResponseBodyReturnValueHandler());
			handlers.add(new HttpEntityMethodProcessor(getMessageConverters(),
					this.contentNegotiationManager, this.requestResponseBodyAdvice));
			handlers.add(new HttpHeadersReturnValueHandler());
			handlers.add(new CallableMethodReturnValueHandler());
			handlers.add(new DeferredResultMethodReturnValueHandler());
			handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));

			// Annotation-based return value types --> 译文：基于注解的返回值类型
			handlers.add(new ServletModelAttributeMethodProcessor(false));
			handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
					this.contentNegotiationManager, this.requestResponseBodyAdvice));

			// Multi-purpose return value types --> 译文：多用途返回值类型
			handlers.add(new ViewNameMethodReturnValueHandler());
			handlers.add(new MapMethodProcessor());

			// Custom return value types --> 译文：自定义返回值类型
			if (getCustomReturnValueHandlers() != null) {
				handlers.addAll(getCustomReturnValueHandlers());
			}

			// Catch-all --> 注解：包罗万象
			if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
				handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
			}
			else {
				handlers.add(new ServletModelAttributeMethodProcessor(true));
			}

			return handlers;
		}

		private static Predicate<MethodParameter> methodParamPredicate(
				List<HandlerMethodArgumentResolver> resolvers, Class<?> resolverType) {

			return parameter -> {
				for (HandlerMethodArgumentResolver resolver : resolvers) {
					if (resolver.supportsParameter(parameter)) {
						return resolverType.isInstance(resolver);
					}
				}
				return false;
			};
		}
	}

}
