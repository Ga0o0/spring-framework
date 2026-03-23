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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor, field, setter method, or config method as to be autowired by
 * Spring's dependency injection facilities. This is an alternative to the JSR-330
 * {@link jakarta.inject.Inject} annotation, adding required-vs-optional semantics.
 *
 * <h3>Autowired Constructors</h3>
 * <p>Only one constructor of any given bean class may declare this annotation with the
 * {@link #required} attribute set to {@code true}, indicating <i>the</i> constructor
 * to be autowired when used as a Spring bean. Furthermore, if the {@code required}
 * attribute is set to {@code true}, only a single constructor may be annotated
 * with {@code @Autowired}. If multiple <i>non-required</i> constructors declare the
 * annotation, they will be considered as candidates for autowiring. The constructor
 * with the greatest number of dependencies that can be satisfied by matching beans
 * in the Spring container will be chosen. If none of the candidates can be satisfied,
 * then a primary/default constructor (if present) will be used. Similarly, if a
 * class declares multiple constructors but none of them is annotated with
 * {@code @Autowired}, then a primary/default constructor (if present) will be used.
 * If a class only declares a single constructor to begin with, it will always be used,
 * even if not annotated. An annotated constructor does not have to be public.
 *
 * <h3>Autowired Fields</h3>
 * <p>Fields are injected right after construction of a bean, before any config methods
 * are invoked. Such a config field does not have to be public.
 *
 * <h3>Autowired Methods</h3>
 * <p>Config methods may have an arbitrary name and any number of arguments; each of
 * those arguments will be autowired with a matching bean in the Spring container.
 * Bean property setter methods are effectively just a special case of such a general
 * config method. Such config methods do not have to be public.
 *
 * <h3>Autowired Parameters</h3>
 * <p>Although {@code @Autowired} can technically be declared on individual method
 * or constructor parameters since Spring Framework 5.0, most parts of the
 * framework ignore such declarations. The only part of the core Spring Framework
 * that actively supports autowired parameters is the JUnit Jupiter support in
 * the {@code spring-test} module (see the
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext framework</a>
 * reference documentation for details).
 *
 * <h3>Multiple Arguments and 'required' Semantics</h3>
 * <p>In the case of a multi-arg constructor or method, the {@link #required} attribute
 * is applicable to all arguments. Individual parameters may be declared as Java-8 style
 * {@link java.util.Optional} or, as of Spring Framework 5.0, also as {@code @Nullable}
 * or a not-null parameter type in Kotlin, overriding the base 'required' semantics.
 *
 * <h3>Autowiring Arrays, Collections, and Maps</h3>
 * <p>In case of an array, {@link java.util.Collection}, or {@link java.util.Map}
 * dependency type, the container autowires all beans matching the declared value
 * type. For such purposes, the map keys must be declared as type {@code String}
 * which will be resolved to the corresponding bean names. Such a container-provided
 * collection will be ordered, taking into account
 * {@link org.springframework.core.Ordered Ordered} and
 * {@link org.springframework.core.annotation.Order @Order} values of the target
 * components, otherwise following their registration order in the container.
 * Alternatively, a single matching target bean may also be a generally typed
 * {@code Collection} or {@code Map} itself, getting injected as such.
 *
 * <h3>Not supported in {@code BeanPostProcessor} or {@code BeanFactoryPostProcessor}</h3>
 * <p>Note that actual injection is performed through a
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} which in turn means that you <em>cannot</em>
 * use {@code @Autowired} to inject references into
 * {@link org.springframework.beans.factory.config.BeanPostProcessor
 * BeanPostProcessor} or
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * types. Please consult the javadoc for the {@link AutowiredAnnotationBeanPostProcessor}
 * class (which, by default, checks for the presence of this annotation).
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Qualifier
 * @see Value
 */
// 此注解用于标记构造函数、字段、setter 方法或配置方法，使其由 Spring 的依赖注入机制自动装配。
// 它是 JSR-330 {@link jakarta.inject.Inject} 注解的替代方案，增加了必需与可选语义。
//
// <h3>自动装配的构造函数</h3>
//
// <p>任何给定 bean 类只能有一个构造函数声明此注解，并将 {@link #required} 属性设置为 {@code true}，表示该构造函数在用作 Spring bean 时将被自动装配。
// 此外，如果 {@code required} 属性设置为 {@code true}，则只能有一个构造函数使用 {@code @Autowired} 注解。如果多个非必需的构造函数声明了此注解，它们将被视为自动装配的候选对象。
// 系统将选择依赖项数量最多的构造函数，这些依赖项可以通过 Spring 容器中匹配的 bean 来满足。
//
// </p>如果所有候选构造函数都无法满足要求，则会使用主构造函数/默认构造函数（如果存在）。
// 类似地，如果一个类声明了多个构造函数，但没有一个构造函数使用 `@Autowired` 注解，则会使用主构造函数/默认构造函数（如果存在）。
// 如果一个类一开始只声明了一个构造函数，则始终会使用该构造函数，即使它没有被注解。被注解的构造函数不必是公共的。
//
// <h3>自动注入字段</h3>
//
// <p>字段会在 bean 构造完成后立即注入，在调用任何配置方法之前。这样的配置字段不必是公共的。
//
// <h3>自动注入方法</h3>
//
// <p>配置方法可以具有任意名称和任意数量的参数；每个参数都会自动注入到 Spring 容器中匹配的 bean 中。
// bean 属性 setter 方法实际上只是这种通用配置方法的一个特例。这样的配置方法不必是公共的。
//
// <h3>自动装配参数</h3>
//
// <p>虽然从 Spring Framework 5.0 开始，理论上可以在单个方法或构造函数参数上声明 {@code @Autowired}，但框架的大部分组件都会忽略此类声明。
// Spring Framework 核心组件中唯一积极支持自动装配参数的部分是 {@code spring-test} 模块中的 JUnit Jupiter 支持
// （详情请参阅 <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext 框架</a> 参考文档）。
//
// <h3>多个参数和“required”语义</h3>
//
// <p>对于多参数构造函数或方法，{@link #required} 属性适用于所有参数。
// 单个参数可以声明为 Java 8 风格的 {@link java.util.Optional}，或者从 Spring Framework 5.0 开始，
// 也可以声明为 {@code @Nullable} 或 Kotlin 中的非空参数类型，从而覆盖基本的“required”语义。
//
// <h3>自动装配数组、集合和映射</h3>
//
// <p>对于数组、{@link java.util.Collection} 或 {@link java.util.Map} 依赖类型，容器会自动装配所有与声明的值类型匹配的 bean。
// 为此，映射的键必须声明为 {@code String} 类型，该类型将被解析为相应的 bean 名称。容器提供的此类集合将按顺序排列，
// 顺序依据目标组件的 {@link org.springframework.core.Ordered Ordered} 和
// {@link org.springframework.core.annotation.Order @Order} 值，否则将按照它们在容器中的注册顺序排列。
// 或者，单个匹配的目标 bean 本身也可以是通用类型的 {@code Collection} 或 {@code Map}，并按此方式注入。
//
// <h3>{@code BeanPostProcessor} 或 {@code BeanFactoryPostProcessor} 不支持此操作。</h3>
//
// <p>请注意，实际的注入是通过 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 执行的，
// 这意味着您<em>不能</em>使用 {@code @Autowired} 将引用注入到 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
// 或 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor} 类型中。
// 请参阅 {@link AutowiredAnnotationBeanPostProcessor} 类的 Javadoc（默认情况下，该类会检查是否存在此注解）。
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

	/**
	 * Declares whether the annotated dependency is required.
	 * <p>Defaults to {@code true}.
	 */
	// 声明带注解的依赖项是否为必需项。
	// <p>默认为 {@code true}。</p>
	boolean required() default true;

}
