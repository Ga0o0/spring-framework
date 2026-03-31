/**
 * Servlet#init(jakarta.servlet.ServletConfig)
 *
 * @see jakarta.servlet.Servlet#init(jakarta.servlet.ServletConfig)
 */
package org.springframework.web.servlet._mine.servlet.servlet01_init;

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

/**
 * Servlet#init(jakarta.servlet.ServletConfig) - DispatcherServlet#initStrategies(...)
 *
 * @see org.springframework.web.servlet.DispatcherServlet#initStrategies(org.springframework.context.ApplicationContext)
 *
 * ## 初始化 Servlet 使用的策略对象
 *
 * @see org.springframework.web.servlet.DispatcherServlet#initMultipartResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initLocaleResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initThemeResolver(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerMappings(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerAdapters(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initHandlerExceptionResolvers(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initRequestToViewNameTranslator(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initViewResolvers(org.springframework.context.ApplicationContext)
 * @see org.springframework.web.servlet.DispatcherServlet#initFlashMapManager(org.springframework.context.ApplicationContext)
 *
 * ## 从文件 org/springframework/web/servlet/DispatcherServlet.properties 中获取默认策略，并进行初始化
 *
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultStrategy(org.springframework.context.ApplicationContext, java.lang.Class)
 * @see org.springframework.web.servlet.DispatcherServlet#getDefaultStrategies(org.springframework.context.ApplicationContext, java.lang.Class)
 */