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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.AutowiredPropertyMarker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.StringUtils;
import org.springframework.util.function.ThrowingSupplier;

/**
 * Abstract bean factory superclass that implements default bean creation,
 * with the full capabilities specified by the {@link RootBeanDefinition} class.
 * Implements the {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * interface in addition to AbstractBeanFactory's {@link #createBean} method.
 *
 * <p>Provides bean creation (with constructor resolution), property population,
 * wiring (including autowiring), and initialization. Handles runtime bean
 * references, resolves managed collections, calls initialization methods, etc.
 * Supports autowiring constructors, properties by name, and properties by type.
 *
 * <p>The main template method to be implemented by subclasses is
 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)}, used for
 * autowiring. In case of a {@link org.springframework.beans.factory.ListableBeanFactory}
 * which is capable of searching its bean definitions, matching beans will typically be
 * implemented through such a search. Otherwise, simplified matching can be implemented.
 *
 * <p>Note that this class does <i>not</i> assume or implement bean definition
 * registry capabilities. See {@link DefaultListableBeanFactory} for an implementation
 * of the {@link org.springframework.beans.factory.ListableBeanFactory} and
 * {@link BeanDefinitionRegistry} interfaces, which represent the API and SPI
 * view of such a factory, respectively.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @since 13.02.2004
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 */
// 抽象 Bean 工厂超类，实现默认 Bean 创建方法，并具备 {@link RootBeanDefinition} 类指定的全部功能。
// 除了 AbstractBeanFactory 的 {@link #createBean} 方法外，
// 还实现了 {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory} 接口。
//
// <p>提供 Bean 创建（包含构造函数解析）、属性填充、装配（包括自动装配）和初始化功能。
// 处理运行时 Bean 引用、解析托管集合、调用初始化方法等。支持自动装配构造函数、按名称装配属性以及按类型装配属性。
//
// <p>子类需要实现的主要模板方法是 {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)}，用于自动装配。
// 如果 {@link org.springframework.beans.factory.ListableBeanFactory} 能够搜索其自身的 bean 定义，
// 则通常会通过此类搜索来实现匹配 bean。否则，可以实现简化的匹配。
//
// <p>请注意，此类 <i>不</i> 假设或实现 bean 定义注册表功能。请参阅 {@link DefaultListableBeanFactory}，
// 了解 {@link org.springframework.beans.factory.ListableBeanFactory} 和 {@link BeanDefinitionRegistry} 接口的实现，
// 这两个接口分别代表此类工厂的 API 和 SPI 视图。
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	/** Strategy for creating bean instances. */
	// 创建 bean 实例的策略。
	private InstantiationStrategy instantiationStrategy;

	/** Resolver strategy for method parameter names. */
	// 方法参数名称的解析器策略。
	@Nullable
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	/** Whether to automatically try to resolve circular references between beans. */
	// 是否自动尝试解决 bean 之间的循环引用。
	private boolean allowCircularReferences = true;

	/**
	 * Whether to resort to injecting a raw bean instance in case of circular reference,
	 * even if the injected bean eventually got wrapped.
	 */
	// 如果存在循环引用，即使注入的 bean 最终被包装，是否仍需注入原始 bean 实例。
	private boolean allowRawInjectionDespiteWrapping = false;

	/**
	 * Dependency types to ignore on dependency check and autowire, as Set of
	 * Class objects: for example, String. Default is none.
	 */
	// 依赖项检查和自动装配时忽略的依赖类型，以 Class 对象的 Set 形式表示：例如，String。默认值为 none。
	private final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

	/**
	 * Dependency interfaces to ignore on dependency check and autowire, as Set of
	 * Class objects. By default, only the BeanFactory interface is ignored.
	 */
	// 依赖检查和自动装配时忽略的依赖接口，以 Class 对象集合的形式。默认情况下，仅忽略 BeanFactory 接口。
	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

	/**
	 * The name of the currently created bean, for implicit dependency registration
	 * on getBean etc invocations triggered from a user-specified Supplier callback.
	 */
	// 当前创建的 bean 的名称，用于从用户指定的供应商回调触发的 getBean 等调用上的隐式依赖注册。
	private final NamedThreadLocal<String> currentlyCreatedBean = new NamedThreadLocal<>("Currently created bean");

	/** Cache of unfinished FactoryBean instances: FactoryBean name to BeanWrapper. */
	// 未完成的 FactoryBean 实例的缓存：FactoryBean 名称为 BeanWrapper。
	private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

	/** Cache of candidate factory methods per factory class. */
	// 每个工厂类的候选工厂方法缓存。
	private final ConcurrentMap<Class<?>, Method[]> factoryMethodCandidateCache = new ConcurrentHashMap<>();

	/** Cache of filtered PropertyDescriptors: bean Class to PropertyDescriptor array. */
	// 已筛选 PropertyDescriptors 的缓存：将 bean 类缓存到 PropertyDescriptor 数组中。
	private final ConcurrentMap<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache =
			new ConcurrentHashMap<>();


	/**
	 * Create a new AbstractAutowireCapableBeanFactory.
	 */
	// 创建一个新的 AbstractAutowireCapableBeanFactory。
	public AbstractAutowireCapableBeanFactory() {
		super();
		ignoreDependencyInterface(BeanNameAware.class);
		ignoreDependencyInterface(BeanFactoryAware.class);
		ignoreDependencyInterface(BeanClassLoaderAware.class);
		this.instantiationStrategy = new CglibSubclassingInstantiationStrategy();
	}

	/**
	 * Create a new AbstractAutowireCapableBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or {@code null} if none
	 */
	// 使用给定的父级创建一个新的 AbstractAutowireCapableBeanFactory。
	// @param parentBeanFactory 父 bean 工厂，如果没有则为 {@code null}
	public AbstractAutowireCapableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
		this();
		setParentBeanFactory(parentBeanFactory);
	}


	/**
	 * Set the instantiation strategy to use for creating bean instances.
	 * Default is CglibSubclassingInstantiationStrategy.
	 * @see CglibSubclassingInstantiationStrategy
	 */
	// 设置用于创建 bean 实例的实例化策略。默认为 CglibSubclassingInstantiationStrategy。
	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * Return the instantiation strategy to use for creating bean instances.
	 */
	// 返回用于创建 bean 实例的实例化策略。
	public InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}

	/**
	 * Set the ParameterNameDiscoverer to use for resolving method parameter
	 * names if needed (e.g. for constructor names).
	 * <p>Default is a {@link DefaultParameterNameDiscoverer}.
	 */
	// 设置用于解析方法参数名称的 ParameterNameDiscoverer（如果需要）（例如，构造函数名称）。
	// <p>默认为 {@link DefaultParameterNameDiscoverer}。
	public void setParameterNameDiscoverer(@Nullable ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	/**
	 * Return the ParameterNameDiscoverer to use for resolving method parameter
	 * names if needed.
	 */
	// 如果需要，返回用于解析方法参数名称的 ParameterNameDiscoverer。
	@Nullable
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.parameterNameDiscoverer;
	}

	/**
	 * Set whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 * <p>Note that circular reference resolution means that one of the involved beans
	 * will receive a reference to another bean that is not fully initialized yet.
	 * This can lead to subtle and not-so-subtle side effects on initialization;
	 * it does work fine for many scenarios, though.
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
	 * between your beans. Refactor your application logic to have the two beans
	 * involved delegate to a third bean that encapsulates their common logic.
	 */
	// 设置是否允许 Bean 之间循环引用 - 并自动尝试解决它们。
	// <p>请注意，循环引用解析意味着其中一个涉及的 Bean 将收到对另一个尚未完全初始化的 Bean 的引用。
	// 这可能会对初始化产生一些微妙或不太微妙的副作用；不过，在很多情况下，它都能正常工作。
	// <p>默认值为“true”。关闭此选项会在遇到循环引用时抛出异常，从而完全禁止循环引用。
	// <p><b>注意：</b>通常建议不要依赖 Bean 之间的循环引用。重构您的应用程序逻辑，
	// 让涉及的两个 Bean 委托给第三个 Bean，该 Bean 封装了它们的公共逻辑。
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	/**
	 * Return whether to allow circular references between beans.
	 * @since 5.3.10
	 * @see #setAllowCircularReferences
	 */
	// 返回是否允许 bean 之间的循环引用。
	public boolean isAllowCircularReferences() {
		return this.allowCircularReferences;
	}

	/**
	 * Set whether to allow the raw injection of a bean instance into some other
	 * bean's property, despite the injected bean eventually getting wrapped
	 * (for example, through AOP auto-proxying).
	 * <p>This will only be used as a last resort in case of a circular reference
	 * that cannot be resolved otherwise: essentially, preferring a raw instance
	 * getting injected over a failure of the entire bean wiring process.
	 * <p>Default is "false", as of Spring 2.0. Turn this on to allow for non-wrapped
	 * raw beans injected into some of your references, which was Spring 1.2's
	 * (arguably unclean) default behavior.
	 * <p><b>NOTE:</b> It is generally recommended to not rely on circular references
	 * between your beans, in particular with auto-proxying involved.
	 * @see #setAllowCircularReferences
	 */
	// 设置是否允许将一个 bean 实例直接注入到其他 bean 的属性中，
	// 即使注入的 bean 最终会被包装（例如，通过 AOP 自动代理）。
	// <p>此选项仅在出现无法通过其他方式解决的循环引用时作为最后的手段：本质上，优先注入原始实例，
	// 而不是整个 bean 装配过程失败。<p>从 Spring 2.0 开始，默认值为“false”。
	// 启用此选项可允许将未包装的原始 bean 注入到某些引用中，这是 Spring 1.2（可能不太干净）的默认行为。
	// <p><b>注意：</b>通常建议不要依赖 bean 之间的循环引用，尤其是在涉及自动代理的情况下。
	public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}

	/**
	 * Return whether to allow the raw injection of a bean instance.
	 * @since 5.3.10
	 * @see #setAllowRawInjectionDespiteWrapping
	 */
	// 返回是否允许 Bean 实例的原始注入。
	public boolean isAllowRawInjectionDespiteWrapping() {
		return this.allowRawInjectionDespiteWrapping;
	}

	/**
	 * Ignore the given dependency type for autowiring:
	 * for example, String. Default is none.
	 */
	// 忽略自动装配时指定的依赖类型：例如 String。默认值为 None。
	public void ignoreDependencyType(Class<?> type) {
		this.ignoredDependencyTypes.add(type);
	}

	/**
	 * Ignore the given dependency interface for autowiring.
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through
	 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	// 忽略自动装配时指定的依赖接口。
	// <p>这通常由应用程序上下文用来注册以其他方式解析的依赖项，
	// 例如通过 BeanFactoryAware 解析 BeanFactory，或通过 ApplicationContextAware 解析 ApplicationContext。
	// <p>默认情况下，仅忽略 BeanFactoryAware 接口。要忽略其他类型，请针对每种类型调用此方法。
	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof AbstractAutowireCapableBeanFactory otherAutowireFactory) {
			this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
			this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
			this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
			this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
		}
	}


	//-------------------------------------------------------------------------
	// Typical methods for creating and populating external bean instances
	//-------------------------------------------------------------------------

	@Override
	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new CreateFromClassBeanDefinition(beanClass);
		bd.setScope(SCOPE_PROTOTYPE);
		bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
		return (T) createBean(beanClass.getName(), bd, null);
	}

	@Override
	public void autowireBean(Object existingBean) {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
		bd.setScope(SCOPE_PROTOTYPE);
		bd.allowCaching = ClassUtils.isCacheSafe(bd.getBeanClass(), getBeanClassLoader());
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public Object configureBean(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition mbd = getMergedBeanDefinition(beanName);
		RootBeanDefinition bd = null;
		if (mbd instanceof RootBeanDefinition rbd) {
			bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
		}
		if (bd == null) {
			bd = new RootBeanDefinition(mbd);
		}
		if (!bd.isPrototype()) {
			bd.setScope(SCOPE_PROTOTYPE);
			bd.allowCaching = ClassUtils.isCacheSafe(ClassUtils.getUserClass(existingBean), getBeanClassLoader());
		}
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(beanName, bd, bw);
		return initializeBean(beanName, existingBean, bd);
	}


	//-------------------------------------------------------------------------
	// Specialized methods for fine-grained control over the bean lifecycle
	//-------------------------------------------------------------------------

	@Deprecated
	@Override
	public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		return createBean(beanClass.getName(), bd, null);
	}

	@Override
	public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
		}
		else {
			Object bean = getInstantiationStrategy().instantiate(bd, null, this);
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}

	@Override
	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {

		if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
			throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
		}
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd =
				new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
		bd.setScope(SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}

	@Override
	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition bd = getMergedBeanDefinition(beanName);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}

	@Override
	public Object initializeBean(Object existingBean, String beanName) {
		return initializeBean(beanName, existingBean, null);
	}

	@Deprecated(since = "6.1")
	@Override
	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessBeforeInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Deprecated(since = "6.1")
	@Override
	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			// 执行 BeanPostProcessor.postProcessAfterInitialization() 方法
			Object current = processor.postProcessAfterInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Override
	public void destroyBean(Object existingBean) {
		new DisposableBeanAdapter(existingBean, getBeanPostProcessorCache().destructionAware).destroy();
	}


	//-------------------------------------------------------------------------
	// Delegate methods for resolving injection points
	//-------------------------------------------------------------------------

	@Override
	public Object resolveBeanByName(String name, DependencyDescriptor descriptor) {
		InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
		try {
			return getBean(name, descriptor.getDependencyType());
		}
		finally {
			ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
		}
	}

	@Override
	@Nullable
	public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException {
		return resolveDependency(descriptor, requestingBeanName, null, null);
	}


	//---------------------------------------------------------------------
	// Implementation of relevant AbstractBeanFactory template methods
	//---------------------------------------------------------------------

	/**
	 * Central method of this class: creates a bean instance,
	 * populates the bean instance, applies post-processors, etc.
	 * @see #doCreateBean
	 */
	// 此类的核心方法：创建 bean 实例、填充 bean 实例、应用后处理器等。
	@Override
	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		if (logger.isTraceEnabled()) {
			logger.trace("Creating instance of bean '" + beanName + "'");
		}
		RootBeanDefinition mbdToUse = mbd;

		// Make sure bean class is actually resolved at this point, and
		// clone the bean definition in case of a dynamically resolved Class
		// which cannot be stored in the shared merged bean definition.
		// --> 译文：确保此时 bean 类确实已被解析，并且如果动态解析的类无法存储在共享合并 bean 定义中，则克隆 bean 定义。
		// 1. 解析 bean 的 Class
		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
			mbdToUse = new RootBeanDefinition(mbd);
			mbdToUse.setBeanClass(resolvedClass);
			try {
				// 2. 处理 lookup-method 和 replace-method
				mbdToUse.prepareMethodOverrides();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
						beanName, "Validation of method overrides failed", ex);
			}
		}

		try {
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			// --> 译文：让 BeanPostProcessors 有机会返回代理而不是目标 bean 实例。
			// 3. 给 BeanPostProcessor 一个机会返回代理对象
			Object bean = resolveBeforeInstantiation(beanName, mbdToUse); // 应用实例化前后处理器，解析指定 bean 是否存在实例化前快捷方式。
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			// bean 实例化之前的 BeanPostProcessor 失败
			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
					"BeanPostProcessor before instantiation of bean failed", ex);
		}

		try {
			// 4. 实际创建 bean
			Object beanInstance = doCreateBean(beanName, mbdToUse, args); // important
			if (logger.isTraceEnabled()) {
				logger.trace("Finished creating instance of bean '" + beanName + "'");
			}
			return beanInstance;
		}
		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
			// A previously detected exception with proper bean creation context already,
			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
			// --> 译文：先前检测到的异常已经具有正确的 bean 创建上下文，或者非法的单例状态被传达到 DefaultSingletonBeanRegistry。
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
		}
	}

	/**
	 * Actually create the specified bean. Pre-creation processing has already happened
	 * at this point, e.g. checking {@code postProcessBeforeInstantiation} callbacks.
	 * <p>Differentiates between default bean instantiation, use of a
	 * factory method, and autowiring a constructor.
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param args explicit arguments to use for constructor or factory method invocation
	 * @return a new instance of the bean
	 * @throws BeanCreationException if the bean could not be created
	 * @see #instantiateBean
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 */
	// 实际创建指定的 Bean。此时创建前的预处理已经完成，例如检查 {@code postProcessBeforeInstantiation} 回调。
	// <p>区分默认 Bean 实例化、使用工厂方法以及自动装配构造函数。
	// @param beanName Bean 的名称
	// @param mbd Bean 的合并 Bean 定义
	// @param args 调用构造函数或工厂方法时使用的显式参数
	// @return Bean 的新实例
	// @throws BeanCreationException 如果无法创建 Bean
	protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
			throws BeanCreationException {

		// Instantiate the bean. --> 译文：实例化 Bean。
		// 1. 创建 Bean 实例（通过构造器或工厂方法）
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}
		if (instanceWrapper == null) {
			instanceWrapper = createBeanInstance(beanName, mbd, args); // important
		}
		Object bean = instanceWrapper.getWrappedInstance();
		Class<?> beanType = instanceWrapper.getWrappedClass();
		if (beanType != NullBean.class) {
			mbd.resolvedTargetType = beanType;
		}

		// Allow post-processors to modify the merged bean definition. --> 译文：允许后处理器修改合并的 bean 定义。
		// 2. 允许 MergedBeanDefinitionPostProcessor 处理
		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
					// 执行 MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition() 方法
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex);
				}
				mbd.markAsPostProcessed();
			}
		}

		// Eagerly cache singletons to be able to resolve circular references
		// even when triggered by lifecycle interfaces like BeanFactoryAware.
		// --> 译文：急切地缓存单例，以便能够解决循环引用，即使由 BeanFactoryAware 等生命周期接口触发。
		// 3. 处理循环依赖：提前暴露单例 bean
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
				isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			if (logger.isTraceEnabled()) {
				logger.trace("Eagerly caching bean '" + beanName +
						"' to allow for resolving potential circular references");
			}
			// important：一级缓存
			// getEarlyBeanReference(...) -> 执行 SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference() 方法
			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
		}

		// Initialize the bean instance. --> 译文：初始化 Bean 实例。
		// 4. 初始化 bean 实例
		Object exposedObject = bean;
		try {
			// 4.1 属性填充（依赖注入） --- important
			populateBean(beanName, mbd, instanceWrapper);

			// 4.2 执行初始化方法
			// 执行 一些 Aware 接口、BeanPostProcessor 和 InitializingBean.afterPropertiesSet and custom init methods
			exposedObject = initializeBean(beanName, exposedObject, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException bce && beanName.equals(bce.getBeanName())) {
				throw bce;
			}
			else {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
			}
		}

		// 5. 循环依赖检查
		if (earlySingletonExposure) {
			Object earlySingletonReference = getSingleton(beanName, false);
			if (earlySingletonReference != null) {
				if (exposedObject == bean) {
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					// 检查依赖
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
								"] in its raw version as part of a circular reference, but has eventually been " +
								"wrapped. This means that said other beans do not use the final version of the " +
								"bean. This is often the result of over-eager type matching - consider using " +
								"'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
					}
				}
			}
		}

		// Register bean as disposable. --> 译文：将 bean 注册为一次性的。
		// 6. 注册销毁方法
		try {
			// 注册 DisposableBean 和 destroy method
			// 执行 DestructionAwareBeanPostProcessor.requiresDestruction()
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException(
					mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
		}

		return exposedObject;
	}

	@Override
	@Nullable
	protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = determineTargetType(beanName, mbd, typesToMatch);
		// Apply SmartInstantiationAwareBeanPostProcessors to predict the
		// eventual type after a before-instantiation shortcut.
		if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			boolean matchingOnlyFactoryBean = (typesToMatch.length == 1 && typesToMatch[0] == FactoryBean.class);
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				Class<?> predicted = bp.predictBeanType(targetType, beanName);
				if (predicted != null &&
						(!matchingOnlyFactoryBean || FactoryBean.class.isAssignableFrom(predicted))) {
					return predicted;
				}
			}
		}
		return targetType;
	}

	/**
	 * Determine the target type for the given bean definition.
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd the merged bean definition for the bean
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return the type for the bean if determinable, or {@code null} otherwise
	 */
	// 确定给定 bean 定义的目标类型。
	// @param beanName bean 的名称（用于错误处理目的）
	// @param mbd bean 的合并 bean 定义
	// @param typesToMatch 在内部类型匹配目的的情况下要匹配的类型（也表示返回的 {@code Class} 永远不会暴露给应用程序代码）
	// @return 如果可确定，则返回 bean 的类型，否则返回 {@code null}
	@Nullable
	protected Class<?> determineTargetType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = mbd.getTargetType();
		if (targetType == null) {
			if (mbd.getFactoryMethodName() != null) {
				targetType = getTypeForFactoryMethod(beanName, mbd, typesToMatch);
			}
			else {
				targetType = resolveBeanClass(mbd, beanName, typesToMatch);
				if (mbd.hasBeanClass()) {
					targetType = getInstantiationStrategy().getActualBeanClass(mbd, beanName, this);
				}
			}
			if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
				mbd.resolvedTargetType = targetType;
			}
		}
		return targetType;
	}

	/**
	 * Determine the target type for the given bean definition which is based on
	 * a factory method. Only called if there is no singleton instance registered
	 * for the target bean already.
	 * <p>This implementation determines the type matching {@link #createBean}'s
	 * different creation strategies. As far as possible, we'll perform static
	 * type checking to avoid creation of the target bean.
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd the merged bean definition for the bean
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return the type for the bean if determinable, or {@code null} otherwise
	 * @see #createBean
	 */
	// 基于工厂方法确定给定 bean 定义的目标类型。仅当目标 bean 尚未注册单例实例时调用。
	// <p>此实现确定与 {@link #createBean} 不同创建策略匹配的类型。我们将尽可能执行静态类型检查以避免创建目标 bean。
	// @param beanName bean 的名称（用于错误处理）
	// @param mbd bean 的合并 bean 定义
	// @param typesToMatch 在内部类型匹配的情况下要匹配的类型（也表示返回的 {@code Class} 永远不会暴露给应用程序代码）
	// @return 如果可确定，则返回 bean 的类型，否则返回 {@code null}
	@Nullable
	protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		ResolvableType cachedReturnType = mbd.factoryMethodReturnType;
		if (cachedReturnType != null) {
			return cachedReturnType.resolve();
		}

		Class<?> commonType = null;
		Method uniqueCandidate = mbd.factoryMethodToIntrospect;

		if (uniqueCandidate == null) {
			Class<?> factoryClass;
			boolean isStatic = true;

			String factoryBeanName = mbd.getFactoryBeanName();
			if (factoryBeanName != null) {
				if (factoryBeanName.equals(beanName)) {
					throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
							"factory-bean reference points back to the same bean definition");
				}
				// Check declared factory method return type on factory class.
				factoryClass = getType(factoryBeanName);
				isStatic = false;
			}
			else {
				// Check declared factory method return type on bean class.
				factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
			}

			if (factoryClass == null) {
				return null;
			}
			factoryClass = ClassUtils.getUserClass(factoryClass);

			// If all factory methods have the same return type, return that type.
			// Can't clearly figure out exact method due to type converting / autowiring!
			int minNrOfArgs =
					(mbd.hasConstructorArgumentValues() ? mbd.getConstructorArgumentValues().getArgumentCount() : 0);
			Method[] candidates = this.factoryMethodCandidateCache.computeIfAbsent(factoryClass,
					clazz -> ReflectionUtils.getUniqueDeclaredMethods(clazz, ReflectionUtils.USER_DECLARED_METHODS));

			for (Method candidate : candidates) {
				if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate) &&
						candidate.getParameterCount() >= minNrOfArgs) {
					// Declared type variables to inspect?
					if (candidate.getTypeParameters().length > 0) {
						try {
							// Fully resolve parameter names and argument values.
							ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
							Class<?>[] paramTypes = candidate.getParameterTypes();
							String[] paramNames = null;
							if (cav.containsNamedArgument()) {
								ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
								if (pnd != null) {
									paramNames = pnd.getParameterNames(candidate);
								}
							}
							Set<ConstructorArgumentValues.ValueHolder> usedValueHolders = new HashSet<>(paramTypes.length);
							Object[] args = new Object[paramTypes.length];
							for (int i = 0; i < args.length; i++) {
								ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
										i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
								if (valueHolder == null) {
									valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
								}
								if (valueHolder != null) {
									args[i] = valueHolder.getValue();
									usedValueHolders.add(valueHolder);
								}
							}
							Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(
									candidate, args, getBeanClassLoader());
							uniqueCandidate = (commonType == null && returnType == candidate.getReturnType() ?
									candidate : null);
							commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
							if (commonType == null) {
								// Ambiguous return types found: return null to indicate "not determinable".
								return null;
							}
						}
						catch (Throwable ex) {
							if (logger.isDebugEnabled()) {
								logger.debug("Failed to resolve generic return type for factory method: " + ex);
							}
						}
					}
					else {
						uniqueCandidate = (commonType == null ? candidate : null);
						commonType = ClassUtils.determineCommonAncestor(candidate.getReturnType(), commonType);
						if (commonType == null) {
							// Ambiguous return types found: return null to indicate "not determinable".
							return null;
						}
					}
				}
			}

			mbd.factoryMethodToIntrospect = uniqueCandidate;
			if (commonType == null) {
				return null;
			}
		}

		// Common return type found: all factory methods return same type. For a non-parameterized
		// unique candidate, cache the full type declaration context of the target factory method.
		try {
			cachedReturnType = (uniqueCandidate != null ?
					ResolvableType.forMethodReturnType(uniqueCandidate) : ResolvableType.forClass(commonType));
			mbd.factoryMethodReturnType = cachedReturnType;
			return cachedReturnType.resolve();
		}
		catch (LinkageError err) {
			// E.g. a NoClassDefFoundError for a generic method return type
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to resolve type for factory method of bean '" + beanName + "': " +
						(uniqueCandidate != null ? uniqueCandidate : commonType), err);
			}
			return null;
		}
	}

	/**
	 * This implementation attempts to query the FactoryBean's generic parameter metadata
	 * if present to determine the object type. If not present, i.e. the FactoryBean is
	 * declared as a raw type, it checks the FactoryBean's {@code getObjectType} method
	 * on a plain instance of the FactoryBean, without bean properties applied yet.
	 * If this doesn't return a type yet and {@code allowInit} is {@code true}, full
	 * creation of the FactoryBean is attempted as fallback (through delegation to the
	 * superclass implementation).
	 * <p>The shortcut check for a FactoryBean is only applied in case of a singleton
	 * FactoryBean. If the FactoryBean instance itself is not kept as singleton,
	 * it will be fully created to check the type of its exposed object.
	 */
	// 此实现会尝试查询 FactoryBean 的泛型参数元数据（如果存在）以确定对象类型。
	// 如果不存在，即 FactoryBean 被声明为原始类型，则会在 FactoryBean 的普通实例上检查 FactoryBean
	// 的 {@code getObjectType} 方法（尚未应用 bean 属性）。
	// 如果该方法尚未返回类型且 {@code allowInit} 为 {@code true}，
	// 则会尝试完整创建 FactoryBean 作为后备方案（通过委托给超类实现）。
	// <p>FactoryBean 的快捷检查仅适用于单例 FactoryBean 的情况。
	// 如果 FactoryBean 实例本身未保持为单例，则会完整创建该实例以检查其暴露对象的类型。
	@Override
	protected ResolvableType getTypeForFactoryBean(String beanName, RootBeanDefinition mbd, boolean allowInit) {
		ResolvableType result;

		// Check if the bean definition itself has defined the type with an attribute
		try {
			result = getTypeForFactoryBeanFromAttributes(mbd);
			if (result != ResolvableType.NONE) {
				return result;
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
					String.valueOf(ex.getMessage()));
		}

		// For instance supplied beans, try the target type and bean class immediately
		if (mbd.getInstanceSupplier() != null) {
			result = getFactoryBeanGeneric(mbd.targetType);
			if (result.resolve() != null) {
				return result;
			}
			result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
			if (result.resolve() != null) {
				return result;
			}
		}

		// Consider factory methods
		String factoryBeanName = mbd.getFactoryBeanName();
		String factoryMethodName = mbd.getFactoryMethodName();

		// Scan the factory bean methods
		if (factoryBeanName != null) {
			if (factoryMethodName != null) {
				// Try to obtain the FactoryBean's object type from its factory method
				// declaration without instantiating the containing bean at all.
				BeanDefinition factoryBeanDefinition = getBeanDefinition(factoryBeanName);
				Class<?> factoryBeanClass;
				if (factoryBeanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition &&
						abstractBeanDefinition.hasBeanClass()) {
					factoryBeanClass = abstractBeanDefinition.getBeanClass();
				}
				else {
					RootBeanDefinition fbmbd = getMergedBeanDefinition(factoryBeanName, factoryBeanDefinition);
					factoryBeanClass = determineTargetType(factoryBeanName, fbmbd);
				}
				if (factoryBeanClass != null) {
					result = getTypeForFactoryBeanFromMethod(factoryBeanClass, factoryMethodName);
					if (result.resolve() != null) {
						return result;
					}
				}
			}
			// If not resolvable above and the referenced factory bean doesn't exist yet,
			// exit here - we don't want to force the creation of another bean just to
			// obtain a FactoryBean's object type...
			if (!isBeanEligibleForMetadataCaching(factoryBeanName)) {
				return ResolvableType.NONE;
			}
		}

		// If we're allowed, we can create the factory bean and call getObjectType() early
		if (allowInit) {
			FactoryBean<?> factoryBean = (mbd.isSingleton() ?
					getSingletonFactoryBeanForTypeCheck(beanName, mbd) :
					getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));
			if (factoryBean != null) {
				// Try to obtain the FactoryBean's object type from this early stage of the instance.
				Class<?> type = getTypeForFactoryBean(factoryBean);
				if (type != null) {
					return ResolvableType.forClass(type);
				}
				// No type found for shortcut FactoryBean instance:
				// fall back to full creation of the FactoryBean instance.
				return super.getTypeForFactoryBean(beanName, mbd, true);
			}
		}

		if (factoryBeanName == null && mbd.hasBeanClass() && factoryMethodName != null) {
			// No early bean instantiation possible: determine FactoryBean's type from
			// static factory method signature or from class inheritance hierarchy...
			return getTypeForFactoryBeanFromMethod(mbd.getBeanClass(), factoryMethodName);
		}

		// For regular beans, try the target type and bean class as fallback
		if (mbd.getInstanceSupplier() == null) {
			result = getFactoryBeanGeneric(mbd.targetType);
			if (result.resolve() != null) {
				return result;
			}
			result = getFactoryBeanGeneric(mbd.hasBeanClass() ? ResolvableType.forClass(mbd.getBeanClass()) : null);
			if (result.resolve() != null) {
				return result;
			}
		}

		// FactoryBean type not resolvable
		return ResolvableType.NONE;
	}

	/**
	 * Introspect the factory method signatures on the given bean class,
	 * trying to find a common {@code FactoryBean} object type declared there.
	 * @param beanClass the bean class to find the factory method on
	 * @param factoryMethodName the name of the factory method
	 * @return the common {@code FactoryBean} object type, or {@code null} if none
	 */
	// 检查给定 bean 类上的工厂方法签名，尝试找到在那里声明的通用 {@code FactoryBean} 对象类型。
	// @param beanClass 是要在其上查找工厂方法的 bean 类
	// @param factoryMethodName 是工厂方法的名称
	// @return 通用 {@code FactoryBean} 对象类型，如果没有则返回 {@code null}
	private ResolvableType getTypeForFactoryBeanFromMethod(Class<?> beanClass, String factoryMethodName) {
		// CGLIB subclass methods hide generic parameters; look at the original user class.
		Class<?> factoryBeanClass = ClassUtils.getUserClass(beanClass);
		FactoryBeanMethodTypeFinder finder = new FactoryBeanMethodTypeFinder(factoryMethodName);
		ReflectionUtils.doWithMethods(factoryBeanClass, finder, ReflectionUtils.USER_DECLARED_METHODS);
		return finder.getResult();
	}

	/**
	 * Obtain a reference for early access to the specified bean,
	 * typically for the purpose of resolving a circular reference.
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd the merged bean definition for the bean
	 * @param bean the raw bean instance
	 * @return the object to expose as bean reference
	 */
	// 获取指定 Bean 的引用，以便提前访问，通常用于解决循环引用。
	// @param beanName Bean 的名称（用于错误处理）
	// @param mbd Bean 的合并 Bean 定义
	// @param bean 原始 Bean 实例
	// @return 要作为 Bean 引用公开的对象
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				// 获取指定 bean 的引用以便提前访问，通常用于解决循环引用问题。
				// 执行 SmartInstantiationAwareBeanPostProcessor#getEarlyBeanReference() 方法
				exposedObject = bp.getEarlyBeanReference(exposedObject, beanName);
			}
		}
		return exposedObject;
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Obtain a "shortcut" singleton FactoryBean instance to use for a
	 * {@code getObjectType()} call, without full initialization of the FactoryBean.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return the FactoryBean instance, or {@code null} to indicate
	 * that we couldn't obtain a shortcut FactoryBean instance
	 */
	// 获取用于 {@code getObjectType()} 调用的“快捷方式”单例 FactoryBean 实例，而无需完全初始化 FactoryBean。
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @return FactoryBean 实例，或 {@code null} 表示我们无法获取快捷方式 FactoryBean 实例
	@Nullable
	private FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		synchronized (getSingletonMutex()) {
			BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);
			if (bw != null) {
				return (FactoryBean<?>) bw.getWrappedInstance();
			}
			Object beanInstance = getSingleton(beanName, false);
			if (beanInstance instanceof FactoryBean<?> factoryBean) {
				return factoryBean;
			}
			if (isSingletonCurrentlyInCreation(beanName) ||
					(mbd.getFactoryBeanName() != null && isSingletonCurrentlyInCreation(mbd.getFactoryBeanName()))) {
				return null;
			}

			Object instance;
			try {
				// Mark this bean as currently in creation, even if just partially.
				beforeSingletonCreation(beanName);
				// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
				instance = resolveBeforeInstantiation(beanName, mbd);
				if (instance == null) {
					bw = createBeanInstance(beanName, mbd, null);
					instance = bw.getWrappedInstance();
				}
			}
			catch (UnsatisfiedDependencyException ex) {
				// Don't swallow, probably misconfiguration...
				throw ex;
			}
			catch (BeanCreationException ex) {
				// Don't swallow a linkage error since it contains a full stacktrace on
				// first occurrence... and just a plain NoClassDefFoundError afterwards.
				if (ex.contains(LinkageError.class)) {
					throw ex;
				}
				// Instantiation failure, maybe too early...
				if (logger.isDebugEnabled()) {
					logger.debug("Bean creation exception on singleton FactoryBean type check: " + ex);
				}
				onSuppressedException(ex);
				return null;
			}
			finally {
				// Finished partial creation of this bean.
				afterSingletonCreation(beanName);
			}

			FactoryBean<?> fb = getFactoryBean(beanName, instance);
			if (bw != null) {
				this.factoryBeanInstanceCache.put(beanName, bw);
			}
			return fb;
		}
	}

	/**
	 * Obtain a "shortcut" non-singleton FactoryBean instance to use for a
	 * {@code getObjectType()} call, without full initialization of the FactoryBean.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return the FactoryBean instance, or {@code null} to indicate
	 * that we couldn't obtain a shortcut FactoryBean instance
	 */
	// 获取用于 {@code getObjectType()} 调用的“快捷方式”非单例 FactoryBean 实例，而无需完全初始化 FactoryBean。
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @return FactoryBean 实例，或 {@code null} 表示我们无法获取快捷方式 FactoryBean 实例
	@Nullable
	private FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		if (isPrototypeCurrentlyInCreation(beanName)) {
			return null;
		}

		Object instance;
		try {
			// Mark this bean as currently in creation, even if just partially.
			beforePrototypeCreation(beanName);
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			instance = resolveBeforeInstantiation(beanName, mbd);
			if (instance == null) {
				BeanWrapper bw = createBeanInstance(beanName, mbd, null);
				instance = bw.getWrappedInstance();
			}
		}
		catch (UnsatisfiedDependencyException ex) {
			// Don't swallow, probably misconfiguration...
			throw ex;
		}
		catch (BeanCreationException ex) {
			// Instantiation failure, maybe too early...
			if (logger.isDebugEnabled()) {
				logger.debug("Bean creation exception on non-singleton FactoryBean type check: " + ex);
			}
			onSuppressedException(ex);
			return null;
		}
		finally {
			// Finished partial creation of this bean.
			afterPrototypeCreation(beanName);
		}

		return getFactoryBean(beanName, instance);
	}

	/**
	 * Apply MergedBeanDefinitionPostProcessors to the specified bean definition,
	 * invoking their {@code postProcessMergedBeanDefinition} methods.
	 * @param mbd the merged bean definition for the bean
	 * @param beanType the actual type of the managed bean instance
	 * @param beanName the name of the bean
	 * @see MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
	 */
	// 将 MergedBeanDefinitionPostProcessors 应用于指定的 bean 定义，并调用其 {@code postProcessMergedBeanDefinition} 方法。
	// @param mbd bean 的合并 bean 定义
	// @param beanType 托管 bean 实例的实际类型
	// @param beanName bean 的名称
	protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) {
		for (MergedBeanDefinitionPostProcessor processor : getBeanPostProcessorCache().mergedDefinition) {
			// 执行 MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition() 方法
			processor.postProcessMergedBeanDefinition(mbd, beanType, beanName);
		}
	}

	/**
	 * Apply before-instantiation post-processors, resolving whether there is a
	 * before-instantiation shortcut for the specified bean.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return the shortcut-determined bean instance, or {@code null} if none
	 */
	// 应用实例化前后处理器，解析指定 bean 是否存在实例化前快捷方式。
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @return 快捷方式确定的 bean 实例，如果没有则返回 {@code null}
	@SuppressWarnings("deprecation")
	@Nullable
	protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			// Make sure bean class is actually resolved at this point. --> 译文：确保此时 bean 类确实已被解析。
			if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
				Class<?> targetType = determineTargetType(beanName, mbd); // 确定给定 bean 定义的目标类型
				if (targetType != null) {
					// 执行 InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation() 方法
					bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
					if (bean != null) {
						// 执行 BeanPostProcessor.postProcessAfterInitialization() 方法
						bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
					}
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}

	/**
	 * Apply InstantiationAwareBeanPostProcessors to the specified bean definition
	 * (by class and name), invoking their {@code postProcessBeforeInstantiation} methods.
	 * <p>Any returned object will be used as the bean instead of actually instantiating
	 * the target bean. A {@code null} return value from the post-processor will
	 * result in the target bean being instantiated.
	 * @param beanClass the class of the bean to be instantiated
	 * @param beanName the name of the bean
	 * @return the bean object to use instead of a default instance of the target bean, or {@code null}
	 * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
	 */
	// 将 InstantiationAwareBeanPostProcessors 应用于指定的 bean 定义（按类和名称），并调用其 {@code postProcessBeforeInstantiation} 方法。
	// <p>任何返回的对象都将用作 bean，而不是实际实例化目标 bean。如果后处理器返回 {@code null} 值，则将实例化目标 bean。
	// @param beanClass 待实例化的 bean 的类
	// @param beanName bean 的名称
	// @return 用来代替目标 bean 默认实例的 bean 对象，或者 {@code null}
	@Nullable
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
		for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
			// 执行 InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation() 方法
			Object result = bp.postProcessBeforeInstantiation(beanClass, beanName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Create a new instance for the specified bean, using an appropriate instantiation strategy:
	 * factory method, constructor autowiring, or simple instantiation.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @param args explicit arguments to use for constructor or factory method invocation
	 * @return a BeanWrapper for the new instance
	 * @see #obtainFromSupplier
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 * @see #instantiateBean
	 */
	// 使用适当的实例化策略为指定的 bean 创建新实例：工厂方法、构造函数自动装配或简单实例化。
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @param args 用于构造函数或工厂方法调用的显式参数
	// @return 新实例的 BeanWrapper
	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
		// Make sure bean class is actually resolved at this point. --> 译文：确保此时 bean 类确实已被解析。
		// 1. 解析 bean 的 Class
		Class<?> beanClass = resolveBeanClass(mbd, beanName);

		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
		}

		// 2. 检查是否有 Supplier；有，则使用 supplier 获取 bean 实例
		if (args == null) {
			Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
			if (instanceSupplier != null) {
				return obtainFromSupplier(instanceSupplier, beanName, mbd);
			}
		}

		// 3. 如果有工厂方法，使用工厂方法创建
		if (mbd.getFactoryMethodName() != null) {
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		// Shortcut when re-creating the same bean... --> 译文：重新创建同一个 bean 时的快捷方式...
		boolean resolved = false;
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}
		// 4. 如果有解析过的构造函数，直接使用
		if (resolved) {
			if (autowireNecessary) {
				return autowireConstructor(beanName, mbd, null, null); // 自动装配构造函数
			}
			else {
				return instantiateBean(beanName, mbd); // 默认构造函数
			}
		}

		// Candidate constructors for autowiring? --> 译文：自动装配的候选构造函数？
		// SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors()
		// 5. 选择合适的构造函数
		// 执行 SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors() 方法
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
			return autowireConstructor(beanName, mbd, ctors, args); // 使用构造函数自动装配
		}

		// Preferred constructors for default construction? --> 译文：用于默认构造的首选构造函数？
		ctors = mbd.getPreferredConstructors();
		if (ctors != null) {
			return autowireConstructor(beanName, mbd, ctors, null);
		}

		// No special handling: simply use no-arg constructor. --> 译文：无特殊处理：直接使用无参数构造函数。
		// 6. 默认使用无参构造函数
		return instantiateBean(beanName, mbd);
	}

	/**
	 * Obtain a bean instance from the given supplier.
	 * @param supplier the configured supplier
	 * @param beanName the corresponding bean name
	 * @return a BeanWrapper for the new instance
	 */
	// 从指定的供应商获取一个 bean 实例。
	// @param supplier 已配置的供应商
	// @param beanName 对应的 bean 名称
	// @return 新实例的 BeanWrapper
	private BeanWrapper obtainFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd) {
		String outerBean = this.currentlyCreatedBean.get();
		this.currentlyCreatedBean.set(beanName);
		Object instance;

		try {
			instance = obtainInstanceFromSupplier(supplier, beanName, mbd);
		}
		catch (Throwable ex) {
			if (ex instanceof BeansException beansException) {
				throw beansException;
			}
			throw new BeanCreationException(beanName, "Instantiation of supplied bean failed", ex);
		}
		finally {
			if (outerBean != null) {
				this.currentlyCreatedBean.set(outerBean);
			}
			else {
				this.currentlyCreatedBean.remove();
			}
		}

		if (instance == null) {
			instance = new NullBean();
		}
		BeanWrapper bw = new BeanWrapperImpl(instance);
		initBeanWrapper(bw);
		return bw;
	}

	/**
	 * Obtain a bean instance from the given supplier.
	 * @param supplier the configured supplier
	 * @param beanName the corresponding bean name
	 * @param mbd the bean definition for the bean
	 * @return the bean instance (possibly {@code null})
	 * @since 6.0.7
	 */
	// 从指定的 supplier 获取 bean 实例。
	// @param supplier 已配置的供应商
	// @param beanName 对应的 bean 名称
	// @param mbd bean 的定义
	// @return bean 实例（可能为 {@code null}）
	@Nullable
	protected Object obtainInstanceFromSupplier(Supplier<?> supplier, String beanName, RootBeanDefinition mbd)
			throws Exception {

		if (supplier instanceof ThrowingSupplier<?> throwingSupplier) {
			return throwingSupplier.getWithException();
		}
		return supplier.get();
	}

	/**
	 * Overridden in order to implicitly register the currently created bean as
	 * dependent on further beans getting programmatically retrieved during a
	 * {@link Supplier} callback.
	 * @since 5.0
	 * @see #obtainFromSupplier
	 */
	@Override
	protected Object getObjectForBeanInstance(
			Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {

		String currentlyCreatedBean = this.currentlyCreatedBean.get();
		if (currentlyCreatedBean != null) {
			registerDependentBean(beanName, currentlyCreatedBean);
		}

		return super.getObjectForBeanInstance(beanInstance, name, beanName, mbd);
	}

	/**
	 * Determine candidate constructors to use for the given bean, checking all registered
	 * {@link SmartInstantiationAwareBeanPostProcessor SmartInstantiationAwareBeanPostProcessors}.
	 * @param beanClass the raw class of the bean
	 * @param beanName the name of the bean
	 * @return the candidate constructors, or {@code null} if none specified
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors
	 */
	// 确定给定 bean 的候选构造函数，检查所有已注册的 {@link SmartInstantiationAwareBeanPostProcessor SmartInstantiationAwareBeanPostProcessors}。
	// @param beanClass bean 的原始类
	// @param beanName bean 的名称
	// @return 候选构造函数，如果没有指定，则返回 {@code null}
	// @throws org.springframework.beans.BeansException 如果发生错误
	@Nullable
	protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(@Nullable Class<?> beanClass, String beanName)
			throws BeansException {

		if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
			for (SmartInstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().smartInstantiationAware) {
				// 执行 SmartInstantiationAwareBeanPostProcessor#determineCandidateConstructors() 方法
				Constructor<?>[] ctors = bp.determineCandidateConstructors(beanClass, beanName);
				if (ctors != null) {
					return ctors;
				}
			}
		}
		return null;
	}

	/**
	 * Instantiate the given bean using its default constructor.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return a BeanWrapper for the new instance
	 */
	// 使用默认构造函数实例化给定的 bean。
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @return 新实例的 BeanWrapper
	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mbd) {
		try {
			Object beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, this);
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
		}
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the
	 * mbd parameter specifies a class, rather than a factoryBean, or an instance variable
	 * on a factory object itself configured using Dependency Injection.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @param explicitArgs argument values passed in programmatically via the getBean method,
	 * or {@code null} if none (implying the use of constructor argument values from bean definition)
	 * @return a BeanWrapper for the new instance
	 * @see #getBean(String, Object[])
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(
			String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {

		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
	}

	/**
	 * "autowire constructor" (with constructor arguments by type) behavior.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based
	 * dependency resolution.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @param ctors the chosen candidate constructors
	 * @param explicitArgs argument values passed in programmatically via the getBean method,
	 * or {@code null} if none (implying the use of constructor argument values from bean definition)
	 * @return a BeanWrapper for the new instance
	 */
	// “自动装配构造函数”（按类型传递构造函数参数）行为。如果显式指定了构造函数参数值，也会应用此行为，并将所有剩余参数与 bean 工厂中的 bean 进行匹配。
	//
	// <p>这对应于构造函数注入：在此模式下，Spring bean 工厂能够承载需要基于构造函数的依赖项解析的组件。
	//
	// @param beanName bean 的名称
	// @param mbd bean 的定义
	// @param ctors 选定的候选构造函数
	// @param explicitArgs 通过 getBean 方法以编程方式传递的参数值，如果没有传递参数值，则为 {@code null}（表示使用 bean 定义中的构造函数参数值）
	// @return 新实例的 BeanWrapper
	protected BeanWrapper autowireConstructor(
			String beanName, RootBeanDefinition mbd, @Nullable Constructor<?>[] ctors, @Nullable Object[] explicitArgs) {

		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
	}

	/**
	 * Populate the bean instance in the given BeanWrapper with the property values
	 * from the bean definition.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @param bw the BeanWrapper with bean instance
	 */
	// 使用 Bean 定义中的属性值填充给定 BeanWrapper 中的 Bean 实例。
	// @param beanName Bean 的名称
	// @param mbd Bean 的 Bean 定义
	// @param bw 包含 Bean 实例的 BeanWrapper
	protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
		// 1. 检查 Bean 是否为 null
		if (bw == null) {
			if (mbd.hasPropertyValues()) { // bean 定义了属性值
				// 无法将属性值应用于空实例
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
			}
			else {
				// Skip property population phase for null instance. --> 译文：跳过空实例的属性填充阶段。
				return;
			}
		}

		// 2. Bean 实例的 Class 是 record 类型 -> 不处理
		if (bw.getWrappedClass().isRecord()) {
			if (mbd.hasPropertyValues()) {
				// 无法将属性值应用于 record
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to a record");
			}
			else {
				// Skip property population phase for records since they are immutable. --> 译文：跳过记录的属性填充阶段，因为它们是不可变的。
				return;
			}
		}

		// Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
		// state of the bean before properties are set. This can be used, for example,
		// to support styles of field injection.
		// --> 译文：为所有 InstantiationAwareBeanPostProcessors 提供在设置属性之前修改 Bean 状态的机会。例如，这可以用于支持字段注入的样式。
		// 3. 对用户定义的 Bean 实例提供在设置属性之前修改 Bean 状态的机会
		// 执行 InstantiationAwareBeanPostProcessor 的 postProcessAfterInstantiation 方法
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
				// 执行 InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation()
				if (!bp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
					return;
				}
			}
		}

		// 4. 获取 bean 的属性值
		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

		// 5. 处理自动装配
		int resolvedAutowireMode = mbd.getResolvedAutowireMode();
		if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
			// Add property values based on autowire by name if applicable. --> 译文：如果适用，则根据名称自动装配添加属性值。
			if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}
			// Add property values based on autowire by type if applicable. --> 译文：如果适用，则根据类型自动装配添加属性值。
			if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}
			pvs = newPvs;
		}

		// 6. 使用 InstantiationAwareBeanPostProcessor#postProcessProperties() 方法处理属性值
		if (hasInstantiationAwareBeanPostProcessors()) {
			if (pvs == null) {
				pvs = mbd.getPropertyValues();
			}
			for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
				// 执行 InstantiationAwareBeanPostProcessor#postProcessProperties() 方法
				PropertyValues pvsToUse = bp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
				if (pvsToUse == null) {
					return;
				}
				pvs = pvsToUse;
			}
		}

		// 7. 进行依赖关系检查
		boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
		if (needsDepCheck) { // 进行依赖关系检查
			PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			// 如果需要，执行依赖项检查，确保所有公开的属性都已设置。
			checkDependencies(beanName, mbd, filteredPds, pvs);
		}

		// 8. 应用属性值
		if (pvs != null) {
			applyPropertyValues(beanName, mbd, bw, pvs); // 应用属性值；即：给相应的属性设置值
		}
	}

	/**
	 * Fill in any missing property values with references to
	 * other beans in this factory if autowire is set to "byName".
	 * @param beanName the name of the bean we're wiring up.
	 * Useful for debugging messages; not used functionally.
	 * @param mbd bean definition to update through autowiring
	 * @param bw the BeanWrapper from which we can obtain information about the bean
	 * @param pvs the PropertyValues to register wired objects with
	 */
	// 如果自动装配设置为 “byName”，则使用工厂中其他 bean 的引用填充任何缺失的属性值。
	// @param beanName 要装配的 bean 的名称。用于调试消息；功能上不适用。
	// @param mbd 要通过自动装配更新的 bean 定义
	// @param bw BeanWrapper，我们可以从中获取有关 bean 的信息
	// @param pvs 要注册装配对象的 PropertyValues
	protected void autowireByName(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				registerDependentBean(propertyName, beanName);
				if (logger.isTraceEnabled()) {
					logger.trace("Added autowiring by name from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
							"' by name: no matching bean found");
				}
			}
		}
	}

	/**
	 * Abstract method defining "autowire by type" (bean properties by type) behavior.
	 * <p>This is like PicoContainer default, in which there must be exactly one bean
	 * of the property type in the bean factory. This makes bean factories simple to
	 * configure for small namespaces, but doesn't work as well as standard Spring
	 * behavior for bigger applications.
	 * @param beanName the name of the bean to autowire by type
	 * @param mbd the merged bean definition to update through autowiring
	 * @param bw the BeanWrapper from which we can obtain information about the bean
	 * @param pvs the PropertyValues to register wired objects with
	 */
	// 定义“按类型自动装配”（按类型装配 bean 属性）行为的抽象方法。
	//
	// <p>这类似于 PicoContainer 的默认行为，即 bean 工厂中必须恰好存在一个具有该属性类型的 bean。
	// 这使得 bean 工厂在小型命名空间中易于配置，但在大型应用程序中不如标准的 Spring 行为有效。
	// @param beanName 要按类型自动装配的 bean 的名称
	// @param mbd 要通过自动装配更新的合并 bean 定义
	// @param bw 可以从中获取 bean 信息的 BeanWrapper
	// @param pvs 要注册已装配对象的 PropertyValues
	protected void autowireByType(
			String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}

		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		Set<String> autowiredBeanNames = new LinkedHashSet<>(propertyNames.length * 2);
		for (String propertyName : propertyNames) {
			try {
				PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
				// Don't try autowiring by type for type Object: never makes sense,
				// even if it technically is an unsatisfied, non-simple property.
				if (Object.class != pd.getPropertyType()) {
					MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
					// Do not allow eager init for type matching in case of a prioritized post-processor.
					boolean eager = !(bw.getWrappedInstance() instanceof PriorityOrdered);
					DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
					Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
					if (autowiredArgument != null) {
						pvs.add(propertyName, autowiredArgument);
					}
					for (String autowiredBeanName : autowiredBeanNames) {
						registerDependentBean(autowiredBeanName, beanName);
						if (logger.isTraceEnabled()) {
							logger.trace("Autowiring by type from bean name '" + beanName + "' via property '" +
									propertyName + "' to bean named '" + autowiredBeanName + "'");
						}
					}
					autowiredBeanNames.clear();
				}
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
			}
		}
	}


	/**
	 * Return an array of non-simple bean properties that are unsatisfied.
	 * These are probably unsatisfied references to other beans in the
	 * factory. Does not include simple properties like primitives or Strings.
	 * @param mbd the merged bean definition the bean was created with
	 * @param bw the BeanWrapper the bean was created with
	 * @return an array of bean property names
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	// 返回一个包含未满足的非简单 bean 属性的数组。这些属性可能是工厂中其他 bean 的未满足引用。不包含简单属性，例如基本类型或字符串。
	// @param mbd 创建 bean 时使用的合并 bean 定义
	// @param bw 创建 bean 时使用的 BeanWrapper
	// @return bean 属性名称数组
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<>();
		PropertyValues pvs = mbd.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
					!BeanUtils.isSimpleProperty(pd.getPropertyType())) {
				result.add(pd.getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
	 * excluding ignored dependency types or properties defined on ignored dependency interfaces.
	 * @param bw the BeanWrapper the bean was created with
	 * @param cache whether to cache filtered PropertyDescriptors for the given bean Class
	 * @return the filtered PropertyDescriptors
	 * @see #isExcludedFromDependencyCheck
	 * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
	 */
	// 从给定的 BeanWrapper 中提取经过筛选的 PropertyDescriptors 集合，排除忽略的依赖类型或在忽略的依赖接口上定义的属性。
	// @param bw 创建 bean 时使用的 BeanWrapper
	// @param cache 是否缓存给定 bean 类的已筛选 PropertyDescriptors
	// @return 已筛选的 PropertyDescriptors
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
		PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
		if (filtered == null) {
			filtered = filterPropertyDescriptorsForDependencyCheck(bw);
			if (cache) {
				PropertyDescriptor[] existing =
						this.filteredPropertyDescriptorsCache.putIfAbsent(bw.getWrappedClass(), filtered);
				if (existing != null) {
					filtered = existing;
				}
			}
		}
		return filtered;
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
	 * excluding ignored dependency types or properties defined on ignored dependency interfaces.
	 * @param bw the BeanWrapper the bean was created with
	 * @return the filtered PropertyDescriptors
	 * @see #isExcludedFromDependencyCheck
	 */
	// 从给定的 BeanWrapper 中提取经过筛选的 PropertyDescriptors 集合，排除忽略的依赖类型或在忽略的依赖接口上定义的属性。
	// @param bw 创建 bean 时使用的 BeanWrapper
	// @return 筛选后的 PropertyDescriptors
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
		List<PropertyDescriptor> pds = new ArrayList<>(Arrays.asList(bw.getPropertyDescriptors()));
		pds.removeIf(this::isExcludedFromDependencyCheck);
		return pds.toArray(new PropertyDescriptor[0]);
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB and
	 * properties whose type matches an ignored dependency type or which
	 * are defined by an ignored dependency interface.
	 * @param pd the PropertyDescriptor of the bean property
	 * @return whether the bean property is excluded
	 * @see #ignoreDependencyType(Class)
	 * @see #ignoreDependencyInterface(Class)
	 */
	// 确定给定的 bean 属性是否从依赖项检查中排除。
	// <p>此实现会排除由 CGLIB 定义的属性，以及类型与已忽略的依赖项类型匹配或由已忽略的依赖项接口定义的属性。</p>
	// @param pd bean 属性的 PropertyDescriptor
	// @return bean 属性是否被排除
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				this.ignoredDependencyTypes.contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
	}

	/**
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition the bean was created with
	 * @param pds the relevant property descriptors for the target bean
	 * @param pvs the property values to be applied to the bean
	 * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 */
	// 如果需要，执行依赖项检查，确保所有公开的属性都已设置。依赖项检查可以针对对象（协作 bean）、简单类型（基本类型和字符串）或全部类型（两者都有）。
	// @param beanName bean 的名称
	// @param mbd 创建 bean 时使用的合并 bean 定义
	// @param pds 目标 bean 的相关属性描述符
	// @param pvs 要应用于 bean 的属性值
	protected void checkDependencies(
			String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, @Nullable PropertyValues pvs)
			throws UnsatisfiedDependencyException {

		int dependencyCheck = mbd.getDependencyCheck();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && (pvs == null || !pvs.contains(pd.getName()))) {
				boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
				boolean unsatisfied = (dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_ALL) ||
						(isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
						(!isSimple && dependencyCheck == AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					// 设置此属性值或禁用此 bean 的依赖项检查。
					throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(),
							"Set this property value or disable dependency checking for this bean.");
				}
			}
		}
	}

	/**
	 * Apply the given property values, resolving any runtime references
	 * to other beans in this bean factory. Must use deep copy, so we
	 * don't permanently modify this property.
	 * @param beanName the bean name passed for better exception information
	 * @param mbd the merged bean definition
	 * @param bw the BeanWrapper wrapping the target object
	 * @param pvs the new property values
	 */
	// 应用给定的属性值，并解析此 bean 工厂中对其他 bean 的任何运行时引用。必须使用深拷贝，以避免永久修改此属性。
	// @param beanName 传递的 bean 名称，用于提供更清晰的异常信息
	// @param mbd 合并后的 bean 定义
	// @param bw 包装目标对象的 BeanWrapper
	// @param pvs 新的属性值
	protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
		if (pvs.isEmpty()) {
			return;
		}

		MutablePropertyValues mpvs = null;
		List<PropertyValue> original;

		if (pvs instanceof MutablePropertyValues _mpvs) {
			mpvs = _mpvs;
			if (mpvs.isConverted()) {
				// Shortcut: use the pre-converted values as-is. --> 译文：简便方法：直接使用转换前的值。
				try {
					bw.setPropertyValues(mpvs);
					return;
				}
				catch (BeansException ex) {
					throw new BeanCreationException(
							mbd.getResourceDescription(), beanName, "Error setting property values", ex);
				}
			}
			original = mpvs.getPropertyValueList();
		}
		else {
			original = Arrays.asList(pvs.getPropertyValues());
		}

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

		// Create a deep copy, resolving any references for values. --> 译文：创建深拷贝，并解析所有值引用。
		List<PropertyValue> deepCopy = new ArrayList<>(original.size());
		boolean resolveNecessary = false;
		for (PropertyValue pv : original) {
			if (pv.isConverted()) {
				deepCopy.add(pv);
			}
			else {
				String propertyName = pv.getName();
				Object originalValue = pv.getValue();
				if (originalValue == AutowiredPropertyMarker.INSTANCE) {
					Method writeMethod = bw.getPropertyDescriptor(propertyName).getWriteMethod();
					if (writeMethod == null) {
						throw new IllegalArgumentException("Autowire marker for property without write method: " + pv);
					}
					originalValue = new DependencyDescriptor(new MethodParameter(writeMethod, 0), true);
				}
				// 给定一个 PropertyValue，返回一个值
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;
				boolean convertible = isConvertibleProperty(propertyName, bw);
				if (convertible) {
					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
				}
				// Possibly store converted value in merged bean definition,
				// in order to avoid re-conversion for every created bean instance.
				// --> 译文：可以将转换后的值存储在合并后的 bean 定义中，以避免每次创建 bean 实例时都重新转换。
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				}
				else if (convertible && originalValue instanceof TypedStringValue typedStringValue &&
						!typedStringValue.isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				}
				else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		if (mpvs != null && !resolveNecessary) {
			mpvs.setConverted();
		}

		// Set our (possibly massaged) deep copy. --> 译文：设置我们（可能经过润色的）深度复制。
		try {
			bw.setPropertyValues(new MutablePropertyValues(deepCopy)); // 属性值设置
		}
		catch (BeansException ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
		}
	}

	/**
	 * Determine whether the factory should cache a converted value for the given property.
	 */
	// 确定工厂是否应该缓存给定属性的转换值。
	private boolean isConvertibleProperty(String propertyName, BeanWrapper bw) {
		try {
			return !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName) &&
					BeanUtils.hasUniqueWriteMethod(bw.getPropertyDescriptor(propertyName));
		}
		catch (InvalidPropertyException ex) {
			return false;
		}
	}

	/**
	 * Convert the given value for the specified target property.
	 */
	// 将给定值转换为指定目标属性的值。
	@Nullable
	private Object convertForProperty(
			@Nullable Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {

		if (converter instanceof BeanWrapperImpl beanWrapper) {
			return beanWrapper.convertForProperty(value, propertyName);
		}
		else {
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
			return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
		}
	}


	/**
	 * Initialize the given bean instance, applying factory callbacks
	 * as well as init methods and bean post processors.
	 * <p>Called from {@link #createBean} for traditionally defined beans,
	 * and from {@link #initializeBean} for existing bean instances.
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param bean the new bean instance we may need to initialize
	 * @param mbd the bean definition that the bean was created with
	 * (can also be {@code null}, if given an existing bean instance)
	 * @return the initialized bean instance (potentially wrapped)
	 * @see BeanNameAware
	 * @see BeanClassLoaderAware
	 * @see BeanFactoryAware
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #invokeInitMethods
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	// 初始化给定的 bean 实例，应用工厂回调、init 方法和 bean 后置处理器。
	// <p>对于传统定义的 bean，通过 {@link #createBean} 调用；对于现有的 bean 实例，通过 {@link #initializeBean} 调用。
	// @param beanName 工厂中的 bean 名称（用于调试目的）
	// @param bean 我们可能需要初始化的新 bean 实例
	// @param mbd 创建 bean 时使用的 bean 定义（如果给定了现有的 bean 实例，也可以为 {@code null}）
	// @return 已初始化的 bean 实例（可能已包装）
	@SuppressWarnings("deprecation")
	protected Object initializeBean(String beanName, Object bean, @Nullable RootBeanDefinition mbd) {
		// 1. 调用 Aware 接口的方法
		// 执行一些 aware methods：BeanNameAware、BeanClassLoaderAware、BeanFactoryAware
		invokeAwareMethods(beanName, bean);

		// 2. 应用 BeanPostProcessor 的 postProcessBeforeInitialization() 方法
		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
			// 执行 BeanPostProcessor#postProcessBeforeInitialization() (@Deprecated(since = "6.1"))
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		// 3. 调用初始化方法
		try {
			// 执行 InitializingBean.afterPropertiesSet() 和 自定义 init 方法
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null), beanName, ex.getMessage(), ex);
		}

		// 4. 应用 BeanPostProcessor 的 postProcessAfterInitialization() 方法
		if (mbd == null || !mbd.isSynthetic()) {
			// 执行 BeanPostProcessor#postProcessAfterInitialization() (@Deprecated(since = "6.1"))
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}

		return wrappedBean;
	}

	private void invokeAwareMethods(String beanName, Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof BeanNameAware beanNameAware) {
				beanNameAware.setBeanName(beanName);
			}
			if (bean instanceof BeanClassLoaderAware beanClassLoaderAware) {
				ClassLoader bcl = getBeanClassLoader();
				if (bcl != null) {
					beanClassLoaderAware.setBeanClassLoader(bcl);
				}
			}
			if (bean instanceof BeanFactoryAware beanFactoryAware) {
				beanFactoryAware.setBeanFactory(AbstractAutowireCapableBeanFactory.this);
			}
		}
	}

	/**
	 * Give a bean a chance to initialize itself after all its properties are set,
	 * and a chance to know about its owning bean factory (this object).
	 * <p>This means checking whether the bean implements {@link InitializingBean}
	 * or defines any custom init methods, and invoking the necessary callback(s)
	 * if it does.
	 * @param beanName the bean name in the factory (for debugging purposes)
	 * @param bean the new bean instance we may need to initialize
	 * @param mbd the merged bean definition that the bean was created with
	 * (can also be {@code null}, if given an existing bean instance)
	 * @throws Throwable if thrown by init methods or by the invocation process
	 * @see #invokeCustomInitMethod
	 */
	// 在设置所有属性后，让 Bean 有机会初始化自身，并了解其所属的 Bean 工厂（this 对象）。
	// <p>这意味着检查 Bean 是否实现了 {@link InitializingBean} 或定义了任何自定义的 init 方法，如果是，则调用必要的回调。
	// @param beanName 工厂中的 Bean 名称（用于调试目的）
	// @param bean 我们可能需要初始化的新 Bean 实例
	// @param mbd 创建 Bean 时使用的合并 Bean 定义（如果给定了现有的 Bean 实例，也可以为 {@code null}）
	// @throws 如果由 init 方法或调用过程抛出，则为 Throwable
	protected void invokeInitMethods(String beanName, Object bean, @Nullable RootBeanDefinition mbd)
			throws Throwable {

		// 1. 调用 InitializingBean 的 afterPropertiesSet() 方法
		boolean isInitializingBean = (bean instanceof InitializingBean);
		if (isInitializingBean && (mbd == null || !mbd.hasAnyExternallyManagedInitMethod("afterPropertiesSet"))) {
			if (logger.isTraceEnabled()) {
				logger.trace("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			// 执行 InitializingBean#afterPropertiesSet() 方法
			((InitializingBean) bean).afterPropertiesSet();
		}

		// 2. 调用自定义的 init-method
		if (mbd != null && bean.getClass() != NullBean.class) {
			String[] initMethodNames = mbd.getInitMethodNames();
			if (initMethodNames != null) {
				for (String initMethodName : initMethodNames) {
					if (StringUtils.hasLength(initMethodName) &&
							!(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
							!mbd.hasAnyExternallyManagedInitMethod(initMethodName)) {
						// 对给定的 bean 调用指定的自定义初始化方法。
						invokeCustomInitMethod(beanName, bean, mbd, initMethodName);
					}
				}
			}
		}
	}

	/**
	 * Invoke the specified custom init method on the given bean.
	 * <p>Called by {@link #invokeInitMethods(String, Object, RootBeanDefinition)}.
	 * <p>Can be overridden in subclasses for custom resolution of init methods
	 * with arguments.
	 * @see #invokeInitMethods
	 */
	// 对给定的 bean 调用指定的自定义初始化方法。
	// <p>由 {@link #invokeInitMethods(String, Object, RootBeanDefinition)} 调用。
	// <p>可在子类中重写，以自定义初始化方法的解析。
	protected void invokeCustomInitMethod(String beanName, Object bean, RootBeanDefinition mbd, String initMethodName)
			throws Throwable {

		Class<?> beanClass = bean.getClass();
		MethodDescriptor descriptor = MethodDescriptor.create(beanName, beanClass, initMethodName);
		String methodName = descriptor.methodName();

		Method initMethod = (mbd.isNonPublicAccessAllowed() ?
				BeanUtils.findMethod(descriptor.declaringClass(), methodName) :
				ClassUtils.getMethodIfAvailable(beanClass, methodName));

		if (initMethod == null) {
			if (mbd.isEnforceInitMethod()) {
				throw new BeanDefinitionValidationException("Could not find an init method named '" +
						methodName + "' on bean with name '" + beanName + "'");
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("No default init method named '" + methodName +
							"' found on bean with name '" + beanName + "'");
				}
				// Ignore non-existent default lifecycle methods.
				return;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Invoking init method '" + methodName + "' on bean with name '" + beanName + "'");
		}
		Method methodToInvoke = ClassUtils.getInterfaceMethodIfPossible(initMethod, beanClass);

		try {
			ReflectionUtils.makeAccessible(methodToInvoke);
			methodToInvoke.invoke(bean);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	/**
	 * Applies the {@code postProcessAfterInitialization} callback of all
	 * registered BeanPostProcessors, giving them a chance to post-process the
	 * object obtained from FactoryBeans (for example, to auto-proxy them).
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	// 应用所有已注册的 BeanPostProcessor 的 {@code postProcessAfterInitialization} 回调函数，
	// 使它们有机会对从 FactoryBeans 获取的对象进行后处理（例如，自动代理它们）。
	@SuppressWarnings("deprecation")
	@Override
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return applyBeanPostProcessorsAfterInitialization(object, beanName);
	}

	/**
	 * Overridden to clear FactoryBean instance cache as well.
	 */
	@Override
	protected void removeSingleton(String beanName) {
		synchronized (getSingletonMutex()) {
			super.removeSingleton(beanName);
			this.factoryBeanInstanceCache.remove(beanName);
		}
	}

	/**
	 * Overridden to clear FactoryBean instance cache as well.
	 */
	@Override
	protected void clearSingletonCache() {
		synchronized (getSingletonMutex()) {
			super.clearSingletonCache();
			this.factoryBeanInstanceCache.clear();
		}
	}

	/**
	 * Expose the logger to collaborating delegates.
	 * @since 5.0.7
	 */
	Log getLogger() {
		return logger;
	}


	/**
	 * {@link RootBeanDefinition} subclass for {@code #createBean} calls with
	 * flexible selection of a Kotlin primary / single public / single non-public
	 * constructor candidate in addition to the default constructor.
	 * @see BeanUtils#getResolvableConstructor(Class)
	 */
	@SuppressWarnings("serial")
	private static class CreateFromClassBeanDefinition extends RootBeanDefinition {

		public CreateFromClassBeanDefinition(Class<?> beanClass) {
			super(beanClass);
		}

		public CreateFromClassBeanDefinition(CreateFromClassBeanDefinition original) {
			super(original);
		}

		@Override
		@Nullable
		public Constructor<?>[] getPreferredConstructors() {
			Constructor<?>[] fromAttribute = super.getPreferredConstructors();
			if (fromAttribute != null) {
				return fromAttribute;
			}
			return ConstructorResolver.determinePreferredConstructors(getBeanClass());
		}

		@Override
		public RootBeanDefinition cloneBeanDefinition() {
			return new CreateFromClassBeanDefinition(this);
		}
	}


	/**
	 * Special DependencyDescriptor variant for Spring's good old autowire="byType" mode.
	 * Always optional; never considering the parameter name for choosing a primary candidate.
	 */
	@SuppressWarnings("serial")
	private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

		public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
			super(methodParameter, false, eager);
		}

		@Override
		@Nullable
		public String getDependencyName() {
			return null;
		}
	}


	/**
	 * {@link MethodCallback} used to find {@link FactoryBean} type information.
	 */
	private static class FactoryBeanMethodTypeFinder implements MethodCallback {

		private final String factoryMethodName;

		private ResolvableType result = ResolvableType.NONE;

		FactoryBeanMethodTypeFinder(String factoryMethodName) {
			this.factoryMethodName = factoryMethodName;
		}

		@Override
		public void doWith(Method method) throws IllegalArgumentException {
			if (isFactoryBeanMethod(method)) {
				ResolvableType returnType = ResolvableType.forMethodReturnType(method);
				ResolvableType candidate = returnType.as(FactoryBean.class).getGeneric();
				if (this.result == ResolvableType.NONE) {
					this.result = candidate;
				}
				else {
					Class<?> resolvedResult = this.result.resolve();
					Class<?> commonAncestor = ClassUtils.determineCommonAncestor(candidate.resolve(), resolvedResult);
					if (!ObjectUtils.nullSafeEquals(resolvedResult, commonAncestor)) {
						this.result = ResolvableType.forClass(commonAncestor);
					}
				}
			}
		}

		private boolean isFactoryBeanMethod(Method method) {
			return (method.getName().equals(this.factoryMethodName) &&
					FactoryBean.class.isAssignableFrom(method.getReturnType()));
		}

		ResolvableType getResult() {
			Class<?> resolved = this.result.resolve();
			boolean foundResult = resolved != null && resolved != Object.class;
			return (foundResult ? this.result : ResolvableType.NONE);
		}
	}

}
