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
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringValueResolver;

/**
 * Simple {@link TransactionAttributeSource} implementation that
 * allows attributes to be matched by registered name.
 *
 * @author Juergen Hoeller
 * @since 21.08.2003
 * @see #isMatch
 * @see MethodMapTransactionAttributeSource
 */
// 简单的 {@link TransactionAttributeSource} 实现允许属性通过注册名称进行匹配。
@SuppressWarnings("serial")
public class NameMatchTransactionAttributeSource
		implements TransactionAttributeSource, EmbeddedValueResolverAware, InitializingBean, Serializable {

	/**
	 * Logger available to subclasses.
	 * <p>Static for optimal serialization.
	 */
	protected static final Log logger = LogFactory.getLog(NameMatchTransactionAttributeSource.class);

	/** Keys are method names; values are TransactionAttributes. */
	// 键是方法名称；值是事务属性。
	private final Map<String, TransactionAttribute> nameMap = new HashMap<>();

	@Nullable
	private StringValueResolver embeddedValueResolver;


	/**
	 * Set a name/attribute map, consisting of method names
	 * (e.g. "myMethod") and {@link TransactionAttribute} instances.
	 * @see #setProperties
	 * @see TransactionAttribute
	 */
	public void setNameMap(Map<String, TransactionAttribute> nameMap) {
		nameMap.forEach(this::addTransactionalMethod);
	}

	/**
	 * Parse the given properties into a name/attribute map.
	 * <p>Expects method names as keys and String attributes definitions as values,
	 * parsable into {@link TransactionAttribute} instances via a
	 * {@link TransactionAttributeEditor}.
	 * @see #setNameMap
	 * @see TransactionAttributeEditor
	 */
	// 将给定的属性解析为名称/属性映射。
	// <p>期望方法名称作为键，字符串属性定义作为值，可通过 {@link TransactionAttributeEditor} 解析为 {@link TransactionAttribute} 实例。
	public void setProperties(Properties transactionAttributes) {
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		Enumeration<?> propNames = transactionAttributes.propertyNames();
		while (propNames.hasMoreElements()) {
			String methodName = (String) propNames.nextElement();
			String value = transactionAttributes.getProperty(methodName);
			tae.setAsText(value);
			TransactionAttribute attr = (TransactionAttribute) tae.getValue();
			addTransactionalMethod(methodName, attr);
		}
	}

	/**
	 * Add an attribute for a transactional method.
	 * <p>Method names can be exact matches, or of the pattern "xxx*",
	 * "*xxx", or "*xxx*" for matching multiple methods.
	 * @param methodName the name of the method
	 * @param attr attribute associated with the method
	 */
	public void addTransactionalMethod(String methodName, TransactionAttribute attr) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding transactional method [" + methodName + "] with attribute [" + attr + "]");
		}
		if (this.embeddedValueResolver != null && attr instanceof DefaultTransactionAttribute dta) {
			dta.resolveAttributeStrings(this.embeddedValueResolver);
		}
		this.nameMap.put(methodName, attr);
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	@Override
	public void afterPropertiesSet() {
		for (TransactionAttribute attr : this.nameMap.values()) {
			if (attr instanceof DefaultTransactionAttribute dta) {
				dta.resolveAttributeStrings(this.embeddedValueResolver);
			}
		}
	}


	@Override
	@Nullable
	public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
		if (!ClassUtils.isUserLevelMethod(method)) {
			return null;
		}

		// Look for direct name match. --> 译文：寻找直接名称匹配。
		String methodName = method.getName();
		TransactionAttribute attr = this.nameMap.get(methodName);

		if (attr == null) {
			// Look for most specific name match. --> 译文：寻找最具体的名称匹配。
			String bestNameMatch = null;
			for (String mappedName : this.nameMap.keySet()) {
				// 判断给定的方法名称是否与映射名称匹配。
				if (isMatch(methodName, mappedName) &&
						(bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
					attr = this.nameMap.get(mappedName);
					bestNameMatch = mappedName;
				}
			}
		}

		return attr;
	}

	/**
	 * Determine if the given method name matches the mapped name.
	 * <p>The default implementation checks for "xxx*", "*xxx", and "*xxx*" matches,
	 * as well as direct equality. Can be overridden in subclasses.
	 * @param methodName the method name of the class
	 * @param mappedName the name in the descriptor
	 * @return {@code true} if the names match
	 * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
	 */
	// 判断给定的方法名称是否与映射名称匹配。
	// <p>默认实现会检查 “xxx*” 、 “*xxx” 和 “*xxx*” 是否匹配，以及直接相等性。可在子类中重写。
	// @param methodName 类的方法名称
	// @param mappedName 描述符中的名称
	// @return 如果名称匹配，则返回 {@code true}
	protected boolean isMatch(String methodName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, methodName);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof NameMatchTransactionAttributeSource otherTas &&
				ObjectUtils.nullSafeEquals(this.nameMap, otherTas.nameMap)));
	}

	@Override
	public int hashCode() {
		return NameMatchTransactionAttributeSource.class.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.nameMap;
	}

}
