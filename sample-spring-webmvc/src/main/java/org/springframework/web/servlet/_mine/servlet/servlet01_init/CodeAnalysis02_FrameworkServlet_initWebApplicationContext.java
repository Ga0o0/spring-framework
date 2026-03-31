package org.springframework.web.servlet._mine.servlet.servlet01_init;

import jakarta.servlet.ServletException;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HttpServletBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Servlet#init(jakarta.servlet.ServletConfig) - FrameworkServlet#initWebApplicationContext()
 *
 * @see jakarta.servlet.Servlet#init(jakarta.servlet.ServletConfig)
 *
 * @see jakarta.servlet.http.HttpServlet#init(jakarta.servlet.ServletConfig)
 * @see jakarta.servlet.GenericServlet#init(jakarta.servlet.ServletConfig)
 *
 * @see org.springframework.web.servlet.HttpServletBean#init()
 * @see org.springframework.web.servlet.FrameworkServlet#initServletBean()
 *
 * ## 1. FrameworkServlet#initWebApplicationContext()
 *
 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext()
 *
 * ### 1.1. 在构造时注入了上下文实例 -> 使用它
 *
 * @see org.springframework.web.servlet.FrameworkServlet#configureAndRefreshWebApplicationContext(org.springframework.web.context.ConfigurableWebApplicationContext)
 *
 * ### 1.2. 构造时未注入上下文实例 -> 检查 Servlet 上下文中是否已注册
 *
 * @see org.springframework.web.servlet.FrameworkServlet#findWebApplicationContext()
 *
 * ### 1.3. Servlet 没有定义上下文实例 -> 创建一个本地实例
 *
 * @see org.springframework.web.servlet.FrameworkServlet#createWebApplicationContext(org.springframework.web.context.WebApplicationContext)
 *
 * ### 1.4. 要么上下文不是具有刷新支持的 ConfigurableApplicationContext，要么在构造时注入的上下文已经被刷新 -> 在这里手动触发初始 onRefresh
 *
 * @see org.springframework.web.servlet.FrameworkServlet#onRefresh(org.springframework.context.ApplicationContext)
 */
