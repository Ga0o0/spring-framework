package org.springframework.web.servlet._mine.servlet.servlet01_init;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.HttpServletBean;

/**
 * Servlet#init(jakarta.servlet.ServletConfig) - 事件 ContextRefreshedEvent /事件监听器 ContextRefreshListener
 *
 * @see jakarta.servlet.Servlet#init(jakarta.servlet.ServletConfig)
 * @see jakarta.servlet.http.HttpServlet#init(jakarta.servlet.ServletConfig)
 * @see jakarta.servlet.GenericServlet#init(jakarta.servlet.ServletConfig)
 *
 * @see org.springframework.web.servlet.HttpServletBean#init()
 * @see org.springframework.web.servlet.FrameworkServlet#initServletBean()
 *
 * @see org.springframework.web.servlet.FrameworkServlet#initWebApplicationContext()
 * @see org.springframework.web.servlet.FrameworkServlet#configureAndRefreshWebApplicationContext(org.springframework.web.context.ConfigurableWebApplicationContext)
 *
 * ## 1. 发布 ContextRefreshedEvent 事件
 *
 * @see org.springframework.context.support.AbstractApplicationContext#refresh()
 * @see org.springframework.context.support.AbstractApplicationContext#finishRefresh()
 * @see org.springframework.context.support.AbstractApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
 * @see org.springframework.context.event.ContextRefreshedEvent
 *
 * ## 2. 事件 ContextRefreshedEvent 的监听器 ContextRefreshListener
 *
 * @see org.springframework.context.ConfigurableApplicationContext#addApplicationListener(org.springframework.context.ApplicationListener)
 *
 * @see org.springframework.web.servlet.FrameworkServlet.ContextRefreshListener
 * @see org.springframework.web.servlet.FrameworkServlet.ContextRefreshListener#onApplicationEvent(org.springframework.context.event.ContextRefreshedEvent)
 * @see org.springframework.web.servlet.FrameworkServlet#onApplicationEvent(org.springframework.context.event.ContextRefreshedEvent)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#onRefresh(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initStrategies(org.springframework.context.ApplicationContext)
 */
public class CodeAnalysis03_ContextRefreshListener {

	/**
	 * 1. 发布 ContextRefreshedEvent 事件
	 *
	 * @see org.springframework.context.support.AbstractApplicationContext#finishRefresh()
	 */
	// public abstract class AbstractApplicationContext extends DefaultResourceLoader
	//		implements ConfigurableApplicationContext { ... }
	static abstract class CA01_AbstractApplicationContext extends AbstractApplicationContext {
		@Override
		public void refresh() throws BeansException, IllegalStateException {
			// ...
			// 完成此上下文的刷新，调用 LifecycleProcessor 的 onRefresh() 方法并发布 {@link org.springframework.context.event.ContextRefreshedEvent}。
			finishRefresh(); // important -> go
			// ...
		}

		// 完成此上下文的刷新，调用 LifecycleProcessor 的 onRefresh() 方法并发布 {@link org.springframework.context.event.ContextRefreshedEvent}。
		protected void finishRefresh() {
			// ...
			// 发布最终事件
			publishEvent(new ContextRefreshedEvent(this)); // important -> go
		}
	}

	/**
	 * 2. 事件 ContextRefreshedEvent 的监听器 ContextRefreshListener - 1
	 *
	 * @see org.springframework.web.servlet.FrameworkServlet#configureAndRefreshWebApplicationContext(org.springframework.web.context.ConfigurableWebApplicationContext)
	 */
	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	static abstract class CA02_FrameworkServlet extends HttpServletBean {
		private static final long serialVersionUID = 1L;
		// 用于检测 onRefresh 是否已被调用的标志。
		private volatile boolean refreshEventReceived;
		// 同步 onRefresh 执行的监视器。
		private final Object onRefreshMonitor = new Object();

		protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
			// ...
			// 为给定的事件源创建一个 SourceFilteringListener。
			wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener())); // important -> go
			// ...
		}

		// ApplicationListener 端点仅从此 servlet 的 WebApplicationContext 接收事件，委托给 FrameworkServlet 实例上的 {@code onApplicationEvent}。
		private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
			@Override
			public void onApplicationEvent(ContextRefreshedEvent event) {
				// FrameworkServlet.this.onApplicationEvent(event);
				CA02_FrameworkServlet.this.onApplicationEvent(event); // important -> go
			}
		}

		// 从此 servlet 的 WebApplicationContext 接收刷新事件的回调。
		// <p>默认实现调用 {@link #onRefresh}，触发此 servlet 上下文相关状态的刷新。
		// @param event 传入的 ApplicationContext 事件
		public void onApplicationEvent(ContextRefreshedEvent event) {
			this.refreshEventReceived = true;
			synchronized (this.onRefreshMonitor) {
				onRefresh(event.getApplicationContext()); // important -> go
			}
		}

		// 可重写模板方法，以添加特定于 servlet 的刷新功能。上下文刷新成功后调用。
		// <p>此实现为空。
		protected void onRefresh(ApplicationContext context) {
			// For subclasses: do nothing by default.
		}
	}

	/**
	 * 2. 事件 ContextRefreshedEvent 的监听器 ContextRefreshListener - 2
	 *
	 * @see org.springframework.web.servlet.DispatcherServlet#onRefresh(org.springframework.context.ApplicationContext)
	 */
	// public class DispatcherServlet extends FrameworkServlet { ... }
	static abstract class CA03_DispatcherServlet extends CA02_FrameworkServlet {
		private static final long serialVersionUID = 1L;
		// 此实现调用{@link #initStrategies}。
		@Override
		protected void onRefresh(ApplicationContext context) {
			initStrategies(context); // important -> go
		}
		// 初始化此 Servlet 使用的策略对象。
		// <p>可以在子类中被覆盖，以便初始化更多的策略对象。
		protected void initStrategies(ApplicationContext context) {
			// ...
		}
	}

}
