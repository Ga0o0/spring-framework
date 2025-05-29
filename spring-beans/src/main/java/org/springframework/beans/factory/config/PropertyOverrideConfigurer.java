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

package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanInitializationException;

/**
 * Property resource configurer that overrides bean property values in an application
 * context definition. It <i>pushes</i> values from a properties file into bean definitions.
 *
 * <p>Configuration lines are expected to be of the following form:
 *
 * <pre class="code">beanName.property=value</pre>
 *
 * Example properties file:
 *
 * <pre class="code">dataSource.driverClassName=com.mysql.jdbc.Driver
 * dataSource.url=jdbc:mysql:mydb</pre>
 *
 * In contrast to PropertyPlaceholderConfigurer, the original definition can have default
 * values or no values at all for such bean properties. If an overriding properties file does
 * not have an entry for a certain bean property, the default context definition is used.
 *
 * <p>Note that the context definition <i>is not</i> aware of being overridden;
 * so this is not immediately obvious when looking at the XML definition file.
 * Furthermore, note that specified override values are always <i>literal</i> values;
 * they are not translated into bean references. This also applies when the original
 * value in the XML bean definition specifies a bean reference.
 *
 * <p>In case of multiple PropertyOverrideConfigurers that define different values for
 * the same bean property, the <i>last</i> one will win (due to the overriding mechanism).
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@code convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 12.03.2003
 * @see #convertPropertyValue
 * @see PropertyPlaceholderConfigurer
 */
// 属性资源配置器，用于覆盖应用上下文定义中的 Bean 属性值。它将属性文件中的值<i>推送</i>到 Bean 定义中。
//
// <p>配置行应采用以下格式：
// <pre class="code">beanName.property=value</pre>
//
// 示例属性文件：
// <pre class="code">
// dataSource.driverClassName=com.mysql.jdbc.Driver
// dataSource.url=jdbc:mysql:mydb
// </pre>
//
// 与 PropertyPlaceholderConfigurer 不同，原始定义可以对此类 Bean 属性设置默认值，也可以不设置任何值。
// 如果覆盖的属性文件没有某个 Bean 属性的条目，则使用默认的上下文定义。
//
// <p>请注意，上下文定义<i>无法感知到</i>自身已被覆盖；因此在查看 XML 定义文件时，这一点并非显而易见。此外，指定的覆盖值始终是<i>字面值</i>；
// 它们不会被转换为 Bean 引用。当 XML Bean 定义中的原始值指定为 Bean 引用时，此规则同样适用。
//
// <p>如果多个 PropertyOverrideConfigurers 为同一个 Bean 属性定义了不同的值，则最后一个 PropertyOverrideConfigurers 将生效（由于覆盖机制）。
//
// <p>属性值可以在读取后通过覆盖 {@code convertPropertyValue} 方法进行转换。例如，可以在处理加密值之前对其进行检测和相应的解密。
public class PropertyOverrideConfigurer extends PropertyResourceConfigurer {

	/**
	 * The default bean name separator.
	 */
	// 默认的 bean 名称分隔符。
	public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";


	private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

	private boolean ignoreInvalidKeys = false;

	/**
	 * Contains names of beans that have overrides.
	 */
	// 包含已覆盖的 bean 的名称。
	private final Set<String> beanNames = Collections.newSetFromMap(new ConcurrentHashMap<>(16));


	/**
	 * Set the separator to expect between bean name and property path.
	 * Default is a dot (".").
	 */
	// 设置 bean 名称和属性路径之间的分隔符。默认为句点 (".")。
	public void setBeanNameSeparator(String beanNameSeparator) {
		this.beanNameSeparator = beanNameSeparator;
	}

	/**
	 * Set whether to ignore invalid keys. Default is "false".
	 * <p>If you ignore invalid keys, keys that do not follow the 'beanName.property' format
	 * (or refer to invalid bean names or properties) will just be logged at debug level.
	 * This allows one to have arbitrary other keys in a properties file.
	 */
	// 设置是否忽略无效键。默认为 false。
	// <p>如果忽略无效的键，则不遵循“beanName.property”格式（或引用无效的 Bean 名称或属性）的键将仅在调试级别记录。这允许在属性文件中包含任意其他键。
	public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
		this.ignoreInvalidKeys = ignoreInvalidKeys;
	}


	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {

		for (Enumeration<?> names = props.propertyNames(); names.hasMoreElements();) {
			String key = (String) names.nextElement();
			try {
				processKey(beanFactory, key, props.getProperty(key));
			}
			catch (BeansException ex) {
				String msg = "Could not process key '" + key + "' in PropertyOverrideConfigurer";
				if (!this.ignoreInvalidKeys) {
					throw new BeanInitializationException(msg, ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug(msg, ex);
				}
			}
		}
	}

	/**
	 * Process the given key as 'beanName.property' entry.
	 */
	// 将给定的键作为 “beanName.property” 条目进行处理。
	protected void processKey(ConfigurableListableBeanFactory factory, String key, String value)
			throws BeansException {

		int separatorIndex = key.indexOf(this.beanNameSeparator);
		if (separatorIndex == -1) {
			throw new BeanInitializationException("Invalid key '" + key +
					"': expected 'beanName" + this.beanNameSeparator + "property'");
		}
		String beanName = key.substring(0, separatorIndex);
		String beanProperty = key.substring(separatorIndex + 1);
		this.beanNames.add(beanName);
		applyPropertyValue(factory, beanName, beanProperty, value);
		if (logger.isDebugEnabled()) {
			logger.debug("Property '" + key + "' set to value [" + value + "]");
		}
	}

	/**
	 * Apply the given property value to the corresponding bean.
	 */
	// 将给定的属性值应用到相应的 bean。
	protected void applyPropertyValue(
			ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

		BeanDefinition bd = factory.getBeanDefinition(beanName);
		BeanDefinition bdToUse = bd;
		while (bd != null) {
			bdToUse = bd;
			bd = bd.getOriginatingBeanDefinition();
		}
		PropertyValue pv = new PropertyValue(property, value);
		pv.setOptional(this.ignoreInvalidKeys);
		bdToUse.getPropertyValues().addPropertyValue(pv);
	}


	/**
	 * Were there overrides for this bean?
	 * Only valid after processing has occurred at least once.
	 * @param beanName name of the bean to query status for
	 * @return whether there were property overrides for the named bean
	 */
	// 此 bean 是否有覆盖？仅在处理至少发生一次后有效。
	// @param beanName 用于查询状态的 bean 的名称
	// @return 指定 bean 是否有属性覆盖
	public boolean hasPropertyOverridesFor(String beanName) {
		return this.beanNames.contains(beanName);
	}

}
