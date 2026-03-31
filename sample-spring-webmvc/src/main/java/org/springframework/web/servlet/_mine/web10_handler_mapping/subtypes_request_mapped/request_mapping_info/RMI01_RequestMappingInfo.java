package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped.request_mapping_info;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * RequestMappingInfo
 *
 * @see org.springframework.web.servlet.mvc.method.RequestMappingInfo
 * @see org.springframework.web.bind.annotation.RequestMapping
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#createRequestMappingInfo(RequestMapping, RequestCondition)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping#createRequestMappingInfo(HttpExchange, RequestCondition)
 */
public class RMI01_RequestMappingInfo {

	public static void main(String[] args) {
		RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

		// 1. 手动创建 -> RequestMappingInfo
		RequestMappingInfo info = RequestMappingInfo
				.paths("/foo")
				.methods(GET)
				.params("foo=bar", "customFoo=customBar")
				.headers("foo=bar")
				.consumes("text/plain")
				.produces("text/plain")
				.build();

		// 2. 通过 RequestMapping 创建 -> RequestMappingInfo
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(HandlerWithRequestMapping.class, RequestMapping.class);
		RequestMappingInfo requestMappingInfo = RequestMappingInfo
				.paths(requestMapping.path())
				.methods(requestMapping.method())
				.params(requestMapping.params())
				.headers(requestMapping.headers())
				.consumes(requestMapping.consumes())
				.produces(requestMapping.produces())
				.mappingName(requestMapping.name())
				.options(config)
				.build();

		// 3. 通过 HttpExchange 创建 -> RequestMappingInfo
		HttpExchange httpExchange = AnnotationUtils.findAnnotation(HandlerWithHttpExchange.class, HttpExchange.class);
		RequestMappingInfo httpExchangeInfo = RequestMappingInfo
				.paths(StringUtils.hasText(httpExchange.value()) ? new String[] {httpExchange.value()} : new String[0])
				.methods(StringUtils.hasText(httpExchange.method()) ? new RequestMethod[] {RequestMethod.valueOf(httpExchange.method())} : new RequestMethod[0])
				.consumes(StringUtils.hasText(httpExchange.contentType()) ? new String[] {httpExchange.contentType()} : new String[0])
				.produces(httpExchange.accept())
				.options(config)
				.build();

		// Print
		System.out.println(info);
		System.out.println(requestMappingInfo);
		System.out.println(httpExchangeInfo);
	}

	@RequestMapping(path = "request_mapping", method = RequestMethod.GET,
			params = { "foo=bar", "customFoo=customBar" }, headers = { "foo=bar" },
			consumes = { "text/plain" }, produces = { "text/plain" })
	static class HandlerWithRequestMapping {}

	@HttpExchange(url = "http_exchange", method = "GET",
			contentType = "text/plain", accept = { "text/plain" })
	static class HandlerWithHttpExchange {}
}
