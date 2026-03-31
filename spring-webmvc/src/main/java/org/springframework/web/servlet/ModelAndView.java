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

package org.springframework.web.servlet;

import java.util.Map;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

/**
 * Holder for both Model and View in the web MVC framework.
 * Note that these are entirely distinct. This class merely holds
 * both to make it possible for a controller to return both model
 * and view in a single return value.
 *
 * <p>Represents a model and view returned by a handler, to be resolved
 * by a DispatcherServlet. The view can take the form of a String
 * view name which will need to be resolved by a ViewResolver object;
 * alternatively a View object can be specified directly. The model
 * is a Map, allowing the use of multiple objects keyed by name.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Rossen Stoyanchev
 * @see DispatcherServlet
 * @see ViewResolver
 * @see HandlerAdapter#handle
 * @see org.springframework.web.servlet.mvc.Controller#handleRequest
 */
// Web MVC 框架中模型和视图的持有者。请注意，它们是完全不同的。此类仅持有两者，以便控制器能够在单个返回值中同时返回模型和视图。
//
// <p>表示由处理程序返回的模型和视图，由 DispatcherServlet 解析。视图可以采用字符串视图名称的形式，
// 需要由 ViewResolver 对象解析；或者，也可以直接指定 View 对象。模型是一个 Map，允许使用以名称为键的多个对象。
public class ModelAndView {

	/** View instance or view name String. */
	// 视图实例或视图名称字符串。
	@Nullable
	private Object view;

	/** Model Map. */
	// 模型映射。
	@Nullable
	private ModelMap model;

	/** Optional HTTP status for the response. */
	// 响应的可选 HTTP 状态。
	@Nullable
	private HttpStatusCode status;

	/** Indicates whether this instance has been cleared with a call to {@link #clear()}. */
	// 指示此实例是否已通过调用 {@link #clear()} 清除。
	private boolean cleared = false;


	/**
	 * Default constructor for bean-style usage: populating bean
	 * properties instead of passing in constructor arguments.
	 * @see #setView(View)
	 * @see #setViewName(String)
	 */
	public ModelAndView() {
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with {@code addObject}.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * @see #addObject
	 */
	// 当没有模型数据可供暴露时，此构造函数十分便捷。也可与 {@code addObject} 结合使用。
	// @param viewName 待渲染视图的名称，由 DispatcherServlet 的 ViewResolver 解析
	public ModelAndView(String viewName) {
		this.view = viewName;
	}

	/**
	 * Convenient constructor when there is no model data to expose.
	 * Can also be used in conjunction with {@code addObject}.
	 * @param view the View object to render
	 * @see #addObject
	 */
	public ModelAndView(View view) {
		this.view = view;
	}

	/**
	 * Create a new ModelAndView given a view name and a model.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * @param model a Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be {@code null}, but the
	 * model Map may be {@code null} if there is no model data.
	 */
	public ModelAndView(String viewName, @Nullable Map<String, ?> model) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}

	/**
	 * Create a new ModelAndView given a View object and a model.
	 * <em>Note: the supplied model data is copied into the internal
	 * storage of this class. You should not consider to modify the supplied
	 * Map after supplying it to this class</em>
	 * @param view the View object to render
	 * @param model a Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be {@code null}, but the
	 * model Map may be {@code null} if there is no model data.
	 */
	public ModelAndView(View view, @Nullable Map<String, ?> model) {
		this.view = view;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}

	/**
	 * Create a new ModelAndView given a view name and HTTP status.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * @param status an HTTP status code to use for the response
	 * (to be set just prior to View rendering)
	 * @since 4.3.8
	 */
	public ModelAndView(String viewName, HttpStatusCode status) {
		this.view = viewName;
		this.status = status;
	}

