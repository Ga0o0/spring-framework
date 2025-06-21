/*
 * Copyright 2002-2025 the original author or authors.
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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Represents a user-defined {@link Configuration @Configuration} class.
 * <p>Includes a set of {@link Bean} methods, including all such methods
 * defined in the ancestry of the class, in a 'flattened-out' manner.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 3.0
 * @see BeanMethod
 * @see ConfigurationClassParser
 */
// 表示用户定义的 {@link Configuration @Configuration} 类。
// <p>以“扁平化”的方式包含一组 {@link Bean} 方法，其中包括在该类的祖先中定义的所有此类方法。
final class ConfigurationClass {

	private final AnnotationMetadata metadata;

	private final Resource resource;

	@Nullable
	private String beanName;

	private final Set<ConfigurationClass> importedBy = new LinkedHashSet<>(1);

	private final Set<BeanMethod> beanMethods = new LinkedHashSet<>();

	private final Map<String, Class<? extends BeanDefinitionReader>> importedResources =
			new LinkedHashMap<>();

	private final Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> importBeanDefinitionRegistrars =
			new LinkedHashMap<>();

	final Set<String> skippedBeanMethods = new HashSet<>();


	/**
	 * Create a new {@link ConfigurationClass} with the given name.
	 * @param metadataReader reader used to parse the underlying {@link Class}
	 * @param beanName must not be {@code null}
	 */
	// 使用给定名称创建一个新的 {@link ConfigurationClass}。
	// @param metadataReader 用于解析底层 {@link Class} 的读取器
	// @param beanName 不能为 {@code null}
	ConfigurationClass(MetadataReader metadataReader, String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = metadataReader.getAnnotationMetadata();
		this.resource = metadataReader.getResource();
		this.beanName = beanName;
	}

	/**
	 * Create a new {@link ConfigurationClass} representing a class that was imported
	 * using the {@link Import} annotation or automatically processed as a nested
	 * configuration class (if importedBy is not {@code null}).
	 * @param metadataReader reader used to parse the underlying {@link Class}
	 * @param importedBy the configuration class importing this one
	 * @since 3.1.1
	 */
	ConfigurationClass(MetadataReader metadataReader, ConfigurationClass importedBy) {
		this.metadata = metadataReader.getAnnotationMetadata();
		this.resource = metadataReader.getResource();
		this.importedBy.add(importedBy);
	}

	/**
	 * Create a new {@link ConfigurationClass} with the given name.
	 * @param clazz the underlying {@link Class} to represent
	 * @param beanName name of the {@code @Configuration} class bean
	 */
	// 使用给定名称创建一个新的 {@link ConfigurationClass}。
	// @param clazz 表示底层 {@link Class}
	// @param beanName {@code @Configuration} 类 bean 的名称
	ConfigurationClass(Class<?> clazz, String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = AnnotationMetadata.introspect(clazz);
		this.resource = new DescriptiveResource(clazz.getName());
		this.beanName = beanName;
	}

	/**
	 * Create a new {@link ConfigurationClass} representing a class that was imported
	 * using the {@link Import} annotation or automatically processed as a nested
	 * configuration class (if imported is {@code true}).
	 * @param clazz the underlying {@link Class} to represent
	 * @param importedBy the configuration class importing this one
	 * @since 3.1.1
	 */
	ConfigurationClass(Class<?> clazz, ConfigurationClass importedBy) {
		this.metadata = AnnotationMetadata.introspect(clazz);
		this.resource = new DescriptiveResource(clazz.getName());
		this.importedBy.add(importedBy);
	}

	/**
	 * Create a new {@link ConfigurationClass} with the given name.
	 * @param metadata the metadata for the underlying class to represent
	 * @param beanName name of the {@code @Configuration} class bean
	 */
	// 使用给定名称创建一个新的 {@link ConfigurationClass}。
	// @param metadata 表示底层类的元数据
	// @param beanName {@code @Configuration} 类 bean 的名称
	ConfigurationClass(AnnotationMetadata metadata, String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		this.metadata = metadata;
		this.resource = new DescriptiveResource(metadata.getClassName());
		this.beanName = beanName;
	}


	AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	Resource getResource() {
		return this.resource;
	}

	String getSimpleName() {
		return ClassUtils.getShortName(getMetadata().getClassName());
	}

	void setBeanName(@Nullable String beanName) {
		this.beanName = beanName;
	}

	@Nullable
	String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return whether this configuration class was registered via @{@link Import} or
	 * automatically registered due to being nested within another configuration class.
	 * @since 3.1.1
	 * @see #getImportedBy()
	 */
	// 返回此配置类是通过 @{@link Import} 注册的还是由于嵌套在另一个配置类中而自动注册的。
	boolean isImported() {
		return !this.importedBy.isEmpty();
	}

