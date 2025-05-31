package org.springframework.sample.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 引导 {@code @Configuration} 类
 */
class C02BootstrappingConfigurationClassesTests {

	/**
	 * 1. 通过 {@code AnnotationConfigApplicationContext}
	 */
	@Test
	void testViaAnnotationConfigApplicationContext() {
		// <p>{@code @Configuration} 类通常使用 {@link AnnotationConfigApplicationContext} 或
		// 其支持 Web 的变体 {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext} 进行引导。
		// 前者的一个简单示例如下：

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(AppConfig.class);
		ctx.refresh();
		MyBean myBean = ctx.getBean(MyBean.class);
		System.out.println(myBean);

		// <p>有关更多详细信息，请参阅 {@link AnnotationConfigApplicationContext} javadoc，有关 {@code Servlet} 容器中的 Web 配置说明，
		// 请参阅 {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext}。
	}

	/**
	 * 2. 通过 Spring {@code <beans>} XML
	 */
	@Test
	void testViaSpringBeansXML() {
		// <h3>通过 Spring {@code <beans>} XML</h3>
		//
		// <p>作为直接针对 {@code AnnotationConfigApplicationContext} 注册 {@code @Configuration} 类的替代方法，
		// {@code @Configuration} 类可以在 Spring XML 文件中声明为普通的 {@code <bean>} 定义：
		//
		// <pre class="code">
		// <beans>
		//		<context:annotation-config/>
		// 		<bean class="com.acme.AppConfig"/>
		// </beans>
		// </pre>
		//
		// <p>在上面的示例中，需要 {@code <context:annotation-config/> 才能启用 {@link ConfigurationClassPostProcessor} 和其他与注释相关的后处理器，
		// 以便于处理 {@code @Configuration} 类。

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
		context.setConfigLocation("org/springframework/sample/configuration/via-spring-beans-xml.xml");
		context.refresh();
		MyBean myBean = context.getBean(MyBean.class);
		System.out.println(myBean);
	}

	@Test
	void testViaComponentScanning() {
		// <h3>通过组件扫描</h3>
		//
		// <p>由于 {@code @Configuration} 使用 {@link Component @Component} 进行元注释，因此 {@code @Configuration} 类是组件扫描的候选者;
		// 例如，使用 {@link ComponentScan @ComponentScan} 或 Spring XML 的 {@code <context:component-scan/>} 元素;
		// 因此也可以像任何常规 {@code @Component} 一样利用 {@link Autowired @Autowired}/{@link jakarta.inject.Inject @Inject}。
		// 特别是，如果存在单个构造函数，则自动装配语义将透明地应用于该构造函数：
		//
		// <pre class="code">
		// @Configuration
		// public class AppConfig {
		//
		//		private final SomeBean someBean;
		//
		// 		public AppConfig(SomeBean someBean) {
		// 			this.someBean = someBean;
		// 		}
		// 		// 使用 “SomeBean” 的 @Bean 定义
		// }
		// </pre>
		//
		// <p>{@code @Configuration} 类不仅可以使用组件扫描进行引导，还可以使用 {@link ComponentScan @ComponentScan} 批注自行<em>配置</em>组件扫描：
		//
		// <pre class="code">
		// @Configuration
		// @ComponentScan("com.acme.app.services")
		// public class AppConfig {
		// 		// 各种 @Bean 定义 ...
		// }
		// </pre>
		//
		// <p>有关详细信息，请参阅 {@link ComponentScan @ComponentScan} javadoc。
	}

	@Configuration
	public static class AppConfig {
		@Bean
		public MyBean myBean() {
			return new MyBean();
		}
	}

	public static class MyBean {
	}


}
