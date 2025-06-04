/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.validation.method;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

/**
 * Contract to apply method validation and handle the results.
 * Exposes methods that return {@link MethodValidationResult}, and methods that
 * handle the results, by default raising {@link MethodValidationException}.
 *
 * @author Rossen Stoyanchev
 * @since 6.1
 */
public interface MethodValidator {

	/**
	 * Determine the applicable validation groups. By default, obtained from an
	 * {@link org.springframework.validation.annotation.Validated @Validated}
	 * annotation on the method, or on the class level.
	 * @param target the target Object
	 * @param method the target method
	 * @return the applicable validation groups as a {@code Class} array
	 */
	Class<?>[] determineValidationGroups(Object target, Method method);

	/**
	 * Validate the given method arguments and return validation results.
	 * @param target the target Object
	 * @param method the target method
	 * @param parameters the parameters, if already created and available
	 * @param arguments the candidate argument values to validate
	 * @param groups validation groups from {@link #determineValidationGroups}
	 * @return the result of validation
	 */
	// 验证给定方法的参数并返回验证结果。
	// @param target 目标对象
	// @param method 目标方法
	// @param parameter 参数（如果已创建且可用）
	// @param argument 待验证的候选参数值
	// @param groups 来自 {@link #determineValidationGroups} 的验证组
	// @return 验证结果
	MethodValidationResult validateArguments(
			Object target, Method method, @Nullable MethodParameter[] parameters,
			Object[] arguments, Class<?>[] groups);

	/**
	 * Delegate to {@link #validateArguments} and handle the validation result,
	 * by default raising {@link MethodValidationException} in case of errors.
	 * Implementations may provide alternative handling, e.g. injecting
	 * {@link org.springframework.validation.Errors} into the method.
	 * @throws MethodValidationException in case of unhandled errors.
	 */
	// 委托给 {@link #validateArguments} 并处理验证结果，默认情况下，如果发生错误，将引发 {@link MethodValidationException}。
	// 实现可以提供其他处理方式，例如，在方法中注入 {@link org.springframework.validation.Errors}。
	// @throws MethodValidationException 如果发生未处理的错误
	default void applyArgumentValidation(
			Object target, Method method, @Nullable MethodParameter[] parameters,
			Object[] arguments, Class<?>[] groups) {

		MethodValidationResult result = validateArguments(target, method, parameters, arguments, groups);
		if (result.hasErrors()) {
			throw new MethodValidationException(result);
		}
	}

	/**
	 * Validate the given return value and return validation results.
	 * @param target the target Object
	 * @param method the target method
	 * @param returnType the return parameter, if already created and available
	 * @param returnValue the return value to validate
	 * @param groups validation groups from {@link #determineValidationGroups}
	 * @return the result of validation
	 */
	// 验证给定的返回值并返回验证结果。
	// @param target 目标对象
	// @param method 目标方法
	// @param returnType 返回参数（如果已创建且可用）
	// @param returnValue 待验证的返回值
	// @param groups 验证组（来自 {@link #determineValidationGroups}）
	// @return 验证结果
	MethodValidationResult validateReturnValue(
			Object target, Method method, @Nullable MethodParameter returnType,
			@Nullable Object returnValue, Class<?>[] groups);

	/**
	 * Delegate to {@link #validateReturnValue} and handle the validation result,
	 * by default raising {@link MethodValidationException} in case of errors.
	 * Implementations may provide alternative handling.
	 * @throws MethodValidationException in case of unhandled errors.
	 */
	// 委托给 {@link #validateReturnValue} 并处理验证结果，默认情况下，如果发生错误，将引发 {@link MethodValidationException}。
	// 具体实现可以提供其他处理方式。如果发生未处理的错误，将 @throws MethodValidationException。
	default void applyReturnValueValidation(
			Object target, Method method, @Nullable MethodParameter returnType,
			@Nullable Object returnValue, Class<?>[] groups) {

		MethodValidationResult result = validateReturnValue(target, method, returnType, returnValue, groups);
		if (result.hasErrors()) {
			throw new MethodValidationException(result);
		}
	}

}
