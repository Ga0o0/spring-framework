/*
 * Copyright 2002-2013 the original author or authors.
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

/**
 * Simple implementation of the {@link TypeConverter} interface that does not operate on
 * a specific target object. This is an alternative to using a full-blown BeanWrapperImpl
 * instance for arbitrary type conversion needs, while using the very same conversion
 * algorithm (including delegation to {@link java.beans.PropertyEditor} and
 * {@link org.springframework.core.convert.ConversionService}) underneath.
 *
 * <p><b>Note:</b> Due to its reliance on {@link java.beans.PropertyEditor PropertyEditors},
 * SimpleTypeConverter is <em>not</em> thread-safe. Use a separate instance for each thread.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapperImpl
 */
// SimpleTypeConverter 是 {@link TypeConverter} 接口的一个简单实现，它不针对特定目标对象进行操作。
// 对于任意类型转换需求，它提供了一种替代方案，无需使用功能齐全的 BeanWrapperImpl 实例，
// 同时底层使用相同的转换算法（包括委托给 {@link java.beans.PropertyEditor} 和
// {@link org.springframework.core.convert.ConversionService}）。
//
// <p><b>注意：</b>由于 SimpleTypeConverter 依赖于 {@link java.beans.PropertyEditor PropertyEditors}，
// 因此它不是线程安全的。请为每个线程使用单独的实例。
public class SimpleTypeConverter extends TypeConverterSupport {

	public SimpleTypeConverter() {
		this.typeConverterDelegate = new TypeConverterDelegate(this);
		registerDefaultEditors();
	}

}
