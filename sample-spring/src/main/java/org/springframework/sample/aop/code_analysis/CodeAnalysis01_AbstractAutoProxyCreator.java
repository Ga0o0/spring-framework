package org.springframework.sample.aop.code_analysis;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.TargetSourceCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractAutoProxyCreator 重要方法
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getEarlyBeanReference(java.lang.Object, java.lang.String)
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
*/
public class CodeAnalysis01_AbstractAutoProxyCreator {

	/**
	 * AbstractAutoProxyCreator
	 *
	 * @see AbstractAutoProxyCreator
	 */
	static abstract class CA01_AbstractAutoProxyCreator extends AbstractAutoProxyCreator {
		@Serial
		private static final long serialVersionUID = 1L;
		private TargetSourceCreator[] customTargetSourceCreators;
		private BeanFactory beanFactory;
		private final Set<String> targetSourcedBeans = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
		private final Map<Object, Object> earlyBeanReferences = new ConcurrentHashMap<>(16);
		private final Map<Object, Class<?>> proxyTypes = new ConcurrentHashMap<>(16);
		private final Map<Object, Boolean> advisedBeans = new ConcurrentHashMap<>(256);

		/**
		 * AbstractAutoProxyCreator 重要方法 - postProcessBeforeInstantiation(...)
		 *
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessBeforeInstantiation(java.lang.Class, java.lang.String)
		 *
		 * ## 1. 获取自定义的 TargetSourceCreator，并执行 TargetSourceCreator#getTargetSource(...) 方法获取 TargetSource 实例返回
		 *
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getCustomTargetSource(java.lang.Class, java.lang.String)
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#customTargetSourceCreators
		 * @see org.springframework.aop.framework.autoproxy.TargetSourceCreator
		 * @see org.springframework.aop.framework.autoproxy.TargetSourceCreator#getTargetSource(java.lang.Class, java.lang.String)
		 */
		@Override
		public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
			// 为给定的 bean 类和 bean 名称构建缓存键。
			Object cacheKey = getCacheKey(beanClass, beanName);

			if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
				if (this.advisedBeans.containsKey(cacheKey)) {
					return null;
				}
				if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
					this.advisedBeans.put(cacheKey, Boolean.FALSE);
					return null;
				}
			}

			// 如果我们有自定义的 TargetSource，请在此处创建代理。抑制目标 Bean 不必要的默认实例化：TargetSource 将以自定义方式处理目标实例。
			TargetSource targetSource = getCustomTargetSource(beanClass, beanName); // important -> go
			if (targetSource != null) {
				if (StringUtils.hasLength(beanName)) {
					this.targetSourcedBeans.add(beanName);
				}
				// 返回给定 bean 是否需要代理，以及需要应用哪些附加建议（例如 AOP Alliance 拦截器）和 advisors。
				Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
				// 为给定的 bean 创建 AOP 代理。
				Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
				this.proxyTypes.put(cacheKey, proxy.getClass());
				return proxy;
			}

			return null;
		}

		// 为 bean 实例创建目标源。
		@Nullable
		protected TargetSource getCustomTargetSource(Class<?> beanClass, String beanName) {
			// 我们无法为直接注册的单例创建花哨的目标源。
			if (this.customTargetSourceCreators != null &&
					this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
				// org.springframework.aop.framework.autoproxy.TargetSourceCreator
				for (TargetSourceCreator tsc : this.customTargetSourceCreators) {
					TargetSource ts = tsc.getTargetSource(beanClass, beanName);
					if (ts != null) {
						// ...
						return ts;
					}
				}
			}

			// 未找到自定义 TargetSource。
			return null;
		}

		/**
		 * AbstractAutoProxyCreator 重要方法 - getEarlyBeanReference(...)
		 *
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getEarlyBeanReference(java.lang.Object, java.lang.String)
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#wrapIfNecessary(java.lang.Object, java.lang.String, java.lang.Object)
		 * -> AbstractAutoProxyCreator 重要方法 - wrapIfNecessary(...)
		 */
		@Override
		public Object getEarlyBeanReference(Object bean, String beanName) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			this.earlyBeanReferences.put(cacheKey, bean);
			return wrapIfNecessary(bean, beanName, cacheKey); // important -> go
		}

		/**
		 * AbstractAutoProxyCreator 重要方法 - postProcessAfterInitialization(...)
		 *
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization(java.lang.Object, java.lang.String)
		 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#wrapIfNecessary(java.lang.Object, java.lang.String, java.lang.Object)
		 * -> AbstractAutoProxyCreator 重要方法 - wrapIfNecessary(...)
		 */
		// 如果该 bean 被子类标识为代理 bean，则使用配置的拦截器创建代理。
		@Override
		@Nullable
		public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
			if (bean != null) {
				Object cacheKey = getCacheKey(bean.getClass(), beanName);
				if (this.earlyBeanReferences.remove(cacheKey) != bean) {
					return wrapIfNecessary(bean, beanName, cacheKey); // important -> go
				}
			}
			return bean;
		}
	}
}
