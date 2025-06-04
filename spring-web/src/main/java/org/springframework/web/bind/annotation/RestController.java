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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

/**
 * A convenience annotation that is itself annotated with
 * {@link Controller @Controller} and {@link ResponseBody @ResponseBody}.
 * <p>
 * Types that carry this annotation are treated as controllers where
 * {@link RequestMapping @RequestMapping} methods assume
 * {@link ResponseBody @ResponseBody} semantics by default.
 *
 * <p><b>NOTE:</b> {@code @RestController} is processed if an appropriate
 * {@code HandlerMapping}-{@code HandlerAdapter} pair is configured such as the
 * {@code RequestMappingHandlerMapping}-{@code RequestMappingHandlerAdapter}
 * pair which are the default in the MVC Java config and the MVC namespace.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 4.0
 */
// 这是一个便捷的注解，其本身带有 {@link Controller @Controller} 和 {@link ResponseBody @ResponseBody} 注解。
//
// <p> 带有此注解的类型将被视为控制器，其中 {@link RequestMapping @RequestMapping} 方法默认采用 {@link ResponseBody @ResponseBody} 语义。
// <p><b>注意：</b> 如果配置了适当的 {@code HandlerMapping}-{@code HandlerAdapter} 对
// （例如，MVC Java 配置和 MVC 命名空间中的默认设置是 {@code RequestMappingHandlerMapping}-{@code RequestMappingHandlerAdapter} 对），则会处理 {@code @RestController}。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController {

	/**
	 * The value may indicate a suggestion for a logical component name,
	 * to be turned into a Spring bean in case of an autodetected component.
	 * @return the suggested component name, if any (or empty String otherwise)
	 * @since 4.0.1
	 */
	// 该值可能指示逻辑组件名称的建议，在自动检测到组件的情况下转换为 Spring bean。
	// @return 建议的组件名称（如果有）（否则返回空字符串）
	@AliasFor(annotation = Controller.class)
	String value() default "";

}
