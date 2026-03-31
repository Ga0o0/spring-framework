package org.springframework.web.servlet._mine.servlet.servlet01_init;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HttpServletBean;

import java.util.HashSet;
import java.util.Set;

/**
 * Servlet#init(jakarta.servlet.ServletConfig) - FrameworkServlet#initServletBean()
 *
 * @see jakarta.servlet.Servlet#init(jakarta.servlet.ServletConfig)
 *
 * @see jakarta.servlet.http.HttpServlet#init(jakarta.servlet.ServletConfig)
 * @see jakarta.servlet.GenericServlet#init(jakarta.servlet.ServletConfig)
 *
 * @see org.springframework.web.servlet.HttpServletBean#init()
 * @see org.springframework.web.servlet.FrameworkServlet#initServletBean()
 */
public class CodeAnalysis01_Servlet_Init {

	/**
	 * @see jakarta.servlet.GenericServlet#init(jakarta.servlet.ServletConfig)
	 */
	// public abstract class GenericServlet implements Servlet, ServletConfig, java.io.Serializable { ... }
	static abstract class CA01_GenericServlet implements Servlet {
		private transient ServletConfig config;
		@Override
		public void init(ServletConfig config) throws ServletException {
			this.config = config;
			this.init(); // important -> go
		}

		public void init() throws ServletException {
			// NOOP by default
		}
	}

	/**
	 * @see org.springframework.web.servlet.HttpServletBean#init()
	 */
	// public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware { ... }
	static class CA02_HttpServletBean extends HttpServlet {
		private static final long serialVersionUID = 1L;
		private ConfigurableEnvironment environment;
		private final Set<String> requiredProperties = new HashSet<>(4);
		// ...

		// 将配置参数映射到此 servlet 的 bean 属性上，并调用子类初始化。
		// @throws ServletException 如果 bean 属性无效（或缺少必需属性），或者子类初始化失败
		@Override
		public final void init() throws ServletException {
			// 从初始化参数设置 bean 属性。
			/*PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties); // 创建新的 ServletConfigPropertyValues。
			if (!pvs.isEmpty()) {
				try {
					// 获取给定目标对象的 BeanWrapper，以 JavaBean 风格访问属性。
					BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
					// 创建一个新的 ServletContextResourceLoader。
					ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
					// 为指定类型的所有属性注册指定的自定义属性编辑器。
					bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
					initBeanWrapper(bw); // 此默认实现为空。
					bw.setPropertyValues(pvs, true); // 执行批量更新，更好地控制行为。
				}
				catch (BeansException ex) {
					// ...
					throw ex;
				}
			}*/

			// 让子类做任何它们喜欢的初始化。
			// 子类可以重写此方法以执行自定义初始化。此 servlet 的所有 bean 属性都将在调用此方法之前设置。<p>此默认实现为空。
			initServletBean(); // important -> go
		}
		// ...

		// 子类可以重写此方法以执行自定义初始化。此 servlet 的所有 bean 属性都将在调用此方法之前设置。
		// <p>此默认实现为空。
		// @throws ServletException 如果子类初始化失败
		protected void initServletBean() throws ServletException {
		}
		// ...
	}

	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#initServletBean()
	 */
	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	static abstract class CA03_FrameworkServlet extends HttpServletBean {
		private static final long serialVersionUID = 1L;
		// 此 servlet 的 WebApplicationContext。
		private WebApplicationContext webApplicationContext;

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

		// 初始化并发布此 servlet 的 WebApplicationContext。
		// <p>委托 {@link #createWebApplicationContext} 实际创建上下文。可在子类中重写。@return WebApplicationContext 实例
		protected WebApplicationContext initWebApplicationContext() {
			// ...
			return null;
		}

		// 在设置所有 bean 属性并加载 WebApplicationContext 后，将调用此方法。
		// 默认实现为空；子类可以重写此方法以执行所需的任何初始化操作。
		// @throws ServletException 如果发生初始化异常
		protected void initFrameworkServlet() throws ServletException {
		}
	}


}
