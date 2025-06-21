package org.springframework.sample.configuration;

import lombok.NonNull;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

import java.util.Objects;


/**
 * {@code @Configuration} + {@code @Conditional} + {@link org.springframework.context.annotation.ConfigurationCondition}
 *
 * @see org.springframework.context.annotation.ConfigurationCondition
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass(org.springframework.context.annotation.ConfigurationClass, java.util.function.Predicate)
 * @see org.springframework.context.annotation.ConditionEvaluator#shouldSkip(AnnotatedTypeMetadata, org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase)
 */
public class CONF03_AtConditional_ConfigurationConditionOnClass {

    public static void main(String[] args) {
		class Tag {}

		// Condition
		class OnTagConfigurationCondition implements ConfigurationCondition {
			@Override
			public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
				Assert.notNull(metadata, "AnnotatedTypeMetadata must not be null");
				return Objects.requireNonNull(context.getBeanFactory()).getBeanNamesForType(Tag.class).length > 0;
			}

			@Override
			@NonNull
			public ConfigurationPhase getConfigurationPhase() {
				return ConfigurationPhase.PARSE_CONFIGURATION;
			}
		}

		// @Configuration + @Conditional
		@Configuration
		@Conditional(value = OnTagConfigurationCondition.class)
		class AppConfig {}

        // Test
		GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(Tag.class);
        context.registerBean(AppConfig.class);
		context.registerBean(ConfigurationClassPostProcessor.class);
        context.refresh();

		AppConfig bean = context.getBean(AppConfig.class);
        System.out.println(bean);
    }

}
