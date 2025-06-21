package org.springframework.sample.configuration;

import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.GenericApplicationContext;

// @ImportResource 指示一个或多个包含要导入的 bean 定义的资源。
//
// <p>与 {@link Import @Import} 类似，此注解提供的功能类似于 Spring XML 中的 {@code <import/>} 元素。
// 它通常用于设计由 {@link AnnotationConfigApplicationContext} 引导的 {@link Configuration @Configuration} 类，但仍需要某些 XML 功能（例如命名空间）。
/**
 * {@code @ImportResource} + {@code @Configuration}
 *
 * @see org.springframework.context.annotation.ImportResource
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass(org.springframework.context.annotation.ConfigurationClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsFromImportedResources(java.util.Map)
 * @see org.springframework.beans.factory.support.BeanDefinitionReader#loadBeanDefinitions(String)
 */
public class CONF40_AtImportResource {

	public static void main(String[] args) {

		// ~~~~~~~~~~~~~~~ @Configuration + @ImportResource ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// @ImportResource("CONF40_AtImportResource-context.xml")
		// @ImportResource("classpath:CONF40_AtImportResource-context.xml")
		// @ImportResource("CONF40_AtImportResource-context.groovy")
		// @ImportResource("classpath:CONF40_AtImportResource-context.groovy")
		@ImportResource(locations = "classpath:CONF40_AtImportResource-context.groovy", reader = GroovyBeanDefinitionReader.class)
		@Configuration
		class AppConfig {
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig.class);
		context.refresh();

		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName + ": " + context.getBeanDefinition(beanDefinitionName));
		}

	}

}
