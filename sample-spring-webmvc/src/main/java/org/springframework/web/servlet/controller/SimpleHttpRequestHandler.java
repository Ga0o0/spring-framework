package org.springframework.web.servlet.controller;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.HttpRequestHandler;

import java.io.IOException;

/**
 * @see org.springframework.web.HttpRequestHandler
 */
public class SimpleHttpRequestHandler implements HttpRequestHandler {
	@Override
	public void handleRequest(@Nonnull HttpServletRequest request,
							  @Nonnull HttpServletResponse response) throws ServletException, IOException {
		// TODO
	}
}
