package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped.handler_method;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedMethod;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * AnnotatedMethod
 *
 * @see org.springframework.core.annotation.AnnotatedMethod
 */
public class HM01_AnnotatedMethod {

	static class SimpleController {
		@RequestMapping
		public Object get(Long id, String name) {
			System.out.println("id: " + id + ", name: " + name);
			return new Object();
		}
	}

	/**
	 * @see org.springframework.core.annotation.AnnotatedMethod#AnnotatedMethod(java.lang.reflect.Method)
	 * @see org.springframework.core.annotation.AnnotatedMethod#AnnotatedMethod(org.springframework.core.annotation.AnnotatedMethod)
	 */
	static class Constructors {
		public static void main(String[] args) throws NoSuchMethodException {
			Method getMethod = SimpleController.class.getDeclaredMethod("get", Long.class, String.class);
			AnnotatedMethod annotatedMethod = new AnnotatedMethod(getMethod);

			// Print
			System.out.println(annotatedMethod);
		}
	}


	/**
	 * @see AnnotatedMethod#getMethod()
	 * @see AnnotatedMethod#getMethodParameters()
	 * @see AnnotatedMethod#getReturnType()
	 * @see AnnotatedMethod#getReturnValueType(Object)
	 * @see AnnotatedMethod#isVoid()
	 * @see AnnotatedMethod#getMethodAnnotation(Class)
	 * @see AnnotatedMethod#hasMethodAnnotation(Class)
	 */
	static class Methods {
		public static void main(String[] args) throws NoSuchMethodException {
			Method getMethod = SimpleController.class.getDeclaredMethod("get", Long.class, String.class);
			AnnotatedMethod annotatedMethod = new AnnotatedMethod(getMethod);

			// Methods
			Method method = annotatedMethod.getMethod();
			MethodParameter[] methodParameters = annotatedMethod.getMethodParameters();
			MethodParameter returnType = annotatedMethod.getReturnType();
			MethodParameter returnValueType = annotatedMethod.getReturnValueType("hi");
			boolean isVoid = annotatedMethod.isVoid();
			RequestMapping methodAnnotation = annotatedMethod.getMethodAnnotation(RequestMapping.class);
			boolean hasMethodAnnotation = annotatedMethod.hasMethodAnnotation(RequestMapping.class);

			// Print
			System.out.println("method: " 				+ method);
			System.out.println("methodParameters: " 	+ Arrays.toString(methodParameters));
			System.out.println("returnType: " 			+ returnType);
			System.out.println("returnValueType: " 		+ returnValueType);
			System.out.println("isVoid: " 				+ isVoid);
			System.out.println("methodAnnotation: " 	+ methodAnnotation);
			System.out.println("hasMethodAnnotation: " 	+ hasMethodAnnotation);
		}
	}
}
/*
********************************* Class API Docs *********************************
Method 句柄的便捷包装器，提供对方法和方法参数的深度注释自省，包括从具体目标方法中暴露接口声明的参数注释。

********************************* Class Definition *********************************
public class AnnotatedMethod {
	private final Method method;
	private final Method bridgedMethod;
	private final MethodParameter[] parameters;
	private volatile List<Annotation[][]> inheritedParameterAnnotations;
	// ...
}
**/
