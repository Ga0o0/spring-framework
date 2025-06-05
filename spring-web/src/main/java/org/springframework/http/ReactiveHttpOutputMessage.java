/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.http;

import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;

/**
 * A "reactive" HTTP output message that accepts output as a {@link Publisher}.
 *
 * <p>Typically implemented by an HTTP request on the client-side or an
 * HTTP response on the server-side.
 *
 * @author Arjen Poutsma
 * @author Sebastien Deleuze
 * @since 5.0
 */
// 一种“响应式”HTTP 输出消息，以 {@link Publisher} 的形式接受输出。
//
// <p>通常由客户端的 HTTP 请求或服务器端的 HTTP 响应实现。
public interface ReactiveHttpOutputMessage extends HttpMessage {

	/**
	 * Return a {@link DataBufferFactory} that can be used to create the body.
	 * @return a buffer factory
	 * @see #writeWith(Publisher)
	 */
	// 返回一个可用于创建主体的 {@link DataBufferFactory}。
	// @return 缓冲区工厂
	DataBufferFactory bufferFactory();

	/**
	 * Register an action to apply just before the HttpOutputMessage is committed.
	 * <p><strong>Note:</strong> the supplied action must be properly deferred,
	 * e.g. via {@link Mono#defer} or {@link Mono#fromRunnable}, to ensure it's
	 * executed in the right order, relative to other actions.
	 * @param action the action to apply
	 */
	// 在提交 HttpOutputMessage 之前注册一个要应用的操作。
	// <p><strong>注意：</strong>提供的操作必须正确延迟，例如通过 {@link Mono#defer} 或 {@link Mono#fromRunnable}，以确保它相对于其他操作按正确的顺序执行。
	// @param action 要应用的操作
	void beforeCommit(Supplier<? extends Mono<Void>> action);

	/**
	 * Whether the HttpOutputMessage is committed.
	 */
	// HttpOutputMessage 是否已提交。
	boolean isCommitted();

	/**
	 * Use the given {@link Publisher} to write the body of the message to the
	 * underlying HTTP layer.
	 * @param body the body content publisher
	 * @return a {@link Mono} that indicates completion or error
	 */
	// 使用给定的 {@link Publisher} 将消息正文写入底层 HTTP 层。
	// @param body 正文内容发布者
	// @return 一个 {@link Mono} 表示完成或错误
	Mono<Void> writeWith(Publisher<? extends DataBuffer> body);

	/**
	 * Use the given {@link Publisher} of {@code Publishers} to write the body
	 * of the HttpOutputMessage to the underlying HTTP layer, flushing after
	 * each {@code Publisher<DataBuffer>}.
	 * @param body the body content publisher
	 * @return a {@link Mono} that indicates completion or error
	 */
	// 使用给定的 {@code Publishers} 中的 {@link Publisher} 将 HttpOutputMessage 的主体写入底层 HTTP 层，
	// 并在每个 {@code Publisher<DataBuffer>} 之后刷新。
	// @param body 主体内容发布者
	// @return 一个 {@link Mono} 表示完成或错误
	Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body);

	/**
	 * Indicate that message handling is complete, allowing for any cleanup or
	 * end-of-processing tasks to be performed such as applying header changes
	 * made via {@link #getHeaders()} to the underlying HTTP message (if not
	 * applied already).
	 * <p>This method should be automatically invoked at the end of message
	 * processing so typically applications should not have to invoke it.
	 * If invoked multiple times it should have no side effects.
	 * @return a {@link Mono} that indicates completion or error
	 */
	// 指示消息处理已完成，允许执行任何清理或结束处理任务，例如将通过 {@link #getHeaders()} 进行的标头更改应用于底层 HTTP 消息（如果尚未应用）。
	// <p>此方法应在消息处理结束时自动调用，因此应用程序通常不必调用它。如果多次调用，应该不会产生副作用。
	// @return 一个 {@link Mono}，指示完成或错误
	Mono<Void> setComplete();

}
