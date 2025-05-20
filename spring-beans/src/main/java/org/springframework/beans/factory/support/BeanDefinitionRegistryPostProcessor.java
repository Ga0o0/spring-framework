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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
// 对标准 {@link BeanFactoryPostProcessor} SPI 的扩展，
// 允许在常规 BeanFactoryPostProcessor 检测生效之前注册其他 Bean 定义。
// 具体来说，BeanDefinitionRegistryPostProcessor 可以注册其他 Bean 定义，
// 这些 Bean 定义反过来又会定义 BeanFactoryPostProcessor 实例。
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its
	 * standard initialization. All regular bean definitions will have been loaded,
	 * but no beans will have been instantiated yet. This allows for adding further
	 * bean definitions before the next post-processing phase kicks in.
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 在应用上下文的标准初始化之后，修改其内部 Bean 定义注册表。
	// 所有常规 Bean 定义都将被加载，但尚未实例化任何 Bean。
	// 这允许在下一个后处理阶段启动之前添加更多 bean 定义。
	// @param registry 应用程序上下文使用的 bean 定义注册表
	// @throws org.springframework.beans.BeansException（如果发生错误）
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

	/**
	 * Empty implementation of {@link BeanFactoryPostProcessor#postProcessBeanFactory}
	 * since custom {@code BeanDefinitionRegistryPostProcessor} implementations will
	 * typically only provide a {@link #postProcessBeanDefinitionRegistry} method.
	 * @since 6.1
	 */
	// {@link BeanFactoryPostProcessor#postProcessBeanFactory} 的空实现，
	// 因为自定义 {@code BeanDefinitionRegistryPostProcessor} 实现通常
	// 只提供一个 {@link #postProcessBeanDefinitionRegistry} 方法。
	@Override
	default void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

}
