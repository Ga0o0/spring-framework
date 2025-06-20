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

import java.io.Flushable;

/**
 * Representation of an ongoing {@link PlatformTransactionManager} transaction.
 * Extends the common {@link TransactionExecution} interface.
 *
 * <p>Transactional code can use this to retrieve status information,
 * and to programmatically request a rollback (instead of throwing
 * an exception that causes an implicit rollback).
 *
 * <p>Includes the {@link SavepointManager} interface to provide access
 * to savepoint management facilities. Note that savepoint management
 * is only available if supported by the underlying transaction manager.
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see #setRollbackOnly()
 * @see PlatformTransactionManager#getTransaction
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus()
 */
// 表示正在进行的 {@link PlatformTransactionManager} 事务。扩展了通用的 {@link TransactionExecution} 接口。
//
// <p>事务代码可以使用它来检索状态信息，并以编程方式请求回滚（而不是抛出导致隐式回滚的异常）。
//
// <p>包含 {@link SavepointManager} 接口，用于访问保存点管理功能。请注意，保存点管理仅在底层事务管理器支持的情况下可用。
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {

	/**
	 * Return whether this transaction internally carries a savepoint,
	 * that is, has been created as nested transaction based on a savepoint.
	 * <p>This method is mainly here for diagnostic purposes, alongside
	 * {@link #isNewTransaction()}. For programmatic handling of custom
	 * savepoints, use the operations provided by {@link SavepointManager}.
	 * <p>The default implementation returns {@code false}.
	 * @see #isNewTransaction()
	 * @see #createSavepoint()
	 * @see #rollbackToSavepoint(Object)
	 * @see #releaseSavepoint(Object)
	 */
	// 返回此事务是否内部带有保存点，即是否已基于保存点创建为嵌套事务。
	// <p>此方法主要用于诊断目的，与 {@link #isNewTransaction()} 一起使用。
	// 如需以编程方式处理自定义保存点，请使用 {@link SavepointManager} 提供的操作。
	// <p>默认实现返回 {@code false}。
	default boolean hasSavepoint() {
		return false;
	}

	/**
	 * Flush the underlying session to the datastore, if applicable:
	 * for example, all affected Hibernate/JPA sessions.
	 * <p>This is effectively just a hint and may be a no-op if the underlying
	 * transaction manager does not have a flush concept. A flush signal may
	 * get applied to the primary resource or to transaction synchronizations,
	 * depending on the underlying resource.
	 * <p>The default implementation is empty, considering flush as a no-op.
	 */
	// 如果适用，请将底层会话刷新到数据存储区：例如，所有受影响的 Hibernate/JPA 会话。
	// <p>这实际上只是一个提示，如果底层事务管理器没有刷新概念，则可能为空操作。
	// 刷新信号可能会应用于主资源或事务同步，具体取决于底层资源。
	// <p>默认实现为空，将刷新视为空操作。
	@Override
	default void flush() {
	}

}
