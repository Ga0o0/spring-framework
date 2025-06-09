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

import org.aopalliance.aop.Advice;

/**
 * Base interface holding AOP <b>advice</b> (action to take at a joinpoint)
 * and a filter determining the applicability of the advice (such as
 * a pointcut). <i>This interface is not for use by Spring users, but to
 * allow for commonality in support for different types of advice.</i>
 *
 * <p>Spring AOP is based around <b>around advice</b> delivered via method
 * <b>interception</b>, compliant with the AOP Alliance interception API.
 * The Advisor interface allows support for different types of advice,
 * such as <b>before</b> and <b>after</b> advice, which need not be
 * implemented using interception.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
// 包含 AOP <b>advice</b>（在连接点执行的操作）和用于确定建议适用性的过滤器（例如切入点）的基本接口。
// <i>此接口不供 Spring 用户使用，但允许通用地支持不同类型的建议。</i>
//
// <p>Spring AOP 基于通过方法 <b>interception</b> 传递的 <b>around advice</b>，符合 AOP 联盟拦截 API。
// Advisor 接口支持不同类型的 advice，例如 <b>before</b> 和 <b>after</b> advice，这些建议无需使用拦截来实现。
public interface Advisor {

	/**
	 * Common placeholder for an empty {@code Advice} to be returned from
	 * {@link #getAdvice()} if no proper advice has been configured (yet).
	 * @since 5.0
	 */
	// 如果尚未配置适当的建议，则从 {@link #getAdvice()} 返回空的 {@code Advice} 的通用占位符。
	Advice EMPTY_ADVICE = new Advice() {};


	/**
	 * Return the advice part of this aspect. An advice may be an
	 * interceptor, a before advice, a throws advice, etc.
	 * @return the advice that should apply if the pointcut matches
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	// 返回此方面的建议部分。建议可以是拦截器、前置建议、抛出建议等。
	// @return 如果切入点匹配则应用的建议
	Advice getAdvice();

	/**
	 * Return whether this advice is associated with a particular instance
	 * (for example, creating a mixin) or shared with all instances of
	 * the advised class obtained from the same Spring bean factory.
	 * <p><b>Note that this method is not currently used by the framework.</b>
	 * Typical Advisor implementations always return {@code true}.
	 * Use singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model.
	 * <p>As of 6.0.10, the default implementation returns {@code true}.
	 * @return whether this advice is associated with a particular target instance
	 */
	// 返回此建议是否与特定实例关联（例如，创建混合宏），或者是否与从同一 Spring bean 工厂获取的被建议类的所有实例共享。
	// <p><b>请注意，框架当前未使用此方法。</b>典型的 Advisor 实现始终返回 {@code true}。
	// 使用单例/原型 bean 定义或适当的编程代理创建，以确保 Advisor 具有正确的生命周期模型。
	// <p>从 6.0.10 开始，默认实现返回 {@code true}。
	// @return 此建议是否与特定目标实例关联
	default boolean isPerInstance() {
		return true;
	}

}
