/**
 * @Autowired
 *
 * @see org.springframework.beans.factory.annotation.Autowired
 *
 * @see org.springframework.sample.bean_post_processor.resolve_dependency
 *
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject(Object, String, org.springframework.beans.PropertyValues)
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredMethodElement#inject(Object, String, org.springframework.beans.PropertyValues)
 */
package org.springframework.sample.bean_post_processor.annotation_autowired.autowired;
/*		@Autowired
********************************* Class API Docs *********************************
此注解用于标记构造函数、字段、setter 方法或配置方法，使其由 Spring 的依赖注入机制自动装配。
它是 JSR-330 {@link jakarta.inject.Inject} 注解的替代方案，增加了必需与可选语义。

<h3>自动装配的构造函数</h3>

<p>任何给定 bean 类只能有一个构造函数声明此注解，并将 {@link #required} 属性设置为 {@code true}，表示该构造函数在用作 Spring bean 时将被自动装配。
此外，如果 {@code required} 属性设置为 {@code true}，则只能有一个构造函数使用 {@code @Autowired} 注解。如果多个非必需的构造函数声明了此注解，它们将被视为自动装配的候选对象。
系统将选择依赖项数量最多的构造函数，这些依赖项可以通过 Spring 容器中匹配的 bean 来满足。

</p>如果所有候选构造函数都无法满足要求，则会使用主构造函数/默认构造函数（如果存在）。
类似地，如果一个类声明了多个构造函数，但没有一个构造函数使用 `@Autowired` 注解，则会使用主构造函数/默认构造函数（如果存在）。
如果一个类一开始只声明了一个构造函数，则始终会使用该构造函数，即使它没有被注解。被注解的构造函数不必是公共的。

<h3>自动注入字段</h3>

<p>字段会在 bean 构造完成后立即注入，在调用任何配置方法之前。这样的配置字段不必是公共的。

<h3>自动注入方法</h3>

<p>配置方法可以具有任意名称和任意数量的参数；每个参数都会自动注入到 Spring 容器中匹配的 bean 中。
bean 属性 setter 方法实际上只是这种通用配置方法的一个特例。这样的配置方法不必是公共的。

<h3>自动装配参数</h3>

<p>虽然从 Spring Framework 5.0 开始，理论上可以在单个方法或构造函数参数上声明 {@code @Autowired}，但框架的大部分组件都会忽略此类声明。
Spring Framework 核心组件中唯一积极支持自动装配参数的部分是 {@code spring-test} 模块中的 JUnit Jupiter 支持
（详情请参阅 <a href="https:docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext 框架</a> 参考文档）。

<h3>多个参数和“required”语义</h3>

<p>对于多参数构造函数或方法，{@link #required} 属性适用于所有参数。
单个参数可以声明为 Java 8 风格的 {@link java.util.Optional}，或者从 Spring Framework 5.0 开始，
也可以声明为 {@code @Nullable} 或 Kotlin 中的非空参数类型，从而覆盖基本的“required”语义。

<h3>自动装配数组、集合和映射</h3>

<p>对于数组、{@link java.util.Collection} 或 {@link java.util.Map} 依赖类型，容器会自动装配所有与声明的值类型匹配的 bean。
为此，映射的键必须声明为 {@code String} 类型，该类型将被解析为相应的 bean 名称。容器提供的此类集合将按顺序排列，
顺序依据目标组件的 {@link org.springframework.core.Ordered Ordered} 和
{@link org.springframework.core.annotation.Order @Order} 值，否则将按照它们在容器中的注册顺序排列。
或者，单个匹配的目标 bean 本身也可以是通用类型的 {@code Collection} 或 {@code Map}，并按此方式注入。

<h3>{@code BeanPostProcessor} 或 {@code BeanFactoryPostProcessor} 不支持此操作。</h3>

<p>请注意，实际的注入是通过 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor} 执行的，
这意味着您<em>不能</em>使用 {@code @Autowired} 将引用注入到 {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
或 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor} 类型中。
请参阅 {@link AutowiredAnnotationBeanPostProcessor} 类的 Javadoc（默认情况下，该类会检查是否存在此注解）。


********************************* Class Definition *********************************
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
	boolean required() default true;
}
**/