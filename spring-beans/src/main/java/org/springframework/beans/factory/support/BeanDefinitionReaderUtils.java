/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility methods that are useful for bean definition reader implementations.
 * Mainly intended for internal use.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
// 对 Bean 定义读取器实现有用的实用方法。主要供内部使用。
public abstract class BeanDefinitionReaderUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	// 生成的 bean 名称的分隔符。如果类名或父类名不唯一，则会添加 “#1”、“#2” 等，直到名称唯一。
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;


	/**
	 * Create a new GenericBeanDefinition for the given parent name and class name,
	 * eagerly loading the bean class if a ClassLoader has been specified.
	 * @param parentName the name of the parent bean, if any
	 * @param className the name of the bean class, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be {@code null} to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	// 为给定的父名称和类名创建一个新的 GenericBeanDefinition，如果已指定 ClassLoader，则急切地加载 bean 类。
	// @param parentName 父 bean 的名称（如果有）
	// @param className bean 类的名称（如果有）
	// @param classLoader 用于加载 bean 类的 ClassLoader（可以是 {@code null} 仅按名称注册 bean 类）
	// @return bean 定义
	// @throws ClassNotFoundException 如果无法加载 bean 类
	public static AbstractBeanDefinition createBeanDefinition(
			@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

		GenericBeanDefinition bd = new GenericBeanDefinition();
		bd.setParentName(parentName);
		if (className != null) {
			if (classLoader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			}
			else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * Generate a bean name for the given top-level bean definition,
	 * unique within the given bean factory.
	 * @param beanDefinition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 * @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
	 */
	// 为给定的顶级 bean 定义生成一个 bean 名称，该名称在给定的 bean 工厂中是唯一的。
	// @param beanDefinition 生成 bean 名称的 bean 定义
	// @param registry 该定义将要注册的 bean 工厂（用于检查现有的 bean 名称）
	// @return 生成的 bean 名称
	// @throws BeanDefinitionStoreException（如果无法为给定的 bean 定义生成唯一名称）
	// @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
	public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		return generateBeanName(beanDefinition, registry, false);
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 * @param definition the bean definition to generate a bean name for
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @param isInnerBean whether the given bean definition will be registered
	 * as inner bean or as top-level bean (allowing for special name generation
	 * for inner beans versus top-level beans)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 */
	// 为给定的 bean 定义生成一个 bean 名称，该名称在给定的 bean 工厂中是唯一的。
	// @param beanDefinition 生成 bean 名称的 bean 定义
	// @param registry 该定义将要注册的 bean 工厂（用于检查现有的 bean 名称）
	// @param isInnerBean 给定的 bean 定义是否将注册为内部 bean 或顶级 bean（允许为内部 bean 和顶级 bean 生成特殊名称）
	// @return 生成的 bean 名称
	// @throws BeanDefinitionStoreException（如果无法为给定的 bean 定义生成唯一名称）
	public static String generateBeanName(
			BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedBeanName = definition.getBeanClassName();
		if (generatedBeanName == null) {
			if (definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			}
			else if (definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		}

		if (isInnerBean) {
			// Inner bean: generate identity hashcode suffix. --> 译文：内部 bean：生成身份哈希码后缀。
			return generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		}

		// Top-level bean: use plain class name with unique suffix if necessary. --> 译文：顶级 bean：如有必要，请使用带有唯一后缀的普通类名。
		return uniqueBeanName(generatedBeanName, registry);
	}

	/**
	 * Turn the given bean name into a unique bean name for the given bean factory,
	 * appending a unique counter as suffix if necessary.
	 * @param beanName the original bean name
	 * @param registry the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @return the unique bean name to use
	 * @since 5.1
	 */
	// 将给定的 bean 名称转换为给定 bean 工厂的唯一 bean 名称，如有必要，附加一个唯一的计数器作为后缀。
	// @param beanName 原始 bean 名称
	// @param registry 定义将要注册到的 bean 工厂（用于检查是否存在现有的 bean 名称）
	// @return 要使用的唯一 bean 名称
	public static String uniqueBeanName(String beanName, BeanDefinitionRegistry registry) {
		String id = beanName;
		int counter = -1;

		// Increase counter until the id is unique.
		String prefix = beanName + GENERATED_BEAN_NAME_SEPARATOR;
		while (counter == -1 || registry.containsBeanDefinition(id)) {
			counter++;
			id = prefix + counter;
		}
		return id;
	}

	/**
	 * Register the given bean definition with the given bean factory.
	 * @param definitionHolder the bean definition including name and aliases
	 * @param registry the bean factory to register with
	 * @throws BeanDefinitionStoreException if registration failed
	 */
	// 将指定的 bean 定义注册到指定的 bean 工厂。
	// @param definitionHolder bean 定义，包括名称和别名
	// @param registry 要注册的 bean 工厂
	// @throws BeanDefinitionStoreException（如果注册失败）
	public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// Register bean definition under primary name. --> 译文：使用主名称注册 Bean 定义。
		String beanName = definitionHolder.getBeanName();
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// Register aliases for bean name, if any. --> 译文：为 Bean 名称注册别名（如果有）。
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				registry.registerAlias(beanName, alias); // 指定名称，为其注册别名。
			}
		}
	}

	/**
	 * Register the given bean definition with a generated name,
	 * unique within the given bean factory.
	 * @param definition the bean definition to generate a bean name for
	 * @param registry the bean factory to register with
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition or the definition cannot be registered
	 */
	// 使用生成的名称注册给定的 bean 定义，该名称在给定的 bean 工厂中是唯一的。
	// @param definition 要为其生成 bean 名称的 bean 定义
	// @param registry 要注册的 bean 工厂
	// @return 生成的 bean 名称
	// 如果无法为给定的 bean 定义生成唯一名称或无法注册该定义，则抛出 BeanDefinitionStoreException
	public static String registerWithGeneratedName(
			AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		String generatedName = generateBeanName(definition, registry, false);
		registry.registerBeanDefinition(generatedName, definition);
		return generatedName;
	}

}
