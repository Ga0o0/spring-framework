/*
 * Copyright 2002-2017 the original author or authors.
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Extension of the {@link org.springframework.transaction.PlatformTransactionManager}
 * interface, exposing a method for executing a given callback within a transaction.
 *
 * <p>Implementors of this interface automatically express a preference for
 * callbacks over programmatic {@code getTransaction}, {@code commit}
 * and {@code rollback} calls. Calling code may check whether a given
 * transaction manager implements this interface to choose to prepare a
 * callback instead of explicit transaction demarcation control.
 *
 * <p>Spring's {@link TransactionTemplate} and
 * {@link org.springframework.transaction.interceptor.TransactionInterceptor}
 * detect and use this PlatformTransactionManager variant automatically.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
// 扩展了 {@link org.springframework.transaction.PlatformTransactionManager} 接口，
// 公开了一个用于在事务中执行指定回调的方法。
//
// <p>此接口的实现者会自动优先使用回调，而不是通过编程方式调用 {@code getTransaction}、{@code commit} 和 {@code rollback}。
// 调用代码可以检查给定的事务管理器是否实现了此接口，并选择准备回调，而不是显式地进行事务划分控制。
//
// <p>Spring 的 {@link TransactionTemplate} 和
// {@link org.springframework.transaction.interceptor.TransactionInterceptor}
// 会自动检测并使用此 PlatformTransactionManager 变体。
public interface CallbackPreferringPlatformTransactionManager extends PlatformTransactionManager {

	/**
	 * Execute the action specified by the given callback object within a transaction.
	 * <p>Allows for returning a result object created within the transaction, that is,
	 * a domain object or a collection of domain objects. A RuntimeException thrown
	 * by the callback is treated as a fatal exception that enforces a rollback.
	 * Such an exception gets propagated to the caller of the template.
	 * @param definition the definition for the transaction to wrap the callback in
	 * @param callback the callback object that specifies the transactional action
	 * @return a result object returned by the callback, or {@code null} if none
	 * @throws TransactionException in case of initialization, rollback, or system errors
	 * @throws RuntimeException if thrown by the TransactionCallback
	 */
	// 在事务中执行给定回调对象指定的操作。
	// <p>允许返回在事务中创建的结果对象，即域对象或域对象集合。
	// 回调抛出的 RuntimeException 被视为强制回滚的致命异常。此类异常将传播给模板的调用者。
	// @param definition 用于包装回调的事务的定义
	// @param callback 指定事务操作的回调对象
	// @return 回调返回的结果对象，如果没有则返回 {@code null}
	// @throws TransactionException（如果发生初始化、回滚或系统错误）
	// @throws RuntimeException（如果由 TransactionCallback 抛出）
	@Nullable
	<T> T execute(@Nullable TransactionDefinition definition, TransactionCallback<T> callback)
			throws TransactionException;

}
