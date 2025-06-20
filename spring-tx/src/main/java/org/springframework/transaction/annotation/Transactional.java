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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;

/**
 * Describes a transaction attribute on an individual method or on a class.
 *
 * <p>When this annotation is declared at the class level, it applies as a default
 * to all methods of the declaring class and its subclasses. Note that it does not
 * apply to ancestor classes up the class hierarchy; inherited methods need to be
 * locally redeclared in order to participate in a subclass-level annotation. For
 * details on method visibility constraints, consult the
 * <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html#transaction-declarative-annotations-method-visibility">Transaction Management</a>
 * section of the reference manual.
 *
 * <p>This annotation is generally directly comparable to Spring's
 * {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute}
 * class, and in fact {@link AnnotationTransactionAttributeSource} will directly
 * convert this annotation's attributes to properties in {@code RuleBasedTransactionAttribute},
 * so that Spring's transaction support code does not have to know about annotations.
 *
 * <h3>Attribute Semantics</h3>
 *
 * <p>If no custom rollback rules are configured in this annotation, the transaction
 * will roll back on {@link RuntimeException} and {@link Error} but not on checked
 * exceptions.
 *
 * <p>Rollback rules determine if a transaction should be rolled back when a given
 * exception is thrown, and the rules are based on types or patterns. Custom
 * rules may be configured via {@link #rollbackFor}/{@link #noRollbackFor} and
 * {@link #rollbackForClassName}/{@link #noRollbackForClassName}, which allow
 * rules to be specified as types or patterns, respectively.
 *
 * <p>When a rollback rule is defined with an exception type, that type will be
 * used to match against the type of a thrown exception and its super types,
 * providing type safety and avoiding any unintentional matches that may occur
 * when using a pattern. For example, a value of
 * {@code jakarta.servlet.ServletException.class} will only match thrown exceptions
 * of type {@code jakarta.servlet.ServletException} and its subclasses.
 *
 * <p>When a rollback rule is defined with an exception pattern, the pattern can
 * be a fully qualified class name or a substring of a fully qualified class name
 * for an exception type (which must be a subclass of {@code Throwable}), with no
 * wildcard support at present. For example, a value of
 * {@code "jakarta.servlet.ServletException"} or {@code "ServletException"} will
 * match {@code jakarta.servlet.ServletException} and its subclasses.
 *
 * <p><strong>WARNING:</strong> You must carefully consider how specific a pattern
 * is and whether to include package information (which isn't mandatory). For example,
 * {@code "Exception"} will match nearly anything and will probably hide other
 * rules. {@code "java.lang.Exception"} would be correct if {@code "Exception"}
 * were meant to define a rule for all checked exceptions. With more unique
 * exception names such as {@code "BaseBusinessException"} there is likely no
 * need to use the fully qualified class name for the exception pattern. Furthermore,
 * rollback rules defined via patterns may result in unintentional matches for
 * similarly named exceptions and nested classes. This is due to the fact that a
 * thrown exception is considered to be a match for a given pattern-based rollback
 * rule if the name of thrown exception contains the exception pattern configured
 * for the rollback rule. For example, given a rule configured to match against
 * {@code "com.example.CustomException"}, that rule will match against an exception
 * named {@code com.example.CustomExceptionV2} (an exception in the same package as
 * {@code CustomException} but with an additional suffix) or an exception named
 * {@code com.example.CustomException$AnotherException} (an exception declared as
 * a nested class in {@code CustomException}).
 *
 * <p>For specific information about the semantics of other attributes in this
 * annotation, consult the {@link org.springframework.transaction.TransactionDefinition}
 * and {@link org.springframework.transaction.interceptor.TransactionAttribute} javadocs.
 *
 * <h3>Transaction Management</h3>
 *
 * <p>This annotation commonly works with thread-bound transactions managed by a
 * {@link org.springframework.transaction.PlatformTransactionManager}, exposing a
 * transaction to all data access operations within the current execution thread.
 * <b>Note: This does NOT propagate to newly started threads within the method.</b>
 *
 * <p>Alternatively, this annotation may demarcate a reactive transaction managed
 * by a {@link org.springframework.transaction.ReactiveTransactionManager} which
 * uses the Reactor context instead of thread-local variables. As a consequence,
 * all participating data access operations need to execute within the same
 * Reactor context in the same reactive pipeline.
 *
 * <p><b>Note: When configured with a {@code ReactiveTransactionManager}, all
 * transaction-demarcated methods are expected to return a reactive pipeline.</b>
 * Void methods or regular return types need to be associated with a regular
 * {@code PlatformTransactionManager}, e.g. through {@link #transactionManager()}.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Mark Paluch
 * @since 1.2
 * @see org.springframework.transaction.interceptor.TransactionAttribute
 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 */
