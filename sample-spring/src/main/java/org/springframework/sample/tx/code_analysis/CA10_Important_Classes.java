package org.springframework.sample.tx.code_analysis;

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
public class CA10_Important_Classes {

	/**
	 * @see CA04_Xml_AnnotationDrivenBeanDefinitionParser
	 * @see CA10_Anno_EnableTransactionManagement
	 */
}
