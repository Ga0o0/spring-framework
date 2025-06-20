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

package org.springframework.transaction.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.util.ClassUtils;

/**
 * Selects which implementation of {@link AbstractTransactionManagementConfiguration}
 * should be used based on the value of {@link EnableTransactionManagement#mode} on the
 * importing {@code @Configuration} class.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableTransactionManagement
 * @see ProxyTransactionManagementConfiguration
 * @see TransactionManagementConfigUtils#TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME
 * @see TransactionManagementConfigUtils#JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME
 */
// 根据导入 {@code @Configuration} 类上的 {@link EnableTransactionManagement#mode} 的值
// 选择应使用 {@link AbstractTransactionManagementConfiguration} 的哪个实现。
public class TransactionManagementConfigurationSelector extends AdviceModeImportSelector<EnableTransactionManagement> {

	/**
	 * Returns {@link ProxyTransactionManagementConfiguration} or
	 * {@code AspectJ(Jta)TransactionManagementConfiguration} for {@code PROXY}
	 * and {@code ASPECTJ} values of {@link EnableTransactionManagement#mode()},
	 * respectively.
	 */
	// 分别为 {@link EnableTransactionManagement#mode()} 的 {@code PROXY} 和 {@code ASPECTJ} 值
	// 返回 {@link ProxyTransactionManagementConfiguration} 或 {@code AspectJ(Jta)TransactionManagementConfiguration}。
	@Override
	protected String[] selectImports(AdviceMode adviceMode) {
		return switch (adviceMode) {
			case PROXY -> new String[] {AutoProxyRegistrar.class.getName(),
					ProxyTransactionManagementConfiguration.class.getName()};
			case ASPECTJ -> new String[] {determineTransactionAspectClass()};
		};
	}

	private String determineTransactionAspectClass() {
		return (ClassUtils.isPresent("jakarta.transaction.Transactional", getClass().getClassLoader()) ?
				// JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME = org.springframework.transaction.aspectj.AspectJJtaTransactionManagementConfiguration
				TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME :
				// TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME = org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
				TransactionManagementConfigUtils.TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME);
	}

}
