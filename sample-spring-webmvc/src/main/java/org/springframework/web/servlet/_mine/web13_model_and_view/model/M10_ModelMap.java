package org.springframework.web.servlet._mine.web13_model_and_view.model;

import org.springframework.ui.ModelMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ModelMap
 *
 * @see org.springframework.ui.ModelMap
 */
public class M10_ModelMap {

	/**
	 * @see ModelMap#ModelMap()
	 * @see ModelMap#ModelMap(String, Object)
	 * @see ModelMap#ModelMap(Object)
	 */
	static class Constructors {
		public static void main(String[] args) {
			// Params
			String attributeName = "id";
			Object attributeValue = 1;

			// Constructors
			ModelMap modelMap1 = new ModelMap();
			ModelMap modelMap2 = new ModelMap(attributeName, attributeValue);
			ModelMap modelMap3 = new ModelMap(attributeValue);

			// Print
			System.out.println(modelMap1);	// {}
			System.out.println(modelMap2);	// {id=1}
			System.out.println(modelMap3);	// {integer=1}
		}
	}

	/**
	 * @see ModelMap#addAttribute(String, Object)
	 * @see ModelMap#addAttribute(Object)
	 * @see ModelMap#addAllAttributes(Collection)
	 * @see ModelMap#addAllAttributes(Map)
	 * @see ModelMap#containsAttribute(String)
	 * @see ModelMap#getAttribute(String)
	 */
	static class Methods {
		public static void main(String[] args) {
			// Params
			String attributeName = "id";
			Object attributeValue = 1;
			Collection<?> attributeValues = List.of(attributeValue);
			Map<String, ?> attributes = Map.of(attributeName, attributeValues);

			// Constructors
			ModelMap modelMap = new ModelMap();

			// Methods
			// ModelMap#addAttribute(String, Object)
			ModelMap modelMap1 = modelMap.addAttribute(attributeName, attributeValue);
			System.out.println(modelMap1);

			// ModelMap#addAttribute(Object)
			ModelMap modelMap2 = modelMap.addAttribute(attributeValue);
			System.out.println(modelMap2);

			// @see ModelMap#addAllAttributes(Collection)
			ModelMap modelMap3 = modelMap.addAllAttributes(attributeValues);
			System.out.println(modelMap3);

			// ModelMap#addAllAttributes(Map)
			ModelMap modelMap4 = modelMap.addAllAttributes(attributes);
			System.out.println(modelMap4);

			// ModelMap#containsAttribute(String)
			boolean containsAttribute = modelMap.containsAttribute(attributeName);
			System.out.println("modelMap containsAttribute(" + attributeName + "): " + containsAttribute);

			// ModelMap#getAttribute(String)
			Object attribute = modelMap.getAttribute(attributeName);
			System.out.println("modelMap getAttribute(" + attributeName + "): " + attribute);
		}
	}
}
/*
********************************* Class API Docs *********************************
java.util.Map 的实现，用于构建用于 UI 工具的模型数据。支持链式调用和模型属性名生成。

<p>此类用作 Servlet MVC 的通用模型持有者，但并不依赖于它。请查看 org.springframework.ui.Model 接口获取接口变体。

********************************* Class Definition *********************************
public class ModelMap extends LinkedHashMap<String, Object> {
	// ...
}
**/
