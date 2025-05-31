/*
 * Copyright 2002-2019 the original author or authors.
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
 * Indicates one or more <em>component classes</em> to import &mdash; typically
 * {@link Configuration @Configuration} classes.
 *
 * <p>Provides functionality equivalent to the {@code <import/>} element in Spring XML.
 * Allows for importing {@code @Configuration} classes, {@link ImportSelector} and
 * {@link ImportBeanDefinitionRegistrar} implementations, as well as regular component
 * classes (as of 4.2; analogous to {@link AnnotationConfigApplicationContext#register}).
 *
 * <p>{@code @Bean} definitions declared in imported {@code @Configuration} classes should be
 * accessed by using {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
 * injection. Either the bean itself can be autowired, or the configuration class instance
 * declaring the bean can be autowired. The latter approach allows for explicit, IDE-friendly
 * navigation between {@code @Configuration} class methods.
 *
 * <p>May be declared at the class level or as a meta-annotation.
 *
 * <p>If XML or other non-{@code @Configuration} bean definition resources need to be
 * imported, use the {@link ImportResource @ImportResource} annotation instead.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Configuration
 * @see ImportSelector
 * @see ImportBeanDefinitionRegistrar
 * @see ImportResource
 */
// 指示要导入的一个或多个<em>组件类</em> - 通常是 {@link Configuration @Configuration} 类。
//
// <p>提供与 Spring XML 中的 {@code <import/>} 元素等效的功能。
// 允许导入 {@code @Configuration} 类、{@link ImportSelector} 和 {@link ImportBeanDefinitionRegistrar} 实现，
// 以及常规组件类（从 4.2 开始；类似于 {@link AnnotationConfigApplicationContext#register}）。
//
// <p>在导入的 {@code @Configuration} 类中声明的 {@code @Bean} 定义应通过 {@link org.springframework.beans.factory.annotation.Autowired @Autowired} 注入来访问。
// Bean 本身可以自动装配，也可以声明该 Bean 的配置类实例可以自动装配。后一种方法允许在 {@code @Configuration} 类方法之间进行显式的、IDE 友好的导航。
//
// <p>可以在类级别声明，也可以作为元注解声明。
//
// <p>如果需要导入 XML 或其他非 {@code @Configuration} bean 定义资源，请改用 {@link ImportResource @ImportResource} 注解。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {

	/**
	 * {@link Configuration @Configuration}, {@link ImportSelector},
	 * {@link ImportBeanDefinitionRegistrar}, or regular component classes to import.
	 */
	// {@link Configuration @Configuration}、{@link ImportSelector}、{@link ImportBeanDefinitionRegistrar} 或要导入的常规组件类。
	Class<?>[] value();

}
