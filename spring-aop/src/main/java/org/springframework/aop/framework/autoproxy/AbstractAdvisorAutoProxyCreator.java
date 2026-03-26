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

package org.springframework.aop.framework.autoproxy;

import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 *
 * <p>Subclasses may override the {@link #findCandidateAdvisors()} method to
 * return a custom list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 *
 * <p>Advisors or advices requiring ordering should be annotated with
 * {@link org.springframework.core.annotation.Order @Order} or implement the
 * {@link org.springframework.core.Ordered} interface. This class sorts
 * advisors using the {@link AnnotationAwareOrderComparator}. Advisors that are
 * not annotated with {@code @Order} or don't implement the {@code Ordered}
 * interface will be considered as unordered; they will appear at the end of the
 * advisor chain in an undefined order.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
// 通用自动代理创建器，根据检测到的每个 bean 的 Advisor 为特定 bean 构建 AOP 代理。
//
// <p>子类可以重写 {@link #findCandidateAdvisors()} 方法，以返回适用于任何对象的自定义 Advisor 列表。
// 子类还可以重写继承的 {@link #shouldSkip} 方法，以从自动代理中排除某些对象。
//
// <p>需要排序的 Advisor 或建议应使用 {@link org.springframework.core.annotation.Order @Order}
// 注解或实现 {@link org.springframework.core.Ordered} 接口。
// 此类使用 {@link AnnotationAwareOrderComparator} 对 Advisor 进行排序。
// 未使用 {@code @Order} 注解或未实现 {@code Ordered} 接口的 Advisor 将被视为无序的；它们将以未定义的顺序出现在 Advisor 链的末尾。
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	@Nullable
	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory clbf)) {
			throw new IllegalArgumentException(
					"AdvisorAutoProxyCreator requires a ConfigurableListableBeanFactory: " + beanFactory);
		}
		initBeanFactory(clbf);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}


	@Override
	@Nullable
	protected Object[] getAdvicesAndAdvisorsForBean(
			Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {

		// 查找所有符合条件的 Advisor 来自动代理此类
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * @param beanClass the clazz to find advisors for
	 * @param beanName the name of the currently proxied bean
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	// 查找所有符合条件的 Advisor 来自动代理此类。
	// @param beanClass 指定要查找 Advisor 的类
	// @param beanName 当前代理的 bean 的名称
	// @return 返回空列表，如果不存在切入点或拦截器，则返回 null 值
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 查找所有用于自动代理的候选 Advisors
		// -> 	AbstractAdvisorAutoProxyCreator#findCandidateAdvisors()
		// 		AnnotationAwareAspectJAutoProxyCreator#findCandidateAdvisors()
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 搜索给定的候选 Advisor，找到所有适用于指定 Bean 的 Advisor
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName); // apply ClassFilter/MethodMatcher
		extendAdvisors(eligibleAdvisors); // 扩展 Advisors
		if (!eligibleAdvisors.isEmpty()) {
			try {
				eligibleAdvisors = sortAdvisors(eligibleAdvisors);
			}
			catch (BeanCreationException ex) {
				// Advisor 排序失败，并创建了意外的 Bean，这可能是由于自定义使用了 Ordered 接口。请考虑改用 @Order 注解。
				throw new AopConfigException("Advisor sorting failed with unexpected bean creation, probably due " +
						"to custom use of the Ordered interface. Consider using the @Order annotation instead.", ex);
			}
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	// 查找所有用于自动代理的候选 Advisors
	// @return 候选顾问列表
	protected List<Advisor> findCandidateAdvisors() {
		Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * Search the given candidate Advisors to find all Advisors that
	 * can apply to the specified bean.
	 * @param candidateAdvisors the candidate Advisors
	 * @param beanClass the target's bean class
	 * @param beanName the target's bean name
	 * @return the List of applicable Advisors
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	// 搜索给定的候选 Advisor，找到所有适用于指定 Bean 的 Advisor。
	// @param candidatesAdvisors 候选 Advisor
	// @param beanClass 目标 Bean 类
	// @param beanName 目标 Bean 名称
	// @return 适用 Advisor 列表
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

		// 设置当前代理 Bean 实例的名称。
		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			// apply ClassFilter/MethodMatcher
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * @param beanName the name of the Advisor bean
	 * @return whether the bean is eligible
	 */
	// 返回具有给定名称的 Advisor bean 是否有资格进行代理。
	// @param beanName Advisor bean 的名称
	// @return 该 bean 是否有资格
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * @param advisors the source List of Advisors
	 * @return the sorted List of Advisors
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	// 根据顺序对顾问进行排序。子类可以选择重写此方法来自定义排序策略。
	// @param advisors 源顾问列表
	// @return 排序后的顾问列表
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * Extension hook that subclasses can override to register additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * <p>The default implementation is empty.
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * @param candidateAdvisors the Advisors that have already been identified as
	 * applying to a given bean
	 */
	// 子类可以重写扩展钩子，以便根据迄今为止获得的已排序的 Advisor 来注册其他 Advisor。
	// <p>默认实现为空。
	// <p>通常用于添加 Advisor，以公开后续 Advisor 所需的上下文信息。
	// @param candidatesAdvisors 已确定为
	// 应用于给定 bean 的 Advisor
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	 * This auto-proxy creator always returns pre-filtered Advisors.
	 */
	// 这个自动代理创建器总是返回预先筛选过的顾问。
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
	 */
	// BeanFactoryAdvisorRetrievalHelper 的子类，委托给周围的 AbstractAdvisorAutoProxyCreator 设施。
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
