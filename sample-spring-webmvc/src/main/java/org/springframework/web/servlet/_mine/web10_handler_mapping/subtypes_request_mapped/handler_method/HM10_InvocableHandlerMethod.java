package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_request_mapped.handler_method;

import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.validation.method.MethodValidator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.HandlerMethodValidator;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

/**
 * InvocableHandlerMethod
 *
 * @see org.springframework.web.method.support.InvocableHandlerMethod
 */
public class HM10_InvocableHandlerMethod {

	static class SimpleController {
		@RequestMapping
		public Object get(Long id, String name) {
			System.out.println("id: " + id + ", name: " + name);
			return new Object();
		}
	}

	/**
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#InvocableHandlerMethod(HandlerMethod)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#InvocableHandlerMethod(Object, Method)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#InvocableHandlerMethod(Object, String, Class[])
	 */
	static class Constructors {
		public static void main(String[] args) throws NoSuchMethodException {
			// Params
			Object bean = new SimpleController();
			String methodName = "get";
			Class<?>[] parameterTypes = {Long.class, String.class};
			Method method = SimpleController.class.getMethod(methodName, parameterTypes);
			HandlerMethod handlerMethod = new  HandlerMethod(bean, method);
			MessageSource messageSource = new StaticMessageSource();

			// Constructors
			InvocableHandlerMethod invocableHandlerMethod1 = new InvocableHandlerMethod(handlerMethod);
			InvocableHandlerMethod invocableHandlerMethod2 = new InvocableHandlerMethod(bean, method);
			InvocableHandlerMethod invocableHandlerMethod3 = new InvocableHandlerMethod(bean, methodName, parameterTypes);

			// Print
			System.out.println(invocableHandlerMethod1);
			System.out.println(invocableHandlerMethod2);
			System.out.println(invocableHandlerMethod3);
		}
	}

	/**
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#setHandlerMethodArgumentResolvers(HandlerMethodArgumentResolverComposite)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#setParameterNameDiscoverer(ParameterNameDiscoverer)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#setDataBinderFactory(WebDataBinderFactory)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#setMethodValidator(MethodValidator)
	 * @see org.springframework.web.method.support.InvocableHandlerMethod#invokeForRequest(NativeWebRequest, ModelAndViewContainer, Object...)
	 */
	static class Methods {
		public static void main(String[] args) throws NoSuchMethodException {
			// InvocableHandlerMethod
			Object bean = new SimpleController();
			String methodName = "get";
			Class<?>[] parameterTypes = {Long.class, String.class};
			InvocableHandlerMethod invocableHandlerMethod = new InvocableHandlerMethod(bean, methodName, parameterTypes);

			// Params
			HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();
			ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

			WebBindingInitializer initializer = new ConfigurableWebBindingInitializer();
			WebDataBinderFactory dataBinderFactory = new DefaultDataBinderFactory(initializer);

			MethodValidator methodValidator = HandlerMethodValidator.from(initializer, parameterNameDiscoverer,
					modelAttribute -> true, requestParam -> true);

			// Methods
			// 设置 HandlerMethodArgumentResolvers 用于解析方法参数值。
			invocableHandlerMethod.setHandlerMethodArgumentResolvers(resolvers);
			// 设置 ParameterNameDiscoverer 以便在需要时解析参数名称（例如默认请求属性名称）。
			invocableHandlerMethod.setParameterNameDiscoverer(parameterNameDiscoverer);
			// 设置 WebDataBinderFactory 以传递给参数解析器，允许它们创建 WebDataBinder 用于数据绑定和类型转换。
			invocableHandlerMethod.setDataBinderFactory(dataBinderFactory);
			// 设置 MethodValidator 以执行方法验证，如果控制器方法 shouldValidateArguments() 或 shouldValidateReturnValue()。
			invocableHandlerMethod.setMethodValidator(methodValidator);

			// 在解析给定请求上下文中的参数值后，调用该方法。
			// invocableHandlerMethod.invokeForRequest()
		}
	}
}
/*
********************************* Class API Docs *********************************
HandlerMethod 的扩展，通过 HandlerMethodArgumentResolver 列表，使用从当前 HTTP 请求解析的参数值来调用底层方法。

********************************* Class Definition *********************************
public class InvocableHandlerMethod extends HandlerMethod {
	private static final Object[] EMPTY_ARGS = new Object[0];
	private static final Class<?>[] EMPTY_GROUPS = new Class<?>[0];

	private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();
	private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	private WebDataBinderFactory dataBinderFactory;
	private MethodValidator methodValidator;
	private Class<?>[] validationGroups = EMPTY_GROUPS;
	// ...
}
**/
