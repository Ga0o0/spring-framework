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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * The root interface for accessing a Spring bean container.
 *
 * <p>This is the basic client view of a bean container;
 * further interfaces such as {@link ListableBeanFactory} and
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * are available for specific purposes.
 *
 * <p>This interface is implemented by objects that hold a number of bean definitions,
 * each uniquely identified by a String name. Depending on the bean definition,
 * the factory will return either an independent instance of a contained object
 * (the Prototype design pattern), or a single shared instance (a superior
 * alternative to the Singleton design pattern, in which the instance is a
 * singleton in the scope of the factory). Which type of instance will be returned
 * depends on the bean factory configuration: the API is the same. Since Spring
 * 2.0, further scopes are available depending on the concrete application
 * context (e.g. "request" and "session" scopes in a web environment).
 *
 * <p>The point of this approach is that the BeanFactory is a central registry
 * of application components, and centralizes configuration of application
 * components (no more do individual objects need to read properties files,
 * for example). See chapters 4 and 11 of "Expert One-on-One J2EE Design and
 * Development" for a discussion of the benefits of this approach.
 *
 * <p>Note that it is generally better to rely on Dependency Injection
 * ("push" configuration) to configure application objects through setters
 * or constructors, rather than use any form of "pull" configuration like a
 * BeanFactory lookup. Spring's Dependency Injection functionality is
 * implemented using this BeanFactory interface and its subinterfaces.
 *
 * <p>Normally a BeanFactory will load bean definitions stored in a configuration
 * source (such as an XML document), and use the {@code org.springframework.beans}
 * package to configure the beans. However, an implementation could simply return
 * Java objects it creates as necessary directly in Java code. There are no
 * constraints on how the definitions could be stored: LDAP, RDBMS, XML,
 * properties file, etc. Implementations are encouraged to support references
 * amongst beans (Dependency Injection).
 *
 * <p>In contrast to the methods in {@link ListableBeanFactory}, all of the
 * operations in this interface will also check parent factories if this is a
 * {@link HierarchicalBeanFactory}. If a bean is not found in this factory instance,
 * the immediate parent factory will be asked. Beans in this factory instance
 * are supposed to override beans of the same name in any parent factory.
 *
 * <p>Bean factory implementations should support the standard bean lifecycle interfaces
 * as far as possible. The full set of initialization methods and their standard order is:
 * <ol>
 * <li>BeanNameAware's {@code setBeanName}
 * <li>BeanClassLoaderAware's {@code setBeanClassLoader}
 * <li>BeanFactoryAware's {@code setBeanFactory}
 * <li>EnvironmentAware's {@code setEnvironment}
 * <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
 * <li>ResourceLoaderAware's {@code setResourceLoader}
 * (only applicable when running in an application context)
 * <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
 * (only applicable when running in an application context)
 * <li>MessageSourceAware's {@code setMessageSource}
 * (only applicable when running in an application context)
 * <li>ApplicationContextAware's {@code setApplicationContext}
 * (only applicable when running in an application context)
 * <li>ServletContextAware's {@code setServletContext}
 * (only applicable when running in a web application context)
 * <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
 * <li>InitializingBean's {@code afterPropertiesSet}
 * <li>a custom {@code init-method} definition
 * <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
 * </ol>
 *
 * <p>On shutdown of a bean factory, the following lifecycle methods apply:
 * <ol>
 * <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
 * <li>DisposableBean's {@code destroy}
 * <li>a custom {@code destroy-method} definition
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.EnvironmentAware#setEnvironment
 * @see org.springframework.context.EmbeddedValueResolverAware#setEmbeddedValueResolver
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
// 用于访问 Spring bean 容器的根接口。
//
// <p>这是 bean 容器的基本客户端视图；
// 其他接口，例如 {@link ListableBeanFactory} 和 {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}，可用于特定用途。
//
// <p>此接口由包含多个 bean 定义的对象实现，每个 bean 定义都由一个 String 名称唯一标识。
// 根据 bean 定义，工厂将返回一个包含对象的独立实例（原型设计模式），或一个共享实例（单例设计模式的更优替代方案，在单例模式中，实例在工厂作用域内是单例）。
// 返回哪种类型的实例取决于 bean 工厂配置：API 相同。从 Spring 2.0 开始，根据具体的应用上下文，可以使用更多作用域（例如，在 Web 环境中，可以使用“请求”和“会话”作用域）。
//
// <p>这种方法的重点在于，BeanFactory 是应用程序组件的中央注册表，并集中管理应用程序组件的配置（例如，单个对象不再需要读取属性文件）。
// 有关此方法的优势，请参阅《Expert One-on-One J2EE 设计和开发》的第 4 章和第 11 章。
//
// <p>请注意，通常最好依靠依赖注入（“推送”配置）通过 setter 或构造函数来配置应用程序对象，而不是使用任何形式的“拉”配置（例如 BeanFactory 查找）。
// Spring 的依赖注入功能就是使用此 BeanFactory 接口及其子接口实现的。
//
// <p>通常，BeanFactory 会加载存储在配置源（例如 XML 文档）中的 Bean 定义，并使用 {@code org.springframework.beans} 包来配置 Bean。
// 但是，实现也可以直接在 Java 代码中返回它根据需要创建的 Java 对象。
// 对于定义的存储方式没有任何限制：LDAP、RDBMS、XML、属性文件等。鼓励实现支持 bean 之间的引用（依赖注入）。
//
// <p>与 {@link ListableBeanFactory} 中的方法不同，如果此接口是 {@link HierarchicalBeanFactory}，则此接口中的所有操作还会检查父工厂。
// 如果在此工厂实例中未找到 bean，则会询问其直接父工厂。此工厂实例中的 bean 应该覆盖任何父工厂中同名的 bean。
//
// <p>Bean 工厂实现应尽可能支持标准 bean 生命周期接口。完整的初始化方法及其标准顺序如下：
// <ol>
// <li>BeanNameAware 的 {@code setBeanName}
// <li>BeanClassLoaderAware 的 {@code setBeanClassLoader}
// <li>BeanFactoryAware 的 {@code setBeanFactory}
// <li>EnvironmentAware 的 {@code setEnvironment}
// <li>EmbeddedValueResolverAware 的 {@code setEmbeddedValueResolver}
// <li>ResourceLoaderAware 的 {@code setResourceLoader}（仅在应用程序上下文中运行时适用）
// <li>ApplicationEventPublisherAware 的 {@code setApplicationEventPublisher} （仅在应用程序上下文中运行时适用）
// <li>MessageSourceAware 的 {@code setMessageSource} （仅在应用程序上下文中运行时适用）
// <li>ApplicationContextAware 的{@code setApplicationContext} （仅在应用程序上下文中运行时适用）
// <li>ServletContextAware 的 {@code setServletContext} （仅在 Web 应用程序上下文中运行时适用）
// <li>BeanPostProcessors 的 {@code postProcessBeforeInitialization} 方法
// <li>InitializingBean 的 {@code afterPropertiesSet} 方法
// <li>自定义的 {@code init-method} 方法
// <li>BeanPostProcessors 的 {@code postProcessAfterInitialization} 方法
// </ol>
//
// <p>Bean 工厂关闭时，以下生命周期方法适用：
// <ol>
// <li>DestructionAwareBeanPostProcessors 的 {@code postProcessBeforeDestruction} 方法
// <li>DisposableBean 的 {@code destroy} 方法
// <li>自定义的 {@code destroy-method} 方法
// </ol>
public interface BeanFactory {

