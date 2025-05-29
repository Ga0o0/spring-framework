package org.springframework.sample.context;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

class ApplicationContextTests {

	@Test
	void testEnvironment() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
		context.setConfigLocation("applicationContext.xml");

		ConfigurableEnvironment environment = context.getEnvironment();
		environment.setRequiredProperties("config.load");
		environment.validateRequiredProperties();

		context.refresh();
	}


}
