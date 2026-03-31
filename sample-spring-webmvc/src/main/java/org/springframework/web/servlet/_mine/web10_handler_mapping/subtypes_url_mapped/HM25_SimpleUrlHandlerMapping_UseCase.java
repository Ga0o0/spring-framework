package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_url_mapped;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.Properties;

/**
 * SimpleUrlHandlerMapping
 *
 * <p>实现 org.springframework.web.servlet.HandlerMapping 接口，将 URL 映射到请求处理程序 Bean。
 * 支持映射到 Bean 实例和 Bean 名称；非单例处理程序必须使用 Bean 名称。
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 *
 * ## register Handlers
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping#setApplicationContext(ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#setApplicationContext(ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext(ApplicationContext)
 * @see org.springframework.context.support.ApplicationObjectSupport#initApplicationContext()
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping#initApplicationContext()
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping#registerHandlers(java.util.Map)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#registerHandler(java.lang.String, java.lang.Object)
 *
 * ## get Handler
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping#getHandler(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#getHandlerInternal(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping#lookupHandler(java.lang.String, jakarta.servlet.http.HttpServletRequest)
 */
public class HM25_SimpleUrlHandlerMapping_UseCase {
	/* -------------------------------- AbstractHandlerMapping#getHandler() 的逻辑 ---------------------

	1. 遍历 SimpleUrlHandlerMapping#urlMap 的 values，如果其为 String 类型
	   则使用 ApplicationContext#getBean(handlerName) 获取对应的 bean instance，
	   并遵从 { key: URL path, value: handler bean instance} 的格式将数据放入 AbstractUrlHandlerMapping#handlerMap  中
	   两个特例 URL path:
	   		/ 	-> AbstractUrlHandlerMapping#rootHandler
			/*	-> AbstractUrlHandlerMapping#defaultHandler

	SimpleUrlHandlerMapping#urlMap 	-> { key: URL path, value: handler bean name}

	ApplicationContext#bean			-> { beanName: 		handler bean name,
										 bean instance: handler bean instance }

	AbstractUrlHandlerMapping#handlerMap 	-> { key: URL path, value: handler bean instance}

	AbstractHandlerMapping#getHandler() = URL path -> handler bean name -> handler bean instance
	**/
	public static void main(String[] args) throws Exception {
		// 创建一个具有默认设置的 SimpleUrlHandlerMapping
		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();

		// set urlMap
		Properties mappings = new Properties();
		mappings.setProperty("/*/baz", "controller");
		handlerMapping.setMappings(mappings);

		// StaticApplicationContext
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerBean("controller", Object.class, Object::new);
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
