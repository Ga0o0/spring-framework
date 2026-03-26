package org.springframework.sample.aop.code_analysis;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. AbstractAutoProxyCreator#getAdvicesAndAdvisorsForBean(...)
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
 *
 * 2. AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 *
 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
 */
public class CodeAnalysis10_AbstractAdvisorAutoProxyCreator {

	/**
	 * 查找所有符合条件的 Advisor 来自动代理此类
	 *
	 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
	 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean(java.lang.Class, java.lang.String, org.springframework.aop.TargetSource)
	 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findEligibleAdvisors(java.lang.Class, java.lang.String)
	 *
	 * @see org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
	 * @see org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper#findAdvisorBeans()
	 */
	static abstract class CA01_AbstractAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {
		@Serial
		private static final long serialVersionUID = 1L;
		@Nullable
		private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;

		@Override
		@Nullable
		protected Object[] getAdvicesAndAdvisorsForBean(
				Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

			// 查找所有符合条件的 Advisor 来自动代理此类
			List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName); // important -> go
			if (advisors.isEmpty()) {
				return DO_NOT_PROXY;
			}
			return advisors.toArray();
		}

		// 查找所有符合条件的 Advisor 来自动代理此类
		protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
			// 查找所有用于自动代理的候选 Advisors
			// -> 	AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
			// 		AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
			List<Advisor> candidateAdvisors = findCandidateAdvisors(); // important -> go
			// 找到所有适用于指定 Bean 的 Advisor
			List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName); // apply ClassFilter/MethodMatcher
			extendAdvisors(eligibleAdvisors); // 扩展 Advisors
			if (!eligibleAdvisors.isEmpty()) {
				try {
					eligibleAdvisors = sortAdvisors(eligibleAdvisors);
				}
				catch (BeanCreationException ex) {
					// ...
				}
			}
			return eligibleAdvisors;
		}

		// 查找所有用于自动代理的候选 Advisors
		// @return 候选顾问列表
		protected List<Advisor> findCandidateAdvisors() {
			Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
			return this.advisorRetrievalHelper.findAdvisorBeans(); // important -> go
		}
	}

	/**
	 * @see org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper#findAdvisorBeans()
	 */
	static class CA03_BeanFactoryAdvisorRetrievalHelper extends BeanFactoryAdvisorRetrievalHelper {
		private final ConfigurableListableBeanFactory beanFactory;
		private volatile String[] cachedAdvisorBeanNames;
		public CA03_BeanFactoryAdvisorRetrievalHelper(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
			this.beanFactory = beanFactory;
		}

		// 在当前 bean 工厂中查找所有符合条件的 Advisor bean，忽略 FactoryBeans 并排除当前正在创建的 bean。
		public List<Advisor> findAdvisorBeans() {
			// 如果尚未缓存，则确定 advisor bean 名称列表。
			String[] advisorNames = this.cachedAdvisorBeanNames;
			if (advisorNames == null) {
				// 不要在此处初始化 FactoryBeans：我们需要将所有常规 bean 保持未初始化状态，以便自动代理创建器可以应用于它们！
				advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
						this.beanFactory, Advisor.class, true, false); // important -> go
				this.cachedAdvisorBeanNames = advisorNames;
			}
			if (advisorNames.length == 0) {
				return new ArrayList<>();
			}

			List<Advisor> advisors = new ArrayList<>();
			for (String name : advisorNames) {
				if (isEligibleBean(name)) { // 判断指定名称的切面 bean 是否符合条件。
					if (this.beanFactory.isCurrentlyInCreation(name)) {
						// ...
					}
					else {
						try {
							advisors.add(this.beanFactory.getBean(name, Advisor.class)); // important -> go
						}
						catch (BeanCreationException ex) {
							Throwable rootCause = ex.getMostSpecificCause();
							if (rootCause instanceof BeanCurrentlyInCreationException bce) {
								String bceBeanName = bce.getBeanName();
								if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
									// ...
									// 忽略：表示指向我们试图为其提供建议的 bean 的引用。我们希望找到除当前创建的 bean 本身之外的其他顾问。
									continue;
								}
							}
							throw ex;
						}
					}
				}
			}
			return advisors;
		}
	}

}
