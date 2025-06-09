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

import java.lang.reflect.Method;

/**
 * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.
 *
 * <p>A {@code MethodMatcher} may be evaluated <b>statically</b> or at <b>runtime</b>
 * (dynamically). Static matching involves a method and (possibly) method attributes.
 * Dynamic matching also makes arguments for a particular call available, and any
 * effects of running previous advice applying to the joinpoint.
 *
 * <p>If an implementation returns {@code false} from its {@link #isRuntime()}
 * method, evaluation can be performed statically, and the result will be the same
 * for all invocations of this method, whatever their arguments. This means that
 * if the {@link #isRuntime()} method returns {@code false}, the 3-arg
 * {@link #matches(Method, Class, Object[])} method will never be invoked.
 *
 * <p>If an implementation returns {@code true} from its 2-arg
 * {@link #matches(Method, Class)} method and its {@link #isRuntime()} method
 * returns {@code true}, the 3-arg {@link #matches(Method, Class, Object[])}
 * method will be invoked <i>immediately before each potential execution of the
 * related advice</i> to decide whether the advice should run. All previous advice,
 * such as earlier interceptors in an interceptor chain, will have run, so any
 * state changes they have produced in parameters or {@code ThreadLocal} state will
 * be available at the time of evaluation.
 *
 * <p><strong>WARNING</strong>: Concrete implementations of this interface must
 * provide proper implementations of {@link Object#equals(Object)},
 * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the
 * matcher to be used in caching scenarios &mdash; for example, in proxies generated
 * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation
 * must generate a unique string representation that aligns with the logic used
 * to implement {@code equals()}. See concrete implementations of this interface
 * within the framework for examples.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
// 作为 {@link Pointcut} 的一部分：检查目标方法是否有资格获得建议。
//
// <p>可以<b>静态</b>或在<b>运行时</b>（动态）评估 {@code MethodMatcher}。静态匹配涉及方法和（可能的）方法属性。
// 动态匹配还会提供特定调用的参数，并将运行先前建议的任何效果应用于连接点。
//
// <p>如果实现从其 {@link #isRuntime()} 方法返回 {@code false}，则可以静态执行评估，并且对于此方法的所有调用，结果都将相同，无论它们的参数是什么。
// 这意味着，如果 {@link #isRuntime()} 方法返回 {@code false}，则永远不会调用 3 参数 {@link #matches(Method, Class, Object[])} 方法。
//
// <p>如果一个实现从其 2-arg {@link #matches(Method, Class)} 方法返回 {@code true}，
// 并且其 {@link #isRuntime()} 方法返回 {@code true}，则 3-arg {@link #matches(Method, Class, Object[])}
// 方法将在<i>每次潜在执行相关建议之前立即</i>调用，以决定是否应该运行该建议。
// 所有先前的建议（例如拦截器链中的早期拦截器）都将运行，因此它们在参数或 {@code ThreadLocal} 状态中产生的任何状态更改都将在评估时可用。
//
// <p><strong>警告</strong>：此接口的具体实现必须提供 {@link Object#equals(Object)}、{@link Object#hashCode()} 和
// {@link Object#toString()} 的正确实现，以便允许匹配器用于缓存场景 - 例如，在由 CGLIB 生成的代理中。
// 从 Spring Framework 6.0.13 开始，{@code toString()} 实现必须生成一个唯一的字符串表示，
// 该表示必须与实现 {@code equals()} 的逻辑一致。有关示例，请参阅框架内此接口的具体实现。
public interface MethodMatcher {

	/**
	 * Perform static checking to determine whether the given method matches.
	 * <p>If this method returns {@code false} or if {@link #isRuntime()}
	 * returns {@code false}, no runtime check (i.e. no
	 * {@link #matches(Method, Class, Object[])} call) will be made.
	 * @param method the candidate method
	 * @param targetClass the target class
	 * @return whether this method matches statically
	 */
	// 执行静态检查以确定给定方法是否匹配。
	// <p>如果此方法返回 {@code false} 或 {@link #isRuntime()} 返回 {@code false}，
	// 则不会进行运行时检查（即不会调用 {@link #matches(Method, Class, Object[])}）。
	// @param method 候选方法
	// @param targetClass 目标类
	// @return 此方法是否静态匹配
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * Is this {@code MethodMatcher} dynamic, that is, must a final check be made
	 * via the {@link #matches(Method, Class, Object[])} method at runtime even
	 * if {@link #matches(Method, Class)} returns {@code true}?
	 * <p>Can be invoked when an AOP proxy is created, and need not be invoked
	 * again before each method invocation.
	 * @return whether a runtime match via {@link #matches(Method, Class, Object[])}
	 * is required if static matching passed
	 */
	// 这个 {@code MethodMatcher} 是动态的吗？
	// 也就是说，即使 {@link #matches(Method, Class)} 返回 {@code true}，
	// 也必须在运行时通过 {@link #matches(Method, Class, Object[])} 方法进行最终检查吗？
	// <p>可以在创建 AOP 代理时调用，并且不需要在每次方法调用之前再次调用。
	// @return 如果静态匹配通过，是否需要通过 {@link #matches(Method, Class, Object[])} 进行运行时匹配
	boolean isRuntime();

	/**
	 * Check whether there is a runtime (dynamic) match for this method, which
	 * must have matched statically.
	 * <p>This method is invoked only if {@link #matches(Method, Class)} returns
	 * {@code true} for the given method and target class, and if
	 * {@link #isRuntime()} returns {@code true}.
	 * <p>Invoked immediately before potential running of the advice, after any
	 * advice earlier in the advice chain has run.
	 * @param method the candidate method
	 * @param targetClass the target class
	 * @param args arguments to the method
	 * @return whether there's a runtime match
	 * @see #matches(Method, Class)
	 */
	// 检查此方法是否存在运行时（动态）匹配，该方法必须静态匹配。
	// <p>仅当 {@link #matches(Method, Class)} 对于给定方法和目标类返回 {@code true}，
	// 并且 {@link #isRuntime()} 返回 {@code true} 时，才会调用此方法。
	// <p>在建议可能运行之前立即调用，在建议链中较早的任何建议运行之后。
	// @param method 候选方法@param targetClass 目标类
	// @param args 方法的参数@return 是否存在运行时匹配
	boolean matches(Method method, Class<?> targetClass, Object... args);


	/**
	 * Canonical instance of a {@code MethodMatcher} that matches all methods.
	 */
	// 匹配所有方法的 {@code MethodMatcher} 的规范实例。
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

}
