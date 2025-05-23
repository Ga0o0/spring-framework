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

package org.springframework.core.env;

/**
 * Interface representing the environment in which the current application is running.
 * Models two key aspects of the application environment: <em>profiles</em> and
 * <em>properties</em>. Methods related to property access are exposed via the
 * {@link PropertyResolver} superinterface.
 *
 * <p>A <em>profile</em> is a named, logical group of bean definitions to be registered
 * with the container only if the given profile is <em>active</em>. Beans may be assigned
 * to a profile whether defined in XML or via annotations; see the spring-beans 3.1 schema
 * or the {@link org.springframework.context.annotation.Profile @Profile} annotation for
 * syntax details. The role of the {@code Environment} object with relation to profiles is
 * in determining which profiles (if any) are currently {@linkplain #getActiveProfiles
 * active}, and which profiles (if any) should be {@linkplain #getDefaultProfiles active
 * by default}.
 *
 * <p><em>Properties</em> play an important role in almost all applications, and may
 * originate from a variety of sources: properties files, JVM system properties, system
 * environment variables, JNDI, servlet context parameters, ad-hoc Properties objects,
 * Maps, and so on. The role of the {@code Environment} object with relation to properties
 * is to provide the user with a convenient service interface for configuring property
 * sources and resolving properties from them.
 *
 * <p>Beans managed within an {@code ApplicationContext} may register to be {@link
 * org.springframework.context.EnvironmentAware EnvironmentAware} or {@code @Inject} the
 * {@code Environment} in order to query profile state or resolve properties directly.
 *
 * <p>In most cases, however, application-level beans should not need to interact with the
 * {@code Environment} directly but instead may request to have {@code ${...}} property
 * values replaced by a property placeholder configurer such as
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer}, which itself is {@code EnvironmentAware} and
 * registered by default when using {@code <context:property-placeholder/>}.
 *
 * <p>Configuration of the {@code Environment} object must be done through the
 * {@code ConfigurableEnvironment} interface, returned from all
 * {@code AbstractApplicationContext} subclass {@code getEnvironment()} methods. See
 * {@link ConfigurableEnvironment} Javadoc for usage examples demonstrating manipulation
 * of property sources prior to application context {@code refresh()}.
 *
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.1
 * @see PropertyResolver
 * @see EnvironmentCapable
 * @see ConfigurableEnvironment
 * @see AbstractEnvironment
 * @see StandardEnvironment
 * @see org.springframework.context.EnvironmentAware
 * @see org.springframework.context.ConfigurableApplicationContext#getEnvironment
 * @see org.springframework.context.ConfigurableApplicationContext#setEnvironment
 * @see org.springframework.context.support.AbstractApplicationContext#createEnvironment
 */
// 表示当前应用运行环境的接口。它对应用环境的两个关键方面进行建模：<em>profiles</em> 和 <em>properties</em>。
// 与属性访问相关的方法通过 {@link PropertyResolver} 超接口公开。
//
// <p><em>profile</em> 是一个命名的、逻辑上的 bean 定义组，只有当给定的配置文件处于 <em>active</em> 状态时，它才会向容器注册。
// bean 可以通过 XML 或注解的方式分配给配置文件；有关语法详细信息，请参阅 spring-beans 3.1 schema
// 或 {@link org.springframework.context.annotation.Profile @Profile} 注解。
// 与配置文件相关的 {@code Environment} 对象的作用是确定哪些配置文件（如果有）当前处于 {@linkplain #getActiveProfiles 状态}，
// 以及哪些配置文件（如果有）应该默认处于 {@linkplain #getDefaultProfiles 状态}。
//
// <p><em>属性</em>在几乎所有应用程序中都扮演着重要的角色，并且可能来自各种来源：
// 属性文件、JVM 系统属性、系统环境变量、JNDI、Servlet 上下文参数、临时属性对象、Map 等等。
// {@code Environment} 对象与属性相关的作用是为用户提供一个便捷的服务接口，用于配置属性源并从中解析属性。
//
// <p>在 {@code ApplicationContext} 中管理的 Bean 可以注册为
// {@link org.springframework.context.EnvironmentAware EnvironmentAware}
// 或 {@code @Inject} {@code Environment}，以便查询配置文件状态或直接解析属性。
//
// <p>然而，在大多数情况下，应用级 bean 不需要直接与 {@code Environment} 交互，
// 而是可以请求使用属性占位符配置器（例如 {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer PropertySourcesPlaceholderConfigurer}）替换 {@code ${...}} 属性值。
// 该配置器本身是 {@code EnvironmentAware}，在使用 {@code <context:property-placeholder/>} 时默认注册。
//
// <p>{@code Environment} 对象的配置必须通过 {@code ConfigurableEnvironment} 接口完成，
// 该接口由所有 {@code AbstractApplicationContext} 子类 {@code getEnvironment()} 方法返回。
// 有关在应用上下文 {@code refresh()} 之前操作属性源的示例，请参阅 {@link ConfigurableEnvironment} Javadoc。
public interface Environment extends PropertyResolver {

	/**
	 * Return the set of profiles explicitly made active for this environment. Profiles
	 * are used for creating logical groupings of bean definitions to be registered
	 * conditionally, for example based on deployment environment. Profiles can be
	 * activated by setting {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 * "spring.profiles.active"} as a system property or by calling
	 * {@link ConfigurableEnvironment#setActiveProfiles(String...)}.
	 * <p>If no profiles have explicitly been specified as active, then any
	 * {@linkplain #getDefaultProfiles() default profiles} will automatically be activated.
	 * @see #getDefaultProfiles
	 * @see ConfigurableEnvironment#setActiveProfiles
	 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
	 */
	// 返回为此环境明确激活的一组配置文件。
	// 配置文件用于创建有条件注册的 bean 定义的逻辑分组，例如基于部署环境。
	// 可以通过将 {@linkplain AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME "spring.profiles.active"}
	// 设置为系统属性或调用 {@link ConfigurableEnvironment#setActiveProfiles(String...)} 来激活配置文件。
	// <p>如果没有明确指定配置文件为活动配置文件，则任何 {@linkplain #getDefaultProfiles() 默认配置文件} 都将自动激活。
	String[] getActiveProfiles();

