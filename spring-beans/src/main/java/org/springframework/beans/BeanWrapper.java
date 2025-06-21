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

package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 *
 * <p>Typically not used directly but rather implicitly via a
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>Provides operations to analyze and manipulate standard JavaBeans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, and query the readability/writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 *
 * <p>A BeanWrapper's default for the "extractOldValueForEditor" setting
 * is "false", to avoid side effects caused by getter method invocations.
 * Turn this to "true" to expose present property values to custom editors.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
// Spring 底层 JavaBeans 基础架构的核心接口。
//
// <p>通常不直接使用，而是通过 {@link org.springframework.beans.factory.BeanFactory}
// 或 {@link org.springframework.validation.DataBinder} 隐式使用。
//
// <p>提供分析和操作标准 JavaBean 的操作：获取和设置属性值（单个或批量）、获取属性描述符以及查询属性的可读性/可写性。
//
// <p>此接口支持<b>嵌套属性</b>，从而可以设置无限深度的子属性。
//
// <p>BeanWrapper 的 “extractOldValueForEditor” 默认设置为 “false”，以避免调用 getter 方法时产生的副作用。
// 将其设置为 “true” 即可将当前属性值暴露给自定义编辑器。
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	/**
	 * Specify a limit for array and collection auto-growing.
	 * <p>Default is unlimited on a plain BeanWrapper.
	 * @since 4.1
	 */
	// 指定数组和集合自动增长的限制。
	// <p>对于普通的 BeanWrapper，默认值为无限制。
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	/**
	 * Return the limit for array and collection auto-growing.
	 * @since 4.1
	 */
	// 返回数组和集合自动增长的限制。
	int getAutoGrowCollectionLimit();

	/**
	 * Return the bean instance wrapped by this object.
	 */
	// 返回此对象包装的 Bean 实例。
	Object getWrappedInstance();

	/**
	 * Return the type of the wrapped bean instance.
	 */
	// 返回被包装的 bean 实例的类型。
	Class<?> getWrappedClass();

	/**
	 * Obtain the PropertyDescriptors for the wrapped object
	 * (as determined by standard JavaBeans introspection).
	 * @return the PropertyDescriptors for the wrapped object
	 */
	// 获取被包装对象的 PropertyDescriptors（由标准 JavaBeans 自省确定）。
	// @return 被包装对象的 PropertyDescriptors
	PropertyDescriptor[] getPropertyDescriptors();

	/**
	 * Obtain the property descriptor for a specific property
	 * of the wrapped object.
	 * @param propertyName the property to obtain the descriptor for
	 * (may be a nested path, but not an indexed/mapped property)
	 * @return the property descriptor for the specified property
	 * @throws InvalidPropertyException if there is no such property
	 */
	// 获取包装对象特定属性的属性描述符。
	// @param propertyName 要获取描述符的属性（可以是嵌套路径，但不能是索引/映射属性）
	// @return 指定属性的属性描述符
	// @throws InvalidPropertyException（如果不存在该属性）
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
