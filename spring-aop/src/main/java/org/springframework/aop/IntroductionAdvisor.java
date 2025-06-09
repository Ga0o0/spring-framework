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

/**
 * Superinterface for advisors that perform one or more AOP <b>introductions</b>.
 *
 * <p>This interface cannot be implemented directly; subinterfaces must
 * provide the advice type implementing the introduction.
 *
 * <p>Introduction is the implementation of additional interfaces
 * (not implemented by a target) via AOP advice.
 *
 * @author Rod Johnson
 * @since 04.04.2003
 * @see IntroductionInterceptor
 */
// 执行一个或多个 AOP <b>introductions</b> 的通知器的父接口。-
//
// <p>此接口无法直接实现；子接口必须提供实现该引入的通知类型。
//
// <p>引入是通过 AOP 通知实现附加接口（目标未实现）。
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	/**
	 * Return the filter determining which target classes this introduction
	 * should apply to.
	 * <p>This represents the class part of a pointcut. Note that method
	 * matching doesn't make sense to introductions.
	 * @return the class filter
	 */
	// 返回用于确定此引入应应用于哪些目标类的过滤器。
	// <p>这表示切入点的类部分。请注意，方法匹配对引入没有意义。
	// @return 类过滤器
	ClassFilter getClassFilter();

	/**
	 * Can the advised interfaces be implemented by the introduction advice?
	 * Invoked before adding an IntroductionAdvisor.
	 * @throws IllegalArgumentException if the advised interfaces can't be
	 * implemented by the introduction advice
	 */
	// 被通知的接口能否通过引入通知实现？在添加 IntroductionAdvisor 之前调用。
	// @throws IllegalArgumentException 如果被通知的接口无法通过引入通知实现
	void validateInterfaces() throws IllegalArgumentException;

}
