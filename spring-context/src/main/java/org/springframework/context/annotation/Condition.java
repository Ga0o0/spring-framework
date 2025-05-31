/*
 * Copyright 2002-2024 the original author or authors.
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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * A single condition that must be {@linkplain #matches matched} in order for a
 * component to be registered.
 *
 * <p>Conditions are checked immediately before the bean definition is due to be
 * registered and are free to veto registration based on any criteria that can
 * be determined at that point.
 *
 * <p>Conditions must follow the same restrictions as a {@link BeanFactoryPostProcessor}
 * and take care to never interact with bean instances. For more fine-grained control
 * over conditions that interact with {@code @Configuration} beans, consider implementing
 * the {@link ConfigurationCondition} interface.
 *
 * <p>Multiple conditions on a given class or on a given method will be ordered
 * according to the semantics of Spring's {@link org.springframework.core.Ordered}
 * interface and {@link org.springframework.core.annotation.Order @Order} annotation.
 * See {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
 * for details.
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see ConfigurationCondition
 * @see Conditional
 * @see ConditionContext
 */
// 组件注册时必须满足的单个条件必须{@linkplain #matches 匹配}。
//
// <p>在 bean 定义即将注册之前，会立即检查条件，并且可以根据当时确定的任何条件否决注册。
//
// <p>条件必须遵循与 {@link BeanFactoryPostProcessor} 相同的限制，并且注意切勿与 bean 实例交互。
// 为了对与 {@code @Configuration} bean 交互的条件进行更细粒度的控制，请考虑实现 {@link ConfigurationCondition} 接口。
//
// <p>给定类或给定方法上的多个条件将根据 Spring 的 {@link org.springframework.core.Ordered} 接口
// 和 {@link org.springframework.core.annotation.Order @Order} 注解的语义进行排序。
// 详情请参阅 {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}。
@FunctionalInterface
public interface Condition {

	/**
	 * Determine if the condition matches.
	 * @param context the condition context
	 * @param metadata the metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
	 * or {@link org.springframework.core.type.MethodMetadata method} being checked
	 * @return {@code true} if the condition matches and the component can be registered,
	 * or {@code false} to veto the annotated component's registration
	 */
	// 判断条件是否匹配。
	// @param context 条件上下文
	// @param metadata 被检查的 {@link org.springframework.core.type.AnnotationMetadata 类}
	// 或 {@link org.springframework.core.type.MethodMetadata 方法} 的元数据
	// @return {@code true} 如果条件匹配且组件可以注册，则返回 {@code false} 否决被注解组件的注册
	boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);

}
