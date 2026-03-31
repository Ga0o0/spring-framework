package org.springframework.web.servlet._mine.web14_request_to_view_name_translator;

import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * DefaultRequestToViewNameTranslator
 *
 * @see org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator
 * @see RTVNT01_RequestToViewNameTranslator
 */
public class RTVNT02_DefaultRequestToViewNameTranslator {

	/**
	 * @see DefaultRequestToViewNameTranslator#setPrefix(String)
	 * @see DefaultRequestToViewNameTranslator#setSuffix(String)
	 * @see DefaultRequestToViewNameTranslator#setSeparator(String)
	 * @see DefaultRequestToViewNameTranslator#setStripLeadingSlash(boolean)
	 * @see DefaultRequestToViewNameTranslator#setStripTrailingSlash(boolean)
	 * @see DefaultRequestToViewNameTranslator#setStripExtension(boolean)
	 */
	public static void main(String[] args) throws Exception {
		DefaultRequestToViewNameTranslator translator = new DefaultRequestToViewNameTranslator();

		// ---------------------- DefaultRequestToViewNameTranslator -----------------------------------
		// 设置要添加到生成的视图名称前的前缀。
		translator.setPrefix("/WEB-INF/jsp/");
		// 设置要附加到生成的视图名称的后缀。
		translator.setSuffix(".jsp");
		// 设置视图名称中分隔符 / 的替换值。默认情况下，分隔符仍为 /。
		translator.setSeparator("/");
		// 设置生成视图名称时是否从 URI 中删除前导斜杠。默认值为 true。
		translator.setStripLeadingSlash(true);
		// 设置生成视图名称时是否从 URI 中删除尾部斜杠。默认值为 true。
		translator.setStripTrailingSlash(true);
		// 设置生成视图名称时是否从 URI 中去除文件扩展名。默认值为 true。
		translator.setStripExtension(true);


		// ---------------------- RequestToViewNameTranslator -----------------------------------
		// RequestToViewNameTranslator#getViewName(HttpServletRequest)
		MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/index");
		// see ServletRequestPathUtils#getCachedPath(ServletRequest)
// 		request.setAttribute(UrlPathHelper.PATH_ATTRIBUTE, "path");
//		UrlPathHelper.defaultInstance.resolveAndCacheLookupPath(request);

//		request.setAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE, "path");
		ServletRequestPathUtils.parseAndCache(request);

		// 将给定的 HttpServletRequest 转换为视图名称。
		// @param request 传入的 HttpServletRequest 提供要从中解析视图名称的上下文
		// @return 视图名称，如果没有找到默认值，则返回 null
		// @throws Exception，如果视图名称转换失败
		String viewName = translator.getViewName(request);
		System.out.println(viewName); // /WEB-INF/jsp/path.jsp
	}

}
/*
********************************* Class API Docs *********************************
RequestToViewNameTranslator 只是将传入请求的 URI 转换为视图名称。

<p>可以在 org.springframework.web.servlet.DispatcherServlet 上下文中明确定义为 viewNameTranslator bean。
否则，将使用简单的默认实例。<p>默认转换只是去除 URI 的前导斜杠和尾随斜杠以及文件扩展名，并将结果作为视图名称返回，
并根据需要添加配置的前缀（DefaultRequestToViewNameTranslator#setPrefix()）和后缀（DefaultRequestToViewNameTranslator#setSuffix()）。

<p>可以分别使用 DefaultRequestToViewNameTranslator#setStripLeadingSlash() 和
DefaultRequestToViewNameTranslator#setStripExtension() 属性禁用去除前导斜杠和文件扩展名的功能。

<p>以下是一些请求到视图名称转换的示例。
<ul>
<li>{@code http:localhost:8080/gamecast/display.html}				>> display
<li>{@code http:localhost:8080/gamecast/displayShoppingCart.html} 	>> displayShoppingCart
<li>{@code http:localhost:8080/gamecast/admin/index.html} 			>> admin/index
</ul>

********************************* Class Definition *********************************
public class DefaultRequestToViewNameTranslator implements RequestToViewNameTranslator {
	private static final String SLASH = "/";
	private String prefix = "";
	private String suffix = "";
	private String separator = SLASH;
	private boolean stripLeadingSlash = true;
	private boolean stripTrailingSlash = true;
	private boolean stripExtension = true;
	// ...
}
**/
