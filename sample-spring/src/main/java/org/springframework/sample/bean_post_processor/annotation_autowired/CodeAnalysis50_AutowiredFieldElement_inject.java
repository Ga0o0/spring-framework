package org.springframework.sample.bean_post_processor.annotation_autowired;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * AutowiredFieldElement#inject(...)
 *
 * # 1. AutowiredFieldElement#inject(...)
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 *
 * ## 1.1. 存在缓存
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#resolveCachedArgument(java.lang.String, java.lang.Object)
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * ## 1.2. 不存在缓存；AutowiredFieldElement#resolveFieldValue(...) 会缓存解析的内容
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#resolveFieldValue(java.lang.reflect.Field, java.lang.Object, java.lang.String)
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * # 2. AutowireCapableBeanFactory#resolveDependency(...)
 *
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 */
public class CodeAnalysis50_AutowiredFieldElement_inject {

	static class CodeAnalysis01_AutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {
		private ConfigurableListableBeanFactory beanFactory;
		private class AutowiredFieldElement extends AutowiredElement {
			private volatile boolean cached;
			private volatile Object cachedFieldValue;
			public AutowiredFieldElement(Field field, boolean required) {
				super(field, null, required);
			}
			@Override
			protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
				Field field = (Field) this.member;
				Object value;
				// 1. 缓存检查
				if (this.cached) {
					try {
						// 已缓存参数数组，直接使用
						value = resolveCachedArgument(beanName, this.cachedFieldValue);  // important -> go
					}
					catch (BeansException ex) {
						this.cached = false;
						// ...
						value = resolveFieldValue(field, bean, beanName);	// important -> go
					}
				}
				else {
					// invoke
					value = resolveFieldValue(field, bean, beanName);	// important -> go
				}
				if (value != null) {
					ReflectionUtils.makeAccessible(field);
					field.set(bean, value);
				}
			}

			private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName) {
				// 1，解析字段值
				DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
				desc.setContainingClass(bean.getClass());
				Set<String> autowiredBeanNames = new LinkedHashSet<>(2);
				Assert.state(beanFactory != null, "No BeanFactory available");
				TypeConverter typeConverter = beanFactory.getTypeConverter();
				Object value;
				try {
					value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);  // important -> go
				}
				catch (BeansException ex) {
					throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
				}

				// 2. 缓存解析的数据
				synchronized (this) {
					if (!this.cached) {
						if (value != null || this.required) {
							Object cachedFieldValue = desc;
							registerDependentBeans(beanName, autowiredBeanNames);	// important -> go
							if (value != null && autowiredBeanNames.size() == 1) {
								String autowiredBeanName = autowiredBeanNames.iterator().next();
								if (beanFactory.containsBean(autowiredBeanName) &&
										beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
									cachedFieldValue = new ShortcutDependencyDescriptor(desc, autowiredBeanName);
								}
							}
							this.cachedFieldValue = cachedFieldValue;
							this.cached = true;
						}
						else {
							this.cachedFieldValue = null;
							// cached flag remains false
						}
					}
				}
				return value;
			}
		}

		@Nullable
		private Object resolveCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
			if (cachedArgument instanceof DependencyDescriptor descriptor) {
				Assert.state(this.beanFactory != null, "No BeanFactory available");
				return this.beanFactory.resolveDependency(descriptor, beanName, null, null); // important -> go
			}
			else {
				return cachedArgument;
			}
		}

		// 将指定的 bean 注册为依赖于自动装配的 bean。
		private void registerDependentBeans(@Nullable String beanName, Set<String> autowiredBeanNames) {
			if (beanName != null) {
				for (String autowiredBeanName : autowiredBeanNames) {
					if (this.beanFactory != null && this.beanFactory.containsBean(autowiredBeanName)) {
						this.beanFactory.registerDependentBean(autowiredBeanName, beanName); // important -> go
					}
					// ...
				}
			}
		}

		private static class ShortcutDependencyDescriptor extends DependencyDescriptor {
			private static final long serialVersionUID = 1L;
			private final String shortcut;
			public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut) {
				super(original);
				this.shortcut = shortcut;
			}
			// ...
		}

		// 表示注入信息的基类。
		private abstract static class AutowiredElement extends InjectionMetadata.InjectedElement {
			protected final boolean required;
			protected AutowiredElement(Member member, @Nullable PropertyDescriptor pd, boolean required) {
				super(member, pd);
				this.required = required;
			}
		}
	}

}
