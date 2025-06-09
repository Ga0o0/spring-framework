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

package org.springframework.aop.framework.adapter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor that registers {@link AdvisorAdapter} beans in the BeanFactory with
 * an {@link AdvisorAdapterRegistry} (by default the {@link GlobalAdvisorAdapterRegistry}).
 *
 * <p>The only requirement for it to work is that it needs to be defined
 * in application context along with "non-native" Spring AdvisorAdapters
 * that need to be "recognized" by Spring's AOP framework.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see #setAdvisorAdapterRegistry
 * @see AdvisorAdapter
 */
// BeanPostProcessor 使用 {@link AdvisorAdapterRegistry}（默认情况下为 {@link GlobalAdvisorAdapterRegistry}）
// 在 BeanFactory 中注册 {@link AdvisorAdapter} bean。
//
// <p>要使其工作，唯一的要求是它需要在应用程序上下文中定义，同时需要与需要被 Spring 的 AOP 框架“识别”的“非原生”Spring AdvisorAdapters 一起定义。
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();


	/**
	 * Specify the AdvisorAdapterRegistry to register AdvisorAdapter beans with.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}


	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AdvisorAdapter advisorAdapter) {
			this.advisorAdapterRegistry.registerAdvisorAdapter(advisorAdapter);
		}
		return bean;
	}

}
