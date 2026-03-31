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

package org.springframework.web.method.annotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Manages controller-specific session attributes declared via
 * {@link SessionAttributes @SessionAttributes}. Actual storage is
 * delegated to a {@link SessionAttributeStore} instance.
 *
 * <p>When a controller annotated with {@code @SessionAttributes} adds
 * attributes to its model, those attributes are checked against names and
 * types specified via {@code @SessionAttributes}. Matching model attributes
 * are saved in the HTTP session and remain there until the controller calls
 * {@link SessionStatus#setComplete()}.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
// 管理通过 {@link SessionAttributes @SessionAttributes} 声明的控制器特定会话属性。实际存储委托给 {@link SessionAttributeStore} 实例。
//
// <p>当使用 {@code @SessionAttributes} 注解的控制器向其模型添加属性时，这些属性会根据通过 {@code @SessionAttributes} 指定的名称和类型进行检查。
// 匹配的模型属性将保存在 HTTP 会话中，并一直保留到控制器调用 {@link SessionStatus#setComplete()} 为止。
public class SessionAttributesHandler {

	/**
	 * Key for known-attribute-names storage (a String array) as a session attribute.
	 * <p>This is necessary for consistent handling of type-based session attributes
	 * in distributed session scenarios where handler methods from the same class
	 * may get invoked on different servers.
	 * @since 6.1.4
	 */
	public static final String SESSION_KNOWN_ATTRIBUTE = SessionAttributesHandler.class.getName() + ".KNOWN";


	private final Set<String> attributeNames = new HashSet<>();

	private final Set<Class<?>> attributeTypes = new HashSet<>();

	private final Set<String> knownAttributeNames = Collections.newSetFromMap(new ConcurrentHashMap<>(4));

	private final SessionAttributeStore sessionAttributeStore;


	/**
	 * Create a new session attributes handler. Session attribute names and types
	 * are extracted from the {@code @SessionAttributes} annotation, if present,
	 * on the given type.
	 * @param handlerType the controller type
	 * @param sessionAttributeStore used for session access
	 */
	// 创建一个新的 session 属性 handler。Session 属性名称和类型将从给定类型上的 @SessionAttributes 注解（如果存在）中提取。
	// @param handlerType 控制器类型
	// @param sessionAttributeStore 用于会话访问
	public SessionAttributesHandler(Class<?> handlerType, SessionAttributeStore sessionAttributeStore) {
		Assert.notNull(sessionAttributeStore, "SessionAttributeStore may not be null");
		this.sessionAttributeStore = sessionAttributeStore;

		SessionAttributes ann = AnnotatedElementUtils.findMergedAnnotation(handlerType, SessionAttributes.class);
		if (ann != null) {
			Collections.addAll(this.attributeNames, ann.names());
			Collections.addAll(this.attributeTypes, ann.types());
		}
		this.knownAttributeNames.addAll(this.attributeNames);
	}


	/**
	 * Whether the controller represented by this instance has declared any
	 * session attributes through an {@link SessionAttributes} annotation.
	 */
	// 此实例所代表的控制器是否已通过 {@link SessionAttributes} 注解声明任何会话属性。
	public boolean hasSessionAttributes() {
		return (!this.attributeNames.isEmpty() || !this.attributeTypes.isEmpty());
	}

	/**
	 * Whether the attribute name or type match the names and types specified
	 * via {@code @SessionAttributes} on the underlying controller.
	 * <p>Attributes successfully resolved through this method are "remembered"
	 * and subsequently used in {@link #retrieveAttributes(WebRequest)} and
	 * {@link #cleanupAttributes(WebRequest)}.
	 * @param attributeName the attribute name to check
	 * @param attributeType the type for the attribute
	 */
	public boolean isHandlerSessionAttribute(String attributeName, Class<?> attributeType) {
		Assert.notNull(attributeName, "Attribute name must not be null");
		if (this.attributeTypes.contains(attributeType)) {
			this.knownAttributeNames.add(attributeName);
			return true;
		}
		else {
			return this.attributeNames.contains(attributeName);
		}
	}

	/**
	 * Store a subset of the given attributes in the session. Attributes not
	 * declared as session attributes via {@code @SessionAttributes} are ignored.
	 * @param request the current request
	 * @param attributes candidate attributes for session storage
	 */
	// 将给定属性的子集存储在会话中。未通过 {@code @SessionAttributes} 声明为会话属性的属性将被忽略。
	// @param request 当前请求
	// @param attribute 会话存储的候选属性
	public void storeAttributes(WebRequest request, Map<String, ?> attributes) {
		attributes.forEach((name, value) -> {
			if (value != null && isHandlerSessionAttribute(name, value.getClass())) {
				this.sessionAttributeStore.storeAttribute(request, name, value);
			}
		});

		// Store known attribute names in session (for distributed sessions)
		// Only necessary for type-based attributes which get added to knownAttributeNames when touched.
		// --> 译文：在会话中存储已知属性名称（对于分布式会话）仅对于在触摸时添加到 knownAttributeNames 的基于类型的属性才有必要。
		if (!this.attributeTypes.isEmpty()) {
			// 将提供的属性存储在后端会话中。
			this.sessionAttributeStore.storeAttribute(request,
					SESSION_KNOWN_ATTRIBUTE, StringUtils.toStringArray(this.knownAttributeNames));
		}
	}

	/**
	 * Retrieve "known" attributes from the session, i.e. attributes listed
	 * by name in {@code @SessionAttributes} or attributes previously stored
	 * in the model that matched by type.
	 * @param request the current request
	 * @return a map with handler session attributes, possibly empty
	 */
	// 从会话中检索“已知”属性，即 {@code @SessionAttributes} 中按名称列出的属性，或先前存储在模型中且按类型匹配的属性。
	// @param request 当前请求
	// @return 包含处理程序会话属性的映射，可能为空
	public Map<String, Object> retrieveAttributes(WebRequest request) {
		// Restore known attribute names from session (for distributed sessions)
		// Only necessary for type-based attributes which get added to knownAttributeNames when touched.
		if (!this.attributeTypes.isEmpty()) {
			Object known = this.sessionAttributeStore.retrieveAttribute(request, SESSION_KNOWN_ATTRIBUTE);
			if (known instanceof String[] retrievedAttributeNames) {
				this.knownAttributeNames.addAll(Arrays.asList(retrievedAttributeNames));
			}
		}

		Map<String, Object> attributes = new HashMap<>();
		for (String name : this.knownAttributeNames) {
			Object value = this.sessionAttributeStore.retrieveAttribute(request, name);
			if (value != null) {
				attributes.put(name, value);
			}
		}
		return attributes;
	}

	/**
	 * Remove "known" attributes from the session, i.e. attributes listed
	 * by name in {@code @SessionAttributes} or attributes previously stored
	 * in the model that matched by type.
	 * @param request the current request
	 */
	// 从会话中移除 “已知” 属性，即 {@code @SessionAttributes} 中按名称列出的属性，或先前存储在模型中且按类型匹配的属性。
	// @param request 当前请求
	public void cleanupAttributes(WebRequest request) {
		for (String attributeName : this.knownAttributeNames) {
			// 清理后端会话中的指定属性。
			this.sessionAttributeStore.cleanupAttribute(request, attributeName);
		}
	}

	/**
	 * A pass-through call to the underlying {@link SessionAttributeStore}.
	 * @param request the current request
	 * @param attributeName the name of the attribute of interest
	 * @return the attribute value, or {@code null} if none
	 */
	@Nullable
	Object retrieveAttribute(WebRequest request, String attributeName) {
		return this.sessionAttributeStore.retrieveAttribute(request, attributeName);
	}

}
