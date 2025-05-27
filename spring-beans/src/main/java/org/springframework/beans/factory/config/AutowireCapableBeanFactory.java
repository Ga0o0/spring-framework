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

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * Extension of the {@link org.springframework.beans.factory.BeanFactory}
 * interface to be implemented by bean factories that are capable of
 * autowiring, provided that they want to expose this functionality for
 * existing bean instances.
 *
 * <p>This subinterface of BeanFactory is not meant to be used in normal
 * application code: stick to {@link org.springframework.beans.factory.BeanFactory}
 * or {@link org.springframework.beans.factory.ListableBeanFactory} for
 * typical use cases.
 *
 * <p>Integration code for other frameworks can leverage this interface to
 * wire and populate existing bean instances that Spring does not control
 * the lifecycle of. This is particularly useful for WebWork Actions and
 * Tapestry Page objects, for example.
 *
 * <p>Note that this interface is not implemented by
 * {@link org.springframework.context.ApplicationContext} facades,
 * as it is hardly ever used by application code. That said, it is available
 * from an application context too, accessible through ApplicationContext's
 * {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()}
 * method.
 *
 * <p>You may also implement the {@link org.springframework.beans.factory.BeanFactoryAware}
 * interface, which exposes the internal BeanFactory even when running in an
 * ApplicationContext, to get access to an AutowireCapableBeanFactory:
 * simply cast the passed-in BeanFactory to AutowireCapableBeanFactory.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
