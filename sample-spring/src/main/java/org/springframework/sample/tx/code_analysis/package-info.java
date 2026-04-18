/**
 * Transaction Manager
 *
 * @see org.springframework.transaction.config.TxNamespaceHandler
 */
package org.springframework.sample.tx.code_analysis;

/**
 * spring-tx/src/main/resources/META-INF/spring.handlers
 *
 * @see org.springframework.transaction.config.TxNamespaceHandler
 * <p>
 * <tx:advice />
 * @see org.springframework.transaction.config.TxAdviceBeanDefinitionParser
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 * <p>
 * <tx:annotation-driven />
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser
 * @see org.springframework.transaction.aspectj.AnnotationTransactionAspect
 * <p>
 * <tx:jta-transaction-manager />
 * @see org.springframework.transaction.config.JtaTransactionManagerBeanDefinitionParser
 */

/**
 * {@code <tx:advice />} -> TxAdviceBeanDefinitionParser
 *
 * @see org.springframework.transaction.config.TxAdviceBeanDefinitionParser
 * @see org.springframework.transaction.config.TxAdviceBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)
 * @see org.springframework.transaction.config.TxAdviceBeanDefinitionParser#parseAttributeSource(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#nameMap
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */

/**
 * {@code <tx:annotation-driven />} -> AnnotationDrivenBeanDefinitionParser
 *
 * <p>重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点重点
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * # 1. 注册 TransactionalEventListenerFactory 的 BeanDefinition
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser#registerTransactionalEventListenerFactory(org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.event.TransactionalEventListenerFactory
 *
 * # 2. 注册 TransactionAspect 的 BeanDefinition
 *
 * ## 2.1. mode = aspectj -> JtaAnnotationTransactionAspect
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser#registerTransactionAspect(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.aspectj.AnnotationTransactionAspect
 *
 * ### 2.1.1. 存在 jakarta.transaction.Transactional -> JtaAnnotationTransactionAspect
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser#registerJtaTransactionAspect(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.aspectj.JtaAnnotationTransactionAspect
 *
 * ## 2.2. mode = proxy -> BeanFactoryTransactionAttributeSourceAdvisor
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer#configureAutoProxyCreator(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 */

/**
 * {@code <tx:jta-transaction-manager />} -> JtaTransactionManagerBeanDefinitionParser
 *
 * @see org.springframework.transaction.config.JtaTransactionManagerBeanDefinitionParser
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */

/**
 * {@code EnableTransactionManagement} ==  {@code <tx:annotation-driven />}
 *
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @see org.springframework.transaction.annotation.TransactionManagementConfigurationSelector
 * @see org.springframework.transaction.annotation.TransactionManagementConfigurationSelector#selectImports(org.springframework.context.annotation.AdviceMode)
 *
 * # 1. AdviceMode.PROXY
 *
 * ## 1.1. AutoProxyRegistrar
 *
 * @see org.springframework.context.annotation.AutoProxyRegistrar
 * @see org.springframework.context.annotation.AutoProxyRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 *
 * ## 1.2. ProxyTransactionManagementConfiguration
 *
 * @see org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 *
 * # 2. AdviceMode.ASPECTJ
 *
 * @see org.springframework.transaction.annotation.TransactionManagementConfigurationSelector#determineTransactionAspectClass()
 *
 * ### 2.1. 存在 jakarta.transaction.Transactional -> JtaAnnotationTransactionAspect
 *
 * @see org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration
 * @see spring-aspects/src/main/java/org/springframework/transaction/aspectj/AspectJJtaTransactionManagementConfiguration.java
 * @see org.springframework.transaction.aspectj.JtaAnnotationTransactionAspect
 *
 * ### 2.2. 不存在 jakarta.transaction.Transactional -> AnnotationTransactionAspect
 *
 * @see org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
 * @see spring-aspects/src/main/java/org/springframework/transaction/aspectj/AspectJTransactionManagementConfiguration.java
 * @see org.springframework.transaction.aspectj.AnnotationTransactionAspect
 */

/**
 * 重点关注：{@code EnableTransactionManagement} ==  {@code <tx:annotation-driven />}
 *
 * # 1. {@code <tx:annotation-driven />} 中的重点
 *
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer#configureAutoProxyCreator(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * # 2. {@code EnableTransactionManagement} 中的重点
 *
 * @see org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration
 *
 * # 3. {@code EnableTransactionManagement} 和 {@code <tx:annotation-driven />} 最终做的事
 *
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */


/**
 * AnnotationTransactionAttributeSource
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource()
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 */

/**
 * BeanFactoryTransactionAttributeSourceAdvisor
 *
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#pointcut
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setTransactionAttributeSource(org.springframework.transaction.interceptor.TransactionAttributeSource)
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setAdviceBeanName(java.lang.String)
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setAdvice(org.aopalliance.aop.Advice)
 *
 * # 1. pointcut
 *
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#pointcut
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut#matches(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.TransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, Class)
 *
 * ## 1.1. NameMatchTransactionAttributeSource##getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#nameMap
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#isMatch(String, String)
 *
 * ## 1.2. AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#attributeCache
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#computeTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#findTransactionAttribute(java.lang.reflect.Method)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#determineTransactionAttribute(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#annotationParsers
 *
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 *
 * ### 1.2.1. SpringTransactionAnnotationParser
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(org.springframework.core.annotation.AnnotationAttributes)
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * ### 1.2.2. JtaTransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see jakarta.transaction.Transactional
 *
 * ### 1.2.3. Ejb3TransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 * @see jakarta.ejb.TransactionAttribute
 */

