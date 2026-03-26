/**
 * 无论是基于 AspectJ 注解，还是基于 schema 文件配置去声明一个 Aspect，
 * 最终的处理类都是 {@link org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator} 的子类
 */
package org.springframework.sample.aop.code_analysis;
/*---------------------------------------------------------------- 声明一个 Aspect 的两种方式 ----------------------------------------------------------------
声明一个 Aspect 的两种方式（@Aspect === <aop:config>）：
	1. 基于 schema 文件配置；	即：<aop:config>			--> AspectJAwareAdvisorAutoProxyCreator 	--源码解读--> CodeAnalysis01_ConfigBeanDefinitionParser_parse.java
	2，基于 AspectJ 注解；	即：@Aspect				--> AnnotationAwareAspectJAutoProxyCreator

启用 @Aspect 注解支持的两种方式：
	1. @EnableAspectJAutoProxy		--> AnnotationAwareAspectJAutoProxyCreator	--源码解读--> CodeAnalysis01_AtEnableAspectJAutoProxy.java
	2. <aop:aspectj-autoproxy />	--> AnnotationAwareAspectJAutoProxyCreator	--源码解读--> CodeAnalysis10_AspectJAutoProxyBeanDefinitionParser.java

-----------------------------------------------------------------------------------------------------------------------------------------------------------*/

/*------------------------------------------------------------ AbstractAutoProxyCreator 及其子类 ------------------------------------------------------------
ProxyConfig																[class]
\--extends-- ProxyProcessorSupport 										[class]
	\--extends-- AbstractAutoProxyCreator 								[abstract class]	important
		\--extends-- BeanNameAutoProxyCreator 							[class]
		\--extends-- AbstractAdvisorAutoProxyCreator 					[abstract class]
			\--extends-- DefaultAdvisorAutoProxyCreator 				[class]				功能不详
			\--extends-- InfrastructureAdvisorAutoProxyCreator 			[class]				事务标签 <tx:annotation-driven/> 的处理类
			\--extends-- AspectJAwareAdvisorAutoProxyCreator 			[class]				<aop:config> 标签声明的 Aspect 解析类
				\--extends-- AnnotationAwareAspectJAutoProxyCreator 	[class]				基于 AspectJ 注解声明的 Aspect 解析类；@EnableAspectJAutoProxy 和 <aop:aspectj-autoproxy />

public abstract class AbstractAutoProxyCreator extends ProxyProcessorSupport
		implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware { ... }
-----------------------------------------------------------------------------------------------------------------------------------------------------------*/

/**------------------------------------------------------------ AbstractAutoProxyCreator 及其子类 ------------------------------------------------------------
 * @see org.springframework.aop.framework.ProxyConfig
 * @see org.springframework.aop.framework.ProxyProcessorSupport
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator
 * @see org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
-----------------------------------------------------------------------------------------------------------------------------------------------------------*/

/**------------------------------------------------------ AbstractAutoProxyCreator 重要方法 - 按调用顺序 ------------------------------------------------------
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getEarlyBeanReference(java.lang.Object, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
-----------------------------------------------------------------------------------------------------------------------------------------------------------*/

/**
 * AbstractAutoProxyCreator 重要方法 - postProcessBeforeInstantiation(...)
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
 *
 * ## 1. 获取自定义的 TargetSourceCreator，并执行 TargetSourceCreator#getTargetSource(...) 方法获取 TargetSource 实例返回
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getCustomTargetSource(java.lang.Class, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#customTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.TargetSourceCreator
 * @see org.springframework.aop.framework.autoproxy.TargetSourceCreator#getTargetSource(java.lang.Class, java.lang.String)
 */

/**
 * AbstractAutoProxyCreator 重要方法 - getEarlyBeanReference(...)
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getEarlyBeanReference(java.lang.Object, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#wrapIfNecessary(java.lang.Object, java.lang.String, java.lang.Object)
 * -> AbstractAutoProxyCreator 重要方法 - wrapIfNecessary(...) -> 创建代理
 */

/**
 * AbstractAutoProxyCreator 重要方法 - postProcessAfterInitialization(...)
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#wrapIfNecessary(java.lang.Object, java.lang.String, java.lang.Object)
 * -> AbstractAutoProxyCreator 重要方法 - wrapIfNecessary(...) -> 创建代理
 */

