package org.springframework.web.servlet._mine.servlet.servlet02_service;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HttpServletBean;

import java.io.IOException;

/**
 * Servlet#service(...) - FrameworkServlet#doXxx(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * @see jakarta.servlet.Servlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 *
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see jakarta.servlet.http.HttpServlet#service(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 *
 * ## 处理各种请求
 *
 * @see org.springframework.web.servlet.FrameworkServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doHead(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doPost(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doPut(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doDelete(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doOptions(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 * @see org.springframework.web.servlet.FrameworkServlet#doTrace(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
 */
public class CodeAnalysis02_FrameworkServlet_doXxx {

	// public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware { ... }
	static abstract class CA02_FrameworkServlet extends HttpServletBean {
		private static final long serialVersionUID = 1L;
		// 是否应该将 HTTP OPTIONS 请求发送到 {@link #doService}？
		private boolean dispatchOptionsRequest = false;
		// 是否应该将 HTTP TRACE 请求发送到 {@link #doService}？
		private boolean dispatchTraceRequest = false;

		// 将 GET 请求委托给 processRequest/doService。
		// <p>也将由 HttpServlet 的 {@code doHead} 默认实现调用，并使用仅捕获内容长度的 {@code NoBodyResponse}。
		@Override
		protected final void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			processRequest(request, response);  // important -> go
		}

		// 将 POST 请求委托给 {@link #processRequest}
		@Override
		protected final void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			processRequest(request, response);
		}

		// 将 PUT 请求委托给 {@link #processRequest}。
		@Override
		protected final void doPut(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			processRequest(request, response);
		}

		//将 DELETE 请求委托给 {@link #processRequest}。
		@Override
		protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			processRequest(request, response);
		}

		// 如果需要，将 OPTIONS 请求委托给 {@link #processRequest}。
		// <p>否则，应用 HttpServlet 的标准 OPTIONS 处理，并且如果调度后仍未设置“Allow”标头，也同样如此。
		@Override
		protected void doOptions(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			if (this.dispatchOptionsRequest || CorsUtils.isPreFlightRequest(request)) {
				processRequest(request, response);
				if (response.containsHeader(HttpHeaders.ALLOW)) {
					// 来自处理程序的正确 OPTIONS 响应 - 我们完成了。
					return;
				}
			}

			// 使用响应包装器以便始终将 PATCH 添加到允许的方法中
			super.doOptions(request, new HttpServletResponseWrapper(response) {
				@Override
				public void setHeader(String name, String value) {
					if (HttpHeaders.ALLOW.equals(name)) {
						value = (StringUtils.hasLength(value) ? value + ", " : "") + HttpMethod.PATCH.name();
					}
					super.setHeader(name, value);
				}
			});
		}

		// 如果需要，将 TRACE 请求委托给 {@link #processRequest}。
		// <p>否则，应用 HttpServlet 的标准 TRACE 处理。
		// @see #doService
		@Override
		protected void doTrace(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			if (this.dispatchTraceRequest) {
				processRequest(request, response);
				if ("message/http".equals(response.getContentType())) {
					// Proper TRACE response coming from a handler - we're done.
					return;
				}
			}
			// Work around until https://github.com/jakartaee/servlet/pull/545 is fixed and in use
			if (request.getDispatcherType() != DispatcherType.ERROR) {
				super.doTrace(request, response);
			}
		}

		// 处理此请求，无论结果如何，都发布一个事件。
		// <p>实际的事件处理由抽象 {@link #doService} 模板方法执行。
		protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			// ...
		}

	}
}
