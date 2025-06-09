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

/**
 * Filter that restricts matching of a pointcut or introduction to a given set
 * of target classes.
 *
 * <p>Can be used as part of a {@link Pointcut} or for the entire targeting of
 * an {@link IntroductionAdvisor}.
 *
 * <p><strong>WARNING</strong>: Concrete implementations of this interface must
 * provide proper implementations of {@link Object#equals(Object)},
 * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the
 * filter to be used in caching scenarios &mdash; for example, in proxies generated
 * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation
 * must generate a unique string representation that aligns with the logic used
 * to implement {@code equals()}. See concrete implementations of this interface
 * within the framework for examples.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Pointcut
 * @see MethodMatcher
 */
// 用于将切入点或引入点限制为给定目标类集的过滤器。
//
// <p>可用作 {@link Pointcut} 的一部分，或用于 {@link IntroductionAdvisor} 的整个定位。
//
// <p><strong>警告</strong>：此接口的具体实现必须提供 {@link Object#equals(Object)}、{@link Object#hashCode()} 和
// {@link Object#toString()} 的正确实现，以便允许过滤器用于缓存场景 - 例如，在 CGLIB 生成的代理中。
// 从 Spring Framework 6.0.13 开始，{@code toString()} 实现必须生成与实现 {@code equals()} 的逻辑一致的唯一字符串表示形式。
// 有关示例，请参阅框架内此接口的具体实现。
@FunctionalInterface
public interface ClassFilter {

	/**
	 * Should the pointcut apply to the given interface or target class?
	 * @param clazz the candidate target class
	 * @return whether the advice should apply to the given target class
	 */
	// 切入点应该应用于给定的接口还是目标类？
	// @param clazz 候选目标类
	// @return 通知是否应应用于给定的目标类
	boolean matches(Class<?> clazz);


	/**
	 * Canonical instance of a {@code ClassFilter} that matches all classes.
	 */
	// 匹配所有类的 {@code ClassFilter} 的规范实例。
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
