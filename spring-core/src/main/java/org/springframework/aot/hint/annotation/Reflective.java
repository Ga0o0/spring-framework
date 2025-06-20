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
 * Indicate that the annotated element requires reflection.
 *
 * <p>When present, either directly or as a meta-annotation, this annotation
 * triggers the configured {@linkplain ReflectiveProcessor processors} against
 * the annotated element. By default, a reflection hint is registered for the
 * annotated element so that it can be discovered and invoked if necessary.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 6.0
 * @see SimpleReflectiveProcessor
 * @see ReflectiveRuntimeHintsRegistrar
 * @see RegisterReflectionForBinding @RegisterReflectionForBinding
 */
// 指示被注解的元素需要反射。
//
// <p>当此注解存在时（无论是直接存在还是以元注解形式存在），都会触发针对被注解元素配置的 {@linkplain ReflectiveProcessor 处理器}。
// 默认情况下，系统会为被注解的元素注册一个反射提示，以便在必要时发现并调用该提示。
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Reflective {

	/**
	 * Alias for {@link #processors()}.
	 */
	// {@link #processors()} 的别名。
	@AliasFor("processors")
	Class<? extends ReflectiveProcessor>[] value() default SimpleReflectiveProcessor.class;

	/**
	 * {@link ReflectiveProcessor} implementations to invoke against the
	 * annotated element.
	 */
	// {@link ReflectiveProcessor} 实现来调用注释元素。
	@AliasFor("value")
	Class<? extends ReflectiveProcessor>[] processors() default SimpleReflectiveProcessor.class;

}
