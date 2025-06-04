package org.springframework.web.servlet.controller;

import jakarta.annotation.Nonnull;
import org.springframework.web.servlet.function.*;

/**
 * @see org.springframework.web.servlet.function.HandlerFunction
 */
public class SimpleHandlerFunction implements HandlerFunction<AsyncServerResponse> {
	@Nonnull
	@Override
	public AsyncServerResponse handle(@Nonnull ServerRequest request) throws Exception {
		// TODO
		return null;
	}
}
