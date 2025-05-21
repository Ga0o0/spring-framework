/*
 * Copyright 2002-2007 the original author or authors.
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

/**
 * Extended interface for a resource that is loaded from an enclosing
 * 'context', e.g. from a {@link jakarta.servlet.ServletContext} but also
 * from plain classpath paths or relative file system paths (specified
 * without an explicit prefix, hence applying relative to the local
 * {@link ResourceLoader}'s context).
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.context.support.ServletContextResource
 */
// 扩展接口，用于从封闭的 “上下文” 加载资源，例如从 {@link jakarta.servlet.ServletContext} 加载，
// 也可以从普通的 Classpath 路径或相对文件系统路径（未指定显式前缀，因此相对于本地 {@link ResourceLoader} 上下文）加载。
public interface ContextResource extends Resource {

	/**
	 * Return the path within the enclosing 'context'.
	 * <p>This is typically path relative to a context-specific root directory,
	 * e.g. a ServletContext root or a PortletContext root.
	 */
	// 返回封闭 “上下文” 内的路径。
	// <p>这通常是相对于上下文特定根目录的路径，例如 ServletContext 根目录或 PortletContext 根目录。
	String getPathWithinContext();

}
