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

package org.springframework.aot.hint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * Indicates that the classes specified in the annotation attributes require some
 * reflection hints for binding or reflection-based serialization purposes. For each
 * class specified, hints on constructors, fields, properties, record components,
 * including types transitively used on properties and record components are registered.
 * At least one class must be specified in the {@code value} or {@code classes} annotation
 * attributes.
 *
 * <p>The annotated element can be a configuration class &mdash; for example:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;RegisterReflectionForBinding({Foo.class, Bar.class})
 * public class MyConfig {
 *     // ...
 * }</pre>
 *
 * <p>The annotated element can be any Spring bean class or method &mdash; for example:
 *
 * <pre class="code">
 * &#064;Service
 * public class MyService {
 *
 *     &#064;RegisterReflectionForBinding(Baz.class)
 *     public void process() {
 *         // ...
 *     }
 *
 * }</pre>
 *
 * <p>The annotated element can also be any test class that uses the <em>Spring
 * TestContext Framework</em> to load an {@code ApplicationContext}.
 *
 * @author Sebastien Deleuze
 * @since 6.0
 * @see org.springframework.aot.hint.BindingReflectionHintsRegistrar
 * @see Reflective @Reflective
 */
// 指示注释属性中指定的类需要一些反射提示以用于绑定或基于反射的序列化目的。
// 对于指定的每个类，都会注册有关构造函数、字段、属性、记录组件（包括在属性和记录组件上传递使用的类型）的提示。
// 必须在 {@code value} 或 {@code classes} 注释属性中指定至少一个类。
//
// <p>带注释的元素可以是配置类 &mdash; 例如：
//
// <pre class="code">
// @Configuration
// @RegisterReflectionForBinding({Foo.class, Bar.class})
// public class MyConfig {
// 		// ...
// }</pre>
//
// <p>带注释的元素可以是任何 Spring bean 类或方法 &mdash;例如：
//
// <pre class="code">
// @Service
// public class MyService {
// 		@RegisterReflectionForBinding(Baz.class)
// 		public void process() { // ... }
// }</pre>
//
// <p>带注释的元素也可以是任何使用 <em>Spring TestContext Framework</em> 加载 {@code ApplicationContext} 的测试类。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective(RegisterReflectionForBindingProcessor.class)
public @interface RegisterReflectionForBinding {

	/**
	 * Alias for {@link #classes()}.
	 */
	@AliasFor("classes")
	Class<?>[] value() default {};

	/**
	 * Classes for which reflection hints should be registered.
	 * <p>At least one class must be specified either via {@link #value} or {@code classes}.
	 * @see #value()
	 */
	@AliasFor("value")
	Class<?>[] classes() default {};

}
