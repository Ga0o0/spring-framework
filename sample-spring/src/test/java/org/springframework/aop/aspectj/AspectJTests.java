package org.springframework.aop.aspectj;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;

public class AspectJTests {

	@Test
	public void testAspectJ() {
		AspectJProxyFactory proxyFactory = new AspectJProxyFactory();

		proxyFactory.setTarget(new AopAtAspectServiceImpl());
		proxyFactory.setInterfaces(IProxyService.class);
		// setTarget() 必须在该方法之前；否则该方法执行时找不到 targetClass
		proxyFactory.addAspect(SimpleAspect.class);
		proxyFactory.setProxyTargetClass(true);

		IProxyService proxy = proxyFactory.getProxy();
		invoke(proxy);
	}

	@Test
	public void testAtAspectJ() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AtAspectJConfig.class);
		context.refresh();

		IProxyService service = context.getBean(IProxyService.class);

		// use configured instance
		invoke(service);
	}

	private void invoke(IProxyService service) {
		// use configured instance
		service.doMethod1();
		System.out.println(service.doMethod2());
		try {
			System.out.println(service.doMethod3());
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@EnableAspectJAutoProxy
	@Configuration
	@ComponentScan("org.springframework.aop.aspectj")
	public static class AtAspectJConfig {
	}


	/**
	 * 定义接口
	 */
	public interface IProxyService {
		void doMethod1();
		String doMethod2();
		String doMethod3() throws Exception;
	}

	/**
	 * 实现类
	 */
	@Service
	public static class AopAtAspectServiceImpl implements IProxyService {

		@Override
		public void doMethod1() {
			System.out.println("AopAtAspectServiceImpl.doMethod1()");
		}

		@Override
		public String doMethod2() {
			System.out.println("AopAtAspectServiceImpl.doMethod2()");
			return "hello world";
		}

		@Override
		public String doMethod3() throws Exception {
			System.out.println("AopAtAspectServiceImpl.doMethod3()");
			throw new Exception("some exception");
		}

	}

}
