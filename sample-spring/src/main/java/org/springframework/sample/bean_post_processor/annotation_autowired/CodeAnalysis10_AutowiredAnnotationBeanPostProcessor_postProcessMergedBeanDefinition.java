package org.springframework.sample.bean_post_processor.annotation_autowired;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 查找被 autowired（@jakarta.inject.Inject、@javax.inject.Injec、t@Autowired、@Value） 注解标记的字段和方法并进行封装和检查
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#findInjectionMetadata(java.lang.String, java.lang.Class, org.springframework.beans.factory.support.RootBeanDefinition)
 *
 * ## 1. 查找被 autowired（@jakarta.inject.Inject、@javax.inject.Inject、t@Autowired、@Value） 注解标记的字段和方法，并封装成 AutowiredFieldElement、AutowiredMethodElement 放入 InjectionMetadata 中返回
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#findAutowiringMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#buildAutowiringMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement
 *
 * ## 2. 检查 InjectionMetadata#injectedElements 集合元素，并将检查后元素赋值给 InjectionMetadata#checkedElements
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkConfigMembers(org.springframework.beans.factory.support.RootBeanDefinition)
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkedElements
 */
public class CodeAnalysis10_AutowiredAnnotationBeanPostProcessor_postProcessMergedBeanDefinition {
	static class CodeAnalysis_AutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {
		private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);
		private final Set<String> lookupMethodsChecked = Collections.newSetFromMap(new ConcurrentHashMap<>(256));
		private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<>(256);
		private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

		@Override
		public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
			// 在 bean 定义上注册外部管理的配置成员。
			findInjectionMetadata(beanName, beanType, beanDefinition); // important -> go

