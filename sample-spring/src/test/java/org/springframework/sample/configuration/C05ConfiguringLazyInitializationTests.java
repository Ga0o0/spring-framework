package org.springframework.sample.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// <h2>配置延迟初始化</h2>
public class C05ConfiguringLazyInitializationTests {

	// <h2>配置延迟初始化</h2>
	//
	// <p>默认情况下，{@code @Bean} 方法将在容器启动时<em>立即实例化</em>。
	// 为了避免这种情况，可以将 {@code @Configuration} 与 {@link Lazy @Lazy} 批注结合使用，以指示类中声明的所有 {@code @Bean} 方法默认都采用延迟初始化。
	// 请注意，{@code @Lazy} 也可以用于单个 {@code @Bean} 方法。

	@Test
	void test1() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig1.class);
		context.refresh();

		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		String[] singletonNames = beanFactory.getSingletonNames();
		List<String> beanNames = List.of("myBean1", "myBean2");
		System.out.println(Arrays.stream(singletonNames).filter(beanNames::contains).toList());

		Map<String, MyBean> beansOfType = context.getBeansOfType(MyBean.class);
		for (Map.Entry<String, MyBean> entry : beansOfType.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		String[] afterSingletonNames = beanFactory.getSingletonNames();
		System.out.println(Arrays.stream(afterSingletonNames).filter(beanNames::contains).toList());
	}

	@Test
	void test2() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig2.class);
		context.refresh();

		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		String[] singletonNames = beanFactory.getSingletonNames();
		List<String> beanNames = List.of("myBean1", "myBean2");
		System.out.println(Arrays.stream(singletonNames).filter(beanNames::contains).toList());

		Map<String, MyBean> beansOfType = context.getBeansOfType(MyBean.class);
		for (Map.Entry<String, MyBean> entry : beansOfType.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		String[] afterSingletonNames = beanFactory.getSingletonNames();
		System.out.println(Arrays.stream(afterSingletonNames).filter(beanNames::contains).toList());
	}


	/**
	 * 将 {@code @Configuration} 与 {@link Lazy @Lazy} 批注结合使用，以指示类中声明的所有 {@code @Bean} 方法默认都采用延迟初始化。
	 */
	@Lazy
	@Configuration
	public static class AppConfig1 {

		@Bean
		public MyBean myBean1() {
			return new MyBean();
		}

		@Bean
		public MyBean myBean2() {
			return new MyBean();
		}

	}

	/**
	 * {@code @Lazy} 也可以用于单个 {@code @Bean} 方法。
	 */
	@Configuration
	public static class AppConfig2 {

		@Bean
		public MyBean myBean1() {
			return new MyBean();
		}

		@Bean
		@Lazy
		public MyBean myBean2() {
			return new MyBean();
		}

	}

	public static class MyBean {
	}

}
