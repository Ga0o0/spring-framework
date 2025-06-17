package org.springframework.tx;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DeclarativeXmlByTxAdviceTests extends AbstractTests {

	/**
	 * DeclarativeXmlByTxAdviceTests-context.xml
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
		public void doSomething() {
			System.out.println("OuterService#doSomething()");
			innerService.doSomething();
		}
	}

	public static class InnerService {
		public void doSomething() {
			System.out.println("InnerService#doSomething()");
		}
	}

}
