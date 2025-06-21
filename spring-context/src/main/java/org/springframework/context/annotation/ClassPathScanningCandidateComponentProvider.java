/*
 * Copyright 2002-2025 the original author or authors.
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

package org.springframework.context.annotation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.SpringProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.ClassFormatException;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A component provider that scans for candidate components starting from a
 * specified base package. Can use the {@linkplain CandidateComponentsIndex component
 * index}, if it is available, and scans the classpath otherwise.
 *
 * <p>Candidate components are identified by applying exclude and include filters.
 * {@link AnnotationTypeFilter} and {@link AssignableTypeFilter} include filters
 * for an annotation/target-type that is annotated with {@link Indexed} are
 * supported: if any other include filter is specified, the index is ignored and
 * classpath scanning is used instead.
 *
 * <p>This implementation is based on Spring's
 * {@link org.springframework.core.type.classreading.MetadataReader MetadataReader}
 * facility, backed by an ASM {@link org.springframework.asm.ClassReader ClassReader}.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see org.springframework.core.type.AnnotationMetadata
 * @see ScannedGenericBeanDefinition
 * @see CandidateComponentsIndex
 */
// 组件提供程序从指定的基础包开始扫描候选组件。
// 如果可用，可以使用 {@linkplain CandidateComponentsIndex 组件索引}，否则扫描类路径。
//
// <p>候选组件通过应用排除和包含过滤器来识别。
// 支持针对使用 {@link Indexed} 注解的注解/目标类型的 {@link AnnotationTypeFilter} 和
// {@link AssignableTypeFilter} 包含过滤器：如果指定了任何其他包含过滤器，则忽略索引，改用类路径扫描。
//
// <p>此实现基于 Spring 的 {@link org.springframework.core.type.classreading.MetadataReader MetadataReader} 工具，
// 由 ASM {@link org.springframework.asm.ClassReader ClassReader} 支持。
@SuppressWarnings("removal") // components index
public class ClassPathScanningCandidateComponentProvider implements EnvironmentCapable, ResourceLoaderAware {

	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

	/**
	 * System property that instructs Spring to ignore class format exceptions during
	 * classpath scanning, in particular for unsupported class file versions.
	 * By default, such a class format mismatch leads to a classpath scanning failure.
	 * @since 6.1.2
	 * @see ClassFormatException
	 */
	public static final String IGNORE_CLASSFORMAT_PROPERTY_NAME = "spring.classformat.ignore";

	private static final boolean shouldIgnoreClassFormatException =
			SpringProperties.getFlag(IGNORE_CLASSFORMAT_PROPERTY_NAME);


	protected final Log logger = LogFactory.getLog(getClass());

	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	private final List<TypeFilter> includeFilters = new ArrayList<>();

	private final List<TypeFilter> excludeFilters = new ArrayList<>();

	@Nullable
	private Environment environment;

	@Nullable
	private ConditionEvaluator conditionEvaluator;

	@Nullable
	private ResourcePatternResolver resourcePatternResolver;

	@Nullable
	private MetadataReaderFactory metadataReaderFactory;

	@Nullable
	private CandidateComponentsIndex componentsIndex;


	/**
	 * Protected constructor for flexible subclass initialization.
	 * @since 4.3.6
	 */
	protected ClassPathScanningCandidateComponentProvider() {
	}

