package org.springframework.sample.cache._mine.code_analysis;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

/**
 * AutoProxyRegistrar
 *
 * @see org.springframework.cache.annotation.CachingConfigurationSelector#getProxyImports()
 * @see org.springframework.context.annotation.AutoProxyRegistrar
 * @see org.springframework.context.annotation.AutoProxyRegistrar#registerBeanDefinitions(org.springframework.core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
 *
 * ## 1. @EnableCaching#mode == AdviceMode.PROXY -> 注册一个名为 org.springframework.aop.config.internalAutoProxyCreator，类型为 InfrastructureAdvisorAutoProxyCreator 的 BeanDefinition
 *
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 * @see org.springframework.aop.config.AopConfigUtils#registerAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.config.AopConfigUtils#registerOrEscalateApcAsRequired(java.lang.Class, org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 * @see org.springframework.aop.config.AopConfigUtils#AUTO_PROXY_CREATOR_BEAN_NAME
 * @see org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry#registerBeanDefinition(java.lang.String, org.springframework.beans.factory.config.BeanDefinition)
 *
 * ### 1.1. @EnableCaching#proxyTargetClass == true -> 为上面（步骤 1 中）的 BeanDefinition 注册一个属性 proxyTargetClass，值为 true
 *
 * @see org.springframework.aop.config.AopConfigUtils#forceAutoProxyCreatorToUseClassProxying(org.springframework.beans.factory.support.BeanDefinitionRegistry)
 */
public class CodeAnalysis10_AutoProxyRegistrar {

	static class CA01_AutoProxyRegistrar extends AutoProxyRegistrar {
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
				// AnnotationAttributes candidate = AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
				AnnotationAttributes candidate = CA02_AnnotationConfigUtils.attributesFor(importingClassMetadata, annType);
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

	static abstract class CA02_AnnotationConfigUtils {
		static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, Class<?> annotationType) {
			return attributesFor(metadata, annotationType.getName());
		}
		static AnnotationAttributes attributesFor(AnnotatedTypeMetadata metadata, String annotationTypeName) {
			return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationTypeName));
		}
	}
}
