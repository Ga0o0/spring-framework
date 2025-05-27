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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

/**
 * Interface responsible for creating instances corresponding to a root bean definition.
 *
 * <p>This is pulled out into a strategy as various approaches are possible,
 * including using CGLIB to create subclasses on the fly to support Method Injection.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
// 负责创建与根 bean 定义对应的实例的接口。
//
// <p>由于有多种方法可用，因此将其提取到策略中，包括使用 CGLIB 动态创建子类以支持方法注入。
public interface InstantiationStrategy {

	/**
	 * Return an instance of the bean with the given name in this factory.
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * The name can be {@code null} if we are autowiring a bean which doesn't
	 * belong to the factory.
	 * @param owner the owning BeanFactory
	 * @return a bean instance for this bean definition
	 * @throws BeansException if the instantiation attempt failed
	 */
	// 返回此工厂中指定名称的 Bean 实例。
	// @param bd Bean 定义
	// @param beanName Bean 在当前上下文中创建时的名称。如果自动装配的 Bean 不属于此工厂，则名称可以为 {@code null}。
	// @param owner 所属 BeanFactory
	// @return 此 Bean 定义的 Bean 实例
	// @throws BeansException 如果实例化尝试失败
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner)
			throws BeansException;

	/**
	 * Return an instance of the bean with the given name in this factory,
	 * creating it via the given constructor.
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * The name can be {@code null} if we are autowiring a bean which doesn't
	 * belong to the factory.
	 * @param owner the owning BeanFactory
	 * @param ctor the constructor to use
	 * @param args the constructor arguments to apply
	 * @return a bean instance for this bean definition
	 * @throws BeansException if the instantiation attempt failed
	 */
	// 返回此工厂中指定名称的 bean 实例，并通过指定的构造函数创建。
	// @param bd bean 定义
	// @param beanName bean 在此上下文中创建时的名称。如果我们自动装配的 bean 不属于此工厂，则名称可以为 {@code null}。
	// @param owner 所属的 BeanFactory
	// @param ctor 要使用的构造函数
	// @param args 要应用的构造函数参数
	// @return 此 bean 定义的 bean 实例
	// @throws BeansException 如果实例化尝试失败
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			Constructor<?> ctor, Object... args) throws BeansException;

	/**
	 * Return an instance of the bean with the given name in this factory,
	 * creating it via the given factory method.
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * The name can be {@code null} if we are autowiring a bean which doesn't
	 * belong to the factory.
	 * @param owner the owning BeanFactory
	 * @param factoryBean the factory bean instance to call the factory method on,
	 * or {@code null} in case of a static factory method
	 * @param factoryMethod the factory method to use
	 * @param args the factory method arguments to apply
	 * @return a bean instance for this bean definition
	 * @throws BeansException if the instantiation attempt failed
	 */
	// 返回此工厂中指定名称的 Bean 实例，并通过指定的工厂方法创建该 Bean。
	// @param bd Bean 定义
	// @param beanName 在该上下文中创建 Bean 时的名称。如果要自动装配不属于此工厂的 Bean，则名称可以为 {@code null}。
	// @param owner 所属 BeanFactory
	// @param factoryBean 调用工厂方法的工厂 Bean 实例，如果是静态工厂方法，则为 {@code null}。
	// @param factoryMethod 要使用的工厂方法
	// @param args 要应用的工厂方法参数
	// @return 此 Bean 定义的 Bean 实例
	// 如果实例化尝试失败，则抛出 BeansException
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Object factoryBean, Method factoryMethod, Object... args)
			throws BeansException;

	/**
	 * Determine the actual class for the given bean definition, as instantiated at runtime.
	 * @since 6.0
	 */
	// 确定给定 bean 定义的实际类（在运行时实例化）。
	default Class<?> getActualBeanClass(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		return bd.getBeanClass();
	}

}
