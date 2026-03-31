/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerMapping}
 * interface that maps from URLs to request handler beans. Supports both mapping to bean
 * instances and mapping to bean names; the latter is required for non-singleton handlers.
 *
 * <p>The "urlMap" property is suitable for populating the handler map with
 * bean references, e.g. via the map element in XML bean definitions.
 *
 * <p>Mappings to bean names can be set via the "mappings" property, in a form
 * accepted by the {@code java.util.Properties} class, as follows:
 *
 * <pre class="code">
 * /welcome.html=ticketController
 * /show.html=ticketController</pre>
 *
 * <p>The syntax is {@code PATH=HANDLER_BEAN_NAME}. If the path doesn't begin
 * with a slash, one is prepended.
 *
 * <p>Supports direct matches (given "/test" -&gt; registered "/test") and "*"
 * matches (given "/test" -&gt; registered "/t*"). For details on the pattern
 * options, see the {@link org.springframework.web.util.pattern.PathPattern}
 * javadoc.

 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see #setMappings
 * @see #setUrlMap
 * @see BeanNameUrlHandlerMapping
 */
// 实现 {@link org.springframework.web.servlet.HandlerMapping} 接口，将 URL 映射到请求处理程序 Bean。
// 支持映射到 Bean 实例和 Bean 名称；非单例处理程序必须使用 Bean 名称。
//
// <p>“urlMap” 属性适用于使用 Bean 引用填充处理程序映射，例如通过 XML Bean 定义中的 map 元素。
//
// <p>可以通过 “mappings” 属性设置 Bean 名称的映射，其格式为 {@code java.util.Properties} 类接受的格式，如下所示：
// <pre class="code">
// 	/welcome.html=ticketController
// 	/show.html=ticketController
// </pre>
//
// <p>语法为 {@code PATH=HANDLER_BEAN_NAME}。如果路径不以斜杠开头，则会在前面添加一个斜杠。
//
// <p>支持直接匹配（例如，给定 "/test" -> 注册的 "/test”）和 "*" 匹配（例如，给定 "/test" -> 注册的 "/t*"）。
// 有关模式选项的详细信息，请参阅 {@link org.springframework.web.util.pattern.PathPattern} javadoc。
public class SimpleUrlHandlerMapping extends AbstractUrlHandlerMapping {

	private final Map<String, Object> urlMap = new LinkedHashMap<>();


	/**
	 * Create a {@code SimpleUrlHandlerMapping} with default settings.
	 */
	// 创建一个具有默认设置的 {@code SimpleUrlHandlerMapping}。
	public SimpleUrlHandlerMapping() {
	}

	/**
	 * Create a {@code SimpleUrlHandlerMapping} using the supplied URL map.
	 * @param urlMap map with URL paths as keys and handler beans (or handler
	 * bean names) as values
	 * @since 5.2
	 * @see #setUrlMap(Map)
	 */
	// 使用提供的 URL 映射创建 {@code SimpleUrlHandlerMapping}。
	// @param urlMap 以 URL 路径为键，处理程序 bean（或处理程序 bean 名称）为值的映射
	public SimpleUrlHandlerMapping(Map<String, ?> urlMap) {
		setUrlMap(urlMap);
	}

	/**
	 * Create a {@code SimpleUrlHandlerMapping} using the supplied URL map and order.
	 * @param urlMap map with URL paths as keys and handler beans (or handler
	 * bean names) as values
	 * @param order the order value for this {@code SimpleUrlHandlerMapping}
	 * @since 5.2
	 * @see #setUrlMap(Map)
	 * @see #setOrder(int)
	 */
	// 使用提供的 URL 映射和 order 创建一个 {@code SimpleUrlHandlerMapping}。
	// @param urlMap 以 URL 路径为键，处理程序 bean（或处理程序 bean 名称）为值的映射
	// @param order 此 {@code SimpleUrlHandlerMapping} 的顺序值
	public SimpleUrlHandlerMapping(Map<String, ?> urlMap, int order) {
		setUrlMap(urlMap);
		setOrder(order);
	}


