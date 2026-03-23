package org.springframework.sample.bean_post_processor.annotation_common;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 检查被 init 和 destroy 候选注解标注的类，并收集被候选 Resource 注解标记的字段和方法，封装成 InjectionMetadata 实例后再进行检查
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 *
 * ## 1. 检查所有被候选 init 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class)
 *
 * ### 1.1. 先从缓存中获取当前 beanClass 的 LifecycleMetadata 实例， 如果没有，则收集被候选 init 和 destroy 注解标注的方法来创建一个 LifecycleMetadata 实例返回
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#buildLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#LifecycleMetadata(java.lang.Class, java.util.Collection, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#beanClass
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 *
 * ### 1.2. 检查 initMethods 和 destroyMethods，并将其分别赋值给 checkedInitMethods 和 checkedDestroyMethods
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkInitDestroyMethods(org.springframework.beans.factory.support.RootBeanDefinition)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkedInitMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkedDestroyMethods
 *
 * ## 2. 收集被候选 Resource 注解（@EJB（jakarta.ejb.EJB）, @Resource（jakarta.annotation.Resource）, @Resource（javax.annotation.Resource））标记的字段和方法，并封装成 InjectionMetadata 实例返回
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#findResourceMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#buildResourceMetadata(Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#forElements(java.util.Collection, java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.EjbRefElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LegacyResourceElement
 *
 * ## 3. 检查 InjectionMetadata#injectedElements 元素，并将检查后的元素其赋值给 InjectionMetadata#checkedElements
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkConfigMembers(org.springframework.beans.factory.support.RootBeanDefinition)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkedElements
 */
