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

package org.springframework.aop;

import java.io.Serializable;

/**
 * Canonical ClassFilter instance that matches all classes.
 *
 * @author Rod Johnson
 */
// 匹配所有类的规范 ClassFilter 实例。
@SuppressWarnings("serial")
final class TrueClassFilter implements ClassFilter, Serializable {

	public static final TrueClassFilter INSTANCE = new TrueClassFilter();

	/**
	 * Enforce Singleton pattern.
	 */
	// 强制实施单例模式。
	private TrueClassFilter() {
	}

	@Override
	public boolean matches(Class<?> clazz) {
		return true;
	}

	/**
	 * Required to support serialization. Replaces with canonical
	 * instance on deserialization, protecting Singleton pattern.
	 * Alternative to overriding {@code equals()}.
	 */
	// 支持序列化所必需的。反序列化时替换为规范实例，保护单例模式。替代覆盖 {@code equals()} 的方法。
	private Object readResolve() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "ClassFilter.TRUE";
	}

}