/**
 * TransactionInterceptor
 *
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#invokeWithinTransaction(java.lang.reflect.Method, Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback)
 *
 *
 * # 1. 获取 TransactionAttributeSource -> AnnotationTransactionAttributeSource
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#getTransactionAttributeSource()
 *
 * ## 1.1. {@code EnableTransactionManagement} 中 TransactionAttributeSource 的设置地点
 * @see org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration#transactionInterceptor(org.springframework.transaction.interceptor.TransactionAttributeSource)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 *
 * ## 1.2. {@code <tx:annotation-driven />} 中 TransactionAttributeSource 的设置地点
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer#configureAutoProxyCreator(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 *
 *
 * # 2. 获取 TransactionAttribute -> AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#attributeCache
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#computeTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#findTransactionAttribute(java.lang.reflect.Method)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#determineTransactionAttribute(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#annotationParsers
 *
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 *
 * ## 2.1. SpringTransactionAnnotationParser
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(org.springframework.core.annotation.AnnotationAttributes)
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * ## 2.2. JtaTransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see jakarta.transaction.Transactional
 *
 * ## 2.3. Ejb3TransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 * @see jakarta.ejb.TransactionAttribute
 *
 *
 * # 3. 确定 TransactionManager
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#determineTransactionManager(org.springframework.transaction.interceptor.TransactionAttribute)
 * @see org.springframework.transaction.TransactionManager
 *
 * # 4. 如果 TransactionManager 是 org.springframework.transaction.ReactiveTransactionManager 类型，调用 ReactiveTransactionSupport#invokeWithinTransaction() 方法处理并返回
 *
 * @see org.springframework.transaction.ReactiveTransactionManager
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.ReactiveTransactionSupport#invokeWithinTransaction(java.lang.reflect.Method, Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback, org.springframework.transaction.interceptor.TransactionAttribute, org.springframework.transaction.ReactiveTransactionManager)
 *
 * # 5. 步骤判断不成功（TransactionManager 不是 org.springframework.transaction.ReactiveTransactionManager 类型），进行下一步，将 TransactionManager 转换成 org.springframework.transaction.PlatformTransactionManager
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#asPlatformTransactionManager(Object)
 *
 * # 6. 如有需要，根据给定的 TransactionAttribute 创建事务
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#createTransactionIfNecessary(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String)
 *
 * ## 6.1. 处理传播行为
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
 *
 * ### 6.1.1. 返回当前事务状态的事务对象
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
 *
 * ### 6.1.2. 检查给定的事务对象是否存在现有事务（即已启动的事务）；如果存在现有事务，为现有事务创建 org.springframework.transaction.TransactionStatus
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#isExistingTransaction(java.lang.Object)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#handleExistingTransaction(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean)
 *
 * ### 6.1.3. 处理事务超时
 *
 * ### 6.1.4. 开启新的事务
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#startTransaction(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean, boolean, org.springframework.transaction.support.AbstractPlatformTransactionManager.SuspendedResourcesHolder)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#newTransactionStatus(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean, boolean, boolean, boolean, java.lang.Object)
 * @see org.springframework.transaction.TransactionExecutionListener#beforeBegin(org.springframework.transaction.TransactionExecution)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object, org.springframework.transaction.TransactionDefinition)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#prepareSynchronization(org.springframework.transaction.support.DefaultTransactionStatus, org.springframework.transaction.TransactionDefinition)
 * @see org.springframework.transaction.TransactionExecutionListener#afterBegin(org.springframework.transaction.TransactionExecution, java.lang.Throwable)
 *
 * ## 6.2. 根据给定的属性和状态对象准备一个 TransactionInfo 对象
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#prepareTransactionInfo(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String, org.springframework.transaction.TransactionStatus)
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#TransactionInfo(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, String)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#newTransactionStatus(org.springframework.transaction.TransactionStatus)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#bindToThread()
 *
 *
 * # 7。 执行目标调用
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback#proceedWithInvocation()
 * @see org.aopalliance.intercept.Joinpoint#proceed()
 *
 * # 8. 处理 throwable，完成事务。我们可能会提交或回滚，具体取决于配置。
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#completeTransactionAfterThrowing(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo, java.lang.Throwable)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#transactionAttribute
 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn(Throwable)
 *
 * ## 8.1. txInfo.transactionAttribute.rollbackOn(ex) = true
 *
 * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
 *
 * ## 8.2. txInfo.transactionAttribute.rollbackOn(ex) = false
 *
 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
 *
 *
 * # 9. 重置 TransactionInfo ThreadLocal
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#cleanupTransactionInfo(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#restoreThreadLocalStatus()
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#transactionInfoHolder
 * @see java.lang.ThreadLocal#set(Object)
 *
 * # 10. 调用成功完成后执行，但不在处理异常后执行
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#commitTransactionAfterReturning(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo)
 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
 */


