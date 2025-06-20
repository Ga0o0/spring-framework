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

package org.springframework.transaction.support;

import org.springframework.lang.Nullable;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionUsageException;

/**
 * Abstract base implementation of the
 * {@link org.springframework.transaction.TransactionStatus} interface.
 *
 * <p>Pre-implements the handling of local rollback-only and completed flags, and
 * delegation to an underlying {@link org.springframework.transaction.SavepointManager}.
 * Also offers the option of a holding a savepoint within the transaction.
 *
 * <p>Does not assume any specific internal transaction handling, such as an
 * underlying transaction object, and no transaction synchronization mechanism.
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #setRollbackOnly()
 * @see #isRollbackOnly()
 * @see #setCompleted()
 * @see #isCompleted()
 * @see #getSavepointManager()
 * @see SimpleTransactionStatus
 * @see DefaultTransactionStatus
 */
// {@link org.springframework.transaction.TransactionStatus} 接口的抽象基实现。
//
// <p>预实现了本地仅回滚和已完成标志的处理，并委托给底层 {@link org.springframework.transaction.SavepointManager}。同时提供了在事务中保存保存点的选项。
//
// <p>不假设任何特定的内部事务处理，例如底层事务对象，也不假设任何事务同步机制。
public abstract class AbstractTransactionStatus implements TransactionStatus {

	private boolean rollbackOnly = false;

	private boolean completed = false;

	@Nullable
	private Object savepoint;


	//---------------------------------------------------------------------
	// Implementation of TransactionExecution
	//---------------------------------------------------------------------

	@Override
	public void setRollbackOnly() {
		if (this.completed) {
			throw new IllegalStateException("Transaction completed");
		}
		this.rollbackOnly = true;
	}

	/**
	 * Determine the rollback-only flag via checking both the local rollback-only flag
	 * of this TransactionStatus and the global rollback-only flag of the underlying
	 * transaction, if any.
	 * @see #isLocalRollbackOnly()
	 * @see #isGlobalRollbackOnly()
	 */
	@Override
	public boolean isRollbackOnly() {
		return (isLocalRollbackOnly() || isGlobalRollbackOnly());
	}

	/**
	 * Determine the rollback-only flag via checking this TransactionStatus.
	 * <p>Will only return "true" if the application called {@code setRollbackOnly}
	 * on this TransactionStatus object.
	 */
	// 通过检查此 TransactionStatus 来确定仅回滚标志。
	// <p>仅当应用程序在此 TransactionStatus 对象上调用 {@code setRollbackOnly} 时才返回“true”。
	public boolean isLocalRollbackOnly() {
		return this.rollbackOnly;
	}

	/**
	 * Template method for determining the global rollback-only flag of the
	 * underlying transaction, if any.
	 * <p>This implementation always returns {@code false}.
	 */
	public boolean isGlobalRollbackOnly() {
		return false;
	}

	/**
	 * Mark this transaction as completed, that is, committed or rolled back.
	 */
	// 将此事务标记为已完成，即已提交或已回滚。
	public void setCompleted() {
		this.completed = true;
	}

	@Override
	public boolean isCompleted() {
		return this.completed;
	}


	//---------------------------------------------------------------------
	// Handling of current savepoint state
	//---------------------------------------------------------------------

	@Override
	public boolean hasSavepoint() {
		return (this.savepoint != null);
	}

	/**
	 * Set a savepoint for this transaction. Useful for PROPAGATION_NESTED.
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NESTED
	 */
	// 为该事务设置一个保存点。用于 PROPAGATION_NESTED。
	protected void setSavepoint(@Nullable Object savepoint) {
		this.savepoint = savepoint;
	}

	/**
	 * Get the savepoint for this transaction, if any.
	 */
	// 获取此事务的保存点（如果有）。
	@Nullable
	protected Object getSavepoint() {
		return this.savepoint;
	}

	/**
	 * Create a savepoint and hold it for the transaction.
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 */
	// 创建一个保存点并保存到事务中。
	// 如果底层事务不支持保存点，则抛出 org.springframework.transaction.NestedTransactionNotSupportedException
	public void createAndHoldSavepoint() throws TransactionException {
		setSavepoint(getSavepointManager().createSavepoint());
	}

	/**
	 * Roll back to the savepoint that is held for the transaction
	 * and release the savepoint right afterwards.
	 */
	// 回滚到为事务保留的保存点，然后立即释放保存点。
	public void rollbackToHeldSavepoint() throws TransactionException {
		Object savepoint = getSavepoint(); // 获取此事务的保存点（如果有）。
		if (savepoint == null) {
			// 无法回滚到保存点 - 没有与当前事务关联的保存点
			throw new TransactionUsageException(
					"Cannot roll back to savepoint - no savepoint associated with current transaction");
		}
		getSavepointManager().rollbackToSavepoint(savepoint); // 回滚到指定的保存点。
		getSavepointManager().releaseSavepoint(savepoint); // 显式释放指定的保存点。
		setSavepoint(null); // 为该事务设置一个保存点。
	}

	/**
	 * Release the savepoint that is held for the transaction.
	 */
	// 释放为事务保留的保存点。
	public void releaseHeldSavepoint() throws TransactionException {
		Object savepoint = getSavepoint();
		if (savepoint == null) {
			throw new TransactionUsageException(
					"Cannot release savepoint - no savepoint associated with current transaction");
		}
		getSavepointManager().releaseSavepoint(savepoint);
		setSavepoint(null);
	}


	//---------------------------------------------------------------------
	// Implementation of SavepointManager
	//---------------------------------------------------------------------

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * @see #getSavepointManager()
	 * @see SavepointManager#createSavepoint()
	 */
	@Override
	public Object createSavepoint() throws TransactionException {
		return getSavepointManager().createSavepoint();
	}

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * @see #getSavepointManager()
	 * @see SavepointManager#rollbackToSavepoint(Object)
	 */
	// 如果可能的话，此实现将委托给底层事务的 SavepointManager。
	@Override
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().rollbackToSavepoint(savepoint);
	}

	/**
	 * This implementation delegates to a SavepointManager for the
	 * underlying transaction, if possible.
	 * @see #getSavepointManager()
	 * @see SavepointManager#releaseSavepoint(Object)
	 */
	// 如果可能的话，此实现将委托给底层事务的 SavepointManager。
	@Override
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		getSavepointManager().releaseSavepoint(savepoint);
	}

	/**
	 * Return a SavepointManager for the underlying transaction, if possible.
	 * <p>Default implementation always throws a NestedTransactionNotSupportedException.
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support savepoints
	 */
	// 如果可能，返回底层事务的 SavepointManager。
	// <p>默认实现总是抛出 NestedTransactionNotSupportedException。
	// @throws org.springframework.transaction.NestedTransactionNotSupportedException 如果底层事务不支持 Savepoint
	protected SavepointManager getSavepointManager() {
		throw new NestedTransactionNotSupportedException("This transaction does not support savepoints");
	}

}
