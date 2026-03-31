package org.springframework.web.servlet._mine.web13_model_and_view.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.util.Locale;
import java.util.Map;

/**
 * View
 *
 * @see org.springframework.web.servlet.View
 */
public class V01_View {

	/**
	 * @see View#getContentType()
	 * @see View#render(Map, HttpServletRequest, HttpServletResponse)
	 *
	 * @see View#RESPONSE_STATUS_ATTRIBUTE
	 * @see View#PATH_VARIABLES
	 * @see View#SELECTED_CONTENT_TYPE
	 */
	public static void main(String[] args) throws Exception {
		UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".jsp");
		viewResolver.setViewClass(InternalResourceView.class);
		viewResolver.setApplicationContext(new StaticApplicationContext());
		View view = viewResolver.resolveViewName("index", Locale.getDefault());

		// View#getContentType()
		// 如果已预先确定，则返回视图的内容类型。
		// <p>可用于预先检查视图的内容类型，即在实际渲染尝试之前。
		String contentType = view.getContentType();

		// View#render(Map, HttpServletRequest, HttpServletResponse)
		// 根据指定的模型渲染视图。
		// <p>第一步是准备请求：在 JSP 中，这意味着将模型对象设置为请求属性。第二步是实际渲染视图，例如通过 RequestDispatcher 包含 JSP。
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelMap modelMap = new ModelMap();
		view.render(modelMap, request, response);

		// Print
		System.out.println(contentType);
		System.out.println(response.getContentAsString().isEmpty());

		// 包含响应状态代码的 {@link HttpServletRequest} 属性的名称。
		// <p>注意：此属性不需要得到所有 View 实现的支持。
		System.out.println(View.RESPONSE_STATUS_ATTRIBUTE);
		// 包含路径变量映射的 {@link HttpServletRequest} 属性的名称。
		// 该映射包含基于字符串的 URI 模板变量名作为键，以及其对应的基于对象的值（从 URL 的各个部分提取并进行类型转换）。
		// <p>注意：并非所有 View 实现都要求支持此属性。
		System.out.println(View.PATH_VARIABLES);
		// 内容协商期间选择的 {@link org.springframework.http.MediaType}，可能比视图配置的更具体。
		// 例如：“application/vnd.example-v1+xml” vs “application/*+xml”。
		System.out.println(View.SELECTED_CONTENT_TYPE);
	}

}
/*
********************************* Class API Docs *********************************
MVC view 用于 Web 交互。实现负责渲染内容并公开模型。单个 view 公开多个模型属性。

<p>此类及其相关的 MVC 方法在 Rod Johnson 所著的《Expert One-On-One J2EE Design and Development》（Wrox，2002 年）第 12 章中进行了讨论。

<p>view 的实现可能差异很大。一种显而易见的实现是基于 JSP 的。其他实现可能基于 XSLT，或者使用 HTML 生成库。此接口旨在避免限制可能的实现范围。

<p>views 应该是 bean。它们很可能由 ViewResolver 实例化为 bean。由于此接口是无状态的，因此 view 实现应该是线程安全的。

********************************* Class Definition *********************************
public interface View {
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";

	@Nullable
	default String getContentType() {
		return null;
	}
	void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception;
	}
}
**/
