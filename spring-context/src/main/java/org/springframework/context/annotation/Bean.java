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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.annotation.AliasFor;

/**
 * Indicates that a method produces a bean to be managed by the Spring container.
 *
 * <h3>Overview</h3>
 *
 * <p>The names and semantics of the attributes to this annotation are intentionally
 * similar to those of the {@code <bean/>} element in the Spring XML schema. For
 * example:
 *
 * <pre class="code">
 *     @Bean
 *     public MyBean myBean() {
 *         // instantiate and configure MyBean obj
 *         return obj;
 *     }
 * </pre>
 *
 * <h3>Bean Names</h3>
 *
 * <p>While a {@link #name} attribute is available, the default strategy for
 * determining the name of a bean is to use the name of the {@code @Bean} method.
 * This is convenient and intuitive, but if explicit naming is desired, the
 * {@code name} attribute (or its alias {@code value}) may be used. Also note
 * that {@code name} accepts an array of Strings, allowing for multiple names
 * (i.e. a primary bean name plus one or more aliases) for a single bean.
 *
 * <pre class="code">
 *     @Bean({"b1", "b2"}) // bean available as 'b1' and 'b2', but not 'myBean'
 *     public MyBean myBean() {
 *         // instantiate and configure MyBean obj
 *         return obj;
 *     }
 * </pre>
 *
 * <h3>Profile, Scope, Lazy, DependsOn, Primary, Order</h3>
 *
 * <p>Note that the {@code @Bean} annotation does not provide attributes for profile,
 * scope, lazy, depends-on or primary. Rather, it should be used in conjunction with
 * {@link Scope @Scope}, {@link Lazy @Lazy}, {@link DependsOn @DependsOn} and
 * {@link Primary @Primary} annotations to declare those semantics. For example:
 *
 * <pre class="code">
 *     @Bean
 *     @Profile("production")
 *     @Scope("prototype")
 *     public MyBean myBean() {
 *         // instantiate and configure MyBean obj
 *         return obj;
 *     }
 * </pre>
 *
 * The semantics of the above-mentioned annotations match their use at the component
 * class level: {@code @Profile} allows for selective inclusion of certain beans.
 * {@code @Scope} changes the bean's scope from singleton to the specified scope.
 * {@code @Lazy} only has an actual effect in case of the default singleton scope.
 * {@code @DependsOn} enforces the creation of specific other beans before this
 * bean will be created, in addition to any dependencies that the bean expressed
 * through direct references, which is typically helpful for singleton startup.
 * {@code @Primary} is a mechanism to resolve ambiguity at the injection point level
 * if a single target component needs to be injected but several beans match by type.
 *
 * <p>Additionally, {@code @Bean} methods may also declare qualifier annotations
 * and {@link org.springframework.core.annotation.Order @Order} values, to be
 * taken into account during injection point resolution just like corresponding
 * annotations on the corresponding component classes but potentially being very
 * individual per bean definition (in case of multiple definitions with the same
 * bean class). Qualifiers narrow the set of candidates after the initial type match;
 * order values determine the order of resolved elements in case of collection
 * injection points (with several target beans matching by type and qualifier).
 *
 * <p><b>NOTE:</b> {@code @Order} values may influence priorities at injection points,
 * but please be aware that they do not influence singleton startup order which is an
 * orthogonal concern determined by dependency relationships and {@code @DependsOn}
 * declarations as mentioned above. Also, {@link jakarta.annotation.Priority} is not
 * available at this level since it cannot be declared on methods; its semantics can
 * be modeled through {@code @Order} values in combination with {@code @Primary} on
 * a single bean per type.
 *
 * <h3>{@code @Bean} Methods in {@code @Configuration} Classes</h3>
 *
 * <p>Typically, {@code @Bean} methods are declared within {@code @Configuration}
 * classes. In this case, bean methods may reference other {@code @Bean} methods in the
 * same class by calling them <i>directly</i>. This ensures that references between beans
 * are strongly typed and navigable. Such so-called <em>'inter-bean references'</em> are
 * guaranteed to respect scoping and AOP semantics, just like {@code getBean()} lookups
 * would. These are the semantics known from the original 'Spring JavaConfig' project
 * which require CGLIB subclassing of each such configuration class at runtime. As a
 * consequence, {@code @Configuration} classes and their factory methods must not be
 * marked as final or private in this mode. For example:
 *
 * <pre class="code">
 * @Configuration
 * public class AppConfig {
 *
 *     @Bean
 *     public FooService fooService() {
 *         return new FooService(fooRepository());
 *     }
 *
 *     @Bean
 *     public FooRepository fooRepository() {
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     // ...
 * }</pre>
 *
 * <h3>{@code @Bean} <em>Lite</em> Mode</h3>
 *
 * <p>{@code @Bean} methods may also be declared within classes that are <em>not</em>
 * annotated with {@code @Configuration}. If a bean method is declared on a bean
 * that is <em>not</em> annotated with {@code @Configuration} it is processed in a
 * so-called <em>'lite'</em> mode.
 *
 * <p>Bean methods in <em>lite</em> mode will be treated as plain <em>factory
 * methods</em> by the container (similar to {@code factory-method} declarations
 * in XML), with scoping and lifecycle callbacks properly applied. The containing
 * class remains unmodified in this case, and there are no unusual constraints for
 * the containing class or the factory methods.
 *
 * <p>In contrast to the semantics for bean methods in {@code @Configuration} classes,
 * <em>'inter-bean references'</em> are not supported in <em>lite</em> mode. Instead,
 * when one {@code @Bean}-method invokes another {@code @Bean}-method in <em>lite</em>
 * mode, the invocation is a standard Java method invocation; Spring does not intercept
 * the invocation via a CGLIB proxy. This is analogous to inter-{@code @Transactional}
 * method calls where in proxy mode, Spring does not intercept the invocation &mdash;
 * Spring does so only in AspectJ mode.
 *
 * <p>For example:
 *
 * <pre class="code">
 * @Component
 * public class Calculator {
 *     public int sum(int a, int b) {
 *         return a+b;
 *     }
 *
 *     @Bean
 *     public MyBean myBean() {
 *         return new MyBean();
 *     }
 * }</pre>
 *
 * <h3>Bootstrapping</h3>
 *
 * <p>See the @{@link Configuration} javadoc for further details including how to bootstrap
 * the container using {@link AnnotationConfigApplicationContext} and friends.
 *
 * <h3>{@code BeanFactoryPostProcessor}-returning {@code @Bean} methods</h3>
 *
 * <p>Special consideration must be taken for {@code @Bean} methods that return Spring
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * ({@code BFPP}) types. Because {@code BFPP} objects must be instantiated very early in the
 * container lifecycle, they can interfere with processing of annotations such as {@code @Autowired},
 * {@code @Value}, and {@code @PostConstruct} within {@code @Configuration} classes. To avoid these
 * lifecycle issues, mark {@code BFPP}-returning {@code @Bean} methods as {@code static}. For example:
 *
 * <pre class="code">
 *     @Bean
 *     public static PropertySourcesPlaceholderConfigurer pspc() {
 *         // instantiate, configure and return pspc ...
 *     }
 * </pre>
 *
 * By marking this method as {@code static}, it can be invoked without causing instantiation of its
 * declaring {@code @Configuration} class, thus avoiding the above-mentioned lifecycle conflicts.
 * Note however that {@code static} {@code @Bean} methods will not be enhanced for scoping and AOP
 * semantics as mentioned above. This works out in {@code BFPP} cases, as they are not typically
 * referenced by other {@code @Bean} methods. As a reminder, an INFO-level log message will be
 * issued for any non-static {@code @Bean} methods having a return type assignable to
 * {@code BeanFactoryPostProcessor}.
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see Configuration
 * @see Scope
 * @see DependsOn
 * @see Lazy
 * @see Primary
 * @see org.springframework.stereotype.Component
 * @see org.springframework.beans.factory.annotation.Autowired
 * @see org.springframework.beans.factory.annotation.Value
 */
