/*
 * Copyright 2002-2022 the original author or authors.
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

import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * Extension of the {@link InstantiationAwareBeanPostProcessor} interface,
 * adding a callback for predicting the eventual type of a processed bean.
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. In general, application-provided
 * post-processors should simply implement the plain {@link BeanPostProcessor}
 * interface.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/**
	 * Predict the type of the bean to be eventually returned from this
	 * processor's {@link #postProcessBeforeInstantiation} callback.
	 * <p>The default implementation returns {@code null}.
	 * Specific implementations should try to predict the bean type as
	 * far as known/cached already, without extra processing steps.
	 * @param beanClass the raw class of the bean
	 * @param beanName the name of the bean
	 * @return the type of the bean, or {@code null} if not predictable
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	@Nullable
	default Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	/**
	 * Determine the type of the bean to be eventually returned from this
	 * processor's {@link #postProcessBeforeInstantiation} callback.
	 * <p>The default implementation returns the given bean class as-is.
	 * Specific implementations should fully evaluate their processing steps
	 * in order to create/initialize a potential proxy class upfront.
	 * @param beanClass the raw class of the bean
	 * @param beanName the name of the bean
	 * @return the type of the bean (never {@code null})
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @since 6.0
	 */
	default Class<?> determineBeanType(Class<?> beanClass, String beanName) throws BeansException {
		return beanClass;
	}

	/**
	 * Determine the candidate constructors to use for the given bean.
	 * <p>The default implementation returns {@code null}.
	 * @param beanClass the raw class of the bean (never {@code null})
	 * @param beanName the name of the bean
	 * @return the candidate constructors, or {@code null} if none specified
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 确定给定 bean 的候选构造函数。
	//
	// <p>默认实现返回 {@code null}。</p>
	// @param beanClass bean 的原始类（永远不会返回 {@code null}）
	// @param beanName bean 的名称
	// @return 候选构造函数，如果没有指定，则返回 {@code null}
	// @throws org.springframework.beans.BeansException 如果发生错误
	@Nullable
	default Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName)
			throws BeansException {

		return null;
	}

	/**
	 * Obtain a reference for early access to the specified bean,
	 * typically for the purpose of resolving a circular reference.
	 * <p>This callback gives post-processors a chance to expose a wrapper
	 * early - that is, before the target bean instance is fully initialized.
	 * The exposed object should be equivalent to the what
	 * {@link #postProcessBeforeInitialization} / {@link #postProcessAfterInitialization}
	 * would expose otherwise. Note that the object returned by this method will
	 * be used as bean reference unless the post-processor returns a different
	 * wrapper from said post-process callbacks. In other words: Those post-process
	 * callbacks may either eventually expose the same reference or alternatively
	 * return the raw bean instance from those subsequent callbacks (if the wrapper
	 * for the affected bean has been built for a call to this method already,
	 * it will be exposes as final bean reference by default).
	 * <p>The default implementation returns the given {@code bean} as-is.
	 * @param bean the raw bean instance
	 * @param beanName the name of the bean
	 * @return the object to expose as bean reference
	 * (typically with the passed-in bean instance as default)
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 获取指定 bean 的引用以便提前访问，通常用于解决循环引用问题。
	//
	// <p>此回调允许后处理器在目标 bean 实例完全初始化之前提前暴露包装器。
	// 暴露的对象应与 {@link #postProcessBeforeInitialization} / {@link #postProcessAfterInitialization} 通常会暴露的对象等效。
	// 请注意，除非后处理器从所述后处理回调中返回不同的包装器，否则此方法返回的对象将用作 bean 引用。
	// 换句话说：这些后处理回调最终可能暴露相同的引用，或者从后续回调中返回原始 bean 实例（如果受影响 bean 的包装器已为调用此方法构建，则默认情况下它将作为最终 bean 引用暴露）。
	//
	// <p>默认实现按原样返回给定的 {@code bean}。
	// @param bean 原始 bean 实例
	// @param beanName bean 的名称
	// @return 要作为 bean 引用公开的对象（通常默认为传入的 bean 实例）
	default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
