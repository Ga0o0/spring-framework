/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
// 为应用程序提供配置的中央接口。该接口在应用程序运行时为只读，但如果实现支持，则可以重新加载。
//
// <p>ApplicationContext 提供：
// <ul>
// <li>用于访问应用程序组件的 Bean 工厂方法。继承自 {@link org.springframework.beans.factory.ListableBeanFactory}。
// <li>以通用方式加载文件资源的能力。继承自 {@link org.springframework.core.io.ResourceLoader} 接口。
// <li>向已注册的监听器发布事件的能力。继承自 {@link ApplicationEventPublisher} 接口。
// <li>解析消息的能力，支持国际化。继承自 {@link MessageSource} 接口。
// <li>从父上下文继承。子上下文中的定义始终优先。例如，这意味着整个 Web 应用程序可以使用单个父上下文，而每个 servlet 都有自己的子上下文，并且这些子上下文独立于任何其他 servlet。
// </ul>
//
// <p>除了标准的 {@link org.springframework.beans.factory.BeanFactory} 生命周期功能外，
// ApplicationContext 实现还可以检测并调用 {@link ApplicationContextAware} Bean 以及 {@link ResourceLoaderAware}、
// {@link ApplicationEventPublisherAware} 和 {@link MessageSourceAware} Bean。
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * @return the unique id of the context, or {@code null} if none
	 */
	// 返回此应用上下文的唯一 ID。
	// @return 上下文的唯一 ID，如果没有则返回 {@code null}
	@Nullable
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * @return a name for the deployed application, or the empty String by default
	 */
	// 返回此上下文所属的已部署应用的名称。
	// @return 已部署应用的名称，默认为空字符串。
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 */
	// 返回此上下文的友好名称。
	// @return 此上下文的显示名称（永不返回 {@code null}）
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	// 返回此上下文首次加载的时间戳。
	// @return 此上下文首次加载的时间戳（毫秒）
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or {@code null} if there is no parent
	 */
	// 返回父上下文，如果没有父上下文且这是上下文层次结构的根，则返回 {@code null}。
	// @return 父上下文，如果没有父上下文，则返回 {@code null}
	@Nullable
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too. The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b> In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (e.g. if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	// 为该上下文公开 AutowireCapableBeanFactory 功能。
	// <p>应用程序代码通常不会使用此方法，除非用于初始化位于应用程序上下文之外的 Bean 实例，并将 Spring Bean 生命周期（全部或部分）应用于它们。
	// <p>或者，{@link ConfigurableApplicationContext} 接口公开的内部 BeanFactory 也提供对 {@link AutowireCapableBeanFactory} 接口的访问。
	// 该方法主要用作 ApplicationContext 接口上便捷的特定功能。
	// <p><b>注意：从 4.2 开始，此方法在应用程序上下文关闭后将始终抛出 IllegalStateException。</b>在当前的 Spring Framework 版本中，只有可刷新的应用程序上下文具有此行为；
	// 从 4.2 开始，所有应用程序上下文实现都必须遵循此行为。
	// @return 返回此上下文的 AutowireCapableBeanFactory
	// 如果上下文不支持 {@link AutowireCapableBeanFactory} 接口，或者尚未持有支持自动装配的 bean 工厂（例如，如果从未调用过 {@code refresh()}），
	// 或者上下文已关闭，则抛出 IllegalStateException 异常
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
