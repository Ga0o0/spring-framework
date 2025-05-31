package org.springframework.sample.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


// <h2>对 {@code @Configuration} 类的测试支持</h2>
//
// <p>{@code spring-test} 模块中提供的 Spring <em>TestContext 框架</em> 提供了 {@code @ContextConfiguration} 注释，
// 该注释可以接受 <em>组件类</em> 引用数组; 通常是 {@code @Configuration} 或 {@code @Component} 类。
//
// <pre class="code">
// @ExtendWith(SpringExtension.class)
// @ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
// class MyTests {
//
// 		@Autowired MyBean myBean;
//
// 		@Autowired DataSource dataSource;
//
// 		@Test void test() {
// 			// assertions against myBean ...
// 		}
// }
// </pre>
//
// <p>有关详细信息，请参阅 <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-framework">TestContext 框架</a> 参考文档。

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
class MyTests {

	@Autowired
	MyBean myBean;

	@Autowired
	DataSource dataSource;

	@Test
	void test() {
		// assertions against myBean ...
		System.out.println(myBean);
		System.out.println(dataSource);
	}
}

class MyBean {
}

class DataSource {
}

@Configuration
class DatabaseConfig {
	@Bean
	public DataSource dataSource() {
		return new DataSource();
	}
}

@Configuration
class AppConfig {
	@Bean
	public MyBean myBean() {
		return new MyBean();
	}
}


