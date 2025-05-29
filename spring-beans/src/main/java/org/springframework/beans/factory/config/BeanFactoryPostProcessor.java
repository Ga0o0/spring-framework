/*
 * Copyright 2002-2019 the original author or authors.
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

import org.springframework.beans.BeansException;

/**
 * Factory hook that allows for custom modification of an application context's
 * bean definitions, adapting the bean property values of the context's underlying
 * bean factory.
 *
 * <p>Useful for custom config files targeted at system administrators that
 * override bean properties configured in the application context. See
 * {@link PropertyResourceConfigurer} and its concrete implementations for
 * out-of-the-box solutions that address such configuration needs.
 *
 * <p>A {@code BeanFactoryPostProcessor} may interact with and modify bean
 * definitions, but never bean instances. Doing so may cause premature bean
 * instantiation, violating the container and causing unintended side effects.
 * If bean instance interaction is required, consider implementing
 * {@link BeanPostProcessor} instead.
 *
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} auto-detects {@code BeanFactoryPostProcessor}
 * beans in its bean definitions and applies them before any other beans get created.
 * A {@code BeanFactoryPostProcessor} may also be registered programmatically
 * with a {@code ConfigurableApplicationContext}.
 *
 * <h3>Ordering</h3>
 * <p>{@code BeanFactoryPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanFactoryPostProcessor} beans that are registered programmatically
 * with a {@code ConfigurableApplicationContext} will be applied in the order of
 * registration; any ordering semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanFactoryPostProcessor} beans.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 06.07.2003
 * @see BeanPostProcessor
 * @see PropertyResourceConfigurer
 */
// 工厂钩子允许自定义修改应用程序上下文的 bean 定义，从而调整上下文底层 bean 工厂的 bean 属性值。
//
// <p>对于面向系统管理员的自定义配置文件很有用，这些配置文件会覆盖应用程序上下文中配置的 bean 属性。
// 请参阅 {@link PropertyResourceConfigurer} 及其具体实现，了解满足此类配置需求的开箱即用解决方案。
//
// <p>{@code BeanFactoryPostProcessor} 可以与 bean 定义交互并修改 bean 定义，但绝不会与 bean 实例交互并修改 bean 实例。
// 这样做可能会导致 bean 过早实例化，违反容器并产生意外的副作用。如果需要 bean 实例交互，请考虑改为实现 {@link BeanPostProcessor}。
//
// <h3>注册</h3>
// <p>{@code ApplicationContext} 会自动检测其 bean 定义中的 {@code BeanFactoryPostProcessor} bean，并在创建任何其他 bean 之前应用它们。
// 也可以通过编程方式向 {@code ConfigurableApplicationContext} 注册 {@code BeanFactoryPostProcessor}。
//
// <h3>排序</h3> <p>在 {@code ApplicationContext} 中自动检测到的 {@code BeanFactoryPostProcessor} bean
// 将根据 {@link org.springframework.core.PriorityOrdered} 和 {@link org.springframework.core.Ordered} 语义进行排序。
// 相反，通过编程方式向 {@code ConfigurableApplicationContext} 注册的 {@code BeanFactoryPostProcessor} bean 将按照注册顺序应用；
// 对于通过编程方式注册的后处理器，任何通过实现 {@code PriorityOrdered} 或 {@code Ordered} 接口表达的排序语义都将被忽略。
// 此外，{@link org.springframework.core.annotation.Order @Order} 注释不适用于 {@code BeanFactoryPostProcessor} bean。
@FunctionalInterface
public interface BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for overriding or adding
	 * properties even to eager-initializing beans.
	 * @param beanFactory the bean factory used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 在标准初始化之后修改应用上下文的内部 bean 工厂。
	// 所有 bean 定义都已加载，但尚未实例化任何 bean。这允许覆盖或添加属性，即使是预先初始化的 bean。
	// @param beanFactory 是应用上下文使用的 bean 工厂。
	// @throws org.springframework.beans.BeansException 是错误抛出的异常。
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
