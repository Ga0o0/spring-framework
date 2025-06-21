/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * Exception thrown in case of a reference to a bean that's currently in creation.
 * Typically happens when constructor autowiring matches the currently constructed bean.
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
// 当引用正在创建的 bean 时会抛出异常。这种情况通常发生在构造函数自动装配匹配到当前已创建的 bean 时。
@SuppressWarnings("serial")
public class BeanCurrentlyInCreationException extends BeanCreationException {

	/**
	 * Create a new BeanCurrentlyInCreationException,
	 * with a default error message that indicates a circular reference.
	 * @param beanName the name of the bean requested
	 */
	public BeanCurrentlyInCreationException(String beanName) {
		super(beanName,
				"Requested bean is currently in creation: Is there an unresolvable circular reference?");
	}

	/**
	 * Create a new BeanCurrentlyInCreationException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public BeanCurrentlyInCreationException(String beanName, String msg) {
		super(beanName, msg);
	}

}
