package org.springframework.web.servlet.controller;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * @see jakarta.servlet.Servlet
 */
public class SimpleServlet implements Servlet {

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {

	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
		// TODO
	}

	@Override
	public String getServletInfo() {
		return "";
	}

	@Override
	public void destroy() {

	}

}
