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
import org.springframework.lang.Nullable;

/**
 * Factory hook that allows for custom modification of new bean instances &mdash;
 * for example, checking for marker interfaces or wrapping beans with proxies.
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.
 *
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} can autodetect {@code BeanPostProcessor} beans
 * in its bean definitions and apply those post-processors to any beans subsequently
 * created. A plain {@code BeanFactory} allows for programmatic registration of
 * post-processors, applying them to all beans created through the bean factory.
 *
 * <h3>Ordering</h3>
 * <p>{@code BeanPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanPostProcessor} beans that are registered programmatically with a
 * {@code BeanFactory} will be applied in the order of registration; any ordering
 * semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanPostProcessor} beans.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
// 工厂钩子允许自定义修改新的 bean 实例 - 例如，检查标记接口或使用代理包装 bean。
//
// <p>通常，通过标记接口或类似方式填充 bean 的后处理器将实现 {@link #postProcessBeforeInitialization}，
// 而使用代理包装 bean 的后处理器通常会实现 {@link #postProcessAfterInitialization}。
//
// <h3>注册</h3>
// <p>{@code ApplicationContext} 可以自动检测其 bean 定义中的 {@code BeanPostProcessor} bean，并将这些后处理器应用于随后创建的任何 bean。
// 简单的 {@code BeanFactory} 允许以编程方式注册后处理器，并将其应用于通过 bean 工厂创建的所有 bean。
//
// <h3>排序</h3>
// <p>在 {@code ApplicationContext} 中自动检测到的 {@code BeanPostProcessor} bean 将
// 根据 {@link org.springframework.core.PriorityOrdered} 和 {@link org.springframework.core.Ordered} 语义进行排序。
// 相反，通过 {@code BeanFactory} 以编程方式注册的 {@code BeanPostProcessor} bean 将按照注册顺序应用；
// 对于以编程方式注册的后处理器，任何通过实现 {@code PriorityOrdered} 或 {@code Ordered} 接口表达的排序语义都将被忽略。
// 此外，{@link org.springframework.core.annotation.Order @Order} 注解不适用于 {@code BeanPostProcessor} bean。
public interface BeanPostProcessor {

	/**
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	// 在任何 bean 初始化回调（例如 InitializingBean 的 {@code afterPropertiesSet} 或自定义的 init 方法）<i>之前</i>，
	// 将此 {@code BeanPostProcessor} 应用于给定的新 bean 实例。该 bean 将已填充属性值。返回的 bean 实例可能是原始 bean 的包装器。
	//
	// <p>默认实现按原样返回给定的 {@code bean}。
	// @param bean 新的 bean 实例
	// @param beanName bean 的名称
	// @return 要使用的 bean 实例，可以是原始实例，也可以是包装后的实例；如果为 {@code null}，则不会调用后续的 BeanPostProcessor
	// @throws org.springframework.beans.BeansException 以防出错
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other {@code BeanPostProcessor} callbacks.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	// 在任何 bean 初始化回调（例如 InitializingBean 的 {@code afterPropertiesSet} 或自定义的 init 方法）<i>之后</i>，
	// 将此 {@code BeanPostProcessor} 应用于给定的新 bean 实例。该 bean 将已填充属性值。返回的 bean 实例可能是原始 bean 的包装器。
	//
	// <p>对于 FactoryBean，此回调将针对 FactoryBean 实例和 FactoryBean 创建的对象调用（从 Spring 2.0 开始）。
	// 后处理器可以通过相应的 {@code bean instanceof FactoryBean} 检查来决定是应用于 FactoryBean 还是创建的对象，或者两者兼而有之。
	//
	// <p>与所有其他 {@code BeanPostProcessor} 回调不同，此回调还将在
	// 由 {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} 方法触发的短路之后调用。
	//
	// <p>默认实现按原样返回给定的 {@code bean}。
	// @param bean 新的 bean 实例
	// @param beanName bean 的名称
	// @return 要使用的 bean 实例，可以是原始的，也可以是包装的；如果为 {@code null}，则不会调用后续的 BeanPostProcessors
	// @throws org.springframework.beans.BeansException 以防出现错误
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
