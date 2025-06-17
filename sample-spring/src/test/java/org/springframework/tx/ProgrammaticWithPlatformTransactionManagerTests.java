package org.springframework.tx;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Consumer;

public class ProgrammaticWithPlatformTransactionManagerTests {

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
		public InnerService innerService(PlatformTransactionManager ptm) {
			return new InnerService(ptm);
		}

		@Bean
		public OuterService outerService(PlatformTransactionManager ptm, InnerService innerService) {
			return new OuterService(ptm, innerService);
		}
	}

	public static class OuterService {
		private final PlatformTransactionManager ptm;
		private final InnerService innerService;
		public OuterService(PlatformTransactionManager ptm, InnerService innerService) {
			this.innerService = innerService;
			this.ptm = ptm;
		}
		public void doSomething() {
			invoke(ptm, TransactionDefinition.PROPAGATION_REQUIRED, nul -> {
				System.out.println("OuterService#doSomething()");
				innerService.doSomething();
			});
		}
	}

	public static class InnerService {
		private final PlatformTransactionManager ptm;
		public InnerService(PlatformTransactionManager ptm) {
			this.ptm = ptm;
		}
		public void doSomething() {
			invoke(ptm, TransactionDefinition.PROPAGATION_REQUIRED,nul -> {
				System.out.println("InnerService#doSomething()");
			});
		}
	}

	private static <T> void invoke(PlatformTransactionManager ptm, int propagationBehavior, Consumer<T> consumer) {
		DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setName("OuterService/InnerService - Transaction");
		transactionDefinition.setTimeout(TransactionDefinition.TIMEOUT_DEFAULT);
		transactionDefinition.setPropagationBehavior(propagationBehavior);
		TransactionStatus status = ptm.getTransaction(transactionDefinition);
		try {
			consumer.accept(null);
			ptm.commit(status);
		} catch (Exception e) {
			ptm.rollback(status);
		}
	}

}
