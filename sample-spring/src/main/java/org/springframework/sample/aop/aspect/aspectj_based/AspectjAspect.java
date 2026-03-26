package org.springframework.sample.aop.aspect.aspectj_based;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * 基于 @Aspect 定义 Aspect
 */
@Aspect
public class AspectjAspect {

	/**
	 * define point cut.
	 */
	@Pointcut("execution(* org.springframework.sample.aop.aspect.TargetService.*(..))") // 执行 TargetService 接口定义的任何方法
	private void pointCutMethod() {}

	/**
	 * 环绕通知
	 *
	 * @param pjp pjp
	 * @return obj
	 * @throws Throwable exception
	 */
	@Around("pointCutMethod()")
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
	@Before("pointCutMethod()")
	public void doBefore() {
		System.out.println("前置通知");
	}

	/**
	 * 后置返回通知
	 *
	 * @param result return val
	 */
	@AfterReturning(pointcut = "pointCutMethod()", returning = "result")
	public void doAfterReturning(String result) {
		System.out.println("后置返回通知, 返回值: " + result);
	}

	/**
	 * 后置异常通知
	 *
	 * @param e exception
	 */
	@AfterThrowing(pointcut = "pointCutMethod()", throwing = "e")
	public void doAfterThrowing(Exception e) {
		System.out.println("后置异常通知, 异常: " + e.getMessage());
	}

	/**
	 * 后置通知
	 */
	@After("pointCutMethod()")
	public void doAfter() {
		System.out.println("后置通知");
	}

}
