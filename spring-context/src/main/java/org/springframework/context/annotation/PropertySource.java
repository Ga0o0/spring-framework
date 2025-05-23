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

import org.springframework.core.io.support.PropertySourceFactory;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link org.springframework.core.env.PropertySource PropertySource} to Spring's
 * {@link org.springframework.core.env.Environment Environment}. To be used in
 * conjunction with @{@link Configuration} classes.
 *
 * <h3>Example usage</h3>
 *
 * <p>Given a file {@code app.properties} containing the key/value pair
 * {@code testbean.name=myTestBean}, the following {@code @Configuration} class
 * uses {@code @PropertySource} to contribute {@code app.properties} to the
 * {@code Environment}'s set of {@code PropertySources}.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/app.properties")
 * public class AppConfig {
 *
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * <p>Notice that the {@code Environment} object is
 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired} into the
 * configuration class and then used when populating the {@code TestBean} object. Given
 * the configuration above, a call to {@code testBean.getName()} will return "myTestBean".
 *
 * <h3>Resolving <code>${...}</code> placeholders in {@code <bean>} and {@code @Value} annotations</h3>
 *
 * <p>In order to resolve ${...} placeholders in {@code <bean>} definitions or {@code @Value}
 * annotations using properties from a {@code PropertySource}, you must ensure that an
 * appropriate <em>embedded value resolver</em> is registered in the {@code BeanFactory}
 * used by the {@code ApplicationContext}. This happens automatically when using
 * {@code <context:property-placeholder>} in XML. When using {@code @Configuration} classes
 * this can be achieved by explicitly registering a {@code PropertySourcesPlaceholderConfigurer}
 * via a {@code static} {@code @Bean} method. Note, however, that explicit registration
 * of a {@code PropertySourcesPlaceholderConfigurer} via a {@code static} {@code @Bean}
 * method is typically only required if you need to customize configuration such as the
 * placeholder syntax, etc. See the "Working with externalized values" section of
 * {@link Configuration @Configuration}'s javadocs and "a note on
 * BeanFactoryPostProcessor-returning {@code @Bean} methods" of {@link Bean @Bean}'s
 * javadocs for details and examples.
 *
 * <h3>Resolving ${...} placeholders within {@code @PropertySource} resource locations</h3>
 *
 * <p>Any ${...} placeholders present in a {@code @PropertySource} {@linkplain #value()
 * resource location} will be resolved against the set of property sources already
 * registered against the environment. For example:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
 * public class AppConfig {
 *
 *     &#064;Autowired
 *     Environment env;
 *
 *     &#064;Bean
 *     public TestBean testBean() {
 *         TestBean testBean = new TestBean();
 *         testBean.setName(env.getProperty("testbean.name"));
 *         return testBean;
 *     }
 * }</pre>
 *
 * <p>Assuming that "my.placeholder" is present in one of the property sources already
 * registered &mdash; for example, system properties or environment variables &mdash;
 * the placeholder will be resolved to the corresponding value. If not, then "default/path"
 * will be used as a default. Expressing a default value (delimited by colon ":") is
 * optional. If no default is specified and a property cannot be resolved, an {@code
 * IllegalArgumentException} will be thrown.
 *
 * <h3>A note on property overriding with {@code @PropertySource}</h3>
 *
 * <p>In cases where a given property key exists in more than one property resource
 * file, the last {@code @PropertySource} annotation processed will 'win' and override
 * any previous key with the same name.
 *
 * <p>For example, given two properties files {@code a.properties} and
 * {@code b.properties}, consider the following two configuration classes
 * that reference them with {@code @PropertySource} annotations:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/a.properties")
 * public class ConfigA { }
 *
 * &#064;Configuration
 * &#064;PropertySource("classpath:/com/myco/b.properties")
 * public class ConfigB { }
 * </pre>
 *
 * <p>The override ordering depends on the order in which these classes are registered
 * with the application context.
 *
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(ConfigA.class);
 * ctx.register(ConfigB.class);
 * ctx.refresh();
 * </pre>
 *
 * <p>In the scenario above, the properties in {@code b.properties} will override any
 * duplicates that exist in {@code a.properties}, because {@code ConfigB} was registered
 * last.
 *
 * <p>In certain situations, it may not be possible or practical to tightly control
 * property source ordering when using {@code @PropertySource} annotations. For example,
 * if the {@code @Configuration} classes above were registered via component-scanning,
 * the ordering is difficult to predict. In such cases &mdash; and if overriding is important
 * &mdash; it is recommended that the user fall back to using the programmatic
 * {@code PropertySource} API. See {@link org.springframework.core.env.ConfigurableEnvironment
 * ConfigurableEnvironment} and {@link org.springframework.core.env.MutablePropertySources
 * MutablePropertySources} javadocs for details.
 *
 * <p>{@code @PropertySource} can be used as a <em>{@linkplain Repeatable repeatable}</em>
 * annotation. {@code @PropertySource} may also be used as a <em>meta-annotation</em>
 * to create custom <em>composed annotations</em> with attribute overrides.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.1
 * @see PropertySources
 * @see Configuration
 * @see org.springframework.core.env.PropertySource
 * @see org.springframework.core.env.ConfigurableEnvironment#getPropertySources()
 * @see org.springframework.core.env.MutablePropertySources
 */
// 此注解提供了一种便捷的声明式机制，用于将 {@link org.springframework.core.env.PropertySource PropertySource} 添加
// 到 Spring 的 {@link org.springframework.core.env.Environment Environment} 中。
// 需与 @{@link Configuration} 类结合使用。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySources.class)
public @interface PropertySource {