			// 利用时机清除单例实例化后不再需要的缓存。injectionMetadataCache 本身保持不变，因为否则它无法通过外部管理的配置成员可靠地重建。
			if (beanDefinition.isSingleton()) {
				this.candidateConstructorsCache.remove(beanType);
				// 通过实际的查找覆盖，使其与 bean 定义一起保持完整。
				if (!beanDefinition.hasMethodOverrides()) {
					this.lookupMethodsChecked.remove(beanName);
				}
			}
		}

		private InjectionMetadata findInjectionMetadata(String beanName, Class<?> beanType, RootBeanDefinition beanDefinition) {
			InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null); // important -> go
			metadata.checkConfigMembers(beanDefinition);
			return metadata;
		}

		private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
			// 为了向后兼容自定义调用者，回退使用类名作为缓存键。
			String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
			// 首先快速检查并发映射，并使用最少的锁定。
			InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
			if (InjectionMetadata.needsRefresh(metadata, clazz)) {
				synchronized (this.injectionMetadataCache) {
					metadata = this.injectionMetadataCache.get(cacheKey);
					if (InjectionMetadata.needsRefresh(metadata, clazz)) {
						if (metadata != null) {
							metadata.clear(pvs);
						}
						metadata = buildAutowiringMetadata(clazz); // important -> go
						this.injectionMetadataCache.put(cacheKey, metadata);
					}
				}
			}
			return metadata;
		}

		private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
			// 1. 检查给定类是否被 autowired（autowiredAnnotationTypes=@jakarta.inject.Inject、@javax.inject.Inject、t@Autowired、@Value） 注解标注（在类型、方法或字段级别）
			if (!AnnotationUtils.isCandidateClass(clazz, this.autowiredAnnotationTypes)) {
				return InjectionMetadata.EMPTY;
			}

			final List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
			Class<?> targetClass = clazz;

			do {
				// 2. 获取被 autowired 注解标注的字段，并将其封装到 AutowiredFieldElement 对象中，最后添加到集合中
				final List<InjectionMetadata.InjectedElement> fieldElements = new ArrayList<>();
				// 在给定类中所有本地声明的字段上调用给定的回调函数。
				ReflectionUtils.doWithLocalFields(targetClass, field -> { // important -> go
					// 查找符合要求（@Autowired、@Value、@Inject 之一）的注解
					MergedAnnotation<?> ann = findAutowiredAnnotation(field); // important -> go
					if (ann != null) {
						// 静态字段不支持自动装配注释
						if (Modifier.isStatic(field.getModifiers())) {
							if (logger.isInfoEnabled()) {
								logger.info("Autowired annotation is not supported on static fields: " + field);
							}
							return;
						}
						// 确定被注解的字段或方法是否需要依赖项。即：根据注解的 required 字段进行判断
						boolean required = determineRequiredStatus(ann); // 获取属性 @Autowired#required 的值
						// add AutowiredFieldElement to fieldElements
						// fieldElements.add(new AutowiredFieldElement(field, required)); // important -> go
						// 存在报错，所有隐藏上一行代码
					}
				});

				// 3. 获取被 autowired 注解标注的方法，并将其封装到 AutowiredMethodElement 对象中，最后添加到集合中
				final List<InjectionMetadata.InjectedElement> methodElements = new ArrayList<>();
				// 对给定类的所有匹配方法执行给定的回调操作，这些方法可以是本地声明的或等效的（例如，给定类实现的基于 Java 8 的接口上的默认方法）。
				ReflectionUtils.doWithLocalMethods(targetClass, method -> { // important -> go
					// 查找提供的 {@link Method bridge Method} 的本地原始方法。
					Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
					// 比较桥接方法和它所桥接的方法的签名。
					if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
						return;
					}
					// 查找符合要求（@Autowired、@Value、@Inject 之一）的注解
					MergedAnnotation<?> ann = findAutowiredAnnotation(bridgedMethod); // important -> go
					// 给定一个可能来自接口的方法，以及当前反射调用中使用的目标类，如果存在则查找相应的目标方法;
					if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
						// 静态方法不支持自动装配注释
						if (Modifier.isStatic(method.getModifiers())) {
							if (logger.isInfoEnabled()) {
								logger.info("Autowired annotation is not supported on static methods: " + method);
							}
							return;
						}
						// 自动装配注释只能用于有参数的方法
						if (method.getParameterCount() == 0) {
							if (method.getDeclaringClass().isRecord()) {
								// Annotations on the compact constructor arguments made available on accessors, ignoring.
								// --> 译文：对访问器上可用的紧凑构造函数参数的注释，忽略。
								return;
							}
							if (logger.isInfoEnabled()) {
								logger.info("Autowired annotation should only be used on methods with parameters: " +
										method);
							}
						}
						// 确定被注解的字段或方法是否需要依赖项。即：根据注解的 required 字段进行判断
						boolean required = determineRequiredStatus(ann); // 获取属性 @Autowired#required 的值
						// 为给定方法查找 JavaBean {@code PropertyDescriptor}，该方法可以是该 bean 属性的读取方法或写入方法。
						PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
						// add AutowiredMethodElement to fieldElements
						// methodElements.add(new AutowiredMethodElement(method, required, pd)); // important -> go
						// 存在报错，所有隐藏上一行代码
					}
				});

				elements.addAll(0, sortMethodElements(methodElements, targetClass));
				elements.addAll(0, fieldElements);
				targetClass = targetClass.getSuperclass();
			}
			while (targetClass != null && targetClass != Object.class);

			// 4. 使用被 autowired 注解标注的字段和方法封装成的 AutowiredFieldElement、AutowiredMethodElement 的对象，创建一个 InjectionMetadata 对象返回
			return InjectionMetadata.forElements(elements, clazz); // important -> go
		}

		private MergedAnnotation<?> findAutowiredAnnotation(AccessibleObject ao) {
			// 创建一个新的 {@link MergedAnnotations} 实例，其中包含指定元素的所有注释和元注释。
			MergedAnnotations annotations = MergedAnnotations.from(ao);
			for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
				MergedAnnotation<?> annotation = annotations.get(type);
				if (annotation.isPresent()) {
					return annotation;
				}
			}
			return null;
		}

		// 如果可能，通过 ASM 对方法元素进行排序，以确定声明顺序。
		private List<InjectionMetadata.InjectedElement> sortMethodElements(
				List<InjectionMetadata.InjectedElement> methodElements, Class<?> targetClass) {
			// ...
			return methodElements;
		}

	}
}
