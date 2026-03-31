/**
 * ViewResolver
 *
 * @see org.springframework.web.servlet.ViewResolver
 */
package org.springframework.web.servlet._mine.web17_view_resolver;
/*
ViewResolver											[interface]
	\--impl----- ViewResolverComposite					[class]
	\--impl----- ContentNegotiatingViewResolver 		[class]
	\--impl----- BeanNameViewResolver					[class]
	\--extends-- AbstractCachingViewResolver			[abstract class]
		\--extends-- ResourceBundleViewResolver			[class]
		\--extends-- XmlViewResolver					[class]
		\--extends-- UrlBasedViewResolver				[class]
			\--extends-- ScriptTemplateViewResolver		[class]
			\--extends-- InternalResourceViewResolver	[class]
			\--extends-- XsltViewResolver				[class]
			\--extends-- AbstractTemplateViewResolver	[abstract class]
				\--extends-- GroovyMarkupViewResolver	[class]
				\--extends-- FreeMarkerViewResolver		[class]
*/

/**
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#processDispatchResult(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, org.springframework.web.servlet.HandlerExecutionChain, org.springframework.web.servlet.ModelAndView, java.lang.Exception)
 * @see org.springframework.web.servlet.DispatcherServlet#render(org.springframework.web.servlet.ModelAndView, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#resolveViewName(java.lang.String, java.util.Map, java.util.Locale, jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.servlet.ViewResolver#resolveViewName(java.lang.String, java.util.Locale)
 */