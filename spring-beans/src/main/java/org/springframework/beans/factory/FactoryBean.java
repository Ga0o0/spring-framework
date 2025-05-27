/*
 * Copyright 2002-2020 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects used within a {@link BeanFactory} which
 * are themselves factories for individual objects. If a bean implements this
 * interface, it is used as a factory for an object to expose, not directly as a
 * bean instance that will be exposed itself.
 *
 * <p><b>NB: A bean that implements this interface cannot be used as a normal bean.</b>
 * A FactoryBean is defined in a bean style, but the object exposed for bean
 * references ({@link #getObject()}) is always the object that it creates.
 *
 * <p>FactoryBeans can support singletons and prototypes, and can either create
 * objects lazily on demand or eagerly on startup. The {@link SmartFactoryBean}
 * interface allows for exposing more fine-grained behavioral metadata.
 *
 * <p>This interface is heavily used within the framework itself, for example for
 * the AOP {@link org.springframework.aop.framework.ProxyFactoryBean} or the
 * {@link org.springframework.jndi.JndiObjectFactoryBean}. It can be used for
 * custom components as well; however, this is only common for infrastructure code.
 *
 * <p><b>{@code FactoryBean} is a programmatic contract. Implementations are not
 * supposed to rely on annotation-driven injection or other reflective facilities.</b>
 * {@link #getObjectType()} {@link #getObject()} invocations may arrive early in the
 * bootstrap process, even ahead of any post-processor setup. If you need access to
 * other beans, implement {@link BeanFactoryAware} and obtain them programmatically.
 *
 * <p><b>The container is only responsible for managing the lifecycle of the FactoryBean
 * instance, not the lifecycle of the objects created by the FactoryBean.</b> Therefore,
 * a destroy method on an exposed bean object (such as {@link java.io.Closeable#close()})
 * will <i>not</i> be called automatically. Instead, a FactoryBean should implement
 * {@link DisposableBean} and delegate any such close call to the underlying object.
 *
 * <p>Finally, FactoryBean objects participate in the containing BeanFactory's
 * synchronization of bean creation. There is usually no need for internal
 * synchronization other than for purposes of lazy initialization within the
 * FactoryBean itself (or the like).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @param <T> the bean type
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
// {@link BeanFactory} 中使用的对象需要实现此接口，这些对象本身就是各个对象的工厂。
// 如果某个 Bean 实现了此接口，它将被用作要公开对象的工厂，而不是直接作为将自身公开的 Bean 实例。
//
// <p><b>注意：实现此接口的 Bean 不能用作普通 Bean。</b>
// FactoryBean 以 Bean 风格定义，但为 Bean 引用 ({@link #getObject()}) 公开的对象始终是它创建的对象。
//
// <p>FactoryBean 可以支持单例和原型，并且可以按需延迟创建对象或在启动时主动创建对象。
// {@link SmartFactoryBean} 接口允许公开更细粒度的行为元数据。
//
// <p>此接口在框架内部被广泛使用，
// 例如 AOP {@link org.springframework.aop.framework.ProxyFactoryBean}
// 或 {@link org.springframework.jndi.JndiObjectFactoryBean}。
// 它也可以用于自定义组件；但是，这仅适用于基础架构代码。
//
// <p><b>{@code FactoryBean} 是一个编程式契约。其实现不应依赖于注解驱动的注入或其他反射机制。</b>
// {@link #getObjectType()} {@link #getObject()} 调用可能在引导过程的早期就到达，甚至在任何后处理器设置之前就到达。
// 如果您需要访问其他 Bean，请实现 {@link BeanFactoryAware} 并以编程方式获取它们。
//
// <p><b>容器仅负责管理 FactoryBean 实例的生命周期，而不负责 FactoryBean 创建的对象的生命周期。</b>
// 因此，暴露的 Bean 对象上的 destroy 方法（例如 {@link java.io.Closeable#close()}）将<i>不会</i>自动调用。
// 相反，FactoryBean 应该实现 {@link DisposableBean} 接口，并将任何此类关闭调用委托给底层对象。
//
// <p>最后，FactoryBean 对象会参与包含 BeanFactory 的 Bean 创建同步。
// 通常情况下，除了 FactoryBean 自身（或类似情况）的延迟初始化之外，不需要进行内部同步。
public interface FactoryBean<T> {

	/**
	 * The name of an attribute that can be
	 * {@link org.springframework.core.AttributeAccessor#setAttribute set} on a
	 * {@link org.springframework.beans.factory.config.BeanDefinition} so that
	 * factory beans can signal their object type when it can't be deduced from
	 * the factory bean class.
	 * @since 5.2
	 */
	// 可以在 {@link org.springframework.beans.factory.config.BeanDefinition} 上
	// 设置 {@link org.springframework.core.AttributeAccessor#setAttribute set} 的属性名称，
	// 以便工厂 bean 在无法从工厂 bean 类中推断出其对象类型时可以发出信号。
	String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";


	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * <p>As with a {@link BeanFactory}, this allows support for both the
	 * Singleton and Prototype design pattern.
	 * <p>If this FactoryBean is not fully initialized yet at the time of
	 * the call (for example because it is involved in a circular reference),
	 * throw a corresponding {@link FactoryBeanNotInitializedException}.
	 * <p>As of Spring 2.0, FactoryBeans are allowed to return {@code null}
	 * objects. The factory will consider this as normal value to be used; it
	 * will not throw a FactoryBeanNotInitializedException in this case anymore.
	 * FactoryBean implementations are encouraged to throw
	 * FactoryBeanNotInitializedException themselves now, as appropriate.
	 * @return an instance of the bean (can be {@code null})
	 * @throws Exception in case of creation errors
	 * @see FactoryBeanNotInitializedException
	 */
	// 返回此工厂管理的对象的一个实例（可能是共享的或独立的）。
	// <p>与 {@link BeanFactory} 类似，这允许同时支持单例 (Singleton) 和原型 (Prototype) 设计模式。
	// <p>如果此 FactoryBean 在调用时尚未完全初始化（例如，因为它涉及循环引用），
	// 则抛出相应的 {@link FactoryBeanNotInitializedException}。
	// <p>从 Spring 2.0 开始，FactoryBean 允许返回 {@code null} 对象。
	// 工厂会将此视为可用的正常值；在这种情况下，它不会再抛出 FactoryBeanNotInitializedException。
	// 现在，鼓励 FactoryBean 实现自行抛出 FactoryBeanNotInitializedException（视情况而定）。
	// @return 一个 bean 实例（可以为 {@code null}）
	// @throws Exception（如果发生创建错误）
	@Nullable
	T getObject() throws Exception;

	/**
	 * Return the type of object that this FactoryBean creates,
	 * or {@code null} if not known in advance.
	 * <p>This allows one to check for specific types of beans without
	 * instantiating objects, for example on autowiring.
	 * <p>In the case of implementations that are creating a singleton object,
	 * this method should try to avoid singleton creation as far as possible;
	 * it should rather estimate the type in advance.
	 * For prototypes, returning a meaningful type here is advisable too.
	 * <p>This method can be called <i>before</i> this FactoryBean has
	 * been fully initialized. It must not rely on state created during
	 * initialization; of course, it can still use such state if available.
	 * <p><b>NOTE:</b> Autowiring will simply ignore FactoryBeans that return
	 * {@code null} here. Therefore, it is highly recommended to implement
	 * this method properly, using the current state of the FactoryBean.
	 * @return the type of object that this FactoryBean creates,
	 * or {@code null} if not known at the time of the call
	 * @see ListableBeanFactory#getBeansOfType
	 */
	// 返回此 FactoryBean 创建的对象类型，如果事先未知，则返回 {@code null}。
	// <p>这允许在不实例化对象的情况下检查特定类型的 bean，例如在自动装配时。
	// <p>对于创建单例对象的实现，此方法应尽可能避免创建单例；它应该提前估计类型。对于原型，建议在此处返回一个有意义的类型。
	// <p>此方法可以在此 FactoryBean 完全初始化之前调用。它不能依赖于初始化期间创建的状态；当然，如果可用，它仍然可以使用此类状态。
	// <p><b>注意：</b>自动装配将忽略在此处返回 {@code null} 的 FactoryBean。
	// 因此，强烈建议使用 FactoryBean 的当前状态正确实现此方法。@return 此 FactoryBean 创建的对象类型，如果在调用时未知，则返回 {@code null}
	@Nullable
	Class<?> getObjectType();

	/**
	 * Is the object managed by this factory a singleton? That is,
	 * will {@link #getObject()} always return the same object
	 * (a reference that can be cached)?
	 * <p><b>NOTE:</b> If a FactoryBean indicates to hold a singleton object,
	 * the object returned from {@code getObject()} might get cached
	 * by the owning BeanFactory. Hence, do not return {@code true}
	 * unless the FactoryBean always exposes the same reference.
	 * <p>The singleton status of the FactoryBean itself will generally
	 * be provided by the owning BeanFactory; usually, it has to be
	 * defined as singleton there.
	 * <p><b>NOTE:</b> This method returning {@code false} does not
	 * necessarily indicate that returned objects are independent instances.
	 * An implementation of the extended {@link SmartFactoryBean} interface
	 * may explicitly indicate independent instances through its
	 * {@link SmartFactoryBean#isPrototype()} method. Plain {@link FactoryBean}
	 * implementations which do not implement this extended interface are
	 * simply assumed to always return independent instances if the
	 * {@code isSingleton()} implementation returns {@code false}.
	 * <p>The default implementation returns {@code true}, since a
	 * {@code FactoryBean} typically manages a singleton instance.
	 * @return whether the exposed object is a singleton
	 * @see #getObject()
	 * @see SmartFactoryBean#isPrototype()
	 */
	// 此工厂管理的对象是单例吗？也就是说，{@link #getObject()} 是否始终返回同一个对象（可缓存的引用）？
	// <p><b>注意：</b>如果 FactoryBean 指示持有单例对象，则 {@code getObject()} 返回的对象可能会被其所属的 BeanFactory 缓存。
	// 因此，除非 FactoryBean 始终公开相同的引用，否则不要返回 {@code true}。
	// <p>FactoryBean 本身的单例状态通常由其所属的 BeanFactory 提供；通常，必须在那里将其定义为单例。
	// <p><b>注意：</b>此方法返回 {@code false} 并不一定表示返回的对象是独立实例。
	// 扩展的 {@link SmartFactoryBean} 接口的实现可以通过其 {@link SmartFactoryBean#isPrototype()} 方法明确指示独立实例。
	// 如果 {@code isSingleton()} 实现返回 {@code false}，则未实现此扩展接口的普通 {@link FactoryBean} 实现将始终返回独立实例。
	// <p>默认实现返回 {@code true}，因为 {@code FactoryBean} 通常管理单例实例。@return 暴露的对象是否为单例
	default boolean isSingleton() {
		return true;
	}

}
