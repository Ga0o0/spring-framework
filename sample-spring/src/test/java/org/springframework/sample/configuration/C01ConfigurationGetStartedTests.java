package org.springframework.sample.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

class C01ConfigurationGetStartedTests {

	@Test
	public void testConfiguration() {
		final GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AppConfig.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}


	@Configuration
	public static class AppConfig {
		@Bean
		public MyBean myBean() {
			return new MyBean();
		}
	}

	public static class MyBean {}

}
