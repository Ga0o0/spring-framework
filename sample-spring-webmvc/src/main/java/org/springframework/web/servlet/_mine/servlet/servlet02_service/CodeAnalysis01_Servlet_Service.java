package org.springframework.web.servlet._mine.servlet.servlet02_service;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Servlet#service(...) - HttpServlet#service(...)
 *
 * @see jakarta.servlet.Servlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 *
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */
public class CodeAnalysis01_Servlet_Service {

	/**
	 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
	 */
	// public abstract class HttpServlet extends GenericServlet { ... }
	static abstract class CA01_HttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		private static final String METHOD_DELETE = "DELETE";
		private static final String METHOD_HEAD = "HEAD";
		private static final String METHOD_GET = "GET";
		private static final String METHOD_OPTIONS = "OPTIONS";
		private static final String METHOD_POST = "POST";
		private static final String METHOD_PUT = "PUT";
		private static final String METHOD_TRACE = "TRACE";

		private static final String HEADER_IFMODSINCE = "If-Modified-Since";
		private static final String HEADER_LASTMOD = "Last-Modified";

		private static final String LSTRING_FILE = "jakarta.servlet.http.LocalStrings";
		private static final ResourceBundle lStrings = ResourceBundle.getBundle(LSTRING_FILE);

		@Override
		public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
			HttpServletRequest request;
			HttpServletResponse response;
			try {
				request = (HttpServletRequest) req;
				response = (HttpServletResponse) res;
			} catch (ClassCastException e) {
				throw new ServletException(lStrings.getString("http.non_http"));
			}
			// -> HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
			service(request, response); // important -> go
		}

		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String method = req.getMethod();
			if (method.equals(METHOD_GET)) {
				long lastModified = getLastModified(req);
				if (lastModified == -1) {
					// servlet 不支持 if-modified-since，因此没有必要执行更复杂的逻辑。
					doGet(req, resp);
				} else {
					long ifModifiedSince;
					try {
						ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
					} catch (IllegalArgumentException iae) {
						// 日期标头无效 - 如同未设置日期标头一样继续执行
						ifModifiedSince = -1;
					}
					if (ifModifiedSince < (lastModified / 1000 * 1000)) {
						// 如果 servlet 修改时间较晚，则调用 doGet() 方法。
						// 为了进行正确的比较，请向下取整到最接近的秒。ifModifiedSince 设置为 -1 时，结果始终会较晚。
						maybeSetLastModified(resp, lastModified);
						doGet(req, resp);
					} else {
						resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					}
				}
			} else if (method.equals(METHOD_HEAD)) {
				long lastModified = getLastModified(req);
				maybeSetLastModified(resp, lastModified);
				doHead(req, resp);
			} else if (method.equals(METHOD_POST)) {
				doPost(req, resp);
			} else if (method.equals(METHOD_PUT)) {
				doPut(req, resp);
			} else if (method.equals(METHOD_DELETE)) {
				doDelete(req, resp);
			} else if (method.equals(METHOD_OPTIONS)) {
				doOptions(req, resp);
			} else if (method.equals(METHOD_TRACE)) {
				doTrace(req, resp);
			} else {
				// 请注意，这意味着此服务器上的任何位置都没有任何 servlet 支持所请求的方法。
				String errMsg = lStrings.getString("http.method_not_implemented");
				Object[] errArgs = new Object[1];
				errArgs[0] = method;
				errMsg = MessageFormat.format(errMsg, errArgs);

				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
			}
		}

		// 如果尚未设置且值有意义，则设置 Last-Modified 实体标头字段。
		// 此方法在 doGet 之前调用，以确保在写入响应数据之前设置标头。子类可能已经设置了此标头，因此我们需要进行检查。
		private void maybeSetLastModified(HttpServletResponse resp, long lastModified) {
			if (resp.containsHeader(HEADER_LASTMOD)) {
				return;
			}
			if (lastModified >= 0) {
				resp.setDateHeader(HEADER_LASTMOD, lastModified);
			}
		}
	}

	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	// public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware { ... }
	static abstract class CA02_HttpServletBean extends CA01_HttpServlet {
		private static final long serialVersionUID = 1L;
	}
	static abstract class CA03_FrameworkServlet extends CA02_HttpServletBean {
		private static final long serialVersionUID = 1L;
		// {@link jakarta.servlet.http.HttpServlet} 支持的 HTTP 方法。
		private static final Set<String> HTTP_SERVLET_METHODS =
				Set.of("DELETE", "HEAD", "GET", "OPTIONS", "POST", "PUT", "TRACE");

		// 覆盖父类实现，以便使用 PATCH 或非标准 HTTP 方法 （WebDAV） 拦截请求。
		@Override
		protected void service(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException { // override from HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)

			if (HTTP_SERVLET_METHODS.contains(request.getMethod())) {
				// -> HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
				super.service(request, response);  // important -> go
			}
			else {
				processRequest(request, response);
			}
		}

		protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			// ...
		}
	}

}
