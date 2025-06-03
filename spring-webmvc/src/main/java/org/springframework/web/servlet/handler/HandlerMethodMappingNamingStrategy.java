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

package org.springframework.web.servlet.handler;

import org.springframework.web.method.HandlerMethod;

/**
 * A strategy for assigning a name to a handler method's mapping.
 *
 * <p>The strategy can be configured on
 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping
 * AbstractHandlerMethodMapping}. It is used to assign a name to the mapping of
 * every registered handler method. The names can then be queried via
 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerMethodsForMappingName(String)
 * AbstractHandlerMethodMapping#getHandlerMethodsForMappingName}.
 *
 * <p>Applications can build a URL to a controller method by name with the help
 * of the static method
 * {@link org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder#fromMappingName(String)
 * MvcUriComponentsBuilder#fromMappingName} or in JSPs through the "mvcUrl"
 * function registered by the Spring tag library.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 * @param <T> the mapping type
 */
// 为处理程序方法的映射分配名称的策略。
//
// <p>该策略可在 {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping AbstractHandlerMethodMapping} 上配置。
// 它用于为每个已注册的处理程序方法的映射分配名称。之后，可以通过
// {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerMethodsForMappingName(String)
// AbstractHandlerMethodMapping#getHandlerMethodsForMappingName} 查询这些名称。
//
// <p>应用程序可以借助静态方法 {@link org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder#fromMappingName(String)
// MvcUriComponentsBuilder#fromMappingName} 或在 JSP 中使用 Spring 标签库注册的 “mvcUrl” 函数，根据名称构建指向控制器方法的 URL。
@FunctionalInterface
public interface HandlerMethodMappingNamingStrategy<T> {

	/**
	 * Determine the name for the given HandlerMethod and mapping.
	 * @param handlerMethod the handler method
	 * @param mapping the mapping
	 * @return the name
	 */
	// 确定给定 HandlerMethod 和映射的名称。
	// @param handlerMethod 处理程序方法
	// @param mapping 映射
	// @return 名称
	String getName(HandlerMethod handlerMethod, T mapping);

}
