package org.springframework.sample.aop.code_analysis.schema_based;

import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 解析 <aop:advice/> 标签
 *
 * @see CodeAnalysis01_ConfigBeanDefinitionParser_parse
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAspect(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 *
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parseAdvice(java.lang.String, int, org.w3c.dom.Element, org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, java.util.List, java.util.List)
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#createAdviceDefinition(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, java.lang.String, int, org.springframework.beans.factory.support.RootBeanDefinition, org.springframework.beans.factory.support.RootBeanDefinition, java.util.List, java.util.List)
 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#getAdviceClass(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
 */
public class CodeAnalysis02_ConfigBeanDefinitionParser_getAdviceClass {

	/**
	 * BeanDefinitionParser 用于 <aop:config> 标签
	 *
	 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#getAdviceClass(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	static class ConfigBeanDefinitionParser extends CodeAnalysis01_ConfigBeanDefinitionParser_parse.CA01_ConfigBeanDefinitionParser {
		// static class CA01_ConfigBeanDefinitionParser extends org.springframework.aop.config.ConfigBeanDefinitionParser {
		private static final String BEFORE = "before";
		private static final String AFTER = "after";
		private static final String AFTER_RETURNING_ELEMENT = "after-returning";
		private static final String AFTER_THROWING_ELEMENT = "after-throwing";
		private static final String AROUND = "around";

		// 获取与提供的 {@link Element} 对应的 advice 实现类。
		private Class<?> getAdviceClass(Element adviceElement, ParserContext parserContext) {
			String elementName = parserContext.getDelegate().getLocalName(adviceElement);
			return switch (elementName) { // important -> go
				case BEFORE -> AspectJMethodBeforeAdvice.class;
				case AFTER -> AspectJAfterAdvice.class;
				case AFTER_RETURNING_ELEMENT -> AspectJAfterReturningAdvice.class;
				case AFTER_THROWING_ELEMENT -> AspectJAfterThrowingAdvice.class;
				case AROUND -> AspectJAroundAdvice.class;
				default -> throw new IllegalArgumentException("Unknown advice kind [" + elementName + "].");
			};
		}
	}

}