	/**
	 * Return the set of profiles to be active by default when no active profiles have
	 * been set explicitly.
	 * @see #getActiveProfiles
	 * @see ConfigurableEnvironment#setDefaultProfiles
	 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
	 */
	// 当未明确设置活动配置文件时，返回默认活动的配置文件集。
	String[] getDefaultProfiles();

	/**
	 * Determine whether one of the given profile expressions matches the
	 * {@linkplain #getActiveProfiles() active profiles} &mdash; or in the case
	 * of no explicit active profiles, whether one of the given profile expressions
	 * matches the {@linkplain #getDefaultProfiles() default profiles}.
	 * <p>Profile expressions allow for complex, boolean profile logic to be
	 * expressed &mdash; for example {@code "p1 & p2"}, {@code "(p1 & p2) | p3"},
	 * etc. See {@link Profiles#of(String...)} for details on the supported
	 * expression syntax.
	 * <p>This method is a convenient shortcut for
	 * {@code env.acceptsProfiles(Profiles.of(profileExpressions))}.
	 * @since 5.3.28
	 * @see Profiles#of(String...)
	 * @see #acceptsProfiles(Profiles)
	 */
	// 确定给定的配置文件表达式是否与 {@linkplain #getActiveProfiles() 活动配置文件} 匹配 -
	// 或者，如果没有显式指定活动配置文件，则确定给定的配置文件表达式是否与 {@linkplain #getDefaultProfiles() 默认配置文件} 匹配。
	// <p>配置文件表达式允许表达复杂的布尔配置文件逻辑 - 例如 {@code "p1 & p2"}、{@code "(p1 & p2) | p3"} 等。
	// 有关支持的表达式语法的详细信息，请参阅 {@link Profiles#of(String...)}。
	// <p>此方法是 {@code env.acceptsProfiles(Profiles.of(profileExpressions))} 的便捷快捷方式。
	default boolean matchesProfiles(String... profileExpressions) {
		return acceptsProfiles(Profiles.of(profileExpressions));
	}

	/**
	 * Determine whether one or more of the given profiles is active &mdash; or
	 * in the case of no explicit {@linkplain #getActiveProfiles() active profiles},
	 * whether one or more of the given profiles is included in the set of
	 * {@linkplain #getDefaultProfiles() default profiles}.
	 * <p>If a profile begins with '!' the logic is inverted, meaning this method
	 * will return {@code true} if the given profile is <em>not</em> active. For
	 * example, {@code env.acceptsProfiles("p1", "!p2")} will return {@code true}
	 * if profile 'p1' is active or 'p2' is not active.
	 * @throws IllegalArgumentException if called with a {@code null} array, an
	 * empty array, zero arguments or if any profile is {@code null}, empty, or
	 * whitespace only
	 * @see #getActiveProfiles
	 * @see #getDefaultProfiles
	 * @see #matchesProfiles(String...)
	 * @see #acceptsProfiles(Profiles)
	 * @deprecated as of 5.1 in favor of {@link #acceptsProfiles(Profiles)} or
	 * {@link #matchesProfiles(String...)}
	 */
	// 确定给定的配置文件中是否有一个或多个处于活动状态 -
	// 或者，如果没有显式指定 {@linkplain #getActiveProfiles() 活动配置文件}，
	// 则确定给定的配置文件中是否有一个或多个包含在 {@linkplain #getDefaultProfiles() 默认配置文件} 集合中。
	// <p>如果配置文件以“!”开头，则逻辑相反，这意味着如果给定的配置文件<em>未</em>处于活动状态，则此方法将返回 {@code true}。
	// 例如，如果配置文件“p1”处于活动状态或“p2”未处于活动状态，则 {@code env.acceptsProfiles("p1", "!p2")} 将返回 {@code true}。
	// @throws IllegalArgumentException 如果使用 {@code null} 数组、空数组、零个参数或任何配置文件为 {@code null}、空或仅包含空格，则抛出 IllegalArgumentException
	@Deprecated
	boolean acceptsProfiles(String... profiles);

	/**
	 * Determine whether the given {@link Profiles} predicate matches the
	 * {@linkplain #getActiveProfiles() active profiles} &mdash; or in the case
	 * of no explicit active profiles, whether the given {@code Profiles} predicate
	 * matches the {@linkplain #getDefaultProfiles() default profiles}.
	 * <p>If you wish provide profile expressions directly as strings, use
	 * {@link #matchesProfiles(String...)} instead.
	 * @since 5.1
	 * @see #matchesProfiles(String...)
	 * @see Profiles#of(String...)
	 */
	// 判断给定的 {@link Profiles} 谓词是否与 {@linkplain #getActiveProfiles() 活动配置文件匹配 -
	// 或者，如果没有显式指定活动配置文件，则判断给定的 {@code Profiles} 谓词
	// 是否与 {@linkplain #getDefaultProfiles() 默认配置文件匹配。
	// <p>如果您希望直接以字符串形式提供配置文件表达式，请使用 {@link #matchesProfiles(String...)}。
	boolean acceptsProfiles(Profiles profiles);

}
