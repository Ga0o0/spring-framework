package org.springframework.web.servlet._mine.servlet.servlet01_init;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.ViewResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Servlet#init(jakarta.servlet.ServletConfig) - DispatcherServlet#initStrategies(...)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#initStrategies(org.springframework.context.ApplicationContext)
 *
 * ## 初始化 Servlet 使用的策略对象
 *
 * @see org.springframework.web.servlet.DispatcherServlet#initMultipartResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initLocaleResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initThemeResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerMappings(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerAdapters(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerExceptionResolvers(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initRequestToViewNameTranslator(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initViewResolvers(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initFlashMapManager(org.springframework.context.ApplicationContext)
 *
 * ## 从文件 org/springframework/web/servlet/DispatcherServlet.properties 中获取默认策略，并进行初始化
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultStrategy(org.springframework.context.ApplicationContext, java.lang.Class)
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultStrategies(org.springframework.context.ApplicationContext, java.lang.Class)
 */
public class CodeAnalysis04_DispatcherServlet_initStrategies {
	static class CA01_DispatcherServlet extends DispatcherServlet {
		private static final long serialVersionUID = 1L;
		// 此命名空间的 bean 工厂中 MultipartResolver 对象的已知名称。
		public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";
		// 此命名空间的 bean 工厂中 LocaleResolver 对象的已知名称。
		public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";
		// 此命名空间的 bean 工厂中 ThemeResolver 对象的已知名称。
		// @deprecated，自 6.0 起，无直接替代品
		@Deprecated
		public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";
		// 此命名空间的 bean 工厂中 HandlerMapping 对象的已知名称。仅在 “detectAllHandlerMappings” 关闭时使用。
		public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";
		// 此命名空间的 Bean 工厂中 HandlerAdapter 对象的已知名称。仅在 “detectAllHandlerAdapters” 关闭时使用。
		public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";
		// 此命名空间的 Bean 工厂中 HandlerExceptionResolver 对象的已知名称。仅在 “detectAllHandlerExceptionResolvers” 关闭时使用。
		public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";
		// 此命名空间的 bean 工厂中 RequestToViewNameTranslator 对象的已知名称。
		public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";
		// 此命名空间的 bean 工厂中 ViewResolver 对象的已知名称。仅在 “detectAllViewResolvers” 关闭时使用。
		public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";
		// 此命名空间的 bean 工厂中 FlashMapManager 对象的已知名称。
		public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

		// ...
		// 检测所有 HandlerMappings 还是仅期望 “handlerMapping” bean？
		private boolean detectAllHandlerMappings = true;
		// 检测所有 HandlerAdapter 还是仅期望 “handlerAdapter” bean？
		private boolean detectAllHandlerAdapters = true;
		// 检测所有 HandlerExceptionResolvers 还是仅期望 “handlerExceptionResolver” bean？
		private boolean detectAllHandlerExceptionResolvers = true;
		// 检测所有 ViewResolvers 还是仅期望 “viewResolver” bean？
		private boolean detectAllViewResolvers = true;
		// ...

		// 此 servlet 使用的 MultipartResolver。
		private MultipartResolver multipartResolver;
		// 此 servlet 使用的 LocaleResolver。
		private LocaleResolver localeResolver;
		// 此 servlet 使用的 ThemeResolver。
		@Deprecated
		private ThemeResolver themeResolver;
		// 此 servlet 使用的 HandlerMappings 列表。
		private List<HandlerMapping> handlerMappings;
		// 此 servlet 使用的 HandlerAdapters 列表。
		private List<HandlerAdapter> handlerAdapters;
		// 此 servlet 使用的 HandlerExceptionResolver 列表。
		private List<HandlerExceptionResolver> handlerExceptionResolvers;
		// 此 servlet 使用的 RequestToViewNameTranslator。
		private RequestToViewNameTranslator viewNameTranslator;
		// 此 servlet 使用的 FlashMapManager。
		private FlashMapManager flashMapManager;
		// 此 servlet 使用的 ViewResolver 列表。
		private List<ViewResolver> viewResolvers;
		// ...
		private boolean parseRequestPath;

