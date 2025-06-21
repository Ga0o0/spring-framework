package org.springframework.sample.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * 在配置类实现的接口上注册默认方法。
 *
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass(org.springframework.context.annotation.ConfigurationClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#retrieveBeanMethodMetadata(org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processInterfaces(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 */
public class CONF60_DefaultMethodsOfInterfaces {

	public static void main(String[] args) {

		class Service {}

		interface Parent {
			@Bean
			default Service service1() {
				return new Service();
			}
		}

		interface Child extends Parent {
			@Bean
			default Service service2() {
				return new Service();
			}
		}

		// 对于在配置类实现的接口上注册默认方法
		// ConfigurationClassParser#processInterfaces() 方法处理之前
		// ConfigurationClassParser#retrieveBeanMethodMetadata() 已经处理过了
		@Configuration
		class AppConfig implements Child {
			@Bean
			Service service3() {
				return new Service();
			}
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
