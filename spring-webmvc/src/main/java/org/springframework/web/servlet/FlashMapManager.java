/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * A strategy interface for retrieving and saving FlashMap instances.
 * See {@link FlashMap} for a general overview of flash attributes.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 * @see FlashMap
 */
// 用于检索和保存 FlashMap 实例的策略接口。有关 Flash 属性的概述，请参阅 {@link FlashMap}。
public interface FlashMapManager {

	/**
	 * Find a FlashMap saved by a previous request that matches to the current
	 * request, remove it from underlying storage, and also remove other
	 * expired FlashMap instances.
	 * <p>This method is invoked in the beginning of every request in contrast
	 * to {@link #saveOutputFlashMap}, which is invoked only when there are
	 * flash attributes to be saved - i.e. before a redirect.
	 * @param request the current request
	 * @param response the current response
	 * @return a FlashMap matching the current request or {@code null}
	 */
	// 查找与当前请求匹配的先前请求保存的 FlashMap，将其从底层存储中删除，并删除其他过期的 FlashMap 实例。
	// <p>此方法在每个请求开始时调用，与 {@link #saveOutputFlashMap} 相反，后者仅在有要保存的 Flash 属性时调用 - 即在重定向之前。
	// @param request 当前请求
	// @param respond 当前响应
	// @return 与当前请求匹配的 FlashMap 或 {@code null}
	@Nullable
	FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response);

	/**
	 * Save the given FlashMap, in some underlying storage and set the start
	 * of its expiration period.
	 * <p><strong>NOTE:</strong> Invoke this method prior to a redirect in order
	 * to allow saving the FlashMap in the HTTP session or in a response
	 * cookie before the response is committed.
	 * @param flashMap the FlashMap to save
	 * @param request the current request
	 * @param response the current response
	 */
	// 将给定的 FlashMap 保存在某些底层存储中，并设置其有效期的开始时间。
	// <p><strong>注意：</strong>在重定向之前调用此方法，以便在提交响应之前将 FlashMap 保存在 HTTP 会话或响应 cookie 中。
	// @param flashMap 要保存的 FlashMap
	// @param request 当前请求
	// @param respond 当前响应
	void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response);

}
