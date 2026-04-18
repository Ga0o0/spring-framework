/**
 * TransactionAttribute
 *
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 *
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */
package org.springframework.sample.tx.tx02_transaction_attribute;

/**
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#determineTransactionAttribute(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 */


/*
// 此接口向 {@link TransactionDefinition} 添加了 {@code rollbackOn} 规范。
// 由于自定义 {@code rollbackOn} 只能通过 AOP 实现，因此它位于 AOP 相关的事务子包中。
public interface TransactionAttribute extends TransactionDefinition {
	// ...
}
**/