/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.lang.Nullable;

/**
 * Interface for factories that can create Spring AOP Advisors from classes
 * annotated with AspectJ annotation syntax.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see AspectMetadata
 * @see org.aspectj.lang.reflect.AjTypeSystem
 */
// 用于创建 Spring AOP Advisor 的工厂接口，该工厂可以从使用 AspectJ 注解语法注解的类创建 Spring AOP Advisor。
public interface AspectJAdvisorFactory {

	/**
	 * Determine whether the given class is an aspect, as reported
	 * by AspectJ's {@link org.aspectj.lang.reflect.AjTypeSystem}.
	 * <p>Will simply return {@code false} if the supposed aspect is
	 * invalid (such as an extension of a concrete aspect class).
	 * Will return true for some aspects that Spring AOP cannot process,
	 * such as those with unsupported instantiation models.
	 * Use the {@link #validate} method to handle these cases if necessary.
	 * @param clazz the supposed annotation-style AspectJ class
	 * @return whether this class is recognized by AspectJ as an aspect class
	 */
	// 根据 AspectJ 的 {@link org.aspectj.lang.reflect.AjTypeSystem} 判断给定的类是否为切面。
	//
	// <p>如果假定的切面无效（例如，它是某个具体切面类的扩展），则返回 {@code false}。
	// 对于 Spring AOP 无法处理的某些切面（例如，使用了不支持的实例化模型的切面），则返回 true。
	// 如有必要，请使用 {@link #validate} 方法处理这些情况。
	// @param clazz 假定的带有注解的 AspectJ 类
	// @return 此类是否被 AspectJ 识别为切面类
	boolean isAspect(Class<?> clazz);

	/**
	 * Is the given class a valid AspectJ aspect class?
	 * @param aspectClass the supposed AspectJ annotation-style class to validate
	 * @throws AopConfigException if the class is an invalid aspect
	 * (which can never be legal)
	 * @throws NotAnAtAspectException if the class is not an aspect at all
	 * (which may or may not be legal, depending on the context)
	 */
	// 给定的类是否为有效的 AspectJ 切面类？
	// @param aspectClass 要验证的 AspectJ 注解式类
	// @throws AopConfigException 如果该类是无效的切面（这永远不可能合法）
	// @throws NotAnAtAspectException 如果该类根本不是切面（这可能合法也可能不合法，取决于上下文）
	void validate(Class<?> aspectClass) throws AopConfigException;

	/**
	 * Build Spring AOP Advisors for all annotated At-AspectJ methods
	 * on the specified aspect instance.
	 * @param aspectInstanceFactory the aspect instance factory
	 * (not the aspect instance itself in order to avoid eager instantiation)
	 * @return a list of advisors for this class
	 */
	// 为指定切面实例上所有带注解的 At-AspectJ 方法构建 Spring AOP 顾问。
	// @param aspectInstanceFactory 切面实例工厂（而非切面实例本身，以避免急切实例化）
	// @return 此类顾问的列表
	List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory);

	/**
	 * Build a Spring AOP Advisor for the given AspectJ advice method.
	 * @param candidateAdviceMethod the candidate advice method
	 * @param aspectInstanceFactory the aspect instance factory
	 * @param declarationOrder the declaration order within the aspect
	 * @param aspectName the name of the aspect
	 * @return {@code null} if the method is not an AspectJ advice method
	 * or if it is a pointcut that will be used by other advice but will not
	 * create a Spring advice in its own right
	 */
	// 为给定的 AspectJ advice 方法构建一个 Spring AOP Advisor。
	// @param candidateAdviceMethod 候选建议方法
	// @param aspectInstanceFactory 切面实例工厂
	// @param declarationOrder 切面内的声明顺序
	// @param aspectName 切面的名称
	// @return 如果该方法不是 AspectJ 建议方法，或者它是一个会被其他建议使用但本身不会创建 Spring 建议的切入点，则返回 {@code null}
	@Nullable
	Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
			int declarationOrder, String aspectName);

	/**
	 * Build a Spring AOP Advice for the given AspectJ advice method.
	 * @param candidateAdviceMethod the candidate advice method
	 * @param expressionPointcut the AspectJ expression pointcut
	 * @param aspectInstanceFactory the aspect instance factory
	 * @param declarationOrder the declaration order within the aspect
	 * @param aspectName the name of the aspect
	 * @return {@code null} if the method is not an AspectJ advice method
	 * or if it is a pointcut that will be used by other advice but will not
	 * create a Spring advice in its own right
	 * @see org.springframework.aop.aspectj.AspectJAroundAdvice
	 * @see org.springframework.aop.aspectj.AspectJMethodBeforeAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterReturningAdvice
	 * @see org.springframework.aop.aspectj.AspectJAfterThrowingAdvice
	 */
	// 为给定的 AspectJ advice 方法构建一个 Spring AOP Advice。
	// @param candidateAdviceMethod 候选建议方法
	// @param expressionPointcut AspectJ 表达式切入点
	// @param aspectInstanceFactory 切面实例工厂
	// @param declarationOrder 切面内的声明顺序
	// @param aspectName 切面的名称
	// @return 如果该方法不是 AspectJ 建议方法，或者它是一个会被其他建议使用但本身不会创建 Spring 建议的切入点，则返回 {@code null}
	@Nullable
	Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
			MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName);

}
