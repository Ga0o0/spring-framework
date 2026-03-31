package org.springframework.web.servlet._mine.web13_model_and_view;

/**
 * ModelAndViewContainer
 *
 * @see org.springframework.web.method.support.ModelAndViewContainer
 */
public class MV02_ModelAndViewContainer {

	/**
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setViewName(String)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getViewName()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setView(Object)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getView()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#isViewReference()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getModel()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getDefaultModel()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setRedirectModel(org.springframework.ui.ModelMap)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setRedirectModelScenario(boolean)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setStatus(org.springframework.http.HttpStatusCode)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getStatus()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setBindingDisabled(String)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#isBindingDisabled(String)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setBinding(String, boolean)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#getSessionStatus()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#setRequestHandled(boolean)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#isRequestHandled()
	 * @see org.springframework.web.method.support.ModelAndViewContainer#addAttribute(String, Object)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#addAttribute(Object)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#addAllAttributes(java.util.Map)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#mergeAttributes(java.util.Map)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#removeAttributes(java.util.Map)
	 * @see org.springframework.web.method.support.ModelAndViewContainer#containsAttribute(String)
	 */
	public static void main(String[] args) {}
}
/*
********************************* Class API Docs *********************************
记录 HandlerMethodArgumentResolvers 和 HandlerMethodReturnValueHandlers 在调用控制器方法过程中做出的与模型和视图相关的决策。

<p>ModelAndViewContainer#setRequestHandled() 标志可用于指示请求已被直接处理，并且不需要视图解析。

<p>实例化时会自动创建默认的  Model。可通过 ModelAndViewContainer#setRedirectModel() 提供备用模型实例，以用于重定向场景。
当 ModelAndViewContainer#setRedirectModelScenario() 设置为 true 以表示重定向场景时，ModelAndViewContainer#getModel() 将返回重定向模型而不是默认模型。

********************************* Class Definition *********************************
public class ModelAndViewContainer {
	private boolean ignoreDefaultModelOnRedirect = true;1
	private Object view;
	private final ModelMap defaultModel = new BindingAwareModelMap();
	private ModelMap redirectModel;
	private boolean redirectModelScenario = false;
	private HttpStatusCode status;
	private final Set<String> noBinding = new HashSet<>(4);
	private final Set<String> bindingDisabled = new HashSet<>(4);
	private final SessionStatus sessionStatus = new SimpleSessionStatus();
	private boolean requestHandled = false;
	// ...
}
**/