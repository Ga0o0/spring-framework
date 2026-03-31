package org.springframework.web.servlet._mine.web17_view_resolver;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.util.Locale;

/**
 * ViewResolver
 *
 * @see org.springframework.web.servlet.ViewResolver
 */
public class VR01_ViewResolver {

	/**
	 * @see org.springframework.web.servlet.ViewResolver#resolveViewName(String, Locale)
	 */
	public static void main(String[] args) throws Exception {
		UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".jsp");
		viewResolver.setViewClass(InternalResourceView.class);
		viewResolver.setApplicationContext(new StaticApplicationContext());

		// 按名称解析给定的视图。
		// <p>注意：为允许 ViewResolver 链式调用，如果未定义具有给定名称的视图，ViewResolver 应返回 {@code null}。
		// 但这不是必需的：某些 ViewResolver 将始终尝试构建具有给定名称的视图对象，无法返回 {@code null}（而是在视图创建失败时抛出异常）。
		View view = viewResolver.resolveViewName("index", Locale.getDefault());
		System.out.println(view);
	}
}
/*
********************************* Class API Docs *********************************
接口由能够按名称解析视图的对象实现。

<p>视图状态在应用程序运行期间不会发生变化，因此实现可以自由缓存视图。

<p>鼓励实现支持国际化，即本地化视图解析。

********************************* Class Definition *********************************
public interface ViewResolver {
	View resolveViewName(String viewName, Locale locale) throws Exception;
}
**/