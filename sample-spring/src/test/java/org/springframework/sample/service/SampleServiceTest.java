package org.springframework.sample.service;

import org.junit.jupiter.api.Test;
import org.springframework.sample.listener.MyService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class SampleServiceTest {

	@Test
	public void test() {
		ClassPathXmlApplicationContext ac =
				new ClassPathXmlApplicationContext("${spring.config:spring}.xml");

		SampleService bean = ac.getBean(SampleService.class);
		assertNotNull(bean);
		bean.sayHello();

		AppConfig.M m = ac.getBean(AppConfig.M.class);
		assertNotNull(m);
		m.m();

		MyService myService = ac.getBean(MyService.class);
		assertNotNull(myService);
		myService.update();

	}


}