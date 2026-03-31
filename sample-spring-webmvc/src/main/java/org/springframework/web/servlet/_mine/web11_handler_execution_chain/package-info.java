/**
 * HandlerExecutionChain
 *
 * ## Handlers
 *
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.HttpRequestHandler
 * @see jakarta.servlet.Servlet
 * @see org.springframework.web.servlet.function.HandlerFunction
 * @see org.springframework.web.method.HandlerMethod
 *
 * @see org.springframework.web.servlet._mine.web10_handler_mapping.handler
 *
 * ## HandlerInterceptor
 *
 * @see org.springframework.web.servlet.HandlerInterceptor
 *
 * ## HandlerExecutionChain(Object handler, List<HandlerInterceptor> interceptorList)
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain
 * @see org.springframework.web.servlet.HandlerExecutionChain#handler
 * @see org.springframework.web.servlet.HandlerExecutionChain#interceptorList
 */
package org.springframework.web.servlet._mine.web11_handler_execution_chain;

/**
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandlerExecutionChain(java.lang.Object, jakarta.servlet.http.HttpServletRequest)
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain
 * @see org.springframework.web.servlet.HandlerExecutionChain#handler
 * @see org.springframework.web.servlet.HandlerExecutionChain#interceptorList
 */