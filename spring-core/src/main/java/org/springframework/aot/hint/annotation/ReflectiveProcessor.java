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

package org.springframework.aot.hint.annotation;

import java.lang.reflect.AnnotatedElement;

import org.springframework.aot.hint.ReflectionHints;

/**
 * Process an {@link AnnotatedElement} and register the necessary reflection
 * hints for it.
 *
 * <p>{@code ReflectiveProcessor} implementations are registered via
 * {@link Reflective#processors() @Reflective(processors = ...)}.
 *
 * @author Stephane Nicoll
 * @since 6.0
 * @see Reflective @Reflective
 */
// 处理 {@link AnnotatedElement} 并为其注册必要的反射提示。
//
// <p>{@code ReflectiveProcessor} 实现通过 {@link Reflective#processors() @Reflective(processors = ...)} 注册。
public interface ReflectiveProcessor {

	/**
	 * Register {@link ReflectionHints} against the specified {@link AnnotatedElement}.
	 * @param hints the reflection hints instance to use
	 * @param element the element to process
	 */
	// 针对指定的 {@link AnnotatedElement} 注册 {@link ReflectionHints}。
	// @param hints 指定要使用的反射提示实例
	// @param element 指定要处理的元素
	void registerReflectionHints(ReflectionHints hints, AnnotatedElement element);

}
