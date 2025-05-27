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

package org.springframework.context;

/**
 * Interface that encapsulates event publication functionality.
 *
 * <p>Serves as a super-interface for {@link ApplicationContext}.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 1.1.1
 * @see ApplicationContext
 * @see ApplicationEventPublisherAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.event.EventPublicationInterceptor
 * @see org.springframework.transaction.event.TransactionalApplicationListener
 */
// 封装事件发布功能的接口。
//
// <p>作为 {@link ApplicationContext} 的父接口。
@FunctionalInterface
public interface ApplicationEventPublisher {

	/**
	 * Notify all <strong>matching</strong> listeners registered with this
	 * application of an application event. Events may be framework events
	 * (such as ContextRefreshedEvent) or application-specific events.
	 * <p>Such an event publication step is effectively a hand-off to the
	 * multicaster and does not imply synchronous/asynchronous execution
	 * or even immediate execution at all. Event listeners are encouraged
	 * to be as efficient as possible, individually using asynchronous
	 * execution for longer-running and potentially blocking operations.
	 * <p>For usage in a reactive call stack, include event publication
	 * as a simple hand-off:
	 * {@code Mono.fromRunnable(() -> eventPublisher.publishEvent(...))}.
	 * As with any asynchronous execution, thread-local data is not going
	 * to be available for reactive listener methods. All state which is
	 * necessary to process the event needs to be included in the event
	 * instance itself.
	 * <p>For the convenient inclusion of the current transaction context
	 * in a reactive hand-off, consider using
	 * {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(Function)}.
	 * For thread-bound transactions, this is not necessary since the
	 * state will be implicitly available through thread-local storage.
	 * @param event the event to publish
	 * @see #publishEvent(Object)
	 * @see ApplicationListener#supportsAsyncExecution()
	 * @see org.springframework.context.event.ContextRefreshedEvent
	 * @see org.springframework.context.event.ContextClosedEvent
	 */
	// 将应用程序事件通知给在此应用程序中注册的所有<strong>匹配</strong>的监听器。
	// 事件可以是框架事件（例如 ContextRefreshedEvent），也可以是应用程序特定的事件。
	// <p>此类事件发布步骤实际上是向多播器移交，并不意味着同步/异步执行，甚至根本不会立即执行。
	// 我们鼓励事件监听器尽可能高效，对于运行时间较长且可能阻塞的操作，最好单独使用异步执行。
	// <p>在响应式调用栈中使用时，请将事件发布作为简单的移交操作：{@code Mono.fromRunnable(() -> eventPublisher.publishEvent(...))}。
	// 与任何异步执行一样，线程本地数据将不可用于响应式监听器方法。处理事件所需的所有状态都需要包含在事件实例本身中。
	// <p>为了方便地在响应式切换中包含当前事务上下文，
	// 可以考虑使用 {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(Function)}。
	// 对于线程绑定的事务，无需这样做，因为状态将通过线程本地存储隐式获取。
	// @param event 要发布的事件
	default void publishEvent(ApplicationEvent event) {
		publishEvent((Object) event);
	}

	/**
	 * Notify all <strong>matching</strong> listeners registered with this
	 * application of an event.
	 * <p>If the specified {@code event} is not an {@link ApplicationEvent},
	 * it is wrapped in a {@link PayloadApplicationEvent}.
	 * <p>Such an event publication step is effectively a hand-off to the
	 * multicaster and does not imply synchronous/asynchronous execution
	 * or even immediate execution at all. Event listeners are encouraged
	 * to be as efficient as possible, individually using asynchronous
	 * execution for longer-running and potentially blocking operations.
	 * <p>For the convenient inclusion of the current transaction context
	 * in a reactive hand-off, consider using
	 * {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(Object)}.
	 * For thread-bound transactions, this is not necessary since the
	 * state will be implicitly available through thread-local storage.
	 * @param event the event to publish
	 * @since 4.2
	 * @see #publishEvent(ApplicationEvent)
	 * @see PayloadApplicationEvent
	 */
	// 将事件通知到此应用中所有已注册的<strong>匹配</strong>监听器。
	// <p>如果指定的 {@code event} 不是 {@link ApplicationEvent}，则会将其包装在 {@link PayloadApplicationEvent} 中。
	// <p>此类事件发布步骤实际上是将事件移交给多播器，并不意味着同步/异步执行，甚至根本不会立即执行。
	// 我们鼓励事件监听器尽可能高效，对于运行时间较长且可能阻塞的操作，单独使用异步执行。
	// <p>为了方便地在响应式移交中包含当前事务上下文，请考虑使用 {@link org.springframework.transaction.reactive.TransactionalEventPublisher#publishEvent(Object)}。
	// 对于线程绑定的事务，这不是必需的，因为状态将通过线程本地存储隐式获取。
	// @param event 要发布的事件
	void publishEvent(Object event);

}
