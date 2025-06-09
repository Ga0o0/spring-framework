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

package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * Interface allowing extension to the Spring AOP framework to allow
 * handling of new Advisors and Advice types.
 *
 * <p>Implementing objects can create AOP Alliance Interceptors from
 * custom advice types, enabling these advice types to be used
 * in the Spring AOP framework, which uses interception under the covers.
 *
 * <p>There is no need for most Spring users to implement this interface;
 * do so only if you need to introduce more Advisor or Advice types to Spring.
 *
 * @author Rod Johnson
 */
// 此接口允许扩展 Spring AOP 框架，以处理新的 Advisor 和 Advice 类型。
//
// <p>实现对象可以根据自定义 Advice 类型创建 AOP Alliance Interceptors，
// 从而使这些 Advice 类型能够在 Spring AOP 框架中使用，该框架在底层使用了拦截功能。
//
// <p>大多数 Spring 用户无需实现此接口；只有当您需要向 Spring 引入更多 Advisor 或 Advice 类型时才需要实现。
public interface AdvisorAdapter {

	/**
	 * Does this adapter understand this advice object? Is it valid to
	 * invoke the {@code getInterceptors} method with an Advisor that
	 * contains this advice as an argument?
	 * @param advice an Advice such as a BeforeAdvice
	 * @return whether this adapter understands the given advice object
	 * @see #getInterceptor(org.springframework.aop.Advisor)
	 * @see org.springframework.aop.BeforeAdvice
	 */
	// 此适配器是否理解此建议对象？
	// 使用包含此建议作为参数的 Advisor 调用 {@code getInterceptors} 方法是否有效？
	// @param advice 一个 Advice，例如 BeforeAdvice 。
	// @return 此适配器是否理解给定的建议对象。
	boolean supportsAdvice(Advice advice);

	/**
	 * Return an AOP Alliance MethodInterceptor exposing the behavior of
	 * the given advice to an interception-based AOP framework.
	 * <p>Don't worry about any Pointcut contained in the Advisor;
	 * the AOP framework will take care of checking the pointcut.
	 * @param advisor the Advisor. The supportsAdvice() method must have
	 * returned true on this object
	 * @return an AOP Alliance interceptor for this Advisor. There's
	 * no need to cache instances for efficiency, as the AOP framework
	 * caches advice chains.
	 */
	// 返回一个 AOP 联盟方法拦截器 (MethodInterceptor)，将给定建议的行为暴露给基于拦截的 AOP 框架。
	// <p>无需担心 Advisor 中包含的任何切入点 (Pointcut)；AOP 框架将负责检查切入点。
	// @param advisor 表示 Advisor。此对象的 supportAdvice() 方法必须返回 true。
	// @return 表示此 Advisor 的 AOP 联盟拦截器。无需为了提高效率而缓存实例，因为 AOP 框架会缓存建议链。
	MethodInterceptor getInterceptor(Advisor advisor);

}
