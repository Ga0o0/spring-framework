/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Convenience methods operating on bean factories, in particular
 * on the {@link ListableBeanFactory} interface.
 *
 * <p>Returns bean counts, bean names or bean instances,
 * taking into account the nesting hierarchy of a bean factory
 * (which the methods defined on the ListableBeanFactory interface don't,
 * in contrast to the methods defined on the BeanFactory interface).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 */
// 针对 bean 工厂（特别是 {@link ListableBeanFactory} 接口）的便捷方法。
//
// <p>返回 bean 数量、bean 名称或 bean 实例，并考虑 bean 工厂的嵌套层次结构（
// ListableBeanFactory 接口上定义的方法不考虑这一点，而 BeanFactory 接口上定义的方法则不同）。
public abstract class BeanFactoryUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	// 用于分隔生成的 bean 名称。如果类名或父名称不唯一，则会附加“#1”、“#2”等，直到名称唯一为止。
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";

	/**
	 * Cache from name with factory bean prefix to stripped name without dereference.
	 * @since 5.1
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	private static final Map<String, String> transformedBeanNameCache = new ConcurrentHashMap<>();


	/**
	 * Return whether the given name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 * @param name the name of the bean
	 * @return whether the given name is a factory dereference
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	// 返回给定名称是否为工厂解引用（以工厂解引用前缀开头）。
	// @param name bean 的名称
	// @return 给定名称是否为工厂解引用
	public static boolean isFactoryDereference(@Nullable String name) {
		return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
	}

	/**
	 * Return the actual bean name, stripping out the factory dereference
	 * prefix (if any, also stripping repeated factory prefixes if found).
	 * @param name the name of the bean
	 * @return the transformed name
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	// 返回实际的 Bean 名称，去掉工厂解引用前缀（如果有的话，如果发现重复的工厂前缀，也要一并去掉）。
	// @param name 组件的名称
	// @return 转换后的名称
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			return name;
		}
		return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
			do {
				beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
			}
			while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
			return beanName;
		});
	}

	/**
	 * Return whether the given name is a bean name which has been generated
	 * by the default naming strategy (containing a "#..." part).
	 * @param name the name of the bean
	 * @return whether the given name is a generated bean name
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils#generateBeanName
	 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
	 */
	public static boolean isGeneratedBeanName(@Nullable String name) {
		return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
	}

	/**
	 * Extract the "raw" bean name from the given (potentially generated) bean name,
	 * excluding any "#..." suffixes which might have been added for uniqueness.
	 * @param name the potentially generated bean name
	 * @return the raw bean name
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 */
	public static String originalBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
		return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
	}


	// Retrieval of bean names

	/**
	 * Count all beans in any hierarchy in which this factory participates.
	 * Includes counts of ancestor bean factories.
	 * <p>Beans that are "overridden" (specified in a descendant factory
	 * with the same name) are only counted once.
	 * @param lbf the bean factory
	 * @return count of beans including those defined in ancestor factories
	 * @see #beanNamesIncludingAncestors
	 */
	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesIncludingAncestors(lbf).length;
	}

	/**
	 * Return all bean names in the factory, including ancestor factories.
	 * @param lbf the bean factory
	 * @return the array of matching bean names, or an empty array if none
	 * @see #beanNamesForTypeIncludingAncestors
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesForTypeIncludingAncestors(lbf, Object.class);
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
	 * includes prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type the type that beans must match (as a {@code ResolvableType})
	 * @return the array of matching bean names, or an empty array if none
	 * @since 4.2
	 * @see ListableBeanFactory#getBeanNamesForType(ResolvableType)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "allowEagerInit" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param type the type that beans must match (as a {@code ResolvableType})
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return the array of matching bean names, or an empty array if none
	 * @since 5.2
	 * @see ListableBeanFactory#getBeanNamesForType(ResolvableType, boolean, boolean)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						pbf, type, includeNonSingletons, allowEagerInit);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of {@code beanNamesForTypeIncludingAncestors} automatically
	 * includes prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type the type that beans must match (as a {@code Class})
	 * @return the array of matching bean names, or an empty array if none
	 * @see ListableBeanFactory#getBeanNamesForType(Class)
	 */
	// 获取给定类型的所有 Bean 名称，包括祖先工厂中定义的 Bean 名称。如果 Bean 定义被覆盖，则返回唯一名称。
	// <p>会考虑 FactoryBeans 创建的对象，这意味着 FactoryBeans 会被初始化。如果 FactoryBean 创建的对象不匹配，则原始 FactoryBean 本身将与类型进行匹配。
	// <p>此版本的 {@code beanNamesForTypeIncludingAncestors} 自动包含原型和 FactoryBeans。
	// @param lbf Bean 工厂
	// @param type Bean 必须匹配的类型（以 {@code Class} 的形式）
	// @return 匹配 Bean 名称的数组，如果没有，则返回空数组
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * Get all bean names for the given type, including those defined in ancestor
	 * factories. Will return unique names in case of overridden bean definitions.
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "allowEagerInit" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @param type the type that beans must match
	 * @return the array of matching bean names, or an empty array if none
	 * @see ListableBeanFactory#getBeanNamesForType(Class, boolean, boolean)
	 */
	// 获取给定类型的所有 Bean 名称，包括在祖先工厂中定义的 Bean 名称。如果 Bean 定义被覆盖，则返回唯一名称。
	// <p>如果设置了“allowEagerInit”标志，则考虑 FactoryBeans 创建的对象，这意味着 FactoryBeans 将被初始化。
	// 如果 FactoryBean 创建的对象不匹配，则原始 FactoryBean 本身将与类型匹配。
	// 如果未设置“allowEagerInit”，则仅检查原始 FactoryBeans（这不需要初始化每个 FactoryBean）。
	// @param lbf Bean 工厂
	// @param includeNonSingletons 是否同时包含原型 Bean、作用域 Bean 或仅包含单例 Bean（也适用于 FactoryBeans）
	// @param allowEagerInit 是否初始化 <i>lazy-init 单例 </i> 和 <i>FactoryBeans</i> 创建的对象（或通过带有“factory-bean”引用的工厂方法创建的对象）
	// 以进行类型检查。请注意，FactoryBeans 需要立即初始化以确定其类型：因此请注意，传入此标志的“true”将初始化 FactoryBeans 和“factory-bean”引用。
	// @param type bean 必须匹配的类型 @return 匹配 bean 名称的数组，如果没有则返回空数组
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						pbf, type, includeNonSingletons, allowEagerInit);
				// 将给定的 bean 名称 result 与给定的父 bean 名称 result 合并。
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * Get all bean names whose {@code Class} has the supplied {@link Annotation}
	 * type, including those defined in ancestor factories, without creating any bean
	 * instances yet. Will return unique names in case of overridden bean definitions.
	 * @param lbf the bean factory
	 * @param annotationType the type of annotation to look for
	 * @return the array of matching bean names, or an empty array if none
	 * @since 5.0
	 * @see ListableBeanFactory#getBeanNamesForAnnotation(Class)
	 */
	public static String[] beanNamesForAnnotationIncludingAncestors(
			ListableBeanFactory lbf, Class<? extends Annotation> annotationType) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForAnnotation(annotationType);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForAnnotationIncludingAncestors(pbf, annotationType);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}


	// Retrieval of bean instances

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the Map of matching bean instances, or an empty Map if none
	 * @throws BeansException if a bean could not be created
	 * @see ListableBeanFactory#getBeansOfType(Class)
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<>(4);
		result.putAll(lbf.getBeansOfType(type));
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type);
				parentResult.forEach((beanName, beanInstance) -> {
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, beanInstance);
					}
				});
			}
		}
		return result;
	}

	/**
	 * Return all beans of the given type or subtypes, also picking up beans defined in
	 * ancestor bean factories if the current bean factory is a HierarchicalBeanFactory.
	 * The returned Map will only contain beans of this type.
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return the Map of matching bean instances, or an empty Map if none
	 * @throws BeansException if a bean could not be created
	 * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	 */
	// 返回给定类型或子类型的所有 Bean，如果当前 Bean 工厂是 HierarchicalBeanFactory，则还会选择在祖先 Bean 工厂中定义的 Bean。
	// 返回的 Map 将仅包含此类型的 Bean。
	// <p>如果设置了“allowEagerInit”标志，则不会考虑 FactoryBeans 创建的对象，这意味着 FactoryBeans 将被初始化。
	// 如果 FactoryBean 创建的对象不匹配，则将使用原始 FactoryBean 本身与类型进行匹配。
	// 如果未设置“allowEagerInit”，则仅检查原始 FactoryBeans（这不需要初始化每个 FactoryBean）。
	// <p><b>注意：同名 Bean 将在“最低”工厂级别中优先处理，即，此类 Bean 将从它们所在的最低工厂返回，从而隐藏祖先工厂中对应的 Bean。</b>
	// 此功能允许通过在子工厂中明确选择相同的 Bean 名称来“替换” Bean；祖先工厂中的 bean 将不可见，即使按类型查找也不可见。
	// @param lbf bean 工厂
	// @param type 要匹配的 bean 类型
	// @param includeNonSingletons 是否包含原型 bean、作用域 bean 或仅包含单例 bean（同样适用于 FactoryBeans）
	// @param allowEagerInit 是否初始化 <i>lazy-init 单例</i> 和 <i>FactoryBeans</i> 创建的对象
	// （或通过带有“factory-bean”引用的工厂方法创建的对象）以进行类型检查。请注意，FactoryBeans 需要立即初始化才能确定其类型：
	// 因此，传入此标志的“true”将初始化 FactoryBeans 和“factory-bean”引用。
	// @return 匹配 bean 实例的 Map，如果没有则返回空 Map
	// @throws BeansException 如果无法创建 bean
	// @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<>(4);
		result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type, includeNonSingletons, allowEagerInit);
				parentResult.forEach((beanName, beanInstance) -> {
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, beanInstance);
					}
				});
			}
		}
		return result;
	}

	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of {@code beanOfTypeIncludingAncestors} automatically includes
	 * prototypes and FactoryBeans.
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the matching bean instance
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, also picking up beans
	 * defined in ancestor bean factories if the current bean factory is a
	 * HierarchicalBeanFactory. Useful convenience method when we expect a
	 * single bean and don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit" flag is set,
	 * which means that FactoryBeans will get initialized. If the object created by the
	 * FactoryBean doesn't match, the raw FactoryBean itself will be matched against the
	 * type. If "allowEagerInit" is not set, only raw FactoryBeans will be checked
	 * (which doesn't require initialization of each FactoryBean).
	 * <p><b>Note: Beans of the same name will take precedence at the 'lowest' factory level,
	 * i.e. such beans will be returned from the lowest factory that they are being found in,
	 * hiding corresponding beans in ancestor factories.</b> This feature allows for
	 * 'replacing' beans by explicitly choosing the same bean name in a child factory;
	 * the bean in the ancestor factory won't be visible then, not even for by-type lookups.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return the matching bean instance
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	public static <T> T beanOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans, which means that FactoryBeans
	 * will get initialized. If the object created by the FactoryBean doesn't match,
	 * the raw FactoryBean itself will be matched against the type.
	 * <p>This version of {@code beanOfType} automatically includes
	 * prototypes and FactoryBeans.
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @return the matching bean instance
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @see ListableBeanFactory#getBeansOfType(Class)
	 */
	public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * Return a single bean of the given type or subtypes, not looking in ancestor
	 * factories. Useful convenience method when we expect a single bean and
	 * don't care about the bean name.
	 * <p>Does consider objects created by FactoryBeans if the "allowEagerInit"
	 * flag is set, which means that FactoryBeans will get initialized. If the
	 * object created by the FactoryBean doesn't match, the raw FactoryBean itself
	 * will be matched against the type. If "allowEagerInit" is not set,
	 * only raw FactoryBeans will be checked (which doesn't require initialization
	 * of each FactoryBean).
	 * @param lbf the bean factory
	 * @param type type of bean to match
	 * @param includeNonSingletons whether to include prototype or scoped beans too
	 * or just singletons (also applies to FactoryBeans)
	 * @param allowEagerInit whether to initialize <i>lazy-init singletons</i> and
	 * <i>objects created by FactoryBeans</i> (or by factory methods with a
	 * "factory-bean" reference) for the type check. Note that FactoryBeans need to be
	 * eagerly initialized to determine their type: So be aware that passing in "true"
	 * for this flag will initialize FactoryBeans and "factory-bean" references.
	 * @return the matching bean instance
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	 */
	public static <T> T beanOfType(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}


	/**
	 * Merge the given bean names result with the given parent result.
	 * @param result the local bean name result
	 * @param parentResult the parent bean name result (possibly empty)
	 * @param hbf the local bean factory
	 * @return the merged result (possibly the local result as-is)
	 * @since 4.3.15
	 */
	// 将给定的 bean 名称 result 与给定的父 bean 名称 result 合并。
	// @param result 本地 bean 名称 result
	// @param parentResult 父 bean 名称 result（可能为空）
	// @param hbf 本地 bean 工厂
	// @return 合并后的结果（可能为本地结果）
	private static String[] mergeNamesWithParent(String[] result, String[] parentResult, HierarchicalBeanFactory hbf) {
		if (parentResult.length == 0) {
			return result;
		}
		List<String> merged = new ArrayList<>(result.length + parentResult.length);
		merged.addAll(Arrays.asList(result));
		for (String beanName : parentResult) {
			if (!merged.contains(beanName) && !hbf.containsLocalBean(beanName)) {
				merged.add(beanName);
			}
		}
		return StringUtils.toStringArray(merged);
	}

	/**
	 * Extract a unique bean for the given type from the given Map of matching beans.
	 * @param type type of bean to match
	 * @param matchingBeans all matching beans found
	 * @return the unique bean instance
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 */
	private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
		int count = matchingBeans.size();
		if (count == 1) {
			return matchingBeans.values().iterator().next();
		}
		else if (count > 1) {
			throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
		}
		else {
			throw new NoSuchBeanDefinitionException(type);
		}
	}

}
