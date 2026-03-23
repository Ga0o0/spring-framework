package org.springframework.sample.bean_post_processor.annotation_autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.beans.factory.annotation.Value
 * @see jakarta.inject.Inject
 * @see javax.inject.Inject
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 */
public class AA01_Autowired_Simple {

	static class Dao {}
	static class AutowiredService {
		@Autowired
		private Dao dao;
	}
	static class JakartaInjectService {
		@jakarta.inject.Inject
		private Dao dao;
	}
	/*static class JavaxInjectService {
		@javax.inject.Inject
		private Dao dao;
	}*/
	static class ValueService {
		@Value("")
		private Dao dao;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
		context.registerBean(Dao.class);
		context.registerBean(AutowiredService.class);
		context.registerBean(JakartaInjectService.class);
		// context.registerBean(JavaxInjectService.class);
		context.registerBean(ValueService.class);
		context.refresh();

		// Test
		System.out.println("****************      Print Services      *****************************");
		AutowiredService 		service1 = context.getBean(AutowiredService.class);
		JakartaInjectService 	service2 = context.getBean(JakartaInjectService.class);
		// JavaxInjectService 		service3 = context.getBean(JavaxInjectService.class);
		ValueService 			service4 = context.getBean(ValueService.class);
		System.out.println("AutowiredService.dao=" 		+ service1.dao);
		System.out.println("JakartaInjectService.dao=" 	+ service2.dao);
		// System.out.println("JavaxInjectService.dao=" 	+ service3.dao);
		System.out.println("ValueService.dao=" 			+ service4.dao);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
