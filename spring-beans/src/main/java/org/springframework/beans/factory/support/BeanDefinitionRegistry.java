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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * Interface for registries that hold bean definitions, for example RootBeanDefinition
 * and ChildBeanDefinition instances. Typically implemented by BeanFactories that
 * internally work with the AbstractBeanDefinition hierarchy.
 *
 * <p>This is the only interface in Spring's bean factory packages that encapsulates
 * <i>registration</i> of bean definitions. The standard BeanFactory interfaces
 * only cover access to a <i>fully configured factory instance</i>.
 *
 * <p>Spring's bean definition readers expect to work on an implementation of this
 * interface. Known implementors within the Spring core are DefaultListableBeanFactory
 * and GenericApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
// 用于保存 Bean 定义（例如 RootBeanDefinition 和 ChildBeanDefinition 实例）的注册表接口。
// 通常由内部与 AbstractBeanDefinition 层次结构配合使用的 BeanFactory 实现。
//
// <p>这是 Spring Bean 工厂包中唯一封装 Bean 定义<i>注册</i>的接口。
// 标准 BeanFactory 接口仅涵盖对<i>完全配置的工厂实例</i>的访问。
//
// <p>Spring 的 Bean 定义读取器期望使用此接口的实现。
// Spring 核心中已知的实现者是 DefaultListableBeanFactory 和 GenericApplicationContext。
public interface BeanDefinitionRegistry extends AliasRegistry {

	/**
	 * Register a new bean definition with this registry.
	 * Must support RootBeanDefinition and ChildBeanDefinition.
	 * @param beanName the name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeanDefinitionStoreException if the BeanDefinition is invalid
	 * @throws BeanDefinitionOverrideException if there is already a BeanDefinition
	 * for the specified bean name and we are not allowed to override it
	 * @see GenericBeanDefinition
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	// 使用此注册表注册一个新的 Bean 定义。必须支持 RootBeanDefinition 和 ChildBeanDefinition。
	// @param beanName 要注册的 Bean 实例的名称
	// @param beanDefinition 要注册的 Bean 实例的定义
	// @throws BeanDefinitionStoreException 如果 BeanDefinition 无效
	// @throws BeanDefinitionOverrideException 如果指定的 Bean 名称已经存在，并且不允许覆盖它
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	/**
	 * Remove the BeanDefinition for the given name.
	 * @param beanName the name of the bean instance to register
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	// 移除给定名称的 BeanDefinition。
	// @param beanName 需要注册的 Bean 实例的名称
	// @throws NoSuchBeanDefinitionException（如果不存在这样的 Bean 定义）
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Return the BeanDefinition for the given bean name.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for the given name (never {@code null})
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 */
	// 返回指定 Bean 名称的 BeanDefinition。
	// @param beanName 需要查找定义的 Bean 名称
	// @return 指定名称的 BeanDefinition（永不为 null）
	// @throws NoSuchBeanDefinitionException（如果不存在这样的 Bean 定义）
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Check if this registry contains a bean definition with the given name.
	 * @param beanName the name of the bean to look for
	 * @return if this registry contains a bean definition with the given name
	 */
	// 检查此注册表是否包含具有指定名称的 Bean 定义。
	// @param beanName 要查找的 bean 的名称
	// @return 如果此注册表包含具有给定名称的 bean 定义，则返回结果
	boolean containsBeanDefinition(String beanName);

	/**
	 * Return the names of all beans defined in this registry.
	 * @return the names of all beans defined in this registry,
	 * or an empty array if none defined
	 */
	// 返回此注册表中定义的所有 Bean 的名称。
	// @return 如果未定义，则返回一个空数组。
	String[] getBeanDefinitionNames();

	/**
	 * Return the number of beans defined in the registry.
	 * @return the number of beans defined in the registry
	 */
	// 返回注册表中定义的 bean 数量。
	// @return 注册表中定义的 bean 数量
	int getBeanDefinitionCount();

	/**
	 * Determine whether the bean definition for the given name is overridable,
	 * i.e. whether {@link #registerBeanDefinition} would successfully return
	 * against an existing definition of the same name.
	 * <p>The default implementation returns {@code true}.
	 * @param beanName the name to check
	 * @return whether the definition for the given bean name is overridable
	 * @since 6.1
	 */
	// 判断给定名称的 bean 定义是否可覆盖，即 {@link #registerBeanDefinition} 是否可以成功返回与现有同名定义对应的结果。
	// <p>默认实现返回 {@code true}。
	// @param beanName 要检查的名称
	// @return 指定 bean 名称的定义是否可覆盖
	default boolean isBeanDefinitionOverridable(String beanName) {
		return true;
	}

	/**
	 * Determine whether the given bean name is already in use within this registry,
	 * i.e. whether there is a local bean or alias registered under this name.
	 * @param beanName the name to check
	 * @return whether the given bean name is already in use
	 */
	// 判断给定的 bean 名称是否已在此注册表中使用，即是否有本地 bean 或别名以此名称注册。
	// @param beanName 要检查的名称
	// @return 给定的 bean 名称是否已被使用
	boolean isBeanNameInUse(String beanName);

}
