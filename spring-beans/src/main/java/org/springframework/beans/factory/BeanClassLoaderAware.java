/*
 * Copyright 2002-2017 the original author or authors.
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
 * Callback that allows a bean to be aware of the bean
 * {@link ClassLoader class loader}; that is, the class loader used by the
 * present bean factory to load bean classes.
 *
 * <p>This is mainly intended to be implemented by framework classes which
 * have to pick up application classes by name despite themselves potentially
 * being loaded from a shared class loader.
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.0
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
// 回调函数允许 Bean 感知其所属的 Bean {@link ClassLoader 类加载器}；即当前 Bean 工厂用来加载 Bean 类的类加载器。
//
// <p>该函数主要由框架类实现，这些框架类必须根据名称获取应用程序类，尽管它们本身可能由共享类加载器加载。
//
// <p>有关所有 Bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。
public interface BeanClassLoaderAware extends Aware {

	/**
	 * Callback that supplies the bean {@link ClassLoader class loader} to
	 * a bean instance.
	 * <p>Invoked <i>after</i> the population of normal bean properties but
	 * <i>before</i> an initialization callback such as
	 * {@link InitializingBean InitializingBean's}
	 * {@link InitializingBean#afterPropertiesSet()}
	 * method or a custom init-method.
	 * @param classLoader the owning class loader
	 */
	// 将 Bean {@link ClassLoader 类加载器} 提供给 Bean 实例的回调函数。
	// <p>在 Bean 常规属性填充<i>之后</i>、初始化回调（例如 {@link InitializingBean InitializingBean 的}
	// {@link InitializingBean#afterPropertiesSet()} 方法或自定义的 init 方法）<i>之前</i>调用。
	// @param classLoader 所属的类加载器
	void setBeanClassLoader(ClassLoader classLoader);

}
