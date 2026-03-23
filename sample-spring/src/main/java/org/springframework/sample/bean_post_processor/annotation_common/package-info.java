/**
 * CommonAnnotationBeanPostProcessor
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor
 */
package org.springframework.sample.bean_post_processor.annotation_common;

/**
 * 检查被 init 和 destroy 候选注解标注的类，并收集被候选 Resource 注解标记的字段和方法，封装成 InjectionMetadata 实例后再进行检查
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 *
 * ## 1. 检查所有被候选 init 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class)
 *
 * ### 1.1. 先从缓存中获取当前 beanClass 的 LifecycleMetadata 实例， 如果没有，则收集被候选 init 和 destroy 注解标注的方法来创建一个 LifecycleMetadata 实例返回
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#buildLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#LifecycleMetadata(java.lang.Class, java.util.Collection, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#beanClass
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 *
 * ### 1.2. 检查 initMethods 和 destroyMethods，并将其分别赋值给 checkedInitMethods 和 checkedDestroyMethods
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkInitDestroyMethods(org.springframework.beans.factory.support.RootBeanDefinition)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkedInitMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#checkedDestroyMethods
 *
 * ## 2. 收集被候选 Resource 注解（@EJB（jakarta.ejb.EJB）, @Resource（jakarta.annotation.Resource）, @Resource（javax.annotation.Resource））标记的字段和方法，并封装成 InjectionMetadata 实例返回
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#findResourceMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#buildResourceMetadata(Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#forElements(java.util.Collection, java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.EjbRefElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LegacyResourceElement
 *
 * ## 3. 检查 InjectionMetadata#injectedElements 元素，并将检查后的元素其赋值给 InjectionMetadata#checkedElements
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkConfigMembers(org.springframework.beans.factory.support.RootBeanDefinition)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkedElements
 */

/**
 * 向被候选 Resource 注解标记的字段和方法注入数据
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#postProcessProperties(org.springframework.beans.PropertyValues, java.lang.Object, java.lang.String)
 *
 * ## 1. 收集被候选 Resource 注解（@EJB（jakarta.ejb.EJB）, @Resource（jakarta.annotation.Resource）, @Resource（javax.annotation.Resource））标记的字段和方法，并封装成 InjectionMetadata 实例返回
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#findResourceMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor#buildResourceMetadata(Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#forElements(java.util.Collection, java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.EjbRefElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LegacyResourceElement
 *
 * ## 2. 向被候选 Resource 注解标记的字段和方法注入数据
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement#getResourceToInject(java.lang.Object, java.lang.String)
 *
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.EjbRefElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.ResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 * @see org.springframework.context.annotation.CommonAnnotationBeanPostProcessor.LegacyResourceElement#getResourceToInject(java.lang.Object, java.lang.String)
 */

