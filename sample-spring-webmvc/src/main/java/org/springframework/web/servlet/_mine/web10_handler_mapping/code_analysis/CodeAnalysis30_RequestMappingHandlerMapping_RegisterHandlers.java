package org.springframework.web.servlet._mine.web10_handler_mapping.code_analysis;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * RequestMappingHandlerMapping
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 *
 * RequestMappingHandlerMapping - Register Handler -> RequestMappingHandlerMapping#afterPropertiesSet()
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#afterPropertiesSet()
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#afterPropertiesSet()
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#initHandlerMethods()
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#processCandidateBean(java.lang.String)
 *
 * ## 1. 期望 handler 具有类级别的 @Controller 注解
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#isHandler(Class)
 *
 * ## 2. 在指定的 handler bean 中查找 handler 方法
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#detectHandlerMethods(java.lang.Object)
 *
 * ### 2.1. 使用类和方法级别的 @RequestMapping 和 @HttpExchange 注解来创建 RequestMappingInfo -> RequestMappingHandlerMapping#getMappingForMethod(...)
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#getMappingForMethod(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#createRequestMappingInfo(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#createRequestMappingInfo(org.springframework.web.service.annotation.HttpExchange, org.springframework.web.servlet.mvc.condition.RequestCondition)
 *
 * ### 2.2. 选择目标类的可调用方法：如果实际在目标类上暴露，则选择给定方法本身，否则选择目标类的接口之一或目标类本身上的相应方法 -> AopUtils#selectInvocableMethod(...)
 *
 * @see org.springframework.aop.support.AopUtils#selectInvocableMethod(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.core.MethodIntrospector#selectInvocableMethod(java.lang.reflect.Method, java.lang.Class)
 *
 * ### 2.3. 注册一个 handler 方法及其唯一映射
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#registerHandlerMethod(java.lang.Object, java.lang.reflect.Method, org.springframework.web.servlet.mvc.method.RequestMappingInfo)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#registerHandlerMethod(java.lang.Object, java.lang.reflect.Method, java.lang.Object)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#updateConsumesCondition(org.springframework.web.servlet.mvc.method.RequestMappingInfo, java.lang.reflect.Method)
 */
public class CodeAnalysis30_RequestMappingHandlerMapping_RegisterHandlers {

	/**
	 * RequestMappingHandlerMapping
	 *
	 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
	 */
	// public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
	//		implements MatchableHandlerMapping, EmbeddedValueResolverAware { ... }
	static class CA01_RequestMappingHandlerMapping extends RequestMappingHandlerMapping {
		private Map<String, Predicate<Class<?>>> pathPrefixes = Collections.emptyMap();
		private StringValueResolver embeddedValueResolver;
		private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

		@Override
		@SuppressWarnings("deprecation")
		public void afterPropertiesSet() {
			// ...
			// 在初始化时检测 handler 方法。
			// super.afterPropertiesSet();
			((AbstractHandlerMethodMapping)this).afterPropertiesSet(); // important -> go
		}

		// <p>期望 handler 具有类级别的 @{@link Controller} 注解
		@Override
		protected boolean isHandler(Class<?> beanType) { // important -> go
			return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class); // important -> go
		}

