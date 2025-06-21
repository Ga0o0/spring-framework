package org.springframework.sample.configuration;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.sample.configuration.components.SimpleComponent;

/**
 * {@code @ComponentScans}
 *
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.ComponentScans
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ComponentScanAnnotationParser#parse(org.springframework.core.annotation.AnnotationAttributes, String)
 */
public class CONF25_AtComponentScans {

	public static void main(String[] args) {

		// ~~ 示例 1 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@ComponentScans({
			@ComponentScan(basePackageClasses = CONF25_AtComponentScans.class),
			@ComponentScan(basePackageClasses = SimpleComponent.class),
		})
		// 等同于
		/*@ComponentScan(basePackageClasses = SimpleComponent.class)
		@ComponentScan(basePackageClasses = SimpleComponent.class)*/
		@Configuration
		class AppConfig1 {
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class); // 支持 @Inject/@Autowired/@Value
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig1.class);
		context.refresh();

		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName + ": " + context.getBeanDefinition(beanDefinitionName));
		}
	}


}
