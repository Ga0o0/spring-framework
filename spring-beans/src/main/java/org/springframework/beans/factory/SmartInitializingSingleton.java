/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory;

/**
 * Callback interface triggered at the end of the singleton pre-instantiation phase
 * during {@link BeanFactory} bootstrap. This interface can be implemented by
 * singleton beans in order to perform some initialization after the regular
 * singleton instantiation algorithm, avoiding side effects with accidental early
 * initialization (e.g. from {@link ListableBeanFactory#getBeansOfType} calls).
 * In that sense, it is an alternative to {@link InitializingBean} which gets
 * triggered right at the end of a bean's local construction phase.
 *
 * <p>This callback variant is somewhat similar to
 * {@link org.springframework.context.event.ContextRefreshedEvent} but doesn't
 * require an implementation of {@link org.springframework.context.ApplicationListener},
 * with no need to filter context references across a context hierarchy etc.
 * It also implies a more minimal dependency on just the {@code beans} package
 * and is being honored by standalone {@link ListableBeanFactory} implementations,
 * not just in an {@link org.springframework.context.ApplicationContext} environment.
 *
 * <p><b>NOTE:</b> If you intend to start/manage asynchronous tasks, preferably
 * implement {@link org.springframework.context.Lifecycle} instead which offers
 * a richer model for runtime management and allows for phased startup/shutdown.
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons()
 */
// 在 {@link BeanFactory} 引导过程中，单例预实例化阶段结束时触发的回调接口。
// 此接口可由单例Bean实现，以便在常规单例实例化算法之后执行一些初始化操作，从而避免因意外提前初始化
// （例如，来自{@link ListableBeanFactory#getBeansOfType}调用的初始化）而产生的副作用。
// 从这个意义上说，它是{@link InitializingBean}的替代方案，后者会在Bean的本地构造阶段结束时立即触发。
//
// <p>这种回调变体与{@link org.springframework.context.event.ContextRefreshedEvent}有些相似，
// 但不需要实现{@link org.springframework.context.ApplicationListener}，也无需在上下文层次结构中过滤上下文引用等。
// 它还意味着对{@code beans}包的依赖更小，并且得到了独立{@link ListableBeanFactory}实现的认可，
// 而不仅仅是在{@link org.springframework.context.ApplicationContext}环境中。
//
// <p><b>注意：</b>如果您打算启动/管理异步任务，最好实现{@link org.springframework.context.Lifecycle}，
// 它提供了更丰富的运行时管理模型，并允许分阶段启动/关闭。
public interface SmartInitializingSingleton {

	/**
	 * Invoked right at the end of the singleton pre-instantiation phase,
	 * with a guarantee that all regular singleton beans have been created
	 * already. {@link ListableBeanFactory#getBeansOfType} calls within
	 * this method won't trigger accidental side effects during bootstrap.
	 * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
	 * lazily initialized on demand after {@link BeanFactory} bootstrap,
	 * and not for any other bean scope either. Carefully use it for beans
	 * with the intended bootstrap semantics only.
	 */
	// 此回调在单例预实例化阶段结束时被调用，确保所有常规单例 Bean 都已经创建完成。
	// 在此方法中调用{@link ListableBeanFactory#getBeansOfType}不会在引导过程中引发意外的副作用。
	//
	// <p><b>注意：</b>对于在{@link BeanFactory}引导后按需延迟初始化的单例Bean，
	// 以及任何其他Bean作用域的Bean，此回调都不会被触发。请谨慎使用，仅用于具有预期引导语义的Bean。
	void afterSingletonsInstantiated();

}
