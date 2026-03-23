/**
 * @Value
 *
 * @see org.springframework.beans.factory.annotation.Value
 *
 * @see org.springframework.sample.bean_post_processor.resolve_dependency
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 */
package org.springframework.sample.bean_post_processor.annotation_autowired.value;
/*		@Value
********************************* Class API Docs *********************************
用于字段或方法/构造函数参数级别的注解，指示被注解元素的默认值表达式。

通常用于表达式驱动或属性驱动的依赖注入。也支持动态解析处理程序方法参数——例如，在 Spring MVC 中。

一个常见的用例是使用 #{systemProperties.myProp} 风格的 SpEL（Spring 表达式语言）表达式注入值。或者，也可以使用 ${my.app.myProp} 风格的属性占位符注入值。

请注意，@Value 注解的实际处理由 BeanPostProcessor 执行，这意味着您不能在 BeanPostProcessor 或 BeanFactoryPostProcessor 类型中使用 @Value。
请参阅 AutowiredAnnotationBeanPostProcessor 类的 Javadoc（默认情况下，该类会检查是否存在此注解）。


********************************* Class Definition *********************************
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
	String value();
}
**/

/* ************************ DefaultListableBeanFactory#doResolveDependency(...) ************************
@Nullable
public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
		@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

	InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
	try {
		// ...

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
			}
			catch (UnsupportedOperationException ex) {
				// A custom TypeConverter which does not support TypeDescriptor resolution...
				// --> 译文：自定义 TypeConverter 不支持 TypeDescriptor 解析...
				return (descriptor.getField() != null ?
						converter.convertIfNecessary(value, type, descriptor.getField()) :
						converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
			}
			// ...
		}
		// ...
	}
	// ...
}
**/