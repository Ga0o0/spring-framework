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

package org.springframework.transaction.annotation;

import org.springframework.transaction.TransactionDefinition;

/**
 * Enumeration that represents transaction isolation levels for use with the
 * {@link Transactional @Transactional} annotation, corresponding to the
 * {@link TransactionDefinition} interface.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 */
// 表示与 {@link Transactional @Transactional} 注释一起使用的事务隔离级别的枚举，对应于 {@link TransactionDefinition} 接口。
public enum Isolation {

	/**
	 * Use the default isolation level of the underlying data store.
	 * <p>All other levels correspond to the JDBC isolation levels.
	 * @see java.sql.Connection
	 */
	// 使用底层数据存储的默认隔离级别。
	// <p>所有其他级别均对应于 JDBC 隔离级别。
	DEFAULT(TransactionDefinition.ISOLATION_DEFAULT),

	/**
	 * A constant indicating that dirty reads, non-repeatable reads, and phantom reads
	 * can occur.
	 * <p>This level allows a row changed by one transaction to be read by
	 * another transaction before any changes in that row have been committed
	 * (a "dirty read"). If any of the changes are rolled back, the second
	 * transaction will have retrieved an invalid row.
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 */
	// 一个常量，指示可能发生脏读、不可重复读和幻读。
	// <p>此级别允许一个事务更改的行在该行的任何更改提交之前被另一个事务读取（即“脏读”）。如果任何更改被回滚，则第二个事务将检索到无效行。
	READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED),

	/**
	 * A constant indicating that dirty reads are prevented; non-repeatable reads
	 * and phantom reads can occur.
	 * <p>This level only prohibits a transaction from reading a row with uncommitted
	 * changes in it.
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 */
	// 一个常量，表示阻止脏读；可能会发生不可重复读和幻像读。
	//* <p>此级别仅禁止事务读取包含未提交更改的行。
	READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED),

	/**
	 * A constant indicating that dirty reads and non-repeatable reads are
	 * prevented; phantom reads can occur.
	 * <p>This level prohibits a transaction from reading a row with uncommitted changes
	 * in it, and it also prohibits the situation where one transaction reads a row,
	 * a second transaction alters the row, and the first transaction re-reads the row,
	 * getting different values the second time (a "non-repeatable read").
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 */
	// 一个常量，指示阻止脏读和不可重复读；可能会发生幻读。
	// <p>此级别禁止事务读取包含未提交更改的行，也禁止出现以下情况：
	// 一个事务读取某行，另一个事务修改该行，然后第一个事务重新读取该行，第二次读取时获得不同的值（“不可重复读”）。
	REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ),

	/**
	 * A constant indicating that dirty reads, non-repeatable reads, and phantom
	 * reads are prevented.
	 * <p>This level includes the prohibitions in {@link #REPEATABLE_READ}
	 * and further prohibits the situation where one transaction reads all rows that
	 * satisfy a {@code WHERE} condition, a second transaction inserts a row
	 * that satisfies that {@code WHERE} condition, and the first transaction
	 * re-reads for the same condition, retrieving the additional "phantom" row
	 * in the second read.
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 */
	// 指示阻止脏读、不可重复读和幻读的常量。
	// <p>此级别包含 {@link #REPEATABLE_READ} 中的禁止操作，并进一步禁止以下情况：
	// 一个事务读取满足 {@code WHERE} 条件的所有行，第二个事务插入满足该 {@code WHERE} 条件的行，
	// 然后第一个事务根据相同条件重新读取，并在第二次读取中检索额外的“幻读”行。
	SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE);


	private final int value;


	Isolation(int value) {
		this.value = value;
	}

	public int value() {
		return this.value;
	}

}
