package org.springframework.sample.bean_post_processor.annotation_common;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @see jakarta.annotation.Resource
 * @see javax.annotation.Resource
 * @see jakarta.ejb.EJB
 *
 * @see jakarta.annotation.PostConstruct
 * @see jakarta.annotation.PreDestroy
 * @see javax.annotation.PostConstruct
 * @see javax.annotation.PreDestroy
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
public class AC01_Common_Simple {

	static class Dao {}
	static class BaseService {
		@PostConstruct
		public void init() {
			System.out.println("init called");
		}
		@PreDestroy
		public void destroy() {
			System.out.println("destroy called");
		}
	}
	static class JakartaResourceService extends BaseService {
		@jakarta.annotation.Resource
		private Dao dao;
	}
	/*static class JavaxResourceService extends BaseService {
		@javax.annotation.Resource
		private Dao dao;
	}*/
	/*static class EJBService extends BaseService {
		@jakarta.ejb.EJB
		private Dao dao;
	}*/

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(CommonAnnotationBeanPostProcessor.class);
		context.registerBean(Dao.class);
		context.registerBean(JakartaResourceService.class);
		// context.registerBean(JavaxService.class);
		// context.registerBean(EJBService.class);
		context.refresh();

		// Test
		System.out.println("****************      	Print Services      	*****************************");
		JakartaResourceService service1 = context.getBean(JakartaResourceService.class);
		// JavaxResourceService service2 	= context.getBean(JavaxResourceService.class);
		// EJBService service3 	= context.getBean(EJBService.class);
		System.out.println("JakartaResourceService.dao=" 		+ service1.dao);
		// System.out.println("JavaxResourceService.dao=" 		+ service2.dao);
		// System.out.println("EJBService.dao=" 		+ service3.dao);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
