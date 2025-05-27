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

package org.springframework.context.event;

import java.util.function.Predicate;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects that can manage a number of
 * {@link ApplicationListener} objects and publish events to them.
 *
 * <p>An {@link org.springframework.context.ApplicationEventPublisher}, typically
 * a Spring {@link org.springframework.context.ApplicationContext}, can use an
 * {@code ApplicationEventMulticaster} as a delegate for actually publishing events.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see ApplicationListener
 */
// 需要由对象实现的接口，该接口可以管理多个 {@link ApplicationListener} 对象并向其发布事件。
//
// <p>{@link org.springframework.context.ApplicationEventPublisher}
// （通常是 Spring {@link org.springframework.context.ApplicationContext}）可以
// 使用 {@code ApplicationEventMulticaster} 作为委托来实际发布事件。
public interface ApplicationEventMulticaster {

	/**
	 * Add a listener to be notified of all events.
	 * @param listener the listener to add
	 * @see #removeApplicationListener(ApplicationListener)
	 * @see #removeApplicationListeners(Predicate)
	 */
	// 添加一个监听器，用于接收所有事件的通知。
	// @param listener 要添加的监听器
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * Add a listener bean to be notified of all events.
	 * @param listenerBeanName the name of the listener bean to add
	 * @see #removeApplicationListenerBean(String)
	 * @see #removeApplicationListenerBeans(Predicate)
	 */
	// 添加一个监听器 Bean，用于接收所有事件的通知。
	// @param listenerBeanName 要添加的监听器 Bean 的名称
	void addApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove a listener from the notification list.
	 * @param listener the listener to remove
	 * @see #addApplicationListener(ApplicationListener)
	 * @see #removeApplicationListeners(Predicate)
	 */
	// 从通知列表中移除一个监听器。
	// @param listener 要移除的监听器
	void removeApplicationListener(ApplicationListener<?> listener);

	/**
	 * Remove a listener bean from the notification list.
	 * @param listenerBeanName the name of the listener bean to remove
	 * @see #addApplicationListenerBean(String)
	 * @see #removeApplicationListenerBeans(Predicate)
	 */
	// 从通知列表中移除一个监听器 bean。
	// @param listenerBeanName 需要移除的监听器 bean 的名称
	void removeApplicationListenerBean(String listenerBeanName);

	/**
	 * Remove all matching listeners from the set of registered
	 * {@code ApplicationListener} instances (which includes adapter classes
	 * such as {@link ApplicationListenerMethodAdapter}, e.g. for annotated
	 * {@link EventListener} methods).
	 * <p>Note: This just applies to instance registrations, not to listeners
	 * registered by bean name.
	 * @param predicate the predicate to identify listener instances to remove,
	 * e.g. checking {@link SmartApplicationListener#getListenerId()}
	 * @since 5.3.5
	 * @see #addApplicationListener(ApplicationListener)
	 * @see #removeApplicationListener(ApplicationListener)
	 */
	// 从已注册的 {@code ApplicationListener} 实例集合（包括适配器类，例如 {@link ApplicationListenerMethodAdapter，
	// 例如带注解的 {@link EventListener} 方法）中移除所有匹配的监听器。
	// <p>注意：这仅适用于实例注册，不适用于通过 Bean 名称注册的监听器。
	// @param predicate 用于标识要移除的监听器实例的谓词，例如检查 {@link SmartApplicationListener#getListenerId()}
	void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate);

	/**
	 * Remove all matching listener beans from the set of registered
	 * listener bean names (referring to bean classes which in turn
	 * implement the {@link ApplicationListener} interface directly).
	 * <p>Note: This just applies to bean name registrations, not to
	 * programmatically registered {@code ApplicationListener} instances.
	 * @param predicate the predicate to identify listener bean names to remove
	 * @since 5.3.5
	 * @see #addApplicationListenerBean(String)
	 * @see #removeApplicationListenerBean(String)
	 */
	// 从已注册的侦听器 Bean 名称集合（指直接实现 {@link ApplicationListener} 接口的 Bean 类）中移除所有匹配的侦听器 Bean。
	// <p>注意：这仅适用于 Bean 名称注册，不适用于以编程方式注册的 {@code ApplicationListener} 实例。
	// @param predicate 用于标识要移除的侦听器 Bean 名称的谓词
	void removeApplicationListenerBeans(Predicate<String> predicate);

	/**
	 * Remove all listeners registered with this multicaster.
	 * <p>After a remove call, the multicaster will perform no action
	 * on event notification until new listeners are registered.
	 * @see #removeApplicationListeners(Predicate)
	 */
	// 删除在此多播器上注册的所有监听器。
	// <p>调用 remove 后，多播器将不会对事件通知执行任何操作，直到注册新的监听器为止。
	void removeAllListeners();

	/**
	 * Multicast the given application event to appropriate listeners.
	 * <p>Consider using {@link #multicastEvent(ApplicationEvent, ResolvableType)}
	 * if possible as it provides better support for generics-based events.
	 * <p>If a matching {@code ApplicationListener} does not support asynchronous
	 * execution, it must be run within the calling thread of this multicast call.
	 * @param event the event to multicast
	 * @see ApplicationListener#supportsAsyncExecution()
	 */
	// 将给定的应用程序事件多播到适当的监听器。
	// <p>如果可能，请考虑使用 {@link #multicastEvent(ApplicationEvent, ResolvableType)}，因为它可以为基于泛型的事件提供更好的支持。
	// <p>如果匹配的 {@code ApplicationListener} 不支持异步执行，则必须在此多播调用的调用线程中运行。
	// @param event 要多播的事件
	void multicastEvent(ApplicationEvent event);

	/**
	 * Multicast the given application event to appropriate listeners.
	 * <p>If the {@code eventType} is {@code null}, a default type is built
	 * based on the {@code event} instance.
	 * <p>If a matching {@code ApplicationListener} does not support asynchronous
	 * execution, it must be run within the calling thread of this multicast call.
	 * @param event the event to multicast
	 * @param eventType the type of event (can be {@code null})
	 * @since 4.2
	 * @see ApplicationListener#supportsAsyncExecution()
	 */
	// 将给定的应用程序事件多播到相应的监听器。
	// <p>如果 {@code eventType} 为 {@code null}，则系统会根据 {@code event} 实例构建默认类型。
	// <p>如果匹配的 {@code ApplicationListener} 不支持异步执行，则它必须在本次多播调用的调用线程中运行。
	// @param event 要多播的事件
	// @param eventType 事件类型（可以为 {@code null}）
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);

}
