/*
 * Copyright 2002-2024 the original author or authors.
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

import java.lang.reflect.Method;

import org.springframework.util.ClassUtils;

/**
 * Descriptor for a {@link Method Method} which holds a
 * reference to the method's {@linkplain #declaringClass declaring class},
 * {@linkplain #methodName name}, and {@linkplain #parameterTypes parameter types}.
 *
 * @author Sam Brannen
 * @since 6.0.11
 * @param declaringClass the method's declaring class
 * @param methodName the name of the method
 * @param parameterTypes the types of parameters accepted by the method
 */
record MethodDescriptor(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {

	/**
	 * Create a {@link MethodDescriptor} for the supplied bean class and method name.
	 * <p>The supplied {@code methodName} may be a {@linkplain Method#getName()
	 * simple method name} or a {@linkplain ClassUtils#getQualifiedMethodName(Method)
	 * qualified method name}.
	 * <p>If the method name is fully qualified, this utility will parse the
	 * method name and its declaring class from the qualified method name and then
	 * attempt to load the method's declaring class using the {@link ClassLoader}
	 * of the supplied {@code beanClass}. Otherwise, the returned descriptor will
	 * reference the supplied {@code beanClass} and {@code methodName}.
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param beanClass the bean class
	 * @param methodName the name of the method
	 * @return a new {@code MethodDescriptor}; never {@code null}
	 */
	// 为提供的 bean 类和方法名创建一个 {@link MethodDescriptor}。
	//
	// <p>提供的 {@code methodName} 可以是 {@linkplain Method#getName() 简单方法名}
	// 或 {@linkplain ClassUtils#getQualifiedMethodName(Method) 限定方法名}。
	//
	// <p>如果方法名是完全限定的，此工具将从限定方法名中解析出方法名及其声明类，然后尝试使用提供的
	// {@code beanClass} 的 {@link ClassLoader} 加载方法的声明类。
	// 否则，返回的描述符将引用提供的 {@code beanClass} 和 {@code methodName}。
	//
	// @param beanName 工厂中的 bean 名称（用于调试目的）
	// @param beanClass bean 类
	// @param methodName 方法名
	// @return 一个新的 {@code MethodDescriptor}；永远不会返回 {@code null}
	static MethodDescriptor create(String beanName, Class<?> beanClass, String methodName) {
		try {
			Class<?> declaringClass = beanClass;
			String methodNameToUse = methodName;

			// Parse fully-qualified method name if necessary.
			int indexOfDot = methodName.lastIndexOf('.');
			if (indexOfDot > 0) {
				String className = methodName.substring(0, indexOfDot);
				methodNameToUse = methodName.substring(indexOfDot + 1);
				if (!beanClass.getName().equals(className)) {
					declaringClass = ClassUtils.forName(className, beanClass.getClassLoader());
				}
			}
			return new MethodDescriptor(declaringClass, methodNameToUse);
		}
		catch (Exception | LinkageError ex) {
			throw new BeanDefinitionValidationException(
					"Could not create MethodDescriptor for method '%s' on bean with name '%s': %s"
						.formatted(methodName, beanName, ex.getMessage()));
		}
	}

}
