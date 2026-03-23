package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 为 @Resource#type 指定一个存在到 bean 名称
 *
 * @see org.springframework.context.annotation.Lazy
 *
 * @see CommonAnnotationBeanPostProcessor.ResourceElement#ResourceElement(java.lang.reflect.Member, java.lang.reflect.AnnotatedElement, java.beans.PropertyDescriptor)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#buildLazyResourceProxy(org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 * @see org.springframework.aop.framework.ProxyFactory#getProxy(java.lang.ClassLoader)
 */
public class AJR05_AtJakartaResource_AtLazy {
	static class Dao {}

	static class Service {
		@Lazy
		@Resource
		private Dao dao;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		// context.registerBean(Dao.class);
		context.registerBeanDefinition("dao", BeanDefinitionBuilder.rootBeanDefinition(Dao.class).getBeanDefinition());
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