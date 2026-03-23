package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 多实例无法确定 - 问题描述
 *
 * <p>问题描述：多实例注入导致无法选择，从而报错
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineAutowireCandidate(java.util.Map, org.springframework.beans.factory.config.DependencyDescriptor)
 */
public class AJR01_AtJakartaResource_MultiDependency_ExceptionDesc {

	interface Dao {}
	
	static class DaoImpl1 implements Dao {}
	static class DaoImpl2 implements Dao {}

	static class Service {
		@Resource
		private Dao dao;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean(DaoImpl1.class);
		context.registerBean(DaoImpl2.class);
		// DaoImpl1 和 DaoImpl2 都是 Dao 的实例，系统无法确定选择哪个，故报错：
		// Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'Dao' available: expected single matching bean but found 2: DaoImpl1, DaoImpl2
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
