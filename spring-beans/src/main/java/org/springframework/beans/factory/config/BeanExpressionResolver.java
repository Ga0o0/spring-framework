/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for resolving a value by evaluating it as an expression,
 * if applicable.
 *
 * <p>A raw {@link org.springframework.beans.factory.BeanFactory} does not
 * contain a default implementation of this strategy. However,
 * {@link org.springframework.context.ApplicationContext} implementations
 * will provide expression support out of the box.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
// 策略接口，用于通过将值求值作为表达式来解析（如果适用）。
//
// <p>原始的 {@link org.springframework.beans.factory.BeanFactory} 不包含此策略的默认实现。
// 但是，{@link org.springframework.context.ApplicationContext} 实现将提供开箱即用的表达式支持。
public interface BeanExpressionResolver {

	/**
	 * Evaluate the given value as an expression, if applicable;
	 * return the value as-is otherwise.
	 * @param value the value to evaluate as an expression
	 * @param beanExpressionContext the bean expression context to use when
	 * evaluating the expression
	 * @return the resolved value (potentially the given value as-is)
	 * @throws BeansException if evaluation failed
	 */
	// 如果适用，则将给定值作为表达式求值；否则，按原样返回该值。
	// @param value 要作为表达式求值的值
	// @param beanExpressionContext 求值时使用的 Bean 表达式上下文
	// @return 解析后的值（可能为给定值的原样）
	// @throws BeansException 如果求值失败，则抛出 BeansException
	@Nullable
	Object evaluate(@Nullable String value, BeanExpressionContext beanExpressionContext) throws BeansException;

}
