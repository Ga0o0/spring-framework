package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 为 @Resource#type 指定一个存在到 bean 名称
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#ResourceElement(java.lang.reflect.Member, java.lang.reflect.AnnotatedElement, java.beans.PropertyDescriptor)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#autowireResource(org.springframework.beans.factory.BeanFactory, org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 */
public class AJR04_AtJakartaResource_SpecType {
	static class Dao {}

	static class Service {
		@Resource(type = Dao.class)
		private Dao dao;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean(Dao.class);
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