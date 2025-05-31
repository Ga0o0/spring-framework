/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.context.annotation;

import java.util.function.Predicate;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by types that determine which @{@link Configuration}
 * class(es) should be imported based on a given selection criteria, usually one or
 * more annotation attributes.
 *
 * <p>An {@link ImportSelector} may implement any of the following
 * {@link org.springframework.beans.factory.Aware Aware} interfaces,
 * and their respective methods will be called prior to {@link #selectImports}:
 * <ul>
 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}</li>
 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}</li>
 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}</li>
 * </ul>
 *
 * <p>Alternatively, the class may provide a single constructor with one or more of
 * the following supported parameter types:
 * <ul>
 * <li>{@link org.springframework.core.env.Environment Environment}</li>
 * <li>{@link org.springframework.beans.factory.BeanFactory BeanFactory}</li>
 * <li>{@link java.lang.ClassLoader ClassLoader}</li>
 * <li>{@link org.springframework.core.io.ResourceLoader ResourceLoader}</li>
 * </ul>
 *
 * <p>{@code ImportSelector} implementations are usually processed in the same way
 * as regular {@code @Import} annotations, however, it is also possible to defer
 * selection of imports until all {@code @Configuration} classes have been processed
 * (see {@link DeferredImportSelector} for details).
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see DeferredImportSelector
 * @see Import
 * @see ImportBeanDefinitionRegistrar
 * @see Configuration
 */
// 由确定应根据给定的选择标准（通常是一个或多个注释属性）导入哪个 @{@link Configuration} 类的类型实现的接口。
//
// <p>{@link ImportSelector} 可以实现以下任何 {@link org.springframework.beans.factory.Aware Aware} 接口，
// 并且它们各自的方法将在 {@link #selectImports} 之前调用：
//
// <ul>
// <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware</li>
// <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware</li>
// <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware</li>
// <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware</li>
// </ul>
//
// <p>或者，该类可以提供具有以下受支持一种或多种参数类型的单个构造函数：
// <ul>
// <li>{@link org.springframework.core.env.Environment </li>
// <li>{@link org.springframework.beans.factory.BeanFactory BeanFactory </li>
// <li>{@link java.lang.ClassLoader ClassLoader </li>
// <li>{@link org.springframework.core.io.ResourceLoader ResourceLoader </li>
// </ul>
//
// <p>{@code ImportSelector} 实现通常以与常规 {@code @Import} 注释相同的方式处理，
// 但是，也可以推迟导入选择，直到所有 {@code @Configuration} 类都已处理完毕（有关详细信息，请参阅 {@link DeferredImportSelector}）。
public interface ImportSelector {

	/**
	 * Select and return the names of which class(es) should be imported based on
	 * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
	 * @return the class names, or an empty array if none
	 */
	// 根据导入 @{@link Configuration} 类的 {@link AnnotationMetadata}，选择并返回应导入的类的名称。
	// @return 类名，如果没有，则返回空数组。
	String[] selectImports(AnnotationMetadata importingClassMetadata);

	/**
	 * Return a predicate for excluding classes from the import candidates, to be
	 * transitively applied to all classes found through this selector's imports.
	 * <p>If this predicate returns {@code true} for a given fully-qualified
	 * class name, said class will not be considered as an imported configuration
	 * class, bypassing class file loading as well as metadata introspection.
	 * @return the filter predicate for fully-qualified candidate class names
	 * of transitively imported configuration classes, or {@code null} if none
	 * @since 5.2.4
	 */
	// 返回一个用于从导入候选中排除类的谓词，该谓词可传递地应用于通过此选择器的导入找到的所有类。
	// <p>如果此谓词对给定的完全限定类名返回 {@code true}，则该类将不会被视为导入的配置类，从而绕过类文件加载和元数据自检。
	// @return 传递导入的配置类的完全限定候选类名的过滤谓词，如果没有则返回 {@code null}
	@Nullable
	default Predicate<String> getExclusionFilter() {
		return null;
	}

}
