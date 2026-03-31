package org.springframework.web.servlet._mine.web15_handler_exception_resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.Set;

/**
 * HandlerExceptionResolver
 *
 * @see org.springframework.web.servlet.HandlerExceptionResolver
 */
public class HER01_HandlerExceptionResolver {

	/**
	 * @see org.springframework.web.servlet.HandlerExceptionResolver#resolveException(HttpServletRequest, HttpServletResponse, Object, Exception)
	 */
	public static void main(String[] args) {
		Object handler = new Object();

		DefaultHandlerExceptionResolver resolver = new DefaultHandlerExceptionResolver();
		resolver.setMappedHandlers(Set.of(handler));

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		HttpMessageNotWritableException exception = new HttpMessageNotWritableException("HttpMessageNotWritableException");
		// 检查该解析器是否应该应用
		// （即，提供的 handler 是否与任何配置的 {@linkplain #setMappedHandlers handler} 或 {@linkplain #setMappedHandlerClasses handler类} 匹配），
		// 然后委托给 AbstractHandlerExceptionResolver#doResolveException() 模板方法。
		ModelAndView modelAndView = resolver.resolveException(request, response, handler, exception);

		System.out.println(modelAndView);
	}
}
/*
********************************* Class API Docs *********************************
接口由对象实现，用于解析 handler 映射或执行期间抛出的异常，通常情况下是错误视图。实现者通常在应用程序上下文中注册为 Bean。

<p>错误视图类似于 JSP 错误页面，但可以用于任何类型的异常，包括任何已检查异常，并可能针对特定 handler 进行细粒度的映射。

********************************* Class Definition *********************************
public interface HandlerExceptionResolver {
	@Nullable
	ModelAndView resolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex);
}
**/
