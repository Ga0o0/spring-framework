package org.springframework.sample.tx.code_analysis;

import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * TransactionTemplate
 *
 * @see org.springframework.transaction.support.TransactionTemplate
 * @see org.springframework.transaction.support.TransactionTemplate#execute(TransactionCallback)
 */
public class CA20_TransactionTemplate {

	static class CA01_TransactionTemplate extends TransactionTemplate {
		private PlatformTransactionManager transactionManager;

		@Override
		@Nullable
		public <T> T execute(TransactionCallback<T> action) throws TransactionException {
			Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");

			if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager cpptm) {
				return cpptm.execute(this, action);
			}
			else {
				// 根据指定的传播行为返回当前活动的事务或创建一个新事务。
				TransactionStatus status = this.transactionManager.getTransaction(this);
				T result;
				try {
					result = action.doInTransaction(status);
				}
				catch (RuntimeException | Error ex) {
					// Transactional code threw application exception -> rollback --> 译文：事务代码引发应用程序异常 -> 回滚
					rollbackOnException(status, ex);
					throw ex;
				}
				catch (Throwable ex) {
					// Transactional code threw unexpected exception -> rollback
					rollbackOnException(status, ex);
					throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
				}
				// 提交给定的事务
				this.transactionManager.commit(status);
				return result;
			}
		}

		// 执行回滚，并正确处理回滚异常。
		// @param status 表示事务的对象
		// @param ex 抛出的应用程序异常或错误
		// @throws TransactionException 表示发生回滚错误
		private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
			Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");

			logger.debug("Initiating transaction rollback on application exception", ex);
			try {
				this.transactionManager.rollback(status);
			}
			catch (TransactionSystemException ex2) {
				logger.error("Application exception overridden by rollback exception", ex);
				ex2.initApplicationException(ex);
				throw ex2;
			}
			catch (RuntimeException | Error ex2) {
				logger.error("Application exception overridden by rollback exception", ex);
				throw ex2;
			}
		}
	}
}
