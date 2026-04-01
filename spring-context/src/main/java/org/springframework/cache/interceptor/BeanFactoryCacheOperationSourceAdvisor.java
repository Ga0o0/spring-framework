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

package org.springframework.cache.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * Advisor driven by a {@link CacheOperationSource}, used to include a
 * cache advice bean for methods that are cacheable.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 * @see #setAdviceBeanName
 * @see CacheInterceptor
 */
// 由 {@link CacheOperationSource} 驱动的 Advisor，用于为可缓存的方法包含缓存建议 bean。
@SuppressWarnings("serial")
public class BeanFactoryCacheOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

	private final CacheOperationSourcePointcut pointcut = new CacheOperationSourcePointcut();


	/**
	 * Set the cache operation attribute source which is used to find cache
	 * attributes. This should usually be identical to the source reference
	 * set on the cache interceptor itself.
	 * @see CacheInterceptor#setCacheOperationSource
	 */
	// 设置用于查找缓存属性的缓存操作属性源。这通常应与缓存拦截器本身设置的源引用相同。
	public void setCacheOperationSource(CacheOperationSource cacheOperationSource) {
		this.pointcut.setCacheOperationSource(cacheOperationSource);
	}

	/**
	 * Set the {@link ClassFilter} to use for this pointcut.
	 * Default is {@link ClassFilter#TRUE}.
	 */
	// 设置此切入点要使用的 {@link ClassFilter}。默认值为 {@link ClassFilter#TRUE}。
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
