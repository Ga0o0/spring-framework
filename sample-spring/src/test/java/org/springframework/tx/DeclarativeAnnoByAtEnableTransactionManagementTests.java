package org.springframework.tx;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class DeclarativeAnnoByAtEnableTransactionManagementTests extends AbstractTests {

	@Test
	public void test() {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);
		context.registerBean(AppConfig.class);
		context.refresh();

		OuterService service = context.getBean(OuterService.class);
		service.doSomething();
	}

	@Configuration
	@EnableTransactionManagement
	static class AppConfig {
		@Bean
		public PlatformTransactionManager transactionManager() {
			return new SimpleTransactionManager();
		}

		@Bean
		public InnerService innerService() {
			return new InnerService();
		}

		@Bean
		public OuterService outerService(InnerService innerService) {
			return new OuterService(innerService);
		}
	}

	public static class OuterService {
		private final InnerService innerService;
		public OuterService(InnerService innerService) {
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
