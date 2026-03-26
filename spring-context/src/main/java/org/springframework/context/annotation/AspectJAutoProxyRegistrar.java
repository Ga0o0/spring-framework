/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Registers an {@link org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 * AnnotationAwareAspectJAutoProxyCreator} against the current {@link BeanDefinitionRegistry}
 * as appropriate based on a given @{@link EnableAspectJAutoProxy} annotation.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableAspectJAutoProxy
 */
// 根据给定的 @{@link EnableAspectJAutoProxy} 注释，根据当前 {@link BeanDefinitionRegistry}
// 注册一个 {@link org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator AnnotationAwareAspectJAutoProxyCreator}。
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * Register, escalate, and configure the AspectJ auto proxy creator based on the value
	 * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
	 * {@code @Configuration} class.
	 */
	// 根据导入 {@code @Configuration} 类上的 @{@link EnableAspectJAutoProxy#proxyTargetClass()} 属性的值注册、升级和配置 AspectJ 自动代理创建器。
	@Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		// 1. 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AnnotationAwareAspectJAutoProxyCreator
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		// 2. 获取 @EnableAspectJAutoProxy 注解的属性（proxyTargetClass 和 exposeProxy）并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例
		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			// 获取属性值 proxyTargetClass 的值，并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例的 PropertyValues#proxyTargetClass
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			// 获取属性值 exposeProxy 的值，并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例的 PropertyValues#exposeProxy
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

}
