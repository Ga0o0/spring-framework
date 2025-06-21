package org.springframework.sample.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

/**
 * {@code @Import} + {@code @Configuration}
 *
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass(org.springframework.context.annotation.ConfigurationClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#getImports(org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processImports(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.Collection, java.util.function.Predicate, boolean)
 */
public class CONF35_AtImport_AtConfiguration {

	public static void main(String[] args) {

		class Service{}

		// 正常的 Configuration 对象
		@Configuration
		class Config {
			@Bean
			public Service service() {
				return new Service();
			}
		}

		// ~~~~~~~~~~~~~~~ @Configuration + @Import ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@Import(Config.class)
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
