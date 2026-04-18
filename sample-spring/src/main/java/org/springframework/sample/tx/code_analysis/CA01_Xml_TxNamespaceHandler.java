package org.springframework.sample.tx.code_analysis;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.transaction.config.JtaTransactionManagerBeanDefinitionParser;
import org.springframework.transaction.config.TxNamespaceHandler;
import org.w3c.dom.Element;

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
public class CA01_Xml_TxNamespaceHandler {

	/**
	 * @see TxNamespaceHandler#init()
	 */
	// public class TxNamespaceHandler extends NamespaceHandlerSupport {
	static class CA01_TxNamespaceHandler extends NamespaceHandlerSupport {
		static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";
		static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

		// 读取 <tx:advice id="" transaction-manager=""/> 元素的属性 transaction-manager 的值；默认为 transactionManager
		static String getTransactionManagerName(Element element) {
			return (element.hasAttribute(TRANSACTION_MANAGER_ATTRIBUTE) ?
					element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE) : DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
		}

		@Override
		public void init() {
			// <tx:advice id="" transaction-manager=""/>
			/*registerBeanDefinitionParser("advice", new TxAdviceBeanDefinitionParser());*/ // -> TransactionAttributeSource
			// <tx:annotation-driven/>
			/*registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());*/
			// <tx:jta-transaction-manager/>
			registerBeanDefinitionParser("jta-transaction-manager", new JtaTransactionManagerBeanDefinitionParser());
		}
	}

}
