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

/**
 * A marker superinterface indicating that a bean is eligible to be notified by the
 * Spring container of a particular framework object through a callback-style method.
 * The actual method signature is determined by individual subinterfaces but should
 * typically consist of just one void-returning method that accepts a single argument.
 *
 * <p>Note that merely implementing {@link Aware} provides no default functionality.
 * Rather, processing must be done explicitly, for example in a
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}.
 * Refer to {@link org.springframework.context.support.ApplicationContextAwareProcessor}
 * for an example of processing specific {@code *Aware} interface callbacks.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
// 一个标记超接口，指示某个 bean 有资格通过回调方法接收 Spring 容器关于特定框架对象的通知。
// 实际方法签名由各个子接口决定，但通常只包含一个接受单个参数且返回 void 的方法。
//
// <p>请注意，仅实现 {@link Aware} 接口不提供任何默认功能。
// 相反，必须显式处理，例如在 {@link org.springframework.beans.factory.config.BeanPostProcessor} 中。
// 有关处理特定 {@code Aware} 接口回调的示例，请参阅 {@link org.springframework.context.support.ApplicationContextAwareProcessor}。
public interface Aware {

}
