package org.springframework.aop.advices;

import org.springframework.aop.MethodBeforeAdvice;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Simple MethodBeforeAdvice
 * @see org.springframework.aop.MethodBeforeAdvice
 * @see org.springframework.aop.framework.adapter.DefaultAdvisorAdapterRegistry#getInterceptors(org.springframework.aop.Advisor)
 * @see org.springframework.aop.framework.adapter.AdvisorAdapter
 * @see org.springframework.aop.framework.adapter.MethodBeforeAdviceAdapter
 */
public class SimpleMethodBeforeAdvice implements MethodBeforeAdvice {
	@Override
	public void before(@Nonnull Method method, @Nonnull Object[] args, Object target) throws Throwable {
		System.out.println("SimpleMethodBeforeAdvice.before()");
	}
}
