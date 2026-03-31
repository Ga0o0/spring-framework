/**
 * HandlerExceptionResolver
 *
 * @see org.springframework.web.servlet.HandlerExceptionResolver
 * @see org.springframework.web.servlet.handler.HandlerExceptionResolverComposite
 * @see org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver
 * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver
 * @see org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
 * @see org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
 * @see org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver
 */
package org.springframework.web.servlet._mine.web01_strategies.strategies06_handler_exception_resolver;

/*
HandlerExceptionResolver										[interface]
	\--impl----- HandlerExceptionResolverComposite				[class]
	\--impl----- AbstractHandlerExceptionResolver				[abstract class]
		\--extends-- SimpleMappingExceptionResolver				[class]
		\--extends-- AbstractHandlerMethodExceptionResolver		[abstract class]
			\--extends--ExceptionHandlerExceptionResolver		[class]
		\--extends--DefaultHandlerExceptionResolver				[class]
		\--extends--ResponseStatusExceptionResolver				[class]
*/
