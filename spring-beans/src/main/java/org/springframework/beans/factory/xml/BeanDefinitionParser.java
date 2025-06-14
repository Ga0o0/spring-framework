/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom,
 * top-level (directly under {@code <beans/>}) tags.
 *
 * <p>Implementations are free to turn the metadata in the custom tag into as many
 * {@link BeanDefinition BeanDefinitions} as required.
 *
 * <p>The parser locates a {@link BeanDefinitionParser} from the associated
 * {@link NamespaceHandler} for the namespace in which the custom tag resides.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
// {@link DefaultBeanDefinitionDocumentReader} 使用的接口，用于处理自定义的顶级（直接位于 {@code <beans/>} 下）标签。
//
// <p>实现可以根据需要将自定义标签中的元数据转换为任意数量的 {@link BeanDefinition BeanDefinitions}。
//
// <p>解析器从关联的 {@link NamespaceHandler} 中找到自定义标签所在命名空间的 {@link BeanDefinitionParser}。
public interface BeanDefinitionParser {

	/**
	 * Parse the specified {@link Element} and register the resulting
	 * {@link BeanDefinition BeanDefinition(s)} with the
	 * {@link org.springframework.beans.factory.xml.ParserContext#getRegistry() BeanDefinitionRegistry}
	 * embedded in the supplied {@link ParserContext}.
	 * <p>Implementations must return the primary {@link BeanDefinition} that results
	 * from the parse if they will ever be used in a nested fashion (for example as
	 * an inner tag in a {@code <property/>} tag). Implementations may return
	 * {@code null} if they will <strong>not</strong> be used in a nested fashion.
	 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return the primary {@link BeanDefinition}
	 */
	// 解析指定的 {@link Element} 并使用嵌入在提供的 {@link ParserContext} 中的
	// {@link org.springframework.beans.factory.xml.ParserContext#getRegistry() BeanDefinitionRegistry}
	// 注册生成的 {@link BeanDefinition BeanDefinition(s)}。
	// <p>如果实现将以嵌套方式使用（例如，作为 {@code <property/>} 标签中的内部标签），
	// 则必须返回解析生成的主 {@link BeanDefinition}。如果实现<strong>不</strong>以嵌套方式使用，则可能返回 {@code null}。
	// @param element 要解析为一个或多个 {@link BeanDefinition BeanDefinitions} 的元素
	// @param parserContext 封装解析过程当前状态的对象；提供对 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 的访问
	// @return 主要 {@link BeanDefinition}
	@Nullable
	BeanDefinition parse(Element element, ParserContext parserContext);

}
