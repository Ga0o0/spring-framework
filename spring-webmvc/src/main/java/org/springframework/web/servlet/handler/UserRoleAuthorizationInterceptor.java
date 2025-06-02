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

package org.springframework.web.servlet.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that checks the authorization of the current user via the
 * user's roles, as evaluated by HttpServletRequest's isUserInRole method.
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see jakarta.servlet.http.HttpServletRequest#isUserInRole
 */
// 拦截器通过用户的角色检查当前用户的授权，由 HttpServletRequest 的 isUserInRole 方法评估。
public class UserRoleAuthorizationInterceptor implements HandlerInterceptor {

	@Nullable
	private String[] authorizedRoles;


	/**
	 * Set the roles that this interceptor should treat as authorized.
	 * @param authorizedRoles array of role names
	 */
	// 设置此拦截器应视为授权的角色。
	// @param authorizedRoles 角色名称数组
	public final void setAuthorizedRoles(String... authorizedRoles) {
		this.authorizedRoles = authorizedRoles;
	}


	@Override
	public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException, IOException {

		if (this.authorizedRoles != null) {
			for (String role : this.authorizedRoles) {
				if (request.isUserInRole(role)) {
					return true;
				}
			}
		}
		handleNotAuthorized(request, response, handler);
		return false;
	}

	/**
	 * Handle a request that is not authorized according to this interceptor.
	 * Default implementation sends HTTP status code 403 ("forbidden").
	 * <p>This method can be overridden to write a custom message, forward or
	 * redirect to some error page or login page, or throw a ServletException.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler chosen handler to execute, for type and/or instance evaluation
	 * @throws jakarta.servlet.ServletException if there is an internal error
	 * @throws java.io.IOException in case of an I/O error when writing the response
	 */
	// 处理此拦截器未授权的请求。默认实现会发送 HTTP 状态码 403（“禁止访问”）。
	// <p>此方法可以被重写，用于编写自定义消息、转发或重定向到某个错误页面或登录页面，或者抛出 ServletException。
	// @param request 当前 HTTP 请求
	// @param respond 当前 HTTP 响应
	// @param handler 指定要执行的处理程序，用于类型和/或实例评估
	// @throws jakarta.servlet.ServletException（如果发生内部错误）
	// @throws java.io.IOException（如果在写入响应时发生 I/O 错误）
	protected void handleNotAuthorized(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws ServletException, IOException {

		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}

}
