package org.springframework.sample.bean_post_processor.annotation_common.jakarta_resource.dependency_types;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * ObjectFactory
 *
 * @see org.springframework.beans.factory.ObjectFactory
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, java.lang.String, java.util.Set, org.springframework.beans.TypeConverter)
 */
public class DT02_DependencyTypes_ObjectFactory {
	static class Dao {}

	static class Service {
		@Resource
		private ObjectFactory<Dao> optionalDao;
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
		System.out.println("Service.dao=\t" + service.optionalDao.getObject());

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}
}