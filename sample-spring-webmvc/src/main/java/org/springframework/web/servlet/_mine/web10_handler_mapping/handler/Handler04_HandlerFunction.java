package org.springframework.web.servlet._mine.web10_handler_mapping.handler;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Handler impl {@link org.springframework.web.servlet.function.HandlerFunction}
 *
 * @see org.springframework.web.servlet.function.HandlerFunction
 * @see org.springframework.web.servlet.function.ServerRequest
 * @see org.springframework.web.servlet.function.ServerResponse
 *
 * @see org.springframework.web.servlet.function.support.HandlerFunctionAdapter
 */
public class Handler04_HandlerFunction implements HandlerFunction<ServerResponse> {
	@Override
	@NonNull
	public ServerResponse handle(@NonNull ServerRequest request) throws Exception {
		return ServerResponse.ok().body("Handler impl HandlerFunction");
	}
}