	/**
	 * Create a ClassPathScanningCandidateComponentProvider with a {@link StandardEnvironment}.
	 * @param useDefaultFilters whether to register the default filters for the
	 * {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
		this(useDefaultFilters, new StandardEnvironment());
	}

	/**
	 * Create a ClassPathScanningCandidateComponentProvider with the given {@link Environment}.
	 * @param useDefaultFilters whether to register the default filters for the
	 * {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * @param environment the Environment to use
	 * @see #registerDefaultFilters()
	 */
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment) {
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		setEnvironment(environment);
		setResourceLoader(null);
	}


	/**
	 * Set the resource pattern to use when scanning the classpath.
	 * This value will be appended to each base package name.
	 * @see #findCandidateComponents(String)
	 * @see #DEFAULT_RESOURCE_PATTERN
	 */
	// 设置扫描类路径时使用的资源模式。此值将附加到每个基础包名称。
	public void setResourcePattern(String resourcePattern) {
		Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
		this.resourcePattern = resourcePattern;
	}

	/**
	 * Add an include type filter to the <i>end</i> of the inclusion list.
	 */
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	/**
	 * Add an exclude type filter to the <i>front</i> of the exclusion list.
	 */
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	/**
	 * Reset the configured type filters.
	 * @param useDefaultFilters whether to re-register the default filters for
	 * the {@link Component @Component}, {@link Repository @Repository},
	 * {@link Service @Service}, and {@link Controller @Controller}
	 * stereotype annotations
	 * @see #registerDefaultFilters()
	 */
	public void resetFilters(boolean useDefaultFilters) {
		this.includeFilters.clear();
		this.excludeFilters.clear();
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
	}

	/**
	 * Register the default filter for {@link Component @Component}.
	 * <p>This will implicitly register all annotations that have the
	 * {@link Component @Component} meta-annotation including the
	 * {@link Repository @Repository}, {@link Service @Service}, and
	 * {@link Controller @Controller} stereotype annotations.
	 * <p>Also supports Jakarta EE's {@link jakarta.annotation.ManagedBean} and
	 * JSR-330's {@link jakarta.inject.Named} annotations (as well as their
	 * pre-Jakarta {@code javax.annotation.ManagedBean} and {@code javax.inject.Named}
	 * equivalents), if available.
	 */
	// 为 {@link Component @Component} 注册默认过滤器。
	// <p>这将隐式注册所有带有 {@link Component @Component} 元注解的注解，
	// 包括 {@link Repository @Repository}、{@link Service @Service} 和 {@link Controller @Controller} 构造型注解。
	// <p>还支持 Jakarta EE 的 {@link jakarta.annotation.ManagedBean}
	// 和 JSR-330 的 {@link jakarta.inject.Named} 注解
	// （以及 Jakarta 之前的 {@code javax.annotation.ManagedBean} 和 {@code javax.inject.Named} 等效注解）（如果可用）。
	@SuppressWarnings("unchecked")
	protected void registerDefaultFilters() {
		// org.springframework.stereotype.Component
		// jakarta.annotation.ManagedBean
		// javax.annotation.ManagedBean
		// jakarta.inject.Named
		// javax.inject.Named
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("jakarta.annotation.ManagedBean", cl)), false));
			logger.trace("JSR-250 'jakarta.annotation.ManagedBean' found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-250 1.1 API (as included in Jakarta EE) not available - simply skip.
		}
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)), false));
			logger.trace("JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-250 1.1 API not available - simply skip.
		}
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("jakarta.inject.Named", cl)), false));
			logger.trace("JSR-330 'jakarta.inject.Named' annotation found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API (as included in Jakarta EE) not available - simply skip.
		}
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
			logger.trace("JSR-330 'javax.inject.Named' annotation found and supported for component scanning");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}

	/**
	 * Set the Environment to use when resolving placeholders and evaluating
	 * {@link Conditional @Conditional}-annotated component classes.
	 * <p>The default is a {@link StandardEnvironment}.
	 * @param environment the Environment to use
	 */
	// 设置解析占位符和评估带有 {@link Conditional @Conditional} 注解的组件类时要使用的环境。
	// <p>默认值为 {@link StandardEnvironment}。
	// @param environment 要使用的环境
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
		this.conditionEvaluator = null;
	}

	@Override
	public final Environment getEnvironment() {
		if (this.environment == null) {
			this.environment = new StandardEnvironment();
		}
		return this.environment;
	}

	/**
	 * Return the {@link BeanDefinitionRegistry} used by this scanner, if any.
	 */
	@Nullable
	protected BeanDefinitionRegistry getRegistry() {
		return null;
	}

	/**
	 * Set the {@link ResourceLoader} to use for resource locations.
	 * This will typically be a {@link ResourcePatternResolver} implementation.
	 * <p>Default is a {@code PathMatchingResourcePatternResolver}, also capable of
	 * resource pattern resolving through the {@code ResourcePatternResolver} interface.
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	// 设置用于资源位置的 {@link ResourceLoader}。
	// 这通常是 {@link ResourcePatternResolver} 的实现。
	// <p>默认是 {@code PathMatchingResourcePatternResolver}，也能够通过 {@code ResourcePatternResolver} 接口进行资源模式解析。
	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		// 为给定的 {@link ResourceLoader} 返回一个默认的 {@link ResourcePatternResolver}。
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		// 为给定的 {@link ResourceLoader} 创建一个新的 CachingMetadataReaderFactory，如果支持则使用共享资源缓存，否则使用本地资源缓存。
		this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		// 使用指定的类加载器从 {@value #COMPONENTS_RESOURCE_LOCATION} 加载并实例化 {@link CandidateComponentsIndex}。如果没有可用的索引，则返回 {@code null}。
		this.componentsIndex = CandidateComponentsIndexLoader.loadIndex(this.resourcePatternResolver.getClassLoader());
	}

	/**
	 * Return the ResourceLoader that this component provider uses.
	 */
	public final ResourceLoader getResourceLoader() {
		return getResourcePatternResolver();
	}

	private ResourcePatternResolver getResourcePatternResolver() {
		if (this.resourcePatternResolver == null) {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
		}
		return this.resourcePatternResolver;
	}

	/**
	 * Set the {@link MetadataReaderFactory} to use.
	 * <p>Default is a {@link CachingMetadataReaderFactory} for the specified
	 * {@linkplain #setResourceLoader resource loader}.
	 * <p>Call this setter method <i>after</i> {@link #setResourceLoader} in order
	 * for the given MetadataReaderFactory to override the default factory.
	 */
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	/**
	 * Return the MetadataReaderFactory used by this component provider.
	 */
	// 返回此组件提供者使用的 MetadataReaderFactory。
	public final MetadataReaderFactory getMetadataReaderFactory() {
		if (this.metadataReaderFactory == null) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory();
		}
		return this.metadataReaderFactory;
	}


	/**
	 * Scan the component index or class path for candidate components.
	 * @param basePackage the package to check for annotated classes
	 * @return a corresponding Set of autodetected bean definitions
	 */
	// 扫描组件索引或类路径以查找候选组件。
	// @param basePackage 用于检查带注解类的包
	// @return 一组相应的自动检测的 bean 定义
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		if (this.componentsIndex != null && indexSupportsIncludeFilters()) {
			return addCandidateComponentsFromIndex(this.componentsIndex, basePackage);
		}
		else {
			return scanCandidateComponents(basePackage);
		}
	}

	/**
	 * Determine if the component index can be used by this instance.
	 * @return {@code true} if the index is available and the configuration of this
	 * instance is supported by it, {@code false} otherwise
	 * @since 5.0
	 */
	// 判断组件索引是否可被该实例使用。
	// @return {@code true} 如果索引可用且该实例的配置受其支持，{@code false} 否则
	private boolean indexSupportsIncludeFilters() {
		for (TypeFilter includeFilter : this.includeFilters) {
			if (!indexSupportsIncludeFilter(includeFilter)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if the specified include {@link TypeFilter} is supported by the index.
	 * @param filter the filter to check
	 * @return whether the index supports this include filter
	 * @since 5.0
	 * @see #extractStereotype(TypeFilter)
	 */
	// 判断索引是否支持指定的包含类型 {@link TypeFilter}。
	// @param filter 需要检查的过滤器
	// @return 索引是否支持此包含过滤器
	private boolean indexSupportsIncludeFilter(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter annotationTypeFilter) {
			Class<? extends Annotation> annotationType = annotationTypeFilter.getAnnotationType();
			// AnnotationUtils.isAnnotationDeclaredLocally() -> 确定指定的注释是否在提供的 annotationType 上本地声明（即直接存在）。
			return (AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, annotationType) ||
					annotationType.getName().startsWith("jakarta.") ||
					annotationType.getName().startsWith("javax."));
		}
		if (filter instanceof AssignableTypeFilter assignableTypeFilter) {
			Class<?> target = assignableTypeFilter.getTargetType();
			return AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, target);
		}
		return false;
	}

	/**
	 * Extract the stereotype to use for the specified compatible filter.
	 * @param filter the filter to handle
	 * @return the stereotype in the index matching this filter
	 * @since 5.0
	 * @see #indexSupportsIncludeFilter(TypeFilter)
	 */
	// 提取要用于指定兼容过滤器的构造型。
	// @param filter 要处理的过滤器
	// @return 索引中与此过滤器匹配的构造型
	@Nullable
	private String extractStereotype(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter annotationTypeFilter) {
			return annotationTypeFilter.getAnnotationType().getName();
		}
		if (filter instanceof AssignableTypeFilter assignableTypeFilter) {
			return assignableTypeFilter.getTargetType().getName();
		}
		return null;
	}

	private Set<BeanDefinition> addCandidateComponentsFromIndex(CandidateComponentsIndex index, String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			Set<String> types = new HashSet<>();
			// includeFilters = @org.springframework.stereotype.Component、
			// 					@jakarta.annotation.ManagedBean、@javax.annotation.ManagedBean、
			// 					@jakarta.inject.Named、@javax.inject.Named
			for (TypeFilter filter : this.includeFilters) {
				String stereotype = extractStereotype(filter);
				if (stereotype == null) {
					throw new IllegalArgumentException("Failed to extract stereotype from " + filter);
				}
				// index.getCandidateTypes() -> 返回与指定 stereotype 关联的候选类型。
				types.addAll(index.getCandidateTypes(basePackage, stereotype));
			}
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			for (String type : types) {
				// 例如：type = @Component 等等
				MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(type);
				// 根据 excludeFilters 和 （includeFilters + @Conditional） 判断其是否为候选组件
				if (isCandidateComponent(metadataReader)) {
					ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
					sbd.setSource(metadataReader.getResource());
					if (isCandidateComponent(sbd)) {
						if (debugEnabled) {
							logger.debug("Using candidate component class from index: " + type);
						}
						candidates.add(sbd);
					}
					else {
						if (debugEnabled) {
							logger.debug("Ignored because not a concrete top-level class: " + type);
						}
					}
				}
				else {
					if (traceEnabled) {
						logger.trace("Ignored because matching an exclude filter: " + type);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}

	private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			// basePackage = com.example
			// resolveBasePackage(basePackage) -> com/example
			// packageSearchPath = classpath*:com/example/**/*.class
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.resourcePattern;
			Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			for (Resource resource : resources) {
				String filename = resource.getFilename();
				if (filename != null && filename.contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
					// Ignore CGLIB-generated classes in the classpath --> 译文：忽略类路径中由 CGLIB 生成的类
					continue;
				}
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				try {
					MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
					// 根据 excludeFilters 和 （includeFilters + @Conditional） 判断其是否为候选组件
					if (isCandidateComponent(metadataReader)) {
						ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
						sbd.setSource(resource);
						if (isCandidateComponent(sbd)) {
							if (debugEnabled) {
								logger.debug("Identified candidate component class: " + resource);
							}
							candidates.add(sbd);
						}
						else {
							if (debugEnabled) {
								logger.debug("Ignored because not a concrete top-level class: " + resource);
							}
						}
					}
					else {
						if (traceEnabled) {
							logger.trace("Ignored because not matching any filter: " + resource);
						}
					}
				}
				catch (FileNotFoundException ex) {
					if (traceEnabled) {
						logger.trace("Ignored non-readable " + resource + ": " + ex.getMessage());
					}
				}
				catch (ClassFormatException ex) {
					if (shouldIgnoreClassFormatException) {
						if (debugEnabled) {
							logger.debug("Ignored incompatible class format in " + resource + ": " + ex.getMessage());
						}
					}
					else {
						throw new BeanDefinitionStoreException("Incompatible class format in " + resource +
								": set system property 'spring.classformat.ignore' to 'true' " +
								"if you mean to ignore such files during classpath scanning", ex);
					}
				}
				catch (Throwable ex) {
					throw new BeanDefinitionStoreException("Failed to read candidate component class: " + resource, ex);
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}


	/**
	 * Resolve the specified base package into a pattern specification for
	 * the package search path.
	 * <p>The default implementation resolves placeholders against system properties,
	 * and converts a "."-based package path to a "/"-based resource path.
	 * @param basePackage the base package as specified by the user
	 * @return the pattern specification to be used for package searching
	 */
	// 将指定的基包解析为用于包搜索路径的模式规范。
	// <p>默认实现会依据系统属性解析其中的占位符，并将基于“.”的包路径转换为基于“/”的资源路径。
	// @param basePackage 用户指定的基包
	// @return 用于包搜索的模式规范
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(getEnvironment().resolveRequiredPlaceholders(basePackage));
	}

	/**
	 * Determine whether the given class does not match any exclude filter
	 * and does match at least one include filter.
	 * @param metadataReader the ASM ClassReader for the class
	 * @return whether the class qualifies as a candidate component
	 */
	// 判断给定的类是否不匹配任何排除过滤器，但至少匹配一个包含过滤器。
	// @param metadataReader 该类的 ASM ClassReader
	// @return 该类是否符合候选组件的条件
	protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
		for (TypeFilter tf : this.excludeFilters) {
			if (tf.match(metadataReader, getMetadataReaderFactory())) {
				return false;
			}
		}
		for (TypeFilter tf : this.includeFilters) {
			if (tf.match(metadataReader, getMetadataReaderFactory())) {
				// 根据任何 @Conditional 注解判断给定类是否为候选组件
				return isConditionMatch(metadataReader);
			}
		}
		return false;
	}

	/**
	 * Determine whether the given class is a candidate component based on any
	 * {@code @Conditional} annotations.
	 * @param metadataReader the ASM ClassReader for the class
	 * @return whether the class qualifies as a candidate component
	 */
	// 根据任何 {@code @Conditional} 注解判断给定类是否为候选组件。
	// @param metadataReader 该类的 ASM ClassReader
	// @return 该类是否符合候选组件的条件
	private boolean isConditionMatch(MetadataReader metadataReader) {
		if (this.conditionEvaluator == null) {
			this.conditionEvaluator =
					new ConditionEvaluator(getRegistry(), this.environment, this.resourcePatternResolver);
		}
		return !this.conditionEvaluator.shouldSkip(metadataReader.getAnnotationMetadata());
	}

	/**
	 * Determine whether the given bean definition qualifies as a candidate component.
	 * <p>The default implementation checks whether the class is not dependent on an
	 * enclosing class as well as whether the class is either concrete (and therefore
	 * not an interface) or has {@link Lookup @Lookup} methods.
	 * <p>Can be overridden in subclasses.
	 * @param beanDefinition the bean definition to check
	 * @return whether the bean definition qualifies as a candidate component
	 */
	// 确定给定的 bean 定义是否符合候选组件的条件。
	// <p>默认实现会检查该类是否不依赖于外部类，以及该类是否是具体的（因此不是接口）或是否具有 {@link Lookup @Lookup} 方法。
	// <p>可在子类中重写。
	// @param beanDefinition 要检查的 bean 定义
	// @return 该 bean 定义是否符合候选组件的条件
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		AnnotationMetadata metadata = beanDefinition.getMetadata();
		return (metadata.isIndependent() && (metadata.isConcrete() ||
				(metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
	}


	/**
	 * Clear the local metadata cache, if any, removing all cached class metadata.
	 */
	public void clearCache() {
		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory cmrf) {
			// Clear cache in externally provided MetadataReaderFactory; this is a no-op
			// for a shared cache since it'll be cleared by the ApplicationContext.
			cmrf.clearCache();
		}
	}

}
