package org.springframework.web.servlet._mine.web13_model_and_view.model;

import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Model
 *
 * @see org.springframework.ui.Model
 */
public class M01_Model {

	/**
	 * @see org.springframework.ui.Model#addAttribute(String, Object)
	 * @see org.springframework.ui.Model#addAttribute(Object)
	 * @see org.springframework.ui.Model#addAllAttributes(Collection)
	 * @see org.springframework.ui.Model#addAllAttributes(Map)
	 * @see org.springframework.ui.Model#mergeAttributes(Map)
	 * @see org.springframework.ui.Model#containsAttribute(String)
	 * @see org.springframework.ui.Model#getAttribute(String)
	 * @see org.springframework.ui.Model#asMap()
	 */
	public static void main(String[] args) {
		// Params
		String attributeName = "id";
		Object attributeValue = 1;
		Collection<?> attributeValues = List.of(attributeValue);
		Map<String, ?> attributes = Map.of(attributeName, attributeValues);

		// Constructors
		ConcurrentModel model = new ConcurrentModel();

		// Methods
		// Model#addAttribute(String, Object)
		Model model1 = model.addAttribute(attributeName, attributeValue);
		System.out.println(model1);

		// Model#addAttribute(Object)
		Model model2 = model.addAttribute(attributeValue);
		System.out.println(model2);

		// @see Model#addAllAttributes(Collection)
		Model model3 = model.addAllAttributes(attributeValues);
		System.out.println(model3);

		// Model#addAllAttributes(Map)
		Model model4 = model.addAllAttributes(attributes);
		System.out.println(model4);

		// Model#mergeAttributes(Map)
		Map<String, String> map = Map.of("k1", "v1", "k2", "v2");
		ConcurrentModel mergeAttributes = model.mergeAttributes(map);
		System.out.println("model mergeAttributes map, model is: " + mergeAttributes);

		// Model#containsAttribute(String)
		boolean containsAttribute = model.containsAttribute(attributeName);
		System.out.println("model containsAttribute(" + attributeName + "): " + containsAttribute);

		// Model#getAttribute(String)
		Object attribute = model.getAttribute(attributeName);
		System.out.println("model getAttribute(" + attributeName + "): " + attribute);

		// Model#asMap()
		Map<String, Object> map1 = model.asMap();
		System.out.println(map1);
	}
}
/*
********************************* Class API Docs *********************************
定义 model 属性持有者的接口。

<p>主要用于向 model 添加属性。

<p>允许将整个 model 作为 {@code java.util.Map} 访问。

********************************* Class Definition *********************************
public interface Model {
	Model addAttribute(String attributeName, @Nullable Object attributeValue);
	Model addAttribute(Object attributeValue);
	Model addAllAttributes(Collection<?> attributeValues);
	Model addAllAttributes(Map<String, ?> attributes);
	Model mergeAttributes(Map<String, ?> attributes);
	boolean containsAttribute(String attributeName);
	Object getAttribute(String attributeName);
	Map<String, Object> asMap();
}
**/