public class CodeAnalysis02_FrameworkServlet_initWebApplicationContext {

	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#initServletBean()
	 */
	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	static abstract class CA01_FrameworkServlet extends HttpServletBean {
		private static final long serialVersionUID = 1L;
		// WebApplicationContext 命名空间的后缀。如果此类的 servlet 在上下文中被赋予名称“test”，则该 servlet 使用的命名空间将解析为“test-servlet”。
		public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";
		// 任意数量的这些字符都被视为单个 init-param 字符串值中多个值之间的分隔符。
		private static final String INIT_PARAM_DELIMITERS = ",; \t\n";
		// 此 servlet 的 WebApplicationContext。
		private WebApplicationContext webApplicationContext;
		// 用于检测 onRefresh 是否已被调用的标志。
		private volatile boolean refreshEventReceived;
		// 同步 onRefresh 执行的监视器。
		private final Object onRefreshMonitor = new Object();
		// 我们是否应该将上下文发布为 ServletContext 属性？
		private boolean publishContext = true;
		// 要分配的 WebApplicationContext ID。
		private String contextId;
		// 此 servlet 的命名空间。
		private String namespace;
		// 要应用于上下文的实际 ApplicationContextInitializer 实例。
		private final List<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
				new ArrayList<>();
		// 通过 init 参数设置的 ApplicationContextInitializer 类名，以逗号分隔。
		private String contextInitializerClasses;
		// 用于查找 WebApplicationContext 的 ServletContext 属性。
		private String contextAttribute;
		// FrameworkServlet 的默认上下文类。
		public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;
		// 要创建的 WebApplicationContext 实现类。
		private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;
		// 显式上下文配置位置。
		private String contextConfigLocation;
		// WebApplicationContext 的 ServletContext 属性的前缀。补全内容为 servlet 名称。
		public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

		// {@link HttpServletBean} 的重写方法，在设置所有 bean 属性后调用。创建此 servlet 的 WebApplicationContext。
		@Override
		protected final void initServletBean() throws ServletException {
			// ...
			try {
				// 初始化并发布此 servlet 的 WebApplicationContext。
				this.webApplicationContext = initWebApplicationContext(); // important -> go
				initFrameworkServlet();
			}
			catch (ServletException | RuntimeException ex) {
				// ...
				throw ex;
			}
			// ...
		}

		/**
		 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext()
		 */
		// 初始化并发布此 servlet 的 WebApplicationContext。
		// <p>委托 {@link #createWebApplicationContext} 实际创建上下文。可在子类中重写。@return WebApplicationContext 实例
		protected WebApplicationContext initWebApplicationContext() {
			// // 查找此 Web 应用的根 {@code WebApplicationContext}，通常通过 {@link org.springframework.web.context.ContextLoaderListener} 加载。
			WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
			WebApplicationContext wac = null;

			if (this.webApplicationContext != null) {
				// 1. 在构造时注入了上下文实例 -> 使用它
				wac = this.webApplicationContext;
				if (wac instanceof ConfigurableWebApplicationContext cwac && !cwac.isActive()) {
					// 上下文还未刷新 -> 提供设置父上下文、设置应用上下文id等服务
					if (cwac.getParent() == null) {
						// 上下文实例在没有明确父级的情况下被注入 -> 将根应用程序上下文（如果有；可能为空）设置为父级
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);  // important -> go
				}
			}
			if (wac == null) {
				// 2. 构造时未注入上下文实例 -> 检查 Servlet 上下文中是否已注册。如果存在，则假定父上下文（如果有）已设置，并且用户已执行任何初始化操作，例如设置上下文 ID
				wac = findWebApplicationContext();  // important -> go
			}
			if (wac == null) {
				// 3. 此 servlet 没有定义上下文实例 -> 创建一个本地实例
				wac = createWebApplicationContext(rootContext);  // important -> go
			}

			if (!this.refreshEventReceived) {
				// 4. 要么上下文不是具有刷新支持的 ConfigurableApplicationContext，要么在构造时注入的上下文已经被刷新 -> 在这里手动触发初始 onRefresh。
				synchronized (this.onRefreshMonitor) {
					onRefresh(wac);  // important -> go // important -> go // important -> go // important -> go
				}
			}

			if (this.publishContext) {
				// 5. 将上下文发布为 Servlet 上下文属性。
				String attrName = getServletContextAttributeName();
				getServletContext().setAttribute(attrName, wac);
			}

			return wac;
		}

		// 使用 {@link #setContextAttribute 配置名称} 从 {@code ServletContext} 属性中检索 {@code WebApplicationContext}。在初始化（或调用）此 servlet 之前，
		// 必须已加载 {@code WebApplicationContext} 并将其存储在 {@code ServletContext} 中。
		// <p>子类可以重写此方法，以提供不同的 {@code WebApplicationContext} 检索策略。
		// @return 此 servlet 的 WebApplicationContext，如果未找到，则返回 {@code null}
		@Nullable
		protected WebApplicationContext findWebApplicationContext() {
			String attrName = getContextAttribute();
			if (attrName == null) {
				return null;
			}
			WebApplicationContext wac =
					WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
			if (wac == null) {
				throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
			}
			return wac;
		}

		// 返回用于检索此 servlet 应该使用的 {@link WebApplicationContext} 的 ServletContext 属性的名称。
		@Nullable
		public String getContextAttribute() {
			return this.contextAttribute;
		}

		// 实例化此 servlet 的 WebApplicationContext，可以是默认的 {@link org.springframework.web.context.support.XmlWebApplicationContext} 或
		// {@link #setContextClass 自定义上下文类}（如果已设置）。委托给 #createWebApplicationContext(ApplicationContext)。
		// @param parent 要使用的父 WebApplicationContext，如果没有则返回 {@code null}
		// @return 此 servlet 的 WebApplicationContext
		protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
			return createWebApplicationContext((ApplicationContext) parent);
		}

