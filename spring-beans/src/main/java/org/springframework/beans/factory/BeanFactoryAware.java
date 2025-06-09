/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by beans that wish to be aware of their
 * owning {@link BeanFactory}.
 *
 * <p>For example, beans can look up collaborating beans via the factory
 * (Dependency Lookup). Note that most beans will choose to receive references
 * to collaborating beans via corresponding bean properties or constructor
 * arguments (Dependency Injection).
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 11.03.2003
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
// 需要由希望了解其所属 {@link BeanFactory} 的 Bean 实现的接口。
//
// <p>例如，Bean 可以通过工厂查找协作 Bean（依赖项查找）。需要注意的是，
// 大多数 Bean 会选择通过相应的 Bean 属性或构造函数参数接收协作 Bean 的引用（依赖项注入）。
//
// <p>有关所有 Bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。
public interface BeanFactoryAware extends Aware {

	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>Invoked after the population of normal bean properties
	 * but before an initialization callback such as
	 * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
	 * @param beanFactory owning BeanFactory (never {@code null}).
	 * The bean can immediately call methods on the factory.
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	// 将所属工厂提供给 Bean 实例的回调。
	// <p>在填充常规 Bean 属性之后、初始化回调（例如 {@link InitializingBean#afterPropertiesSet()} 或自定义初始化方法）之前调用。
	// @param beanFactory 所属 BeanFactory（永远不会为 {@code null}）。Bean 可以立即调用工厂方法。
	// @throws BeansException 表示初始化错误
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
