package org.springframework.aop.advices;

import org.springframework.aop.AfterReturningAdvice;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Simple AfterReturningAdviceAdapter
 * @see org.springframework.aop.AfterReturningAdvice
 * @see org.springframework.aop.framework.adapter.DefaultAdvisorAdapterRegistry#getInterceptors(org.springframework.aop.Advisor)
 * @see org.springframework.aop.framework.adapter.AdvisorAdapter
 * @see org.springframework.aop.framework.adapter.AfterReturningAdviceAdapter
 */
public class SimpleAfterReturningAdvice implements AfterReturningAdvice {
	@Override
	public void afterReturning(Object returnValue, @Nonnull Method method, @Nonnull Object[] args, Object target) throws Throwable {
		System.out.println("SimpleAfterReturningAdvice.afterReturning()");
	}
}
