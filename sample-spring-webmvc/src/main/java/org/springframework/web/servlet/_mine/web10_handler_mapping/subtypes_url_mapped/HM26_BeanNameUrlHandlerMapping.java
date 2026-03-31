package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_url_mapped;

import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

/**
 * BeanNameUrlHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 */
public class HM26_BeanNameUrlHandlerMapping {
	public static void main(String[] args) {
		BeanNameUrlHandlerMapping handlerMapping = new BeanNameUrlHandlerMapping();
		System.out.println(handlerMapping);
	}
}
/*
********************************* Class API Docs *********************************
实现 org.springframework.web.servlet.HandlerMapping 接口，
将 URL 映射到名称以斜杠 ("/") 开头的 Bean，类似于 Struts 将 URL 映射到操作名称的方式。

<p>这是 org.springframework.web.servlet.DispatcherServlet 以及
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping 使用的默认实现。
或者，SimpleUrlHandlerMapping 允许以声明方式自定义 handler 映射。

<p>该映射是从 URL 到 Bean 名称的映射。因此，传入的 URL “/foo” 将映射到名为 “/foo” 的 handler，
如果多个 Bean 映射到单个 handler ，则将映射到 “/foo /foo2” 。

<p>支持直接匹配（例如，给定 "/test" -> 注册的 "/test”）和 "*" 匹配（例如，给定 "/test" -> 注册的 "/t*"）。
有关模式选项的详细信息，请参阅 {@link org.springframework.web.util.pattern.PathPattern} javadoc。

********************************* Class Definition *********************************
public class BeanNameUrlHandlerMapping extends AbstractDetectingUrlHandlerMapping { ... }
**/