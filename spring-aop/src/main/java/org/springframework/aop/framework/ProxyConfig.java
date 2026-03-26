/*
 * Copyright 2002-2021 the original author or authors.
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

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Convenience superclass for configuration used in creating proxies,
 * to ensure that all proxy creators have consistent properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
// 用于创建代理的配置的便捷超类，以确保所有代理创建者都具有一致的属性。
public class ProxyConfig implements Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability. */
	private static final long serialVersionUID = -8409359707199703185L;


	private boolean proxyTargetClass = false;

	private boolean optimize = false;

	boolean opaque = false;

	boolean exposeProxy = false;

	private boolean frozen = false;


	/**
	 * Set whether to proxy the target class directly, instead of just proxying
	 * specific interfaces. Default is "false".
	 * <p>Set this to "true" to force proxying for the TargetSource's exposed
	 * target class. If that target class is an interface, a JDK proxy will be
	 * created for the given interface. If that target class is any other class,
	 * a CGLIB proxy will be created for the given class.
	 * <p>Note: Depending on the configuration of the concrete proxy factory,
	 * the proxy-target-class behavior will also be applied if no interfaces
	 * have been specified (and no interface autodetection is activated).
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	// 设置是否直接代理目标类，而不是仅代理特定接口。默认值为 “false”。
	//
	// <p>设置为 “true” 将强制代理 TargetSource 暴露的目标类。
	// 如果目标类是接口，则会为该接口创建一个 JDK 代理。如果目标类是其他类，则会为该类创建一个 CGLIB 代理。
	//
	// <p>注意：根据具体代理工厂的配置，即使未指定接口（且未激活接口自动检测），proxy-target-class 行为也将适用。
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * Return whether to proxy the target class directly as well as any interfaces.
	 */
	// 返回是否直接代理目标类以及任何接口。
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}

	/**
	 * Set whether proxies should perform aggressive optimizations.
	 * The exact meaning of "aggressive optimizations" will differ
	 * between proxies, but there is usually some tradeoff.
	 * Default is "false".
	 * <p>With Spring's current proxy options, this flag effectively
	 * enforces CGLIB proxies (similar to {@link #setProxyTargetClass})
	 * but without any class validation checks (for final methods etc).
	 */
	// 设置代理是否应执行积极优化。“积极优化”的确切含义因代理而异，但通常需要权衡利弊。默认值为“false”。
	//
	// <p>使用 Spring 当前的代理选项，此标志可有效强制执行 CGLIB 代理（类似于 {@link #setProxyTargetClass}），
	// 但不执行任何类验证检查（例如 final 方法）。
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	/**
	 * Return whether proxies should perform aggressive optimizations.
	 */
	// 返回代理是否应该执行积极优化。
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * Set whether proxies created by this configuration should be prevented
	 * from being cast to {@link Advised} to query proxy status.
	 * <p>Default is "false", meaning that any AOP proxy can be cast to
	 * {@link Advised}.
	 */
	// 设置是否应阻止此配置创建的代理转换为 {@link Advised} 来查询代理状态。
	// <p>默认值为“false”，表示任何 AOP 代理都可以转换为 {@link Advised}。
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	/**
	 * Return whether proxies created by this configuration should be
	 * prevented from being cast to {@link Advised}.
	 */
	// 返回是否应阻止通过此配置创建的代理被强制转换为 {@link Advised}。
	public boolean isOpaque() {
		return this.opaque;
	}

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses {@code this}, the invocation will not be advised).
	 * <p>Default is "false", in order to avoid unnecessary extra interception.
	 * This means that no guarantees are provided that AopContext access will
	 * work consistently within any method of the advised object.
	 */
	// 设置是否应将代理作为 ThreadLocal 暴露给 AOP 框架，以便通过 AopContext 类进行检索。
	// 如果被建议的对象需要调用自身上的另一个被建议的方法，则此功能非常有用。（如果使用 {@code this}，则不会建议该调用。）
	//
	// <p>默认值为“false”，以避免不必要的额外拦截。这意味着不保证在被建议对象的任何方法中都能始终如一地访问 AopContext。
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}

	/**
	 * Return whether the AOP proxy will expose the AOP proxy for
	 * each invocation.
	 */
	// 返回 AOP 代理是否会在每次调用时都暴露 AOP 代理。
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	/**
	 * Set whether this config should be frozen.
	 * <p>When a config is frozen, no advice changes can be made. This is
	 * useful for optimization, and useful when we don't want callers to
	 * be able to manipulate configuration after casting to Advised.
	 */
	// 设置此配置是否应冻结。
	// <p>当配置被冻结时，将无法进行任何建议更改。这对于优化很有用，并且当我们不希望调用者在转换为“Advised”状态后能够操作配置时也很有用。
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	/**
	 * Return whether the config is frozen, and no advice changes can be made.
	 */
	// 返回配置是否已冻结，且无法进行任何建议更改。
	public boolean isFrozen() {
		return this.frozen;
	}


	/**
	 * Copy configuration from the other config object.
	 * @param other object to copy configuration from
	 */
	// 从其他配置对象复制配置。
	// @param 要从中复制配置的其他对象
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}
