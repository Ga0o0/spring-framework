package org.springframework.sample.tx.code_analysis;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.JtaTransactionAnnotationParser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.SpringTransactionAnnotationParser;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanFactoryTransactionAttributeSourceAdvisor
 *
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#pointcut
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setTransactionAttributeSource(org.springframework.transaction.interceptor.TransactionAttributeSource)
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setAdviceBeanName(java.lang.String)
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#setAdvice(org.aopalliance.aop.Advice)
 *
 * # 1. pointcut
 *
 * @see org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor#pointcut
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut#matches(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.TransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, Class)
 *
 * ## 1.1. NameMatchTransactionAttributeSource##getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 *
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#nameMap
 * @see org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource#isMatch(String, String)
 *
 * ## 1.2. AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
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
 * ### 1.2.1. SpringTransactionAnnotationParser
 *
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser
 * @see org.springframework.transaction.annotation.Transactional
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(java.lang.reflect.AnnotatedElement)
 * @see org.springframework.transaction.annotation.SpringTransactionAnnotationParser#parseTransactionAnnotation(org.springframework.core.annotation.AnnotationAttributes)
 * @see org.springframework.transaction.interceptor.RuleBasedTransactionAttribute
 *
 * ### 1.2.2. JtaTransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.JtaTransactionAnnotationParser
 * @see jakarta.transaction.Transactional
 *
 * ### 1.2.3. Ejb3TransactionAnnotationParser -> 省略细节
 *
 * @see org.springframework.transaction.annotation.Ejb3TransactionAnnotationParser
 * @see jakarta.ejb.TransactionAttribute
 */
public class CA12_Important_BeanFactoryTransactionAttributeSourceAdvisor {

	/**
	 * @see BeanFactoryTransactionAttributeSourceAdvisor
	 */
	// public class BeanFactoryTransactionAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor { ... }
	static class CA01_BeanFactoryTransactionAttributeSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {
		// private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut(); // -> 源码
		private final CA02_TransactionAttributeSourcePointcut pointcut = new CA02_TransactionAttributeSourcePointcut();

		// ...
		@Override
		public Pointcut getPointcut() {
			return this.pointcut;
		}
	}

