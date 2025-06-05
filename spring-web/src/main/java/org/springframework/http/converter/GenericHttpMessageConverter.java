/*
 * Copyright 2002-2018 the original author or authors.
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
import java.lang.reflect.Type;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * A specialization of {@link HttpMessageConverter} that can convert an HTTP request
 * into a target object of a specified generic type and a source object of a specified
 * generic type into an HTTP response.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Sebastien Deleuze
 * @since 3.2
 * @param <T> the converted object type
 * @see org.springframework.core.ParameterizedTypeReference
 */
// {@link HttpMessageConverter} 的专门化，可以将 HTTP 请求转换为指定泛型类型的目标对象，并将指定泛型类型的源对象转换为 HTTP 响应。
public interface GenericHttpMessageConverter<T> extends HttpMessageConverter<T> {

	/**
	 * Indicates whether the given type can be read by this converter.
	 * This method should perform the same checks as
	 * {@link HttpMessageConverter#canRead(Class, MediaType)} with additional ones
	 * related to the generic type.
	 * @param type the (potentially generic) type to test for readability
	 * @param contextClass a context class for the target type, for example a class
	 * in which the target type appears in a method signature (can be {@code null})
	 * @param mediaType the media type to read, can be {@code null} if not specified.
	 * Typically, the value of a {@code Content-Type} header.
	 * @return {@code true} if readable; {@code false} otherwise
	 */
	// 指示此转换器是否可以读取给定类型。
	// 此方法应执行与 {@link HttpMessageConverter#canRead(Class, MediaType)} 相同的检查，以及与通用类型相关的其他检查。
	// @param type 要测试可读性的（可能是通用的）类型
	// @param contextClass 目标类型的上下文类，例如，目标类型出现在方法签名中的类（可以为 {@code null}）
	// @param mediaType 要读取的媒体类型，如果未指定，可以为 {@code null}。通常为 {@code Content-Type} 标头的值。
	// @return 如果可读，则返回 {@code true}；否则返回 {@code false}
	boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType);

	/**
	 * Read an object of the given type form the given input message, and returns it.
	 * @param type the (potentially generic) type of object to return. This type must have
	 * previously been passed to the {@link #canRead canRead} method of this interface,
	 * which must have returned {@code true}.
	 * @param contextClass a context class for the target type, for example a class
	 * in which the target type appears in a method signature (can be {@code null})
	 * @param inputMessage the HTTP input message to read from
	 * @return the converted object
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotReadableException in case of conversion errors
	 */
	// 从给定的输入消息中读取指定类型的对象并返回。
	// @param type 要返回的对象类型（可能是泛型）。此类型必须先前已传递给此接口的 {@link #canRead canRead} 方法，并且该方法必须返回 {@code true}。
	// @param contextClass 目标类型的上下文类，例如，目标类型出现在方法签名中的类（可以为 {@code null}）
	// @param inputMessage 要读取的 HTTP 输入消息
	// @return 转换后的对象
	// @throws IOException（如果发生 I/O 错误）
	// @throws HttpMessageNotReadableException（如果发生转换错误）
	T read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException;

	/**
	 * Indicates whether the given class can be written by this converter.
	 * <p>This method should perform the same checks as
	 * {@link HttpMessageConverter#canWrite(Class, MediaType)} with additional ones
	 * related to the generic type.
	 * @param type the (potentially generic) type to test for writability
	 * (can be {@code null} if not specified)
	 * @param clazz the source object class to test for writability
	 * @param mediaType the media type to write (can be {@code null} if not specified);
	 * typically the value of an {@code Accept} header.
	 * @return {@code true} if writable; {@code false} otherwise
	 * @since 4.2
	 */
	// 指示此转换器是否可以写入给定的类。
	// <p>此方法应执行与 {@link HttpMessageConverter#canWrite(Class, MediaType)} 相同的检查，并附加与泛型类型相关的检查。
	// @param type 待测试可写性的（可能是泛型）类型（如果未指定，可以为 {@code null}）
	// @param clazz 待测试可写的源对象类
	// @param mediaType 待写入的媒体类型（如果未指定，可以为 {@code null}）；通常为 {@code Accept} 标头的值。
	// @return 如果可写，则返回 {@code true}；否则返回 {@code false}
	boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType);

	/**
	 * Write a given object to the given output message.
	 * @param t the object to write to the output message. The type of this object must
	 * have previously been passed to the {@link #canWrite canWrite} method of this
	 * interface, which must have returned {@code true}.
	 * @param type the (potentially generic) type of object to write. This type must have
	 * previously been passed to the {@link #canWrite canWrite} method of this interface,
	 * which must have returned {@code true}. Can be {@code null} if not specified.
	 * @param contentType the content type to use when writing. May be {@code null} to
	 * indicate that the default content type of the converter must be used. If not
	 * {@code null}, this media type must have previously been passed to the
	 * {@link #canWrite canWrite} method of this interface, which must have returned
	 * {@code true}.
	 * @param outputMessage the message to write to
	 * @throws IOException in case of I/O errors
	 * @throws HttpMessageNotWritableException in case of conversion errors
	 * @since 4.2
	 */
	// 将给定对象写入给定的输出消息。
	// @param t 要写入输出消息的对象。此对象的类型必须先前已传递给此接口的 {@link #canWrite canWrite} 方法，该方法必须返回 {@code true}。
	// @param type 要写入的对象类型（可能是通用的）。
	// 此类型必须先前已传递给此接口的 {@link #canWrite canWrite} 方法，该方法必须返回 {@code true}。如果未指定，则可以为 {@code null}。
	// @param contentType 写入时使用的内容类型。可以为 {@code null}，以指示必须使用转换器的默认内容类型。
	// 如果不是 {@code null}，则此媒体类型必须先前已传递给此接口的 {@link #canWrite canWrite} 方法，该方法必须返回 {@code true}。
	// @param outputMessage 要写入的消息
	// @throws IOException（如果发生 I/O 错误）
	// @throws HttpMessageNotWritableException（如果发生转换错误）
	void write(T t, @Nullable Type type, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException;

}
