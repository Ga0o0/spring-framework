package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.dependency_types;

import jakarta.annotation.Resource;
import jakarta.inject.Provider;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Provider
 *
 * @see jakarta.inject.Provider
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#jakartaInjectProviderClass
 */
public class DT04_DependencyTypes_Provider {
	static class Dao {}

	static class Service {
		@Resource
		private Provider<Dao> optionalDao;
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
		System.out.println("Service.dao=\t" + service.optionalDao);
		System.out.println("Service.dao=\t" + service.optionalDao.get());

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}
}