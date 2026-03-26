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

package org.springframework.aop.aspectj;

import java.util.Objects;

import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;

import org.springframework.aop.ClassFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Spring AOP {@link ClassFilter} implementation using AspectJ type matching.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.0
 */
// 使用 AspectJ 类型匹配实现 Spring AOP {@link ClassFilter}。
public class TypePatternClassFilter implements ClassFilter {

	private String typePattern = "";

	@Nullable
	private TypePatternMatcher aspectJTypePatternMatcher;


	/**
	 * Creates a new instance of the {@link TypePatternClassFilter} class.
	 * <p>This is the JavaBean constructor; be sure to set the
	 * {@link #setTypePattern(String) typePattern} property, else a
	 * no doubt fatal {@link IllegalStateException} will be thrown
	 * when the {@link #matches(Class)} method is first invoked.
	 */
	public TypePatternClassFilter() {
	}

	/**
	 * Create a fully configured {@link TypePatternClassFilter} using the
	 * given type pattern.
	 * @param typePattern the type pattern that AspectJ weaver should parse
	 */
	// 使用给定的类型模式创建完全配置的 {@link TypePatternClassFilter}。
	// @param typePattern AspectJ 织入器应解析的类型模式
	public TypePatternClassFilter(String typePattern) {
		setTypePattern(typePattern);
	}


	/**
	 * Set the AspectJ type pattern to match.
	 * <p>Examples include:
	 * <code class="code">
	 * org.springframework.beans.*
	 * </code>
	 * This will match any class or interface in the given package.
	 * <code class="code">
	 * org.springframework.beans.ITestBean+
	 * </code>
	 * This will match the {@code ITestBean} interface and any class
	 * that implements it.
	 * <p>These conventions are established by AspectJ, not Spring AOP.
	 * @param typePattern the type pattern that AspectJ weaver should parse
	 */
	// 设置要匹配的 AspectJ 类型模式。
	//
	// <p>示例包括：
	//
	// <code class="code">
	// org.springframework.beans.*
	// </code>
	// 这将匹配给定包中的任何类或接口。
	//
	// <code class="code">
	// org.springframework.beans.ITestBean+
	// </code>
	// 这将匹配 {@code ITestBean} 接口以及任何实现该接口的类。
	//
	// <p>这些约定由 AspectJ 制定，而非 Spring AOP。
	// @param typePattern AspectJ 织入器应解析的类型模式
	public void setTypePattern(String typePattern) {
		Assert.notNull(typePattern, "Type pattern must not be null");
		this.typePattern = typePattern;
		this.aspectJTypePatternMatcher =
				PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().
				parseTypePattern(replaceBooleanOperators(typePattern));
	}

	/**
	 * Return the AspectJ type pattern to match.
	 */
	public String getTypePattern() {
		return this.typePattern;
	}


	/**
	 * Should the pointcut apply to the given interface or target class?
	 * @param clazz candidate target class
	 * @return whether the advice should apply to this candidate target class
	 * @throws IllegalStateException if no {@link #setTypePattern(String)} has been set
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		Assert.state(this.aspectJTypePatternMatcher != null, "No type pattern has been set");
		return this.aspectJTypePatternMatcher.matches(clazz);
	}

	/**
	 * If a type pattern has been specified in XML, the user cannot
	 * write {@code and} as "&&" (though &amp;&amp; will work).
	 * We also allow {@code and} between two sub-expressions.
	 * <p>This method converts back to {@code &&} for the AspectJ pointcut parser.
	 */
	// 如果 XML 中指定了类型模式，用户不能将 {@code and} 写成 "&&"（尽管 &amp;&amp; 可以）。我们也允许在两个子表达式之间使用 {@code and}。
	//
	// <p>此方法会将 AspectJ 切入点解析器转换回 {@code &&}。</p>
	private String replaceBooleanOperators(String pcExpr) {
		String result = StringUtils.replace(pcExpr," and "," && ");
		result = StringUtils.replace(result, " or ", " || ");
		return StringUtils.replace(result, " not ", " ! ");
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof TypePatternClassFilter that &&
				ObjectUtils.nullSafeEquals(this.typePattern, that.typePattern)));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.typePattern);
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.typePattern;
	}

}
