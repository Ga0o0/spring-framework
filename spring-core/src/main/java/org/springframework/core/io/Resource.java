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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;

/**
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see FileUrlResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
// 资源描述符的接口，该接口抽象自底层资源的实际类型，例如文件或类路径资源。
//
// <p>如果资源以物理形式存在，则可以为每个资源打开一个输入流 (InputStream)，
// 但对于某些资源，只能返回 URL 或文件句柄。实际行为取决于具体实现。
public interface Resource extends InputStreamSource {

	/**
	 * Determine whether this resource actually exists in physical form.
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a {@code Resource} handle only guarantees a valid
	 * descriptor handle.
	 */
	// 确定此资源是否以物理形式实际存在。
	// <p>此方法执行确定性的存在性检查，而 {@code Resource} 句柄的存在仅保证描述符句柄有效。
	boolean exists();

	/**
	 * Indicate whether non-empty contents of this resource can be read via
	 * {@link #getInputStream()}.
	 * <p>Will be {@code true} for typical resource descriptors that exist
	 * since it strictly implies {@link #exists()} semantics as of 5.1.
	 * Note that actual content reading may still fail when attempted.
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be read.
	 * @see #getInputStream()
	 * @see #exists()
	 */
	// 指示此资源的非空内容是否可以通过 {@link #getInputStream()} 读取。
	// <p>对于典型的存在资源描述符，将为 {@code true}，因为它严格隐含了 5.1 版的 {@link #exists()} 语义。
	// 请注意，实际内容读取仍可能失败。但是，{@code false} 值明确表明无法读取资源内容。
	default boolean isReadable() {
		return exists();
	}

	/**
	 * Indicate whether this resource represents a handle with an open stream.
	 * If {@code true}, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be {@code false} for typical resource descriptors.
	 */
	// 指示此资源是否表示具有打开流的句柄。
	// 如果为 {@code true}，则输入流无法多次读取，必须读取并关闭以避免资源泄漏。
	// <p>对于典型的资源描述符，该值为 {@code false}。
	default boolean isOpen() {
		return false;
	}

	/**
	 * Determine whether this resource represents a file in a file system.
	 * <p>A value of {@code true} strongly suggests (but does not guarantee)
	 * that a {@link #getFile()} call will succeed.
	 * <p>This is conservatively {@code false} by default.
	 * @since 5.0
	 * @see #getFile()
	 */
	// 判断此资源是否代表文件系统中的文件。
	// <p>如果值为 {@code true}，则强烈建议（但不保证）{@link #getFile()} 调用成功。
	// <p>默认情况下，此值保守地为 {@code false}。
	default boolean isFile() {
		return false;
	}

	/**
	 * Return a URL handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as a descriptor
	 */
	// 返回此资源的 URL 句柄。
	// @throws IOException 如果资源无法解析为 URL，即如果资源无法作为描述符使用，则抛出 IOException
	URL getURL() throws IOException;

	/**
	 * Return a URI handle for this resource.
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as a descriptor
	 * @since 2.5
	 */
	// 返回此资源的 URI 句柄。
	// @throws IOException 如果资源无法解析为 URI，即资源无法作为描述符使用，则抛出 IOException。
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource.
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @see #getInputStream()
	 */
	// 返回此资源的文件句柄。
	// @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，即如果资源在文件系统中不可用，则抛出 java.io.FileNotFoundException
	// @throws IOException 如果出现常规解析/读取失败，则抛出 IOException
	File getFile() throws IOException;

	/**
	 * Return a {@link ReadableByteChannel}.
	 * <p>It is expected that each call creates a <i>fresh</i> channel.
	 * <p>The default implementation returns {@link Channels#newChannel(InputStream)}
	 * with the result of {@link #getInputStream()}.
	 * @return the byte channel for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't exist
	 * @throws IOException if the content channel could not be opened
	 * @since 5.0
	 * @see #getInputStream()
	 */
	// 返回一个 {@link ReadableByteChannel}。
	// <p>预计每次调用都会创建一个<i>新的</i>通道。
	// <p>默认实现返回 {@link Channels#newChannel(InputStream)} 及其 {@link #getInputStream()} 的结果。
	// @return 底层资源的字节通道（不能为 null）
	// @throws java.io.FileNotFoundException 如果底层资源不存在
	// @throws IOException 如果无法打开内容通道
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	/**
	 * Return the contents of this resource as a byte array.
	 * @return the contents of this resource as byte array
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @since 6.0.5
	 */
	// 以字节数组形式返回此资源的内容。
	// @return 将此资源的内容作为字节数组返回
	// @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，即如果资源在文件系统中不可用
	// @throws IOException 如果出现常规解析/读取失败
	default byte[] getContentAsByteArray() throws IOException {
		return FileCopyUtils.copyToByteArray(getInputStream());
	}

	/**
	 * Return the contents of this resource as a string, using the specified charset.
	 * @param charset the charset to use for decoding
	 * @return the contents of this resource as a {@code String}
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @since 6.0.5
	 */
	// 使用指定的字符集，以字符串形式返回此资源的内容。
	// @param charset 用于解码的字符集
	// @return 将此资源的内容作为 {@code String} 返回
	// @throws java.io.FileNotFoundException 如果资源无法解析为绝对文件路径，即如果资源在文件系统中不可用
	// @throws IOException 如果出现常规解析/读取失败，则抛出 IOException
	default String getContentAsString(Charset charset) throws IOException {
		return FileCopyUtils.copyToString(new InputStreamReader(getInputStream(), charset));
	}

	/**
	 * Determine the content length for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	// 确定此资源的内容长度。
	// @throws IOException 如果资源无法解析（在文件系统中或其他已知的物理资源类型中），则抛出 IOException
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	// 确定此资源的最后修改时间戳。
	// @throws IOException 如果资源无法解析（在文件系统中或其他已知的物理资源类型中），则抛出 IOException
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource.
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 */
	// 创建相对于此资源的资源。
	// @param relativePath 相对路径（相对于此资源）
	// @return 相对资源的资源句柄
	// @throws IOException 如果无法确定相对资源，则抛出 IOException
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * Determine the filename for this resource &mdash; typically the last
	 * part of the path &mdash; for example, {@code "myfile.txt"}.
	 * <p>Returns {@code null} if this type of resource does not
	 * have a filename.
	 * <p>Implementations are encouraged to return the filename unencoded.
	 */
	// 确定此资源的文件名 &mdash; 通常是路径的最后一部分 &mdash; 例如，{@code "myfile.txt"}。
	// <p>如果此类型的资源没有文件名，则返回 {@code null}。
	// <p>建议实现返回未编码的文件名。
	@Nullable
	String getFilename();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their {@code toString} method.
	 * @see Object#toString()
	 */
	// 返回此资源的描述，用于在处理资源时输出错误。
	// <p>也建议实现从其 {@code toString} 方法返回此值。
	String getDescription();

}
