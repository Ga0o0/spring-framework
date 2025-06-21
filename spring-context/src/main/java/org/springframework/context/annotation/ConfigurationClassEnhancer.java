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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aot.AotDetector;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.cglib.core.ClassGenerator;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.cglib.transform.ClassEmitterTransformer;
import org.springframework.cglib.transform.TransformingClassGenerator;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;
import org.springframework.objenesis.ObjenesisException;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Enhances {@link Configuration} classes by generating a CGLIB subclass which
 * interacts with the Spring container to respect bean scoping semantics for
 * {@code @Bean} methods. Each such {@code @Bean} method will be overridden in
 * the generated subclass, only delegating to the actual {@code @Bean} method
 * implementation if the container actually requests the construction of a new
 * instance. Otherwise, a call to such an {@code @Bean} method serves as a
 * reference back to the container, obtaining the corresponding bean by name.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see #enhance
 * @see ConfigurationClassPostProcessor
 */
// 通过生成一个与 Spring 容器交互的 CGLIB 子类来增强 {@link Configuration} 类，以遵循 {@code @Bean} 方法的 bean 作用域语义。
// 生成的子类会重写每个 {@code @Bean} 方法，仅当容器实际请求创建新实例时才会委托给实际的 {@code @Bean} 方法实现。
//
// 否则，对此类 {@code @Bean} 方法的调用将作为对容器的引用，通过名称获取相应的 bean。
class ConfigurationClassEnhancer {

	// The callbacks to use. Note that these callbacks must be stateless. --> 译文：要使用的回调函数。请注意，这些回调函数必须是无状态的。
	static final Callback[] CALLBACKS = new Callback[] {
			new BeanMethodInterceptor(),
			new BeanFactoryAwareMethodInterceptor(),
			NoOp.INSTANCE
	};

	private static final ConditionalCallbackFilter CALLBACK_FILTER = new ConditionalCallbackFilter(CALLBACKS);

	private static final String BEAN_FACTORY_FIELD = "$$beanFactory";


	private static final Log logger = LogFactory.getLog(ConfigurationClassEnhancer.class);

	private static final SpringObjenesis objenesis = new SpringObjenesis();


