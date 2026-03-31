package org.springframework.web.servlet._mine.web12_handler_adapter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;

import java.io.IOException;

/**
 * HttpRequestHandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 * @see org.springframework.web.HttpRequestHandler
 */
public class HA11_HttpRequestHandlerAdapter {

	/**
	 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter#supports(Object)
	 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter#getLastModified(HttpServletRequest, Object)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// HttpRequestHandlerAdapter
		HttpRequestHandlerAdapter handlerAdapter = new HttpRequestHandlerAdapter();

		// 1. HttpRequestHandlerAdapter#supports(Object)
		boolean supports1 = handlerAdapter.supports(handler);
		boolean supports2 = handlerAdapter.supports(new Object());
		System.out.println("HttpRequestHandlerAdapter supports MyController: " 	+ supports1);
		System.out.println("HttpRequestHandlerAdapter supports Object: " 		+ supports2);

		// 2. HttpRequestHandlerAdapter#handle(...)
		ModelAndView mv = handlerAdapter.handle(request, response, handler);
		System.out.println("HttpRequestHandlerAdapter handle MyHandler: " 		+ mv);

		// 3. HttpRequestHandlerAdapter#getLastModified(HttpServletRequest, Object)
		long lastModified = handlerAdapter.getLastModified(request, handler);
		System.out.println("HttpRequestHandlerAdapter lastModified: " 			+ lastModified);
	}

	/**
	 * MyHandler impl {@link org.springframework.web.HttpRequestHandler}
	 *
	 * @see org.springframework.web.HttpRequestHandler
	 */
	static class MyHandler implements HttpRequestHandler {
		@Override
		public void handleRequest(@NonNull HttpServletRequest request,
								  @NonNull HttpServletResponse response) throws ServletException, IOException {
			System.out.println("MyHandler impl HttpRequestHandler...");
		}
	}
}
/*
********************************* Class API Docs *********************************
适配器将普通的 org.springframework.web.HttpRequestHandler 接口
与通用的 org.springframework.web.servlet.DispatcherServlet 接口结合使用。
支持实现 LastModified 接口的处理程序。

<p>这是一个 SPI 类，应用程序代码不直接使用。

********************************* Class Definition *********************************
public class HttpRequestHandlerAdapter implements HandlerAdapter {
	// ...
}
**/
