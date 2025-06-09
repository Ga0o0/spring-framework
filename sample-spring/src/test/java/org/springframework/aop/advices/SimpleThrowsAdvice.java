package org.springframework.aop.advices;

import org.springframework.aop.ThrowsAdvice;

/**
 * Simple ThrowsAdvice
 * @see org.springframework.aop.ThrowsAdvice
 * @see org.springframework.aop.framework.adapter.DefaultAdvisorAdapterRegistry#getInterceptors(org.springframework.aop.Advisor)
 * @see org.springframework.aop.framework.adapter.AdvisorAdapter
 * @see org.springframework.aop.framework.adapter.ThrowsAdviceAdapter
 */
public class SimpleThrowsAdvice implements ThrowsAdvice {

	public void afterThrowing(Exception ex) {
		System.out.println("SimpleThrowsAdvice.afterThrowing()");
		System.out.println(ex.getMessage());
	}

}
