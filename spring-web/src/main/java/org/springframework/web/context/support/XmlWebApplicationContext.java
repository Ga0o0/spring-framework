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

package org.springframework.web.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * {@link org.springframework.web.context.WebApplicationContext} implementation
 * which takes its configuration from XML documents, understood by an
 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 * This is essentially the equivalent of
 * {@link org.springframework.context.support.GenericXmlApplicationContext}
 * for a web environment.
 *
 * <p>By default, the configuration will be taken from "/WEB-INF/applicationContext.xml"
 * for the root context, and "/WEB-INF/test-servlet.xml" for a context with the namespace
 * "test-servlet" (like for a DispatcherServlet instance with the servlet-name "test").
 *
 * <p>The config location defaults can be overridden via the "contextConfigLocation"
 * context-param of {@link org.springframework.web.context.ContextLoader} and servlet
 * init-param of {@link org.springframework.web.servlet.FrameworkServlet}. Config locations
 * can either denote concrete files like "/WEB-INF/context.xml" or Ant-style patterns
 * like "/WEB-INF/*-context.xml" (see {@link org.springframework.util.PathMatcher}
 * javadoc for pattern details).
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p><b>For a WebApplicationContext that reads in a different bean definition format,
 * create an analogous subclass of {@link AbstractRefreshableWebApplicationContext}.</b>
 * Such a context implementation can be specified as "contextClass" context-param
 * for ContextLoader or "contextClass" init-param for FrameworkServlet.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setNamespace
 * @see #setConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see org.springframework.web.context.ContextLoader#initWebApplicationContext
 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext
 */
// {@link org.springframework.web.context.WebApplicationContext} 实现，其配置从 XML 文档中获取，
// 并由 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader} 读取。
// 这本质上相当于 Web 环境中的 {@link org.springframework.context.support.GenericXmlApplicationContext}。
//
// <p>默认情况下，根上下文的配置将从“/WEB-INF/applicationContext.xml”获取，
// 命名空间为“test-servlet”的上下文的配置将从“/WEB-INF/test-servlet.xml”获取（例如，servlet 名称为“test”的 DispatcherServlet 实例）。
//
// <p>可以通过 {@link org.springframework.web.context.ContextLoader} 的上下文参数“contextConfigLocation”
// 和 {@link org.springframework.web.servlet.FrameworkServlet} 的 servlet 初始化参数覆盖配置位置的默认值。
// 配置位置可以表示具体的文件，例如“/WEB-INF/context.xml”，也可以表示 Ant 风格的模式，
// 例如“/WEB-INF/*-context.xml”（有关模式详细信息，请参阅 {@link org.springframework.util.PathMatcher} 的 javadoc）。
//
// <p>注意：如果有多个配置位置，后面加载的 bean 定义将覆盖前面加载的文件中定义的 bean。
// 可以利用这一点，通过额外的 XML 文件故意覆盖某些 bean 定义。
//
// <p><b>对于以不同 bean 定义格式读取的 WebApplicationContext，请创建 {@link AbstractRefreshableWebApplicationContext} 的类似子类。
// </b> 此类上下文实现可以指定为 ContextLoader 的“contextClass”上下文参数或 FrameworkServlet 的“contextClass”初始化参数。
public class XmlWebApplicationContext extends AbstractRefreshableWebApplicationContext {

	/** Default config location for the root context. */
	// 根上下文的默认配置位置。
	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";

	/** Default prefix for building a config location for a namespace. */
	// 用于构建命名空间配置位置的默认前缀。
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	/** Default suffix for building a config location for a namespace. */
	// 用于构建命名空间配置位置的默认后缀。
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";


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
		// resource loading environment. --> 译文：使用此上下文的资源加载环境配置 bean 定义读取器。
		beanDefinitionReader.setEnvironment(getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions. --> 译文：允许子类提供读取器的自定义初始化，然后继续实际加载 bean 定义。
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 * @param beanDefinitionReader the bean definition reader used by this context
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setValidationMode
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	// 初始化用于加载此上下文的 bean 定义的 bean 定义读取器。默认实现为空。
	// <p>可在子类中重写，例如，关闭 XML 验证或使用不同的 XmlBeanDefinitionParser 实现。
	// @param beanDefinitionReader 此上下文使用的 bean 定义读取器
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by the refreshBeanFactory method;
	 * therefore this method is just supposed to load and/or register bean definitions.
	 * <p>Delegates to a ResourcePatternResolver for resolving location patterns
	 * into Resource instances.
	 * @throws IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	// 使用给定的 XmlBeanDefinitionReader 加载 bean 定义。
	// <p>bean 工厂的生命周期由 refreshBeanFactory 方法处理；因此，此方法仅用于加载和/或注册 bean 定义。
	// <p>委托 ResourcePatternResolver 将位置模式解析为 Resource 实例。
	// @throws IOException 如果未找到所需的 XML 文档，则抛出 @throws IOException
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (String configLocation : configLocations) {
				reader.loadBeanDefinitions(configLocation);
			}
		}
	}

	/**
	 * The default location for the root context is "/WEB-INF/applicationContext.xml",
	 * and "/WEB-INF/test-servlet.xml" for a context with the namespace "test-servlet"
	 * (like for a DispatcherServlet instance with the servlet-name "test").
	 */
	@Override
	protected String[] getDefaultConfigLocations() {
		if (getNamespace() != null) {
			return new String[] {DEFAULT_CONFIG_LOCATION_PREFIX + getNamespace() + DEFAULT_CONFIG_LOCATION_SUFFIX};
		}
		else {
			return new String[] {DEFAULT_CONFIG_LOCATION};
		}
	}

}
