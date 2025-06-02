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

package org.springframework.web.servlet.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base Controller interface, representing a component that receives
 * {@code HttpServletRequest} and {@code HttpServletResponse}
 * instances just like a {@code HttpServlet} but is able to
 * participate in an MVC workflow. Controllers are comparable to the
 * notion of a Struts {@code Action}.
 *
 * <p>Any implementation of the Controller interface should be a
 * <i>reusable, thread-safe</i> class, capable of handling multiple
 * HTTP requests throughout the lifecycle of an application. To be able to
 * configure a Controller easily, Controller implementations are encouraged
 * to be (and usually are) JavaBeans.
 *
 * <h3><a name="workflow">Workflow</a></h3>
 *
 * <p>After a {@code DispatcherServlet} has received a request and has
 * done its work to resolve locales, themes, and suchlike, it then tries
 * to resolve a Controller, using a
 * {@link org.springframework.web.servlet.HandlerMapping HandlerMapping}.
 * When a Controller has been found to handle the request, the
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest}
 * method of the located Controller will be invoked; the located Controller
 * is then responsible for handling the actual request and &mdash; if applicable
 * &mdash; returning an appropriate
 * {@link org.springframework.web.servlet.ModelAndView ModelAndView}.
 * So actually, this method is the main entry point for the
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet}
 * which delegates requests to controllers.
 *
 * <p>So basically any <i>direct</i> implementation of the {@code Controller} interface
 * just handles HttpServletRequests and should return a ModelAndView, to be further
 * interpreted by the DispatcherServlet. Any additional functionality such as
 * optional validation, form handling, etc. should be obtained through extending
 * {@link org.springframework.web.servlet.mvc.AbstractController AbstractController}
 * or one of its subclasses.
 *
 * <h3>Notes on design and testing</h3>
 *
 * <p>The Controller interface is explicitly designed to operate on HttpServletRequest
 * and HttpServletResponse objects, just like an HttpServlet. It does not aim to
 * decouple itself from the Servlet API, in contrast to, for example, WebWork, JSF or Tapestry.
 * Instead, the full power of the Servlet API is available, allowing Controllers to be
 * general-purpose: a Controller is able to not only handle web user interface
 * requests but also to process remoting protocols or to generate reports on demand.
 *
 * <p>Controllers can easily be tested by passing in mock objects for the
 * HttpServletRequest and HttpServletResponse objects as parameters to the
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest}
 * method. As a convenience, Spring ships with a set of Servlet API mocks
 * that are suitable for testing any kind of web components, but are particularly
 * suitable for testing Spring web controllers. In contrast to a Struts Action,
 * there is no need to mock the ActionServlet or any other infrastructure;
 * mocking HttpServletRequest and HttpServletResponse is sufficient.
 *
 * <p>If Controllers need to be aware of specific environment references, they can
 * choose to implement specific awareness interfaces, just like any other bean in a
 * Spring (web) application context can do, for example:
 * <ul>
 * <li>{@code org.springframework.context.ApplicationContextAware}</li>
 * <li>{@code org.springframework.context.ResourceLoaderAware}</li>
 * <li>{@code org.springframework.web.context.ServletContextAware}</li>
 * </ul>
 *
 * <p>Such environment references can easily be passed in testing environments,
 * through the corresponding setters defined in the respective awareness interfaces.
 * In general, it is recommended to keep the dependencies as minimal as possible:
 * for example, if all you need is resource loading, implement ResourceLoaderAware only.
 * Alternatively, derive from the WebApplicationObjectSupport base class, which gives
 * you all those references through convenient accessors but requires an
 * ApplicationContext reference on initialization.
 *
 * <p>Controllers can use the {@code checkNotModified} methods on
 * {@link org.springframework.web.context.request.WebRequest} for HTTP caching
 * support.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see SimpleControllerHandlerAdapter
 * @see AbstractController
 * @see org.springframework.mock.web.MockHttpServletRequest
 * @see org.springframework.mock.web.MockHttpServletResponse
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ResourceLoaderAware
 * @see org.springframework.web.context.ServletContextAware
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
// 基本 Controller 接口，表示一个接收 {@code HttpServletRequest} 和 {@code HttpServletResponse} 实例的组件，
// 就像 {@code HttpServlet} 一样，但能够参与 MVC 工作流。控制器与 Struts {@code Action} 的概念类似。
//
// <p>任何 Controller 接口的实现都应该是<i>可重用、线程安全的</i>类，能够在应用程序的整个生命周期内处理多个 HTTP 请求。
// 为了能够轻松配置 Controller，建议（通常也是）将 Controller 实现为 JavaBean。
//
// <h3><a name="workflow">Workflow</a></h3>
//
// <p>在 {@code DispatcherServlet} 收到请求并完成解析语言环境、主题等工作后，它会尝试使用
// {@link org.springframework.web.servlet.HandlerMapping HandlerMapping} 解析 Controller。
// 当找到一个控制器来处理请求时，将调用该控制器的 {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest} 方法；
// 该控制器负责处理实际请求，并且如果适用，返回相应的 {@link org.springframework.web.servlet.ModelAndView ModelAndView}。
// 因此，实际上，此方法是 {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet} 的主要入口点，它将请求委托给控制器。
//
// <p>因此，基本上任何 <i>直接</i> 实现 {@code Controller} 接口的类都只处理 HttpServletRequests，并应返回一个 ModelAndView，以便由 DispatcherServlet 进一步解释。
// 任何其他功能（例如可选验证、表单处理等）都应通过扩展 {@link org.springframework.web.servlet.mvc.AbstractController AbstractController} 或其子类之一来获取。
//
// <h3>设计和测试注意事项</h3>
//
// <p>Controller 接口明确设计为操作 HttpServletRequest 和 HttpServletResponse 对象，就像 HttpServlet 一样。
// 它并非旨在与 Servlet API 解耦，这与 WebWork、JSF 或 Tapestry 等不同。相反，它充分利用了 Servlet API 的全部功能，
// 使 Controller 具有通用性：Controller 不仅能够处理 Web 用户界面请求，还能处理远程协议或按需生成报告。
//
// <p>通过将 HttpServletRequest 和 HttpServletResponse 对象的模拟对象作为参数传递给
// {@link #handleRequest(HttpServletRequest, HttpServletResponse) handleRequest} 方法，可以轻松测试 Controller。
// 为方便起见，Spring 附带了一组 Servlet API 模拟对象，适用于测试任何类型的 Web 组件，但尤其适合测试 Spring Web 控制器。
// 与 Struts Action 不同，无需模拟 ActionServlet 或任何其他基础结构；模拟 HttpServletRequest 和 HttpServletResponse 就足够了。
//
// <p>如果控制器需要了解特定的环境引用，它们可以选择实现特定的感知接口，就像 Spring（web）应用程序上下文中的任何其他 Bean 一样，例如：
// <ul>
// <li>{@code org.springframework.context.ApplicationContextAware}
// <li>{@code org.springframework.context.ResourceLoaderAware}
// <li>{@code org.springframework.web.context.ServletContextAware} </li>
// </ul>
//
// <p>此类环境引用可以通过相应感知接口中定义的相应设置器轻松地在测试环境中传递。
// 通常，建议尽可能减少依赖关系：例如，如果您只需要资源加载，则仅实现 ResourceLoaderAware。
// 或者，从 WebApplicationObjectSupport 基类派生，该基类通过便捷的访问器为您提供所有这些引用，但在初始化时需要 ApplicationContext 引用。
//
// <p>控制器可以使用 {@link org.springframework.web.context.request.WebRequest} 上的 {@code checkNotModified} 方法来获得 HTTP 缓存支持。
@FunctionalInterface
public interface Controller {

	/**
	 * Process the request and return a ModelAndView object which the DispatcherServlet
	 * will render. A {@code null} return value is not an error: it indicates that
	 * this object completed request processing itself and that there is therefore no
	 * ModelAndView to render.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render, or {@code null} if handled directly
	 * @throws Exception in case of errors
	 */
	// 处理请求并返回一个 ModelAndView 对象，DispatcherServlet 将渲染该对象。
	// 返回值为 {@code null} 并非错误：它表示此对象已自行完成请求处理，因此没有 ModelAndView 可供渲染。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @return 一个 ModelAndView 对象，如果直接处理则返回 {@code null}
	// @throws Exception（如果发生错误）
	@Nullable
	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