		// 使用类和方法级别的 {@link RequestMapping @RequestMapping} 和
		// {@link HttpExchange @HttpExchange} 注解来创建 {@link RequestMappingInfo}。
		// @return 创建的 {@code RequestMappingInfo}，如果该方法没有 {@code @RequestMapping}
		// 或 {@code @HttpExchange} 注解，则返回 {@code null}
		@Override
		@Nullable
		protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) { // important -> go
			// 1. 根据 Method 上的 @HttpExchange 和 @RequestMapping 注解创建的 RequestMappingInfo
			RequestMappingInfo info = createRequestMappingInfo(method); // 方法 请求映射信息
			if (info != null) {
				// 2. 根据 Class 上的 @HttpExchange 和 @RequestMapping 注解创建的 RequestMappingInfo
				RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType); // important -> go
				if (typeInfo != null) {
					// 合并 Class 和 Method 的 RequestMappingInfo
					info = typeInfo.combine(info);
				}
				if (info.isEmptyMapping()) { // 请求映射是空的 URL 路径映射
					// 使用 "" 和 "/" 路径创建一个新的 RequestMappingInfo
					info = info.mutate().paths("", "/").options(this.config).build();
				}
				String prefix = getPathPrefix(handlerType);
				if (prefix != null) {
					// 使用给定的路径创建一个新的 RequestMappingInfo
					info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
				}
			}
			return info;
		}

		@Nullable
		private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
			RequestMappingInfo requestMappingInfo = null;
			// getCustomTypeCondition(clazz) -> 提供自定义类型级请求条件。
			// getCustomMethodCondition((Method) element) -> 提供自定义方法级请求条件。
			RequestCondition<?> customCondition = (element instanceof Class<?> clazz ?
					getCustomTypeCondition(clazz) : getCustomMethodCondition((Method) element));

			// 1. 创建一个新的 MergedAnnotations 实例，其中包含来自指定元素的所有注释和元注释，并且取决于 SearchStrategy，相关的继承元素。
			// 然后 查询 @RequestMapping 和 @HttpExchange 注解，并封装成 AnnotationDescriptor 返回
			List<AnnotationDescriptor> descriptors = getAnnotationDescriptors(element); // important -> go

			// 2. 处理 @RequestMapping 注解相关的 AnnotationDescriptor
			List<AnnotationDescriptor> requestMappings = descriptors.stream()
					.filter(desc -> desc.annotation instanceof RequestMapping).toList(); // important -> go
			if (!requestMappings.isEmpty()) {
				if (requestMappings.size() > 1 && logger.isWarnEnabled()) {
					logger.warn("Multiple @RequestMapping annotations found on %s, but only the first will be used: %s"
							.formatted(element, requestMappings));
				}
				// 从提供的 {@link RequestMapping @RequestMapping} 注释、元注释或在注释层次结构中合并注释属性的合成结果创建 {@link RequestMappingInfo}。
				requestMappingInfo = createRequestMappingInfo((RequestMapping) requestMappings.get(0).annotation, customCondition);
			}

			// 3. 处理 @HttpExchange 注解相关的 AnnotationDescriptor
			List<AnnotationDescriptor> httpExchanges = descriptors.stream()
					.filter(desc -> desc.annotation instanceof HttpExchange).toList(); // important -> go
			if (!httpExchanges.isEmpty()) {
				Assert.state(requestMappingInfo == null,
						() -> "%s is annotated with @RequestMapping and @HttpExchange annotations, but only one is allowed: %s"
								.formatted(element, Stream.of(requestMappings, httpExchanges).flatMap(List::stream).toList()));
				Assert.state(httpExchanges.size() == 1,
						() -> "Multiple @HttpExchange annotations found on %s, but only one is allowed: %s"
								.formatted(element, httpExchanges));
				// 从提供的 {@link RequestMapping @RequestMapping} 注释、元注释或在注释层次结构中合并注释属性的合成结果创建 {@link RequestMappingInfo}。
				requestMappingInfo = createRequestMappingInfo((HttpExchange) httpExchanges.get(0).annotation, customCondition);
			}

			// 根据 @HttpExchange 创建的 RequestMappingInfo 会覆盖 @RequestMapping 创建的 RequestMappingInfo
			return requestMappingInfo;
		}

		@Nullable
		String getPathPrefix(Class<?> handlerType) {
			for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
				if (entry.getValue().test(handlerType)) {
					String prefix = entry.getKey();
					if (this.embeddedValueResolver != null) {
						prefix = this.embeddedValueResolver.resolveStringValue(prefix);
					}
					return prefix;
				}
			}
			return null;
		}

		private static List<AnnotationDescriptor> getAnnotationDescriptors(AnnotatedElement element) { // important -> go
			// 创建一个新的 {@link MergedAnnotations} 实例，其中包含来自指定元素的所有注释和元注释，并且取决于 {@link SearchStrategy}，相关的继承元素。
			return MergedAnnotations.from(element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none())
					.stream()
					// 创建一个新的 {@link Predicate}，如果指定数组中包含 {@linkplain MergedAnnotation#getType() 合并的注解类型}，则结果为 {@code true}。
					.filter(MergedAnnotationPredicates.typeIn(RequestMapping.class, HttpExchange.class))
					// 创建一个新的有状态的、一次性使用的 {@link Predicate}，该谓词仅匹配提取值的第一个运行。
					.filter(MergedAnnotationPredicates.firstRunOf(MergedAnnotation::getAggregateIndex)) // 获取包含此注解的聚合集合的索引。
					.map(AnnotationDescriptor::new)
					.distinct()
					.toList();
		}

		// 从提供的 {@link RequestMapping @RequestMapping} 注释、元注释或在注释层次结构中合并注释属性的合成结果创建 {@link RequestMappingInfo}。
		protected RequestMappingInfo createRequestMappingInfo(
				RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) { // important -> go

			// RequestMappingInfo.DefaultBuilder
			RequestMappingInfo.Builder builder = RequestMappingInfo
					.paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
					.methods(requestMapping.method())
					.params(requestMapping.params())
					.headers(requestMapping.headers())
					.consumes(requestMapping.consumes())
					.produces(requestMapping.produces())
					.mappingName(requestMapping.name());

			if (customCondition != null) {
				builder.customCondition(customCondition);
			}

			return builder.options(this.config).build();
		}

		@Override
		protected Set<String> getMappingPathPatterns(RequestMappingInfo info) {
			return info.getPatternValues();
		}

		private static class AnnotationDescriptor {
			private final Annotation annotation;
			private final MergedAnnotation<?> root;

			AnnotationDescriptor(MergedAnnotation<Annotation> mergedAnnotation) {
				this.annotation = mergedAnnotation.synthesize();
				this.root = mergedAnnotation.getRoot();
			}

			@Override
			public boolean equals(Object obj) {
				return (obj instanceof AnnotationDescriptor that && this.annotation.equals(that.annotation));
			}

			@Override
			public int hashCode() {
				return this.annotation.hashCode();
			}

			@Override
			public String toString() {
				return this.root.synthesize().toString();
			}
		}
	}

	/**
	 * @see AbstractHandlerMethodMapping
	 */
	// public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean { ... }
	static abstract class CA02_AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {
		private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";
		private boolean detectHandlerMethodsInAncestorContexts = false;
		private HandlerMethodMappingNamingStrategy<T> namingStrategy;
		private final MappingRegistry mappingRegistry = new MappingRegistry();

		// 在初始化时检测处理程序方法。
		@Override
		public void afterPropertiesSet() {
			// 扫描 ApplicationContext 中的 bean，检测并注册 handler 程序方法。
			initHandlerMethods();  // important -> go
		}

		// 扫描 ApplicationContext 中的 bean，检测并注册 handler 程序方法。
		protected void initHandlerMethods() {
			for (String beanName : getCandidateBeanNames()) {
				// SCOPED_TARGET_NAME_PREFIX = "scopedTarget."
				if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
					// 处理候选 Bean
					processCandidateBean(beanName); // important -> go
				}
			}
			// handlerMethodsInitialized(getHandlerMethods()); // 日志打印总计；总计包括检测到的映射 + 通过 registerMapping 显式注册
		}

		// 确定应用程序上下文中候选 bean 的名称。
		protected String[] getCandidateBeanNames() {
			return (this.detectHandlerMethodsInAncestorContexts ?
					// 获取给定类型的所有 Bean 名称，包括祖先工厂中定义的 Bean 名称。
					BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
					// 返回与给定类型（包括子类）匹配的 Bean 的名称，
					obtainApplicationContext().getBeanNamesForType(Object.class));
		}

		// 确定指定候选 bean 的类型，如果确定为处理程序类型，则调用 {@link #detectHandlerMethods}。
		// <p>此实现通过检查 {@link org.springframework.beans.factory.BeanFactory#getType}
		// 并使用 bean 名称调用 {@link #detectHandlerMethods} 来避免创建 bean。
		// @param beanName 为候选 bean 的名称
		protected void processCandidateBean(String beanName) {
			Class<?> beanType = null;
			try {
				// 确定具有给定名称的 bean 的类型。
				beanType = obtainApplicationContext().getType(beanName);
			}
			catch (Throwable ex) {
				// An unresolvable bean type, probably from a lazy bean - let's ignore it.
				// --> 译文：一种无法解析的 bean 类型，可能来自惰性 bean - 让我们忽略它。
				if (logger.isTraceEnabled()) {
					logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
				}
			}
			// RequestMappingHandlerMapping.isHandler()：期望处理程序具有类型级别的 @{@link Controller} 注释。
			if (beanType != null && isHandler(beanType)) {
				// 在指定的 handler bean 中查找 handler 方法
				detectHandlerMethods(beanName); // important -> go
			}
		}

		// 在指定的 handler bean 中查找 handler 方法。
		// @param handler 可以是 bean 名称或实际的 handler 实例
		protected void detectHandlerMethods(Object handler) {
			// 确定 handler 的类型
			Class<?> handlerType = (handler instanceof String beanName ?
					obtainApplicationContext().getType(beanName) : handler.getClass());

			if (handlerType != null) {
				Class<?> userType = ClassUtils.getUserClass(handlerType);
				// 根据关联元数据的查找，选择给定目标类型的方法。 T -> RequestMappingInfo
				Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
						(MethodIntrospector.MetadataLookup<T>) method -> {
							try {
								// 使用类和方法级别的 @RequestMapping 和 @HttpExchange 注解来创建 RequestMappingInfo
								return getMappingForMethod(method, userType);  // important -> go
							}
							catch (Throwable ex) {
								throw new IllegalStateException("Invalid mapping on handler class [" +
										userType.getName() + "]: " + method, ex);
							}
						});
				// ...
				// mapping -> RequestMappingInfo
				methods.forEach((method, mapping) -> {
					// 选择目标类的可调用方法：如果实际在目标类上暴露，则选择给定方法本身，否则选择目标类的接口之一或目标类本身上的相应方法。
					Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
					// 注册一个 handler 方法及其唯一映射
					registerHandlerMethod(handler, invocableMethod, mapping);  // important -> go
				});
			}
		}

		// 注册一个 handler 方法及其唯一映射。在启动时为每个检测到的 handler 方法调用。
		// @param handler 处理程序的 bean 名称或处理程序实例
		// @param method 要注册的方法
		// @param mapping 与处理程序方法关联的映射条件
		// 如果另一个方法已在同一映射下注册，则抛出 IllegalStateException
		protected void registerHandlerMethod(Object handler, Method method, T mapping) {
			this.mappingRegistry.register(mapping, handler, method);
		}

		// 返回非模式的请求映射路径。
		protected Set<String> getDirectPaths(T mapping) {
			Set<String> urls = Collections.emptySet();
			// getMappingPathPatterns(mapping) -> 提取并返回所提供映射中包含的 URL 路径。
			for (String path : getMappingPathPatterns(mapping)) {
				// 给定的 {@code path} 是否代表可由此接口的实现匹配的模式？
				if (!getPathMatcher().isPattern(path)) {
					urls = (urls.isEmpty() ? new HashSet<>(1) : urls);
					urls.add(path);
				}
			}
			return urls;
		}

		protected Set<String> getMappingPathPatterns(T mapping) {
			return Collections.emptySet();
		}

		// 返回配置的命名策略或{@code null}。
		@Nullable
		public HandlerMethodMappingNamingStrategy<T> getNamingStrategy() {
			return this.namingStrategy;
		}

		// 指定类型是否为具有处理程序方法的处理程序。
		// @param beanType 被检查的 bean 的类型
		// @return 如果是处理程序类型，则返回 "true"；否则返回 "false"。
		protected abstract boolean isHandler(Class<?> beanType);

		// 提供 handler 方法的映射。无法提供映射的方法不是处理程序方法。
		// @param method 提供映射的方法
		// @param handlerType 处理程序类型，可能是方法声明类的子类型
		// @return 映射，如果方法未映射，则返回 {@code null}
		@Nullable
		protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

		// 一个注册表，用于维护所有到处理程序方法的映射，公开执行查找的方法并提供并发访问。
		//
		// <p>用于测试目的的包私有。
		class MappingRegistry {
			private final Map<T, MappingRegistration<T>> registry = new HashMap<>();
			private final MultiValueMap<String, T> pathLookup = new LinkedMultiValueMap<>();
			private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();
			private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();
			private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

			public void register(T mapping, Object handler, Method method) {
				this.readWriteLock.writeLock().lock();
				try {
					// 1. 创建 HandlerMethod 实例。
					HandlerMethod handlerMethod = createHandlerMethod(handler, method);
					// validateMethodMapping(handlerMethod, mapping);

					// Enable method validation, if applicable --> 译文：启用方法验证（如果适用）
					handlerMethod = handlerMethod.createWithValidateFlags();

					// 2. 返回非模式的请求映射路径。并注册到 pathLookup
					// Set<String> directPaths = AbstractHandlerMethodMapping.this.getDirectPaths(mapping);
					Set<String> directPaths = CA02_AbstractHandlerMethodMapping.this.getDirectPaths(mapping);
					for (String path : directPaths) {
						this.pathLookup.add(path, mapping);
					}

					// 3. 确定给定 HandlerMethod 和映射的名称。
					String name = null;
					// 返回配置的命名策略或{@code null}。
					if (getNamingStrategy() != null) {
						name = getNamingStrategy().getName(handlerMethod, mapping);
						addMappingName(name, handlerMethod);
					}

					// 4. 根据类型/方法上的 @CrossOrigin 来初始化一个 CorsConfiguration，并注册到 corsLookup
					CorsConfiguration corsConfig = initCorsConfiguration(handler, method, mapping);
					if (corsConfig != null) {
						corsConfig.validateAllowCredentials();
						corsConfig.validateAllowPrivateNetwork();
						this.corsLookup.put(handlerMethod, corsConfig);
					}

					// 注册 mapping 到 MappingRegistry
					this.registry.put(mapping,
							new MappingRegistration<>(mapping, handlerMethod, directPaths, name, corsConfig != null));
				}
				finally {
					this.readWriteLock.writeLock().unlock();
				}
			}

			// 创建 HandlerMethod 实例。
			// @param handler 可以是 Bean 名称或实际的处理程序实例
			// @param method 目标方法
			// @return 已创建的 HandlerMethod
			protected HandlerMethod createHandlerMethod(Object handler, Method method) {
				if (handler instanceof String beanName) {
					return new HandlerMethod(beanName,
							obtainApplicationContext().getAutowireCapableBeanFactory(),
							obtainApplicationContext(),
							method);
				}
				return new HandlerMethod(handler, method);
			}

			// 提取并返回映射的 CORS 配置。
			@Nullable
			protected CorsConfiguration initCorsConfiguration(Object handler, Method method, T mapping) {
				return null;
			}

			private void addMappingName(String name, HandlerMethod handlerMethod) {
				List<HandlerMethod> oldList = this.nameLookup.get(name);
				if (oldList == null) {
					oldList = Collections.emptyList();
				}

				for (HandlerMethod current : oldList) {
					if (handlerMethod.equals(current)) {
						return;
					}
				}

				List<HandlerMethod> newList = new ArrayList<>(oldList.size() + 1);
				newList.addAll(oldList);
				newList.add(handlerMethod);
				this.nameLookup.put(name, newList);
			}
		}

		static class MappingRegistration<T> {
			private final T mapping;
			private final HandlerMethod handlerMethod;
			private final Set<String> directPaths;
			private final String mappingName;
			private final boolean corsConfig;

			public MappingRegistration(T mapping, HandlerMethod handlerMethod,
									   @Nullable Set<String> directPaths, @Nullable String mappingName, boolean corsConfig) {

				Assert.notNull(mapping, "Mapping must not be null");
				Assert.notNull(handlerMethod, "HandlerMethod must not be null");
				this.mapping = mapping;
				this.handlerMethod = handlerMethod;
				this.directPaths = (directPaths != null ? directPaths : Collections.emptySet());
				this.mappingName = mappingName;
				this.corsConfig = corsConfig;
			}

			public T getMapping() {
				return this.mapping;
			}

			public HandlerMethod getHandlerMethod() {
				return this.handlerMethod;
			}

			public Set<String> getDirectPaths() {
				return this.directPaths;
			}

			@Nullable
			public String getMappingName() {
				return this.mappingName;
			}

			public boolean hasCorsConfig() {
				return this.corsConfig;
			}
		}
	}

}
