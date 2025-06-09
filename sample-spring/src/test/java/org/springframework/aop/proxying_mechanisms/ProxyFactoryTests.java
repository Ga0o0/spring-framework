package org.springframework.aop.proxying_mechanisms;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.aop.TargetSource;
import org.springframework.aop.advices.SimpleAfterReturningAdvice;
import org.springframework.aop.advices.SimpleMethodBeforeAdvice;
import org.springframework.aop.advices.SimpleThrowsAdvice;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @see org.springframework.aop.framework.ProxyFactory
 * @see org.springframework.aop.framework.JdkDynamicProxyTests
 * @see org.springframework.aop.framework.ObjenesisProxyTests
 * @see org.springframework.aop.framework.CglibProxyTests
 * @see org.springframework.aop.framework.AbstractAopProxyTests
 */
public class ProxyFactoryTests {

	@Test
	public void testAopProxyingMechanisms() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new SimplePojo());
		// proxyFactory.addInterface(Pojo.class);
		// advice
		proxyFactory.addAdvice(new SimpleAfterReturningAdvice());
		proxyFactory.addAdvice(new SimpleMethodBeforeAdvice());
		proxyFactory.addAdvice(new SimpleThrowsAdvice());
		// proxyFactory.setProxyTargetClass(true);

		Object proxy = proxyFactory.getProxy();
		if (proxy instanceof Pojo pojo) {
			pojo.foo();
		}
	}

	@Test
	public void testAopProxyingMechanisms2() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return SimplePojo.class;
			}
			@Override
			public Object getTarget() throws Exception {
				return new SimplePojo();
			}
		});
		proxyFactory.addInterface(Pojo.class);
		// advice
		proxyFactory.addAdvice(new SimpleAfterReturningAdvice());
		proxyFactory.addAdvice(new SimpleMethodBeforeAdvice());
		proxyFactory.addAdvice(new SimpleThrowsAdvice());
		proxyFactory.setProxyTargetClass(true);

		Object proxy = proxyFactory.getProxy();
		if (proxy instanceof Pojo pojo) {
			pojo.foo();
		}
	}

	interface Pojo {
		void foo();
	}

	static class SimplePojo implements Pojo {
		public void foo() {
			System.out.println("SimplePojo.foo");
		}
	}

}

