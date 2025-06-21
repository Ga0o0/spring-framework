package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.function.Predicate;

/**
 * ImportSelector
 *
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportSelector
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#getImports(org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processImports(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.Collection, java.util.function.Predicate, boolean)
 */
public class CONF30_AtImport_ImportSelector {

	public static void main(String[] args) {
		class One {}
		class Two {}
		class Three {}

		// ~~~~~~~~~~~~~~~ ImportSelector ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		class MyImportSelector implements ImportSelector {
			private final List<String> classNameList = List.of(One.class.getName());
			// private final List<String> classNameList = List.of(One.class.getName(), Two.class.getName());

			@Override
			public String @NonNull [] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
				return new String[]{One.class.getName(), Two.class.getName(), Three.class.getName()};
			}

			@Override
			public Predicate<String> getExclusionFilter() {
				// 排除的类会使用 java.lang.Object 对象代替；当存在多个排除的类时，会合并成一个 java.lang.Object Bean
				return classNameList::contains;
			}
		}

		// ~~~~~~~~~~~~~~~ @Configuration + @Import ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@Import(MyImportSelector.class)
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
