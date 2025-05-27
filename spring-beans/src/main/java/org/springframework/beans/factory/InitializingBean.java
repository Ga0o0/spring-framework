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

package org.springframework.beans.factory;

/**
 * Interface to be implemented by beans that need to react once all their properties
 * have been set by a {@link BeanFactory}: e.g. to perform custom initialization,
 * or merely to check that all mandatory properties have been set.
 *
 * <p>An alternative to implementing {@code InitializingBean} is specifying a custom
 * init method, for example in an XML bean definition. For a list of all bean
 * lifecycle methods, see the {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
// 需要由 Bean 实现的接口，这些 Bean 需要在 {@link BeanFactory} 设置所有属性后做出响应：
// 例如，执行自定义初始化，或仅检查所有必需属性是否已设置。
//
// <p>实现 {@code InitializingBean} 的另一种方法是指定自定义的 init 方法，例如在 XML Bean 定义中指定。
// 有关所有 Bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。
public interface InitializingBean {

	/**
	 * Invoked by the containing {@code BeanFactory} after it has set all bean properties
	 * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
	 * <p>This method allows the bean instance to perform validation of its overall
	 * configuration and final initialization when all bean properties have been set.
	 * @throws Exception in the event of misconfiguration (such as failure to set an
	 * essential property) or if initialization fails for any other reason
	 */
	// 在设置所有 bean 属性并满足 {@link BeanFactoryAware}、{@code ApplicationContextAware} 等条件后，
	// 由包含它的 {@code BeanFactory} 调用。
	// <p>此方法允许 bean 实例在其所有 bean 属性均已设置后执行其整体配置的验证和最终初始化。
	// @throws Exception 如果配置错误（例如未能设置必要属性）或由于其他原因初始化失败
	void afterPropertiesSet() throws Exception;

}
