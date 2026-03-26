package org.springframework.sample.aop.code_analysis;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.SmartClassLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
public class CodeAnalysis02_AbstractAutoProxyCreator_wrapIfNecessary {

	/**
	 * AbstractAutoProxyCreator
	 *
	 * @see AbstractAutoProxyCreator#wrapIfNecessary(Object, String, Object)
	 */
	static abstract class CA01_AbstractAutoProxyCreator extends AbstractAutoProxyCreator {
		@Serial
		private static final long serialVersionUID = 1L;
		// 指示是否应冻结代理。此设置由父类覆盖，以防止配置过早冻结。
		private boolean freezeProxy = false;
		@Nullable
		private BeanFactory beanFactory;
		private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
		private final Map<Object, Object> earlyBeanReferences = new ConcurrentHashMap<>(16);
		private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);
		private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

		// 如果需要，即如果它符合代理条件，则包装给定的 bean。
		protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
			if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
				return bean;
			}
			if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
				return bean;
			}
			// isInfrastructureClass(...) -> 是否是 不应该被代理的基础结构类
			// shouldSkip(...) -> beanName 是否以 beanClassName 开头，以 “.ORIGINAL“ 结尾
			if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) { // important -> go
				this.advisedBeans.put(cacheKey, Boolean.FALSE);
				return bean;
			}

			// Create proxy if we have advice. --> 译文：如果我们有 advice，请创建代理。
			// 返回需要应用的 advices（例如 AOP Alliance 拦截器）和 advisors
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null); // important -> go
			if (specificInterceptors != DO_NOT_PROXY) {
				this.advisedBeans.put(cacheKey, Boolean.TRUE);
				// 为给定的 bean 创建 AOP 代理。
				Object proxy = createProxy(
						bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean)); // important -> go
				this.proxyTypes.put(cacheKey, proxy.getClass());
				return proxy;
			}

			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// 为给定的 bean 创建 AOP 代理。
		protected Object createProxy(Class<?> beanClass, @Nullable String beanName,
									 @Nullable Object[] specificInterceptors, TargetSource targetSource) {
			return buildProxy(beanClass, beanName, specificInterceptors, targetSource, false);
		}

		private Object buildProxy(Class<?> beanClass, @Nullable String beanName,
								  @Nullable Object[] specificInterceptors, TargetSource targetSource, boolean classOnly) {

			if (this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
				// AutoProxyUtils.exposeTargetClass(clbf, beanName, beanClass);
				CA03_AutoProxyUtils.exposeTargetClass(clbf, beanName, beanClass);
			}

			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.copyFrom(this); // 从其他配置对象复制配置

			if (proxyFactory.isProxyTargetClass()) { // 返回是否直接代理目标类以及任何接口
				//  明确处理 JDK 代理目标和 lambda（用于引入建议场景）
				if (Proxy.isProxyClass(beanClass) || ClassUtils.isLambdaClass(beanClass)) {
					//  必须允许 introductions；不能仅将接口设置为代理的接口。
					for (Class<?> ifc : beanClass.getInterfaces()) {
						proxyFactory.addInterface(ifc);
					}
				}
			}
			else {
				// 没有强制执行 proxyTargetClass 标志，让我们应用默认检查...
				if (shouldProxyTargetClass(beanClass, beanName)) { // 确定给定的 bean 是否应该通过其目标类而不是其接口进行代理
					proxyFactory.setProxyTargetClass(true); // 设置是否直接代理目标类，而不是仅代理特定接口
				}
				else {
					evaluateProxyInterfaces(beanClass, proxyFactory); // 检查给定 bean 类的接口，并在适当的情况下将其应用于 ProxyFactory
				}
			}

			// 确定给定 bean 的 Advisor，包括特定拦截器和通用拦截器，所有拦截器均适配 Advisor 接口。
			Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
			proxyFactory.addAdvisors(advisors);
			proxyFactory.setTargetSource(targetSource);
			customizeProxyFactory(proxyFactory);

			proxyFactory.setFrozen(this.freezeProxy); // 设置此配置是否应冻结
			if (advisorsPreFiltered()) { // Advisor 是否已预先过滤
				proxyFactory.setPreFiltered(true);
			}

			// 如果 Bean 类未在覆盖类加载器中本地加载，则使用原始 ClassLoader
			ClassLoader classLoader = getProxyClassLoader();
			if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != beanClass.getClassLoader()) {
				classLoader = smartClassLoader.getOriginalClassLoader();
			}
			// proxyFactory.getProxyClass(...)	->  根据此工厂中的设置确定代理类
			// proxyFactory.getProxy(...)		->  根据此工厂中的设置创建一个新的代理
			return (classOnly ? proxyFactory.getProxyClass(classLoader) : proxyFactory.getProxy(classLoader)); // important -> go
		}

		// 返回给定 bean 类是否代表不应该被代理的基础结构类。
		// <p>默认实现将 Advice、Advisors 和 AopInfrastructureBeans 视为基础结构类。
		protected boolean isInfrastructureClass(Class<?> beanClass) {
			boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
					Pointcut.class.isAssignableFrom(beanClass) ||
					Advisor.class.isAssignableFrom(beanClass) ||
					AopInfrastructureBean.class.isAssignableFrom(beanClass);
			// ...
			return retVal;
		}

		protected boolean shouldSkip(Class<?> beanClass, String beanName) {
			// return AutoProxyUtils.isOriginalInstance(beanName, beanClass);
			return CA03_AutoProxyUtils.isOriginalInstance(beanName, beanClass);
		}

		// 返回给定 bean 是否需要代理，以及需要应用哪些附加 advices（例如 AOP Alliance 拦截器）和 advisors。
		@Nullable
		protected abstract Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
																 @Nullable TargetSource customTargetSource) throws BeansException;
	}

	/**
	 * @see ProxyFactory
	 */
	static class CA02_ProxyFactory extends ProxyFactory {
		@Serial
		private static final long serialVersionUID = 1L;
		// 根据此工厂中的设置确定代理类。
		public Class<?> getProxyClass(@Nullable ClassLoader classLoader) {
			return createAopProxy().getProxyClass(classLoader); // important -> go
		}

		// 根据此工厂中的设置创建一个新的代理。
		public Object getProxy(@Nullable ClassLoader classLoader) {
			return createAopProxy().getProxy(classLoader); // important -> go
		}
	}

	/**
	 * @see AutoProxyUtils
	 */
	static class CA03_AutoProxyUtils extends AutoProxyUtils {
		// 根据 {@link AutowireCapableBeanFactory#ORIGINAL_INSTANCE_SUFFIX}
		// 判断给定的 bean 名称是否为“原始实例”，并跳过任何代理尝试。
		static boolean isOriginalInstance(String beanName, Class<?> beanClass) {
			if (!StringUtils.hasLength(beanName) || beanName.length() !=
					beanClass.getName().length() + AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX.length()) {
				return false;
			}
			return (beanName.startsWith(beanClass.getName()) &&
					beanName.endsWith(AutowireCapableBeanFactory.ORIGINAL_INSTANCE_SUFFIX));
		}

		// 如果可能，为指定的 bean 公开给定的目标类。
		static void exposeTargetClass(
				ConfigurableListableBeanFactory beanFactory, @Nullable String beanName, Class<?> targetClass) {

			if (beanName != null && beanFactory.containsBeanDefinition(beanName)) {
				beanFactory.getMergedBeanDefinition(beanName).setAttribute(ORIGINAL_TARGET_CLASS_ATTRIBUTE, targetClass);
			}
		}
	}
}
