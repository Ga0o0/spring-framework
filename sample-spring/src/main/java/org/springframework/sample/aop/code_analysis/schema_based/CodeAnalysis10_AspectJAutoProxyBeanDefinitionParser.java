package org.springframework.sample.aop.code_analysis.schema_based;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * <aop:aspectj-autoproxy proxy-target-class="" expose-proxy=""/> -> AspectJAutoProxyBeanDefinitionParser#parse(...)
 *
 * @see org.springframework.aop.config.AspectJAutoProxyBeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.aop.config.AopNamespaceUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
 *
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
 */
public class CodeAnalysis10_AspectJAutoProxyBeanDefinitionParser {

	/**
	 * @see org.springframework.aop.config.AspectJAutoProxyBeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	static class CA01_AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {
		@Override
		@Nullable
		public BeanDefinition parse(Element element, ParserContext parserContext) {
			// 1. 注册 AnnotationAwareAspectJAutoProxyCreator 的 BeanDefinition，并获取 <aop:aspectj-autoproxy/> 标签的属性设置给它
			AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);  // important -> go
			extendBeanDefinition(element, parserContext);
			return null;
		}

		private void extendBeanDefinition(Element element, ParserContext parserContext) {
			// ...
		}
	}

	/**
	 * @see org.springframework.aop.config.AopNamespaceUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.xml.ParserContext, org.w3c.dom.Element)
	 */
	static class CA02_AopNamespaceUtils extends AopNamespaceUtils {
		// 在 AOP 相关的 XML 标签上发现的 {@code proxy-target-class} 属性。
		public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";
		// 在 AOP 相关的 XML 标签上发现的 {@code reveal-proxy} 属性。
		private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";

		public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
				ParserContext parserContext, Element sourceElement) {

			// 1. 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AnnotationAwareAspectJAutoProxyCreator
			BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
					parserContext.getRegistry(), parserContext.extractSource(sourceElement));  // important -> go
			// 2. 获取 <aop:aspectj-autoproxy proxy-target-class="" expose-proxy=""/> 标签的属性（expose-proxy 和 proxy-target-class）并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例
			useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);  // important -> go
			// 3. 注册组件
			registerComponentIfNecessary(beanDefinition, parserContext);
		}

		private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, @Nullable Element sourceElement) {
			if (sourceElement != null) {
				// 获取标签属性 proxy-target-class 设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的 BeanDefinition 的 proxyTargetClass 的属性
				boolean proxyTargetClass = Boolean.parseBoolean(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
				if (proxyTargetClass) {
					AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
				}
				// 获取标签属性 expose-proxy 设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的 BeanDefinition 的 exposeProxy 的属性
				boolean exposeProxy = Boolean.parseBoolean(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
				if (exposeProxy) {
					AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
				}
			}
		}

		private static void registerComponentIfNecessary(@Nullable BeanDefinition beanDefinition, ParserContext parserContext) {
			if (beanDefinition != null) {
				parserContext.registerComponent(
						new BeanComponentDefinition(beanDefinition, AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME));
			}
		}
	}

	/**
	 * @see org.springframework.aop.config.AopConfigUtils#registerAspectJAnnotationAutoProxyCreatorIfNecessary(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
	 */
	static class CA03_AopConfigUtils extends AopConfigUtils {
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

		public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
				BeanDefinitionRegistry registry, @Nullable Object source) {
			return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
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
