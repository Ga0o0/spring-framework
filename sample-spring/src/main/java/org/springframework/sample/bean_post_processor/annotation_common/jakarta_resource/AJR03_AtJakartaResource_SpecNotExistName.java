package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 为 @Resource#name 指定一个不存在到 bean 名称
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#ResourceElement(java.lang.reflect.Member, java.lang.reflect.AnnotatedElement, java.beans.PropertyDescriptor)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#autowireResource(org.springframework.beans.factory.BeanFactory, org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 */
public class AJR03_AtJakartaResource_SpecNotExistName {
	static class Dao {
	}
	
	static class Service {
		@Resource(name = "dao1")
		private Dao dao;
	}
	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean("dao", Dao.class);
		// 因为指定 @Resource#name = dao1，而这里设置 Dao.class 实例的名称为 dao，它们不一致，故会报错
		// 错误简述：Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'dao' available
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