package org.springframework.sample.configuration;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

// <h2>使用外部化值</h2>


// <h3>使用 {@code Environment} API</h3>
class C0301UsingTheEnvironmentApiTests {
	// <h3>使用 {@code Environment} API</h3>
	//
	// <p>可以通过将 Spring {@link org.springframework.core.env.Environment} 注入到 {@code @Configuration} 类中来查找外部化值;
	// 例如，使用 {@code @Autowired} 注释：
	//
	// <pre class="code">
	// @Configuration
	// public class AppConfig {
	//
	//		@Autowired Environment env;
	//
	// 		@Bean
	// 		public MyBean myBean() {
	// 			MyBean myBean = new MyBean();
	// 			myBean.setName(env.getProperty("bean.name"));
	// 			return myBean;
	// 		}
	// }
	// </pre>
	//
	// <p>通过 {@code Environment} 解析的属性驻留在一个或多个“属性源”对象中，
	// 并且 {@code @Configuration} 类可以使用 {@link PropertySource @PropertySource} 注释向 {@code Environment} 对象贡献属性源：
	//
	// <pre class="code">
	// @Configuration
	// @PropertySource("classpath:/com/acme/app.properties")
	// public class AppConfig {
	//
	// 		@Inject Environment env;
	//
	// 		@Bean
	// 		public MyBean myBean() {
	//			return new MyBean(env.getProperty("bean.name"));
	//		}
	// }
	// </pre>
	//
	// <p>有关更多详细信息，请参阅 {@link org.springframework.core.env.Environment Environment} 和 {@link PropertySource @PropertySource} javadoc。

	/**
	 * 使用 {@code Environment} API
	 */
	@Test
	void testUsingTheEnvironmentAPI1() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

		// add property
		Map<String, Object> source = new HashMap<>();
		source.put("bean.name", "myBeanBean222");

		ConfigurableEnvironment environment = context.getEnvironment();
		MutablePropertySources propertySources = environment.getPropertySources();
		MapPropertySource mapPropertySource = new MapPropertySource("map", source);
		propertySources.addLast(mapPropertySource);

		context.register(AppConfig1.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);

	}

	/**
	 * 使用 {@code Environment} API
	 */
	@Test
	void testUsingTheEnvironmentAPI2() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig2.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}

	/**
	 * 使用 {@code Environment} API
	 */
	@Configuration
	public static class AppConfig1 {

		@Autowired
		Environment env;

		@Bean
		public MyBean myBean() {
			MyBean myBean = new MyBean();
			myBean.setName(env.getProperty("bean.name"));
			return myBean;
		}
	}

	/**
	 * 通过 {@code Environment} 解析的属性驻留在一个或多个“属性源”对象中，
	 * 并且 {@code @Configuration} 类可以使用 {@link PropertySource @PropertySource} 注释向 {@code Environment} 对象贡献属性源
	 */
	@Configuration
	@PropertySource("classpath:/org/springframework/sample/configuration/app.properties")
	public static class AppConfig2 {

		@Inject
		Environment env;

		@Bean
		public MyBean myBean() {
			return new MyBean(env.getProperty("bean.name"));
		}
	}


	public static class MyBean {

		private String name;

		public MyBean() {
		}

		public MyBean(String name) {
			this.name = name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "MyBean{" +
					"name='" + name + '\'' +
					'}';
		}
	}

}


// <h3>使用 {@code @Value} 注解</h3>
class C0302UsingTheValueAnnotationTests {
	// <h3>使用 {@code @Value} 注解</h3>
	//
	// <p>可以使用 {@link Value @Value} 注解将外部化值注入到 {@code @Configuration} 类中：
	//
	// <pre class="code">
	// @Configuration
	// @PropertySource("classpath:/com/acme/app.properties")
	// public class AppConfig {
	// 		@Value("${bean.name}") String beanName;
	//
	// 		@Bean
	// 		public MyBean myBean() {
	// 			return new MyBean(beanName);
	// 		}
	// }
	// </pre>
	//
	// <p>这种方法通常与 Spring 的 {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer PropertySourcesPlaceholderConfigurer} 结合使用，
	// 可以通过 {@code <context:property-placeholder/>} 在 XML 配置中<em>自动</em>启用，
	// 也可以通过专用的 {@code static} {@code @Bean} 方法在 {@code @Configuration} 类中<em>显式</em>启用
	// （有关详细信息，请参阅 {@link Bean @Bean} 的 javadocs 中的“有关 BeanFactoryPostProcessor 返回 {@code @Bean} 方法的说明”）。
	// 但请注意，通常仅在需要自定义配置（例如占位符语法等）时才需要通过 {@code static} {@code @Bean} 方法显式注册 {@code PropertySourcesPlaceholderConfigurer}。
	// 具体来说，如果没有 bean 后处理器（例如 {@code PropertySourcesPlaceholderConfigurer}）为 {@code ApplicationContext} 注册<em>嵌入式值解析器</em>，
	// Spring 将注册一个默认的<em>嵌入式值解析器</em>，该解析器根据 {@code Environment} 中注册的属性源解析占位符。
	// 请参阅下面关于使用 {@code @ImportResource} 通过 Spring XML 编写 {@code @Configuration} 类的部分；
	// 请参阅 {@link Value @Value} javadoc；并参阅 {@link Bean @Bean} javadoc，了解如何使用 {@code BeanFactoryPostProcessor} 类型（例如 {@code PropertySourcesPlaceholderConfigurer}）的详细信息。

	@Test
	void testUsingTheValueAnnotation() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();
		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}


	@Configuration
	@PropertySource("classpath:/org/springframework/sample/configuration/app.properties")
	public static class AppConfig {

		@Value("${bean.name}")
		String beanName;

		@Bean
		public MyBean myBean() {
			return new MyBean(beanName);
		}
	}

	 public static class MyBean {
		 private final String name;
		 public MyBean(String name) {
			 this.name = name;
		 }
		 @Override
		 public String toString() {
			 return "MyBean{" +
					 "name='" + name + '\'' +
					 '}';
		 }
	 }

}
