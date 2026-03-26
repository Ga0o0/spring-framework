/**
 * 用 Java 配置声明一个 Aspect
 *
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 */
package org.springframework.sample.aop.code_analysis.aspectj_based;

/**
 * AspectJ 的启用 -> 注解 @EnableAspectJAutoProxy
 *
 * @see org.springframework.context.annotation.EnableAspectJAutoProxy
 * @see org.springframework.context.annotation.AspectJAutoProxyRegistrar
 *
 * @see org.springframework.context.annotation.AspectJAutoProxyRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 *
 * ## 1. 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AnnotationAwareAspectJAutoProxyCreator
 *
 * @see org.springframework.aop.config.AopConfigUtils#registerOrEscalateApcAsRequired(java.lang.Class, org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 *
 * ### 1.1. 进行升级处理时，优先级的获取
 *
 * @see org.springframework.aop.config.AopConfigUtils#findPriorityForClass(java.lang.String)
 *
 * @see org.springframework.aop.config.AopConfigUtils#APC_PRIORITY_LIST
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 *
 * ## 2. AnnotationAwareAspectJAutoProxyCreator
 *
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 */