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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Abstract {@link BeanDefinitionParser} implementation providing
 * a number of convenience methods and a
 * {@link AbstractBeanDefinitionParser#parseInternal template method}
 * that subclasses must override to provide the actual parsing logic.
 *
 * <p>Use this {@link BeanDefinitionParser} implementation when you want
 * to parse some arbitrarily complex XML into one or more
 * {@link BeanDefinition BeanDefinitions}. If you just want to parse some
 * XML into a single {@code BeanDefinition}, you may wish to consider
 * the simpler convenience extensions of this class, namely
 * {@link AbstractSingleBeanDefinitionParser} and
 * {@link AbstractSimpleBeanDefinitionParser}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Dave Syer
 * @since 2.0
 */
// 抽象 {@link BeanDefinitionParser} 实现提供了许多便捷方法和一个
// {@link AbstractBeanDefinitionParser#parseInternal 模板方法}，子类必须重写这些方法才能提供实际的解析逻辑。
//
// <p>如果您想要将任意复杂的 XML 解析为一个或多个 {@link BeanDefinition BeanDefinitions}，请使用此 {@link BeanDefinitionParser} 实现。
// 如果您只想将一些 XML 解析为单个 {@code BeanDefinition}，则不妨考虑此类更简单的便捷扩展，
// 即 {@link AbstractSingleBeanDefinitionParser} 和 {@link AbstractSimpleBeanDefinitionParser}。
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

	/** Constant for the "id" attribute. */
	public static final String ID_ATTRIBUTE = "id";

	/** Constant for the "name" attribute. */
	public static final String NAME_ATTRIBUTE = "name";


	@Override
	@Nullable
	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		// 中心模板方法，用于将提供的 Element 解析为一个或多个 BeanDefinition。
		AbstractBeanDefinition definition = parseInternal(element, parserContext);
		if (definition != null && !parserContext.isNested()) {
			try {
				// 解析提供的 BeanDefinition 的 ID。
				String id = resolveId(element, definition, parserContext);
				if (!StringUtils.hasText(id)) {
					parserContext.getReaderContext().error(
							"Id is required for element '" + parserContext.getDelegate().getLocalName(element)
									+ "' when used as a top-level tag", element);
				}
				String[] aliases = null;
				// 确定元素的 “name” 属性是否应解析为 Bean 定义别名，即替代的 Bean 定义名称。
				if (shouldParseNameAsAliases()) {
					String name = element.getAttribute(NAME_ATTRIBUTE);
					if (StringUtils.hasLength(name)) {
						aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
					}
				}
				BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
				registerBeanDefinition(holder, parserContext.getRegistry());
				if (shouldFireEvents()) {
					BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
					postProcessComponentDefinition(componentDefinition);
					parserContext.registerComponent(componentDefinition);
				}
			}
			catch (BeanDefinitionStoreException ex) {
				String msg = ex.getMessage();
				parserContext.getReaderContext().error((msg != null ? msg : ex.toString()), element);
				return null;
			}
		}
		return definition;
	}

	/**
	 * Resolve the ID for the supplied {@link BeanDefinition}.
	 * <p>When using {@link #shouldGenerateId generation}, a name is generated automatically.
	 * Otherwise, the ID is extracted from the "id" attribute, potentially with a
	 * {@link #shouldGenerateIdAsFallback() fallback} to a generated id.
	 * @param element the element that the bean definition has been built from
	 * @param definition the bean definition to be registered
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return the resolved id
	 * @throws BeanDefinitionStoreException if no unique name could be generated
	 * for the given bean definition
	 */
	// 解析提供的 {@link BeanDefinition} 的 ID。
	// <p>使用 {@link #shouldGenerateId generation} 时，会自动生成名称。
	// 否则，会从“id”属性中提取 ID，并可能使用 {@link #shouldGenerateIdAsFallback() fallback} 回退到生成的 ID。
	// @param element 构建 bean 定义的元素
	// @param definition 需要注册的 bean 定义
	// @param parserContext 封装解析过程当前状态的对象；提供对 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 的访问
	// @return 解析后的 ID
	// @throws BeanDefinitionStoreException 如果无法为给定的 bean 定义生成唯一名称
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		// 是否应该生成 ID，而不是从传入的 Element 中读取？
		if (shouldGenerateId()) {
			return parserContext.getReaderContext().generateBeanName(definition);
		}
		else {
			String id = element.getAttribute(ID_ATTRIBUTE);
			if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
				id = parserContext.getReaderContext().generateBeanName(definition);
			}
			return id;
		}
	}

	/**
	 * Register the supplied {@link BeanDefinitionHolder bean} with the supplied
	 * {@link BeanDefinitionRegistry registry}.
	 * <p>Subclasses can override this method to control whether the supplied
	 * {@link BeanDefinitionHolder bean} is actually even registered, or to
	 * register even more beans.
	 * <p>The default implementation registers the supplied {@link BeanDefinitionHolder bean}
	 * with the supplied {@link BeanDefinitionRegistry registry} only if the {@code isNested}
	 * parameter is {@code false}, because one typically does not want inner beans
	 * to be registered as top level beans.
	 * @param definition the bean definition to be registered
	 * @param registry the registry that the bean is to be registered with
	 * @see BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)
	 */
	// 使用提供的 {@link BeanDefinitionRegistry registry} 注册提供的 {@link BeanDefinitionHolder bean}。
	// <p>子类可以覆盖此方法来控制提供的 {@link BeanDefinitionHolder bean} 是否实际注册，或者注册更多 bean。
	// <p>默认实现仅在 {@code isNested} 参数为 {@code false} 时才使用提供的
	// {@link BeanDefinitionRegistry registry} 注册提供的 {@link BeanDefinitionHolder bean}，因为通常不希望将内部 bean 注册为顶级 bean。
	// @param definition 要注册的 bean 定义
	// @param registry 要注册 bean 的注册表
	protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
		// 将指定的 bean 定义注册到指定的 bean 工厂。
		BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
	}


	/**
	 * Central template method to actually parse the supplied {@link Element}
	 * into one or more {@link BeanDefinition BeanDefinitions}.
	 * @param element the element that is to be parsed into one or more {@link BeanDefinition BeanDefinitions}
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return the primary {@link BeanDefinition} resulting from the parsing of the supplied {@link Element}
	 * @see #parse(org.w3c.dom.Element, ParserContext)
	 * @see #postProcessComponentDefinition(org.springframework.beans.factory.parsing.BeanComponentDefinition)
	 */
	// 中心模板方法，用于将提供的 {@link Element} 解析为一个或多个 {@link BeanDefinition BeanDefinitions}。
	// @param element 需要解析为一个或多个 {@link BeanDefinition BeanDefinitions} 的元素
	// @param parserContext 封装解析过程当前状态的对象；提供对 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 的访问
	// @return 解析提供的 {@link Element} 后生成的主 {@link BeanDefinition}
	@Nullable
	protected abstract AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext);

	/**
	 * Should an ID be generated instead of read from the passed in {@link Element}?
	 * <p>Disabled by default; subclasses can override this to enable ID generation.
	 * Note that this flag is about <i>always</i> generating an ID; the parser
	 * won't even check for an "id" attribute in this case.
	 * @return whether the parser should always generate an id
	 */
	// 是否应该生成 ID，而不是从传入的 {@link Element} 中读取？
	// <p>默认禁用；子类可以重写此标记以启用 ID 生成。
	// 请注意，此标志表示<i>始终</i>生成 ID；在这种情况下，解析器甚至不会检查“id”属性。
	// @return 解析器是否应始终生成 ID
	protected boolean shouldGenerateId() {
		return false;
	}

	/**
	 * Should an ID be generated instead if the passed in {@link Element} does not
	 * specify an "id" attribute explicitly?
	 * <p>Disabled by default; subclasses can override this to enable ID generation
	 * as fallback: The parser will first check for an "id" attribute in this case,
	 * only falling back to a generated ID if no value was specified.
	 * @return whether the parser should generate an id if no id was specified
	 */
	protected boolean shouldGenerateIdAsFallback() {
		return false;
	}

	/**
	 * Determine whether the element's "name" attribute should get parsed as
	 * bean definition aliases, i.e. alternative bean definition names.
	 * <p>The default implementation returns {@code true}.
	 * @return whether the parser should evaluate the "name" attribute as aliases
	 * @since 4.1.5
	 */
	// 确定元素的“name”属性是否应解析为 Bean 定义别名，即替代的 Bean 定义名称。
	// <p>默认实现返回 {@code true}。
	// @return 解析器是否应将“name”属性评估为别名
	protected boolean shouldParseNameAsAliases() {
		return true;
	}

	/**
	 * Determine whether this parser is supposed to fire a
	 * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition}
	 * event after parsing the bean definition.
	 * <p>This implementation returns {@code true} by default; that is,
	 * an event will be fired when a bean definition has been completely parsed.
	 * Override this to return {@code false} in order to suppress the event.
	 * @return {@code true} in order to fire a component registration event
	 * after parsing the bean definition; {@code false} to suppress the event
	 * @see #postProcessComponentDefinition
	 * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
	 */
	protected boolean shouldFireEvents() {
		return true;
	}

	/**
	 * Hook method called after the primary parsing of a
	 * {@link BeanComponentDefinition} but before the
	 * {@link BeanComponentDefinition} has been registered with a
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 * <p>Derived classes can override this method to supply any custom logic that
	 * is to be executed after all the parsing is finished.
	 * <p>The default implementation is a no-op.
	 * @param componentDefinition the {@link BeanComponentDefinition} that is to be processed
	 */
	protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
	}

}
