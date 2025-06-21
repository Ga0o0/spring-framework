package org.springframework.sample.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Quick Started
 *
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#registerBeanDefinitionForImportedConfigurationClass(org.springframework.context.annotation.ConfigurationClass)
 */
public class CONF01_QuickStart {

	public static void main(String[] args) {

		@Configuration
		class AppConfig {

		}

		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);
		context.registerBean(AppConfig.class);
		context.refresh();

		AppConfig config = context.getBean(AppConfig.class);
		System.out.println(config);
	}

}