	/**
	 * Used to dereference a {@link FactoryBean} instance and distinguish it from
	 * beans <i>created</i> by the FactoryBean. For example, if the bean named
	 * {@code myJndiObject} is a FactoryBean, getting {@code &myJndiObject}
	 * will return the factory, not the instance returned by the factory.
	 */
	// 用于取消引用 {@link FactoryBean} 实例，并将其与 FactoryBean <i>创建的</i> Bean 区分开来。
	// 例如，如果名为 {@code myJndiObject} 的 Bean 是 FactoryBean，则获取 {@code &myJndiObject} 将返回工厂，而不是工厂返回的实例。
	String FACTORY_BEAN_PREFIX = "&";


	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>This method allows a Spring BeanFactory to be used as a replacement for the
	 * Singleton or Prototype design pattern. Callers may retain references to
	 * returned objects in the case of Singleton beans.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to retrieve
	 * @return an instance of the bean.
	 * Note that the return value will never be {@code null} but possibly a stub for
	 * {@code null} returned from a factory method, to be checked via {@code equals(null)}.
	 * Consider using {@link #getBeanProvider(Class)} for resolving optional dependencies.
	 * @throws NoSuchBeanDefinitionException if there is no bean with the specified name
	 * @throws BeansException if the bean could not be obtained
	 */
	// 返回指定 bean 的实例，该实例可以是共享的，也可以是独立的。
	// <p>此方法允许使用 Spring BeanFactory 替代 Singleton 或 Prototype 设计模式。对于 Singleton bean，调用者可以保留对返回对象的引用。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到该 bean，则会询问父工厂。
	// @param name 要检索的 bean 的名称
	// @return 该 bean 的实例。
	// 请注意，返回值永远不会为 {@code null}，而可能是工厂方法返回的 {@code null} 的存根，需要通过 {@code equals(null)} 进行检查。
	// 考虑使用 {@link #getBeanProvider(Class)} 来解析可选依赖项。
	// @throws NoSuchBeanDefinitionException 如果不存在指定名称的 Bean，则抛出 NoSuchBeanDefinitionException
	// @throws BeansException 如果无法获取 Bean，则抛出 BeansException
	Object getBean(String name) throws BeansException;

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Behaves the same as {@link #getBean(String)}, but provides a measure of type
	 * safety by throwing a BeanNotOfRequiredTypeException if the bean is not of the
	 * required type. This means that ClassCastException can't be thrown on casting
	 * the result correctly, as can happen with {@link #getBean(String)}.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to retrieve
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @return an instance of the bean.
	 * Note that the return value will never be {@code null}. In case of a stub for
	 * {@code null} from a factory method having been resolved for the requested bean, a
	 * {@code BeanNotOfRequiredTypeException} against the NullBean stub will be raised.
	 * Consider using {@link #getBeanProvider(Class)} for resolving optional dependencies.
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws BeansException if the bean could not be created
	 */
	// 返回指定 bean 的实例，该实例可以是共享的，也可以是独立的。
	// <p>行为与 {@link #getBean(String)} 相同，但如果 bean 不是所需类型，则会抛出 BeanNotOfRequiredTypeException 异常，从而提供类型安全性。
	// 这意味着在正确转换结果时不会抛出 ClassCastException 异常，而 {@link #getBean(String)} 则可能会抛出该异常。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到该 bean，则会询问父工厂。
	// @param name 要检索的 bean 的名称
	// @param requiredType bean 必须匹配的类型；可以是接口或超类
	// @return bean 的实例。
	// 请注意，返回值永远不会为 {@code null}。如果工厂方法中 {@code null} 的存根已为请求的 bean 解析，
	// 则会针对 NullBean 存根引发 {@code BeanNotOfRequiredTypeException}。请考虑使用 {@link #getBeanProvider(Class)} 来解析可选依赖项。
	// 如果不存在此类 bean 定义，则抛出 NoSuchBeanDefinitionException
	// 如果 bean 不是所需类型，则抛出 BeanNotOfRequiredTypeException
	// 如果无法创建 bean，则抛出 BeansException
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Allows for specifying explicit constructor arguments / factory method arguments,
	 * overriding the specified default arguments (if any) in the bean definition.
	 * Note that the provided arguments need to match a specific candidate constructor /
	 * factory method in the order of declared parameters.
	 * @param name the name of the bean to retrieve
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanDefinitionStoreException if arguments have been given but
	 * the affected bean isn't a prototype
	 * @throws BeansException if the bean could not be created
	 * @since 2.5
	 */
	// 返回指定 bean 的实例，该实例可以是共享的，也可以是独立的。
	// <p>允许指定显式构造函数参数/工厂方法参数，并覆盖 bean 定义中指定的默认参数（如果有）。
	// 请注意，提供的参数需要按照声明参数的顺序与特定的候选构造函数/工厂方法匹配。
	// @param name 要检索的 bean 的名称
	// @param args 使用显式参数创建 bean 实例时使用的参数（仅适用于创建新实例，而不是检索现有实例）
	// @return bean 的实例
	// 如果不存在这样的 bean 定义，则抛出 NoSuchBeanDefinitionException
	// 如果已提供参数，但受影响的 bean 不是原型，则抛出 BeanDefinitionStoreException
	// 如果无法创建 bean，则抛出 BeansException
	Object getBean(String name, Object... args) throws BeansException;

	/**
	 * Return the bean instance that uniquely matches the given object type, if any.
	 * <p>This method goes into {@link ListableBeanFactory} by-type lookup territory
	 * but may also be translated into a conventional by-name lookup based on the name
	 * of the given type. For more extensive retrieval operations across sets of beans,
	 * use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @return an instance of the single bean matching the required type
	 * @throws NoSuchBeanDefinitionException if no bean of the given type was found
	 * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
	 * @throws BeansException if the bean could not be created
	 * @since 3.0
	 * @see ListableBeanFactory
	 */
	// 返回与给定对象类型唯一匹配的 Bean 实例（如果有）。
	// <p>此方法属于 {@link ListableBeanFactory} 按类型查找的范畴，但也可以转换为基于给定类型名称的常规按名称查找。
	// 如需跨 Bean 集合进行更广泛的检索操作，请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
	// @param requiredType Bean 必须匹配的类型；可以是接口或超类
	// @return 与所需类型匹配的单个 Bean 的实例
	// 如果未找到给定类型的 Bean，则抛出 NoSuchBeanDefinitionException
	// 如果找到多个给定类型的 Bean，则抛出 NoUniqueBeanDefinitionException
	// 如果无法创建 Bean，则抛出 BeansException
	<T> T getBean(Class<T> requiredType) throws BeansException;

	/**
	 * Return an instance, which may be shared or independent, of the specified bean.
	 * <p>Allows for specifying explicit constructor arguments / factory method arguments,
	 * overriding the specified default arguments (if any) in the bean definition.
	 * Note that the provided arguments need to match a specific candidate constructor /
	 * factory method in the order of declared parameters.
	 * <p>This method goes into {@link ListableBeanFactory} by-type lookup territory
	 * but may also be translated into a conventional by-name lookup based on the name
	 * of the given type. For more extensive retrieval operations across sets of beans,
	 * use {@link ListableBeanFactory} and/or {@link BeanFactoryUtils}.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @param args arguments to use when creating a bean instance using explicit arguments
	 * (only applied when creating a new instance as opposed to retrieving an existing one)
	 * @return an instance of the bean
	 * @throws NoSuchBeanDefinitionException if there is no such bean definition
	 * @throws BeanDefinitionStoreException if arguments have been given but
	 * the affected bean isn't a prototype
	 * @throws BeansException if the bean could not be created
	 * @since 4.1
	 */
	// 返回指定 bean 的实例，该实例可以是共享的，也可以是独立的。
	// <p>允许指定显式构造函数参数/工厂方法参数，并覆盖 bean 定义中指定的默认参数（如果有）。请注意，提供的参数需要按照声明参数的顺序与特定的候选构造函数/工厂方法匹配。
	// <p>此方法属于 {@link ListableBeanFactory} 按类型查找的范围，但也可以转换为基于给定类型名称的常规按名称查找。
	// 要跨 bean 集进行更广泛的检索操作，请使用 {@link ListableBeanFactory} 和/或 {@link BeanFactoryUtils}。
	// @param requiredType bean 必须匹配的类型；可以是接口或超类
	// @param args 使用显式参数创建 bean 实例时使用的参数（仅在创建新实例时使用，而不是检索现有实例）
	// @return bean 的实例
	// 如果不存在这样的 bean 定义，则抛出 NoSuchBeanDefinitionException
	// 如果已提供参数但受影响的 bean 不是原型，则抛出 BeanDefinitionStoreException
	// 如果无法创建 bean，则抛出 BeansException
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	/**
	 * Return a provider for the specified bean, allowing for lazy on-demand retrieval
	 * of instances, including availability and uniqueness options.
	 * <p>For matching a generic type, consider {@link #getBeanProvider(ResolvableType)}.
	 * @param requiredType type the bean must match; can be an interface or superclass
	 * @return a corresponding provider handle
	 * @since 5.1
	 * @see #getBeanProvider(ResolvableType)
	 */
	// 返回指定 Bean 的提供程序，允许延迟按需检索实例，包括可用性和唯一性选项。
	// <p>要匹配泛型类型，请考虑 {@link #getBeanProvider(ResolvableType)}。
	// @param requiredType Bean 必须匹配的类型；可以是接口或超类
	// @return 相应的提供程序句柄
	<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);

	/**
	 * Return a provider for the specified bean, allowing for lazy on-demand retrieval
	 * of instances, including availability and uniqueness options. This variant allows
	 * for specifying a generic type to match, similar to reflective injection points
	 * with generic type declarations in method/constructor parameters.
	 * <p>Note that collections of beans are not supported here, in contrast to reflective
	 * injection points. For programmatically retrieving a list of beans matching a
	 * specific type, specify the actual bean type as an argument here and subsequently
	 * use {@link ObjectProvider#orderedStream()} or its lazy streaming/iteration options.
	 * <p>Also, generics matching is strict here, as per the Java assignment rules.
	 * For lenient fallback matching with unchecked semantics (similar to the 'unchecked'
	 * Java compiler warning), consider calling {@link #getBeanProvider(Class)} with the
	 * raw type as a second step if no full generic match is
	 * {@link ObjectProvider#getIfAvailable() available} with this variant.
	 * @return a corresponding provider handle
	 * @param requiredType type the bean must match; can be a generic type declaration
	 * @since 5.1
	 * @see ObjectProvider#iterator()
	 * @see ObjectProvider#stream()
	 * @see ObjectProvider#orderedStream()
	 */
	// 返回指定 Bean 的提供程序，允许延迟按需检索实例，包括可用性和唯一性选项。此变体允许指定要匹配的泛型类型，类似于在方法/构造函数参数中使用泛型类型声明的反射注入点。
	// <p>请注意，与反射注入点不同，此处不支持 Bean 集合。要以编程方式检索与特定类型匹配的 Bean 列表，请在此处指定实际 Bean 类型作为参数，
	// 然后使用 {@link ObjectProvider#orderedStream()} 或其延迟流/迭代选项。
	// <p>此外，根据 Java 赋值规则，此处的泛型匹配非常严格。为了使用未检查语义进行宽松的回退匹配（类似于“未检查”Java 编译器警告），
	// 如果此变体没有可用的完整泛型匹配 {@link ObjectProvider#getIfAvailable()}，请考虑在第二步调用 {@link #getBeanProvider(Class)} 并使用原始类型。
	// @return 对应的提供程序句柄
	// @param requiredType Bean 必须匹配的类型；可以是泛型类型声明
	<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);

	/**
	 * Does this bean factory contain a bean definition or externally registered singleton
	 * instance with the given name?
	 * <p>If the given name is an alias, it will be translated back to the corresponding
	 * canonical bean name.
	 * <p>If this factory is hierarchical, will ask any parent factory if the bean cannot
	 * be found in this factory instance.
	 * <p>If a bean definition or singleton instance matching the given name is found,
	 * this method will return {@code true} whether the named bean definition is concrete
	 * or abstract, lazy or eager, in scope or not. Therefore, note that a {@code true}
	 * return value from this method does not necessarily indicate that {@link #getBean}
	 * will be able to obtain an instance for the same name.
	 * @param name the name of the bean to query
	 * @return whether a bean with the given name is present
	 */
	// 此 Bean 工厂是否包含具有给定名称的 Bean 定义或外部注册的单例实例？
	// <p>如果给定名称是别名，它将被转换回相应的规范 Bean 名称。
	// <p>如果此工厂是分层的，则当在此工厂实例中找不到该 Bean 时，将询问任何父工厂。
	// <p>如果找到与给定名称匹配的 Bean 定义或单例实例，则此方法将返回 {@code true}，无论指定的 Bean 定义是具体还是抽象、惰性加载还是立即加载，也无论其是否在作用域内。
	// 因此，请注意，此方法的 {@code true} 返回值并不一定表示 {@link #getBean} 能够获取同名的实例。
	// @param name 要查询的 Bean 的名称
	// @return 是否存在具有给定名称的 Bean
	boolean containsBean(String name);

	/**
	 * Is this bean a shared singleton? That is, will {@link #getBean} always
	 * return the same instance?
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * independent instances. It indicates non-singleton instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isPrototype} operation to explicitly
	 * check for independent instances.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return whether this bean corresponds to a singleton instance
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @see #getBean
	 * @see #isPrototype
	 */
	// 此 Bean 是共享单例吗？也就是说，{@link #getBean} 是否总是返回同一个实例？
	// <p>注意：此方法返回 {@code false} 并不明确指示独立实例。它指示非单例实例，这些实例也可能对应于作用域 Bean。使用 {@link #isPrototype} 操作可以显式检查独立实例。
	// <p>将别名转换回相应的规范 Bean 名称。
	// <p>如果在此工厂实例中找不到该 Bean，则会询问父工厂。
	// @param name 要查询的 Bean 的名称
	// @return 此 Bean 是否对应于单例实例
	// 如果不存在具有给定名称的 Bean，则抛出 NoSuchBeanDefinitionException
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Is this bean a prototype? That is, will {@link #getBean} always return
	 * independent instances?
	 * <p>Note: This method returning {@code false} does not clearly indicate
	 * a singleton object. It indicates non-independent instances, which may correspond
	 * to a scoped bean as well. Use the {@link #isSingleton} operation to explicitly
	 * check for a shared singleton instance.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return whether this bean will always deliver independent instances
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 2.0.3
	 * @see #getBean
	 * @see #isSingleton
	 */
	// 这个 bean 是原型吗？也就是说，{@link #getBean} 是否总是返回独立实例？
	// <p>注意：此方法返回 {@code false} 并不明确指示单例对象。它指示非独立实例，这些实例也可能对应于作用域 bean。使用 {@link #isSingleton} 操作可以显式检查共享单例实例。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到该 bean，则会询问父工厂。
	// @param name 要查询的 bean 的名称
	// @return 此 bean 是否总是会传递独立实例
	// 如果不存在具有给定名称的 bean，则抛出 NoSuchBeanDefinitionException
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Check whether the bean with the given name matches the specified type.
	 * More specifically, check whether a {@link #getBean} call for the given name
	 * would return an object that is assignable to the specified target type.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @param typeToMatch the type to match against (as a {@code ResolvableType})
	 * @return {@code true} if the bean type matches,
	 * {@code false} if it doesn't match or cannot be determined yet
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 4.2
	 * @see #getBean
	 * @see #getType
	 */
	// 检查给定名称的 bean 是否与指定类型匹配。更具体地说，检查对给定名称的 {@link #getBean} 调用是否返回可赋值给指定目标类型的对象。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到该 bean，则会询问父工厂。
	// @param name 要查询的 bean 的名称
	// @param typeToMatch 要匹配的类型（以 {@code ResolvableType} 形式）
	// @return {@code true} 如果 bean 类型匹配，
	// {@code false} 如果不匹配或尚无法确定
	// @throws NoSuchBeanDefinitionException 如果不存在具有给定名称的 bean
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * Check whether the bean with the given name matches the specified type.
	 * More specifically, check whether a {@link #getBean} call for the given name
	 * would return an object that is assignable to the specified target type.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @param typeToMatch the type to match against (as a {@code Class})
	 * @return {@code true} if the bean type matches,
	 * {@code false} if it doesn't match or cannot be determined yet
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 2.0.1
	 * @see #getBean
	 * @see #getType
	 */
	// 检查给定名称的 bean 是否与指定类型匹配。
	// 更具体地说，检查给定名称的 {@link #getBean} 调用是否返回可赋值给指定目标类型的对象。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到该 bean，则会询问父工厂。
	// @param name 要查询的 bean 的名称
	// @param typeToMatch 要匹配的类型（作为 {@code Class}）
	// @return {@code true} 如果 bean 类型匹配，
	// {@code false} 如果不匹配或尚无法确定
	// 如果不存在具有给定名称的 bean，则抛出 NoSuchBeanDefinitionException
	boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * Determine the type of the bean with the given name. More specifically,
	 * determine the type of object that {@link #getBean} would return for the given name.
	 * <p>For a {@link FactoryBean}, return the type of object that the FactoryBean creates,
	 * as exposed by {@link FactoryBean#getObjectType()}. This may lead to the initialization
	 * of a previously uninitialized {@code FactoryBean} (see {@link #getType(String, boolean)}).
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @return the type of the bean, or {@code null} if not determinable
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 1.1.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	// 确定具有给定名称的 bean 的类型。更具体地说，确定 {@link #getBean} 为给定名称返回的对象类型。
	// <p>对于 {@link FactoryBean}，返回 FactoryBean 创建的对象类型，由 {@link FactoryBean#getObjectType()} 公开。
	// 这可能会导致初始化之前未初始化的 {@code FactoryBean}（参见 {@link #getType(String, boolean)}）。
	// <p>将别名转换回相应的规范 bean 名称。
	// <p>如果在此工厂实例中找不到 bean，则会询问父工厂。
	// @param name 要查询的 bean 的名称
	// @return bean 的类型，如果无法确定，则返回 {@code null}
	// 如果不存在具有给定名称的 bean，则抛出 NoSuchBeanDefinitionException
	@Nullable
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * Determine the type of the bean with the given name. More specifically,
	 * determine the type of object that {@link #getBean} would return for the given name.
	 * <p>For a {@link FactoryBean}, return the type of object that the FactoryBean creates,
	 * as exposed by {@link FactoryBean#getObjectType()}. Depending on the
	 * {@code allowFactoryBeanInit} flag, this may lead to the initialization of a previously
	 * uninitialized {@code FactoryBean} if no early type information is available.
	 * <p>Translates aliases back to the corresponding canonical bean name.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the name of the bean to query
	 * @param allowFactoryBeanInit whether a {@code FactoryBean} may get initialized
	 * just for the purpose of determining its object type
	 * @return the type of the bean, or {@code null} if not determinable
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @since 5.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	// 确定具有给定名称的 Bean 的类型。更具体地说，确定 {@link #getBean} 将为给定名称返回的对象类型。
	// <p>对于 {@link FactoryBean}，返回 FactoryBean 创建的对象类型，该类型由 {@link FactoryBean#getObjectType()} 公开。
	// 	根据 {@code allowFactoryBeanInit} 标志，如果没有可用的早期类型信息，这可能会导致初始化先前未初始化的 {@code FactoryBean}。
	// <p>将别名转换回相应的规范 Bean 名称。
	// <p>如果在此工厂实例中找不到 Bean，则会询问父工厂。
	// @param name 待查询 bean 的名称
	// @param allowFactoryBeanInit 是否可以仅为了确定其对象类型而初始化 {@code FactoryBean}
	// @return bean 的类型，如果无法确定则返回 {@code null}
	// @throws NoSuchBeanDefinitionException 如果没有指定名称的 bean
	@Nullable
	Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

	/**
	 * Return the aliases for the given bean name, if any.
	 * <p>All of those aliases point to the same bean when used in a {@link #getBean} call.
	 * <p>If the given name is an alias, the corresponding original bean name
	 * and other aliases (if any) will be returned, with the original bean name
	 * being the first element in the array.
	 * <p>Will ask the parent factory if the bean cannot be found in this factory instance.
	 * @param name the bean name to check for aliases
	 * @return the aliases, or an empty array if none
	 * @see #getBean
	 */
	// 返回给定 bean 名称的别名（如果有）。
	// <p>所有这些别名在 {@link #getBean} 调用中使用时都指向同一个 bean。
	// <p>如果给定名称是别名，则将返回相应的原始 bean 名称和其他别名（如果有），原始 bean 名称是数组中的第一个元素。
	// <p>如果在此工厂实例中找不到该 bean，将询问父工厂。
	// @param name 用于检查别名的 bean 名称
	// @return 别名，如果没有，则返回空数组
	String[] getAliases(String name);

}
