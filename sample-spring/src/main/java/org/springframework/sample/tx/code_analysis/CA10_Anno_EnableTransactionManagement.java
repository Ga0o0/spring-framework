package org.springframework.sample.tx.code_analysis;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.annotation.TransactionManagementConfigurationSelector;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

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
public class CA10_Anno_EnableTransactionManagement {

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Import(TransactionManagementConfigurationSelector.class)
	@interface CA01_EnableTransactionManagement {

		// 指示是否要创建基于子类 (CGLIB) 的代理 ({@code true})，而不是基于标准 Java 接口的代理 ({@code false})。
		// 默认值为 {@code false}。<strong>仅当 {@link #mode()} 设置为 {@link AdviceMode#PROXY} 时才适用。</strong>。
		// <p>请注意，将此属性设置为 {@code true} 将影响所有需要代理的 Spring 管理的 bean，而不仅仅是那些标有 {@code @Transactional} 的 bean。
		// 例如，其他标有 Spring 的 {@code @Async} 批注的 bean 将同时升级为子类代理。
		// 除非明确期望一种代理类型而不是另一种代理类型（例如在测试中），否则这种方法在实践中不会产生负面影响。
		boolean proxyTargetClass() default false;

		// 指示应如何应用事务通知。
		// <p><b>默认值为 {@link AdviceMode#PROXY}。</b>
		// 请注意，代理模式仅允许通过代理拦截调用。同一类中的本地调用无法通过这种方式拦截；
		// 本地调用中此类方法上的 {@link Transactional} 注解将被忽略，因为 Spring 的拦截器在这种运行时场景下甚至不会启动。
		// 如果需要更高级的拦截模式，请考虑将其切换为 {@link AdviceMode#ASPECTJ}。
		AdviceMode mode() default AdviceMode.PROXY;

		// 当在特定连接点应用多个建议时，指示事务顾问的执行顺序。
		// <p>默认值为 {@link Ordered#LOWEST_PRECEDENCE}。
		int order() default Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * @see org.springframework.transaction.annotation.TransactionManagementConfigurationSelector#selectImports(org.springframework.context.annotation.AdviceMode)
	 */
	static class CA02_TransactionManagementConfigurationSelector extends AdviceModeImportSelector<EnableTransactionManagement> {
		// 分别为 {@link EnableTransactionManagement#mode()} 的 {@code PROXY} 和 {@code ASPECTJ} 值
		// 返回 {@link ProxyTransactionManagementConfiguration} 或 {@code AspectJ(Jta)TransactionManagementConfiguration}。
		@Override
		protected String[] selectImports(AdviceMode adviceMode) {
			return switch (adviceMode) {
				case PROXY -> new String[]{AutoProxyRegistrar.class.getName(),
						ProxyTransactionManagementConfiguration.class.getName()};
				case ASPECTJ -> new String[]{determineTransactionAspectClass()};
			};
		}

		private String determineTransactionAspectClass() {
			return (ClassUtils.isPresent("jakarta.transaction.Transactional", getClass().getClassLoader()) ?
					// JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME = org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration
					TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME :
					// TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME = org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
					TransactionManagementConfigUtils.TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME);
		}
	}

	/**
	 * ProxyTransactionManagementConfiguration
	 *
	 * @see ProxyTransactionManagementConfiguration
	 */
	// {@code @Configuration} 类注册了启用基于代理的注释驱动的事务管理所需的 Spring 基础结构 bean。
	@Configuration(proxyBeanMethods = false)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	// @ImportRuntimeHints(TransactionRuntimeHints.class) // -> 源码
	@ImportRuntimeHints(CA03_TransactionRuntimeHints.class)
	static class CA03_ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {

		// TRANSACTION_ADVISOR_BEAN_NAME = org.springframework.transaction.config.internalTransactionAdvisor
		@Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
		@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
		public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
				TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {

			BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
			advisor.setTransactionAttributeSource(transactionAttributeSource);
			advisor.setAdvice(transactionInterceptor);
			if (this.enableTx != null) {
				advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
			}
			return advisor;
		}

