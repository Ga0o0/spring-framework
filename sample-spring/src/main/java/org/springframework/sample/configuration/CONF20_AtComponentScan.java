package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.sample.configuration.components.SimpleComponent;
import org.springframework.sample.configuration.components.SimpleService;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ComponentScan}
 *
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ComponentScanAnnotationParser#parse(org.springframework.core.annotation.AnnotationAttributes, String)
 */
public class CONF20_AtComponentScan {
	public static void main(String[] args) {
		// 1. @ComponentScan 声明方式
		// testDefinitionTypes();

		// 2. @Configuration + @ComponentScan 默认配置
		// testDefault();

		// 3. 测试 @ComponentScan 的相关属性
		testSomeFieldOfAtComponentScan();
	}

	/**
	 * {@code @ComponentScan} 声明方式
	 * <ul>
	 *     <li>直接注解；参考：{@link AppConfig1}</li>
	 *     <li>间接注解一：注解层次结构中的某个位置已用作元注解的注解；参考：{@link AppConfig2}</li>
	 *     <li>间接注解二：通过 @Inherited 从父类继承而来；参考：{@link AppConfig3}</li>
	 * </ul>
	 */
	private static void testDefinitionTypes() {

		// 直接注解
		@ComponentScan(basePackages = "org.springframework.sample.configuration.components")
		@Configuration
		class AppConfig1 {
		}

		// 间接注解一：注解层次结构中的某个位置已用作元注解的注解
		@EnableComponentScan
		@Configuration
		class AppConfig2 {
		}

		// 间接注解二：通过 @Inherited 从父类继承而来
		@Configuration
		class AppConfig3 extends AppConfig2 {
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig1.class);
		// context.registerBean(AppConfig2.class);
		// context.registerBean(AppConfig3.class);
		context.refresh();

		SimpleService service = context.getBean(SimpleService.class);
		service.sayHello();

		SimpleComponent component = context.getBean(SimpleComponent.class);
		component.sayHello();
	}

	/**
	 * {@code @ComponentScan} 默认配置
	 */
	private static void testDefault() {
		@ComponentScan
		@Configuration
		class AppConfig {
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(AppConfig.class);
		context.refresh();

		SimpleService service = context.getBean(SimpleService.class);
		service.sayHello();

		SimpleComponent component = context.getBean(SimpleComponent.class);
		component.sayHello();
	}

	/**
	 * 测试 @ComponentScan 的相关属性
	 */
	private static void testSomeFieldOfAtComponentScan() {

		// ~~~~~~~~~~~~~~~  1. ComponentScan#basePackages / ComponentScan#basePackageClasses ~~~~~~~~~~~~~~~
		@ComponentScan(basePackages = "org.springframework.sample.configuration.components")
		// 等同于
		@ComponentScan(basePackageClasses = SimpleComponent.class)
		@Configuration
		class AppConfigForBasePackages {}

		// ~~~~~~~~~~~~~~~  2. ComponentScan#nameGenerator ~~~~~~~~~~~~~~~
		@ComponentScan(basePackageClasses = SimpleComponent.class, nameGenerator = AnnotationBeanNameGenerator.class)
		@Configuration
		class AppConfigForNameGenerator {}

		// 排除 SimpleComponent 类
		class TypeExcludeFilter implements TypeFilter {
			@Override
			public boolean match(@NonNull MetadataReader metadataReader,
								 @NonNull MetadataReaderFactory metadataReaderFactory) {
				String className = metadataReader.getClassMetadata().getClassName();
				return SimpleComponent.class.getName().equals(className);
			}
		}

		// ~~~~~~~~~~~~~~~ 3. Filter ~~~~~~~~~~~~~~~
		@ComponentScan(basePackageClasses = SimpleComponent.class, excludeFilters = {
			@ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class)
		})
		@Configuration
		class AppConfigForFilters {}


		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		// context.registerBean(AppConfigForBasePackages.class);
		// context.registerBean(AppConfigForNameGenerator.class);
		context.registerBean(AppConfigForFilters.class);
		context.refresh();

		SimpleService service = context.getBean(SimpleService.class);
		service.sayHello();

		SimpleComponent component = context.getBean(SimpleComponent.class);
		component.sayHello();
	}

	/**
	 * 自定义注解
	 */
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@ComponentScan(basePackages = "org.springframework.sample.configuration.components")
	@Inherited
	@interface EnableComponentScan {

	}

}
