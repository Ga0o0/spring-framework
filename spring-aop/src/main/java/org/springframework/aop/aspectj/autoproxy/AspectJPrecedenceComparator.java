/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.aop.aspectj.autoproxy;

import java.util.Comparator;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJAopUtils;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;

/**
 * Orders AspectJ advice/advisors by precedence (<i>not</i> invocation order).
 *
 * <p>Given two pieces of advice, {@code A} and {@code B}:
 * <ul>
 * <li>If {@code A} and {@code B} are defined in different aspects, then the advice
 * in the aspect with the lowest order value has the highest precedence.</li>
 * <li>If {@code A} and {@code B} are defined in the same aspect, if one of
 * {@code A} or {@code B} is a form of <em>after</em> advice, then the advice declared
 * last in the aspect has the highest precedence. If neither {@code A} nor {@code B}
 * is a form of <em>after</em> advice, then the advice declared first in the aspect
 * has the highest precedence.</li>
 * </ul>
 *
 * <p>Important: This comparator is used with AspectJ's
 * {@link org.aspectj.util.PartialOrder PartialOrder} sorting utility. Thus, unlike
 * a normal {@link Comparator}, a return value of {@code 0} from this comparator
 * means we don't care about the ordering, not that the two elements must be sorted
 * identically.
 *
 * @author Adrian Colyer
 * @author Juergen Hoeller
 * @since 2.0
 */
// AspectJ 建议/顾问按优先级排序（<i>而非</i>调用顺序）。
//
// <p>假设有两个建议 {@code A} 和 {@code B}：
// <ul>
// <li>如果 {@code A} 和 {@code B} 定义在不同的切面中，则优先级值最低的切面中的建议优先级最高。</li>
// <li>如果 {@code A} 和 {@code B} 定义在同一个切面中，且 {@code A} 或 {@code B} 中有一个是 <em>after</em> 建议，则该切面中最后声明的建议优先级最高。
// 	   如果 {@code A} 和 {@code B} 都不是 <em>after</em> 通知，则切面中第一个声明的通知优先级最高。</li>
// </ul>
//
// <p>重要提示：此比较器与 AspectJ 的 {@link org.aspectj.util.PartialOrder PartialOrder} 排序工具一起使用。
// 因此，与普通的 {@link Comparator} 不同，此比较器返回 {@code 0} 表示我们不关心顺序，而不是两个元素必须排序相同。
class AspectJPrecedenceComparator implements Comparator<Advisor> {

	private static final int HIGHER_PRECEDENCE = -1;

	private static final int SAME_PRECEDENCE = 0;

	private static final int LOWER_PRECEDENCE = 1;


	private final Comparator<? super Advisor> advisorComparator;


	/**
	 * Create a default {@code AspectJPrecedenceComparator}.
	 */
	public AspectJPrecedenceComparator() {
		this.advisorComparator = AnnotationAwareOrderComparator.INSTANCE;
	}

	/**
	 * Create an {@code AspectJPrecedenceComparator}, using the given {@link Comparator}
	 * for comparing {@link org.springframework.aop.Advisor} instances.
	 * @param advisorComparator the {@code Comparator} to use for advisors
	 */
	public AspectJPrecedenceComparator(Comparator<? super Advisor> advisorComparator) {
		Assert.notNull(advisorComparator, "Advisor comparator must not be null");
		this.advisorComparator = advisorComparator;
	}


	@Override
	public int compare(Advisor o1, Advisor o2) {
		int advisorPrecedence = this.advisorComparator.compare(o1, o2);
		if (advisorPrecedence == SAME_PRECEDENCE && declaredInSameAspect(o1, o2)) {
			advisorPrecedence = comparePrecedenceWithinAspect(o1, o2);
		}
		return advisorPrecedence;
	}

	private int comparePrecedenceWithinAspect(Advisor advisor1, Advisor advisor2) {
		boolean oneOrOtherIsAfterAdvice =
				(AspectJAopUtils.isAfterAdvice(advisor1) || AspectJAopUtils.isAfterAdvice(advisor2));
		int adviceDeclarationOrderDelta = getAspectDeclarationOrder(advisor1) - getAspectDeclarationOrder(advisor2);

		if (oneOrOtherIsAfterAdvice) {
			// the advice declared last has higher precedence
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 was declared before advice2
				// so advice1 has lower precedence
				return LOWER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return HIGHER_PRECEDENCE;
			}
		}
		else {
			// the advice declared first has higher precedence
			if (adviceDeclarationOrderDelta < 0) {
				// advice1 was declared before advice2
				// so advice1 has higher precedence
				return HIGHER_PRECEDENCE;
			}
			else if (adviceDeclarationOrderDelta == 0) {
				return SAME_PRECEDENCE;
			}
			else {
				return LOWER_PRECEDENCE;
			}
		}
	}

	private boolean declaredInSameAspect(Advisor advisor1, Advisor advisor2) {
		return (hasAspectName(advisor1) && hasAspectName(advisor2) &&
				getAspectName(advisor1).equals(getAspectName(advisor2)));
	}

	private boolean hasAspectName(Advisor advisor) {
		return (advisor instanceof AspectJPrecedenceInformation ||
				advisor.getAdvice() instanceof AspectJPrecedenceInformation);
	}

	// pre-condition is that hasAspectName returned true
	private String getAspectName(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		Assert.state(precedenceInfo != null, () -> "Unresolvable AspectJPrecedenceInformation for " + advisor);
		return precedenceInfo.getAspectName();
	}

	private int getAspectDeclarationOrder(Advisor advisor) {
		AspectJPrecedenceInformation precedenceInfo = AspectJAopUtils.getAspectJPrecedenceInformationFor(advisor);
		return (precedenceInfo != null ? precedenceInfo.getDeclarationOrder() : 0);
	}

}
