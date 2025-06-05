package org.springframework.web.servlet.conf;

import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor
 * @see org.springframework.web.bind.annotation.ControllerAdvice
 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor#handleReturnValue(Object, org.springframework.core.MethodParameter,
 * org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyAdviceChain#processBody(Object, org.springframework.core.MethodParameter,
 * org.springframework.http.MediaType, Class, org.springframework.http.server.ServerHttpRequest, org.springframework.http.server.ServerHttpResponse)
 */
@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice<String> {

	/*
	 curl --location 'http://localhost:8080/rest'
	 */

	@Override
	public boolean supports(@Nonnull MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
		return String.class.equals(returnType.getParameterType());
	}

	@Override
	public String beforeBodyWrite(String body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType,
								  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
								  @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
		body = body + "-----------------------------";
		return body;
	}
}
