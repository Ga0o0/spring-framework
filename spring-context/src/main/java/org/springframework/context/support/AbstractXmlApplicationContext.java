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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.BeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
// 方便的 {@link org.springframework.context.ApplicationContext} 实现基类，
// 可从包含 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader} 可理解的 bean 定义的 XML 文档中提取配置。
//
// <p>子类只需实现 {@link #getConfigResources} 和/或 {@link #getConfigLocations} 方法。
// 此外，它们可以重写 {@link #getResourceByPath} 钩子，以便以特定于环境的方式解释相对路径，
// 以及/或者重写 {@link #getResourcePatternResolver} 以进行扩展的模式解析。
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	// 使用给定的父上下文创建一个新的 AbstractXmlApplicationContext。
	// @param parent 父上下文
	public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	// 通过 XmlBeanDefinitionReader 加载 bean 定义。
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory. --> 译文：为给定的 BeanFactory 创建一个新的 XmlBeanDefinitionReader。
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment. --> 译文：使用此上下文的资源加载环境配置 Bean 定义读取器。
		// 设置读取 Bean 定义时要使用的环境。
		beanDefinitionReader.setEnvironment(getEnvironment());
		// 设置要用于资源位置的 ResourceLoader。
		beanDefinitionReader.setResourceLoader(this);
		// 设置用于分析的 SAX 实体解析程序。
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions. --> 译文：允许子类提供读取器的自定义初始化，然后继续实际加载 Bean 定义。
		// 初始化用于加载此上下文的 bean 定义的 bean 定义读取器。默认实现会设置验证标志。
		initBeanDefinitionReader(beanDefinitionReader);
		// 使用给定的 XmlBeanDefinitionReader 加载 Bean 定义。
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Initialize the bean definition reader used for loading the bean definitions
	 * of this context. The default implementation sets the validating flag.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different {@link BeanDefinitionDocumentReader} implementation.
	 * @param reader the bean definition reader used by this context
	 * @see XmlBeanDefinitionReader#setValidating
	 * @see XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	// 初始化用于加载此上下文的 bean 定义的 bean 定义读取器。默认实现会设置验证标志。
	// <p>可在子类中重写，例如，关闭 XML 验证或使用其他 {@link BeanDefinitionDocumentReader} 实现。
	// @param reader 此上下文使用的 bean 定义读取器
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
	 * method; hence this method is just supposed to load and/or register bean definitions.
	 * @param reader the XmlBeanDefinitionReader to use
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	// 使用给定的 XmlBeanDefinitionReader 加载 Bean 定义。
	// <p>Bean 工厂的生命周期由 {@link #refreshBeanFactory} 方法处理；因此，此方法仅用于加载和/或注册 Bean 定义。
	// @param reader 要使用的 XmlBeanDefinitionReader
	// @throws BeansException（如果 Bean 注册错误）
	// @throws IOException（如果未找到所需的 XML 文档）
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		// 返回一个 Resource 对象数组，指向构建此上下文所需的 XML Bean 定义文件。
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		// 返回一个资源位置数组，指向构建此上下文所需的 XML bean 定义文件。
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			// 从指定的资源位置加载 bean 定义。
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Return an array of Resource objects, referring to the XML bean definition
	 * files that this context should be built with.
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide pre-built Resource objects rather than location Strings.
	 * @return an array of Resource objects, or {@code null} if none
	 * @see #getConfigLocations()
	 */
	// 返回一个 Resource 对象数组，指向构建此上下文所需的 XML Bean 定义文件。
	// <p>默认实现返回 {@code null}。子类可以重写此实现，以提供预构建的 Resource 对象，而不是位置字符串。
	// @return 一个 Resource 对象数组，如果没有，则返回 {@code null}
	@Nullable
	protected Resource[] getConfigResources() {
		return null;
	}

}
