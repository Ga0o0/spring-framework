package org.springframework.sample.tx.code_analysis;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Element;

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
public class CA04_Xml_AnnotationDrivenBeanDefinitionParser {

	// {@link org.springframework.beans.factory.xml.BeanDefinitionParser BeanDefinitionParser} 实现允许用户轻松配置所有必要的基础架构 Bean，以启用注解驱动的事务划分。
	//
	// <p>默认情况下，所有代理均创建为 JDK 代理。如果您将对象注入为具体类而非接口，这可能会导致一些问题。
	// 为了克服此限制，您可以将“{@code proxy-target-class}”属性设置为“{@code true}”，这将导致创建基于类的代理。
	// class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {
	static class CA01_AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {
		// 解析 {@code <tx:annotation-driven/>} 标签。
		// 将根据需要向容器 {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary 注册一个 AutoProxyCreator}
		@Override
		@Nullable
		public BeanDefinition parse(Element element, ParserContext parserContext) {
			// 注册 TransactionalEventListenerFactory 的 Bean 定义到 ParserContext
			registerTransactionalEventListenerFactory(parserContext); // 注册的 Bean -> TransactionalEventListenerFactory
			// <tx:annotation-driven mode=""/>，属性 mode 的默认值为 proxy；可选值：aspectj/proxy
			String mode = element.getAttribute("mode");
			if ("aspectj".equals(mode)) {
				// mode = aspectj
				registerTransactionAspect(element, parserContext); // 注册的 Bean -> AnnotationTransactionAspect
				if (ClassUtils.isPresent("jakarta.transaction.Transactional", getClass().getClassLoader())) {
					registerJtaTransactionAspect(element, parserContext); // 注册的 Bean -> JtaAnnotationTransactionAspect
				}
			}
			else {
				// mode = proxy
				// AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext); // 源码
				CA01_AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
				// 上面的方法注册的 Bean：
				// 1. InfrastructureAdvisorAutoProxyCreator
				// 2. AnnotationTransactionAttributeSource
				// 3. TransactionInterceptor
				// 4. BeanFactoryTransactionAttributeSourceAdvisor
			}
			return null;
		}


		// 内部类实际上在代理模式下只是引入了AOP框架依赖。
		private static class AopAutoProxyConfigurer {
			public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
				// 1. 必要时注册 AutoProxyCreator -> InfrastructureAdvisorAutoProxyCreator
				AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element); // -> InfrastructureAdvisorAutoProxyCreator

				// TRANSACTION_ADVISOR_BEAN_NAME = "org.springframework.transaction.config.internalTransactionAdvisor"
				String txAdvisorBeanName = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
				if (!parserContext.getRegistry().containsBeanDefinition(txAdvisorBeanName)) { // 无 TRANSACTION_ADVISOR_BEAN_NAME 时的处理
					Object eleSource = parserContext.extractSource(element);

					// 2. Create the TransactionAttributeSource definition. --> 译文：创建 TransactionAttributeSource 定义。
					RootBeanDefinition sourceDef = new RootBeanDefinition(
							"org.springframework.transaction.annotation.AnnotationTransactionAttributeSource"); // -> AnnotationTransactionAttributeSource
					sourceDef.setSource(eleSource);
					sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
					String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

					// 3. Create the TransactionInterceptor definition. --> 译文：创建 TransactionInterceptor 定义。
					RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class); // -> TransactionInterceptor
					interceptorDef.setSource(eleSource);
					interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
					registerTransactionManager(element, interceptorDef);
					interceptorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
					String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

					// 4. Create the TransactionAttributeSourceAdvisor definition. --> 译文：创建 TransactionAttributeSourceAdvisor 定义。
					RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class); // -> BeanFactoryTransactionAttributeSourceAdvisor
					advisorDef.setSource(eleSource);
					advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
					advisorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName)); // important
					advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);  // important
					if (element.hasAttribute("order")) {
						advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
					}
					parserContext.getRegistry().registerBeanDefinition(txAdvisorBeanName, advisorDef);

					// 5. 使用 CompositeComponentDefinition 包含多个嵌套的 ComponentDefinition 实例，将它们聚合到一个命名的组件组中。
					CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
					// TransactionAttributeSource -> AnnotationTransactionAttributeSource
					compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
					// TransactionInterceptor
					compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
					// TransactionAttributeSourceAdvisor -> BeanFactoryTransactionAttributeSourceAdvisor
					compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, txAdvisorBeanName));
					parserContext.registerComponent(compositeDef);
				}
			}
		}


		private void registerTransactionAspect(Element element, ParserContext parserContext) {
			// TRANSACTION_ASPECT_BEAN_NAME = "org.springframework.transaction.config.internalTransactionAspect"
			String txAspectBeanName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME;
			// TRANSACTION_ASPECT_CLASS_NAME = "org.springframework.transaction.aspectj.AnnotationTransactionAspect"
			// -> spring-aspects/src/main/java/org/springframework/transaction/aspectj/AnnotationTransactionAspect.aj
			String txAspectClassName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_CLASS_NAME;
			if (!parserContext.getRegistry().containsBeanDefinition(txAspectBeanName)) {
				RootBeanDefinition def = new RootBeanDefinition();
				def.setBeanClassName(txAspectClassName);
				def.setFactoryMethodName("aspectOf");
				// register TransactionManager
				registerTransactionManager(element, def);
				parserContext.registerBeanComponent(new BeanComponentDefinition(def, txAspectBeanName));
			}
		}

		private void registerJtaTransactionAspect(Element element, ParserContext parserContext) {
			// JTA_TRANSACTION_ASPECT_BEAN_NAME = "org.springframework.transaction.config.internalJtaTransactionAspect"
			String txAspectBeanName = TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_BEAN_NAME;
			// JTA_TRANSACTION_ASPECT_CLASS_NAME = "org.springframework.transaction.aspectj.JtaAnnotationTransactionAspect"
			// -> spring-aspects/src/main/java/org/springframework/transaction/aspectj/JtaAnnotationTransactionAspect.aj
			String txAspectClassName = TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CLASS_NAME;
			if (!parserContext.getRegistry().containsBeanDefinition(txAspectBeanName)) {
				RootBeanDefinition def = new RootBeanDefinition();
				def.setBeanClassName(txAspectClassName);
				def.setFactoryMethodName("aspectOf");
				// register TransactionManager
				registerTransactionManager(element, def);
				parserContext.registerBeanComponent(new BeanComponentDefinition(def, txAspectBeanName));
			}
		}

		private static void registerTransactionManager(Element element, BeanDefinition def) {
			def.getPropertyValues().add("transactionManagerBeanName",
					// TxNamespaceHandler.getTransactionManagerName(element)); // -> 源码
					CA01_Xml_TxNamespaceHandler.CA01_TxNamespaceHandler.getTransactionManagerName(element));
		}

		private void registerTransactionalEventListenerFactory(ParserContext parserContext) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClass(TransactionalEventListenerFactory.class);
			parserContext.registerBeanComponent(new BeanComponentDefinition(def,
					// TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME = "org.springframework.transaction.config.internalTransactionalEventListenerFactory"
					TransactionManagementConfigUtils.TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME));
		}

	}

}
