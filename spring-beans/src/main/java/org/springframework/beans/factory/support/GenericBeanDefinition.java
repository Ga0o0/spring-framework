/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * GenericBeanDefinition is a one-stop shop for declarative bean definition purposes.
 * Like all common bean definitions, it allows for specifying a class plus optionally
 * constructor argument values and property values. Additionally, deriving from a
 * parent bean definition can be flexibly configured through the "parentName" property.
 *
 * <p>In general, use this {@code GenericBeanDefinition} class for the purpose of
 * registering declarative bean definitions (e.g. XML definitions which a bean
 * post-processor might operate on, potentially even reconfiguring the parent name).
 * Use {@code RootBeanDefinition}/{@code ChildBeanDefinition} where parent/child
 * relationships happen to be pre-determined, and prefer {@link RootBeanDefinition}
 * specifically for programmatic definitions derived from factory methods/suppliers.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see #setParentName
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
// GenericBeanDefinition 是声明式 Bean 定义的一站式解决方案。与所有常见的 Bean 定义一样，
// 它允许指定类以及可选的构造函数参数值和属性值。此外，可以通过“parentName”属性灵活地配置从父 Bean 定义派生。
//
// <p>通常，使用此 {@code GenericBeanDefinition} 类来注册声明式 Bean 定义（例如，Bean 后处理器可能对其进行操作的 XML 定义，
// 甚至可能重新配置父级名称）。如果父/子关系是预先确定的，请使用 {@code RootBeanDefinition}/{@code ChildBeanDefinition}；
// 对于从工厂方法/供应商派生的编程式定义，则优先使用 {@link RootBeanDefinition}。
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

	@Nullable
	private String parentName;


	/**
	 * Create a new GenericBeanDefinition, to be configured through its bean
	 * properties and configuration methods.
	 * @see #setBeanClass
	 * @see #setScope
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public GenericBeanDefinition() {
		super();
	}

	/**
	 * Create a new GenericBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	// 创建一个新的 GenericBeanDefinition 作为给定 bean 定义的深层副本。
	// @param original 要复制的原始 bean 定义
	public GenericBeanDefinition(BeanDefinition original) {
		super(original);
	}


	@Override
	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Override
	@Nullable
	public String getParentName() {
		return this.parentName;
	}


	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new GenericBeanDefinition(this);
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof GenericBeanDefinition that &&
				ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other)));
	}

	@Override
	public String toString() {
		if (this.parentName != null) {
			return "Generic bean with parent '" + this.parentName + "': " + super.toString();
		}
		return "Generic bean: " + super.toString();
	}

}
