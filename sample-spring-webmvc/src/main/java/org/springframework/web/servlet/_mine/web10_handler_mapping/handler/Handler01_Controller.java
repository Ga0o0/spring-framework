package org.springframework.web.servlet._mine.web10_handler_mapping.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Handler impl {@link org.springframework.web.servlet.mvc.Controller}
 *
 * @see org.springframework.web.servlet.mvc.Controller
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 *
 * @see  org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 */
public class Handler01_Controller implements Controller {
	@Override
	public ModelAndView handleRequest(@NonNull HttpServletRequest request,
									  @NonNull HttpServletResponse response) throws Exception {
		return new ModelAndView("index");
	}
}
