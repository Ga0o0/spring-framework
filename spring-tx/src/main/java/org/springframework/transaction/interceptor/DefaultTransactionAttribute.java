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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * Spring's common transaction attribute implementation.
 * Rolls back on runtime, but not checked, exceptions by default.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Mark Paluch
 * @since 16.03.2003
 */
// Spring 的通用事务属性实现。
// 默认情况下，在运行时（而非受检）发生异常时进行回滚。
@SuppressWarnings("serial")
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	@Nullable
	private String descriptor;

	@Nullable
	private String timeoutString;

	@Nullable
	private String qualifier;

	private Collection<String> labels = Collections.emptyList();


	/**
	 * Create a new {@code DefaultTransactionAttribute} with default settings.
	 * Can be modified through bean property setters.
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute() {
		super();
	}

	/**
	 * Copy constructor. Definition can be modified through bean property setters.
	 * @see #setPropagationBehavior
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 * @see #setName
	 */
	public DefaultTransactionAttribute(TransactionAttribute other) {
		super(other);
	}

	/**
	 * Create a new {@code DefaultTransactionAttribute} with the given
	 * propagation behavior. Can be modified through bean property setters.
	 * @param propagationBehavior one of the propagation constants in the
	 * TransactionDefinition interface
	 * @see #setIsolationLevel
	 * @see #setTimeout
	 * @see #setReadOnly
	 */
	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}


	/**
	 * Set a descriptor for this transaction attribute,
	 * e.g. indicating where the attribute is applying.
	 * @since 4.3.4
	 */
	// 为此事务属性设置描述符，例如指示该属性的应用位置。
	public void setDescriptor(@Nullable String descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * Return a descriptor for this transaction attribute,
	 * or {@code null} if none.
	 * @since 4.3.4
	 */
	@Nullable
	public String getDescriptor() {
		return this.descriptor;
	}

	/**
	 * Set the timeout to apply, if any,
	 * as a String value that resolves to a number of seconds.
	 * @since 5.3
	 * @see #setTimeout
	 * @see #resolveAttributeStrings
	 */
	// 将要应用的超时时间（如果有）设置为解析为秒数的字符串值。
	public void setTimeoutString(@Nullable String timeoutString) {
		this.timeoutString = timeoutString;
	}

	/**
	 * Return the timeout to apply, if any,
	 * as a String value that resolves to a number of seconds.
	 * @since 5.3
	 * @see #getTimeout
	 * @see #resolveAttributeStrings
	 */
	@Nullable
	public String getTimeoutString() {
		return this.timeoutString;
	}

	/**
	 * Associate a qualifier value with this transaction attribute.
	 * <p>This may be used for choosing a corresponding transaction manager
	 * to process this specific transaction.
	 * @since 3.0
	 * @see #resolveAttributeStrings
	 */
	public void setQualifier(@Nullable String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * Return a qualifier value associated with this transaction attribute.
	 * @since 3.0
	 */
	@Override
	@Nullable
	public String getQualifier() {
		return this.qualifier;
	}

	/**
	 * Associate one or more labels with this transaction attribute.
	 * <p>This may be used for applying specific transactional behavior
	 * or follow a purely descriptive nature.
	 * @since 5.3
	 * @see #resolveAttributeStrings
	 */
	public void setLabels(Collection<String> labels) {
		this.labels = labels;
	}

	@Override
	public Collection<String> getLabels() {
		return this.labels;
	}

	/**
	 * The default behavior is as with EJB: rollback on unchecked exception
	 * ({@link RuntimeException}), assuming an unexpected outcome outside any
	 * business rules. Additionally, we also attempt to rollback on {@link Error} which
	 * is clearly an unexpected outcome as well. By contrast, a checked exception is
	 * considered a business exception and therefore a regular expected outcome of the
	 * transactional business method, i.e. a kind of alternative return value which
	 * still allows for regular completion of resource operations.
	 * <p>This is largely consistent with TransactionTemplate's default behavior,
	 * except that TransactionTemplate also rolls back on undeclared checked exceptions
	 * (a corner case). For declarative transactions, we expect checked exceptions to be
	 * intentionally declared as business exceptions, leading to a commit by default.
	 * @see org.springframework.transaction.support.TransactionTemplate#execute
	 */
	// 默认行为与 EJB 相同：在未检查异常（{@link RuntimeException}）上回滚，假设任何业务规则之外的意外结果。
	// 此外，我们还会尝试回滚 {@link Error}，这显然也是一个意外结果。相比之下，已检查异常被视为业务异常，
	// 因此是事务业务方法的常规预期结果，即一种仍然允许定期完成资源操作的替代返回值。
	// <p>这与 TransactionTemplate 的默认行为基本一致，只是 TransactionTemplate 还会在未声明的已检查异常（一个极端情况）上回滚。
	// 对于声明式事务，我们期望已检查异常被有意声明为业务异常，从而默认导致提交。
	@Override
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}


	/**
	 * Resolve attribute values that are defined as resolvable Strings:
	 * {@link #setTimeoutString}, {@link #setQualifier}, {@link #setLabels}.
	 * This is typically used for resolving "${...}" placeholders.
	 * @param resolver the embedded value resolver to apply, if any
	 * @since 5.3
	 */
	// 解析定义为可解析字符串的属性值：{@link #setTimeoutString}、{@link #setQualifier}、{@link #setLabels}。这通常用于解析 “${...}” 占位符。
	// @param resolver 要应用的嵌入值解析器（如果有）
	public void resolveAttributeStrings(@Nullable StringValueResolver resolver) {
		String timeoutString = this.timeoutString;
		if (StringUtils.hasText(timeoutString)) {
			if (resolver != null) {
				timeoutString = resolver.resolveStringValue(timeoutString);
			}
			if (StringUtils.hasLength(timeoutString)) {
				try {
					setTimeout(Integer.parseInt(timeoutString));
				}
				catch (RuntimeException ex) {
					throw new IllegalArgumentException(
							"Invalid timeoutString value \"" + timeoutString + "\"; " + ex);
				}
			}
		}

		if (resolver != null) {
			if (this.qualifier != null) {
				this.qualifier = resolver.resolveStringValue(this.qualifier);
			}
			Set<String> resolvedLabels = new LinkedHashSet<>(this.labels.size());
			for (String label : this.labels) {
				resolvedLabels.add(resolver.resolveStringValue(label));
			}
			this.labels = resolvedLabels;
		}
	}

	/**
	 * Return an identifying description for this transaction attribute.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected final StringBuilder getAttributeDescription() {
		StringBuilder result = getDefinitionDescription();
		if (StringUtils.hasText(this.qualifier)) {
			result.append("; '").append(this.qualifier).append('\'');
		}
		if (!this.labels.isEmpty()) {
			result.append("; ").append(this.labels);
		}
		return result;
	}

}
