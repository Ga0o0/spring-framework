package org.springframework.web.servlet.conf;

import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor
 * @see org.springframework.web.bind.annotation.ControllerAdvice
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle(org.springframework.web.context.request.ServletWebRequest,
 * org.springframework.web.method.support.ModelAndViewContainer, Object...)
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor#resolveArgument(org.springframework.core.MethodParameter,
 * org.springframework.web.method.support.ModelAndViewContainer,
 * org.springframework.web.context.request.NativeWebRequest, org.springframework.web.bind.support.WebDataBinderFactory)
 * @see org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver#readWithMessageConverters(HttpInputMessage, MethodParameter, Type)
 */
@ControllerAdvice
public class MyRequestBodyAdvice implements RequestBodyAdvice {

	/*

	   curl --location --request GET 'http://localhost:8080/req_body' \
			--header 'Content-Type: application/json' \
			--data '{
				"id": 1,
				"name": "xiaoming"
			}'

	*/

	@Override
	public boolean supports(@Nonnull MethodParameter methodParameter, @Nonnull Type targetType,
							@Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Nonnull
	@Override
	public HttpInputMessage beforeBodyRead(@Nonnull HttpInputMessage inputMessage, @Nonnull MethodParameter parameter,
										   @Nonnull Type targetType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		System.out.println("org.springframework.web.servlet.conf.MyRequestBodyAdvice.beforeBodyRead()");
		System.out.println(inputMessage.getBody());

		// 这里读取后，afterBodyRead() 方法中就再无法读取
		// System.out.println(new String(inputMessage.getBody().readAllBytes()));
		return inputMessage;
	}

	@Nonnull
	@Override
	public Object afterBodyRead(@Nonnull Object body, @Nonnull HttpInputMessage inputMessage,
								@Nonnull MethodParameter parameter, @Nonnull Type targetType,
								@Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
		System.out.println("org.springframework.web.servlet.conf.MyRequestBodyAdvice.afterBodyRead()");
		System.out.println(body);
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, @Nonnull HttpInputMessage inputMessage,
								  @Nonnull MethodParameter parameter, @Nonnull Type targetType,
								  @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
		System.out.println("org.springframework.web.servlet.conf.MyRequestBodyAdvice.handleEmptyBody()");
		System.out.println(body);
		return body;
	}
}