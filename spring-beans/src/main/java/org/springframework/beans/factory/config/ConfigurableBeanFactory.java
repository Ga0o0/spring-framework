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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * Configuration interface to be implemented by most bean factories. Provides
 * facilities to configure a bean factory, in addition to the bean factory
 * client methods in the {@link org.springframework.beans.factory.BeanFactory}
 * interface.
 *
 * <p>This bean factory interface is not meant to be used in normal application
 * code: Stick to {@link org.springframework.beans.factory.BeanFactory} or
 * {@link org.springframework.beans.factory.ListableBeanFactory} for typical
 * needs. This extended interface is just meant to allow for framework-internal
 * plug'n'play and for special access to bean factory configuration methods.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
// 大多数 Bean 工厂都需要实现的配置接口。
// 除了 {@link org.springframework.beans.factory.BeanFactory} 接口中的 Bean 工厂客户端方法之外，还提供了配置 Bean 工厂的功能。
//
// <p>此 Bean 工厂接口不适用于普通应用程序代码：通常情况下，请使用 {@link org.springframework.beans.factory.BeanFactory}
// 或 {@link org.springframework.beans.factory.ListableBeanFactory} 接口。
// 此扩展接口仅用于实现框架内部的即插即用功能以及对 Bean 工厂配置方法的特殊访问。
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	/**
	 * Scope identifier for the standard singleton scope: {@value}.
	 * <p>Custom scopes can be added via {@code registerScope}.
	 * @see #registerScope
	 */
	// 标准单例作用域的标识符：{@value}。
	// <p>可以通过 {@code registerScope} 添加自定义作用域。
	String SCOPE_SINGLETON = "singleton";

	/**
	 * Scope identifier for the standard prototype scope: {@value}.
	 * <p>Custom scopes can be added via {@code registerScope}.
	 * @see #registerScope
	 */
	// 标准原型作用域的标识符：{@value}。
	// <p>可以通过 {@code registerScope} 添加自定义作用域。
	String SCOPE_PROTOTYPE = "prototype";


	/**
	 * Set the parent of this bean factory.
	 * <p>Note that the parent cannot be changed: It should only be set outside
	 * a constructor if it isn't available at the time of factory instantiation.
	 * @param parentBeanFactory the parent BeanFactory
	 * @throws IllegalStateException if this factory is already associated with
	 * a parent BeanFactory
	 * @see #getParentBeanFactory()
	 */
	// 设置此 bean 工厂的父级。
	// <p>请注意，父级无法更改：仅当在工厂实例化时父级不可用时，才应在构造函数外部设置。
	// @param parentBeanFactory 父 BeanFactory
	// @throws IllegalStateException 如果此工厂已与父 BeanFactory 关联
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	/**
	 * Set the class loader to use for loading bean classes.
	 * Default is the thread context class loader.
	 * <p>Note that this class loader will only apply to bean definitions
	 * that do not carry a resolved bean class yet. This is the case as of
	 * Spring 2.0 by default: Bean definitions only carry bean class names,
	 * to be resolved once the factory processes the bean definition.
	 * @param beanClassLoader the class loader to use,
	 * or {@code null} to suggest the default class loader
	 */
	// 设置用于加载 Bean 类的类加载器。默认为线程上下文类加载器。
	// <p>请注意，此类加载器仅适用于尚未包含已解析 Bean 类的 Bean 定义。
	// 从 Spring 2.0 开始，默认情况下是这种情况：Bean 定义仅包含 Bean 类名，在工厂处理 Bean 定义后进行解析。
	// @param beanClassLoader 要使用的类加载器，或 {@code null} 建议使用默认类加载器
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

	/**
	 * Return this factory's class loader for loading bean classes
	 * (only {@code null} if even the system ClassLoader isn't accessible).
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	// 返回此工厂的类加载器，用于加载 Bean 类（如果系统 ClassLoader 也无法访问，则返回 {@code null}）。
	@Nullable
	ClassLoader getBeanClassLoader();

	/**
	 * Specify a temporary ClassLoader to use for type matching purposes.
	 * Default is none, simply using the standard bean ClassLoader.
	 * <p>A temporary ClassLoader is usually just specified if
	 * <i>load-time weaving</i> is involved, to make sure that actual bean
	 * classes are loaded as lazily as possible. The temporary loader is
	 * then removed once the BeanFactory completes its bootstrap phase.
	 * @since 2.5
	 */
	// 指定一个临时的 ClassLoader 用于类型匹配。默认值为 None，即直接使用标准 Bean ClassLoader。
	// <p>通常仅在涉及<i>加载时织入</i>时才指定临时 ClassLoader，以确保实际的 Bean 类尽可能延迟加载。BeanFactory 完成引导阶段后，临时加载器将被移除。
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

	/**
	 * Return the temporary ClassLoader to use for type matching purposes,
	 * if any.
	 * @since 2.5
	 */
	@Nullable
	ClassLoader getTempClassLoader();

	/**
	 * Set whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes. Default is on.
	 * <p>Turn this flag off to enable hot-refreshing of bean definition objects
	 * and in particular bean classes. If this flag is off, any creation of a bean
	 * instance will re-query the bean class loader for newly resolved classes.
	 */
	// 设置是否缓存 bean 元数据，例如给定的 bean 定义（以合并的方式）和已解析的 bean 类。默认开启。
	// <p>关闭此标志可启用 bean 定义对象（尤其是 bean 类）的热刷新。如果关闭此标志，则任何 bean 实例的创建都将重新查询 bean 类加载器以获取新解析的类。
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	/**
	 * Return whether to cache bean metadata such as given bean definitions
	 * (in merged fashion) and resolved bean classes.
	 */
	// 返回是否缓存 bean 元数据，例如给定的 bean 定义（以合并的方式）和已解析的 bean 类。
	boolean isCacheBeanMetadata();

	/**
	 * Specify the resolution strategy for expressions in bean definition values.
	 * <p>There is no expression support active in a BeanFactory by default.
	 * An ApplicationContext will typically set a standard expression strategy
	 * here, supporting "#{...}" expressions in a Unified EL compatible style.
	 * @since 3.0
	 */
	// 指定 Bean 定义值中表达式的解析策略。
	// <p>BeanFactory 默认不启用表达式支持。
	// ApplicationContext 通常会在此处设置标准表达式策略，以兼容 Unified EL 的风格支持 “#{...}” 表达式。
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

	/**
	 * Return the resolution strategy for expressions in bean definition values.
	 * @since 3.0
	 */
	// 返回 Bean 定义值中表达式的解析策略。
	@Nullable
	BeanExpressionResolver getBeanExpressionResolver();

	/**
	 * Specify a {@link ConversionService} to use for converting
	 * property values, as an alternative to JavaBeans PropertyEditors.
	 * @since 3.0
	 */
	// 指定一个 {@link ConversionService} 用于转换属性值，作为 JavaBeans PropertyEditors 的替代方案。
	void setConversionService(@Nullable ConversionService conversionService);

	/**
	 * Return the associated ConversionService, if any.
	 * @since 3.0
	 */
	// 返回关联的 ConversionService（如果有）。
	@Nullable
	ConversionService getConversionService();

	/**
	 * Add a PropertyEditorRegistrar to be applied to all bean creation processes.
	 * <p>Such a registrar creates new PropertyEditor instances and registers them
	 * on the given registry, fresh for each bean creation attempt. This avoids
	 * the need for synchronization on custom editors; hence, it is generally
	 * preferable to use this method instead of {@link #registerCustomEditor}.
	 * @param registrar the PropertyEditorRegistrar to register
	 */
	// 添加一个 PropertyEditorRegistrar 以应用于所有 Bean 创建过程。
	// <p>此类注册器会创建新的 PropertyEditor 实例并将其注册到指定的注册表中，每次创建 Bean 时都会刷新。
	// 这避免了自定义编辑器的同步需求；因此，通常建议使用此方法而不是 {@link #registerCustomEditor}。
	// @param registrar 要注册的 PropertyEditorRegistrar
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	/**
	 * Register the given custom property editor for all properties of the
	 * given type. To be invoked during factory configuration.
	 * <p>Note that this method will register a shared custom editor instance;
	 * access to that instance will be synchronized for thread-safety. It is
	 * generally preferable to use {@link #addPropertyEditorRegistrar} instead
	 * of this method, to avoid for the need for synchronization on custom editors.
	 * @param requiredType type of the property
	 * @param propertyEditorClass the {@link PropertyEditor} class to register
	 */
	// 为指定类型的所有属性注册指定的自定义属性编辑器。该方法将在工厂配置期间调用。
	// <p>请注意，此方法将注册一个共享的自定义编辑器实例；出于线程安全的考虑，对该实例的访问将被同步。
	// 通常，最好使用 {@link #addPropertyEditorRegistrar} 代替此方法，以避免自定义编辑器需要同步。
	// @param requiredType 属性类型
	// @param propertyEditorClass 要注册的 {@link PropertyEditor} 类
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	/**
	 * Initialize the given PropertyEditorRegistry with the custom editors
	 * that have been registered with this BeanFactory.
	 * @param registry the PropertyEditorRegistry to initialize
	 */
	// 使用已在此 BeanFactory 中注册的自定义编辑器初始化给定的 PropertyEditorRegistry。
	// @param registry 要初始化的 PropertyEditorRegistry
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	/**
	 * Set a custom type converter that this BeanFactory should use for converting
	 * bean property values, constructor argument values, etc.
	 * <p>This will override the default PropertyEditor mechanism and hence make
	 * any custom editors or custom editor registrars irrelevant.
	 * @since 2.5
	 * @see #addPropertyEditorRegistrar
	 * @see #registerCustomEditor
	 */
	// 设置此 BeanFactory 应该使用的自定义类型转换器，用于转换 Bean 属性值、构造函数参数值等。
	// <p>这将覆盖默认的 PropertyEditor 机制，从而使任何自定义编辑器或自定义编辑器注册器都不再相关。
	void setTypeConverter(TypeConverter typeConverter);

	/**
	 * Obtain a type converter as used by this BeanFactory. This may be a fresh
	 * instance for each call, since TypeConverters are usually <i>not</i> thread-safe.
	 * <p>If the default PropertyEditor mechanism is active, the returned
	 * TypeConverter will be aware of all custom editors that have been registered.
	 * @since 2.5
	 */
	// 获取此 BeanFactory 使用的类型转换器。每次调用都可能返回一个新实例，因为 TypeConverter 通常<i>不是</i>线程安全的。
	// <p>如果默认的 PropertyEditor 机制处于活动状态，则返回的 TypeConverter 将识别所有已注册的自定义编辑器。
	TypeConverter getTypeConverter();

	/**
	 * Add a String resolver for embedded values such as annotation attributes.
	 * @param valueResolver the String resolver to apply to embedded values
	 * @since 3.0
	 */
	// 为嵌入值（例如注解属性）添加一个字符串解析器。
	// @param valueResolver 要应用于嵌入值的字符串解析器
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * Determine whether an embedded value resolver has been registered with this
	 * bean factory, to be applied through {@link #resolveEmbeddedValue(String)}.
	 * @since 4.3
	 */
	// 判断此 bean 工厂是否已注册嵌入值解析器，以便通过 {@link #resolveEmbeddedValue(String)} 进行应用。
	boolean hasEmbeddedValueResolver();

	/**
	 * Resolve the given embedded value, e.g. an annotation attribute.
	 * @param value the value to resolve
	 * @return the resolved value (may be the original value as-is)
	 * @since 3.0
	 */
	// 解析给定的嵌入值，例如注解属性。
	// @param value 要解析的值
	// @return 解析后的值（可能是原始值）
	@Nullable
	String resolveEmbeddedValue(String value);

	/**
	 * Add a new BeanPostProcessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * <p>Note: Post-processors submitted here will be applied in the order of
	 * registration; any ordering semantics expressed through implementing the
	 * {@link org.springframework.core.Ordered} interface will be ignored. Note
	 * that autodetected post-processors (e.g. as beans in an ApplicationContext)
	 * will always be applied after programmatically registered ones.
	 * @param beanPostProcessor the post-processor to register
	 */
	// 添加一个新的 BeanPostProcessor，它将应用于此工厂创建的 Bean。在工厂配置期间调用。
	// <p>注意：此处提交的后处理器将按照注册的顺序应用；任何通过实现 {@link org.springframework.core.Ordered} 接口表达的排序语义都将被忽略。
	// 请注意，自动检测到的后处理器（例如 ApplicationContext 中的 Bean）将始终在以编程方式注册的后处理器之后应用。
	// @param beanPostProcessor 要注册的后处理器
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Return the current number of registered BeanPostProcessors, if any.
	 */
	// 返回当前已注册的 BeanPostProcessor 数量（如果有）。
	int getBeanPostProcessorCount();

	/**
	 * Register the given scope, backed by the given Scope implementation.
	 * @param scopeName the scope identifier
	 * @param scope the backing Scope implementation
	 */
	// 注册给定的作用域，并由给定的 Scope 实现支持。
	// @param scopeName 作用域标识符
	// @param scope 支持的 Scope 实现
	void registerScope(String scopeName, Scope scope);

	/**
	 * Return the names of all currently registered scopes.
	 * <p>This will only return the names of explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 * @return the array of scope names, or an empty array if none
	 * @see #registerScope
	 */
	// 返回所有当前已注册的作用域的名称。
	// <p>这将仅返回显式注册的作用域的名称。内置作用域（例如“singleton”和“prototype”）将不会公开。
	// @return 作用域名称数组，如果没有，则返回空数组。
	String[] getRegisteredScopeNames();

	/**
	 * Return the Scope implementation for the given scope name, if any.
	 * <p>This will only return explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 * @param scopeName the name of the scope
	 * @return the registered Scope implementation, or {@code null} if none
	 * @see #registerScope
	 */
	// 返回给定作用域名称的 Scope 实现（如果有）。
	// <p>这将仅返回显式注册的作用域。内置作用域（例如“singleton”和“prototype”）将不会公开。
	// @param scopeName 作用域的名称
	// @return 注册的作用域实现，如果没有，则返回 {@code null}
	@Nullable
	Scope getRegisteredScope(String scopeName);

	/**
	 * Set the {@code ApplicationStartup} for this bean factory.
	 * <p>This allows the application context to record metrics during application startup.
	 * @param applicationStartup the new application startup
	 * @since 5.3
	 */
	// 设置此 bean 工厂的 {@code ApplicationStartup}。
	// <p>这允许应用程序上下文在应用程序启动期间记录指标。
	void setApplicationStartup(ApplicationStartup applicationStartup);

	/**
	 * Return the {@code ApplicationStartup} for this bean factory.
	 * @since 5.3
	 */
	// 返回此 bean 工厂的 {@code ApplicationStartup}。
	ApplicationStartup getApplicationStartup();

	/**
	 * Copy all relevant configuration from the given other factory.
	 * <p>Should include all standard configuration settings as well as
	 * BeanPostProcessors, Scopes, and factory-specific internal settings.
	 * Should not include any metadata of actual bean definitions,
	 * such as BeanDefinition objects and bean name aliases.
	 * @param otherFactory the other BeanFactory to copy from
	 */
	// 从给定的其他工厂复制所有相关配置。
	// <p>应包含所有标准配置设置以及 BeanPostProcessor、Scopes 和工厂特定的内部设置。
	// 不应包含任何实际 Bean 定义的元数据，例如 BeanDefinition 对象和 Bean 名称别名。
	// @param otherFactory 要从中复制的另一个 BeanFactory
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	/**
	 * Given a bean name, create an alias. We typically use this method to
	 * support names that are illegal within XML ids (used for bean names).
	 * <p>Typically invoked during factory configuration, but can also be
	 * used for runtime registration of aliases. Therefore, a factory
	 * implementation should synchronize alias access.
	 * @param beanName the canonical name of the target bean
	 * @param alias the alias to be registered for the bean
	 * @throws BeanDefinitionStoreException if the alias is already in use
	 */
	// 给定一个 Bean 名称，创建一个别名。我们通常使用此方法来支持 XML ID（用于 Bean 名称）中非法的名称。
	// <p>通常在工厂配置期间调用，但也可以用于运行时注册别名。因此，工厂实现应该同步别名访问。
	// @param beanName 目标 Bean 的规范名称
	// @param alias 为该 Bean 注册的别名
	// @throws 如果别名已被使用，则抛出 BeanDefinitionStoreException
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	/**
	 * Resolve all alias target names and aliases registered in this
	 * factory, applying the given StringValueResolver to them.
	 * <p>The value resolver may for example resolve placeholders
	 * in target bean names and even in alias names.
	 * @param valueResolver the StringValueResolver to apply
	 * @since 2.5
	 */
	// 解析此工厂中注册的所有别名目标名称和别名，并使用给定的 StringValueResolver 对其进行解析。
	// <p>例如，值解析器可以解析目标 Bean 名称中的占位符，甚至可以解析别名中的占位符。
	// @param valueResolver 需要应用的 StringValueResolver
	void resolveAliases(StringValueResolver valueResolver);

	/**
	 * Return a merged BeanDefinition for the given bean name,
	 * merging a child bean definition with its parent if necessary.
	 * Considers bean definitions in ancestor factories as well.
	 * @param beanName the name of the bean to retrieve the merged definition for
	 * @return a (potentially merged) BeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean definition with the given name
	 * @since 2.5
	 */
	// 返回给定 bean 名称的合并 BeanDefinition，必要时将子 bean 定义与其父级合并。同时考虑祖先工厂中的 bean 定义。
	// @param beanName 要检索合并定义的 bean 的名称
	// @return 给定 bean 的（可能已合并的）BeanDefinition
	// @throws NoSuchBeanDefinitionException（如果不存在具有给定名称的 bean 定义）
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Determine whether the bean with the given name is a FactoryBean.
	 * @param name the name of the bean to check
	 * @return whether the bean is a FactoryBean
	 * ({@code false} means the bean exists but is not a FactoryBean)
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 2.5
	 */
	// 判断具有给定名称的 bean 是否为 FactoryBean。
	// @param name 需要检查的 bean 的名称
	// @return 该 bean 是否为 FactoryBean（{@code false} 表示该 bean 存在但不是 FactoryBean）
	// @throws NoSuchBeanDefinitionException（如果不存在具有给定名称的 bean）
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Explicitly control the current in-creation status of the specified bean.
	 * For container-internal use only.
	 * @param beanName the name of the bean
	 * @param inCreation whether the bean is currently in creation
	 * @since 3.1
	 */
	// 显式控制指定 bean 的当前创建状态。仅供容器内部使用。
	// @param beanName bean 的名称
	// @param inCreation bean 当前是否正在创建中
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	/**
	 * Determine whether the specified bean is currently in creation.
	 * @param beanName the name of the bean
	 * @return whether the bean is currently in creation
	 * @since 2.5
	 */
	// 判断指定的 bean 当前是否正在创建中。
	// @param beanName Bean 的名称
	// @return Bean 当前是否正在创建中
	boolean isCurrentlyInCreation(String beanName);

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 * @param beanName the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 * @since 2.5
	 */
	// 为给定的 Bean 注册一个依赖 Bean，并在给定 Bean 被销毁之前销毁。
	// @param beanName Bean 的名称
	// @param dependentBeanName 依赖 Bean 的名称
	void registerDependentBean(String beanName, String dependentBeanName);

	/**
	 * Return the names of all beans which depend on the specified bean, if any.
	 * @param beanName the name of the bean
	 * @return the array of dependent bean names, or an empty array if none
	 * @since 2.5
	 */
	// 返回所有依赖于指定 Bean 的 Bean 的名称（如果有）。
	// @param beanName Bean 的名称
	// @return 依赖 Bean 名称数组，如果没有，则返回空数组。
	String[] getDependentBeans(String beanName);

	/**
	 * Return the names of all beans that the specified bean depends on, if any.
	 * @param beanName the name of the bean
	 * @return the array of names of beans which the bean depends on,
	 * or an empty array if none
	 * @since 2.5
	 */
	// 返回指定 bean 所依赖的所有 bean 的名称（如果有）。
	// @param beanName bean 的名称
	// @return bean 所依赖 bean 的名称数组，如果没有，则返回空数组。
	String[] getDependenciesForBean(String beanName);

	/**
	 * Destroy the given bean instance (usually a prototype instance
	 * obtained from this factory) according to its bean definition.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * @param beanName the name of the bean definition
	 * @param beanInstance the bean instance to destroy
	 */
	// 根据 bean 的定义，销毁给定的 bean 实例（通常是从此工厂获取的原型实例）。
	// <p>销毁过程中出现的任何异常都应被捕获并记录下来，而不是传播给此方法的调用者。
	// @param beanName bean 定义的名称
	// @param beanInstance 要销毁的 bean 实例
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * Destroy the specified scoped bean in the current target scope, if any.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * @param beanName the name of the scoped bean
	 */
	// 销毁当前目标作用域中指定的 bean（如果有）。
	// <p>任何在销毁过程中发生的异常都应被捕获并记录下来，而不是传播给此方法的调用者。
	// @param beanName 作用域 bean 的名称
	void destroyScopedBean(String beanName);

	/**
	 * Destroy all singleton beans in this factory, including inner beans that have
	 * been registered as disposable. To be called on shutdown of a factory.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 */
	// 销毁此工厂中的所有单例 bean，包括已注册为可释放的内部 bean。在工厂关闭时调用。
	// <p>销毁过程中出现的任何异常都应被捕获并记录下来，而不是传播给此方法的调用者。
	void destroySingletons();

}
