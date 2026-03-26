package org.springframework.sample.aop.aspect.schema_based;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 基于 Schema 定义 Aspect
 *
 * <p>不再像 {@link org.springframework.sample.aop.aspect.aspectj_based.AspectjAspect}
 * 一样使用 @Aspect 注解声明切面，而改用 schema 的形式（即：<aop:config>）
 *
 * @see org.springframework.sample.aop.aspect.aspectj_based.AspectjAspect
 */
public class SchemaAspect {

	/**
	 * 环绕通知
	 *
	 * @param pjp pjp
	 * @return obj
	 * @throws Throwable exception
	 */
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("-----------------------");
		System.out.println("环绕通知: 进入方法");
		Object o = pjp.proceed();
		System.out.println("环绕通知: 退出方法");
		return o;
	}

	/**
	 * 前置通知
	 */
	public void doBefore() {
		System.out.println("前置通知");
	}

	/**
	 * 后置返回通知
	 *
	 * @param result return val
	 */
	public void doAfterReturning(String result) {
		System.out.println("后置返回通知, 返回值: " + result);
	}

	/**
	 * 后置异常通知
	 *
	 * @param e exception
	 */
	public void doAfterThrowing(Exception e) {
		System.out.println("后置异常通知, 异常: " + e.getMessage());
	}

	/**
	 * 后置通知
	 */
	public void doAfter() {
		System.out.println("后置通知");
	}

}