// 描述单个方法或类的事务属性。
//
// <p>当此注解在类级别声明时，它将默认应用于声明类及其子类的所有方法。
// 请注意，它不适用于类层次结构中的祖先类；继承的方法需要在本地重新声明才能参与子类级别的注解。
// 有关方法可见性约束的详细信息，请参阅参考手册中的
// <a href="https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html#transaction-declarative-annotations-method-visibility">事务管理</a>部分。
//
// <p>此注解通常直接与 Spring 的 {@link org.springframework.transaction.interceptor.RuleBasedTransactionAttribute} 类相当，
// 实际上 {@link AnnotationTransactionAttributeSource} 将直接将此注解的属性
// 转换为 {@code RuleBasedTransactionAttribute} 中的属性，以便 Spring 的事务支持代码不必了解注解。
//
// <h3>属性语义</h3>
//
// <p>如果此注解中未配置自定义回滚规则，则事务将在 {@link RuntimeException} 和 {@link Error} 上回滚，但不会在受检异常上回滚。
//
// <p>回滚规则确定在抛出给定异常时是否应回滚事务，并且规则基于类型或模式。
// 可以通过 {@link #rollbackFor}/{@link #noRollbackFor} 和
// {@link #rollbackForClassName}/{@link #noRollbackForClassName} 配置自定义规则，它们分别允许将规则指定为类型或模式。
//
// <p>当回滚规则使用异常类型定义时，该类型将用于匹配抛出异常的类型及其超类型，从而提供类型安全性并避免使用模式时可能发生的任何意外匹配。
// 例如，{@code jakarta.servlet.ServletException.class} 的值将仅匹配抛出的 {@code jakarta.servlet.ServletException} 类型及其子类的异常。
//
// <p>当回滚规则使用异常模式定义时，该模式可以是异常类型的完全限定类名或完全限定类名的子字符串（必须是 {@code Throwable} 的子类），目前不支持通配符。
// 例如，{@code "jakarta.servlet.ServletException"} 或 {@code "ServletException"} 的值将匹配 {@code jakarta.servlet.ServletException} 及其子类。
//
// <p><strong>警告：</strong>您必须仔细考虑模式的具体性以及是否包含包信息（包信息并非强制性要求）。
// 例如，{@code "Exception"} 几乎可以匹配任何内容，并且可能会隐藏其他规则。
// 如果 {@code "Exception"} 旨在为所有已检查异常定义规则，则 {@code "java.lang.Exception"} 是正确的。
// 如果异常名称更加独特，例如 {@code "BaseBusinessException"}，则可能无需使用异常模式的完全限定类名。
// 此外，通过模式定义的回滚规则可能会导致名称相似的异常和嵌套类意外匹配。
// 这是因为，如果抛出的异常名称包含为回滚规则配置的异常模式，则抛出的异常会被视为与给定基于模式的回滚规则匹配。
// 例如，给定一个配置为匹配 {@code “com.example.CustomException”} 的规则，
// 该规则将匹配名为 {@code com.example.CustomExceptionV2} 的异常（与 {@code CustomException} 位于同一包中但带有附加后缀的异常）
// 或名为 {@code com.example.CustomException$AnotherException} 的异常（在 {@code CustomException} 中声明为嵌套类的异常）。
//
// <p>有关此注解中其他属性的语义的具体信息，请参阅 {@link org.springframework.transaction.TransactionDefinition} 和
// {@link org.springframework.transaction.interceptor.TransactionAttribute} javadoc。
//
// <h3>事务管理</h3>
//
// <p>此注解通常与由 {@link org.springframework.transaction.PlatformTransactionManager} 管理的线程绑定事务一起使用，
// 将事务公开给当前执行线程内的所有数据访问操作。 <b>注意：此注解不会传播到方法内新启动的线程。</b>
//
// <p>或者，此注解可以划分由 {@link org.springframework.transaction.ReactiveTransactionManager} 管理的响应式事务，
// 该事务使用 Reactor 上下文而非线程局部变量。因此，所有参与的数据访问操作都需要在同一个响应式管道中的同一个 Reactor 上下文中执行。
//
// <p><b>注意：使用 {@code ReactiveTransactionManager} 配置时，所有事务划分的方法都应返回一个响应式管道。</b>
// 空方法或常规返回类型需要与常规 {@code PlatformTransactionManager} 关联，例如通过 {@link #transactionManager()}。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
public @interface Transactional {

