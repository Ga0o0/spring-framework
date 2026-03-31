package org.springframework.web.servlet._mine.web11_handler_execution_chain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.Arrays;
import java.util.List;

/**
 * HandlerExecutionChain
 *
 * @see org.springframework.web.servlet.HandlerExecutionChain
 */
public class H01_HandlerExecutionChain {

	static final MyHandler handler = new MyHandler();
	static final HandlerInterceptor interceptor = new MyHandlerInterceptor();
	static final HandlerInterceptor[] interceptors = { interceptor };
	static final List<HandlerInterceptor> interceptorList = List.of(interceptor);

	/**
	 * @see org.springframework.web.servlet.HandlerExecutionChain#HandlerExecutionChain(Object)
	 * @see org.springframework.web.servlet.HandlerExecutionChain#HandlerExecutionChain(Object, org.springframework.web.servlet.HandlerInterceptor...)
	 * @see org.springframework.web.servlet.HandlerExecutionChain#HandlerExecutionChain(Object, java.util.List)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Constructors
			HandlerExecutionChain executionChain1 = new HandlerExecutionChain(handler);
			HandlerExecutionChain executionChain2 = new HandlerExecutionChain(handler, interceptors);
			HandlerExecutionChain executionChain3 = new HandlerExecutionChain(handler, interceptorList);

			// Print
			System.out.println(executionChain1);
			System.out.println(executionChain2);
			System.out.println(executionChain3);
		}
	}

	/**
	 * @see HandlerExecutionChain#getHandler()
	 * @see HandlerExecutionChain#addInterceptor(HandlerInterceptor)
	 * @see HandlerExecutionChain#addInterceptor(int, HandlerInterceptor)
	 * @see HandlerExecutionChain#addInterceptors(HandlerInterceptor...)
	 * @see HandlerExecutionChain#getInterceptors()
	 * @see HandlerExecutionChain#getInterceptorList()
	 */
	static class Methods {
		public static void main(String[] args) {
			HandlerExecutionChain executionChain = new HandlerExecutionChain(handler);

			// Methods
			Object myHandler = executionChain.getHandler();
			executionChain.addInterceptor(interceptor);
			executionChain.addInterceptor(0, interceptor);
			executionChain.addInterceptors(interceptors);
			HandlerInterceptor[] getInterceptors = executionChain.getInterceptors();
			List<HandlerInterceptor> getInterceptorList = executionChain.getInterceptorList();

			// Print
			System.out.println(myHandler);
			System.out.println(Arrays.toString(getInterceptors));
			System.out.println(getInterceptorList);
		}
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
Handler 执行链，由 Handler 对象和任何 Handler 拦截器组成。由 HandlerMapping 的 HandlerMapping#getHandler(...) 方法返回。

********************************* Class Definition *********************************
public class HandlerExecutionChain {
	// ...
	private final Object handler;
	private final List<HandlerInterceptor> interceptorList = new ArrayList<>();
	private int interceptorIndex = -1;
	// ...
}
**/