// 扩展 {@link org.springframework.beans.factory.BeanFactory} 接口，
// 由具有自动装配能力的 Bean 工厂实现，前提是它们希望为现有 Bean 实例公开此功能。
//
// <p>BeanFactory 的这个子接口不适用于普通的应用程序代码：
// 典型用例请坚持使用 {@link org.springframework.beans.factory.BeanFactory}
// 或 {@link org.springframework.beans.factory.ListableBeanFactory}。
//
// <p>其他框架的集成代码可以利用此接口来连接和填充 Spring 不控制其生命周期的现有 Bean 实例。
// 例如，这对于 WebWork Actions 和 Tapestry Page 对象特别有用。
//
// <p>请注意，此接口未由 {@link org.springframework.context.ApplicationContext} 外观实现，因为它几乎从未被应用程序代码使用。
// 也就是说，它也可以从应用程序上下文中使用，通过 ApplicationContext
// 的 {@link org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()} 方法访问。
//
// <p>您还可以实现 {@link org.springframework.beans.factory.BeanFactoryAware} 接口，
// 该接口即使在 ApplicationContext 中运行时也会公开内部 BeanFactory，以访问 AutowireCapableBeanFactory：
// 只需将传入的 BeanFactory 转换为 AutowireCapableBeanFactory。
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * Constant that indicates no externally defined autowiring. Note that
	 * BeanFactoryAware etc and annotation-driven injection will still be applied.
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	// 常量，指示没有外部定义的自动装配。请注意，BeanFactoryAware 等以及注解驱动的注入仍然有效。
	int AUTOWIRE_NO = 0;

	/**
	 * Constant that indicates autowiring bean properties by name
	 * (applying to all bean property setters).
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	// 常量，指示按名称自动装配 Bean 属性（适用于所有 Bean 属性设置器）。
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * Constant that indicates autowiring bean properties by type
	 * (applying to all bean property setters).
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	// 常量，指示按类型自动装配 Bean 属性（适用于所有 Bean 属性设置器）。
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * Constant that indicates autowiring the greediest constructor that
	 * can be satisfied (involves resolving the appropriate constructor).
	 * @see #autowire
	 */
	// 常量，指示自动装配可满足条件的最贪婪构造函数（涉及解析适当的构造函数）。
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * @see #autowire
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * prefer annotation-based autowiring for clearer demarcation of autowiring needs.
	 */
	// 常量，指示通过对 bean 类进行自省来确定合适的自动装配策略。
	// @see #autowire
	// @deprecated 自 Spring 3.0 起：如果您使用混合自动装配策略，请优先使用基于注解的自动装配，以便更清晰地划分自动装配需求。
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;

	/**
	 * Suffix for the "original instance" convention when initializing an existing
	 * bean instance: to be appended to the fully-qualified bean class name,
	 * e.g. "com.mypackage.MyClass.ORIGINAL", in order to enforce the given instance
	 * to be returned, i.e. no proxies etc.
	 * @since 5.1
	 * @see #initializeBean(Object, String)
	 * @see #applyBeanPostProcessorsBeforeInitialization(Object, String)
	 * @see #applyBeanPostProcessorsAfterInitialization(Object, String)
	 */
	// 初始化现有 bean 实例时“原始实例”约定的后缀：附加到完全限定的 bean 类名之后，
	// 例如"com.mypackage.MyClass.ORIGINAL"，用于强制返回给定的实例，即不使用代理等。
	String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";


	//-------------------------------------------------------------------------
	// Typical methods for creating and populating external bean instances --> 译文：创建和填充外部 bean 实例的典型方法
	//-------------------------------------------------------------------------

	/**
	 * Fully create a new bean instance of the given class.
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}.
	 * <p>Note: This is intended for creating a fresh instance, populating annotated
	 * fields and methods as well as applying all standard bean initialization callbacks.
	 * Constructor resolution is based on Kotlin primary / single public / single non-public,
	 * with a fallback to the default constructor in ambiguous scenarios, also influenced
	 * by {@link SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors}
	 * (e.g. for annotation-driven constructor selection).
	 * @param beanClass the class of the bean to create
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 */
	// 完全创建给定类的新 Bean 实例。
	// <p>执行 Bean 的完整初始化，包括所有适用的 {@link BeanPostProcessor BeanPostProcessors}。
	// <p>注意：这旨在创建一个新的实例，填充带注解的字段和方法，以及应用所有标准的 Bean 初始化回调。
	// 构造函数解析基于 Kotlin 的 primary / single public / single non-public 构造函数，在不明确的情况下会回退到默认构造函数，
	// 也受 {@link SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors} 的影响（例如，用于注解驱动的构造函数选择）。
	// @param beanClass 要创建的 Bean 的类
	// @return 新的 Bean 实例
	// @throws BeansException（如果实例化或装配失败）
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * Populate the given bean instance through applying after-instantiation callbacks
	 * and bean property post-processing (e.g. for annotation-driven injection).
	 * <p>Note: This is essentially intended for (re-)populating annotated fields and
	 * methods, either for new instances or for deserialized instances. It does
	 * <i>not</i> imply traditional by-name or by-type autowiring of properties;
	 * use {@link #autowireBeanProperties} for those purposes.
	 * @param existingBean the existing bean instance
	 * @throws BeansException if wiring failed
	 */
	// 通过应用实例化后回调和 Bean 属性后处理（例如，用于注解驱动的注入）来填充给定的 Bean 实例。
	// <p>注意：这主要用于（重新）填充带注解的字段和方法，无论是用于新实例还是反序列化实例。
	// 它<i>不</i>意味着传统的按名称或按类型自动装配属性；请使用 {@link #autowireBeanProperties} 来实现这些目的。
	// @param existingBean 现有的 Bean 实例
	// @throws BeansException 如果装配失败
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * Configure the given raw bean: autowiring bean properties, applying
	 * bean property values, applying factory callbacks such as {@code setBeanName}
	 * and {@code setBeanFactory}, and also applying all bean post processors
	 * (including ones which might wrap the given raw bean).
	 * <p>This is effectively a superset of what {@link #initializeBean} provides,
	 * fully applying the configuration specified by the corresponding bean definition.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (a bean definition of that name has to be available)
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name
	 * @throws BeansException if the initialization failed
	 * @see #initializeBean
	 */
	// 配置给定的原始 Bean：自动装配 Bean 属性，应用 Bean 属性值，应用工厂回调（例如 {@code setBeanName} 和 {@code setBeanFactory}），
	// 以及应用所有 Bean 后处理器（包括可能包装给定原始 Bean 的处理器）。
	// <p>这实际上是 {@link #initializeBean} 提供的功能的超集，完全应用相应 bean 定义指定的配置。
	// <b>注意：此方法需要指定名称的 bean 定义！</b>
	// @param existingBean 现有的 bean 实例
	// @param beanName bean 的名称，必要时会传递给它（必须存在该名称的 bean 定义）
	// @return 要使用的 bean 实例，可以是原始实例或包装后的实例
	// @throws org.springframework.beans.factory.NoSuchBeanDefinitionException（如果不存在指定名称的 bean 定义）
	// @throws BeansException（如果初始化失败）
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle --> 译文：用于对 bean 生命周期进行细粒度控制的专用方法
	//-------------------------------------------------------------------------

	/**
	 * Fully create a new bean instance of the given class with the specified
	 * autowire strategy. All constants defined in this interface are supported here.
	 * <p>Performs full initialization of the bean, including all applicable
	 * {@link BeanPostProcessor BeanPostProcessors}. This is effectively a superset
	 * of what {@link #autowire} provides, adding {@link #initializeBean} behavior.
	 * @param beanClass the class of the bean to create
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @deprecated as of 6.1, in favor of {@link #createBean(Class)}
	 */
	// 使用指定的自动装配策略完全创建给定类的新 bean 实例。此处支持此接口中定义的所有常量。
	// <p>执行 bean 的完整初始化，包括所有适用的 {@link BeanPostProcessor BeanPostProcessors}。
	// 这实际上是 {@link #autowire} 提供的功能的超集，添加了 {@link #initializeBean} 行为。
	// @param beanClass 要创建的 bean 的类 @param autowireMode 使用此接口中的常量按名称或类型
	// @param dependencyCheck 是否对对象执行依赖性检查（不适用于自动装配构造函数，因此在此忽略）
	// @return 新的 bean 实例
	// @throws BeansException 如果实例化或装配失败
	@Deprecated(since = "6.1")
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * Instantiate a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * before-instantiation callbacks (e.g. for annotation-driven injection).
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the construction of the instance.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance (not applicable to autowiring a constructor,
	 * thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	// 使用指定的自动装配策略实例化给定类的新 Bean 实例。此接口中定义的所有常量均受支持。
	// 也可以使用 {@code AUTOWIRE_NO} 调用，以便仅应用实例化前的回调（例如，用于注解驱动的注入）。
	// <p>不应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调或执行任何进一步的 Bean 初始化。
	// 此接口为这些目的提供了独特的、细粒度的操作，例如 {@link #initializeBean}。
	// 但是，如果适用于实例的构造，则会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	// @param beanClass 要实例化的 bean 的类 @param autowireMode 按名称或类型，使用此接口中的常量
	// @param dependencyCheck 是否对 bean 实例中的对象引用执行依赖性检查（不适用于自动装配构造函数，因此在那里忽略）
	// @return 新的 bean 实例
	// @throws BeansException 如果实例化或装配失败
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * Autowire the bean properties of the given bean instance by name or type.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * after-instantiation callbacks (e.g. for annotation-driven injection).
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	// 按名称或类型自动装配给定 Bean 实例的 Bean 属性。也可以使用 {@code AUTOWIRE_NO} 调用，以便仅应用实例化后回调（例如，用于注解驱动的注入）。
	// <p>不<i</i>应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调或执行任何进一步的 Bean 初始化。
	// 此接口为这些目的提供了独特的细粒度操作，例如 {@link #initializeBean}。
	// 但是，如果适用于实例的配置，则会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	// @param existingBean 现有 Bean 实例
	// @param autowireMode 按名称或类型，使用此接口中的常量
	// @param dependencyCheck 是否对 Bean 实例中的对象引用执行依赖关系检查
	// @throws BeansException 如果装配失败
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * Apply the property values of the bean definition with the given name to
	 * the given bean instance. The bean definition can either define a fully
	 * self-contained bean, reusing its property values, or just property values
	 * meant to be used for existing bean instances.
	 * <p>This method does <i>not</i> autowire bean properties; it just applies
	 * explicitly defined property values. Use the {@link #autowireBeanProperties}
	 * method to autowire an existing bean instance.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean definition in the bean factory
	 * (a bean definition of that name has to be available)
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name
	 * @throws BeansException if applying the property values failed
	 * @see #autowireBeanProperties
	 */
	// 将指定名称的 bean 定义的属性值应用于指定的 bean 实例。bean 定义可以定义一个完全独立的 bean，重用其属性值，也可以仅定义用于现有 bean 实例的属性值。
	// <p>此方法<i>不会</i>自动装配 bean 属性；它只应用显式定义的属性值。使用 {@link #autowireBeanProperties} 方法自动装配现有 bean 实例。
	// <b>注意：此方法需要指定名称的 bean 定义！</b> <p><i>不会</i>应用标准 {@link BeanPostProcessor BeanPostProcessors} 回调或
	// 对 bean 执行任何进一步的初始化。此接口为此目的提供了独特的细粒度操作，例如 {@link #initializeBean}。
	// 但是，如果适用于实例的配置，则会应用 {@link InstantiationAwareBeanPostProcessor} 回调。
	// @param existingBean 现有的 bean 实例
	// @param beanName bean 工厂中 bean 定义的名称（必须有一个该名称的 bean 定义）
	// @throws org.springframework.beans.factory.NoSuchBeanDefinitionException 如果没有给定名称的 bean 定义
	// @throws BeansException 如果应用属性值失败
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 * Initialize the given raw bean, applying factory callbacks
	 * such as {@code setBeanName} and {@code setBeanFactory},
	 * also applying all bean post processors (including ones which
	 * might wrap the given raw bean).
	 * <p>Note that no bean definition of the given name has to exist
	 * in the bean factory. The passed-in bean name will simply be used
	 * for callbacks but not checked against the registered bean definitions.
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors};
	 * can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
	 * enforce the given instance to be returned, i.e. no proxies etc)
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if the initialization failed
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 */
	// 初始化给定的原始 bean，应用工厂回调函数，例如 {@code setBeanName} 和 {@code setBeanFactory}，
	// 并应用所有 bean 后置处理器（包括可能包装给定原始 bean 的处理器）。
	// <p>请注意，bean 工厂中不必存在给定名称的 bean 定义。传入的 bean 名称将仅用于回调，而不会与已注册的 bean 定义进行检查。
	// @param existingBean 现有 bean 实例
	// @param beanName bean 的名称，必要时传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	// 可以遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定，以强制返回给定实例，即没有代理等）
	// @return 要使用的 bean 实例，可以是原始实例，也可以是包装的实例
	// @throws BeansException 如果初始化失败
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessBeforeInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors};
	 * can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
	 * enforce the given instance to be returned, i.e. no proxies etc)
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 * @deprecated as of 6.1, in favor of implicit post-processing through
	 * {@link #initializeBean(Object, String)}
	 */
	// 将 {@link BeanPostProcessor BeanPostProcessors} 应用于给定的现有 bean 实例，调用其 {@code postProcessBeforeInitialization} 方法。
	// 返回的 bean 实例可能是原始 bean 的包装器。
	// @param existingBean 现有 bean 实例
	// @param beanName bean 的名称，必要时传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	// 可以遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定以强制返回给定实例，即没有代理等）
	// @return 要使用的 bean 实例，原始实例或包装实例
	// @throws BeansException（如果任何后处理失败）
	// @see BeanPostProcessor#postProcessBeforeInitialization
	// @see #ORIGINAL_INSTANCE_SUFFIX @deprecated 自 6.1 起，支持通过 {@link #initializeBean(Object, String)} 进行隐式后处理
	@Deprecated(since = "6.1")
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * Apply {@link BeanPostProcessor BeanPostProcessors} to the given existing bean
	 * instance, invoking their {@code postProcessAfterInitialization} methods.
	 * The returned bean instance may be a wrapper around the original.
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean, to be passed to it if necessary
	 * (only passed to {@link BeanPostProcessor BeanPostProcessors};
	 * can follow the {@link #ORIGINAL_INSTANCE_SUFFIX} convention in order to
	 * enforce the given instance to be returned, i.e. no proxies etc)
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws BeansException if any post-processing failed
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 * @deprecated as of 6.1, in favor of implicit post-processing through
	 * {@link #initializeBean(Object, String)}
	 */
	// 将 {@link BeanPostProcessor BeanPostProcessors} 应用于给定的现有 bean 实例，
	// 调用其 {@code postProcessAfterInitialization} 方法。返回的 bean 实例可能是原始 bean 的包装器。
	// @param existingBean 现有 bean 实例
	// @param beanName bean 的名称，必要时传递给它（仅传递给 {@link BeanPostProcessor BeanPostProcessors}；
	// 可以遵循 {@link #ORIGINAL_INSTANCE_SUFFIX} 约定以强制返回给定实例，即没有代理等）
	// @return 要使用的 bean 实例，原始实例或包装实例 @throws BeansException（如果任何后处理失败）
	// @see BeanPostProcessor#postProcessAfterInitialization
	// @see #ORIGINAL_INSTANCE_SUFFIX
	// @deprecated 自 6.1 起，支持通过 {@link #initializeBean(Object, String)} 进行隐式后处理
	@Deprecated(since = "6.1")
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * Destroy the given bean instance (typically coming from {@link #createBean(Class)}),
	 * applying the {@link org.springframework.beans.factory.DisposableBean} contract as well as
	 * registered {@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}.
	 * <p>Any exception that arises during destruction should be caught
	 * and logged instead of propagated to the caller of this method.
	 * @param existingBean the bean instance to destroy
	 */
	// 销毁给定的 bean 实例（通常来自 {@link #createBean(Class)}），
	// 应用 {@link org.springframework.beans.factory.DisposableBean} 契约以及已注册的 {@link DestructionAwareBeanPostProcessor}。
	// <p>销毁过程中出现的任何异常都应被捕获并记录，而不是传播给此方法的调用者。
	// @param existingBean 要销毁的 bean 实例
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// Delegate methods for resolving injection points --> 译文：解析注入点的委托方法
	//-------------------------------------------------------------------------

	/**
	 * Resolve the bean instance that uniquely matches the given object type, if any,
	 * including its bean name.
	 * <p>This is effectively a variant of {@link #getBean(Class)} which preserves the
	 * bean name of the matching instance.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @return the bean name plus bean instance
	 * @throws NoSuchBeanDefinitionException if no matching bean was found
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found
	 * @throws BeansException if the bean could not be created
	 * @since 4.3.3
	 * @see #getBean(Class)
	 */
	// 解析与给定对象类型（如果有）唯一匹配的 Bean 实例，包括其 Bean 名称。
	// <p>这实际上是 {@link #getBean(Class)} 的一个变体，它保留了匹配实例的 Bean 名称。
	// @param requiredType Bean 必须匹配的类型；可以是接口或超类
	// @return Bean 名称加 Bean 实例
	// @throws NoSuchBeanDefinitionException（如果未找到匹配的 Bean）
	// @throws NoUniqueBeanDefinitionException（如果找到多个匹配的 Bean）
	// @throws BeansException（如果无法创建 Bean）
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	/**
	 * Resolve a bean instance for the given bean name, providing a dependency descriptor
	 * for exposure to target factory methods.
	 * <p>This is effectively a variant of {@link #getBean(String, Class)} which supports
	 * factory methods with an {@link org.springframework.beans.factory.InjectionPoint}
	 * argument.
	 * @param name the name of the bean to look up
	 * @param descriptor the dependency descriptor for the requesting injection point
	 * @return the corresponding bean instance
	 * @throws NoSuchBeanDefinitionException if there is no bean with the specified name
	 * @throws BeansException if the bean could not be created
	 * @since 5.1.5
	 * @see #getBean(String, Class)
	 */
	// 根据给定的 bean 名称解析一个 bean 实例，并提供一个依赖描述符，用于暴露给目标工厂方法。
	// <p>这实际上是 {@link #getBean(String, Class)} 的一个变体，
	// 它支持带有 {@link org.springframework.beans.factory.InjectionPoint} 参数的工厂方法。
	// @param name 要查找的 bean 的名称
	// @param descriptor 请求注入点的依赖描述符
	// @return 相应的 bean 实例
	// @throws NoSuchBeanDefinitionException（如果不存在具有指定名称的 bean）
	// @throws BeansException（如果无法创建 bean）
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * @param descriptor the descriptor for the dependency (field/method/constructor)
	 * @param requestingBeanName the name of the bean which declares the given dependency
	 * @return the resolved object, or {@code null} if none found
	 * @throws NoSuchBeanDefinitionException if no matching bean was found
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found
	 * @throws BeansException if dependency resolution failed for any other reason
	 * @since 2.5
	 * @see #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)
	 */
	// 解析此工厂中定义的 bean 的指定依赖关系。
	// @param descriptor 依赖项的描述符（字段/方法/构造函数）
	// @param requestingBeanName 声明给定依赖项的 bean 的名称
	// @return 已解析的对象，如果未找到，则返回 {@code null}
	// @throws NoSuchBeanDefinitionException 如果未找到匹配的 bean
	// @throws NoUniqueBeanDefinitionException 如果找到多个匹配的 bean
	// @throws BeansException 如果依赖关系解析由于任何其他原因失败
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * @param descriptor the descriptor for the dependency (field/method/constructor)
	 * @param requestingBeanName the name of the bean which declares the given dependency
	 * @param autowiredBeanNames a Set that all names of autowired beans (used for
	 * resolving the given dependency) are supposed to be added to
	 * @param typeConverter the TypeConverter to use for populating arrays and collections
	 * @return the resolved object, or {@code null} if none found
	 * @throws NoSuchBeanDefinitionException if no matching bean was found
	 * @throws NoUniqueBeanDefinitionException if more than one matching bean was found
	 * @throws BeansException if dependency resolution failed for any other reason
	 * @since 2.5
	 * @see DependencyDescriptor
	 */
	// 解析此工厂中定义的 bean 的指定依赖关系。
	// @param descriptor 依赖项的描述符（字段/方法/构造函数）
	// @param requestingBeanName 声明给定依赖项的 bean 的名称
	// @param autowiredBeanNames 一个集合，所有自动装配 bean 的名称（用于解析给定的依赖项）都应该添加到
	// @param typeConverter 用于填充数组和集合的 TypeConverter
	// @return 已解析的对象，如果未找到，则返回 {@code null}
	// @throws NoSuchBeanDefinitionException 如果未找到匹配的 bean
	// @throws NoUniqueBeanDefinitionException 如果找到多个匹配的 bean
	// @throws BeansException 如果依赖关系解析由于任何其他原因失败
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;

}
