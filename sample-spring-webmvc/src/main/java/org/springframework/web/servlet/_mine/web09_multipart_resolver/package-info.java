/**
 * MultipartResolver
 *
 * @see org.springframework.web.multipart.MultipartResolver
 * @see org.springframework.web.multipart.support.StandardServletMultipartResolver
 */
package org.springframework.web.servlet._mine.web09_multipart_resolver;
/**
 * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## 1. DispatcherServlet#checkMultipart(...)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#checkMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#isMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#resolveMultipart(jakarta.servlet.http.HttpServletRequest)
 *
 * ## 2. DispatcherServlet#cleanupMultipart(...)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#cleanupMultipart(jakarta.servlet.http.HttpServletRequest)
 * @see org.springframework.web.multipart.MultipartResolver#cleanupMultipart(org.springframework.web.multipart.MultipartHttpServletRequest)
 */