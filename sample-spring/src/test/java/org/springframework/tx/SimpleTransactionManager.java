package org.springframework.tx;

import org.springframework.lang.NonNull;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @see spring-tx/src/test/java/org/springframework/transaction/support/TestTransactionManager.java
 */
public class SimpleTransactionManager extends AbstractPlatformTransactionManager {

    private static final Object TRANSACTION = "transaction";

    private boolean existingTransaction = false;

    private boolean canCreateTransaction = true;

    protected boolean begin = false;

    protected boolean commit = false;

    protected boolean rollback = false;

    protected boolean rollbackOnly = false;

    public SimpleTransactionManager() {
        setTransactionSynchronization(SYNCHRONIZATION_NEVER);
    }

    public SimpleTransactionManager(boolean existingTransaction, boolean canCreateTransaction) {
        this.existingTransaction = existingTransaction;
        this.canCreateTransaction = canCreateTransaction;
        setTransactionSynchronization(SYNCHRONIZATION_NEVER);
    }

    @Override
    @NonNull
    protected Object doGetTransaction() {
        return TRANSACTION;
    }

    @Override
    protected boolean isExistingTransaction(@NonNull Object transaction) {
        return existingTransaction;
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition definition) {
        if (!TRANSACTION.equals(transaction)) {
            throw new IllegalArgumentException("Not the same transaction object");
        }
        if (!this.canCreateTransaction) {
            throw new CannotCreateTransactionException("Cannot create transaction");
        }
        this.begin = true;
		System.out.println("doBegin");
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if (!TRANSACTION.equals(status.getTransaction())) {
            throw new IllegalArgumentException("Not the same transaction object");
        }
        this.commit = true;
		System.out.println("doCommit");
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        if (!TRANSACTION.equals(status.getTransaction())) {
            throw new IllegalArgumentException("Not the same transaction object");
        }
        this.rollback = true;
		System.out.println("doRollback");
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        if (!TRANSACTION.equals(status.getTransaction())) {
            throw new IllegalArgumentException("Not the same transaction object");
        }
        this.rollbackOnly = true;
		System.out.println("doSetRollbackOnly");
    }

}