	/**
	 * Indicate the unique name of this property source.
	 * <p>If omitted, the {@link #factory} will generate a name based on the
	 * underlying resource (in the case of
	 * {@link org.springframework.core.io.support.DefaultPropertySourceFactory
	 * DefaultPropertySourceFactory}: derived from the resource description through
	 * a corresponding name-less
	 * {@link org.springframework.core.io.support.ResourcePropertySource
	 * ResourcePropertySource} constructor).
	 * <p>The name of a {@code PropertySource} serves two general purposes.
	 * <ul>
	 * <li>Diagnostics: to determine the source of the properties in logging and
	 * debugging &mdash; for example, in a Spring Boot application via Spring
	 * Boot's {@code PropertySourceOrigin}.</li>
	 * <li>Programmatic interaction with
	 * {@link org.springframework.core.env.MutablePropertySources MutablePropertySources}:
	 * the name can be used to retrieve properties from a particular property
	 * source (or to determine if a particular named property source already exists).
	 * The name can also be used to add a new property source relative to an existing
	 * property source (see
	 * {@link org.springframework.core.env.MutablePropertySources#addBefore addBefore()} and
	 * {@link org.springframework.core.env.MutablePropertySources#addAfter addAfter()}).</li>
	 * </ul>
	 * @see org.springframework.core.env.PropertySource#getName()
	 * @see org.springframework.core.io.Resource#getDescription()
	 */
	// 指示此属性源的唯一名称。
	// <p>如果省略，{@link #factory} 将根据底层资源生成一个名称（对于 {@link org.springframework.core.io.support.DefaultPropertySourceFactory DefaultPropertySourceFactory}：
	// 通过相应的无名 {@link org.springframework.core.io.support.ResourcePropertySource ResourcePropertySource} 构造函数从资源描述中派生）。
	// <p>{@code PropertySource} 的名称有两个一般用途。
	// <ul>
	// <li>诊断：在日志记录和调试中确定属性的来源 -例如，在 Spring Boot 应用程序中，通过 Spring Boot 的 {@code PropertySourceOrigin} 进行设置。</li>
	// <li>与 {@link org.springframework.core.env.MutablePropertySources MutablePropertySources} 进行编程交互：
	// 该名称可用于从特定属性源检索属性（或确定特定命名的属性源是否已存在）。
	// 该名称还可用于相对于现有属性源添加新的属性源（参见
	// {@link org.springframework.core.env.MutablePropertySources#addBefore addBefore()} 和
	// {@link org.springframework.core.env.MutablePropertySources#addAfter addAfter()}）。</li>
	// </ul>
	String name() default "";

	/**
	 * Indicate the resource locations of the properties files to be loaded.
	 * <p>The default {@link #factory() factory} supports both traditional and
	 * XML-based properties file formats &mdash; for example,
	 * {@code "classpath:/com/myco/app.properties"} or {@code "file:/path/to/file.xml"}.
	 * <p>As of Spring Framework 6.1, resource location wildcards are also
	 * supported &mdash; for example, {@code "classpath*:/config/*.properties"}.
	 * <p>{@code ${...}} placeholders will be resolved against property sources already
	 * registered with the {@code Environment}. See {@linkplain PropertySource above}
	 * for examples.
	 * <p>Each location will be added to the enclosing {@code Environment} as its own
	 * property source, and in the order declared (or in the order in which resource
	 * locations are resolved when location wildcards are used).
	 */
	// 指示要加载的属性文件的资源位置。
	// <p>默认的 {@link #factory() factory} 支持传统和基于 XML 的属性文件格式，
	// 例如 {@code "classpath:/com/myco/app.properties"} 或 {@code "file:/path/to/file.xml"}。
	// <p>从 Spring Framework 6.1 开始，还支持资源位置通配符，
	// 例如 {@code "classpath*:/config/*.properties"}。<p>{@code ${...}} 占位符将根据已在 {@code Environment} 中注册的属性源进行解析。
	// 有关示例，请参阅上面的 {@linkplain PropertySource}。
	// <p>每个位置都将作为其自己的属性源添加到封闭的 {@code Environment} 中，并按照声明的顺序（或按照使用位置通配符时资源位置解析的顺序）添加。
	String[] value();

	/**
	 * Indicate if a failure to find a {@link #value property resource} should be
	 * ignored.
	 * <p>{@code true} is appropriate if the properties file is completely optional.
	 * <p>Default is {@code false}.
	 * @since 4.0
	 */
	// 指示是否应忽略找不到 {@link #value 属性资源} 的情况。
	// <p>如果属性文件完全可选，则为 {@code true}。
	// <p>默认值为 {@code false}。
	boolean ignoreResourceNotFound() default false;

	/**
	 * A specific character encoding for the given resources, e.g. "UTF-8".
	 * @since 4.3
	 */
	// 指定资源的特定字符编码，例如“UTF-8”。
	String encoding() default "";

	/**
	 * Specify a custom {@link PropertySourceFactory}, if any.
	 * <p>By default, a default factory for standard resource files will be used
	 * which supports {@code *.properties} and {@code *.xml} file formats for
	 * {@link java.util.Properties}.
	 * @since 4.3
	 * @see org.springframework.core.io.support.DefaultPropertySourceFactory
	 * @see org.springframework.core.io.support.ResourcePropertySource
	 */
	// 指定自定义 {@link PropertySourceFactory}（如果有）。
	// <p>默认情况下，将使用标准资源文件的默认工厂，该工厂支持 {@link java.util.Properties} 的 {@code *.properties} 和 {@code *.xml} 文件格式。
	Class<? extends PropertySourceFactory> factory() default PropertySourceFactory.class;

}
