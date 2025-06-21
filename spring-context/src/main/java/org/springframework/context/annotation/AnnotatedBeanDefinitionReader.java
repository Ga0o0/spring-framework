/*
 * Copyright 2002-2024 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Convenient adapter for programmatic registration of bean classes.
 *
 * <p>This is an alternative to {@link ClassPathBeanDefinitionScanner}, applying
 * the same resolution of annotations but for explicitly registered classes only.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 3.0
 * @see AnnotationConfigApplicationContext#register
 */
// 便捷的适配器，用于以编程方式注册 Bean 类。
//
// <p>这是 {@link ClassPathBeanDefinitionScanner} 的替代方案，应用相同的注解解析，但仅适用于显式注册的类。
public class AnnotatedBeanDefinitionReader {

	private final BeanDefinitionRegistry registry;

	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private ConditionEvaluator conditionEvaluator;


	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry.
	 * <p>If the registry is {@link EnvironmentCapable}, e.g. is an {@code ApplicationContext},
	 * the {@link Environment} will be inherited, otherwise a new
	 * {@link StandardEnvironment} will be created and used.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @see #AnnotatedBeanDefinitionReader(BeanDefinitionRegistry, Environment)
	 * @see #setEnvironment(Environment)
	 */
	// 为给定的注册表创建一个新的 {@code AnnotatedBeanDefinitionReader}。
	// <p>如果注册表是 {@link EnvironmentCapable}，例如，是一个 {@code ApplicationContext}，
	// 则将继承 {@link Environment}；否则，将创建并使用一个新的 {@link StandardEnvironment}。
	// @param registry 以 {@code BeanDefinitionRegistry} 的形式加载 bean 定义的 {@code BeanFactory}
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry,
	 * using the given {@link Environment}.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @param environment the {@code Environment} to use when evaluating bean definition
	 * profiles.
	 * @since 3.1
	 */
	// 使用给定的 {@link Environment}，为给定的注册表创建一个新的 {@code AnnotatedBeanDefinitionReader}。
	// @param registry 以 {@code BeanDefinitionRegistry} 的形式加载 bean 定义的 {@code BeanFactory}。
	// @param environment 评估 bean 定义配置文件时使用的 {@code Environment}。
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}


	/**
	 * Get the BeanDefinitionRegistry that this reader operates on.
	 */
	// 获取此读取器所操作的BeanDefinitionRegistry。
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the {@code Environment} to use when evaluating whether
	 * {@link Conditional @Conditional}-annotated component classes should be registered.
	 * <p>The default is a {@link StandardEnvironment}.
	 */
	// 设置在评估是否应注册带有 {@link Conditional @Conditional} 注解的组件类时所使用的 {@code Environment}。
	// <p>默认值为一个 {@link StandardEnvironment}。
	public void setEnvironment(Environment environment) {
		this.conditionEvaluator = new ConditionEvaluator(this.registry, environment, null);
	}

	/**
	 * Set the {@code BeanNameGenerator} to use for detected bean classes.
	 * <p>The default is a {@link AnnotationBeanNameGenerator}.
	 */
	// 设置用于检测到的 bean 类的 {@code BeanNameGenerator}。
	// <p>默认值为一个{@link AnnotationBeanNameGenerator}。
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator =
				(beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * Set the {@code ScopeMetadataResolver} to use for registered component classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 */
	// 设置用于已注册组件类的 {@code ScopeMetadataResolver}。
	// <p>默认值为一个 {@link AnnotationScopeMetadataResolver}。
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}


	/**
	 * Register one or more component classes to be processed.
	 * <p>Calls to {@code register} are idempotent; adding the same
	 * component class more than once has no additional effect.
	 * @param componentClasses one or more component classes,
	 * e.g. {@link Configuration @Configuration} classes
	 */
	// 注册一个或多个要处理的组件类。
	// <p>对 {@code register} 的调用是幂等的；多次添加同一个组件类不会产生任何额外效果。</p>
	// @param componentClasses 一个或多个组件类，例如 {@link Configuration @Configuration} 类
	public void register(Class<?>... componentClasses) {
		for (Class<?> componentClass : componentClasses) {
			registerBean(componentClass);
		}
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 */
	// 从给定的 bean 类注册一个 bean，并从类声明的注解中获取其元数据。
	// @param beanClass bean 的类
	public void registerBean(Class<?> beanClass) {
		doRegisterBean(beanClass, null, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @since 5.2
	 */
	// 从给定的 bean 类注册一个 bean，其元数据来源于类声明的注解。
	// @param beanClass bean 的类
	// @param name bean 的显式名称（或 {@code null} 以生成默认 bean 名称）
	public void registerBean(Class<?> beanClass, @Nullable String name) {
		doRegisterBean(beanClass, name, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	// 从给定的 bean 类注册一个 bean，并从类声明的注解中获取其元数据。
	// @param beanClass bean 的类
	// @param qualifiers 除了 bean 类级别的限定符之外，还要考虑的特定限定符注解
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> beanClass, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(beanClass, null, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	// 从给定的 bean 类注册一个 bean，并从类声明的注解中获取其元数据。
	// @param beanClass bean 的类
	// @param name bean 的显式名称（或 {@code null} 以生成默认 bean 名称）
	// @param qualifiers 除了 bean 类级别的限定符之外，还要考虑的特定限定符注解
	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> beanClass, @Nullable String name,
			Class<? extends Annotation>... qualifiers) {

		doRegisterBean(beanClass, name, qualifiers, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	// 从给定的 bean 类注册一个 bean，其元数据来源于类声明的注解，并使用给定的 supplier 来获取新实例（可能声明为 lambda 表达式或方法引用）。
	// @param beanClass bean 的类
	// @param supplier 用于创建 bean 实例的回调函数（可以为 {@code null}）
	public <T> void registerBean(Class<T> beanClass, @Nullable Supplier<T> supplier) {
		doRegisterBean(beanClass, null, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	// 从给定的 bean 类注册一个 bean，其元数据来源于类声明的注解，并使用给定的 supplier 来获取新实例（可能声明为 lambda 表达式或方法引用）。
	// @param beanClass bean 的类
	// @param name bean 的显式名称（或 {@code null} 以生成默认 bean 名称）
	// @param supplier 用于创建 bean 实例的回调函数（可以为 {@code null}）
	public <T> void registerBean(Class<T> beanClass, @Nullable String name, @Nullable Supplier<T> supplier) {
		doRegisterBean(beanClass, name, null, supplier, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * (or {@code null} for generating a default bean name)
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.2
	 */
	// 从给定的 bean 类注册一个 bean，其元数据源自类声明的注解。
	// @param beanClass bean 的类
	// @param name bean 的显式名称（或 {@code null} 以生成默认 bean 名称）
	// @param supplier 用于创建 bean 实例的回调函数（可以为 {@code null}）
	// @param customizers 用于自定义工厂的 {@link BeanDefinition} 的一个或多个回调函数，例如设置延迟初始化或主标志
	public <T> void registerBean(Class<T> beanClass, @Nullable String name, @Nullable Supplier<T> supplier,
			BeanDefinitionCustomizer... customizers) {

		doRegisterBean(beanClass, name, null, supplier, customizers);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * @param qualifiers specific qualifier annotations to consider, if any,
	 * in addition to qualifiers at the bean class level
	 * @param supplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param customizers one or more callbacks for customizing the factory's
	 * {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 */
	// 从给定的 bean 类注册一个 bean，并从类声明的注解中获取其元数据。
	// @param beanClass bean 的类
	// @param name bean 的显式名称
	// @param qualifiers 除了 bean 类级别的限定符之外，还要考虑哪些特定的限定符注解（如果有）
	// @param supplier 用于创建 bean 实例的回调函数（可以为 {@code null}）
	// @param customizers 用于自定义工厂的 {@link BeanDefinition} 的一个或多个回调函数，例如设置延迟初始化或主标志
	private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
			@Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
			@Nullable BeanDefinitionCustomizer[] customizers) {

		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) { // 根据 @Conditional 评估是否跳过该 bean 的注册
			return;
		}

		// key: org.springframework.context.annotation.ConfigurationClassPostProcessor.candidate, value: Boolean.TRUE
		abd.setAttribute(ConfigurationClassUtils.CANDIDATE_ATTRIBUTE, Boolean.TRUE);
		abd.setInstanceSupplier(supplier);
		// 解析与提供的 Bean definition 对应的 ScopeMetadata
		// 创建一个 ScopeMetadata，然后获取 @Scope 注解的 value、proxyMode 属性并设置给 ScopeMetadata 的 scopeName、scopedProxyMode 属性
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
		// 为给定的 bean 定义生成 bean 名称
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

		// 处理注解 @Lazy/@Primary/@DependsOn/@Role/@Description
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				}
				else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				}
				else {
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
		// 执行 BeanDefinitionCustomizer.customize() 方法
		if (customizers != null) {
			for (BeanDefinitionCustomizer customizer : customizers) {
				customizer.customize(abd);
			}
		}

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		// 应用 ScopeMetadata 的 scopedProxyMode 属性
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		// 注册 BeanDefinition 到 BeanDefinitionRegistry；
		// 执行 BeanDefinitionRegistry.registerBeanDefinition() 和 BeanDefinitionRegistry.registerAlias()
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}


	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable environmentCapable) {
			return environmentCapable.getEnvironment();
		}
		return new StandardEnvironment();
	}

}
