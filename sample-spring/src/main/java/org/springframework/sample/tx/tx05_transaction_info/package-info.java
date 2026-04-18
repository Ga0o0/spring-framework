/**
 * TransactionInfo
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo
 */
package org.springframework.sample.tx.tx05_transaction_info;

/**
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#invokeWithinTransaction(java.lang.reflect.Method, java.lang.Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#createTransactionIfNecessary(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#prepareTransactionInfo(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String, org.springframework.transaction.TransactionStatus)
 */

/*
// 用于保存事务信息的不透明对象。子类必须将其传回此类的方法，但无法查看其内部内容。
protected static final class TransactionInfo {
	// ...
}
**/