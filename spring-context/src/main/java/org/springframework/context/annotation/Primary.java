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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a bean should be given preference when multiple candidates
 * are qualified to autowire a single-valued dependency. If exactly one
 * 'primary' bean exists among the candidates, it will be the autowired value.
 *
 * <p>This annotation is semantically equivalent to the {@code <bean>} element's
 * {@code primary} attribute in Spring XML.
 *
 * <p>May be used on any class directly or indirectly annotated with
 * {@code @Component} or on methods annotated with @{@link Bean}.
 *
 * <h2>Example</h2>
 * <pre class="code">
 * &#064;Component
 * public class FooService {
 *
 *     private FooRepository fooRepository;
 *
 *     &#064;Autowired
 *     public FooService(FooRepository fooRepository) {
 *         this.fooRepository = fooRepository;
 *     }
 * }
 *
 * &#064;Component
 * public class JdbcFooRepository extends FooRepository {
 *
 *     public JdbcFooRepository(DataSource dataSource) {
 *         // ...
 *     }
 * }
 *
 * &#064;Primary
 * &#064;Component
 * public class HibernateFooRepository extends FooRepository {
 *
 *     public HibernateFooRepository(SessionFactory sessionFactory) {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * <p>Because {@code HibernateFooRepository} is marked with {@code @Primary},
 * it will be injected preferentially over the jdbc-based variant assuming both
 * are present as beans within the same Spring application context, which is
 * often the case when component-scanning is applied liberally.
 *
 * <p>Note that using {@code @Primary} at the class level has no effect unless
 * component-scanning is being used. If a {@code @Primary}-annotated class is
 * declared via XML, {@code @Primary} annotation metadata is ignored, and
 * {@code <bean primary="true|false"/>} is respected instead.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Lazy
 * @see Bean
 * @see ComponentScan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.beans.factory.config.BeanDefinition#setPrimary
 */
// 表示当多个候选 Bean 有资格自动装配单值依赖项时，应优先考虑某个 Bean。如果候选 Bean 中恰好存在一个“主”Bean，则它将是自动装配的值。
//
// <p>此注解在语义上等同于 Spring XML 中的 {@code <bean>} 元素的 {@code primary} 属性。
//
// <p>可以用于任何直接或间接使用 {@code @Component} 注解的类，或用于使用 @{@link Bean} 注解的方法。
//
// <h2>示例</h2>
//
// <pre class="code">
// @Component
// public class FooService {
//
// 		private FooRepository fooRepository;
//
// 		@Autowired
// 		public FooService(FooRepository fooRepository) {
// 			this.fooRepository = fooRepository;
// 		}
// }
//
// @Component
// public class JdbcFooRepository extends FooRepository {
//
// 		public JdbcFooRepository(DataSource dataSource) {
// 			// ...
// 		}
// }
//
// @Primary
// @Component
// public class HibernateFooRepository extends FooRepository {
//
// 		public HibernateFooRepository(SessionFactory sessionFactory) {
// 			// ...
// 		}
// }
// </pre> <p>因为 {@code HibernateFooRepository} 标有 {@code @Primary}，所以它将优先于基于 jdbc 的变体注入，
// 假设两者都作为 bean 存在于同一个 Spring 应用程序上下文中，这在大量应用组件扫描时通常就是这种情况。
//
// <p>请注意，除非使用组件扫描，否则在类级别使用 {@code @Primary} 无效。
// 如果通过 XML 声明了带有 {@code @Primary} 注释的类，则会忽略 {@code @Primary} 注释元数据，而改为遵循 {@code <bean primary="true|false"/>}。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {

}
