package org.springframework.sample.aop.code_analysis;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.DeclareParentsAdvisor;
import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.AspectMetadata;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectJAdvisorsBuilder;
import org.springframework.aop.aspectj.annotation.LazySingletonAspectInstanceFactoryDecorator;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.PrototypeAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConvertingComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.comparator.InstanceComparator;

import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnnotationAwareAspectJAutoProxyCreator 重要方法 - AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors() -> 查找所有用于自动代理的候选 Advisors
 *
 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
 *
 * ## 1. 查找所有用于自动代理的候选 Advisors
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 *
 * ## 2. 在当前 bean 工厂中查找带有 AspectJ 注解的切面 bean，并返回代表它们的 Spring AOP Advisor 列表
 *
 * @see org.springframework.aop.aspectj.annotation.BeanFactoryAspectJAdvisorsBuilder#buildAspectJAdvisors()
 * @see org.springframework.aop.aspectj.annotation.AspectMetadata#AspectMetadata(java.lang.Class, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvisors(org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory)
 *
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvisor(java.lang.reflect.Method, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl#InstantiationModelAwarePointcutAdvisorImpl(org.springframework.aop.aspectj.AspectJExpressionPointcut, java.lang.reflect.Method, org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 * @see org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl#instantiateAdvice(org.springframework.aop.aspectj.AspectJExpressionPointcut)
 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvice(java.lang.reflect.Method, org.springframework.aop.aspectj.AspectJExpressionPointcut, org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory, int, java.lang.String)
 */
public class CodeAnalysis11_AnnotationAwareAspectJAutoProxyCreator {

	/**
	 * AspectJAwareAdvisorAutoProxyCreator
	 *
	 * @see org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
	 */
	static class CA01_AnnotationAwareAspectJAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
		@Serial
		private static final long serialVersionUID = 1L;
		private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;

		@Override
		protected List<Advisor> findCandidateAdvisors() {
			// 添加所有根据超类规则找到的 Spring advisors
			List<Advisor> advisors = super.findCandidateAdvisors();
			// 为 Bean Factory 中的所有 AspectJ aspects 构建 Advisors
			if (this.aspectJAdvisorsBuilder != null) {
				advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors()); // important -> go
			}
			return advisors;
		}
	}

	/**
	 * @see org.springframework.aop.aspectj.annotation.BeanFactoryAspectJAdvisorsBuilder#buildAspectJAdvisors()
	 */
	static class CA02_BeanFactoryAspectJAdvisorsBuilder extends BeanFactoryAspectJAdvisorsBuilder {
		private final ListableBeanFactory beanFactory;
		private final AspectJAdvisorFactory advisorFactory;
		private volatile List<String> aspectBeanNames;
		private final Map<String, List<Advisor>> advisorsCache = new ConcurrentHashMap<>();
		private final Map<String, MetadataAwareAspectInstanceFactory> aspectFactoryCache = new ConcurrentHashMap<>();
		public CA02_BeanFactoryAspectJAdvisorsBuilder(ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
			super(beanFactory, advisorFactory);
			Assert.notNull(beanFactory, "ListableBeanFactory must not be null");
			Assert.notNull(advisorFactory, "AspectJAdvisorFactory must not be null");
			this.beanFactory = beanFactory;
			this.advisorFactory = advisorFactory;
		}

		// 在当前 bean 工厂中查找带有 AspectJ 注解的切面 bean，并返回代表它们的 Spring AOP Advisor 列表。
		// <p>为每个 AspectJ 建议方法创建一个 Spring Advisor。
		public List<Advisor> buildAspectJAdvisors() {
			List<String> aspectNames = this.aspectBeanNames;

			if (aspectNames == null) {
				synchronized (this) {
					aspectNames = this.aspectBeanNames;
					if (aspectNames == null) {
						List<Advisor> advisors = new ArrayList<>();
						aspectNames = new ArrayList<>();
						String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
								this.beanFactory, Object.class, true, false);  // important -> go
						for (String beanName : beanNames) {
							if (!isEligibleBean(beanName)) {
								continue;
							}
							// 我们必须小心，不要急于实例化 bean，因为在这种情况下，它们会被 Spring 容器缓存，但不会被织入。
							Class<?> beanType = this.beanFactory.getType(beanName, false);
							if (beanType == null) {
								continue;
							}
							if (this.advisorFactory.isAspect(beanType)) {
								try {
									AspectMetadata amd = new AspectMetadata(beanType, beanName);  // important -> go
									if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
										MetadataAwareAspectInstanceFactory factory =
												new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
										List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);  // important -> go
										if (this.beanFactory.isSingleton(beanName)) {
											this.advisorsCache.put(beanName, classAdvisors);
										}
										else {
											this.aspectFactoryCache.put(beanName, factory);
										}
										advisors.addAll(classAdvisors);
									}
									else {
										// Per target or per this.
										if (this.beanFactory.isSingleton(beanName)) {
											throw new IllegalArgumentException("Bean with name '" + beanName +
													"' is a singleton, but aspect instantiation model is not singleton");
										}
										MetadataAwareAspectInstanceFactory factory =
												new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
										this.aspectFactoryCache.put(beanName, factory);
										advisors.addAll(this.advisorFactory.getAdvisors(factory));  // important -> go
									}
									aspectNames.add(beanName);
								}
								catch (IllegalArgumentException | IllegalStateException | AopConfigException ex) {
									// ...
								}
							}
						}
						this.aspectBeanNames = aspectNames;
						return advisors;
					}
				}
			}

			if (aspectNames.isEmpty()) {
				return Collections.emptyList();
			}
			List<Advisor> advisors = new ArrayList<>();
			for (String aspectName : aspectNames) {
				List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
				if (cachedAdvisors != null) {
					advisors.addAll(cachedAdvisors);
				}
				else {
					MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
					advisors.addAll(this.advisorFactory.getAdvisors(factory));
				}
			}
			return advisors;
		}
	}

	/**
	 * @see org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory#getAdvisors(org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory)
	 */
	 static class CA03_ReflectiveAspectJAdvisorFactory extends ReflectiveAspectJAdvisorFactory {
		@Serial
		private static final long serialVersionUID = 1L;
		// Exclude @Pointcut methods
		private static final ReflectionUtils.MethodFilter adviceMethodFilter = ReflectionUtils.USER_DECLARED_METHODS
				.and(method -> (AnnotationUtils.getAnnotation(method, Pointcut.class) == null));

		private static final Comparator<Method> adviceMethodComparator;

		static {
			// 注意：虽然 @After 注解的顺序在 @AfterReturning 和 @AfterThrowing 之前，
			// 但实际上 @After 注解方法会在 @AfterReturning 和 @AfterThrowing 方法之后调用。
			//
			// 这是因为 AspectJAfterAdvice.invoke(MethodInvocation) 在 try 代码块中调用了 proceed() 方法，而 @After 注解方法仅在相应的 finally 代码块中调用。
			Comparator<Method> adviceKindComparator = new ConvertingComparator<>(
					new InstanceComparator<>(
							Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class),
					(Converter<Method, Annotation>) method -> {
						AspectJAnnotation ann = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(method);
						return (ann != null ? ann.getAnnotation() : null);
					});
			Comparator<Method> methodNameComparator = new ConvertingComparator<>(Method::getName);
			adviceMethodComparator = adviceKindComparator.thenComparing(methodNameComparator);
		}

		@Nullable
		private final BeanFactory beanFactory = null;

		@Override
		public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
			Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
			String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
			validate(aspectClass);

			// 我们需要用装饰器包装 MetadataAwareAspectInstanceFactory，使其只实例化一次。
			MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
					new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

			List<Advisor> advisors = new ArrayList<>();
			for (Method method : getAdvisorMethods(aspectClass)) { // 获取被 AspectJ 注解标记的 Methods // important -> go
				if (method.equals(ClassUtils.getMostSpecificMethod(method, aspectClass))) {
					// 在 Spring Framework 5.2.7 之前，advisors.size() 被用作 getAdvisor(...) 的 declarationOrderInAspect 参数，以表示已声明方法列表中的“当前位置”。
					// 然而，自 Java 7 起，“当前位置”不再有效，因为 JDK 不再按照源代码中声明的顺序返回已声明的方法。
					// 因此，为了确保 JVM 启动时通知顺序的可靠性，我们现在将所有通过反射发现的通知方法的 declarationOrderInAspect 硬编码为 0。
					// 具体来说，该值 0 与 AspectJPrecedenceComparator.getAspectDeclarationOrder(Advisor) 中使用的默认值一致。
					Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, 0, aspectName); // important -> go
					if (advisor != null) {
						advisors.add(advisor);
					}
				}
			}

			// 如果是针对每个目标的方面，则发出虚拟实例化方面。
			if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
				Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
				advisors.add(0, instantiationAdvisor);
			}

			// 查找 introduction 字段
			for (Field field : aspectClass.getDeclaredFields()) {
				Advisor advisor = getDeclareParentsAdvisor(field);
				if (advisor != null) {
					advisors.add(advisor);
				}
			}

			return advisors;
		}

		@Override
		@Nullable
		public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
								  int declarationOrderInAspect, String aspectName) {

			validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());

			AspectJExpressionPointcut expressionPointcut = getPointcut(candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
			if (expressionPointcut == null) {
				return null;
			}

			try {
				/*return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
						this, aspectInstanceFactory, declarationOrderInAspect, aspectName);*/ // important -> go
				return new CA04_InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
						this, aspectInstanceFactory, declarationOrderInAspect, aspectName); // important -> go
			} catch (IllegalArgumentException | IllegalStateException ex) {
				// ...
				return null;
			}
		}

		// important -> go
		// important -> go
		// important -> go
		/*@Override
		@Nullable
		public Advice getAdvice(Method candidateAdviceMethod, AspectJExpressionPointcut expressionPointcut,
								MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {

			Class<?> candidateAspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
			validate(candidateAspectClass);

			AspectJAnnotation aspectJAnnotation = AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(candidateAdviceMethod);
			if (aspectJAnnotation == null) {
				return null;
			}

			// 如果执行到这里，我们就知道这是一个 AspectJ 方法。检查它是否是一个带有 AspectJ 注解的类。
			if (!isAspect(candidateAspectClass)) {
				// ...
			}
			// ...

			AbstractAspectJAdvice springAdvice;

			switch (aspectJAnnotation.getAnnotationType()) {
				case AtPointcut -> {
					if (logger.isDebugEnabled()) {
						logger.debug("Processing pointcut '" + candidateAdviceMethod.getName() + "'");
					}
					return null;
				}
				case AtAround -> springAdvice = new AspectJAroundAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				case AtBefore -> springAdvice = new AspectJMethodBeforeAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				case AtAfter -> springAdvice = new AspectJAfterAdvice(
						candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
				case AtAfterReturning -> {
					springAdvice = new AspectJAfterReturningAdvice(
							candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
					AfterReturning afterReturningAnnotation = (AfterReturning) aspectJAnnotation.getAnnotation();
					if (StringUtils.hasText(afterReturningAnnotation.returning())) {
						springAdvice.setReturningName(afterReturningAnnotation.returning());
					}
				}
				case AtAfterThrowing -> {
					springAdvice = new AspectJAfterThrowingAdvice(
							candidateAdviceMethod, expressionPointcut, aspectInstanceFactory);
					AfterThrowing afterThrowingAnnotation = (AfterThrowing) aspectJAnnotation.getAnnotation();
					if (StringUtils.hasText(afterThrowingAnnotation.throwing())) {
						springAdvice.setThrowingName(afterThrowingAnnotation.throwing());
					}
				}
				default -> throw new UnsupportedOperationException(
						"Unsupported advice type on method: " + candidateAdviceMethod);
			}

			// 现在来配置 advice...
			springAdvice.setAspectName(aspectName);
			springAdvice.setDeclarationOrder(declarationOrder);
			String[] argNames = this.parameterNameDiscoverer.getParameterNames(candidateAdviceMethod);
			if (argNames != null) {
				springAdvice.setArgumentNamesFromStringArray(argNames);
			}
			springAdvice.calculateArgumentBindings();

			return springAdvice;
		}*/

		@Nullable
		private AspectJExpressionPointcut getPointcut(Method candidateAdviceMethod, Class<?> candidateAspectClass) {
			// ...
			AspectJExpressionPointcut ajexp =
					new AspectJExpressionPointcut(candidateAspectClass, new String[0], new Class<?>[0]);
			// ...
			return ajexp;
		}

		private List<Method> getAdvisorMethods(Class<?> aspectClass) {
			List<Method> methods = new ArrayList<>();
			ReflectionUtils.doWithMethods(aspectClass, methods::add, adviceMethodFilter);
			if (methods.size() > 1) {
				methods.sort(adviceMethodComparator);
			}
			return methods;
		}

		// 为给定的 introduction 字段构建一个 {@link org.springframework.aop.aspectj.DeclareParentsAdvisor}。
		//
		// <p>生成的 Advisor 需要进行目标评估。
		@Nullable
		private Advisor getDeclareParentsAdvisor(Field introductionField) {
			DeclareParents declareParents = introductionField.getAnnotation(DeclareParents.class);
			// ...
			return new DeclareParentsAdvisor(introductionField.getType(), declareParents.value(), declareParents.defaultImpl());
		}
	}

	/**
	 * @see org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl
	 */
	static class CA04_InstantiationModelAwarePointcutAdvisorImpl
			implements Advisor {
		private static final Advice EMPTY_ADVICE = new Advice() {};
		private final AspectJExpressionPointcut declaredPointcut;
		private final AspectJAdvisorFactory aspectJAdvisorFactory;
		private transient Method aspectJAdviceMethod;
		private final MetadataAwareAspectInstanceFactory aspectInstanceFactory;
		private final int declarationOrder;
		private final String aspectName;
		private Advice instantiatedAdvice;

		public CA04_InstantiationModelAwarePointcutAdvisorImpl(AspectJExpressionPointcut declaredPointcut,
					  Method aspectJAdviceMethod, AspectJAdvisorFactory aspectJAdvisorFactory,
					  MetadataAwareAspectInstanceFactory aspectInstanceFactory, int declarationOrder, String aspectName) {
			this.declaredPointcut = declaredPointcut;
			// ...
			this.aspectJAdviceMethod = aspectJAdviceMethod;
			this.aspectJAdvisorFactory = aspectJAdvisorFactory;
			this.aspectInstanceFactory = aspectInstanceFactory;
			this.declarationOrder = declarationOrder;
			this.aspectName = aspectName;

			if (aspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
				// ...
			}
			else {
				// ...
				this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut); // important -> go
			}
		}

		private Advice instantiateAdvice(AspectJExpressionPointcut pointcut) {
			Advice advice = this.aspectJAdvisorFactory.getAdvice(this.aspectJAdviceMethod, pointcut,
					this.aspectInstanceFactory, this.declarationOrder, this.aspectName); // important -> go
			return (advice != null ? advice : EMPTY_ADVICE);
		}

		@Override
		public Advice getAdvice() {
			return null;
		}
		@Override
		public boolean isPerInstance() {
			return Advisor.super.isPerInstance();
		}
	}
}
