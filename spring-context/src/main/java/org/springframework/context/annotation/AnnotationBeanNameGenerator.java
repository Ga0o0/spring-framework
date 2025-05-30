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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link BeanNameGenerator} implementation for bean classes annotated with the
 * {@link org.springframework.stereotype.Component @Component} annotation or
 * with another annotation that is itself annotated with {@code @Component} as a
 * meta-annotation. For example, Spring's stereotype annotations (such as
 * {@link org.springframework.stereotype.Repository @Repository}) are
 * themselves annotated with {@code @Component}.
 *
 * <p>Also supports Jakarta EE's {@link jakarta.annotation.ManagedBean} and
 * JSR-330's {@link jakarta.inject.Named} annotations (as well as their pre-Jakarta
 * {@code javax.annotation.ManagedBean} and {@code javax.inject.Named} equivalents),
 * if available. Note that Spring component annotations always override such
 * standard annotations.
 *
 * <p>If the annotation's value doesn't indicate a bean name, an appropriate
 * name will be built based on the short name of the class (with the first
 * letter lower-cased), unless the first two letters are uppercase. For example:
 *
 * <pre class="code">com.xyz.FooServiceImpl -&gt; fooServiceImpl</pre>
 * <pre class="code">com.xyz.URLFooServiceImpl -&gt; URLFooServiceImpl</pre>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.stereotype.Component#value()
 * @see org.springframework.stereotype.Repository#value()
 * @see org.springframework.stereotype.Service#value()
 * @see org.springframework.stereotype.Controller#value()
 * @see jakarta.inject.Named#value()
 * @see FullyQualifiedAnnotationBeanNameGenerator
 */
