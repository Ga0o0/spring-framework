package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.multi_dependency;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * 多实例无法确定 - 解决方案 - @Priority
 *
 * @see jakarta.annotation.Priority
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineAutowireCandidate(java.util.Map, org.springframework.beans.factory.config.DependencyDescriptor)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#determineHighestPriorityCandidate(java.util.Map, java.lang.Class)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#getPriority(java.lang.Object)
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#getDependencyComparator()
 * @see org.springframework.core.OrderComparator#getPriority(java.lang.Object)
 */
public class AJR03_AtJakartaResource_MultiDependency_AtPriority {
	interface Dao {}

	@Priority(Integer.MAX_VALUE)
	static class DaoImpl1 implements Dao {}
	// @Priority(Integer.MAX_VALUE)
	@Priority(Integer.MAX_VALUE - 1)
	static class DaoImpl2 implements Dao {}
	// 当存在两个或两个以上的实例的 优先级一样时，会报错：
	// Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'Dao' available: Multiple beans found with the same highest priority (2147483647) among candidates: [DaoImpl1, DaoImpl2]

	static class Service {
		@Resource
		private Dao dao;
	}
	
	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();

		// 设置 DefaultListableBeanFactory#dependencyComparator；原因：@Priority 注解的解析依赖 Comparator
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		if (beanFactory instanceof DefaultListableBeanFactory factory) {
			factory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
		}

		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean(DaoImpl1.class);
		context.registerBean(DaoImpl2.class);
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