	/**
	 * Alias for {@link #transactionManager}.
	 * @see #transactionManager
	 */
	// {@link #transactionManager} 的别名。
	@AliasFor("transactionManager")
	String value() default "";

	/**
	 * A <em>qualifier</em> value for the specified transaction.
	 * <p>May be used to determine the target transaction manager, matching the
	 * qualifier value (or the bean name) of a specific
	 * {@link org.springframework.transaction.TransactionManager TransactionManager}
	 * bean definition.
	 * @since 4.2
	 * @see #value
	 * @see org.springframework.transaction.PlatformTransactionManager
	 * @see org.springframework.transaction.ReactiveTransactionManager
	 */
	// 指定事务的<em>限定符</em>值。
	// <p>可用于确定目标事务管理器，匹配特定 {@link org.springframework.transaction.TransactionManager TransactionManager}
	// bean 定义的限定符值（或 bean 名称）。
	@AliasFor("value")
	String transactionManager() default "";

	/**
	 * Defines zero (0) or more transaction labels.
	 * <p>Labels may be used to describe a transaction, and they can be evaluated
	 * by individual transaction managers. Labels may serve a solely descriptive
	 * purpose or map to pre-defined transaction manager-specific options.
	 * <p>See the documentation of the actual transaction manager implementation
	 * for details on how it evaluates transaction labels.
	 * @since 5.3
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#getLabels()
	 */
	// 定义零 (0) 个或多个事务标签。
	// <p>标签可用于描述事务，并可由各个事务管理器进行评估。标签可以仅用于描述目的，也可以映射到预定义的事务管理器特定选项。
	// <p>有关如何评估事务标签的详细信息，请参阅实际事务管理器实现的文档。
	String[] label() default {};

