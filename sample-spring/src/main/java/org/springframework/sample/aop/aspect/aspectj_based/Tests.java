package org.springframework.sample.aop.aspect.aspectj_based;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.sample.aop.aspect.TargetService;
import org.springframework.sample.aop.aspect.TargetServiceImpl;

/**
 * 用 Java 配置声明一个 Aspect
 */
public class Tests {

	@Configuration
	@EnableAspectJAutoProxy
	static class Config {
		/**
		 * 目标类
		 */
		@Bean
		public TargetService targetService() {
			return new TargetServiceImpl();
		}

		/**
		 * Aspect 实例
		 */
		@Bean
		public AspectjAspect aspect() {
			return new AspectjAspect();
		}
	}

	/**
	 * Test
	 */
	public static void main(String[] args) {
		GenericApplicationContext context = new GenericApplicationContext();
		context.registerBean(ConfigurationClassPostProcessor.class);
		context.registerBean(Config.class);
		context.refresh();

		// Test
		TargetService service = context.getBean(TargetService.class);
		service.doMethod1();
		service.doMethod2();
		try {
			service.doMethod3();
		} catch (Exception ignore) {}
	}

}
