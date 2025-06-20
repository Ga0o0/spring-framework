/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that the annotated element represents a stereotype for the index.
 *
 * <p>The {@code CandidateComponentsIndex} is an alternative to classpath
 * scanning that uses a metadata file generated at compilation time. The
 * index allows retrieving the candidate components (i.e. fully qualified
 * name) based on a stereotype. This annotation instructs the generator to
 * index the element on which the annotated element is present or if it
 * implements or extends from the annotated element. The stereotype is the
 * fully qualified name of the annotated element.
 *
 * <p>Consider the default {@link Component} annotation that is meta-annotated
 * with this annotation. If a component is annotated with {@link Component},
 * an entry for that component will be added to the index using the
 * {@code org.springframework.stereotype.Component} stereotype.
 *
 * <p>This annotation is also honored on meta-annotations. Consider this
 * custom annotation:
 * <pre class="code">
 * package com.example;
 *
 * &#064;Target(ElementType.TYPE)
 * &#064;Retention(RetentionPolicy.RUNTIME)
 * &#064;Documented
 * &#064;Indexed
 * &#064;Service
 * public @interface PrivilegedService { ... }
 * </pre>
 *
 * If the above annotation is present on a type, it will be indexed with two
 * stereotypes: {@code org.springframework.stereotype.Component} and
 * {@code com.example.PrivilegedService}. While {@link Service} isn't directly
 * annotated with {@code Indexed}, it is meta-annotated with {@link Component}.
 *
 * <p>It is also possible to index all implementations of a certain interface or
 * all the subclasses of a given class by adding {@code @Indexed} on it.
 *
 * Consider this base interface:
 * <pre class="code">
 * package com.example;
 *
 * &#064;Indexed
 * public interface AdminService { ... }
 * </pre>
 *
 * Now, consider an implementation of this {@code AdminService} somewhere:
 * <pre class="code">
 * package com.example.foo;
 *
 * import com.example.AdminService;
 *
 * public class ConfigurationAdminService implements AdminService { ... }
 * </pre>
 *
 * Because this class implements an interface that is indexed, it will be
 * automatically included with the {@code com.example.AdminService} stereotype.
 * If there are more {@code @Indexed} interfaces and/or superclasses in the
 * hierarchy, the class will map to all their stereotypes.
 *
 * @author Stephane Nicoll
 * @since 5.0
 */
// 指示带注释的元素代表索引的构造型。
//
// <p>{@code CandidateComponentsIndex} 是类路径扫描的替代方案，它使用编译时生成的元数据文件。
// 索引允许根据构造型检索候选组件（即完全限定名称）。此注释指示生成器索引带注释元素所在的元素，
// 或者索引该元素是否实现或扩展自带注释的元素。构造型是带注释元素的完全限定名称。
//
// <p>考虑使用此注释进行元注释的默认 {@link Component} 注释。
// 如果组件使用 {@link Component} 注释，则将使用 {@code org.springframework.stereotype.Component} 构造型将该组件的条目添加到索引中。
//
// <p>此注释也适用于元注释。考虑这个自定义注释：
// <pre class="code">
// package com.example;
//
// @Target(ElementType.TYPE)
// @Retention(RetentionPolicy.RUNTIME)
// @Documented
// @Indexed
// @Service
// public @interface PrivilegedService { ... }
// </pre>
//
// 如果上述注解存在于某个类型上，它将使用两个构造型进行索引：
// {@code org.springframework.stereotype.Component} 和 {@code com.example.PrivilegedService}。
// 虽然 {@link Service} 没有直接使用 {@code Indexed} 注解，但它使用 {@link Component} 进行元注解。
//
// <p>还可以通过在某个接口上添加 {@code @Indexed} 来索引该接口的所有实现或给定类的所有子类。考虑这个基本接口：
// <pre class="code">
// package com.example;
//
// @Indexed
// public interface AdminService { ... }
// </pre>
//
// 现在，考虑这个 {@code AdminService} 在某处的实现：
//
// <pre class="code">
// package com.example.foo;
//
// import com.example.AdminService;
//
// public ConfigurationAdminService implements AdminService { ... }
// </pre>
//
// 由于此类实现了一个已索引的接口，它将自动包含在 {@code com.example.AdminService} 构造型中。
// 如果层次结构中存在更多 {@code @Indexed} 接口和/或超类，则该类将映射到所有它们的构造型。
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Indexed {
}
