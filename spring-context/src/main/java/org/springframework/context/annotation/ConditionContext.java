/*
 * Copyright 2002-2020 the original author or authors.
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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

/**
 * Context information for use by {@link Condition} implementations.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
// {@link Condition} 实现使用的上下文信息。
public interface ConditionContext {

	/**
	 * Return the {@link BeanDefinitionRegistry} that will hold the bean definition
	 * should the condition match.
	 * @throws IllegalStateException if no registry is available (which is unusual:
	 * only the case with a plain {@link ClassPathScanningCandidateComponentProvider})
	 */
	// 如果条件匹配，则返回用于保存 bean 定义的 {@link BeanDefinitionRegistry}。
	// 如果没有可用的注册表，则抛出 IllegalStateException（这种情况很不常见：
	// 只有使用普通的 {@link ClassPathScanningCandidateComponentProvider} 时才会出现）。
	BeanDefinitionRegistry getRegistry();

	/**
	 * Return the {@link ConfigurableListableBeanFactory} that will hold the bean
	 * definition should the condition match, or {@code null} if the bean factory is
	 * not available (or not downcastable to {@code ConfigurableListableBeanFactory}).
	 */
	// 如果条件匹配，则返回用于保存 bean 定义的 {@link ConfigurableListableBeanFactory}；
	// 如果 bean 工厂不可用（或无法向下转换为 {@code ConfigurableListableBeanFactory}），则返回 {@code null}。
	@Nullable
	ConfigurableListableBeanFactory getBeanFactory();

	/**
	 * Return the {@link Environment} for which the current application is running.
	 */
	// 返回当前应用程序正在运行的 {@link Environment}。
	Environment getEnvironment();

	/**
	 * Return the {@link ResourceLoader} currently being used.
	 */
	// 返回当前正在使用的 {@link ResourceLoader}。
	ResourceLoader getResourceLoader();

	/**
	 * Return the {@link ClassLoader} that should be used to load additional classes
	 * (only {@code null} if even the system ClassLoader isn't accessible).
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	// 返回用于加载其他类的 {@link ClassLoader}（如果系统 ClassLoader 都无法访问，则返回 {@code null}）。
	// @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	@Nullable
	ClassLoader getClassLoader();

}
