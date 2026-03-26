/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Proxy;

import org.springframework.aop.SpringProxy;
import org.springframework.util.ClassUtils;

/**
 * Default {@link AopProxyFactory} implementation, creating either a CGLIB proxy
 * or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true for a given
 * {@link AdvisedSupport} instance:
 * <ul>
 * <li>the {@code optimize} flag is set
 * <li>the {@code proxyTargetClass} flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>In general, specify {@code proxyTargetClass} to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @author Sam Brannen
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
// 默认的 {@link AopProxyFactory} 实现，创建 CGLIB 代理或 JDK 动态代理。
//
// <p>如果对于给定的 {@link AdvisedSupport} 实例，以下之一成立，则创建 CGLIB 代理：
//
// <ul>
// <li>设置了 {@code Optimize} 标志
// <li>设置了 {@code proxyTargetClass} 标志
// <li>未指定代理接口
// </ul>
//
// <p>通常，指定 {@code proxyTargetClass} 来强制使用 CGLIB 代理，或指定一个或多个接口来使用 JDK 动态代理。
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

	/**
	 * Singleton instance of this class.
	 * @since 6.0.10
	 */
	public static final DefaultAopProxyFactory INSTANCE = new DefaultAopProxyFactory();

	private static final long serialVersionUID = 7930414337282325166L;


	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		// config.isOptimize() 						-> 代理是否应该执行积极优化；ProxyConfig.optimize；默认值为 false
		// config.isProxyTargetClass() 				-> 是否直接代理目标类以及任何接口；即：ProxyConfig#proxyTargetClass；默认值为 false
		// hasNoUserSuppliedProxyInterfaces(config) -> 确定所提供的 AdvisedSupport 是否仅指定了 SpringProxy 接口（或者根本没有指定代理接口）。即：AdvisedSupport#interfaces（代理需要实现的接口）== 0
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass(); // TargetSource#getTargetClass()
			if (targetClass == null) {
				// TargetSource 无法确定目标类：创建代理需要接口或目标。
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			// targetClass.isInterface() 			 -> 此 Class 对象是否表示接口类型。
			// Proxy.isProxyClass(targetClass)  	 -> 如果给定类是代理类，则返回 true。
			// ClassUtils.isLambdaClass(targetClass) -> 判断提供的 Class 是否为 JVM 生成的 Lambda 表达式或方法引用的实现类。
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	// 确定所提供的 {@link AdvisedSupport} 是否仅指定了 {@link org.springframework.aop.SpringProxy} 接口（或者根本没有指定代理接口）。
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
	}

}