	/**
	 * Merge the imported-by declarations from the given configuration class into this one.
	 * @since 4.0.5
	 */
	// 将给定配置类中的导入声明合并到此配置类中。
	void mergeImportedBy(ConfigurationClass otherConfigClass) {
		this.importedBy.addAll(otherConfigClass.importedBy);
	}

	/**
	 * Return the configuration classes that imported this class,
	 * or an empty Set if this configuration was not imported.
	 * @since 4.0.5
	 * @see #isImported()
	 */
	Set<ConfigurationClass> getImportedBy() {
		return this.importedBy;
	}

	void addBeanMethod(BeanMethod method) {
		this.beanMethods.add(method);
	}

	Set<BeanMethod> getBeanMethods() {
		return this.beanMethods;
	}

	boolean hasNonStaticBeanMethods() {
		for (BeanMethod beanMethod : this.beanMethods) {
			if (!beanMethod.getMetadata().isStatic()) {
				return true;
			}
		}
		return false;
	}

	void addImportedResource(String importedResource, Class<? extends BeanDefinitionReader> readerClass) {
		this.importedResources.put(importedResource, readerClass);
	}

	Map<String, Class<? extends BeanDefinitionReader>> getImportedResources() {
		return this.importedResources;
	}

	void addImportBeanDefinitionRegistrar(ImportBeanDefinitionRegistrar registrar, AnnotationMetadata importingClassMetadata) {
		this.importBeanDefinitionRegistrars.put(registrar, importingClassMetadata);
	}

	Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> getImportBeanDefinitionRegistrars() {
		return this.importBeanDefinitionRegistrars;
	}

	void validate(ProblemReporter problemReporter) {
		Map<String, Object> attributes = this.metadata.getAnnotationAttributes(Configuration.class.getName());

		// A configuration class may not be final (CGLIB limitation) unless it declares proxyBeanMethods=false
		// --> 译文：配置类可能不是最终的（CGLIB 限制），除非它声明 proxyBeanMethods=false
		if (attributes != null && (Boolean) attributes.get("proxyBeanMethods")) {
			if (hasNonStaticBeanMethods() && this.metadata.isFinal()) {
				problemReporter.error(new FinalConfigurationProblem());
			}
			for (BeanMethod beanMethod : this.beanMethods) {
				beanMethod.validate(problemReporter);
			}
		}

		// A configuration class may not contain overloaded bean methods unless it declares enforceUniqueMethods=false
		// --> 译文：配置类不能包含重载的 bean 方法，除非它声明了 enforceUniqueMethods=false
		if (attributes != null && (Boolean) attributes.get("enforceUniqueMethods")) {
			Map<String, MethodMetadata> beanMethodsByName = new LinkedHashMap<>();
			for (BeanMethod beanMethod : this.beanMethods) {
				MethodMetadata current = beanMethod.getMetadata();
				MethodMetadata existing = beanMethodsByName.put(current.getMethodName(), current);
				if (existing != null && existing.getDeclaringClassName().equals(current.getDeclaringClassName())) {
					problemReporter.error(new BeanMethodOverloadingProblem(existing.getMethodName()));
				}
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof ConfigurationClass that &&
				getMetadata().getClassName().equals(that.getMetadata().getClassName())));
	}

	@Override
	public int hashCode() {
		return getMetadata().getClassName().hashCode();
	}

	@Override
	public String toString() {
		return "ConfigurationClass: beanName '" + this.beanName + "', " + this.resource;
	}


	/**
	 * Configuration classes must be non-final to accommodate CGLIB subclassing.
	 */
	private class FinalConfigurationProblem extends Problem {

		FinalConfigurationProblem() {
			super(String.format("@Configuration class '%s' may not be final. Remove the final modifier to continue.",
					getSimpleName()), new Location(getResource(), getMetadata()));
		}
	}


	/**
	 * Configuration classes are not allowed to contain overloaded bean methods
	 * by default (as of 6.0).
	 */
	private class BeanMethodOverloadingProblem extends Problem {

		BeanMethodOverloadingProblem(String methodName) {
			super(String.format("@Configuration class '%s' contains overloaded @Bean methods with name '%s'. Use " +
							"unique method names for separate bean definitions (with individual conditions etc) " +
							"or switch '@Configuration.enforceUniqueMethods' to 'false'.",
					getSimpleName(), methodName), new Location(getResource(), getMetadata()));
		}
	}

}
