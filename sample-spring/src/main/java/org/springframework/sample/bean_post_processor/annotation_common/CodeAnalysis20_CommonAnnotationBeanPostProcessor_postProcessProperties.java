package org.springframework.sample.bean_post_processor.annotation_common;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向被候选 Resource 注解标记的字段和方法注入数据
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#postProcessProperties(org.springframework.beans.PropertyValues, java.lang.Object, java.lang.String)
 *
 * ## 1. 收集被候选 Resource 注解（@EJB（jakarta.ejb.EJB）, @Resource（jakarta.annotation.Resource）, @Resource（javax.annotation.Resource））标记的字段和方法，并封装成 InjectionMetadata 实例返回
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
 * ## 2. 向被候选 Resource 注解标记的字段和方法注入数据
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#getResourceToInject(java.lang.Object, java.lang.String)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.EjbRefElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LegacyResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 */
public class CodeAnalysis20_CommonAnnotationBeanPostProcessor_postProcessProperties {
	static class CodeAnalysis_CommonAnnotationBeanPostProcessor extends CommonAnnotationBeanPostProcessor {
		private static final long serialVersionUID = 1L;
		private static final Set<Class<? extends Annotation>> resourceAnnotationTypes = new LinkedHashSet<>(4);
		private static final Class<? extends Annotation> jakartaResourceType;
		private static final Class<? extends Annotation> javaxResourceType;
		private static final Class<? extends Annotation> ejbAnnotationType;
		static {
			jakartaResourceType = loadAnnotationType("jakarta.annotation.Resource"); // important -> go
			if (jakartaResourceType != null) {
				resourceAnnotationTypes.add(jakartaResourceType);
			}

			javaxResourceType = loadAnnotationType("javax.annotation.Resource"); // important -> go
			if (javaxResourceType != null) {
				resourceAnnotationTypes.add(javaxResourceType);
			}

			ejbAnnotationType = loadAnnotationType("jakarta.ejb.EJB"); // important -> go
			if (ejbAnnotationType != null) {
				resourceAnnotationTypes.add(ejbAnnotationType);
			}
		}
		private final Set<String> ignoredResourceTypes = new HashSet<>(1);
		private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

		@Override
		public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
			InjectionMetadata metadata = findResourceMetadata(beanName, bean.getClass(), pvs); // important -> go
			try {
				metadata.inject(bean, beanName, pvs); // important -> go
			}
			catch (Throwable ex) {
				throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
			}
			return pvs;
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
						metadata = buildResourceMetadata(clazz);  // important -> go
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
				ReflectionUtils.doWithLocalFields(targetClass, field -> {
					// 静态字段不支持 @EJB（jakarta.ejb.EJB） 注释；
					if (ejbAnnotationType != null && field.isAnnotationPresent(ejbAnnotationType)) {
						if (Modifier.isStatic(field.getModifiers())) {
							throw new IllegalStateException("@EJB annotation is not supported on static fields");
						}
						// jakarta.ejb.EJB --> EjbRefElement
						// currElements.add(new EjbRefElement(field, field, null)); // important -> go
						// 存在报错，所有隐藏上一行代码
					}
					// 静态字段不支持 @Resource（jakarta.annotation.Resource） 注释；
					else if (jakartaResourceType != null && field.isAnnotationPresent(jakartaResourceType)) {
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
					else if (javaxResourceType != null && field.isAnnotationPresent(javaxResourceType)) {
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
							// currElements.add(new EjbRefElement(method, bridgedMethod, pd)); // important -> go
							// 存在报错，所有隐藏上一行代码
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
								// currElements.add(new ResourceElement(method, bridgedMethod, pd)); // important -> go
								// 存在报错，所有隐藏上一行代码
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
			return InjectionMetadata.forElements(elements, clazz);
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
