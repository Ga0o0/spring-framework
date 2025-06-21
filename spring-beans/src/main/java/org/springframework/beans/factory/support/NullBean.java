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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

/**
 * Internal representation of a null bean instance, e.g. for a {@code null} value
 * returned from {@link FactoryBean#getObject()} or from a factory method.
 *
 * <p>Each such null bean is represented by a dedicated {@code NullBean} instance
 * which are not equal to each other, uniquely differentiating each bean as returned
 * from all variants of {@link org.springframework.beans.factory.BeanFactory#getBean}.
 * However, each such instance will return {@code true} for {@code #equals(null)}
 * and returns "null" from {@code #toString()}, which is how they can be tested
 * externally (since this class itself is not public).
 *
 * @author Juergen Hoeller
 * @since 5.0
 */
// 空 bean 实例的内部表示，例如，从{@link FactoryBean#getObject()} 或工厂方法返回的 {@code null} 值。
//
// <p>每个这样的空bean都由一个专用的{@code NullBean}实例表示，这些实例彼此不相等，
// 从而唯一区分从所有{@link org.springframework.beans.factory.BeanFactory#getBean}变体返回的每个bean。
// 但是，每个这样的实例对于{@code #equals(null)}都将返回{@code true}，并且从{@code #toString()}返回“null”，
// 这是它们可以在外部进行测试的方式（因为该类本身不是公共的）。
final class NullBean {

	NullBean() {
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || other == null);
	}

	@Override
	public int hashCode() {
		return NullBean.class.hashCode();
	}

	@Override
	public String toString() {
		return "null";
	}

}
