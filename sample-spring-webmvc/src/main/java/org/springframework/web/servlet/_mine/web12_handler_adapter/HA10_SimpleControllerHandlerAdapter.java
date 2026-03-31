package org.springframework.web.servlet._mine.web12_handler_adapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;

/**
 * SimpleControllerHandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.mvc.Controller
 */
public class HA10_SimpleControllerHandlerAdapter {

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter#supports(Object)
	 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter#getLastModified(HttpServletRequest, Object)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// SimpleControllerHandlerAdapter
		SimpleControllerHandlerAdapter handlerAdapter = new SimpleControllerHandlerAdapter();

		// 1. HandlerAdapter#supports(Object)
		boolean supports1 = handlerAdapter.supports(handler);
		boolean supports2 = handlerAdapter.supports(new Object());
		System.out.println("SimpleControllerHandlerAdapter supports MyController: " 	+ supports1);
		System.out.println("SimpleControllerHandlerAdapter supports Object: " 			+ supports2);

		// 2. HandlerAdapter#handle(...)
		ModelAndView mv = handlerAdapter.handle(request, response, handler);
		System.out.println("SimpleControllerHandlerAdapter handle MyHandler: " 			+ mv);

		// 3. HandlerAdapter#getLastModified(HttpServletRequest, Object)
		long lastModified = handlerAdapter.getLastModified(request, handler);
		System.out.println("SimpleControllerHandlerAdapter lastModified: " 				+ lastModified);
	}

	/**
	 * MyHandler impl {@link org.springframework.web.servlet.mvc.Controller}
	 *
	 * @see org.springframework.web.servlet.mvc.Controller
	 */
	static class MyHandler implements Controller {
		@Override
		public ModelAndView handleRequest(@NonNull HttpServletRequest request,
										  @NonNull HttpServletResponse response) throws Exception {
			return new ModelAndView("index");
		}
	}
}
/*
********************************* Class API Docs *********************************
适配器将普通的 Controller 工作流接口与通用的 org.springframework.web.servlet.DispatcherServlet 接口结合使用。
支持实现 LastModified 接口的处理程序。

<p>这是一个 SPI 类，应用程序代码不直接使用。

********************************* Class Definition *********************************
public class SimpleControllerHandlerAdapter implements HandlerAdapter {
	// ...
}
**/