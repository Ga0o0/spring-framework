/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition;

/**
 * Enumeration that represents transaction propagation behaviors for use
 * with the {@link Transactional} annotation, corresponding to the
 * {@link TransactionDefinition} interface.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
// 表示与 {@link Transactional} 注释一起使用的事务传播行为的枚举，对应于 {@link TransactionDefinition} 接口。
public enum Propagation {

	/**
	 * Support a current transaction, create a new one if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>This is the default setting of a transaction annotation.
	 */
	// 支持当前事务，如果不存在则创建一个新的。
	// 类似于同名的 EJB 事务属性。
	// <p>这是事务注解的默认设置。
	REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED),

	/**
	 * Support a current transaction, execute non-transactionally if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p>Note: For transaction managers with transaction synchronization,
	 * {@code SUPPORTS} is slightly different from no transaction at all,
	 * as it defines a transaction scope that synchronization will apply for.
	 * As a consequence, the same resources (JDBC Connection, Hibernate Session, etc)
	 * will be shared for the entire specified scope. Note that this depends on
	 * the actual synchronization configuration of the transaction manager.
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setTransactionSynchronization
	 */
	// 支持当前事务，如果不存在则以非事务方式执行。
	// 类似于同名的 EJB 事务属性。
	// <p>注意：对于具有事务同步功能的事务管理器，{@code SUPPORTS} 与完全没有事务略有不同，因为它定义了同步将适用的事务范围。
	// 因此，相同的资源（JDBC 连接、Hibernate 会话等）将在整个指定范围内共享。请注意，这取决于事务管理器的实际同步配置。
	SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS),

	/**
	 * Support a current transaction, throw an exception if none exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	// 支持当前事务，如果不存在则抛出异常。
	// 类似于同名的 EJB 事务属性。
	MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY),

	/**
	 * Create a new transaction, and suspend the current transaction if one exists.
	 * Analogous to the EJB transaction attribute of the same name.
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code jakarta.transaction.TransactionManager} to be
	 * made available to it (which is server-specific in standard Jakarta EE).
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	// 创建新事务，并暂停当前事务（如果存在）。
	// 类似于 EJB 中同名的事务属性。
	// <p><b>注意：</b>实际的事务暂停功能并非在所有事务管理器上都能开箱即用。
	// 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager}，
	// 它需要 {@code jakarta.transaction.TransactionManager} 可用（在标准 Jakarta EE 中，它是特定于服务器的）。
	REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW),

	/**
	 * Execute non-transactionally, suspend the current transaction if one exists.
	 * Analogous to EJB transaction attribute of the same name.
	 * <p><b>NOTE:</b> Actual transaction suspension will not work out-of-the-box
	 * on all transaction managers. This in particular applies to
	 * {@link org.springframework.transaction.jta.JtaTransactionManager},
	 * which requires the {@code jakarta.transaction.TransactionManager} to be
	 * made available to it (which is server-specific in standard Jakarta EE).
	 * @see org.springframework.transaction.jta.JtaTransactionManager#setTransactionManager
	 */
	// 以非事务方式执行，如果存在当前事务，则暂停该事务。
	// 类似于同名的 EJB 事务属性。
	// <p><b>注意：</b>实际的事务暂停功能并非在所有事务管理器上都能开箱即用。
	// 这尤其适用于 {@link org.springframework.transaction.jta.JtaTransactionManager}，
	// 它需要 {@code jakarta.transaction.TransactionManager} 可用（在标准 Jakarta EE 中，它是特定于服务器的）。
	NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED),

	/**
	 * Execute non-transactionally, throw an exception if a transaction exists.
	 * Analogous to EJB transaction attribute of the same name.
	 */
	// 以非事务方式执行，如果存在事务则抛出异常。
	// 类似于同名的 EJB 事务属性。
	NEVER(TransactionDefinition.PROPAGATION_NEVER),

	/**
	 * Execute within a nested transaction if a current transaction exists,
	 * behave like {@code REQUIRED} otherwise. There is no analogous feature in EJB.
	 * <p>Note: Actual creation of a nested transaction will only work on specific
	 * transaction managers. Out of the box, this only applies to the JDBC
	 * DataSourceTransactionManager. Some JTA providers might support nested
	 * transactions as well.
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 */
	// 如果当前事务存在，则在嵌套事务中执行，否则行为类似于 {@code REQUIRED}。
	// EJB 中没有类似的功能。
	// <p>注意：实际创建嵌套事务仅适用于特定的事务管理器。
	// 默认情况下，这仅适用于 JDBC DataSourceTransactionManager。某些 JTA 提供程序可能也支持嵌套事务。
	NESTED(TransactionDefinition.PROPAGATION_NESTED);


	private final int value;


	Propagation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
