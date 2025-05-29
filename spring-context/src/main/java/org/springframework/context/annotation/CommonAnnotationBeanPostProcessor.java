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

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aot.generate.AccessControl;
import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.support.ClassHintUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.jndi.support.SimpleJndiBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that supports common Java annotations out of the box, in particular the common
 * annotations in the {@code jakarta.annotation} package. These common Java
 * annotations are supported in many Jakarta EE technologies (e.g. JSF and JAX-RS).
 *
 * <p>This post-processor includes support for the {@link jakarta.annotation.PostConstruct}
 * and {@link jakarta.annotation.PreDestroy} annotations - as init annotation
 * and destroy annotation, respectively - through inheriting from
 * {@link InitDestroyAnnotationBeanPostProcessor} with pre-configured annotation types.
 *
 * <p>The central element is the {@link jakarta.annotation.Resource} annotation
 * for annotation-driven injection of named beans, by default from the containing
 * Spring BeanFactory, with only {@code mappedName} references resolved in JNDI.
 * The {@link #setAlwaysUseJndiLookup "alwaysUseJndiLookup" flag} enforces JNDI lookups
 * equivalent to standard Jakarta EE resource injection for {@code name} references
 * and default names as well. The target beans can be simple POJOs, with no special
 * requirements other than the type having to match.
 *
 * <p>Additionally, the original {@code javax.annotation} variants of the annotations
 * dating back to the JSR-250 specification (Java EE 5-8, also included in JDK 6-8)
 * are still supported as well. Note that this is primarily for a smooth upgrade path,
 * not for adoption in new applications.
 *
 * <p>This post-processor also supports the EJB {@link jakarta.ejb.EJB} annotation,
 * analogous to {@link jakarta.annotation.Resource}, with the capability to
 * specify both a local bean name and a global JNDI name for fallback retrieval.
 * The target beans can be plain POJOs as well as EJB Session Beans in this case.
 *
 * <p>For default usage, resolving resource names as Spring bean names,
 * simply define the following in your application context:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/&gt;</pre>
 *
 * For direct JNDI access, resolving resource names as JNDI resource references
 * within the Jakarta EE application's "java:comp/env/" namespace, use the following:
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"&gt;
 *   &lt;property name="alwaysUseJndiLookup" value="true"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * {@code mappedName} references will always be resolved in JNDI,
 * allowing for global JNDI names (including "java:" prefix) as well. The
 * "alwaysUseJndiLookup" flag just affects {@code name} references and
 * default names (inferred from the field name / property name).
 *
 * <p><b>NOTE:</b> A default CommonAnnotationBeanPostProcessor will be registered
 * by the "context:annotation-config" and "context:component-scan" XML tags.
 * Remove or turn off the default annotation configuration there if you intend
 * to specify a custom CommonAnnotationBeanPostProcessor bean definition!
 * <p><b>NOTE:</b> Annotation injection will be performed <i>before</i> XML injection;
 * thus the latter configuration will override the former for properties wired through
 * both approaches.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 * @see #setAlwaysUseJndiLookup
 * @see #setResourceFactory
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 */
// {@link org.springframework.beans.factory.config.BeanPostProcessor} 实现
// 支持开箱即用的常见 Java 注解，特别是 {@code jakarta.annotation} 包中的常见注解。
// 许多 Jakarta EE 技术（例如 JSF 和 JAX-RS）都支持这些常见的 Java 注解。
//
// <p>此后处理器通过从 {@link InitDestroyAnnotationBeanPostProcessor} 继承并预配置注解类型，
// 支持 {@link jakarta.annotation.PostConstruct} 和 {@link jakarta.annotation.PreDestroy} 注解（分别用作 init 注解和 destroy 注解）。
//
// <p>其核心元素是 {@link jakarta.annotation.Resource} 注解，用于注解驱动的命名 Bean 注入，
// 默认情况下，它来自包含它的 Spring BeanFactory，并且仅在 JNDI 中解析 {@code mappedName} 引用。
// {@link #setAlwaysUseJndiLookup “alwaysUseJndiLookup” 标志} 强制执行与标准 Jakarta EE 资源注入等效的 JNDI 查找，
// 适用于 {@code name} 引用和默认名称。目标 bean 可以是简单的 POJO，除了类型必须匹配之外没有其他特殊要求。
//
// <p>此外，JSR-250 规范（Java EE 5-8，也包含在 JDK 6-8 中）中原始的 {@code javax.annotation} 注解变体仍然受支持。
// 请注意，这主要是为了平滑升级路径，而不是为了在新应用程序中采用。
//
// <p>此后处理器还支持 EJB {@link jakarta.ejb.EJB} 注解，类似于 {@link jakarta.annotation.Resource}，
// 能够指定本地 bean 名称和全局 JNDI 名称以进行回退检索。在这种情况下，目标 bean 可以是普通的 POJO，也可以是 EJB 会话 Bean。
//
// <p>对于默认用法，将资源名称解析为 Spring bean 名称，只需在应用程序上下文中定义以下内容：
// <pre class="code"> <bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor"/></pre>
//
// 对于直接 JNDI 访问，将资源名称解析为 Jakarta EE 应用程序的“java:comp/env/”命名空间中的 JNDI 资源引用，使用以下内容：
// <pre class="code">
// <bean class="org.springframework.context.annotation.CommonAnnotationBeanPostProcessor">
// 		<property name="alwaysUseJndiLookup" value="true"/>
// </bean></pre>
//
// {@code mappedName} 引用将始终在 JNDI 中解析，从而允许使用全局 JNDI 名称（包括“java:”前缀）。
// “alwaysUseJndiLookup”标志仅影响 {@code name} 引用和默认名称（根据字段名称/属性名称推断）。
//
// <p><b>注意：</b>默认的 CommonAnnotationBeanPostProcessor 将通过 “context:annotation-config” 和 “context:component-scan” XML 标签注册。
// 如果您打算指定自定义的 CommonAnnotationBeanPostProcessor Bean 定义，请移除或关闭这两个标签中的默认注解配置！
//
// <p><b>注意：</b>注解注入将在 XML 注入<i>之前</i>执行；因此，对于通过这两种方式连接的属性，后者的配置将覆盖前者。
@SuppressWarnings("serial")
public class CommonAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Serializable {

	// Defensive reference to JNDI API for JDK 9+ (optional java.naming module) --> 译文：对 JDK 9+ 的 JNDI API 的防御性引用（可选的 java.naming 模块）
	private static final boolean jndiPresent = ClassUtils.isPresent(
			"javax.naming.InitialContext", CommonAnnotationBeanPostProcessor.class.getClassLoader());

	private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);

	@Nullable
	private static final Class<? extends Annotation> jakartaResourceType;

	@Nullable
	private static final Class<? extends Annotation> javaxResourceType;

	@Nullable
	private static final Class<? extends Annotation> ejbAnnotationType;

	static {
		jakartaResourceType = loadAnnotationType("jakarta.annotation.Resource");
		if (jakartaResourceType != null) {
			resourceAnnotationTypes.add(jakartaResourceType);
		}

		javaxResourceType = loadAnnotationType("javax.annotation.Resource");
		if (javaxResourceType != null) {
			resourceAnnotationTypes.add(javaxResourceType);
		}

		ejbAnnotationType = loadAnnotationType("jakarta.ejb.EJB");
		if (ejbAnnotationType != null) {
			resourceAnnotationTypes.add(ejbAnnotationType);
		}
	}


	private final Set<String> ignoredResourceTypes = new HashSet<>(1);

	private boolean fallbackToDefaultTypeMatch = true;

	private boolean alwaysUseJndiLookup = false;

	@Nullable
	private transient BeanFactory jndiFactory;

	@Nullable
	private transient BeanFactory resourceFactory;

	@Nullable
	private transient BeanFactory beanFactory;

	@Nullable
	private transient StringValueResolver embeddedValueResolver;

	private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


	/**
	 * Create a new CommonAnnotationBeanPostProcessor,
	 * with the init and destroy annotation types set to
	 * {@link jakarta.annotation.PostConstruct} and {@link jakarta.annotation.PreDestroy},
	 * respectively.
	 */
	// 创建一个新的 CommonAnnotationBeanPostProcessor，
	// 并将 init 和 destroy 注释类型分别设置为 {@link jakarta.annotation.PostConstruct}
	// 和 {@link jakarta.annotation.PreDestroy}。
	public CommonAnnotationBeanPostProcessor() {
		setOrder(Ordered.LOWEST_PRECEDENCE - 3);

		// Jakarta EE 9 set of annotations in jakarta.annotation package
		addInitAnnotationType(loadAnnotationType("jakarta.annotation.PostConstruct"));
		addDestroyAnnotationType(loadAnnotationType("jakarta.annotation.PreDestroy"));

		// Tolerate legacy JSR-250 annotations in javax.annotation package
		addInitAnnotationType(loadAnnotationType("javax.annotation.PostConstruct"));
		addDestroyAnnotationType(loadAnnotationType("javax.annotation.PreDestroy"));

		// java.naming module present on JDK 9+?
		if (jndiPresent) {
			this.jndiFactory = new SimpleJndiBeanFactory();
		}
	}


	/**
	 * Ignore the given resource type when resolving {@code @Resource} annotations.
	 * @param resourceType the resource type to ignore
	 */
	public void ignoreResourceType(String resourceType) {
		Assert.notNull(resourceType, "Ignored resource type must not be null");
		this.ignoredResourceTypes.add(resourceType);
	}

	/**
	 * Set whether to allow a fallback to a type match if no explicit name has been
	 * specified. The default name (i.e. the field name or bean property name) will
	 * still be checked first; if a bean of that name exists, it will be taken.
	 * However, if no bean of that name exists, a by-type resolution of the
	 * dependency will be attempted if this flag is "true".
	 * <p>Default is "true". Switch this flag to "false" in order to enforce a
	 * by-name lookup in all cases, throwing an exception in case of no name match.
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency
	 */
	public void setFallbackToDefaultTypeMatch(boolean fallbackToDefaultTypeMatch) {
		this.fallbackToDefaultTypeMatch = fallbackToDefaultTypeMatch;
	}

	/**
	 * Set whether to always use JNDI lookups equivalent to standard Jakarta EE resource
	 * injection, <b>even for {@code name} attributes and default names</b>.
	 * <p>Default is "false": Resource names are used for Spring bean lookups in the
	 * containing BeanFactory; only {@code mappedName} attributes point directly
	 * into JNDI. Switch this flag to "true" for enforcing Jakarta EE style JNDI lookups
	 * in any case, even for {@code name} attributes and default names.
	 * @see #setJndiFactory
	 * @see #setResourceFactory
	 */
	public void setAlwaysUseJndiLookup(boolean alwaysUseJndiLookup) {
		this.alwaysUseJndiLookup = alwaysUseJndiLookup;
	}

	/**
	 * Specify the factory for objects to be injected into {@code @Resource} /
	 * {@code @EJB} annotated fields and setter methods,
	 * <b>for {@code mappedName} attributes that point directly into JNDI</b>.
	 * This factory will also be used if "alwaysUseJndiLookup" is set to "true" in order
	 * to enforce JNDI lookups even for {@code name} attributes and default names.
	 * <p>The default is a {@link org.springframework.jndi.support.SimpleJndiBeanFactory}
	 * for JNDI lookup behavior equivalent to standard Jakarta EE resource injection.
	 * @see #setResourceFactory
	 * @see #setAlwaysUseJndiLookup
	 */
	public void setJndiFactory(BeanFactory jndiFactory) {
		Assert.notNull(jndiFactory, "BeanFactory must not be null");
		this.jndiFactory = jndiFactory;
	}

	/**
	 * Specify the factory for objects to be injected into {@code @Resource} /
	 * {@code @EJB} annotated fields and setter methods,
	 * <b>for {@code name} attributes and default names</b>.
	 * <p>The default is the BeanFactory that this post-processor is defined in,
	 * if any, looking up resource names as Spring bean names. Specify the resource
	 * factory explicitly for programmatic usage of this post-processor.
	 * <p>Specifying Spring's {@link org.springframework.jndi.support.SimpleJndiBeanFactory}
	 * leads to JNDI lookup behavior equivalent to standard Jakarta EE resource injection,
	 * even for {@code name} attributes and default names. This is the same behavior
	 * that the "alwaysUseJndiLookup" flag enables.
	 * @see #setAlwaysUseJndiLookup
	 */
	public void setResourceFactory(BeanFactory resourceFactory) {
		Assert.notNull(resourceFactory, "BeanFactory must not be null");
		this.resourceFactory = resourceFactory;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
		if (this.resourceFactory == null) {
			this.resourceFactory = beanFactory;
		}
		if (beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory) {
			this.embeddedValueResolver = new EmbeddedValueResolver(configurableBeanFactory);
		}
	}


	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName);
		InjectionMetadata metadata = findResourceMetadata(beanName, beanType, null);
		metadata.checkConfigMembers(beanDefinition);
	}

	@Override
	@Nullable
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		BeanRegistrationAotContribution parentAotContribution = super.processAheadOfTime(registeredBean);
		Class<?> beanClass = registeredBean.getBeanClass();
		String beanName = registeredBean.getBeanName();
		RootBeanDefinition beanDefinition = registeredBean.getMergedBeanDefinition();
		InjectionMetadata metadata = findResourceMetadata(beanName, beanClass,
				beanDefinition.getPropertyValues());
		Collection<LookupElement> injectedElements = getInjectedElements(metadata,
				beanDefinition.getPropertyValues());
		if (!ObjectUtils.isEmpty(injectedElements)) {
			AotContribution aotContribution = new AotContribution(beanClass, injectedElements,
					getAutowireCandidateResolver(registeredBean));
			return BeanRegistrationAotContribution.concat(parentAotContribution, aotContribution);
		}
		return parentAotContribution;
	}

	@Nullable
	private AutowireCandidateResolver getAutowireCandidateResolver(RegisteredBean registeredBean) {
		if (registeredBean.getBeanFactory() instanceof DefaultListableBeanFactory lbf) {
			return lbf.getAutowireCandidateResolver();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<LookupElement> getInjectedElements(InjectionMetadata metadata, PropertyValues propertyValues) {
		return (Collection) metadata.getInjectedElements(propertyValues);
	}

	@Override
	public void resetBeanDefinition(String beanName) {
		this.injectionMetadataCache.remove(beanName);
	}

	@Override
	@Nullable
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) {
		return true;
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
		InjectionMetadata metadata = findResourceMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
		}
		return pvs;
	}

	/**
	 * <em>Native</em> processing method for direct calls with an arbitrary target
	 * instance, resolving all of its fields and methods which are annotated with
	 * one of the supported 'resource' annotation types.
	 * @param bean the target instance to process
	 * @throws BeanCreationException if resource injection failed
	 * @since 6.1.3
	 */
	public void processInjection(Object bean) throws BeanCreationException {
		Class<?> clazz = bean.getClass();
		InjectionMetadata metadata = findResourceMetadata(clazz.getName(), clazz, null);
		try {
			metadata.inject(bean, null, null);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					"Injection of resource dependencies failed for class [" + clazz + "]", ex);
		}
	}


	private InjectionMetadata findResourceMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
		// Fall back to class name as cache key, for backwards compatibility with custom callers. --> 译文：回退到类名作为缓存键，以便与自定义调用者向后兼容。
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// Quick check on the concurrent map first, with minimal locking. --> 译文：首先快速检查并发映射，并使用最少的锁定。
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					metadata = buildResourceMetadata(clazz); // go
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildResourceMetadata(Class<?> clazz) {
		// 确定给定类是否适合携带指定注解之一（在类型、方法或字段级别）。
		if (!AnnotationUtils.isCandidateClass(clazz, resourceAnnotationTypes)) {
			return InjectionMetadata.EMPTY;
		}

		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			// 在给定类中所有本地声明的字段上调用给定的回调函数。
			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				// 静态字段不支持 @EJB（jakarta.ejb.EJB） 注释；
				if (ejbAnnotationType != null && field.isAnnotationPresent(ejbAnnotationType)) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@EJB annotation is not supported on static fields");
					}
					// jakarta.ejb.EJB --> EjbRefElement
					currElements.add(new EjbRefElement(field, field, null));
				}
				// 静态字段不支持 @Resource（jakarta.annotation.Resource） 注释；
				else if (jakartaResourceType != null && field.isAnnotationPresent(jakartaResourceType)) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Resource annotation is not supported on static fields");
					}
					if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
						// jakarta.annotation.Resource --> ResourceElement
						currElements.add(new ResourceElement(field, field, null));
					}
				}
				// 静态字段不支持 @Resource（javax.annotation.Resource） 注释；
				else if (javaxResourceType != null && field.isAnnotationPresent(javaxResourceType)) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Resource annotation is not supported on static fields");
					}
					// javax.annotation.Resource --> LegacyResourceElement
					if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
						currElements.add(new LegacyResourceElement(field, field, null));
					}
				}
			});

			// 对给定类的所有匹配方法执行给定的回调操作，这些方法可以是本地声明的或等效的（例如，给定类实现的基于 Java 8 的接口上的默认方法）。
			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					return;
				}
				if (ejbAnnotationType != null && bridgedMethod.isAnnotationPresent(ejbAnnotationType)) {
					if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
						// 静态方法不支持 @EJB 注释
						if (Modifier.isStatic(method.getModifiers())) {
							throw new IllegalStateException("@EJB annotation is not supported on static methods");
						}
						// @EJB 注释需要单参数方法
						if (method.getParameterCount() != 1) {
							throw new IllegalStateException("@EJB annotation requires a single-arg method: " + method);
						}
						PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
						// jakarta.ejb.EJB --> EjbRefElement
						currElements.add(new EjbRefElement(method, bridgedMethod, pd));
					}
				}
				else if (jakartaResourceType != null && bridgedMethod.isAnnotationPresent(jakartaResourceType)) {
					if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
						// 静态方法不支持 @Resource 注释
						if (Modifier.isStatic(method.getModifiers())) {
							throw new IllegalStateException("@Resource annotation is not supported on static methods");
						}
						// @Resource 注释需要单参数方法
						Class<?>[] paramTypes = method.getParameterTypes();
						if (paramTypes.length != 1) {
							throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
						}
						if (!this.ignoredResourceTypes.contains(paramTypes[0].getName())) {
							PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
							// jakarta.annotation.Resource --> ResourceElement
							currElements.add(new ResourceElement(method, bridgedMethod, pd));
						}
					}
				}
				else if (javaxResourceType != null && bridgedMethod.isAnnotationPresent(javaxResourceType)) {
					if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
						// 静态方法不支持 @Resource 注释
						if (Modifier.isStatic(method.getModifiers())) {
							throw new IllegalStateException("@Resource annotation is not supported on static methods");
						}
						// @Resource 注释需要单参数方法
						Class<?>[] paramTypes = method.getParameterTypes();
						if (paramTypes.length != 1) {
							throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
						}
						if (!this.ignoredResourceTypes.contains(paramTypes[0].getName())) {
							PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
							// javax.annotation.Resource --> LegacyResourceElement
							currElements.add(new LegacyResourceElement(method, bridgedMethod, pd));
						}
					}
				}
			});

			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		// 返回一个 InjectionMetadata 实例，可能为空元素。
		return InjectionMetadata.forElements(elements, clazz);
	}

	/**
	 * Obtain a lazily resolving resource proxy for the given name and type,
	 * delegating to {@link #getResource} on demand once a method call comes in.
	 * @param element the descriptor for the annotated field/method
	 * @param requestingBeanName the name of the requesting bean
	 * @return the resource object (never {@code null})
	 * @since 4.2
	 * @see #getResource
	 * @see Lazy
	 */
	protected Object buildLazyResourceProxy(LookupElement element, @Nullable String requestingBeanName) {
		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return element.lookupType;
			}
			@Override
			public Object getTarget() {
				return getResource(element, requestingBeanName);
			}
		};

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		if (element.lookupType.isInterface()) {
			pf.addInterface(element.lookupType);
		}
		ClassLoader classLoader = (this.beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory ?
				configurableBeanFactory.getBeanClassLoader() : null);
		return pf.getProxy(classLoader);
	}

	/**
	 * Obtain the resource object for the given name and type.
	 * @param element the descriptor for the annotated field/method
	 * @param requestingBeanName the name of the requesting bean
	 * @return the resource object (never {@code null})
	 * @throws NoSuchBeanDefinitionException if no corresponding target resource found
	 */
	// 获取指定名称和类型的资源对象。
	// @param element 被注解的字段/方法的描述符
	// @param requestingBeanName 请求 Bean 的名称
	// @return 资源对象（永不返回 null）
	// 如果未找到相应的目标资源，则抛出 NoSuchBeanDefinitionException
	protected Object getResource(LookupElement element, @Nullable String requestingBeanName)
			throws NoSuchBeanDefinitionException {

		// JNDI lookup to perform? --> 译文：要执行 JNDI 查找吗？
		String jndiName = null;
		if (StringUtils.hasLength(element.mappedName)) {
			jndiName = element.mappedName;
		}
		else if (this.alwaysUseJndiLookup) {
			jndiName = element.name;
		}
		if (jndiName != null) {
			if (this.jndiFactory == null) {
				// 未配置 JNDI 工厂 - 指 定“jndiFactory” 属性
				throw new NoSuchBeanDefinitionException(element.lookupType,
						"No JNDI factory configured - specify the 'jndiFactory' property");
			}
			return this.jndiFactory.getBean(jndiName, element.lookupType);
		}

		// Regular resource autowiring --> 译文：常规资源自动装配
		if (this.resourceFactory == null) {
			// 未配置资源工厂 - 指定 “resourceFactory” 属性
			throw new NoSuchBeanDefinitionException(element.lookupType,
					"No resource factory configured - specify the 'resourceFactory' property");
		}
		return autowireResource(this.resourceFactory, element, requestingBeanName); // go
	}

	/**
	 * Obtain a resource object for the given name and type through autowiring
	 * based on the given factory.
	 * @param factory the factory to autowire against
	 * @param element the descriptor for the annotated field/method
	 * @param requestingBeanName the name of the requesting bean
	 * @return the resource object (never {@code null})
	 * @throws NoSuchBeanDefinitionException if no corresponding target resource found
	 */
	// 通过基于给定工厂的自动装配，获取给定名称和类型的资源对象。
	// @param factory 要自动装配的工厂
	// @param element 被注解的字段/方法的描述符
	// @param requestingBeanName 请求 Bean 的名称
	// @return 资源对象（永不返回 {@code null}）
	// 如果未找到相应的目标资源，则抛出 NoSuchBeanDefinitionException
	protected Object autowireResource(BeanFactory factory, LookupElement element, @Nullable String requestingBeanName)
			throws NoSuchBeanDefinitionException {

		Object resource;
		Set<String> autowiredBeanNames;
		String name = element.name;

		if (factory instanceof AutowireCapableBeanFactory autowireCapableBeanFactory) {
			// 是否支持回退到默认类型匹配 && @Resource.name 无值 && factory 不包含名称为 name 的 bean
			if (this.fallbackToDefaultTypeMatch && element.isDefaultName && !factory.containsBean(name)) {
				autowiredBeanNames = new LinkedHashSet<>();
				// 解析此工厂中定义的 bean 的指定依赖关系。
				resource = autowireCapableBeanFactory.resolveDependency(
						element.getDependencyDescriptor(), requestingBeanName, autowiredBeanNames, null); // go
				if (resource == null) {
					// 没有可解析的资源对象
					throw new NoSuchBeanDefinitionException(element.getLookupType(), "No resolvable resource object");
				}
			}
			else {
				// 根据给定的 bean 名称解析一个 bean 实例，并提供一个依赖描述符，用于暴露给目标工厂方法。
				// 最终执行 AbstractBeanFactory.getBean(java.lang.String, java.lang.Class<T>) 方法
				resource = autowireCapableBeanFactory.resolveBeanByName(name, element.getDependencyDescriptor());
				autowiredBeanNames = Collections.singleton(name);
			}
		}
		else {
			resource = factory.getBean(name, element.lookupType);
			autowiredBeanNames = Collections.singleton(name);
		}

		if (factory instanceof ConfigurableBeanFactory configurableBeanFactory) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (requestingBeanName != null && configurableBeanFactory.containsBean(autowiredBeanName)) {
					// 为给定的 Bean 注册一个依赖 Bean，并在给定 Bean 被销毁之前销毁。
					configurableBeanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
				}
			}
		}

		return resource;
	}


	@SuppressWarnings("unchecked")
	@Nullable
	private static Class<? extends Annotation> loadAnnotationType(String name) {
		try {
			return (Class<? extends Annotation>)
					ClassUtils.forName(name, CommonAnnotationBeanPostProcessor.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			return null;
		}
	}


	/**
	 * Class representing generic injection information about an annotated field
	 * or setter method, supporting @Resource and related annotations.
	 */
	// 表示有关注释字段或 setter 方法的通用注入信息的类，支持 @Resource 和相关注释。
	protected abstract static class LookupElement extends InjectionMetadata.InjectedElement {

		protected String name = "";

		protected boolean isDefaultName = false;

		protected Class<?> lookupType = Object.class;

		@Nullable
		protected String mappedName;

		public LookupElement(Member member, @Nullable PropertyDescriptor pd) {
			super(member, pd);
		}

		/**
		 * Return the resource name for the lookup.
		 */
		// 返回要查找的资源名称。
		public final String getName() {
			return this.name;
		}

		/**
		 * Return the desired type for the lookup.
		 */
		// 返回要查找的类型。
		public final Class<?> getLookupType() {
			return this.lookupType;
		}

		/**
		 * Build a DependencyDescriptor for the underlying field/method.
		 */
		// 为底层字段/方法构建 DependencyDescriptor。
		public final DependencyDescriptor getDependencyDescriptor() {
			if (this.isField) {
				return new ResourceElementResolver.LookupDependencyDescriptor(
						(Field) this.member, this.lookupType, isLazyLookup());
			}
			else {
				return new ResourceElementResolver.LookupDependencyDescriptor(
						(Method) this.member, this.lookupType, isLazyLookup());
			}
		}

		/**
		 * Determine whether this dependency is marked for lazy lookup.
		 * The default is {@code false}.
		 * @since 6.1.2
		 */
		// 确定此依赖项是否标记为延迟查找。默认值为 {@code false}。
		boolean isLazyLookup() {
			return false;
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method, supporting the @Resource annotation.
	 */
	// 表示有关注释字段或 setter 方法的注入信息的类，支持 @Resource 注释。
	private class ResourceElement extends LookupElement {

		private final boolean lazyLookup;

		public ResourceElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
			super(member, pd);
			jakarta.annotation.Resource resource = ae.getAnnotation(jakarta.annotation.Resource.class);
			String resourceName = resource.name();
			Class<?> resourceType = resource.type();
			// resourceName 为空；采用默认名称
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				// 从字段名获取 resourceName
				resourceName = this.member.getName();
				// 从 setter 方法名获取 resourceName
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = StringUtils.uncapitalizeAsProperty(resourceName.substring(3));
				}
			}
			else if (embeddedValueResolver != null) {
				// 解析给定的字符串值，例如解析占位符。
				resourceName = embeddedValueResolver.resolveStringValue(resourceName);
			}
			if (Object.class != resourceType) {
				// 检查 resourceType
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method. --> 译文：未指定资源类型...检查字段/方法。
				// 获取 resourceType
				resourceType = getResourceType();
			}
			this.name = (resourceName != null ? resourceName : "");
			this.lookupType = resourceType;
			String lookupValue = resource.lookup();
			this.mappedName = (StringUtils.hasLength(lookupValue) ? lookupValue : resource.mappedName());
			Lazy lazy = ae.getAnnotation(Lazy.class);
			this.lazyLookup = (lazy != null && lazy.value());
		}

		@Override
		protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
			return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName) :
					getResource(this, requestingBeanName));
		}

		@Override
		boolean isLazyLookup() {
			return this.lazyLookup;
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method, supporting the @Resource annotation.
	 */
	// 表示有关注释字段或 setter 方法的注入信息的类，支持 @Resource 注释。
	private class LegacyResourceElement extends LookupElement {

		private final boolean lazyLookup;

		public LegacyResourceElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
			super(member, pd);
			javax.annotation.Resource resource = ae.getAnnotation(javax.annotation.Resource.class);
			String resourceName = resource.name();
			Class<?> resourceType = resource.type();
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				resourceName = this.member.getName();
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = StringUtils.uncapitalizeAsProperty(resourceName.substring(3));
				}
			}
			else if (embeddedValueResolver != null) {
				resourceName = embeddedValueResolver.resolveStringValue(resourceName);
			}
			if (Object.class != resourceType) {
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method.
				resourceType = getResourceType();
			}
			this.name = (resourceName != null ? resourceName : "");
			this.lookupType = resourceType;
			String lookupValue = resource.lookup();
			this.mappedName = (StringUtils.hasLength(lookupValue) ? lookupValue : resource.mappedName());
			Lazy lazy = ae.getAnnotation(Lazy.class);
			this.lazyLookup = (lazy != null && lazy.value());
		}

		@Override
		protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
			return (this.lazyLookup ? buildLazyResourceProxy(this, requestingBeanName) :
					getResource(this, requestingBeanName));
		}

		@Override
		boolean isLazyLookup() {
			return this.lazyLookup;
		}
	}


	/**
	 * Class representing injection information about an annotated field
	 * or setter method, supporting the @EJB annotation.
	 */
	// 表示有关注释字段或 setter 方法的注入信息的类，支持 @EJB 注释。
	private class EjbRefElement extends LookupElement {

		private final String beanName;

		public EjbRefElement(Member member, AnnotatedElement ae, @Nullable PropertyDescriptor pd) {
			super(member, pd);
			jakarta.ejb.EJB resource = ae.getAnnotation(jakarta.ejb.EJB.class);
			String resourceBeanName = resource.beanName();
			String resourceName = resource.name();
			// resourceName 为空；采用默认名称
			this.isDefaultName = !StringUtils.hasLength(resourceName);
			if (this.isDefaultName) {
				// 从字段名获取 resourceName
				resourceName = this.member.getName();
				// 从 setter 方法名获取 resourceName
				if (this.member instanceof Method && resourceName.startsWith("set") && resourceName.length() > 3) {
					resourceName = StringUtils.uncapitalizeAsProperty(resourceName.substring(3));
				}
			}
			Class<?> resourceType = resource.beanInterface();
			if (Object.class != resourceType) {
				// 检查 resourceType
				checkResourceType(resourceType);
			}
			else {
				// No resource type specified... check field/method. --> 译文：未指定资源类型...检查字段/方法。
				// 获取 resourceType
				resourceType = getResourceType();
			}
			this.beanName = resourceBeanName;
			this.name = resourceName;
			this.lookupType = resourceType;
			this.mappedName = resource.mappedName();
		}

		@Override
		protected Object getResourceToInject(Object target, @Nullable String requestingBeanName) {
			if (StringUtils.hasLength(this.beanName)) {
				if (beanFactory != null && beanFactory.containsBean(this.beanName)) {
					// Local match found for explicitly specified local bean name. --> 译文：找到明确指定的本地 bean 名称的本地匹配。
					Object bean = beanFactory.getBean(this.beanName, this.lookupType);
					if (requestingBeanName != null && beanFactory instanceof ConfigurableBeanFactory configurableBeanFactory) {
						configurableBeanFactory.registerDependentBean(this.beanName, requestingBeanName);
					}
					return bean;
				}
				// 无法解析本地 BeanFactory 中的 “beanName”。请考虑指定一个通用的 “name” 值。
				else if (this.isDefaultName && !StringUtils.hasLength(this.mappedName)) {
					throw new NoSuchBeanDefinitionException(this.beanName,
							"Cannot resolve 'beanName' in local BeanFactory. Consider specifying a general 'name' value instead.");
				}
			}
			// JNDI name lookup - may still go to a local BeanFactory.
			return getResource(this, requestingBeanName);
		}
	}


	/**
	 * {@link BeanRegistrationAotContribution} to inject resources on fields and methods.
	 */
	private static class AotContribution implements BeanRegistrationAotContribution {

		private static final String REGISTERED_BEAN_PARAMETER = "registeredBean";

		private static final String INSTANCE_PARAMETER = "instance";

		private final Class<?> target;

		private final Collection<LookupElement> lookupElements;

		@Nullable
		private final AutowireCandidateResolver candidateResolver;

		AotContribution(Class<?> target, Collection<LookupElement> lookupElements,
				@Nullable AutowireCandidateResolver candidateResolver) {

			this.target = target;
			this.lookupElements = lookupElements;
			this.candidateResolver = candidateResolver;
		}

		@Override
		public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
			GeneratedClass generatedClass = generationContext.getGeneratedClasses()
					.addForFeatureComponent("ResourceAutowiring", this.target, type -> {
						type.addJavadoc("Resource autowiring for {@link $T}.", this.target);
						type.addModifiers(javax.lang.model.element.Modifier.PUBLIC);
					});
			GeneratedMethod generateMethod = generatedClass.getMethods().add("apply", method -> {
				method.addJavadoc("Apply resource autowiring.");
				method.addModifiers(javax.lang.model.element.Modifier.PUBLIC,
						javax.lang.model.element.Modifier.STATIC);
				method.addParameter(RegisteredBean.class, REGISTERED_BEAN_PARAMETER);
				method.addParameter(this.target, INSTANCE_PARAMETER);
				method.returns(this.target);
				method.addCode(generateMethodCode(generatedClass.getName(),
						generationContext.getRuntimeHints()));
			});
			beanRegistrationCode.addInstancePostProcessor(generateMethod.toMethodReference());

			registerHints(generationContext.getRuntimeHints());
		}

		private CodeBlock generateMethodCode(ClassName targetClassName, RuntimeHints hints) {
			CodeBlock.Builder code = CodeBlock.builder();
			for (LookupElement lookupElement : this.lookupElements) {
				code.addStatement(generateMethodStatementForElement(
						targetClassName, lookupElement, hints));
			}
			code.addStatement("return $L", INSTANCE_PARAMETER);
			return code.build();
		}

		private CodeBlock generateMethodStatementForElement(ClassName targetClassName,
				LookupElement lookupElement, RuntimeHints hints) {

			Member member = lookupElement.getMember();
			if (member instanceof Field field) {
				return generateMethodStatementForField(
						targetClassName, field, lookupElement, hints);
			}
			if (member instanceof Method method) {
				return generateMethodStatementForMethod(
						targetClassName, method, lookupElement, hints);
			}
			throw new IllegalStateException(
					"Unsupported member type " + member.getClass().getName());
		}

		private CodeBlock generateMethodStatementForField(ClassName targetClassName,
				Field field, LookupElement lookupElement, RuntimeHints hints) {

			hints.reflection().registerField(field);
			CodeBlock resolver = generateFieldResolverCode(field, lookupElement);
			AccessControl accessControl = AccessControl.forMember(field);
			if (!accessControl.isAccessibleFrom(targetClassName)) {
				return CodeBlock.of("$L.resolveAndSet($L, $L)", resolver,
						REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
			}
			return CodeBlock.of("$L.$L = $L.resolve($L)", INSTANCE_PARAMETER,
					field.getName(), resolver, REGISTERED_BEAN_PARAMETER);
		}

		private CodeBlock generateFieldResolverCode(Field field, LookupElement lookupElement) {
			if (lookupElement.isDefaultName) {
				return CodeBlock.of("$T.$L($S)", ResourceElementResolver.class,
						"forField", field.getName());
			}
			else {
				return CodeBlock.of("$T.$L($S, $S)", ResourceElementResolver.class,
						"forField", field.getName(), lookupElement.getName());
			}
		}

		private CodeBlock generateMethodStatementForMethod(ClassName targetClassName,
				Method method, LookupElement lookupElement, RuntimeHints hints) {

			CodeBlock resolver = generateMethodResolverCode(method, lookupElement);
			AccessControl accessControl = AccessControl.forMember(method);
			if (!accessControl.isAccessibleFrom(targetClassName)) {
				hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
				return CodeBlock.of("$L.resolveAndSet($L, $L)", resolver,
						REGISTERED_BEAN_PARAMETER, INSTANCE_PARAMETER);
			}
			hints.reflection().registerMethod(method, ExecutableMode.INTROSPECT);
			return CodeBlock.of("$L.$L($L.resolve($L))", INSTANCE_PARAMETER,
					method.getName(), resolver, REGISTERED_BEAN_PARAMETER);

		}

		private CodeBlock generateMethodResolverCode(Method method, LookupElement lookupElement) {
			if (lookupElement.isDefaultName) {
				return CodeBlock.of("$T.$L($S, $T.class)", ResourceElementResolver.class,
						"forMethod", method.getName(), lookupElement.getLookupType());
			}
			else {
				return CodeBlock.of("$T.$L($S, $T.class, $S)", ResourceElementResolver.class,
						"forMethod", method.getName(), lookupElement.getLookupType(), lookupElement.getName());
			}
		}

		private void registerHints(RuntimeHints runtimeHints) {
			this.lookupElements.forEach(lookupElement ->
					registerProxyIfNecessary(runtimeHints, lookupElement.getDependencyDescriptor()));
		}

		private void registerProxyIfNecessary(RuntimeHints runtimeHints, DependencyDescriptor dependencyDescriptor) {
			if (this.candidateResolver != null) {
				Class<?> proxyClass =
						this.candidateResolver.getLazyResolutionProxyClass(dependencyDescriptor, null);
				if (proxyClass != null) {
					ClassHintUtils.registerProxyIfNecessary(proxyClass, runtimeHints);
				}
			}
		}
	}

}
