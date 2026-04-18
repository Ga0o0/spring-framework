package org.springframework.sample.tx.code_analysis;

import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser;
import org.springframework.transaction.annotation.JtaTransactionAnnotationParser;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * AnnotationTransactionAttributeSource
 *
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource()
 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#AnnotationTransactionAttributeSource(boolean)
 */
public class CA11_Important_AnnotationTransactionAttributeSource {

	// public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource implements Serializable { ... }
	static class CA01_AnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {
		private static final boolean jta12Present;
		private static final boolean ejb3Present;
		static {
			ClassLoader classLoader = AnnotationTransactionAttributeSource.class.getClassLoader();
			jta12Present = ClassUtils.isPresent("jakarta.transaction.Transactional", classLoader);
			ejb3Present = ClassUtils.isPresent("jakarta.ejb.TransactionAttribute", classLoader);
		}
		private final boolean publicMethodsOnly;
		private final Set<TransactionAnnotationParser> annotationParsers;

		// 创建一个默认的 AnnotationTransactionAttributeSource，
		// 支持带有 {@code Transactional} 注释或 EJB3 {@link jakarta.ejb.TransactionAttribute} 注释的公共方法。
		public CA01_AnnotationTransactionAttributeSource() {
			this(true);
		}

		// 创建自定义 AnnotationTransactionAttributeSource，
		// 支持带有 {@code Transactional} 注释或
		// EJB3 {@link jakarta.ejb.TransactionAttribute} 注释的公共方法。
		// @param publicMethodsOnly 是否仅支持带有 {@code Transactional} 注释的公共方法（通常用于基于代理的 AOP），
		// 或者也支持受保护/私有方法（通常用于 AspectJ 类编织）
		public CA01_AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
			this.publicMethodsOnly = publicMethodsOnly;
			if (jta12Present || ejb3Present) {
				this.annotationParsers = new LinkedHashSet<>(4);
				// 用于解析 Spring 的 @Transactional 注解的策略实现。
				this.annotationParsers.add(new SpringTransactionAnnotationParser());
				if (jta12Present) {
					// 用于解析 JTA 1.2 的 @jakarta.transaction.Transactional 注解的策略实现。
					this.annotationParsers.add(new JtaTransactionAnnotationParser());
				}
				if (ejb3Present) {
					// 用于解析 EJB3 的 @jakarta.ejb.TransactionAttribute 注解的策略实现。
					this.annotationParsers.add(new Ejb3TransactionAnnotationParser());
				}
			}
			else {
				this.annotationParsers = Collections.singleton(new SpringTransactionAnnotationParser());
			}
		}
	}

}
