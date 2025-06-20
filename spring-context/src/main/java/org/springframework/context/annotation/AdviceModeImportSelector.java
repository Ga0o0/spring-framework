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

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Convenient base class for {@link ImportSelector} implementations that select imports
 * based on an {@link AdviceMode} value from an annotation (such as the {@code @Enable*}
 * annotations).
 *
 * @author Chris Beams
 * @since 3.1
 * @param <A> annotation containing {@linkplain #getAdviceModeAttributeName() AdviceMode attribute}
 */
// 方便的 {@link ImportSelector} 实现基类，根据注释中的 {@link AdviceMode} 值选择导入（例如 {@code @Enable*} 注释）。
//
// @param <A> 注释包含 {@linkplain #getAdviceModeAttributeName() AdviceMode 属性}
public abstract class AdviceModeImportSelector<A extends Annotation> implements ImportSelector {

	/**
	 * The default advice mode attribute name.
	 */
	// 默认建议模式属性名称。
	public static final String DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME = "mode";


	/**
	 * The name of the {@link AdviceMode} attribute for the annotation specified by the
	 * generic type {@code A}. The default is {@value #DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME},
	 * but subclasses may override in order to customize.
	 */
	// 泛型类型 {@code A} 指定的注释的 {@link AdviceMode} 属性名称。
	// 默认值为 {@value #DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME}，但子类可以重写该属性以进行自定义。
	protected String getAdviceModeAttributeName() {
		return DEFAULT_ADVICE_MODE_ATTRIBUTE_NAME;
	}

	/**
	 * This implementation resolves the type of annotation from generic metadata and
	 * validates that (a) the annotation is in fact present on the importing
	 * {@code @Configuration} class and (b) that the given annotation has an
	 * {@linkplain #getAdviceModeAttributeName() advice mode attribute} of type
	 * {@link AdviceMode}.
	 * <p>The {@link #selectImports(AdviceMode)} method is then invoked, allowing the
	 * concrete implementation to choose imports in a safe and convenient fashion.
	 * @throws IllegalArgumentException if expected annotation {@code A} is not present
	 * on the importing {@code @Configuration} class or if {@link #selectImports(AdviceMode)}
	 * returns {@code null}
	 */
	// 此实现从通用元数据中解析注释类型，并验证 (a) 注释确实存在于导入的 {@code @Configuration} 类中，
	// 以及 (b) 给定的注释具有类型为 {@link AdviceMode} 的 {@linkplain #getAdviceModeAttributeName() 建议模式属性}。
	// <p>然后调用 {@link #selectImports(AdviceMode)} 方法，允许具体实现以安全便捷的方式选择导入。
	// @throws IllegalArgumentException 如果导入的 {@code @Configuration} 类中不存在预期的注释 {@code A}，
	// 或者 {@link #selectImports(AdviceMode)} 返回 {@code null}
	@Override
	public final String[] selectImports(AnnotationMetadata importingClassMetadata) {
		Class<?> annType = GenericTypeResolver.resolveTypeArgument(getClass(), AdviceModeImportSelector.class);
		// AdviceModeImportSelector 的无法解析的类型参数
		Assert.state(annType != null, "Unresolvable type argument for AdviceModeImportSelector");

		AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
		if (attributes == null) {
			throw new IllegalArgumentException(String.format(
					"@%s is not present on importing class '%s' as expected",
					annType.getSimpleName(), importingClassMetadata.getClassName()));
		}

		AdviceMode adviceMode = attributes.getEnum(getAdviceModeAttributeName());
		String[] imports = selectImports(adviceMode);
		if (imports == null) {
			throw new IllegalArgumentException("Unknown AdviceMode: " + adviceMode);
		}
		return imports;
	}

	/**
	 * Determine which classes should be imported based on the given {@code AdviceMode}.
	 * <p>Returning {@code null} from this method indicates that the {@code AdviceMode}
	 * could not be handled or was unknown and that an {@code IllegalArgumentException}
	 * should be thrown.
	 * @param adviceMode the value of the {@linkplain #getAdviceModeAttributeName()
	 * advice mode attribute} for the annotation specified via generics.
	 * @return array containing classes to import (empty array if none;
	 * {@code null} if the given {@code AdviceMode} is unknown)
	 */
	// 根据给定的 {@code AdviceMode} 确定应导入哪些类。
	// <p>从此方法返回 {@code null} 表示无法处理 {@code AdviceMode} 或该 {@code AdviceMode} 未知，
	// 应抛出 {@code IllegalArgumentException}。
	// @param adviceMode 是通过泛型指定的注释的 {@linkplain #getAdviceModeAttributeName() 建议模式属性} 的值。
	// @return 包含要导入的类的数组（如果没有，则为空数组；如果给定的 {@code AdviceMode} 未知，则为 {@code null}）
	@Nullable
	protected abstract String[] selectImports(AdviceMode adviceMode);

}
