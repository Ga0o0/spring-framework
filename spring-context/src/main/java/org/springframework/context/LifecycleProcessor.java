/*
 * Copyright 2002-2009 the original author or authors.
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

/**
 * Strategy interface for processing Lifecycle beans within the ApplicationContext.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
// 用于处理 ApplicationContext 中的生命周期 bean 的策略接口。
public interface LifecycleProcessor extends Lifecycle {

	/**
	 * Notification of context refresh, e.g. for auto-starting components.
	 */
	// 上下文刷新通知，例如用于自动启动的组件。
	void onRefresh();

	/**
	 * Notification of context close phase, e.g. for auto-stopping components.
	 */
	// 上下文关闭阶段通知，例如用于自动停止的组件。
	void onClose();

}
