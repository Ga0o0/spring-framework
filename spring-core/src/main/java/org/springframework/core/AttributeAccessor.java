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

package org.springframework.core;

import java.util.function.Function;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Interface defining a generic contract for attaching and accessing metadata
 * to/from arbitrary objects.
 *
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 2.0
 */
// 定义通用契约的接口，用于将元数据附加到任意对象并从任意对象访问元数据。
public interface AttributeAccessor {

	/**
	 * Set the attribute defined by {@code name} to the supplied {@code value}.
	 * <p>If {@code value} is {@code null}, the attribute is {@link #removeAttribute removed}.
	 * <p>In general, users should take care to prevent overlaps with other
	 * metadata attributes by using fully-qualified names, perhaps using
	 * class or package names as prefix.
	 * @param name the unique attribute key
	 * @param value the attribute value to be attached
	 */
	// 将 {@code name} 定义的属性设置为提供的 {@code value}。
	// <p>如果 {@code value} 为 {@code null}，则该属性被 {@link #removeAttribute 移除}。
	// <p>通常，用户应注意避免与其他元数据属性重叠，请使用完全限定名称，例如使用类名或包名作为前缀。
	// @param name 唯一属性键
	// @param value 要附加的属性值
	void setAttribute(String name, @Nullable Object value);

	/**
	 * Get the value of the attribute identified by {@code name}.
	 * <p>Return {@code null} if the attribute doesn't exist.
	 * @param name the unique attribute key
	 * @return the current value of the attribute, if any
	 */
	// 获取 {@code name} 标识的属性值。
	// <p>如果属性不存在，则返回 {@code null}。
	// @param name 唯一属性键
	// @return 属性的当前值（如果有）
	@Nullable
	Object getAttribute(String name);

	/**
	 * Compute a new value for the attribute identified by {@code name} if
	 * necessary and {@linkplain #setAttribute set} the new value in this
	 * {@code AttributeAccessor}.
	 * <p>If a value for the attribute identified by {@code name} already exists
	 * in this {@code AttributeAccessor}, the existing value will be returned
	 * without applying the supplied compute function.
	 * <p>The default implementation of this method is not thread safe but can
	 * be overridden by concrete implementations of this interface.
	 * @param <T> the type of the attribute value
	 * @param name the unique attribute key
	 * @param computeFunction a function that computes a new value for the attribute
	 * name; the function must not return a {@code null} value
	 * @return the existing value or newly computed value for the named attribute
	 * @since 5.3.3
	 * @see #getAttribute(String)
	 * @see #setAttribute(String, Object)
	 */
	// 如果需要，为 {@code name} 标识的属性计算新值，并 {@linkplain #setAttribute set} 此 {@code AttributeAccessor} 中的新值。
	// <p>如果此 {@code AttributeAccessor} 中已存在 {@code name} 标识的属性值，则将返回现有值，而不应用提供的计算函数。
	// <p>此方法的默认实现不是线程安全的，但可以被此接口的具体实现覆盖。
	// @param <T> 属性值的类型
	// @param name 唯一属性键
	// @param computeFunction 为属性名称计算新值的函数；该函数不得返回 null 值
	// @return 返回指定属性的现有值或新计算的值
	@SuppressWarnings("unchecked")
	default <T> T computeAttribute(String name, Function<String, T> computeFunction) {
		Assert.notNull(name, "Name must not be null");
		Assert.notNull(computeFunction, "Compute function must not be null");
		Object value = getAttribute(name);
		if (value == null) {
			value = computeFunction.apply(name);
			Assert.state(value != null,
					() -> String.format("Compute function must not return null for attribute named '%s'", name));
			setAttribute(name, value);
		}
		return (T) value;
	}

	/**
	 * Remove the attribute identified by {@code name} and return its value.
	 * <p>Return {@code null} if no attribute under {@code name} is found.
	 * @param name the unique attribute key
	 * @return the last value of the attribute, if any
	 */
	// 移除由 {@code name} 标识的属性并返回其值。
	// <p>如果 {@code name} 下未找到任何属性，则返回 {@code null}。
	// @param name 唯一属性键
	// @return 属性的最后一个值（如果有）
	@Nullable
	Object removeAttribute(String name);

	/**
	 * Return {@code true} if the attribute identified by {@code name} exists.
	 * <p>Otherwise return {@code false}.
	 * @param name the unique attribute key
	 */
	// 如果由 {@code name} 标识的属性存在，则返回 {@code true}。
	// <p>否则返回 {@code false}。
	// @param name 唯一属性键
	boolean hasAttribute(String name);

	/**
	 * Return the names of all attributes.
	 */
	// 返回所有属性的名称。
	String[] attributeNames();

}
