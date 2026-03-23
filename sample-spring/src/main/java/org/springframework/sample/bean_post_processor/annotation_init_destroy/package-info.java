/**
 * InitDestroyAnnotationBeanPostProcessor
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor
 */
package org.springframework.sample.bean_post_processor.annotation_init_destroy;

/**
 * 执行所有被候选 init 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
 *
 * ## 1. 先从缓存中获取当前 beanClass 的 LifecycleMetadata 实例， 如果没有，则收集被候选 init 和 destroy 注解标注的方法来创建一个 LifecycleMetadata 实例返回
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#buildLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#LifecycleMetadata(java.lang.Class, java.util.Collection, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#beanClass
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 *
 * ## 2. 执行所有被候选 init 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#invokeInitMethods(java.lang.Object, java.lang.String)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMethod#invoke(java.lang.Object)
 * @see java.lang.reflect.Method#invoke(Object, Object...)
 */

/**
 * 执行所有被候选 destroy 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
 *
 * ## 1. 先从缓存中获取当前 beanClass 的 LifecycleMetadata 实例， 如果没有，则收集被候选 init 和 destroy 注解标注的方法来创建一个 LifecycleMetadata 实例返回
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#findLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#buildLifecycleMetadata(java.lang.Class)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#LifecycleMetadata(java.lang.Class, java.util.Collection, java.util.Collection)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#beanClass
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#initMethods
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#destroyMethods
 *
 * ## 2. 执行所有被候选 destroy 注解标记的方法
 *
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMetadata#invokeDestroyMethods(java.lang.Object, java.lang.String)
 * @see org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.LifecycleMethod#invoke(java.lang.Object)
 * @see java.lang.reflect.Method#invoke(Object, Object...)
 */