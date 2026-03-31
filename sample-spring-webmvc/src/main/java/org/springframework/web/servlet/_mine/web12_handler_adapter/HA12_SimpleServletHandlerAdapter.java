package org.springframework.web.servlet._mine.web12_handler_adapter;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleServletHandlerAdapter;

/**
 * SimpleServletHandlerAdapter
 *
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 * @see jakarta.servlet.http.HttpServlet
 * @see jakarta.servlet.GenericServlet
 * @see jakarta.servlet.Servlet
 */
public class HA12_SimpleServletHandlerAdapter {

	/**
	 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter#supports(Object)
	 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter#getLastModified(HttpServletRequest, Object)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// SimpleServletHandlerAdapter
		SimpleServletHandlerAdapter handlerAdapter = new SimpleServletHandlerAdapter();

		// 1. SimpleServletHandlerAdapter#supports(Object)
		boolean supports1 = handlerAdapter.supports(handler);
		boolean supports2 = handlerAdapter.supports(new Object());
		System.out.println("SimpleServletHandlerAdapter supports MyController: " 	+ supports1);
		System.out.println("SimpleServletHandlerAdapter supports Object: " 		+ supports2);

		// 2. SimpleServletHandlerAdapter#handle(...)
		ModelAndView mv = handlerAdapter.handle(request, response, handler);
		System.out.println("SimpleServletHandlerAdapter handle MyHandler: " 		+ mv);

		// 3. SimpleServletHandlerAdapter#getLastModified(HttpServletRequest, Object)
		long lastModified = handlerAdapter.getLastModified(request, handler);
		System.out.println("SimpleServletHandlerAdapter lastModified: " 			+ lastModified);
	}

	/**
	 * MyHandler impl {@link jakarta.servlet.Servlet}
	 *
	 * @see jakarta.servlet.http.HttpServlet
	 * @see jakarta.servlet.GenericServlet
	 * @see jakarta.servlet.Servlet
	 */
	static class MyHandler extends HttpServlet {
		private static final long serialVersionUID = 1L;
	}
}
/*
********************************* Class API Docs *********************************
适配器将 Servlet 接口与通用 DispatcherServlet 结合使用。调用 Servlet 的 service 方法来处理请求。

<p>不明确支持上次修改时间检查：这通常由 Servlet 实现本身处理（通常从 HttpServlet 基类派生）。

<p>此适配器默认不激活；它需要在 DispatcherServlet 上下文中定义为 bean。它将自动应用于实现 Servlet 接口的映射处理程序 bean。

<p>请注意，定义为 bean 的 Servlet 实例将不会接收初始化和销毁回调，除非在 DispatcherServlet 上下文中定义了特殊的后处理器
（例如 SimpleServletPostProcessor）。

<p><b>或者，考虑使用 Spring 的 ServletWrappingController 包装 Servlet。</b>这尤其适用于现有的 Servlet 类，允许指定 Servlet 初始化参数等。

********************************* Class Definition *********************************
public class SimpleServletHandlerAdapter implements HandlerAdapter {
	// ...
}
**/
