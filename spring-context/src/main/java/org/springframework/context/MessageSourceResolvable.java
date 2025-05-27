/*
 * Copyright 2002-2017 the original author or authors.
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

import org.springframework.lang.Nullable;

/**
 * Interface for objects that are suitable for message resolution in a
 * {@link MessageSource}.
 *
 * <p>Spring's own validation error classes implement this interface.
 *
 * @author Juergen Hoeller
 * @see MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 * @see org.springframework.validation.ObjectError
 * @see org.springframework.validation.FieldError
 */
// 适用于 {@link MessageSource} 中消息解析的对象接口。
//
// <p>Spring 自身的验证错误类实现了此接口。
@FunctionalInterface
public interface MessageSourceResolvable {

	/**
	 * Return the codes to be used to resolve this message, in the order that
	 * they should get tried. The last code will therefore be the default one.
	 * @return a String array of codes which are associated with this message
	 */
	// 按尝试的顺序返回用于解析此消息的代码。因此，最后一个代码将是默认代码。
	// @return 与此消息关联的代码的字符串数组
	@Nullable
	String[] getCodes();

	/**
	 * Return the array of arguments to be used to resolve this message.
	 * <p>The default implementation simply returns {@code null}.
	 * @return an array of objects to be used as parameters to replace
	 * placeholders within the message text
	 * @see java.text.MessageFormat
	 */
	// 返回用于解析此消息的参数数组。
	// <p>默认实现仅返回 {@code null}。
	// @return 一个对象数组，用作参数来替换消息文本中的占位符。
	@Nullable
	default Object[] getArguments() {
		return null;
	}

	/**
	 * Return the default message to be used to resolve this message.
	 * <p>The default implementation simply returns {@code null}.
	 * Note that the default message may be identical to the primary
	 * message code ({@link #getCodes()}), which effectively enforces
	 * {@link org.springframework.context.support.AbstractMessageSource#setUseCodeAsDefaultMessage}
	 * for this particular message.
	 * @return the default message, or {@code null} if no default
	 */
	// 返回用于解析此消息的默认消息。
	// <p>默认实现仅返回 {@code null}。请注意，默认消息可能与主消息代码 ({@link #getCodes()}) 相同，
	// 这有效地强制执行此特定消息的 {@link org.springframework.context.support.AbstractMessageSource#setUseCodeAsDefaultMessage}。
	// @return 默认消息，如果没有默认消息，则返回 {@code null}
	@Nullable
	default String getDefaultMessage() {
		return null;
	}

}
