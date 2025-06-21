package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.sample.configuration.components.SimpleComponent;

import java.util.Objects;


/**
 * {@code @ComponentScan} + {@code @Conditional}
 *
 * @see CONF02_AtConditional_ConditionOnClass
 * @see CONF03_AtConditional_ConfigurationConditionOnClass
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.ComponentScan
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass(org.springframework.context.annotation.ConfigurationClass, org.springframework.context.annotation.ConfigurationClassParser.SourceClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ComponentScanAnnotationParser#parse(org.springframework.core.annotation.AnnotationAttributes, String)
 */
public class CONF21_AtComponentScan_AtConditional {

	public static void main(String[] args) {

		class Tag {}

		// My Condition
		class MyCondition implements Condition {
			@Override
			public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
				return Objects.requireNonNull(context.getBeanFactory()).getBeanNamesForType(Tag.class).length > 0;
			}
		}

		// My ConfigurationCondition
		class MyConfigurationCondition implements ConfigurationCondition {
			@Override
			@NonNull
			public ConfigurationPhase getConfigurationPhase() {
				return ConfigurationPhase.REGISTER_BEAN;
			}
			@Override
			public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
				return Objects.requireNonNull(context.getBeanFactory()).getBeanNamesForType(Tag.class).length > 0;
			}
		}

		// AppConfig
		@ComponentScan(basePackageClasses = SimpleComponent.class)
		// @Conditional(MyCondition.class)
		@Conditional(MyConfigurationCondition.class)
		@Configuration
		class AppConfig {
		}

		// Test
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);    // 支持 @Configuration
		context.registerBean(Tag.class);
		context.registerBean(AppConfig.class);
		context.refresh();

		for (String beanDefinitionName : context.getBeanDefinitionNames()) {
			System.out.println(beanDefinitionName + ": " + context.getBean(beanDefinitionName));
		}


	}


}
