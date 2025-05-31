package org.springframework.sample.component;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

public class ClassPathBeanDefinitionScannerTests {

	public static final String BASE_PACKAGE = "org.springframework.sample.component.beans";

	@Test
	public void test1() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);

		scanner.scan(BASE_PACKAGE);

		String[] beanDefinitionNames = registry.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
			System.out.println(beanDefinition);
		}
	}

	@Test
	public void test2() {
		SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);

		Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(BASE_PACKAGE);
		for (BeanDefinition candidate : candidateComponents) {
			System.out.println(candidate);
		}
	}

	@Test
	public void test3() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan(BASE_PACKAGE);
		context.refresh();

		String[] beanDefinitionNames = context.getBeanDefinitionNames();
		for (String beanDefinitionName : beanDefinitionNames) {
			BeanDefinition beanDefinition = context.getBeanDefinition(beanDefinitionName);
			System.out.println(beanDefinition);
		}
	}


}
