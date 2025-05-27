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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * Simple interface for bean definition readers that specifies load methods with
 * {@link Resource} and {@link String} location parameters.
 *
 * <p>Concrete bean definition readers can of course add additional
 * load and register methods for bean definitions, specific to
 * their bean definition format.
 *
 * <p>Note that a bean definition reader does not have to implement
 * this interface. It only serves as a suggestion for bean definition
 * readers that want to follow standard naming conventions.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.core.io.Resource
 */
// 一个用于 Bean 定义读取器的简单接口，使用 {@link Resource} 和 {@link String} 位置参数指定加载方法。
//
// <p>具体的 Bean 定义读取器当然可以根据其 Bean 定义格式，为 Bean 定义添加额外的加载和注册方法。
//
// <p>请注意，Bean 定义读取器不必实现此接口。它仅作为对希望遵循标准命名约定的 Bean 定义读取器的建议。
public interface BeanDefinitionReader {

	/**
	 * Return the bean factory to register the bean definitions with.
	 * <p>The factory is exposed through the {@link BeanDefinitionRegistry} interface,
	 * encapsulating the methods that are relevant for bean definition handling.
	 */
	//* 返回用于注册 bean 定义的 bean 工厂。
	//* <p>该工厂通过 {@link BeanDefinitionRegistry} 接口公开，封装了与 bean 定义处理相关的方法。
	BeanDefinitionRegistry getRegistry();

	/**
	 * Return the {@link ResourceLoader} to use for resource locations.
	 * <p>Can be checked for the {@code ResourcePatternResolver} interface and cast
	 * accordingly, for loading multiple resources for a given resource pattern.
	 * <p>A {@code null} return value suggests that absolute resource loading
	 * is not available for this bean definition reader.
	 * <p>This is mainly meant to be used for importing further resources
	 * from within a bean definition resource, for example via the "import"
	 * tag in XML bean definitions. It is recommended, however, to apply
	 * such imports relative to the defining resource; only explicit full
	 * resource locations will trigger absolute path based resource loading.
	 * <p>There is also a {@code loadBeanDefinitions(String)} method available,
	 * for loading bean definitions from a resource location (or location pattern).
	 * This is a convenience to avoid explicit {@code ResourceLoader} handling.
	 * @see #loadBeanDefinitions(String)
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 */
	// 返回用于资源位置的 {@link ResourceLoader}。
	// <p>可以检查 {@code ResourcePatternResolver} 接口并进行相应转换，以便为给定的资源模式加载多个资源。
	// <p>返回值 {@code null} 表示此 bean 定义读取器无法使用绝对资源加载。
	// <p>这主要用于从 bean 定义资源内部导入更多资源，例如通过 XML bean 定义中的“import”标签。但是，建议相对于定义资源应用此类导入；只有明确的完整资源位置才会触发基于绝对路径的资源加载。
	// <p>还有一个 {@code loadBeanDefinitions(String)} 方法可用，用于从资源位置（或位置模式）加载 bean 定义。这可以方便地避免显式处理 {@code ResourceLoader}。
	@Nullable
	ResourceLoader getResourceLoader();

	/**
	 * Return the class loader to use for bean classes.
	 * <p>{@code null} suggests to not load bean classes eagerly
	 * but rather to just register bean definitions with class names,
	 * with the corresponding classes to be resolved later (or never).
	 */
	// 返回用于 bean 类的类加载器。
	// <p>{@code null} 建议不要立即加载 bean 类，而是仅使用类名注册 bean 定义，相应的类稍后再解析（或从不解析）。
	@Nullable
	ClassLoader getBeanClassLoader();

	/**
	 * Return the {@link BeanNameGenerator} to use for anonymous beans
	 * (without explicit bean name specified).
	 */
	// 返回用于匿名 bean（未指定显式 bean 名称）的 {@link BeanNameGenerator}。
	BeanNameGenerator getBeanNameGenerator();


	/**
	 * Load bean definitions from the specified resource.
	 * @param resource the resource descriptor
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	// 从指定资源加载 bean 定义。
	// @param resource 资源描述符
	// @return 找到的 bean 定义数量
	// @throws 加载或解析错误时抛出 BeanDefinitionStoreException
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resources.
	 * @param resources the resource descriptors
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resource location.
	 * <p>The location can also be a location pattern, provided that the
	 * {@link ResourceLoader} of this bean definition reader is a
	 * {@code ResourcePatternResolver}.
	 * @param location the resource location, to be loaded with the {@code ResourceLoader}
	 * (or {@code ResourcePatternResolver}) of this bean definition reader
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 * @see #getResourceLoader()
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource)
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource[])
	 */
	// 从指定的资源位置加载 bean 定义。<p>该位置也可以是位置模式，前提是此 bean 定义读取器的 {@link ResourceLoader} 是 {@code ResourcePatternResolver}。
	// @param location 资源位置，将使用此 bean 定义读取器的 {@code ResourceLoader}（或 {@code ResourcePatternResolver}）加载
	// @return 找到的 bean 定义的数量
	// @throws BeanDefinitionStoreException（如果发生加载或解析错误）
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	/**
	 * Load bean definitions from the specified resource locations.
	 * @param locations the resource locations, to be loaded with the {@code ResourceLoader}
	 * (or {@code ResourcePatternResolver}) of this bean definition reader
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	// 从指定的资源位置加载 bean 定义。
	// @param location 资源位置，使用这个 bean 定义读取器的 {@code ResourceLoader} （或 {@code ResourcePatternResolver}）加载
	// @return 找到的 bean 定义的数量
	// @throws BeanDefinitionStoreException 如果出现加载或解析错误
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
