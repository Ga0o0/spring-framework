package org.springframework.tx;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class DeclarativeAnnoByTxAnnotationDrivenTests extends AbstractTests {

	/**
	 * DeclarativeAnnoByTxAnnotationDrivenTests-context.xml
	 */
	@Test
	public void test() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
		OuterService service = context.getBean(OuterService.class);
		service.doSomething();
	}

	public static class OuterService {
		private InnerService innerService;
		public void setInnerService(InnerService innerService) {
			this.innerService = innerService;
		}

		@Transactional
		public void doSomething() {
			System.out.println("OuterService#doSomething()");
			innerService.doSomething();
		}
	}

	public static class InnerService {
		@Transactional(propagation = Propagation.REQUIRED)
		public void doSomething() {
			System.out.println("InnerService#doSomething()");
		}
	}

}
