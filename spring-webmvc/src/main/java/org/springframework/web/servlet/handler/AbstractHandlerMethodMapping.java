/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.web.servlet.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodIntrospector;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Abstract base class for {@link HandlerMapping} implementations that define
 * a mapping between a request and a {@link HandlerMethod}.
 *
 * Abstract base class for URL-mapped {@link HandlerMapping} implementations.
 *
 * <p>For each registered handler method, a unique mapping is maintained with
 * subclasses defining the details of the mapping type {@code <T>}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @param <T> the mapping for a {@link HandlerMethod} containing the conditions
 * needed to match the handler method to an incoming request.
 */
// {@link HandlerMapping} 实现的抽象基类，定义 request 和 {@link HandlerMethod} 之间的映射。
//
// <p>对于每个已注册的处理程序方法，都会维护一个唯一的映射，并通过子类定义映射类型 {@code <T>} 的详细信息。
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {

	/**
	 * Bean name prefix for target beans behind scoped proxies. Used to exclude those
	 * targets from handler method detection, in favor of the corresponding proxies.
	 * <p>We're not checking the autowire-candidate status here, which is how the
	 * proxy target filtering problem is being handled at the autowiring level,
	 * since autowire-candidate may have been turned to {@code false} for other
	 * reasons, while still expecting the bean to be eligible for handler methods.
	 * <p>Originally defined in {@link org.springframework.aop.scope.ScopedProxyUtils}
	 * but duplicated here to avoid a hard dependency on the spring-aop module.
	 */
	// 作用域代理后的目标 Bean 的名称前缀。用于将这些目标从处理程序方法检测中排除，以支持相应的代理。
	// <p>我们这里不检查自动装配候选状态，这是在自动装配级别处理代理目标过滤问题的方式，
	// 因为自动装配候选可能由于其他原因已变为 {@code false}，但仍然期望该 Bean 符合处理程序方法的条件。
	// <p>最初在 {@link org.springframework.aop.scope.ScopedProxyUtils} 中定义，但在此处重复，以避免对 spring-aop 模块的硬依赖。
	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

	private static final HandlerMethod PREFLIGHT_AMBIGUOUS_MATCH =
			new HandlerMethod(new EmptyHandler(), ClassUtils.getMethod(EmptyHandler.class, "handle"));

	private static final CorsConfiguration ALLOW_CORS_CONFIG = new CorsConfiguration();

	static {
		ALLOW_CORS_CONFIG.addAllowedOriginPattern("*");
		ALLOW_CORS_CONFIG.addAllowedMethod("*");
		ALLOW_CORS_CONFIG.addAllowedHeader("*");
		ALLOW_CORS_CONFIG.setAllowCredentials(true);
	}


	private boolean detectHandlerMethodsInAncestorContexts = false;

	@Nullable
	private HandlerMethodMappingNamingStrategy<T> namingStrategy;

	private final MappingRegistry mappingRegistry = new MappingRegistry();


	@Override
	public void setPatternParser(@Nullable PathPatternParser patternParser) {
		Assert.state(this.mappingRegistry.getRegistrations().isEmpty(),
				"PathPatternParser must be set before the initialization of " +
						"request mappings through InitializingBean#afterPropertiesSet.");
		super.setPatternParser(patternParser);
	}

	/**
	 * Whether to detect handler methods in beans in ancestor ApplicationContexts.
	 * <p>Default is "false": Only beans in the current ApplicationContext are
	 * considered, i.e. only in the context that this HandlerMapping itself
	 * is defined in (typically the current DispatcherServlet's context).
	 * <p>Switch this flag on to detect handler beans in ancestor contexts
	 * (typically the Spring root WebApplicationContext) as well.
	 * @see #getCandidateBeanNames()
	 */
	public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
		this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
	}

	/**
	 * Configure the naming strategy to use for assigning a default name to every
	 * mapped handler method.
	 * <p>The default naming strategy is based on the capital letters of the
	 * class name followed by "#" and then the method name, e.g. "TC#getFoo"
	 * for a class named TestController with method getFoo.
	 */
	public void setHandlerMethodMappingNamingStrategy(HandlerMethodMappingNamingStrategy<T> namingStrategy) {
		this.namingStrategy = namingStrategy;
	}

	/**
	 * Return the configured naming strategy or {@code null}.
	 */
	// 返回配置的命名策略或{@code null}。
	@Nullable
	public HandlerMethodMappingNamingStrategy<T> getNamingStrategy() {
		return this.namingStrategy;
	}

	/**
	 * Return a (read-only) map with all mappings and HandlerMethod's.
	 */
	// 返回包含所有映射和 HandlerMethod 的（只读）映射。
	public Map<T, HandlerMethod> getHandlerMethods() {
		this.mappingRegistry.acquireReadLock();
		try {
			return this.mappingRegistry.getRegistrations().entrySet().stream()
					.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().handlerMethod));
		}
		finally {
			this.mappingRegistry.releaseReadLock();
		}
	}

	/**
	 * Return the handler methods for the given mapping name.
	 * @param mappingName the mapping name
	 * @return a list of matching HandlerMethod's or {@code null}; the returned
	 * list will never be modified and is safe to iterate.
	 * @see #setHandlerMethodMappingNamingStrategy
	 */
	@Nullable
	public List<HandlerMethod> getHandlerMethodsForMappingName(String mappingName) {
		return this.mappingRegistry.getHandlerMethodsByMappingName(mappingName);
	}

	/**
	 * Return the internal mapping registry. Provided for testing purposes.
	 */
	MappingRegistry getMappingRegistry() {
		return this.mappingRegistry;
	}

	/**
	 * Register the given mapping.
	 * <p>This method may be invoked at runtime after initialization has completed.
	 * @param mapping the mapping for the handler method
	 * @param handler the handler
	 * @param method the method
	 */
	public void registerMapping(T mapping, Object handler, Method method) {
		if (logger.isTraceEnabled()) {
			logger.trace("Register \"" + mapping + "\" to " + method.toGenericString());
		}
		this.mappingRegistry.register(mapping, handler, method);
	}

	/**
	 * Un-register the given mapping.
	 * <p>This method may be invoked at runtime after initialization has completed.
	 * @param mapping the mapping to unregister
	 */
	public void unregisterMapping(T mapping) {
		if (logger.isTraceEnabled()) {
			logger.trace("Unregister mapping \"" + mapping + "\"");
		}
		this.mappingRegistry.unregister(mapping);
	}


	// Handler method detection

	/**
	 * Detects handler methods at initialization.
	 * @see #initHandlerMethods
	 */
	// 在初始化时检测处理程序方法。
	@Override
	public void afterPropertiesSet() {
		// 扫描 ApplicationContext 中的 bean，检测并注册 handler 程序方法。
		initHandlerMethods();
	}

	/**
	 * Scan beans in the ApplicationContext, detect and register handler methods.
	 * @see #getCandidateBeanNames()
	 * @see #processCandidateBean
	 * @see #handlerMethodsInitialized
	 */
	// 扫描 ApplicationContext 中的 bean，检测并注册 handler 程序方法。
	protected void initHandlerMethods() {
		for (String beanName : getCandidateBeanNames()) {
			// SCOPED_TARGET_NAME_PREFIX = "scopedTarget."
			if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
				processCandidateBean(beanName); // 处理候选 Bean
			}
		}
		handlerMethodsInitialized(getHandlerMethods()); // 日志打印总计；总计包括检测到的映射 + 通过 registerMapping 显式注册
	}

	/**
	 * Determine the names of candidate beans in the application context.
	 * @since 5.1
	 * @see #setDetectHandlerMethodsInAncestorContexts
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors
	 */
	// 确定应用程序上下文中候选 bean 的名称。
	protected String[] getCandidateBeanNames() {
		return (this.detectHandlerMethodsInAncestorContexts ?
				// 获取给定类型的所有 Bean 名称，包括祖先工厂中定义的 Bean 名称。
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
				// 返回与给定类型（包括子类）匹配的 Bean 的名称，
				obtainApplicationContext().getBeanNamesForType(Object.class));
	}

	/**
	 * Determine the type of the specified candidate bean and call
	 * {@link #detectHandlerMethods} if identified as a handler type.
	 * <p>This implementation avoids bean creation through checking
	 * {@link org.springframework.beans.factory.BeanFactory#getType}
	 * and calling {@link #detectHandlerMethods} with the bean name.
	 * @param beanName the name of the candidate bean
	 * @since 5.1
	 * @see #isHandler
	 * @see #detectHandlerMethods
	 */
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
			// 检测处理程序方法
			detectHandlerMethods(beanName); // 在指定的处理程序 bean 中查找处理程序方法。
		}
	}

	/**
	 * Look for handler methods in the specified handler bean.
	 * @param handler either a bean name or an actual handler instance
	 * @see #getMappingForMethod
	 */
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
							return getMappingForMethod(method, userType);
						}
						catch (Throwable ex) {
							throw new IllegalStateException("Invalid mapping on handler class [" +
									userType.getName() + "]: " + method, ex);
						}
					});
			if (logger.isTraceEnabled()) {
				logger.trace(formatMappings(userType, methods));
			}
			else if (mappingsLogger.isDebugEnabled()) {
				mappingsLogger.debug(formatMappings(userType, methods));
			}
			// mapping -> RequestMappingInfo
			methods.forEach((method, mapping) -> {
				// 选择目标类的可调用方法：如果实际在目标类上暴露，则选择给定方法本身，否则选择目标类的接口之一或目标类本身上的相应方法。
				Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
				// 注册一个 handler 方法及其唯一映射
				registerHandlerMethod(handler, invocableMethod, mapping);
			});
		}
	}

	private String formatMappings(Class<?> userType, Map<Method, T> methods) {
		String packageName = ClassUtils.getPackageName(userType);
		String formattedType = (StringUtils.hasText(packageName) ?
				Arrays.stream(packageName.split("\\."))
						.map(packageSegment -> packageSegment.substring(0, 1))
						.collect(Collectors.joining(".", "", "." + userType.getSimpleName())) :
				userType.getSimpleName());
		Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
				.map(Class::getSimpleName)
				.collect(Collectors.joining(",", "(", ")"));
		return methods.entrySet().stream()
				.map(e -> {
					Method method = e.getKey();
					return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
				})
				.collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
	}

	/**
	 * Register a handler method and its unique mapping. Invoked at startup for
	 * each detected handler method.
	 * @param handler the bean name of the handler or the handler instance
	 * @param method the method to register
	 * @param mapping the mapping conditions associated with the handler method
	 * @throws IllegalStateException if another method was already registered
	 * under the same mapping
	 */
	// 注册一个 handler 方法及其唯一映射。在启动时为每个检测到的 handler 方法调用。
	// @param handler 处理程序的 bean 名称或处理程序实例
	// @param method 要注册的方法
	// @param mapping 与处理程序方法关联的映射条件
	// 如果另一个方法已在同一映射下注册，则抛出 IllegalStateException
	protected void registerHandlerMethod(Object handler, Method method, T mapping) {
		this.mappingRegistry.register(mapping, handler, method);
	}

	/**
	 * Create the HandlerMethod instance.
	 * @param handler either a bean name or an actual handler instance
	 * @param method the target method
	 * @return the created HandlerMethod
	 */
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

	/**
	 * Extract and return the CORS configuration for the mapping.
	 */
	// 提取并返回映射的 CORS 配置。
	@Nullable
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method, T mapping) {
		return null;
	}

	/**
	 * Invoked after all handler methods have been detected.
	 * @param handlerMethods a read-only map with handler methods and mappings.
	 */
	// 在检测到所有处理程序方法后调用。
	// @param handlerMethods 包含处理程序方法和映射的只读映射。
	protected void handlerMethodsInitialized(Map<T, HandlerMethod> handlerMethods) {
		// Total includes detected mappings + explicit registrations via registerMapping
		// --> 译文：总计包括检测到的映射 + 通过 registerMapping 显式注册
		int total = handlerMethods.size();
		if ((logger.isTraceEnabled() && total == 0) || (logger.isDebugEnabled() && total > 0) ) {
			logger.debug(total + " mappings in " + formatMappingName());
		}
	}


	// Handler method lookup

	/**
	 * Look up a handler method for the given request.
	 */
	// 查找给定请求的处理程序方法。
	@Override
	@Nullable
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		// 初始化用于请求映射的路径。
		String lookupPath = initLookupPath(request);
		this.mappingRegistry.acquireReadLock();
		try {
			// 查找与当前请求最匹配的处理程序方法。如果找到多个匹配项，则选择最佳匹配项。
			HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
			// handlerMethod.createWithResolvedBean() -> 如果提供的实例包含 bean 名称而不是对象实例，则在创建和返回 {@link HandlerMethod} 之前解析 bean 名称。
			return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
		}
		finally {
			this.mappingRegistry.releaseReadLock();
		}
	}

	/**
	 * Look up the best-matching handler method for the current request.
	 * If multiple matches are found, the best match is selected.
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 * @return the best-matching handler method, or {@code null} if no match
	 * @see #handleMatch(Object, String, HttpServletRequest)
	 * @see #handleNoMatch(Set, String, HttpServletRequest)
	 */
	// 查找与当前请求最匹配的处理程序方法。如果找到多个匹配项，则选择最佳匹配项。
	// @param lookupPath 映射当前 Servlet 映射中的查找路径
	// @param request 当前请求
	// @return 最匹配的处理程序方法，如果没有匹配，则返回 {@code null}
	@Nullable
	protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
		List<Match> matches = new ArrayList<>();
		List<T> directPathMatches = this.mappingRegistry.getMappingsByDirectPath(lookupPath);
		if (directPathMatches != null) {
			addMatchingMappings(directPathMatches, matches, request);
		}
		if (matches.isEmpty()) {
			addMatchingMappings(this.mappingRegistry.getRegistrations().keySet(), matches, request);
		}
		if (!matches.isEmpty()) {
			Match bestMatch = matches.get(0);
			if (matches.size() > 1) {
				Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
				matches.sort(comparator);
				bestMatch = matches.get(0);
				if (logger.isTraceEnabled()) {
					logger.trace(matches.size() + " matching mappings: " + matches);
				}
				if (CorsUtils.isPreFlightRequest(request)) {
					for (Match match : matches) {
						if (match.hasCorsConfig()) {
							return PREFLIGHT_AMBIGUOUS_MATCH;
						}
					}
				}
				else {
					Match secondBestMatch = matches.get(1);
					if (comparator.compare(bestMatch, secondBestMatch) == 0) {
						Method m1 = bestMatch.getHandlerMethod().getMethod();
						Method m2 = secondBestMatch.getHandlerMethod().getMethod();
						String uri = request.getRequestURI();
						throw new IllegalStateException(
								"Ambiguous handler methods mapped for '" + uri + "': {" + m1 + ", " + m2 + "}");
					}
				}
			}
			request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, bestMatch.getHandlerMethod());
			handleMatch(bestMatch.mapping, lookupPath, request);
			return bestMatch.getHandlerMethod();
		}
		else {
			return handleNoMatch(this.mappingRegistry.getRegistrations().keySet(), lookupPath, request);
		}
	}

	private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
		for (T mapping : mappings) {
			T match = getMatchingMapping(mapping, request);
			if (match != null) {
				matches.add(new Match(match, this.mappingRegistry.getRegistrations().get(mapping)));
			}
		}
	}

	/**
	 * Invoked when a matching mapping is found.
	 * @param mapping the matching mapping
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 */
	protected void handleMatch(T mapping, String lookupPath, HttpServletRequest request) {
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, lookupPath);
	}

	/**
	 * Invoked when no matching mapping is not found.
	 * @param mappings all registered mappings
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 * @throws ServletException in case of errors
	 */
	@Nullable
	protected HandlerMethod handleNoMatch(Set<T> mappings, String lookupPath, HttpServletRequest request)
			throws Exception {

		return null;
	}

	@Override
	protected boolean hasCorsConfigurationSource(Object handler) {
		return super.hasCorsConfigurationSource(handler) ||
				(handler instanceof HandlerMethod handlerMethod &&
						this.mappingRegistry.getCorsConfiguration(handlerMethod) != null);
	}

	@Override
	@Nullable
	protected CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
		CorsConfiguration corsConfig = super.getCorsConfiguration(handler, request);
		if (handler instanceof HandlerMethod handlerMethod) {
			if (handlerMethod.equals(PREFLIGHT_AMBIGUOUS_MATCH)) {
				return AbstractHandlerMethodMapping.ALLOW_CORS_CONFIG;
			}
			else {
				CorsConfiguration corsConfigFromMethod = this.mappingRegistry.getCorsConfiguration(handlerMethod);
				corsConfig = (corsConfig != null ? corsConfig.combine(corsConfigFromMethod) : corsConfigFromMethod);
			}
		}
		return corsConfig;
	}


	// Abstract template methods

	/**
	 * Whether the given type is a handler with handler methods.
	 * @param beanType the type of the bean being checked
	 * @return "true" if this a handler type, "false" otherwise.
	 */
	// 指定类型是否为具有处理程序方法的处理程序。
	// @param beanType 被检查的 bean 的类型
	// @return 如果是处理程序类型，则返回 "true"；否则返回 "false"。
	protected abstract boolean isHandler(Class<?> beanType);

	/**
	 * Provide the mapping for a handler method. A method for which no
	 * mapping can be provided is not a handler method.
	 * @param method the method to provide a mapping for
	 * @param handlerType the handler type, possibly a subtype of the method's
	 * declaring class
	 * @return the mapping, or {@code null} if the method is not mapped
	 */
	// 提供 handler 方法的映射。无法提供映射的方法不是处理程序方法。
	// @param method 提供映射的方法
	// @param handlerType 处理程序类型，可能是方法声明类的子类型
	// @return 映射，如果方法未映射，则返回 {@code null}
	@Nullable
	protected abstract T getMappingForMethod(Method method, Class<?> handlerType);

	/**
	 * Extract and return the URL paths contained in the supplied mapping.
	 * @deprecated as of 5.3 in favor of providing non-pattern mappings via
	 * {@link #getDirectPaths(Object)} instead
	 */
	// 提取并返回所提供映射中包含的 URL 路径。
	// @deprecated 自 5.3 起，改为通过 {@link #getDirectPaths(Object)} 提供非模式映射
	@Deprecated
	protected Set<String> getMappingPathPatterns(T mapping) {
		return Collections.emptySet();
	}

	/**
	 * Return the request mapping paths that are not patterns.
	 * @since 5.3
	 */
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

	/**
	 * Check if a mapping matches the current request and return a (potentially
	 * new) mapping with conditions relevant to the current request.
	 * @param mapping the mapping to get a match for
	 * @param request the current HTTP servlet request
	 * @return the match, or {@code null} if the mapping doesn't match
	 */
	@Nullable
	protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);

	/**
	 * Return a comparator for sorting matching mappings.
	 * The returned comparator should sort 'better' matches higher.
	 * @param request the current request
	 * @return the comparator (never {@code null})
	 */
	protected abstract Comparator<T> getMappingComparator(HttpServletRequest request);


	/**
	 * A registry that maintains all mappings to handler methods, exposing methods
	 * to perform lookups and providing concurrent access.
	 *
	 * <p>Package-private for testing purposes.
	 */
	// 一个注册表，用于维护所有到处理程序方法的映射，公开执行查找的方法并提供并发访问。
	//
	// <p>用于测试目的的包私有。
	class MappingRegistry {

		private final Map<T, MappingRegistration<T>> registry = new HashMap<>();

		private final MultiValueMap<String, T> pathLookup = new LinkedMultiValueMap<>();

		private final Map<String, List<HandlerMethod>> nameLookup = new ConcurrentHashMap<>();

		private final Map<HandlerMethod, CorsConfiguration> corsLookup = new ConcurrentHashMap<>();

		private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

		/**
		 * Return all registrations.
		 * @since 5.3
		 */
		public Map<T, MappingRegistration<T>> getRegistrations() {
			return this.registry;
		}

		/**
		 * Return matches for the given URL path. Not thread-safe.
		 * @see #acquireReadLock()
		 */
		@Nullable
		public List<T> getMappingsByDirectPath(String urlPath) {
			return this.pathLookup.get(urlPath);
		}

		/**
		 * Return handler methods by mapping name. Thread-safe for concurrent use.
		 */
		@Nullable
		public List<HandlerMethod> getHandlerMethodsByMappingName(String mappingName) {
			return this.nameLookup.get(mappingName);
		}

		/**
		 * Return CORS configuration. Thread-safe for concurrent use.
		 */
		@Nullable
		public CorsConfiguration getCorsConfiguration(HandlerMethod handlerMethod) {
			HandlerMethod original = handlerMethod.getResolvedFromHandlerMethod();
			return this.corsLookup.get(original != null ? original : handlerMethod);
		}

		/**
		 * Acquire the read lock when using getMappings and getMappingsByUrl.
		 */
		// 使用 getMappings 和 getMappingsByUrl 时获取读锁。
		public void acquireReadLock() {
			this.readWriteLock.readLock().lock();
		}

		/**
		 * Release the read lock after using getMappings and getMappingsByUrl.
		 */
		public void releaseReadLock() {
			this.readWriteLock.readLock().unlock();
		}

		public void register(T mapping, Object handler, Method method) {
			this.readWriteLock.writeLock().lock();
			try {
				// 1. 创建 HandlerMethod 实例。
				HandlerMethod handlerMethod = createHandlerMethod(handler, method);
				validateMethodMapping(handlerMethod, mapping);

				// Enable method validation, if applicable --> 译文：启用方法验证（如果适用）
				handlerMethod = handlerMethod.createWithValidateFlags();

				// 2. 返回非模式的请求映射路径。并注册到 pathLookup
				Set<String> directPaths = AbstractHandlerMethodMapping.this.getDirectPaths(mapping);
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

		private void validateMethodMapping(HandlerMethod handlerMethod, T mapping) {
			MappingRegistration<T> registration = this.registry.get(mapping);
			HandlerMethod existingHandlerMethod = (registration != null ? registration.getHandlerMethod() : null);
			if (existingHandlerMethod != null && !existingHandlerMethod.equals(handlerMethod)) {
				throw new IllegalStateException(
						"Ambiguous mapping. Cannot map '" + handlerMethod.getBean() + "' method \n" +
						handlerMethod + "\nto " + mapping + ": There is already '" +
						existingHandlerMethod.getBean() + "' bean method\n" + existingHandlerMethod + " mapped.");
			}
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

		public void unregister(T mapping) {
			this.readWriteLock.writeLock().lock();
			try {
				MappingRegistration<T> registration = this.registry.remove(mapping);
				if (registration == null) {
					return;
				}

				for (String path : registration.getDirectPaths()) {
					List<T> mappings = this.pathLookup.get(path);
					if (mappings != null) {
						mappings.remove(registration.getMapping());
						if (mappings.isEmpty()) {
							this.pathLookup.remove(path);
						}
					}
				}

				removeMappingName(registration);

				this.corsLookup.remove(registration.getHandlerMethod());
			}
			finally {
				this.readWriteLock.writeLock().unlock();
			}
		}

		private void removeMappingName(MappingRegistration<T> definition) {
			String name = definition.getMappingName();
			if (name == null) {
				return;
			}
			HandlerMethod handlerMethod = definition.getHandlerMethod();
			List<HandlerMethod> oldList = this.nameLookup.get(name);
			if (oldList == null) {
				return;
			}
			if (oldList.size() <= 1) {
				this.nameLookup.remove(name);
				return;
			}
			List<HandlerMethod> newList = new ArrayList<>(oldList.size() - 1);
			for (HandlerMethod current : oldList) {
				if (!current.equals(handlerMethod)) {
					newList.add(current);
				}
			}
			this.nameLookup.put(name, newList);
		}
	}


	static class MappingRegistration<T> {

		private final T mapping;

		private final HandlerMethod handlerMethod;

		private final Set<String> directPaths;

		@Nullable
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


	/**
	 * A thin wrapper around a matched HandlerMethod and its mapping, for the purpose of
	 * comparing the best match with a comparator in the context of the current request.
	 */
	private class Match {

		private final T mapping;

		private final MappingRegistration<T> registration;

		public Match(T mapping, MappingRegistration<T> registration) {
			this.mapping = mapping;
			this.registration = registration;
		}

		public HandlerMethod getHandlerMethod() {
			return this.registration.getHandlerMethod();
		}

		public boolean hasCorsConfig() {
			return this.registration.hasCorsConfig();
		}

		@Override
		public String toString() {
			return this.mapping.toString();
		}
	}


	private class MatchComparator implements Comparator<Match> {

		private final Comparator<T> comparator;

		public MatchComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(Match match1, Match match2) {
			return this.comparator.compare(match1.mapping, match2.mapping);
		}
	}


	private static class EmptyHandler {

		@SuppressWarnings("unused")
		public void handle() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

}
