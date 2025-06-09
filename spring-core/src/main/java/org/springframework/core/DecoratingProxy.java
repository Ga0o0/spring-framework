/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.core;

/**
 * Interface to be implemented by decorating proxies, in particular Spring AOP
 * proxies but potentially also custom proxies with decorator semantics.
 *
 * <p>Note that this interface should just be implemented if the decorated class
 * is not within the hierarchy of the proxy class to begin with. In particular,
 * a "target-class" proxy such as a Spring AOP CGLIB proxy should not implement
 * it since any lookup on the target class can simply be performed on the proxy
 * class there anyway.
 *
 * <p>Defined in the core module in order to allow
 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
 * (and potential other candidates without spring-aop dependencies) to use it
 * for introspection purposes, in particular annotation lookups.
 *
 * @author Juergen Hoeller
 * @since 4.3
 */
// 该接口由装饰代理实现，特别是 Spring AOP 代理，但也可能包括具有装饰器语义的自定义代理。
//
// <p>请注意，只有当被装饰类不在代理类的层次结构中时，才应该实现此接口。
// 特别是，“目标类”代理（例如 Spring AOP CGLIB 代理）不应该实现此接口，因为任何对目标类的查找都可以在代理类上直接执行。
//
// <p>在核心模块中定义，以便允许 {@link org.springframework.core.annotation.AnnotationAwareOrderComparator}
// （以及其他可能不依赖 spring-aop 的候选对象）将其用于自省目的，尤其是注解查找。
public interface DecoratingProxy {

	/**
	 * Return the (ultimate) decorated class behind this proxy.
	 * <p>In case of an AOP proxy, this will be the ultimate target class,
	 * not just the immediate target (in case of multiple nested proxies).
	 * @return the decorated class (never {@code null})
	 */
	// 返回此代理背后的（最终）装饰类。
	// <p>对于 AOP 代理，这将是最终目标类，而不仅仅是直接目标（在多个嵌套代理的情况下）。
	// @return 被装饰的类（永远不会为 {@code null}）
	Class<?> getDecoratedClass();

}
