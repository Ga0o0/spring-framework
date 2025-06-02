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

package org.springframework.web.multipart;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A representation of an uploaded file received in a multipart request.
 *
 * <p>The file contents are either stored in memory or temporarily on disk.
 * In either case, the user is responsible for copying file contents to a
 * session-level or persistent store as and if desired. The temporary storage
 * will be cleared at the end of request processing.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see org.springframework.web.multipart.MultipartHttpServletRequest
 * @see org.springframework.web.multipart.MultipartResolver
 */
// 在多部分请求中收到的已上传文件的表示。
//
// <p>文件内容要么存储在内存中，要么临时存储在磁盘上。无论哪种情况，
// 用户都需要根据需要将文件内容复制到会话级存储或持久性存储中。临时存储将在请求处理结束时被清除。
public interface MultipartFile extends InputStreamSource {

	/**
	 * Return the name of the parameter in the multipart form.
	 * @return the name of the parameter (never {@code null} or empty)
	 */
	// 返回多部分表单中的参数名称。
	// @return 参数名称（绝不会为 {@code null} 或空）
	String getName();

	/**
	 * Return the original filename in the client's filesystem.
	 * <p>This may contain path information depending on the browser used,
	 * but it typically will not with any other than Opera.
	 * <p><strong>Note:</strong> Please keep in mind this filename is supplied
	 * by the client and should not be used blindly. In addition to not using
	 * the directory portion, the file name could also contain characters such
	 * as ".." and others that can be used maliciously. It is recommended to not
	 * use this filename directly. Preferably generate a unique one and save
	 * this one somewhere for reference, if necessary.
	 * @return the original filename, or the empty String if no file has been chosen
	 * in the multipart form, or {@code null} if not defined or not available
	 * @see <a href="https://tools.ietf.org/html/rfc7578#section-4.2">RFC 7578, Section 4.2</a>
	 * @see <a href="https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload">Unrestricted File Upload</a>
	 */
	// 返回客户端文件系统中的原始文件名。
	// <p>这可能包含路径信息，具体取决于所使用的浏览器，但通常除 Opera 浏览器外不会包含路径信息。
	// <p><strong>注意：</strong>请记住，此文件名由客户端提供，不应盲目使用。除了不使用目录部分外，文件名还可能包含 “..” 等可被恶意使用的字符。
	// 建议不要直接使用此文件名。最好生成一个唯一的文件名，并将其保存在某个地方，以便在必要时参考。
	// @return 返回原始文件名；如果在多部分表单中未选择任何文件，则返回空字符串；如果未定义或不可用，则返回 {@code null}
	// @see <a href="https://tools.ietf.org/html/rfc7578#section-4.2">RFC 7578, Section 4.2</a>
	// @see <a href="https://owasp.org/www-community/vulnerabilities/Unrestricted_File_Upload">不受限制的文件上传</a>
	@Nullable
	String getOriginalFilename();

	/**
	 * Return the content type of the file.
	 * @return the content type, or {@code null} if not defined
	 * (or no file has been chosen in the multipart form)
	 */
	// 返回文件的内容类型。
	// @return 内容类型，如果未定义（或在多部分表单中未选择任何文件），则返回 {@code null}。
	@Nullable
	String getContentType();

	/**
	 * Return whether the uploaded file is empty, that is, either no file has
	 * been chosen in the multipart form or the chosen file has no content.
	 */
	// 返回上传文件是否为空，即在多部分表单中未选择任何文件，或者所选文件没有内容。
	boolean isEmpty();

	/**
	 * Return the size of the file in bytes.
	 * @return the size of the file, or 0 if empty
	 */
	// 返回文件大小（以字节为单位）。
	// @return 文件大小，如果为空，则返回 0。
	long getSize();

