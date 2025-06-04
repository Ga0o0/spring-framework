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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Specialization of {@link Component @Component} for classes that declare
 * {@link ExceptionHandler @ExceptionHandler}, {@link InitBinder @InitBinder}, or
 * {@link ModelAttribute @ModelAttribute} methods to be shared across
 * multiple {@code @Controller} classes.
 *
 * <p>Classes annotated with {@code @ControllerAdvice} can be declared explicitly
 * as Spring beans or auto-detected via classpath scanning. All such beans are
 * sorted based on {@link org.springframework.core.Ordered Ordered} semantics or
 * {@link org.springframework.core.annotation.Order @Order} /
 * {@link jakarta.annotation.Priority @Priority} declarations, with {@code Ordered}
 * semantics taking precedence over {@code @Order} / {@code @Priority} declarations.
 * {@code @ControllerAdvice} beans are then applied in that order at runtime.
 * Note, however, that {@code @ControllerAdvice} beans that implement
 * {@link org.springframework.core.PriorityOrdered PriorityOrdered} are <em>not</em>
 * given priority over {@code @ControllerAdvice} beans that implement {@code Ordered}.
 * In addition, {@code Ordered} is not honored for scoped {@code @ControllerAdvice}
 * beans &mdash; for example if such a bean has been configured as a request-scoped
 * or session-scoped bean.  For handling exceptions, an {@code @ExceptionHandler}
 * will be picked on the first advice with a matching exception handler method. For
 * model attributes and data binding initialization, {@code @ModelAttribute} and
 * {@code @InitBinder} methods will follow {@code @ControllerAdvice} order.
 *
 * <p>Note: For {@code @ExceptionHandler} methods, a root exception match will be
 * preferred to just matching a cause of the current exception, among the handler
 * methods of a particular advice bean. However, a cause match on a higher-priority
 * advice will still be preferred over any match (whether root or cause level)
 * on a lower-priority advice bean. As a consequence, please declare your primary
 * root exception mappings on a prioritized advice bean with a corresponding order.
 *
 * <p>By default, the methods in an {@code @ControllerAdvice} apply globally to
 * all controllers. Use selectors such as {@link #annotations},
 * {@link #basePackageClasses}, and {@link #basePackages} (or its alias
 * {@link #value}) to define a more narrow subset of targeted controllers.
 * If multiple selectors are declared, boolean {@code OR} logic is applied, meaning
 * selected controllers should match at least one selector. Note that selector checks
 * are performed at runtime, so adding many selectors may negatively impact
 * performance and add complexity.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @author Sam Brannen
 * @since 3.2
 * @see org.springframework.stereotype.Controller
 * @see RestControllerAdvice
 */
// {@link Component @Component} 特化，适用于声明 {@link ExceptionHandler @ExceptionHandler}、{@link InitBinder @InitBinder}
// 或 {@link ModelAttribute @ModelAttribute} 方法并在多个 {@code @Controller} 类之间共享的类。
//
// <p>使用 {@code @ControllerAdvice} 注解的类可以显式声明为 Spring bean，也可以通过类路径扫描自动检测。
// 所有此类 bean 均基于 {@link org.springframework.core.Ordered Ordered} 语义或
// {@link org.springframework.core.annotation.Order @Order} / {@link jakarta.annotation.Priority @Priority} 声明进行排序，
// 其中 {@code Ordered} 语义优先于 {@code @Order} / {@code @Priority} 声明。
// 然后，{@code @ControllerAdvice} bean 将在运行时按该顺序应用。但请注意，实现 {@link org.springframework.core.PriorityOrdered PriorityOrdered} 的
// {@code @ControllerAdvice} bean 的优先级<em>不</em>高于实现 {@code Ordered} 的 {@code @ControllerAdvice} bean。
// 此外，对于有作用域的 {@code @ControllerAdvice} bean（例如，如果此类 bean 已配置为请求作用域或会话作用域 bean），{@code Ordered} 不受尊重。
// 对于处理异常，将在第一个具有匹配异常处理程序方法的建议中挑选一个 {@code @ExceptionHandler}。
// 对于模型属性和数据绑定初始化，{@code @ModelAttribute} 和 {@code @InitBinder} 方法将遵循 {@code @ControllerAdvice} 顺序。
//
// <p>注意：对于 {@code @ExceptionHandler} 方法，在特定建议 bean 的处理程序方法中，根异常匹配优先于仅匹配当前异常的原因。
// 但是，与低优先级建议 bean 上的任何匹配（无论是根级别还是原因级别）相比，高优先级建议上的原因匹配仍将更受青睐。
// 因此，请在优先建议 bean 上以相应的顺序声明主要根异常映射。
//
// <p>默认情况下，{@code @ControllerAdvice} 中的方法全局应用于所有控制器。
// 使用选择器（例如 {@link #annotations}、{@link #basePackageClasses} 和
// {@link #basePackages}（或其别名 {@link #value}）来定义更窄的目标控制器子集。
// 如果声明了多个选择器，则应用布尔 {@code OR} 逻辑，这意味着选定的控制器应至少匹配一个选择器。
// 请注意，选择器检查是在运行时执行的，因此添加许多选择器可能会对性能产生负面影响并增加复杂性。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ControllerAdvice {

	/**
	 * Alias for {@link Component#value}.
	 * @since 6.1
	 */
	@AliasFor(annotation = Component.class, attribute = "value")
	String name() default "";

	/**
	 * Alias for the {@link #basePackages} attribute.
	 * <p>Allows for more concise annotation declarations &mdash; for example,
	 * {@code @ControllerAdvice("org.my.pkg")} is equivalent to
	 * {@code @ControllerAdvice(basePackages = "org.my.pkg")}.
	 * @since 4.0
	 * @see #basePackages
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * Array of base packages.
	 * <p>Controllers that belong to those base packages or sub-packages thereof
	 * will be included &mdash; for example,
	 * {@code @ControllerAdvice(basePackages = "org.my.pkg")} or
	 * {@code @ControllerAdvice(basePackages = {"org.my.pkg", "org.my.other.pkg"})}.
	 * <p>{@link #value} is an alias for this attribute, simply allowing for
	 * more concise use of the annotation.
	 * <p>Also consider using {@link #basePackageClasses} as a type-safe
	 * alternative to String-based package names.
	 * @since 4.0
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages} for specifying the packages
	 * in which to select controllers to be advised by the {@code @ControllerAdvice}
	 * annotated class.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.
	 * @since 4.0
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Array of classes.
	 * <p>Controllers that are assignable to at least one of the given types
	 * will be advised by the {@code @ControllerAdvice} annotated class.
	 * @since 4.0
	 */
	Class<?>[] assignableTypes() default {};

	/**
	 * Array of annotation types.
	 * <p>Controllers that are annotated with at least one of the supplied annotation
	 * types will be advised by the {@code @ControllerAdvice} annotated class.
	 * <p>Consider creating a custom composed annotation or use a predefined one,
	 * like {@link RestController @RestController}.
	 * @since 4.0
	 */
	Class<? extends Annotation>[] annotations() default {};

}
