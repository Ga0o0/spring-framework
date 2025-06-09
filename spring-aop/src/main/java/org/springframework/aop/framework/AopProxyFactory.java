/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.aop.framework;

/**
 * Interface to be implemented by factories that are able to create
 * AOP proxies based on {@link AdvisedSupport} configuration objects.
 *
 * <p>Proxies should observe the following contract:
 * <ul>
 * <li>They should implement all interfaces that the configuration
 * indicates should be proxied.
 * <li>They should implement the {@link Advised} interface.
 * <li>They should implement the equals method to compare proxied
 * interfaces, advice, and target.
 * <li>They should be serializable if all advisors and target
 * are serializable.
 * <li>They should be thread-safe if advisors and target
 * are thread-safe.
 * </ul>
 *
 * <p>Proxies may or may not allow advice changes to be made.
 * If they do not permit advice changes (for example, because
 * the configuration was frozen) a proxy should throw an
 * {@link AopConfigException} on an attempted advice change.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
// 接口由能够基于 {@link AdvisedSupport} 配置对象创建 AOP 代理的工厂实现。
//
// <p>代理应遵守以下契约：
// <ul>
// <li>它们应实现配置指示应代理的所有接口。
// <li>它们应实现 {@link Advised} 接口。
// <li>它们应实现 equals 方法来比较代理接口、建议和目标。
// <li>如果所有顾问和目标都是可序列化的，它们也应该是可序列化的。
// <li>如果顾问和目标是线程安全的，它们也应该是线程安全的。
// </ul>
//
// <p>代理可能允许或不允许进行建议更改。如果它们不允许建议更改（例如，因为配置被冻结），
// 则代理应在尝试更改建议时抛出 {@link AopConfigException}。
public interface AopProxyFactory {

	/**
	 * Create an {@link AopProxy} for the given AOP configuration.
	 * @param config the AOP configuration in the form of an
	 * AdvisedSupport object
	 * @return the corresponding AOP proxy
	 * @throws AopConfigException if the configuration is invalid
	 */
	// 为给定的 AOP 配置创建一个 {@link AopProxy}。
	// @param config 以 AdvisedSupport 对象的形式返回 AOP 配置
	// @return 相应的 AOP 代理
	// 如果配置无效，则抛出 AopConfigException
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
