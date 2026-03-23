package org.springframework.sample.bean_post_processor.annotation_autowired;

/*import jakarta.inject.Provider;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.ConstructorResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.NullBean;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;*/

/**
 * DefaultListableBeanFactory#doResolveDependency(...)
 *
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 */
public class CodeAnalysis70_AutowireCapableBeanFactory_resolveDependency {
	/*static class CodeAnalysis70_DefaultListableBeanFactory extends DefaultListableBeanFactory {

		private static Class<?> jakartaInjectProviderClass;

		static {
			try {
				jakartaInjectProviderClass = ClassUtils.forName("jakarta.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
			} catch (ClassNotFoundException ex) {
				jakartaInjectProviderClass = null;
			}
		}

		public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
										@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
			// 初始化底层方法参数的参数名称发现（如果有）。
			descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
			// 1. 处理 Optional 类型
			if (Optional.class == descriptor.getDependencyType()) {
				return createOptionalDependency(descriptor, requestingBeanName);    // important -> go
			}
			// 2. 处理 ObjectFactory / ObjectProvider 类型
			else if (ObjectFactory.class == descriptor.getDependencyType() ||
					ObjectProvider.class == descriptor.getDependencyType()) {
				return new DependencyObjectProvider(descriptor, requestingBeanName);
			}
			// 3. 处理 jakarta.inject.Provider 类型
			else if (jakartaInjectProviderClass == descriptor.getDependencyType()) {
				return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
			}
			// 4. 处理依赖项延迟解析
			else if (descriptor.supportsLazyResolution()) {
				Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
						descriptor, requestingBeanName);
				if (result != null) {
					return result;
				}
			}
			// 5. 常规类型：执行核心解析
			return doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter); // go
		}

		@Nullable
		public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
										  @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

			InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
			try {
				// 步骤 1：单个 bean 匹配的预解析快捷方式，例如 @Autowired
				Object shortcut = descriptor.resolveShortcut(this);
				if (shortcut != null) {
					return shortcut;
				}

				Class<?> type = descriptor.getDependencyType();

				// 步骤 2：预定义值或表达式，例如来自 @Value
				Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
				if (value != null) {
					if (value instanceof String strValue) {
						String resolvedValue = resolveEmbeddedValue(strValue);
						BeanDefinition bd = (beanName != null && containsBean(beanName) ?
								getMergedBeanDefinition(beanName) : null);
						value = evaluateBeanDefinitionString(resolvedValue, bd);
					}
					TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
					try {
						// 解析 ${...} 占位符和 #{...} SpEL表达式
						return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
					} catch (UnsupportedOperationException ex) {
						// 自定义 TypeConverter 不支持 TypeDescriptor 解析...
						return (descriptor.getField() != null ?
								converter.convertIfNecessary(value, type, descriptor.getField()) :
								converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
					}
				}

				// 步骤 3a：多个 bean 作为流/数组/标准集合/普通映射
				Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
				if (multipleBeans != null) {
					return multipleBeans;
				}
				// 步骤 3b：直接 bean 匹配，可能是 Collection / Map 类型的直接 bean
				// 查找符合所需类型的 Bean 实例。在为指定 Bean 自动装配时调用。
				Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
				if (matchingBeans.isEmpty()) {
					// 步骤 3c（回退）：用于收集多个 bean 的自定义 Collection / Map 声明
					multipleBeans = resolveMultipleBeansFallback(descriptor, beanName, autowiredBeanNames, typeConverter);
					if (multipleBeans != null) {
						return multipleBeans;
					}
					// Raise exception if nothing found for required injection point --> 译文：如果未找到所需注入点，则引发异常
					if (isRequired(descriptor)) {
						raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
					}
					return null;
				}

				String autowiredBeanName;
				Object instanceCandidate;

				// 步骤 4：确定单一候选人
				if (matchingBeans.size() > 1) {
					// 在给定的 bean 集合中确定自动装配候选对象。
					autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
					if (autowiredBeanName == null) {
						if (isRequired(descriptor) || !indicatesArrayCollectionOrMap(type)) {
							// 如果未找到所需注入点的明确匹配，则引发异常
							return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
						} else {
							// 对于可选的 Collection/Map，默默忽略非唯一情况：可能它本来就是多个常规 bean 的空集合（特别是在 4.3 之前，我们甚至没有寻找集合 bean）。
							return null;
						}
					}
					instanceCandidate = matchingBeans.get(autowiredBeanName);
				} else {
					// 我们只有一个匹配项。
					Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
					autowiredBeanName = entry.getKey();
					instanceCandidate = entry.getValue();
				}

				// 步骤 5：验证单个结果
				if (autowiredBeanNames != null) {
					autowiredBeanNames.add(autowiredBeanName);
				}
				if (instanceCandidate instanceof Class) {
					// 将指定的 bean 名称（作为此依赖项的匹配算法的候选结果）解析为来自给定工厂的 bean 实例。
					instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
				}
				Object result = instanceCandidate;
				if (result instanceof NullBean) {
					if (isRequired(descriptor)) {
						// 如果所需注入点出现空值，则引发异常
						raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
					}
					result = null;
				}
				// 判断给定类型是否可以通过给定值进行赋值（假设通过反射设置）。将原始包装类视为可赋值给相应的原始类型。
				if (!ClassUtils.isAssignableValue(type, result)) {
					throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
				}
				return result;
			} finally {
				ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
			}
		}
	}*/
}
