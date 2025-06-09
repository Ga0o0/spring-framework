/**
 * Package containing Spring's basic AOP infrastructure, compliant with the
 * <a href="http://aopalliance.sourceforge.net">AOP Alliance</a> interfaces.
 *
 * <p>Spring AOP supports proxying interfaces or classes, introductions, and offers
 * static and dynamic pointcuts.
 *
 * <p>Any Spring AOP proxy can be cast to the ProxyConfig AOP configuration interface
 * in this package to add or remove interceptors.
 *
 * <p>The ProxyFactoryBean is a convenient way to create AOP proxies in a BeanFactory
 * or ApplicationContext. However, proxies can be created programmatically using the
 * ProxyFactory class.
 */
// 该软件包包含 Spring 的基本 AOP 基础架构，符合 <a href="http://aopalliance.sourceforge.net">AOP Alliance</a> 接口。
//
// <p>Spring AOP 支持代理接口或类、引入，并提供静态和动态切入点。
//
// <p>任何 Spring AOP 代理都可以转换为此软件包中的 ProxyConfig AOP 配置接口，以添加或删除拦截器。
//
// <p>ProxyFactoryBean 是在 BeanFactory 或 ApplicationContext 中创建 AOP 代理的便捷方法。
// 但是，可以使用 ProxyFactory 类以编程方式创建代理。
@NonNullApi
@NonNullFields
package org.springframework.aop.framework;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
