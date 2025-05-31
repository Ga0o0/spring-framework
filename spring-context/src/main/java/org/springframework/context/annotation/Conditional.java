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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component is only eligible for registration when all
 * {@linkplain #value specified conditions} match.
 *
 * <p>A <em>condition</em> is any state that can be determined programmatically
 * before the bean definition is due to be registered (see {@link Condition} for details).
 *
 * <p>The {@code @Conditional} annotation may be used in any of the following ways:
 * <ul>
 * <li>as a type-level annotation on any class directly or indirectly annotated with
 * {@code @Component}, including {@link Configuration @Configuration} classes</li>
 * <li>as a meta-annotation, for the purpose of composing custom stereotype
 * annotations</li>
 * <li>as a method-level annotation on any {@link Bean @Bean} method</li>
 * </ul>
 *
 * <p>If a {@code @Configuration} class is marked with {@code @Conditional},
 * all of the {@code @Bean} methods, {@link Import @Import} annotations, and
 * {@link ComponentScan @ComponentScan} annotations associated with that
 * class will be subject to the conditions.
 *
 * <p><strong>NOTE</strong>: Inheritance of {@code @Conditional} annotations
 * is not supported; any conditions from superclasses or from overridden
 * methods will not be considered. In order to enforce these semantics,
 * {@code @Conditional} itself is not declared as
 * {@link java.lang.annotation.Inherited @Inherited}; furthermore, any
 * custom <em>composed annotation</em> that is meta-annotated with
 * {@code @Conditional} must not be declared as {@code @Inherited}.
 *
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 4.0
 * @see Condition
 */
// 表示组件仅在所有 {@linkplain #value 指定的条件} 均满足时才有资格注册。
//
// <p><em>条件</em> 是指在 Bean 定义注册之前可以通过编程方式确定的任何状态（详情请参阅 {@link Condition}）。
//
// <p>{@code @Conditional} 批注可以按以下任一方式使用：
// <ul>
// <li>作为直接或间接使用 {@code @Component} 批注的任何类上的类型级别批注，包括 {@link Configuration @Configuration} 类</li>
// <li>作为元批注，用于组成自定义构造型批注</li> <li>作为任何 {@link Bean @Bean} 方法上的方法级别批注</li>
// </ul>
//
// <p>如果 {@code @Configuration} 类标有 {@code @Conditional}，
// 则与该类关联的所有 {@code @Bean} 方法、{@link Import @Import} 批注和 {@link ComponentScan @ComponentScan} 批注都将受条件约束。
//
// <p><strong>注意</strong>：不支持继承 {@code @Conditional} 批注；不会考虑来自超类或重写方法的任何条件。
// 为了强制执行这些语义，{@code @Conditional} 本身未声明为 {@link java.lang.annotation.Inherited @Inherited}；
// 此外，任何使用 {@code @Conditional} 进行元注释的自定义<em>组合注释</em>都不能声明为 {@code @Inherited}。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

	/**
	 * All {@link Condition} classes that must {@linkplain Condition#matches match}
	 * in order for the component to be registered.
	 */
	// 所有 {@link Condition} 类必须 {@linkplain Condition#matches match} 才能注册该组件。
	Class<? extends Condition>[] value();

}
