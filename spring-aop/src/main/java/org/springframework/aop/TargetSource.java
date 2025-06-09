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

package org.springframework.aop;

import org.springframework.lang.Nullable;

/**
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 *
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 *
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
// {@code TargetSource} 用于获取 AOP 调用的当前“目标”，如果没有环绕通知选择结束拦截器链本身，则将通过反射调用该目标。
//
// <p>如果 {@code TargetSource} 是“静态的”，它将始终返回相同的目标，从而允许在 AOP 框架中进行优化。
// 动态目标源可以支持池化、热交换等功能。
//
// <p>应用程序开发人员通常不需要直接使用 {@code TargetSources}：这是一个 AOP 框架接口。
public interface TargetSource extends TargetClassAware {

	/**
	 * Return the type of targets returned by this {@link TargetSource}.
	 * <p>Can return {@code null}, although certain usages of a {@code TargetSource}
	 * might just work with a predetermined target class.
	 * @return the type of targets returned by this {@link TargetSource}
	 */
	// 返回此 {@link TargetSource} 返回的目标类型。
	// <p>可以返回 {@code null}，尽管 {@code TargetSource} 的某些用法可能仅适用于预定的目标类。
	// @return 此 {@link TargetSource} 返回的目标类型
	@Override
	@Nullable
	Class<?> getTargetClass();

	/**
	 * Will all calls to {@link #getTarget()} return the same object?
	 * <p>In that case, there will be no need to invoke {@link #releaseTarget(Object)},
	 * and the AOP framework can cache the return value of {@link #getTarget()}.
	 * <p>The default implementation returns {@code false}.
	 * @return {@code true} if the target is immutable
	 * @see #getTarget
	 */
	// 所有对 {@link #getTarget()} 的调用都会返回同一个对象吗？
	// <p>如果是，则无需调用 {@link #releaseTarget(Object)}，AOP 框架可以缓存 {@link #getTarget()} 的返回值。
	// <p>默认实现返回 {@code false}。
	// @return {@code true} 如果目标是不可变的
	default boolean isStatic() {
		return false;
	}

	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * @return the target object which contains the joinpoint,
	 * or {@code null} if there is no actual target instance
	 * @throws Exception if the target object can't be resolved
	 */
	// 返回目标实例。
	// 在 AOP 框架调用 AOP 方法调用的 “目标” 之前立即调用。
	// @return 返回包含连接点的目标对象，如果不存在实际的目标实例，则返回 {@code null}；
	// @throws Exception，如果目标对象无法解析
	@Nullable
	Object getTarget() throws Exception;

	/**
	 * Release the given target object obtained from the
	 * {@link #getTarget()} method, if any.
	 * <p>The default implementation is empty.
	 * @param target object obtained from a call to {@link #getTarget()}
	 * @throws Exception if the object can't be released
	 */
	// 释放通过 {@link #getTarget()} 方法获取的给定目标对象（如果有）。
	// <p>默认实现为空。@param 通过调用 {@link #getTarget()} 获取的目标对象
	// @throws Exception（如果无法释放对象）
	default void releaseTarget(Object target) throws Exception {
	}

}
