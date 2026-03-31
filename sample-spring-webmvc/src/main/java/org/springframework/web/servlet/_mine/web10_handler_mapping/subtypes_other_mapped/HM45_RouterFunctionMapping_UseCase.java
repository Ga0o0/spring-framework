package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_other_mapped;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

/**
 * RouterFunctionMapping
 *
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping
 *
 * ## register Handlers
 *
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping#afterPropertiesSet()
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping#routerFunction
 *
 * ## get Handler
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping#getHandlerInternal(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping#routerFunction
 * @see org.springframework.web.servlet.function.RouterFunction#route(org.springframework.web.servlet.function.ServerRequest)
 * @see org.springframework.web.servlet.function.HandlerFunction
 */
public class HM45_RouterFunctionMapping_UseCase {
	/* -------------------------------- register/get Handler() 的逻辑 ---------------------
	1. register Handlers
		handler -> org.springframework.web.servlet.function.HandlerFunction

		RouterFunctionMapping#routerFunction ->  RouterFunctionMapping { HandlerFunction }

	2. get Handlers
		AbstractHandlerMapping#getHandler(...) ->  RouterFunctionMapping#routerFunction#route(...) -> HandlerFunction
	**/
	public static void main(String[] args) throws Exception {
		HandlerFunction<ServerResponse> handlerFunction = request -> {
			System.out.println("request is: " + request);
			return ServerResponse.ok().build();
		};
		RouterFunction<ServerResponse> routerFunction = RouterFunctions.route()
				.GET("/*/baz", handlerFunction).build();

		RouterFunctionMapping functionMapping = new RouterFunctionMapping();
		functionMapping.setRouterFunction(routerFunction);

		// HttpServletRequest
		MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/*/baz");
		HandlerExecutionChain handler1 = functionMapping.getHandler(request1);
		System.out.println("request: /*/baz -> " + handler1);

		MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/foo/baz");
		HandlerExecutionChain handler2 = functionMapping.getHandler(request2);
		System.out.println("request: /foo/baz -> " + handler2);

		MockHttpServletRequest request3 = new MockHttpServletRequest("GET", "/foo/baz2");
		HandlerExecutionChain handler3 = functionMapping.getHandler(request3);
		System.out.println("request: /foo/baz2 -> " + handler3);
	}
}
