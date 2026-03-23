package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 多实例无法确定 - 解决方案 - 候选实例名 和 属性或方法名 相同
 *
 * @see Priority
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineAutowireCandidate(java.util.Map, org.springframework.beans.factory.config.DependencyDescriptor)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#matchesBeanName(String, String)
 */
public class AJR04_AtJakartaResource_MultiDependency_DependencyName {
	interface Dao {}

	static class DaoImpl1 implements Dao {}
	static class DaoImpl2 implements Dao {}

	static class Service {
		@Resource
		private Dao dao1;
	}
	
	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean("dao1", DaoImpl1.class); // 这里 beanName 一定要和 属性或方法名 相同，才能避免多实例无法选择的异常
		context.registerBean(DaoImpl2.class);
		context.registerBean(Service.class);
		context.refresh();

		// Test
		System.out.println("****************      	Print Services      	*****************************");
		Service service = context.getBean(Service.class);
		System.out.println("Service.dao=\t\t\t" + service.dao1);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}
}