package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped.handler_method;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * HandlerMethod
 *
 * @see org.springframework.web.method.HandlerMethod
 */
public class HM02_HandlerMethod {

	static class SimpleController {
		@RequestMapping
		public Object get(Long id, String name) {
			System.out.println("id: " + id + ", name: " + name);
			return new Object();
		}
	}

	/**
	 * @see org.springframework.web.method.HandlerMethod#HandlerMethod(Object, Method)
	 * @see org.springframework.web.method.HandlerMethod#HandlerMethod(Object, String, Class[])
	 * @see org.springframework.web.method.HandlerMethod#HandlerMethod(String, BeanFactory, Method)
	 * @see org.springframework.web.method.HandlerMethod#HandlerMethod(String, BeanFactory, MessageSource, Method)
	 */
	static class Constructors {
		public static void main(String[] args) throws NoSuchMethodException {
			// Params
			Object bean = new SimpleController();
			String methodName = "get";
			Class<?>[] parameterTypes = {Long.class, String.class};
			Method method = SimpleController.class.getMethod(methodName, parameterTypes);
			String beanName = "simpleController";

			StaticApplicationContext context = new StaticApplicationContext();
			context.registerBean(beanName, SimpleController.class);
			/*DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
			beanFactory.registerSingleton(beanName, bean);
			MessageSource messageSource = new StaticMessageSource();*/

			// Constructors
			HandlerMethod handlerMethod1 = new HandlerMethod(bean, method);
			HandlerMethod handlerMethod2 = new HandlerMethod(bean, methodName, parameterTypes);
			HandlerMethod handlerMethod3 = new HandlerMethod(beanName, context, method);
			HandlerMethod handlerMethod4 = new HandlerMethod(beanName, context, context, method);

			// Print
			System.out.println(handlerMethod1);
			System.out.println(handlerMethod2);
			System.out.println(handlerMethod3);
			System.out.println(handlerMethod4);
		}

	}

	/**
	 * @see HandlerMethod#getBean()
	 * @see HandlerMethod#getBeanType()
	 * @see HandlerMethod#shouldValidateArguments()
	 * @see HandlerMethod#shouldValidateReturnValue()
	 * @see HandlerMethod#getResolvedFromHandlerMethod()
	 * @see HandlerMethod#createWithValidateFlags()
	 * @see HandlerMethod#createWithResolvedBean()
	 * @see HandlerMethod#getShortLogMessage()
	 */
	static class Methods {
		public static void main(String[] args) throws NoSuchMethodException {
			Object bean = new SimpleController();
			Method method = SimpleController.class.getMethod("get", Long.class, String.class);

			HandlerMethod handlerMethod = new HandlerMethod(bean, method);

			// Methods
			Object object = handlerMethod.getBean();
			Class<?> beanType = handlerMethod.getBeanType();
			boolean shouldValidateArguments = handlerMethod.shouldValidateArguments();
			boolean shouldValidateReturnValue = handlerMethod.shouldValidateReturnValue();
			HandlerMethod resolvedFromHandlerMethod = handlerMethod.getResolvedFromHandlerMethod();
			HandlerMethod withValidateFlags = handlerMethod.createWithValidateFlags();
			HandlerMethod withResolvedBean = handlerMethod.createWithResolvedBean();
			String shortLogMessage = handlerMethod.getShortLogMessage();

			// Print
			System.out.println("object: " + object);
			System.out.println("beanType: " + beanType);
			System.out.println("shouldValidateArguments: " + shouldValidateArguments);
			System.out.println("shouldValidateReturnValue: " + shouldValidateReturnValue);
			System.out.println("resolvedFromHandlerMethod: " + resolvedFromHandlerMethod);
			System.out.println("withValidateFlags: " + withValidateFlags);
			System.out.println("withResolvedBean: " + withResolvedBean);
			System.out.println("shortLogMessage: " + shortLogMessage);
		}
	}
}
/*
********************************* Class API Docs *********************************
封装由 AnnotatedMethod#getMethod() 方法和 HandlerMethod#getBean() bean 组成的 handler 方法的信息。
提供对方法参数、方法返回值、方法注解等的便捷访问。

<p>该类可以通过 bean 实例或 bean 名称（例如 lazy-init bean、prototype bean）创建。
使用 HandlerMethod#createWithResolvedBean() 获取 HandlerMethod 实例，并通过关联的 BeanFactory 解析 bean 实例。

********************************* Class Definition *********************************
public class HandlerMethod extends AnnotatedMethod {
	// ...
	private final Object bean;
	private final BeanFactory beanFactory;
	private final MessageSource messageSource;
	private final Class<?> beanType;
	private final boolean validateArguments;
	private final boolean validateReturnValue;
	private HttpStatusCode responseStatus;
	private String responseStatusReason;
	private HandlerMethod resolvedFromHandlerMethod;
	private final String description;
	// ...
}
**/