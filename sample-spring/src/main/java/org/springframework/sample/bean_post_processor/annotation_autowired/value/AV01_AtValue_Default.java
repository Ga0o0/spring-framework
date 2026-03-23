package org.springframework.sample.bean_post_processor.annotation_autowired.value;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @Value
 *
 * @see org.springframework.beans.factory.annotation.Value
 *
 * @see org.springframework.sample.bean_post_processor.resolve_dependency
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, String, java.util.Set, org.springframework.beans.TypeConverter)
 */
public class AV01_AtValue_Default {

	static class Dao {}

	public static class Service {
		@Value("")
		private Dao dao;
		private Dao daoMethodParam;
		private Dao daoMethod;
		private Dao setDaoMethod;

		public Service(@Value("") Dao dao) {
			this.daoMethodParam = dao;
		}

		@Value("")
		public Dao dao(Dao dao) {
			this.daoMethod = dao;
			return dao;
		}

		@Value("")
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
		System.out.println(context.getEnvironment().getProperty("user.home"));

		System.out.println("****************      	Print Services      	*****************************");
		Service service = context.getBean(Service.class);
		System.out.println("Service.dao=\t\t\t\t\t" 				+ service.dao);
		System.out.println("Service.daoMethodParam=\t\t\t" 			+ service.daoMethodParam);
		System.out.println("Service.daoMethod=\t\t\t\t" 			+ service.daoMethod);
		System.out.println("Service.setDaoMethod=\t\t\t" 			+ service.setDaoMethod);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
