/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.expression;

/**
 * Parses expression strings into compiled expressions that can be evaluated.
 *
 * <p>Supports parsing template expressions as well as standard expression strings.
 *
 * @author Keith Donald
 * @author Andy Clement
 * @since 3.0
 */
// 将表达式字符串解析为可求值的编译表达式。
//
// <p>支持解析模板表达式以及标准表达式字符串。
public interface ExpressionParser {

	/**
	 * Parse the expression string and return an {@link Expression} object that
	 * can be used for repeated evaluation.
	 * <p>Examples:
	 * <pre class="code">
	 *     3 + 4
	 *     name.firstName
	 * </pre>
	 * @param expressionString the raw expression string to parse
	 * @return an {@code Expression} for the parsed expression
	 * @throws ParseException if an exception occurred during parsing
	 */
	// 解析表达式字符串并返回一个可用于重复求值的 {@link Expression} 对象。
	// <p>示例：
	// <pre class="code">
	// 		3 + 4
	// 		name.firstName
	// </pre>
	// @param ExpressionString 待解析的原始表达式字符串
	// @return 解析后的表达式的 {@code Expression}
	// @throws ParseException 如果解析过程中发生异常
	Expression parseExpression(String expressionString) throws ParseException;

	/**
	 * Parse the expression string and return an {@link Expression} object that
	 * can be used for repeated evaluation.
	 * <p>Examples:
	 * <pre class="code">
	 *     3 + 4
	 *     name.firstName
	 * </pre>
	 * @param expressionString the raw expression string to parse
	 * @param context a context for influencing the expression parsing routine
	 * @return an {@code Expression} for the parsed expression
	 * @throws ParseException if an exception occurred during parsing
	 */
	// 解析表达式字符串并返回一个可用于重复求值的 {@link Expression} 对象。
	// <p>示例：
	// <pre class="code">
	// 		3 + 4
	// 		name.firstName
	// </pre>
	// @param ExpressionString 待解析的原始表达式字符串
	// @param context 影响表达式解析过程的上下文
	// @return 已解析表达式的 {@code Expression}
	// @throws ParseException 如果解析过程中发生异常
	Expression parseExpression(String expressionString, ParserContext context) throws ParseException;

}
