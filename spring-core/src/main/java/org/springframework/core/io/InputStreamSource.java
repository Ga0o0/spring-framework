/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple interface for objects that are sources for an {@link InputStream}.
 *
 * <p>This is the base interface for Spring's more extensive {@link Resource} interface.
 *
 * <p>For single-use streams, {@link InputStreamResource} can be used for any
 * given {@code InputStream}. Spring's {@link ByteArrayResource} or any
 * file-based {@code Resource} implementation can be used as a concrete
 * instance, allowing one to read the underlying content stream multiple times.
 * This makes this interface useful as an abstract content source for mail
 * attachments, for example.
 *
 * @author Juergen Hoeller
 * @since 20.01.2004
 * @see java.io.InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
// 一个简单的接口，用于作为 {@link InputStream} 源的对象。
//
// <p>这是 Spring 更强大的 {@link Resource} 接口的基础接口。
//
// <p>对于一次性使用的流，{@link InputStreamResource} 可用于任何给定的 {@code InputStream}。
// Spring 的 {@link ByteArrayResource} 或任何基于文件的 {@code Resource} 实现都可以用作具体实例，
// 允许多次读取底层内容流。这使得此接口可用作邮件附件等内容的抽象源。
@FunctionalInterface
public interface InputStreamSource {

	/**
	 * Return an {@link InputStream} for the content of an underlying resource.
	 * <p>It is usually expected that every such call creates a <i>fresh</i> stream.
	 * <p>This requirement is particularly important when you consider an API such
	 * as JavaMail, which needs to be able to read the stream multiple times when
	 * creating mail attachments. For such a use case, it is <i>required</i>
	 * that each {@code getInputStream()} call returns a fresh stream.
	 * @return the input stream for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource does not exist
	 * @throws IOException if the content stream could not be opened
	 * @see Resource#isReadable()
	 * @see Resource#isOpen()
	 */
	// 返回底层资源内容的 {@link InputStream}。
	// <p>通常预期每次此类调用都会创建一个<i>新鲜的</i>流。
	// <p>当考虑像 JavaMail 这样的 API 时，此要求尤为重要，因为它需要在创建邮件附件时多次读取该流。
	// 对于这样的用例，<i>要求</i>每次 {@code getInputStream()} 调用都返回一个新鲜的流。
	// @return 底层资源的输入流（不能为 {@code null}）
	// @throws java.io.FileNotFoundException 如果底层资源不存在，则抛出 java.io.FileNotFoundException
	// @throws java.io.IOException 如果无法打开内容流，则抛出 IOException
	InputStream getInputStream() throws IOException;

}
