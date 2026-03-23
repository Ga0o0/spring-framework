package org.springframework.sample.bean_post_processor.annotation_autowired.autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Autowired
 *
 * @see Autowired
 *
 * @see AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * ## 1. 检查 required 是否为 true
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#isRequired(org.springframework.beans.factory.config.DependencyDescriptor)
 * @see org.springframework.beans.factory.config.DependencyDescriptor#isRequired()
 *
 * ## 2. required == true 情况下的处理
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#raiseNoMatchingBeanFound(java.lang.Class, org.springframework.core.ResolvableType, org.springframework.beans.factory.config.DependencyDescriptor)
 */
public class AA01_AtAutowired_Fields {

	static class Dao {}

	static class Service {
		// 当容器中不存在 Dao.class 的实例时，@Autowired#required = true 会报错：
		// Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'Dao' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
		// @Autowired#required = false 不会报错；
		@Autowired(required = false) // required：声明带注解的依赖项是否为必需项
		private Dao dao;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
		// context.registerBean(Dao.class);
		context.registerBean(Service.class);
		context.refresh();

		// Test
		System.out.println("****************      	Print Services      	*****************************");
		Service service = context.getBean(Service.class);
		System.out.println("Service.dao=\t\t\t" 		+ service.dao);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
