/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.Ordered;

/**
 * {@code @Order} defines the sort order for an annotated component.
 *
 * <p>The {@link #value} is optional and represents an order value as defined in the
 * {@link Ordered} interface. Lower values have higher priority. The default value is
 * {@code Ordered.LOWEST_PRECEDENCE}, indicating the lowest priority (losing to any
 * other specified order value).
 *
 * <p><b>NOTE:</b> Since Spring 4.0, annotation-based ordering is supported for many
 * kinds of components in Spring, even for collection injection where the order values
 * of the target components are taken into account (either from their target class or
 * from their {@code @Bean} method). While such order values may influence priorities
 * at injection points, please be aware that they do not influence singleton startup
 * order which is an orthogonal concern determined by dependency relationships and
 * {@code @DependsOn} declarations (influencing a runtime-determined dependency graph).
 *
 * <p>Since Spring 4.1, the standard {@link jakarta.annotation.Priority} annotation
 * can be used as a drop-in replacement for this annotation in ordering scenarios.
 * Note that {@code @Priority} may have additional semantics when a single element
 * has to be picked (see {@link AnnotationAwareOrderComparator#getPriority}).
 *
 * <p>Alternatively, order values may also be determined on a per-instance basis
 * through the {@link Ordered} interface, allowing for configuration-determined
 * instance values instead of hard-coded values attached to a particular class.
 *
 * <p>Consult the javadoc for {@link org.springframework.core.OrderComparator
 * OrderComparator} for details on the sort semantics for non-ordered objects.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.core.Ordered
 * @see AnnotationAwareOrderComparator
 * @see OrderUtils
 * @see jakarta.annotation.Priority
 */
// {@code @Order} 定义带注解组件的排序顺序。<p>{@link #value} 是可选的，表示 {@link Ordered} 接口中定义的排序值。值越低，优先级越高。
// 默认值为 {@code Ordered.LOWEST_PRECEDENCE}，表示优先级最低（低于任何其他指定的排序值）。
//
// <p><b>注意：</b>从 Spring 4.0 开始，Spring 中的许多组件都支持基于注解的排序，甚至对于集合注入也支持排序，因为会考虑目标组件的排序值（来自其目标类或其 {@code @Bean} 方法）。
// 虽然这些排序值可能会影响注入点的优先级，但请注意，它们不会影响单例启动顺序，单例启动顺序是由依赖关系和 {@code @DependsOn} 声明（影响运行时确定的依赖图）决定的正交问题。
//
// <p>自 Spring 4.1 起，标准 {@link jakarta.annotation.Priority} 注解可在排序场景中直接替代此注解。
// 请注意，当需要选择单个元素时，{@code @Priority} 可能具有其他语义（参见 {@link AnnotationAwareOrderComparator#getPriority}）。
//
// <p>或者，也可以通过 {@link Ordered} 接口基于每个实例确定顺序值，从而允许使用由配置确定的实例值，而不是附加到特定类的硬编码值。
//
// <p>有关无序对象的排序语义的详细信息，请参阅 {@link org.springframework.core.OrderComparator OrderComparator} 的 javadoc。
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Order {

	/**
	 * The order value.
	 * <p>Default is {@link Ordered#LOWEST_PRECEDENCE}.
	 * @see Ordered#getOrder()
	 */
	int value() default Ordered.LOWEST_PRECEDENCE;

}
