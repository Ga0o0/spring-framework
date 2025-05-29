/*
 * Copyright 2002-2021 the original author or authors.
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
import org.springframework.beans.PropertyValues;
import org.springframework.lang.Nullable;

/**
 * Subinterface of {@link BeanPostProcessor} that adds a before-instantiation callback,
 * and a callback after instantiation but before explicit properties are set or
 * autowiring occurs.
 *
 * <p>Typically used to suppress default instantiation for specific target beans,
 * for example to create proxies with special TargetSources (pooling targets,
 * lazily initializing targets, etc), or to implement additional injection strategies
 * such as field injection.
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. It is recommended to implement the plain
 * {@link BeanPostProcessor} interface as far as possible.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.2
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator
 */
// {@link BeanPostProcessor} 的子接口，添加了一个实例化前回调，以及一个实例化后但在设置显式属性或自动装配发生之前的回调。
//
// <p>通常用于抑制特定目标 Bean 的默认实例化，例如，使用特殊的 TargetSource（池化目标、延迟初始化目标等）创建代理，或实现其他注入策略，例如字段注入。
//
// <p><b>注意：</b>此接口是特殊用途的接口，主要用于框架内部使用。建议尽可能实现普通的 {@link BeanPostProcessor} 接口。
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor <i>before the target bean gets instantiated</i>.
	 * The returned bean object may be a proxy to use instead of the target bean,
	 * effectively suppressing default instantiation of the target bean.
	 * <p>If a non-null object is returned by this method, the bean creation process
	 * will be short-circuited. The only further processing applied is the
	 * {@link #postProcessAfterInitialization} callback from the configured
	 * {@link BeanPostProcessor BeanPostProcessors}.
	 * <p>This callback will be applied to bean definitions with their bean class,
	 * as well as to factory-method definitions in which case the returned bean type
	 * will be passed in here.
	 * <p>Post-processors may implement the extended
	 * {@link SmartInstantiationAwareBeanPostProcessor} interface in order
	 * to predict the type of the bean object that they are going to return here.
	 * <p>The default implementation returns {@code null}.
	 * @param beanClass the class of the bean to be instantiated
	 * @param beanName the name of the bean
	 * @return the bean object to expose instead of a default instance of the target bean,
	 * or {@code null} to proceed with default instantiation
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see #postProcessAfterInstantiation
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getBeanClass()
	 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getFactoryMethodName()
	 */
	// 在<i>目标 bean 实例化</i>之前应用此 BeanPostProcessor。
	// 返回的 bean 对象可以作为目标 bean 的代理使用，从而有效地抑制目标 bean 的默认实例化。
	// <p>如果此方法返回非空对象，则 bean 创建过程将被短路。
	// 唯一需要进一步处理的是来自已配置的 {@link BeanPostProcessor BeanPostProcessors} 的 {@link #postProcessAfterInitialization} 回调。
	// <p>此回调将应用于 bean 定义及其 bean 类，以及工厂方法定义，在这种情况下，返回的 bean 类型将在此处传递。
	// <p>后处理器可以实现扩展的 {@link SmartInstantiationAwareBeanPostProcessor} 接口，以便预测它们将在此处返回的 bean 对象的类型。
	// <p>默认实现返回 {@code null}。
	// @param beanClass 要实例化的 bean 的类
	// @param beanName bean 的名称
	// @return 要公开的 bean 对象，而不是目标 bean 的默认实例，或者 {@code null} 继续进行默认实例化
	// @throws org.springframework.beans.BeansException（如果发生错误）
	@Nullable
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * Perform operations after the bean has been instantiated, via a constructor or factory method,
	 * but before Spring property population (from explicit properties or autowiring) occurs.
	 * <p>This is the ideal callback for performing custom field injection on the given bean
	 * instance, right before Spring's autowiring kicks in.
	 * <p>The default implementation returns {@code true}.
	 * @param bean the bean instance created, with properties not having been set yet
	 * @param beanName the name of the bean
	 * @return {@code true} if properties should be set on the bean; {@code false}
	 * if property population should be skipped. Normal implementations should return {@code true}.
	 * Returning {@code false} will also prevent any subsequent InstantiationAwareBeanPostProcessor
	 * instances being invoked on this bean instance.
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see #postProcessBeforeInstantiation
	 */
	// 通过构造函数或工厂方法实例化 bean 之后，但在 Spring 属性填充（来自显式属性或自动装配）之前执行操作。
	// <p>这是在给定 bean 实例上执行自定义字段注入的理想回调，就在 Spring 自动装配开始之前。
	// <p>默认实现返回 {@code true}。
	// @param bean 创建的 bean 实例，属性尚未设置
	// @param beanName bean 的名称
	// @return {@code true} 如果应在 bean 上设置属性；{@code false} 如果应跳过属性填充。正常实现应返回 {@code true}。
	// 返回 {@code false} 还将阻止在此 bean 实例上调用任何后续的 InstantiationAwareBeanPostProcessor 实例。
	// @throws org.springframework.beans.BeansException
	// @see #postProcessBeforeInstantiation
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	/**
	 * Post-process the given property values before the factory applies them
	 * to the given bean.
	 * <p>The default implementation returns the given {@code pvs} as-is.
	 * @param pvs the property values that the factory is about to apply (never {@code null})
	 * @param bean the bean instance created, but whose properties have not yet been set
	 * @param beanName the name of the bean
	 * @return the actual property values to apply to the given bean (can be the passed-in
	 * PropertyValues instance), or {@code null} to skip property population
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @since 5.1
	 */
	// 在工厂将给定的属性值应用于给定的 bean 之前，对其进行后处理。
	// <p>默认实现按原样返回给定的 {@code pvs}。
	// @param pvs 工厂即将应用的属性值（从不 {@code null}）
	// @param bean 已创建的 bean 实例，但其属性尚未设置
	// @param beanName bean 的名称
	// @return 要应用于给定 bean 的实际属性值（可以是传入的 PropertyValues 实例），或 {@code null} 跳过属性填充
	// @throws org.springframework.beans.BeansException 出现错误时
	// @since 5.1
	@Nullable
	default PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
			throws BeansException {

		return pvs;
	}

}
