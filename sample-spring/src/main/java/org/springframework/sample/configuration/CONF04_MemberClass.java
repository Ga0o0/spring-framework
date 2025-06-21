package org.springframework.sample.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.support.GenericApplicationContext;

/**
 * {@code @Configuration} + Class Has Parents
 *
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processMemberClasses(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 */
public class CONF04_MemberClass {

	public static void main(String[] args) {

		class Service{}

		@Configuration
		class AppConfig {

			@Bean
			public Service service() {
				return new Service();
			}

			// ~~~~~~~~~~~~~~~~ 错误示范 ** 错误示范 ** 错误示范 ~~~~~~~~~~~~~~~~
			// 异常描述：No default constructor found
			// 关于嵌套类：@Configuration 注解的文档注释中有描述
			// 原因：不支持实例嵌套类
			/*@Configuration
			class InnerAppConfig1 {
				@Bean
				public Service innerService1() {
					return new Service();
				}
			}*/
			// ~~~~~~~~~~~~~~~~ 错误示范 ** 错误示范 ** 错误示范 ~~~~~~~~~~~~~~~~

			@Configuration
			static class InnerAppConfig2 {
				@Bean
				public Service innerService2() {
					return new Service();
				}
			}

		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AppConfig.class);
		context.registerBean(ConfigurationClassPostProcessor.class);
		context.refresh();

		AppConfig config = context.getBean(AppConfig.class);
		System.out.println(config);

		for (String beanName : context.getBeanNamesForType(Service.class)) {
			System.out.println(beanName + ": " + context.getBean(beanName));
		}


	}

}
