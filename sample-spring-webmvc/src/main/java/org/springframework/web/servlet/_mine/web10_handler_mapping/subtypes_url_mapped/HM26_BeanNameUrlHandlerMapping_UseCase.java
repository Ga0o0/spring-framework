package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_url_mapped;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

/**
 * BeanNameUrlHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 *
 * ## detect Handlers
 *
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping#setApplicationContext(org.springframework.context.ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#setApplicationContext(org.springframework.context.ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext(org.springframework.context.ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext()
 * @see org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping#initApplicationContext()
 * @see org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping#detectHandlers()
 *
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping#determineUrlsForHandler(java.lang.String)
 *
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#registerHandler(java.lang.String[], java.lang.String)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#registerHandler(java.lang.String, java.lang.Object)
 *
 * ## get Handler
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#getHandlerInternal(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#lookupHandler(java.lang.String, jakarta.servlet.http.HttpServletRequest)
 */
public class HM26_BeanNameUrlHandlerMapping_UseCase {

	/* -------------------------------- detect/get Handler() 的逻辑 ---------------------
		1. 检查 ApplicationContext 中所有的 bean/alias 名称，筛选以 ”/“ 开头的名称，并认定它为 URL path
		ApplicationContext#bean					-> { beanName: 		URL path（以 / 开头）,
											 		 bean instance: handler bean instance }

		2. 遍历以上的 URL paths，并以 URL path 为并 bean name 从 ApplicationContext 中获取其对应的 handler bean instance，
		   并以 { key: URL path(bean name), value: handler bean instance } 的格式放入 AbstractUrlHandlerMapping#handlerMap 中
		AbstractUrlHandlerMapping#handlerMap 	-> { key: URL path, value: handler bean instance}


		AbstractHandlerMapping#getHandler() -> AbstractUrlHandlerMapping#handlerMap -> URL path(bean name) -> handler bean instance
	**/

	public static void main(String[] args) throws Exception {
		// 创建一个具有默认设置的 SimpleUrlHandlerMapping
		BeanNameUrlHandlerMapping handlerMapping = new BeanNameUrlHandlerMapping();

		// StaticApplicationContext
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerBean("/*/baz", Object.class, Object::new);
		// -> AbstractUrlHandlerMapping#initApplicationContext()
		handlerMapping.setApplicationContext(context);

		// HttpServletRequest
		MockHttpServletRequest request1 = new MockHttpServletRequest("GET", "/*/baz");
		HandlerExecutionChain handler1 = handlerMapping.getHandler(request1);
		System.out.println("request: /*/baz -> " + handler1);

		MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/foo/baz");
		HandlerExecutionChain handler2 = handlerMapping.getHandler(request2);
		System.out.println("request: /foo/baz -> " + handler2);

		MockHttpServletRequest request3 = new MockHttpServletRequest("GET", "/foo/baz2");
		HandlerExecutionChain handler3 = handlerMapping.getHandler(request3);
		System.out.println("request: /foo/baz2 -> " + handler3);
	}
}
