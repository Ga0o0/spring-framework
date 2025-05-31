/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Indicates that a class declares one or more {@link Bean @Bean} methods and
 * may be processed by the Spring container to generate bean definitions and
 * service requests for those beans at runtime, for example:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // instantiate, configure and return bean ...
 *     }
 * }</pre>
 *
 * <h2>Bootstrapping {@code @Configuration} classes</h2>
 *
 * <h3>Via {@code AnnotationConfigApplicationContext}</h3>
 *
 * <p>{@code @Configuration} classes are typically bootstrapped using either
 * {@link AnnotationConfigApplicationContext} or its web-capable variant,
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext}. A simple example with the former follows:
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(AppConfig.class);
 * ctx.refresh();
 * MyBean myBean = ctx.getBean(MyBean.class);
 * // use myBean ...
 * </pre>
 *
 * <p>See the {@link AnnotationConfigApplicationContext} javadocs for further details, and see
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} for web configuration instructions in a
 * {@code Servlet} container.
 *
 * <h3>Via Spring {@code <beans>} XML</h3>
 *
 * <p>As an alternative to registering {@code @Configuration} classes directly against an
 * {@code AnnotationConfigApplicationContext}, {@code @Configuration} classes may be
 * declared as normal {@code <bean>} definitions within Spring XML files:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *    &lt;context:annotation-config/&gt;
 *    &lt;bean class="com.acme.AppConfig"/&gt;
 * &lt;/beans&gt;
 * </pre>
 *
 * <p>In the example above, {@code <context:annotation-config/>} is required in order to
 * enable {@link ConfigurationClassPostProcessor} and other annotation-related
 * post processors that facilitate handling {@code @Configuration} classes.
 *
 * <h3>Via component scanning</h3>
 *
 * <p>Since {@code @Configuration} is meta-annotated with {@link Component @Component},
 * {@code @Configuration} classes are candidates for component scanning &mdash;
 * for example, using {@link ComponentScan @ComponentScan} or Spring XML's
 * {@code <context:component-scan/>} element &mdash; and therefore may also take
 * advantage of {@link Autowired @Autowired}/{@link jakarta.inject.Inject @Inject}
 * like any regular {@code @Component}. In particular, if a single constructor is
 * present, autowiring semantics will be applied transparently for that constructor:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     private final SomeBean someBean;
 *
 *     public AppConfig(SomeBean someBean) {
 *         this.someBean = someBean;
 *     }
 *
 *     // &#064;Bean definition using "SomeBean"
 *
 * }</pre>
 *
 * <p>{@code @Configuration} classes may not only be bootstrapped using component
 * scanning, but may also themselves <em>configure</em> component scanning using
 * the {@link ComponentScan @ComponentScan} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ComponentScan("com.acme.app.services")
 * public class AppConfig {
 *     // various &#064;Bean definitions ...
 * }</pre>
 *
 * <p>See the {@link ComponentScan @ComponentScan} javadocs for details.
 *
 * <h2>Working with externalized values</h2>
 *
 * <h3>Using the {@code Environment} API</h3>
 *
 * <p>Externalized values may be looked up by injecting the Spring
 * {@link org.springframework.core.env.Environment} into a {@code @Configuration}
 * class &mdash; for example, using the {@code @Autowired} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Autowired Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         MyBean myBean = new MyBean();
 *         myBean.setName(env.getProperty("bean.name"));
 *         return myBean;
 *     }
 * }</pre>
 *
 * <p>Properties resolved through the {@code Environment} reside in one or more "property
 * source" objects, and {@code @Configuration} classes may contribute property sources to
 * the {@code Environment} object using the {@link PropertySource @PropertySource}
 * annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064;Inject Environment env;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(env.getProperty("bean.name"));
 *     }
 * }</pre>
 *
 * <p>See the {@link org.springframework.core.env.Environment Environment}
 * and {@link PropertySource @PropertySource} javadocs for further details.
 *
 * <h3>Using the {@code @Value} annotation</h3>
 *
 * <p>Externalized values may be injected into {@code @Configuration} classes using
 * the {@link Value @Value} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/acme/app.properties")
 * public class AppConfig {
 *
 *     &#064;Value("${bean.name}") String beanName;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(beanName);
 *     }
 * }</pre>
 *
 * <p>This approach is often used in conjunction with Spring's
 * {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 * PropertySourcesPlaceholderConfigurer} that can be enabled <em>automatically</em>
 * in XML configuration via {@code <context:property-placeholder/>} or <em>explicitly</em>
 * in a {@code @Configuration} class via a dedicated {@code static} {@code @Bean} method
 * (see "a note on BeanFactoryPostProcessor-returning {@code @Bean} methods" of
 * {@link Bean @Bean}'s javadocs for details). Note, however, that explicit registration
 * of a {@code PropertySourcesPlaceholderConfigurer} via a {@code static} {@code @Bean}
 * method is typically only required if you need to customize configuration such as the
 * placeholder syntax, etc. Specifically, if no bean post-processor (such as a
 * {@code PropertySourcesPlaceholderConfigurer}) has registered an <em>embedded value
 * resolver</em> for the {@code ApplicationContext}, Spring will register a default
 * <em>embedded value resolver</em> which resolves placeholders against property sources
 * registered in the {@code Environment}. See the section below on composing
 * {@code @Configuration} classes with Spring XML using {@code @ImportResource}; see
 * the {@link Value @Value} javadocs; and see the {@link Bean @Bean} javadocs for details
 * on working with {@code BeanFactoryPostProcessor} types such as
 * {@code PropertySourcesPlaceholderConfigurer}.
 *
 * <h2>Composing {@code @Configuration} classes</h2>
 *
 * <h3>With the {@code @Import} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be composed using the {@link Import @Import} annotation,
 * similar to the way that {@code <import>} works in Spring XML. Because
 * {@code @Configuration} objects are managed as Spring beans within the container,
 * imported configurations may be injected &mdash; for example, via constructor injection:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class DatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return DataSource
 *     }
 * }
 *
 * &#064;Configuration
 * &#064;Import(DatabaseConfig.class)
 * public class AppConfig {
 *
 *     private final DatabaseConfig dataConfig;
 *
 *     public AppConfig(DatabaseConfig dataConfig) {
 *         this.dataConfig = dataConfig;
 *     }
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // reference the dataSource() bean method
 *         return new MyBean(dataConfig.dataSource());
 *     }
 * }</pre>
 *
 * <p>Now both {@code AppConfig} and the imported {@code DatabaseConfig} can be bootstrapped
 * by registering only {@code AppConfig} against the Spring context:
 *
 * <pre class="code">
 * new AnnotationConfigApplicationContext(AppConfig.class);</pre>
 *
 * <h3>With the {@code @Profile} annotation</h3>
 *
 * <p>{@code @Configuration} classes may be marked with the {@link Profile @Profile} annotation to
 * indicate they should be processed only if a given profile or profiles are <em>active</em>:
 *
 * <pre class="code">
 * &#064;Profile("development")
 * &#064;Configuration
 * public class EmbeddedDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return embedded DataSource
 *     }
 * }
 *
 * &#064;Profile("production")
 * &#064;Configuration
 * public class ProductionDatabaseConfig {
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // instantiate, configure and return production DataSource
 *     }
 * }</pre>
 *
 * <p>Alternatively, you may also declare profile conditions at the {@code @Bean} method level
 * &mdash; for example, for alternative bean variants within the same configuration class:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class ProfileDatabaseConfig {
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("development")
 *     public DataSource embeddedDatabase() { ... }
 *
 *     &#064;Bean("dataSource")
 *     &#064;Profile("production")
 *     public DataSource productionDatabase() { ... }
 * }</pre>
 *
 * <p>See the {@link Profile @Profile} and {@link org.springframework.core.env.Environment}
 * javadocs for further details.
 *
 * <h3>With Spring XML using the {@code @ImportResource} annotation</h3>
 *
 * <p>As mentioned above, {@code @Configuration} classes may be declared as regular Spring
 * {@code <bean>} definitions within Spring XML files. It is also possible to
 * import Spring XML configuration files into {@code @Configuration} classes using
 * the {@link ImportResource @ImportResource} annotation. Bean definitions imported from
 * XML can be injected &mdash; for example, using the {@code @Inject} annotation:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;ImportResource("classpath:/com/acme/database-config.xml")
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource; // from XML
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         // inject the XML-defined dataSource bean
 *         return new MyBean(this.dataSource);
 *     }
 * }</pre>
 *
 * <h3>With nested {@code @Configuration} classes</h3>
 *
 * <p>{@code @Configuration} classes may be nested within one another as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class AppConfig {
 *
 *     &#064;Inject DataSource dataSource;
 *
 *     &#064;Bean
 *     public MyBean myBean() {
 *         return new MyBean(dataSource);
 *     }
 *
 *     &#064;Configuration
 *     static class DatabaseConfig {
 *         &#064;Bean
 *         DataSource dataSource() {
 *             return new EmbeddedDatabaseBuilder().build();
 *         }
 *     }
 * }</pre>
 *
 * <p>When bootstrapping such an arrangement, only {@code AppConfig} need be registered
 * against the application context. By virtue of being a nested {@code @Configuration}
 * class, {@code DatabaseConfig} <em>will be registered automatically</em>. This avoids
 * the need to use an {@code @Import} annotation when the relationship between
 * {@code AppConfig} and {@code DatabaseConfig} is already implicitly clear.
 *
 * <p>Note also that nested {@code @Configuration} classes can be used to good effect
 * with the {@code @Profile} annotation to provide two options of the same bean to the
 * enclosing {@code @Configuration} class.
 *
 * <h2>Configuring lazy initialization</h2>
 *
 * <p>By default, {@code @Bean} methods will be <em>eagerly instantiated</em> at container
 * bootstrap time.  To avoid this, {@code @Configuration} may be used in conjunction with
 * the {@link Lazy @Lazy} annotation to indicate that all {@code @Bean} methods declared
 * within the class are by default lazily initialized. Note that {@code @Lazy} may be used
 * on individual {@code @Bean} methods as well.
 *
 * <h2>Testing support for {@code @Configuration} classes</h2>
 *
 * <p>The Spring <em>TestContext framework</em> available in the {@code spring-test} module
 * provides the {@code @ContextConfiguration} annotation which can accept an array of
 * <em>component class</em> references &mdash; typically {@code @Configuration} or
 * {@code @Component} classes.
 *
 * <pre class="code">
 * &#064;ExtendWith(SpringExtension.class)
 * &#064;ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
 * class MyTests {
 *
 *     &#064;Autowired MyBean myBean;
 *
 *     &#064;Autowired DataSource dataSource;
 *
 *     &#064;Test
 *     void test() {
 *         // assertions against myBean ...
 *     }
 * }</pre>
 *
 * <p>See the
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-framework">TestContext framework</a>
 * reference documentation for details.
 *
 * <h2>Enabling built-in Spring features using {@code @Enable} annotations</h2>
 *
 * <p>Spring features such as asynchronous method execution, scheduled task execution,
 * annotation driven transaction management, and even Spring MVC can be enabled and
 * configured from {@code @Configuration} classes using their respective "{@code @Enable}"
 * annotations. See
 * {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync},
 * {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling},
 * {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement},
 * {@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy},
 * and {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}
 * for details.
 *
 * <h2>Constraints when authoring {@code @Configuration} classes</h2>
 *
 * <ul>
 * <li>Configuration classes must be provided as classes (i.e. not as instances returned
 * from factory methods), allowing for runtime enhancements through a generated subclass.
 * <li>Configuration classes must be non-final (allowing for subclasses at runtime),
 * unless the {@link #proxyBeanMethods() proxyBeanMethods} flag is set to {@code false}
 * in which case no runtime-generated subclass is necessary.
 * <li>Configuration classes must be non-local (i.e. may not be declared within a method).
 * <li>Any nested configuration classes must be declared as {@code static}.
 * <li>{@code @Bean} methods may not in turn create further configuration classes
 * (any such instances will be treated as regular beans, with their configuration
 * annotations remaining undetected).
 * </ul>
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Bean
 * @see Profile
 * @see Import
 * @see ImportResource
 * @see ComponentScan
 * @see Lazy
 * @see PropertySource
 * @see AnnotationConfigApplicationContext
 * @see ConfigurationClassPostProcessor
 * @see org.springframework.core.env.Environment
 * @see org.springframework.test.context.ContextConfiguration
 */
// 表示一个类声明了一个或多个 {@link Bean @Bean} 方法，并且可以由 Spring 容器处理以在运行时为这些 bean 生成 bean 定义和服务请求，
// 例如：
//
// <pre class="code">
// @Configuration
// public class AppConfig {
//
// 		@Bean
// 		public MyBean myBean() {
// 			// instantiate, configure and return bean ...
// 		}
// }
// </pre>
//
// <h2>引导 {@code @Configuration} 类</h2>
//
// <h3>通过 {@code AnnotationConfigApplicationContext} </h3>
//
// <p>{@code @Configuration} 类通常使用 {@link AnnotationConfigApplicationContext} 或
// 其支持 Web 的变体 {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext} 进行引导。
// 前者的一个简单示例如下：
//
// <pre class="code">
// 		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
// 		ctx.register(AppConfig.class);
// 		ctx.refresh();
// 		MyBean myBean = ctx.getBean(MyBean.class);
// 		// 使用 myBean ...
// </pre>
//
// <p>有关更多详细信息，请参阅 {@link AnnotationConfigApplicationContext} javadoc，有关 {@code Servlet} 容器中的 Web 配置说明，
// 请参阅 {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext AnnotationConfigWebApplicationContext}。
//
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
// <p>在上面的示例中，需要 {@code <context:annotation-config/> 才能启用 {@link ConfigurationClassPostProcessor} 和其他与注释相关的后处理器，以便于处理 {@code @Configuration} 类。
//
// <h3>通过组件扫描</h3>
//
// <p>由于 {@code @Configuration} 使用 {@link Component @Component} 进行元注释，因此 {@code @Configuration} 类是组件扫描的候选者 ;
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
//
// <h2>使用外部化值</h2>
//
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
//
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
//
// <h2>组合 {@code @Configuration} 类</h2>
//
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
//
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
//
// <h3>使用 Spring XML 使用 {@code @ImportResource} 注解</h3>

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
//
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
//
// <h2>配置延迟初始化</h2>
//
// <p>默认情况下，{@code @Bean} 方法将在容器启动时<em>立即实例化</em>。
// 为了避免这种情况，可以将 {@code @Configuration} 与 {@link Lazy @Lazy} 批注结合使用，以指示类中声明的所有 {@code @Bean} 方法默认都采用延迟初始化。
// 请注意，{@code @Lazy} 也可以用于单个 {@code @Bean} 方法。
//
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
//
// <h2>使用 {@code @Enable} 注释启用内置的 Spring 功能</h2>
//
// <p>可以使用各自的“{@code @Enable}”注释从 {@code @Configuration} 类启用和配置 Spring 功能，例如异步方法执行、计划任务执行、注释驱动的事务管理，甚至 Spring MVC。
// 有关详细信息，请参阅 {@link org.springframework.scheduling.annotation.EnableAsync @EnableAsync}、
// {@link org.springframework.scheduling.annotation.EnableScheduling @EnableScheduling}、
// {@link org.springframework.transaction.annotation.EnableTransactionManagement @EnableTransactionManagement}、
// {@link org.springframework.context.annotation.EnableAspectJAutoProxy @EnableAspectJAutoProxy} 和
// {@link org.springframework.web.servlet.config.annotation.EnableWebMvc @EnableWebMvc}。
//
// <h2>编写 {@code @Configuration} 类时的约束</h2>
//
// <ul>
// <li>配置类必须以类的形式提供（即，而不是作为从工厂方法返回的实例），从而允许通过生成的子类进行运行时增强。
// <li>配置类必须是非最终的（允许在运行时创建子类），除非 {@link #proxyBeanMethods() proxyBeanMethods} 标志设置为 {@code false}，
// 在这种情况下无需在运行时生成子类。
// <li>配置类必须是非本地的（即不能在方法内声明）。
// <li>任何嵌套的配置类都必须声明为 {@code static}。
// <li>{@code @Bean} 方法不得依次创建其他配置类（任何此类实例都将被视为常规 Bean，其配置注解将保持不被检测到的状态）。
// </ul>
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

	/**
	 * Explicitly specify the name of the Spring bean definition associated with the
	 * {@code @Configuration} class. If left unspecified (the common case), a bean
	 * name will be automatically generated.
	 * <p>The custom name applies only if the {@code @Configuration} class is picked
	 * up via component scanning or supplied directly to an
	 * {@link AnnotationConfigApplicationContext}. If the {@code @Configuration} class
	 * is registered as a traditional XML bean definition, the name/id of the bean
	 * element will take precedence.
	 * <p>Alias for {@link Component#value}.
	 * @return the explicit component name, if any (or empty String otherwise)
	 * @see AnnotationBeanNameGenerator
	 */
	// 明确指定与 {@code @Configuration} 类关联的 Spring bean 定义的名称。如果未指定（常见情况），则会自动生成 bean 名称。
	// <p>仅当通过组件扫描获取 {@code @Configuration} 类或将其直接提供给 {@link AnnotationConfigApplicationContext} 时，才会应用自定义名称。
	// 如果 {@code @Configuration} 类注册为传统 XML bean 定义，则 bean 元素的名称/ID 将优先使用。
	//
	// <p>{@link Component#value} 的别名。@return 显式组件名称（如果有）（否则返回空字符串）
	@AliasFor(annotation = Component.class)
	String value() default "";

	/**
	 * Specify whether {@code @Bean} methods should get proxied in order to enforce
	 * bean lifecycle behavior, e.g. to return shared singleton bean instances even
	 * in case of direct {@code @Bean} method calls in user code. This feature
	 * requires method interception, implemented through a runtime-generated CGLIB
	 * subclass which comes with limitations such as the configuration class and
	 * its methods not being allowed to declare {@code final}.
	 * <p>The default is {@code true}, allowing for 'inter-bean references' via direct
	 * method calls within the configuration class as well as for external calls to
	 * this configuration's {@code @Bean} methods, e.g. from another configuration class.
	 * If this is not needed since each of this particular configuration's {@code @Bean}
	 * methods is self-contained and designed as a plain factory method for container use,
	 * switch this flag to {@code false} in order to avoid CGLIB subclass processing.
	 * <p>Turning off bean method interception effectively processes {@code @Bean}
	 * methods individually like when declared on non-{@code @Configuration} classes,
	 * a.k.a. "@Bean Lite Mode" (see {@link Bean @Bean's javadoc}). It is therefore
	 * behaviorally equivalent to removing the {@code @Configuration} stereotype.
	 * @since 5.2
	 */
	// 指定是否应代理 {@code @Bean} 方法以强制执行 Bean 生命周期行为，
	// 例如，即使在用户代码中直接调用 {@code @Bean} 方法时也返回共享的单例 Bean 实例。
	// 此功能需要方法拦截，通过运行时生成的 CGLIB 子类实现，但这存在一些限制，例如配置类及其方法不允许声明 {@code final}。
	//
	// <p>默认值为 {@code true}，允许通过配置类内的直接方法调用进行“Bean 间引用”，
	// 以及允许从外部调用此配置的 {@code @Bean} 方法（例如从另一个配置类调用）。
	// 如果由于此特定配置的每个 {@code @Bean} 方法都是自包含的并且设计为供容器使用的普通工厂方法而不需要这样做，
	// 请将此标志切换为 {@code false} 以避免 CGLIB 子类处理。
	//
	// <p>关闭 Bean 方法拦截可以有效地单独处理 {@code @Bean} 方法，就像在非 {@code @Configuration} 类上声明一样，
	// 也称为“@Bean 精简模式”（参见 {@link Bean @Bean 的 javadoc}）。
	// 因此，它在行为上等同于删除 {@code @Configuration} 构造型。
	boolean proxyBeanMethods() default true;

	/**
	 * Specify whether {@code @Bean} methods need to have unique method names,
	 * raising an exception otherwise in order to prevent accidental overloading.
	 * <p>The default is {@code true}, preventing accidental method overloads which
	 * get interpreted as overloaded factory methods for the same bean definition
	 * (as opposed to separate bean definitions with individual conditions etc).
	 * Switch this flag to {@code false} in order to allow for method overloading
	 * according to those semantics, accepting the risk for accidental overlaps.
	 * @since 6.0
	 */
	// 指定 {@code @Bean} 方法是否需要具有唯一的方法名称，否则会引发异常以防止意外重载。
	//
	// <p>默认值为 {@code true}，用于防止意外方法重载，这些方法重载会被解释为同一 bean 定义的重载工厂方法（而不是具有单独条件等的单独 bean 定义）。
	// 将此标志切换为 {@code false}，以便允许根据这些语义进行方法重载，但接受意外重叠的风险。
	boolean enforceUniqueMethods() default true;

}
