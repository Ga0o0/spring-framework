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
 * Common representation of the current state of a transaction.
 * Serves as base interface for {@link TransactionStatus} as well as
 * {@link ReactiveTransaction}, and as of 6.1 also as transaction
 * representation for {@link TransactionExecutionListener}.
 *
 * @author Juergen Hoeller
 * @since 5.2
 */
// 事务当前状态的通用表示。
// 作为 {@link TransactionStatus} 和 {@link ReactiveTransaction} 的基接口，
// 并且从 6.1 版本开始还作为 {@link TransactionExecutionListener} 的事务表示。
public interface TransactionExecution {

	/**
	 * Return the defined name of the transaction (possibly an empty String).
	 * <p>In case of Spring's declarative transactions, the exposed name will be
	 * the {@code fully-qualified class name + "." + method name} (by default).
	 * <p>The default implementation returns an empty String.
	 * @since 6.1
	 * @see TransactionDefinition#getName()
	 */
	// 返回事务的定义名称（可能为空字符串）。
	// <p>对于 Spring 的声明式事务，默认暴露的名称为 {@code 完全限定类名 + "." + 方法名}。
	// <p>默认实现返回空字符串。
	default String getTransactionName() {
		return "";
	}

	/**
	 * Return whether there is an actual transaction active: this is meant to cover
	 * a new transaction as well as participation in an existing transaction, only
	 * returning {@code false} when not running in an actual transaction at all.
	 * <p>The default implementation returns {@code true}.
	 * @since 6.1
	 * @see #isNewTransaction()
	 * @see #isNested()
	 * @see #isReadOnly()
	 */
	// 返回是否存在实际处于活动状态的事务：
	// 这涵盖新事务以及参与现有事务的情况，仅当未在实际事务中运行时才返回 {@code false}。
	// <p>默认实现返回 {@code true}。
	default boolean hasTransaction() {
		return true;
	}

	/**
	 * Return whether the transaction manager considers the present transaction
	 * as new; otherwise participating in an existing transaction, or potentially
	 * not running in an actual transaction in the first place.
	 * <p>This is primarily here for transaction manager state handling.
	 * Prefer the use of {@link #hasTransaction()} for application purposes
	 * since this is usually semantically appropriate.
	 * <p>The "new" status can be transaction manager specific, e.g. returning
	 * {@code true} for an actual nested transaction but potentially {@code false}
	 * for a savepoint-based nested transaction scope if the savepoint management
	 * is explicitly exposed (such as on {@link TransactionStatus}). A combined
	 * check for any kind of nested execution is provided by {@link #isNested()}.
	 * <p>The default implementation returns {@code true}.
	 * @see #hasTransaction()
	 * @see #isNested()
	 * @see TransactionStatus#hasSavepoint()
	 */
	// 返回事务管理器是否将当前事务视为新事务；否则，则将其视为参与现有事务，或者可能根本不在实际事务中运行。
	// <p>这主要用于事务管理器状态处理。对于应用程序，建议使用 {@link #hasTransaction()}，因为这通常在语义上是合适的。
	// <p>“新”状态可能因事务管理器而异，例如，对于实际嵌套事务，返回 {@code true}；
	// 但如果显式公开了保存点管理（例如在 {@link TransactionStatus} 上），则对于基于保存点的嵌套事务范围，可能返回 {@code false}。
	// {@link #isNested()} 提供了对任何类型嵌套执行的组合检查。
	// <p>默认实现返回 {@code true}。
	default boolean isNewTransaction() {
		return true;
	}

	/**
	 * Return if this transaction executes in a nested fashion within another.
	 * <p>The default implementation returns {@code false}.
	 * @since 6.1
	 * @see #hasTransaction()
	 * @see #isNewTransaction()
	 * @see TransactionDefinition#PROPAGATION_NESTED
	 */
	// 如果此事务以嵌套方式在另一个事务中执行，则返回。
	// <p>默认实现返回 {@code false}。
	default boolean isNested() {
		return false;
	}

	/**
	 * Return if this transaction is defined as read-only transaction.
	 * <p>The default implementation returns {@code false}.
	 * @since 6.1
	 * @see TransactionDefinition#isReadOnly()
	 */
	// 如果此事务定义为只读事务，则返回。
	// <p>默认实现返回 {@code false}。
	default boolean isReadOnly() {
		return false;
	}

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback, as
	 * alternative to throwing an exception which would in turn trigger a rollback.
	 * <p>The default implementation throws an UnsupportedOperationException.
	 * @see #isRollbackOnly()
	 */
	// 将事务设置为仅回滚。这将指示事务管理器，事务的唯一可能结果是回滚，而不是抛出异常（该异常反过来会触发回滚）。
	// <p>默认实现会抛出 UnsupportedOperationException。
	default void setRollbackOnly() {
		throw new UnsupportedOperationException("setRollbackOnly not supported");
	}

	/**
	 * Return whether the transaction has been marked as rollback-only
	 * (either by the application or by the transaction infrastructure).
	 * <p>The default implementation returns {@code false}.
	 * @see #setRollbackOnly()
	 */
	// 返回事务是否已被标记为仅回滚（由应用程序或事务基础结构标记）。
	// <p>默认实现返回 {@code false}。
	default boolean isRollbackOnly() {
		return false;
	}

	/**
	 * Return whether this transaction is completed, that is,
	 * whether it has already been committed or rolled back.
	 * <p>The default implementation returns {@code false}.
	 */
	// 返回此事务是否已完成，即是否已提交或回滚。
	// <p>默认实现返回 {@code false}。
	default boolean isCompleted() {
		return false;
	}

}
