package org.springframework.sample.aop.code_analysis.aspectj_based;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

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
public class CodeAnalysis01_AtEnableAspectJAutoProxy {

	/*
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Import(AspectJAutoProxyRegistrar.class)
	public @interface EnableAspectJAutoProxy {
		// 指示是否创建基于子类 (CGLIB) 的代理，而不是基于标准 Java 接口的代理。默认值为 {@code false}。
		boolean proxyTargetClass() default false;

		// 指示代理是否应由 AOP 框架以 {@code ThreadLocal} 的形式暴露，
		// 以便通过 {@link org.springframework.aop.framework.AopContext} 类进行检索。默认关闭，即不保证 {@code AopContext} 访问有效。
		boolean exposeProxy() default false;
	}
	**/

	/**
	 * @see org.springframework.context.annotation.AspectJAutoProxyRegistrar#registerBeanDefinitions(AnnotationMetadata, BeanDefinitionRegistry)
	 */
	static class CA01_AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {
		// 根据导入 {@code @Configuration} 类上的 @{@link EnableAspectJAutoProxy#proxyTargetClass()} 属性的值注册、升级和配置 AspectJ 自动代理创建器。
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
			// 1. 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AnnotationAwareAspectJAutoProxyCreator
			AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry); // important -> go

			// 2. 获取 @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true) 注解的属性并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例
			AnnotationAttributes enableAspectJAutoProxy = new AnnotationAttributes(); // 源码在下面一行，但是这里不能使用，故隐藏
			/*AnnotationAttributes enableAspectJAutoProxy =
					AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);*/
			if (enableAspectJAutoProxy != null) {
				// 获取属性值 proxyTargetClass 的值，并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例的 PropertyValues#proxyTargetClass
				if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
					AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
				}
				// 获取属性值 exposeProxy 的值，并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例的 PropertyValues#exposeProxy
				if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
					AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
				}
			}
		}
	}

	/**
	 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	static class CA02_AopConfigUtils extends AopConfigUtils {
		// 内部管理的自动代理创建者的 bean 名称。
		public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
				"org.springframework.aop.config.internalAutoProxyCreator";

		// 按升级顺序存储自动代理创建者类。
		private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<>(3);

		static { // important -> go
			// Set up the escalation list...
			// (1) AutoProxyRegistrar(@EnableCaching/@EnableTransactionManagement)
			// (2) Parser(<cache:annotation-driven/>/<tx:annotation-driven/>)
			APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
			APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);   	// <aop:config/>
			APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);	// @EnableAspectJAutoProxy/<aop:aspectj-autoproxy />
		}

		public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
			return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
		}

		public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
				BeanDefinitionRegistry registry, @Nullable Object source) {
			return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source); // important -> go
		}

		private static BeanDefinition registerOrEscalateApcAsRequired(
				Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {

			Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

			// 1. BeanDefinitionRegistry 中存在 org.springframework.aop.config.internalAutoProxyCreator 的 BeanDefinition，
			// 比较当前实例和容器中 BeanDefinition#beanClass 的优先级，将较大的 beanClass 设置给 BeanDefinition
			if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
				BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
				if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
					int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName()); // important -> go
					int requiredPriority = findPriorityForClass(cls); // important -> go
					// 升级处理
					if (currentPriority < requiredPriority) {
						apcDefinition.setBeanClassName(cls.getName());
					}
				}
				return null;
			}

			// 2. BeanDefinitionRegistry 中不存在 org.springframework.aop.config.internalAutoProxyCreator 的 BeanDefinition 时，创建一个并进行注册
			RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
			beanDefinition.setSource(source);
			beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
			return beanDefinition;
		}

		private static int findPriorityForClass(Class<?> clazz) {
			return APC_PRIORITY_LIST.indexOf(clazz);
		}

		private static int findPriorityForClass(@Nullable String className) {
			for (int i = 0; i < APC_PRIORITY_LIST.size(); i++) {
				Class<?> clazz = APC_PRIORITY_LIST.get(i);
				if (clazz.getName().equals(className)) {
					return i;
				}
			}
			throw new IllegalArgumentException(
					"Class name [" + className + "] is not a known auto-proxy creator class");
		}
	}
}
