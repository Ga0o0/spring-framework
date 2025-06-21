package org.springframework.sample.configuration;

import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;

/**
 * {@code @PropertySource}
 *
 * @see jakarta.inject.Inject
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.PropertySource
 * @see org.springframework.context.annotation.PropertySources
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.PropertySourceRegistry#processPropertySource(AnnotationAttributes)
 * @see org.springframework.core.io.support.PropertySourceProcessor#processPropertySource(org.springframework.core.io.support.PropertySourceDescriptor)
 */
public class CONF15_AtPropertySources {

	public static void main(String[] args) {

		class User {
			private final String name;

			public User(String name) {
				this.name = name;
			}

			@Override
			public String toString() {
				return "User{" +
						"name='" + name + '\'' +
						'}';
			}
		}

		// ~~ 示例 1 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@PropertySources({
				// 后面的 @PropertySource 会覆盖前面的 @PropertySource
			@PropertySource("property-source.yml"),
			@PropertySource(value = { "property-source.properties" }),
		})
		// 等同于
		/*@PropertySource("property-source.yml")
		@PropertySource(value = { "property-source.properties" })*/
		@Configuration
		class AppConfig {

			@Inject
			private Environment env;

			@Bean
			public User getUser() {
				return new User(env.getProperty("name"));
			}
		}

		// ~~ 示例 2 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@PropertySources({
			// 后面的 @PropertySource 会覆盖前面的 @PropertySource
			@PropertySource("property-source.yml"),
			@PropertySource(value = { "property-source.properties" }),
		})
		@PropertySource("property-source2.yml") // 该代码会显示 property-source.properties 的内容；原因：@PropertySource 被处理时会忽略重复的文件
		@Configuration
		class AppConfig1 {

			@Inject
			private Environment env;

			@Bean
			public User getUser() {
				return new User(env.getProperty("name"));
			}
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class); // 支持 @Inject/@Autowired/@Value
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		// context.registerBean(AppConfig1.class);
		context.registerBean(AppConfig.class);
		context.refresh();

		User bean = context.getBean(User.class);
		System.out.println(bean);

	}
}
