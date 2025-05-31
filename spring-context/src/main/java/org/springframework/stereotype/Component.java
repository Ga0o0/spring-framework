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

package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class is a <em>component</em>.
 *
 * <p>Such classes are considered as candidates for auto-detection
 * when using annotation-based configuration and classpath scanning.
 *
 * <p>A component may optionally specify a logical component name via the
 * {@link #value value} attribute of this annotation.
 *
 * <p>Other class-level annotations may be considered as identifying
 * a component as well, typically a special kind of component &mdash;
 * for example, the {@link Repository @Repository} annotation or AspectJ's
 * {@link org.aspectj.lang.annotation.Aspect @Aspect} annotation. Note, however,
 * that the {@code @Aspect} annotation does not automatically make a class
 * eligible for classpath scanning.
 *
 * <p>Any annotation meta-annotated with {@code @Component} is considered a
 * <em>stereotype</em> annotation which makes the annotated class eligible for
 * classpath scanning. For example, {@link Service @Service},
 * {@link Controller @Controller}, and {@link Repository @Repository} are
 * stereotype annotations. Stereotype annotations may also support configuration
 * of a logical component name by overriding the {@link #value} attribute of this
 * annotation via {@link org.springframework.core.annotation.AliasFor @AliasFor}.
 *
 * <p>As of Spring Framework 6.1, support for configuring the name of a stereotype
 * component by convention (i.e., via a {@code String value()} attribute without
 * {@code @AliasFor}) is deprecated and will be removed in a future version of the
 * framework. Consequently, custom stereotype annotations must use {@code @AliasFor}
 * to declare an explicit alias for this annotation's {@link #value} attribute.
 * See the source code declaration of {@link Repository#value()} and
 * {@link org.springframework.web.bind.annotation.ControllerAdvice#name()
 * ControllerAdvice.name()} for concrete examples.
 *
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see Repository
 * @see Service
 * @see Controller
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 */
// 指示带注解的类是一个<em>组件</em>。
//
// <p>使用基于注解的配置和类路径扫描时，此类类将被视为自动检测的候选对象。
//
// <p>组件可以选择通过此注解的 {@link #value value} 属性指定逻辑组件名称。
//
// <p>其他类级别注解也可以被视为组件标识，通常是一种特殊类型的组件，
// 例如 {@link Repository @Repository} 注解或 AspectJ 的 {@link org.aspectj.lang.annotation.Aspect @Aspect} 注解。
// 但请注意，{@code @Aspect} 注解不会自动使类符合类路径扫描的条件。
//
// <p>任何使用 {@code @Component} 进行元注解的注解都被视为<em>构造型</em>注解，这使得带注解的类符合类路径扫描的条件。
// 例如，{@link Service @Service}、{@link Controller @Controller} 和 {@link Repository @Repository} 都是构造型注解。
// 构造型注解还可以通过 {@link org.springframework.core.annotation.AliasFor @AliasFor} 覆盖此注解的 {@link #value} 属性来支持配置逻辑组件名称。
//
// <p>从 Spring Framework 6.1 开始，通过约定（即通过 {@code String value()} 属性而不使用 {@code @AliasFor}）配置构造型组件名称的功能已弃用，并将在框架的未来版本中移除。
// 因此，自定义构造型注解必须使用 {@code @AliasFor} 为该注解的 {@link #value} 属性声明一个显式别名。
// 具体示例请参见{@link Repository#value()} 和 {@link org.springframework.web.bind.annotation.ControllerAdvice#name() ControllerAdvice.name()} 的源代码声明。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface Component {

	/**
	 * The value may indicate a suggestion for a logical component name,
	 * to be turned into a Spring bean name in case of an autodetected component.
	 * @return the suggested component name, if any (or empty String otherwise)
	 */
	// 该值可能表示对逻辑组件名称的建议，在自动检测到组件的情况下转换为 Spring bean 名称。
	// @return 建议的组件名称（如果有）（否则返回空字符串）
	String value() default "";

}
