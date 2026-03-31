package org.springframework.web.servlet._mine.web10_handler_mapping;

import org.springframework.util.PathMatcher;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Map;

/**
 * AbstractHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 */
public class HM11_AbstractHandlerMapping {

	/**
	 * @see AbstractHandlerMapping#setDefaultHandler(Object)
	 * @see AbstractHandlerMapping#getDefaultHandler()
	 * @see AbstractHandlerMapping#setPatternParser(PathPatternParser)
	 * @see AbstractHandlerMapping#getPatternParser()
	 * @see AbstractHandlerMapping#setUrlPathHelper(UrlPathHelper)
	 * @see AbstractHandlerMapping#getUrlPathHelper()
	 * @see AbstractHandlerMapping#setPathMatcher(PathMatcher)
	 * @see AbstractHandlerMapping#getPathMatcher()
	 * @see AbstractHandlerMapping#setInterceptors(Object...)
	 * @see AbstractHandlerMapping#getAdaptedInterceptors()
	 * @see AbstractHandlerMapping#setCorsConfigurations(Map)
	 * @see AbstractHandlerMapping#setCorsConfigurationSource(CorsConfigurationSource)
	 * @see AbstractHandlerMapping#getCorsConfigurationSource()
	 * @see AbstractHandlerMapping#setCorsProcessor(CorsProcessor)
	 * @see AbstractHandlerMapping#getCorsProcessor()
	 * @see AbstractHandlerMapping#setOrder(int)
	 */
	public static void main(String[] args) {

	}
}
/*
********************************* Class API Docs *********************************
// {@link org.springframework.web.servlet.HandlerMapping} 实现的抽象基类。
// 支持排序、默认 handler 和 handler 拦截器，包括按路径模式映射的 handler 拦截器。
//
// <p>注意：此基类<i>不</i>支持公开 {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}。
// 此属性的支持取决于具体的子类，通常基于请求 URL 映射。

********************************* Class Definition *********************************
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
		implements HandlerMapping, Ordered, BeanNameAware {
	static final String SUPPRESS_LOGGING_ATTRIBUTE = AbstractHandlerMapping.class.getName() + ".SUPPRESS_LOGGING";
	protected final Log mappingsLogger = LogDelegateFactory.getHiddenLog(HandlerMapping.class.getName() + ".Mappings");

	private Object defaultHandler;
	private PathPatternParser patternParser = new PathPatternParser();
	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	private PathMatcher pathMatcher = new AntPathMatcher();
	private final List<Object> interceptors = new ArrayList<>();
	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();
	private CorsConfigurationSource corsConfigurationSource;
	private CorsProcessor corsProcessor = new DefaultCorsProcessor();
	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered
	private String beanName;
	// ...
}
**/