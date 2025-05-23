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

package org.springframework.core.env;

import org.springframework.lang.Nullable;

/**
 * Interface for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
// 用于解析任何底层源的属性的接口。
public interface PropertyResolver {

	/**
	 * Return whether the given property key is available for resolution,
	 * i.e. if the value for the given key is not {@code null}.
	 */
	// 返回给定的属性键是否可供解析，即给定键的值是否不为 {@code null}。
	boolean containsProperty(String key);

	/**
	 * Return the property value associated with the given key,
	 * or {@code null} if the key cannot be resolved.
	 * @param key the property name to resolve
	 * @see #getProperty(String, String)
	 * @see #getProperty(String, Class)
	 * @see #getRequiredProperty(String)
	 */
	// 返回与给定键关联的属性值，如果键无法解析，则返回 {@code null}。
	// @param key 需要解析的属性名称
	@Nullable
	String getProperty(String key);

	/**
	 * Return the property value associated with the given key, or
	 * {@code defaultValue} if the key cannot be resolved.
	 * @param key the property name to resolve
	 * @param defaultValue the default value to return if no value is found
	 * @see #getRequiredProperty(String)
	 * @see #getProperty(String, Class)
	 */
	// 返回与给定键关联的属性值，如果无法解析该键，则返回 {@code defaultValue}。
	// @param key 需要解析的属性名称
	// @param defaultValue 未找到值时返回的默认值
	String getProperty(String key, String defaultValue);

	/**
	 * Return the property value associated with the given key,
	 * or {@code null} if the key cannot be resolved.
	 * @param key the property name to resolve
	 * @param targetType the expected type of the property value
	 * @see #getRequiredProperty(String, Class)
	 */
	// 返回与给定键关联的属性值，如果无法解析该键，则返回 {@code null}。
	// @param key 需要解析的属性名称
	// @param targetType 属性值的预期类型
	@Nullable
	<T> T getProperty(String key, Class<T> targetType);

	/**
	 * Return the property value associated with the given key,
	 * or {@code defaultValue} if the key cannot be resolved.
	 * @param key the property name to resolve
	 * @param targetType the expected type of the property value
	 * @param defaultValue the default value to return if no value is found
	 * @see #getRequiredProperty(String, Class)
	 */
	// 返回与给定键关联的属性值，如果无法解析该键，则返回 {@code defaultValue}。
	// @param key 需要解析的属性名称
	// @param targetType 属性值的预期类型
	// @param defaultValue 未找到值时返回的默认值
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);

	/**
	 * Return the property value associated with the given key (never {@code null}).
	 * @throws IllegalStateException if the key cannot be resolved
	 * @see #getRequiredProperty(String, Class)
	 */
	// 返回与给定键关联的属性值（永远不会返回 null）。
	// @throws IllegalStateException 如果无法解析键，则抛出 IllegalStateException
	String getRequiredProperty(String key) throws IllegalStateException;

	/**
	 * Return the property value associated with the given key, converted to the given
	 * targetType (never {@code null}).
	 * @throws IllegalStateException if the given key cannot be resolved
	 */
	// 返回与给定键关联的属性值，并转换为给定的 targetType（永远不会为 null）。
	// @throws IllegalStateException 如果无法解析给定的键，则抛出 IllegalStateException
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

	/**
	 * Resolve ${...} placeholders in the given text, replacing them with corresponding
	 * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
	 * no default value are ignored and passed through unchanged.
	 * @param text the String to resolve
	 * @return the resolved String (never {@code null})
	 * @throws IllegalArgumentException if given text is {@code null}
	 * @see #resolveRequiredPlaceholders
	 */
	// 解析给定文本中的 ${...} 占位符，并将其替换为由 {@link #getProperty} 解析的相应属性值。
	// 没有默认值的无法解析的占位符将被忽略，并保持不变。
	// @param text 待解析的字符串
	// @return 解析后的字符串（永不为 null）
	// @throws 如果给定的文本为 null，则抛出 IllegalArgumentException
	String resolvePlaceholders(String text);

	/**
	 * Resolve ${...} placeholders in the given text, replacing them with corresponding
	 * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
	 * no default value will cause an IllegalArgumentException to be thrown.
	 * @return the resolved String (never {@code null})
	 * @throws IllegalArgumentException if given text is {@code null}
	 * or if any placeholders are unresolvable
	 */
	// 解析给定文本中的 ${...} 占位符，并将其替换为通过 {@link #getProperty} 解析的相应属性值。
	// 无法解析且没有默认值的占位符将引发 IllegalArgumentException 异常。
	// @return 解析后的字符串（永不为 null）
	// @throws IllegalArgumentException，如果给定文本为 null 或任何占位符无法解析
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
