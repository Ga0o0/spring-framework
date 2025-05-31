/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.core.type;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotationCollectors;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotationSelectors;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

/**
 * Defines access to the annotations of a specific type ({@link AnnotationMetadata class}
 * or {@link MethodMetadata method}), in a form that does not necessarily require
 * class loading of the types being inspected. Note, however, that classes for
 * encountered annotations will be loaded.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Mark Pollack
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see AnnotationMetadata
 * @see MethodMetadata
 */
// 定义对特定类型（{@link AnnotationMetadata 类} 或 {@link MethodMetadata 方法}）注解的访问，访问方式无需加载被检查类型的类。但请注意，遇到的注解对应的类会被加载。
public interface AnnotatedTypeMetadata {

	/**
	 * Get annotation details based on the direct annotations and meta-annotations
	 * of the underlying element.
	 * @return merged annotations based on the direct annotations and meta-annotations
	 * @since 5.2
	 */
	// 根据底层元素的直接注解和元注解获取注解详情。
	// @return 根据直接注解和元注解合并后的注释
	MergedAnnotations getAnnotations();

	/**
	 * Determine whether the underlying element has an annotation or meta-annotation
	 * of the given type defined.
	 * <p>If this method returns {@code true}, then
	 * {@link #getAnnotationAttributes} will return a non-null Map.
	 * @param annotationName the fully-qualified class name of the annotation
	 * type to look for
	 * @return whether a matching annotation is defined
	 */
	// 确定底层元素是否定义了指定类型的注解或元注解。
	// <p>如果此方法返回 {@code true}，则 {@link #getAnnotationAttributes} 将返回一个非空的 Map。
	// @param commentName 要查找的注解类型的完全限定类名
	// @return 是否定义了匹配的注解
	default boolean isAnnotated(String annotationName) {
		return getAnnotations().isPresent(annotationName);
	}

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} semantics
	 * are fully supported, both within a single annotation and within annotation
	 * hierarchies.
	 * @param annotationName the fully-qualified class name of the annotation
	 * type to look for
	 * @return a {@link Map} of attributes, with each annotation attribute name
	 * as map key (e.g. "location") and the attribute's value as map value; or
	 * {@code null} if no matching annotation is found
	 */
	// 检索给定类型的注解的属性（如果有）（即，如果在底层元素上定义，则为直接注解或元注解）。
	// <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} 语义完全受支持，无论是在单个注释中还是在注释层次结构中。
	// @param commentName 要查找的注释类型的完全限定类名
	// @return 属性的 {@link Map}，其中每个注释属性名称作为映射键（例如“location”），属性的值作为映射值；如果未找到匹配的注释，则返回 {@code null}
	@Nullable
	default Map<String, Object> getAnnotationAttributes(String annotationName) {
		return getAnnotationAttributes(annotationName, false);
	}

	/**
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} semantics
	 * are fully supported, both within a single annotation and within annotation
	 * hierarchies.
	 * @param annotationName the fully-qualified class name of the annotation
	 * type to look for
	 * @param classValuesAsString whether to convert class references to String
	 * class names for exposure as values in the returned Map, instead of Class
	 * references which might potentially have to be loaded first
	 * @return a {@link Map} of attributes, with each annotation attribute name
	 * as map key (e.g. "location") and the attribute's value as map value; or
	 * {@code null} if no matching annotation is found
	 */
	@Nullable
	default Map<String, Object> getAnnotationAttributes(String annotationName,
			boolean classValuesAsString) {

		MergedAnnotation<Annotation> annotation = getAnnotations().get(annotationName,
				null, MergedAnnotationSelectors.firstDirectlyDeclared());
		if (!annotation.isPresent()) {
			return null;
		}
		return annotation.asAnnotationAttributes(Adapt.values(classValuesAsString, true));
	}

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * <p>Note: this method does <i>not</i> take attribute overrides on composed
	 * annotations into account.
	 * @param annotationName the fully-qualified class name of the annotation
	 * type to look for
	 * @return a {@link MultiValueMap} of attributes, with each annotation attribute
	 * name as map key (e.g. "location") and a list of the attribute's values as
	 * map value; or {@code null} if no matching annotation is found
	 * @see #getAllAnnotationAttributes(String, boolean)
	 */
	@Nullable
	default MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
		return getAllAnnotationAttributes(annotationName, false);
	}

	/**
	 * Retrieve all attributes of all annotations of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation).
	 * <p>Note: this method does <i>not</i> take attribute overrides on composed
	 * annotations into account.
	 * @param annotationName the fully-qualified class name of the annotation
	 * type to look for
	 * @param classValuesAsString whether to convert class references to String
	 * class names for exposure as values in the returned Map, instead of Class
	 * references which might potentially have to be loaded first
	 * @return a {@link MultiValueMap} of attributes, with each annotation attribute
	 * name as map key (e.g. "location") and a list of the attribute's values as
	 * map value; or {@code null} if no matching annotation is found
	 * @see #getAllAnnotationAttributes(String)
	 */
	// 检索给定类型的所有注解的所有属性（如果有）（即，如果在底层元素上定义，则为直接注解或元注解）。
	// <p>注意：此方法<i>不</i>考虑组合注释上的属性覆盖。
	// @param commentName 要查找的注解类型的完全限定类名
	// @param classValuesAsString 是否将类引用转换为 String 类名，以便在返回的 Map 中将其作为值公开，而不是可能需要先加载的 Class 引用
	// @return 属性的 {@link MultiValueMap}，其中每个注释属性名称作为映射键（例如“location”），属性值列表作为映射值；如果未找到匹配的注释，则返回 {@code null}
	// @see #getAllAnnotationAttributes(String)
	@Nullable
	default MultiValueMap<String, Object> getAllAnnotationAttributes(
			String annotationName, boolean classValuesAsString) {

		// 工厂方法，用于根据一组布尔值标志创建 {@link Adapt} 数组。
		Adapt[] adaptations = Adapt.values(classValuesAsString, true);
		// getAnnotations() --> 根据底层元素的直接注解和元注解获取注解详情。
		return getAnnotations().stream(annotationName)
				// 创建一个新的有状态的、一次性使用的 {@link Predicate}，用于匹配基于提取的键唯一性的注解。
				.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
				.map(MergedAnnotation::withNonMergedAttributes)
				.collect(MergedAnnotationCollectors.toMultiValueMap(
						map -> (map.isEmpty() ? null : map), adaptations));
	}

	/**
	 * Retrieve all <em>repeatable annotations</em> of the given type within the
	 * annotation hierarchy <em>above</em> the underlying element (as direct
	 * annotation or meta-annotation); and for each annotation found, merge that
	 * annotation's attributes with <em>matching</em> attributes from annotations
	 * in lower levels of the annotation hierarchy and store the results in an
	 * instance of {@link AnnotationAttributes}.
	 * <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} semantics
	 * are fully supported, both within a single annotation and within annotation
	 * hierarchies.
	 * @param annotationType the annotation type to find
	 * @param containerType the type of the container that holds the annotations
	 * @param classValuesAsString whether to convert class references to {@code String}
	 * class names for exposure as values in the returned {@code AnnotationAttributes},
	 * instead of {@code Class} references which might potentially have to be loaded
	 * first
	 * @return the set of all merged repeatable {@code AnnotationAttributes} found,
	 * or an empty set if none were found
	 * @since 6.1
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, boolean, boolean)
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, Predicate, boolean, boolean)
	 */
	default Set<AnnotationAttributes> getMergedRepeatableAnnotationAttributes(
			Class<? extends Annotation> annotationType, Class<? extends Annotation> containerType,
			boolean classValuesAsString) {

		return getMergedRepeatableAnnotationAttributes(annotationType, containerType, classValuesAsString, false);
	}

	/**
	 * Retrieve all <em>repeatable annotations</em> of the given type within the
	 * annotation hierarchy <em>above</em> the underlying element (as direct
	 * annotation or meta-annotation); and for each annotation found, merge that
	 * annotation's attributes with <em>matching</em> attributes from annotations
	 * in lower levels of the annotation hierarchy and store the results in an
	 * instance of {@link AnnotationAttributes}.
	 * <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} semantics
	 * are fully supported, both within a single annotation and within annotation
	 * hierarchies.
	 * <p>If the {@code sortByReversedMetaDistance} flag is set to {@code true},
	 * the results will be sorted in {@link Comparator#reversed() reversed} order
	 * based on each annotation's {@linkplain MergedAnnotation#getDistance()
	 * meta distance}, which effectively orders meta-annotations before annotations
	 * that are declared directly on the underlying element.
	 * @param annotationType the annotation type to find
	 * @param containerType the type of the container that holds the annotations
	 * @param classValuesAsString whether to convert class references to {@code String}
	 * class names for exposure as values in the returned {@code AnnotationAttributes},
	 * instead of {@code Class} references which might potentially have to be loaded
	 * first
	 * @param sortByReversedMetaDistance {@code true} if the results should be
	 * sorted in reversed order based on each annotation's meta distance
	 * @return the set of all merged repeatable {@code AnnotationAttributes} found,
	 * or an empty set if none were found
	 * @since 6.1
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, boolean)
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, Predicate, boolean, boolean)
	 */
	// 检索底层元素上方注释层次结构中给定类型的所有可重复注释（作为直接注释或元注释）；
	// 对于找到的每个注释，将该注释的属性与注释层次结构较低级别的注释中的匹配属性合并，并将结果存储在 {@link AnnotationAttributes} 的实例中。
	// <p>完全支持 {@link org.springframework.core.annotation.AliasFor @AliasFor} 语义，无论是在单个注释中还是在注释层次结构中。
	// <p>如果 {@code sortByReversedMetaDistance} 标志设置为 {@code true}，
	// 则结果将根据每个注释的 {@linkplain MergedAnnotation#getDistance() meta distance} 以 {@link Comparator#reversed() reversed} 顺序排序，
	// 这实际上将元注释排序在直接在底层元素上声明的注释之前。
	// @param commentType 要查找的注释类型
	// @param containerType 保存注释的容器类型
	// @param classValuesAsString 是否将类引用转换为 {@code String} 类名，
	// 以便在返回的 {@code AnnotationAttributes} 中将其作为值公开，而不是 {@code Class} 引用，后者可能必须先加载
	// @param sortByReversedMetaDistance {@code true} 是否应根据每个注释的元距离按相反顺序对结果进行排序
	// @return 找到的所有合并的可重复 {@code AnnotationAttributes} 的集合，如果没有找到，则返回空集
	default Set<AnnotationAttributes> getMergedRepeatableAnnotationAttributes(
			Class<? extends Annotation> annotationType, Class<? extends Annotation> containerType,
			boolean classValuesAsString, boolean sortByReversedMetaDistance) {

		return getMergedRepeatableAnnotationAttributes(annotationType, containerType,
				mergedAnnotation -> true, classValuesAsString, sortByReversedMetaDistance);
	}

	/**
	 * Retrieve all <em>repeatable annotations</em> of the given type within the
	 * annotation hierarchy <em>above</em> the underlying element (as direct
	 * annotation or meta-annotation); and for each annotation found, merge that
	 * annotation's attributes with <em>matching</em> attributes from annotations
	 * in lower levels of the annotation hierarchy and store the results in an
	 * instance of {@link AnnotationAttributes}.
	 * <p>{@link org.springframework.core.annotation.AliasFor @AliasFor} semantics
	 * are fully supported, both within a single annotation and within annotation
	 * hierarchies.
	 * <p>The supplied {@link Predicate} will be used to filter the results. For
	 * example, supply {@code mergedAnnotation -> true} to include all annotations
	 * in the results; supply {@code MergedAnnotation::isDirectlyPresent} to limit
	 * the results to directly declared annotations, etc.
	 * <p>If the {@code sortByReversedMetaDistance} flag is set to {@code true},
	 * the results will be sorted in {@link Comparator#reversed() reversed} order
	 * based on each annotation's {@linkplain MergedAnnotation#getDistance()
	 * meta distance}, which effectively orders meta-annotations before annotations
	 * that are declared directly on the underlying element.
	 * @param annotationType the annotation type to find
	 * @param containerType the type of the container that holds the annotations
	 * @param predicate a {@code Predicate} to apply to each {@code MergedAnnotation}
	 * to determine if it should be included in the results
	 * @param classValuesAsString whether to convert class references to {@code String}
	 * class names for exposure as values in the returned {@code AnnotationAttributes},
	 * instead of {@code Class} references which might potentially have to be loaded
	 * first
	 * @param sortByReversedMetaDistance {@code true} if the results should be
	 * sorted in reversed order based on each annotation's meta distance
	 * @return the set of all merged repeatable {@code AnnotationAttributes} found,
	 * or an empty set if none were found
	 * @since 6.1.2
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, boolean)
	 * @see #getMergedRepeatableAnnotationAttributes(Class, Class, boolean, boolean)
	 */
	default Set<AnnotationAttributes> getMergedRepeatableAnnotationAttributes(
			Class<? extends Annotation> annotationType, Class<? extends Annotation> containerType,
			Predicate<MergedAnnotation<? extends Annotation>> predicate, boolean classValuesAsString,
			boolean sortByReversedMetaDistance) {

		Stream<MergedAnnotation<Annotation>> stream = getAnnotations().stream()
				.filter(predicate)
				.filter(MergedAnnotationPredicates.typeIn(containerType, annotationType));

		if (sortByReversedMetaDistance) {
			stream = stream.sorted(reversedMetaDistance());
		}

		Adapt[] adaptations = Adapt.values(classValuesAsString, true);
		return stream
				.map(annotation -> annotation.asAnnotationAttributes(adaptations))
				.flatMap(attributes -> {
					if (containerType.equals(attributes.annotationType())) {
						return Stream.of(attributes.getAnnotationArray(MergedAnnotation.VALUE));
					}
					return Stream.of(attributes);
				})
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}


	private static Comparator<MergedAnnotation<Annotation>> reversedMetaDistance() {
		return Comparator.<MergedAnnotation<Annotation>> comparingInt(MergedAnnotation::getDistance).reversed();
	}

}
