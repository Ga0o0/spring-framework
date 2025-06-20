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

import org.springframework.transaction.TransactionManager;

/**
 * Interface to be implemented by @{@link org.springframework.context.annotation.Configuration
 * Configuration} classes annotated with @{@link EnableTransactionManagement} that wish to
 * (or need to) explicitly specify the default {@code PlatformTransactionManager} bean
 * (or {@code ReactiveTransactionManager} bean) to be used for annotation-driven
 * transaction management, as opposed to the default approach of a by-type lookup.
 * One reason this might be necessary is if there are two {@code PlatformTransactionManager}
 * beans (or two {@code ReactiveTransactionManager} beans) present in the container.
 *
 * <p>See @{@link EnableTransactionManagement} for general examples and context;
 * see {@link #annotationDrivenTransactionManager()} for detailed instructions.
 *
 * <p><b>NOTE: A {@code TransactionManagementConfigurer} will get initialized early.</b>
 * Do not inject common dependencies into autowired fields directly; instead, consider
 * declaring a lazy {@link org.springframework.beans.factory.ObjectProvider} for those.
 *
 * <p>Note that in by-type lookup disambiguation cases, an alternative approach to
 * implementing this interface is to simply mark one of the offending
 * {@code PlatformTransactionManager} {@code @Bean} methods (or
 * {@code ReactiveTransactionManager} {@code @Bean} methods) as
 * {@link org.springframework.context.annotation.Primary @Primary}.
 * This is even generally preferred since it doesn't lead to early initialization
 * of the {@code TransactionManager} bean.
 *
 * @author Chris Beams
 * @since 3.1
 * @see EnableTransactionManagement
 * @see org.springframework.context.annotation.Primary
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.ReactiveTransactionManager
 */
// 由使用 @{@link EnableTransactionManagement} 注释的 @{@link org.springframework.context.annotation.Configuration Configuration}
// 类实现的接口，这些类希望（或需要）明确指定默认的 {@code PlatformTransactionManager} bean（或 {@code ReactiveTransactionManager} bean）
// 用于注释驱动的事务管理，而不是按类型查找的默认方法。 这可能是必要的一个原因是如果容器中存在两个 {@code PlatformTransactionManager} bean
// （或两个 {@code ReactiveTransactionManager} bean）。
//
// <p>有关一般示例和上下文，请参阅 @{@link EnableTransactionManagement}；有关详细说明，请参阅 {@link #annotationDrivenTransactionManager()}。
//
// <p><b>注意：{@code TransactionManagementConfigurer} 将尽早初始化。</b>不要将公共依赖项直接注入自动装配字段；
// 而是考虑为这些字段声明一个惰性 {@link org.springframework.beans.factory.ObjectProvider}。
//
// <p>请注意，在按类型查找歧义的情况下，实现此接口的另一种方法是简单地将其中一个有问题的 {@code PlatformTransactionManager} {@code @Bean} 方法
// （或 {@code ReactiveTransactionManager} {@code @Bean} 方法）标记为 {@link org.springframework.context.annotation.Primary @Primary}。
// 这通常是首选，因为它不会导致 {@code TransactionManager} bean 的过早初始化。
public interface TransactionManagementConfigurer {

	/**
	 * Return the default transaction manager bean to use for annotation-driven database
	 * transaction management, i.e. when processing {@code @Transactional} methods.
	 * <p>There are two basic approaches to implementing this method:
	 * <h4>1. Implement the method and annotate it with {@code @Bean}</h4>
	 * In this case, the implementing {@code @Configuration} class implements this method,
	 * marks it with {@code @Bean}, and configures and returns the transaction manager
	 * directly within the method body:
	 * <pre class="code">
	 * &#064;Bean
	 * &#064;Override
	 * public PlatformTransactionManager annotationDrivenTransactionManager() {
	 *     return new DataSourceTransactionManager(dataSource());
	 * }</pre>
	 * <h4>2. Implement the method without {@code @Bean} and delegate to another existing
	 * {@code @Bean} method</h4>
	 * <pre class="code">
	 * &#064;Bean
	 * public PlatformTransactionManager txManager() {
	 *     return new DataSourceTransactionManager(dataSource());
	 * }
	 *
	 * &#064;Override
	 * public PlatformTransactionManager annotationDrivenTransactionManager() {
	 *     return txManager(); // reference the existing {@code @Bean} method above
	 * }</pre>
	 * If taking approach #2, be sure that <em>only one</em> of the methods is marked
	 * with {@code @Bean}!
	 * <p>In either scenario #1 or #2, it is important that the
	 * {@code PlatformTransactionManager} instance is managed as a Spring bean within the
	 * container since most {@code PlatformTransactionManager} implementations take advantage
	 * of Spring lifecycle callbacks such as {@code InitializingBean} and
	 * {@code BeanFactoryAware}. Note that the same guidelines apply to
	 * {@code ReactiveTransactionManager} beans.
	 * @return a {@link org.springframework.transaction.PlatformTransactionManager} or
	 * {@link org.springframework.transaction.ReactiveTransactionManager} implementation
	 */
	// 返回用于注解驱动的数据库事务管理的默认事务管理器 bean，即在处理 {@code @Transactional} 方法时。
	// <p>实现此方法有两种基本方法：
	//
	// <h4>1. 实现该方法并使用 {@code @Bean} 对其进行注解</h4>
	// 在这种情况下，实现 {@code @Configuration} 类实现此方法，使用 {@code @Bean} 对其进行标记，并在方法体中直接配置和返回事务管理器：
	// <pre class="code">
	// @Bean
	// @Override
	// public PlatformTransactionManager commentDrivenTransactionManager() {
	// 		return new DataSourceTransactionManager(dataSource());
	// }</pre>
	//
	// <h4>2. 实现不使用 {@code @Bean} 的方法并委托给另一个现有的 {@code @Bean} 方法</h4>
	// <pre class="code">
	// @Bean
	// public PlatformTransactionManager txManager() {
	// 		return new DataSourceTransactionManager(dataSource());
	// }
	//
	// @Override
	// public PlatformTransactionManager annotationDrivenTransactionManager() {
	// 		return txManager(); // 引用上面现有的 {@code @Bean} 方法 }
	// }</pre>
	// 如果采用方法 2，请确保<em>只有一个</em>方法标有 {@code @Bean}！
	//
	// <p>无论是方案 1 还是方案 2，重要的是将 {@code PlatformTransactionManager} 实例作为容器中的 Spring bean 进行管理，
	// 因为大多数 {@code PlatformTransactionManager} 实现都利用了 Spring 生命周期回调，
	// 例如 {@code InitializingBean} 和 {@code BeanFactoryAware}。
	// 请注意，相同的准则也适用于 {@code ReactiveTransactionManager} bean。
	// @return 一个 {@link org.springframework.transaction.PlatformTransactionManager} 或
	// {@link org.springframework.transaction.ReactiveTransactionManager} 实现
	TransactionManager annotationDrivenTransactionManager();

}
