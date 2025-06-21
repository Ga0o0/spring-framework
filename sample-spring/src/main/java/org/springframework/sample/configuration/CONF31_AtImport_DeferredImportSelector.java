package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * DeferredImportSelector（延迟导入选择器）【延迟、分组】
 * <p>
 * {@link org.springframework.context.annotation.ImportSelector} 的变体，在所有 {@code @Configuration} bean 处理完毕后运行。当所选导入为 {@code @Conditional} 时，此类选择器尤其有用。
 *
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ImportSelector
 * @see org.springframework.context.annotation.DeferredImportSelector
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConfigurationClassParser#getImports(org.springframework.context.annotation.ConfigurationClassParser.SourceClass)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processImports(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.Collection, java.util.function.Predicate, boolean)
 * @see org.springframework.context.annotation.ConfigurationClassParser#parse(java.util.Set)
 * @see org.springframework.context.annotation.ConfigurationClassParser.DeferredImportSelectorHandler#process()
 * @see org.springframework.context.annotation.ConfigurationClassParser.DeferredImportSelectorGroupingHandler#register(org.springframework.context.annotation.ConfigurationClassParser.DeferredImportSelectorHolder)
 * @see org.springframework.context.annotation.ConfigurationClassParser.DeferredImportSelectorGroupingHandler#processGroupImports()
 */
public class CONF31_AtImport_DeferredImportSelector {

	public static void main(String[] args) {
		class One {}
		class Two {}
		class Three {}

		// ~~~~~~~~~~~~~~~ DeferredImportSelector ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		class MyDeferredImportSelector implements DeferredImportSelector {
			private final List<String> classNameList = List.of(One.class.getName());

			// 未被调用
			@Override
			public String @NonNull [] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
				System.out.println("invoke MyDeferredImportSelector#selectImports()");
				return new String[]{One.class.getName(), Two.class.getName(), Three.class.getName()};
			}

			@Override
			public Predicate<String> getExclusionFilter() {
				System.out.println("invoke MyDeferredImportSelector#getExclusionFilter()");
				return classNameList::contains;
			}

			@Override
			public Class<? extends DeferredImportSelector.Group> getImportGroup() {
				System.out.println("invoke MyDeferredImportSelector#getImportGroup()");
				return MyGroup.class;
			}

			// CUSTOM Group
			private static class MyGroup implements DeferredImportSelector.Group {
				private final Map<String, AnnotationMetadata> entries = new LinkedHashMap<>();
				@Override
				public void process(@NonNull AnnotationMetadata metadata, @NonNull DeferredImportSelector selector) {
					System.out.println("invoke MyGroup#process()");
					entries.put(One.class.getName(), metadata);
					entries.put(Two.class.getName(), metadata);
					entries.put(Three.class.getName(), metadata);
				}
				@Override
				public @NonNull Iterable<Entry> selectImports() {
					System.out.println("invoke MyGroup#selectImports()");
					return entries.entrySet().stream().map( entry ->
							new Entry(entry.getValue(), entry.getKey())).toList();
				}
			}
		}


		// ~~~~~~~~~~~~~~~ @Configuration + @Import ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		@Import(MyDeferredImportSelector.class)
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
