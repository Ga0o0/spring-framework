package org.springframework.sample.configuration;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Arrays;

/**
 * {@code @Bean} + {@code @Configuration}
 *
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass(org.springframework.context.annotation.ConfigurationClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#registerBeanDefinitionForImportedConfigurationClass(org.springframework.context.annotation.ConfigurationClass)
 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod(org.springframework.context.annotation.BeanMethod)
 */
public class CONF50_AtBean {


	public static void main(String[] args) {
		// 1. @Configuration + @Import + @Lazy/@Primary/@DependsOn/@Role/@Description
		// testAtImportWithCommonDefinitionAnnotations();

		// 2. @Configuration + @Bean 的属性
		// testAtBeanWithFields();

		// 3. @Configuration + @Bean + @Lazy/@Primary/@DependsOn/@Role/@Description
		testAtBeanWithCommonDefinitionAnnotations();
	}

	/**
	 * {@code @Configuration} + {@code @Import} + {@code @Lazy/@Primary/@DependsOn/@Role/@Description}
	 *
	 * @see org.springframework.context.annotation.Bean
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.context.annotation.Import
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#registerBeanDefinitionForImportedConfigurationClass(org.springframework.context.annotation.ConfigurationClass)
	 * @see org.springframework.context.annotation.AnnotationConfigUtils#processCommonDefinitionAnnotations(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition, org.springframework.core.type.AnnotatedTypeMetadata)
	 */
	private static void testAtImportWithCommonDefinitionAnnotations() {

		@Lazy        // 指示 Bean 是否延迟初始化。
		@Primary    // 表示当多个候选 Bean 有资格自动装配单值依赖项时，应优先考虑某个 Bean。
		@DependsOn    // 当前 Bean 所依赖的 Bean。任何指定的 Bean 都保证先于此 Bean 由容器创建。
		@Role(value = BeanDefinition.ROLE_APPLICATION)    // 指示给定 bean 的 “角色” 提示。
		@Description(value = "This is a Service")        // 向从 @Component 或 @Bean 派生的 bean 定义添加文本描述。
		class Service {
		}

		@Lazy        // 指示 Bean 是否延迟初始化。
		@Primary    // 表示当多个候选 Bean 有资格自动装配单值依赖项时，应优先考虑某个 Bean。
		@DependsOn    // 当前 Bean 所依赖的 Bean。任何指定的 Bean 都保证先于此 Bean 由容器创建。
		@Role(value = BeanDefinition.ROLE_APPLICATION)    // 指示给定 bean 的 “角色” 提示。
		@Description(value = "This is a Service")        // 向从 @Component 或 @Bean 派生的 bean 定义添加文本描述。
		@Configuration
		class Config {
			@Bean
			public Service service3() {
				return new Service();
			}
		}

		// @Import(Service.class)
		@Import(Config.class)
		@Configuration
		class AppConfig {
			@Bean
			public Service service1() {
				return new Service();
			}

			@Bean
			public Service service2() {
				return new Service();
			}
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig.class);
		context.refresh();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ context.getBeanDefinitionNames() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName + ": " + context.getBeanDefinition(beanDefinitionName));
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ beanFactory.getSingletonNames() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		for (String singletonName : beanFactory.getSingletonNames()) {
			System.out.println(singletonName + ": " + beanFactory.getBean(singletonName));
		}
	}


	/**
	 * {@code @Configuration} + {@code @Bean} 的属性
	 *
	 * @see org.springframework.context.annotation.Bean
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod(org.springframework.context.annotation.BeanMethod)
	 */
	private static void testAtBeanWithFields() {

		class Service {
			public void init() {
				System.out.println("init");
			}

			public void destroy() {
				System.out.println("destroy");
			}
		}

		@Configuration
		class AppConfig {
			// name：此 bean 的名称，如果有多个名称，则为主 bean 名称加上别名。 默认第一个参数为 Bean Name；其他为 Bean Alias
			// autowireCandidate: 此 Bean 是否可以自动装配到其他 Bean 中？
			// initMethod：初始化期间在 Bean 实例上调用的方法的可选名称。
			// destroyMethod：关闭应用上下文时，在 Bean 实例上调用的可选方法名称。
			@Bean(name = {"service", "service1", "service2"}, autowireCandidate = true, initMethod = "init", destroyMethod = "destroy")
			@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON) // 当与 @Component 结合使用类型级别注解时，@Scope 表示用于所注解类型实例的作用域名称。
			public Service service() {
				return new Service();
			}
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig.class);
		context.refresh();

		System.out.println(context.getBean(Service.class));

		String[] services = context.getAliases("service");
		System.out.println(Arrays.toString(services));
		context.close();
	}

	/**
	 * {@code @Configuration} + {@code @Bean} + {@code @Lazy/@Primary/@DependsOn/@Role/@Description}
	 *
	 * @see org.springframework.context.annotation.Bean
	 * @see org.springframework.context.annotation.Configuration
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader.TrackedConditionEvaluator)
	 * @see org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod(org.springframework.context.annotation.BeanMethod)
	 * @see org.springframework.context.annotation.AnnotationConfigUtils#processCommonDefinitionAnnotations(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition, org.springframework.core.type.AnnotatedTypeMetadata)
	 */
	private static void testAtBeanWithCommonDefinitionAnnotations() {
		class Service {}

		@Configuration
		class AppConfig {
			@Lazy        	// 指示 Bean 是否延迟初始化。
			@Primary    	// 表示当多个候选 Bean 有资格自动装配单值依赖项时，应优先考虑某个 Bean。
			@DependsOn    	// 当前 Bean 所依赖的 Bean。任何指定的 Bean 都保证先于此 Bean 由容器创建。
			@Role(value = BeanDefinition.ROLE_APPLICATION)    // 指示给定 bean 的 “角色” 提示。
			@Description(value = "This is a Service")        // 向从 @Component 或 @Bean 派生的 bean 定义添加文本描述。
			@Bean
			public Service service() {
				return new Service();
			}
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig.class);
		context.refresh();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ context.getBeanDefinitionNames() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName + ": " + context.getBeanDefinition(beanDefinitionName));
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~ beanFactory.getSingletonNames() ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		for (String singletonName : beanFactory.getSingletonNames()) {
			System.out.println(singletonName + ": " + beanFactory.getBean(singletonName));
		}
	}


}
