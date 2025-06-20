/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.transaction.jta;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import org.springframework.lang.Nullable;

/**
 * Strategy interface for creating JTA {@link jakarta.transaction.Transaction}
 * objects based on specified transactional characteristics.
 *
 * <p>The default implementation, {@link SimpleTransactionFactory}, simply
 * wraps a standard JTA {@link jakarta.transaction.TransactionManager}.
 * This strategy interface allows for more sophisticated implementations
 * that adapt to vendor-specific JTA extensions.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see jakarta.transaction.TransactionManager#getTransaction()
 * @see SimpleTransactionFactory
 * @see JtaTransactionManager
 */
// 基于指定事务特性创建 JTA {@link jakarta.transaction.Transaction} 对象的策略接口。
//
// <p>默认实现 {@link SimpleTransactionFactory} 仅包装了标准 JTA
// {@link jakarta.transaction.TransactionManager}。此策略接口允许更复杂的实现，以适应特定于供应商的 JTA 扩展。
public interface TransactionFactory {

	/**
	 * Create an active Transaction object based on the given name and timeout.
	 * @param name the transaction name (may be {@code null})
	 * @param timeout the transaction timeout (may be -1 for the default timeout)
	 * @return the active Transaction object (never {@code null})
	 * @throws NotSupportedException if the transaction manager does not support
	 * a transaction of the specified type
	 * @throws SystemException if the transaction manager failed to create the
	 * transaction
	 */
	// 根据给定的名称和超时创建一个活动的事务对象。
	// @param name 事务名称（可能是 {@code null}）
	// @param timeout 事务超时（默认超时可能是 -1）
	// @return 活动事务对象（从不 {@code null}）
	// @throws NotSupportedException 如果事务管理器不支持指定类型的事务
	// @throws SystemException 如果事务管理器无法创建事务
	Transaction createTransaction(@Nullable String name, int timeout) throws NotSupportedException, SystemException;

	/**
	 * Determine whether the underlying transaction manager supports XA transactions
	 * managed by a resource adapter (i.e. without explicit XA resource enlistment).
	 * <p>Typically {@code false}. Checked by
	 * {@link org.springframework.jca.endpoint.AbstractMessageEndpointFactory}
	 * in order to differentiate between invalid configuration and valid
	 * ResourceAdapter-managed transactions.
	 * @see jakarta.resource.spi.ResourceAdapter#endpointActivation
	 * @see jakarta.resource.spi.endpoint.MessageEndpointFactory#isDeliveryTransacted
	 */
	// 确定底层事务管理器是否支持由资源适配器管理的 XA 事务（即无需显式 XA 资源登记）。
	// <p>通常为 {@code false}。由 {@link org.springframework.jca.endpoint.AbstractMessageEndpointFactory}
	// 检查，以区分无效配置和有效的资源适配器管理的事务。
	boolean supportsResourceAdapterManagedTransactions();

}
