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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Reads a given fully-populated set of ConfigurationClass instances, registering bean
 * definitions with the given {@link BeanDefinitionRegistry} based on its contents.
 *
 * <p>This class was modeled after the {@link BeanDefinitionReader} hierarchy, but does
 * not implement/extend any of its artifacts as a set of configuration classes is not a
 * {@link Resource}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @author Sebastien Deleuze
 * @since 3.0
 * @see ConfigurationClassParser
 */
// 读取给定的一组已完全填充的 ConfigurationClass 实例，并根据其内容将 Bean 定义注册到给定的 {@link BeanDefinitionRegistry}。
//
// <p>此类仿照 {@link BeanDefinitionReader} 层次结构建模，但未实现/扩展其任何构件，因为一组配置类不是 {@link Resource}。
class ConfigurationClassBeanDefinitionReader {

	private static final Log logger = LogFactory.getLog(ConfigurationClassBeanDefinitionReader.class);

	private static final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private final BeanDefinitionRegistry registry;

	private final SourceExtractor sourceExtractor;

	private final ResourceLoader resourceLoader;

	private final Environment environment;

	private final BeanNameGenerator importBeanNameGenerator;

	private final ImportRegistry importRegistry;

	private final ConditionEvaluator conditionEvaluator;


	/**
	 * Create a new {@link ConfigurationClassBeanDefinitionReader} instance
	 * that will be used to populate the given {@link BeanDefinitionRegistry}.
	 */
	// 创建一个新的 {@link ConfigurationClassBeanDefinitionReader} 实例，用于填充给定的 {@link BeanDefinitionRegistry}。
	ConfigurationClassBeanDefinitionReader(BeanDefinitionRegistry registry, SourceExtractor sourceExtractor,
			ResourceLoader resourceLoader, Environment environment, BeanNameGenerator importBeanNameGenerator,
			ImportRegistry importRegistry) {

		this.registry = registry;
		this.sourceExtractor = sourceExtractor;
		this.resourceLoader = resourceLoader;
		this.environment = environment;
		this.importBeanNameGenerator = importBeanNameGenerator;
		this.importRegistry = importRegistry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, resourceLoader);
	}


	/**
	 * Read {@code configurationModel}, registering bean definitions
	 * with the registry based on its contents.
	 */
	// 读取 configurationModel，根据其内容向注册表注册 bean 定义。
	public void loadBeanDefinitions(Set<ConfigurationClass> configurationModel) {
		TrackedConditionEvaluator trackedConditionEvaluator = new TrackedConditionEvaluator();
		for (ConfigurationClass configClass : configurationModel) {
			// 读取特定的 ConfigurationClass，为该类本身及其所有 @Bean 方法注册 BeanDefinition
			loadBeanDefinitionsForConfigurationClass(configClass, trackedConditionEvaluator);
		}
	}

	/**
	 * Read a particular {@link ConfigurationClass}, registering bean definitions
	 * for the class itself and all of its {@link Bean} methods.
	 */
	// 读取特定的 ConfigurationClass，为该类本身及其所有 @Bean 方法注册 BeanDefinition
	private void loadBeanDefinitionsForConfigurationClass(
			ConfigurationClass configClass, TrackedConditionEvaluator trackedConditionEvaluator) {

		// 1. 检查是否应该跳过该 ConfigurationClass；是 -> 从 registry 和 importRegistry 中移除
		if (trackedConditionEvaluator.shouldSkip(configClass)) {
			String beanName = configClass.getBeanName();
			if (StringUtils.hasLength(beanName) && this.registry.containsBeanDefinition(beanName)) {
				// 移除给定名称的 BeanDefinition。
				this.registry.removeBeanDefinition(beanName);
			}
			this.importRegistry.removeImportingClass(configClass.getMetadata().getClassName());
			return;
		}

		// 2. 如果是通过 @Import 注册 -> 将 @Configuration 类本身注册为 BeanDefinition
		if (configClass.isImported()) {
			// 将 @Configuration 类本身注册为 BeanDefinition。
			registerBeanDefinitionForImportedConfigurationClass(configClass);
		}

		// 3. 处理 ConfigurationClass 中被 @Bean 标记的方法
		for (BeanMethod beanMethod : configClass.getBeanMethods()) {
			// 读取给定的 BeanMethod，根据其内容向 BeanDefinitionRegistry 注册 bean 定义。
			loadBeanDefinitionsForBeanMethod(beanMethod);
		}

		// 4. 处理 @ImportResource
		loadBeanDefinitionsFromImportedResources(configClass.getImportedResources());
		// 5. 处理 @Import 中 ImportBeanDefinitionRegistrar 的子类
		loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
	}

	/**
	 * Register the {@link Configuration} class itself as a bean definition.
	 */
	// 将 {@link Configuration} 类本身注册为 bean 定义。
	private void registerBeanDefinitionForImportedConfigurationClass(ConfigurationClass configClass) {
		AnnotationMetadata metadata = configClass.getMetadata();
		AnnotatedGenericBeanDefinition configBeanDef = new AnnotatedGenericBeanDefinition(metadata);

		// 创建一个 ScopeMetadata，然后获取 @Scope 注解的 value、proxyMode 属性并设置给 ScopeMetadata 的 scopeName、scopedProxyMode 属性
		ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(configBeanDef);
		configBeanDef.setScope(scopeMetadata.getScopeName());
		// 为给定的 BeanDefinition 生成 bean 名称
		String configBeanName = this.importBeanNameGenerator.generateBeanName(configBeanDef, this.registry);
		// 处理注解 @Lazy/@Primary/@DependsOn/@Role/@Description
		AnnotationConfigUtils.processCommonDefinitionAnnotations(configBeanDef, metadata);

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(configBeanDef, configBeanName);
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		// 注册 BeanDefinition 到注册表
		this.registry.registerBeanDefinition(definitionHolder.getBeanName(), definitionHolder.getBeanDefinition());
		configClass.setBeanName(configBeanName);

		if (logger.isTraceEnabled()) {
			logger.trace("Registered bean definition for imported class '" + configBeanName + "'");
		}
	}

	/**
	 * Read the given {@link BeanMethod}, registering bean definitions
	 * with the BeanDefinitionRegistry based on its contents.
	 */
	// 读取给定的 {@link BeanMethod}，根据其内容向 BeanDefinitionRegistry 注册 bean 定义。
	private void loadBeanDefinitionsForBeanMethod(BeanMethod beanMethod) {
		ConfigurationClass configClass = beanMethod.getConfigurationClass();
		MethodMetadata metadata = beanMethod.getMetadata();
		String methodName = metadata.getMethodName();

		// Do we need to mark the bean as skipped by its condition?  --> 译文：我们是否需要根据其条件将 bean 标记为已跳过？
		// 1. 根据 @Conditional 确定是否跳过该方法
		if (this.conditionEvaluator.shouldSkip(metadata, ConfigurationPhase.REGISTER_BEAN)) {
			configClass.skippedBeanMethods.add(methodName);
			return;
		}
		if (configClass.skippedBeanMethods.contains(methodName)) {
			return;
		}

		// 2. 获取 @Bean 注解的属性
		AnnotationAttributes bean = AnnotationConfigUtils.attributesFor(metadata, Bean.class);
		Assert.state(bean != null, "No @Bean annotation attributes"); // 没有 @Bean 注释属性

		// 3. 处理 @Bean 注解的 name 属性
		// Consider name and any aliases --> 译文：考虑名称和任何别名
		List<String> names = new ArrayList<>(Arrays.asList(bean.getStringArray("name")));
		// names 不为空使用 names[0]（name属性的第一个分隔值），反之使用 methodName（方法名）
		String beanName = (!names.isEmpty() ? names.remove(0) : methodName);

		// Register aliases even when overridden --> 译文：即使被覆盖，也要注册别名
		for (String alias : names) {
			this.registry.registerAlias(beanName, alias); // 将 names 的所有值注册为该 bean 的别名
		}

		// Has this effectively been overridden before (e.g. via XML)? --> 译文：这之前是否被有效覆盖过（例如通过 XML）？
		if (isOverriddenByExistingDefinition(beanMethod, beanName)) {
			if (beanName.equals(beanMethod.getConfigurationClass().getBeanName())) {
				// 异常信息：从 @Bean 方法 “beanMethod.getMetadata().getMethodName()” 派生的 Bean 名称与包含配置类的 Bean 名称冲突；请确保这些名称唯一！
				throw new BeanDefinitionStoreException(beanMethod.getConfigurationClass().getResource().getDescription(),
						beanName, "Bean name derived from @Bean method '" + beanMethod.getMetadata().getMethodName() +
						"' clashes with bean name for containing configuration class; please make those names unique!");
			}
			return;
		}

		// 4. 创建 ConfigurationClassBeanDefinition
		ConfigurationClassBeanDefinition beanDef = new ConfigurationClassBeanDefinition(configClass, metadata, beanName);
		beanDef.setSource(this.sourceExtractor.extractSource(metadata, configClass.getResource()));

		// 5. 处理 static @Bean 方法
		if (metadata.isStatic()) {
			// static @Bean method
			if (configClass.getMetadata() instanceof StandardAnnotationMetadata sam) {
				beanDef.setBeanClass(sam.getIntrospectedClass());
			}
			else {
				beanDef.setBeanClassName(configClass.getMetadata().getClassName());
			}
			beanDef.setUniqueFactoryMethodName(methodName); 	// 设置工厂方法名
		}
		// 5. 处理 instance @Bean 方法
		else {
			// instance @Bean method
			beanDef.setFactoryBeanName(configClass.getBeanName());	// 设置工厂类
			beanDef.setUniqueFactoryMethodName(methodName);			// 设置工厂方法名
		}

		if (metadata instanceof StandardMethodMetadata sam) {
			beanDef.setResolvedFactoryMethod(sam.getIntrospectedMethod());
		}

		// 6. 设置 BeanDefinition 的相关属性 -> 通过 @Bean 的属性和 @Bean 方法上的注解
		beanDef.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);		// 设置自动装配模式
		// 处理注解 @Lazy/@Primary/@DependsOn/@Role/@Description
		AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDef, metadata);

		boolean autowireCandidate = bean.getBoolean("autowireCandidate"); // @Bean.autowireCandidate
		if (!autowireCandidate) {
			beanDef.setAutowireCandidate(false);
		}

		String initMethodName = bean.getString("initMethod"); // @Bean.initMethod
		if (StringUtils.hasText(initMethodName)) {
			beanDef.setInitMethodName(initMethodName);
		}

		String destroyMethodName = bean.getString("destroyMethod"); // @Bean.destroyMethod
		beanDef.setDestroyMethodName(destroyMethodName);

		// 7. 处理 @Scope 注解
		// Consider scoping
		ScopedProxyMode proxyMode = ScopedProxyMode.NO;
		AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(metadata, Scope.class); // 返回 @Scope 的属性列表
		if (attributes != null) {
			beanDef.setScope(attributes.getString("value"));
			proxyMode = attributes.getEnum("proxyMode");
			if (proxyMode == ScopedProxyMode.DEFAULT) {
				proxyMode = ScopedProxyMode.NO;
			}
		}

		// 代理模式下 BeanDefinition 的处理
		// Replace the original bean definition with the target one, if necessary --> 译文：如果需要，用目标 bean 定义替换原始 bean 定义
		BeanDefinition beanDefToRegister = beanDef;
		if (proxyMode != ScopedProxyMode.NO) {
			// 为提供的目标 bean 生成一个作用域代理
			BeanDefinitionHolder proxyDef = ScopedProxyCreator.createScopedProxy(
					new BeanDefinitionHolder(beanDef, beanName), this.registry,
					proxyMode == ScopedProxyMode.TARGET_CLASS);
			beanDefToRegister = new ConfigurationClassBeanDefinition(
					(RootBeanDefinition) proxyDef.getBeanDefinition(), configClass, metadata, beanName);
		}

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Registering bean definition for @Bean method %s.%s()",
					configClass.getMetadata().getClassName(), beanName));
		}
		// 8. 注册 BeanDefinition 到注册表
		this.registry.registerBeanDefinition(beanName, beanDefToRegister);
	}

	protected boolean isOverriddenByExistingDefinition(BeanMethod beanMethod, String beanName) {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return false;
		}
		BeanDefinition existingBeanDef = this.registry.getBeanDefinition(beanName);

		// Is the existing bean definition one that was created from a configuration class?
		// -> allow the current bean method to override, since both are at second-pass level.
		// However, if the bean method is an overloaded case on the same configuration class,
		// preserve the existing bean definition.
		// --> 译文：现有的 bean 定义是否由配置类创建？如果是，则允许当前 bean 方法重写，因为两者都处于第二遍处理阶段。
		// 但是，如果 bean 方法是同一配置类的重载版本，则保留现有的 bean 定义。
		if (existingBeanDef instanceof ConfigurationClassBeanDefinition ccbd) {
			if (ccbd.getMetadata().getClassName().equals(
					beanMethod.getConfigurationClass().getMetadata().getClassName())) {
				if (ccbd.getFactoryMethodMetadata().getMethodName().equals(ccbd.getFactoryMethodName())) {
					ccbd.setNonUniqueFactoryMethodName(ccbd.getFactoryMethodMetadata().getMethodName());
				}
				return true;
			}
			else {
				return false;
			}
		}

		// A bean definition resulting from a component scan can be silently overridden
		// by an @Bean method - and as of 6.1, even when general overriding is disabled
		// as long as the bean class is the same.
		// --> 译文：通过组件扫描得到的 bean 定义可以被 @Bean 方法静默覆盖 —— 从 6.1 版本开始，即使禁用了常规覆盖，只要 bean 类相同，也可以这样做。
		if (existingBeanDef instanceof ScannedGenericBeanDefinition scannedBeanDef) {
			if (beanMethod.getMetadata().getReturnTypeName().equals(scannedBeanDef.getBeanClassName())) {
				this.registry.removeBeanDefinition(beanName);
			}
			return false;
		}

		// Has the existing bean definition bean marked as a framework-generated bean?
		// -> allow the current bean method to override it, since it is application-level
		// --> 译文：现有 bean 定义是否标记为框架生成的 bean？-> 允许当前 bean 方法覆盖它，因为它属于应用层。
		if (existingBeanDef.getRole() > BeanDefinition.ROLE_APPLICATION) {
			return false;
		}

		// At this point, it's a top-level override (probably XML), just having been parsed
		// before configuration class processing kicks in... --> 译文：目前，这是一个顶层覆盖（可能是 XML），只是在配置类处理开始之前就已经被解析过了 ...
		if (!this.registry.isBeanDefinitionOverridable(beanName)) {
			throw new BeanDefinitionStoreException(beanMethod.getConfigurationClass().getResource().getDescription(),
					beanName, "@Bean definition illegally overridden by existing bean definition: " + existingBeanDef);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Skipping bean definition for %s: a definition for bean '%s' " +
					"already exists. This top-level bean definition is considered as an override.",
					beanMethod, beanName));
		}
		return true;
	}

	private void loadBeanDefinitionsFromImportedResources(
			Map<String, Class<? extends BeanDefinitionReader>> importedResources) {

		Map<Class<?>, BeanDefinitionReader> readerInstanceCache = new HashMap<>();

		importedResources.forEach((resource, readerClass) -> {
			// Default reader selection necessary? --> 译文：是否需要选择默认 reader？
			if (BeanDefinitionReader.class == readerClass) {
				if (StringUtils.endsWithIgnoreCase(resource, ".groovy")) {
					// When clearly asking for Groovy, that's what they'll get... --> 译文：当明确要求 Groovy 时，他们就会得到它......
					readerClass = GroovyBeanDefinitionReader.class;
				}
				else {
					// Primarily ".xml" files but for any other extension as well --> 译文：主要为 “.xml” 文件，但也适用于任何其他扩展名
					readerClass = XmlBeanDefinitionReader.class;
				}
			}

			BeanDefinitionReader reader = readerInstanceCache.get(readerClass);
			if (reader == null) {
				try {
					// Instantiate the specified BeanDefinitionReader --> 译文：实例化指定的 BeanDefinitionReader
					reader = readerClass.getConstructor(BeanDefinitionRegistry.class).newInstance(this.registry);
					// Delegate the current ResourceLoader to it if possible --> 译文：如果可能，将当前的 ResourceLoader 委托给它
					if (reader instanceof AbstractBeanDefinitionReader abdr) {
						abdr.setResourceLoader(this.resourceLoader);
						abdr.setEnvironment(this.environment);
					}
					readerInstanceCache.put(readerClass, reader);
				}
				catch (Throwable ex) {
					// 无法实例化 BeanDefinitionReader 类“readerClass.getName()”
					throw new IllegalStateException(
							"Could not instantiate BeanDefinitionReader class [" + readerClass.getName() + "]");
				}
			}

			// TODO SPR-6310: qualify relative path locations as done in AbstractContextLoader.modifyLocations
			// TODO SPR-6310：限定相对路径位置，如在 AbstractContextLoader.modifyLocations 中所做的那样。
			reader.loadBeanDefinitions(resource); // 从指定的资源位置加载 bean 定义。
		});
	}

	private void loadBeanDefinitionsFromRegistrars(Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> registrars) {
		// invoke ImportBeanDefinitionRegistrar.registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry, BeanNameGenerator)
		registrars.forEach((registrar, metadata) ->
				registrar.registerBeanDefinitions(metadata, this.registry, this.importBeanNameGenerator));
	}


	/**
	 * {@link RootBeanDefinition} marker subclass used to signify that a bean definition
	 * was created from a configuration class as opposed to any other configuration source.
	 * Used in bean overriding cases where it's necessary to determine whether the bean
	 * definition was created externally.
	 */
	// {@link RootBeanDefinition} 标记子类，用于指示 bean 定义是从配置类创建的，而不是从其他配置源创建的。
	// 用于 bean 覆盖的情况，需要确定 bean 定义是否由外部创建。
	@SuppressWarnings("serial")
	private static class ConfigurationClassBeanDefinition extends RootBeanDefinition implements AnnotatedBeanDefinition {

		private final AnnotationMetadata annotationMetadata;

		private final MethodMetadata factoryMethodMetadata;

		private final String derivedBeanName;

		public ConfigurationClassBeanDefinition(
				ConfigurationClass configClass, MethodMetadata beanMethodMetadata, String derivedBeanName) {

			this.annotationMetadata = configClass.getMetadata();
			this.factoryMethodMetadata = beanMethodMetadata;
			this.derivedBeanName = derivedBeanName;
			setResource(configClass.getResource());
			setLenientConstructorResolution(false);
		}

		public ConfigurationClassBeanDefinition(RootBeanDefinition original,
				ConfigurationClass configClass, MethodMetadata beanMethodMetadata, String derivedBeanName) {

			super(original);
			this.annotationMetadata = configClass.getMetadata();
			this.factoryMethodMetadata = beanMethodMetadata;
			this.derivedBeanName = derivedBeanName;
		}

		private ConfigurationClassBeanDefinition(ConfigurationClassBeanDefinition original) {
			super(original);
			this.annotationMetadata = original.annotationMetadata;
			this.factoryMethodMetadata = original.factoryMethodMetadata;
			this.derivedBeanName = original.derivedBeanName;
		}

		@Override
		public AnnotationMetadata getMetadata() {
			return this.annotationMetadata;
		}

		@Override
		@NonNull
		public MethodMetadata getFactoryMethodMetadata() {
			return this.factoryMethodMetadata;
		}

		@Override
		public boolean isFactoryMethod(Method candidate) {
			return (super.isFactoryMethod(candidate) && BeanAnnotationHelper.isBeanAnnotated(candidate) &&
					BeanAnnotationHelper.determineBeanNameFor(candidate).equals(this.derivedBeanName));
		}

		@Override
		public ConfigurationClassBeanDefinition cloneBeanDefinition() {
			return new ConfigurationClassBeanDefinition(this);
		}
	}


	/**
	 * Evaluate {@code @Conditional} annotations, tracking results and taking into
	 * account 'imported by'.
	 */
	// 评估 {@code @Conditional} 注解，跟踪结果并考虑“导入”。
	private class TrackedConditionEvaluator {

		private final Map<ConfigurationClass, Boolean> skipped = new HashMap<>();

		public boolean shouldSkip(ConfigurationClass configClass) {
			Boolean skip = this.skipped.get(configClass);
			if (skip == null) {
				if (configClass.isImported()) {
					boolean allSkipped = true;
					for (ConfigurationClass importedBy : configClass.getImportedBy()) {
						if (!shouldSkip(importedBy)) {
							allSkipped = false;
							break;
						}
					}
					if (allSkipped) {
						// The config classes that imported this one were all skipped, therefore we are skipped...
						// --> 译文：导入这个的配置类都被跳过了，因此我们被跳过了......
						skip = true;
					}
				}
				if (skip == null) {
					// 根据 @Conditional 注解确定是否应跳过某项。
					skip = conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN);
				}
				this.skipped.put(configClass, skip);
			}
			return skip;
		}
	}

}
