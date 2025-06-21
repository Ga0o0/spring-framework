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

package org.springframework.beans.factory.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * Support base class for singleton registries which need to handle
 * {@link org.springframework.beans.factory.FactoryBean} instances,
 * integrated with {@link DefaultSingletonBeanRegistry}'s singleton management.
 *
 * <p>Serves as base class for {@link AbstractBeanFactory}.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 */
// 支持需要处理 {@link org.springframework.beans.factory.FactoryBean} 实例的单例注册中心的基类，
// 并与 {@link DefaultSingletonBeanRegistry} 的单例管理集成。
//
// <p>作为 {@link AbstractBeanFactory} 的基类。
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	/** Cache of singleton objects created by FactoryBeans: FactoryBean name to object. */
	// FactoryBeans 创建的单例对象的缓存：FactoryBean 名称到对象。
	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);


	/**
	 * Determine the type for the given FactoryBean.
	 * @param factoryBean the FactoryBean instance to check
	 * @return the FactoryBean's object type,
	 * or {@code null} if the type cannot be determined yet
	 */
	@Nullable
	protected Class<?> getTypeForFactoryBean(FactoryBean<?> factoryBean) {
		try {
			return factoryBean.getObjectType();
		}
		catch (Throwable ex) {
			// Thrown from the FactoryBean's getObjectType implementation.
			logger.info("FactoryBean threw exception from getObjectType, despite the contract saying " +
					"that it should return null if the type of its object cannot be determined yet", ex);
			return null;
		}
	}

	/**
	 * Determine the bean type for a FactoryBean by inspecting its attributes for a
	 * {@link FactoryBean#OBJECT_TYPE_ATTRIBUTE} value.
	 * @param attributes the attributes to inspect
	 * @return a {@link ResolvableType} extracted from the attributes or
	 * {@code ResolvableType.NONE}
	 * @since 5.2
	 */
	ResolvableType getTypeForFactoryBeanFromAttributes(AttributeAccessor attributes) {
		Object attribute = attributes.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
		if (attribute == null) {
			return ResolvableType.NONE;
		}
		if (attribute instanceof ResolvableType resolvableType) {
			return resolvableType;
		}
		if (attribute instanceof Class<?> clazz) {
			return ResolvableType.forClass(clazz);
		}
		throw new IllegalArgumentException("Invalid value type for attribute '" +
				FactoryBean.OBJECT_TYPE_ATTRIBUTE + "': " + attribute.getClass().getName());
	}

	/**
	 * Determine the FactoryBean object type from the given generic declaration.
	 * @param type the FactoryBean type
	 * @return the nested object type, or {@code NONE} if not resolvable
	 */
	ResolvableType getFactoryBeanGeneric(@Nullable ResolvableType type) {
		return (type != null ? type.as(FactoryBean.class).getGeneric() : ResolvableType.NONE);
	}

	/**
	 * Obtain an object to expose from the given FactoryBean, if available
	 * in cached form. Quick check for minimal synchronization.
	 * @param beanName the name of the bean
	 * @return the object obtained from the FactoryBean,
	 * or {@code null} if not available
	 */
	// 从给定的 FactoryBean 中获取要公开的对象（如果缓存中存在）。快速检查以实现最小同步。
	// @param beanName bean 的名称
	// @return 从 FactoryBean 获取的对象，如果不可用则返回 {@code null}
	@Nullable
	protected Object getCachedObjectForFactoryBean(String beanName) {
		return this.factoryBeanObjectCache.get(beanName);
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @param shouldPostProcess whether the bean is subject to post-processing
	 * @return the object obtained from the FactoryBean
	 * @throws BeanCreationException if FactoryBean object creation failed
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	// 从给定的 FactoryBean 获取要公开的对象。
	// @param factory FactoryBean 实例
	// @param beanName bean 的名称
	// @param shouldPostProcess bean 是否需要进行后处理
	// @return 从 FactoryBean 获取的对象
	// @throws BeanCreationException 如果 FactoryBean 对象创建失败
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
		// 单例模式，先从缓冲取值，再调用 FactoryBean#getObject() 方法获取返回值，如果返回值为null，就返回 NullBean 对象
		// 非单例模式，直接调用 FactoryBean#getObject() 方法获取返回值，如果返回值为null，就返回 NullBean 对象
		// 对象获取到了，再对 FactoryBean 创建的 Object 进行后处理
		if (factory.isSingleton() && containsSingleton(beanName)) {
			synchronized (getSingletonMutex()) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if (object == null) {
					// 调用 FactoryBean#getObject() 方法获取返回值，如果返回值为null，就返回 NullBean 对象
					object = doGetObjectFromFactoryBean(factory, beanName);
					// Only post-process and store if not put there already during getObject() call above
					// (e.g. because of circular reference processing triggered by custom getBean calls)
					// --> 译文：仅当上述 getObject() 调用期间尚未将其放入时才进行后处理和存储（例如，由于自定义 getBean 调用触发的循环引用处理）。
					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
					if (alreadyThere != null) {
						object = alreadyThere;
					}
					else {
						if (shouldPostProcess) { // 需要进行后处理
							if (isSingletonCurrentlyInCreation(beanName)) { // 单例 bean 正在创建中
								// Temporarily return non-post-processed object, not storing it yet..
								// --> 译文：暂时返回未经后处理的对象，暂不存储。
								return object;
							}
							beforeSingletonCreation(beanName); // 单例创建前的回调
							try {
								// 对 FactoryBean 进行后处理，即：调用 BeanPostProcessor.postProcessAfterInitialization()
								object = postProcessObjectFromFactoryBean(object, beanName);
							}
							catch (Throwable ex) {
								// FactoryBean 单例对象的后处理失败
								throw new BeanCreationException(beanName,
										"Post-processing of FactoryBean's singleton object failed", ex);
							}
							finally {
								afterSingletonCreation(beanName); // 单例创建后的回调
							}
						}
						if (containsSingleton(beanName)) {
							this.factoryBeanObjectCache.put(beanName, object);
						}
					}
				}
				return object;
			}
		}
		else {
			Object object = doGetObjectFromFactoryBean(factory, beanName);
			if (shouldPostProcess) {
				try {
					object = postProcessObjectFromFactoryBean(object, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
				}
			}
			return object;
		}
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @return the object obtained from the FactoryBean
	 * @throws BeanCreationException if FactoryBean object creation failed
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	// 从给定的 FactoryBean 获取要公开的对象。
	// @param factory FactoryBean 实例
	// @param beanName bean 的名称
	// @return 从 FactoryBean 获取的对象
	// @throws BeanCreationException 如果 FactoryBean 对象创建失败
	private Object doGetObjectFromFactoryBean(FactoryBean<?> factory, String beanName) throws BeanCreationException {
		Object object;
		try {
			object = factory.getObject();
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			// FactoryBean 在创建对象时抛出异常
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}

		// Do not accept a null value for a FactoryBean that's not fully
		// initialized yet: Many FactoryBeans just return null then.
		// --> 译文：不要接受尚未完全初始化的 FactoryBean 的空值：很多 FactoryBean 在这种情况下会直接返回 null。
		if (object == null) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(
						beanName, "FactoryBean which is currently in creation returned null from getObject");
			}
			object = new NullBean();
		}
		return object;
	}

	/**
	 * Post-process the given object that has been obtained from the FactoryBean.
	 * The resulting object will get exposed for bean references.
	 * <p>The default implementation simply returns the given object as-is.
	 * Subclasses may override this, for example, to apply post-processors.
	 * @param object the object obtained from the FactoryBean.
	 * @param beanName the name of the bean
	 * @return the object to expose
	 * @throws org.springframework.beans.BeansException if any post-processing failed
	 */
	// 对从 FactoryBean 获取的给定对象进行后处理。处理后的对象将对外公开，以便进行 bean 引用。
	// <p>默认实现直接返回给定对象。子类可以重写此方法，例如，应用后处理器。
	// @param object 从 FactoryBean 获取的对象。
	// @param beanName bean 的名称。
	// @return 要对外公开的对象。
	// @throws org.springframework.beans.BeansException 如果任何后处理失败，则抛出异常。
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
		return object;
	}

	/**
	 * Get a FactoryBean for the given bean if possible.
	 * @param beanName the name of the bean
	 * @param beanInstance the corresponding bean instance
	 * @return the bean instance as FactoryBean
	 * @throws BeansException if the given bean cannot be exposed as a FactoryBean
	 */
	protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
		if (!(beanInstance instanceof FactoryBean<?> factoryBean)) {
			throw new BeanCreationException(beanName,
					"Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
		}
		return factoryBean;
	}

	/**
	 * Overridden to clear the FactoryBean object cache as well.
	 */
	@Override
	protected void removeSingleton(String beanName) {
		synchronized (getSingletonMutex()) {
			super.removeSingleton(beanName);
			this.factoryBeanObjectCache.remove(beanName);
		}
	}

	/**
	 * Overridden to clear the FactoryBean object cache as well.
	 */
	@Override
	protected void clearSingletonCache() {
		synchronized (getSingletonMutex()) {
			super.clearSingletonCache();
			this.factoryBeanObjectCache.clear();
		}
	}

}