// {@link BeanNameGenerator} 实现，适用于使用 {@link org.springframework.stereotype.Component @Component} 注解
// 或其他本身已使用 {@code @Component} 作为元注解的注解的 bean 类。
// 例如，Spring 的构造型注解（例如 {@link org.springframework.stereotype.Repository @Repository}）本身就使用 {@code @Component} 注解。
//
// <p>如果可用，也支持 Jakarta EE 的 {@link jakarta.annotation.ManagedBean} 和 JSR-330 的 {@link jakarta.inject.Named} 注解
// （以及 Jakarta 之前的 {@code javax.annotation.ManagedBean} 和 {@code javax.inject.Named} 等效注解）。请注意，Spring 组件注解始终会覆盖此类标准注解。
//
// <p>如果注解的值未指定 Bean 名称，则将根据类的简称（首字母小写）构建合适的名称，除非前两个字母大写。
// 例如：
//
// <pre class="code">com.xyz.FooServiceImpl -> fooServiceImpl</pre>
// <pre class="code">com.xyz.URLFooServiceImpl -> URLFooServiceImpl</pre>
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	/**
	 * A convenient constant for a default {@code AnnotationBeanNameGenerator} instance,
	 * as used for component scanning purposes.
	 * @since 5.2
	 */
	// 默认 {@code AnnotationBeanNameGenerator} 实例的便捷常量，用于组件扫描目的。
	public static final AnnotationBeanNameGenerator INSTANCE = new AnnotationBeanNameGenerator();

	private static final String COMPONENT_ANNOTATION_CLASSNAME = "org.springframework.stereotype.Component";

	private static final Adapt[] ADAPTATIONS = Adapt.values(false, true);


	private static final Log logger = LogFactory.getLog(AnnotationBeanNameGenerator.class);

	/**
	 * Set used to track which stereotype annotations have already been checked
	 * to see if they use a convention-based override for the {@code value}
	 * attribute in {@code @Component}.
	 * @since 6.1
	 * @see #determineBeanNameFromAnnotation(AnnotatedBeanDefinition)
	 */
	// 设置用于跟踪哪些构造型注释已经被检查过，以查看它们是否对 {@code @Component} 中的 {@code value} 属性使用基于约定的覆盖。
	private static final Set<String> conventionBasedStereotypeCheckCache = ConcurrentHashMap.newKeySet();

	private final Map<String, Set<String>> metaAnnotationTypesCache = new ConcurrentHashMap<>();



	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		if (definition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
			String beanName = determineBeanNameFromAnnotation(annotatedBeanDefinition);
			if (StringUtils.hasText(beanName)) {
				// Explicit bean name found.
				return beanName;
			}
		}
		// Fallback: generate a unique default bean name. --> 译文：回退：生成唯一的缺省 Bean 名称。
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * Derive a bean name from one of the annotations on the class.
	 * @param annotatedDef the annotation-aware bean definition
	 * @return the bean name, or {@code null} if none is found
	 */
	// 从类上的某个注解中获取 bean 的名称。
	// @param annotatedDef 支持注解的 bean 定义
	// @return bean 的名称，如果未找到，则返回 {@code null}
	@Nullable
	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
		AnnotationMetadata metadata = annotatedDef.getMetadata();

		String beanName = getExplicitBeanName(metadata);
		if (beanName != null) {
			return beanName;
		}

		// List of annotations directly present on the class we're searching on.
		// MergedAnnotation implementations do not implement equals()/hashCode(),
		// so we use a List and a 'visited' Set below.
		List<MergedAnnotation<Annotation>> mergedAnnotations = metadata.getAnnotations().stream()
				.filter(MergedAnnotation::isDirectlyPresent)
				.toList();

		Set<AnnotationAttributes> visited = new HashSet<>();

		for (MergedAnnotation<Annotation> mergedAnnotation : mergedAnnotations) {
			AnnotationAttributes attributes = mergedAnnotation.asAnnotationAttributes(ADAPTATIONS);
			if (visited.add(attributes)) {
				String annotationType = mergedAnnotation.getType().getName();
				Set<String> metaAnnotationTypes = this.metaAnnotationTypesCache.computeIfAbsent(annotationType,
						key -> getMetaAnnotationTypes(mergedAnnotation));
				if (isStereotypeWithNameValue(annotationType, metaAnnotationTypes, attributes)) {
					Object value = attributes.get(MergedAnnotation.VALUE);
					if (value instanceof String currentName && !currentName.isBlank()) {
						if (conventionBasedStereotypeCheckCache.add(annotationType) &&
								metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) && logger.isWarnEnabled()) {
							if (hasExplicitlyAliasedValueAttribute(mergedAnnotation.getType())) {
								logger.warn("""
										Although the 'value' attribute in @%s declares @AliasFor for an attribute \
										other than @Component's 'value' attribute, the value is still used as the \
										@Component name based on convention. As of Spring Framework 7.0, such a \
										'value' attribute will no longer be used as the @Component name."""
											.formatted(annotationType));
							}
							else {
								logger.warn("""
										Support for convention-based @Component names is deprecated and will \
										be removed in a future version of the framework. Please annotate the \
										'value' attribute in @%s with @AliasFor(annotation=Component.class) \
										to declare an explicit alias for @Component's 'value' attribute."""
											.formatted(annotationType));
							}
						}
						if (beanName != null && !currentName.equals(beanName)) {
							throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
									"component names: '" + beanName + "' versus '" + currentName + "'");
						}
						beanName = currentName;
					}
				}
			}
		}
		return beanName;
	}

	private Set<String> getMetaAnnotationTypes(MergedAnnotation<Annotation> mergedAnnotation) {
		Set<String> result = MergedAnnotations.from(mergedAnnotation.getType()).stream()
				.map(metaAnnotation -> metaAnnotation.getType().getName())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return (result.isEmpty() ? Collections.emptySet() : result);
	}

	/**
	 * Get the explicit bean name for the underlying class, as configured via
	 * {@link org.springframework.stereotype.Component @Component} and taking into
	 * account {@link org.springframework.core.annotation.AliasFor @AliasFor}
	 * semantics for annotation attribute overrides for {@code @Component}'s
	 * {@code value} attribute.
	 * @param metadata the {@link AnnotationMetadata} for the underlying class
	 * @return the explicit bean name, or {@code null} if not found
	 * @since 6.1
	 * @see org.springframework.stereotype.Component#value()
	 */
	// 获取底层类的显式 bean 名称，通过 {@link org.springframework.stereotype.Component @Component} 配置，
	// 并考虑 {@link org.springframework.core.annotation.AliasFor @AliasFor} 语义，
	// 用于注释属性覆盖 {@code @Component} 的 {@code value} 属性。
	// @param metadata 底层类的 {@link AnnotationMetadata}
	// @return 显式 bean 名称，如果未找到，则返回 {@code null}
	@Nullable
	private String getExplicitBeanName(AnnotationMetadata metadata) {
		List<String> names = metadata.getAnnotations().stream(COMPONENT_ANNOTATION_CLASSNAME)
				.map(annotation -> annotation.getString(MergedAnnotation.VALUE))
				.filter(StringUtils::hasText)
				.map(String::trim)
				.distinct()
				.toList();

		if (names.size() == 1) {
			return names.get(0);
		}
		if (names.size() > 1) {
			throw new IllegalStateException(
					"Stereotype annotations suggest inconsistent component names: " + names);
		}
		return null;
	}

	/**
	 * Check whether the given annotation is a stereotype that is allowed
	 * to suggest a component name through its {@code value()} attribute.
	 * @param annotationType the name of the annotation class to check
	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
	 * @param attributes the map of attributes for the given annotation
	 * @return whether the annotation qualifies as a stereotype with component name
	 */
	// 检查给定注解是否为允许通过其 {@code value()} 属性建议组件名称的构造型。
	// @param commentType 要检查的注解类的名称
	// @param metaAnnotationTypes 给定注解上的元注解名称
	// @param attribute 给定注解的属性映射
	// @return 该注解是否符合具有组件名称的构造型
	protected boolean isStereotypeWithNameValue(String annotationType,
			Set<String> metaAnnotationTypes, Map<String, Object> attributes) {

		boolean isStereotype = metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) ||
				annotationType.equals("jakarta.annotation.ManagedBean") ||
				annotationType.equals("javax.annotation.ManagedBean") ||
				annotationType.equals("jakarta.inject.Named") ||
				annotationType.equals("javax.inject.Named");

		return (isStereotype && attributes.containsKey(MergedAnnotation.VALUE));
	}

	/**
	 * Derive a default bean name from the given bean definition.
	 * <p>The default implementation delegates to {@link #buildDefaultBeanName(BeanDefinition)}.
	 * @param definition the bean definition to build a bean name for
	 * @param registry the registry that the given bean definition is being registered with
	 * @return the default bean name (never {@code null})
	 */
	// 从给定的 bean 定义中获取一个默认的 bean 名称。
	// <p>默认实现委托给 {@link #buildDefaultBeanName(BeanDefinition)}。
	// @param definition 需要为其构建 bean 名称的 bean 定义
	// @param registry 指定 bean 定义正在注册的注册表
	// @return 默认 bean 名称（永不为 {@code null}）
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return buildDefaultBeanName(definition);
	}

	/**
	 * Derive a default bean name from the given bean definition.
	 * <p>The default implementation simply builds a decapitalized version
	 * of the short class name: e.g. "mypackage.MyJdbcDao" &rarr; "myJdbcDao".
	 * <p>Note that inner classes will thus have names of the form
	 * "outerClassName.InnerClassName", which because of the period in the
	 * name may be an issue if you are autowiring by name.
	 * @param definition the bean definition to build a bean name for
	 * @return the default bean name (never {@code null})
	 */
	// 从给定的 bean 定义中获取一个默认的 bean 名称。
	// <p>默认实现只是构建一个首字母大写的短类名：例如 “mypackage.MyJdbcDao”→“myJdbcDao”。
	// <p>请注意，内部类的名称将采用 “outerClassName.InnerClassName” 的形式，
	// 如果您通过名称自动装配，由于名称中含有句点，这可能会出现问题。
	// @param definition 要为其构建 bean 名称的 bean 定义
	// @return 默认 bean 名称（永不返回 {@code null}）
	protected String buildDefaultBeanName(BeanDefinition definition) {
		String beanClassName = definition.getBeanClassName();
		Assert.state(beanClassName != null, "No bean class name set");
		String shortClassName = ClassUtils.getShortName(beanClassName);
		return StringUtils.uncapitalizeAsProperty(shortClassName);
	}

	/**
	 * Determine if the supplied annotation type declares a {@code value()} attribute
	 * with an explicit alias configured via {@link AliasFor @AliasFor}.
	 * @since 6.2.3
	 */
	// 确定所提供的注释类型是否声明了通过 {@link AliasFor @AliasFor} 配置的明确别名的 {@code value()} 属性。
	private static boolean hasExplicitlyAliasedValueAttribute(Class<? extends Annotation> annotationType) {
		Method valueAttribute = ReflectionUtils.findMethod(annotationType, MergedAnnotation.VALUE);
		return (valueAttribute != null && valueAttribute.isAnnotationPresent(AliasFor.class));
	}

}
