/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.bind.support;

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.WebRequest;

/**
 * Strategy interface for storing model attributes in a backend session.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see org.springframework.web.bind.annotation.SessionAttributes
 */
// 用于在后端会话中存储模型属性的策略接口。
public interface SessionAttributeStore {

	/**
	 * Store the supplied attribute in the backend session.
	 * <p>Can be called for new attributes as well as for existing attributes.
	 * In the latter case, this signals that the attribute value may have been modified.
	 * @param request the current request
	 * @param attributeName the name of the attribute
	 * @param attributeValue the attribute value to store
	 */
	// 将提供的属性存储在后端会话中。
	// <p>可以针对新属性或现有属性调用。在现有属性的情况下，这表示属性值可能已被修改。
	// @param request 当前请求
	// @param attributeName 属性名称
	// @param attributeValue 要存储的属性值
	void storeAttribute(WebRequest request, String attributeName, Object attributeValue);

	/**
	 * Retrieve the specified attribute from the backend session.
	 * <p>This will typically be called with the expectation that the
	 * attribute is already present, with an exception to be thrown
	 * if this method returns {@code null}.
	 * @param request the current request
	 * @param attributeName the name of the attribute
	 * @return the current attribute value, or {@code null} if none
	 */
	// 从后端会话中检索指定的属性。
	// <p>此方法通常会在预期属性已存在的情况下调用，如果此方法返回 {@code null}，则会抛出异常。
	// @param request 当前请求
	// @param attributeName 属性名称
	// @return 当前属性值，如果没有则返回 {@code null}。
	@Nullable
	Object retrieveAttribute(WebRequest request, String attributeName);

	/**
	 * Clean up the specified attribute in the backend session.
	 * <p>Indicates that the attribute name will not be used anymore.
	 * @param request the current request
	 * @param attributeName the name of the attribute
	 */
	// 清理后端会话中的指定属性。
	// <p>表示该属性名称将不再使用。
	// @param request 当前请求
	// @param attributeName 属性名称
	void cleanupAttribute(WebRequest request, String attributeName);

}
