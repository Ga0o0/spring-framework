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

package org.springframework.context.annotation;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Conventions;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Utilities for identifying and configuring {@link Configuration} classes.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Stephane Nicoll
 * @since 6.0
 */
// 用于识别和配置 {@link Configuration} 类的实用程序。
public abstract class ConfigurationClassUtils {

	static final String CONFIGURATION_CLASS_FULL = "full";

	static final String CONFIGURATION_CLASS_LITE = "lite";

	/**
	 * When set to {@link Boolean#TRUE}, this attribute signals that the bean class
	 * for the given {@link BeanDefinition} should be considered as a candidate
	 * configuration class in 'lite' mode by default.
	 * <p>For example, a class registered directly with an {@code ApplicationContext}
	 * should always be considered a configuration class candidate.
	 * @since 6.0.10
	 */
	// 当设置为 {@link Boolean#TRUE} 时，此属性表示给定 {@link BeanDefinition} 的 bean 类应默认被视为 lite 模式下的候选配置类。
	// <p>例如，直接使用 {@code ApplicationContext} 注册的类应始终被视为配置类候选。
	// org.springframework.context.annotation.ConfigurationClassPostProcessor.candidate
	static final String CANDIDATE_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "candidate");

	// org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass
	static final String CONFIGURATION_CLASS_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "configurationClass");

	// org.springframework.context.annotation.ConfigurationClassPostProcessor.order
	static final String ORDER_ATTRIBUTE =
			Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor.class, "order");


	private static final Log logger = LogFactory.getLog(ConfigurationClassUtils.class);

	private static final Set<String> candidateIndicators = Set.of(
			Component.class.getName(),
			ComponentScan.class.getName(),
			Import.class.getName(),
			ImportResource.class.getName());


	/**
	 * Initialize a configuration class proxy for the specified class.
	 * @param userClass the configuration class to initialize
	 */
	// 为指定类初始化配置类代理。
	// @param userClass 需要初始化的配置类
	@SuppressWarnings("unused") // Used by AOT-optimized generated code --> 译文：由 AOT 优化生成的代码使用
	public static Class<?> initializeConfigurationClass(Class<?> userClass) {
		Class<?> configurationClass = new ConfigurationClassEnhancer().enhance(userClass, null);
		Enhancer.registerStaticCallbacks(configurationClass, ConfigurationClassEnhancer.CALLBACKS);
		return configurationClass;
	}


	/**
	 * Check whether the given bean definition is a candidate for a configuration class
	 * (or a nested component class declared within a configuration/component class,
	 * to be auto-registered as well), and mark it accordingly.
	 * @param beanDef the bean definition to check
	 * @param metadataReaderFactory the current factory in use by the caller
	 * @return whether the candidate qualifies as (any kind of) configuration class
	 */
	// 检查给定的 bean 定义是否为 configuration 类（或 configuration/component 类中声明的嵌套组件类，也将被自动注册），并进行相应的标记。
	// @param beanDef 待检查的 bean 定义
	// @param metadataReaderFactory 调用者当前使用的工厂
	// @return 候选对象是否符合（任何类型的）配置类的条件
	static boolean checkConfigurationClassCandidate(
			BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {

		// ConfigurationClassUtils.checkConfigurationClassCandidate() 执行的三件重要的事：
		// a. 类被 @Configuration 注解标记 并且 @Configuration.proxyBeanMethods = true，
		// 		在该 BeanDefinition 中添加属性 org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass = full
		// b. 类被注解 @Configuration(proxyBeanMethods = false)、@Component、@ComponentScan、@Import、@ImportResource 标记 或 类中存在方法被 @Bean 标记
		// 		在该 BeanDefinition 中添加属性 org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass = lite
		// c. 处理 @Order 注解，
		// 		并将其 value 值添加到属性 org.springframework.context.annotation.ConfigurationClassPostProcessor.order 中

		String className = beanDef.getBeanClassName();
		if (className == null || beanDef.getFactoryMethodName() != null) {
			return false;
		}

		AnnotationMetadata metadata;
		if (beanDef instanceof AnnotatedBeanDefinition annotatedBd &&
				className.equals(annotatedBd.getMetadata().getClassName())) {
			// Can reuse the pre-parsed metadata from the given BeanDefinition... --> 译文：可以重用给定 BeanDefinition 的预解析元数据...
			metadata = annotatedBd.getMetadata();
		}
		else if (beanDef instanceof AbstractBeanDefinition abstractBd && abstractBd.hasBeanClass()) {
			// Check already loaded Class if present...
			// since we possibly can't even load the class file for this Class. --> 译文：检查已加载的类（如果存在）... 	因为我们可能无法加载该类的类文件。
			Class<?> beanClass = abstractBd.getBeanClass();
			// BeanFactoryPostProcessor & BeanPostProcessor & AopInfrastructureBean & EventListenerFactory
			if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass) ||
					BeanPostProcessor.class.isAssignableFrom(beanClass) ||
					AopInfrastructureBean.class.isAssignableFrom(beanClass) ||
					EventListenerFactory.class.isAssignableFrom(beanClass)) {
				return false;
			}
			metadata = AnnotationMetadata.introspect(beanClass);
		}
		else {
			try {
				// 获取给定类名的 MetadataReader。
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
				metadata = metadataReader.getAnnotationMetadata();
			}
			catch (IOException ex) {
				if (logger.isDebugEnabled()) {
					// 找不到用于自检配置注释的类文件
					logger.debug("Could not find class file for introspecting configuration annotations: " +
							className, ex);
				}
				return false;
			}
		}

		// 检索给定类型的注解的属性（如果有）（即，如果在底层元素上定义，则为直接注解或元注解）。
		Map<String, Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
		if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
			// 设置 CONFIGURATION_CLASS_ATTRIBUTE 属性：org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass = full
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
		}
		else if (config != null ||
				// org.springframework.context.annotation.ConfigurationClassPostProcessor.candidate = true
				Boolean.TRUE.equals(beanDef.getAttribute(CANDIDATE_ATTRIBUTE)) ||
				// 类被注解 @Component、@ComponentScan、@Import、@ImportResource 标记 或 类中存在方法被 @Bean 标记
				isConfigurationCandidate(metadata)) {
			// 设置 CONFIGURATION_CLASS_ATTRIBUTE 属性：org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass = lite
			beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
		}
		else {
			return false;
		}

		// It's a full or lite configuration candidate... Let's determine the order value, if any. --> 译文：它是一个完整或精简配置候选... 让我们确定排序值（如果有）。
		Integer order = getOrder(metadata);	// 处理 @Order 注解
		if (order != null) {
			// ORDER_ATTRIBUTE -> org.springframework.context.annotation.ConfigurationClassPostProcessor.order
			beanDef.setAttribute(ORDER_ATTRIBUTE, order);
		}

		return true;
	}

	/**
	 * Check the given metadata for a configuration class candidate
	 * (or nested component class declared within a configuration/component class).
	 * @param metadata the metadata of the annotated class
	 * @return {@code true} if the given class is to be registered for
	 * configuration class processing; {@code false} otherwise
	 */
	// 检查给定的元数据中是否存在配置类候选（或在 configuration/component 类中声明的嵌套组件类）。
	// @param metadata 带注解类的元数据
	// @return {@code true} 如果给定的类要注册用于配置类处理；{@code false} 否则
	static boolean isConfigurationCandidate(AnnotationMetadata metadata) {
		// 类被注解 @Component、@ComponentScan、@Import、@ImportResource 标记 或 类中存在方法被 @Bean 标记

		// Do not consider an interface or an annotation... --> 译文：不要考虑接口或注释......
		if (metadata.isInterface()) {
			return false;
		}

		// Any of the typical annotations found? --> 译文：发现任何典型的注释吗？
		for (String indicator : candidateIndicators) {
			// candidateIndicators == @Component、@ComponentScan、@Import、@ImportResource
			if (metadata.isAnnotated(indicator)) {
				return true;
			}
		}

		// Finally, let's look for @Bean methods... --> 译文：最后，让我们寻找 @Bean 方法...
		return hasBeanMethods(metadata); // 确定底层类是否具有被 @Bean 注解的方法
	}

	static boolean hasBeanMethods(AnnotationMetadata metadata) {
		try {
			// 确定底层类是否具有被 @Bean 注解的方法
			return metadata.hasAnnotatedMethods(Bean.class.getName());
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to introspect @Bean methods on class [" + metadata.getClassName() + "]: " + ex);
			}
			return false;
		}
	}

	/**
	 * Determine the order for the given configuration class metadata.
	 * @param metadata the metadata of the annotated class
	 * @return the {@code @Order} annotation value on the configuration class,
	 * or {@code Ordered.LOWEST_PRECEDENCE} if none declared
	 * @since 5.0
	 */
	// 确定给定配置类元数据的顺序。
	// @param metadata 带注解类的元数据
	// @return 配置类上的 {@code @Order} 注解值，如果未声明，则返回 {@code Ordered.LOWEST_PRECEDENCE}
	@Nullable
	public static Integer getOrder(AnnotationMetadata metadata) {
		Map<String, Object> orderAttributes = metadata.getAnnotationAttributes(Order.class.getName());
		return (orderAttributes != null ? ((Integer) orderAttributes.get(AnnotationUtils.VALUE)) : null);
	}

	/**
	 * Determine the order for the given configuration class bean definition,
	 * as set by {@link #checkConfigurationClassCandidate}.
	 * @param beanDef the bean definition to check
	 * @return the {@link Order @Order} annotation value on the configuration class,
	 * or {@link Ordered#LOWEST_PRECEDENCE} if none declared
	 * @since 4.2
	 */
	// 确定给定配置类 bean 定义的顺序，由 {@link #checkConfigurationClassCandidate} 设置。
	// @param beanDef 需要检查的 bean 定义
	// @return 配置类上的 {@link Order @Order} 注解值，如果未声明，则返回 {@link Ordered#LOWEST_PRECEDENCE}。
	public static int getOrder(BeanDefinition beanDef) {
		Integer order = (Integer) beanDef.getAttribute(ORDER_ATTRIBUTE);
		return (order != null ? order : Ordered.LOWEST_PRECEDENCE);
	}

}
