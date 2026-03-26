/**
 * 用 XML 配置声明一个 Aspect （spring-aop/src/main/resources/META-INF/spring.handlers）
 *
 * @see org.springframework.aop.config.AopNamespaceHandler
 */
package org.springframework.sample.aop.code_analysis.schema_based;

/**
 * AopNamespaceHandler 中的 BeanDefinitionParser
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser
 * @see org.springframework.aop.config.AspectJAutoProxyBeanDefinitionParser
 */

/**
 * <aop:config proxy-target-class="" expose-proxy=""/> -> ConfigBeanDefinitionParser#parse(...)
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * ## 1. 注册 AspectJAwareAdvisorAutoProxyCreator 的 BeanDefinition，并获取 <aop:config/> 标签的属性设置给它
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#configureAutoProxyCreator(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#configureAutoProxyCreator(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
 * @see org.springframework.aop.config.AopNamespaceUtils#registerAspectJAutoProxyCreatorIfNecessary(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
 * @see org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator
 *
 * @see org.springframework.aop.config.AopConfigUtils#APC_PRIORITY_LIST
 *
 * ## 2. 获取给定 DOM 元素的所有子元素，并进行解析
 *
 * ### 2.1. 解析 <aop:pointcut/> -> AspectJExpressionPointcut
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parsePointcut(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * ### 2.2. 解析 <aop:advisor/> -> DefaultBeanFactoryPointcutAdvisor
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAdvisor(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * ### 2.3. 解析 <aop:aspect/> 及其子标签 <aop:declare-parents/> 和 <aop:advice/> -> AspectJPointcutAdvisor
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAspect(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * #### 2.3.1. 解析 <aop:advice/> 标签
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAdvice(java.lang.String, int, org.w3c.dom.Element, org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, java.util.List, java.util.List)
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#createAdviceDefinition(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, java.lang.String, int, org.springframework.beans.factory.support.RootBeanDefinition, org.springframework.beans.factory.support.RootBeanDefinition, java.util.List, java.util.List)
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#getAdviceClass(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 */

/**
 * <aop:aspectj-autoproxy proxy-target-class="" expose-proxy=""/> -> AspectJAutoProxyBeanDefinitionParser#parse(...)
 *
 * @see org.springframework.aop.config.AspectJAutoProxyBeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.aop.config.AopNamespaceUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 */