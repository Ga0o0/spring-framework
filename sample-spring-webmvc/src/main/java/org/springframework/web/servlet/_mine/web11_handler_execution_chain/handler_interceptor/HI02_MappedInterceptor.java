package org.springframework.web.servlet._mine.web11_handler_execution_chain.handler_interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;

/**
 * MappedInterceptor
 *
 * @see org.springframework.web.servlet.handler.MappedInterceptor
 */
public class HI02_MappedInterceptor {

	/**
	 * @see org.springframework.web.servlet.handler.MappedInterceptor#MappedInterceptor(String[], String[], HandlerInterceptor, PathPatternParser)
	 * @see org.springframework.web.servlet.handler.MappedInterceptor#MappedInterceptor(String[], HandlerInterceptor)
	 * @see org.springframework.web.servlet.handler.MappedInterceptor#MappedInterceptor(String[], String[], HandlerInterceptor)
	 * @see org.springframework.web.servlet.handler.MappedInterceptor#MappedInterceptor(String[], WebRequestInterceptor)
	 * @see org.springframework.web.servlet.handler.MappedInterceptor#MappedInterceptor(String[], String[], WebRequestInterceptor)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Params
			String[] includePatterns = {"/a", "/b"};
			String[] excludePatterns = {"/c", "/d"};
			HandlerInterceptor interceptor = new MyHandlerInterceptor();
			PathPatternParser parser = new PathPatternParser();
			WebRequestInterceptor webRequestInterceptor = new MyWebRequestInterceptor();

			// Constructors
			MappedInterceptor mappedInterceptor1 = new MappedInterceptor(includePatterns, excludePatterns, interceptor, parser);
			MappedInterceptor mappedInterceptor2 = new MappedInterceptor(includePatterns, interceptor);
			MappedInterceptor mappedInterceptor3 = new MappedInterceptor(includePatterns, excludePatterns, interceptor);
			MappedInterceptor mappedInterceptor4 = new MappedInterceptor(includePatterns, webRequestInterceptor);
			MappedInterceptor mappedInterceptor5 = new MappedInterceptor(includePatterns, excludePatterns, webRequestInterceptor);

			// Print
			System.out.println(mappedInterceptor1);
			System.out.println(mappedInterceptor2);
			System.out.println(mappedInterceptor3);
			System.out.println(mappedInterceptor4);
			System.out.println(mappedInterceptor5);
		}
	}

	/**
	 * @see MappedInterceptor#getIncludePathPatterns()
	 * @see MappedInterceptor#getExcludePathPatterns()
	 * @see MappedInterceptor#getInterceptor()
	 * @see MappedInterceptor#setPathMatcher(PathMatcher)
	 * @see MappedInterceptor#getPathMatcher()
	 * @see MappedInterceptor#matches(HttpServletRequest)
	 */
	static class Methods {
		public static void main(String[] args) {
			// Params
			String[] includePatterns = {"/a", "/b"};
			String[] excludePatterns = {"/c", "/d"};
			HandlerInterceptor interceptor = new MyHandlerInterceptor();
			PathPatternParser parser = new PathPatternParser();

			// Constructors
			MappedInterceptor mappedInterceptor = new MappedInterceptor(includePatterns, excludePatterns, interceptor, parser);

			// Methods
			String[] includePathPatterns = mappedInterceptor.getIncludePathPatterns();
			String[] excludePathPatterns = mappedInterceptor.getExcludePathPatterns();
			HandlerInterceptor interceptor1 = mappedInterceptor.getInterceptor();
			PathMatcher pathMatcher = mappedInterceptor.getPathMatcher();
			mappedInterceptor.setPathMatcher(pathMatcher);

			// MappedInterceptor#matches(HttpServletRequest)
			// 检查此拦截器是否已映射到请求。
			MockHttpServletRequest request1 = new MockHttpServletRequest(null, "http://localhost:8080/a");
			request1.setAttribute(UrlPathHelper.PATH_ATTRIBUTE, "/a");
			boolean matches1 = mappedInterceptor.matches(request1);

			MockHttpServletRequest request2 = new MockHttpServletRequest(null, "http://localhost:8080/c");
			request2.setAttribute(UrlPathHelper.PATH_ATTRIBUTE, "/c");
			boolean matches2 = mappedInterceptor.matches(request2);

			// Print
			System.out.println("includePathPatterns: " 	+ Arrays.toString(includePathPatterns));
			System.out.println("excludePathPatterns: " 	+ Arrays.toString(excludePathPatterns));
			System.out.println("interceptor1: " 		+ interceptor1);
			System.out.println("pathMatcher: " 			+ pathMatcher);
			System.out.println("matches1: " 			+ matches1);
			System.out.println("matches2: " 			+ matches2);
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

	/**
	 * MyWebRequestInterceptor impl {@link org.springframework.web.context.request.WebRequestInterceptor}
	 *
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 */
	static class MyWebRequestInterceptor implements WebRequestInterceptor {

		@Override
		public void preHandle(@NonNull WebRequest request) throws Exception {
			System.out.println("invoke MyWebRequestInterceptor#preHandle()");
		}

		@Override
		public void postHandle(@NonNull WebRequest request, ModelMap model) throws Exception {
			System.out.println("invoke MyWebRequestInterceptor#postHandle()");
		}

		@Override
		public void afterCompletion(@NonNull WebRequest request, Exception ex) throws Exception {
			System.out.println("invoke MyWebRequestInterceptor#afterCompletion()");
		}
	}

}
/*
********************************* Class API Docs *********************************
封装一个 {@link HandlerInterceptor}，并使用 URL 模式来确定它是否适用于给定的请求。

<p>模式匹配可以使用 {@link PathMatcher} 或已解析的 {@link PathPattern} 来完成。
语法基本相同，但后者更适合 Web 使用，效率更高。选择哪种方式取决于是否存在已解析的 {@linkplain UrlPathHelper#resolveAndCacheLookupPath} {@code String}
查找路径或已解析的 {@linkplain ServletRequestPathUtils#parseAndCache parsed} {@code RequestPath}，而这又取决于与当前请求匹配的 {@link HandlerMapping}。

<p>{@code MappedInterceptor} 由 {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping AbstractHandlerMethodMapping} 的子类支持，
这些子类可以检测 {@code MappedInterceptor} 类型的 bean，并检查直接注册到该 bean 的拦截器是否为这种类型。

********************************* Class Definition *********************************
public final class MappedInterceptor implements HandlerInterceptor {
	private static final PathMatcher defaultPathMatcher = new AntPathMatcher();
	private final PatternAdapter[] includePatterns;
	private final PatternAdapter[] excludePatterns;
	private PathMatcher pathMatcher = defaultPathMatcher;
	private final HandlerInterceptor interceptor;
	// ...
}
**/