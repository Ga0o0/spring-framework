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

package org.springframework.validation.annotation;

import java.lang.annotation.Annotation;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

/**
 * Utility class for handling validation annotations.
 * Mainly for internal use within the framework.
 *
 * @author Christoph Dreis
 * @author Juergen Hoeller
 * @since 5.3.7
 */
public abstract class ValidationAnnotationUtils {

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


	/**
	 * Determine any validation hints by the given annotation.
	 * <p>This implementation checks for Spring's
	 * {@link org.springframework.validation.annotation.Validated},
	 * {@code @jakarta.validation.Valid}, and custom annotations whose
	 * name starts with "Valid" which may optionally declare validation
	 * hints through the "value" attribute.
	 * @param ann the annotation (potentially a validation annotation)
	 * @return the validation hints to apply (possibly an empty array),
	 * or {@code null} if this annotation does not trigger any validation
	 */
	// 通过给定的注解确定所有验证提示。
	// <p>此实现检查 Spring 的 {@link org.springframework.validation.annotation.Validated}、
	// {@code @jakarta.validation.Valid} 以及名称以“Valid”开头的自定义注解，这些注解可以通过“value”属性选择性地声明验证提示。
	// @param ann 注解（可能是一个验证注解）
	// @return 要应用的验证提示（可能为空数组），如果此注解未触发任何验证，则返回 {@code null}
	@Nullable
	public static Object[] determineValidationHints(Annotation ann) {
		// Direct presence of @Validated ? --> 译文：直接存在 @Validated 吗？
		if (ann instanceof Validated validated) {
			return validated.value();
		}
		// Direct presence of @Valid ? --> 译文：直接存在 @Valid 吗？
		Class<? extends Annotation> annotationType = ann.annotationType();
		if ("jakarta.validation.Valid".equals(annotationType.getName())) {
			return EMPTY_OBJECT_ARRAY;
		}
		// Meta presence of @Validated ? --> 译文：@Validated 的元存在吗？
		Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
		if (validatedAnn != null) {
			return validatedAnn.value();
		}
		// Custom validation annotation ? --> 译文：自定义验证注释？
		if (annotationType.getSimpleName().startsWith("Valid")) {
			return convertValidationHints(AnnotationUtils.getValue(ann));
		}
		// No validation triggered --> 译文：未触发验证
		return null;
	}

	private static Object[] convertValidationHints(@Nullable Object hints) {
		if (hints == null) {
			return EMPTY_OBJECT_ARRAY;
		}
		return (hints instanceof Object[] objectHints ? objectHints : new Object[] {hints});
	}

}
