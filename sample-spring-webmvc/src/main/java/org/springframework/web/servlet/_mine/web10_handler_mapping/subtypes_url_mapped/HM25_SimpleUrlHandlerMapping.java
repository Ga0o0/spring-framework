package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_url_mapped;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SimpleUrlHandlerMapping
 *
 * <p>实现 org.springframework.web.servlet.HandlerMapping 接口，将 URL 映射到请求处理程序 Bean。
 * 支持映射到 Bean 实例和 Bean 名称；非单例处理程序必须使用 Bean 名称。
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMappingTests
 */
public class HM25_SimpleUrlHandlerMapping {

	/**
	 * @see SimpleUrlHandlerMapping#SimpleUrlHandlerMapping()
	 * @see SimpleUrlHandlerMapping#SimpleUrlHandlerMapping(java.util.Map)
	 * @see SimpleUrlHandlerMapping#SimpleUrlHandlerMapping(java.util.Map, int)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Params
			Map<String, Object> urlMap = new LinkedHashMap<>(); // URL 映射
			int order = Ordered.LOWEST_PRECEDENCE;

			// Constructors
			// 创建一个具有默认设置的 SimpleUrlHandlerMapping
			SimpleUrlHandlerMapping handlerMapping1 = new SimpleUrlHandlerMapping();
			// 使用提供的 URL 映射创建 SimpleUrlHandlerMapping
			SimpleUrlHandlerMapping handlerMapping2 = new SimpleUrlHandlerMapping(urlMap);
			// 使用提供的 URL 映射和 order 创建一个 SimpleUrlHandlerMapping
			SimpleUrlHandlerMapping handlerMapping3 = new SimpleUrlHandlerMapping(urlMap, order);

			// Print
			System.out.println(handlerMapping1);
			System.out.println(handlerMapping2);
			System.out.println(handlerMapping3);
		}
	}

	/**
	 * @see SimpleUrlHandlerMapping#setMappings(java.util.Properties)
	 * @see SimpleUrlHandlerMapping#setUrlMap(java.util.Map)
	 * @see SimpleUrlHandlerMapping#getUrlMap()
	 */
	static class Methods {
		public static void main(String[] args) throws Exception {
			// 创建一个具有默认设置的 SimpleUrlHandlerMapping
			SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();

			// SimpleUrlHandlerMapping#setMappings(java.util.Properties)
			Properties mappings = new Properties();
			mappings.setProperty("controller", "bean:name=controller");
			mapping.setMappings(mappings);

			// SimpleUrlHandlerMapping#setUrlMap(java.util.Map)
			Map<String, Object> urlMap = new LinkedHashMap<>(); // URL 映射
			Object controller = new Object();
			urlMap.put("/*/baz", controller);
			mapping.setUrlMap(urlMap);

			// SimpleUrlHandlerMapping#getUrlMap()
			Map<String, ?> mappingUrlMap = mapping.getUrlMap();
			System.out.println(mappingUrlMap);
		}
	}

}
/*
********************************* Class API Docs *********************************
实现 org.springframework.web.servlet.HandlerMapping 接口，将 URL 映射到请求处理程序 Bean。
支持映射到 Bean 实例和 Bean 名称；非单例处理程序必须使用 Bean 名称。

<p>“urlMap” 属性适用于使用 Bean 引用填充处理程序映射，例如通过 XML Bean 定义中的 map 元素。

<p>可以通过 “mappings” 属性设置 Bean 名称的映射，其格式为 java.util.Properties 类接受的格式，如下所示：
<pre class="code">
	/welcome.html=ticketController
	/show.html=ticketController
</pre>

<p>语法为 {@code PATH=HANDLER_BEAN_NAME}。如果路径不以斜杠开头，则会在前面添加一个斜杠。

<p>支持直接匹配（例如，给定 "/test" -> 注册的 "/test”）和 "*" 匹配（例如，给定 "/test" -> 注册的 "/t*"）。
有关模式选项的详细信息，请参阅 org.springframework.web.util.pattern.PathPattern javadoc。

********************************* Class Definition *********************************
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {
	private final Map<String, Object> urlMap = new LinkedHashMap<>();
	// ...
}
**/
