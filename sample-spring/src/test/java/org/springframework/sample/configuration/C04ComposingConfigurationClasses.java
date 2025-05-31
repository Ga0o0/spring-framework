package org.springframework.sample.configuration;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;

// <h2>组合 {@code @Configuration} 类</h2>

// <h3>使用 {@code @Import} 注解</h3>
class C0401WithTheImportAnnotationTests {
	// <h3>使用 {@code @Import} 注解</h3>
	//
	// <p>可以使用 {@link Import @Import} 注解组合 {@code @Configuration} 类，类似于 Spring XML 中 {@code <import>} 的工作方式。
	// 由于 {@code @Configuration} 对象在容器内作为 Spring bean 进行管理，因此可以注入导入的配置; 例如，通过构造函数注入：
	//
	// <pre class="code">
	// @Configuration
	// public class DatabaseConfig {
	//
	// 		@Bean
	// 		public DataSource dataSource() {
	// 			// 实例化、配置并返回 DataSource
	// 		}
	// }
	//
	// @Configuration
	// @Import(DatabaseConfig.class)
	// public class AppConfig {
	//
	// 		private final DatabaseConfig dataConfig;
	//
	// 		public AppConfig(DatabaseConfig dataConfig) {
	// 			this.dataConfig = dataConfig;
	// 		}
	//
	// 		@Bean
	// 		public MyBean myBean() {
	// 			// 引用 dataSource() bean 方法
	// 			return new MyBean(dataConfig.dataSource());
	// 		}
	// }
	// </pre>
	//
	// <p>现在，只需在 Spring 上下文中注册 {@code AppConfig}，即可引导 {@code AppConfig} 和导入的 {@code DatabaseConfig}：
	//
	// <pre class="code">new AnnotationConfigApplicationContext(AppConfig.class);</pre>

	@Test
	void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}

	@Configuration
	public static class DatabaseConfig {

		@Bean
		public DataSource dataSource() {
			// 实例化、配置并返回 DataSource
			return new DataSource();
		}
	}

	@Configuration
	@Import(DatabaseConfig.class)
	public static class AppConfig {

		private final DatabaseConfig dataConfig;

		public AppConfig(DatabaseConfig dataConfig) {
			this.dataConfig = dataConfig;
		}

		@Bean
		public MyBean myBean() {
			// 引用 dataSource() bean 方法
			return new MyBean(dataConfig.dataSource());
		}
	}

	public static class DataSource {
	}

	public static class MyBean {
		private final DataSource dataSource;

		public MyBean(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public String toString() {
			return "MyBean{" +
					"dataSource=" + dataSource +
					'}';
		}
	}

}

// <h3>使用 {@code @Profile} 注释</h3>
class C0402WithTheProfileAnnotationTests {
	// <h3>使用 {@code @Profile} 注释</h3>
	//
	// <p>{@code @Configuration} 类可以使用 {@link Profile @Profile} 注释进行标记，以指示仅当给定的配置文件处于<em>活动</em>状态时才应处理它们：
	//
	// <pre class="code">
	// @Profile("development")
	// @Configuration
	// public class EmbeddedDatabaseConfig {
	//
	// 		@Bean
	// 		public DataSource dataSource() {
	// 			// 实例化、配置和返回嵌入式 DataSource
	// 		}
	// }
	//
	// @Profile("production")
	// @Configuration
	// public class ProductionDatabaseConfig {
	//
	// 		@Bean
	// 		public DataSource dataSource() {
	// 			// 实例化、配置和返回生产 DataSource
	// 		}
	// }
	// </pre>
	//
	// <p>或者，您也可以在{@code @Bean} 方法级别 &mdash; 例如，对于同一配置类中的替代 bean 变体：
	//
	// <pre class="code">
	// @Configuration
	// public class ProfileDatabaseConfig {
	//
	// 		@Bean("dataSource")
	// 		@Profile("development")
	// 		public DataSource embeddedDatabase() { ... }
	//
	// 		@Bean("dataSource")
	// 		@Profile("production")
	// 		public DataSource productionDatabase() { ... }
	// </pre>
	//
	// <p>有关更多详细信息，请参阅 {@link Profile @Profile} 和 {@link org.springframework.core.env.Environment} javadoc。

	@Test
	void test1() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("development");
		context.register(EmbeddedDatabaseConfig.class);
		context.register(ProductionDatabaseConfig.class);
		context.refresh();

