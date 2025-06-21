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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

/**
 * Internal class used to evaluate {@link Conditional} annotations.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 4.0
 */
// 用于评估 {@link Conditional} 注释的内部类。
class ConditionEvaluator {

	private final ConditionContextImpl context;


	/**
	 * Create a new {@link ConditionEvaluator} instance.
	 */
	// 创建一个新的 {@link ConditionEvaluator} 实例。
	public ConditionEvaluator(@Nullable BeanDefinitionRegistry registry,
			@Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

		this.context = new ConditionContextImpl(registry, environment, resourceLoader);
	}


	/**
	 * Determine if an item should be skipped based on {@code @Conditional} annotations.
	 * The {@link ConfigurationPhase} will be deduced from the type of item (i.e. a
	 * {@code @Configuration} class will be {@link ConfigurationPhase#PARSE_CONFIGURATION})
	 * @param metadata the meta data
	 * @return if the item should be skipped
	 */
	// 根据 {@code @Conditional} 注解确定是否应跳过某项。{@link ConfigurationPhase} 将根据项的类型推断出来
	// （例如，{@code @Configuration} 类将是 {@link ConfigurationPhase#PARSE_CONFIGURATION}）
	// @param metadata 元数据
	// @return 是否应跳过该项
	public boolean shouldSkip(AnnotatedTypeMetadata metadata) {
		return shouldSkip(metadata, null);
	}

	/**
	 * Determine if an item should be skipped based on {@code @Conditional} annotations.
	 * @param metadata the meta data
	 * @param phase the phase of the call
	 * @return if the item should be skipped
	 */
	// 根据 @Conditional 注解确定是否应跳过某项。
	// @param metadata 元数据
	// @param phase 调用阶段
	// @return 是否应跳过该项
	public boolean shouldSkip(@Nullable AnnotatedTypeMetadata metadata, @Nullable ConfigurationPhase phase) {
		if (metadata == null || !metadata.isAnnotated(Conditional.class.getName())) {
			return false;
		}

		if (phase == null) {
			if (metadata instanceof AnnotationMetadata annotationMetadata &&
					// 被 @Component、@ComponentScan、@Import、@ImportResource、@Bean 标记
					ConfigurationClassUtils.isConfigurationCandidate(annotationMetadata)) {
				return shouldSkip(metadata, ConfigurationPhase.PARSE_CONFIGURATION);
			}
			return shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN);
		}

		// 处理 @Conditional 的 value 属性中的 Condition 子类，并进行初始化，再存储到 conditions
		List<Condition> conditions = new ArrayList<>();
		// 获取 @Conditional 注解的属性 value 中的存储的 Class<? extends Condition>[]
		for (String[] conditionClasses : getConditionClasses(metadata)) {
			for (String conditionClass : conditionClasses) {
				// 将给定的类名，使用类的 “主” 构造函数（对于 Kotlin 类，可能声明了默认参数）或其默认构造函数（对于常规 Java 类，需要标准的无参数设置）来实例化该类。
				Condition condition = getCondition(conditionClass, this.context.getClassLoader());
				conditions.add(condition);
			}
		}

		AnnotationAwareOrderComparator.sort(conditions); // 排序

		for (Condition condition : conditions) {
			ConfigurationPhase requiredPhase = null;
			if (condition instanceof ConfigurationCondition configurationCondition) {
				requiredPhase = configurationCondition.getConfigurationPhase();
			}
			// important :::: invoke Condition#matches()
			if ((requiredPhase == null || requiredPhase == phase) && !condition.matches(this.context, metadata)) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private List<String[]> getConditionClasses(AnnotatedTypeMetadata metadata) {
		// 检索给定类型的所有注解的所有属性（如果有）（即，如果在底层元素上定义，则为直接注解或元注解）。
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Conditional.class.getName(), true);
		Object values = (attributes != null ? attributes.get("value") : null);
		return (List<String[]>) (values != null ? values : Collections.emptyList());
	}

	private Condition getCondition(String conditionClassName, @Nullable ClassLoader classloader) {
		// 将给定的类名解析为 Class 实例。
		Class<?> conditionClass = ClassUtils.resolveClassName(conditionClassName, classloader);
		// 使用类的 “主” 构造函数（对于 Kotlin 类，可能声明了默认参数）或其默认构造函数（对于常规 Java 类，需要标准的无参数设置）来实例化该类。
		return (Condition) BeanUtils.instantiateClass(conditionClass);
	}


	/**
	 * Implementation of a {@link ConditionContext}.
	 */
	// {@link ConditionContext} 的实现。
	private static class ConditionContextImpl implements ConditionContext {

		@Nullable
		private final BeanDefinitionRegistry registry;

		@Nullable
		private final ConfigurableListableBeanFactory beanFactory;

		private final Environment environment;

		private final ResourceLoader resourceLoader;

		@Nullable
		private final ClassLoader classLoader;

		public ConditionContextImpl(@Nullable BeanDefinitionRegistry registry,
				@Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

			this.registry = registry;
			this.beanFactory = deduceBeanFactory(registry);
			this.environment = (environment != null ? environment : deduceEnvironment(registry));
			this.resourceLoader = (resourceLoader != null ? resourceLoader : deduceResourceLoader(registry));
			this.classLoader = deduceClassLoader(resourceLoader, this.beanFactory);
		}

		@Nullable
		private static ConfigurableListableBeanFactory deduceBeanFactory(@Nullable BeanDefinitionRegistry source) {
			if (source instanceof ConfigurableListableBeanFactory configurableListableBeanFactory) {
				return configurableListableBeanFactory;
			}
			if (source instanceof ConfigurableApplicationContext configurableApplicationContext) {
				return configurableApplicationContext.getBeanFactory();
			}
			return null;
		}

		private static Environment deduceEnvironment(@Nullable BeanDefinitionRegistry source) {
			if (source instanceof EnvironmentCapable environmentCapable) {
				return environmentCapable.getEnvironment();
			}
			return new StandardEnvironment();
		}

		private static ResourceLoader deduceResourceLoader(@Nullable BeanDefinitionRegistry source) {
			if (source instanceof ResourceLoader resourceLoader) {
				return resourceLoader;
			}
			return new DefaultResourceLoader();
		}

		@Nullable
		private static ClassLoader deduceClassLoader(@Nullable ResourceLoader resourceLoader,
				@Nullable ConfigurableListableBeanFactory beanFactory) {

			if (resourceLoader != null) {
				ClassLoader classLoader = resourceLoader.getClassLoader();
				if (classLoader != null) {
					return classLoader;
				}
			}
			if (beanFactory != null) {
				return beanFactory.getBeanClassLoader();
			}
			return ClassUtils.getDefaultClassLoader();
		}

		@Override
		public BeanDefinitionRegistry getRegistry() {
			Assert.state(this.registry != null, "No BeanDefinitionRegistry available");
			return this.registry;
		}

		@Override
		@Nullable
		public ConfigurableListableBeanFactory getBeanFactory() {
			return this.beanFactory;
		}

		@Override
		public Environment getEnvironment() {
			return this.environment;
		}

		@Override
		public ResourceLoader getResourceLoader() {
			return this.resourceLoader;
		}

		@Override
		@Nullable
		public ClassLoader getClassLoader() {
			return this.classLoader;
		}
	}

}
