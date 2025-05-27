/*
 * Copyright 2002-2016 the original author or authors.
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
 * Interface to be implemented by beans that want to be aware of their
 * bean name in a bean factory. Note that it is not usually recommended
 * that an object depends on its bean name, as this represents a potentially
 * brittle dependence on external configuration, as well as a possibly
 * unnecessary dependence on a Spring API.
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
// 需要由希望在 bean 工厂中获取其 bean 名称的 bean 实现的接口。
// 请注意，通常不建议对象依赖于其 bean 名称，因为这可能会导致对外部配置的脆弱依赖，以及对 Spring API 的不必要的依赖。
//
// <p>有关所有 bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。
public interface BeanNameAware extends Aware {

	/**
	 * Set the name of the bean in the bean factory that created this bean.
	 * <p>Invoked after population of normal bean properties but before an
	 * init callback such as {@link InitializingBean#afterPropertiesSet()}
	 * or a custom init-method.
	 * @param name the name of the bean in the factory.
	 * Note that this name is the actual bean name used in the factory, which may
	 * differ from the originally specified name: in particular for inner bean
	 * names, the actual bean name might have been made unique through appending
	 * "#..." suffixes. Use the {@link BeanFactoryUtils#originalBeanName(String)}
	 * method to extract the original bean name (without suffix), if desired.
	 */
	// 设置创建此 bean 的 bean 工厂中 bean 的名称。
	// <p>在填充常规 bean 属性之后、初始化回调（例如 {@link InitializingBean#afterPropertiesSet()} 或自定义 init 方法）之前调用。
	// @param name 工厂中 bean 的名称。
	// 请注意，此名称是工厂中使用的实际 bean 名称，可能与最初指定的名称不同：特别是对于内部 bean 名称，实际 bean 名称可能通过附加“#...”后缀使其唯一。
	// 如果需要，可以使用 {@link BeanFactoryUtils#originalBeanName(String)} 方法提取原始 bean 名称（不带后缀）。
	void setBeanName(String name);

}
