/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.core.io.support;

import java.util.Arrays;
import java.util.List;

import org.springframework.lang.Nullable;

/**
 * Descriptor for a {@link org.springframework.core.env.PropertySource PropertySource}.
 *
 * @author Stephane Nicoll
 * @since 6.0
 * @param locations the locations to consider
 * @param ignoreResourceNotFound whether a failure to find a property resource
 * should be ignored
 * @param name the name of the property source, or {@code null} to infer one
 * @param propertySourceFactory the type of {@link PropertySourceFactory} to use,
 * or {@code null} to use the default
 * @param encoding the encoding, or {@code null} to use the default encoding
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.context.annotation.PropertySource
 */
// {@link org.springframework.core.env.PropertySource PropertySource} 的描述符。
//
// @param location 需要考虑的位置
// @param ignoreResourceNotFound 是否忽略属性资源查找失败
// @param name 属性源的名称，或 {@code null} 推断名称
// @param propertySourceFactory 使用的 {@link PropertySourceFactory} 的类型，或 {@code null} 使用默认类型
// @param encoding 编码方式，或 {@code null} 使用默认编码
public record PropertySourceDescriptor(List<String> locations, boolean ignoreResourceNotFound,
		@Nullable String name, @Nullable Class<? extends PropertySourceFactory> propertySourceFactory,
		@Nullable String encoding) {

	/**
	 * Create a descriptor with the specified locations.
	 * @param locations the locations to consider
	 */
	// 创建具有指定位置的描述符。
	// @param location 要考虑的位置
	public PropertySourceDescriptor(String... locations) {
		this(Arrays.asList(locations), false, null, null, null);
	}

}