	/**
	 * Map URL paths to handler bean names.
	 * This is the typical way of configuring this HandlerMapping.
	 * <p>Supports direct URL matches and Ant-style pattern matches. For syntax
	 * details, see the {@link org.springframework.util.AntPathMatcher} javadoc.
	 * @param mappings properties with URLs as keys and bean names as values
	 * @see #setUrlMap
	 */
	// 将 URL 路径映射到 handler bean 名称。这是配置此 HandlerMapping 的典型方法。
	// <p>支持直接 URL 匹配和 Ant 风格的模式匹配。有关语法详情，请参阅 {@link org.springframework.util.AntPathMatcher} javadoc。</p>
	// @param mappings 属性，以 URL 为键，bean 名称为值
	public void setMappings(Properties mappings) {
		CollectionUtils.mergePropertiesIntoMap(mappings, this.urlMap);
	}

	/**
	 * Set a Map with URL paths as keys and handler beans (or handler bean names)
	 * as values. Convenient for population with bean references.
	 * <p>Supports direct URL matches and Ant-style pattern matches. For syntax
	 * details, see the {@link org.springframework.util.AntPathMatcher} javadoc.
	 * @param urlMap map with URLs as keys and beans as values
	 * @see #setMappings
	 */
	public void setUrlMap(Map<String, ?> urlMap) {
		this.urlMap.putAll(urlMap);
	}

	/**
	 * Allow {@code Map} access to the URL path mappings, with the option to add or
	 * override specific entries.
	 * <p>Useful for specifying entries directly, for example via "urlMap[myKey]".
	 * This is particularly useful for adding or overriding entries in child
	 * bean definitions.
	 */
	public Map<String, ?> getUrlMap() {
		return this.urlMap;
	}


	/**
	 * Calls the {@link #registerHandlers} method in addition to the
	 * superclass's initialization.
	 */
	// 除了调用超类的初始化方法外，还调用 {@link #registerHandlers} 方法。
	@Override
	public void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		registerHandlers(this.urlMap);
	}

	/**
	 * Register all handlers specified in the URL map for the corresponding paths.
	 * @param urlMap a Map with URL paths as keys and handler beans or bean names as values
	 * @throws BeansException if a handler couldn't be registered
	 * @throws IllegalStateException if there is a conflicting handler registered
	 */
	// 注册 URL 映射中指定的所有处理程序及其对应的路径。
	// @param urlMap 一个映射，以 URL 路径为键，处理程序 bean 或 bean 名称为值
	// @throws BeansException 如果无法注册处理程序
	// @throws IllegalStateException 如果已注册冲突的处理程序
	protected void registerHandlers(Map<String, Object> urlMap) throws BeansException {
		if (urlMap.isEmpty()) {
			logger.trace("No patterns in " + formatMappingName());
		}
		else {
			urlMap.forEach((url, handler) -> {
				// Prepend with slash if not already present. --> 译文：若前面未加斜杠，则加上斜杠。
				if (!url.startsWith("/")) {
					url = "/" + url;
				}
				// Remove whitespace from handler bean name. --> 译文：从处理程序 Bean 名称中移除空格。
				if (handler instanceof String handlerName) {
					handler = handlerName.trim();
				}
				registerHandler(url, handler);
			});
			logMappings();
		}
	}

	private void logMappings() {
		if (mappingsLogger.isDebugEnabled()) {
			Map<String, Object> map = new LinkedHashMap<>(getHandlerMap());
			if (getRootHandler() != null) {
				map.put("/", getRootHandler());
			}
			if (getDefaultHandler() != null) {
				map.put("/**", getDefaultHandler());
			}
			mappingsLogger.debug(formatMappingName() + " " + map);
		}
		else if (logger.isDebugEnabled()) {
			List<String> patterns = new ArrayList<>();
			if (getRootHandler() != null) {
				patterns.add("/");
			}
			if (getDefaultHandler() != null) {
				patterns.add("/**");
			}
			patterns.addAll(getHandlerMap().keySet());
			logger.debug("Patterns " + patterns + " in " + formatMappingName());
		}
	}

}
