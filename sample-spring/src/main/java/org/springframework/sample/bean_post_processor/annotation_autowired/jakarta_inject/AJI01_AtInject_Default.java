package org.springframework.sample.bean_post_processor.annotation_autowired.jakarta_inject;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import jakarta.inject.Inject;

/**
 * @jakarta.inject.Inject
 *
 * @see jakarta.inject.Inject
 *
 * @see org.springframework.sample.bean_post_processor.annotation_autowired.autowired
 * @see org.springframework.sample.bean_post_processor.resolve_dependency
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 */
public class AJI01_AtInject_Default {

	static class Dao {}

	static class Service {
		@Inject
		private Dao dao;
		private Dao daoConstructorMethod;
		private Dao daoMethod;
		private Dao setDaoMethod;

		@Inject
		public Service(Dao dao) {
			this.daoConstructorMethod = dao;
		}

		@Inject
		public Dao dao(Dao dao) {
			this.daoMethod = dao;
			return dao;
		}

		@Inject
		public Dao setDao(Dao dao) {
			this.setDaoMethod = dao;
			return dao;
		}
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
		context.registerBean(Dao.class);
		context.registerBean(Service.class);
		context.refresh();

		// Test
		System.out.println("****************      	Print Services      	*****************************");
		Service service = context.getBean(Service.class);
		System.out.println("Service.dao=\t\t\t\t\t" 				+ service.dao);
		System.out.println("Service.daoConstructorMethod=\t" 		+ service.daoConstructorMethod);
		System.out.println("Service.daoMethod=\t\t\t\t" 			+ service.daoMethod);
		System.out.println("Service.setDaoMethod=\t\t\t" 			+ service.setDaoMethod);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
