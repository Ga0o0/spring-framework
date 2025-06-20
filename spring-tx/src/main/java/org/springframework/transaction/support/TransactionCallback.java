/*
 * Copyright 2002-2021 the original author or authors.
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
import org.springframework.transaction.TransactionStatus;

/**
 * Callback interface for transactional code. Used with {@link TransactionTemplate}'s
 * {@code execute} method, often as anonymous class within a method implementation.
 *
 * <p>Typically used to assemble various calls to transaction-unaware data access
 * services into a higher-level service method with transaction demarcation. As an
 * alternative, consider the use of declarative transaction demarcation (e.g. through
 * Spring's {@link org.springframework.transaction.annotation.Transactional} annotation).
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @param <T> the result type
 * @see TransactionTemplate
 * @see CallbackPreferringPlatformTransactionManager
 */
// 事务代码的回调接口。与 {@link TransactionTemplate} 的 {@code execute} 方法一起使用，通常作为方法实现中的匿名类。
//
// <p>通常用于将对事务无关数据访问服务的各种调用组装到具有事务划分的更高级别的服务方法中。
// 或者，也可以考虑使用声明式事务划分（例如，通过 Spring 的 {@link org.springframework.transaction.annotation.Transactional} 注解）。
@FunctionalInterface
public interface TransactionCallback<T> {

	/**
	 * Gets called by {@link TransactionTemplate#execute} within a transactional context.
	 * Does not need to care about transactions itself, although it can retrieve and
	 * influence the status of the current transaction via the given status object,
	 * e.g. setting rollback-only.
	 * <p>Allows for returning a result object created within the transaction, i.e. a
	 * domain object or a collection of domain objects. A RuntimeException thrown by the
	 * callback is treated as application exception that enforces a rollback. Any such
	 * exception will be propagated to the caller of the template, unless there is a
	 * problem rolling back, in which case a TransactionException will be thrown.
	 * @param status associated transaction status
	 * @return a result object, or {@code null}
	 * @see TransactionTemplate#execute
	 * @see CallbackPreferringPlatformTransactionManager#execute
	 */
	// 在事务上下文中由 {@link TransactionTemplate#execute} 调用。
	// 无需关注事务本身，但它可以通过给定的状态对象检索和影响当前事务的状态，例如设置仅回滚。
	// <p>允许返回在事务中创建的结果对象，例如一个域对象或一个域对象集合。
	// 回调抛出的 RuntimeException 被视为强制回滚的应用程序异常。
	// 任何此类异常都将传播给模板的调用者，除非回滚出现问题，在这种情况下将抛出 TransactionException。
	// @param status 关联的事务状态 @return 结果对象，或 {@code null}
	@Nullable
	T doInTransaction(TransactionStatus status);

}
