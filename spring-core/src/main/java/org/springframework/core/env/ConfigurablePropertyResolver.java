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

package org.springframework.core.env;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.lang.Nullable;

/**
 * Configuration interface to be implemented by most if not all {@link PropertyResolver}
 * types. Provides facilities for accessing and customizing the
 * {@link org.springframework.core.convert.ConversionService ConversionService}
 * used when converting property values from one type to another.
 *
 * @author Chris Beams
 * @since 3.1
 */
// 大多数（如果不是全部）{@link PropertyResolver} 类型都需要实现的配置接口。
// 它提供访问和自定义 {@link org.springframework.core.convert.ConversionService ConversionService} 的功能，
// 用于将属性值从一种类型转换为另一种类型。
public interface ConfigurablePropertyResolver extends PropertyResolver {

	/**
	 * Return the {@link ConfigurableConversionService} used when performing type
	 * conversions on properties.
	 * <p>The configurable nature of the returned conversion service allows for
	 * the convenient addition and removal of individual {@code Converter} instances:
	 * <pre class="code">
	 * ConfigurableConversionService cs = env.getConversionService();
	 * cs.addConverter(new FooConverter());
	 * </pre>
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	// 返回对属性执行类型转换时使用的 {@link ConfigurableConversionService}。
	// <p>返回的转换服务的可配置特性允许方便地添加和删除单个 {@code Converter} 实例：
	// <pre class="code">
	// 		ConfigurableConversionService cs = env.getConversionService();
	// 		cs.addConverter(new FooConverter());
	// </pre>
	ConfigurableConversionService getConversionService();

	/**
	 * Set the {@link ConfigurableConversionService} to be used when performing type
	 * conversions on properties.
	 * <p><strong>Note:</strong> as an alternative to fully replacing the
	 * {@code ConversionService}, consider adding or removing individual
	 * {@code Converter} instances by drilling into {@link #getConversionService()}
	 * and calling methods such as {@code #addConverter}.
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see #getConversionService()
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	// 设置对属性执行类型转换时使用的 {@link ConfigurableConversionService}。
	// <p><strong>注意：</strong>除了完全替换 {@code ConversionService}，
	// 还可以考虑通过深入研究 {@link #getConversionService()}
	// 并调用 {@code #addConverter} 等方法来添加或删除单个 {@code Converter} 实例。
	void setConversionService(ConfigurableConversionService conversionService);

	/**
	 * Set the prefix that placeholders replaced by this resolver must begin with.
	 */
	// 设置此解析器替换的占位符必须以其开头的前缀。
	void setPlaceholderPrefix(String placeholderPrefix);

	/**
	 * Set the suffix that placeholders replaced by this resolver must end with.
	 */
	// 设置此解析器替换的占位符必须以此后缀结尾。
	void setPlaceholderSuffix(String placeholderSuffix);

	/**
	 * Specify the separating character between the placeholders replaced by this
	 * resolver and their associated default value, or {@code null} if no such
	 * special character should be processed as a value separator.
	 */
	// 指定此解析器替换的占位符与其关联的默认值之间的分隔符，如果不存在应作为值分隔符处理的特殊字符，则返回 {@code null}。
	void setValueSeparator(@Nullable String valueSeparator);

	/**
	 * Set whether to throw an exception when encountering an unresolvable placeholder
	 * nested within the value of a given property. A {@code false} value indicates strict
	 * resolution, i.e. that an exception will be thrown. A {@code true} value indicates
	 * that unresolvable nested placeholders should be passed through in their unresolved
	 * ${...} form.
	 * <p>Implementations of {@link #getProperty(String)} and its variants must inspect
	 * the value set here to determine correct behavior when property values contain
	 * unresolvable placeholders.
	 * @since 3.2
	 */
	// 设置当给定属性的值中嵌套了无法解析的占位符时是否抛出异常。{@code false} 值表示严格解析，即会抛出异常。
	// {@code true} 值表示无法解析的嵌套占位符应以其未解析的 ${...} 形式传递。
	// <p>{@link #getProperty(String)} 及其变体的实现必须检查此处设置的值，以确定当属性值包含无法解析的占位符时是否采取正确的行为。
	void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

	/**
	 * Specify which properties must be present, to be verified by
	 * {@link #validateRequiredProperties()}.
	 */
	// 指定必须存在的属性，并通过 {@link #validateRequiredProperties()} 进行验证。
	void setRequiredProperties(String... requiredProperties);

	/**
	 * Validate that each of the properties specified by
	 * {@link #setRequiredProperties} is present and resolves to a
	 * non-{@code null} value.
	 * @throws MissingRequiredPropertiesException if any of the required
	 * properties are not resolvable.
	 */
	// 验证 {@link #setRequiredProperties} 指定的每个属性是否存在，并且解析为非 {@code null} 值。
	// @throws MissingRequiredPropertiesException 如果任何必需属性无法解析
	void validateRequiredProperties() throws MissingRequiredPropertiesException;

}
