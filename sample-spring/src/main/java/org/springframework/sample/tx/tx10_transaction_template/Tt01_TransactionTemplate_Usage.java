package org.springframework.sample.tx.tx10_transaction_template;

import lombok.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionTemplate
 *
 * @see TransactionTemplate
 */
public class Tt01_TransactionTemplate_Usage {
	private static TransactionTemplate transactionTemplate;

	public static void main(String[] args) {
		// 使用 TransactionCallback 来支持不返回值
		transactionTemplate.execute(transactionStatus -> {
			try {
				return doBusinessWithResult(); // 业务代码
			} catch (Exception ex) {
				transactionStatus.setRollbackOnly(); // 这种是在 commit 方法里面处理回滚
				// 记录异常
				ex.printStackTrace();
			}
			return null;
		});

		// 使用 TransactionCallback 来支持不返回值
		transactionTemplate.execute(transactionStatus -> {
			try {
				return doBusinessWithResult(); // 业务代码
			} catch (Exception ex) {
				// 记录异常
				ex.printStackTrace();
				throw ex;  // 这种是在 execute 方法的异常捕获里面处理回滚
			}
		});


		// 使用 TransactionCallbackWithoutResult 来支持不返回值
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(@NonNull TransactionStatus status) {
				try {
					doBusinessWithoutResult(); // 业务代码
				} catch (Exception e) {
					status.setRollbackOnly();
					// 记录异常
					e.printStackTrace();
				}
			}
		});
	}

	private static void doBusinessWithoutResult() {}
	private static <T> T doBusinessWithResult() {
		return null;
	}

}
