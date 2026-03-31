/**
 * Servlet#service(...)
 *
 * @see jakarta.servlet.Servlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 */
package org.springframework.web.servlet._mine.servlet.servlet02_service;

/**
 * Servlet#service(...) - HttpServlet#service(...)
 *
 * @see jakarta.servlet.Servlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 *
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */

/**
 * Servlet#service(...) - FrameworkServlet#doXxx(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see jakarta.servlet.Servlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 *
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## 处理各种请求
 *
 * @see org.springframework.web.servlet.FrameworkServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doHead(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doPost(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doPut(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doDelete(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doOptions(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doTrace(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */

/**
 * Servlet#service(...) - FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.FrameworkServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## FrameworkServlet#processRequest(...)
 *
 * @see org.springframework.web.servlet.FrameworkServlet#processRequest(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#doService(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */

/**
 * Servlet#service(...) - DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## 1. checkMultipart
 *
 * @see org.springframework.web.servlet.DispatcherServlet#checkMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#isMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#resolveMultipart(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 2. getHandler
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.HandlerExecutionChain
 *
 * ## 3. getHandlerAdapter -> 选择一个 HandlerAdapter 来处理当前 Handler
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getHandlerAdapter(java.lang.Object)
 * @see org.springframework.web.servlet.HandlerAdapter#supports(java.lang.Object)
 *
 * ## 4. 调用已注册 interceptors 的 preHandle 方法，所有 preHandle 调用成功完成并返回 true 的 interceptors 都会调用 afterCompletion 回调。
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#applyPreHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#triggerAfterCompletion(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Exception)
 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
 *
 * ## 5. 使用给定的 handler 来处理当前请求
 *
 * @see org.springframework.web.servlet.HandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * ## 6. 视图名称翻译：执行 RequestToViewNameTranslator#getViewName(HttpServletRequest) 方法从 HttpServletRequest 转换 view 名称，并设置给 ModelAndView#view 属性
 *
 * @see org.springframework.web.servlet.DispatcherServlet#applyDefaultViewName(jakarta.servlet.http.HttpServletRequest, org.springframework.web.servlet.ModelAndView)
 * @see org.springframework.web.servlet.ModelAndView#view
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultViewName(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.RequestToViewNameTranslator#getViewName(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 7. 应用已注册 interceptors 的 postHandle 方法；即：HandlerInterceptor#postHandle()。
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain#applyPostHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.servlet.ModelAndView)
 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
 */

/**
 * Servlet#service(...) - HandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * @see org.springframework.web.servlet.HandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 *
 * # RequestMappingHandlerAdapter
 *
 * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, java.lang.Object)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#handleInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.method.HandlerMethod)
 *
 *
 * ## 1. 检查给定请求是否支持方法以及所需的会话（如果有）。
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#checkRequest(jakarta.servlet.http.HttpServletRequest)
 *
 *
 * ## 2. 如果需要视图解析，则调用 RequestMapping handler 方法准备 ModelAndView
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#invokeHandlerMethod(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.method.HandlerMethod)
 *
 * ### 2.1. 调用该方法并通过已配置的 HandlerMethodReturnValueHandlers 之一处理返回值
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 *
 * #### 2.1.1. 在给定请求的上下文中解析其参数值后调用该方法
 *
 * @see org.springframework.web.method.support.InvocableHandlerMethod#invokeForRequest(org.springframework.web.context.request.NativeWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 * @see org.springframework.web.method.support.InvocableHandlerMethod#getMethodArgumentValues(org.springframework.web.context.request.NativeWebRequest, org.springframework.web.method.support.ModelAndViewContainer, java.lang.Object...)
 * @see org.springframework.validation.method.MethodValidator#applyArgumentValidation(java.lang.Object, java.lang.reflect.Method, org.springframework.core.MethodParameter[], java.lang.Object[], java.lang.Class[])
 * @see org.springframework.web.method.support.InvocableHandlerMethod#doInvoke(java.lang.Object...)
 * @see org.springframework.validation.method.MethodValidator#applyReturnValueValidation(java.lang.Object, java.lang.reflect.Method, org.springframework.core.MethodParameter, java.lang.Object, java.lang.Class[])
 *
 * #### 2.1.2. 遍历已注册的 HandlerMethodReturnValueHandlers 并调用支持它的那个
 *
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite#handleReturnValue(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite#selectHandler(java.lang.Object, org.springframework.core.MethodParameter)
 * @see org.springframework.web.method.support.HandlerMethodReturnValueHandler#handleReturnValue(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
 *
 * ### 2.2. 创建一个 ModelAndView
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#getModelAndView(org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.method.annotation.ModelFactory, org.springframework.web.context.request.NativeWebRequest)
 * @see org.springframework.web.method.annotation.ModelFactory#updateModel(org.springframework.web.context.request.NativeWebRequest, org.springframework.web.method.support.ModelAndViewContainer)
 * @see org.springframework.web.servlet.ModelAndView#ModelAndView(String, java.util.Map, org.springframework.http.HttpStatusCode)
 *
 * ## 3. 处理请求头 Cache-Control
 *
 * ### 3.1. HandlerMethod 所在类被 @SessionAttributes 注解标注，并且其属性有值
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheSeconds(jakarta.servlet.http.HttpServletResponse, int)
 *
 * ### 3.2. HandlerMethod 所在类被 @SessionAttributes 注解标注，并且其属性无值
 *
 * @see org.springframework.web.servlet.support.WebContentGenerator#prepareResponse(jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheControl(jakarta.servlet.http.HttpServletResponse, org.springframework.http.CacheControl)
 * @see org.springframework.web.servlet.support.WebContentGenerator#applyCacheSeconds(jakarta.servlet.http.HttpServletResponse, int)
 *
 */

