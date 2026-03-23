package org.springframework.sample.bean_post_processor.annotation_autowired.autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Autowired
 *
 * @see Autowired
 *
 * @see AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 */
public class AA01_AtAutowired_Default {

	static class Dao {}

	static class Service {
		@Autowired
		private Dao dao;
		private Dao daoConstructorMethod;
		private Dao daoMethod;
		private Dao setDaoMethod;

		@Autowired
		public Service(Dao dao) {
			this.daoConstructorMethod = dao;
		}

		@Autowired
		public Dao dao(Dao dao) {
			this.daoMethod = dao;
			return dao;
		}

		@Autowired
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
