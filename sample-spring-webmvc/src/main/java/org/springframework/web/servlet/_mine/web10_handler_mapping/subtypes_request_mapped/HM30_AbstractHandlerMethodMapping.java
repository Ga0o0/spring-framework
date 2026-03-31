package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMethodMappingNamingStrategy;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * AbstractHandlerMethodMapping
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping
 */
public class HM30_AbstractHandlerMethodMapping {

	@Controller
	static class MyHandler {
		@RequestMapping
		public void handlerMethod1() {}

		@RequestMapping
		public void handlerMethod2() {}

		@RequestMapping
		@CrossOrigin(originPatterns = "*")
		public void corsHandlerMethod() {}
	}

	/**
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#setDetectHandlerMethodsInAncestorContexts(boolean)
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#setHandlerMethodMappingNamingStrategy(org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy)
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getNamingStrategy()
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerMethods()
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#getHandlerMethodsForMappingName(String)
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#registerMapping(Object, Object, java.lang.reflect.Method)
	 * @see org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#unregisterMapping(Object)
	 */
	public static void main(String[] args) throws NoSuchMethodException {
		AbstractHandlerMethodMapping<RequestMappingInfo> handlerMethodMapping = new RequestMappingHandlerMapping();

		// Methods
		// AbstractHandlerMethodMapping#setDetectHandlerMethodsInAncestorContexts(boolean)
		handlerMethodMapping.setDetectHandlerMethodsInAncestorContexts(false);
		// AbstractHandlerMethodMapping#setHandlerMethodMappingNamingStrategy(HandlerMethodMappingNamingStrategy)
		HandlerMethodMappingNamingStrategy<RequestMappingInfo> namingStrategy = new RequestMappingInfoHandlerMethodMappingNamingStrategy();
		handlerMethodMapping.setHandlerMethodMappingNamingStrategy(namingStrategy);
		// AbstractHandlerMethodMapping#getNamingStrategy()
		HandlerMethodMappingNamingStrategy<RequestMappingInfo> mappingNamingStrategy = handlerMethodMapping.getNamingStrategy();
		// AbstractHandlerMethodMapping#getHandlerMethods()
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMethodMapping.getHandlerMethods();
		// AbstractHandlerMethodMapping#getHandlerMethodsForMappingName(String)
		List<HandlerMethod> handlerMethodsForMappingName = handlerMethodMapping.getHandlerMethodsForMappingName("");
		// AbstractHandlerMethodMapping#registerMapping(Object, Object, java.lang.reflect.Method)
		RequestMappingInfo mapping = createRequestMappingInfo();
		Object handler = new MyHandler();
		Method method = MyHandler.class.getDeclaredMethod("handlerMethod1");
		handlerMethodMapping.registerMapping(mapping, handler, method);
		// AbstractHandlerMethodMapping#unregisterMapping(Object)
		handlerMethodMapping.unregisterMapping(mapping);
	}

	public static RequestMappingInfo createRequestMappingInfo() {
		return RequestMappingInfo.paths("/getAll").build();
	}
}
/*
********************************* Class API Docs *********************************
HandlerMapping 实现的抽象基类，定义 request 和 HandlerMethod 之间的映射。

<p>对于每个已注册的处理程序方法，都会维护一个唯一的映射，并通过子类定义映射类型 <T> 的详细信息。

********************************* Class Definition *********************************
public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean {
	private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";
	private static final HandlerMethod PREFLIGHT_AMBIGUOUS_MATCH =
			new HandlerMethod(new AbstractHandlerMethodMapping.EmptyHandler(), ClassUtils.getMethod(AbstractHandlerMethodMapping.EmptyHandler.class, "handle"));

	private static final CorsConfiguration ALLOW_CORS_CONFIG = new CorsConfiguration();
	static {
		ALLOW_CORS_CONFIG.addAllowedOriginPattern("*");
		ALLOW_CORS_CONFIG.addAllowedMethod("*");
		ALLOW_CORS_CONFIG.addAllowedHeader("*");
		ALLOW_CORS_CONFIG.setAllowCredentials(true);
	}

	private boolean detectHandlerMethodsInAncestorContexts = false;
	private HandlerMethodMappingNamingStrategy<T> namingStrategy;
	private final AbstractHandlerMethodMapping.MappingRegistry mappingRegistry = new AbstractHandlerMethodMapping.MappingRegistry();
	// ...
}
**/