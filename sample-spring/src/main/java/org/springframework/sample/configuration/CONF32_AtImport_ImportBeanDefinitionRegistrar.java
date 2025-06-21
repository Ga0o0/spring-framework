package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ImportBeanDefinitionRegistrar
 *
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 * @see org.springframework.context.annotation.AspectJAutoProxyRegistrar
 * @see org.springframework.context.annotation.AutoProxyRegistrar
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#getImports(org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processImports(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.Collection, java.util.function.Predicate, boolean)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsFromRegistrars(java.util.Map)
 */
public class CONF32_AtImport_ImportBeanDefinitionRegistrar {

	public static void main(String[] args) {

		class One {}

		// ~~~~~~~~~~~~~~~ ImportSelector ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
			public static final String ONE = "one-service";
			@Override
			public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
				AbstractBeanDefinition definition = BeanDefinitionBuilder
						.rootBeanDefinition(One.class)
						.getBeanDefinition();
				registry.registerBeanDefinition(ONE, definition);
			}
		}

		// ~~~~~~~~~~~~~~~ @Configuration + @Import ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@Import(MyImportBeanDefinitionRegistrar.class)
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
