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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Beans on which the current bean depends. Any beans specified are guaranteed to be
 * created by the container before this bean. Used infrequently in cases where a bean
 * does not explicitly depend on another through properties or constructor arguments,
 * but rather depends on the side effects of another bean's initialization.
 *
 * <p>A depends-on declaration can specify both an initialization-time dependency and,
 * in the case of singleton beans only, a corresponding destruction-time dependency.
 * Dependent beans that define a depends-on relationship with a given bean are destroyed
 * first, prior to the given bean itself being destroyed. Thus, a depends-on declaration
 * can also control shutdown order.
 *
 * <p>May be used on any class directly or indirectly annotated with
 * {@link org.springframework.stereotype.Component} or on methods annotated
 * with {@link Bean}.
 *
 * <p>Using {@link DependsOn} at the class level has no effect unless component-scanning
 * is being used. If a {@link DependsOn}-annotated class is declared via XML,
 * {@link DependsOn} annotation metadata is ignored, and
 * {@code <bean depends-on="..."/>} is respected instead.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
// 当前 Bean 所依赖的 Bean。任何指定的 Bean 都保证先于此 Bean 由容器创建。
// 当 Bean 不通过属性或构造函数参数显式地依赖于另一个 Bean，而是依赖于另一个 Bean 初始化的副作用时，此方法很少使用。
//
// <p>depends-on 声明既可以指定初始化时的依赖关系，也可以在单例 Bean 的情况下指定相应的销毁时依赖关系。
// 与给定 Bean 定义依赖关系的 Bean 会先被销毁，然后再销毁给定 Bean 本身。因此，depends-on 声明还可以控制关闭顺序。
//
// <p>可以用于任何直接或间接使用 {@link org.springframework.stereotype.Component} 注解的类，或使用 {@link Bean} 注解的方法。
//
// <p>除非使用组件扫描，否则在类级别使用 {@link DependsOn} 无效。
// 如果通过 XML 声明了带有 {@link DependsOn} 注释的类，则会忽略 {@link DependsOn} 注释元数据，而改为遵循 {@code <bean depends-on="..."/>}。
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DependsOn {

	String[] value() default {};

}
