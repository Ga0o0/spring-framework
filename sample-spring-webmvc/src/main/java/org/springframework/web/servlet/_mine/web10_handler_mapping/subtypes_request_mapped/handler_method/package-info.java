/**
 * @see org.springframework.core.annotation.AnnotatedMethod
 * @see org.springframework.web.method.HandlerMethod
 * @see org.springframework.web.method.support.InvocableHandlerMethod
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
 *
 * @see org.springframework.web.reactive.result.method.InvocableHandlerMethod
 * @see org.springframework.web.reactive.result.method.SyncInvocableHandlerMethod
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerInternal(jakarta.servlet.http.HttpServletRequest)
 */
package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped.handler_method;

/*--------------------------normal-----------------------------
AnnotatedMethod 										[class]
	\--extends-- HandlerMethod 							[class]
		\--extends-- InvocableHandlerMethod 			[class]
			\--extends-- ServletInvocableHandlerMethod 	[class]
**/

/*-------------------------reactive----------------------------
AnnotatedMethod 										[class]
	\--extends-- HandlerMethod 							[class]
		\--extends-- InvocableHandlerMethod 			[class]
		\--extends-- SyncInvocableHandlerMethod 		[class]
**/