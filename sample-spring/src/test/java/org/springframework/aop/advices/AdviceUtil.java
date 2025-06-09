package org.springframework.aop.advices;

import org.aopalliance.aop.Advice;
import org.springframework.aop.framework.AdvisedSupport;

public class AdviceUtil {

	public static void addAdvice(AdvisedSupport support, Advice advice) {
		support.addAdvice(advice);
	}

	public static void addDefaultAdvices(AdvisedSupport support) {
		support.addAdvice(new SimpleAfterReturningAdvice());
		support.addAdvice(new SimpleMethodBeforeAdvice());
		support.addAdvice(new SimpleThrowsAdvice());
	}

}