		// 实例化此 servlet 的 WebApplicationContext，可以是默认的 {@link org.springframework.web.context.support.XmlWebApplicationContext} 或
		// {@link #setContextClass 自定义上下文类}（如果已设置）。
		// <p>此实现要求自定义上下文实现 {@link org.springframework.web.context.ConfigurableWebApplicationContext} 接口。可在子类中重写。
		// <p>不要忘记将此 servlet 实例注册为创建的上下文上的应用程序监听器（用于触发其 {@link #onRefresh 回调}），
		// 并在返回上下文实例之前调用 {@link org.springframework.context.ConfigurableApplicationContext#refresh()}。
		// @param parent 要使用的父 ApplicationContext，如果没有则返回 {@code null}
		// @return 此 servlet 的 WebApplicationContext
		protected WebApplicationContext createWebApplicationContext(@Nullable ApplicationContext parent) {
			// 默认值：XmlWebApplicationContext.class
			Class<?> contextClass = getContextClass();	// important -> go
			if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
				throw new ApplicationContextException(
						"Fatal initialization error in servlet with name '" + getServletName() +
								"': custom WebApplicationContext class [" + contextClass.getName() +
								"] is not of type ConfigurableWebApplicationContext");
			}
			// 使用类的 “主” 构造函数或其默认构造函数来实例化该类。
			ConfigurableWebApplicationContext wac =
					(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

			wac.setEnvironment(getEnvironment());
			wac.setParent(parent);
			String configLocation = getContextConfigLocation();
			if (configLocation != null) {
				wac.setConfigLocation(configLocation);
			}
			configureAndRefreshWebApplicationContext(wac); // important -> go

			return wac;
		}

		// 返回自定义上下文类。
		public Class<?> getContextClass() {
			// 默认值：XmlWebApplicationContext.class
			return this.contextClass;
		}

		// 返回显式上下文配置位置（如有）。
		@Nullable
		public String getContextConfigLocation() {
			return this.contextConfigLocation;
		}

		// 返回此 servlet 的 WebApplicationContext 的 ServletContext 属性名称。
		// <p>默认实现返回 {@code SERVLET_CONTEXT_PREFIX + servlet 名称}。</p>
		public String getServletContextAttributeName() {
			// SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT."
			return SERVLET_CONTEXT_PREFIX + getServletName();
		}

		protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
			if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
				// 应用程序上下文 id 仍然设置为其原始默认值 -> 根据可用信息分配更有用的 id
				if (this.contextId != null) {
					wac.setId(this.contextId);
				}
				else {
					// 生成默认 ID...
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
							ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
				}
			}

			wac.setServletContext(getServletContext()); // 设置此 Web 应用上下文的 ServletContext。
			wac.setServletConfig(getServletConfig());	// 设置此 Web 应用上下文的 ServletConfig。
			wac.setNamespace(getNamespace());			// 设置此 Web 应用上下文的命名空间，用于构建默认上下文配置位置。
			// 为给定的事件源创建一个 SourceFilteringListener。
			wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener())); // important -> go // important -> go // important -> go

			// 在任何情况下，当上下文刷新时，都会调用 wac 环境的 #initPropertySources；在这里急切地执行此操作，以确保 servlet 属性源已到位，可用于在 #refresh 之前发生的任何后处理或初始化
			ConfigurableEnvironment env = wac.getEnvironment(); // AbstractRefreshableWebApplicationContext.createEnvironment() -> StandardServletEnvironment
			if (env instanceof ConfigurableWebEnvironment cwe) {
				// 使用给定的参数，将任何用作占位符的存根属性源实例替换为真实的 servlet 上下文/配置属性源。
				cwe.initPropertySources(getServletContext(), getServletConfig()); // important -> go
			}

			// 在刷新并激活给定的 WebApplicationContext 作为此 servlet 的上下文之前，对其进行后处理。
			postProcessWebApplicationContext(wac);
			applyInitializers(wac); // important -> go
			wac.refresh(); // important -> go
		}

		// 返回此 servlet 的命名空间，如果没有设置自定义命名空间，则回退到默认方案：例如，对于名为“test”的 servlet，返回“test-servlet”。
		public String getNamespace() {
			return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
		}

		// ApplicationListener 端点仅从此 servlet 的 WebApplicationContext 接收事件，委托给 FrameworkServlet 实例上的 {@code onApplicationEvent}。
		private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
			@Override
			public void onApplicationEvent(ContextRefreshedEvent event) {
				// FrameworkServlet.this.onApplicationEvent(event);
				onApplicationEvent(event);
			}
		}

		// 从此 servlet 的 WebApplicationContext 接收刷新事件的回调。
		// <p>默认实现调用 {@link #onRefresh}，触发此 servlet 上下文相关状态的刷新。
		// @param event 传入的 ApplicationContext 事件
		public void onApplicationEvent(ContextRefreshedEvent event) {
			this.refreshEventReceived = true;
			synchronized (this.onRefreshMonitor) {
				onRefresh(event.getApplicationContext());
			}
		}

		// 可重写模板方法，以添加特定于 servlet 的刷新功能。上下文刷新成功后调用。
		// <p>此实现为空。
		protected void onRefresh(ApplicationContext context) {
			// For subclasses: do nothing by default.
		}

		// 在刷新并激活给定的 WebApplicationContext 作为此 servlet 的上下文之前，对其进行后处理。
		// <p>默认实现为空。此方法返回后，将自动调用 {@code refresh()}。
		// <p>请注意，此方法旨在允许子类修改应用程序上下文，而 {@link #initWebApplicationContext} 旨在
		// 允许最终用户通过使用 {@link ApplicationContextInitializer ApplicationContextInitializers} 修改上下文。
		// @param wac 已配置的 WebApplicationContext（尚未刷新）
		protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
		}

		// 在 WebApplicationContext 刷新之前，将其委托给由 “contextInitializerClasses” servlet 初始化参数指定的任何 {@link ApplicationContextInitializer} 实例。
		// <p>另请参阅 {@link #postProcessWebApplicationContext}，它旨在允许子类（而非最终用户）修改应用程序上下文，并在此方法之前立即调用。
		// @param wac 已配置的 WebApplicationContext（尚未刷新）
		protected void applyInitializers(ConfigurableApplicationContext wac) {
			// GLOBAL_INITIALIZER_CLASSES_PARAM = "globalInitializerClasses"
			String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
			if (globalClassNames != null) {
				for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
					this.contextInitializers.add(loadInitializer(className, wac));
				}
			}

			if (this.contextInitializerClasses != null) {
				for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
					this.contextInitializers.add(loadInitializer(className, wac));
				}
			}

			// 使用默认的 {@link AnnotationAwareOrderComparator} 对给定列表进行排序。
			AnnotationAwareOrderComparator.sort(this.contextInitializers);
			for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
				// invoke org.springframework.context.ApplicationContextInitializer.initialize()
				initializer.initialize(wac); // important -> go
			}
		}

		@SuppressWarnings("unchecked")
		private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(
				String className, ConfigurableApplicationContext wac) {
			try {
				Class<?> initializerClass = ClassUtils.forName(className, wac.getClassLoader());
				Class<?> initializerContextClass =
						GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
				if (initializerContextClass != null && !initializerContextClass.isInstance(wac)) {
					throw new ApplicationContextException(String.format(
							"Could not apply context initializer [%s] since its generic parameter [%s] " +
									"is not assignable from the type of application context used by this " +
									"framework servlet: [%s]", initializerClass.getName(), initializerContextClass.getName(),
							wac.getClass().getName()));
				}
				return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(String.format("Could not load class [%s] specified " +
						"via 'contextInitializerClasses' init-param", className), ex);
			}
		}

		// 在设置所有 bean 属性并加载 WebApplicationContext 后，将调用此方法。
		// 默认实现为空；子类可以重写此方法以执行所需的任何初始化操作。
		// @throws ServletException 如果发生初始化异常
		protected void initFrameworkServlet() throws ServletException {
		}
	}

	/**
	 * @see DispatcherServlet#initStrategies(ApplicationContext)
	 */
	// public class DispatcherServlet extends FrameworkServlet { ... }
	// static abstract class CA02_DispatcherServlet extends DispatcherServlet {
	static abstract class CA02_DispatcherServlet extends CA01_FrameworkServlet {
		private static final long serialVersionUID = 1L;
		// 此实现调用{@link #initStrategies}。
		@Override
		protected void onRefresh(ApplicationContext context) {
			initStrategies(context);
		}
		// 初始化此 Servlet 使用的策略对象。
		// <p>可以在子类中被覆盖，以便初始化更多的策略对象。
		protected void initStrategies(ApplicationContext context) {
			// ...
		}
	}

}
