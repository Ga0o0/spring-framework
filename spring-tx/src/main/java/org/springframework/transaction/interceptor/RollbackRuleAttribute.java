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

package org.springframework.transaction.interceptor;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Rule determining whether a given exception should cause a rollback.
 *
 * <p>Multiple such rules can be applied to determine whether a transaction
 * should commit or rollback after an exception has been thrown.
 *
 * <p>Each rule is based on an exception type or exception pattern, supplied via
 * {@link #RollbackRuleAttribute(Class)} or {@link #RollbackRuleAttribute(String)},
 * respectively.
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
 * <p>See the javadocs for
 * {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * for further details on rollback rule semantics, patterns, and warnings regarding
 * possible unintentional matches with pattern-based rules.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 09.04.2003
 * @see NoRollbackRuleAttribute
 */
// 确定给定异常是否应导致回滚的规则。
//
// <p>可以应用多个这样的规则来确定在抛出异常后事务是否应提交或回滚。
//
// <p>每个规则都基于一种异常类型或异常模式，分别通过 {@link #RollbackRuleAttribute(Class)}
// 或 {@link #RollbackRuleAttribute(String)} 提供。
//
// <p>当使用异常类型定义回滚规则时，该类型将用于匹配抛出的异常类型及其超类型，从而提供类型安全性并避免使用模式时可能发生的任何意外匹配。
// 例如，{@code jakarta.servlet.ServletException.class} 的值将仅匹配类型为
// {@code jakarta.servlet.ServletException} 及其子类的抛出异常。
//
// <p>当回滚规则使用异常模式定义时，该模式可以是完全限定类名，也可以是异常类型（必须是 Throwable 的子类）的完全限定类名的子字符串，
// 目前不支持通配符。例如，值 {@code "jakarta.servlet.ServletException"} 或 {@code "ServletException"}
// 将匹配 {@code jakarta.servlet.ServletException} 及其子类。
//
// <p>有关回滚规则语义、模式以及与基于模式的规则可能出现的意外匹配相关的警告的更多详细信息，
// 请参阅 {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 javadoc。
@SuppressWarnings("serial")
public class RollbackRuleAttribute implements Serializable{

	/**
	 * The {@linkplain RollbackRuleAttribute rollback rule} for
	 * {@link RuntimeException RuntimeExceptions}.
	 */
	// {@link RuntimeException RuntimeExceptions} 的 {@linkplain RollbackRuleAttribute 回滚规则}。
	public static final RollbackRuleAttribute ROLLBACK_ON_RUNTIME_EXCEPTIONS =
			new RollbackRuleAttribute(RuntimeException.class);


	/**
	 * Exception pattern: used when searching for matches in a thrown exception's
	 * class hierarchy based on names of exceptions, with zero type safety and
	 * potentially resulting in unintentional matches for similarly named exception
	 * types and nested exception types.
	 */
	// 异常模式：用于根据异常名称在抛出的异常的类层次结构中搜索匹配项，具有零类型安全性，并且可能导致类似名称的异常类型和嵌套异常类型的意外匹配。
	private final String exceptionPattern;

	/**
	 * Exception type: used to ensure type safety when searching for matches in
	 * a thrown exception's class hierarchy.
	 * @since 6.0
	 */
	// 异常类型：用于在抛出的异常的类层次结构中搜索匹配项时确保类型安全。
	@Nullable
	private final Class<? extends Throwable> exceptionType;


	/**
	 * Create a new instance of the {@code RollbackRuleAttribute} class
	 * for the given {@code exceptionType}.
	 * <p>This is the preferred way to construct a rollback rule that matches
	 * the supplied exception type and its subclasses with type safety.
	 * <p>See the javadocs for
	 * {@link org.springframework.transaction.annotation.Transactional @Transactional}
	 * for further details on rollback rule semantics.
	 * @param exceptionType exception type; must be {@link Throwable} or a subclass
	 * of {@code Throwable}
	 * @throws IllegalArgumentException if the supplied {@code exceptionType} is
	 * not a {@code Throwable} type or is {@code null}
	 */
	// 为给定的 {@code exceptionType} 创建 {@code RollbackRuleAttribute} 类的新实例。
	// <p>这是构建与提供的异常类型及其子类匹配且类型安全的回滚规则的首选方法。
	// <p>有关回滚规则语义的更多详细信息，请参阅 {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 javadoc。
	// @param exceptionType 异常类型；必须是 {@link Throwable} 或 {@code Throwable} 的子类
	// @throws IllegalArgumentException，如果提供的 {@code exceptionType} 不是 {@code Throwable} 类型或为 {@code null}
	@SuppressWarnings("unchecked")
	public RollbackRuleAttribute(Class<?> exceptionType) {
		Assert.notNull(exceptionType, "'exceptionType' cannot be null");
		if (!Throwable.class.isAssignableFrom(exceptionType)) {
			throw new IllegalArgumentException(
					"Cannot construct rollback rule from [" + exceptionType.getName() + "]: it's not a Throwable");
		}
		this.exceptionPattern = exceptionType.getName();
		this.exceptionType = (Class<? extends Throwable>) exceptionType;
	}

	/**
	 * Create a new instance of the {@code RollbackRuleAttribute} class
	 * for the given {@code exceptionPattern}.
	 * <p>See the javadocs for
	 * {@link org.springframework.transaction.annotation.Transactional @Transactional}
	 * for further details on rollback rule semantics, patterns, and warnings regarding
	 * possible unintentional matches.
	 * <p>For improved type safety and to avoid unintentional matches, use
	 * {@link #RollbackRuleAttribute(Class)} instead.
	 * @param exceptionPattern the exception name pattern; can also be a fully
	 * package-qualified class name
	 * @throws IllegalArgumentException if the supplied {@code exceptionPattern}
	 * is {@code null} or empty
	 */
	// 为给定的 {@code exceptionPattern} 创建 {@code RollbackRuleAttribute} 类的新实例。
	// <p>有关回滚规则语义、模式和可能出现的意外匹配警告的更多详细信息，请参阅
	// {@link org.springframework.transaction.annotation.Transactional @Transactional} 的 javadoc。
	// <p>为了提高类型安全性并避免意外匹配，请改用 {@link #RollbackRuleAttribute(Class)}。
	// @param exceptionPattern 异常名称模式；也可以是完全包限定的类名
	// @throws IllegalArgumentException 如果提供的 {@code exceptionPattern} 为 {@code null} 或为空
	public RollbackRuleAttribute(String exceptionPattern) {
		Assert.hasText(exceptionPattern, "'exceptionPattern' cannot be null or empty");
		this.exceptionPattern = exceptionPattern;
		this.exceptionType = null;
	}


	/**
	 * Get the configured exception name pattern that this rule uses for matching.
	 * @see #getDepth(Throwable)
	 */
	// 获取此规则用于匹配的已配置异常名称模式。
	public String getExceptionName() {
		return this.exceptionPattern;
	}

	/**
	 * Return the depth of the superclass matching, with the following semantics.
	 * <ul>
	 * <li>{@code -1} means this rule does not match the supplied {@code exception}.</li>
	 * <li>{@code 0} means this rule matches the supplied {@code exception} directly.</li>
	 * <li>Any other positive value means this rule matches the supplied {@code exception}
	 * within the superclass hierarchy, where the value is the number of levels in the
	 * class hierarchy between the supplied {@code exception} and the exception against
	 * which this rule matches directly.</li>
	 * </ul>
	 * <p>When comparing roll back rules that match against a given exception, a rule
	 * with a lower matching depth wins. For example, a direct match ({@code depth == 0})
	 * wins over a match in the superclass hierarchy ({@code depth > 0}).
	 * <p>When constructed with an exception pattern via {@link #RollbackRuleAttribute(String)},
	 * a match against a nested exception type or similarly named exception type
	 * will return a depth signifying a match at the corresponding level in the
	 * class hierarchy as if there had been a direct match.
	 */
	// 返回超类匹配的深度，具有以下语义。
	// <ul>
	// <li>{@code -1} 表示此规则与提供的 {@code 异常} 不匹配。</li>
	// <li>{@code 0} 表示此规则直接与提供的 {@code 异常} 匹配。</li>
	// <li>任何其他正值均表示此规则与超类层次结构中提供的 {@code 异常} 匹配，
	// 其中该值是提供的 {@code 异常} 与此规则直接匹配的异常之间在类层次结构中的级别数。</li>
	// </ul>
	// <p>在比较与给定异常匹配的回滚规则时，匹配深度较低的规则胜出。
	// 例如，直接匹配 ({@codedepth == 0}) 胜过超类层次结构中的匹配 ({@codedepth> 0})。
	// <p>当通过 {@link #RollbackRuleAttribute(String)} 使用异常模式构造时，
	// 针对嵌套异常类型或类似命名的异常类型的匹配将返回一个深度，表示在类层次结构中相应级别的匹配，就好像存在直接匹配一样。
	public int getDepth(Throwable exception) {
		return getDepth(exception.getClass(), 0);
	}


	private int getDepth(Class<?> exceptionType, int depth) {
		if (this.exceptionType != null) {
			if (this.exceptionType.equals(exceptionType)) {
				// Found it!
				return depth;
			}
		}
		else if (exceptionType.getName().contains(this.exceptionPattern)) {
			// Found it!
			return depth;
		}
		// If we've gone as far as we can go and haven't found it...
		if (exceptionType == Throwable.class) {
			return -1;
		}
		return getDepth(exceptionType.getSuperclass(), depth + 1);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof RollbackRuleAttribute that &&
				this.exceptionPattern.equals(that.exceptionPattern)));
	}

	@Override
	public int hashCode() {
		return this.exceptionPattern.hashCode();
	}

	@Override
	public String toString() {
		return "RollbackRuleAttribute with pattern [" + this.exceptionPattern + "]";
	}

}
