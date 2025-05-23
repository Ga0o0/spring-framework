/*
 * Copyright 2002-2021 the original author or authors.
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
 * {@link Environment} implementation suitable for use in 'standard' (i.e. non-web)
 * applications.
 *
 * <p>In addition to the usual functions of a {@link ConfigurableEnvironment} such as
 * property resolution and profile-related operations, this implementation configures two
 * default property sources, to be searched in the following order:
 * <ul>
 * <li>{@linkplain AbstractEnvironment#getSystemProperties() system properties}
 * <li>{@linkplain AbstractEnvironment#getSystemEnvironment() system environment variables}
 * </ul>
 *
 * That is, if the key "xyz" is present both in the JVM system properties as well as in
 * the set of environment variables for the current process, the value of key "xyz" from
 * system properties will return from a call to {@code environment.getProperty("xyz")}.
 * This ordering is chosen by default because system properties are per-JVM, while
 * environment variables may be the same across many JVMs on a given system.  Giving
 * system properties precedence allows for overriding of environment variables on a
 * per-JVM basis.
 *
 * <p>These default property sources may be removed, reordered, or replaced; and
 * additional property sources may be added using the {@link MutablePropertySources}
 * instance available from {@link #getPropertySources()}. See
 * {@link ConfigurableEnvironment} Javadoc for usage examples.
 *
 * <p>See {@link SystemEnvironmentPropertySource} javadoc for details on special handling
 * of property names in shell environments (e.g. Bash) that disallow period characters in
 * variable names.
 *
 * @author Chris Beams
 * @author Phillip Webb
 * @since 3.1
 * @see ConfigurableEnvironment
 * @see SystemEnvironmentPropertySource
 * @see org.springframework.web.context.support.StandardServletEnvironment
 */
// {@link Environment} 实现适用于“标准”（即非 Web）应用程序。
//
// <p>除了 {@link ConfigurableEnvironment} 的常规功能（例如属性解析和与配置文件相关的操作）之外，此实现还配置了两个默认属性源，按以下顺序搜索：
// <ul>
// <li>{@linkplain AbstractEnvironment#getSystemProperties() 系统属性}
// <li>{@linkplain AbstractEnvironment#getSystemEnvironment() 系统环境变量}
// </ul>
//
// 也就是说，如果键“xyz”同时存在于 JVM 系统属性和当前进程的环境变量集合中
// ，则调用 {@code environment.getProperty("xyz")} 将返回系统属性中键“xyz”的值。
// 默认选择此顺序，因为系统属性是针对每个 JVM 的，而环境变量在给定系统上的多个 JVM 之间可能相同。
// 赋予系统属性优先级允许基于每个 JVM 覆盖环境变量。
//
// <p>这些默认属性源可以被移除、重新排序或替换；可以使用 {@link #getPropertySources()} 提供的 {@link MutablePropertySources} 实例添加其他属性源。
// 有关使用示例，请参阅 {@link ConfigurableEnvironment} Javadoc。
//
// <p>有关在 Shell 环境（例如 Bash）中禁止变量名使用句点字符的属性名特殊处理的详细信息，请参阅 {@link SystemEnvironmentPropertySource} Javadoc。
public class StandardEnvironment extends AbstractEnvironment {

	/** System environment property source name: {@value}. */
	// 系统环境属性源名称：{@value}。
	public static final String SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME = "systemEnvironment";

	/** JVM system properties property source name: {@value}. */
	// JVM 系统属性属性源名称：{@value}。
	public static final String SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "systemProperties";


	/**
	 * Create a new {@code StandardEnvironment} instance with a default
	 * {@link MutablePropertySources} instance.
	 */
	// 使用默认的 {@link MutablePropertySources} 实例创建一个新的 {@code StandardEnvironment} 实例。
	public StandardEnvironment() {
	}

	/**
	 * Create a new {@code StandardEnvironment} instance with a specific
	 * {@link MutablePropertySources} instance.
	 * @param propertySources property sources to use
	 * @since 5.3.4
	 */
	protected StandardEnvironment(MutablePropertySources propertySources) {
		super(propertySources);
	}


	/**
	 * Customize the set of property sources with those appropriate for any standard
	 * Java environment:
	 * <ul>
	 * <li>{@value #SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME}
	 * <li>{@value #SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME}
	 * </ul>
	 * <p>Properties present in {@value #SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME} will
	 * take precedence over those in {@value #SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME}.
	 * @see AbstractEnvironment#customizePropertySources(MutablePropertySources)
	 * @see #getSystemProperties()
	 * @see #getSystemEnvironment()
	 */
	// 使用适用于任何标准的属性源自定义属性源集
	// Java 环境：
	// <ul>
	// <li>{@value #SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME}
	// <li>{@value #SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME}
	// </ul>
	// <p>{@value #SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME} 中的属性优先于 {@value #SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME} 中的属性。
	@Override
	protected void customizePropertySources(MutablePropertySources propertySources) {
		propertySources.addLast(
				new PropertiesPropertySource(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, getSystemProperties()));
		propertySources.addLast(
				new SystemEnvironmentPropertySource(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, getSystemEnvironment()));
	}

}