		/**
		 * @see org.springframework.web.servlet.DispatcherServlet#initStrategies(org.springframework.context.ApplicationContext)
		 */
		// 初始化此 Servlet 使用的策略对象。
		// <p>可以在子类中被覆盖，以便初始化更多的策略对象。
		protected void initStrategies(ApplicationContext context) { // important -> go
			// 初始化多文件上传的组件（MultipartResolver）。如果在 BeanFactory 中没有，则不提供。
			initMultipartResolver(context);
			// 初始化本地语言环境（LocaleResolver）。如果在 BeanFactory 中没有，默认为 AcceptHeaderLocaleResolver。
			initLocaleResolver(context);
			// 初始化模板处理器（ThemeResolver）。如果在 BeanFactory 中没有，默认使用 FixedThemeResolver。
			initThemeResolver(context);
			// 初始化 HandlerMapping。如果在 BeanFactory 中没有，默认使用 BeanNameUrlHandlerMapping。
			initHandlerMappings(context);
			// 初始化参数适配器（HandlerAdapter）。如果在 BeanFactory 中没有，默认为 SimpleControllerHandlerAdapter。
			initHandlerAdapters(context);
			// 初始化异常拦截器（HandlerExceptionResolver）。如果在 BeanFactory 中没有，则通过注册默认 HandlerExceptionResolvers。
			initHandlerExceptionResolvers(context);
			// 初始化视图预处理器（RequestToViewNameTranslator）。如果在 BeanFactory 中没有，则默认为 DefaultRequestToViewNameTranslator。
			initRequestToViewNameTranslator(context);
			// 初始化视图转换器（ViewResolver）。如果在 BeanFactory 中没有，则默认为 InternalResourceViewResolver。
			initViewResolvers(context);
			// 初始化 FlashMap 管理器（FlashMapManager）。如果在 BeanFactory 中没有，则默认为 DefaultFlashMapManager。
			initFlashMapManager(context);
			// 默认配置存在文件 DispatcherServlet.properties 中
			// org.springframework.web.servlet.DispatcherServlet.DEFAULT_STRATEGIES_PATH -> org/springframework/web/servlet/DispatcherServlet.properties
		}

		// 初始化此类使用的 MultipartResolver。
		// <p>如果此命名空间的 BeanFactory 中未定义具有给定名称的 bean，则不提供多部分处理。
		private void initMultipartResolver(ApplicationContext context) {
			try {
				// MULTIPART_RESOLVER_BEAN_NAME = multipartResolver
				this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
				// ...
			} catch (NoSuchBeanDefinitionException ex) {
				// 默认没有多部分解析器。
				this.multipartResolver = null;
				// ...
			}
		}

		// 初始化此类使用的 LocaleResolver。
		// <p>如果此命名空间的 BeanFactory 中未定义具有给定名称的 bean，则我们默认使用 AcceptHeaderLocaleResolver。
		private void initLocaleResolver(ApplicationContext context) {
			try {
				// LOCALE_RESOLVER_BEAN_NAME = localeResolver
				this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
				// ...
			} catch (NoSuchBeanDefinitionException ex) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
				// ...
			}
		}

