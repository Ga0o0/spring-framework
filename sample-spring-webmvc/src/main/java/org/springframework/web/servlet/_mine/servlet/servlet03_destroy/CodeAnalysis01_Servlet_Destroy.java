package org.springframework.web.servlet._mine.servlet.servlet03_destroy;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * @see jakarta.servlet.Servlet#destroy()
 *
 * @see org.springframework.web.servlet.FrameworkServlet#destroy()
 * @see org.springframework.context.ConfigurableApplicationContext#close()
 */
public class CodeAnalysis01_Servlet_Destroy {
	static abstract class CA01_FrameworkServlet extends FrameworkServlet {
		private static final long serialVersionUID = 1L;
		// 此 servlet 的 WebApplicationContext。
		private WebApplicationContext webApplicationContext;
		// WebApplicationContext 是否通过 {@link #setApplicationContext} 注入。
		private boolean webApplicationContextInjected = false;

		// 关闭此 servlet 的 WebApplicationContext。
		// @see org.springframework.context.ConfigurableApplicationContext#close()
		@Override
		public void destroy() {
			getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
			// Only call close() on WebApplicationContext if locally managed...
			if (!this.webApplicationContextInjected &&
					this.webApplicationContext instanceof ConfigurableApplicationContext cac) {
				cac.close(); // -> ConfigurableApplicationContext#close()
			}
		}
	}
}
