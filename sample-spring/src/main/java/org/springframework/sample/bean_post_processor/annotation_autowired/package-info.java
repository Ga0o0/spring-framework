/**
 * AutowiredAnnotationBeanPostProcessor
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 */
package org.springframework.sample.bean_post_processor.annotation_autowired;

/**
 * 查找被 autowired（@jakarta.inject.Inject、@javax.inject.Injec、t@Autowired、@Value） 注解标记的字段和方法并进行封装和检查
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(org.springframework.beans.factory.support.RootBeanDefinition, java.lang.Class, java.lang.String)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#findInjectionMetadata(java.lang.String, java.lang.Class, org.springframework.beans.factory.support.RootBeanDefinition)
 *
 * ## 1. 查找被 autowired（@jakarta.inject.Inject、@javax.inject.Inject、t@Autowired、@Value） 注解标记的字段和方法，并封装成 AutowiredFieldElement、AutowiredMethodElement 放入 InjectionMetadata 中返回
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#findAutowiringMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#buildAutowiringMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement
 *
 * ## 2. 检查 InjectionMetadata#injectedElements 集合元素，并将检查后元素赋值给 InjectionMetadata#checkedElements
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkConfigMembers(org.springframework.beans.factory.support.RootBeanDefinition)
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#checkedElements
 */

/**
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#postProcessProperties(org.springframework.beans.PropertyValues, java.lang.Object, java.lang.String)
 *
 * ## 1. 查找被 autowired（@jakarta.inject.Inject、@javax.inject.Inject、t@Autowired、@Value） 注解标记的字段和方法，并封装成 AutowiredFieldElement、AutowiredMethodElement 放入 InjectionMetadata 中返回
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#findAutowiringMetadata(java.lang.String, java.lang.Class, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#buildAutowiringMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#InjectionMetadata(java.lang.Class, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#targetClass
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#injectedElements
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement
 *
 * ## 2. 对被 autowired（@jakarta.inject.Inject、@javax.inject.Inject、t@Autowired、@Value） 注解标记的字段和方法进行值注入
 *
 * @see org.springframework.beans.factory.annotation.InjectionMetadata#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 *
 * ### 2.1. 被 autowired 注解标记的字段进行值注入
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#resolveFieldValue(java.lang.reflect.Field, java.lang.Object, java.lang.String)
 * @see java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
 *
 * ### 2.2. 被 autowired 注解标记的方法进行值注入
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(java.lang.Object, java.lang.String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#resolveMethodArguments(java.lang.reflect.Method, java.lang.Object, java.lang.String)
 * @see java.lang.reflect.Method#invoke(java.lang.Object, java.lang.Object...)
 */

