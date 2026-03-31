package org.springframework.web.servlet._mine.web14_request_to_view_name_translator;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * RequestToViewNameTranslator
 *
 * @see org.springframework.web.servlet.RequestToViewNameTranslator
 * @see org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator
 *
 *
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.DispatcherServlet#applyDefaultViewName(jakarta.servlet.http.HttpServletRequest, org.springframework.web.servlet.ModelAndView)
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultViewName(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.RequestToViewNameTranslator#getViewName(jakarta.servlet.http.HttpServletRequest)
 */
public class RTVNT01_RequestToViewNameTranslator {

	/**
	 * @see org.springframework.web.servlet.RequestToViewNameTranslator#getViewName(HttpServletRequest)
	 */
	public static void main(String[] args) throws Exception {
		RequestToViewNameTranslator translator = new DefaultRequestToViewNameTranslator();

		// RequestToViewNameTranslator#getViewName(HttpServletRequest)
		MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "http://localhost:8080/user/getAll");
		// see ServletRequestPathUtils#getCachedPath(ServletRequest)
		request.setAttribute(UrlPathHelper.PATH_ATTRIBUTE, "index");
		request.setAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE, "index");

		// 将给定的 HttpServletRequest 转换为视图名称。
		// @param request 传入的 HttpServletRequest 提供要从中解析视图名称的上下文
		// @return 视图名称，如果没有找到默认值，则返回 null
		// @throws Exception，如果视图名称转换失败
		String viewName = translator.getViewName(request);
		System.out.println(viewName);
	}
}
/*
********************************* Class API Docs *********************************
当没有明确提供视图名称时，用于将传入的 jakarta.servlet.http.HttpServletRequest 转换为逻辑视图名称的策略接口。

********************************* Class Definition *********************************
public interface RequestToViewNameTranslator {
	String getViewName(HttpServletRequest request) throws Exception;
}
**/