	/**
	 * The transaction propagation type.
	 * <p>Defaults to {@link Propagation#REQUIRED}.
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
	 */
	// 事务传播类型。
	// <p>默认为 {@link Propagation#REQUIRED}。
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * The transaction isolation level.
	 * <p>Defaults to {@link Isolation#DEFAULT}.
	 * <p>Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions. Consider switching the "validateExistingTransactions" flag to
	 * "true" on your transaction manager if you'd like isolation level declarations
	 * to get rejected when participating in an existing transaction with a different
	 * isolation level.
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
	 */
	// 事务隔离级别。
	// <p>默认为 {@link Isolation#DEFAULT}。
	// <p>专为与 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW} 配合使用而设计，因为它仅适用于新启动的事务。
	// 如果您希望在参与具有不同隔离级别的现有事务时拒绝隔离级别声明，请考虑在事务管理器上将“validateExistingTransactions”标志切换为“true”。
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * The timeout for this transaction (in seconds).
	 * <p>Defaults to the default timeout of the underlying transaction system.
	 * <p>Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions.
	 * @return the timeout in seconds
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	// 此事务的超时时间（以秒为单位）。
	//* <p>默认为底层事务系统的默认超时时间。
	//* <p>专为与 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW} 配合使用而设计，因为它仅适用于新启动的事务。
	//* @return 超时时间（以秒为单位）。
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * The timeout for this transaction (in seconds).
	 * <p>Defaults to the default timeout of the underlying transaction system.
	 * <p>Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions.
	 * @return the timeout in seconds as a String value, e.g. a placeholder
	 * @since 5.3
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	// 此事务的超时时间（以秒为单位）。
	//* <p>默认为底层事务系统的默认超时时间。
	//* <p>专为与 {@link Propagation#REQUIRED} 或 {@link Propagation#REQUIRES_NEW} 配合使用而设计，因为它仅适用于新启动的事务。
	//* @return 以字符串值（例如占位符）形式返回超时时间（以秒为单位）
	String timeoutString() default "";

	/**
	 * A boolean flag that can be set to {@code true} if the transaction is
	 * effectively read-only, allowing for corresponding optimizations at runtime.
	 * <p>Defaults to {@code false}.
	 * <p>This just serves as a hint for the actual transaction subsystem;
	 * it will <i>not necessarily</i> cause failure of write access attempts.
	 * A transaction manager which cannot interpret the read-only hint will
	 * <i>not</i> throw an exception when asked for a read-only transaction
	 * but rather silently ignore the hint.
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	// 一个布尔标志，如果事务实际上是只读的，则可以设置为 {@code true}，以便在运行时进行相应的优化。
	// <p>默认为 {@code false}。<p>这仅作为对实际事务子系统的提示；它<i>不一定</i>会导致写入访问尝试失败。
	// 无法解释只读提示的事务管理器在被请求进行只读事务时<i>不会</i>抛出异常，而是会默默忽略该提示。
	boolean readOnly() default false;

	/**
	 * Defines zero (0) or more exception {@linkplain Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must cause
	 * a transaction rollback.
	 * <p>By default, a transaction will be rolled back on {@link RuntimeException}
	 * and {@link Error} but not on checked exceptions (business exceptions). See
	 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)}
	 * for a detailed explanation.
	 * <p>This is the preferred way to construct a rollback rule (in contrast to
	 * {@link #rollbackForClassName}), matching the exception type and its subclasses
	 * in a type-safe manner. See the {@linkplain Transactional class-level javadocs}
	 * for further details on rollback rule semantics.
	 * @see #rollbackForClassName
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	// 定义零 (0) 个或多个异常 {@linkplain Class types}，这些异常必须是 {@link Throwable} 的子类，用于指示哪些异常类型必须导致事务回滚。
	// <p>默认情况下，事务将在 {@link RuntimeException} 和 {@link Error} 上回滚，但不会在已检查的异常（业务异常）上回滚。
	// 有关详细说明，请参阅 {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)}。
	// <p>这是构建回滚规则的首选方法（与 {@link #rollbackForClassName} 相反），以类型安全的方式匹配异常类型及其子类。
	// 有关回滚规则语义的更多详细信息，请参阅 {@linkplain Transactional class-level javadocs}。
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}), indicating which exception types must cause
	 * a transaction rollback.
	 * <p>See the {@linkplain Transactional class-level javadocs} for further details
	 * on rollback rule semantics, patterns, and warnings regarding possible
	 * unintentional matches.
	 * @see #rollbackFor
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	// 定义零 (0) 个或多个异常名称模式（这些异常必须是 {@link Throwable} 的子类），指示哪些异常类型必须导致事务回滚。
	// <p>有关回滚规则语义、模式以及可能出现的意外匹配警告的更多详细信息，请参阅 {@linkplain Transactional 类级 javadocs}。
	String[] rollbackForClassName() default {};

	/**
	 * Defines zero (0) or more exception {@link Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must
	 * <b>not</b> cause a transaction rollback.
	 * <p>This is the preferred way to construct a rollback rule (in contrast to
	 * {@link #noRollbackForClassName}), matching the exception type and its subclasses
	 * in a type-safe manner. See the {@linkplain Transactional class-level javadocs}
	 * for further details on rollback rule semantics.
	 * @see #noRollbackForClassName
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	// 定义零 (0) 个或多个异常 {@link Class 类型}，这些类型必须是 {@link Throwable} 的子类，用于指示哪些异常类型<b>不能</b>导致事务回滚。
	// <p>这是构建回滚规则的首选方法（与 {@link #noRollbackForClassName} 相对），以类型安全的方式匹配异常类型及其子类。
	// 有关回滚规则语义的更多详细信息，请参阅 {@linkplain Transactional 类级 javadocs}。
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
	 * cause a transaction rollback.
	 * <p>See the {@linkplain Transactional class-level javadocs} for further details
	 * on rollback rule semantics, patterns, and warnings regarding possible
	 * unintentional matches.
	 * @see #noRollbackFor
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	// 定义零 (0) 个或多个异常名称模式（这些异常必须是 {@link Throwable} 的子类），指示哪些异常类型<b>不能</b>导致事务回滚。
	// <p>有关回滚规则语义、模式以及可能出现的意外匹配警告的更多详细信息，请参阅 {@linkplain Transactional 类级 javadocs}。
	String[] noRollbackForClassName() default {};

}
