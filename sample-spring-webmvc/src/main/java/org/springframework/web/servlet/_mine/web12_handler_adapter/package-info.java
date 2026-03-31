/**
 * HandlerAdapter
 *
 * @see org.springframework.web.servlet.HandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter
 */
package org.springframework.web.servlet._mine.web12_handler_adapter;

/*
HandlerAdapter 										[interface]
	\--impl----- SimpleServletHandlerAdapter 		[class]
	\--impl----- HttpRequestHandlerAdapter 			[class]
	\--impl----- HandlerFunctionAdapter 			[class]
	\--impl----- SimpleControllerHandlerAdapter 	[class]
	\--impl----- AbstractHandlerMethodAdapter 		[abstract class]
		\--extends- RequestMappingHandlerAdapter 	[class]
**/

/**
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#getHandlerAdapter(java.lang.Object)
 * @see org.springframework.web.servlet.HandlerAdapter#supports(java.lang.Object)
 */
