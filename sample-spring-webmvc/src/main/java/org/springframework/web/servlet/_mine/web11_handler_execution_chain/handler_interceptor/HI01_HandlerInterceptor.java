package org.springframework.web.servlet._mine.web11_handler_execution_chain.handler_interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * HandlerInterceptor
 *
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public class HI01_HandlerInterceptor {

	/**
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView)
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)
	 */
	public static void main(String[] args) throws Exception {
		// Params
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MyHandler handler = new MyHandler();

		// Methods
		MyHandlerInterceptor interceptor = new MyHandlerInterceptor();

		//  Handler 执行前的拦截点。
		//  在 HandlerMapping 确定合适的 Handler 对象之后，但在 HandlerAdapter 调用 Handler 之前调用。
		// <p>DispatcherServlet 处理执行链中的 Handler ，该执行链由任意数量的拦截器组成， Handler 本身位于末尾。
		// 使用此方法，每个拦截器都可以决定中止执行链，通常是发送 HTTP 错误或编写自定义响应。
		// <p><strong>注意：</strong>异步请求处理有特殊注意事项。更多详情请参阅 {@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
		// <p>默认实现返回 {@code true}。
		boolean preHandle = interceptor.preHandle(request, response, handler);

		// 成功执行 Handler 后的拦截点。
		// 在 HandlerAdapter 实际调用 Handler 之后、DispatcherServlet 渲染视图之前调用。可以通过给定的 ModelAndView 向视图公开其他模型对象。
		// <p>DispatcherServlet 处理执行链中的 Handler ，该执行链由任意数量的拦截器组成， Handler 本身位于链的末尾。
		// 使用此方法，每个拦截器都可以对执行进行后处理，并按执行链的逆序应用。
		// <p><strong>注意：</strong>异步请求处理需要特别注意。更多详情请参阅
		// {@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
		// <p>默认实现为空。
		ModelAndView modelAndView = new ModelAndView();
		interceptor.postHandle(request, response, handler, modelAndView);

		// 请求处理完成后的回调，即在渲染视图之后。
		// 将在 Handler 执行的任何结果上调用，从而允许正确的资源清理。
		// <p>注意：仅当此拦截器的 {@code preHandle} 方法成功完成并返回 {@code true} 时才会调用！
		// <p>与 {@code postHandle} 方法一样，该方法将按相反的顺序在链中的每个拦截器上调用，因此第一个拦截器将是最后调用的。
		// <p><strong>注意：</strong>异步请求处理有特殊注意事项。有关更多详细信息，请参阅 {@link org.springframework.web.servlet.AsyncHandlerInterceptor}。
		// <p>默认实现为空。
		interceptor.afterCompletion(request, response, handler, null);
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

	/**
	 * MyHandler impl {@link org.springframework.web.servlet.HandlerInterceptor}
	 *
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 */
	static class MyHandlerInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(@NonNull HttpServletRequest request,
								 @NonNull HttpServletResponse response,
								 @NonNull Object handler) throws Exception {
			System.out.println("invoke MyHandlerInterceptor#preHandle()");
			return HandlerInterceptor.super.preHandle(request, response, handler);
		}

		@Override
		public void postHandle(@NonNull HttpServletRequest request,
							   @NonNull HttpServletResponse response,
							   @NonNull Object handler, ModelAndView modelAndView) throws Exception {
			System.out.println("invoke MyHandlerInterceptor#postHandle()");
		}

		@Override
		public void afterCompletion(@NonNull HttpServletRequest request,
									@NonNull HttpServletResponse response,
									@NonNull Object handler, Exception ex) throws Exception {
			System.out.println("invoke MyHandlerInterceptor#afterCompletion()");
		}
	}
}
/*
********************************* Class API Docs *********************************
工作流接口允许自定义 Handler 执行链。
应用程序可以为特定 Handler 组注册任意数量的现有或自定义拦截器，从而添加常见的预处理行为，而无需修改每个 Handler 的实现。

<p>HandlerInterceptor 在相应的 HandlerAdapter 触发 Handler 本身的执行之前被调用。
此机制可用于大量预处理方面，或常见的 Handler 行为，例如语言环境或主题更改。其主要目的是允许分解重复的 Handler 代码。

<p>在异步处理场景中， Handler 可以在单独的线程中执行，而主线程退出时不会渲染或调用 {@code postHandle} 和 {@code afterCompletion} 回调。
当并发 Handler 执行完成后，请求将被分派回来以继续渲染模型，并且此契约的所有方法都会再次被调用。
更多选项和详细信息，请参阅 {@code org.springframework.web.servlet.AsyncHandlerInterceptor}

<p>通常，每个 HandlerMapping bean 都会定义一个拦截器链，并共享其粒度。
为了能够将某个拦截器链应用于一组 Handler ，需要通过一个 HandlerMapping bean 映射所需的 Handler 。
拦截器本身在应用程序上下文中定义为 bean，由映射 bean 定义通过其“interceptors”属性（在 XML 中为 <ref> 的 <list>）引用。

<p>HandlerInterceptor 基本类似于 Servlet 过滤器，但与后者不同的是，它只允许自定义预处理（可以选择禁止 Handler 本身的执行）和自定义后处理。
过滤器功能更强大，例如，它们允许交换沿链传递的请求和响应对象。
请注意，过滤器在 web.xml 中进行配置，而 HandlerInterceptor 则在应用程序上下文中进行配置。

<p>作为基本准则，细粒度的处理器相关的预处理任务是 HandlerInterceptor 实现的候选，尤其是分离出来的通用处理器代码和授权检查。
另一方面，过滤器非常适合处理请求内容和视图内容，例如多部分表单和 GZIP 压缩。这通常体现在需要将过滤器映射到特定内容类型（例如图像）或所有请求时。

<p><strong>注意：</strong>拦截器并不适合用作安全层，因为它可能与带注解的控制器路径匹配不匹配。
通常，我们建议使用 Spring Security，或者将类似的方法集成到 Servlet 过滤器链中，并尽早应用。

********************************* Class Definition *********************************
public interface HandlerInterceptor {
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return true;
	}
	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
							@Nullable ModelAndView modelAndView) throws Exception {
	}
	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
								 @Nullable Exception ex) throws Exception {
	}
}
**/