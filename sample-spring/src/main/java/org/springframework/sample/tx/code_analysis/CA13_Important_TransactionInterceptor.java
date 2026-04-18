package org.springframework.sample.tx.code_analysis;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * TransactionInterceptor
 *
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#invokeWithinTransaction(java.lang.reflect.Method, Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback)
 *
 *
 * # 1. 获取 TransactionAttributeSource -> AnnotationTransactionAttributeSource
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#getTransactionAttributeSource()
 *
 * ## 1.1. {@code EnableTransactionManagement} 中 TransactionAttributeSource 的设置地点
 * @see org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration#transactionInterceptor(org.springframework.transaction.interceptor.TransactionAttributeSource)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 *
 * ## 1.2. {@code <tx:annotation-driven />} 中 TransactionAttributeSource 的设置地点
 * @see org.springframework.transaction.config.AnnotationDrivenBeanDefinitionParser.AopAutoProxyConfigurer#configureAutoProxyCreator(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 *
 *
 * # 2. 获取 TransactionAttribute -> AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#attributeCache
 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#computeTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#findTransactionAttribute(java.lang.reflect.Method)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#determineTransactionAttribute(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#annotationParsers
 *
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.TransactionAnnotationParser
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 *
 * ## 2.1. SpringTransactionAnnotationParser
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(org.springframework.core.annotation.AnnotationAttributes)
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * ## 2.2. JtaTransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see jakarta.transaction.Transactional
 *
 * ## 2.3. Ejb3TransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 * @see jakarta.ejb.TransactionAttribute
 *
 *
 * # 3. 确定 TransactionManager
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#determineTransactionManager(org.springframework.transaction.interceptor.TransactionAttribute)
 * @see org.springframework.transaction.TransactionManager
 *
 * # 4. 如果 TransactionManager 是 org.springframework.transaction.ReactiveTransactionManager 类型，调用 ReactiveTransactionSupport#invokeWithinTransaction() 方法处理并返回
 *
 * @see org.springframework.transaction.ReactiveTransactionManager
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.ReactiveTransactionSupport#invokeWithinTransaction(java.lang.reflect.Method, Class, org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback, org.springframework.transaction.interceptor.TransactionAttribute, org.springframework.transaction.ReactiveTransactionManager)
 *
 * # 5. 步骤判断不成功（TransactionManager 不是 org.springframework.transaction.ReactiveTransactionManager 类型），进行下一步，将 TransactionManager 转换成 org.springframework.transaction.PlatformTransactionManager
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#asPlatformTransactionManager(Object)
 *
 * # 6. 如有需要，根据给定的 TransactionAttribute 创建事务
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#createTransactionIfNecessary(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String)
 *
 * ## 6.1. 处理传播行为
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
 *
 * ### 6.1.1. 返回当前事务状态的事务对象
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
 *
 * ### 6.1.2. 检查给定的事务对象是否存在现有事务（即已启动的事务）；如果存在现有事务，为现有事务创建 org.springframework.transaction.TransactionStatus
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#isExistingTransaction(java.lang.Object)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#handleExistingTransaction(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean)
 *
 * ### 6.1.3. 处理事务超时
 *
 * ### 6.1.4. 开启新的事务
 *
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#startTransaction(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean, boolean, org.springframework.transaction.support.AbstractPlatformTransactionManager.SuspendedResourcesHolder)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#newTransactionStatus(org.springframework.transaction.TransactionDefinition, java.lang.Object, boolean, boolean, boolean, boolean, java.lang.Object)
 * @see org.springframework.transaction.TransactionExecutionListener#beforeBegin(org.springframework.transaction.TransactionExecution)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object, org.springframework.transaction.TransactionDefinition)
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#prepareSynchronization(org.springframework.transaction.support.DefaultTransactionStatus, org.springframework.transaction.TransactionDefinition)
 * @see org.springframework.transaction.TransactionExecutionListener#afterBegin(org.springframework.transaction.TransactionExecution, java.lang.Throwable)
 *
 * ## 6.2. 根据给定的属性和状态对象准备一个 TransactionInfo 对象
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#prepareTransactionInfo(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, java.lang.String, org.springframework.transaction.TransactionStatus)
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#TransactionInfo(org.springframework.transaction.PlatformTransactionManager, org.springframework.transaction.interceptor.TransactionAttribute, String)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#newTransactionStatus(org.springframework.transaction.TransactionStatus)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#bindToThread()
 *
 *
 * # 7。 执行目标调用
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.InvocationCallback#proceedWithInvocation()
 * @see org.aopalliance.intercept.Joinpoint#proceed()
 *
 * # 8. 处理 throwable，完成事务。我们可能会提交或回滚，具体取决于配置。
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#completeTransactionAfterThrowing(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo, java.lang.Throwable)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#transactionAttribute
 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn(Throwable)
 *
 * ## 8.1. txInfo.transactionAttribute.rollbackOn(ex) = true
 *
 * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
 *
 * ## 8.2. txInfo.transactionAttribute.rollbackOn(ex) = false
 *
 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
 *
 *
 * # 9. 重置 TransactionInfo ThreadLocal
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#cleanupTransactionInfo(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo)
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo#restoreThreadLocalStatus()
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#transactionInfoHolder
 * @see java.lang.ThreadLocal#set(Object)
 *
 * # 10. 调用成功完成后执行，但不在处理异常后执行
 *
 * @see org.springframework.transaction.interceptor.TransactionAspectSupport#commitTransactionAfterReturning(org.springframework.transaction.interceptor.TransactionAspectSupport.TransactionInfo)
 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
 */
public class CA13_Important_TransactionInterceptor {

	/**
	 * @see TransactionInterceptor
	 */
	// public class TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor, Serializable {
	static class CA01_TransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor {
	// static class CA01_TransactionInterceptor extends TransactionInterceptor {
		@Override
		@Nullable
		public Object invoke(MethodInvocation invocation) throws Throwable {
			// Work out the target class: may be {@code null}.
			// The TransactionAttributeSource should be passed the target class
			// as well as the method, which may be from an interface.
			// --> 译文：确定目标类：可能是 {@code null}。TransactionAttributeSource 应该传递目标类以及方法，该方法可能来自接口。
			Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

			// Adapt to TransactionAspectSupport's invokeWithinTransaction... --> 译文：适配 TransactionAspectSupport 的 invokeWithinTransaction...
			return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
			// ->
		}
	}

	/**
	 * @see TransactionAspectSupport
	 */
	static class CAO2_TransactionAspectSupport extends TransactionAspectSupport {
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
			// ...

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

				// ...

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
							// ...
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

	}

	// 回调事务模型中 Throwable 的内部持有者类。
	private static class ThrowableHolder {
		@Nullable
		public Throwable throwable;
	}

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


}
