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

import java.io.Flushable;

/**
 * Interface to be implemented by transaction objects that are able to
 * return an internal rollback-only marker, typically from another
 * transaction that has participated and marked it as rollback-only.
 *
 * <p>Autodetected by {@link DefaultTransactionStatus} in order to always
 * return a current rollbackOnly flag even if not resulting from the current
 * TransactionStatus.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see DefaultTransactionStatus#isGlobalRollbackOnly()
 */
// 由能够返回内部仅回滚标记的事务对象实现的接口，通常来自参与并将其标记为仅回滚的另一个事务。
//
// <p>由 {@link DefaultTransactionStatus} 自动检测，以便始终返回当前 rollbackOnly 标志，即使不是由 currentTransactionStatus 产生的。
public interface SmartTransactionObject extends Flushable {

	/**
	 * Return whether the transaction is internally marked as rollback-only.
	 * Can, for example, check the JTA UserTransaction.
	 * <p>The default implementation returns {@code false}.
	 * @see jakarta.transaction.UserTransaction#getStatus
	 * @see jakarta.transaction.Status#STATUS_MARKED_ROLLBACK
	 */
	// 返回事务是否在内部标记为仅回滚。例如，可以检查 JTA UserTransaction。
	// <p>默认实现返回 {@code false}。
	default boolean isRollbackOnly() {
		return false;
	}

	/**
	 * Flush the underlying sessions to the datastore, if applicable:
	 * for example, all affected Hibernate/JPA sessions.
	 * <p>The default implementation is empty, considering flush as a no-op.
	 */
	// 如果适用，将底层会话刷新到数据存储区：例如，所有受影响的 Hibernate/JPA 会话。
	// <p>默认实现为空，将刷新视为无操作。
	@Override
	default void flush() {
	}

}
