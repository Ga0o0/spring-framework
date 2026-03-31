package org.springframework.web.servlet._mine.web10_handler_mapping;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * HandlerMapping
 *
 * @see org.springframework.web.servlet.HandlerMapping
 */
public class HM01_HandlerMapping {

	/**
	 * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_HANDLER_ATTRIBUTE
	 * @see org.springframework.web.servlet.HandlerMapping#LOOKUP_PATH
	 * @see org.springframework.web.servlet.HandlerMapping#PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE
	 * @see org.springframework.web.servlet.HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE
	 * @see org.springframework.web.servlet.HandlerMapping#INTROSPECT_TYPE_LEVEL_MAPPING
	 * @see org.springframework.web.servlet.HandlerMapping#URI_TEMPLATE_VARIABLES_ATTRIBUTE
	 * @see org.springframework.web.servlet.HandlerMapping#MATRIX_VARIABLES_ATTRIBUTE
	 * @see org.springframework.web.servlet.HandlerMapping#PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
	 *
	 * @see org.springframework.web.servlet.HandlerMapping#usesPathPatterns()
	 * @see org.springframework.web.servlet.HandlerMapping#getHandler(HttpServletRequest)
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
		// System.out.println(HandlerMapping.LOOKUP_PATH);
		System.out.println(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		System.out.println(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		System.out.println(HandlerMapping.INTROSPECT_TYPE_LEVEL_MAPPING);
		System.out.println(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		System.out.println(HandlerMapping.MATRIX_VARIABLES_ATTRIBUTE);
		System.out.println(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

		// HandlerMapping
		HandlerMapping handlerMapping = new RequestMappingHandlerMapping();

		// Methods
		// HandlerMapping 实例是否已启用已解析的 PathPattern
		boolean usesPathPatterns = handlerMapping.usesPathPatterns();
		// 返回当前请求的 handler 及其所有 interceptors
		HttpServletRequest request = null;
		HandlerExecutionChain executionChain = handlerMapping.getHandler(request);

		// Print
		System.out.println(usesPathPatterns);
		System.out.println(executionChain);
	}
}
/*
********************************* Class API Docs *********************************
定义 requests 和 handler objects 之间映射的对象需要实现的接口。

<p>此类可由应用程序开发人员实现，但并非必需，因为框架中包含
{@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping} 和
{@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}。
如果应用程序上下文中未注册 HandlerMapping bean，则默认使用前者。

<p>HandlerMapping 实现可以支持映射的拦截器，但这不是必须的。
handler 始终包装在 {@link HandlerExecutionChain} 实例中，并可选地附带一些 {@link HandlerInterceptor} 实例。
DispatcherServlet 将首先按给定顺序调用每个 HandlerInterceptor 的 {@code preHandle} 方法，如果所有 {@code preHandle} 方法都返回 {@code true}，则最终调用处理程序本身。

<p>参数化映射的能力是此 MVC 框架的一项强大而独特的功能。例如，可以基于会话状态、Cookie 状态或许多其他变量编写自定义映射。似乎没有其他 MVC 框架拥有如此高的灵活性。

<p>注意：实现可以实现 {@link org.springframework.core.Ordered} 接口，以便指定排序顺序，从而指定 DispatcherServlet 应用的优先级。非 Ordered 实例将被视为最低优先级。

********************************* Class Definition *********************************
public interface HandlerMapping {
	String BEST_MATCHING_HANDLER_ATTRIBUTE 			= HandlerMapping.class.getName() + ".bestMatchingHandler";
	@Deprecated String LOOKUP_PATH 					= HandlerMapping.class.getName() + ".lookupPath";
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE 	= HandlerMapping.class.getName() + ".pathWithinHandlerMapping";
	String BEST_MATCHING_PATTERN_ATTRIBUTE 			= HandlerMapping.class.getName() + ".bestMatchingPattern";
	String INTROSPECT_TYPE_LEVEL_MAPPING 			= HandlerMapping.class.getName() + ".introspectTypeLevelMapping";
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE 		= HandlerMapping.class.getName() + ".uriTemplateVariables";
	String MATRIX_VARIABLES_ATTRIBUTE 				= HandlerMapping.class.getName() + ".matrixVariables";
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE 		= HandlerMapping.class.getName() + ".producibleMediaTypes";

	default boolean usesPathPatterns() {
		return false;
	}
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;
}
**/
