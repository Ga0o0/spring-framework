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

package org.springframework.beans.factory.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A root bean definition represents the <b>merged bean definition at runtime</b>
 * that backs a specific bean in a Spring BeanFactory. It might have been created
 * from multiple original bean definitions that inherit from each other, e.g.
 * {@link GenericBeanDefinition GenericBeanDefinitions} from XML declarations.
 * A root bean definition is essentially the 'unified' bean definition view at runtime.
 *
 * <p>Root bean definitions may also be used for <b>registering individual bean
 * definitions in the configuration phase.</b> This is particularly applicable for
 * programmatic definitions derived from factory methods (e.g. {@code @Bean} methods)
 * and instance suppliers (e.g. lambda expressions) which come with extra type metadata
 * (see {@link #setTargetType(ResolvableType)}/{@link #setResolvedFactoryMethod(Method)}).
 *
 * <p>Note: The preferred choice for bean definitions derived from declarative sources
 * (e.g. XML definitions) is the flexible {@link GenericBeanDefinition} variant.
 * GenericBeanDefinition comes with the advantage that it allows for dynamically
 * defining parent dependencies, not 'hard-coding' the role as a root bean definition,
 * even supporting parent relationship changes in the bean post-processor phase.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
// 根 Bean 定义表示<b>运行时合并的 Bean 定义</b>，它支持 Spring BeanFactory 中的特定 Bean。
// 它可能由多个相互继承的原始 Bean 定义创建而成，例如，来自 XML 声明的 {@link GenericBeanDefinition GenericBeanDefinitions}。
// 根 Bean 定义本质上是运行时“统一”的 Bean 定义视图。
//
// <p>根 Bean 定义也可用于<b>在配置阶段注册单个 Bean 定义</b>。
// 这尤其适用于从工厂方法（例如 {@code @Bean} 方法）和实例提供者（例如 lambda 表达式）
// 派生的带有额外类型元数据的编程式定义（参见 {@link #setTargetType(ResolvableType)}/{@link #setResolvedFactoryMethod(Method)}）。
//
// <p>注意：对于从声明式源（例如 XML 定义）派生的 Bean 定义，
// 首选灵活的 {@link GenericBeanDefinition} 变体。GenericBeanDefinition 的优势在于它允许动态定义父级依赖关系，
// 无需将角色“硬编码”为根 Bean 定义，甚至支持在 Bean 后处理阶段更改父级关系。
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	@Nullable
	private BeanDefinitionHolder decoratedDefinition;

	@Nullable
	private AnnotatedElement qualifiedElement;

	/** Determines if the definition needs to be re-merged. */
	volatile boolean stale;

	boolean allowCaching = true;

	boolean isFactoryMethodUnique;

	@Nullable
	volatile ResolvableType targetType;

	/** Package-visible field for caching the determined Class of a given bean definition. */
	// 用于缓存给定Bean定义所确定的Class的包可见字段。
	@Nullable
	volatile Class<?> resolvedTargetType;

	/** Package-visible field for caching if the bean is a factory bean. */
	@Nullable
	volatile Boolean isFactoryBean;

	/** Package-visible field for caching the return type of a generically typed factory method. */
	@Nullable
	volatile ResolvableType factoryMethodReturnType;

	/** Package-visible field for caching a unique factory method candidate for introspection. */
	@Nullable
	volatile Method factoryMethodToIntrospect;

	/** Package-visible field for caching a resolved destroy method name (also for inferred). */
	@Nullable
	volatile String resolvedDestroyMethodName;

	/** Common lock for the four constructor fields below. */
	final Object constructorArgumentLock = new Object();

	/** Package-visible field for caching the resolved constructor or factory method. */
	// 用于缓存已解析构造函数或工厂方法的包可见字段。
	@Nullable
	Executable resolvedConstructorOrFactoryMethod;

	/** Package-visible field that marks the constructor arguments as resolved. */
	boolean constructorArgumentsResolved = false;

	/** Package-visible field for caching fully resolved constructor arguments. */
	@Nullable
	Object[] resolvedConstructorArguments;

	/** Package-visible field for caching partly prepared constructor arguments. */
	@Nullable
	Object[] preparedConstructorArguments;

	/** Common lock for the two post-processing fields below. */
	// 以下两个后处理字段共用一个锁。
	final Object postProcessingLock = new Object();

	/** Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied. */
	// 包可见字段，指示已应用 MergedBeanDefinitionPostProcessor。
	boolean postProcessed = false;

	/** Package-visible field that indicates a before-instantiation post-processor having kicked in. */
	@Nullable
	volatile Boolean beforeInstantiationResolved;

	@Nullable
	private Set<Member> externallyManagedConfigMembers;

	@Nullable
	private Set<String> externallyManagedInitMethods;

	@Nullable
	private Set<String> externallyManagedDestroyMethods;


	/**
	 * Create a new RootBeanDefinition, to be configured through its bean
	 * properties and configuration methods.
	 * @see #setBeanClass
	 * @see #setScope
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public RootBeanDefinition() {
	}

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * @param beanClass the class of the bean to instantiate
	 * @see #setBeanClass
	 */
	// 为单例创建一个新的 RootBeanDefinition。
	// @param beanClass 需要实例化的 bean 的类
	public RootBeanDefinition(@Nullable Class<?> beanClass) {
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * @param beanType the type of bean to instantiate
	 * @since 6.0
	 * @see #setTargetType(ResolvableType)
	 * @deprecated as of 6.0.11, in favor of an extra {@link #setTargetType(ResolvableType)} call
	 */
	@Deprecated(since = "6.0.11")
	public RootBeanDefinition(@Nullable ResolvableType beanType) {
		setTargetType(beanType);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton bean, constructing each instance
	 * through calling the given supplier (possibly a lambda or method reference).
	 * @param beanClass the class of the bean to instantiate
	 * @param instanceSupplier the supplier to construct a bean instance,
	 * as an alternative to a declaratively specified factory method
	 * @since 5.0
	 * @see #setInstanceSupplier
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, @Nullable Supplier<T> instanceSupplier) {
		setBeanClass(beanClass);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * Create a new RootBeanDefinition for a scoped bean, constructing each instance
	 * through calling the given supplier (possibly a lambda or method reference).
	 * @param beanClass the class of the bean to instantiate
	 * @param scope the name of the corresponding scope
	 * @param instanceSupplier the supplier to construct a bean instance,
	 * as an alternative to a declaratively specified factory method
	 * @since 5.0
	 * @see #setInstanceSupplier
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, String scope, @Nullable Supplier<T> instanceSupplier) {
		setBeanClass(beanClass);
		setScope(scope);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, @Nullable ConstructorArgumentValues cargs,
			@Nullable MutablePropertyValues pvs) {

		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 */
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
		this.decoratedDefinition = original.decoratedDefinition;
		this.qualifiedElement = original.qualifiedElement;
		this.allowCaching = original.allowCaching;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
		this.targetType = original.targetType;
		this.factoryMethodToIntrospect = original.factoryMethodToIntrospect;
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}


	@Override
	@Nullable
	public String getParentName() {
		return null;
	}

	@Override
	public void setParentName(@Nullable String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	/**
	 * Register a target definition that is being decorated by this bean definition.
	 */
	// 注册一个被这个 bean 定义装饰的目标定义。
	public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	/**
	 * Return the target definition that is being decorated by this bean definition, if any.
	 */
	@Nullable
	public BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	/**
	 * Specify the {@link AnnotatedElement} defining qualifiers,
	 * to be used instead of the target class or factory method.
	 * @since 4.3.3
	 * @see #setTargetType(ResolvableType)
	 * @see #getResolvedFactoryMethod()
	 */
	public void setQualifiedElement(@Nullable AnnotatedElement qualifiedElement) {
		this.qualifiedElement = qualifiedElement;
	}

	/**
	 * Return the {@link AnnotatedElement} defining qualifiers, if any.
	 * Otherwise, the factory method and target class will be checked.
	 * @since 4.3.3
	 */
	@Nullable
	public AnnotatedElement getQualifiedElement() {
		return this.qualifiedElement;
	}

	/**
	 * Specify a generics-containing target type of this bean definition, if known in advance.
	 * @since 4.3.3
	 */
	public void setTargetType(@Nullable ResolvableType targetType) {
		this.targetType = targetType;
	}

	/**
	 * Specify the target type of this bean definition, if known in advance.
	 * @since 3.2.2
	 */
	public void setTargetType(@Nullable Class<?> targetType) {
		this.targetType = (targetType != null ? ResolvableType.forClass(targetType) : null);
	}

	/**
	 * Return the target type of this bean definition, if known
	 * (either specified in advance or resolved on first instantiation).
	 * @since 3.2.2
	 */
	// 如果已知（无论是预先指定还是在首次实例化时解析得出），则返回此Bean定义的目标类型。
	@Nullable
	public Class<?> getTargetType() {
		if (this.resolvedTargetType != null) {
			return this.resolvedTargetType;
		}
		ResolvableType targetType = this.targetType;
		return (targetType != null ? targetType.resolve() : null);
	}

	/**
	 * Return a {@link ResolvableType} for this bean definition,
	 * either from runtime-cached type information or from configuration-time
	 * {@link #setTargetType(ResolvableType)} or {@link #setBeanClass(Class)},
	 * also considering resolved factory method definitions.
	 * @since 5.1
	 * @see #setTargetType(ResolvableType)
	 * @see #setBeanClass(Class)
	 * @see #setResolvedFactoryMethod(Method)
	 */
	@Override
	public ResolvableType getResolvableType() {
		ResolvableType targetType = this.targetType;
		if (targetType != null) {
			return targetType;
		}
		ResolvableType returnType = this.factoryMethodReturnType;
		if (returnType != null) {
			return returnType;
		}
		Method factoryMethod = this.factoryMethodToIntrospect;
		if (factoryMethod != null) {
			return ResolvableType.forMethodReturnType(factoryMethod);
		}
		return super.getResolvableType();
	}

	/**
	 * Determine preferred constructors to use for default construction, if any.
	 * Constructor arguments will be autowired if necessary.
	 * <p>As of 6.1, the default implementation of this method takes the
	 * {@link #PREFERRED_CONSTRUCTORS_ATTRIBUTE} attribute into account.
	 * Subclasses are encouraged to preserve this through a {@code super} call,
	 * either before or after their own preferred constructor determination.
	 * @return one or more preferred constructors, or {@code null} if none
	 * (in which case the regular no-arg default constructor will be called)
	 * @since 5.1
	 */
	// 确定用于默认构造的首选构造函数（如有）。如有必要，构造函数参数将自动装配。
	//
	// <p>从 6.1 版本开始，此方法的默认实现会考虑 {@link #PREFERRED_CONSTRUCTORS_ATTRIBUTE} 属性。
	// 建议子类通过调用 {@code super} 来保留此属性，调用可以在确定自身首选构造函数之前或之后进行。
	//
	// @return 返回一个或多个首选构造函数，如果没有首选构造函数，则返回 {@code null}（在这种情况下，将调用常规的无参默认构造函数）。
	@Nullable
	public Constructor<?>[] getPreferredConstructors() {
		Object attribute = getAttribute(PREFERRED_CONSTRUCTORS_ATTRIBUTE);
		if (attribute == null) {
			return null;
		}
		if (attribute instanceof Constructor<?> constructor) {
			return new Constructor<?>[] {constructor};
		}
		if (attribute instanceof Constructor<?>[]) {
			return (Constructor<?>[]) attribute;
		}
		throw new IllegalArgumentException("Invalid value type for attribute '" +
				PREFERRED_CONSTRUCTORS_ATTRIBUTE + "': " + attribute.getClass().getName());
	}

	/**
	 * Specify a factory method name that refers to a non-overloaded method.
	 */
	// 指定引用非重载方法的工厂方法名称。
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	/**
	 * Specify a factory method name that refers to an overloaded method.
	 * @since 5.2
	 */
	public void setNonUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = false;
	}

	/**
	 * Check whether the given candidate qualifies as a factory method.
	 */
	public boolean isFactoryMethod(Method candidate) {
		return candidate.getName().equals(getFactoryMethodName());
	}

	/**
	 * Set a resolved Java Method for the factory method on this bean definition.
	 * @param method the resolved factory method, or {@code null} to reset it
	 * @since 5.2
	 */
	// 为该 bean 定义中的工厂方法设置一个已解析的 Java 方法。
	// @param method 已解析的工厂方法，或 {@code null} 重置它
	public void setResolvedFactoryMethod(@Nullable Method method) {
		this.factoryMethodToIntrospect = method;
		if (method != null) {
			setUniqueFactoryMethodName(method.getName());
		}
	}

	/**
	 * Return the resolved factory method as a Java Method object, if available.
	 * @return the factory method, or {@code null} if not found or not resolved yet
	 */
	@Nullable
	public Method getResolvedFactoryMethod() {
		return this.factoryMethodToIntrospect;
	}

	@Override
	public void setInstanceSupplier(@Nullable Supplier<?> supplier) {
		super.setInstanceSupplier(supplier);
		Method factoryMethod = (supplier instanceof InstanceSupplier<?> instanceSupplier ?
				instanceSupplier.getFactoryMethod() : null);
		if (factoryMethod != null) {
			setResolvedFactoryMethod(factoryMethod);
		}
	}

	/**
	 * Mark this bean definition as post-processed,
	 * i.e. processed by {@link MergedBeanDefinitionPostProcessor}.
	 * @since 6.0
	 */
	// 将此 bean 定义标记为已后处理，即由 {@link MergedBeanDefinitionPostProcessor} 处理。
	public void markAsPostProcessed() {
		synchronized (this.postProcessingLock) {
			this.postProcessed = true;
		}
	}

	/**
	 * Register an externally managed configuration method or field.
	 */
	// 注册外部管理的配置方法或字段。
	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new LinkedHashSet<>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}

	/**
	 * Determine if the given method or field is an externally managed configuration member.
	 */
	// 确定给定的方法或字段是否是外部管理的配置成员。
	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	/**
	 * Get all externally managed configuration methods and fields (as an immutable Set).
	 * @since 5.3.11
	 */
	public Set<Member> getExternallyManagedConfigMembers() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedConfigMembers)) :
					Collections.emptySet());
		}
	}

	/**
	 * Register an externally managed configuration initialization method &mdash;
	 * for example, a method annotated with JSR-250's {@code javax.annotation.PostConstruct}
	 * or Jakarta's {@link jakarta.annotation.PostConstruct} annotation.
	 * <p>The supplied {@code initMethod} may be a
	 * {@linkplain Method#getName() simple method name} or a
	 * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
	 * qualified method name} for package-private and {@code private} methods.
	 * A qualified name is necessary for package-private and {@code private} methods
	 * in order to disambiguate between multiple such methods with the same name
	 * within a type hierarchy.
	 */
	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new LinkedHashSet<>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	/**
	 * Determine if the given method name indicates an externally managed
	 * initialization method.
	 * <p>See {@link #registerExternallyManagedInitMethod} for details
	 * regarding the format for the supplied {@code initMethod}.
	 */
	// 判断给定的方法名是否表示外部管理的初始化方法。
	//
	// <p>有关提供的 {@code initMethod} 的格式详情，请参阅 {@link #registerExternallyManagedInitMethod}。</p>
	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	/**
	 * Determine if the given method name indicates an externally managed
	 * initialization method, regardless of method visibility.
	 * <p>In contrast to {@link #isExternallyManagedInitMethod(String)}, this
	 * method also returns {@code true} if there is a {@code private} externally
	 * managed initialization method that has been
	 * {@linkplain #registerExternallyManagedInitMethod(String) registered}
	 * using a qualified method name instead of a simple method name.
	 * @since 5.3.17
	 */
	// 无论方法可见性如何，此方法都会判断给定的方法名是否指示外部管理的初始化方法。
	//
	// <p>与 {@link #isExternallyManagedInitMethod(String)} 不同，
	// 如果存在一个已使用限定方法名而非简单方法名注册的 {@code private} 外部管理的初始化方法，此方法也会返回 {@code true}。
	boolean hasAnyExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (isExternallyManagedInitMethod(initMethod)) {
				return true;
			}
			return hasAnyExternallyManagedMethod(this.externallyManagedInitMethods, initMethod);
		}
	}

	/**
	 * Get all externally managed initialization methods (as an immutable Set).
	 * <p>See {@link #registerExternallyManagedInitMethod} for details
	 * regarding the format for the initialization methods in the returned set.
	 * @since 5.3.11
	 */
	public Set<String> getExternallyManagedInitMethods() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedInitMethods)) :
					Collections.emptySet());
		}
	}

	/**
	 * Resolve the inferred destroy method if necessary.
	 * @since 6.0
	 */
	public void resolveDestroyMethodIfNecessary() {
		setDestroyMethodNames(DisposableBeanAdapter
				.inferDestroyMethodsIfNecessary(getResolvableType().toClass(), this));
	}

	/**
	 * Register an externally managed configuration destruction method &mdash;
	 * for example, a method annotated with JSR-250's
	 * {@link jakarta.annotation.PreDestroy} annotation.
	 * <p>The supplied {@code destroyMethod} may be the
	 * {@linkplain Method#getName() simple method name} for non-private methods or the
	 * {@linkplain org.springframework.util.ClassUtils#getQualifiedMethodName(Method)
	 * qualified method name} for {@code private} methods. A qualified name is
	 * necessary for {@code private} methods in order to disambiguate between
	 * multiple private methods with the same name within a class hierarchy.
	 */
	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new LinkedHashSet<>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	/**
	 * Determine if the given method name indicates an externally managed
	 * destruction method.
	 * <p>See {@link #registerExternallyManagedDestroyMethod} for details
	 * regarding the format for the supplied {@code destroyMethod}.
	 */
	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}

	/**
	 * Determine if the given method name indicates an externally managed
	 * destruction method, regardless of method visibility.
	 * <p>In contrast to {@link #isExternallyManagedDestroyMethod(String)}, this
	 * method also returns {@code true} if there is a {@code private} externally
	 * managed destruction method that has been
	 * {@linkplain #registerExternallyManagedDestroyMethod(String) registered}
	 * using a qualified method name instead of a simple method name.
	 * @since 5.3.17
	 */
	boolean hasAnyExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (isExternallyManagedDestroyMethod(destroyMethod)) {
				return true;
			}
			return hasAnyExternallyManagedMethod(this.externallyManagedDestroyMethods, destroyMethod);
		}
	}

	private static boolean hasAnyExternallyManagedMethod(@Nullable Set<String> candidates, String methodName) {
		if (candidates != null) {
			for (String candidate : candidates) {
				int indexOfDot = candidate.lastIndexOf('.');
				if (indexOfDot > 0) {
					String candidateMethodName = candidate.substring(indexOfDot + 1);
					if (candidateMethodName.equals(methodName)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get all externally managed destruction methods (as an immutable Set).
	 * <p>See {@link #registerExternallyManagedDestroyMethod} for details
	 * regarding the format for the destruction methods in the returned set.
	 * @since 5.3.11
	 */
	public Set<String> getExternallyManagedDestroyMethods() {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null ?
					Collections.unmodifiableSet(new LinkedHashSet<>(this.externallyManagedDestroyMethods)) :
					Collections.emptySet());
		}
	}


	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
