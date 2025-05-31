/*
 * Copyright 2002-2018 the original author or authors.
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

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AliasFor;

/**
 * When used as a type-level annotation in conjunction with
 * {@link org.springframework.stereotype.Component @Component},
 * {@code @Scope} indicates the name of a scope to use for instances of
 * the annotated type.
 *
 * <p>When used as a method-level annotation in conjunction with
 * {@link Bean @Bean}, {@code @Scope} indicates the name of a scope to use
 * for the instance returned from the method.
 *
 * <p><b>NOTE:</b> {@code @Scope} annotations are only introspected on the
 * concrete bean class (for annotated components) or the factory method
 * (for {@code @Bean} methods). In contrast to XML bean definitions,
 * there is no notion of bean definition inheritance, and inheritance
 * hierarchies at the class level are irrelevant for metadata purposes.
 *
 * <p>In this context, <em>scope</em> means the lifecycle of an instance,
 * such as {@code singleton}, {@code prototype}, and so forth. Scopes
 * provided out of the box in Spring may be referred to using the
 * {@code SCOPE_*} constants available in the {@link ConfigurableBeanFactory}
 * and {@code WebApplicationContext} interfaces.
 *
 * <p>To register additional custom scopes, see
 * {@link org.springframework.beans.factory.config.CustomScopeConfigurer
 * CustomScopeConfigurer}.
 *
 * @author Mark Fisher
 * @author Chris Beams
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.annotation.Bean
 */
// 当与 {@link org.springframework.stereotype.Component @Component} 结合使用类型级别注解时，{@code @Scope} 表示用于所注解类型实例的作用域名称。
//
// <p>当与 {@link Bean @Bean} 结合使用方法级别注解时，{@code @Scope} 表示用于方法返回实例的作用域名称。
//
// <p><b>注意：</b> {@code @Scope} 注解仅在具体 Bean 类（对于带注解的组件）或工厂方法（对于 {@code @Bean} 方法）上进行自省。
// 与 XML Bean 定义相比，没有 Bean 定义继承的概念，并且类级别的继承层次结构与元数据目的无关。
//
// <p>在这种情况下，<em>作用域</em>表示实例的生命周期，例如 {@code singleton}、{@code prototype} 等等。
// Spring 中开箱即用的作用域可以通过 {@link ConfigurableBeanFactory} 和 {@code WebApplicationContext} 接口中的 {@code SCOPE_} 常量来引用。
//
// <p>要注册其他自定义作用域，请参阅 {@link org.springframework.beans.factory.config.CustomScopeConfigurer CustomScopeConfigurer}。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

	/**
	 * Alias for {@link #scopeName}.
	 * @see #scopeName
	 */
	// {@link #scopeName} 的别名。
	@AliasFor("scopeName")
	String value() default "";

	/**
	 * Specifies the name of the scope to use for the annotated component/bean.
	 * <p>Defaults to an empty string ({@code ""}) which implies
	 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON SCOPE_SINGLETON}.
	 * @since 4.2
	 * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
	 * @see ConfigurableBeanFactory#SCOPE_SINGLETON
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_SESSION
	 * @see #value
	 */
	// 指定带注释的 component/bean 使用的范围名称。
	// <p>默认为空字符串 ({@code ""})，这意味着 {@link ConfigurableBeanFactory#SCOPE_SINGLETON SCOPE_SINGLETON}。
	@AliasFor("value")
	String scopeName() default "";

	/**
	 * Specifies whether a component should be configured as a scoped proxy
	 * and if so, whether the proxy should be interface-based or subclass-based.
	 * <p>Defaults to {@link ScopedProxyMode#DEFAULT}, which typically indicates
	 * that no scoped proxy should be created unless a different default
	 * has been configured at the component-scan instruction level.
	 * <p>Analogous to {@code <aop:scoped-proxy/>} support in Spring XML.
	 * @see ScopedProxyMode
	 */
	// 指定组件是否应配置为作用域代理，如果是，则指定代理是基于接口的还是基于子类的。
	// <p>默认值为 {@link ScopedProxyMode#DEFAULT}，通常表示除非在组件扫描指令级别配置了其他默认值，否则不应创建作用域代理。
	// <p>类似于 Spring XML 中的 {@code <aop:scoped-proxy/>} 支持。
	ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;

}
