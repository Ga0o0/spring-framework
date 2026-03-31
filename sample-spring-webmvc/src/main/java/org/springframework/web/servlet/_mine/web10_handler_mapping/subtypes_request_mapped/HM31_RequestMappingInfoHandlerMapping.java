package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped;

/**
 * RequestMappingInfoHandlerMapping
 *
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping
 */
public class HM31_RequestMappingInfoHandlerMapping {
}
/*
********************************* Class API Docs *********************************
RequestMappingInfo 定义 request 和 handler method 之间的映射的类的抽象基类。

********************************* Class Definition *********************************
public abstract class RequestMappingInfoHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> {
	private static final Method HTTP_OPTIONS_HANDLE_METHOD;
	static {
		// ...
		HTTP_OPTIONS_HANDLE_METHOD = HttpOptionsHandler.class.getMethod("handle");
		// ...
	}
	// ...
}
**/