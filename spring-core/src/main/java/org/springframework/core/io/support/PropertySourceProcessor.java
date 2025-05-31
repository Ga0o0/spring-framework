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

package org.springframework.core.io.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Contribute {@link PropertySource property sources} to the {@link Environment}.
 *
 * <p>This class is stateful and merges descriptors with the same name in a
 * single {@link PropertySource} rather than creating dedicated ones.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @author Juergen Hoeller
 * @since 6.0
 * @see PropertySourceDescriptor
 */
// 将 {@link PropertySource 属性源} 贡献给 {@link Environment}。
//
// <p>此类是有状态的，它会将同名的描述符合并到单个 {@link PropertySource} 中，而不是创建专用的描述符。
public class PropertySourceProcessor {

	private static final PropertySourceFactory defaultPropertySourceFactory = new DefaultPropertySourceFactory();

	private static final Log logger = LogFactory.getLog(PropertySourceProcessor.class);


	private final ConfigurableEnvironment environment;

	private final ResourcePatternResolver resourcePatternResolver;

	private final List<String> propertySourceNames = new ArrayList<>();


	public PropertySourceProcessor(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
		this.environment = environment;
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
	}


	/**
	 * Process the specified {@link PropertySourceDescriptor} against the
	 * environment managed by this instance.
	 * @param descriptor the descriptor to process
	 * @throws IOException if loading the properties failed
	 */
	// 针对此实例管理的环境，处理指定的 {@link PropertySourceDescriptor}。
	// @param descriptor 要处理的描述符
	// @throws IOException，如果加载属性失败
	public void processPropertySource(PropertySourceDescriptor descriptor) throws IOException {
		String name = descriptor.name();
		String encoding = descriptor.encoding();
		List<String> locations = descriptor.locations();
		Assert.isTrue(locations.size() > 0, "At least one @PropertySource(value) location is required");
		boolean ignoreResourceNotFound = descriptor.ignoreResourceNotFound();
		PropertySourceFactory factory = (descriptor.propertySourceFactory() != null ?
				instantiateClass(descriptor.propertySourceFactory()) : defaultPropertySourceFactory);

		for (String location : locations) {
			try {
				// 解析给定文本中的 ${...} 占位符，并将其替换为通过 {@link #getProperty} 解析的相应属性值。
				String resolvedLocation = this.environment.resolveRequiredPlaceholders(location);
				// 将给定的位置模式解析为 {@code Resource} 对象。
				for (Resource resource : this.resourcePatternResolver.getResources(resolvedLocation)) {
					addPropertySource(factory.createPropertySource(name, new EncodedResource(resource, encoding)));
				}
			}
			catch (RuntimeException | IOException ex) {
				// Placeholders not resolvable (IllegalArgumentException) or resource not found when trying to open it
				// --> 译文：占位符无法解析（IllegalArgumentException）或尝试打开时未找到资源
				if (ignoreResourceNotFound && (ex instanceof IllegalArgumentException || isIgnorableException(ex) ||
						isIgnorableException(ex.getCause()))) {
					if (logger.isInfoEnabled()) {
						// 属性位置 “location” 无法解析
						logger.info("Properties location [" + location + "] not resolvable: " + ex.getMessage());
					}
				}
				else {
					throw ex;
				}
			}
		}
	}

	// propertySource 已存在，使用 CompositePropertySource 替换；
	// propertySource 不存在，并且没有任何 PropertySource，就进行 add last，否则 add before 到最后一个元素。
	private void addPropertySource(PropertySource<?> propertySource) {
		String name = propertySource.getName();
		MutablePropertySources propertySources = this.environment.getPropertySources();

		if (this.propertySourceNames.contains(name)) {
			// We've already added a version, we need to extend it --> 译文：我们已经添加了一个版本，我们需要扩展它
			PropertySource<?> existing = propertySources.get(name);
			if (existing != null) {
				PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource rps ?
						rps.withResourceName() : propertySource);
				// existing is CompositePropertySource, add first
				if (existing instanceof CompositePropertySource cps) {
					cps.addFirstPropertySource(newSource);
				}
				// existing is not CompositePropertySource,replace with CompositePropertySource
				else {
					if (existing instanceof ResourcePropertySource rps) {
						existing = rps.withResourceName();
					}
					CompositePropertySource composite = new CompositePropertySource(name);
					composite.addPropertySource(newSource);
					composite.addPropertySource(existing);
					propertySources.replace(name, composite);
				}
				return;
			}
		}

		if (this.propertySourceNames.isEmpty()) {
			propertySources.addLast(propertySource);
		}
		else {
			String lastAdded = this.propertySourceNames.get(this.propertySourceNames.size() - 1);
			propertySources.addBefore(lastAdded, propertySource);
		}
		this.propertySourceNames.add(name);
	}


	private static PropertySourceFactory instantiateClass(Class<? extends PropertySourceFactory> type) {
		try {
			Constructor<? extends PropertySourceFactory> constructor = type.getDeclaredConstructor();
			ReflectionUtils.makeAccessible(constructor);
			return constructor.newInstance();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to instantiate " + type, ex);
		}
	}

	/**
	 * Determine if the supplied exception can be ignored according to
	 * {@code ignoreResourceNotFound} semantics.
	 */
	private static boolean isIgnorableException(@Nullable Throwable ex) {
		return (ex instanceof FileNotFoundException ||
				ex instanceof UnknownHostException ||
				ex instanceof SocketException);
	}

}
