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

package org.aopalliance.intercept;

import java.lang.reflect.AccessibleObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This interface represents a generic runtime joinpoint (in the AOP
 * terminology).
 *
 * <p>A runtime joinpoint is an <i>event</i> that occurs on a static
 * joinpoint (i.e. a location in a program). For instance, an
 * invocation is the runtime joinpoint on a method (static joinpoint).
 * The static part of a given joinpoint can be generically retrieved
 * using the {@link #getStaticPart()} method.
 *
 * <p>In the context of an interception framework, a runtime joinpoint
 * is then the reification of an access to an accessible object (a
 * method, a constructor, a field), i.e. the static part of the
 * joinpoint. It is passed to the interceptors that are installed on
 * the static joinpoint.
 *
 * @author Rod Johnson
 * @see Interceptor
 */
// 此接口表示通用的运行时连接点（AOP 术语）。
//
// <p>运行时连接点是发生在静态连接点（即程序中的某个位置）上的<i>事件</i>。
// 例如，调用（invocation）是方法（静态连接点）上的运行时连接点。可以使用 {@link #getStaticPart()} 方法检索给定连接点的静态部分。
//
// <p>在拦截框架的上下文中，运行时连接点是对可访问对象（方法、构造函数、字段）访问的具体化，即连接点的静态部分。
// 它会被传递给安装在静态连接点上的拦截器。
public interface Joinpoint {

	/**
	 * Proceed to the next interceptor in the chain.
	 * <p>The implementation and the semantics of this method depends
	 * on the actual joinpoint type (see the children interfaces).
	 * @return see the children interfaces' proceed definition
	 * @throws Throwable if the joinpoint throws an exception
	 */
	// 继续执行链中的下一个拦截器。
	// <p>此方法的实现和语义取决于实际的连接点类型（参见子接口）。
	// @return 参见子接口的 proceed 定义。
	// @throws Throwable，如果连接点抛出异常。
	@Nullable
	Object proceed() throws Throwable;

	/**
	 * Return the object that holds the current joinpoint's static part.
	 * <p>For instance, the target object for an invocation.
	 * @return the object (can be null if the accessible object is static)
	 */
	// 返回保存当前连接点静态部分的对象。
	// <p>例如，调用的目标对象。
	// @return 该对象（如果可访问对象是静态的，则可以为 null）
	@Nullable
	Object getThis();

	/**
	 * Return the static part of this joinpoint.
	 * <p>The static part is an accessible object on which a chain of
	 * interceptors is installed.
	 */
	// 返回此连接点的静态部分。
	// <p>静态部分是一个可访问对象，在其上安装了一系列拦截器。
	@Nonnull
	AccessibleObject getStaticPart();

}
