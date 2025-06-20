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

/**
 * Interface that specifies an API to programmatically manage transaction
 * savepoints in a generic fashion. Extended by TransactionStatus to
 * expose savepoint management functionality for a specific transaction.
 *
 * <p>Note that savepoints can only work within an active transaction.
 * Just use this programmatic savepoint handling for advanced needs;
 * else, a subtransaction with PROPAGATION_NESTED is preferable.
 *
 * <p>This interface is inspired by JDBC's Savepoint mechanism
 * but is independent of any specific persistence technology.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see TransactionStatus
 * @see TransactionDefinition#PROPAGATION_NESTED
 * @see java.sql.Savepoint
 */
// 此接口指定一个 API，用于以通用方式编程管理事务保存点。由 TransactionStatus 扩展，以公开特定事务的保存点管理功能。
//
// <p>请注意，保存点只能在活动事务中工作。此编程式保存点处理仅适用于高级需求；否则，建议使用带有 PROPAGATION_NESTED 的子事务。
//
// <p>此接口受 JDBC 的 Savepoint 机制启发，但独立于任何特定的持久化技术。
public interface SavepointManager {

	/**
	 * Create a new savepoint. You can roll back to a specific savepoint
	 * via {@code rollbackToSavepoint}, and explicitly release a savepoint
	 * that you don't need anymore via {@code releaseSavepoint}.
	 * <p>Note that most transaction managers will automatically release
	 * savepoints at transaction completion.
	 * @return a savepoint object, to be passed into
	 * {@link #rollbackToSavepoint} or {@link #releaseSavepoint}
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the savepoint could not be created,
	 * for example because the transaction is not in an appropriate state
	 * @see java.sql.Connection#setSavepoint
	 */
	// 创建一个新的保存点。
	// 您可以通过 {@code rollbackToSavepoint} 回滚到特定的保存点，并通过 {@code releaseSavepoint} 显式释放不再需要的保存点。
	// <p>请注意，大多数事务管理器会在事务完成时自动释放保存点。
	// @return 一个保存点对象，传递给 {@link #rollbackToSavepoint} 或 {@link #releaseSavepoint}
	// @throws NestedTransactionNotSupportedException 如果底层事务不支持保存点
	// @throws TransactionException 如果无法创建保存点（例如，由于事务状态不正确）
	Object createSavepoint() throws TransactionException;

	/**
	 * Roll back to the given savepoint.
	 * <p>The savepoint will <i>not</i> be automatically released afterwards.
	 * You may explicitly call {@link #releaseSavepoint(Object)} or rely on
	 * automatic release on transaction completion.
	 * @param savepoint the savepoint to roll back to
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the rollback failed
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	// 回滚到指定的保存点。
	// <p>保存点之后不会自动释放。您可以显式调用 {@link #releaseSavepoint(Object)} 或依赖事务完成后的自动释放。
	// @param savepoint 回滚到的保存点。
	// @throws NestedTransactionNotSupportedException 如果底层事务不支持保存点。
	// @throws TransactionException 如果回滚失败。
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	/**
	 * Explicitly release the given savepoint.
	 * <p>Note that most transaction managers will automatically release
	 * savepoints on transaction completion.
	 * <p>Implementations should fail as silently as possible if proper
	 * resource cleanup will eventually happen at transaction completion.
	 * @param savepoint the savepoint to release
	 * @throws NestedTransactionNotSupportedException if the underlying
	 * transaction does not support savepoints
	 * @throws TransactionException if the release failed
	 * @see java.sql.Connection#releaseSavepoint
	 */
	// 显式释放指定的保存点。
	// <p>请注意，大多数事务管理器会在事务完成时自动释放保存点。
	// <p>如果最终会在事务完成时进行适当的资源清理，则实现应尽可能悄无声息地失败。
	// @param savepoint 要释放的保存点
	// @throws NestedTransactionNotSupportedException 如果底层事务不支持保存点
	// @throws TransactionException 如果释放失败
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