public class CodeAnalysis10_CommonAnnotationBeanPostProcessor_postProcessMergedBeanDefinition {
	static class CodeAnalysis_InitDestroyAnnotationBeanPostProcessor extends InitDestroyAnnotationBeanPostProcessor {
		private static final long serialVersionUID = 1L;
		private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);
		private static final Class<? extends Annotation> jakartaResourceType;
		private static final Class<? extends Annotation> javaxResourceType;
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
		private final transient LifecycleMetadata emptyLifecycleMetadata = null; // 省略实现，直接使用 null 值代替
		private final Set<Class<? extends Annotation>> initAnnotationTypes = new LinkedHashSet<>(2);
		private final Set<Class<? extends Annotation>> destroyAnnotationTypes = new LinkedHashSet<>(2);
		private final transient Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<>(256);
		private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);


		@Override
		public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
			super.postProcessMergedBeanDefinition(beanDefinition, beanType, beanName); // important -> go
			InjectionMetadata metadata = findResourceMetadata(beanName, beanType, null); // important -> go
			metadata.checkConfigMembers(beanDefinition);
		}

		private LifecycleMetadata findLifecycleMetadata(RootBeanDefinition beanDefinition, Class<?> beanClass) {
			LifecycleMetadata metadata = findLifecycleMetadata(beanClass); // important -> go
			metadata.checkInitDestroyMethods(beanDefinition); // important -> go
			return metadata;
		}

		private LifecycleMetadata findLifecycleMetadata(Class<?> beanClass) {
			if (this.lifecycleMetadataCache == null) {
				// Happens after deserialization, during destruction... --> 译文：发生在反序列化之后，销毁过程中……
				return buildLifecycleMetadata(beanClass); // important -> go
			}
			// Quick check on the concurrent map first, with minimal locking. --> 译文：首先快速检查并发映射，尽量减少锁定。
			LifecycleMetadata metadata = this.lifecycleMetadataCache.get(beanClass);
			if (metadata == null) {
				synchronized (this.lifecycleMetadataCache) {
					metadata = this.lifecycleMetadataCache.get(beanClass);
					if (metadata == null) {
						metadata = buildLifecycleMetadata(beanClass); // important -> go
						this.lifecycleMetadataCache.put(beanClass, metadata);
					}
					return metadata;
				}
			}
			return metadata;
		}

		private LifecycleMetadata buildLifecycleMetadata(final Class<?> beanClass) {
			// 1. 检查类是否被候选注解（initAnnotationTypes 和 destroyAnnotationTypes）标注
			if (!AnnotationUtils.isCandidateClass(beanClass, this.initAnnotationTypes) &&
					!AnnotationUtils.isCandidateClass(beanClass, this.destroyAnnotationTypes)) {
				return this.emptyLifecycleMetadata;
			}

			List<LifecycleMethod> initMethods = new ArrayList<>();
			List<LifecycleMethod> destroyMethods = new ArrayList<>();
			Class<?> currentClass = beanClass;

			do {
				final List<LifecycleMethod> currInitMethods = new ArrayList<>();
				final List<LifecycleMethod> currDestroyMethods = new ArrayList<>();

				// 2. 获取类中被候选注解（initAnnotationTypes 和 destroyAnnotationTypes）标注的方法，
				// 并封装成 LifecycleMethod 添加到各自的集合（currInitMethods 和 currDestroyMethods）中
				ReflectionUtils.doWithLocalMethods(currentClass, method -> { // important -> go
					for (Class<? extends Annotation> initAnnotationType : this.initAnnotationTypes) {
						if (initAnnotationType != null && method.isAnnotationPresent(initAnnotationType)) {
							currInitMethods.add(new LifecycleMethod(method, beanClass)); // important -> go
							if (logger.isTraceEnabled()) {
								logger.trace("Found init method on class [" + beanClass.getName() + "]: " + method);
							}
						}
					}
					for (Class<? extends Annotation> destroyAnnotationType : this.destroyAnnotationTypes) {
						if (destroyAnnotationType != null && method.isAnnotationPresent(destroyAnnotationType)) {
							currDestroyMethods.add(new LifecycleMethod(method, beanClass)); // important -> go
							if (logger.isTraceEnabled()) {
								logger.trace("Found destroy method on class [" + beanClass.getName() + "]: " + method);
							}
						}
					}
				});

				initMethods.addAll(0, currInitMethods);
				destroyMethods.addAll(currDestroyMethods);
				currentClass = currentClass.getSuperclass();
			}
			while (currentClass != null && currentClass != Object.class);

			// 3. 使用 init 和 destroy 方法集创建一个 集合 LifecycleMetadata 并返回
			return (initMethods.isEmpty() && destroyMethods.isEmpty() ? this.emptyLifecycleMetadata :
					new LifecycleMetadata(beanClass, initMethods, destroyMethods)); // important -> go
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
						metadata = buildResourceMetadata(clazz); // important -> go
						this.injectionMetadataCache.put(cacheKey, metadata);
					}
				}
			}
			return metadata;
		}

		private InjectionMetadata buildResourceMetadata(Class<?> clazz) {
			// 1. 检查类是否被候选注解（resourceAnnotationTypes，在类型、方法或字段级别）标注
			// resourceAnnotationTypes = { @EJB（jakarta.ejb.EJB）, @Resource（jakarta.annotation.Resource）, @Resource（javax.annotation.Resource） }
			if (!AnnotationUtils.isCandidateClass(clazz, resourceAnnotationTypes)) {
				return InjectionMetadata.EMPTY;
			}

			List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
			Class<?> targetClass = clazz;

			do {
				final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

				// 2. 处理被候选注解标注的字段，并根据其相应的注解类型创建 LookupElement 实例添加到集合中
				// 在给定类中所有本地声明的字段上调用给定的回调函数。
				ReflectionUtils.doWithLocalFields(targetClass, field -> { // important -> go
					// 静态字段不支持 @EJB（jakarta.ejb.EJB） 注释；
					if (ejbAnnotationType != null && field.isAnnotationPresent(ejbAnnotationType)) { // important -> go
						if (Modifier.isStatic(field.getModifiers())) {
							throw new IllegalStateException("@EJB annotation is not supported on static fields");
						}
						// jakarta.ejb.EJB --> EjbRefElement
						// currElements.add(new EjbRefElement(field, field, null)); // important -> go
						// 存在报错，所有隐藏上一行代码
					}
					// 静态字段不支持 @Resource（jakarta.annotation.Resource） 注释；
					else if (jakartaResourceType != null && field.isAnnotationPresent(jakartaResourceType)) { // important -> go
						if (Modifier.isStatic(field.getModifiers())) {
							throw new IllegalStateException("@Resource annotation is not supported on static fields");
						}
						if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
							// jakarta.annotation.Resource --> ResourceElement
							// currElements.add(new ResourceElement(field, field, null)); // important -> go
							// 存在报错，所有隐藏上一行代码
						}
					}
					// 静态字段不支持 @Resource（javax.annotation.Resource） 注释；
					else if (javaxResourceType != null && field.isAnnotationPresent(javaxResourceType)) { // important -> go
						if (Modifier.isStatic(field.getModifiers())) {
							throw new IllegalStateException("@Resource annotation is not supported on static fields");
						}
						// javax.annotation.Resource --> LegacyResourceElement
						if (!this.ignoredResourceTypes.contains(field.getType().getName())) {
							// currElements.add(new LegacyResourceElement(field, field, null)); // important -> go
							// 存在报错，所有隐藏上一行代码
						}
					}
				});

				// 3. 处理被候选注解标注的方法，并根据其相应的注解类型创建 LookupElement 实例添加到集合中
				// 对给定类的所有匹配方法执行给定的回调操作，这些方法可以是本地声明的或等效的（例如，给定类实现的基于 Java 8 的接口上的默认方法）。
				ReflectionUtils.doWithLocalMethods(targetClass, method -> { // important -> go
					Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
					if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
						return;
					}
					if (ejbAnnotationType != null && bridgedMethod.isAnnotationPresent(ejbAnnotationType)) { // important -> go
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
							// currElements.add(new EjbRefElement(method, bridgedMethod, pd)); // important -> go
							// 存在报错，所有隐藏上一行代码
						}
					}
					else if (jakartaResourceType != null && bridgedMethod.isAnnotationPresent(jakartaResourceType)) { // important -> go
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
								// currElements.add(new ResourceElement(method, bridgedMethod, pd)); // important -> go
								// 存在报错，所有隐藏上一行代码
							}
						}
					}
					else if (javaxResourceType != null && bridgedMethod.isAnnotationPresent(javaxResourceType)) { // important -> go
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
								// currElements.add(new LegacyResourceElement(method, bridgedMethod, pd)); // important -> go
								// 存在报错，所有隐藏上一行代码
							}
						}
					}
				});

				elements.addAll(0, currElements);
				targetClass = targetClass.getSuperclass();
			}
			while (targetClass != null && targetClass != Object.class);

			// 4. 根据 LookupElement 实例集合和被处理类，创建一个 InjectionMetadata 实例并返回
			return InjectionMetadata.forElements(elements, clazz); // important -> go
		}

		// 表示有关注释的 init 和 destroy 方法的信息的类。
		private class LifecycleMetadata {
			private final Class<?> beanClass;
			private final Collection<LifecycleMethod> initMethods;
			private final Collection<LifecycleMethod> destroyMethods;
			private volatile Set<LifecycleMethod> checkedInitMethods;
			private volatile Set<LifecycleMethod> checkedDestroyMethods;

			public LifecycleMetadata(Class<?> beanClass, Collection<LifecycleMethod> initMethods,
									 Collection<LifecycleMethod> destroyMethods) {

				this.beanClass = beanClass;
				this.initMethods = initMethods;
				this.destroyMethods = destroyMethods;
			}

			public void checkInitDestroyMethods(RootBeanDefinition beanDefinition) { // important -> go
				Set<LifecycleMethod> checkedInitMethods = new LinkedHashSet<>(this.initMethods.size());
				for (LifecycleMethod lifecycleMethod : this.initMethods) { // important -> go
					String methodIdentifier = lifecycleMethod.getIdentifier();
					if (!beanDefinition.isExternallyManagedInitMethod(methodIdentifier)) {
						beanDefinition.registerExternallyManagedInitMethod(methodIdentifier);
						checkedInitMethods.add(lifecycleMethod);
						if (logger.isTraceEnabled()) {
							logger.trace("Registered init method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
						}
					}
				}
				Set<LifecycleMethod> checkedDestroyMethods = new LinkedHashSet<>(this.destroyMethods.size());
				for (LifecycleMethod lifecycleMethod : this.destroyMethods) { // important -> go
					String methodIdentifier = lifecycleMethod.getIdentifier();
					if (!beanDefinition.isExternallyManagedDestroyMethod(methodIdentifier)) {
						beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier);
						checkedDestroyMethods.add(lifecycleMethod);
						if (logger.isTraceEnabled()) {
							logger.trace("Registered destroy method on class [" + this.beanClass.getName() + "]: " + methodIdentifier);
						}
					}
				}
				this.checkedInitMethods = checkedInitMethods;
				this.checkedDestroyMethods = checkedDestroyMethods;
			}
			// ...
		}

		// 表示带注解的 init 或 destroy 方法的类。
		private static class LifecycleMethod {
			private final Method method;
			private final String identifier;
			public LifecycleMethod(Method method, Class<?> beanClass) {
				if (method.getParameterCount() != 0) {
					throw new IllegalStateException("Lifecycle annotation requires a no-arg method: " + method);
				}
				this.method = method;
				this.identifier = null;
				/*this.identifier = (isPrivateOrNotVisible(method, beanClass) ?
						ClassUtils.getQualifiedMethodName(method) : method.getName());*/
			}
			// ...
			public String getIdentifier() {
				return this.identifier;
			}
			// ...
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
	}
}
