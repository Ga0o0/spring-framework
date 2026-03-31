package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_other_mapped;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RouterFunctionMapping
 *
 * @see org.springframework.web.servlet.function.support.RouterFunctionMapping
 */
public class HM45_RouterFunctionMapping {

	/**
	 *  @see RouterFunctionMapping#RouterFunctionMapping()
	 *  @see RouterFunctionMapping#RouterFunctionMapping(org.springframework.web.servlet.function.RouterFunction)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Params
			HandlerFunction<ServerResponse> function = request -> ServerResponse.ok().build();
			RouterFunction<ServerResponse> routerFunction = RouterFunctions.route().GET("/*/fn", function).build();

			// Constructors
			RouterFunctionMapping routerFunctionMapping1 = new RouterFunctionMapping();
			RouterFunctionMapping routerFunctionMapping2 = new RouterFunctionMapping(routerFunction);

			// Print
			System.out.println(routerFunctionMapping1);
			System.out.println(routerFunctionMapping2);
		}
	}

	/**
	 *  @see RouterFunctionMapping#setRouterFunction(RouterFunction)
	 *  @see RouterFunctionMapping#getRouterFunction()
	 *  @see RouterFunctionMapping#setMessageConverters(List)
	 *  @see RouterFunctionMapping#setDetectHandlerFunctionsInAncestorContexts(boolean)
	 */
	static class Methods {
		public static void main(String[] args) {
			// Params
			// RouterFunction
			HandlerFunction<ServerResponse> handlerFunction = request -> ServerResponse.ok().build();
			RouterFunction<ServerResponse> routerFunction = RouterFunctions.route().GET("/*/fn", handlerFunction).build();
			// HttpMessageConverters
			List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(4);
			messageConverters.add(new ByteArrayHttpMessageConverter());
			messageConverters.add(new StringHttpMessageConverter());
			messageConverters.add(new AllEncompassingFormHttpMessageConverter());

			RouterFunctionMapping routerFunctionMapping = new RouterFunctionMapping();

			// Methods
			routerFunctionMapping.setRouterFunction(routerFunction);
			RouterFunction<?> function = routerFunctionMapping.getRouterFunction();
			routerFunctionMapping.setMessageConverters(messageConverters);
			routerFunctionMapping.setDetectHandlerFunctionsInAncestorContexts(true);

			// Print
			System.out.println(function);
		}
	}

}
/*
********************************* Class API Docs *********************************
HandlerMapping 实现支持 RouterFunctions。

<p>如果在 {@linkplain #RouterFunctionMapping(RouterFunction) 构造时} 未提供 {@link RouterFunction}，
则此映射将检测应用程序上下文中的所有路由器功能，并在 {@linkplain org.springframework.core.annotation.Order order} 中查阅它们。

********************************* Class Definition *********************************
public class RouterFunctionMapping extends AbstractHandlerMapping implements InitializingBean, MatchableHandlerMapping {
	private RouterFunction<?> routerFunction;
	private List<HttpMessageConverter<?>> messageConverters = Collections.emptyList();
	private boolean detectHandlerFunctionsInAncestorContexts = false;
	// ...
}
**/