/*
 * Copyright 2002-2015 the original author or authors.
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

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Indicates the 'role' hint for a given bean.
 *
 * <p>May be used on any class directly or indirectly annotated with
 * {@link org.springframework.stereotype.Component} or on methods
 * annotated with {@link Bean}.
 *
 * <p>If this annotation is not present on a Component or Bean definition,
 * the default value of {@link BeanDefinition#ROLE_APPLICATION} will apply.
 *
 * <p>If Role is present on a {@link Configuration @Configuration} class,
 * this indicates the role of the configuration class bean definition and
 * does not cascade to all @{@code Bean} methods defined within. This behavior
 * is different from the behavior of the @{@link Lazy} annotation, for example.
 *
 * @author Chris Beams
 * @since 3.1
 * @see BeanDefinition#ROLE_APPLICATION
 * @see BeanDefinition#ROLE_INFRASTRUCTURE
 * @see BeanDefinition#ROLE_SUPPORT
 * @see Bean
 */
// 指示给定 bean 的 “角色” 提示。
//
// <p>可用于任何直接或间接使用 {@link org.springframework.stereotype.Component} 注解的类，或使用 {@link Bean} 注解的方法。
//
// <p>如果组件或 Bean 定义中不存在此注解，则将应用 {@link BeanDefinition#ROLE_APPLICATION} 的默认值。
//
// <p>如果 Role 出现在 {@link Configuration @Configuration} 类中，则指示配置类 bean 定义的角色，并且不会级联到其中定义的所有 @{@code Bean} 方法。此行为与 @{@link Lazy} 注解的行为不同。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Role {

	/**
	 * Set the role hint for the associated bean.
	 * @see BeanDefinition#ROLE_APPLICATION
	 * @see BeanDefinition#ROLE_INFRASTRUCTURE
	 * @see BeanDefinition#ROLE_SUPPORT
	 */
	int value();

}
