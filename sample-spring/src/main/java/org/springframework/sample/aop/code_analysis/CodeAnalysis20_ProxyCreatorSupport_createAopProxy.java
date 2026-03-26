package org.springframework.sample.aop.code_analysis;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AdvisedSupportListener;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.AopProxyFactory;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.aop.framework.ProxyCreatorSupport;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.io.Serial;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * ProxyCreatorSupport 重要方法 - ProxyCreatorSupport#createAopProxy() = ProxyCreatorSupport#getAopProxyFactory() + AopProxyFactory#createAopProx(AdvisedSupport) -> 创建 AOP 代理
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#createAopProxy()
 *
 * ## 1. ProxyCreatorSupport#getAopProxyFactory()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#getAopProxyFactory()
 *
 * @see org.springframework.aop.framework.ProxyCreatorSupport#ProxyCreatorSupport()
 * @see org.springframework.aop.framework.DefaultAopProxyFactory#INSTANCE
 * @see org.springframework.aop.framework.AopProxyFactory
 *
 * ## 2. AopProxyFactory#createAopProx(AdvisedSupport)
 *
 * @see org.springframework.aop.framework.AopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
 * @see org.springframework.aop.framework.DefaultAopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
 *
 * @see org.springframework.aop.framework.JdkDynamicAopProxy
 * @see org.springframework.aop.framework.ObjenesisCglibAopProxy
 */
public class CodeAnalysis20_ProxyCreatorSupport_createAopProxy {

	/**
	 * ProxyCreatorSupport
	 *
	 * @see ProxyCreatorSupport
	 */
	// static class CA01_ProxyCreatorSupport extends ProxyCreatorSupport {
	static class CA01_ProxyCreatorSupport  extends AdvisedSupport {
		@Serial
		private static final long serialVersionUID = 1L;
		private AopProxyFactory aopProxyFactory;
		private final List<AdvisedSupportListener> listeners = new ArrayList<>();
		// 当第一个 AOP 代理创建完成后，设置为 true。
		private boolean active = false;

		public CA01_ProxyCreatorSupport() {
			this.aopProxyFactory = DefaultAopProxyFactory.INSTANCE;
		}

		// 子类应该调用此方法获取新的 AOP 代理。它们<b>不应该</b>使用 {@code this} 作为参数来创建 AOP 代理。
		protected final synchronized AopProxy createAopProxy() {
			if (!this.active) {
				activate();// 激活此代理配置。
			}
			return getAopProxyFactory().createAopProxy(this);
		}

		// 返回此 ProxyConfig 使用的 AopProxyFactory。
		public AopProxyFactory getAopProxyFactory() {
			// see ProxyCreatorSupport.ProxyCreatorSupport() -> DefaultAopProxyFactory.INSTANCE
			return this.aopProxyFactory;
		}

		// 激活此代理配置。
		private void activate() {
			this.active = true;
			for (AdvisedSupportListener listener : this.listeners) {
				listener.activated(this);
			}
		}
	}

	/**
	 * DefaultAopProxyFactory
	 *
	 * @see DefaultAopProxyFactory
	 *
	 * @see org.springframework.aop.framework.AopProxy
	 * @see org.springframework.aop.framework.JdkDynamicAopProxy
	 * @see org.springframework.aop.framework.ObjenesisCglibAopProxy
	 */
	static class CA02_DefaultAopProxyFactory extends DefaultAopProxyFactory {
		private static final long serialVersionUID = 7930414337282325166L;
		public static final DefaultAopProxyFactory INSTANCE = new DefaultAopProxyFactory();

		@Override
		public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
			if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
				Class<?> targetClass = config.getTargetClass(); // TargetSource#getTargetClass()
				if (targetClass == null) {
					throw new AopConfigException("TargetSource cannot determine target class: Either an interface or a target is required for proxy creation.");
				}
				if (targetClass.isInterface() || Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
					// return new JdkDynamicAopProxy(config); // 源码存在
				}
				// return new ObjenesisCglibAopProxy(config); // 源码存在
			}
			else {
				// return new JdkDynamicAopProxy(config); // 源码存在
			}
			return null;
		}

		// 确定所提供的 {@link AdvisedSupport} 是否仅指定了 {@link org.springframework.aop.SpringProxy} 接口（或者根本没有指定代理接口）。
		private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
			Class<?>[] ifcs = config.getProxiedInterfaces();
			return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
		}
	}

/*
// org.springframework.aop.framework.AopProxy
public interface AopProxy {
	// 创建一个新的代理对象。
	Object getProxy();
	// 创建一个新的代理对象。
	Object getProxy(@Nullable ClassLoader classLoader);
	// 确定代理类。
	Class<?> getProxyClass(@Nullable ClassLoader classLoader);
}

// org.springframework.aop.framework.JdkDynamicAopProxy
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable { ... }

// org.springframework.aop.framework.ObjenesisCglibAopProxy
class CglibAopProxy implements AopProxy, Serializable { ... }
class ObjenesisCglibAopProxy extends CglibAopProxy { ... }
**/
}
