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

/**
 * Enumerates the various scoped-proxy options.
 *
 * <p>For a more complete discussion of exactly what a scoped proxy is, see the
 * section of the Spring reference documentation entitled '<em>Scoped beans as
 * dependencies</em>'.
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */
// 枚举各种作用域代理选项。
//
// <p>有关作用域代理的更完整讨论，请参阅 Spring 参考文档中标题为“<em>作用域 bean 作为依赖项</em>”的部分。
public enum ScopedProxyMode {

	/**
	 * Default typically equals {@link #NO}, unless a different default
	 * has been configured at the component-scan instruction level.
	 */
	// 默认值通常等于 {@link #NO}，除非在组件扫描指令级别配置了不同的默认值。
	DEFAULT,

	/**
	 * Do not create a scoped proxy.
	 * <p>This proxy-mode is not typically useful when used with a
	 * non-singleton scoped instance, which should favor the use of the
	 * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
	 * is to be used as a dependency.
	 */
	// 不创建作用域代理。
	// <p>此代理模式通常在与非单例作用域实例一起使用时无用，如果要将其用作依赖项，则应优先使用 {@link #INTERFACES} 或 {@link #TARGET_CLASS} 代理模式。
	NO,

	/**
	 * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
	 * the class of the target object.
	 */
	// 创建一个 JDK 动态代理，该代理实现目标对象类公开的<i>所有</i>接口。
	INTERFACES,

	/**
	 * Create a class-based proxy (uses CGLIB).
	 */
	// 创建基于类的代理（使用 CGLIB）。
	TARGET_CLASS

}
