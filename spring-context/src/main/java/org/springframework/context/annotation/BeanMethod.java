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

package org.springframework.context.annotation;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.core.type.MethodMetadata;
import org.springframework.lang.Nullable;

/**
 * Represents a {@link Configuration @Configuration} class method annotated with
 * {@link Bean @Bean}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see ConfigurationClass
 * @see ConfigurationClassParser
 * @see ConfigurationClassBeanDefinitionReader
 */
// 表示用 {@link Bean @Bean} 注释的 {@link Configuration @Configuration} 类方法。
final class BeanMethod extends ConfigurationMethod {

	BeanMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
		super(metadata, configurationClass);
	}


	@Override
	public void validate(ProblemReporter problemReporter) {
		// 1. 返回值为 void 的方法 -> 报错
		if ("void".equals(getMetadata().getReturnTypeName())) {
			// declared as void: potential misuse of @Bean, maybe meant as init method instead?
			// --> 译文：声明为 void：可能存在 @Bean 注解误用，或许原本是想用作初始化方法？
			problemReporter.error(new VoidDeclaredMethodError());
		}

		// 2. 静态 @Bean 方法无需进一步验证 -> 立即返回
		if (getMetadata().isStatic()) {
			// static @Bean methods have no further constraints to validate -> return immediately
			// --> 译文：静态 @Bean 方法无需进一步验证 -> 立即返回
			return;
		}

		// 3. @Configuration 类中的 @Bean 实例方法必须可重写，以适应 CGLIB
		if (this.configurationClass.getMetadata().isAnnotated(Configuration.class.getName())) {
			if (!getMetadata().isOverridable()) {
				// instance @Bean methods within @Configuration classes must be overridable to accommodate CGLIB
				// --> 译文：@Configuration 类中的 @Bean 实例方法必须可重写，以适应 CGLIB
				problemReporter.error(new NonOverridableMethodError());
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BeanMethod that && this.metadata.equals(that.metadata)));
	}

	@Override
	public int hashCode() {
		return this.metadata.hashCode();
	}

	@Override
	public String toString() {
		return "BeanMethod: " + this.metadata;
	}


	private class VoidDeclaredMethodError extends Problem {

		VoidDeclaredMethodError() {
			super("@Bean method '%s' must not be declared as void; change the method's return type or its annotation."
					.formatted(getMetadata().getMethodName()), getResourceLocation());
		}
	}


	private class NonOverridableMethodError extends Problem {

		NonOverridableMethodError() {
			super("@Bean method '%s' must not be private or final; change the method's modifiers to continue."
					.formatted(getMetadata().getMethodName()), getResourceLocation());
		}
	}

}
