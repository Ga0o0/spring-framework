package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 默认情况下
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#ResourceElement(java.lang.reflect.Member, java.lang.reflect.AnnotatedElement, java.beans.PropertyDescriptor)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#autowireResource(org.springframework.beans.factory.BeanFactory, org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LookupElement, java.lang.String)
 */
public class AJR01_AtJakartaResource_Default {
	static class Dao {}

	// 默认情况（不设置 @Resource#name）下，根据 @Resource 生成的 ResourceElement 的 ResourceElement#name 属性值为
	// 字段名或者方法名（方法名以 set 开头的需要去除 set 并按照字段命名的格式（首字母小写的驼峰命名）来处理）
	static class Service {
		@Resource
		private Dao dao;
		private Dao daoMethod;
		private Dao setDaoMethod;

		@Resource
		public Dao dao(Dao dao) {
			this.daoMethod = dao;
			return dao;
		}

		@Resource
		public Dao setDao(Dao dao) {
			this.setDaoMethod = dao;
			return dao;
		}
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
		System.out.println("Service.daoMethod=\t\t" + service.daoMethod);
		System.out.println("Service.setDaoMethod=\t" + service.setDaoMethod);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}
}