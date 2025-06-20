/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * Enables Spring's annotation-driven transaction management capability, similar to
 * the support found in Spring's {@code <tx:*>} XML namespace. To be used on
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * classes to configure traditional, imperative transaction management or
 * reactive transaction management.
 *
 * <p>The following example demonstrates imperative transaction management
 * using a {@link org.springframework.transaction.PlatformTransactionManager
 * PlatformTransactionManager}. For reactive transaction management, configure a
 * {@link org.springframework.transaction.ReactiveTransactionManager
 * ReactiveTransactionManager} instead.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 * }</pre>
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 *
 * <pre class="code">
 * &lt;beans&gt;
 *
 *     &lt;tx:annotation-driven/&gt;
 *
 *     &lt;bean id="fooRepository" class="com.foo.JdbcFooRepository"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 *     &lt;bean id="dataSource" class="com.vendor.VendorDataSource"/&gt;
 *
 *     &lt;bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager"&gt;
 *         &lt;constructor-arg ref="dataSource"/&gt;
 *     &lt;/bean&gt;
 *
 * &lt;/beans&gt;
 * </pre>
 *
 * In both of the scenarios above, {@code @EnableTransactionManagement} and {@code
 * <tx:annotation-driven/>} are responsible for registering the necessary Spring
 * components that power annotation-driven transaction management, such as the
 * TransactionInterceptor and the proxy- or AspectJ-based advice that weaves the
 * interceptor into the call stack when {@code JdbcFooRepository}'s {@code @Transactional}
 * methods are invoked.
 *
 * <p>A minor difference between the two examples lies in the naming of the {@code
 * TransactionManager} bean: In the {@code @Bean} case, the name is
 * <em>"txManager"</em> (per the name of the method); in the XML case, the name is
 * <em>"transactionManager"</em>. {@code <tx:annotation-driven/>} is hard-wired to
 * look for a bean named "transactionManager" by default, however
 * {@code @EnableTransactionManagement} is more flexible; it will fall back to a by-type
 * lookup for any {@code TransactionManager} bean in the container. Thus the name
 * can be "txManager", "transactionManager", or "tm": it simply does not matter.
 *
 * <p>For those that wish to establish a more direct relationship between
 * {@code @EnableTransactionManagement} and the exact transaction manager bean to be used,
 * the {@link TransactionManagementConfigurer} callback interface may be implemented -
 * notice the {@code implements} clause and the {@code @Override}-annotated method below:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig implements TransactionManagementConfigurer {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 *
 *     &#064;Override
 *     public PlatformTransactionManager annotationDrivenTransactionManager() {
 *         return txManager();
 *     }
 * }</pre>
 *
 * <p>This approach may be desirable simply because it is more explicit, or it may be
 * necessary in order to distinguish between two {@code TransactionManager} beans
 * present in the same container.  As the name suggests, the
 * {@code annotationDrivenTransactionManager()} will be the one used for processing
 * {@code @Transactional} methods. See {@link TransactionManagementConfigurer} Javadoc
 * for further details.
 *
 * <p>The {@link #mode} attribute controls how advice is applied: If the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior
 * of the proxying. Please note that proxy mode allows for interception of calls through
 * the proxy only; local calls within the same class cannot get intercepted that way.
 *
 * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then the
 * value of the {@link #proxyTargetClass} attribute will be ignored. Note also that in
 * this case the {@code spring-aspects} module JAR must be present on the classpath, with
 * compile-time weaving or load-time weaving applying the aspect to the affected classes.
 * There is no proxy involved in such a scenario; local calls will be intercepted as well.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see TransactionManagementConfigurer
 * @see TransactionManagementConfigurationSelector
 * @see ProxyTransactionManagementConfiguration
 * @see org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
 */
// 启用 Spring 的注解驱动事务管理功能，类似于 Spring 的 {@code <tx:>} XML 命名空间中的支持。
// 用于 {@link org.springframework.context.annotation.Configuration @Configuration} 类，以配置传统的命令式事务管理或响应式事务管理。
//
// <p>以下示例演示了使用 {@link org.springframework.transaction.PlatformTransactionManager PlatformTransactionManager} 的命令式事务管理。
// 对于响应式事务管理，请改为配置 {@link org.springframework.transaction.ReactiveTransactionManager ReactiveTransactionManager}。
//
// <pre class="code">
// @Configuration
// @EnableTransactionManagement
// public class AppConfig {
// 		@Bean
// 		public FooRepository fooRepository() {
// 			// 配置并返回具有 @Transactional 方法的类
// 			return new JdbcFooRepository(dataSource());
// 		}

// 		@Bean
// 		public DataSource dataSource() {
// 			// 配置并返回必要的 JDBC DataSource
// 		}
//
// 		@Bean
// 		public PlatformTransactionManager txManager() {
// 			return new DataSourceTransactionManager(dataSource());
// 		}
// }</pre>
//
// <p>作为参考，可以将上面的示例与以下 Spring XML 配置进行比较：
//
// <pre class="code">
// <beans>
// 		<tx:annotation-driven/>
//
// 		<bean id="fooRepository" class="com.foo.JdbcFooRepository">
// 			<constructor-arg ref="dataSource"/>
// 		</bean>
//
// 		<bean id="dataSource" class="com.vendor.VendorDataSource"/>
//
// 		<bean id="transactionManager" class="org.sfwk...DataSourceTransactionManager">
// 			<constructor-arg ref="dataSource"/>
// 		</bean>
// </beans>
// </pre>
//
// 在上述两种情况下，{@code @EnableTransactionManagement} 和 {@code <tx:annotation-driven/>} 负责注册支持注解驱动事务管理所需的 Spring 组件，
// 例如 TransactionInterceptor 和基于代理或基于 AspectJ 的建议，当调用 {@code JdbcFooRepository} 的 {@code @Transactional} 方法。
//
// <p>这两个示例之间的细微差别在于 {@code TransactionManager} bean 的命名：
// 在 {@code @Bean} 情况下，名称为 <em>“txManager”</em>（根据方法名称）；在 XML 情况下，名称为 <em>“transactionManager”</em>。
// {@code <tx:annotation-driven/>} 默认硬编码查找名为 “transactionManager” 的 bean，
// 但 {@code @EnableTransactionManagement} 更灵活；它会回退到按类型查找容器中任何 {@code TransactionManager} bean。
// 因此，名称可以是 “txManager”、“transactionManager” 或 “tm”：这完全没有关系。
//
// <p>对于那些希望在 {@code @EnableTransactionManagement} 和要使用的确切事务管理器 bean 之间建立更直接关系的人，
// 可以实现 {@link TransactionManagementConfigurer} 回调接口 - 请注意下面的 {@code implements} 子句和 {@code @Override} 注释方法：
//
// <pre class="code">
// @Configuration
// @EnableTransactionManagement
// public class AppConfig implements TransactionManagementConfigurer {
// 		@Bean public FooRepository fooRepository() {
// 			// 配置并返回具有 @Transactional 方法的类
// 			return new JdbcFooRepository(dataSource());
// 		}
//
// 		@Bean
// 		public DataSource dataSource() {
// 			// 配置并返回必要的 JDBC DataSource
// 		}
//
// 		@Bean
// 		public PlatformTransactionManager txManager() {
// 			return new DataSourceTransactionManager(dataSource());
// 		}
//
// 		@Override
// 		public PlatformTransactionManager commentDrivenTransactionManager() {
// 			return txManager();
// 		}
// }</pre>
//
// <p>这种方法可能是可取的，因为它更明确，或者可能需要区分同一容器中的两个 {@code TransactionManager} bean。
// 顾名思义，{@code commentDrivenTransactionManager()} 将用于处理 {@code @Transactional} 方法。
// 有关更多详细信息，请参阅 {@link TransactionManagementConfigurer} Javadoc。
//
// <p>{@link #mode} 属性控制如何应用建议：如果模式为 {@link AdviceMode#PROXY}（默认），则其他属性控制代理的行为。
// 请注意，代理模式仅允许通过代理拦截调用；同一类内的本地调用无法通过这种方式拦截。
//
// <p>请注意，如果 {@linkplain #mode} 设置为 {@link AdviceMode#ASPECTJ}，则 {@link #proxyTargetClass} 属性的值将被忽略。
// 另请注意，在这种情况下，{@code spring-aspects} 模块 JAR 必须存在于类路径中，编译时织入或加载时织入会将切面应用到受影响的类。
// 这种情况下不涉及代理；本地调用也会被拦截。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {

	/**
	 * Indicate whether subclass-based (CGLIB) proxies are to be created ({@code true})
	 * as opposed to standard Java interface-based proxies ({@code false}).
	 * The default is {@code false}. <strong>Applicable only if {@link #mode()}
	 * is set to {@link AdviceMode#PROXY}</strong>.
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with
	 * {@code @Transactional}. For example, other beans marked with Spring's
	 * {@code @Async} annotation will be upgraded to subclass proxying at the same
	 * time. This approach has no negative impact in practice unless one is explicitly
	 * expecting one type of proxy vs another, e.g. in tests.
	 */
	// 指示是否要创建基于子类 (CGLIB) 的代理 ({@code true})，而不是基于标准 Java 接口的代理 ({@code false})。
	// 默认值为 {@code false}。<strong>仅当 {@link #mode()} 设置为 {@link AdviceMode#PROXY} 时才适用。</strong>。
	// <p>请注意，将此属性设置为 {@code true} 将影响所有需要代理的 Spring 管理的 bean，而不仅仅是那些标有 {@code @Transactional} 的 bean。
	// 例如，其他标有 Spring 的 {@code @Async} 批注的 bean 将同时升级为子类代理。
	// 除非明确期望一种代理类型而不是另一种代理类型（例如在测试中），否则这种方法在实践中不会产生负面影响。
	boolean proxyTargetClass() default false;

	/**
	 * Indicate how transactional advice should be applied.
	 * <p><b>The default is {@link AdviceMode#PROXY}.</b>
	 * Please note that proxy mode allows for interception of calls through the proxy
	 * only. Local calls within the same class cannot get intercepted that way; an
	 * {@link Transactional} annotation on such a method within a local call will be
	 * ignored since Spring's interceptor does not even kick in for such a runtime
	 * scenario. For a more advanced mode of interception, consider switching this to
	 * {@link AdviceMode#ASPECTJ}.
	 */
	// 指示应如何应用事务通知。
	// <p><b>默认值为 {@link AdviceMode#PROXY}。</b>
	// 请注意，代理模式仅允许通过代理拦截调用。同一类中的本地调用无法通过这种方式拦截；
	// 本地调用中此类方法上的 {@link Transactional} 注解将被忽略，因为 Spring 的拦截器在这种运行时场景下甚至不会启动。
	// 如果需要更高级的拦截模式，请考虑将其切换为 {@link AdviceMode#ASPECTJ}。
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * Indicate the ordering of the execution of the transaction advisor
	 * when multiple advices are applied at a specific joinpoint.
	 * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
	 */
	// 当在特定连接点应用多个建议时，指示事务顾问的执行顺序。
	// <p>默认值为 {@link Ordered#LOWEST_PRECEDENCE}。
	int order() default Ordered.LOWEST_PRECEDENCE;

}
