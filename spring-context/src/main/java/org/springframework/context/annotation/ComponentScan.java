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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Configures component scanning directives for use with {@link Configuration @Configuration}
 * classes.
 *
 * <p>Provides support comparable to Spring's {@code <context:component-scan>}
 * XML namespace element.
 *
 * <p>Either {@link #basePackageClasses} or {@link #basePackages} (or its alias
 * {@link #value}) may be specified to define specific packages to scan. If specific
 * packages are not defined, scanning will occur recursively beginning with the
 * package of the class that declares this annotation.
 *
 * <p>Note that the {@code <context:component-scan>} element has an
 * {@code annotation-config} attribute; however, this annotation does not. This is because
 * in almost all cases when using {@code @ComponentScan}, default annotation config
 * processing (e.g. processing {@code @Autowired} and friends) is assumed. Furthermore,
 * when using {@link AnnotationConfigApplicationContext}, annotation config processors are
 * always registered, meaning that any attempt to disable them at the
 * {@code @ComponentScan} level would be ignored.
 *
 * <p>See {@link Configuration @Configuration}'s Javadoc for usage examples.
 *
 * <p>{@code @ComponentScan} can be used as a <em>{@linkplain Repeatable repeatable}</em>
 * annotation. {@code @ComponentScan} may also be used as a <em>meta-annotation</em>
 * to create custom <em>composed annotations</em> with attribute overrides.
 *
 * <p>Locally declared {@code @ComponentScan} annotations always take precedence
 * over and effectively <em>hide</em> {@code @ComponentScan} meta-annotations,
 * which allows explicit local configuration to override configuration that is
 * <em>meta-present</em> (including composed annotations meta-annotated with
 * {@code @ComponentScan}).
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @see Configuration
 */
// 配置与 {@link Configuration @Configuration} 类一起使用的组件扫描指令。
//
// <p>提供与 Spring 的 {@code <context:component-scan>} XML 命名空间元素相当的支持。
//
// <p>可以指定 {@link #basePackageClasses} 或 {@link #basePackages}（或其别名 {@link #value}）来定义要扫描的特定包。
// 如果未定义特定包，则扫描将从声明此注解的类的包开始递归进行。
//
// <p>请注意，{@code <context:component-scan>} 元素具有 {@code comment-config} 属性；但是此注解没有。
// 这是因为在几乎所有使用 {@code @ComponentScan} 的情况下，都假定使用默认注解配置处理（例如处理 {@code @Autowired} 及其朋友）。
// 此外，使用 {@link AnnotationConfigApplicationContext} 时，注释配置处理器始终处于注册状态，这意味着在 {@code @ComponentScan} 级别禁用它们的任何尝试都将被忽略。
//
// <p>有关使用示例，请参阅 {@link Configuration @Configuration} 的 Javadoc。
//
// <p>{@code @ComponentScan} 可用作 <em>{@linkplain Repeatable repeatable} 注释。
// {@code @ComponentScan} 还可用作 <em>元注释</em>，以创建带有属性覆盖的自定义 <em>组合注释</em>。
//
// <p>本地声明的 {@code @ComponentScan} 注释始终优先于并有效<em>隐藏</em> {@code @ComponentScan} 元注释，
// 这允许显式本地配置覆盖 <em>元存在</em> 的配置（包括使用 {@code @ComponentScan} 元注释的组合注释）。
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(ComponentScans.class)
public @interface ComponentScan {

	/**
	 * Alias for {@link #basePackages}.
	 * <p>Allows for more concise annotation declarations if no other attributes
	 * are needed &mdash; for example, {@code @ComponentScan("org.my.pkg")}
	 * instead of {@code @ComponentScan(basePackages = "org.my.pkg")}.
	 */
	// {@link #basePackages} 的别名。
	// <p>如果不需要其他属性，则允许更简洁的注解声明 -
	// 例如，{@code @ComponentScan("org.my.pkg")} 而不是 {@code @ComponentScan(basePackages = "org.my.pkg")}。
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * <p>{@link #value} is an alias for (and mutually exclusive with) this
	 * attribute.
	 * <p>Use {@link #basePackageClasses} for a type-safe alternative to
	 * String-based package names.
	 */
	// 用于扫描带注解组件的基础包。
	// <p>{@link #value} 是此属性的别名（且与此属性互斥）。
	// <p>使用 {@link #basePackageClasses} 作为基于字符串的包名称的类型安全替代方案。
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages} for specifying the packages
	 * to scan for annotated components. The package of each class specified will be scanned.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.
	 */
	// 类型安全的 {@link #basePackages} 替代方案，用于指定要扫描带注解组件的包。每个指定的类的包都将被扫描。
	// <p>考虑在每个包中创建一个特殊的无操作标记类或接口，除了被此属性引用外，没有其他用途。
	Class<?>[] basePackageClasses() default {};

	/**
	 * The {@link BeanNameGenerator} class to be used for naming detected components
	 * within the Spring container.
	 * <p>The default value of the {@link BeanNameGenerator} interface itself indicates
	 * that the scanner used to process this {@code @ComponentScan} annotation should
	 * use its inherited bean name generator, e.g. the default
	 * {@link AnnotationBeanNameGenerator} or any custom instance supplied to the
	 * application context at bootstrap time.
	 * @see AnnotationConfigApplicationContext#setBeanNameGenerator(BeanNameGenerator)
	 * @see AnnotationBeanNameGenerator
	 * @see FullyQualifiedAnnotationBeanNameGenerator
	 */
	// {@link BeanNameGenerator} 类用于在 Spring 容器中命名检测到的组件。
	// <p>{@link BeanNameGenerator} 接口本身的默认值表示用于处理此 {@code @ComponentScan} 注解的扫描器应该使用其继承的 bean 名称生成器，
	// 例如默认的 {@link AnnotationBeanNameGenerator} 或在启动时提供给应用程序上下文的任何自定义实例。
	Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

	/**
	 * The {@link ScopeMetadataResolver} to be used for resolving the scope of detected components.
	 */
	// {@link ScopeMetadataResolver} 用于解析检测到的组件的作用域。
	Class<? extends ScopeMetadataResolver> scopeResolver() default AnnotationScopeMetadataResolver.class;

	/**
	 * Indicates whether proxies should be generated for detected components, which may be
	 * necessary when using scopes in a proxy-style fashion.
	 * <p>The default is defer to the default behavior of the component scanner used to
	 * execute the actual scan.
	 * <p>Note that setting this attribute overrides any value set for {@link #scopeResolver}.
	 * @see ClassPathBeanDefinitionScanner#setScopedProxyMode(ScopedProxyMode)
	 */
	// 指示是否应为检测到的组件生成代理，这在以代理方式使用作用域时可能是必要的。
	// <p>默认值遵循用于执行实际扫描的组件扫描器的默认行为。
	// <p>请注意，设置此属性会覆盖为 {@link #scopeResolver} 设置的任何值。
	ScopedProxyMode scopedProxy() default ScopedProxyMode.DEFAULT;

	/**
	 * Controls the class files eligible for component detection.
	 * <p>Consider use of {@link #includeFilters} and {@link #excludeFilters}
	 * for a more flexible approach.
	 */
	// 控制符合组件检测条件的类文件。
	// <p>考虑使用 {@link #includeFilters} 和 {@link #excludeFilters} 来实现更灵活的方法。
	String resourcePattern() default ClassPathScanningCandidateComponentProvider.DEFAULT_RESOURCE_PATTERN;

	/**
	 * Indicates whether automatic detection of classes annotated with {@code @Component}
	 * {@code @Repository}, {@code @Service}, or {@code @Controller} should be enabled.
	 */
	// 指示是否启用对带有 {@code @Component}、{@code @Repository}、{@code @Service} 或 {@code @Controller} 注解的类的自动检测。
	boolean useDefaultFilters() default true;

	/**
	 * Specifies which types are eligible for component scanning.
	 * <p>Further narrows the set of candidate components from everything in {@link #basePackages}
	 * to everything in the base packages that matches the given filter or filters.
	 * <p>Note that these filters will be applied in addition to the default filters, if specified.
	 * Any type under the specified base packages which matches a given filter will be included,
	 * even if it does not match the default filters (i.e. is not annotated with {@code @Component}).
	 * @see #resourcePattern()
	 * @see #useDefaultFilters()
	 */
	// 指定哪些类型符合组件扫描的条件。
	// <p>进一步将候选组件范围从 {@link #basePackages} 中的所有内容缩小到基础包中与给定过滤器匹配的所有内容。
	// <p>请注意，如果指定了默认过滤器，则这些过滤器将作为默认过滤器的补充应用。
	// 任何在指定基础包下，符合给定过滤器的类型都将被包含，即使它不符合默认过滤器（即未使用 {@code @Component} 注解）。
	Filter[] includeFilters() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 * @see #resourcePattern
	 */
	// 指定哪些类型不符合组件扫描的条件。
	Filter[] excludeFilters() default {};

	/**
	 * Specify whether scanned beans should be registered for lazy initialization.
	 * <p>Default is {@code false}; switch this to {@code true} when desired.
	 * @since 4.1
	 */
	// 指定是否应将扫描的 bean 注册为延迟初始化。
	// <p>默认值为 {@code false}；如有需要，请将其切换为 {@code true}。
	boolean lazyInit() default false;


	/**
	 * Declares the type filter to be used as an {@linkplain ComponentScan#includeFilters
	 * include filter} or {@linkplain ComponentScan#excludeFilters exclude filter}.
	 */
	// 声明要用作 {@linkplain ComponentScan#includeFilters 包含过滤器}
	// 或 {@linkplain ComponentScan#excludeFilters 排除过滤器} 的类型过滤器。
	@Retention(RetentionPolicy.RUNTIME)
	@Target({})
	@interface Filter {

		/**
		 * The type of filter to use.
		 * <p>Default is {@link FilterType#ANNOTATION}.
		 * @see #classes
		 * @see #pattern
		 */
		// 要使用的过滤器类型。
		// <p>默认为 {@link FilterType#ANNOTATION}。
		FilterType type() default FilterType.ANNOTATION;

		/**
		 * Alias for {@link #classes}.
		 * @see #classes
		 */
		// {@link #classes} 的别名。
		@AliasFor("classes")
		Class<?>[] value() default {};

		/**
		 * The class or classes to use as the filter.
		 * <p>The following table explains how the classes will be interpreted
		 * based on the configured value of the {@link #type} attribute.
		 * <table border="1">
		 * <tr><th>{@code FilterType}</th><th>Class Interpreted As</th></tr>
		 * <tr><td>{@link FilterType#ANNOTATION ANNOTATION}</td>
		 * <td>the annotation itself</td></tr>
		 * <tr><td>{@link FilterType#ASSIGNABLE_TYPE ASSIGNABLE_TYPE}</td>
		 * <td>the type that detected components should be assignable to</td></tr>
		 * <tr><td>{@link FilterType#CUSTOM CUSTOM}</td>
		 * <td>an implementation of {@link TypeFilter}</td></tr>
		 * </table>
		 * <p>When multiple classes are specified, <em>OR</em> logic is applied
		 * &mdash; for example, "include types annotated with {@code @Foo} OR {@code @Bar}".
		 * <p>Custom {@link TypeFilter TypeFilters} may optionally implement any of the
		 * following {@link org.springframework.beans.factory.Aware Aware} interfaces, and
		 * their respective methods will be called prior to {@link TypeFilter#match match}:
		 * <ul>
		 * <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware}</li>
		 * <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}
		 * <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}
		 * <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}
		 * </ul>
		 * <p>Specifying zero classes is permitted but will have no effect on component
		 * scanning.
		 * @since 4.2
		 * @see #value
		 * @see #type
		 */
		// 用作过滤器的一个或多个类。
		// <p>下表说明了如何根据 {@link #type} 属性的配置值解释类。
		// <table border="1">
		// 	<tr>
		// 		<th>{@code FilterType} </th>
		// 		<th>类解释为</th>
		// 	</tr>
		// 	<tr>
		// 		<td>{@link FilterType#ANNOTATION ANNOTATION} </td>
		// 		<td>注释本身</td>
		// 	</tr>
		// <tr>
		// 		<td>{@link FilterType#ASSIGNABLE_TYPE ASSIGNABLE_TYPE} </td>
		// 		<td>检测到的组件应可分配到的类型</td>
		// </tr>
		// <tr>
		// 		<td>{@link FilterType#CUSTOM CUSTOM} </td>
		// 		<td>{@link TypeFilter 的一个实现} </td>
		// </tr>
		// </table>
		// <p>指定多个类时，将应用 <em>OR</em> 逻辑 -例如，“包含带有 {@code @Foo} 或 {@code @Bar} 注释的类型”。
		// <p>自定义 {@link TypeFilter TypeFilters} 可以选择实现以下任何 {@link org.springframework.beans.factory.Aware Aware} 接口，
		// 并且它们各自的方法将在 {@link TypeFilter#match match} 之前调用：
		// <ul>
		// <li>{@link org.springframework.context.EnvironmentAware EnvironmentAware</li>
		// <li>{@link org.springframework.beans.factory.BeanFactoryAware BeanFactoryAware}</li>
		// <li>{@link org.springframework.beans.factory.BeanClassLoaderAware BeanClassLoaderAware}</li>
		// <li>{@link org.springframework.context.ResourceLoaderAware ResourceLoaderAware}</li>
		// </ul>
		// <p>可以指定零个类，但这不会影响组件扫描。
		@AliasFor("value")
		Class<?>[] classes() default {};

		/**
		 * The pattern (or patterns) to use for the filter, as an alternative
		 * to specifying a Class {@link #value}.
		 * <p>If {@link #type} is set to {@link FilterType#ASPECTJ ASPECTJ},
		 * this is an AspectJ type pattern expression. If {@link #type} is
		 * set to {@link FilterType#REGEX REGEX}, this is a regex pattern
		 * for the fully-qualified class names to match.
		 * @see #type
		 * @see #classes
		 */
		// 用于过滤器的模式（或多个模式），作为指定类 {@link #value} 的替代方法。
		// <p>如果 {@link #type} 设置为 {@link FilterType#ASPECTJ ASPECTJ}，则这是一个 AspectJ 类型模式表达式。
		// 如果 {@link #type} 设置为 {@link FilterType#REGEX REGEX}，则这是一个用于匹配完全限定类名的正则表达式模式。
		String[] pattern() default {};

	}

}
