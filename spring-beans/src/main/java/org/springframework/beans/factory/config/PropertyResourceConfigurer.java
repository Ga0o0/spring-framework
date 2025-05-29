/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targeted at system
 * administrators that override bean properties configured in the application context.
 *
 * <p>Two concrete implementations are provided in the distribution:
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * <li>{@link PropertyPlaceholderConfigurer} for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@link #convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
// 允许从属性资源（即属性文件）配置单个 bean 的属性值。这对于系统管理员自定义配置文件非常有用，这些配置文件会覆盖在应用程序上下文中配置的 bean 属性。
//
// <p>发行版中提供了两种具体实现：
// <ul>
// <li>{@link PropertyOverrideConfigurer} 用于 “beanName.property=value” 样式的覆盖（将属性文件中的值<i>推送</i>到 bean 定义中）
// <li>{@link PropertyPlaceholderConfigurer} 用于替换 “${...}” 占位符（将属性文件中的值<i>拉取</i>到 bean 定义中）
// </ul>
//
// <p>属性值可以在读取后进行转换，方法是覆盖 {@link #convertPropertyValue} 方法。例如，可以在处理加密值之前对其进行检测和解密。
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport
		implements BeanFactoryPostProcessor, PriorityOrdered {

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered


	/**
	 * Set the order value of this object for sorting purposes.
	 * @see PriorityOrdered
	 */
	// 设置此对象的顺序值以用于排序目的。
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	/**
	 * {@linkplain #mergeProperties Merge}, {@linkplain #convertProperties convert} and
	 * {@linkplain #processProperties process} properties against the given bean factory.
	 * @throws BeanInitializationException if any properties cannot be loaded
	 */
	// 针对给定的 bean 工厂，使用 {@linkplain #mergeProperties Merge}、{@linkplain #convertProperties convert} 和 {@linkplain #processProperties process} 属性。
	// 如果任何属性无法加载，则抛出 BeanInitializationException
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			Properties mergedProps = mergeProperties();

			// Convert the merged properties, if necessary.
			convertProperties(mergedProps);

			// Let the subclass process the properties.
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Convert the given merged properties, converting property values
	 * if necessary. The result will then be processed.
	 * <p>The default implementation will invoke {@link #convertPropertyValue}
	 * for each property value, replacing the original with the converted value.
	 * @param props the Properties to convert
	 * @see #processProperties
	 */
	// 转换给定的合并属性，必要时转换属性值。然后处理结果。
	// <p>默认实现将为每个属性值调用 {@link #convertPropertyValue}，用转换后的值替换原始值。
	// @param props 要转换的属性
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	/**
	 * Convert the given property from the properties source to the value
	 * which should be applied.
	 * <p>The default implementation calls {@link #convertPropertyValue(String)}.
	 * @param propertyName the name of the property that the value is defined for
	 * @param propertyValue the original value from the properties source
	 * @return the converted value, to be used for processing
	 * @see #convertPropertyValue(String)
	 */
	// 将属性源中的给定属性转换为应应用的值。
	// <p>默认实现调用 {@link #convertPropertyValue(String)}。
	// @param propertyName 为其定义值的属性名称
	// @param propertyValue 属性源中的原始值
	// @return 转换后的值，用于后续处理
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}

	/**
	 * Convert the given property value from the properties source to the value
	 * which should be applied.
	 * <p>The default implementation simply returns the original value.
	 * Can be overridden in subclasses, for example to detect
	 * encrypted values and decrypt them accordingly.
	 * @param originalValue the original value from the properties source
	 * (properties file or local "properties")
	 * @return the converted value, to be used for processing
	 * @see #setProperties
	 * @see #setLocations
	 * @see #setLocation
	 * @see #convertProperty(String, String)
	 */
	// 将属性源中给定的属性值转换为应应用的值。
	// <p>默认实现仅返回原始值。可以在子类中重写，例如，用于检测加密值并相应地解密。
	// @param originalValue 来自属性源（属性文件或本地“属性”）的原始值
	// @return 转换后的值，用于处理
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}


	/**
	 * Apply the given Properties to the given BeanFactory.
	 * @param beanFactory the BeanFactory used by the application context
	 * @param props the Properties to apply
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 将给定的属性应用到给定的 BeanFactory。
	// @param beanFactory 应用上下文使用的 BeanFactory
	// @param props 要应用的属性
	// @throws org.springframework.beans.BeansException 以防出错
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException;

}
