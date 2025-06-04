package org.springframework.web.servlet.controller;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @see org.springframework.web.servlet.mvc.Controller
 */
public class SimpleController implements Controller {
	@Override
	public ModelAndView handleRequest(@Nonnull HttpServletRequest request,
									  @Nonnull HttpServletResponse response) throws Exception {
		// TODO
		return null;
	}
}
