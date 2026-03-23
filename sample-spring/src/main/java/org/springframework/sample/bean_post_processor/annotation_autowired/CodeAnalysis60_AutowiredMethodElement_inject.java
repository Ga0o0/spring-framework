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
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * AutowiredMethodElement#inject(...)
 *
 * # 1. AutowiredMethodElement#inject(...)
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 *
 * ## 1.1. 存在缓存
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#resolveCachedArguments(java.lang.String, java.lang.Object[])
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#resolveCachedArgument(java.lang.String, java.lang.Object)
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * ## 1.2. 不存在缓存；AutowiredMethodElement#resolveMethodArguments(...) 会缓存解析的内容
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#resolveMethodArguments(java.lang.reflect.Method, java.lang.Object, java.lang.String)
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * # 2. AutowireCapableBeanFactory#resolveDependency(...)
 *
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 */
public class CodeAnalysis60_AutowiredMethodElement_inject {
	static class CodeAnalysis01_AutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {
		private ConfigurableListableBeanFactory beanFactory;
		// 表示有关注释方法的注入信息的类。
		private class AutowiredMethodElement extends AutowiredElement {
			private volatile boolean cached;
			private volatile Object[] cachedMethodArguments;
			public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
				super(method, pd, required);
			}

			@Override
			protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
				if (!shouldInject(pvs)) {
					return;
				}
				Method method = (Method) this.member;
				Object[] arguments;
				if (this.cached) {
					try {
						arguments = resolveCachedArguments(beanName, this.cachedMethodArguments);	// important -> go
					}
					catch (BeansException ex) {
						this.cached = false;
						// ...
						arguments = resolveMethodArguments(method, bean, beanName);	// important -> go
					}
				}
				else {
					arguments = resolveMethodArguments(method, bean, beanName);	// important -> go
				}
				if (arguments != null) {
					try {
						ReflectionUtils.makeAccessible(method);
						method.invoke(bean, arguments);
					}
					catch (InvocationTargetException ex) {
						throw ex.getTargetException();
					}
				}
			}

			@Nullable
			private Object[] resolveCachedArguments(@Nullable String beanName, @Nullable Object[] cachedMethodArguments) {
				if (cachedMethodArguments == null) {
					return null;
				}
				Object[] arguments = new Object[cachedMethodArguments.length];
				for (int i = 0; i < arguments.length; i++) {
					arguments[i] = resolveCachedArgument(beanName, cachedMethodArguments[i]); 	// important -> go
				}
				return arguments;
			}

			@Nullable
			private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
				// 1，解析方法参数
				int argumentCount = method.getParameterCount();
				Object[] arguments = new Object[argumentCount];
				DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
				Set<String> autowiredBeanNames = new LinkedHashSet<>(argumentCount * 2);
				Assert.state(beanFactory != null, "No BeanFactory available");
				TypeConverter typeConverter = beanFactory.getTypeConverter();
				for (int i = 0; i < arguments.length; i++) {
					MethodParameter methodParam = new MethodParameter(method, i);
					DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, this.required);
					currDesc.setContainingClass(bean.getClass());
					descriptors[i] = currDesc;
					try {
						Object arg = beanFactory.resolveDependency(currDesc, beanName, autowiredBeanNames, typeConverter);	// important -> go
						if (arg == null && !this.required && !methodParam.isOptional()) {
							arguments = null;
							break;
						}
						arguments[i] = arg;
					}
					catch (BeansException ex) {
						throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
					}
				}

				// 2. 缓存解析的数据
				synchronized (this) {
					if (!this.cached) {
						if (arguments != null) {
							DependencyDescriptor[] cachedMethodArguments = Arrays.copyOf(descriptors, argumentCount);
							registerDependentBeans(beanName, autowiredBeanNames);	// important -> go
							if (autowiredBeanNames.size() == argumentCount) {
								Iterator<String> it = autowiredBeanNames.iterator();
								Class<?>[] paramTypes = method.getParameterTypes();
								for (int i = 0; i < paramTypes.length; i++) {
									String autowiredBeanName = it.next();
									if (arguments[i] != null && beanFactory.containsBean(autowiredBeanName) &&
											beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
										cachedMethodArguments[i] = new ShortcutDependencyDescriptor(
												descriptors[i], autowiredBeanName);
									}
								}
							}
							this.cachedMethodArguments = cachedMethodArguments;
							this.cached = true;
						}
						else {
							this.cachedMethodArguments = null;
							// cached flag remains false
						}
					}
				}
				return arguments;
			}

		}

		// 解析指定的缓存方法参数或字段值。
		@Nullable
		private Object resolveCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
			if (cachedArgument instanceof DependencyDescriptor descriptor) {
				Assert.state(this.beanFactory != null, "No BeanFactory available");
				return this.beanFactory.resolveDependency(descriptor, beanName, null, null);	// important -> go
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

		// 表示注入信息的基类。
		private abstract static class AutowiredElement extends InjectionMetadata.InjectedElement {
			protected final boolean required;
			protected AutowiredElement(Member member, @Nullable PropertyDescriptor pd, boolean required) {
				super(member, pd);
				this.required = required;
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
	}

}
