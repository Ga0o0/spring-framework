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

package org.springframework.aop.framework.autoproxy.target;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Convenient superclass for
 * {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator}
 * implementations that require creating multiple instances of a prototype bean.
 *
 * <p>Uses an internal BeanFactory to manage the target instances,
 * copying the original bean definition to this internal factory.
 * This is necessary because the original BeanFactory will just
 * contain the proxy instance created through auto-proxying.
 *
 * <p>Requires running in an
 * {@link org.springframework.beans.factory.support.AbstractBeanFactory}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
// 方便的 {@link org.springframework.aop.framework.autoproxy.TargetSourceCreator} 实现的超类，用于创建原型 bean 的多个实例。
//
// <p>使用内部 BeanFactory 来管理目标实例，并将原始 bean 定义复制到此内部工厂。这是必要的，因为原始 BeanFactory 只包含通过自动代理创建的代理实例。
//
// <p>需要在 {@link org.springframework.beans.factory.support.AbstractBeanFactory} 中运行。
public abstract class AbstractBeanFactoryBasedTargetSourceCreator
		implements TargetSourceCreator, BeanFactoryAware, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private ConfigurableBeanFactory beanFactory;

	/** Internally used DefaultListableBeanFactory instances, keyed by bean name. */
	private final Map<String, DefaultListableBeanFactory> internalBeanFactories = new HashMap<>();


	@Override
	public final void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ConfigurableBeanFactory clbf)) {
			throw new IllegalStateException("Cannot do auto-TargetSource creation with a BeanFactory " +
					"that doesn't implement ConfigurableBeanFactory: " + beanFactory.getClass());
		}
		this.beanFactory = clbf;
	}

	/**
	 * Return the BeanFactory that this TargetSourceCreators runs in.
	 */
	@Nullable
	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	private ConfigurableBeanFactory getConfigurableBeanFactory() {
		Assert.state(this.beanFactory != null, "BeanFactory not set");
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of the TargetSourceCreator interface
	//---------------------------------------------------------------------

	@Override
	@Nullable
	public final TargetSource getTargetSource(Class<?> beanClass, String beanName) {
		AbstractBeanFactoryBasedTargetSource targetSource =
				createBeanFactoryBasedTargetSource(beanClass, beanName);
		if (targetSource == null) {
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Configuring AbstractBeanFactoryBasedTargetSource: " + targetSource);
		}

		DefaultListableBeanFactory internalBeanFactory = getInternalBeanFactoryForBean(beanName);

		// We need to override just this bean definition, as it may reference other beans
		// and we're happy to take the parent's definition for those.
		// Always use prototype scope if demanded.
		// --> 译文：我们只需要覆盖这个 bean 定义，因为它可能引用其他 bean，并且我们很乐意采用父级的定义。如果需要，请始终使用原型范围。
		BeanDefinition bd = getConfigurableBeanFactory().getMergedBeanDefinition(beanName);
		GenericBeanDefinition bdCopy = new GenericBeanDefinition(bd);
		if (isPrototypeBased()) {
			bdCopy.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		}
		internalBeanFactory.registerBeanDefinition(beanName, bdCopy);

		// Complete configuring the PrototypeTargetSource. --> 译文：完成 PrototypeTargetSource 的配置。
		targetSource.setTargetBeanName(beanName);
		targetSource.setBeanFactory(internalBeanFactory);

		return targetSource;
	}

	/**
	 * Return the internal BeanFactory to be used for the specified bean.
	 * @param beanName the name of the target bean
	 * @return the internal BeanFactory to be used
	 */
	// 返回指定 bean 所使用的内部 BeanFactory。
	// @param beanName 目标 bean 的名称
	// @return 待使用的内部 BeanFactory
	protected DefaultListableBeanFactory getInternalBeanFactoryForBean(String beanName) {
		synchronized (this.internalBeanFactories) {
			return this.internalBeanFactories.computeIfAbsent(beanName,
					name -> buildInternalBeanFactory(getConfigurableBeanFactory()));
		}
	}

	/**
	 * Build an internal BeanFactory for resolving target beans.
	 * @param containingFactory the containing BeanFactory that originally defines the beans
	 * @return an independent internal BeanFactory to hold copies of some target beans
	 */
	// 构建一个内部 BeanFactory 用于解析目标 Bean。
	// @param containingFactory 最初定义 Bean 的包含 BeanFactory
	// @return 一个独立的内部 BeanFactory，用于保存部分目标 Bean 的副本
	protected DefaultListableBeanFactory buildInternalBeanFactory(ConfigurableBeanFactory containingFactory) {
		// Set parent so that references (up container hierarchies) are correctly resolved.
		// --> 译文：设置父级，以便正确解析引用（向上容器层次结构）。
		DefaultListableBeanFactory internalBeanFactory = new DefaultListableBeanFactory(containingFactory);

		// Required so that all BeanPostProcessors, Scopes, etc become available.
		// --> 译文：必需，以便所有 BeanPostProcessors、Scopes 等均可用。
		internalBeanFactory.copyConfigurationFrom(containingFactory);

		// Filter out BeanPostProcessors that are part of the AOP infrastructure,
		// since those are only meant to apply to beans defined in the original factory.
		// --> 译文：过滤掉属于 AOP 基础架构的 BeanPostProcessors，因为它们仅适用于原始工厂中定义的 Bean。
		internalBeanFactory.getBeanPostProcessors().removeIf(beanPostProcessor ->
				beanPostProcessor instanceof AopInfrastructureBean);

		return internalBeanFactory;
	}

	/**
	 * Destroys the internal BeanFactory on shutdown of the TargetSourceCreator.
	 * @see #getInternalBeanFactoryForBean
	 */
	@Override
	public void destroy() {
		synchronized (this.internalBeanFactories) {
			for (DefaultListableBeanFactory bf : this.internalBeanFactories.values()) {
				bf.destroySingletons();
			}
		}
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Return whether this TargetSourceCreator is prototype-based.
	 * The scope of the target bean definition will be set accordingly.
	 * <p>Default is "true".
	 * @see org.springframework.beans.factory.config.BeanDefinition#isSingleton()
	 */
	protected boolean isPrototypeBased() {
		return true;
	}

	/**
	 * Subclasses must implement this method to return a new AbstractPrototypeBasedTargetSource
	 * if they wish to create a custom TargetSource for this bean, or {@code null} if they are
	 * not interested it in, in which case no special target source will be created.
	 * Subclasses should not call {@code setTargetBeanName} or {@code setBeanFactory}
	 * on the AbstractPrototypeBasedTargetSource: This class' implementation of
	 * {@code getTargetSource()} will do that.
	 * @param beanClass the class of the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @return the AbstractPrototypeBasedTargetSource, or {@code null} if we don't match this
	 */
	// 如果子类希望为该 bean 创建自定义 TargetSource，则必须实现此方法以返回新的 AbstractPrototypeBasedTargetSource；
	// 如果子类对此不感兴趣，则返回 {@code null}，在这种情况下不会创建任何特殊的目标源。
	// 子类不应在 AbstractPrototypeBasedTargetSource 上调用 {@code setTargetBeanName} 或 {@code setBeanFactory}：
	// 此类的 {@code getTargetSource()} 实现将执行这些操作。@param beanClass 为该 bean 创建 TargetSource 的 bean 类
	// @param beanName 为 bean 的名称
	// @return AbstractPrototypeBasedTargetSource，如果我们不匹配，则返回 {@code null}
	@Nullable
	protected abstract AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class<?> beanClass, String beanName);

}