	/**
	 * Create a new ModelAndView given a view name, model, and HTTP status.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * @param model a Map of model names (Strings) to model objects
	 * (Objects). Model entries may not be {@code null}, but the
	 * model Map may be {@code null} if there is no model data.
	 * @param status an HTTP status code to use for the response
	 * (to be set just prior to View rendering)
	 * @since 4.3
	 */
	// 给定视图名称、模型和 HTTP 状态，创建一个新的 ModelAndView。
	// @param viewName 待渲染视图的名称，由 DispatcherServlet 的 ViewResolver 解析。
	// @param model 一个模型名称（字符串）到模型对象（对象）的 Map。模型条目不能为 {@code null}，但如果没有模型数据，则模型 Map 可能为 {@code null}。
	// @param status 用于响应的 HTTP 状态码（在视图渲染之前设置）
	public ModelAndView(@Nullable String viewName, @Nullable Map<String, ?> model, @Nullable HttpStatusCode status) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
		this.status = status;
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the View to render, to be resolved
	 * by the DispatcherServlet's ViewResolver
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param view the View object to render
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}


	/**
	 * Set a view name for this ModelAndView, to be resolved by the
	 * DispatcherServlet via a ViewResolver. Will override any
	 * pre-existing view name or View.
	 */
	// 为该模型和视图设置视图名称，该名称将由调度器服务 (DispatcherServlet) 通过视图解析器 (ViewResolver) 解析。这将覆盖任何预先存在的视图名称或视图。
	public void setViewName(@Nullable String viewName) {
		this.view = viewName;
	}

	/**
	 * Return the view name to be resolved by the DispatcherServlet
	 * via a ViewResolver, or {@code null} if we are using a View object.
	 */
	@Nullable
	public String getViewName() {
		return (this.view instanceof String name ? name : null);
	}

	/**
	 * Set a View object for this ModelAndView. Will override any
	 * pre-existing view name or View.
	 */
	// 为该 ModelAndView 设置一个 View 对象。该对象将覆盖任何已存在的视图名称或 View。
	public void setView(@Nullable View view) {
		this.view = view;
	}

	/**
	 * Return the View object, or {@code null} if we are using a view name
	 * to be resolved by the DispatcherServlet via a ViewResolver.
	 */
	@Nullable
	public View getView() {
		return (this.view instanceof View v ? v : null);
	}

	/**
	 * Indicate whether this {@code ModelAndView} has a view, either
	 * as a view name or as a direct {@link View} instance.
	 */
	// 指示此 {@code ModelAndView} 是否具有视图，可以作为视图名称或直接 {@link View} 实例。
	public boolean hasView() {
		return (this.view != null);
	}

	/**
	 * Return whether we use a view reference, i.e. {@code true}
	 * if the view has been specified via a name to be resolved by the
	 * DispatcherServlet via a ViewResolver.
	 */
	public boolean isReference() {
		return (this.view instanceof String);
	}

	/**
	 * Return the model map. May return {@code null}.
	 * Called by DispatcherServlet for evaluation of the model.
	 */
	// 返回模型映射。可能返回 {@code null}。由 DispatcherServlet 调用以评估模型。
	@Nullable
	protected Map<String, Object> getModelInternal() {
		return this.model;
	}

	/**
	 * Return the underlying {@code ModelMap} instance (never {@code null}).
	 */
	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}

	/**
	 * Return the model map. Never returns {@code null}.
	 * To be called by application code for modifying the model.
	 */
	public Map<String, Object> getModel() {
		return getModelMap();
	}

	/**
	 * Set the HTTP status to use for the response.
	 * <p>The response status is set just prior to View rendering.
	 * @since 4.3
	 */
	public void setStatus(@Nullable HttpStatusCode status) {
		this.status = status;
	}

	/**
	 * Return the configured HTTP status for the response, if any.
	 * @since 4.3
	 */
	// 如果有的话，返回响应的配置 HTTP 状态。
	@Nullable
	public HttpStatusCode getStatus() {
		return this.status;
	}


	/**
	 * Add an attribute to the model.
	 * @param attributeName name of the object to add to the model (never {@code null})
	 * @param attributeValue object to add to the model (can be {@code null})
	 * @see ModelMap#addAttribute(String, Object)
	 * @see #getModelMap()
	 */
	public ModelAndView addObject(String attributeName, @Nullable Object attributeValue) {
		getModelMap().addAttribute(attributeName, attributeValue);
		return this;
	}

	/**
	 * Add an attribute to the model using parameter name generation.
	 * @param attributeValue the object to add to the model (never {@code null})
	 * @see ModelMap#addAttribute(Object)
	 * @see #getModelMap()
	 */
	public ModelAndView addObject(Object attributeValue) {
		getModelMap().addAttribute(attributeValue);
		return this;
	}

	/**
	 * Add all attributes contained in the provided Map to the model.
	 * @param modelMap a Map of attributeName &rarr; attributeValue pairs
	 * @see ModelMap#addAllAttributes(Map)
	 * @see #getModelMap()
	 */
	public ModelAndView addAllObjects(@Nullable Map<String, ?> modelMap) {
		getModelMap().addAllAttributes(modelMap);
		return this;
	}


	/**
	 * Clear the state of this ModelAndView object.
	 * The object will be empty afterwards.
	 * <p>Can be used to suppress rendering of a given ModelAndView object
	 * in the {@code postHandle} method of a HandlerInterceptor.
	 * @see #isEmpty()
	 * @see HandlerInterceptor#postHandle
	 */
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}

	/**
	 * Return whether this ModelAndView object is empty,
	 * i.e. whether it does not hold any view and does not contain a model.
	 */
	public boolean isEmpty() {
		return (this.view == null && CollectionUtils.isEmpty(this.model));
	}

	/**
	 * Return whether this ModelAndView object is empty as a result of a call to {@link #clear}
	 * i.e. whether it does not hold any view and does not contain a model.
	 * <p>Returns {@code false} if any additional state was added to the instance
	 * <strong>after</strong> the call to {@link #clear}.
	 * @see #clear()
	 */
	public boolean wasCleared() {
		return (this.cleared && isEmpty());
	}


	/**
	 * Return diagnostic information about this model and view.
	 */
	@Override
	public String toString() {
		return "ModelAndView [view=" + formatView() + "; model=" + this.model + "]";
	}

	private String formatView() {
		return isReference() ? "\"" + this.view + "\"" : "[" + this.view + "]";
	}

}
