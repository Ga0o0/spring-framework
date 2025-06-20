/*
 * Copyright 2002-2024 the original author or authors.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.transaction.ConfigurableTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionExecutionListener;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.util.Assert;

/**
 * Abstract base class that implements Spring's standard transaction workflow,
 * serving as basis for concrete platform transaction managers like
 * {@link org.springframework.transaction.jta.JtaTransactionManager}.
 *
 * <p>This base class provides the following workflow handling:
 * <ul>
 * <li>determines if there is an existing transaction;
 * <li>applies the appropriate propagation behavior;
 * <li>suspends and resumes transactions if necessary;
 * <li>checks the rollback-only flag on commit;
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * <li>triggers registered synchronization callbacks
 * (if transaction synchronization is active).
 * </ul>
 *
 * <p>Subclasses have to implement specific template methods for specific
 * states of a transaction, e.g.: begin, suspend, resume, commit, rollback.
 * The most important of them are abstract and must be provided by a concrete
 * implementation; for the rest, defaults are provided, so overriding is optional.
 *
 * <p>Transaction synchronization is a generic mechanism for registering callbacks
 * that get invoked at transaction completion time. This is mainly used internally
 * by the data access support classes for JDBC, Hibernate, JPA, etc when running
 * within a JTA transaction: They register resources that are opened within the
 * transaction for closing at transaction completion time, allowing e.g. for reuse
 * of the same Hibernate Session within the transaction. The same mechanism can
 * also be leveraged for custom synchronization needs in an application.
 *
 * <p>The state of this class is serializable, to allow for serializing the
 * transaction strategy along with proxies that carry a transaction interceptor.
 * It is up to subclasses if they wish to make their state to be serializable too.
 * They should implement the {@code java.io.Serializable} marker interface in
 * that case, and potentially a private {@code readObject()} method (according
 * to Java serialization rules) if they need to restore any transient state.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 28.03.2003
 * @see #setTransactionSynchronization
 * @see TransactionSynchronizationManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
// 实现 Spring 标准事务工作流的抽象基类，可作为具体平台事务管理器（如 {@link org.springframework.transaction.jta.JtaTransactionManager}）的基础。
//
// <p>此基类提供以下工作流处理：
// <ul>
// <li>确定是否存在现有事务；
// <li>应用适当的传播行为；
// <li>如有必要，暂停并恢复事务；
// <li>在提交时检查“仅回滚”标志；
// <li>在回滚时应用适当的修改（实际回滚或设置“仅回滚”）；
// <li>触发已注册的同步回调（如果事务同步处于活动状态）。
// </ul>
//
// <p>子类必须针对事务的特定状态实现特定的模板方法，例如：开始、暂停、恢复、提交、回滚。其中最重要的方法是抽象的，必须由具体实现提供；其余方法均提供默认值，因此覆盖是可选的。
//
// <p>事务同步是一种通用机制，用于注册在事务完成时调用的回调。这主要由 JDBC、Hibernate、JPA 等数据访问支持类在 JTA 事务中运行时在内部使用：
// 它们注册在事务中打开的资源以便在事务完成时关闭，例如允许在事务中重用同一个 Hibernate Session。同样的机制也可用于满足应用程序中的自定义同步需求。
//
// <p>此类的状态是可序列化的，以允许序列化事务策略以及带有事务拦截器的代理。是否让其状态也可序列化取决于子类。
// 在这种情况下，它们应该实现 {@code java.io.Serializable} 标记接口，如果需要恢复任何瞬时状态，
// 则可能还需要实现私有的 {@code readObject()} 方法（根据 Java 序列化规则）。
@SuppressWarnings("serial")
public abstract class AbstractPlatformTransactionManager
		implements PlatformTransactionManager, ConfigurableTransactionManager, Serializable {

	/**
	 * Always activate transaction synchronization, even for "empty" transactions
	 * that result from PROPAGATION_SUPPORTS with no existing backend transaction.
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_SUPPORTS
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NOT_SUPPORTED
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NEVER
	 */
	// 始终激活事务同步，即使对于由 PROPAGATION_SUPPORTS 导致的没有现有后端事务的“空”事务也是如此。
	public static final int SYNCHRONIZATION_ALWAYS = 0;

	/**
	 * Activate transaction synchronization only for actual transactions,
	 * that is, not for empty ones that result from PROPAGATION_SUPPORTS with
	 * no existing backend transaction.
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_MANDATORY
	 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
	 */
	// 仅为实际事务激活事务同步，也就是说，不为由没有现有后端事务的 PROPAGATION_SUPPORTS 产生的空事务激活事务同步。
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;

	/**
	 * Never active transaction synchronization, not even for actual transactions.
	 */
	// 永远不要主动进行交易同步，即使对于实际交易也不行。
	public static final int SYNCHRONIZATION_NEVER = 2;


	/**
	 * Map of constant names to constant values for the transaction synchronization
	 * constants defined in this class.
	 */
	// 此类中定义的事务同步常量的常量名称到常量值的映射。
	static final Map<String, Integer> constants = Map.of(
			"SYNCHRONIZATION_ALWAYS", SYNCHRONIZATION_ALWAYS,
			"SYNCHRONIZATION_ON_ACTUAL_TRANSACTION", SYNCHRONIZATION_ON_ACTUAL_TRANSACTION,
			"SYNCHRONIZATION_NEVER", SYNCHRONIZATION_NEVER
		);


	protected transient Log logger = LogFactory.getLog(getClass());

	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	private int defaultTimeout = TransactionDefinition.TIMEOUT_DEFAULT;

	private boolean nestedTransactionAllowed = false;

	private boolean validateExistingTransaction = false;

	private boolean globalRollbackOnParticipationFailure = true;

	private boolean failEarlyOnGlobalRollbackOnly = false;

	private boolean rollbackOnCommitFailure = false;

	private Collection<TransactionExecutionListener> transactionExecutionListeners = new ArrayList<>();


	/**
	 * Set the transaction synchronization by the name of the corresponding constant
	 * in this class &mdash; for example, {@code "SYNCHRONIZATION_ALWAYS"}.
	 * @param constantName name of the constant
	 * @see #SYNCHRONIZATION_ALWAYS
	 * @see #SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see #SYNCHRONIZATION_NEVER
	 * @see #setTransactionSynchronization(int)
	 */
	public final void setTransactionSynchronizationName(String constantName) {
		Assert.hasText(constantName, "'constantName' must not be null or blank");
		Integer transactionSynchronization = constants.get(constantName);
		Assert.notNull(transactionSynchronization, "Only transaction synchronization constants allowed");
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Set when this transaction manager should activate the thread-bound
	 * transaction synchronization support. Default is "always".
	 * <p>Note that transaction synchronization isn't supported for
	 * multiple concurrent transactions by different transaction managers.
	 * Only one transaction manager is allowed to activate it at any time.
	 * @see #SYNCHRONIZATION_ALWAYS
	 * @see #SYNCHRONIZATION_ON_ACTUAL_TRANSACTION
	 * @see #SYNCHRONIZATION_NEVER
	 * @see TransactionSynchronizationManager
	 * @see TransactionSynchronization
	 */
	public final void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Return if this transaction manager should activate the thread-bound
	 * transaction synchronization support.
	 */
	// 如果此事务管理器应该激活线程绑定事务同步支持，则返回。
	public final int getTransactionSynchronization() {
		return this.transactionSynchronization;
	}

	/**
	 * Specify the default timeout that this transaction manager should apply
	 * if there is no timeout specified at the transaction level, in seconds.
	 * <p>Default is the underlying transaction infrastructure's default timeout,
	 * e.g. typically 30 seconds in case of a JTA provider, indicated by the
	 * {@code TransactionDefinition.TIMEOUT_DEFAULT} value.
	 * @see org.springframework.transaction.TransactionDefinition#TIMEOUT_DEFAULT
	 */
	public final void setDefaultTimeout(int defaultTimeout) {
		if (defaultTimeout < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid default timeout", defaultTimeout);
		}
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Return the default timeout that this transaction manager should apply
	 * if there is no timeout specified at the transaction level, in seconds.
	 * <p>Returns {@code TransactionDefinition.TIMEOUT_DEFAULT} to indicate
	 * the underlying transaction infrastructure's default timeout.
	 */
	public final int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/**
	 * Set whether nested transactions are allowed. Default is "false".
	 * <p>Typically initialized with an appropriate default by the
	 * concrete transaction manager subclass.
	 */
	public final void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	/**
	 * Return whether nested transactions are allowed.
	 */
	// 返回是否允许嵌套事务。
	public final boolean isNestedTransactionAllowed() {
		return this.nestedTransactionAllowed;
	}

	/**
	 * Set whether existing transactions should be validated before participating
	 * in them.
	 * <p>When participating in an existing transaction (e.g. with
	 * PROPAGATION_REQUIRED or PROPAGATION_SUPPORTS encountering an existing
	 * transaction), this outer transaction's characteristics will apply even
	 * to the inner transaction scope. Validation will detect incompatible
	 * isolation level and read-only settings on the inner transaction definition
	 * and reject participation accordingly through throwing a corresponding exception.
	 * <p>Default is "false", leniently ignoring inner transaction settings,
	 * simply overriding them with the outer transaction's characteristics.
	 * Switch this flag to "true" in order to enforce strict validation.
	 * @since 2.5.1
	 */
	public final void setValidateExistingTransaction(boolean validateExistingTransaction) {
		this.validateExistingTransaction = validateExistingTransaction;
	}

	/**
	 * Return whether existing transactions should be validated before participating
	 * in them.
	 * @since 2.5.1
	 */
	// 返回在参与现有事务之前是否应对其进行验证。
	public final boolean isValidateExistingTransaction() {
		return this.validateExistingTransaction;
	}

	/**
	 * Set whether to globally mark an existing transaction as rollback-only
	 * after a participating transaction failed.
	 * <p>Default is "true": If a participating transaction (e.g. with
	 * PROPAGATION_REQUIRED or PROPAGATION_SUPPORTS encountering an existing
	 * transaction) fails, the transaction will be globally marked as rollback-only.
	 * The only possible outcome of such a transaction is a rollback: The
	 * transaction originator <i>cannot</i> make the transaction commit anymore.
	 * <p>Switch this to "false" to let the transaction originator make the rollback
	 * decision. If a participating transaction fails with an exception, the caller
	 * can still decide to continue with a different path within the transaction.
	 * However, note that this will only work as long as all participating resources
	 * are capable of continuing towards a transaction commit even after a data access
	 * failure: This is generally not the case for a Hibernate Session, for example;
	 * neither is it for a sequence of JDBC insert/update/delete operations.
	 * <p><b>Note:</b>This flag only applies to an explicit rollback attempt for a
	 * subtransaction, typically caused by an exception thrown by a data access operation
	 * (where TransactionInterceptor will trigger a {@code PlatformTransactionManager.rollback()}
	 * call according to a rollback rule). If the flag is off, the caller can handle the exception
	 * and decide on a rollback, independent of the rollback rules of the subtransaction.
	 * This flag does, however, <i>not</i> apply to explicit {@code setRollbackOnly}
	 * calls on a {@code TransactionStatus}, which will always cause an eventual
	 * global rollback (as it might not throw an exception after the rollback-only call).
	 * <p>The recommended solution for handling failure of a subtransaction
	 * is a "nested transaction", where the global transaction can be rolled
	 * back to a savepoint taken at the beginning of the subtransaction.
	 * PROPAGATION_NESTED provides exactly those semantics; however, it will
	 * only work when nested transaction support is available. This is the case
	 * with DataSourceTransactionManager, but not with JtaTransactionManager.
	 * @see #setNestedTransactionAllowed
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	public final void setGlobalRollbackOnParticipationFailure(boolean globalRollbackOnParticipationFailure) {
		this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
	}

	/**
	 * Return whether to globally mark an existing transaction as rollback-only
	 * after a participating transaction failed.
	 */
	// 返回参与事务失败后是否全局将现有事务标记为仅回滚。
	public final boolean isGlobalRollbackOnParticipationFailure() {
		return this.globalRollbackOnParticipationFailure;
	}

	/**
	 * Set whether to fail early in case of the transaction being globally marked
	 * as rollback-only.
	 * <p>Default is "false", only causing an UnexpectedRollbackException at the
	 * outermost transaction boundary. Switch this flag on to cause an
	 * UnexpectedRollbackException as early as the global rollback-only marker
	 * has been first detected, even from within an inner transaction boundary.
	 * <p>Note that, as of Spring 2.0, the fail-early behavior for global
	 * rollback-only markers has been unified: All transaction managers will by
	 * default only cause UnexpectedRollbackException at the outermost transaction
	 * boundary. This allows, for example, to continue unit tests even after an
	 * operation failed and the transaction will never be completed. All transaction
	 * managers will only fail earlier if this flag has explicitly been set to "true".
	 * @since 2.0
	 * @see org.springframework.transaction.UnexpectedRollbackException
	 */
	public final void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
		this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * Return whether to fail early in case of the transaction being globally marked
	 * as rollback-only.
	 * @since 2.0
	 */
	// 如果事务被全局标记为仅回滚，则返回是否提前失败。
	public final boolean isFailEarlyOnGlobalRollbackOnly() {
		return this.failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * Set whether {@code doRollback} should be performed on failure of the
	 * {@code doCommit} call. Typically not necessary and thus to be avoided,
	 * as it can potentially override the commit exception with a subsequent
	 * rollback exception.
	 * <p>Default is "false".
	 * @see #doCommit
	 * @see #doRollback
	 */
	public final void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Return whether {@code doRollback} should be performed on failure of the
	 * {@code doCommit} call.
	 */
	// 返回在 {@code doCommit} 调用失败时是否应执行 {@code doRollback}。
	public final boolean isRollbackOnCommitFailure() {
		return this.rollbackOnCommitFailure;
	}

	@Override
	public final void setTransactionExecutionListeners(Collection<TransactionExecutionListener> listeners) {
		this.transactionExecutionListeners = listeners;
	}

	@Override
	public final Collection<TransactionExecutionListener> getTransactionExecutionListeners() {
		return this.transactionExecutionListeners;
	}


	//---------------------------------------------------------------------
	// Implementation of PlatformTransactionManager
	//---------------------------------------------------------------------

	/**
	 * This implementation handles propagation behavior. Delegates to
	 * {@code doGetTransaction}, {@code isExistingTransaction}
	 * and {@code doBegin}.
	 * @see #doGetTransaction
	 * @see #isExistingTransaction
	 * @see #doBegin
	 */ // 此实现处理传播行为。委托给 {@code doGetTransaction}、{@code isExistingTransaction} 和 {@code doBegin}。
	@Override
	public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
			throws TransactionException {

		// Use defaults if no transaction definition given. --> 译文：如果没有给出事务定义，则使用默认值。
		TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());

		Object transaction = doGetTransaction(); // invoke SimpleTransactionManager.doGetTransaction()
		boolean debugEnabled = logger.isDebugEnabled();

		if (isExistingTransaction(transaction)) { // 检查给定的事务对象是否指示现有事务（即已启动的事务）。
			// Existing transaction found -> check propagation behavior to find out how to behave. --> 译文：发现现有事务 -> 检查传播行为以了解行为方式。
			return handleExistingTransaction(def, transaction, debugEnabled);
		}

		// Check definition settings for new transaction. --> 译文：检查新事务的定义设置。
		if (def.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", def.getTimeout()); // 无效事务超时
		}

		// No existing transaction found -> check propagation behavior to find out how to proceed. --> 译文：未找到现有事务 -> 检查传播行为以了解如何继续。
		if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			// 对于标记为传播“强制”的事务，未找到现有事务
			throw new IllegalTransactionStateException(
					"No existing transaction found for transaction marked with propagation 'mandatory'");
		}
		else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			SuspendedResourcesHolder suspendedResources = suspend(null); // 暂停给定的事务。
			if (debugEnabled) {
				logger.debug("Creating new transaction with name [" + def.getName() + "]: " + def);
			}
			try {
				// 开始新的事务。
				return startTransaction(def, transaction, false, debugEnabled, suspendedResources);
			}
			catch (RuntimeException | Error ex) {
				resume(null, suspendedResources);
				throw ex;
			}
		}
		else {
			// Create "empty" transaction: no actual transaction, but potentially synchronization. --> 译文：创建 “空” 事务：没有实际事务，但可能同步。
			if (def.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
				// 指定了自定义隔离级别，但没有启动实际事务；隔离级别将被有效忽略：
				logger.warn("Custom isolation level specified but no actual transaction initiated; " +
						"isolation level will effectively be ignored: " + def);
			}
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			// 为给定的参数创建一个新的 TransactionStatus，并根据需要初始化事务同步。
			return prepareTransactionStatus(def, null, true, newSynchronization, debugEnabled, null);
		}
	}

	/**
	 * Create a TransactionStatus for an existing transaction.
	 */
	// 为现有交易创建 TransactionStatus。
	private TransactionStatus handleExistingTransaction(
			TransactionDefinition definition, Object transaction, boolean debugEnabled)
			throws TransactionException {

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			// 已找到标记为传播 “never” 的事务的现有事务
			throw new IllegalTransactionStateException(
					"Existing transaction found for transaction marked with propagation 'never'");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction"); // 暂停当前事务
			}
			Object suspendedResources = suspend(transaction); // 暂停给定的事务。
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			// 为给定的参数创建一个新的 TransactionStatus，并根据需要初始化事务同步。
			return prepareTransactionStatus(
					definition, null, false, newSynchronization, debugEnabled, suspendedResources);
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction, creating new transaction with name [" +
						definition.getName() + "]"); // 暂停当前事务，创建名为 ["definition.getName()"] 的新事务
			}
			SuspendedResourcesHolder suspendedResources = suspend(transaction); // 暂停给定的事务。
			try {
				// 开始新的事务。
				return startTransaction(definition, transaction, false, debugEnabled, suspendedResources);
			}
			catch (RuntimeException | Error beginEx) {
				// 内部事务开始失败后恢复外部事务。
				resumeAfterBeginException(transaction, suspendedResources, beginEx);
				throw beginEx;
			}
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (!isNestedTransactionAllowed()) { // 是否允许嵌套事务
				// 事务管理器默认不允许嵌套事务 - 将 “nestedTransactionAllowed” 属性指定为值 “true”
				throw new NestedTransactionNotSupportedException(
						"Transaction manager does not allow nested transactions by default - " +
						"specify 'nestedTransactionAllowed' property with value 'true'");
			}
			if (debugEnabled) {
				// 创建名称为 ["definition.getName()"] 的嵌套事务
				logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
			}
			if (useSavepointForNestedTransaction()) { // 是否在嵌套事务中使用保存点
				// Create savepoint within existing Spring-managed transaction,
				// through the SavepointManager API implemented by TransactionStatus.
				// Usually uses JDBC savepoints. Never activates Spring synchronization.
				// --> 译文：通过 TransactionStatus 实现的 SavepointManager API，在现有的 Spring 管理事务中创建保存点。通常使用 JDBC 保存点。从不激活 Spring 同步。
				DefaultTransactionStatus status = newTransactionStatus(
						definition, transaction, false, false, true, debugEnabled, null);
				// invoke TransactionExecutionListener#beforeBegin()
				this.transactionExecutionListeners.forEach(listener -> listener.beforeBegin(status));
				try {
					status.createAndHoldSavepoint(); // 创建一个保存点并保存到事务中。
				}
				catch (RuntimeException | Error ex) {
					// invoke TransactionExecutionListener#afterBegin()
					this.transactionExecutionListeners.forEach(listener -> listener.afterBegin(status, ex));
					throw ex;
				}
				// invoke TransactionExecutionListener#afterBegin()
				this.transactionExecutionListeners.forEach(listener -> listener.afterBegin(status, null));
				return status;
			}
			else {
				// Nested transaction through nested begin and commit/rollback calls.
				// Usually only for JTA: Spring synchronization might get activated here
				// in case of a pre-existing JTA transaction.
				// --> 译文：通过嵌套的 begin 和 commit/rollback 调用实现嵌套事务。通常仅适用于 JTA：如果已存在 JTA 事务，则可能会在此处激活 Spring 同步。
				return startTransaction(definition, transaction, true, debugEnabled, null);
			}
		}

		// PROPAGATION_REQUIRED, PROPAGATION_SUPPORTS, PROPAGATION_MANDATORY:
		// regular participation in existing transaction.
		// --> 译文：PROPAGATION_REQUIRED、PROPAGATION_SUPPORTS、PROPAGATION_MANDATORY：参与现有事务的常规操作。
		if (debugEnabled) {
			logger.debug("Participating in existing transaction");
		}
		if (isValidateExistingTransaction()) {
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				// 返回当前事务的隔离级别（如果有）。
				Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
					throw new IllegalTransactionStateException("Participating transaction with definition [" +
							definition + "] specifies isolation level which is incompatible with existing transaction: " +
							(currentIsolationLevel != null ?
									DefaultTransactionDefinition.getIsolationLevelName(currentIsolationLevel) :
									"(unknown)"));
				}
			}
			if (!definition.isReadOnly()) {
				if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
					throw new IllegalTransactionStateException("Participating transaction with definition [" +
							definition + "] is not marked as read-only but existing transaction is");
				}
			}
		}
		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		// 为给定的参数创建一个新的 TransactionStatus，并根据需要初始化事务同步。
		return prepareTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
	}

	/**
	 * Start a new transaction.
	 */
	// 开始新的事务。
	private TransactionStatus startTransaction(TransactionDefinition definition, Object transaction,
			boolean nested, boolean debugEnabled, @Nullable SuspendedResourcesHolder suspendedResources) {

		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		// 为给定的参数创建一个 TransactionStatus 实例。
		DefaultTransactionStatus status = newTransactionStatus(
				definition, transaction, true, newSynchronization, nested, debugEnabled, suspendedResources);
		// invoke TransactionExecutionListener#beforeBegin()
		this.transactionExecutionListeners.forEach(listener -> listener.beforeBegin(status));
		try {
			// 根据给定的事务定义，以语义开启一个新事务。
			doBegin(transaction, definition);
		}
		catch (RuntimeException | Error ex) {
			// invoke TransactionExecutionListener#afterBegin()
			this.transactionExecutionListeners.forEach(listener -> listener.afterBegin(status, ex));
			throw ex;
		}
		// 根据需要初始化事务同步。
		prepareSynchronization(status, definition);
		// invoke TransactionExecutionListener#afterBegin()
		this.transactionExecutionListeners.forEach(listener -> listener.afterBegin(status, null));
		return status;
	}

	/**
	 * Create a new TransactionStatus for the given arguments,
	 * also initializing transaction synchronization as appropriate.
	 * @see #newTransactionStatus
	 * @see #prepareTransactionStatus
	 */
	// 为给定的参数创建一个新的 TransactionStatus，并根据需要初始化事务同步。
	private DefaultTransactionStatus prepareTransactionStatus(
			TransactionDefinition definition, @Nullable Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean debug, @Nullable Object suspendedResources) {

		DefaultTransactionStatus status = newTransactionStatus(
				definition, transaction, newTransaction, newSynchronization, false, debug, suspendedResources);
		// 根据需要初始化事务同步。
		prepareSynchronization(status, definition);
		return status;
	}

	/**
	 * Create a TransactionStatus instance for the given arguments.
	 */
	// 为给定的参数创建一个 TransactionStatus 实例。
	private DefaultTransactionStatus newTransactionStatus(
			TransactionDefinition definition, @Nullable Object transaction, boolean newTransaction,
			boolean newSynchronization, boolean nested, boolean debug, @Nullable Object suspendedResources) {

		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		return new DefaultTransactionStatus(definition.getName(), transaction, newTransaction,
				actualNewSynchronization, nested, definition.isReadOnly(), debug, suspendedResources);
	}

	/**
	 * Initialize transaction synchronization as appropriate.
	 */
	// 根据需要初始化事务同步。
	protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
		if (status.isNewSynchronization()) {
			// 公开当前是否存在实际活动事务。由事务管理器在事务开始和清理时调用。
			TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
			// 公开当前事务的隔离级别。由事务管理器在事务开始和清理时调用。
			TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
					definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?
							definition.getIsolationLevel() : null);
			// 为当前事务公开只读标志。由事务管理器在事务开始和清理时调用。
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			// 公开当前事务的名称（如果有）。由事务管理器在事务开始和清理时调用。
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			// 激活当前线程的事务同步。由事务管理器在事务开始时调用。
			TransactionSynchronizationManager.initSynchronization();
		}
	}

	/**
	 * Determine the actual timeout to use for the given definition.
	 * Will fall back to this manager's default timeout if the
	 * transaction definition doesn't specify a non-default value.
	 * @param definition the transaction definition
	 * @return the actual timeout to use
	 * @see org.springframework.transaction.TransactionDefinition#getTimeout()
	 * @see #setDefaultTimeout
	 */
	protected int determineTimeout(TransactionDefinition definition) {
		if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
			return definition.getTimeout();
		}
		return getDefaultTimeout();
	}


	/**
	 * Suspend the given transaction. Suspends transaction synchronization first,
	 * then delegates to the {@code doSuspend} template method.
	 * @param transaction the current transaction object
	 * (or {@code null} to just suspend active synchronizations, if any)
	 * @return an object that holds suspended resources
	 * (or {@code null} if neither transaction nor synchronization active)
	 * @see #doSuspend
	 * @see #resume
	 */
	// 暂停给定的事务。首先暂停事务同步，然后委托给 {@code doSuspend} 模板方法。
	// @param transaction 当前事务对象（或 {@code null} 表示仅暂停活动同步，如果有）
	// @return 保存已暂停资源的对象（或 {@code null} 表示事务和同步均未处于活动状态）
	@Nullable
	protected final SuspendedResourcesHolder suspend(@Nullable Object transaction) throws TransactionException {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			// 暂停所有当前同步并停用当前线程的事务同步。
			List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
			try {
				Object suspendedResources = null;
				if (transaction != null) {
					// 暂停当前事务的资源。事务同步将已被暂停。
					suspendedResources = doSuspend(transaction);
				}
				// 返回当前事务的名称，如果未设置则返回 null。
				String name = TransactionSynchronizationManager.getCurrentTransactionName();
				TransactionSynchronizationManager.setCurrentTransactionName(null);
				// 返回当前事务是否被标记为只读。
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
				// 返回当前事务的隔离级别（如果有）。
				Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
				// 返回当前是否存在实际活动事务。
				boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
				TransactionSynchronizationManager.setActualTransactionActive(false);
				// 暂停资源的持有者。
				return new SuspendedResourcesHolder(
						suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);
			}
			catch (RuntimeException | Error ex) {
				// doSuspend failed - original transaction is still active... --> 译文：doSuspend 失败 - 原始事务仍然处于活动状态......
				doResumeSynchronization(suspendedSynchronizations); // 重新激活当前线程的事务同步并恢复所有给定的同步。
				throw ex;
			}
		}
		else if (transaction != null) {
			// Transaction active but no synchronization active. --> 译文：事务处于活动状态，但没有同步处于活动状态。
			Object suspendedResources = doSuspend(transaction); // 暂停当前事务的资源。
			return new SuspendedResourcesHolder(suspendedResources);
		}
		else {
			// Neither transaction nor synchronization active. --> 译文：事务和同步均未处于活动状态。
			return null;
		}
	}

	/**
	 * Resume the given transaction. Delegates to the {@code doResume}
	 * template method first, then resuming transaction synchronization.
	 * @param transaction the current transaction object
	 * @param resourcesHolder the object that holds suspended resources,
	 * as returned by {@code suspend} (or {@code null} to just
	 * resume synchronizations, if any)
	 * @see #doResume
	 * @see #suspend
	 */
	// 恢复给定的事务。首先委托给 {@code doResume} 模板方法，然后恢复事务同步。
	// @param transaction 当前事务对象
	// @param resourcesHolder 持有已暂停资源的对象，由 {@code suspend} 返回（或 {@code null} 表示仅恢复同步，如果有）
	protected final void resume(@Nullable Object transaction, @Nullable SuspendedResourcesHolder resourcesHolder)
			throws TransactionException {

		if (resourcesHolder != null) {
			Object suspendedResources = resourcesHolder.suspendedResources;
			if (suspendedResources != null) {
				doResume(transaction, suspendedResources);
			}
			List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
			if (suspendedSynchronizations != null) {
				TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
				TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.name);
				doResumeSynchronization(suspendedSynchronizations);
			}
		}
	}

	/**
	 * Resume outer transaction after inner transaction begin failed.
	 */
	// 内部事务开始失败后恢复外部事务。
	private void resumeAfterBeginException(
			Object transaction, @Nullable SuspendedResourcesHolder suspendedResources, Throwable beginEx) {

		try {
			resume(transaction, suspendedResources); // 恢复给定的事务。
		}
		catch (RuntimeException | Error resumeEx) {
			String exMessage = "Inner transaction begin exception overridden by outer transaction resume exception"; // 内部事务开始异常被外部事务恢复异常覆盖
			logger.error(exMessage, beginEx);
			throw resumeEx;
		}
	}

	/**
	 * Suspend all current synchronizations and deactivate transaction
	 * synchronization for the current thread.
	 * @return the List of suspended TransactionSynchronization objects
	 */
	// 暂停所有当前同步并停用当前线程的事务同步。
	// @return 已暂停的 TransactionSynchronization 对象列表
	private List<TransactionSynchronization> doSuspendSynchronization() {
		// 返回当前线程所有已注册同步的不可修改快照列表。
		List<TransactionSynchronization> suspendedSynchronizations =
				TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.suspend(); // 暂停此同步。
		}
		// 停用当前线程的事务同步。
		TransactionSynchronizationManager.clearSynchronization();
		return suspendedSynchronizations;
	}

	/**
	 * Reactivate transaction synchronization for the current thread
	 * and resume all given synchronizations.
	 * @param suspendedSynchronizations a List of TransactionSynchronization objects
	 */
	// 重新激活当前线程的事务同步并恢复所有给定的同步。
	// @param suspensionSynchronizations TransactionSynchronization 对象列表
	private void doResumeSynchronization(List<TransactionSynchronization> suspendedSynchronizations) {
		TransactionSynchronizationManager.initSynchronization(); // 激活当前线程的事务同步。
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.resume(); // 恢复此同步。
			// 为当前线程注册一个新的事务同步。
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}
	}


	/**
	 * This implementation of commit handles participating in existing
	 * transactions and programmatic rollback requests.
	 * Delegates to {@code isRollbackOnly}, {@code doCommit}
	 * and {@code rollback}.
	 * @see org.springframework.transaction.TransactionStatus#isRollbackOnly()
	 * @see #doCommit
	 * @see #rollback
	 */
	// 此提交实现处理参与现有事务和程序化回滚请求。委托给 {@code isRollbackOnly}、{@code doCommit} 和 {@code rollback}。
	@Override
	public final void commit(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			// 事务已完成 - 每个事务不要调用提交或回滚超过一次
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		if (defStatus.isLocalRollbackOnly()) { // 通过检查此 TransactionStatus 来确定仅回滚标志。
			if (defStatus.isDebug()) {
				// 事务代码已请求回滚
				logger.debug("Transactional code has requested rollback");
			}
			processRollback(defStatus, false);
			return;
		}

		if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
			}
			processRollback(defStatus, true);
			return;
		}

		// 处理实际提交。已检查并应用 “仅回滚” 标志。
		processCommit(defStatus);
	}

	/**
	 * Process an actual commit.
	 * Rollback-only flags have already been checked and applied.
	 * @param status object representing the transaction
	 * @throws TransactionException in case of commit failure
	 */
	// 处理实际提交。已检查并应用 “仅回滚” 标志。
	// @param status 表示事务的对象
	// @throws TransactionException 表示提交失败
	private void processCommit(DefaultTransactionStatus status) throws TransactionException {
		try {
			boolean beforeCompletionInvoked = false;
			boolean commitListenerInvoked = false;

			try {
				boolean unexpectedRollback = false;
				prepareForCommit(status);
				triggerBeforeCommit(status);
				triggerBeforeCompletion(status);
				beforeCompletionInvoked = true;

				if (status.hasSavepoint()) {
					if (status.isDebug()) {
						logger.debug("Releasing transaction savepoint");
					}
					unexpectedRollback = status.isGlobalRollbackOnly();
					this.transactionExecutionListeners.forEach(listener -> listener.beforeCommit(status));
					commitListenerInvoked = true;
					status.releaseHeldSavepoint();
				}
				else if (status.isNewTransaction()) {
					if (status.isDebug()) {
						logger.debug("Initiating transaction commit");
					}
					unexpectedRollback = status.isGlobalRollbackOnly();
					this.transactionExecutionListeners.forEach(listener -> listener.beforeCommit(status));
					commitListenerInvoked = true;
					doCommit(status);
				}
				else if (isFailEarlyOnGlobalRollbackOnly()) {
					unexpectedRollback = status.isGlobalRollbackOnly();
				}

				// Throw UnexpectedRollbackException if we have a global rollback-only
				// marker but still didn't get a corresponding exception from commit.
				// --> 译文：如果我们有一个全局回滚标记但仍然没有从提交中获得相应的异常，则抛出 UnexpectedRollbackException。
				if (unexpectedRollback) {
					// 事务静默回滚，因为它已被标记为仅回滚
					throw new UnexpectedRollbackException(
							"Transaction silently rolled back because it has been marked as rollback-only");
				}
			}
			catch (UnexpectedRollbackException ex) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
				this.transactionExecutionListeners.forEach(listener -> listener.afterRollback(status, null));
				throw ex;
			}
			catch (TransactionException ex) {
				if (isRollbackOnCommitFailure()) {
					doRollbackOnCommitException(status, ex);
				}
				else {
					triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
					if (commitListenerInvoked) {
						this.transactionExecutionListeners.forEach(listener -> listener.afterCommit(status, ex));
					}
				}
				throw ex;
			}
			catch (RuntimeException | Error ex) {
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(status);
				}
				doRollbackOnCommitException(status, ex);
				throw ex;
			}

			// Trigger afterCommit callbacks, with an exception thrown there
			// propagated to callers but the transaction still considered as committed.
			// --> 译文：触发 afterCommit 回调，抛出的异常会传播给调用者，但事务仍被视为已提交。
			try {
				triggerAfterCommit(status);
			}
			finally {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
				if (commitListenerInvoked) {
					this.transactionExecutionListeners.forEach(listener -> listener.afterCommit(status, null));
				}
			}

		}
		finally {
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * This implementation of rollback handles participating in existing
	 * transactions. Delegates to {@code doRollback} and
	 * {@code doSetRollbackOnly}.
	 * @see #doRollback
	 * @see #doSetRollbackOnly
	 */
	// 此回滚实现处理参与现有事务。委托给 {@code doRollback} 和 {@code doSetRollbackOnly}。
	@Override
	public final void rollback(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) { // 返回此事务是否已完成，即是否已提交或回滚。
			throw new IllegalTransactionStateException( // 事务已完成 - 每个事务不要调用提交或回滚超过一次
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}

		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
		processRollback(defStatus, false);
	}

	/**
	 * Process an actual rollback.
	 * The completed flag has already been checked.
	 * @param status object representing the transaction
	 * @throws TransactionException in case of rollback failure
	 */
	// 处理实际回滚。已检查完成标志。
	// @param status 表示事务的对象
	// @throws TransactionException 表示回滚失败
	private void processRollback(DefaultTransactionStatus status, boolean unexpected) {
		try {
			boolean unexpectedRollback = unexpected;
			boolean rollbackListenerInvoked = false;

			try {
				// trigger TransactionSynchronization#beforeCompletion()
				triggerBeforeCompletion(status);

				if (status.hasSavepoint()) { // 是否有保存点
					if (status.isDebug()) {
						logger.debug("Rolling back transaction to savepoint"); // 将事务回滚到保存点
					}
					// trigger TransactionExecutionListener#beforeRollback()
					this.transactionExecutionListeners.forEach(listener -> listener.beforeRollback(status));
					rollbackListenerInvoked = true;
					// 回滚到为事务保留的保存点，然后立即释放保存点。
					status.rollbackToHeldSavepoint();
				}
				else if (status.isNewTransaction()) { // 是否有新事务
					if (status.isDebug()) {
						logger.debug("Initiating transaction rollback"); // 启动事务回滚
					}
					// trigger TransactionExecutionListener#beforeRollback()
					this.transactionExecutionListeners.forEach(listener -> listener.beforeRollback(status));
					rollbackListenerInvoked = true;
					// 对给定的事务执行实际回滚。
					doRollback(status);
				}
				else {
					// Participating in larger transaction --> 译文：参与更大的事务
					if (status.hasTransaction()) { // 是否有事务
						if (status.isLocalRollbackOnly() || isGlobalRollbackOnParticipationFailure()) {
							if (status.isDebug()) {
								// 参与事务失败 - 将现有事务标记为仅回滚
								logger.debug("Participating transaction failed - marking existing transaction as rollback-only");
							}
							// 将给定事务设置为仅回滚。
							doSetRollbackOnly(status);
						}
						else {
							if (status.isDebug()) {
								// 参与事务失败 - 让事务发起者决定是否回滚
								logger.debug("Participating transaction failed - letting transaction originator decide on rollback");
							}
						}
					}
					else {
						// 应该回滚事务但不能 - 没有可用的事务
						logger.debug("Should roll back transaction but cannot - no transaction available");
					}
					// Unexpected rollback only matters here if we're asked to fail early --> 译文：如果我们被要求提前失败，意外回滚才会出现
					if (!isFailEarlyOnGlobalRollbackOnly()) {
						unexpectedRollback = false;
					}
				}
			}
			catch (RuntimeException | Error ex) {
				// 触发 afterCompletion 回调。
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				if (rollbackListenerInvoked) {
					// trigger TransactionExecutionListener#afterRollback()
					this.transactionExecutionListeners.forEach(listener -> listener.afterRollback(status, ex));
				}
				throw ex;
			}

			// 触发 afterCompletion 回调。
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
			if (rollbackListenerInvoked) {
				// trigger TransactionExecutionListener#afterRollback()
				this.transactionExecutionListeners.forEach(listener -> listener.afterRollback(status, null));
			}

			// Raise UnexpectedRollbackException if we had a global rollback-only marker --> 译文：如果我们有一个全局回滚标记，则引发 UnexpectedRollbackException
			if (unexpectedRollback) {
				throw new UnexpectedRollbackException( // 事务已回滚，因为它已被标记为仅回滚
						"Transaction rolled back because it has been marked as rollback-only");
			}
		}
		finally {
			// 完成后进行清理，必要时清除同步，并调用 doCleanupAfterCompletion。
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * Invoke {@code doRollback}, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of rollback failure
	 * @see #doRollback
	 */
	// 调用 {@code doRollback}，正确处理回滚异常。
	// @param status 表示事务的对象
	// @param ex 抛出的应用程序异常或错误
	// @throws TransactionException 表示回滚失败
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex) throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				if (status.isDebug()) {
					// 提交异常后启动事务回滚
					logger.debug("Initiating transaction rollback after commit exception", ex);
				}
				doRollback(status);
			}
			else if (status.hasTransaction() && isGlobalRollbackOnParticipationFailure()) {
				if (status.isDebug()) {
					logger.debug("Marking existing transaction as rollback-only after commit exception", ex);
				}
				doSetRollbackOnly(status);
			}
		}
		catch (RuntimeException | Error rbex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
			this.transactionExecutionListeners.forEach(listener -> listener.afterRollback(status, rbex));
			throw rbex;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
		this.transactionExecutionListeners.forEach(listener -> listener.afterRollback(status, null));
	}


	/**
	 * Trigger {@code beforeCommit} callbacks.
	 * @param status object representing the transaction
	 */
	// 触发 {@code beforeCommit} 回调。
	// @param 表示事务的 status 对象
	protected final void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerBeforeCommit(status.isReadOnly());
		}
	}

	/**
	 * Trigger {@code beforeCompletion} callbacks.
	 * @param status object representing the transaction
	 */
	// 触发 {@code beforeCompletion} 回调。
	// @param 表示交易的状态对象
	protected final void triggerBeforeCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerBeforeCompletion();
		}
	}

	/**
	 * Trigger {@code afterCommit} callbacks.
	 * @param status object representing the transaction
	 */
	private void triggerAfterCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerAfterCommit();
		}
	}

	/**
	 * Trigger {@code afterCompletion} callbacks.
	 * @param status object representing the transaction
	 * @param completionStatus completion status according to TransactionSynchronization constants
	 */
	// 触发 {@code afterCompletion} 回调。
	// @param status 表示事务的对象
	// @param completionStatus 根据 TransactionSynchronization 常量确定的完成状态
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus) {
		if (status.isNewSynchronization()) {
			List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronizationManager.clearSynchronization();
			if (!status.hasTransaction() || status.isNewTransaction()) {
				// No transaction or new transaction for the current scope ->
				// invoke the afterCompletion callbacks immediately
				// --> 译文：当前范围没有事务或新事务 -> 立即调用 afterCompletion 回调
				invokeAfterCompletion(synchronizations, completionStatus); // invoke TransactionSynchronization.afterCompletion()
			}
			else if (!synchronizations.isEmpty()) {
				// Existing transaction that we participate in, controlled outside
				// the scope of this Spring transaction manager -> try to register
				// an afterCompletion callback with the existing (JTA) transaction.
				// --> 译文：我们参与的现有事务，在该 Spring 事务管理器的范围之外控制 -> 尝试向现有（JTA）事务注册 afterCompletion 回调。
				registerAfterCompletionWithExistingTransaction(status.getTransaction(), synchronizations);
			}
		}
	}

	/**
	 * Actually invoke the {@code afterCompletion} methods of the
	 * given Spring TransactionSynchronization objects.
	 * <p>To be called by this abstract manager itself, or by special implementations
	 * of the {@code registerAfterCompletionWithExistingTransaction} callback.
	 * @param synchronizations a List of TransactionSynchronization objects
	 * @param completionStatus the completion status according to the
	 * constants in the TransactionSynchronization interface
	 * @see #registerAfterCompletionWithExistingTransaction(Object, java.util.List)
	 * @see TransactionSynchronization#STATUS_COMMITTED
	 * @see TransactionSynchronization#STATUS_ROLLED_BACK
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	// 实际调用给定 Spring TransactionSynchronization 对象的 {@code afterCompletion} 方法。
	// <p>由此抽象管理器本身调用，或由 {@code registerAfterCompletionWithExistingTransaction} 回调的特殊实现调用。
	// @param synchronization TransactionSynchronization 对象列表
	// @param completionStatus 根据 TransactionSynchronization 接口中的常量确定完成状态
	protected final void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, int completionStatus) {
		TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
	}

	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 * @see #doCleanupAfterCompletion
	 */
	// 完成后进行清理，必要时清除同步，并调用 doCleanupAfterCompletion。
	// @param status 表示事务的对象
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted(); // 将此事务标记为已完成，即已提交或已回滚。
		if (status.isNewSynchronization()) {
			// 清除当前线程的整个事务同步状态：注册的同步以及各种事务特征。
			TransactionSynchronizationManager.clear();
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction after completion of inner transaction"); // 内部事务完成后恢复暂停的事务
			}
			Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
			resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources()); // 恢复给定的事务。
		}
	}


	//---------------------------------------------------------------------
	// Template methods to be implemented in subclasses
	//---------------------------------------------------------------------

	/**
	 * Return a transaction object for the current transaction state.
	 * <p>The returned object will usually be specific to the concrete transaction
	 * manager implementation, carrying corresponding transaction state in a
	 * modifiable fashion. This object will be passed into the other template
	 * methods (e.g. doBegin and doCommit), either directly or as part of a
	 * DefaultTransactionStatus instance.
	 * <p>The returned object should contain information about any existing
	 * transaction, that is, a transaction that has already started before the
	 * current {@code getTransaction} call on the transaction manager.
	 * Consequently, a {@code doGetTransaction} implementation will usually
	 * look for an existing transaction and store corresponding state in the
	 * returned transaction object.
	 * @return the current transaction object
	 * @throws org.springframework.transaction.CannotCreateTransactionException
	 * if transaction support is not available
	 * @throws TransactionException in case of lookup or system errors
	 * @see #doBegin
	 * @see #doCommit
	 * @see #doRollback
	 * @see DefaultTransactionStatus#getTransaction
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * Check if the given transaction object indicates an existing transaction
	 * (that is, a transaction which has already started).
	 * <p>The result will be evaluated according to the specified propagation
	 * behavior for the new transaction. An existing transaction might get
	 * suspended (in case of PROPAGATION_REQUIRES_NEW), or the new transaction
	 * might participate in the existing one (in case of PROPAGATION_REQUIRED).
	 * <p>The default implementation returns {@code false}, assuming that
	 * participating in existing transactions is generally not supported.
	 * Subclasses are of course encouraged to provide such support.
	 * @param transaction the transaction object returned by doGetTransaction
	 * @return if there is an existing transaction
	 * @throws TransactionException in case of system errors
	 * @see #doGetTransaction
	 */
	// 检查给定的事务对象是否指示现有事务（即已启动的事务）。
	// <p>结果将根据新事务的指定传播行为进行评估。
	// 现有事务可能会被暂停（如果为 PROPAGATION_REQUIRES_NEW），或者新事务可能会参与现有事务（如果为 PROPAGATION_REQUIRED）。
	// <p>默认实现返回 {@code false}，假设通常不支持参与现有事务。当然，鼓励子类提供此类支持。
	// @param transaction doGetTransaction 返回的事务对象
	// @return 是否存在现有事务
	// @throws TransactionException 发生系统错误
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * Return whether to use a savepoint for a nested transaction.
	 * <p>Default is {@code true}, which causes delegation to DefaultTransactionStatus
	 * for creating and holding a savepoint. If the transaction object does not implement
	 * the SavepointManager interface, a NestedTransactionNotSupportedException will be
	 * thrown. Else, the SavepointManager will be asked to create a new savepoint to
	 * demarcate the start of the nested transaction.
	 * <p>Subclasses can override this to return {@code false}, causing a further
	 * call to {@code doBegin} - within the context of an already existing transaction.
	 * The {@code doBegin} implementation needs to handle this accordingly in such
	 * a scenario. This is appropriate for JTA, for example.
	 * @see DefaultTransactionStatus#createAndHoldSavepoint
	 * @see DefaultTransactionStatus#rollbackToHeldSavepoint
	 * @see DefaultTransactionStatus#releaseHeldSavepoint
	 * @see #doBegin
	 */
	// 返回是否在嵌套事务中使用保存点。
	// <p>默认值为 {@code true}，这意味着将保存点的创建和保存委托给 DefaultTransactionStatus。
	// 如果事务对象未实现 SavepointManager 接口，则会抛出 NestedTransactionNotSupportedException。
	// 否则，将要求 SavepointManager 创建一个新的保存点来标定嵌套事务的起始位置。
	// <p>子类可以重写此方法并返回 {@code false}，从而导致在现有事务的上下文中再次调用 {@code doBegin}。
	// 在这种情况下，{@code doBegin} 实现需要相应地处理这种情况。例如，这适用于 JTA。
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * Begin a new transaction with semantics according to the given transaction
	 * definition. Does not have to care about applying the propagation behavior,
	 * as this has already been handled by this abstract manager.
	 * <p>This method gets called when the transaction manager has decided to actually
	 * start a new transaction. Either there wasn't any transaction before, or the
	 * previous transaction has been suspended.
	 * <p>A special scenario is a nested transaction without savepoint: If
	 * {@code useSavepointForNestedTransaction()} returns "false", this method
	 * will be called to start a nested transaction when necessary. In such a context,
	 * there will be an active transaction: The implementation of this method has
	 * to detect this and start an appropriate nested transaction.
	 * @param transaction the transaction object returned by {@code doGetTransaction}
	 * @param definition a TransactionDefinition instance, describing propagation
	 * behavior, isolation level, read-only flag, timeout, and transaction name
	 * @throws TransactionException in case of creation or system errors
	 * @throws org.springframework.transaction.NestedTransactionNotSupportedException
	 * if the underlying transaction does not support nesting
	 */
	// 根据给定的事务定义，以语义开启一个新事务。无需关心传播行为的应用，因为这已由该抽象管理器处理。
	// <p>当事务管理器决定实际开启一个新事务时，会调用此方法。之前可能不存在任何事务，或者之前的事务已被暂停。
	// <p>一种特殊情况是没有保存点的嵌套事务：如果 {@code useSavepointForNestedTransaction()} 返回“false”，
	// 则在必要时将调用此方法以开启嵌套事务。在这种情况下，将存在一个活动事务：此方法的实现必须检测到这种情况并开启一个合适的嵌套事务。
	// @param transaction {@code doGetTransaction} 返回的事务对象
	// @param definition TransactionDefinition 实例，描述传播行为、隔离级别、只读标志、超时和事务名称
	// @throws TransactionException（如果发生创建或系统错误）
	// @throws org.springframework.transaction.NestedTransactionNotSupportedException（如果底层事务不支持嵌套）
	protected abstract void doBegin(Object transaction, TransactionDefinition definition)
			throws TransactionException;

	/**
	 * Suspend the resources of the current transaction.
	 * Transaction synchronization will already have been suspended.
	 * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * @param transaction the transaction object returned by {@code doGetTransaction}
	 * @return an object that holds suspended resources
	 * (will be kept unexamined for passing it into doResume)
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if suspending is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doResume
	 */
	// 暂停当前事务的资源。事务同步将已被暂停。
	// <p>默认实现会抛出 TransactionSuspensionNotSupportedException，假设事务暂停通常不受支持。
	// @param transaction {@code doGetTransaction} 返回的事务对象
	// @return 一个保存已暂停资源的对象（将保留未检查状态以将其传递给 doResume）
	// @throws org.springframework.transaction.TransactionSuspensionNotSupportedException 如果事务管理器实现不支持暂停
	// @throws TransactionException 如果发生系统错误
	protected Object doSuspend(Object transaction) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Resume the resources of the current transaction.
	 * Transaction synchronization will be resumed afterwards.
	 * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
	 * assuming that transaction suspension is generally not supported.
	 * @param transaction the transaction object returned by {@code doGetTransaction}
	 * @param suspendedResources the object that holds suspended resources,
	 * as returned by doSuspend
	 * @throws org.springframework.transaction.TransactionSuspensionNotSupportedException
	 * if resuming is not supported by the transaction manager implementation
	 * @throws TransactionException in case of system errors
	 * @see #doSuspend
	 */
	protected void doResume(@Nullable Object transaction, Object suspendedResources) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * Return whether to call {@code doCommit} on a transaction that has been
	 * marked as rollback-only in a global fashion.
	 * <p>Does not apply if an application locally sets the transaction to rollback-only
	 * via the TransactionStatus, but only to the transaction itself being marked as
	 * rollback-only by the transaction coordinator.
	 * <p>Default is "false": Local transaction strategies usually don't hold the rollback-only
	 * marker in the transaction itself, therefore they can't handle rollback-only transactions
	 * as part of transaction commit. Hence, AbstractPlatformTransactionManager will trigger
	 * a rollback in that case, throwing an UnexpectedRollbackException afterwards.
	 * <p>Override this to return "true" if the concrete transaction manager expects a
	 * {@code doCommit} call even for a rollback-only transaction, allowing for
	 * special handling there. This will, for example, be the case for JTA, where
	 * {@code UserTransaction.commit} will check the read-only flag itself and
	 * throw a corresponding RollbackException, which might include the specific reason
	 * (such as a transaction timeout).
	 * <p>If this method returns "true" but the {@code doCommit} implementation does not
	 * throw an exception, this transaction manager will throw an UnexpectedRollbackException
	 * itself. This should not be the typical case; it is mainly checked to cover misbehaving
	 * JTA providers that silently roll back even when the rollback has not been requested
	 * by the calling code.
	 * @see #doCommit
	 * @see DefaultTransactionStatus#isGlobalRollbackOnly()
	 * @see DefaultTransactionStatus#isLocalRollbackOnly()
	 * @see org.springframework.transaction.TransactionStatus#setRollbackOnly()
	 * @see org.springframework.transaction.UnexpectedRollbackException
	 * @see jakarta.transaction.UserTransaction#commit()
	 * @see jakarta.transaction.RollbackException
	 */
	// 返回是否在全局标记为 “仅回滚” 的事务上调用 doCommit 函数。
	// <p>如果应用程序通过 TransactionStatus 在本地将事务设置为“仅回滚”，则此方法不适用，但仅适用于事务本身被事务协调器标记为“仅回滚”的情况。
	// <p>默认值为“false”：本地事务策略通常不会在事务本身中保留“仅回滚”标记，因此它们无法将“仅回滚”事务作为事务提交的一部分进行处理。
	// 因此，AbstractPlatformTransactionManager 将在这种情况下触发回滚，随后抛出 UnexpectedRollbackException。
	// <p>如果具体事务管理器即使对于“仅回滚”事务也期望 doCommit 调用，则请重写此方法以返回“true”，从而允许进行特殊处理。
	// 例如，对于 JTA 来说，{@code UserTransaction.commit} 会检查只读标志本身并抛出相应的 RollbackException，其中可能包含具体原因（例如事务超时）。
	// <p>如果此方法返回“true”，但 {@code doCommit} 实现未抛出异常，则此事务管理器将抛出 UnexpectedRollbackException。
	// 这应该不是典型情况；检查此异常主要是为了解决行为不当的 JTA 提供程序，即使调用代码未请求回滚，这些提供程序也会默默地进行回滚。
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return false;
	}

	/**
	 * Make preparations for commit, to be performed before the
	 * {@code beforeCommit} synchronization callbacks occur.
	 * <p>Note that exceptions will get propagated to the commit caller
	 * and cause a rollback of the transaction.
	 * @param status the status representation of the transaction
	 * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>
	 * (note: do not throw TransactionException subclasses here!)
	 */
	// 为提交做好准备，在 {@code beforeCommit} 同步回调发生之前执行。
	// <p>请注意，异常将传播到提交调用方，并导致事务回滚。
	// @param status 事务的状态表示
	// @throws RuntimeException（如果发生错误）；将<b>传播给调用方</b>（注意：此处不要抛出 TransactionException 子类！）
	protected void prepareForCommit(DefaultTransactionStatus status) {
	}

	/**
	 * Perform an actual commit of the given transaction.
	 * <p>An implementation does not need to check the "new transaction" flag
	 * or the rollback-only flag; this will already have been handled before.
	 * Usually, a straight commit will be performed on the transaction object
	 * contained in the passed-in status.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of commit or system errors
	 * @see DefaultTransactionStatus#getTransaction
	 */
	// 对给定的事务执行实际提交。
	// <p>实现无需检查“新事务”标志或仅回滚标志；这些标志之前已经处理过了。通常，将对传入状态中包含的事务对象执行直接提交。
	// @param status 事务的状态表示
	// @throws TransactionException （如果发生提交或系统错误）
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Perform an actual rollback of the given transaction.
	 * <p>An implementation does not need to check the "new transaction" flag;
	 * this will already have been handled before. Usually, a straight rollback
	 * will be performed on the transaction object contained in the passed-in status.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of system errors
	 * @see DefaultTransactionStatus#getTransaction
	 */
	// 对给定的事务执行实际回滚。
	// <p>实现无需检查“新事务”标志；该标志之前已经处理过了。
	// 通常，会直接对传入状态中包含的事务对象执行回滚。
	// @param status 事务的状态表示
	// @throws TransactionException （如果发生系统错误）
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * Set the given transaction rollback-only. Only called on rollback
	 * if the current transaction participates in an existing one.
	 * <p>The default implementation throws an IllegalTransactionStateException,
	 * assuming that participating in existing transactions is generally not
	 * supported. Subclasses are of course encouraged to provide such support.
	 * @param status the status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	// 将给定事务设置为仅回滚。仅当当前事务参与现有事务时，才会在回滚时调用。
	// <p>默认实现会抛出 IllegalTransactionStateException 异常，因为通常不支持参与现有事务。当然，我们鼓励子类提供此类支持。
	// @param status 事务的状态表示
	// @throws TransactionException （如果发生系统错误）
	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
		// 不支持参与现有事务 - 当 “isExistingTransaction” 返回 true 时，必须提供适当的 “doSetRollbackOnly” 行为
		throw new IllegalTransactionStateException(
				"Participating in existing transactions is not supported - when 'isExistingTransaction' " +
				"returns true, appropriate 'doSetRollbackOnly' behavior must be provided");
	}

	/**
	 * Register the given list of transaction synchronizations with the existing transaction.
	 * <p>Invoked when the control of the Spring transaction manager and thus all Spring
	 * transaction synchronizations end, without the transaction being completed yet. This
	 * is for example the case when participating in an existing JTA or EJB CMT transaction.
	 * <p>The default implementation simply invokes the {@code afterCompletion} methods
	 * immediately, passing in "STATUS_UNKNOWN". This is the best we can do if there's no
	 * chance to determine the actual outcome of the outer transaction.
	 * @param transaction the transaction object returned by {@code doGetTransaction}
	 * @param synchronizations a List of TransactionSynchronization objects
	 * @throws TransactionException in case of system errors
	 * @see #invokeAfterCompletion(java.util.List, int)
	 * @see TransactionSynchronization#afterCompletion(int)
	 * @see TransactionSynchronization#STATUS_UNKNOWN
	 */
	// 将给定的事务同步列表注册到现有事务中。
	// <p>当 Spring 事务管理器的控制权（因此所有 Spring 事务同步都已结束，但事务尚未完成）结束时调用。
	// 例如，参与现有 JTA 或 EJB CMT 事务时。<p>默认实现会立即调用 {@code afterCompletion} 方法，
	// 并传入“STATUS_UNKNOWN”。如果无法确定外部事务的实际结果，这是我们能做的最好的事情。
	// @param transaction {@code doGetTransaction} 返回的事务对象
	// @param synchronizations TransactionSynchronization 对象列表
	// @throws TransactionException （如果发生系统错误）
	protected void registerAfterCompletionWithExistingTransaction(
			Object transaction, List<TransactionSynchronization> synchronizations) throws TransactionException {

		// --> 译文：无法将 Spring 完成后同步与现有事务一起注册 - 立即处理 Spring 完成后回调，结果状态为“未知”
		logger.debug("Cannot register Spring after-completion synchronization with existing transaction - " +
				"processing Spring after-completion callbacks immediately, with outcome status 'unknown'");
		// invoke TransactionSynchronization.afterCompletion()
		invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
	}

	/**
	 * Cleanup resources after transaction completion.
	 * <p>Called after {@code doCommit} and {@code doRollback} execution,
	 * on any outcome. The default implementation does nothing.
	 * <p>Should not throw any exceptions but just issue warnings on errors.
	 * @param transaction the transaction object returned by {@code doGetTransaction}
	 */
	// 事务完成后清理资源。
	// <p>在 {@code doCommit} 和 {@code doRollback} 执行后，无论结果如何都会调用。默认实现不执行任何操作。
	// <p>不应抛出任何异常，而应在出现错误时发出警告。
	// @param transaction 为 {@code doGetTransaction} 返回的事务对象
	protected void doCleanupAfterCompletion(Object transaction) {
	}


	//---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		ois.defaultReadObject();

		// Initialize transient fields.
		this.logger = LogFactory.getLog(getClass());
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by {@code suspend} and {@code resume}.
	 */
	// 暂停资源的持有者。由 {@code suspend} 和 {@code resume} 内部使用。
	protected static final class SuspendedResourcesHolder {

		@Nullable
		private final Object suspendedResources;

		@Nullable
		private List<TransactionSynchronization> suspendedSynchronizations;

		@Nullable
		private String name;

		private boolean readOnly;

		@Nullable
		private Integer isolationLevel;

		private boolean wasActive;

		private SuspendedResourcesHolder(Object suspendedResources) {
			this.suspendedResources = suspendedResources;
		}

		private SuspendedResourcesHolder(
				@Nullable Object suspendedResources, List<TransactionSynchronization> suspendedSynchronizations,
				@Nullable String name, boolean readOnly, @Nullable Integer isolationLevel, boolean wasActive) {

			this.suspendedResources = suspendedResources;
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
			this.isolationLevel = isolationLevel;
			this.wasActive = wasActive;
		}
	}

}