	/**
	 * Return the contents of the file as an array of bytes.
	 * @return the contents of the file as bytes, or an empty byte array if empty
	 * @throws IOException in case of access errors (if the temporary store fails)
	 */
	// 以字节数组形式返回文件内容。
	// @return 文件内容为字节，如果为空，则返回一个空的字节数组。
	// @throws 如果访问错误（例如临时存储失败），则抛出 IOException。
	byte[] getBytes() throws IOException;

	/**
	 * Return an InputStream to read the contents of the file from.
	 * <p>The user is responsible for closing the returned stream.
	 * @return the contents of the file as stream, or an empty stream if empty
	 * @throws IOException in case of access errors (if the temporary store fails)
	 */
	// 返回一个用于读取文件内容的输入流。
	// <p>用户负责关闭返回的流。
	// @return 将文件内容作为流返回，如果为空，则返回一个空流。
	// @throws 如果访问错误（例如临时存储失败），则抛出 IOException。
	@Override
	InputStream getInputStream() throws IOException;

	/**
	 * Return a Resource representation of this MultipartFile. This can be used
	 * as input to the {@code RestTemplate} or the {@code WebClient} to expose
	 * content length and the filename along with the InputStream.
	 * @return this MultipartFile adapted to the Resource contract
	 * @since 5.1
	 */
	// 返回此 MultipartFile 的资源表示。这可以用作 {@code RestTemplate} 或 {@code WebClient} 的输入，以便与 InputStream 一起公开内容长度和文件名。
	// @return 此 MultipartFile 已根据资源契约进行适配
	default Resource getResource() {
		return new MultipartFileResource(this);
	}

	/**
	 * Transfer the received file to the given destination file.
	 * <p>This may either move the file in the filesystem, copy the file in the
	 * filesystem, or save memory-held contents to the destination file. If the
	 * destination file already exists, it will be deleted first.
	 * <p>If the target file has been moved in the filesystem, this operation
	 * cannot be invoked again afterwards. Therefore, call this method just once
	 * in order to work with any storage mechanism.
	 * <p><b>NOTE:</b> Depending on the underlying provider, temporary storage
	 * may be container-dependent, including the base directory for relative
	 * destinations specified here (e.g. with Servlet multipart handling).
	 * For absolute destinations, the target file may get renamed/moved from its
	 * temporary location or newly copied, even if a temporary copy already exists.
	 * @param dest the destination file (typically absolute)
	 * @throws IOException in case of reading or writing errors
	 * @throws IllegalStateException if the file has already been moved
	 * in the filesystem and is not available anymore for another transfer
	 * @see jakarta.servlet.http.Part#write(String)
	 */
	// 将接收到的文件传输到指定的目标文件。
	// <p>这可以是在文件系统中移动文件、在文件系统中复制文件，或者将内存中的内容保存到目标文件。如果目标文件已存在，则会先删除该文件。
	// <p>如果目标文件已在文件系统中移动，则此操作之后无法再次调用。因此，只需调用一次此方法即可使用任何存储机制。
	// <p><b>注意：</b>根据底层提供程序的不同，临时存储可能依赖于容器，包括此处指定的相对目标的基目录（例如，使用 Servlet 多部分处理）。
	// 对于绝对目标，即使临时副本已存在，目标文件也可能会从其临时位置重命名/移动或重新复制。
	// @param dest 目标文件（通常为绝对文件）
	// @throws IOException（如果发生读取或写入错误）
	// @throws IllegalStateException（如果文件已在文件系统中移动且无法再用于其他传输）
	void transferTo(File dest) throws IOException, IllegalStateException;

	/**
	 * Transfer the received file to the given destination file.
	 * <p>The default implementation simply copies the file input stream.
	 * @since 5.1
	 * @see #getInputStream()
	 * @see #transferTo(File)
 	 */
	// 将接收到的文件传输到给定的目标文件。
	// <p>默认实现只是复制文件输入流。
	default void transferTo(Path dest) throws IOException, IllegalStateException {
		FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest));
	}

}
