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

package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.vavr.control.Try;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.reactive.TransactionContextManager;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * Base class for transactional aspects, such as the {@link TransactionInterceptor}
 * or an AspectJ aspect.
 *
 * <p>This enables the underlying Spring transaction infrastructure to be used easily
 * to implement an aspect for any aspect system.
 *
 * <p>Subclasses are responsible for calling methods in this class in the correct order.
 *
 * <p>If no transaction name has been specified in the {@link TransactionAttribute},
 * the exposed name will be the {@code fully-qualified class name + "." + method name}
 * (by default).
 *
 * <p>Uses the <b>Strategy</b> design pattern. A {@link PlatformTransactionManager} or
 * {@link ReactiveTransactionManager} implementation will perform the actual transaction
 * management, and a {@link TransactionAttributeSource} (e.g. annotation-based) is used
 * for determining transaction definitions for a particular class or method.
 *
 * <p>A transaction aspect is serializable if its {@code TransactionManager} and
 * {@code TransactionAttributeSource} are serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stéphane Nicoll
 * @author Sam Brannen
 * @author Mark Paluch
 * @author Sebastien Deleuze
 * @author Enric Sala
 * @since 1.1
 * @see PlatformTransactionManager
 * @see ReactiveTransactionManager
 * @see #setTransactionManager
 * @see #setTransactionAttributes
 * @see #setTransactionAttributeSource
 */
