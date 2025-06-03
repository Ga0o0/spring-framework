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

package org.springframework.web.servlet.mvc.method.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.annotation.RepeatableContainers;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Creates {@link RequestMappingInfo} instances from type-level and method-level
 * {@link RequestMapping @RequestMapping} and {@link HttpExchange @HttpExchange}
 * annotations in {@link Controller @Controller} classes.
 *
 * <p><strong>Deprecation Note:</strong></p> In 5.2.4,
 * {@link #setUseSuffixPatternMatch(boolean) useSuffixPatternMatch} and
 * {@link #setUseRegisteredSuffixPatternMatch(boolean) useRegisteredSuffixPatternMatch}
 * were deprecated in order to discourage use of path extensions for request
 * mapping and for content negotiation (with similar deprecations in
 * {@link org.springframework.web.accept.ContentNegotiationManagerFactoryBean
 * ContentNegotiationManagerFactoryBean}). For further context, please read issue
 * <a href="https://github.com/spring-projects/spring-framework/issues/24179">#24179</a>.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @author Olga Maciaszek-Sharma
 * @since 3.1
 */
// 从 {@link Controller @Controller} 类中的类型级和方法级 {@link RequestMapping @RequestMapping}
// 和 {@link HttpExchange @HttpExchange} 注释创建 {@link RequestMappingInfo} 实例。
//
// <p><strong>弃用说明：</strong></p>在 5.2.4 中，{@link #setUseSuffixPatternMatch(boolean) useSuffixPatternMatch} 和
// {@link #setUseRegisteredSuffixPatternMatch(boolean) useRegisteredSuffixPatternMatch} 已被弃用，以阻止使用路径扩展进行请求映射和内容协商
// （在 {@link org.springframework.web.accept.ContentNegotiationManagerFactoryBean ContentNegotiationManagerFactoryBean} 中也有类似的弃用）。有关更多背景信息，请阅读问题 <a href="https://github.com/spring-projects/spring-framework/issues/24179">#24179</a>。
public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
		implements MatchableHandlerMapping, EmbeddedValueResolverAware {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private static final RequestMethod[] EMPTY_REQUEST_METHOD_ARRAY = new RequestMethod[0];


	private boolean defaultPatternParser = true;

	private boolean useSuffixPatternMatch = false;

	private boolean useRegisteredSuffixPatternMatch = false;

	private boolean useTrailingSlashMatch = false;

	private Map<String, Predicate<Class<?>>> pathPrefixes = Collections.emptyMap();

	private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

	@Nullable
	private StringValueResolver embeddedValueResolver;

	private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();


	@Override
	public void setPatternParser(@Nullable PathPatternParser patternParser) {
		if (patternParser != null) {
			this.defaultPatternParser = false;
		}
		super.setPatternParser(patternParser);
	}

	/**
	 * Whether to use suffix pattern match (".*") when matching patterns to
	 * requests. If enabled a method mapped to "/users" also matches to "/users.*".
	 * <p>By default value this is set to {@code false}.
	 * <p>Also see {@link #setUseRegisteredSuffixPatternMatch(boolean)} for
	 * more fine-grained control over specific suffixes to allow.
	 * <p><strong>Note:</strong> This property is ignored when
	 * {@link #setPatternParser(PathPatternParser)} is configured.
	 * @deprecated as of 5.2.4. See class level note on the deprecation of
	 * path extension config options. As there is no replacement for this method,
	 * in 5.2.x it is necessary to set it to {@code false}. In 5.3 the default
	 * changes to {@code false} and use of this property becomes unnecessary.
	 */
	@Deprecated
	public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
		this.useSuffixPatternMatch = useSuffixPatternMatch;
	}

	/**
	 * Whether suffix pattern matching should work only against path extensions
	 * explicitly registered with the {@link ContentNegotiationManager}. This
	 * is generally recommended to reduce ambiguity and to avoid issues such as
	 * when a "." appears in the path for other reasons.
	 * <p>By default this is set to "false".
	 * <p><strong>Note:</strong> This property is ignored when
	 * {@link #setPatternParser(PathPatternParser)} is configured.
	 * @deprecated as of 5.2.4. See class level note on the deprecation of
	 * path extension config options.
	 */
	@Deprecated
	public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch) {
		this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
		this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
	}

	/**
	 * Whether to match to URLs irrespective of the presence of a trailing slash.
	 * If enabled a method mapped to "/users" also matches to "/users/".
	 * <p>The default was changed in 6.0 from {@code true} to {@code false} in
	 * order to support the deprecation of the property.
	 * @deprecated as of 6.0, see
	 * {@link PathPatternParser#setMatchOptionalTrailingSeparator(boolean)}
	 */
	@Deprecated(since = "6.0")
	public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
		this.useTrailingSlashMatch = useTrailingSlashMatch;
		if (getPatternParser() != null) {
			getPatternParser().setMatchOptionalTrailingSeparator(useTrailingSlashMatch);
		}
	}

	/**
	 * Configure path prefixes to apply to controller methods.
	 * <p>Prefixes are used to enrich the mappings of every {@code @RequestMapping}
	 * method and {@code @HttpExchange} method whose controller type is matched
	 * by a corresponding {@code Predicate} in the map. The prefix for the first
	 * matching predicate is used, assuming the input map has predictable order.
	 * <p>Consider using {@link org.springframework.web.method.HandlerTypePredicate
	 * HandlerTypePredicate} to group controllers.
	 * @param prefixes a map with path prefixes as key
	 * @since 5.1
	 */
	public void setPathPrefixes(Map<String, Predicate<Class<?>>> prefixes) {
		this.pathPrefixes = (!prefixes.isEmpty() ?
				Collections.unmodifiableMap(new LinkedHashMap<>(prefixes)) :
				Collections.emptyMap());
	}

	/**
	 * The configured path prefixes as a read-only, possibly empty map.
	 * @since 5.1
	 */
	public Map<String, Predicate<Class<?>>> getPathPrefixes() {
		return this.pathPrefixes;
	}

	/**
	 * Set the {@link ContentNegotiationManager} to use to determine requested media types.
	 * If not set, the default constructor is used.
	 */
	public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
		Assert.notNull(contentNegotiationManager, "ContentNegotiationManager must not be null");
		this.contentNegotiationManager = contentNegotiationManager;
	}

	/**
	 * Return the configured {@link ContentNegotiationManager}.
	 */
	// 返回已配置的 {@link ContentNegotiation Manager}。
	public ContentNegotiationManager getContentNegotiationManager() {
		return this.contentNegotiationManager;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void afterPropertiesSet() {
		this.config = new RequestMappingInfo.BuilderConfiguration(); // 用于请求映射的配置选项容器。
		this.config.setTrailingSlashMatch(useTrailingSlashMatch());	 // 设置是否在 PatternsRequestCondition 中应用尾部斜杠匹配。
		// 设置用于 ProducesRequestCondition 的 ContentNegotiationManager。
		this.config.setContentNegotiationManager(getContentNegotiationManager());

		if (getPatternParser() != null && this.defaultPatternParser &&
				(this.useSuffixPatternMatch || this.useRegisteredSuffixPatternMatch)) { // skip

			setPatternParser(null);
		}

		if (getPatternParser() != null) {
			this.config.setPatternParser(getPatternParser()); // 启用已解析的 {@link PathPattern}
			// PathPatternParser 不支持后缀模式匹配。
			Assert.isTrue(!this.useSuffixPatternMatch && !this.useRegisteredSuffixPatternMatch,
					"Suffix pattern matching not supported with PathPatternParser.");
		}
		else { // skip
			this.config.setSuffixPatternMatch(useSuffixPatternMatch());
			this.config.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
			this.config.setPathMatcher(getPathMatcher());
		}

		super.afterPropertiesSet(); // 在初始化时检测处理程序方法。
	}


	/**
	 * Whether to use registered suffixes for pattern matching.
	 * @deprecated as of 5.2.4. See deprecation notice on
	 * {@link #setUseSuffixPatternMatch(boolean)}.
	 */
	@Deprecated
	public boolean useSuffixPatternMatch() {
		return this.useSuffixPatternMatch;
	}

	/**
	 * Whether to use registered suffixes for pattern matching.
	 * @deprecated as of 5.2.4. See deprecation notice on
	 * {@link #setUseRegisteredSuffixPatternMatch(boolean)}.
	 */
	@Deprecated
	public boolean useRegisteredSuffixPatternMatch() {
		return this.useRegisteredSuffixPatternMatch;
	}

	/**
	 * Whether to match to URLs irrespective of the presence of a trailing slash.
	 */
	// 是否匹配 URL，无论是否存在尾部斜杠。
	public boolean useTrailingSlashMatch() {
		return this.useTrailingSlashMatch;
	}

	/**
	 * Return the file extensions to use for suffix pattern matching.
	 * @deprecated as of 5.2.4. See class-level note on the deprecation of path
	 * extension config options.
	 */
	@Nullable
	@Deprecated
	@SuppressWarnings("deprecation")
	public List<String> getFileExtensions() {
		return this.config.getFileExtensions();
	}

	/**
	 * Obtain a {@link RequestMappingInfo.BuilderConfiguration} that reflects
	 * the internal configuration of this {@code HandlerMapping} and can be used
	 * to set {@link RequestMappingInfo.Builder#options(RequestMappingInfo.BuilderConfiguration)}.
	 * <p>This is useful for programmatic registration of request mappings via
	 * {@link #registerHandlerMethod(Object, Method, RequestMappingInfo)}.
	 * @return the builder configuration that reflects the internal state
	 * @since 5.3.14
	 */
	public RequestMappingInfo.BuilderConfiguration getBuilderConfiguration() {
		return this.config;
	}


	/**
	 * {@inheritDoc}
	 * <p>Expects a handler to have a type-level @{@link Controller} annotation.
	 */
	// <p>期望处理程序具有类型级别的 @{@link Controller} 注释。
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return AnnotatedElementUtils.hasAnnotation(beanType, Controller.class);
	}

	/**
	 * Uses type-level and method-level {@link RequestMapping @RequestMapping}
	 * and {@link HttpExchange @HttpExchange} annotations to create the
	 * {@link RequestMappingInfo}.
	 * @return the created {@code RequestMappingInfo}, or {@code null} if the method
	 * does not have a {@code @RequestMapping} or {@code @HttpExchange} annotation
	 * @see #getCustomMethodCondition(Method)
	 * @see #getCustomTypeCondition(Class)
	 */
	// 使用类型级别和方法级别的 {@link RequestMapping @RequestMapping} 和
	// {@link HttpExchange @HttpExchange} 注解来创建 {@link RequestMappingInfo}。
	// @return 创建的 {@code RequestMappingInfo}，如果该方法没有 {@code @RequestMapping}
	// 或 {@code @HttpExchange} 注解，则返回 {@code null}
	@Override
	@Nullable
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		// 根据 @HttpExchange 和 @RequestMapping 创建的 RequestMappingInfo
		RequestMappingInfo info = createRequestMappingInfo(method); // 方法 请求映射信息
		if (info != null) {
			// 类型 请求映射信息
			RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				// 将 typeInfo 请求映射信息（即当前实例）与 info 请求映射信息实例合并。
				info = typeInfo.combine(info);
			}
			if (info.isEmptyMapping()) {
				// 返回一个构建器，通过修改此构建器来创建新的 RequestMappingInfo。
				info = info.mutate().paths("", "/").options(this.config).build();
			}
			String prefix = getPathPrefix(handlerType);
			if (prefix != null) {
				// 使用给定的路径创建一个新的 {@code RequestMappingInfo.Builder}。
				info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
			}
		}
		return info;
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

	@Nullable
	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		RequestMappingInfo requestMappingInfo = null;
		// getCustomTypeCondition(clazz) -> 提供自定义类型级请求条件。
		// getCustomMethodCondition((Method) element) -> 提供自定义方法级请求条件。
		RequestCondition<?> customCondition = (element instanceof Class<?> clazz ?
				getCustomTypeCondition(clazz) : getCustomMethodCondition((Method) element));

		// 1. 创建一个新的 MergedAnnotations 实例，其中包含来自指定元素的所有注释和元注释，并且取决于 SearchStrategy，相关的继承元素。
		// 然后 查询 @RequestMapping 和 @HttpExchange 注解，并封装成 AnnotationDescriptor 返回
		List<AnnotationDescriptor> descriptors = getAnnotationDescriptors(element);

		// 2. 处理 @RequestMapping 注解相关的 AnnotationDescriptor
		List<AnnotationDescriptor> requestMappings = descriptors.stream()
				.filter(desc -> desc.annotation instanceof RequestMapping).toList();
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
				.filter(desc -> desc.annotation instanceof HttpExchange).toList();
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

	/**
	 * Provide a custom type-level request condition.
	 * The custom {@link RequestCondition} can be of any type so long as the
	 * same condition type is returned from all calls to this method in order
	 * to ensure custom request conditions can be combined and compared.
	 * <p>Consider extending {@link AbstractRequestCondition} for custom
	 * condition types and using {@link CompositeRequestCondition} to provide
	 * multiple custom conditions.
	 * @param handlerType the handler type for which to create the condition
	 * @return the condition, or {@code null}
	 */
	// 提供自定义类型级请求条件。
	// 自定义 {@link RequestCondition} 可以是任何类型，只要所有调用此方法返回的条件类型相同即可，以确保自定义请求条件可以组合和比较。
	// <p>考虑扩展 {@link AbstractRequestCondition} 以自定义条件类型，并使用 {@link CompositeRequestCondition} 提供多个自定义条件。
	// @param handlerType 指定要为其创建条件的处理程序类型；
	// @return 指定条件，或 {@code null}
	@Nullable
	protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
		return null;
	}

	/**
	 * Provide a custom method-level request condition.
	 * The custom {@link RequestCondition} can be of any type so long as the
	 * same condition type is returned from all calls to this method in order
	 * to ensure custom request conditions can be combined and compared.
	 * <p>Consider extending {@link AbstractRequestCondition} for custom
	 * condition types and using {@link CompositeRequestCondition} to provide
	 * multiple custom conditions.
	 * @param method the handler method for which to create the condition
	 * @return the condition, or {@code null}
	 */
	// 提供自定义方法级请求条件。
	// 自定义 {@link RequestCondition} 可以是任何类型，只要所有调用此方法都返回相同的条件类型即可，以确保自定义请求条件可以组合和比较。
	// <p>考虑扩展 {@link AbstractRequestCondition} 以支持自定义条件类型，
	// 并使用 {@link CompositeRequestCondition} 提供多个自定义条件。
	// @param method 为其创建条件的处理程序方法
	// @return 条件，或 {@code null}
	@Nullable
	protected RequestCondition<?> getCustomMethodCondition(Method method) {
		return null;
	}

	/**
	 * Create a {@link RequestMappingInfo} from the supplied
	 * {@link RequestMapping @RequestMapping} annotation, meta-annotation,
	 * or synthesized result of merging annotation attributes within an
	 * annotation hierarchy.
	 */
	// 从提供的 {@link RequestMapping @RequestMapping} 注释、元注释或在注释层次结构中合并注释属性的合成结果创建 {@link RequestMappingInfo}。
	protected RequestMappingInfo createRequestMappingInfo(
			RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

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

	/**
	 * Create a {@link RequestMappingInfo} from the supplied
	 * {@link HttpExchange @HttpExchange} annotation, meta-annotation,
	 * or synthesized result of merging annotation attributes within an
	 * annotation hierarchy.
	 * @since 6.1
	 */
	protected RequestMappingInfo createRequestMappingInfo(
			HttpExchange httpExchange, @Nullable RequestCondition<?> customCondition) {

		RequestMappingInfo.Builder builder = RequestMappingInfo
				.paths(resolveEmbeddedValuesInPatterns(toStringArray(httpExchange.value())))
				.methods(toMethodArray(httpExchange.method()))
				.consumes(toStringArray(httpExchange.contentType()))
				.produces(httpExchange.accept());

		if (customCondition != null) {
			builder.customCondition(customCondition);
		}

		return builder.options(this.config).build();
	}

	/**
	 * Resolve placeholder values in the given array of patterns.
	 * @return a new array with updated patterns
	 */
	// 解析给定模式数组中的占位符值。
	// @return 一个包含更新模式的新数组
	protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
		if (this.embeddedValueResolver == null) {
			return patterns;
		}
		else {
			String[] resolvedPatterns = new String[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
			}
			return resolvedPatterns;
		}
	}

	private static String[] toStringArray(String value) {
		return (StringUtils.hasText(value) ? new String[] {value} : EMPTY_STRING_ARRAY);
	}

	private static RequestMethod[] toMethodArray(String method) {
		return (StringUtils.hasText(method) ?
				new RequestMethod[] {RequestMethod.valueOf(method)} : EMPTY_REQUEST_METHOD_ARRAY);
	}

	@Override
	public void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
		super.registerMapping(mapping, handler, method);
		updateConsumesCondition(mapping, method);
	}

	/**
	 * {@inheritDoc}
	 * <p><strong>Note:</strong> To create the {@link RequestMappingInfo},
	 * please use {@link #getBuilderConfiguration()} and set the options on
	 * {@link RequestMappingInfo.Builder#options(RequestMappingInfo.BuilderConfiguration)}
	 * to match how this {@code HandlerMapping} is configured. This
	 * is important for example to ensure use of
	 * {@link org.springframework.web.util.pattern.PathPattern} or
	 * {@link org.springframework.util.PathMatcher} based matching.
	 * @param handler the bean name of the handler or the handler instance
	 * @param method the method to register
	 * @param mapping the mapping conditions associated with the handler method
	 */
	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, mapping);
		updateConsumesCondition(mapping, method);
	}

	private void updateConsumesCondition(RequestMappingInfo info, Method method) {
		ConsumesRequestCondition condition = info.getConsumesCondition();
		if (!condition.isEmpty()) {
			for (Parameter parameter : method.getParameters()) {
				MergedAnnotation<RequestBody> annot = MergedAnnotations.from(parameter).get(RequestBody.class);
				if (annot.isPresent()) {
					condition.setBodyRequired(annot.getBoolean("required"));
					break;
				}
			}
		}
	}

	@Override
	@Nullable
	public RequestMatchResult match(HttpServletRequest request, String pattern) {
		Assert.state(getPatternParser() == null, "This HandlerMapping uses PathPatterns.");
		RequestMappingInfo info = RequestMappingInfo.paths(pattern).options(this.config).build();
		RequestMappingInfo match = info.getMatchingCondition(request);
		return (match != null && match.getPatternsCondition() != null ?
				new RequestMatchResult(
						match.getPatternsCondition().getPatterns().iterator().next(),
						UrlPathHelper.getResolvedLookupPath(request),
						getPathMatcher()) : null);
	}

	@Override
	@Nullable
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
		// 1. 提取类型/方法上的 @CrossOrigin
		HandlerMethod handlerMethod = createHandlerMethod(handler, method); // 创建 HandlerMethod 实例。
		Class<?> beanType = handlerMethod.getBeanType(); // 此方法返回此处理程序方法的处理程序的类型。
		// 类型上的 @CrossOrigin
		CrossOrigin typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanType, CrossOrigin.class);
		// 方法上的 @CrossOrigin
		CrossOrigin methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, CrossOrigin.class);

		if (typeAnnotation == null && methodAnnotation == null) {
			return null;
		}

		// 2. 合并类型/方法上的 @CrossOrigin 配置到 CorsConfiguration
		CorsConfiguration config = new CorsConfiguration();
		updateCorsConfig(config, typeAnnotation);
		updateCorsConfig(config, methodAnnotation);

		if (CollectionUtils.isEmpty(config.getAllowedMethods())) {
			for (RequestMethod allowedMethod : mappingInfo.getMethodsCondition().getMethods()) {
				config.addAllowedMethod(allowedMethod.name());
			}
		}
		// 应用允许默认值
		return config.applyPermitDefaultValues();
	}

	private void updateCorsConfig(CorsConfiguration config, @Nullable CrossOrigin annotation) {
		if (annotation == null) {
			return;
		}
		for (String origin : annotation.origins()) {
			config.addAllowedOrigin(resolveCorsAnnotationValue(origin));
		}
		for (String patterns : annotation.originPatterns()) {
			config.addAllowedOriginPattern(resolveCorsAnnotationValue(patterns));
		}
		for (RequestMethod method : annotation.methods()) {
			config.addAllowedMethod(method.name());
		}
		for (String header : annotation.allowedHeaders()) {
			config.addAllowedHeader(resolveCorsAnnotationValue(header));
		}
		for (String header : annotation.exposedHeaders()) {
			config.addExposedHeader(resolveCorsAnnotationValue(header));
		}

		String allowCredentials = resolveCorsAnnotationValue(annotation.allowCredentials());
		if ("true".equalsIgnoreCase(allowCredentials)) {
			config.setAllowCredentials(true);
		}
		else if ("false".equalsIgnoreCase(allowCredentials)) {
			config.setAllowCredentials(false);
		}
		else if (!allowCredentials.isEmpty()) {
			throw new IllegalStateException("@CrossOrigin's allowCredentials value must be \"true\", \"false\", " +
					"or an empty string (\"\"): current value is [" + allowCredentials + "]");
		}

		String allowPrivateNetwork = resolveCorsAnnotationValue(annotation.allowPrivateNetwork());
		if ("true".equalsIgnoreCase(allowPrivateNetwork)) {
			config.setAllowPrivateNetwork(true);
		}
		else if ("false".equalsIgnoreCase(allowPrivateNetwork)) {
			config.setAllowPrivateNetwork(false);
		}
		else if (!allowPrivateNetwork.isEmpty()) {
			throw new IllegalStateException("@CrossOrigin's allowPrivateNetwork value must be \"true\", \"false\", " +
					"or an empty string (\"\"): current value is [" + allowPrivateNetwork + "]");
		}

		if (annotation.maxAge() >= 0 ) {
			config.setMaxAge(annotation.maxAge());
		}
	}

	private String resolveCorsAnnotationValue(String value) {
		if (this.embeddedValueResolver != null) {
			String resolved = this.embeddedValueResolver.resolveStringValue(value);
			return (resolved != null ? resolved : "");
		}
		else {
			return value;
		}
	}

	private static List<AnnotationDescriptor> getAnnotationDescriptors(AnnotatedElement element) {
		// 创建一个新的 {@link MergedAnnotations} 实例，其中包含来自指定元素的所有注释和元注释，并且取决于 {@link SearchStrategy}，相关的继承元素。
		return MergedAnnotations.from(element, SearchStrategy.TYPE_HIERARCHY, RepeatableContainers.none())
				.stream()
				// 创建一个新的 {@link Predicate}，如果指定数组中包含 {@linkplain MergedAnnotation#getType() 合并的注解类型}，则结果为 {@code true}。
				.filter(MergedAnnotationPredicates.typeIn(RequestMapping.class, HttpExchange.class))
				// 创建一个新的有状态的、一次性使用的 {@link Predicate}，该谓词仅匹配提取值的第一个运行。
				.filter(MergedAnnotationPredicates.firstRunOf(MergedAnnotation::getAggregateIndex)) // 获取包含此注解的聚合集合的索引。
				.map(AnnotationDescriptor::new)
				.distinct()
				.toList();
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
