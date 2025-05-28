package org.springframework.sample.bean_post_processor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.sample.bean_post_processor.beans.AutowiredService;
import org.springframework.sample.bean_post_processor.beans.Dao;

/**
 * AutowiredAnnotationBeanPostProcessor
 *
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.beans.factory.annotation.Value
 * @see jakarta.inject.Inject
 * @see javax.inject.Inject
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * @see org.springframework.context.annotation.AnnotatedBeanDefinitionReader
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper)
 */
class AutowiredAnnotationBeanPostProcessorTests {

    /*
    public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
		MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, BeanFactoryAware { //... }
    */

	@Test
    public void test() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
		beanPostProcessor.setBeanFactory(factory);
        // add BeanPostProcessor
        factory.addBeanPostProcessor(beanPostProcessor);

        // register bean
        AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(factory);
        beanDefinitionReader.registerBean(Dao.class);
        beanDefinitionReader.registerBean(AutowiredService.class);

        // get bean
        // System.out.println(factory.getBean(Dao.class));
		for (String beanName : factory.getBeanNamesForType(Dao.class)) {
			System.out.println(beanName + ":" + factory.getBean(beanName));
		}
        AutowiredService autowiredService = factory.getBean(AutowiredService.class);
        System.out.println(autowiredService.getDao());

    }

}
