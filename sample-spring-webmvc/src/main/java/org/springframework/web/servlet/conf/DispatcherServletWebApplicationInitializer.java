package org.springframework.web.servlet.conf;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @see org.springframework.web.WebApplicationInitializer
 */
public class DispatcherServletWebApplicationInitializer implements WebApplicationInitializer {

	/**
	 * 指定 {@link org.springframework.context.ApplicationContext} 创建 {@link DispatcherServlet}
	 */
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // Load Spring web application configuration
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.scan("org.springframework.web.servlet.conf", "org.springframework.web.servlet.controller");

        // Create and register the DispatcherServlet
        DispatcherServlet servlet = new DispatcherServlet(context);
		servlet.setContextConfigLocation("org.springframework.web.servlet");
        ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }

// 	/**
//	 * 不指定 {@link org.springframework.context.ApplicationContext} 创建 {@link DispatcherServlet}
//	 * <p>
//	 * 需要文件：webapp/WEB-INF/app-servlet.xml；文件名的来源参考 @see
//	 * @see org.springframework.web.servlet.FrameworkServlet#configureAndRefreshWebApplicationContext(org.springframework.web.context.ConfigurableWebApplicationContext)
//	 * @see org.springframework.web.servlet.FrameworkServlet#getNamespace()
//	 * @see org.springframework.web.context.ConfigurableWebApplicationContext#setNamespace(String)
//	 * @see org.springframework.web.context.support.XmlWebApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
//	 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext#getConfigLocations()
//	 * @see org.springframework.web.context.support.XmlWebApplicationContext#getDefaultConfigLocations()
//	 */
//	@Override
//	public void onStartup(ServletContext servletContext) throws ServletException {
//		// Create and register the DispatcherServlet
//		DispatcherServlet servlet = new DispatcherServlet();
//
//		ServletRegistration.Dynamic registration = servletContext.addServlet("app", servlet);
//		registration.setLoadOnStartup(1);
//		registration.addMapping("/");
//	}
}