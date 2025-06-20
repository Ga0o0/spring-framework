/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.transaction.interceptor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

/**
 * AOP Alliance MethodInterceptor for declarative transaction
 * management using the common Spring transaction infrastructure
 * ({@link org.springframework.transaction.PlatformTransactionManager}/
 * {@link org.springframework.transaction.ReactiveTransactionManager}).
 *
 * <p>Derives from the {@link TransactionAspectSupport} class which
 * contains the integration with Spring's underlying transaction API.
 * TransactionInterceptor simply calls the relevant superclass methods
 * such as {@link #invokeWithinTransaction} in the correct order.
 *
 * <p>TransactionInterceptors are thread-safe.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @see TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.aop.framework.ProxyFactory
 */
// AOP Alliance MethodInterceptor 使用通用 Spring 事务基础架构
// ({@link org.springframework.transaction.PlatformTransactionManager}/
// {@link org.springframework.transaction.ReactiveTransactionManager}) 进行声明式事务管理。
//
// <p>它派生自 {@link TransactionAspectSupport} 类，该类集成了 Spring 底层事务 API。
// TransactionInterceptor 只需按正确顺序调用相关的超类方法，例如 {@link #invokeWithinTransaction}。
//
// <p>TransactionInterceptor 是线程安全的。
@SuppressWarnings("serial")
public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {

	/**
	 * Create a new TransactionInterceptor.
	 * <p>Transaction manager and transaction attributes still need to be set.
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @see #setTransactionAttributeSource(TransactionAttributeSource)
	 */
	// 创建一个新的 TransactionInterceptor。
	// <p>仍然需要设置事务管理器和事务属性。
	public TransactionInterceptor() {
	}

	/**
	 * Create a new TransactionInterceptor.
	 * @param ptm the default transaction manager to perform the actual transaction management
	 * @param tas the attribute source to be used to find transaction attributes
	 * @since 5.2.5
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 */
	// 创建一个新的 TransactionInterceptor。
	// @param ptm 执行实际事务管理的默认事务管理器
	// @param tas 用于查找事务属性的属性源
	public TransactionInterceptor(TransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}

	/**
	 * Create a new TransactionInterceptor.
	 * @param ptm the default transaction manager to perform the actual transaction management
	 * @param tas the attribute source to be used to find transaction attributes
	 * @see #setTransactionManager
	 * @see #setTransactionAttributeSource
	 * @deprecated as of 5.2.5, in favor of
	 * {@link #TransactionInterceptor(TransactionManager, TransactionAttributeSource)}
	 */
	// 创建一个新的 TransactionInterceptor。
	// @param ptm 执行实际事务管理的默认事务管理器
	// @param tas 用于查找事务属性的属性源
	// @see #setTransactionManager
	// @see #setTransactionAttributeSource
	// @deprecated 自 5.2.5 起，支持 {@link #TransactionInterceptor(TransactionManager, TransactionAttributeSource)}
	@Deprecated
	public TransactionInterceptor(PlatformTransactionManager ptm, TransactionAttributeSource tas) {
		setTransactionManager(ptm);
		setTransactionAttributeSource(tas);
	}

	/**
	 * Create a new TransactionInterceptor.
	 * @param ptm the default transaction manager to perform the actual transaction management
	 * @param attributes the transaction attributes in properties format
	 * @see #setTransactionManager
	 * @see #setTransactionAttributes(java.util.Properties)
	 * @deprecated as of 5.2.5, in favor of {@link #setTransactionAttributes(Properties)}
	 */
	// 创建一个新的 TransactionInterceptor。
	// @param ptm 执行实际事务管理的默认事务管理器
	// @param attribute 属性格式的事务属性
	// @see #setTransactionManager
	// @see #setTransactionAttributes(java.util.Properties)
	// @deprecated 自 5.2.5 起，支持 {@link #setTransactionAttributes(Properties)}
	@Deprecated
	public TransactionInterceptor(PlatformTransactionManager ptm, Properties attributes) {
		setTransactionManager(ptm);
		setTransactionAttributes(attributes);
	}


	@Override
	@Nullable
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// Work out the target class: may be {@code null}.
		// The TransactionAttributeSource should be passed the target class
		// as well as the method, which may be from an interface.
		// --> 译文：确定目标类：可能是 {@code null}。TransactionAttributeSource 应该传递目标类以及方法，该方法可能来自接口。
		Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

		// Adapt to TransactionAspectSupport's invokeWithinTransaction... --> 译文：适配 TransactionAspectSupport 的 invokeWithinTransaction...
		return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void writeObject(ObjectOutputStream oos) throws IOException {
		// Rely on default serialization, although this class itself doesn't carry state anyway...
		oos.defaultWriteObject();

		// Deserialize superclass fields.
		oos.writeObject(getTransactionManagerBeanName());
		oos.writeObject(getTransactionManager());
		oos.writeObject(getTransactionAttributeSource());
		oos.writeObject(getBeanFactory());
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization, although this class itself doesn't carry state anyway...
		ois.defaultReadObject();

		// Serialize all relevant superclass fields.
		// Superclass can't implement Serializable because it also serves as base class
		// for AspectJ aspects (which are not allowed to implement Serializable)!
		setTransactionManagerBeanName((String) ois.readObject());
		setTransactionManager((PlatformTransactionManager) ois.readObject());
		setTransactionAttributeSource((TransactionAttributeSource) ois.readObject());
		setBeanFactory((BeanFactory) ois.readObject());
	}

}
