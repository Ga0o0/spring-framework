package org.springframework.web.servlet._mine.web13_model_and_view;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

import java.util.Map;

/**
 * ModelAndView
 *
 * @see org.springframework.web.servlet.ModelAndView
 */
public class MV01_ModelAndView {

	/**
	 * @see ModelAndView#ModelAndView()
	 * @see ModelAndView#ModelAndView(org.springframework.web.servlet.View)
	 * @see ModelAndView#ModelAndView(String, java.util.Map)
	 * @see ModelAndView#ModelAndView(org.springframework.web.servlet.View, java.util.Map)
	 * @see ModelAndView#ModelAndView(String, org.springframework.http.HttpStatusCode)
	 * @see ModelAndView#ModelAndView(String, java.util.Map, org.springframework.http.HttpStatusCode)
	 * @see ModelAndView#ModelAndView(String, String, Object)
	 * @see ModelAndView#ModelAndView(org.springframework.web.servlet.View, String, Object)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Params
			InternalResourceView view = new InternalResourceView();
			String viewName = "index";
			String modelName = "modelName";
			Object modelObject = "modelObject";
			Map<String, ?> model = new ModelMap();
			HttpStatusCode status = HttpStatus.OK;

			// Constructors
			ModelAndView modelAndView1 = new ModelAndView();
			ModelAndView modelAndView2 = new ModelAndView(view);
			ModelAndView modelAndView3 = new ModelAndView(viewName, model);
			ModelAndView modelAndView4 = new ModelAndView(view, model);
			ModelAndView modelAndView5 = new ModelAndView(viewName, status);
			ModelAndView modelAndView6 = new ModelAndView(viewName, model, status);
			ModelAndView modelAndView7 = new ModelAndView(viewName, modelName, modelObject);
			ModelAndView modelAndView8 = new ModelAndView(view, modelName, modelObject);

			// Print
			System.out.println(modelAndView1);
			System.out.println(modelAndView2);
			System.out.println(modelAndView3);
			System.out.println(modelAndView4);
			System.out.println(modelAndView5);
			System.out.println(modelAndView6);
			System.out.println(modelAndView7);
			System.out.println(modelAndView8);
		}
	}

	/**
	 * @see ModelAndView#setViewName(String)
	 * @see ModelAndView#getViewName()
	 * @see ModelAndView#setView(View)
	 * @see ModelAndView#getView()
	 * @see ModelAndView#hasView()
	 * @see ModelAndView#isReference()
	 * @see ModelAndView#getModelMap()
	 * @see ModelAndView#getModel()
	 * @see ModelAndView#getStatus()
	 * @see ModelAndView#addObject(String, Object)
	 * @see ModelAndView#addObject(Object)
	 * @see ModelAndView#addAllObjects(Map)
	 * @see ModelAndView#clear()
	 * @see ModelAndView#isEmpty()
	 * @see ModelAndView#wasCleared()
	 */
	static class Methods {
		public static void main(String[] args) {
			// Params
			InternalResourceView view = new InternalResourceView();
			String viewName = "index";
			String modelName = "modelName";
			Object modelObject = "modelObject";
			Map<String, ?> model = new ModelMap();
			HttpStatusCode status = HttpStatus.OK;

			// Constructors
			ModelAndView modelAndView = new ModelAndView(viewName, model, status);

			// Methods
			modelAndView.setViewName(viewName);
			String viewName1 = modelAndView.getViewName();

			modelAndView.setView(view);
			View view1 = modelAndView.getView();
			boolean hasView = modelAndView.hasView();

			boolean isReference = modelAndView.isReference();

			ModelMap modelMap = modelAndView.getModelMap();
			HttpStatusCode status1 = modelAndView.getStatus();

			modelAndView.addObject(modelName, modelObject);
			modelAndView.addObject(modelObject);
			modelAndView.addAllObjects(modelMap);

			modelAndView.clear();
			boolean isEmpty = modelAndView.isEmpty();
			boolean wasCleared = modelAndView.wasCleared();

			// Print
			System.out.println(viewName1);
			System.out.println(view1);
			System.out.println(hasView);
			System.out.println(isReference);
			System.out.println(status1);
			System.out.println(isEmpty);
			System.out.println(wasCleared);
		}
	}

}
/*
********************************* Class API Docs *********************************
Web MVC 框架中模型和视图的持有者。
请注意，它们是完全不同的。此类仅持有两者，以便控制器能够在单个返回值中同时返回模型和视图。

<p>表示由处理程序返回的模型和视图，由 DispatcherServlet 解析。视图可以采用字符串视图名称的形式，
需要由 ViewResolver 对象解析；或者，也可以直接指定 View 对象。模型是一个 Map，允许使用以名称为键的多个对象。

********************************* Class Definition *********************************
public class ModelAndView {
	// 视图实例或视图名称字符串。
	private Object view;
	// 模型映射。
	private ModelMap model;
	private HttpStatusCode status;
	private boolean cleared = false;
	// ...
}
**/