		DataSource bean = context.getBean(DataSource.class);
		System.out.println(bean);
	}

	@Test
	void test2() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("development");
		context.register(ProfileDatabaseConfig.class);
		context.refresh();

		DataSource bean = context.getBean(DataSource.class);
		System.out.println(bean);
	}


	@Profile("development")
	@Configuration
	public static class EmbeddedDatabaseConfig {
		@Bean
		public DataSource dataSource() {
			// 实例化、配置和返回嵌入式 DataSource
			return new DataSource("development");
		}
	}

	@Profile("production")
	@Configuration
	public static class ProductionDatabaseConfig {
		@Bean
		public DataSource dataSource() {
			// 实例化、配置和返回生产 DataSource
			return new DataSource("production");
		}
	}

	@Configuration
	public static class ProfileDatabaseConfig {

		@Bean("dataSource")
		@Profile("development")
		public DataSource embeddedDatabase() {
			return new DataSource("development");
		}

		@Bean("dataSource")
		@Profile("production")
		public DataSource productionDatabase() {
			return new DataSource("production");
		}

	}

	public static class DataSource {
		private final String name;

		public DataSource(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "DataSource{" +
					"name='" + name + '\'' +
					'}';
		}
	}

}

// <h3>使用 Spring XML 使用 {@code @ImportResource} 注解</h3>
class C0403WithSpringXmlUsingTheImportResourceAnnotationTests {
	// <h3>使用 Spring XML 使用 {@code @ImportResource} 注解</h3>
	//
	// <p>如上所述，{@code @Configuration} 类可以在 Spring XML 文件中声明为常规 Spring {@code <bean>} 定义。
	// 也可以使用 {@link ImportResource @ImportResource} 注解将 Spring XML 配置文件导入到 {@code @Configuration} 类中。
	// 可以注入从 XML 导入的 Bean 定义 &mdash; 例如，使用 {@code @Inject} 注解：
	//
	// <pre class="code">
	// @Configuration
	// @ImportResource("classpath:/com/acme/database-config.xml")
	// public class AppConfig {
	//
	// 		@Inject DataSource dataSource; // 来自 XML
	//
	// 		@Bean
	// 		public MyBean myBean() {
	// 			// 注入 XML 定义的 dataSource bean
	// 			return new MyBean(this.dataSource);
	// 		}
	// }
	// </pre>

	@Test
	void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}

	@Configuration
	@ImportResource("classpath:/org/springframework/sample/configuration/database-config.xml")
	public static class AppConfig {

		@Inject
		DataSource dataSource; // 来自 XML

		@Bean
		public MyBean myBean() {
			// 注入 XML 定义的 dataSource bean
			return new MyBean(this.dataSource);
		}
	}

	public static class DataSource {
		private final String name;

		public DataSource(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "DataSource{" +
					"name='" + name + '\'' +
					'}';
		}
	}

	public static class MyBean {
		private final DataSource dataSource;

		public MyBean(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public String toString() {
			return "MyBean{" +
					"dataSource=" + dataSource +
					'}';
		}
	}

}

// <h3>使用嵌套的 {@code @Configuration} 类</h3>
class C0404WithNestedConfigurationClassesTests {

	// <h3>使用嵌套的 {@code @Configuration} 类</h3>
	//
	// <p>{@code @Configuration} 类可以按如下方式相互嵌套：
	//
	// <pre class="code">
	// @Configuration
	// public class AppConfig {
	//
	// 		@Inject DataSource dataSource;
	//
	// 		@Bean
	// 		public MyBean myBean() {
	// 			return new MyBean(dataSource);
	// 		}
	//
	// 		@Configuration
	// 		static class DatabaseConfig {
	// 			@Bean
	// 			DataSource dataSource() {
	// 				return new EmbeddedDatabaseBuilder().build();
	// 			}
	// 		}
	// }
	// </pre>
	//
	// <p>引导此类安排时，只有 {@code AppConfig} 需要针对应用程序上下文进行注册。由于是嵌套的 {@code @Configuration} 类，{@code DatabaseConfig} <em>将自动注册</em>。
	// 当 {@code AppConfig} 和 {@code DatabaseConfig} 之间的关系已经隐式明确时，这避免了使用 {@code @Import} 注释的需要。
	//
	// <p>另请注意，嵌套的 {@code @Configuration} 类可以与 {@code @Profile} 批注完美结合使用，从而为封闭的 {@code @Configuration} 类提供同一 Bean 的两个选项。

	@Test
	void test() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(AppConfig.class);
		context.refresh();

		MyBean bean = context.getBean(MyBean.class);
		System.out.println(bean);
	}

	@Configuration
	public static class AppConfig {

		@Inject
		DataSource dataSource;

		@Bean
		public MyBean myBean() {
			return new MyBean(dataSource);
		}

		@Configuration
		static class DatabaseConfig {
			@Bean
			DataSource dataSource() {
				return new DataSource("C0404WithNestedConfigurationClassesTests");
			}
		}
	}

	public static class DataSource {
		private final String name;

		public DataSource(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "DataSource{" +
					"name='" + name + '\'' +
					'}';
		}
	}

	public static class MyBean {
		private final DataSource dataSource;

		public MyBean(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public String toString() {
			return "MyBean{" +
					"dataSource=" + dataSource +
					'}';
		}
	}

}