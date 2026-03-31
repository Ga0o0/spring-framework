package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped;

import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * RequestMappingHandlerMapping
 *
 * <p>从 {@link Controller @Controller} 类中的类级和方法级 {@link RequestMapping @RequestMapping}
 * 和 {@link HttpExchange @HttpExchange} 注释创建 {@link RequestMappingInfo} 实例。
 *
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 */
public class HM35_RequestMappingHandlerMapping {
	/*
	public interface HandlerMapping { ... }

	public interface MatchableHandlerMapping extends HandlerMapping { ... }

	public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport
			implements HandlerMapping, Ordered, BeanNameAware { ... }

	public abstract class AbstractHandlerMethodMapping<T> extends AbstractHandlerMapping implements InitializingBean { ... }

	public abstract class RequestMappingInfoHandlerMapping extends AbstractHandlerMethodMapping<RequestMappingInfo> { ... }

	public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
			implements MatchableHandlerMapping, EmbeddedValueResolverAware { ... }
	**/

	/**
	 * @see RequestMappingHandlerMapping#RequestMappingInfoHandlerMapping()
	 */
	static class Constructors {
		public static void main(String[] args) {
			RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
			System.out.println(handlerMapping);
		}
	}

	/**
	 * @see RequestMappingHandlerMapping#setPathPrefixes(Map)
	 * @see RequestMappingHandlerMapping#getPathPrefixes()
	 * @see RequestMappingHandlerMapping#setContentNegotiationManager(ContentNegotiationManager)
	 * @see RequestMappingHandlerMapping#getContentNegotiationManager()
	 * @see RequestMappingHandlerMapping#useTrailingSlashMatch()
	 * @see RequestMappingHandlerMapping#getBuilderConfiguration()
	 */
	static class Methods {
		public static void main(String[] args) {
			RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();

			// Params
			Map<String, Predicate<Class<?>>> prefixes = new HashMap<>();
			ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

			// Methods
			// pathPrefixes - 配置路径前缀以应用于 controller 方法
			handlerMapping.setPathPrefixes(prefixes);
			Map<String, Predicate<Class<?>>> pathPrefixes = handlerMapping.getPathPrefixes();

			// contentNegotiationManager - 设置用于确定请求媒体类型的 ContentNegotiationManager
			handlerMapping.setContentNegotiationManager(contentNegotiationManager);
			ContentNegotiationManager negotiationManager = handlerMapping.getContentNegotiationManager();

			// 是否匹配 URL，无论 URL 末尾是否存在斜杠
			boolean useTrailingSlashMatch = handlerMapping.useTrailingSlashMatch();
			// 获取反映当前 HandlerMapping 内部配置的 RequestMappingInfo.BuilderConfiguration
			RequestMappingInfo.BuilderConfiguration builderConfiguration = handlerMapping.getBuilderConfiguration();

			// Print
			System.out.println("pathPrefixes: " + pathPrefixes);
			System.out.println("negotiationManager: " + negotiationManager);
			System.out.println("useTrailingSlashMatch: " + useTrailingSlashMatch);
			System.out.println("builderConfiguration: " + builderConfiguration);
		}
	}
}
/*
********************************* Class API Docs *********************************
HandlerMapping 可以实现的附加接口，用于公开与其内部请求匹配配置和实现一致的请求匹配 API。

********************************* Class Definition *********************************
public interface MatchableHandlerMapping extends HandlerMapping {
	default PathPatternParser getPatternParser() {
		return null;
	}
	RequestMatchResult match(HttpServletRequest request, String pattern);
}
**/