	/**
	 * Loads the specified class and generates a CGLIB subclass of it equipped with
	 * container-aware callbacks capable of respecting scoping and other bean semantics.
	 * @return the enhanced subclass
	 */
	// 加载指定的类，并生成一个带有容器感知回调的 CGLIB 子类，该回调能够遵循作用域和其他 bean 语义。
	// @return 增强后的子类
	public Class<?> enhance(Class<?> configClass, @Nullable ClassLoader classLoader) {
		if (EnhancedConfiguration.class.isAssignableFrom(configClass)) {// 所有 @Configuration CGLIB 子类都必须实现 EnhancedConfiguration 标记接口
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Ignoring request to enhance %s as it has " +
						"already been enhanced. This usually indicates that more than one " +
						"ConfigurationClassPostProcessor has been registered (e.g. via " +
						"<context:annotation-config>). This is harmless, but you may " +
						"want check your configuration and remove one CCPP if possible",
						configClass.getName()));
			}
			return configClass;
		}

		try {
			// Use original ClassLoader if config class not locally loaded in overriding class loader
			// --> 译文：如果在重写类加载器时配置类未在本地加载，则使用原始类加载器。
			boolean classLoaderMismatch = (classLoader != null && classLoader != configClass.getClassLoader());
			if (classLoaderMismatch && classLoader instanceof SmartClassLoader smartClassLoader) {
				classLoader = smartClassLoader.getOriginalClassLoader();
				classLoaderMismatch = (classLoader != configClass.getClassLoader());
			}
			// Use original ClassLoader if config class relies on package visibility --> 译文：如果配置类依赖于包可见性，请使用原始类加载器。
			if (classLoaderMismatch && reliesOnPackageVisibility(configClass)) {
				classLoader = configClass.getClassLoader();
				classLoaderMismatch = false;
			}
			// important
			Enhancer enhancer = newEnhancer(configClass, classLoader); // 创建一个新的 CGLIB Enhancer 实例。
			Class<?> enhancedClass = createClass(enhancer, classLoaderMismatch); // important
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Successfully enhanced %s; enhanced class name is: %s",
						configClass.getName(), enhancedClass.getName()));
			}
			return enhancedClass;
		}
		catch (CodeGenerationException ex) {
			throw new BeanDefinitionStoreException("Could not enhance configuration class [" + configClass.getName() +
					"]. Consider declaring @Configuration(proxyBeanMethods=false) without inter-bean references " +
					"between @Bean methods on the configuration class, avoiding the need for CGLIB enhancement.", ex);
		}
	}

	/**
	 * Checks whether the given config class relies on package visibility,
	 * either for the class itself or for any of its {@code @Bean} methods.
	 */
	// 检查给定的配置类是否依赖于包可见性，无论是类本身还是其任何 {@code @Bean} 方法。
	private boolean reliesOnPackageVisibility(Class<?> configSuperClass) {
		int mod = configSuperClass.getModifiers();
		if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) {
			return true;
		}
		for (Method method : ReflectionUtils.getDeclaredMethods(configSuperClass)) {
			if (BeanAnnotationHelper.isBeanAnnotated(method)) { // 被 @Bean 注解
				mod = method.getModifiers();
				if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod)) { // 非 public 和 protected
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new CGLIB {@link Enhancer} instance.
	 */
	// 创建一个新的 CGLIB {@link Enhancer} 实例。
	private Enhancer newEnhancer(Class<?> configSuperClass, @Nullable ClassLoader classLoader) {
		Enhancer enhancer = new Enhancer();
		if (classLoader != null) {
			enhancer.setClassLoader(classLoader);
			if (classLoader instanceof SmartClassLoader smartClassLoader &&
					smartClassLoader.isClassReloadable(configSuperClass)) {
				enhancer.setUseCache(false);
			}
		}
		enhancer.setSuperclass(configSuperClass);
		enhancer.setInterfaces(new Class<?>[] {EnhancedConfiguration.class});
		enhancer.setUseFactory(false);
		enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
		enhancer.setAttemptLoad(enhancer.getUseCache() && AotDetector.useGeneratedArtifacts());
		enhancer.setStrategy(new BeanFactoryAwareGeneratorStrategy(classLoader));
		enhancer.setCallbackFilter(CALLBACK_FILTER);
		enhancer.setCallbackTypes(CALLBACK_FILTER.getCallbackTypes());
		return enhancer;
	}

	/**
	 * Uses enhancer to generate a subclass of superclass,
	 * ensuring that callbacks are registered for the new subclass.
	 */
	// 使用增强器生成超类的子类，确保为新的子类注册回调。
	private Class<?> createClass(Enhancer enhancer, boolean fallback) {
		Class<?> subclass;
		try {
			subclass = enhancer.createClass();
		}
		catch (Throwable ex) {
			if (!fallback) {
				throw (ex instanceof CodeGenerationException cgex ? cgex : new CodeGenerationException(ex));
			}
			// Possibly a package-visible @Bean method declaration not accessible
			// in the given ClassLoader -> retry with original ClassLoader
			// --> 译文：可能是包可见的 @Bean 方法声明在给定的类加载器中无法访问 -> 使用原始类加载器重试
			enhancer.setClassLoader(null);
			subclass = enhancer.createClass();
		}

		// Registering callbacks statically (as opposed to thread-local)
		// is critical for usage in an OSGi environment (SPR-5932)...
		// --> 译文：在 OSGi 环境中使用时，静态注册回调函数（而不是线程本地注册）至关重要（SPR-5932）……
		Enhancer.registerStaticCallbacks(subclass, CALLBACKS);
		// CALLBACKS = { BeanMethodInterceptor, BeanFactoryAwareMethodInterceptor, NoOp.INSTANCE }
		return subclass;
	}


	/**
	 * Marker interface to be implemented by all @Configuration CGLIB subclasses.
	 * Facilitates idempotent behavior for {@link ConfigurationClassEnhancer#enhance}
	 * through checking to see if candidate classes are already assignable to it.
	 * <p>Also extends {@link BeanFactoryAware}, as all enhanced {@code @Configuration}
	 * classes require access to the {@link BeanFactory} that created them.
	 * <p>Note that this interface is intended for framework-internal use only, however
	 * must remain public in order to allow access to subclasses generated from other
	 * packages (i.e. user code).
	 */
	// 所有 @Configuration CGLIB 子类都必须实现此标记接口。它通过检查候选类是否已可分配给 {@link ConfigurationClassEnhancer#enhance} 来确保幂等性。
	// <p>此外，它还继承了 {@link BeanFactoryAware}，因为所有增强的 {@code @Configuration} 类都需要访问创建它们的 {@link BeanFactory}。
	// <p>请注意，此接口仅供框架内部使用，但必须保持公开，以便允许访问由其他生成的子类。
	public interface EnhancedConfiguration extends BeanFactoryAware {
	}


	/**
	 * Conditional {@link Callback}.
	 * @see ConditionalCallbackFilter
	 */
	private interface ConditionalCallback extends Callback {

		boolean isMatch(Method candidateMethod);
	}


	/**
	 * A {@link CallbackFilter} that works by interrogating {@link Callback Callbacks} in the order
	 * that they are defined via {@link ConditionalCallback}.
	 */
	private static class ConditionalCallbackFilter implements CallbackFilter {

		private final Callback[] callbacks;

		private final Class<?>[] callbackTypes;

		public ConditionalCallbackFilter(Callback[] callbacks) {
			this.callbacks = callbacks;
			this.callbackTypes = new Class<?>[callbacks.length];
			for (int i = 0; i < callbacks.length; i++) {
				this.callbackTypes[i] = callbacks[i].getClass();
			}
		}

		@Override
		public int accept(Method method) {
			for (int i = 0; i < this.callbacks.length; i++) {
				Callback callback = this.callbacks[i];
				if (!(callback instanceof ConditionalCallback conditional) || conditional.isMatch(method)) {
					return i;
				}
			}
			throw new IllegalStateException("No callback available for method " + method.getName());
		}

		public Class<?>[] getCallbackTypes() {
			return this.callbackTypes;
		}
	}


	/**
	 * Custom extension of CGLIB's DefaultGeneratorStrategy, introducing a {@link BeanFactory} field.
	 * Also exposes the application ClassLoader as thread context ClassLoader for the time of
	 * class generation (in order for ASM to pick it up when doing common superclass resolution).
	 */
	// 对 CGLIB 的 DefaultGeneratorStrategy 进行自定义扩展，引入 {@link BeanFactory} 字段。
	// 此外，在类生成时，还将应用程序类加载器作为线程上下文类加载器公开（以便 ASM 在进行通用超类解析时能够获取它）。
	private static class BeanFactoryAwareGeneratorStrategy extends ClassLoaderAwareGeneratorStrategy {

		public BeanFactoryAwareGeneratorStrategy(@Nullable ClassLoader classLoader) {
			super(classLoader);
		}

		@Override
		protected ClassGenerator transform(ClassGenerator cg) throws Exception {
			ClassEmitterTransformer transformer = new ClassEmitterTransformer() {
				@Override
				public void end_class() {
					declare_field(Opcodes.ACC_PUBLIC, BEAN_FACTORY_FIELD, Type.getType(BeanFactory.class), null);
					super.end_class();
				}
			};
			return new TransformingClassGenerator(cg, transformer);
		}
	}


	/**
	 * Intercepts the invocation of any {@link BeanFactoryAware#setBeanFactory(BeanFactory)} on
	 * {@code @Configuration} class instances for the purpose of recording the {@link BeanFactory}.
	 * @see EnhancedConfiguration
	 */
	// 拦截对 {@code @Configuration} 类实例上任何 {@link BeanFactoryAware#setBeanFactory(BeanFactory)} 的调用，以便记录 {@link BeanFactory}。
	private static class BeanFactoryAwareMethodInterceptor implements MethodInterceptor, ConditionalCallback {

		@Override
		@Nullable
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			Field field = ReflectionUtils.findField(obj.getClass(), BEAN_FACTORY_FIELD);
			Assert.state(field != null, "Unable to find generated BeanFactory field");
			field.set(obj, args[0]);

			// Does the actual (non-CGLIB) superclass implement BeanFactoryAware?
			// If so, call its setBeanFactory() method. If not, just exit.
			// --> 译文：实际的（非 CGLIB 的）超类是否实现了 BeanFactoryAware 接口？如果实现了，则调用其 setBeanFactory() 方法。否则，直接退出。
			if (BeanFactoryAware.class.isAssignableFrom(ClassUtils.getUserClass(obj.getClass().getSuperclass()))) {
				return proxy.invokeSuper(obj, args);
			}
			return null;
		}

		@Override
		public boolean isMatch(Method candidateMethod) {
			return isSetBeanFactory(candidateMethod);
		}

		public static boolean isSetBeanFactory(Method candidateMethod) {
			return (candidateMethod.getName().equals("setBeanFactory") &&
					candidateMethod.getParameterCount() == 1 &&
					BeanFactory.class == candidateMethod.getParameterTypes()[0] &&
					BeanFactoryAware.class.isAssignableFrom(candidateMethod.getDeclaringClass()));
		}
	}


	/**
	 * Intercepts the invocation of any {@link Bean}-annotated methods in order to ensure proper
	 * handling of bean semantics such as scoping and AOP proxying.
	 * @see Bean
	 * @see ConfigurationClassEnhancer
	 */
	// 拦截对任何带有 {@link Bean} 注解的方法的调用，以确保正确处理 bean 语义，例如作用域和 AOP 代理。
	private static class BeanMethodInterceptor implements MethodInterceptor, ConditionalCallback {

		/**
		 * Enhance a {@link Bean @Bean} method to check the supplied BeanFactory for the
		 * existence of this bean object.
		 * @throws Throwable as a catch-all for any exception that may be thrown when invoking the
		 * super implementation of the proxied method i.e., the actual {@code @Bean} method
		 */
		// 增强 {@link Bean @Bean} 方法，使其检查提供的 BeanFactory 中是否存在此 bean 对象。
		// @throws Throwable 作为捕获调用代理方法的父实现（即实际的 {@code @Bean} 方法）时可能抛出的任何异常的容器。
		@Override
		@Nullable
		public Object intercept(Object enhancedConfigInstance, Method beanMethod, Object[] beanMethodArgs,
					MethodProxy cglibMethodProxy) throws Throwable {

			ConfigurableBeanFactory beanFactory = getBeanFactory(enhancedConfigInstance);
			// 确定 Bean 名称 -> 默认方法名，@Bean.value 有值时对其进行覆盖设置
			String beanName = BeanAnnotationHelper.determineBeanNameFor(beanMethod);

			// Determine whether this bean is a scoped-proxy --> 译文：确定此 bean 是否为作用域代理
			if (BeanAnnotationHelper.isScopedProxy(beanMethod)) { // 通过 @Scope.proxyMode 判断
				String scopedBeanName = ScopedProxyCreator.getTargetBeanName(beanName);
				if (beanFactory.isCurrentlyInCreation(scopedBeanName)) {
					beanName = scopedBeanName;
				}
			}

			// To handle the case of an inter-bean method reference, we must explicitly check the
			// container for already cached instances. --> 译文：为了处理 bean 间方法引用的情况，我们必须显式地检查容器中是否已缓存实例。

			// First, check to see if the requested bean is a FactoryBean. If so, create a subclass
			// proxy that intercepts calls to getObject() and returns any cached bean instance.
			// This ensures that the semantics of calling a FactoryBean from within @Bean methods
			// is the same as that of referring to a FactoryBean within XML. See SPR-6602.
			// --> 译文：首先，检查请求的 bean 是否为 FactoryBean。
			// 如果是，则创建一个子类代理，拦截对 getObject() 的调用并返回任何已缓存的 bean 实例。
			// 这确保了从 @Bean 方法中调用 FactoryBean 的语义与在 XML 中引用 FactoryBean 的语义相同。参见 SPR-6602。
			if (factoryContainsBean(beanFactory, BeanFactory.FACTORY_BEAN_PREFIX + beanName) &&
					factoryContainsBean(beanFactory, beanName)) {
				Object factoryBean = beanFactory.getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName);
				if (factoryBean instanceof ScopedProxyFactoryBean) {
					// Scoped proxy factory beans are a special case and should not be further proxied
					// --> 译文：作用域代理工厂 bean 是一种特殊情况，不应再进行代理。
				}
				else {
					// It is a candidate FactoryBean - go ahead with enhancement --> 译文：这是一个候选 FactoryBean —— 继续进行增强。
					return enhanceFactoryBean(factoryBean, beanMethod.getReturnType(), beanFactory, beanName);
				}
			}

			if (isCurrentlyInvokedFactoryMethod(beanMethod)) {
				// The factory is calling the bean method in order to instantiate and register the bean
				// (i.e. via a getBean() call) -> invoke the super implementation of the method to actually
				// create the bean instance. --> 译文：工厂调用 bean 方法来实例化和注册 bean（即通过 getBean() 调用） -> 调用该方法的父类实现来实际创建 bean 实例。
				if (logger.isInfoEnabled() &&
						BeanFactoryPostProcessor.class.isAssignableFrom(beanMethod.getReturnType())) {
					logger.info(String.format("@Bean method %s.%s is non-static and returns an object " +
									"assignable to Spring's BeanFactoryPostProcessor interface. This will " +
									"result in a failure to process annotations such as @Autowired, " +
									"@Resource and @PostConstruct within the method's declaring " +
									"@Configuration class. Add the 'static' modifier to this method to avoid " +
									"these container lifecycle issues; see @Bean javadoc for complete details.",
							beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName()));
				}
				return cglibMethodProxy.invokeSuper(enhancedConfigInstance, beanMethodArgs);
			}

			return resolveBeanReference(beanMethod, beanMethodArgs, beanFactory, beanName);
		}

		@Nullable
		private Object resolveBeanReference(Method beanMethod, Object[] beanMethodArgs,
				ConfigurableBeanFactory beanFactory, String beanName) {

			// The user (i.e. not the factory) is requesting this bean through a call to
			// the bean method, direct or indirect. The bean may have already been marked
			// as 'in creation' in certain autowiring scenarios; if so, temporarily set
			// the in-creation status to false in order to avoid an exception.
			// --> 译文：用户（而非工厂）通过直接或间接调用 bean 的方法来请求此 bean。
			// 在某些自动装配场景中，该 bean 可能已被标记为“创建中”；如果是这种情况，请暂时将“创建中”状态设置为 false，以避免抛出异常。
			boolean alreadyInCreation = beanFactory.isCurrentlyInCreation(beanName);
			try {
				if (alreadyInCreation) {
					beanFactory.setCurrentlyInCreation(beanName, false);
				}
				boolean useArgs = !ObjectUtils.isEmpty(beanMethodArgs);
				if (useArgs && beanFactory.isSingleton(beanName)) {
					// Stubbed null arguments just for reference purposes,
					// expecting them to be autowired for regular singleton references?
					// A safe assumption since @Bean singleton arguments cannot be optional...
					// --> 译文：为了便于引用而设置的空参数，期望它们能被自动装配到常规的单例引用中？这是一个合理的假设，因为 @Bean 单例参数不能是可选的……
					for (Object arg : beanMethodArgs) {
						if (arg == null) {
							useArgs = false;
							break;
						}
					}
				}
				Object beanInstance = (useArgs ? beanFactory.getBean(beanName, beanMethodArgs) :
						beanFactory.getBean(beanName));
				if (!ClassUtils.isAssignableValue(beanMethod.getReturnType(), beanInstance)) {
					// Detect package-protected NullBean instance through equals(null) check --> 译文：通过 equals(null) 检查检测受包保护的 NullBean 实例
					if (beanInstance.equals(null)) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("@Bean method %s.%s called as bean reference " +
									"for type [%s] returned null bean; resolving to null value.",
									beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName(),
									beanMethod.getReturnType().getName()));
						}
						beanInstance = null;
					}
					else {
						String msg = String.format("@Bean method %s.%s called as bean reference " +
								"for type [%s] but overridden by non-compatible bean instance of type [%s].",
								beanMethod.getDeclaringClass().getSimpleName(), beanMethod.getName(),
								beanMethod.getReturnType().getName(), beanInstance.getClass().getName());
						try {
							BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
							msg += " Overriding bean of same name declared in: " + beanDefinition.getResourceDescription();
						}
						catch (NoSuchBeanDefinitionException ex) {
							// Ignore - simply no detailed message then.
						}
						throw new IllegalStateException(msg);
					}
				}
				Method currentlyInvoked = SimpleInstantiationStrategy.getCurrentlyInvokedFactoryMethod(); // 返回当前正在调用的工厂方法，如果没有工厂方法则返回 null。
				if (currentlyInvoked != null) {
					String outerBeanName = BeanAnnotationHelper.determineBeanNameFor(currentlyInvoked);
					beanFactory.registerDependentBean(beanName, outerBeanName);
				}
				return beanInstance;
			}
			finally {
				if (alreadyInCreation) {
					beanFactory.setCurrentlyInCreation(beanName, true);
				}
			}
		}

		@Override
		public boolean isMatch(Method candidateMethod) {
			return (candidateMethod.getDeclaringClass() != Object.class &&
					!BeanFactoryAwareMethodInterceptor.isSetBeanFactory(candidateMethod) &&
					BeanAnnotationHelper.isBeanAnnotated(candidateMethod));
		}

		private ConfigurableBeanFactory getBeanFactory(Object enhancedConfigInstance) {
			Field field = ReflectionUtils.findField(enhancedConfigInstance.getClass(), BEAN_FACTORY_FIELD);
			Assert.state(field != null, "Unable to find generated bean factory field");
			Object beanFactory = ReflectionUtils.getField(field, enhancedConfigInstance);
			Assert.state(beanFactory != null, "BeanFactory has not been injected into @Configuration class");
			Assert.state(beanFactory instanceof ConfigurableBeanFactory,
					"Injected BeanFactory is not a ConfigurableBeanFactory");
			return (ConfigurableBeanFactory) beanFactory;
		}

		/**
		 * Check the BeanFactory to see whether the bean named <var>beanName</var> already
		 * exists. Accounts for the fact that the requested bean may be "in creation", i.e.:
		 * we're in the middle of servicing the initial request for this bean. From an enhanced
		 * factory method's perspective, this means that the bean does not actually yet exist,
		 * and that it is now our job to create it for the first time by executing the logic
		 * in the corresponding factory method.
		 * <p>Said another way, this check repurposes
		 * {@link ConfigurableBeanFactory#isCurrentlyInCreation(String)} to determine whether
		 * the container is calling this method or the user is calling this method.
		 * @param beanName name of bean to check for
		 * @return whether <var>beanName</var> already exists in the factory
		 */
		// 检查 BeanFactory，查看名为 <var>beanName</var> 的 bean 是否已存在。
		// 这考虑到了请求的 bean 可能正在“创建中”的情况，也就是说，我们正在处理对该 bean 的初始请求。
		// 从增强型工厂方法的角度来看，这意味着该 bean 实际上尚不存在，而我们现在的任务是通过执行相应工厂方法中的逻辑来首次创建它。
		//
		// <p>换句话说，此检查重新利用了 {@link ConfigurableBeanFactory#isCurrentlyInCreation(String)} 来确定是容器调用此方法还是用户调用此方法。
		//
		// @param beanName 要检查的 bean 的名称
		// @return <var>beanName</var> 是否已存在于工厂中
		private boolean factoryContainsBean(ConfigurableBeanFactory beanFactory, String beanName) {
			return (beanFactory.containsBean(beanName) && !beanFactory.isCurrentlyInCreation(beanName));
		}

		/**
		 * Check whether the given method corresponds to the container's currently invoked
		 * factory method. Compares method name and parameter types only in order to work
		 * around a potential problem with covariant return types (currently only known
		 * to happen on Groovy classes).
		 */
		// 检查给定方法是否与容器当前调用的工厂方法相对应。
		// 仅比较方法名称和参数类型，以规避协变返回类型可能出现的问题（目前已知仅发生在 Groovy 类中）。
		private boolean isCurrentlyInvokedFactoryMethod(Method method) {
			Method currentlyInvoked = SimpleInstantiationStrategy.getCurrentlyInvokedFactoryMethod();
			return (currentlyInvoked != null && method.getName().equals(currentlyInvoked.getName()) &&
					Arrays.equals(method.getParameterTypes(), currentlyInvoked.getParameterTypes()));
		}

		/**
		 * Create a subclass proxy that intercepts calls to getObject(), delegating to the current BeanFactory
		 * instead of creating a new instance. These proxies are created only when calling a FactoryBean from
		 * within a Bean method, allowing for proper scoping semantics even when working against the FactoryBean
		 * instance directly. If a FactoryBean instance is fetched through the container via &-dereferencing,
		 * it will not be proxied. This too is aligned with the way XML configuration works.
		 */
		// 创建一个子类代理，拦截对 `getObject()` 的调用，并将请求委托给当前的 `BeanFactory`，而不是创建新实例。
		// 这些代理仅在从 `Bean` 方法内部调用 `FactoryBean` 时创建，即使直接操作 `FactoryBean` 实例，也能保证正确的作用域语义。
		// 如果通过容器中的 `&` 解引用获取 `FactoryBean` 实例，则不会使用代理。这也符合 XML 配置的工作方式。
		private Object enhanceFactoryBean(Object factoryBean, Class<?> exposedType,
				ConfigurableBeanFactory beanFactory, String beanName) {

			try {
				Class<?> clazz = factoryBean.getClass();
				boolean finalClass = Modifier.isFinal(clazz.getModifiers());
				boolean finalMethod = Modifier.isFinal(clazz.getMethod("getObject").getModifiers());
				if (finalClass || finalMethod) { // final 类 或者 final getObject() 方法
					if (exposedType.isInterface()) { // 接口
						if (logger.isTraceEnabled()) {
							logger.trace("Creating interface proxy for FactoryBean '" + beanName + "' of type [" +
									clazz.getName() + "] for use within another @Bean method because its " +
									(finalClass ? "implementation class" : "getObject() method") +
									" is final: Otherwise a getObject() call would not be routed to the factory.");
						}
						// 接口采用 jdk 动态代理
						return createInterfaceProxyForFactoryBean(factoryBean, exposedType, beanFactory, beanName);
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Unable to proxy FactoryBean '" + beanName + "' of type [" +
									clazz.getName() + "] for use within another @Bean method because its " +
									(finalClass ? "implementation class" : "getObject() method") +
									" is final: A getObject() call will NOT be routed to the factory. " +
									"Consider declaring the return type as a FactoryBean interface.");
						}
						return factoryBean;
					}
				}
			}
			catch (NoSuchMethodException ex) {
				// No getObject() method -> shouldn't happen, but as long as nobody is trying to call it...
				// --> 译文：没有 getObject() 方法 -> 不应该发生这种情况，但只要没有人尝试调用它……
			}
    		// 默认采用 CGLIB 动态代理
			return createCglibProxyForFactoryBean(factoryBean, beanFactory, beanName);
		}

		private Object createInterfaceProxyForFactoryBean(Object factoryBean, Class<?> interfaceType,
				ConfigurableBeanFactory beanFactory, String beanName) {

			return Proxy.newProxyInstance(
					factoryBean.getClass().getClassLoader(), new Class<?>[] {interfaceType},
					(proxy, method, args) -> {
						if (method.getName().equals("getObject") && args == null) {
							return beanFactory.getBean(beanName);
						}
						return ReflectionUtils.invokeMethod(method, factoryBean, args);
					});
		}

		private Object createCglibProxyForFactoryBean(Object factoryBean,
				ConfigurableBeanFactory beanFactory, String beanName) {

			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(factoryBean.getClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setAttemptLoad(AotDetector.useGeneratedArtifacts());
			enhancer.setCallbackType(MethodInterceptor.class);

			// Ideally create enhanced FactoryBean proxy without constructor side effects,
			// analogous to AOP proxy creation in ObjenesisCglibAopProxy...
			// --> 译文：理想情况下，应创建增强型 FactoryBean 代理，且不产生构造函数副作用，类似于 ObjenesisCglibAopProxy 中的 AOP 代理创建方式……
			Class<?> fbClass = enhancer.createClass();
			Object fbProxy = null;

			if (objenesis.isWorthTrying()) {
				try {
					fbProxy = objenesis.newInstance(fbClass, enhancer.getUseCache());
				}
				catch (ObjenesisException ex) {
					logger.debug("Unable to instantiate enhanced FactoryBean using Objenesis, " +
							"falling back to regular construction", ex);
				}
			}

			if (fbProxy == null) {
				try {
					fbProxy = ReflectionUtils.accessibleConstructor(fbClass).newInstance();
				}
				catch (Throwable ex) {
					throw new IllegalStateException("Unable to instantiate enhanced FactoryBean using Objenesis, " +
							"and regular FactoryBean instantiation via default constructor fails as well", ex);
				}
			}

			((Factory) fbProxy).setCallback(0, (MethodInterceptor) (obj, method, args, proxy) -> {
				if (method.getName().equals("getObject") && args.length == 0) {
					return beanFactory.getBean(beanName);
				}
				return method.invoke(factoryBean, args);
			});

			return fbProxy;
		}
	}

}
