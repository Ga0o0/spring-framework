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

package org.springframework.aop.target;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * {@link org.springframework.aop.TargetSource} that lazily accesses a
 * singleton bean from a {@link org.springframework.beans.factory.BeanFactory}.
 *
 * <p>Useful when a proxy reference is needed on initialization but
 * the actual target object should not be initialized until first use.
 * When the target bean is defined in an
 * {@link org.springframework.context.ApplicationContext} (or a
 * {@code BeanFactory} that is eagerly pre-instantiating singleton beans)
 * it must be marked as "lazy-init" too, else it will be instantiated by said
 * {@code ApplicationContext} (or {@code BeanFactory}) on startup.
 * <p>For example:
 *
 * <pre class="code">
 * &lt;bean id="serviceTarget" class="example.MyService" lazy-init="true"&gt;
 *   ...
 * &lt;/bean&gt;
 *
 * &lt;bean id="service" class="org.springframework.aop.framework.ProxyFactoryBean"&gt;
 *   &lt;property name="targetSource"&gt;
 *     &lt;bean class="org.springframework.aop.target.LazyInitTargetSource"&gt;
 *       &lt;property name="targetBeanName"&gt;&lt;idref local="serviceTarget"/&gt;&lt;/property&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * The "serviceTarget" bean will not get initialized until a method on the
 * "service" proxy gets invoked.
 *
 * <p>Subclasses can extend this class and override the {@link #postProcessTargetObject(Object)} to
 * perform some additional processing with the target object when it is first loaded.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see #postProcessTargetObject
 */
// {@link org.springframework.aop.TargetSource} 以惰性方式从
// {@link org.springframework.beans.factory.BeanFactory} 访问单例 bean。
//
// <p>当初始化时需要代理引用，但实际目标对象直到第一次使用才初始化时，此方法非常有用。
// 当目标 bean 定义在 {@link org.springframework.context.ApplicationContext}
// （或积极预实例化单例 bean 的 {@code BeanFactory}）中时，也必须将其标记为“lazy-init”，
// 否则它将在启动时由上述 {@code ApplicationContext}（或 {@code BeanFactory}）实例化。
//
// <p>例如：
// <pre class="code">
// <bean id="serviceTarget" class="example.MyService" lazy-init="true"> ... </bean>
//
// <bean id="service" class="org.springframework.aop.framework.ProxyFactoryBean">
// 		<property name="targetSource">
// 			<bean class="org.springframework.aop.target.LazyInitTargetSource">
// 				<property name="targetBeanName"><idref local="serviceTarget"/></property>
// 			</bean>
// 		</property>
// </bean>
// </pre>
//
// 直到调用“service”代理上的方法时，“serviceTarget”bean才会初始化。
//
// <p>子类可以扩展此类并重写 {@link #postProcessTargetObject(Object)} 以在首次加载目标对象时执行一些额外的处理。
@SuppressWarnings("serial")
public class LazyInitTargetSource extends AbstractBeanFactoryBasedTargetSource {

	@Nullable
	private Object target;


	@Override
	public synchronized Object getTarget() throws BeansException {
		if (this.target == null) {
			this.target = getBeanFactory().getBean(getTargetBeanName());
			postProcessTargetObject(this.target);
		}
		return this.target;
	}

	/**
	 * Subclasses may override this method to perform additional processing on
	 * the target object when it is first loaded.
	 * @param targetObject the target object that has just been instantiated (and configured)
	 */
	protected void postProcessTargetObject(Object targetObject) {
	}

}
