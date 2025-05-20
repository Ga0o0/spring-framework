package org.springframework.sample.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

//@Component
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("environment=" + beanFactory.getBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME));
//		Object environment = beanFactory.getBean("environment");
//		if(environment instanceof ConfigurableEnvironment env) {
//			if (!env.containsProperty("bu")) {
//				throw new RuntimeException();
//			}
//		}

		// beanFactory instanceof DefaultListableBeanFactory
		if (beanFactory instanceof AbstractApplicationContext context) {
			ConfigurableEnvironment environment = context.getEnvironment();
			System.out.println("environment22=" + environment);
		}

	}
}
