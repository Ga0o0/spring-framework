package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped;

import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;

/**
 * RequestMappingHandlerMapping
 *
 * <p>从 {@link Controller @Controller} 类中的类级和方法级 {@link RequestMapping @RequestMapping}
 * 和 {@link HttpExchange @HttpExchange} 注释创建 {@link RequestMappingInfo} 实例。
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#afterPropertiesSet()
 */
public class HM36_RequestMappingHandlerMapping_UseCase {

	/**
	 * 扫描 @Controller + @RequestMapping/@HttpExchange 注解的例子
	 */
	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		// RequestMappingHandlerMapping#afterPropertiesSet() 会自动扫描 @Controller + @RequestMapping/@HttpExchange 注解
		context.registerBean(RequestMappingHandlerMapping.class);
		context.registerBean(ObjectControllerWithRequestMapping.class); // Controller
		context.registerBean(ObjectControllerWithHttpExchange.class); // Controller
		context.refresh();

		RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
		List<HandlerMethod> handlerMethodsForMappingName = handlerMapping.getHandlerMethodsForMappingName("");

		// Print
		System.out.println("handlerMethods: " + handlerMethods);
		System.out.println("handlerMethodsForMappingName: " + handlerMethodsForMappingName);
	}

	/*------------------------- @HttpExchange vs. @RequestMapping -------------------------
	@HttpExchange 和 @RequestMapping 都是 Spring 中用于处理 HTTP 请求的核心注解，但它们的设计目标和应用场景截然不同。

	简单来说，@RequestMapping 主要用于服务端，负责将请求 “接入” 你的应用；
	而 @HttpExchange 则服务于客户端，负责定义如何 “调出” 去访问别的服务。

	当你开发一个微服务，需要为前端或其他服务提供 API 接口时，使用 @RequestMapping。
	它是 Spring MVC 的核心，用于构建 RESTful 服务。
	当你的应用需要调用另一个微服务（如通过 Eureka 注册中心）或任何第三方 HTTP API 时，使用 @HttpExchange。
	它是 Spring 官方推荐的、替代 OpenFeign 和 RestTemplate 的声明式 HTTP 客户端方案，能帮你写出更简洁、类型安全的调用代码 。
	**/

	/**
	 * @Controller + @RequestMapping
	 */
	@Controller
	@RequestMapping("/request_mapping")
	static class ObjectControllerWithRequestMapping {
		@RequestMapping("/{id}")
		public Object getById(@PathVariable String id) {
			System.out.println("getById() of ObjectControllerWithRequestMapping, id is: " + id);
			return new Object();
		}
	}

	/**
	 * @Controller + @HttpExchange
	 */
	@Controller
	@HttpExchange("/http_exchange")
	static class ObjectControllerWithHttpExchange {
		@HttpExchange("/{id}")
		public Object getById(@PathVariable String id) {
			System.out.println("getById() of ObjectControllerWithHttpExchange, id is: " + id);
			return new Object();
		}
	}
}
