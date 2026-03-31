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

package org.springframework.web.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * MVC framework SPI, allowing parameterization of the core MVC workflow.
 *
 * <p>Interface that must be implemented for each handler type to handle a request.
 * This interface is used to allow the {@link DispatcherServlet} to be indefinitely
 * extensible. The {@code DispatcherServlet} accesses all installed handlers through
 * this interface, meaning that it does not contain code specific to any handler type.
 *
 * <p>Note that a handler can be of type {@code Object}. This is to enable
 * handlers from other frameworks to be integrated with this framework without
 * custom coding, as well as to allow for annotation-driven handler objects that
 * do not obey any specific Java interface.
 *
 * <p>This interface is not intended for application developers. It is available
 * to handlers who want to develop their own web workflow.
 *
 * <p>Note: {@code HandlerAdapter} implementors may implement the {@link
 * org.springframework.core.Ordered} interface to be able to specify a sorting
 * order (and thus a priority) for getting applied by the {@code DispatcherServlet}.
 * Non-Ordered instances get treated as the lowest priority.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
// MVC 框架 SPI，允许对核心 MVC 工作流进行参数化。
//
// <p>每个处理程序类型都必须实现此接口才能处理请求。此接口用于允许 {@link DispatcherServlet} 无限扩展。
// {@code DispatcherServlet} 通过此接口访问所有已安装的处理程序，这意味着它不包含任何特定于处理程序类型的代码。
//
// <p>请注意，处理程序可以是 {@code Object} 类型。这是为了让其他框架的处理程序无需自定义代码即可与此框架集成，
// 并允许使用不遵循任何特定 Java 接口的注解驱动的处理程序对象。
//
// <p>此接口不适用于应用程序开发人员。它适用于想要开发自己的 Web 工作流的处理程序。
//
// <p>注意：{@code HandlerAdapter} 实现者可以实现 {@link org.springframework.core.Ordered} 接口，
// 以便能够指定由 {@code DispatcherServlet} 应用的排序顺序（以及优先级）。非有序实例被视为最低优先级。
public interface HandlerAdapter {

	/**
	 * Given a handler instance, return whether this {@code HandlerAdapter}
	 * can support it. Typical HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will usually only support one handler type each.
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler the handler object to check
	 * @return whether this object can use the given handler
	 */
	// 给定一个处理程序实例，返回此 {@code HandlerAdapter} 是否支持该处理程序。
	// 典型的 HandlerAdapter 会根据处理程序类型进行判断。每个 HandlerAdapter 通常只支持一种处理程序类型。
	// <p>典型实现：
	// <p>{@code
	// return (handler instanceof MyHandler);
	// }
	// @param handler 要检查的处理程序对象
	// @return 此对象是否可以使用给定的处理程序
	boolean supports(Object handler);

	/**
	 * Use the given handler to handle this request.
	 * The workflow that is required may vary widely.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler the handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @return a ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 * @throws Exception in case of errors
	 */
	// 使用给定的 handler 来处理当前请求。所需的工作流程可能差异很大。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @param handler 要使用的处理程序。此对象必须先前已传递给此接口的 {@code support} 方法，并且该方法必须返回 {@code true}。
	// @return 一个包含视图名称和所需模型数据的 ModelAndView 对象，如果请求已直接处理，则返回 {@code null}。
	// @throws 错误时抛出异常
	@Nullable
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 * @param request current HTTP request
	 * @param handler the handler to use
	 * @return the lastModified value for the given handler
	 * @deprecated as of 5.3.9 along with
	 * {@link org.springframework.web.servlet.mvc.LastModified}.
	 */
	// 与 HttpServlet 的 {@code getLastModified} 方法的约定相同。如果处理程序类不支持，则只需返回 -1。
	// @param request 当前 HTTP 请求
	// @param handler 要使用的处理程序
	// @return 给定处理程序的 lastModified 值
	// @deprecated from 5.3.9 as well as {@link org.springframework.web.servlet.mvc.LastModified}.
	@Deprecated
	long getLastModified(HttpServletRequest request, Object handler);

}
