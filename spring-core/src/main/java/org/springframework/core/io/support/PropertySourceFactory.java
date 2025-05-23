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

import java.io.IOException;

import org.springframework.core.env.PropertySource;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for creating resource-based {@link PropertySource} wrappers.
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @see DefaultPropertySourceFactory
 * @see ResourcePropertySource
 */
// 用于创建基于资源的 {@link PropertySource} 包装器的策略接口。
public interface PropertySourceFactory {

	/**
	 * Create a {@link PropertySource} that wraps the given resource.
	 * <p>Implementations will typically create {@link ResourcePropertySource}
	 * instances, with {@link PropertySourceProcessor} automatically adapting
	 * property source names via {@link ResourcePropertySource#withResourceName()}
	 * if necessary, e.g. when combining multiple sources for the same name
	 * into a {@link org.springframework.core.env.CompositePropertySource}.
	 * Custom implementations with custom {@link PropertySource} types need
	 * to make sure to expose distinct enough names, possibly deriving from
	 * {@link ResourcePropertySource} where possible.
	 * @param name the name of the property source
	 * (can be {@code null} in which case the factory implementation
	 * will have to generate a name based on the given resource)
	 * @param resource the resource (potentially encoded) to wrap
	 * @return the new {@link PropertySource} (never {@code null})
	 * @throws IOException if resource resolution failed
	 */
	// 创建一个包装给定资源的 {@link PropertySource} 实例。
	// <p>实现通常会创建 {@link ResourcePropertySource} 实例，
	// {@link PropertySourceProcessor} 会在必要时通过 {@link ResourcePropertySource#withResourceName()} 自动调整属性源名称，
	// 例如，将多个同名源组合成 {@link org.springframework.core.env.CompositePropertySource} 时。
	// 自定义 {@link PropertySource} 类型的实现需要确保暴露足够独特的名称，并尽可能从 {@link ResourcePropertySource} 派生。 
	// @param name 属性源的名称（可以为 {@code null}，此时工厂实现必须根据给定的资源生成名称）
	// @param resource 要包装的资源（可能经过编码）
	// @return 新的 {@link PropertySource}（永远不会为 {@code null}）
	// @throws IOException（如果资源解析失败）
	PropertySource<?> createPropertySource(@Nullable String name, EncodedResource resource) throws IOException;

}