// 指示某个方法生成一个由 Spring 容器管理的 Bean。
//
// <h3>概述</h3>
//
// <p>此注解的属性名称和语义与 Spring XML Schema 中 {@code <bean/>} 元素的属性名称和语义有意设计得类似。例如：
//
// <pre class="code">
// @Bean
// public MyBean myBean() {
// 		// instantiate and configure MyBean obj return obj;
// }
// </pre>
//
// <h3>Bean 名称</h3>
//
// <p>虽然可以使用 {@link #name} 属性，但确定 Bean 名称的默认策略是使用 {@code @Bean} 方法的名称。
// 这很方便且直观，但如果需要显式命名，则可以使用 {@code name} 属性（或其别名 {@code value}）。
// 另请注意，{@code name} 接受字符串数组，允许单个 Bean 使用多个名称（即，一个主 Bean 名称加上一个或多个别名）。
//
// <pre class="code">
// @Bean({"b1", "b2"})	// bean 可用作“b1”和“b2”，但不可用作“myBean”
// public MyBean myBean() {
// 		// 实例化并配置 MyBean obj
// 		return obj;
// }
// </pre>
//
// <h3>Profile、Scope、Lazy、DependsOn、Primary、Order</h3>
//
// <p>请注意，{@code @Bean} 注解不提供 profile、scope、lazy、depends-on 或 primary 属性。
// 相反，它应该与 {@link Scope @Scope}、{@link Lazy @Lazy}、{@link DependsOn @DependsOn} 和 {@link Primary @Primary} 注解结合使用来声明这些语义。例如：
//
// <pre class="code">
// @Bean
// @Profile("production")
// @Scope("prototype")
// public MyBean myBean() {
// 		// instantiate and configure MyBean obj
// 		return obj;
// }
// </pre>
//
// 上述注解的语义与它们在组件类级别的使用相匹配： {@code @Profile} 允许选择性地包含某些 bean。 {@code @Scope} 将 bean 的范围从单例更改为指定范围。{@code @Lazy} 仅在默认单例范围的情况下才有实际效果。
// {@code @DependsOn} 强制在创建此 bean 之前创建特定的其他 bean，以及 bean 通过直接引用表达的任何依赖项，这通常有助于单例启动。
// {@code @Primary} 是一种在需要注入单个目标组件但多个 bean 按类型匹配时在注入点级别解决歧义的机制。
//
// <p>此外，{@code @Bean} 方法还可以声明限定符注解和 {@link org.springframework.core.annotation.Order @Order} 值，
// 这些值将在注入点解析期间被考虑在内，就像相应组件类上的相应注解一样，但每个 Bean 定义可能都非常独特（在同一个 Bean 类的多个定义的情况下）。
// 限定符在初始类型匹配后缩小候选集；在集合注入点的情况下（多个目标 Bean 按类型和限定符匹配），顺序值确定已解析元素的顺序。
//
// <p><b>注意：</b> {@code @Order} 值可能会影响注入点的优先级，但请注意，它们不会影响单例启动顺序，单例启动顺序是由依赖关系和 {@code @DependsOn} 声明确定的正交关注点，如上所述。
// 此外，{@link jakarta.annotation.Priority} 在此级别不可用，因为它不能在方法上声明；
// 其语义可以通过将 {@code @Order} 值与每个类型的单个 bean 上的 {@code @Primary} 结合来建模。
//
// <h3>{@code @Bean} 在 {@code @Configuration} 类中的方法</h3>
//
// <p>通常，{@code @Bean} 方法在 {@code @Configuration} 类中声明。在这种情况下，bean 方法可以通过<i>直接</i>调用同一个类中的其他 {@code @Bean} 方法。
// 这确保了 bean 之间的引用是强类型且可导航的。这种所谓的<em>“bean 间引用”</em>保证遵守作用域和 AOP 语义，就像 {@code getBean()} 查找一样。
// 这些是从原始“Spring JavaConfig”项目中得知的语义，该项目要求在运行时对每个此类配置类进行 CGLIB 子类化。
// 因此，在这种模式下，{@code @Configuration} 类及其工厂方法不能标记为 final 或 private。例如：
//
// <pre class="code">
// @Configuration
// public class AppConfig {
//
// 		@Bean
// 		public FooService fooService() {
// 			return new FooService(fooRepository());
// 		}
//
// 		@Bean
// 		public FooRepository fooRepository() {
// 			return new JdbcFooRepository(dataSource());
// 		}
// 		// ...
// </pre>
//
// <h3>{@code @Bean} <em>精简</em> 模式</h3>
//
// <p>{@code @Bean} 方法也可以在未使用 {@code @Configuration} 注释的类中声明。
// 如果在未使用 {@code @Configuration} 注释的 bean 上声明 bean 方法，则会以所谓的 <em>“精简”</em> 模式进行处理。
//
// <p>容器将 <em>精简</em> 模式下的 Bean 方法视为普通的 <em>工厂方法</em>（类似于 XML 中的 {@code factory-method} 声明），并正确应用作用域和生命周期回调。
// 在这种情况下，包含类保持不变，并且包含类或工厂方法没有任何异常约束。
//
// <p>与 {@code @Configuration} 类中 Bean 方法的语义不同，<em>精简</em> 模式下不支持“Bean 间引用”。
// 相反，当一个 {@code @Bean} 方法在 <em>精简</em> 模式下调用另一个 {@code @Bean} 方法时，该调用是标准的 Java 方法调用；
// Spring 不会通过 CGLIB 代理拦截该调用。这类似于代理模式下的 {@code @Transactional} 方法间调用，Spring 不会拦截该调用 - Spring 仅在 AspectJ 模式下会拦截该调用。
//
// <p>例如：
//
// <pre class="code">
// @Component
// public class Calculator {
//
// 		public int sum(int a, int b) {
// 			return a+b;
// 		}
//
// 		@Bean
// 		public MyBean myBean() {
// 			return new MyBean();
// 		}
// </pre>
//
// <h3>引导</h3>
//
// <p>有关更多详细信息，包括如何使用 {@link AnnotationConfigApplicationContext} 和朋友引导容器，请参阅 @{@link Configuration} javadoc。
//
// <h3>{@code BeanFactoryPostProcessor} - 返回 {@code @Bean} 方法</h3>
//
// <p>对于返回 Spring {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor} ({@code BFPP})类型的 {@code @Bean} 方法，必须特别考虑。
// 由于 {@code BFPP} 对象必须在容器生命周期的早期阶段实例化，因此它们可能会干扰 {@code @Configuration} 类中对 {@code @Autowired}、{@code @Value} 和 {@code @PostConstruct} 等注解的处理。
// 为了避免这些生命周期问题，请将返回 {@code BFPP} 的 {@code @Bean} 方法标记为 {@code static}。
//
// 例如：
//
// <pre class="code">
// @Bean
// public static PropertySourcesPlaceholderConfigurer pspc() {
// 		// instantiate, configure and return pspc ...
// }
// </pre>
//
// 将此方法标记为 {@code static}，则可以在不实例化其声明 {@code @Configuration} 类的情况下调用该方法，从而避免上述生命周期冲突。
// 但请注意，如上所述，{@code static} {@code @Bean} 方法不会针对作用域和 AOP 语义进行增强。
// 这在 {@code BFPP} 情况下有效，因为它们通常不会被其他 {@code @Bean} 方法引用。
// 提醒一下，对于任何非静态 {@code @Bean} 方法，如果其返回类型可分配给 {@code BeanFactoryPostProcessor}，则会发出 INFO 级别的日志消息。
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

	/**
	 * Alias for {@link #name}.
	 * <p>Intended to be used when no other attributes are needed, for example:
	 * {@code @Bean("customBeanName")}.
	 * @since 4.3.3
	 * @see #name
	 */
	// {@link #name} 的别名。
	// <p>用于不需要其他属性时，例如：{@code @Bean("customBeanName")}。
	@AliasFor("name")
	String[] value() default {};

	/**
	 * The name of this bean, or if several names, a primary bean name plus aliases.
	 * <p>If left unspecified, the name of the bean is the name of the annotated method.
	 * If specified, the method name is ignored.
	 * <p>The bean name and aliases may also be configured via the {@link #value}
	 * attribute if no other attributes are declared.
	 * @see #value
	 */
	// 此 bean 的名称，如果有多个名称，则为主 bean 名称加上别名。
	// <p>如果未指定，则 bean 的名称为被注解的方法名称。如果指定，则方法名称将被忽略。
	// <p>如果未声明其他属性，也可以通过 {@link #value} 属性配置 bean 名称和别名。
	@AliasFor("value")
	String[] name() default {};

	/**
	 * Is this bean a candidate for getting autowired into some other bean?
	 * <p>Default is {@code true}; set this to {@code false} for internal delegates
	 * that are not meant to get in the way of beans of the same type in other places.
	 * @since 5.1
	 */
	// 此 Bean 是否可以自动装配到其他 Bean 中？
	// <p>默认值为 {@code true}；对于内部委托，如果不想妨碍其他地方同类型的 Bean，则将其设置为 {@code false}。
	boolean autowireCandidate() default true;

	/**
	 * The optional name of a method to call on the bean instance during initialization.
	 * Not commonly used, given that the method may be called programmatically directly
	 * within the body of a Bean-annotated method.
	 * <p>The default value is {@code ""}, indicating no init method to be called.
	 * @see org.springframework.beans.factory.InitializingBean
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	// 初始化期间在 Bean 实例上调用的方法的可选名称。由于该方法可以在 Bean 注解的方法主体内直接以编程方式调用，因此不常用。
	// <p>默认值为 {@code ""}，表示不调用任何 init 方法。
	String initMethod() default "";

	/**
	 * The optional name of a method to call on the bean instance upon closing the
	 * application context, for example a {@code close()} method on a JDBC
	 * {@code DataSource} implementation, or a Hibernate {@code SessionFactory} object.
	 * The method must have no arguments but may throw any exception.
	 * <p>As a convenience to the user, the container will attempt to infer a destroy
	 * method against an object returned from the {@code @Bean} method. For example, given
	 * an {@code @Bean} method returning an Apache Commons DBCP {@code BasicDataSource},
	 * the container will notice the {@code close()} method available on that object and
	 * automatically register it as the {@code destroyMethod}. This 'destroy method
	 * inference' is currently limited to detecting only public, no-arg methods named
	 * 'close' or 'shutdown'. The method may be declared at any level of the inheritance
	 * hierarchy and will be detected regardless of the return type of the {@code @Bean}
	 * method (i.e., detection occurs reflectively against the bean instance itself at
	 * creation time).
	 * <p>To disable destroy method inference for a particular {@code @Bean}, specify an
	 * empty string as the value, e.g. {@code @Bean(destroyMethod="")}. Note that the
	 * {@link org.springframework.beans.factory.DisposableBean} callback interface will
	 * nevertheless get detected and the corresponding destroy method invoked: In other
	 * words, {@code destroyMethod=""} only affects custom close/shutdown methods and
	 * {@link java.io.Closeable}/{@link java.lang.AutoCloseable} declared close methods.
	 * <p>Note: Only invoked on beans whose lifecycle is under the full control of the
	 * factory, which is always the case for singletons but not guaranteed for any
	 * other scope.
	 * @see org.springframework.beans.factory.DisposableBean
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	// 关闭应用上下文时，在 Bean 实例上调用的可选方法名称，例如 JDBC {@code DataSource} 实现或 Hibernate {@code SessionFactory} 对象上的 {@code close()} 方法。
	// 该方法必须没有参数，但可以抛出任何异常。
	// <p>为方便用户，容器将尝试根据 {@code @Bean} 方法返回的对象推断其销毁方法。
	// 例如，给定一个返回 Apache Commons DBCP {@code BasicDataSource} 的 {@code @Bean} 方法，
	// 容器将注意到该对象上可用的 {@code close()} 方法，并自动将其注册为 {@code destroyMethod}。
	// 此“销毁方法推断”目前仅限于检测名为“close”或“shutdown”的公共、无参数方法。
	// 该方法可以在继承层次结构的任何级别声明，并且无论 {@code @Bean} 方法的返回类型如何，都会被检测到（即，检测是在创建时针对 Bean 实例本身进行反射式检测）。
	// <p>要禁用特定 {@code @Bean} 的销毁方法推断，请指定一个空字符串作为值，例如 {@code @Bean(destroyMethod="")}。
	// 请注意，{@link org.springframework.beans.factory.DisposableBean} 回调接口仍然会被检测到，并且相应的销毁方法会被调用：
	// 换句话说，{@code destroyMethod=""} 仅影响自定义关闭/关闭方法和 {@link java.io.Closeable}/{@link java.lang.AutoCloseable} 声明的关闭方法。
	// <p>注意：仅在生命周期完全由工厂控制的 Bean 上调用，对于单例来说始终如此，但不保证在任何其他范围中都会如此。
	String destroyMethod() default AbstractBeanDefinition.INFER_METHOD;

}