// 事务方面（例如 {@link TransactionInterceptor} 或 AspectJ 方面）的基类。
//
// <p>这使得底层 Spring 事务基础架构能够轻松地用于为任何方面系统实现方面。
//
// <p>子类负责以正确的顺序调用此类中的方法。
//
// <p>如果 {@link TransactionAttribute} 中未指定事务名称，则公开的名称将为 {@code 完全限定类名 + "." + 方法名}（默认）。
//
// <p>使用 <b>Strategy</b> 设计模式。
// {@link PlatformTransactionManager} 或 {@link ReactiveTransactionManager} 实现将执行实际的事务管理，
// 并使用 {@link TransactionAttributeSource}（例如基于注解的）来确定特定类或方法的事务定义。
//
// <p>如果事务方面的 {@code TransactionManager} 和 {@code TransactionAttributeSource} 可序列化，则该事务方面也是可序列化的。
public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

	// NOTE: This class must not implement Serializable because it serves as base
	// class for AspectJ aspects (which are not allowed to implement Serializable)!


	/**
	 * Key to use to store the default transaction manager.
	 */
	// 用于存储默认事务管理器的键。
	private static final Object DEFAULT_TRANSACTION_MANAGER_KEY = new Object();

	private static final String COROUTINES_FLOW_CLASS_NAME = "kotlinx.coroutines.flow.Flow";

	/**
	 * Reactive Streams API present on the classpath?
	 */
	private static final boolean reactiveStreamsPresent = ClassUtils.isPresent(
			"org.reactivestreams.Publisher", TransactionAspectSupport.class.getClassLoader());

	/**
	 * Vavr library present on the classpath?
	 */
	// 类路径上是否存在 Vavr 库？
	private static final boolean vavrPresent = ClassUtils.isPresent(
			"io.vavr.control.Try", TransactionAspectSupport.class.getClassLoader());

	/**
	 * Holder to support the {@code currentTransactionStatus()} method,
	 * and to support communication between different cooperating advices
	 * (e.g. before and after advice) if the aspect involves more than a
	 * single method (as will be the case for around advice).
	 */
	// Holder 支持 {@code currentTransactionStatus()} 方法，
	// 并且如果方面涉及多个方法（就像围绕建议的情况一样），则支持不同合作建议（例如之前和之后的建议）之间的通信。
	private static final ThreadLocal<TransactionInfo> transactionInfoHolder =
			new NamedThreadLocal<>("Current aspect-driven transaction");


	/**
	 * Subclasses can use this to return the current TransactionInfo.
	 * Only subclasses that cannot handle all operations in one method,
	 * such as an AspectJ aspect involving distinct before and after advice,
	 * need to use this mechanism to get at the current TransactionInfo.
	 * An around advice such as an AOP Alliance MethodInterceptor can hold a
	 * reference to the TransactionInfo throughout the aspect method.
	 * <p>A TransactionInfo will be returned even if no transaction was created.
	 * The {@code TransactionInfo.hasTransaction()} method can be used to query this.
	 * <p>To find out about specific transaction characteristics, consider using
	 * TransactionSynchronizationManager's {@code isSynchronizationActive()}
	 * and/or {@code isActualTransactionActive()} methods.
	 * @return the TransactionInfo bound to this thread, or {@code null} if none
	 * @see TransactionInfo#hasTransaction()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isSynchronizationActive()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isActualTransactionActive()
	 */
	@Nullable
	protected static TransactionInfo currentTransactionInfo() throws NoTransactionException {
		return transactionInfoHolder.get();
	}

	/**
	 * Return the transaction status of the current method invocation.
	 * Mainly intended for code that wants to set the current transaction
	 * rollback-only but not throw an application exception.
	 * <p>This exposes the locally declared transaction boundary with its declared name
	 * and characteristics, as managed by the aspect. Ar runtime, the local boundary may
	 * participate in an outer transaction: If you need transaction metadata from such
	 * an outer transaction (the actual resource transaction) instead, consider using
	 * {@link org.springframework.transaction.support.TransactionSynchronizationManager}.
	 * @throws NoTransactionException if the transaction info cannot be found,
	 * because the method was invoked outside an AOP invocation context
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#getCurrentTransactionName()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	public static TransactionStatus currentTransactionStatus() throws NoTransactionException {
		TransactionInfo info = currentTransactionInfo();
		if (info == null || info.transactionStatus == null) {
			throw new NoTransactionException("No transaction aspect-managed TransactionStatus in scope");
		}
		return info.transactionStatus;
	}


	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private final ReactiveAdapterRegistry reactiveAdapterRegistry;

	@Nullable
	private String transactionManagerBeanName;

	@Nullable
	private TransactionManager transactionManager;

	@Nullable
	private TransactionAttributeSource transactionAttributeSource;

	@Nullable
	private BeanFactory beanFactory;

	private final ConcurrentMap<Object, TransactionManager> transactionManagerCache =
			new ConcurrentReferenceHashMap<>(4);

	private final ConcurrentMap<Method, ReactiveTransactionSupport> transactionSupportCache =
			new ConcurrentReferenceHashMap<>(1024);


	protected TransactionAspectSupport() {
		if (reactiveStreamsPresent) {
			this.reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();
		}
		else {
			this.reactiveAdapterRegistry = null;
		}
	}


	/**
	 * Specify the name of the default transaction manager bean.
	 * <p>This can either point to a traditional {@link PlatformTransactionManager} or a
	 * {@link ReactiveTransactionManager} for reactive transaction management.
	 */
	public void setTransactionManagerBeanName(@Nullable String transactionManagerBeanName) {
		this.transactionManagerBeanName = transactionManagerBeanName;
	}

	/**
	 * Return the name of the default transaction manager bean.
	 */
	@Nullable
	protected final String getTransactionManagerBeanName() {
		return this.transactionManagerBeanName;
	}

	/**
	 * Specify the <em>default</em> transaction manager to use to drive transactions.
	 * <p>This can either be a traditional {@link PlatformTransactionManager} or a
	 * {@link ReactiveTransactionManager} for reactive transaction management.
	 * <p>The default transaction manager will be used if a <em>qualifier</em>
	 * has not been declared for a given transaction or if an explicit name for the
	 * default transaction manager bean has not been specified.
	 * @see #setTransactionManagerBeanName
	 */
	// 指定用于驱动事务的<em>默认</em>事务管理器。
	// <p>这可以是传统的 {@link PlatformTransactionManager}，也可以是用于响应式事务管理的 {@link ReactiveTransactionManager}。
	// <p>如果未为给定事务声明<em>限定符</em>，或者未指定默认事务管理器 bean 的明确名称，则将使用默认事务管理器。
	public void setTransactionManager(@Nullable TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the default transaction manager, or {@code null} if unknown.
	 * <p>This can either be a traditional {@link PlatformTransactionManager} or a
	 * {@link ReactiveTransactionManager} for reactive transaction management.
	 */
	// 返回默认事务管理器，如果未知则返回 {@code null}。
	// <p>这可以是传统的 {@link PlatformTransactionManager} 或用于反应式事务管理的 {@link ReactiveTransactionManager}。
	@Nullable
	public TransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	/**
	 * Set properties with method names as keys and transaction attribute
	 * descriptors (parsed via TransactionAttributeEditor) as values:
	 * e.g. key = "myMethod", value = "PROPAGATION_REQUIRED,readOnly".
	 * <p>Note: Method names are always applied to the target class,
	 * no matter if defined in an interface or the class itself.
	 * <p>Internally, a NameMatchTransactionAttributeSource will be
	 * created from the given properties.
	 * @see #setTransactionAttributeSource
	 * @see TransactionAttributeEditor
	 * @see NameMatchTransactionAttributeSource
	 */
	// 使用方法名称作为键，以事务属性描述符（通过 TransactionAttributeEditor 解析）作为值来设置属性：
	// 例如，key = "myMethod"，value = "PROPAGATION_REQUIRED,readOnly"。
	// <p>注意：方法名称始终应用于目标类，无论是在接口中定义还是在类本身中定义。
	// <p>在内部，将根据给定的属性创建一个 NameMatchTransactionAttributeSource。
	public void setTransactionAttributes(Properties transactionAttributes) {
		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		tas.setProperties(transactionAttributes);
		this.transactionAttributeSource = tas;
	}

	/**
	 * Set multiple transaction attribute sources which are used to find transaction
	 * attributes. Will build a CompositeTransactionAttributeSource for the given sources.
	 * @see CompositeTransactionAttributeSource
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	// 设置多个用于查找事务属性的事务属性源。
	// 将为给定的源构建一个 CompositeTransactionAttributeSource。
	public void setTransactionAttributeSources(TransactionAttributeSource... transactionAttributeSources) {
		this.transactionAttributeSource = new CompositeTransactionAttributeSource(transactionAttributeSources);
	}

	/**
	 * Set the transaction attribute source which is used to find transaction
	 * attributes. If specifying a String property value, a PropertyEditor
	 * will create a MethodMapTransactionAttributeSource from the value.
	 * @see TransactionAttributeSourceEditor
	 * @see MethodMapTransactionAttributeSource
	 * @see NameMatchTransactionAttributeSource
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	// 设置用于查找事务属性的事务属性源。
	// 如果指定字符串属性值，PropertyEditor 将根据该值创建一个 MethodMapTransactionAttributeSource。
	public void setTransactionAttributeSource(@Nullable TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	/**
	 * Return the transaction attribute source.
	 */
	// 返回事务属性源。
	@Nullable
	public TransactionAttributeSource getTransactionAttributeSource() {
		return this.transactionAttributeSource;
	}

	/**
	 * Set the BeanFactory to use for retrieving {@code TransactionManager} beans.
	 */
	@Override
	public void setBeanFactory(@Nullable BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the BeanFactory to use for retrieving {@code TransactionManager} beans.
	 */
	@Nullable
	protected final BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Check that required properties were set.
	 */
	// 检查是否设置了必需的属性。
	@Override
	public void afterPropertiesSet() {
		if (getTransactionManager() == null && this.beanFactory == null) {
			// 设置 “transactionManager” 属性或确保在包含 TransactionManager bean 的 BeanFactory 中运行！
			throw new IllegalStateException(
					"Set the 'transactionManager' property or make sure to run within a BeanFactory " +
					"containing a TransactionManager bean!");
		}
		if (getTransactionAttributeSource() == null) {
			// 需要 “transactionAttributeSource” 或 “transactionAttributes”：如果没有事务方法，则不要使用事务方面。
			throw new IllegalStateException(
					"Either 'transactionAttributeSource' or 'transactionAttributes' is required: " +
					"If there are no transactional methods, then don't use a transaction aspect.");
		}
	}


	/**
	 * General delegate for around-advice-based subclasses, delegating to several other template
	 * methods on this class. Able to handle {@link CallbackPreferringPlatformTransactionManager}
	 * as well as regular {@link PlatformTransactionManager} implementations and
	 * {@link ReactiveTransactionManager} implementations for reactive return types.
	 * @param method the Method being invoked
	 * @param targetClass the target class that we're invoking the method on
	 * @param invocation the callback to use for proceeding with the target invocation
	 * @return the return value of the method, if any
	 * @throws Throwable propagated from the target invocation
	 */
	// 基于环绕通知的子类的通用委托，委托给此类中的其他几个模板方法。
	// 能够处理 {@link CallbackPreferringPlatformTransactionManager} 以及常规
	// {@link PlatformTransactionManager} 实现和 {@link ReactiveTransactionManager} 响应式返回类型的实现。
	// @param method 被调用的方法
	// @param targetClass 调用该方法的目标类
	// @param invocation 用于继续目标调用的回调
	// @return 方法的返回值（如果有）
	// @throws 从目标调用传播的 Throwable
	@Nullable
	protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
			final InvocationCallback invocation) throws Throwable {

		// If the transaction attribute is null, the method is non-transactional. --> 译文：如果事务属性为空，则该方法是非事务性的。
		// 获取事务属性源。
		TransactionAttributeSource tas = getTransactionAttributeSource();
		// 获取给定方法的事务属性，如果该方法是非事务性的，则返回 {@code null}。
		final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
		// 确定用于给定事务的特定 TransactionManager。
		final TransactionManager tm = determineTransactionManager(txAttr);

		// ReactiveTransactionManager 的处理逻辑
		if (this.reactiveAdapterRegistry != null && tm instanceof ReactiveTransactionManager rtm) {
			boolean isSuspendingFunction = KotlinDetector.isSuspendingFunction(method);
			boolean hasSuspendingFlowReturnType = isSuspendingFunction &&
					COROUTINES_FLOW_CLASS_NAME.equals(new MethodParameter(method, -1).getParameterType().getName());

			ReactiveTransactionSupport txSupport = this.transactionSupportCache.computeIfAbsent(method, key -> {
				Class<?> reactiveType =
						(isSuspendingFunction ? (hasSuspendingFlowReturnType ? Flux.class : Mono.class) : method.getReturnType());
				ReactiveAdapter adapter = this.reactiveAdapterRegistry.getAdapter(reactiveType);
				if (adapter == null) {
					throw new IllegalStateException("Cannot apply reactive transaction to non-reactive return type [" +
							method.getReturnType() + "] with specified transaction manager: " + tm);
				}
				return new ReactiveTransactionSupport(adapter);
			});

			return txSupport.invokeWithinTransaction(method, targetClass, invocation, txAttr, rtm);
		}

		PlatformTransactionManager ptm = asPlatformTransactionManager(tm);
		// 获取方法的字符串表示形式，用于日志记录。
		final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

		// 非 CallbackPreferringPlatformTransactionManager 的处理逻辑
		if (txAttr == null || !(ptm instanceof CallbackPreferringPlatformTransactionManager cpptm)) {
			// Standard transaction demarcation with getTransaction and commit/rollback calls.
			// --> 译文：使用 getTransaction 和提交/回滚调用进行标准事务划分。
			TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification);

			Object retVal;
			try {
				// This is an around advice: Invoke the next interceptor in the chain.
				// This will normally result in a target object being invoked.
				// --> 译文：这是一个环绕通知：调用链中的下一个拦截器。这通常会导致目标对象被调用。
				retVal = invocation.proceedWithInvocation(); // invoke Joinpoint#proceed()
			}
			catch (Throwable ex) {
				// target invocation exception --> 译文：目标调用异常
				// 处理可抛出对象，完成事务。我们可能会提交或回滚，具体取决于配置。
				completeTransactionAfterThrowing(txInfo, ex);
				throw ex;
			}
			finally {
				// 重置 TransactionInfo ThreadLocal。
				cleanupTransactionInfo(txInfo);
			}

			if (retVal != null && txAttr != null) {
				TransactionStatus status = txInfo.getTransactionStatus();
				if (status != null) {
					if (retVal instanceof Future<?> future && future.isDone()) {
						try {
							future.get();
						}
						catch (ExecutionException ex) {
							// 是否应在给定异常时回滚？
							if (txAttr.rollbackOn(ex.getCause())) {
								// 将事务设置为仅回滚。
								status.setRollbackOnly();
							}
						}
						catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
					else if (vavrPresent && VavrDelegate.isVavrTry(retVal)) {
						// Set rollback-only in case of Vavr failure matching our rollback rules...
						// --> 译文：如果 Vavr 发生与我们的回滚规则相符的故障，则设置仅回滚...
						retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
					}
				}
			}

			// 调用成功完成后执行，但不在处理异常后执行。如果我们没有创建事务，则不执行任何操作。
			commitTransactionAfterReturning(txInfo);
			return retVal;
		}

		else {
			Object result;
			final ThrowableHolder throwableHolder = new ThrowableHolder();

			// It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
			// --> 译文：它是一个 CallbackPreferringPlatformTransactionManager：传入一个 TransactionCallback。
			try {
				// 在事务中执行给定回调对象指定的操作。
				result = cpptm.execute(txAttr, status -> {
					// 根据给定的属性和状态对象准备一个 TransactionInfo 对象。
					TransactionInfo txInfo = prepareTransactionInfo(ptm, txAttr, joinpointIdentification, status);
					try {
						Object retVal = invocation.proceedWithInvocation();
						if (retVal != null && vavrPresent && VavrDelegate.isVavrTry(retVal)) {
							// Set rollback-only in case of Vavr failure matching our rollback rules...
							// --> 译文：如果 Vavr 发生与我们的回滚规则相符的故障，则设置仅回滚...
							retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
						}
						return retVal;
					}
					catch (Throwable ex) {
						if (txAttr.rollbackOn(ex)) {
							// A RuntimeException: will lead to a rollback. --> 译文：RuntimeException：将导致回滚。
							if (ex instanceof RuntimeException runtimeException) {
								throw runtimeException;
							}
							else {
								throw new ThrowableHolderException(ex);
							}
						}
						else {
							// A normal return value: will lead to a commit. --> 译文：正常返回值：将导致提交。
							throwableHolder.throwable = ex;
							return null;
						}
					}
					finally {
						// 重置 TransactionInfo ThreadLocal。
						cleanupTransactionInfo(txInfo);
					}
				});
			}
			catch (ThrowableHolderException ex) {
				throw ex.getCause();
			}
			catch (TransactionSystemException ex2) {
				if (throwableHolder.throwable != null) {
					// 应用程序异常被提交异常覆盖
					logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
					ex2.initApplicationException(throwableHolder.throwable);
				}
				throw ex2;
			}
			catch (Throwable ex2) {
				if (throwableHolder.throwable != null) {
					logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
				}
				throw ex2;
			}

			// Check result state: It might indicate a Throwable to rethrow. --> 译文：检查结果状态：它可能指示要重新抛出一个 Throwable。
			if (throwableHolder.throwable != null) {
				throw throwableHolder.throwable;
			}
			return result;
		}
	}

	/**
	 * Clear the transaction manager cache.
	 */
	protected void clearTransactionManagerCache() {
		this.transactionManagerCache.clear();
		this.beanFactory = null;
	}

	/**
	 * Determine the specific transaction manager to use for the given transaction.
	 */
	// 确定用于给定事务的特定事务管理器。
	@Nullable
	protected TransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr) {
		// Do not attempt to lookup tx manager if no tx attributes are set --> 译文：如果未设置 tx 属性，则不要尝试查找 tx 管理器
		if (txAttr == null || this.beanFactory == null) {
			return getTransactionManager();
		}

		String qualifier = txAttr.getQualifier();
		if (StringUtils.hasText(qualifier)) {
			// 从给定的 BeanFactory 获取一个类型为 TransactionManager 的 bean，
			// 该 bean 声明了一个与给定限定符匹配的限定符（例如，通过 <qualifier> 或 @Qualifier），或者其 bean 名称与给定限定符匹配。
			return determineQualifiedTransactionManager(this.beanFactory, qualifier);
		}
		else if (StringUtils.hasText(this.transactionManagerBeanName)) {
			return determineQualifiedTransactionManager(this.beanFactory, this.transactionManagerBeanName);
		}
		else {
			// 从 this.transactionManager 获取
			TransactionManager defaultTransactionManager = getTransactionManager();
			if (defaultTransactionManager == null) {
				// 从缓存获取
				defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
				if (defaultTransactionManager == null) {
					// 从容器获取
					defaultTransactionManager = this.beanFactory.getBean(TransactionManager.class);
					this.transactionManagerCache.putIfAbsent(
							DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
				}
			}
			return defaultTransactionManager;
		}
	}

	private TransactionManager determineQualifiedTransactionManager(BeanFactory beanFactory, String qualifier) {
		TransactionManager txManager = this.transactionManagerCache.get(qualifier);
		if (txManager == null) {
			// 从给定的 BeanFactory 获取一个类型为 TransactionManager 的 bean，
			// 该 bean 声明了一个与给定限定符匹配的限定符（例如，通过 <qualifier> 或 @Qualifier），或者其 bean 名称与给定限定符匹配。
			txManager = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
					beanFactory, TransactionManager.class, qualifier);
			this.transactionManagerCache.putIfAbsent(qualifier, txManager);
		}
		return txManager;
	}


	@Nullable
	private PlatformTransactionManager asPlatformTransactionManager(@Nullable Object transactionManager) {
		if (transactionManager == null) {
			return null;
		}
		if (transactionManager instanceof PlatformTransactionManager ptm) {
			return ptm;
		}
		else {
			// 指定的事务管理器不是 PlatformTransactionManager
			throw new IllegalStateException(
					"Specified transaction manager is not a PlatformTransactionManager: " + transactionManager);
		}
	}

	private String methodIdentification(Method method, @Nullable Class<?> targetClass,
			@Nullable TransactionAttribute txAttr) {

		String methodIdentification = methodIdentification(method, targetClass);
		if (methodIdentification == null) {
			if (txAttr instanceof DefaultTransactionAttribute dta) {
				methodIdentification = dta.getDescriptor();
			}
			if (methodIdentification == null) {
				methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
			}
		}
		return methodIdentification;
	}

	/**
	 * Convenience method to return a String representation of this Method
	 * for use in logging. Can be overridden in subclasses to provide a
	 * different identifier for the given method.
	 * <p>The default implementation returns {@code null}, indicating the
	 * use of {@link DefaultTransactionAttribute#getDescriptor()} instead,
	 * ending up as {@link ClassUtils#getQualifiedMethodName(Method, Class)}.
	 * @param method the method we're interested in
	 * @param targetClass the class that the method is being invoked on
	 * @return a String representation identifying this method
	 * @see org.springframework.util.ClassUtils#getQualifiedMethodName
	 */
	// 便捷方法，返回此方法的字符串表示形式，用于日志记录。可在子类中重写，为给定方法提供不同的标识符。
	// <p>默认实现返回 {@code null}，表示使用 {@link DefaultTransactionAttribute#getDescriptor()}，
	// 最终结果为 {@link ClassUtils#getQualifiedMethodName(Method, Class)}。
	// @param method 我们感兴趣的方法
	// @param targetClass 调用该方法的类
	// @return 标识此方法的字符串表示形式
	@Nullable
	protected String methodIdentification(Method method, @Nullable Class<?> targetClass) {
		return null;
	}

	/**
	 * Create a transaction if necessary based on the given TransactionAttribute.
	 * <p>Allows callers to perform custom TransactionAttribute lookups through
	 * the TransactionAttributeSource.
	 * @param txAttr the TransactionAttribute (may be {@code null})
	 * @param joinpointIdentification the fully qualified method name
	 * (used for monitoring and logging purposes)
	 * @return a TransactionInfo object, whether a transaction was created.
	 * The {@code hasTransaction()} method on TransactionInfo can be used to
	 * tell if there was a transaction created.
	 * @see #getTransactionAttributeSource()
	 */
	// 如有需要，根据给定的 TransactionAttribute 创建事务。
	// <p>允许调用者通过 TransactionAttributeSource 执行自定义 TransactionAttribute 查找。
	// @param txAttr TransactionAttribute（可能为 {@code null}）
	// @param joinpointIdentification 完全限定方法名（用于监控和日志记录）
	// @return TransactionInfo 对象，指示是否已创建事务。TransactionInfo 上的 {@code hasTransaction()} 方法可用于判断是否已创建事务。
	@SuppressWarnings("serial")
	protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
			@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {

		// If no name specified, apply method identification as transaction name.
		// --> 译文：如果没有指定名称，则应用方法标识作为事务名称。
		if (txAttr != null && txAttr.getName() == null) {
			// 为给定的目标属性创建一个 DelegatingTransactionAttribute。
			txAttr = new DelegatingTransactionAttribute(txAttr) {
				@Override
				public String getName() {
					return joinpointIdentification;
				}
			};
		}

		TransactionStatus status = null;
		if (txAttr != null) {
			if (tm != null) {
				// 根据指定的传播行为，返回当前活动的事务或创建一个新的事务。
				// invoke PlatformTransactionManager.getTransaction()
				status = tm.getTransaction(txAttr);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
							"] because no transaction manager has been configured");
				}
			}
		}
		// 根据给定的属性和状态对象准备一个 TransactionInfo 对象。
		return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
	}

	/**
	 * Prepare a TransactionInfo for the given attribute and status object.
	 * @param txAttr the TransactionAttribute (may be {@code null})
	 * @param joinpointIdentification the fully qualified method name
	 * (used for monitoring and logging purposes)
	 * @param status the TransactionStatus for the current transaction
	 * @return the prepared TransactionInfo object
	 */
	// 根据给定的属性和状态对象准备一个 TransactionInfo 对象。
	// @param txAttr TransactionAttribute（可以为 {@code null}）
	// @param joinpointIdentification 完整限定方法名（用于监控和日志记录）
	// @param status 当前事务的 TransactionStatus
	// @return 准备好的 TransactionInfo 对象
	protected TransactionInfo prepareTransactionInfo(@Nullable PlatformTransactionManager tm,
			@Nullable TransactionAttribute txAttr, String joinpointIdentification,
			@Nullable TransactionStatus status) {

		TransactionInfo txInfo = new TransactionInfo(tm, txAttr, joinpointIdentification);
		if (txAttr != null) {
			// We need a transaction for this method... --> 译文：我们需要针对此方法进行事务...
			if (logger.isTraceEnabled()) {
				logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			// The transaction manager will flag an error if an incompatible tx already exists.
			// --> 译文：如果不兼容的事务已经存在，事务管理器将标记错误。
			txInfo.newTransactionStatus(status);
		}
		else {
			// The TransactionInfo.hasTransaction() method will return false. We created it only
			// to preserve the integrity of the ThreadLocal stack maintained in this class.
			// --> 译文：TransactionInfo.hasTransaction() 方法将返回 false。我们创建它只是为了维护该类中维护的 ThreadLocal 堆栈的完整性。
			if (logger.isTraceEnabled()) {
				logger.trace("No need to create transaction for [" + joinpointIdentification +
						"]: This method is not transactional.");
			}
		}

		// We always bind the TransactionInfo to the thread, even if we didn't create
		// a new transaction here. This guarantees that the TransactionInfo stack
		// will be managed correctly even if no transaction was created by this aspect.
		// --> 译文：即使我们没有在此创建新事务，我们也始终将 TransactionInfo 绑定到线程。
		// 这保证了即使此方面未创建事务，TransactionInfo 堆栈也能得到正确管理。
		txInfo.bindToThread();
		return txInfo;
	}

	/**
	 * Execute after successful completion of call, but not after an exception was handled.
	 * Do nothing if we didn't create a transaction.
	 * @param txInfo information about the current transaction
	 */
	// 调用成功完成后执行，但不在处理异常后执行。如果我们没有创建事务，则不执行任何操作。
	// @param txInfo 当前事务的相关信息
	protected void commitTransactionAfterReturning(@Nullable TransactionInfo txInfo) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
			}
			// 提交给定事务，并考虑其状态。如果事务已通过编程标记为“仅回滚”，则执行回滚。
			txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
		}
	}

	/**
	 * Handle a throwable, completing the transaction.
	 * We may commit or roll back, depending on the configuration.
	 * @param txInfo information about the current transaction
	 * @param ex throwable encountered
	 */
	// 处理 throwable，完成事务。我们可能会提交或回滚，具体取决于配置。
	// @param txInfo 当前事务的信息
	// @param ex 遇到的可抛出对象
	protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo, Throwable ex) {
		if (txInfo != null && txInfo.getTransactionStatus() != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
						"] after exception: " + ex);
			}
			// 我们应该回滚给定的异常吗？
			if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
				try {
					txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
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
			else {
				// We don't roll back on this exception.
				// Will still roll back if TransactionStatus.isRollbackOnly() is true.
				// --> 译文：遇到此异常时，我们不会回滚。如果 TransactionStatus.isRollbackOnly() 为 true，则仍会回滚。
				try {
					txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
				}
				catch (TransactionSystemException ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					ex2.initApplicationException(ex);
					throw ex2;
				}
				catch (RuntimeException | Error ex2) {
					logger.error("Application exception overridden by commit exception", ex);
					throw ex2;
				}
			}
		}
	}

	/**
	 * Reset the TransactionInfo ThreadLocal.
	 * <p>Call this in all cases: exception or normal return!
	 * @param txInfo information about the current transaction (may be {@code null})
	 */
	// 重置 TransactionInfo ThreadLocal。
	// <p>所有情况下均调用此方法：异常或正常返回！
	// @param txInfo 当前事务的信息（可能为 {@code null}）
	protected void cleanupTransactionInfo(@Nullable TransactionInfo txInfo) {
		if (txInfo != null) {
			txInfo.restoreThreadLocalStatus();
		}
	}


	/**
	 * Opaque object used to hold transaction information. Subclasses
	 * must pass it back to methods on this class, but not see its internals.
	 */
	// 用于保存事务信息的不透明对象。子类必须将其传回此类的方法，但无法查看其内部内容。
	protected static final class TransactionInfo {

		@Nullable
		private final PlatformTransactionManager transactionManager;

		@Nullable
		private final TransactionAttribute transactionAttribute;

		private final String joinpointIdentification;

		@Nullable
		private TransactionStatus transactionStatus;

		@Nullable
		private TransactionInfo oldTransactionInfo;

		public TransactionInfo(@Nullable PlatformTransactionManager transactionManager,
				@Nullable TransactionAttribute transactionAttribute, String joinpointIdentification) {

			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public PlatformTransactionManager getTransactionManager() {
			Assert.state(this.transactionManager != null, "No PlatformTransactionManager set");
			return this.transactionManager;
		}

		@Nullable
		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call)
		 * for use in logging.
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newTransactionStatus(@Nullable TransactionStatus status) {
			this.transactionStatus = status;
		}

		@Nullable
		public TransactionStatus getTransactionStatus() {
			return this.transactionStatus;
		}

		/**
		 * Return whether a transaction was created by this aspect,
		 * or whether we just have a placeholder to keep ThreadLocal stack integrity.
		 */
		public boolean hasTransaction() {
			return (this.transactionStatus != null);
		}

		private void bindToThread() {
			// Expose current TransactionStatus, preserving any existing TransactionStatus
			// for restoration after this transaction is complete.
			// --> 译文：公开当前的 TransactionStatus，保留任何现有的 TransactionStatus 以便在此事务完成后恢复。
			this.oldTransactionInfo = transactionInfoHolder.get();
			transactionInfoHolder.set(this);
		}

		private void restoreThreadLocalStatus() {
			// Use stack to restore old transaction TransactionInfo.
			// Will be null if none was set.
			// --> 译文：使用堆栈恢复旧事务的 TransactionInfo。如果未设置，则返回 null。
			transactionInfoHolder.set(this.oldTransactionInfo);
		}

		@Override
		public String toString() {
			return (this.transactionAttribute != null ? this.transactionAttribute.toString() : "No transaction");
		}
	}


	/**
	 * Simple callback interface for proceeding with the target invocation.
	 * Concrete interceptors/aspects adapt this to their invocation mechanism.
	 */
	// 用于继续执行目标调用的简单回调接口。具体拦截器/方面会根据其调用机制进行调整。
	@FunctionalInterface
	protected interface InvocationCallback {

		@Nullable
		Object proceedWithInvocation() throws Throwable;
	}


	/**
	 * Internal holder class for a Throwable in a callback transaction model.
	 */
	// 回调事务模型中 Throwable 的内部持有者类。
	private static class ThrowableHolder {

		@Nullable
		public Throwable throwable;
	}


	/**
	 * Internal holder class for a Throwable, used as a RuntimeException to be
	 * thrown from a TransactionCallback (and subsequently unwrapped again).
	 */
	// Throwable 的内部持有者类，用作从 TransactionCallback 抛出的 RuntimeException（随后再次解包）。
	@SuppressWarnings("serial")
	private static class ThrowableHolderException extends RuntimeException {

		public ThrowableHolderException(Throwable throwable) {
			super(throwable);
		}

		@Override
		public String toString() {
			return getCause().toString();
		}
	}


	/**
	 * Inner class to avoid a hard dependency on the Vavr library at runtime.
	 */
	// 内部类可避免在运行时对 Vavr 库的硬依赖。
	private static class VavrDelegate {

		public static boolean isVavrTry(Object retVal) {
			return (retVal instanceof Try);
		}

		public static Object evaluateTryFailure(Object retVal, TransactionAttribute txAttr, TransactionStatus status) {
			return ((Try<?>) retVal).onFailure(ex -> {
				if (txAttr.rollbackOn(ex)) {
					status.setRollbackOnly();
				}
			});
		}
	}


	/**
	 * Delegate for Reactor-based management of transactional methods with a
	 * reactive return type.
	 */
	private class ReactiveTransactionSupport {

		private final ReactiveAdapter adapter;

		public ReactiveTransactionSupport(ReactiveAdapter adapter) {
			this.adapter = adapter;
		}

		public Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
				InvocationCallback invocation, @Nullable TransactionAttribute txAttr, ReactiveTransactionManager rtm) {

			String joinpointIdentification = methodIdentification(method, targetClass, txAttr);

			// For Mono and suspending functions not returning kotlinx.coroutines.flow.Flow
			if (Mono.class.isAssignableFrom(method.getReturnType()) || (KotlinDetector.isSuspendingFunction(method) &&
					!COROUTINES_FLOW_CLASS_NAME.equals(new MethodParameter(method, -1).getParameterType().getName()))) {

				return TransactionContextManager.currentContext().flatMap(context ->
							Mono.<Object, ReactiveTransactionInfo>usingWhen(
								createTransactionIfNecessary(rtm, txAttr, joinpointIdentification),
								tx -> {
									try {
										return (Mono<?>) invocation.proceedWithInvocation();
									}
									catch (Throwable ex) {
										return Mono.error(ex);
									}
								},
								this::commitTransactionAfterReturning,
								this::completeTransactionAfterThrowing,
								this::rollbackTransactionOnCancel)
							.onErrorMap(this::unwrapIfResourceCleanupFailure))
						.contextWrite(TransactionContextManager.getOrCreateContext())
						.contextWrite(TransactionContextManager.getOrCreateContextHolder());
			}

			// Any other reactive type, typically a Flux
			return this.adapter.fromPublisher(TransactionContextManager.currentContext().flatMapMany(context ->
						Flux.usingWhen(
							createTransactionIfNecessary(rtm, txAttr, joinpointIdentification),
							tx -> {
								try {
									return this.adapter.toPublisher(invocation.proceedWithInvocation());
								}
								catch (Throwable ex) {
									return Mono.error(ex);
								}
							},
							this::commitTransactionAfterReturning,
							this::completeTransactionAfterThrowing,
							this::rollbackTransactionOnCancel)
						.onErrorMap(this::unwrapIfResourceCleanupFailure))
					.contextWrite(TransactionContextManager.getOrCreateContext())
					.contextWrite(TransactionContextManager.getOrCreateContextHolder()));
		}

		@SuppressWarnings("serial")
		private Mono<ReactiveTransactionInfo> createTransactionIfNecessary(ReactiveTransactionManager tm,
				@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {

			// If no name specified, apply method identification as transaction name.
			if (txAttr != null && txAttr.getName() == null) {
				txAttr = new DelegatingTransactionAttribute(txAttr) {
					@Override
					public String getName() {
						return joinpointIdentification;
					}
				};
			}

			final TransactionAttribute attrToUse = txAttr;
			Mono<ReactiveTransaction> tx = (attrToUse != null ? tm.getReactiveTransaction(attrToUse) : Mono.empty());
			return tx.map(it -> prepareTransactionInfo(tm, attrToUse, joinpointIdentification, it)).switchIfEmpty(
					Mono.defer(() -> Mono.just(prepareTransactionInfo(tm, attrToUse, joinpointIdentification, null))));
		}

		private ReactiveTransactionInfo prepareTransactionInfo(@Nullable ReactiveTransactionManager tm,
				@Nullable TransactionAttribute txAttr, String joinpointIdentification,
				@Nullable ReactiveTransaction transaction) {

			ReactiveTransactionInfo txInfo = new ReactiveTransactionInfo(tm, txAttr, joinpointIdentification);
			if (txAttr != null) {
				// We need a transaction for this method...
				if (logger.isTraceEnabled()) {
					logger.trace("Getting transaction for [" + txInfo.getJoinpointIdentification() + "]");
				}
				// The transaction manager will flag an error if an incompatible tx already exists.
				txInfo.newReactiveTransaction(transaction);
			}
			else {
				// The TransactionInfo.hasTransaction() method will return false. We created it only
				// to preserve the integrity of the ThreadLocal stack maintained in this class.
				if (logger.isTraceEnabled()) {
					logger.trace("Don't need to create transaction for [" + joinpointIdentification +
							"]: This method isn't transactional.");
				}
			}

			return txInfo;
		}

		private Mono<Void> commitTransactionAfterReturning(@Nullable ReactiveTransactionInfo txInfo) {
			if (txInfo != null && txInfo.getReactiveTransaction() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() + "]");
				}
				return txInfo.getTransactionManager().commit(txInfo.getReactiveTransaction());
			}
			return Mono.empty();
		}

		private Mono<Void> rollbackTransactionOnCancel(@Nullable ReactiveTransactionInfo txInfo) {
			if (txInfo != null && txInfo.getReactiveTransaction() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Rolling back transaction for [" + txInfo.getJoinpointIdentification() + "] after cancellation");
				}
				return txInfo.getTransactionManager().rollback(txInfo.getReactiveTransaction());
			}
			return Mono.empty();
		}

		private Mono<Void> completeTransactionAfterThrowing(@Nullable ReactiveTransactionInfo txInfo, Throwable ex) {
			if (txInfo != null && txInfo.getReactiveTransaction() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
							"] after exception: " + ex);
				}
				if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
					return txInfo.getTransactionManager().rollback(txInfo.getReactiveTransaction()).onErrorMap(ex2 -> {
								logger.error("Application exception overridden by rollback exception", ex);
								if (ex2 instanceof TransactionSystemException systemException) {
									systemException.initApplicationException(ex);
								}
								else {
									ex2.addSuppressed(ex);
								}
								return ex2;
							}
					);
				}
				else {
					// We don't roll back on this exception.
					// Will still roll back if TransactionStatus.isRollbackOnly() is true.
					return txInfo.getTransactionManager().commit(txInfo.getReactiveTransaction()).onErrorMap(ex2 -> {
								logger.error("Application exception overridden by commit exception", ex);
								if (ex2 instanceof TransactionSystemException systemException) {
									systemException.initApplicationException(ex);
								}
								else {
									ex2.addSuppressed(ex);
								}
								return ex2;
							}
					);
				}
			}
			return Mono.empty();
		}

		/**
		 * Unwrap the cause of a throwable, if produced by a failure
		 * during the async resource cleanup in {@link Flux#usingWhen}.
		 * @param ex the throwable to try to unwrap
		 */
		private Throwable unwrapIfResourceCleanupFailure(Throwable ex) {
			if (ex instanceof RuntimeException && ex.getCause() != null) {
				String msg = ex.getMessage();
				if (msg != null && msg.startsWith("Async resource cleanup failed")) {
					return ex.getCause();
				}
			}
			return ex;
		}
	}


	/**
	 * Opaque object used to hold transaction information for reactive methods.
	 */
	private static final class ReactiveTransactionInfo {

		@Nullable
		private final ReactiveTransactionManager transactionManager;

		@Nullable
		private final TransactionAttribute transactionAttribute;

		private final String joinpointIdentification;

		@Nullable
		private ReactiveTransaction reactiveTransaction;

		public ReactiveTransactionInfo(@Nullable ReactiveTransactionManager transactionManager,
				@Nullable TransactionAttribute transactionAttribute, String joinpointIdentification) {

			this.transactionManager = transactionManager;
			this.transactionAttribute = transactionAttribute;
			this.joinpointIdentification = joinpointIdentification;
		}

		public ReactiveTransactionManager getTransactionManager() {
			Assert.state(this.transactionManager != null, "No ReactiveTransactionManager set");
			return this.transactionManager;
		}

		@Nullable
		public TransactionAttribute getTransactionAttribute() {
			return this.transactionAttribute;
		}

		/**
		 * Return a String representation of this joinpoint (usually a Method call)
		 * for use in logging.
		 */
		public String getJoinpointIdentification() {
			return this.joinpointIdentification;
		}

		public void newReactiveTransaction(@Nullable ReactiveTransaction transaction) {
			this.reactiveTransaction = transaction;
		}

		@Nullable
		public ReactiveTransaction getReactiveTransaction() {
			return this.reactiveTransaction;
		}

		@Override
		public String toString() {
			return (this.transactionAttribute != null ? this.transactionAttribute.toString() : "No transaction");
		}
	}

}