		// 初始化此类使用的 ThemeResolver。
		// <p>如果此命名空间的 BeanFactory 中未定义具有给定名称的 bean，则我们默认使用 FixedThemeResolver。
		@Deprecated
		private void initThemeResolver(ApplicationContext context) {
			try {
				// THEME_RESOLVER_BEAN_NAME = themeResolver
				this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
				// ...
			} catch (NoSuchBeanDefinitionException ex) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
				// ...
			}
		}

		// 初始化此类使用的 HandlerMappings。
		// <p>如果 BeanFactory 中没有为此命名空间定义 HandlerMapping bean，则我们默认使用 BeanNameUrlHandlerMapping。
		private void initHandlerMappings(ApplicationContext context) {
			this.handlerMappings = null;

			if (this.detectAllHandlerMappings) {
				// 查找 ApplicationContext 中的所有 HandlerMappings，包括祖先上下文。
				Map<String, HandlerMapping> matchingBeans =
						BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
				if (!matchingBeans.isEmpty()) {
					this.handlerMappings = new ArrayList<>(matchingBeans.values());
					// 我们保持 HandlerMappings 处于排序状态。
					AnnotationAwareOrderComparator.sort(this.handlerMappings);
				}
			} else {
				try {
					// HANDLER_MAPPING_BEAN_NAME = "handlerMapping"
					HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
					this.handlerMappings = Collections.singletonList(hm);
				} catch (NoSuchBeanDefinitionException ex) {
					// 忽略，我们稍后会添加一个默认的 HandlerMapping。
				}
			}

			// Ensure we have at least one HandlerMapping, by registering
			// a default HandlerMapping if no other mappings are found.
			// --> 译文：确保我们至少有一个 HandlerMapping，如果没有找到其他映射，则注册一个默认 HandlerMapping。
			if (this.handlerMappings == null) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
				// ...
			}

			for (HandlerMapping mapping : this.handlerMappings) {
				if (mapping.usesPathPatterns()) {
					this.parseRequestPath = true;
					break;
				}
			}
		}

		// 初始化此类使用的 HandlerAdapter。
		// <p>如果 BeanFactory 中未为此命名空间定义 HandlerAdapter bean，则我们默认使用 SimpleControllerHandlerAdapter。
		private void initHandlerAdapters(ApplicationContext context) {
			this.handlerAdapters = null;

			if (this.detectAllHandlerAdapters) {
				// 查找 ApplicationContext 中的所有 HandlerAdapter，包括祖先上下文。
				Map<String, HandlerAdapter> matchingBeans =
						BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
				if (!matchingBeans.isEmpty()) {
					this.handlerAdapters = new ArrayList<>(matchingBeans.values());
					// 我们保持 HandlerAdapters 处于排序状态。
					AnnotationAwareOrderComparator.sort(this.handlerAdapters);
				}
			} else {
				try {
					// HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter"
					HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
					this.handlerAdapters = Collections.singletonList(ha);
				} catch (NoSuchBeanDefinitionException ex) {
					// 忽略，我们稍后会添加一个默认的 HandlerAdapter。
				}
			}

			// 确保我们至少有一些 HandlerAdapters，如果没有找到其他适配器，则注册默认 HandlerAdapters。
			if (this.handlerAdapters == null) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
				// ...
			}
		}

		// 初始化此类使用的 HandlerExceptionResolver。
		// <p>如果此命名空间的 BeanFactory 中未定义具有给定名称的 bean，则我们默认不使用异常解析器。
		private void initHandlerExceptionResolvers(ApplicationContext context) {
			this.handlerExceptionResolvers = null;

			if (this.detectAllHandlerExceptionResolvers) {
				// 查找 ApplicationContext 中的所有 HandlerExceptionResolvers，包括祖先上下文。
				Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
						.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
				if (!matchingBeans.isEmpty()) {
					this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
					// 我们保持 HandlerExceptionResolver 处于排序状态。
					AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
				}
			} else {
				try {
					// HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver"
					HandlerExceptionResolver her =
							context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
					this.handlerExceptionResolvers = Collections.singletonList(her);
				} catch (NoSuchBeanDefinitionException ex) {
					// 忽略，没有 HandlerExceptionResolver 也可以。
				}
			}

			// 确保我们至少有一些 HandlerExceptionResolvers，如果未找到其他解析器，则通过注册默认 HandlerExceptionResolvers。
			if (this.handlerExceptionResolvers == null) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
				// ...
			}
		}

		// 初始化此 servlet 实例使用的 RequestToViewNameTranslator。
		// <p>如果没有配置实现，则默认使用 DefaultRequestToViewNameTranslator。
		private void initRequestToViewNameTranslator(ApplicationContext context) {
			try {
				// REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator"
				this.viewNameTranslator =
						context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
				// ...
			} catch (NoSuchBeanDefinitionException ex) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
				// ...
			}
		}

		// 初始化此类使用的 ViewResolvers。
		// <p>如果此命名空间的 BeanFactory 中未定义 ViewResolver bean，则我们默认使用 InternalResourceViewResolver。
		private void initViewResolvers(ApplicationContext context) {
			this.viewResolvers = null;

			if (this.detectAllViewResolvers) {
				// 查找 ApplicationContext 中的所有 ViewResolvers，包括祖先上下文。
				Map<String, ViewResolver> matchingBeans =
						BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
				if (!matchingBeans.isEmpty()) {
					this.viewResolvers = new ArrayList<>(matchingBeans.values());
					// 我们保持 ViewResolvers 处于排序状态。
					AnnotationAwareOrderComparator.sort(this.viewResolvers);
				}
			} else {
				try {
					// VIEW_RESOLVER_BEAN_NAME = "viewResolver"
					ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
					this.viewResolvers = Collections.singletonList(vr);
				} catch (NoSuchBeanDefinitionException ex) {
					// 忽略，我们稍后会添加一个默认的 ViewResolver。
				}
			}

			//  --> 译文：确保我们至少有一个 ViewResolver，如果没有找到其他解析器，则注册一个默认的 ViewResolver。
			if (this.viewResolvers == null) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
				// ...
			}
		}

		// 初始化此 servlet 实例使用的 {@link FlashMapManager}。
		// <p>如果没有配置实现，则默认使用 {@code org.springframework.web.servlet.support.DefaultFlashMapManager}。
		private void initFlashMapManager(ApplicationContext context) {
			try {
				// FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager"
				this.flashMapManager = context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
				// ...
			} catch (NoSuchBeanDefinitionException ex) {
				// 返回给定策略接口的默认策略对象。默认配置：org/springframework/web/servlet/DispatcherServlet.properties
				this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
				// ...
			}
		}

		// ==============================================================================================================
		// ---------------------						getDefaultStrategy							---------------------
		// ==============================================================================================================
		// 定义 DispatcherServlet 默认策略名称的类路径资源名称（相对于 DispatcherServlet 类）。
		private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";
		// 存储默认策略实现
		private static Properties defaultStrategies;

		// 返回给定策略接口的默认策略对象。
		// <p>默认实现委托给 {@link #getDefaultStrategies}，需要列表中的单个对象。
		protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
			List<T> strategies = getDefaultStrategies(context, strategyInterface);
			if (strategies.size() != 1) {
				throw new BeanInitializationException("DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
			}
			return strategies.get(0);
		}

		// 为给定的策略接口创建一个默认策略对象列表。
		// <p>默认实现使用 “DispatcherServlet.properties” 文件（与 DispatcherServlet 类位于同一包中）来确定类名。
		// 它通过上下文的 BeanFactory 实例化策略对象。
		@SuppressWarnings("unchecked")
		protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) { // important -> go
			if (defaultStrategies == null) {
				try {
					// 译文：从属性文件加载默认策略实现。这目前是严格的内部操作，并不适合由应用程序开发人员定制。
					// DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties"
					ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);  // important -> go
					defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);  // important -> go
				} catch (IOException ex) {
					throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
				}
			}

			String key = strategyInterface.getName();
			String value = defaultStrategies.getProperty(key);
			if (value != null) {
				String[] classNames = StringUtils.commaDelimitedListToStringArray(value);	// 将逗号分隔的列表转换为字符串数组
				List<T> strategies = new ArrayList<>(classNames.length);
				for (String className : classNames) {
					try {
						Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
						Object strategy = createDefaultStrategy(context, clazz);
						strategies.add((T) strategy);
					}
					catch (ClassNotFoundException ex) {
						// ...
					}
					// ...
				}
				return strategies;
			}
			else {
				return Collections.emptyList();
			}
		}

		// 创建默认策略。
		// <p>默认实现使用 {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean(Class)}。
		protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
			return context.getAutowireCapableBeanFactory().createBean(clazz);
		}

	}
}
