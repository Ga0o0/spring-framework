/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * Strategy interface for resolving messages, with support for the parameterization
 * and internationalization of such messages.
 *
 * <p>Spring provides two out-of-the-box implementations for production:
 * <ul>
 * <li>{@link org.springframework.context.support.ResourceBundleMessageSource}: built
 * on top of the standard {@link java.util.ResourceBundle}, sharing its limitations.
 * <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource}:
 * highly configurable, in particular with respect to reloading message definitions.
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.support.ResourceBundleMessageSource
 * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
 */
// 用于解析消息的策略接口，支持消息的参数化和国际化。
//
// <p>Spring 提供了两种开箱即用的生产环境实现：
// <ul>
// <li>{@link org.springframework.context.support.ResourceBundleMessageSource}：构建于标准 {@link java.util.ResourceBundle} 之上，并具有相同的局限性。
// <li>{@link org.springframework.context.support.ReloadableResourceBundleMessageSource}：高度可配置，尤其是在重新加载消息定义方面。
// </ul>
public interface MessageSource {

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 * @param code the message code to look up, e.g. 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param defaultMessage a default message to return if the lookup fails
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message if the lookup was successful, otherwise
	 * the default message passed as a parameter (which may be {@code null})
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 */
	// 尝试解析消息。如果未找到消息，则返回默认消息。
	// @param code 需要查找的消息代码，例如“calculator.noRateSet”。建议 MessageSource 用户使用限定的类或包名称来命名消息，以避免潜在的冲突并确保最大程度的清晰度。
	// @param args 参数数组，用于填充消息中的参数（参数在消息中类似于“{0}”、“{1,date}”、“{2,time}”），如果没有参数，则返回 {@code null}。
	// @param defaultMessage 查找失败时返回的默认消息。
	// @param locale 执行查找的语言环境。
	// @return 如果查找成功，则返回解析后的消息；否则，返回作为参数传递的默认消息（可能为 {@code null}）。
	@Nullable
	String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale);

	/**
	 * Try to resolve the message. Treat as an error if the message can't be found.
	 * @param code the message code to look up, e.g. 'calculator.noRateSet'.
	 * MessageSource users are encouraged to base message names on qualified class
	 * or package names, avoiding potential conflicts and ensuring maximum clarity.
	 * @param args an array of arguments that will be filled in for params within
	 * the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 * or {@code null} if none
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null})
	 * @throws NoSuchMessageException if no corresponding message was found
	 * @see #getMessage(MessageSourceResolvable, Locale)
	 * @see java.text.MessageFormat
	 */
	// 尝试解析该消息。如果找不到该消息，则视为错误。
	// @param code 要查找的消息代码，例如“calculator.noRateSet”。建议 MessageSource 用户根据限定的类或包名称来命名消息，以避免潜在的冲突并确保最大程度的清晰度。
	// @param args 参数数组，用于填充消息中的参数（参数在消息中类似于“{0}”、“{1,date}”、“{2,time}”），如果没有，则返回 {@code null}
	// @param locale 进行查找的语言环境
	// @return 已解析的消息（从不返回 {@code null}）
	// @throws NoSuchMessageException 如果未找到相应的消息，则抛出 NoSuchMessageException
	String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * Try to resolve the message using all the attributes contained within the
	 * {@code MessageSourceResolvable} argument that was passed in.
	 * <p>NOTE: We must throw a {@code NoSuchMessageException} on this method
	 * since at the time of calling this method we aren't able to determine if the
	 * {@code defaultMessage} property of the resolvable is {@code null} or not.
	 * @param resolvable the value object storing attributes required to resolve a message
	 * (may include a default message)
	 * @param locale the locale in which to do the lookup
	 * @return the resolved message (never {@code null} since even a
	 * {@code MessageSourceResolvable}-provided default message needs to be non-null)
	 * @throws NoSuchMessageException if no corresponding message was found
	 * (and no default message was provided by the {@code MessageSourceResolvable})
	 * @see MessageSourceResolvable#getCodes()
	 * @see MessageSourceResolvable#getArguments()
	 * @see MessageSourceResolvable#getDefaultMessage()
	 * @see java.text.MessageFormat
	 */
	// 尝试使用传入的 {@code MessageSourceResolvable} 参数中包含的所有属性来解析消息。
	// <p>注意：此方法必须抛出 {@code NoSuchMessageException}，因为调用此方法时我们无法确定
	// {@code defaultMessage} 属性是否为 {@code null}。
	// @param resolvable 存储解析消息所需属性的值对象（可能包含默认消息）
	// @param locale 进行查找的语言环境
	// @return 已解析的消息（永远不会为 {@code null}，因为即使是 {@code MessageSourceResolvable} 提供的默认消息也必须为非空）
	// @throws NoSuchMessageException 如果未找到相应的消息（并且 {@code MessageSourceResolvable} 未提供默认消息），则抛出 NoSuchMessageException
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
