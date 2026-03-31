/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.mvc.method.annotation;

import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves an argument of type {@link Principal}, similar to
 * {@link ServletRequestMethodArgumentResolver} but irrespective of whether the
 * argument is annotated or not. This is done to enable custom argument
 * resolution of a {@link Principal} argument (with a custom annotation).
 *
 * @author Rossen Stoyanchev
 * @since 5.3.1
 */
// 解析类型为 {@link Principal} 的参数，类似于 {@link ServletRequestMethodArgumentResolver}，
// 但无论参数是否带有注解。这样做是为了支持对带有自定义注解的 {@link Principal} 参数进行自定义解析。
public class PrincipalMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Principal.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		if (request == null) {
			throw new IllegalStateException("Current request is not of type HttpServletRequest: " + webRequest);
		}

		Principal principal = request.getUserPrincipal();
		if (principal != null && !parameter.getParameterType().isInstance(principal)) {
			throw new IllegalStateException("Current user principal is not of type [" +
					parameter.getParameterType().getName() + "]: " + principal);
		}

		return principal;
	}

}
