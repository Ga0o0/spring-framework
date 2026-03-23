package org.springframework.sample.bean_post_processor.annotation_autowired.value;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

/*
AutowireCandidateResolver 												[interface]
	\-- impl---- SimpleAutowireCandidateResolver 						[class]
		\--extends-- GenericTypeAwareAutowireCandidateResolver 			[class]
			\--extends-- QualifierAnnotationAutowireCandidateResolver 	[class]		@Qualifier/Value
				\--extends-- ContextAnnotationAutowireCandidateResolver [class]		@Lazy
**/

/**
 * @Value
 *
 * @see org.springframework.beans.factory.annotation.Value
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency(org.springframework.beans.factory.config.DependencyDescriptor, String, java.util.Set, org.springframework.beans.TypeConverter)
 *
 * ## Get AutowireCandidateResolver
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#getAutowireCandidateResolver()
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver#getSuggestedValue(org.springframework.beans.factory.config.DependencyDescriptor)
 *
 * ## value != null && value instanceof String == true -> Get AutowireCandidateResolver
 *
 * @see org.springframework.beans.factory.support.AbstractBeanFactory#resolveEmbeddedValue(java.lang.String)
 *
 * ## AutowireCandidateResolver
 *
 * @see org.springframework.beans.factory.support.AutowireCandidateResolver
 * @see org.springframework.beans.factory.support.SimpleAutowireCandidateResolver
 * @see org.springframework.beans.factory.support.GenericTypeAwareAutowireCandidateResolver
 * @see org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver
 * @see org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver
 */
public class AV02_AtValue_InjectValueFromEnv {

	public static class Props {
		@Value("${user.home}") // 属性
		private String userHome;
	}

	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		// 设置 QualifierAnnotationAutowireCandidateResolver 来支持 @Value 表达式的解析
		// 否则会报错：Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'java.lang.String' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Value("${user.home}")}
		//
		// ********************* 注意 - 注意 - 注意 - 注意 *********************
		// ********************* 注意 - 注意 - 注意 - 注意 *********************
		// ********************* 注意 - 注意 - 注意 - 注意 *********************
		// 设置 AutowireCandidateResolver 后，不再支持 AV01_AtValue_Default 中的功能
		if (beanFactory instanceof DefaultListableBeanFactory factory) {
			QualifierAnnotationAutowireCandidateResolver resolver = new QualifierAnnotationAutowireCandidateResolver(); // 支持 @Value 表达式的解析
			factory.setAutowireCandidateResolver(resolver);
		}
		context.registerBean(AutowiredAnnotationBeanPostProcessor.class);
		context.registerBean(Props.class);
		context.refresh();

		// Test
		System.out.println("user.home=" + context.getEnvironment().getProperty("user.home"));

		System.out.println("****************      	Print Props      	*****************************");
		Props props = context.getBean(Props.class);
		System.out.println("Props.userHome=\t\t\t\t\t" 			+ props.userHome);

		System.out.println("**************** Close GenericApplicationContext *****************************");
		context.close();
	}

}