/**
 * AbstractAutoProxyCreator 重要方法 - wrapIfNecessary(...) -> 创建代理
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#wrapIfNecessary(java.lang.Object, java.lang.String, java.lang.Object)
 *
 * ## 1. 跳过不应该被代理的类
 *
 * ### 1.1. 跳过不应该被代理的基础结构类
 *
 * -> AbstractAutoProxyCreator#isInfrastructureClass(Class)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#isInfrastructureClass(java.lang.Class)
 * -> AnnotationAwareAspectJAutoProxyCreator#isInfrastructureClass(Class)
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#isInfrastructureClass(java.lang.Class)
 *
 * @see org.aopalliance.aop.Advice
 * @see org.springframework.aop.Pointcut
 * @see org.springframework.aop.Advisor
 * @see org.springframework.aop.framework.AopInfrastructureBean
 *
 * ### 1.2. 跳过不应该被代理的基础结构类
 *
 * -> AbstractAutoProxyCreator#shouldSkip(Class, String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#shouldSkip(java.lang.Class, java.lang.String)
 * -> AnnotationAwareAspectJAutoProxyCreator#shouldSkip(Class, String)
 * @see org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator#shouldSkip(java.lang.Class, java.lang.String)
 *
 * #### 1.2.1. 查找所有用于自动代理的候选 Advisors -> AbstractAdvisorAutoProxyCreator/AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * -> AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper#findAdvisorBeans()
 * -> AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * ## 2. 查找所有符合条件的 Advisor 来自动代理此类
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findEligibleAdvisors(java.lang.Class, java.lang.String)
 *
 * ### 2.1. 查找所有用于自动代理的候选 Advisors -> AbstractAdvisorAutoProxyCreator/AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * -> AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper#findAdvisorBeans()
 * -> AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * ## 3. 为给定的 bean 创建 AOP 代理
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#createProxy(java.lang.Class, java.lang.String, java.lang.Object[], org.springframework.aop.TargetSource)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#buildProxy(java.lang.Class, java.lang.String, java.lang.Object[], org.springframework.aop.TargetSource, boolean)
 *
 *
 * ### 3.1. 根据此工厂中的设置确定代理类 -> ProxyFactory#getProxyClass(ClassLoader) =  ProxyCreatorSupport#createAopProxy() + AopProxy#getProxyClass(ClassLoader)
 *
 * @see org.springframework.aop.framework.ProxyFactory#getProxyClass(java.lang.ClassLoader)
 *
 * #### 3.1.1. ProxyCreatorSupport#createAopProxy()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#createAopProxy()
 * -> ProxyCreatorSupport 重要方法 - ProxyCreatorSupport#createAopProxy() = ProxyCreatorSupport#getAopProxyFactory() + AopProxyFactory#createAopProx(AdvisedSupport) -> 创建 AOP 代理
 *
 * #### 3.1.2. AopProxy#getProxyClass(ClassLoader)
 *
 * @see org.springframework.aop.framework.AopProxy#getProxyClass(java.lang.ClassLoader)
 *
 * @see org.springframework.aop.framework.CglibAopProxy#getProxyClass(java.lang.ClassLoader)
 * @see org.springframework.aop.framework.JdkDynamicAopProxy#getProxyClass(java.lang.ClassLoader)
 *
 *
 * ### 3.2. 根据此工厂中的设置创建一个新的代理 -> ProxyFactory#getProxy(ClassLoader) = ProxyCreatorSupport#createAopProxy() + AopProxy#getProxy(classLoader)
 *
 * @see org.springframework.aop.framework.ProxyFactory#getProxy(java.lang.ClassLoader)
 *
 * #### 3.2.1. ProxyCreatorSupport#createAopProxy()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#createAopProxy()
 * -> ProxyCreatorSupport 重要方法 - ProxyCreatorSupport#createAopProxy() = ProxyCreatorSupport#getAopProxyFactory() + AopProxyFactory#createAopProx(AdvisedSupport) -> 创建 AOP 代理
 *
 * #### 3.2.2. AopProxy#getProxy(classLoader)
 *
 * @see org.springframework.aop.framework.AopProxy#getProxy(java.lang.ClassLoader)
 *
 * @see org.springframework.aop.framework.CglibAopProxy#getProxy(java.lang.ClassLoader)
 * @see org.springframework.aop.framework.JdkDynamicAopProxy#getProxy(java.lang.ClassLoader)
 */

/**
 * AbstractAutoProxyCreator 重要方法
 *
 * 1. AbstractAutoProxyCreator#getAdvicesAndAdvisorsForBean(...)
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
 *
 * 2. AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 */

/**
 * AnnotationAwareAspectJAutoProxyCreator 重要方法 - AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors() -> 查找所有用于自动代理的候选 Advisors
 *
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * @see org.springframework.aop.aspectj.annotation.BeanFactoryAspectJAdvisorsBuilder#buildAspectJAdvisors()
 * @see org.springframework.aop.aspectj.annotation.AspectMetadata#AspectMetadata(java.lang.Class, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvisors(org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory)
 *
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvisor(java.lang.reflect.Method, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl#InstantiationModelAwarePointcutAdvisorImpl(org.springframework.aop.aspectj.AspectJExpressionPointcut, java.lang.reflect.Method, org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl#instantiateAdvice(org.springframework.aop.aspectj.AspectJExpressionPointcut)
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvice(java.lang.reflect.Method, org.springframework.aop.aspectj.AspectJExpressionPointcut, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 */

/**
 * ProxyCreatorSupport 重要方法 - ProxyCreatorSupport#createAopProxy() = ProxyCreatorSupport#getAopProxyFactory() + AopProxyFactory#createAopProx(AdvisedSupport) -> 创建 AOP 代理
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#createAopProxy()
 *
 * ## 1. ProxyCreatorSupport#getAopProxyFactory()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#getAopProxyFactory()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#ProxyCreatorSupport()
 * @see org.springframework.aop.framework.DefaultAopProxyFactory#INSTANCE
 * @see org.springframework.aop.framework.AopProxyFactory
 *
 * ## 2. AopProxyFactory#createAopProx(AdvisedSupport)
 *
 * @see org.springframework.aop.framework.AopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
 * @see org.springframework.aop.framework.DefaultAopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
 *
 * @see org.springframework.aop.framework.JdkDynamicAopProxy
 * @see org.springframework.aop.framework.ObjenesisCglibAopProxy
 */