/**
 * HandlerAdapter
 *
 * @see org.springframework.web.servlet.HandlerAdapter
 * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 */
package org.springframework.web.servlet._mine.web01_strategies.strategies05_handler_adapter;

/*
HandlerAdapter 										[interface]
	\--impl----- HttpRequestHandlerAdapter 			[class]
	\--impl----- SimpleServletHandlerAdapter 		[class]
	\--impl----- HandlerFunctionAdapter 			[class]
	\--impl----- SimpleControllerHandlerAdapter 	[class]
	\--impl----- AbstractHandlerMethodAdapter 		[abstract class]
		\--extends- RequestMappingHandlerAdapter 	[class]
**/