package org.springframework.tx;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Consumer;

public class ProgrammaticWithTransactionTemplateTests {

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
	public static class AppConfig {
		@Bean
		public PlatformTransactionManager transactionManager() {
			return new SimpleTransactionManager();
		}

		@Bean
		public TransactionTemplate transactionTemplate(PlatformTransactionManager ptm) {
			return new TransactionTemplate(ptm);
		}

		@Bean
		public InnerService innerService(TransactionTemplate template) {
			return new InnerService(template);
		}

		@Bean
		public OuterService outerService(TransactionTemplate template, InnerService innerService) {
			return new OuterService(template, innerService);
		}
	}

	public static class OuterService {
		private final TransactionTemplate template;
		private final InnerService innerService;
		public OuterService(TransactionTemplate template, InnerService innerService) {
			this.innerService = innerService;
			this.template = template;
		}
		public void doSomething() {
			template.executeWithoutResult(status -> {
				System.out.println("OuterService#doSomething()");
				innerService.doSomething();
			});
		}
	}

	public static class InnerService {
		private final TransactionTemplate template;
		public InnerService(TransactionTemplate template) {
			this.template = template;
		}
		public void doSomething() {
			template.executeWithoutResult(status -> {
				System.out.println("OuterService#doSomething()");
			});
		}
	}

}
