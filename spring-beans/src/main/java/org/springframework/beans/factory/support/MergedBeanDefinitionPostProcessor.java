/*
 * Copyright 2002-2018 the original author or authors.
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

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Post-processor callback interface for <i>merged</i> bean definitions at runtime.
 * {@link BeanPostProcessor} implementations may implement this sub-interface in order
 * to post-process the merged bean definition (a processed copy of the original bean
 * definition) that the Spring {@code BeanFactory} uses to create a bean instance.
 *
 * <p>The {@link #postProcessMergedBeanDefinition} method may for example introspect
 * the bean definition in order to prepare some cached metadata before post-processing
 * actual instances of a bean. It is also allowed to modify the bean definition but
 * <i>only</i> for definition properties which are actually intended for concurrent
 * modification. Essentially, this only applies to operations defined on the
 * {@link RootBeanDefinition} itself but not to the properties of its base classes.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#getMergedBeanDefinition
 */
// 用于在运行时<i>合并</i> bean 定义的后处理器回调接口。
// {@link BeanPostProcessor} 实现可以实现此子接口，以便对合并的 bean 定义（原始 bean 定义的处理后副本）进行后处理，
// Spring {@code BeanFactory} 会使用该定义创建 bean 实例。
//
// <p>例如，{@link #postProcessMergedBeanDefinition} 方法可以自省 bean 定义，以便在对 bean 的实际实例进行后处理之前准备一些缓存的元数据。
// 它还允许修改 bean 定义，但<i>仅限于</i>那些实际上需要并发修改的定义属性。
// 本质上，这仅适用于在 {@link RootBeanDefinition} 本身上定义的操作，而不适用于其基类的属性。
public interface MergedBeanDefinitionPostProcessor extends BeanPostProcessor {

	/**
	 * Post-process the given merged bean definition for the specified bean.
	 * @param beanDefinition the merged bean definition for the bean
	 * @param beanType the actual type of the managed bean instance
	 * @param beanName the name of the bean
	 * @see AbstractAutowireCapableBeanFactory#applyMergedBeanDefinitionPostProcessors
	 */
	// 对指定 bean 的合并 bean 定义进行后处理。
	// @param beanDefinition bean 的合并 bean 定义
	// @param beanType 托管 bean 实例的实际类型
	// @param beanName bean 的名称
	void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName);

	/**
	 * A notification that the bean definition for the specified name has been reset,
	 * and that this post-processor should clear any metadata for the affected bean.
	 * <p>The default implementation is empty.
	 * @param beanName the name of the bean
	 * @since 5.1
	 * @see DefaultListableBeanFactory#resetBeanDefinition
	 */
	// 指定名称的 bean 定义已被重置的通知，并且此后处理器应清除受影响 bean 的所有元数据。
	// <p>默认实现为空。
	// @param beanName bean 的名称
	default void resetBeanDefinition(String beanName) {
	}

}
