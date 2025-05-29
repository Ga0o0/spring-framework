package org.springframework.sample.bean_factory_post_processor;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.sample.bean_factory_post_processor.beans.AppConfig;
import org.springframework.sample.bean_factory_post_processor.beans.Service;

/**
 * ConfigurationClassPostProcessor
 *
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
public class ConfigurationClassPostProcessorTests {

	@Test
	public void test() {
		ConfigurationClassPostProcessor configurationClassPostProcessor = new ConfigurationClassPostProcessor();
		GenericApplicationContext context = new GenericApplicationContext();
		// add BeanFactoryPostProcessor
		context.addBeanFactoryPostProcessor(configurationClassPostProcessor);
		// register bean
		context.registerBean(AppConfig.class);
		context.refresh();
		// get bean
		Service bean = context.getBean(Service.class);
		System.out.println(bean);
	}
}
