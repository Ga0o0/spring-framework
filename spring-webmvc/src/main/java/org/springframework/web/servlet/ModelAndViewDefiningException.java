/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.servlet;

import jakarta.servlet.ServletException;

import org.springframework.util.Assert;

/**
 * Exception to be thrown on error conditions that should forward
 * to a specific view with a specific model.
 *
 * <p>Can be thrown at any time during handler processing.
 * This includes any template methods of pre-built controllers.
 * For example, a form controller might abort to a specific error page
 * if certain parameters do not allow to proceed with the normal workflow.
 *
 * @author Juergen Hoeller
 * @since 22.11.2003
 */
// 当出现错误情况并需要跳转到特定视图和特定模型时，将抛出此异常。
//
// <p>此异常可在处理程序执行期间的任何时间抛出。这包括预构建控制器的任何模板方法。
// 例如，如果某些参数不允许继续执行正常的流程，则表单控制器可能会中止并跳转到特定的错误页面。</p>
@SuppressWarnings("serial")
public class ModelAndViewDefiningException extends ServletException {

	private final ModelAndView modelAndView;


	/**
	 * Create new ModelAndViewDefiningException with the given ModelAndView,
	 * typically representing a specific error page.
	 * @param modelAndView the ModelAndView with view to forward to and model to expose
	 */
	public ModelAndViewDefiningException(ModelAndView modelAndView) {
		Assert.notNull(modelAndView, "ModelAndView must not be null in ModelAndViewDefiningException");
		this.modelAndView = modelAndView;
	}

	/**
	 * Return the ModelAndView that this exception contains for forwarding to.
	 */
	// 返回此异常包含的 ModelAndView，以便转发到该对象。
	public ModelAndView getModelAndView() {
		return this.modelAndView;
	}

}
