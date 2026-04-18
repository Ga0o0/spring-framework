/**
 * TransactionStatus
 *
 * @see org.springframework.transaction.TransactionStatus
 * @see org.springframework.transaction.support.AbstractTransactionStatus
 *
 * @see org.springframework.transaction.support.DefaultTransactionStatus
 * @see org.springframework.transaction.support.SimpleTransactionStatus
 */
package org.springframework.sample.tx.tx04_transaction_status;

/**
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#invokeWithinTransaction(java.lang.reflect.Method, java.lang.Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#createTransactionIfNecessary(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String)
 * @see org.springframework.transaction.PlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
 */

/*
// 表示正在进行的 {@link PlatformTransactionManager} 事务。扩展了通用的 {@link TransactionExecution} 接口。
//
// <p>事务代码可以使用它来检索状态信息，并以编程方式请求回滚（而不是抛出导致隐式回滚的异常）。
//
// <p>包含 {@link SavepointManager} 接口，用于访问保存点管理功能。请注意，保存点管理仅在底层事务管理器支持的情况下可用。
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {
	//...
}
**/