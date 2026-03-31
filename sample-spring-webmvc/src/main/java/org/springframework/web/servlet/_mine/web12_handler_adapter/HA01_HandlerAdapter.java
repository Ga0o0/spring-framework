package org.springframework.web.servlet._mine.web12_handler_adapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;

/**
 * HandlerAdapter
 *
 * @see org.springframework.web.servlet.HandlerAdapter
 */
public class HA01_HandlerAdapter {

	/**
	 * @see org.springframework.web.servlet.HandlerAdapter#supports(Object)
	 * @see org.springframework.web.servlet.HandlerAdapter#handle(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.HandlerAdapter#getLastModified(HttpServletRequest, Object)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// SimpleControllerHandlerAdapter
		SimpleControllerHandlerAdapter handlerAdapter = new SimpleControllerHandlerAdapter();

		// 1. HandlerAdapter#supports(Object)
		boolean supports1 = handlerAdapter.supports(handler);
		boolean supports2 = handlerAdapter.supports(new Object());
		System.out.println("SimpleControllerHandlerAdapter supports MyController: " 	+ supports1);
		System.out.println("SimpleControllerHandlerAdapter supports Object: " 			+ supports2);

		// 2. HandlerAdapter#handle(...)
		ModelAndView mv = handlerAdapter.handle(request, response, handler);
		System.out.println("SimpleControllerHandlerAdapter handle MyHandler: " 			+ mv);

		// 3. HandlerAdapter#getLastModified(HttpServletRequest, Object)
		long lastModified = handlerAdapter.getLastModified(request, handler);
		System.out.println("SimpleControllerHandlerAdapter lastModified: " 				+ lastModified);
	}

	/**
	 * MyHandler impl {@link org.springframework.web.servlet.mvc.Controller}
	 *
	 * @see org.springframework.web.servlet.mvc.Controller
	 */
	static class MyHandler implements Controller {
		@Override
		public ModelAndView handleRequest(@NonNull HttpServletRequest request,
										  @NonNull HttpServletResponse response) throws Exception {
			return new ModelAndView("index");
		}
	}
}
/*
********************************* Class API Docs *********************************
MVC 框架 SPI，允许对核心 MVC 工作流进行参数化。

<p>每个 handler 类型都必须实现此接口才能处理请求。此接口用于允许 {@link DispatcherServlet} 无限扩展。
{@code DispatcherServlet} 通过此接口访问所有已安装的 handler ，这意味着它不包含任何特定于 handler 类型的代码。

<p>请注意， handler 可以是 {@code Object} 类型。这是为了让其他框架的 handler 无需自定义代码即可与此框架集成，
并允许使用不遵循任何特定 Java 接口的注解驱动的 handler 对象。

<p>此接口不适用于应用程序开发人员。它适用于想要开发自己的 Web 工作流的 handler 。

<p>注意：{@code HandlerAdapter} 实现者可以实现 {@link org.springframework.core.Ordered} 接口，
以便能够指定由 {@code DispatcherServlet} 应用的排序顺序（以及优先级）。非有序实例被视为最低优先级。

********************************* Class Definition *********************************
public interface HandlerAdapter {
	boolean supports(Object handler);
	@Nullable ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
	@Deprecated long getLastModified(HttpServletRequest request, Object handler);
}
**/
