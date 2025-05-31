package org.springframework.sample.context;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class AnnotationConfigApplicationContextTests {

	@Test
	public void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		Service service = context.getBean(Service.class);
		System.out.println(service);

		Config config = context.getBean(Config.class);
		System.out.println(config);
	}

	@Configuration
	public static class Config {
		@Bean
		public Service service() {
			return new Service();
		}
	}

	public static class Service {}

}
