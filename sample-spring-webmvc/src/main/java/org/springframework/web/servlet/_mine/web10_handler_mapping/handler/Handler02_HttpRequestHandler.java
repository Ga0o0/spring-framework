package org.springframework.web.servlet._mine.web10_handler_mapping.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpRequestHandler;

import java.io.IOException;

/**
 * Handler impl {@link org.springframework.web.HttpRequestHandler}
 *
 * @see org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 *
 * @see org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter
 */
public class Handler02_HttpRequestHandler implements HttpRequestHandler {
	@Override
	public void handleRequest(@NonNull HttpServletRequest request,
							  @NonNull HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Handler impl HttpRequestHandler...");
	}
}