		@Bean
		@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
		public TransactionAttributeSource transactionAttributeSource() {
			// Accept protected @Transactional methods on CGLIB proxies, as of 6.0.
			// --> 译文：从 6.0 开始，接受 CGLIB 代理上的受保护的 @Transactional 方法。
			return new AnnotationTransactionAttributeSource(false);
		}

		@Bean
		@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
		public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
			TransactionInterceptor interceptor = new TransactionInterceptor();
			interceptor.setTransactionAttributeSource(transactionAttributeSource);
			if (this.txManager != null) {
				interceptor.setTransactionManager(this.txManager);
			}
			return interceptor;
		}
	}


	// {@link RuntimeHintsRegistrar} 实现，为事务管理注册运行时提示。
	static class CA03_TransactionRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.reflection().registerTypes(
					TypeReference.listOf(Isolation.class, Propagation.class),
					TypeHint.builtWith(MemberCategory.DECLARED_FIELDS));
		}

	}

	/**
	 * AutoProxyRegistrar
	 *
	 * @see org.springframework.context.annotation.AutoProxyRegistrar
	 * @see org.springframework.context.annotation.AutoProxyRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	// 根据将 {@code mode} 和 {@code proxyTargetClass} 属性设置为正确值的 {@code @Enable} 注释，
	// 根据当前 {@link BeanDefinitionRegistry} 适当地注册一个自动代理创建器。
	// public class AutoProxyRegistrar implements ImportBeanDefinitionRegistrar {
	static class CA04_AutoProxyRegistrar implements ImportBeanDefinitionRegistrar {
		// 根据给定的注册表注册、升级和配置标准自动代理创建器 (APC)。
		// 其工作原理是查找导入的 {@code @Configuration} 类上声明的、同时具有 {@code mode} 和 {@code proxyTargetClass} 属性的最近的注解。
		// 如果 {@code mode} 设置为 {@code PROXY}，则注册 APC；
		// 如果 {@code proxyTargetClass} 设置为 {@code true}，则强制 APC 使用子类 (CGLIB) 代理。
		// <p>多个 {@code @Enable} 注解同时公开 {@code mode} 和 {@code proxyTargetClass} 属性。
		// 需要注意的是，这些功能中的大多数最终共享一个 {@linkplain AopConfigUtils#AUTO_PROXY_CREATOR_BEAN_NAME 的 APC}。
		// 因此，此实现并不“关心”它找到哪个注释——只要它公开正确的 {@code mode} 和 {@code proxyTargetClass} 属性，就可以以相同的方式注册和配置 APC。
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			boolean candidateFound = false;
			// 获取类上的注解
			Set<String> annTypes = importingClassMetadata.getAnnotationTypes();
			for (String annType : annTypes) {
				// 获取注解的属性
				// AnnotationAttributes candidate = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType); // -> 源码
				AnnotationAttributes candidate = CA04_AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
				if (candidate == null) {
					continue;
				}
				// 处理相关注解属性
				Object mode = candidate.get("mode");
				Object proxyTargetClass = candidate.get("proxyTargetClass");
				if (mode != null && proxyTargetClass != null && AdviceMode.class == mode.getClass() &&
						Boolean.class == proxyTargetClass.getClass()) {
					candidateFound = true;
					if (mode == AdviceMode.PROXY) {
						// 注册 InfrastructureAdvisorAutoProxyCreator 的 BeanDefinition
						AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
						if ((Boolean) proxyTargetClass) { // proxyTargetClass == true
							// 为上面的 BeanDefinition 注册一个属性 proxyTargetClass，值为 true
							AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
							return;
						}
					}
				}
			}
			// ...
		}
	}

	/**
	 * @see CA04_AnnotationConfigUtils#attributesFor(AnnotatedTypeMetadata, String)
	 */
	static class CA04_AnnotationConfigUtils extends AnnotationConfigUtils {
		@Nullable
		static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationTypeName) {
			return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationTypeName));
		}
	}

}
