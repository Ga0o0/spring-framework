package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 多实例无法确定 - 解决方案 - @Primary
 *
 * @see org.springframework.context.annotation.Primary
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineAutowireCandidate(java.util.Map, org.springframework.beans.factory.config.DependencyDescriptor)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determinePrimaryCandidate(java.util.Map, java.lang.Class)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#isPrimary(java.lang.String, java.lang.Object)
 */
public class AJR02_AtJakartaResource_MultiDependency_AtPrimary {

	interface Dao {}

	@Primary
	static class DaoImpl1 implements Dao {}
	static class DaoImpl2 implements Dao {}

	static class Service {
		@Resource
		private Dao dao;
	}

	public static void main(String[] args) {
		testByGenericApplicationContext();
		testByAnnotationConfigApplicationContext();
	}

	private static void testByGenericApplicationContext() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean(DaoImpl1.class);
		// 设置 BeanDefinition#primary 属性，原因：GenericApplicationContext 不会读取 @Primary 注解的内容设置给 BeanDefinition#primary
		context.registerBeanDefinition("daoImpl2",
				BeanDefinitionBuilder.rootBeanDefinition(DaoImpl2.class)
						.setPrimary(true)
						.getBeanDefinition()
		);
		commonTest(context);
	}

	private static void testByAnnotationConfigApplicationContext() {
		// 这里不能使用 GenericApplicationContext，原因：GenericApplicationContext 不会读取 @Primary 注解的内容设置给 BeanDefinition#primary
		// AnnotationConfigApplicationContext 能够读取 @Primary 注解的内容设置给 BeanDefinition#primary，源码大概再 BeanFactory#getBean() 中
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.registerBean(DaoImpl1.class);
		context.registerBean(DaoImpl2.class);
		commonTest(context);
	}

	private static void commonTest(GenericApplicationContext context) {
		context.registerBean(Service.class);
		context.refresh();

		// Test
		System.out.println("****************      	Print Services      	*****************************");
		Service service = context.getBean(Service.class);
		System.out.println("Service.dao=\t\t\t" + service.dao);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}
}