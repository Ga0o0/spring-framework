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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * {@link org.springframework.context.ApplicationListener} decorator that filters
 * events from a specified event source, invoking its delegate listener for
 * matching {@link org.springframework.context.ApplicationEvent} objects only.
 *
 * <p>Can also be used as base class, overriding the {@link #onApplicationEventInternal}
 * method instead of specifying a delegate listener.
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0.5
 */
// {@link org.springframework.context.ApplicationListener} 装饰器，用于过滤来自指定事件源的事件，
// 仅对匹配的 {@link org.springframework.context.ApplicationEvent} 对象调用其委托侦听器。
//
// <p>也可用作基类，覆盖 {@link #onApplicationEventInternal} 方法，而不必指定委托侦听器。
public class SourceFilteringListener implements GenericApplicationListener {

	private final Object source;

	@Nullable
	private GenericApplicationListener delegate;


	/**
	 * Create a SourceFilteringListener for the given event source.
	 * @param source the event source that this listener filters for,
	 * only processing events from this source
	 * @param delegate the delegate listener to invoke with event
	 * from the specified source
	 */
	// 为给定的事件源创建一个 SourceFilteringListener。
	// @param source 此监听器过滤的事件源，仅处理来自此源的事件
	// @param delegate 委托监听器，用于处理来自指定源的事件
	public SourceFilteringListener(Object source, ApplicationListener<?> delegate) {
		this.source = source;
		this.delegate = (delegate instanceof GenericApplicationListener gal ? gal :
				new GenericApplicationListenerAdapter(delegate));
	}

	/**
	 * Create a SourceFilteringListener for the given event source,
	 * expecting subclasses to override the {@link #onApplicationEventInternal}
	 * method (instead of specifying a delegate listener).
	 * @param source the event source that this listener filters for,
	 * only processing events from this source
	 */
	// 为给定的事件源创建一个 SourceFilteringListener，期望子类重写 {@link #onApplicationEventInternal} 方法（而不是指定委托监听器）。
	// @param source 此监听器过滤的事件源，仅处理来自此源的事件
	protected SourceFilteringListener(Object source) {
		this.source = source;
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event.getSource() == this.source) {
			onApplicationEventInternal(event);
		}
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		return (this.delegate == null || this.delegate.supportsEventType(eventType));
	}

	@Override
	public boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return (sourceType != null && sourceType.isInstance(this.source));
	}

	@Override
	public int getOrder() {
		return (this.delegate != null ? this.delegate.getOrder() : Ordered.LOWEST_PRECEDENCE);
	}

	@Override
	public String getListenerId() {
		return (this.delegate != null ? this.delegate.getListenerId() : "");
	}


	/**
	 * Actually process the event, after having filtered according to the
	 * desired event source already.
	 * <p>The default implementation invokes the specified delegate, if any.
	 * @param event the event to process (matching the specified source)
	 */
	// 根据所需事件源进行过滤后，实际处理事件。
	// <p>默认实现会调用指定的委托（如果有）。
	// @param event 要处理的事件（与指定的源匹配）
	protected void onApplicationEventInternal(ApplicationEvent event) {
		if (this.delegate == null) {
			throw new IllegalStateException(
					"Must specify a delegate object or override the onApplicationEventInternal method");
		}
		this.delegate.onApplicationEvent(event);
	}

}
