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

package org.springframework.transaction.support;

import java.util.function.Consumer;

import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Interface specifying basic transaction execution operations.
 * Implemented by {@link TransactionTemplate}. Not often used directly,
 * but a useful option to enhance testability, as it can easily be
 * mocked or stubbed.
 *
 * @author Juergen Hoeller
 * @since 2.0.4
 */
// 指定基本事务执行操作的接口。
// 由 {@link TransactionTemplate} 实现。它不常直接使用，但有助于增强可测试性，因为它易于模拟或存根。
public interface TransactionOperations {

	/**
	 * Execute the action specified by the given callback object within a transaction.
	 * <p>Allows for returning a result object created within the transaction, that is,
	 * a domain object or a collection of domain objects. A RuntimeException thrown
	 * by the callback is treated as a fatal exception that enforces a rollback.
	 * Such an exception gets propagated to the caller of the template.
	 * @param action the callback object that specifies the transactional action
	 * @return a result object returned by the callback, or {@code null} if none
	 * @throws TransactionException in case of initialization, rollback, or system errors
	 * @throws RuntimeException if thrown by the TransactionCallback
	 * @see #executeWithoutResult(Consumer)
	 */
	// 在事务中执行给定回调对象指定的操作。
	// <p>允许返回在事务中创建的结果对象，即一个领域对象或一个领域对象集合。
	// 回调抛出的 RuntimeException 异常将被视为强制回滚的致命异常。此类异常将传播给模板的调用者。
	// @param action 指定事务操作的回调对象
	// @return 回调返回的结果对象，如果没有则返回 {@code null}
	// @throws TransactionException（如果发生初始化、回滚或系统错误）
	// @throws RuntimeException（如果由 TransactionCallback 抛出）
	@Nullable
	<T> T execute(TransactionCallback<T> action) throws TransactionException;

	/**
	 * Execute the action specified by the given {@link Runnable} within a transaction.
	 * <p>If you need to return an object from the callback or access the
	 * {@link org.springframework.transaction.TransactionStatus} from within the callback,
	 * use {@link #execute(TransactionCallback)} instead.
	 * <p>This variant is analogous to using a {@link TransactionCallbackWithoutResult}
	 * but with a simplified signature for common cases - and conveniently usable with
	 * Java 8 lambda expressions.
	 * @param action the Runnable that specifies the transactional action
	 * @throws TransactionException in case of initialization, rollback, or system errors
	 * @throws RuntimeException if thrown by the Runnable
	 * @since 5.2
	 * @see #execute(TransactionCallback)
	 * @see TransactionCallbackWithoutResult
	 */
	// 在事务中执行给定 {@link Runnable} 指定的操作。
	// <p>如果您需要从回调返回对象或在回调中访问 {@link org.springframework.transaction.TransactionStatus}，请改用 {@link #execute(TransactionCallback)}。
	// <p>此变体类似于使用 {@link TransactionCallbackWithoutResult}，但针对常见情况简化了签名 - 并且可方便地与 Java 8 lambda 表达式一起使用。
	// @param action 指定事务操作的 Runnable
	// @throws TransactionException（如果发生初始化、回滚或系统错误）
	// @throws RuntimeException（如果由 Runnable 抛出）
	default void executeWithoutResult(Consumer<TransactionStatus> action) throws TransactionException {
		execute(status -> {
			action.accept(status);
			return null;
		});
	}


	/**
	 * Return an implementation of the {@code TransactionOperations} interface which
	 * executes a given {@link TransactionCallback} without an actual transaction.
	 * <p>Useful for testing: The behavior is equivalent to running with a
	 * transaction manager with no actual transaction (PROPAGATION_SUPPORTS)
	 * and no synchronization (SYNCHRONIZATION_NEVER).
	 * <p>For a {@link TransactionOperations} implementation with actual
	 * transaction processing, use {@link TransactionTemplate} with an appropriate
	 * {@link org.springframework.transaction.PlatformTransactionManager}.
	 * @since 5.2
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS
	 * @see AbstractPlatformTransactionManager#SYNCHRONIZATION_NEVER
	 * @see TransactionTemplate
	 */
	// 返回 {@code TransactionOperations} 接口的实现，该实现执行给定的 {@link TransactionCallback}，而无需实际事务。
	// <p>用于测试：该行为相当于使用没有实际事务（PROPAGATION_SUPPORTS）和没有同步（SYNCHRONIZATION_NEVER）的事务管理器运行。
	// <p>对于具有实际事务处理的 {@link TransactionOperations} 实现，请使用 {@link TransactionTemplate} 和
	// 适当的 {@link org.springframework.transaction.PlatformTransactionManager}。
	static TransactionOperations withoutTransaction() {
		return WithoutTransactionOperations.INSTANCE;
	}

}
