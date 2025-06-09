package org.springframework.aop.proxying_mechanisms;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.aop.advices.SimpleAfterReturningAdvice;
import org.springframework.aop.advices.SimpleMethodBeforeAdvice;
import org.springframework.aop.advices.SimpleThrowsAdvice;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.aop.framework.JdkDynamicProxyTests
 * @see org.springframework.aop.framework.ObjenesisProxyTests
 * @see org.springframework.aop.framework.CglibProxyTests
 * @see org.springframework.aop.framework.ProxyFactoryBeanTests
 */
public class ProxyFactoryBeanTests {

	@Test
	public void testProxyFactoryBean() {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTarget(new SimplePojo());
		proxyFactoryBean.setInterfaces(Pojo.class);
		proxyFactoryBean.addAdvice(new SimpleAfterReturningAdvice());
		proxyFactoryBean.addAdvice(new SimpleMethodBeforeAdvice());
		proxyFactoryBean.addAdvice(new SimpleThrowsAdvice());

		Object object = proxyFactoryBean.getObject();
		if (object instanceof Pojo pojo) {
			pojo.foo();
		}

	}

	interface Pojo {
		void foo();
	}

	static class SimplePojo implements ProxyFactoryTests.Pojo {
		public void foo() {
			System.out.println("SimplePojo.foo");
		}
	}

	public static AdvisedSupport addAdvice(AdvisedSupport support, Advice advice) {
		support.addAdvice(advice);
		return support;
	}

}
