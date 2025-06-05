/**
 * Core Spring AOP interfaces, built on AOP Alliance AOP interoperability interfaces.
 *
 * <p>Any AOP Alliance MethodInterceptor is usable in Spring.
 *
 * <br>Spring AOP also offers:
 * <ul>
 * <li>Introduction support
 * <li>A Pointcut abstraction, supporting "static" pointcuts
 * (class and method-based) and "dynamic" pointcuts (also considering method arguments).
 * There are currently no AOP Alliance interfaces for pointcuts.
 * <li>A full range of advice types, including around, before, after returning and throws advice.
 * <li>Extensibility allowing arbitrary custom advice types to
 * be plugged in without modifying the core framework.
 * </ul>
 *
 * <p>Spring AOP can be used programmatically or (preferably)
 * integrated with the Spring IoC container.
 */
// Spring AOP 核心接口，基于 AOP Alliance AOP 互操作性接口构建。
//
// <p>任何 AOP Alliance MethodInterceptor 都可以在 Spring 中使用。
// <br>Spring AOP 还提供：
// <ul>
// <li>引入支持
// <li>切入点抽象，支持“静态”切入点（基于类和方法）和“动态”切入点（也考虑方法参数）。目前没有 AOP Alliance 提供的切入点接口。
// <li>全面的通知类型，包括 around、before、after 返回和 throws 通知。
// <li>可扩展性，允许在不修改核心框架的情况下插入任意自定义通知类型。
// </ul>
//
// <p>Spring AOP 可以以编程方式使用，或者（最好）与 Spring IoC 容器集成。
@NonNullApi
@NonNullFields
package org.springframework.aop;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
