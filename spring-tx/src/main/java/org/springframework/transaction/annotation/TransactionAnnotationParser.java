/*
 * Copyright 2002-2019 the original author or authors.
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

import java.lang.reflect.AnnotatedElement;

import org.springframework.lang.Nullable;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Strategy interface for parsing known transaction annotation types.
 * {@link AnnotationTransactionAttributeSource} delegates to such
 * parsers for supporting specific annotation types such as Spring's own
 * {@link Transactional}, JTA 1.2's {@link jakarta.transaction.Transactional}
 * or EJB3's {@link jakarta.ejb.TransactionAttribute}.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see AnnotationTransactionAttributeSource
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see JtaTransactionAnnotationParser
 */
// 用于解析已知事务注释类型的策略接口。
// {@link AnnotationTransactionAttributeSource} 委托给此类解析器来支持特定的注释类型，
// 例如 Spring 自己的 {@link Transactional}、JTA 1.2 的 {@link jakarta.transaction.Transactional}
// 或 EJB3 的 {@link jakarta.ejb.TransactionAttribute}。
public interface TransactionAnnotationParser {

	/**
	 * Determine whether the given class is a candidate for transaction attributes
	 * in the annotation format of this {@code TransactionAnnotationParser}.
	 * <p>If this method returns {@code false}, the methods on the given class
	 * will not get traversed for {@code #parseTransactionAnnotation} introspection.
	 * Returning {@code false} is therefore an optimization for non-affected
	 * classes, whereas {@code true} simply means that the class needs to get
	 * fully introspected for each method on the given class individually.
	 * @param targetClass the class to introspect
	 * @return {@code false} if the class is known to have no transaction
	 * annotations at class or method level; {@code true} otherwise. The default
	 * implementation returns {@code true}, leading to regular introspection.
	 * @since 5.2
	 */
	// 确定给定的类是否是此 {@code TransactionAnnotationParser} 的注释格式中的事务属性的候选者。
	// <p>如果此方法返回 {@code false}，则给定类上的方法将不会被遍历以进行 {@code #parseTransactionAnnotation} 自省。
	// 因此，返回 {@code false} 是对不受影响的类的优化，而 {@code true} 仅表示该类需要针对给定类上的每个方法单独进行完全自省。
	// @param targetClass 要自省的类
	// @return 如果已知该类在类或方法级别没有事务注释，则返回 {@code false}；否则返回 {@code true}。默认实现返回 {@code true}，从而进行常规自省。
	default boolean isCandidateClass(Class<?> targetClass) {
		return true;
	}

	/**
	 * Parse the transaction attribute for the given method or class,
	 * based on an annotation type understood by this parser.
	 * <p>This essentially parses a known transaction annotation into Spring's metadata
	 * attribute class. Returns {@code null} if the method/class is not transactional.
	 * @param element the annotated method or class
	 * @return the configured transaction attribute, or {@code null} if none found
	 * @see AnnotationTransactionAttributeSource#determineTransactionAttribute
	 */
	// 根据此解析器能够理解的注解类型，解析给定方法或类的事务属性。
	// <p>这本质上是将已知的事务注解解析为 Spring 的元数据属性类。
	// 如果方法/类不支持事务，则返回 {@code null}。
	// @param element 带注解的方法或类
	// @return 配置的事务属性，如果未找到则返回 {@code null}
	@Nullable
	TransactionAttribute parseTransactionAnnotation(AnnotatedElement element);

}
