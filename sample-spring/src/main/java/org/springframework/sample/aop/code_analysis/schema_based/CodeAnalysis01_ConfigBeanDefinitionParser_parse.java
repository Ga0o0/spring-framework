package org.springframework.sample.aop.code_analysis.schema_based;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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
 *
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
 *
 * @see CodeAnalysis02_ConfigBeanDefinitionParser_getAdviceClass
 */
public class CodeAnalysis01_ConfigBeanDefinitionParser_parse {

	/**
	 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parse(Element, ParserContext)
	 */
	public static class CA01_ConfigBeanDefinitionParser implements BeanDefinitionParser {
	// static class CA01_ConfigBeanDefinitionParser extends org.springframework.aop.config.ConfigBeanDefinitionParser {
		private static final String ASPECT = "aspect";
		private static final String POINTCUT = "pointcut";
		private static final String ADVISOR = "advisor";

		@Override
		@Nullable
		public BeanDefinition parse(Element element, ParserContext parserContext) {
			CompositeComponentDefinition compositeDef =
					new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
			parserContext.pushContainingComponent(compositeDef);

			// 1. 注册 AspectJAwareAdvisorAutoProxyCreator 的 BeanDefinition，并获取 <aop:config/> 标签的属性设置给它
			configureAutoProxyCreator(parserContext, element); // important -> go

			// 2. 获取给定 DOM 元素的所有子元素，并进行解析
			List<Element> childElts = DomUtils.getChildElements(element);
			for (Element elt: childElts) {
				String localName = parserContext.getDelegate().getLocalName(elt);
				switch (localName) { // important -> go
					case POINTCUT -> parsePointcut(elt, parserContext); // pointcut -> AspectJExpressionPointcut
					case ADVISOR -> parseAdvisor(elt, parserContext); 	// advisor  -> DefaultBeanFactoryPointcutAdvisor
					case ASPECT -> parseAspect(elt, parserContext);		// aspect	-> AspectJPointcutAdvisor
					// aspect 标签处理中对 advice 的处理：ConfigBeanDefinitionParser.getAdviceClass(...)
				}
			}

			parserContext.popAndRegisterContainingComponent();
			return null;
		}

		/**
		 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#configureAutoProxyCreator(ParserContext, Element)
		 */
		// 配置自动代理创建器，以支持由 <aop:config/> 标签创建的 BeanDefinitions。如果 proxy-target-class 属性设置为 true ，则将强制使用类代理。
		private void configureAutoProxyCreator(ParserContext parserContext, Element element) {
			AopNamespaceUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext, element);
		}

		/**
		 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parsePointcut(Element, ParserContext)
		 */
		// 解析提供的 {@code <pointcut>} 并使用 BeanDefinitionRegistry 注册生成的 Pointcut。
		private AbstractBeanDefinition parsePointcut(Element pointcutElement, ParserContext parserContext) {
			// ...
			return null;
		}

		/**
		 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAdvisor(Element, ParserContext)
		 */
		// 解析提供的 <advisor> 元素并使用提供的 BeanDefinitionRegistry 注册
		// 生成的 org.springframework.aop.Advisor 和任何生成的 org.springframework.aop.Pointcut
		private void parseAdvisor(Element advisorElement, ParserContext parserContext) {
			// ...
		}

		/**
		 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAspect(Element, ParserContext)
		 */
		private void parseAspect(Element aspectElement, ParserContext parserContext) {
			// ...
		}
	}

	/**
	 * AopNamespaceUtils
	 *
	 * @see org.springframework.aop.config.AopNamespaceUtils
	 */
	static class CA02_AopNamespaceUtils extends AopNamespaceUtils {
		// 在 AOP 相关的 XML 标签上发现的 {@code proxy-target-class} 属性。
		public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";
		// 在 AOP 相关的 XML 标签上发现的 {@code reveal-proxy} 属性。
		private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";

		public static void registerAspectJAutoProxyCreatorIfNecessary(
				ParserContext parserContext, Element sourceElement) {

			// 1. 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AspectJAwareAdvisorAutoProxyCreator
			BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(
					parserContext.getRegistry(), parserContext.extractSource(sourceElement)); // important -> go
			// 2. 获取 <aop:config proxy-target-class="" expose-proxy=""/> 标签的属性（expose-proxy 和 proxy-target-class）并设置给名为 org.springframework.aop.config.internalAutoProxyCreator 的实例
			useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
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
	 * AopConfigUtils
	 *
	 * @see org.springframework.aop.config.AopConfigUtils
	 */
	static class CA03_AopConfigUtils extends AopConfigUtils {
		// 内部管理的自动代理创建者的 bean 名称。
		public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
				"org.springframework.aop.config.internalAutoProxyCreator";

		// 按升级顺序存储自动代理创建者类。
		private static final List<Class<?>> APC_PRIORITY_LIST = new ArrayList<>(3);

		static {// important -> go
			// Set up the escalation list...
			// (1) AutoProxyRegistrar(@EnableCaching/@EnableTransactionManagement)
			// (2) Parser(<cache:annotation-driven/>/<tx:annotation-driven/>)
			APC_PRIORITY_LIST.add(InfrastructureAdvisorAutoProxyCreator.class);
			APC_PRIORITY_LIST.add(AspectJAwareAdvisorAutoProxyCreator.class);   	// <aop:config/>
			APC_PRIORITY_LIST.add(AnnotationAwareAspectJAutoProxyCreator.class);	// @EnableAspectJAutoProxy/<aop:aspectj-autoproxy />
		}

		public static BeanDefinition registerAspectJAutoProxyCreatorIfNecessary(
				BeanDefinitionRegistry registry, @Nullable Object source) {
			// 注册 BeanDefinition，如果已存在进行升级处理；名称：org.springframework.aop.config.internalAutoProxyCreator，类型：AnnotationAwareAspectJAutoProxyCreator
			return registerOrEscalateApcAsRequired(AspectJAwareAdvisorAutoProxyCreator.class, registry, source);// important -> go
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
