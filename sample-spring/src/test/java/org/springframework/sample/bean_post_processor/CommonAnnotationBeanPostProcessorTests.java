package org.springframework.sample.bean_post_processor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.sample.bean_post_processor.beans.Dao;
import org.springframework.sample.bean_post_processor.beans.ResourceService;

/**
 * CommonAnnotationBeanPostProcessor
 *
 * @see jakarta.annotation.Resource
 * @see javax.annotation.Resource
 * @see jakarta.ejb.EJB
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 * @see org.springframework.context.annotation.AnnotatedBeanDefinitionReader
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
class CommonAnnotationBeanPostProcessorTests {

	@Test
    public void test() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        CommonAnnotationBeanPostProcessor beanPostProcessor = new CommonAnnotationBeanPostProcessor();
		beanPostProcessor.setBeanFactory(factory);
        // add BeanPostProcessor
        factory.addBeanPostProcessor(beanPostProcessor);

        // register bean
        AnnotatedBeanDefinitionReader beanDefinitionReader = new AnnotatedBeanDefinitionReader(factory);
        beanDefinitionReader.registerBean(Dao.class, "dao");
        beanDefinitionReader.registerBean(Dao.class, "dao1");
        beanDefinitionReader.registerBean(ResourceService.class);

        // get bean
        // System.out.println(factory.getBean(Dao.class));
		for (String beanName : factory.getBeanNamesForType(Dao.class)) {
			System.out.println(beanName + ":" + factory.getBean(beanName));
		}
        ResourceService resourceService = factory.getBean(ResourceService.class);
        System.out.println(resourceService.getDao());
    }
}
