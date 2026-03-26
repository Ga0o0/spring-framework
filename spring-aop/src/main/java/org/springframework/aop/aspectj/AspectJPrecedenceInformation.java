/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.aop.aspectj;

import org.springframework.core.Ordered;

/**
 * Interface to be implemented by types that can supply the information
 * needed to sort advice/advisors by AspectJ's precedence rules.
 *
 * @author Adrian Colyer
 * @since 2.0
 * @see org.springframework.aop.aspectj.autoproxy.AspectJPrecedenceComparator
 */
// 接口由能够提供所需信息的类型实现，以便根据 AspectJ 的优先级规则对建议/顾问进行排序。
public interface AspectJPrecedenceInformation extends Ordered {

	// Implementation note:
	// We need the level of indirection this interface provides as otherwise the
	// AspectJPrecedenceComparator must ask an Advisor for its Advice in all cases
	// in order to sort advisors. This causes problems with the
	// InstantiationModelAwarePointcutAdvisor which needs to delay creating
	// its advice for aspects with non-singleton instantiation models.
	// --> 译文：实现说明：
	// 我们需要此接口提供的间接层级，否则 AspectJPrecedenceComparator 必须始终向 Advisor 请求其 Advice 才能对 Advisor 进行排序。
	// 这会导致 InstantiationModelAwarePointcutAdvisor 出现问题，因为它需要延迟为具有非单例实例化模型的切面创建其 Advice。

	/**
	 * Return the name of the aspect (bean) in which the advice was declared.
	 */
	// 返回声明了 advice 的方面（bean）的名称。
	String getAspectName();

	/**
	 * Return the declaration order of the advice member within the aspect.
	 */
	// 返回方面中 advice 成员的声明顺序。
	int getDeclarationOrder();

	/**
	 * Return whether this is a before advice.
	 */
	// 返回是否为 before advice
	boolean isBeforeAdvice();

	/**
	 * Return whether this is an after advice.
	 */
	// 返回是否为 after advice
	boolean isAfterAdvice();

}
