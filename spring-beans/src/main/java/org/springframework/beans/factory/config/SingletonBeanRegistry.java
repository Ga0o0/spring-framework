/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;

/**
 * Interface that defines a registry for shared bean instances.
 * Can be implemented by {@link org.springframework.beans.factory.BeanFactory}
 * implementations in order to expose their singleton management facility
 * in a uniform manner.
 *
 * <p>The {@link ConfigurableBeanFactory} interface extends this interface.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
// 定义共享 bean 实例注册表的接口。可以由 {@link org.springframework.beans.factory.BeanFactory} 实现实现，
// 以便以统一的方式公开其单例管理功能。
//
// <p>{@link ConfigurableBeanFactory} 接口扩展了此接口。
public interface SingletonBeanRegistry {

	/**
	 * Register the given existing object as singleton in the bean registry,
	 * under the given bean name.
	 * <p>The given instance is supposed to be fully initialized; the registry
	 * will not perform any initialization callbacks (in particular, it won't
	 * call InitializingBean's {@code afterPropertiesSet} method).
	 * The given instance will not receive any destruction callbacks
	 * (like DisposableBean's {@code destroy} method) either.
	 * <p>When running within a full BeanFactory: <b>Register a bean definition
	 * instead of an existing instance if your bean is supposed to receive
	 * initialization and/or destruction callbacks.</b>
	 * <p>Typically invoked during registry configuration, but can also be used
	 * for runtime registration of singletons. As a consequence, a registry
	 * implementation should synchronize singleton access; it will have to do
	 * this anyway if it supports a BeanFactory's lazy initialization of singletons.
	 * @param beanName the name of the bean
	 * @param singletonObject the existing singleton object
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.DisposableBean#destroy
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition
	 */
	// 在 Bean 注册表中，使用给定的 Bean 名称将给定的现有对象注册为单例。
	// <p>给定的实例应该已完全初始化；注册表不会执行任何初始化回调（特别是，它不会调用 InitializingBean 的 {@code afterPropertiesSet} 方法）。
	// 给定的实例也不会接收任何销毁回调（例如 DisposableBean 的 {@code destroy} 方法）。
	// <p>在完整的 BeanFactory 中运行时：<b>如果您的 Bean 应该接收初始化和/或销毁回调，请注册一个 Bean 定义，而不是现有实例。</b>
	// <p>通常在注册表配置期间调用，但也可以用于单例的运行时注册。因此，注册表实现应该同步单例访问；如果它支持 BeanFactory 的单例延迟初始化，则必须执行此操作。
	// @param beanName Bean 的名称
	// @param singletonObject 现有的单例对象
	void registerSingleton(String beanName, Object singletonObject);

	/**
	 * Return the (raw) singleton object registered under the given name.
	 * <p>Only checks already instantiated singletons; does not return an Object
	 * for singleton bean definitions which have not been instantiated yet.
	 * <p>The main purpose of this method is to access manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to access a singleton
	 * defined by a bean definition that already been created, in a raw fashion.
	 * <p><b>NOTE:</b> This lookup method is not aware of FactoryBean prefixes or aliases.
	 * You need to resolve the canonical bean name first before obtaining the singleton instance.
	 * @param beanName the name of the bean to look for
	 * @return the registered singleton object, or {@code null} if none found
	 * @see ConfigurableListableBeanFactory#getBeanDefinition
	 */
	// 返回以给定名称注册的（原始）单例对象。<p>仅检查已实例化的单例；对于尚未实例化的单例 bean 定义，不返回对象。
	// <p>此方法的主要目的是访问手动注册的单例（参见 {@link #registerSingleton}）。也可用于以原始方式访问由已创建的 bean 定义定义的单例。
	// <p><b>注意：</b>此查找方法无法识别 FactoryBean 前缀或别名。您需要先解析规范的 bean 名称，然后才能获取单例实例。
	// @param beanName 要查找的 bean 的名称
	// @return 注册的单例对象，如果未找到，则返回 {@code null}
	@Nullable
	Object getSingleton(String beanName);

	/**
	 * Check if this registry contains a singleton instance with the given name.
	 * <p>Only checks already instantiated singletons; does not return {@code true}
	 * for singleton bean definitions which have not been instantiated yet.
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to check whether a
	 * singleton defined by a bean definition has already been created.
	 * <p>To check whether a bean factory contains a bean definition with a given name,
	 * use ListableBeanFactory's {@code containsBeanDefinition}. Calling both
	 * {@code containsBeanDefinition} and {@code containsSingleton} answers
	 * whether a specific bean factory contains a local bean instance with the given name.
	 * <p>Use BeanFactory's {@code containsBean} for general checks whether the
	 * factory knows about a bean with a given name (whether manually registered singleton
	 * instance or created by bean definition), also checking ancestor factories.
	 * <p><b>NOTE:</b> This lookup method is not aware of FactoryBean prefixes or aliases.
	 * You need to resolve the canonical bean name first before checking the singleton status.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a singleton instance with the given name
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 * @see org.springframework.beans.factory.BeanFactory#containsBean
	 */
	// 检查此注册表是否包含具有给定名称的单例实例。<p>仅检查已实例化的单例；对于尚未实例化的单例 Bean 定义，不返回 {@code true}。
	// <p>此方法的主要目的是检查手动注册的单例（参见 {@link #registerSingleton}）。也可用于检查由 Bean 定义定义的单例是否已创建。
	// <p>要检查 Bean 工厂是否包含具有给定名称的 Bean 定义，请使用 ListableBeanFactory 的 {@code containsBeanDefinition}。
	// 同时调用 {@code containsBeanDefinition} 和 {@code containsSingleton} 可确定特定 Bean 工厂是否包含具有给定名称的本地 Bean 实例。
	// <p>使用 BeanFactory 的 {@code containsBean} 进行常规检查，以确定工厂是否知道具有给定名称的 Bean（无论是手动注册的单例实例还是由 Bean 定义创建的），同时检查祖先工厂。
	// <p><b>注意：</b>此查找方法无法识别 FactoryBean 的前缀或别名。您需要先解析规范的 bean 名称，然后再检查单例状态。
	// @param beanName 指定要查找的 bean 的名称；
	// @return 表示此 bean 工厂是否包含具有给定名称的单例实例。
	boolean containsSingleton(String beanName);

	/**
	 * Return the names of singleton beans registered in this registry.
	 * <p>Only checks already instantiated singletons; does not return names
	 * for singleton bean definitions which have not been instantiated yet.
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to check which singletons
	 * defined by a bean definition have already been created.
	 * @return the list of names as a String array (never {@code null})
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionNames
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionNames
	 */
	// 返回在此注册表中注册的单例 bean 的名称。<p>仅检查已实例化的单例；不返回尚未实例化的单例 bean 定义的名称。
	// <p>此方法主要用于检查手动注册的单例（参见 {@link #registerSingleton}）。
	// 也可用于检查 bean 定义中定义的哪些单例已创建。@return 将名称列表以字符串数组的形式返回（永不返回 {@code null}）。
	String[] getSingletonNames();

	/**
	 * Return the number of singleton beans registered in this registry.
	 * <p>Only checks already instantiated singletons; does not count
	 * singleton bean definitions which have not been instantiated yet.
	 * <p>The main purpose of this method is to check manually registered singletons
	 * (see {@link #registerSingleton}). Can also be used to count the number of
	 * singletons defined by a bean definition that have already been created.
	 * @return the number of singleton beans
	 * @see #registerSingleton
	 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#getBeanDefinitionCount
	 * @see org.springframework.beans.factory.ListableBeanFactory#getBeanDefinitionCount
	 */
	// 返回此注册表中注册的单例 bean 的数量。<p>仅检查已实例化的单例；不计算尚未实例化的单例 bean 定义。
	// <p>此方法主要用于检查手动注册的单例（参见 {@link #registerSingleton}）。
	// 也可用于统计 bean 定义中已创建的单例数量。@return 单例 bean 的数量
	int getSingletonCount();

	/**
	 * Return the singleton mutex used by this registry (for external collaborators).
	 * @return the mutex object (never {@code null})
	 * @since 4.2
	 */
	// 返回此注册表使用的单例互斥锁（用于外部协作者）。
	// @return 互斥锁对象（永不为 {@code null}）
	Object getSingletonMutex();

}
