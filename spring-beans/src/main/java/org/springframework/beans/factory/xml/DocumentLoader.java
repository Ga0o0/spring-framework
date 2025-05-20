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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * Strategy interface for loading an XML {@link Document}.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see DefaultDocumentLoader
 */
// 用于加载 XML {@link Document} 的策略接口。
public interface DocumentLoader {

	/**
	 * Load a {@link Document document} from the supplied {@link InputSource source}.
	 * @param inputSource the source of the document that is to be loaded
	 * @param entityResolver the resolver that is to be used to resolve any entities
	 * @param errorHandler used to report any errors during document loading
	 * @param validationMode the type of validation
	 * {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_DTD DTD}
	 * or {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_XSD XSD})
	 * @param namespaceAware {@code true} if support for XML namespaces is to be provided
	 * @return the loaded {@link Document document}
	 * @throws Exception if an error occurs
	 */
	// 从提供的 {@link InputSource 源} 加载一个 {@link Document 文档}。
	// @param inputSource 待加载文档的源
	// @param entityResolver 用于解析实体的解析器
	// @param errorHandler 用于报告文档加载过程中的错误
	// @param validationMode 验证类型 {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_DTD DTD} 或 {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_XSD XSD})
	// @param namespaceAware {@code true} 表示需要提供对 XML 命名空间的支持
	// @return 已加载的 {@link Document 文档}
	// @throws Exception 表示发生错误
	Document loadDocument(
			InputSource inputSource, EntityResolver entityResolver,
			ErrorHandler errorHandler, int validationMode, boolean namespaceAware)
			throws Exception;

}
