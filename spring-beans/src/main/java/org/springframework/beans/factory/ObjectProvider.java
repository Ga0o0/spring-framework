/*
 * Copyright 2002-2018 the original author or authors.
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

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * A variant of {@link ObjectFactory} designed specifically for injection points,
 * allowing for programmatic optionality and lenient not-unique handling.
 *
 * <p>As of 5.1, this interface extends {@link Iterable} and provides {@link Stream}
 * support. It can be therefore be used in {@code for} loops, provides {@link #forEach}
 * iteration and allows for collection-style {@link #stream} access.
 *
 * @author Juergen Hoeller
 * @since 4.3
 * @param <T> the object type
 * @see BeanFactory#getBeanProvider
 * @see org.springframework.beans.factory.annotation.Autowired
 */
// {@link ObjectFactory} 的一个变体，专为注入点设计，允许通过编程实现可选性，并允许宽松的非唯一性处理。
//
// <p>从 5.1 版本开始，此接口扩展了 {@link Iterable} 并提供 {@link Stream} 支持。
// 因此，它可以在 {@code for} 循环中使用，提供 {@link #forEach} 迭代，并允许集合样式的 {@link #stream} 访问。
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * <p>Allows for specifying explicit construction arguments, along the
	 * lines of {@link BeanFactory#getBean(String, Object...)}.
	 * @param args arguments to use when creating a corresponding instance
	 * @return an instance of the bean
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	// 返回此工厂管理的对象的实例（可能是共享的，也可能是独立的）。
	// <p>允许指定显式的构造参数，类似于 {@link BeanFactory#getBean(String, Object...)}。
	// @param args 创建相应实例时使用的参数
	// @return Bean 的实例
	// @throws BeansException（如果创建错误）
	T getObject(Object... args) throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @return an instance of the bean, or {@code null} if not available
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	// 返回此工厂管理的对象的实例（可能是共享的，也可能是独立的）。
	// @return Bean 的实例，如果不可用，则返回 {@code null}
	// @throws BeansException（如果创建错误）
	@Nullable
	T getIfAvailable() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @param defaultSupplier a callback for supplying a default object
	 * if none is present in the factory
	 * @return an instance of the bean, or the supplied default object
	 * if no such bean is available
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	// 返回此工厂管理的对象的一个实例（可能是共享的，也可能是独立的）。
	// @param defaultSupplier 如果工厂中不存在默认对象，则返回一个回调函数。
	// @return 一个 bean 实例，如果不存在可用的 bean，则返回提供的默认对象。
	// @throws BeansException 如果创建错误
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object
	 * managed by this factory, if available.
	 * @param dependencyConsumer a callback for processing the target object
	 * if available (not called otherwise)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	// 如果可用，则使用此工厂管理的对象的一个实例（可能是共享的，也可能是独立的）。
	// @param dependencyConsumer 用于处理目标对象的回调（如果可用）（否则不调用）
	// @throws BeansException 如果创建错误
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @return an instance of the bean, or {@code null} if not available or
	 * not unique (i.e. multiple candidates found with none marked as primary)
	 * @throws BeansException in case of creation errors
	 * @see #getObject()
	 */
	// 返回此工厂管理的对象的实例（可能是共享的，也可能是独立的）。
	// @return 一个 bean 实例，如果不可用或不唯一（例如，找到多个候选对象，但没有一个被标记为主 bean），则返回 {@code null}。
	// @throws 如果创建错误，则抛出 BeansException
	@Nullable
	T getIfUnique() throws BeansException;

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * @param defaultSupplier a callback for supplying a default object
	 * if no unique candidate is present in the factory
	 * @return an instance of the bean, or the supplied default object
	 * if no such bean is available or if it is not unique in the factory
	 * (i.e. multiple candidates found with none marked as primary)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfUnique()
	 */
	// 返回此工厂管理的对象的实例（可能是共享的，也可能是独立的）。
	// @param defaultSupplier 如果工厂中不存在唯一候选对象，则返回一个回调函数，用于提供默认对象。
	// @return 一个 bean 实例；如果不存在此类 bean 或该 bean 在工厂中不唯一（例如，找到多个候选对象，但没有一个被标记为主 bean），则返回提供的默认对象。
	// @throws 如果创建错误，则抛出 BeansException
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * Consume an instance (possibly shared or independent) of the object
	 * managed by this factory, if unique.
	 * @param dependencyConsumer a callback for processing the target object
	 * if unique (not called otherwise)
	 * @throws BeansException in case of creation errors
	 * @since 5.0
	 * @see #getIfAvailable()
	 */
	// 如果此工厂管理的对象唯一，则使用该对象的实例（可能是共享的或独立的）。
	// @param dependencyConsumer 如果目标对象唯一，则回调函数用于处理目标对象（否则不调用）
	// @throws BeansException 如果创建错误
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * Return an {@link Iterator} over all matching object instances,
	 * without specific ordering guarantees (but typically in registration order).
	 * @since 5.1
	 * @see #stream()
	 */
	// 返回一个覆盖所有匹配对象实例的 {@link Iterator}，不保证特定的顺序（但通常按注册顺序）。
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances,
	 * without specific ordering guarantees (but typically in registration order).
	 * @since 5.1
	 * @see #iterator()
	 * @see #orderedStream()
	 */
	// 返回一个覆盖所有匹配对象实例的顺序 {@link Stream}，不保证特定的顺序（但通常按注册顺序）。
	default Stream<T> stream() {
		throw new UnsupportedOperationException("Multi element access not supported");
	}

	/**
	 * Return a sequential {@link Stream} over all matching object instances,
	 * pre-ordered according to the factory's common order comparator.
	 * <p>In a standard Spring application context, this will be ordered
	 * according to {@link org.springframework.core.Ordered} conventions,
	 * and in case of annotation-based configuration also considering the
	 * {@link org.springframework.core.annotation.Order} annotation,
	 * analogous to multi-element injection points of list/array type.
	 * @since 5.1
	 * @see #stream()
	 * @see org.springframework.core.OrderComparator
	 */
	// 返回一个包含所有匹配对象实例的顺序 {@link Stream}，该 Stream 根据工厂函数的通用顺序比较器进行预排序。
	// <p>在标准的 Spring 应用上下文中，该 Stream 将根据 {@link org.springframework.core.Ordered} 约定进行排序，
	// 如果是基于注解的配置，还会考虑 {@link org.springframework.core.annotation.Order} 注解，类似于列表/数组类型的多元素注入点。
	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("Ordered element access not supported");
	}

}
