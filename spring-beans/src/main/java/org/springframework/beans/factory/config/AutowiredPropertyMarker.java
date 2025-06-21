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

package org.springframework.beans.factory.config;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * Simple marker class for an individually autowired property value, to be added
 * to {@link BeanDefinition#getPropertyValues()} for a specific bean property.
 *
 * <p>At runtime, this will be replaced with a {@link DependencyDescriptor}
 * for the corresponding bean property's write method, eventually to be resolved
 * through a {@link AutowireCapableBeanFactory#resolveDependency} step.
 *
 * @author Juergen Hoeller
 * @since 5.2
 * @see AutowireCapableBeanFactory#resolveDependency
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.BeanDefinitionBuilder#addAutowiredProperty
 */
// 一个简单的标记类，用于标记单独自动装配的属性值，该值将添加到特定 bean 属性的 {@link BeanDefinition#getPropertyValues()} 中。
//
// <p>运行时，此标记将被替换为对应 bean 属性的 write 方法的 {@link DependencyDescriptor}，
// 最终通过 {@link AutowireCapableBeanFactory#resolveDependency} 步骤解析。
@SuppressWarnings("serial")
public final class AutowiredPropertyMarker implements Serializable {

	/**
	 * The canonical instance for the autowired marker value.
	 */
	// 自动注入标记值的规范实例。
	public static final Object INSTANCE = new AutowiredPropertyMarker();


	private AutowiredPropertyMarker() {
	}

	private Object readResolve() {
		return INSTANCE;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other);
	}

	@Override
	public int hashCode() {
		return AutowiredPropertyMarker.class.hashCode();
	}

	@Override
	public String toString() {
		return "(autowired)";
	}

}
