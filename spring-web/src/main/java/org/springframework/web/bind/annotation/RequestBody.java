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

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.converter.HttpMessageConverter;

/**
 * Annotation indicating a method parameter should be bound to the body of the web request.
 * The body of the request is passed through an {@link HttpMessageConverter} to resolve the
 * method argument depending on the content type of the request. Optionally, automatic
 * validation can be applied by annotating the argument with {@code @Valid}.
 *
 * <p>Supported for annotated handler methods.
 *
 * @author Arjen Poutsma
 * @since 3.0
 * @see RequestHeader
 * @see ResponseBody
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
 */
// 指示方法参数的注解应绑定到 Web 请求的主体。请求主体通过 {@link HttpMessageConverter} 传递，以根据请求的内容类型解析方法参数。
// （可选）可以使用 {@code @Valid} 注解参数来应用自动验证。
//
// <p>支持带注解的处理程序方法。
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {

	/**
	 * Whether body content is required.
	 * <p>Default is {@code true}, leading to an exception thrown in case
	 * there is no body content. Switch this to {@code false} if you prefer
	 * {@code null} to be passed when the body content is {@code null}.
	 * @since 3.2
	 */
	// 是否需要 body 内容。
	// <p>默认值为 {@code true}，如果 body 内容为空，则会抛出异常。
	// 如果您希望 body 内容为 {@code null} 时传递 {@code null}，请将此值切换为 {@code false}。
	boolean required() default true;

}
