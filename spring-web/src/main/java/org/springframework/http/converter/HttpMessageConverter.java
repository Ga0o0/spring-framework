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

package org.springframework.http.converter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * Strategy interface for converting from and to HTTP requests and responses.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.0
 * @param <T> the converted object type
 */
// 用于转换 HTTP 请求和响应的策略接口。
// @param <T> 转换后的对象类型
public interface HttpMessageConverter<T> {

	/**
	 * Indicates whether the given class can be read by this converter.
	 * @param clazz the class to test for readability
	 * @param mediaType the media type to read (can be {@code null} if not specified);
	 * typically the value of a {@code Content-Type} header.
	 * @return {@code true} if readable; {@code false} otherwise
	 */
	// 指示此转换器是否可以读取给定的类。
	// @param clazz 需要测试可读性的类
	// @param mediaType 需要读取的媒体类型（如果未指定，则为 {@code null}）；通常为 {@code Content-Type} 标头的值。
	// @return 如果可读，则返回 {@code true}；否则返回 {@code false}
	boolean canRead(Class<?> clazz, @Nullable MediaType mediaType);

	/**
	 * Indicates whether the given class can be written by this converter.
	 * @param clazz the class to test for writability
	 * @param mediaType the media type to write (can be {@code null} if not specified);
	 * typically the value of an {@code Accept} header.
	 * @return {@code true} if writable; {@code false} otherwise
	 */
	// 指示此转换器是否可以写入给定的类。
	// @param clazz 需要测试可写入性的类
	// @param mediaType 需要写入的媒体类型（如果未指定，则为 {@code null}）；通常为 {@code Accept} 标头的值。
	// @return 如果可写，则返回 {@code true}；否则返回 {@code false}
	boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType);

	/**
	 * Return the list of media types supported by this converter. The list may
	 * not apply to every possible target element type and calls to this method
	 * should typically be guarded via {@link #canWrite(Class, MediaType)
	 * canWrite(clazz, null}. The list may also exclude MIME types supported
	 * only for a specific class. Alternatively, use
	 * {@link #getSupportedMediaTypes(Class)} for a more precise list.
	 * @return the list of supported media types
	 */
	// 返回此转换器支持的媒体类型列表。
	// 该列表可能并非适用于所有可能的目标元素类型，并且通常应通过 {@link #canWrite(Class, MediaType) canWrite(clazz, null} 来保护对此方法的调用。
	// 该列表还可能排除仅支持特定类的 MIME 类型。或者，使用 {@link #getSupportedMediaTypes(Class)} 获取更精确的列表。
	// @return 支持的媒体类型列表
	List<MediaType> getSupportedMediaTypes();

	/**
	 * Return the list of media types supported by this converter for the given
	 * class. The list may differ from {@link #getSupportedMediaTypes()} if the
	 * converter does not support the given Class or if it supports it only for
	 * a subset of media types.
	 * @param clazz the type of class to check
	 * @return the list of media types supported for the given class
	 * @since 5.3.4
	 */
	// 返回此转换器针对给定类支持的媒体类型列表。
	// 如果转换器不支持给定类，或者仅支持部分媒体类型，则该列表可能与 {@link #getSupportedMediaTypes()} 不同。
	// @param clazz 要检查的类的类型
	// @return 给定类支持的媒体类型列表
	default List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
		return (canRead(clazz, null) || canWrite(clazz, null) ?
				getSupportedMediaTypes() : Collections.emptyList());
	}

	/**
	 * Read an object of the given type from the given input message, and returns it.
	 * @param clazz the type of object to return. This type must have previously been passed to the
	 * {@link #canRead canRead} method of this interface, which must have returned {@code true}.
	 * @param inputMessage the HTTP input message to read from
	 * @return the converted object
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotReadableException in case of conversion errors
	 */
	// 从给定的输入消息中读取指定类型的对象并返回。
	// @param clazz 指定要返回的对象类型。此类型必须先前已传递给此接口的 {@link #canRead canRead} 方法，并且该方法必须返回 {@code true}。
	// @param inputMessage 指定要读取的 HTTP 输入消息
	// @return 转换后的对象
	// @throws IOException（如果发生 I/O 错误）
	// @throws HttpMessageNotReadableException（如果发生转换错误）
	T read(Class<? extends T> clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;

	/**
	 * Write a given object to the given output message.
	 * @param t the object to write to the output message. The type of this object must have previously been
	 * passed to the {@link #canWrite canWrite} method of this interface, which must have returned {@code true}.
	 * @param contentType the content type to use when writing. May be {@code null} to indicate that the
	 * default content type of the converter must be used. If not {@code null}, this media type must have
	 * previously been passed to the {@link #canWrite canWrite} method of this interface, which must have
	 * returned {@code true}.
	 * @param outputMessage the message to write to
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotWritableException in case of conversion errors
	 */
	// 将给定对象写入给定的输出消息。
	// @param t 要写入输出消息的对象。此对象的类型必须先前已传递给此接口的 {@link #canWrite canWrite} 方法，该方法必须返回 {@code true}。
	// @param contentType 写入时使用的内容类型。可以为 {@code null}，表示必须使用转换器的默认内容类型。
	// 如果不是 {@code null}，则此媒体类型必须先前已传递给此接口的 {@link #canWrite canWrite} 方法，该方法必须返回 {@code true}。
	// @param outputMessage 要写入的消息
	// @throws IOException（如果发生 I/O 错误）
	// @throws HttpMessageNotWritableException（如果发生转换错误）
	void write(T t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException;

}
