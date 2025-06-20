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

package org.springframework.transaction;

import org.springframework.lang.Nullable;

/**
 * Callback interface for stateless listening to transaction creation/completion steps
 * in a transaction manager. This is primarily meant for observation and statistics;
 * consider stateful transaction synchronizations for resource management purposes.
 *
 * <p>In contrast to synchronizations, the transaction execution listener contract is
 * commonly supported for thread-bound transactions as well as reactive transactions.
 * The callback-provided {@link TransactionExecution} object will be either a
 * {@link TransactionStatus} (for a {@link PlatformTransactionManager} transaction) or
 * a {@link ReactiveTransaction} (for a {@link ReactiveTransactionManager} transaction).
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see ConfigurableTransactionManager#addListener
 * @see org.springframework.transaction.support.TransactionSynchronizationManager#registerSynchronization
 * @see org.springframework.transaction.reactive.TransactionSynchronizationManager#registerSynchronization
 */
// 回调接口，用于在事务管理器中无状态地监听事务的创建/完成步骤。
// 这主要用于观察和统计；出于资源管理的目的，可以考虑使用有状态的事务同步。
//
// <p>与同步不同，事务执行监听器契约通常支持线程绑定事务和响应式事务。
// 回调提供的 {@link TransactionExecution} 对象可以是
// {@link TransactionStatus}（对于 {@link PlatformTransactionManager} 事务）或
// {@link ReactiveTransaction}（对于 {@link ReactiveTransactionManager} 事务）。
public interface TransactionExecutionListener {

	/**
	 * Callback before the transaction begin step.
	 * @param transaction the current transaction
	 */
	// 事务开始步骤前的回调。
	// @param transaction 当前事务
	default void beforeBegin(TransactionExecution transaction) {
	}

	/**
	 * Callback after the transaction begin step.
	 * @param transaction the current transaction
	 * @param beginFailure an exception occurring during begin
	 * (or {@code null} after a successful begin step)
	 */
	// 事务开始步骤后的回调。
	// @param transaction 当前事务
	// @param beginFailure 开始过程中发生的异常（或开始步骤成功后返回 {@code null}）
	default void afterBegin(TransactionExecution transaction, @Nullable Throwable beginFailure) {
	}

	/**
	 * Callback before the transaction commit step.
	 * @param transaction the current transaction
	 */
	// 事务提交步骤前的回调。
	// @param transaction 当前事务
	default void beforeCommit(TransactionExecution transaction) {
	}

	/**
	 * Callback after the transaction commit step.
	 * @param transaction the current transaction
	 * @param commitFailure an exception occurring during commit
	 * (or {@code null} after a successful commit step)
	 */
	// 事务提交步骤后的回调。
	// @param transaction 当前事务
	// @param commitFailure 提交过程中发生的异常（或提交成功后返回 null）
	default void afterCommit(TransactionExecution transaction, @Nullable Throwable commitFailure) {
	}

	/**
	 * Callback before the transaction rollback step.
	 * @param transaction the current transaction
	 */
	// 事务回滚步骤之前的回调。
	// @param transaction 当前事务
	default void beforeRollback(TransactionExecution transaction) {
	}

	/**
	 * Callback after the transaction rollback step.
	 * @param transaction the current transaction
	 * @param rollbackFailure an exception occurring during rollback
	 * (or {@code null} after a successful rollback step)
	 */
	// 事务回滚步骤后的回调函数。
	// @param transaction 当前事务
	// @param rollbackFailure 回滚过程中发生的异常（或回滚成功后返回 null）
	default void afterRollback(TransactionExecution transaction, @Nullable Throwable rollbackFailure) {
	}

}
