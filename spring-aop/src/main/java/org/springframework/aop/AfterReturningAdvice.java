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

package org.springframework.aop;

import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

/**
 * After returning advice is invoked only on normal method return, not if an
 * exception is thrown. Such advice can see the return value, but cannot change it.
 *
 * @author Rod Johnson
 * @see MethodBeforeAdvice
 * @see ThrowsAdvice
 */
// 返回后通知仅在方法正常返回时调用，抛出异常时不会调用。此类通知可以查看返回值，但无法更改它。
public interface AfterReturningAdvice extends AfterAdvice {

	/**
	 * Callback after a given method successfully returned.
	 * @param returnValue the value returned by the method, if any
	 * @param method the method being invoked
	 * @param args the arguments to the method
	 * @param target the target of the method invocation. May be {@code null}.
	 * @throws Throwable if this object wishes to abort the call.
	 * Any exception thrown will be returned to the caller if it's
	 * allowed by the method signature. Otherwise the exception
	 * will be wrapped as a runtime exception.
	 */
	// 指定方法成功返回后的回调。
	// @param returnValue 方法返回的值（如果有）
	// @param method 被调用的方法
	// @param args 方法的参数
	// @param target 方法调用的目标。可以为 {@code null}。
	// @throws 如果此对象希望中止调用，则为 Throwable。如果方法签名允许，任何抛出的异常都将返回给调用者。否则，异常将被包装为运行时异常。
	void afterReturning(@Nullable Object returnValue, Method method, Object[] args, @Nullable Object target) throws Throwable;

}
