package org.springframework.sample.configuration;

import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PropertySource;
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
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.PropertySourceRegistry#processPropertySource(AnnotationAttributes)
 * @see org.springframework.core.io.support.PropertySourceProcessor#processPropertySource(org.springframework.core.io.support.PropertySourceDescriptor)
 */
public class CONF10_AtPropertySource {

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

		// 1. 测试 @PropertySource(value="") 多种格式的 value
		// @PropertySource(value = "property-source.properties")
		// @PropertySource(value = { "classpath:property-source.properties" })
		// @PropertySource(value = { "classpath:/property-source.properties" })
		// @PropertySource(value = { "file:E:/workspace/idea-open-framework/spring-framework-github/sample-spring/src/main/resources/property-source.properties" })
		@PropertySource(value = {"classpath*:*.properties"}) // 从 Spring Framework 6.1 开始，还支持资源位置通配符
		// 2. 测试 @PropertySource 的一些属性
		@PropertySource(value = {"property-source.yml"}, name = "my-property-source")
		// @PropertySource(value = { "property-source222.yml" }, ignoreResourceNotFound = true)
		// @PropertySource(value = { "property-source.yml" }, encoding = "UTF-8")
		// @PropertySource(value = {"property-source.yml"}, factory = DefaultPropertySourceFactory.class)
		@Configuration
		class AppConfig {

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
		context.registerBean(AppConfig.class);
		context.refresh();

		User bean = context.getBean(User.class);
		System.out.println(bean);
	}

}