	/**
	 * @see org.springframework.transaction.interceptor.TransactionAttributeSourcePointcut
	 */
	// final class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {
	static final class CA02_TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut {
		@Nullable
		private TransactionAttributeSource transactionAttributeSource;
		public void setTransactionAttributeSource(@Nullable TransactionAttributeSource transactionAttributeSource) {
			this.transactionAttributeSource = transactionAttributeSource;
		}
		// ...
		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return (this.transactionAttributeSource == null ||
					this.transactionAttributeSource.getTransactionAttribute(method, targetClass) != null);
			// -> AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
		}
	}

	/**
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
	 * @see org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource#getTransactionAttribute(java.lang.reflect.Method, java.lang.Class)
	 */
	// public abstract class AbstractFallbackTransactionAttributeSource
	//		implements TransactionAttributeSource, EmbeddedValueResolverAware { ... }
	static abstract class CA03_AbstractFallbackTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource {
	// static abstract class CA03_AbstractFallbackTransactionAttributeSource implements TransactionAttributeSource, EmbeddedValueResolverAware {

		// 缓存中保存的规范值指示未找到此方法的事务属性，我们不需要再次查找。
		@SuppressWarnings("serial")
		private static final TransactionAttribute NULL_TRANSACTION_ATTRIBUTE = new DefaultTransactionAttribute() {
			@Override
			public String toString() {
				return "null";
			}
		};

		// TransactionAttributes 的缓存，以特定目标类上的方法为键。
		// <p>由于此基类未标记为可序列化，因此缓存将在序列化后重新创建 - 前提是具体子类是可序列化的。
		private final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);
		private transient StringValueResolver embeddedValueResolver;

		// 确定此方法调用的事务属性。
		// <p>如果未找到方法属性，则默认为类的事务属性。
		// @param method 当前调用的方法（永不为 null）
		// @param targetClass 本次调用的目标类（可以为 null）
		// @return 此方法的 TransactionAttribute 属性，如果方法不是事务性的，则返回 null
		@Override
		@Nullable
		public TransactionAttribute getTransactionAttribute(Method method, @Nullable Class<?> targetClass) {
			if (method.getDeclaringClass() == Object.class) {
				return null;
			}

			// 为给定方法和目标类确定缓存键。 -> MethodClassKey
			Object cacheKey = getCacheKey(method, targetClass);
			TransactionAttribute cached = this.attributeCache.get(cacheKey);

			if (cached != null) {
				return (cached != NULL_TRANSACTION_ATTRIBUTE ? cached : null);
			}
			else {
				TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);
				if (txAttr != null) {
					// 返回给定方法的限定名，由完全限定接口/类名 + "." + 方法名组成。
					String methodIdentification = ClassUtils.getQualifiedMethodName(method, targetClass);
					if (txAttr instanceof DefaultTransactionAttribute dta) {
						// 为此事务属性设置描述符，例如指示该属性的应用位置。
						dta.setDescriptor(methodIdentification);
						// 解析定义为可解析字符串的属性值：timeoutString、qualifier、labels。
						dta.resolveAttributeStrings(this.embeddedValueResolver);
					}
					if (logger.isTraceEnabled()) {
						logger.trace("Adding transactional method '" + methodIdentification + "' with attribute: " + txAttr);
					}
					this.attributeCache.put(cacheKey, txAttr);
				}
				else {
					this.attributeCache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);
				}
				return txAttr;
			}
		}
	}

	/**
	 * AnnotationTransactionAttributeSource
	 *
	 * @see org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
	 */
	// public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource
	//		implements Serializable { ... }
	static abstract class CA04_AnnotationTransactionAttributeSource extends CA03_AbstractFallbackTransactionAttributeSource {
		private static final boolean jta12Present;
		private static final boolean ejb3Present;
		static {
			ClassLoader classLoader = AnnotationTransactionAttributeSource.class.getClassLoader();
			jta12Present = ClassUtils.isPresent("jakarta.transaction.Transactional", classLoader);
			ejb3Present = ClassUtils.isPresent("jakarta.ejb.TransactionAttribute", classLoader);
		}
		private final boolean publicMethodsOnly;
		private final Set<TransactionAnnotationParser> annotationParsers;

		// 创建自定义 AnnotationTransactionAttributeSource，
		// 支持带有 {@code Transactional} 注释或
		// EJB3 {@link jakarta.ejb.TransactionAttribute} 注释的公共方法。
		// @param publicMethodsOnly 是否仅支持带有 {@code Transactional} 注释的公共方法（通常用于基于代理的 AOP），
		// 或者也支持受保护/私有方法（通常用于 AspectJ 类编织）
		public CA04_AnnotationTransactionAttributeSource(boolean publicMethodsOnly) {
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

		@Override
		@Nullable
		protected TransactionAttribute findTransactionAttribute(Method method) {
			return determineTransactionAttribute(method);
		}

		// 确定给定方法或类的事务属性。
		// <p>此实现委托已配置的 {@link TransactionAnnotationParser TransactionAnnotationParsers}
		// 将已知注解解析为 Spring 的元数据属性类。如果非事务性，则返回 {@code null}。
		// <p>可以重写以支持带有事务元数据的自定义注解。
		// @param element 带注解的方法或类
		// @return 已配置的事务属性，如果未找到，则返回 {@code null}
		@Nullable
		protected TransactionAttribute determineTransactionAttribute(AnnotatedElement element) {
			for (TransactionAnnotationParser parser : this.annotationParsers) {
				TransactionAttribute attr = parser.parseTransactionAnnotation(element);
				if (attr != null) {
					return attr;
				}
			}
			return null;
		}
	}

	/**
	 *
	 */
	// public class SpringTransactionAnnotationParser implements TransactionAnnotationParser, Serializable {
	static class CA05_SpringTransactionAnnotationParser extends SpringTransactionAnnotationParser {
		@Override
		@Nullable
		public TransactionAttribute parseTransactionAnnotation(AnnotatedElement element) {
			AnnotationAttributes attributes = AnnotatedElementUtils.findMergedAnnotationAttributes(
					element, Transactional.class, false, false);
			if (attributes != null) {
				// 根据 @Transactional 注解来封装一个 NameMatchTransactionAttributeSource
				return parseTransactionAnnotation(attributes);
			}
			else {
				return null;
			}
		}
		// ...

		// 类似于 TxAdviceBeanDefinitionParser#parseAttributeSource() 处理 NameMatchTransactionAttributeSource 的逻辑
		// 根据 @Transactional 注解来封装一个 NameMatchTransactionAttributeSource
		protected TransactionAttribute parseTransactionAnnotation(AnnotationAttributes attributes) {
			RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();

			Propagation propagation = attributes.getEnum("propagation");
			rbta.setPropagationBehavior(propagation.value());
			Isolation isolation = attributes.getEnum("isolation");
			rbta.setIsolationLevel(isolation.value());

			rbta.setTimeout(attributes.getNumber("timeout").intValue());
			String timeoutString = attributes.getString("timeoutString");
			Assert.isTrue(!StringUtils.hasText(timeoutString) || rbta.getTimeout() < 0,
					"Specify 'timeout' or 'timeoutString', not both");
			rbta.setTimeoutString(timeoutString);

			rbta.setReadOnly(attributes.getBoolean("readOnly"));
			rbta.setQualifier(attributes.getString("value"));
			rbta.setLabels(Set.of(attributes.getStringArray("label")));

			// 回滚和不回滚规则列表
			List<RollbackRuleAttribute> rollbackRules = new ArrayList<>();
			// 处理属性 rollbackFor，并封装成 RollbackRuleAttribute
			for (Class<?> rbRule : attributes.getClassArray("rollbackFor")) {
				rollbackRules.add(new RollbackRuleAttribute(rbRule));
			}
			// 处理属性 rollbackForClassName，并封装成 RollbackRuleAttribute
			for (String rbRule : attributes.getStringArray("rollbackForClassName")) {
				rollbackRules.add(new RollbackRuleAttribute(rbRule));
			}
			// 处理属性 noRollbackFor，并封装成 NoRollbackRuleAttribute
			for (Class<?> rbRule : attributes.getClassArray("noRollbackFor")) {
				rollbackRules.add(new NoRollbackRuleAttribute(rbRule));
			}
			// 处理属性 noRollbackForClassName，并封装成 NoRollbackRuleAttribute
			for (String rbRule : attributes.getStringArray("noRollbackForClassName")) {
				rollbackRules.add(new NoRollbackRuleAttribute(rbRule));
			}
			rbta.setRollbackRules(rollbackRules);

			return rbta;
		}
		// ...
	}

}
