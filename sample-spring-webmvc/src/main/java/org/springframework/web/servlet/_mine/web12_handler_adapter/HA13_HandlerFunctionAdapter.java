package org.springframework.web.servlet._mine.web12_handler_adapter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.support.HandlerFunctionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * HandlerFunctionAdapter
 *
 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter
 *
 * @see org.springframework.web.servlet.function.HandlerFunction
 * @see org.springframework.web.servlet.function.ServerRequest
 * @see org.springframework.web.servlet.function.ServerResponse
 */
public class HA13_HandlerFunctionAdapter {

	/**
	 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter#supports(Object)
	 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter#getLastModified(HttpServletRequest, Object)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();

		// HttpMessageConverters
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(4);
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter());
		messageConverters.add(new AllEncompassingFormHttpMessageConverter());
		request.setAttribute(RouterFunctions.REQUEST_ATTRIBUTE, ServerRequest.create(request, messageConverters));

		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// HandlerFunctionAdapter
		HandlerFunctionAdapter handlerAdapter = new HandlerFunctionAdapter();

		// 1. HandlerFunctionAdapter#supports(Object)
		boolean supports1 = handlerAdapter.supports(handler);
		boolean supports2 = handlerAdapter.supports(new Object());
		System.out.println("HandlerFunctionAdapter supports MyController: " 	+ supports1);
		System.out.println("HandlerFunctionAdapter supports Object: " 		+ supports2);

		// 2. HandlerFunctionAdapter#handle(...)
		ModelAndView mv = handlerAdapter.handle(request, response, handler);
		System.out.println("HandlerFunctionAdapter handle MyHandler: " 		+ mv);

		// 3. HandlerFunctionAdapter#getLastModified(HttpServletRequest, Object)
		long lastModified = handlerAdapter.getLastModified(request, handler);
		System.out.println("HandlerFunctionAdapter lastModified: " 			+ lastModified);
	}

	/**
	 * MyHandler impl {@link org.springframework.web.servlet.function.HandlerFunction}
	 *
	 * @see org.springframework.web.servlet.function.HandlerFunction
	 * @see org.springframework.web.servlet.function.ServerRequest
	 * @see org.springframework.web.servlet.function.ServerResponse
	 */
	static class MyHandler implements HandlerFunction<ServerResponse> {
		@Override
		@NonNull
		public ServerResponse handle(@NonNull ServerRequest request) throws Exception {
			return ServerResponse.ok().body("Hello World");
		}
	}
}
/*
********************************* Class API Docs *********************************
HandlerAdapter 实现支持 HandlerFunction。

********************************* Class Definition *********************************
public class HandlerFunctionAdapter implements HandlerAdapter, Ordered {
	// ...
	private int order = Ordered.LOWEST_PRECEDENCE;
	private Long asyncRequestTimeout;
	// ...
}
**/