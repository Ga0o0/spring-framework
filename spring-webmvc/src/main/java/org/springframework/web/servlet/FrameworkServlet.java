/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.util.WebUtils;

/**
 * Base servlet for Spring's web framework. Provides integration with
 * a Spring application context, in a JavaBean-based overall solution.
 *
 * <p>This class offers the following functionality:
 * <ul>
 * <li>Manages a {@link org.springframework.web.context.WebApplicationContext
 * WebApplicationContext} instance per servlet. The servlet's configuration is determined
 * by beans in the servlet's namespace.
 * <li>Publishes events on request processing, whether or not a request is
 * successfully handled.
 * </ul>
 *
 * <p>Subclasses must implement {@link #doService} to handle requests. Because this extends
 * {@link HttpServletBean} rather than HttpServlet directly, bean properties are
 * automatically mapped onto it. Subclasses can override {@link #initFrameworkServlet()}
 * for custom initialization.
 *
 * <p>Detects a "contextClass" parameter at the servlet init-param level,
 * falling back to the default context class,
 * {@link org.springframework.web.context.support.XmlWebApplicationContext
 * XmlWebApplicationContext}, if not found. Note that, with the default
 * {@code FrameworkServlet}, a custom context class needs to implement the
 * {@link org.springframework.web.context.ConfigurableWebApplicationContext
 * ConfigurableWebApplicationContext} SPI.
 *
 * <p>Accepts an optional "contextInitializerClasses" servlet init-param that
 * specifies one or more {@link org.springframework.context.ApplicationContextInitializer
 * ApplicationContextInitializer} classes. The managed web application context will be
 * delegated to these initializers, allowing for additional programmatic configuration,
 * e.g. adding property sources or activating profiles against the {@linkplain
 * org.springframework.context.ConfigurableApplicationContext#getEnvironment() context's
 * environment}. See also {@link org.springframework.web.context.ContextLoader} which
 * supports a "contextInitializerClasses" context-param with identical semantics for
 * the "root" web application context.
 *
 * <p>Passes a "contextConfigLocation" servlet init-param to the context instance,
 * parsing it into potentially multiple file paths which can be separated by any
 * number of commas and spaces, like "test-servlet.xml, myServlet.xml".
 * If not explicitly specified, the context implementation is supposed to build a
 * default location from the namespace of the servlet.
 *
 * <p>Note: In case of multiple config locations, later bean definitions will
 * override ones defined in earlier loaded files, at least when using Spring's
 * default ApplicationContext implementation. This can be leveraged to
 * deliberately override certain bean definitions via an extra XML file.
 *
 * <p>The default namespace is "'servlet-name'-servlet", e.g. "test-servlet" for a
 * servlet-name "test" (leading to a "/WEB-INF/test-servlet.xml" default location
 * with XmlWebApplicationContext). The namespace can also be set explicitly via
 * the "namespace" servlet init-param.
 *
 * <p>{@code FrameworkServlet} may be injected with a web application context,
 * rather than creating its own internally. This is useful in Servlet 3.0+
 * environments, which support programmatic registration of servlet instances. See
 * {@link #FrameworkServlet(WebApplicationContext)} Javadoc for details.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @author Phillip Webb
 * @see #doService
 * @see #setContextClass
 * @see #setContextConfigLocation
 * @see #setContextInitializerClasses
 * @see #setNamespace
 */
// Spring Web 框架的基础 Servlet。通过基于 JavaBean 的整体解决方案，提供与 Spring 应用上下文的集成。
//
// <p>此类提供以下功能：
// <ul>
// <li>为每个 Servlet 管理一个 {@link org.springframework.web.context.WebApplicationContext WebApplicationContext} 实例。
// Servlet 的配置由 Servlet 命名空间中的 Bean 决定。
// <li>无论请求是否成功处理，都会在请求处理时发布事件。
// </ul>
//
// <p>子类必须实现 {@link #doService} 才能处理请求。由于它扩展了 {@link HttpServletBean} 而不是直接扩展了 HttpServlet，因此 Bean 的属性会自动映射到它上面。
// 子类可以重写 {@link #initFrameworkServlet()} 来实现自定义初始化。
//
// <p>在 servlet 初始化参数级别检测 “contextClass” 参数，如果未找到，
// 则回退到默认上下文类 {@link org.springframework.web.context.support.XmlWebApplicationContext XmlWebApplicationContext}。
// 请注意，使用默认的 {@code FrameworkServlet} 时，自定义上下文类需要实现 {@link org.springframework.web.context.ConfigurableWebApplicationContext ConfigurableWebApplicationContext} SPI。
//
// <p>接受可选的“contextInitializerClasses”servlet 初始化参数，该参数指定一个或多个 {@link org.springframework.context.ApplicationContextInitializer ApplicationContextInitializer} 类。
// 托管的 Web 应用程序上下文将委托给这些初始化器，从而允许进行其他编程式配置，例如：添加属性源或根据 {@linkplain org.springframework.context.ConfigurableApplicationContext#getEnvironment() 上下文环境} 激活配置文件。
// 另请参阅 {@link org.springframework.web.context.ContextLoader}，它支持与“根”Web 应用程序上下文语义相同的“contextInitializerClasses”上下文参数。
//
// <p>将“contextConfigLocation”Servlet 初始化参数传递给上下文实例，并将其解析为多个文件路径，这些文件路径可以用任意数量的逗号和空格分隔，例如“test-servlet.xml, myServlet.xml”。
// 如果未明确指定，则上下文实现应该从 Servlet 的命名空间构建一个默认位置。
//
// <p>注意：如果有多个配置位置，后续的 Bean 定义将覆盖先前加载的文件中定义的 Bean 定义，至少在使用 Spring 默认的 ApplicationContext 实现时是这样。
// 可以利用这一点，通过额外的 XML 文件故意覆盖某些 Bean 定义。
//
// <p>默认命名空间为 “'servlet-name'-servlet”，例如，servlet 名称为 “test” 时，
// 默认命名空间为 “test-servlet”（使用 XmlWebApplicationContext 时，默认路径为“/WEB-INF/test-servlet.xml”）。
// 您也可以通过 servlet 初始化参数“namespace”显式设置命名空间。<p>{@code FrameworkServlet} 可以注入 Web 应用上下文，而无需自行创建。
// 这在 Servlet 3.0 及以上版本中非常有用，因为这些版本支持以编程方式注册 servlet 实例。详情请参阅 {@link #FrameworkServlet(WebApplicationContext)} Javadoc。
@SuppressWarnings("serial")
public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware {

	/**
	 * Suffix for WebApplicationContext namespaces. If a servlet of this class is
	 * given the name "test" in a context, the namespace used by the servlet will
	 * resolve to "test-servlet".
	 */
	// WebApplicationContext 命名空间的后缀。如果此类的 servlet 在上下文中被赋予名称“test”，则该 servlet 使用的命名空间将解析为“test-servlet”。
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";

	/**
	 * Default context class for FrameworkServlet.
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	// FrameworkServlet 的默认上下文类。
	public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	/**
	 * Prefix for the ServletContext attribute for the WebApplicationContext.
	 * The completion is the servlet name.
	 */
	// WebApplicationContext 的 ServletContext 属性的前缀。补全内容为 servlet 名称。
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple values in a single init-param String value.
	 */
	// 任意数量的这些字符都被视为单个 init-param 字符串值中多个值之间的分隔符。
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

	/**
	 * HTTP methods supported by {@link jakarta.servlet.http.HttpServlet}.
	 */
	// {@link jakarta.servlet.http.HttpServlet} 支持的 HTTP 方法。
	private static final Set<String> HTTP_SERVLET_METHODS =
			Set.of("DELETE", "HEAD", "GET", "OPTIONS", "POST", "PUT", "TRACE");


	/** ServletContext attribute to find the WebApplicationContext in. */
	// 用于查找 WebApplicationContext 的 ServletContext 属性。
	@Nullable
	private String contextAttribute;

	/** WebApplicationContext implementation class to create. */
	// 要创建的 WebApplicationContext 实现类。
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

	/** WebApplicationContext id to assign. */
	// 要分配的 WebApplicationContext ID。
	@Nullable
	private String contextId;

	/** Namespace for this servlet. */
	// 此 servlet 的命名空间。
	@Nullable
	private String namespace;

	/** Explicit context config location. */
	// 显式上下文配置位置。
	@Nullable
	private String contextConfigLocation;

	/** Actual ApplicationContextInitializer instances to apply to the context. */
	// 要应用于上下文的实际 ApplicationContextInitializer 实例。
	private final List<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
			new ArrayList<>();

	/** Comma-delimited ApplicationContextInitializer class names set through init param. */
	// 通过 init 参数设置的 ApplicationContextInitializer 类名，以逗号分隔。
	@Nullable
	private String contextInitializerClasses;

	/** Should we publish the context as a ServletContext attribute?. */
	// 我们是否应该将上下文发布为 ServletContext 属性？
	private boolean publishContext = true;

	/** Should we publish a ServletRequestHandledEvent at the end of each request?. */
	// 我们是否应该在每个请求结束时发布 ServletRequestHandledEvent？
	private boolean publishEvents = true;

	/** Expose LocaleContext and RequestAttributes as inheritable for child threads?. */
	// 是否将 LocaleContext 和 RequestAttributes 暴露为可继承的子线程？
	private boolean threadContextInheritable = false;

	/** Should we dispatch an HTTP OPTIONS request to {@link #doService}?. */
	// 是否应该将 HTTP OPTIONS 请求发送到 {@link #doService}？
	private boolean dispatchOptionsRequest = false;

	/** Should we dispatch an HTTP TRACE request to {@link #doService}?. */
	// 是否应该将 HTTP TRACE 请求发送到 {@link #doService}？
	private boolean dispatchTraceRequest = false;

	/** Whether to log potentially sensitive info (request params at DEBUG + headers at TRACE). */
	// 是否记录潜在的敏感信息（DEBUG 模式下的请求参数 + TRACE 模式下的请求头）。
	private boolean enableLoggingRequestDetails = false;

	/** WebApplicationContext for this servlet. */
	// 此 servlet 的 WebApplicationContext。
	@Nullable
	private WebApplicationContext webApplicationContext;

	/** If the WebApplicationContext was injected via {@link #setApplicationContext}. */
	// WebApplicationContext 是否通过 {@link #setApplicationContext} 注入。
	private boolean webApplicationContextInjected = false;

	/** Flag used to detect whether onRefresh has already been called. */
	// 用于检测 onRefresh 是否已被调用的标志。
	private volatile boolean refreshEventReceived;

	/** Monitor for synchronized onRefresh execution. */
	// 同步 onRefresh 执行的监视器。
	private final Object onRefreshMonitor = new Object();


	/**
	 * Create a new {@code FrameworkServlet} that will create its own internal web
	 * application context based on defaults and values provided through servlet
	 * init-params. Typically used in Servlet 2.5 or earlier environments, where the only
	 * option for servlet registration is through {@code web.xml} which requires the use
	 * of a no-arg constructor.
	 * <p>Calling {@link #setContextConfigLocation} (init-param 'contextConfigLocation')
	 * will dictate which XML files will be loaded by the
	 * {@linkplain #DEFAULT_CONTEXT_CLASS default XmlWebApplicationContext}
	 * <p>Calling {@link #setContextClass} (init-param 'contextClass') overrides the
	 * default {@code XmlWebApplicationContext} and allows for specifying an alternative class,
	 * such as {@code AnnotationConfigWebApplicationContext}.
	 * <p>Calling {@link #setContextInitializerClasses} (init-param 'contextInitializerClasses')
	 * indicates which {@link ApplicationContextInitializer} classes should be used to
	 * further configure the internal application context prior to refresh().
	 * @see #FrameworkServlet(WebApplicationContext)
	 */
	// 创建一个新的 {@code FrameworkServlet}，它将根据 servlet init-params 提供的默认值和值创建自己的内部 Web 应用程序上下文。
	// 通常用于 Servlet 2.5 或更早版本的环境，在这些环境中，servlet 注册的唯一选项是通过 {@code web.xml}，而这需要使用无参数构造函数。
	// <p>调用 {@link #setContextConfigLocation} (init-param 'contextConfigLocation') 将决定由
	// {@linkplain #DEFAULT_CONTEXT_CLASS 默认 XmlWebApplicationContext} 加载哪些 XML 文件。
	// <p>调用 {@link #setContextClass} (init-param 'contextClass') 将覆盖默认的 {@code XmlWebApplicationContext}，
	// 并允许指定替代类，例如 {@code AnnotationConfigWebApplicationContext}。
	// <p>调用 {@link #setContextInitializerClasses} (init-param 'contextInitializerClasses') 指示在 refresh() 之前
	// 应使用哪些 {@link ApplicationContextInitializer} 类来进一步配置内部应用程序上下文。
	public FrameworkServlet() {
	}

	/**
	 * Create a new {@code FrameworkServlet} with the given web application context. This
	 * constructor is useful in Servlet environments where instance-based registration
	 * of servlets is possible through the {@link ServletContext#addServlet} API.
	 * <p>Using this constructor indicates that the following properties / init-params
	 * will be ignored:
	 * <ul>
	 * <li>{@link #setContextClass(Class)} / 'contextClass'</li>
	 * <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
	 * <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
	 * <li>{@link #setNamespace(String)} / 'namespace'</li>
	 * </ul>
	 * <p>The given web application context may or may not yet be {@linkplain
	 * ConfigurableApplicationContext#refresh() refreshed}. If it (a) is an implementation
	 * of {@link ConfigurableWebApplicationContext} and (b) has <strong>not</strong>
	 * already been refreshed (the recommended approach), then the following will occur:
	 * <ul>
	 * <li>If the given context does not already have a {@linkplain
	 * ConfigurableApplicationContext#setParent parent}, the root application context
	 * will be set as the parent.</li>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #postProcessWebApplicationContext} will be called</li>
	 * <li>Any {@link ApplicationContextInitializer ApplicationContextInitializers} specified through the
	 * "contextInitializerClasses" init-param or through the {@link
	 * #setContextInitializers} property will be applied.</li>
	 * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called</li>
	 * </ul>
	 * If the context has already been refreshed or does not implement
	 * {@code ConfigurableWebApplicationContext}, none of the above will occur under the
	 * assumption that the user has performed these actions (or not) per his or her
	 * specific needs.
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * @param webApplicationContext the context to use
	 * @see #initWebApplicationContext
	 * @see #configureAndRefreshWebApplicationContext
	 * @see org.springframework.web.WebApplicationInitializer
	 */
	// 使用给定的 Web 应用上下文创建一个新的 {@code FrameworkServlet}。此构造函数在 Servlet 环境中非常有用，
	// 因为可以通过 {@link ServletContext#addServlet} API 进行基于实例的 Servlet 注册。
	// <p>使用此构造函数表示将忽略以下属性/初始化参数：
	// <ul>
	// <li>{@link #setContextClass(Class)} / 'contextClass'</li>
	// <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
	// <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
	// <li>{@link #setNamespace(String)} / 'namespace'</li>
	// </ul>
	// <p>给定的 Web 应用上下文可能已刷新，也可能尚未刷新 {@linkplain ConfigurableApplicationContext#refresh()}。
	// 如果它 (a) 是 {@link ConfigurableWebApplicationContext} 的实现，并且 (b) <strong>尚未</strong>刷新（推荐方法），
	// 则将发生以下情况：
	// <ul>
	// <li>如果给定的上下文还没有 {@linkplain ConfigurableApplicationContext#setParent parent}，则根应用程序上下文将被设置为父级。</li>
	// <li>如果给定的上下文尚未分配 {@linkplain ConfigurableApplicationContext#setId id}，则将为其分配一个</li>
	// <li>{@code ServletContext} 和 {@code ServletConfig} 对象将被委托给应用程序上下文</li>
	// <li>{@link #postProcessWebApplicationContext} 将被调用</li>
	// <li>任何通过“contextInitializerClasses”init-param 或通过 {@link #setContextInitializers} 属性
	// 指定的 {@link ApplicationContextInitializer ApplicationContextInitializers} 都将应用。</li>
	// <li>将调用 {@link ConfigurableApplicationContext#refresh refresh()}</li>
	// </ul>
	// 如果上下文已经刷新或者没有实现 {@code ConfigurableWebApplicationContext}，则在假设用户已经根据其特定需求执行了这些操作（或没有执行）的情况下，上述任何操作都不会发生。
	// <p>有关使用示例，请参阅 {@link org.springframework.web.WebApplicationInitializer}。
	public FrameworkServlet(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}


	/**
	 * Set the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} that this servlet is supposed to use.
	 */
	public void setContextAttribute(@Nullable String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	/**
	 * Return the name of the ServletContext attribute which should be used to retrieve the
	 * {@link WebApplicationContext} that this servlet is supposed to use.
	 */
	@Nullable
	public String getContextAttribute() {
		return this.contextAttribute;
	}

	/**
	 * Set a custom context class. This class must be of type
	 * {@link org.springframework.web.context.WebApplicationContext}.
	 * <p>When using the default FrameworkServlet implementation,
	 * the context class must also implement the
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}
	 * interface.
	 * @see #createWebApplicationContext
	 */
	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 */
	// 返回自定义上下文类。
	public Class<?> getContextClass() {
		return this.contextClass;
	}

	/**
	 * Specify a custom WebApplicationContext id,
	 * to be used as serialization id for the underlying BeanFactory.
	 */
	public void setContextId(@Nullable String contextId) {
		this.contextId = contextId;
	}

	/**
	 * Return the custom WebApplicationContext id, if any.
	 */
	@Nullable
	public String getContextId() {
		return this.contextId;
	}

	/**
	 * Set a custom namespace for this servlet,
	 * to be used for building a default context config location.
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * Return the namespace for this servlet, falling back to default scheme if
	 * no custom namespace was set: e.g. "test-servlet" for a servlet named "test".
	 */
	public String getNamespace() {
		return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
	}

	/**
	 * Set the context config location explicitly, instead of relying on the default
	 * location built from the namespace. This location string can consist of
	 * multiple locations separated by any number of commas and spaces.
	 */
	public void setContextConfigLocation(@Nullable String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	/**
	 * Return the explicit context config location, if any.
	 */
	@Nullable
	public String getContextConfigLocation() {
		return this.contextConfigLocation;
	}

	/**
	 * Specify which {@link ApplicationContextInitializer} instances should be used
	 * to initialize the application context used by this {@code FrameworkServlet}.
	 * @see #configureAndRefreshWebApplicationContext
	 * @see #applyInitializers
	 */
	@SuppressWarnings("unchecked")
	public void setContextInitializers(@Nullable ApplicationContextInitializer<?>... initializers) {
		if (initializers != null) {
			for (ApplicationContextInitializer<?> initializer : initializers) {
				this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
			}
		}
	}

	/**
	 * Specify the set of fully-qualified {@link ApplicationContextInitializer} class
	 * names, per the optional "contextInitializerClasses" servlet init-param.
	 * @see #configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext)
	 * @see #applyInitializers(ConfigurableApplicationContext)
	 */
	public void setContextInitializerClasses(String contextInitializerClasses) {
		this.contextInitializerClasses = contextInitializerClasses;
	}

	/**
	 * Set whether to publish this servlet's context as a ServletContext attribute,
	 * available to all objects in the web container. Default is "true".
	 * <p>This is especially handy during testing, although it is debatable whether
	 * it's good practice to let other application objects access the context this way.
	 */
	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}

	/**
	 * Set whether this servlet should publish a ServletRequestHandledEvent at the end
	 * of each request. Default is "true"; can be turned off for a slight performance
	 * improvement, provided that no ApplicationListeners rely on such events.
	 * @see org.springframework.web.context.support.ServletRequestHandledEvent
	 */
	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}

	/**
	 * Set whether to expose the LocaleContext and RequestAttributes as inheritable
	 * for child threads (using an {@link java.lang.InheritableThreadLocal}).
	 * <p>Default is "false", to avoid side effects on spawned background threads.
	 * Switch this to "true" to enable inheritance for custom child threads which
	 * are spawned during request processing and only used for this request
	 * (that is, ending after their initial task, without reuse of the thread).
	 * <p><b>WARNING:</b> Do not use inheritance for child threads if you are
	 * accessing a thread pool which is configured to potentially add new threads
	 * on demand (e.g. a JDK {@link java.util.concurrent.ThreadPoolExecutor}),
	 * since this will expose the inherited context to such a pooled thread.
	 */
	public void setThreadContextInheritable(boolean threadContextInheritable) {
		this.threadContextInheritable = threadContextInheritable;
	}

	/**
	 * Set whether this servlet should dispatch an HTTP OPTIONS request to
	 * the {@link #doService} method.
	 * <p>Default in the {@code FrameworkServlet} is "false", applying
	 * {@link jakarta.servlet.http.HttpServlet}'s default behavior (i.e.enumerating
	 * all standard HTTP request methods as a response to the OPTIONS request).
	 * Note however that as of 4.3 the {@code DispatcherServlet} sets this
	 * property to "true" by default due to its built-in support for OPTIONS.
	 * <p>Turn this flag on if you prefer OPTIONS requests to go through the
	 * regular dispatching chain, just like other HTTP requests. This usually
	 * means that your controllers will receive those requests; make sure
	 * that those endpoints are actually able to handle an OPTIONS request.
	 * <p>Note that HttpServlet's default OPTIONS processing will be applied
	 * in any case if your controllers happen to not set the 'Allow' header
	 * (as required for an OPTIONS response).
	 */
	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	/**
	 * Set whether this servlet should dispatch an HTTP TRACE request to
	 * the {@link #doService} method.
	 * <p>Default is "false", applying {@link jakarta.servlet.http.HttpServlet}'s
	 * default behavior (i.e. reflecting the message received back to the client).
	 * <p>Turn this flag on if you prefer TRACE requests to go through the
	 * regular dispatching chain, just like other HTTP requests. This usually
	 * means that your controllers will receive those requests; make sure
	 * that those endpoints are actually able to handle a TRACE request.
	 * <p>Note that HttpServlet's default TRACE processing will be applied
	 * in any case if your controllers happen to not generate a response
	 * of content type 'message/http' (as required for a TRACE response).
	 */
	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}

	/**
	 * Whether to log request params at DEBUG level, and headers at TRACE level.
	 * Both may contain sensitive information.
	 * <p>By default set to {@code false} so that request details are not shown.
	 * @param enable whether to enable or not
	 * @since 5.1
	 */
	public void setEnableLoggingRequestDetails(boolean enable) {
		this.enableLoggingRequestDetails = enable;
	}

	/**
	 * Whether logging of potentially sensitive, request details at DEBUG and
	 * TRACE level is allowed.
	 * @since 5.1
	 */
	public boolean isEnableLoggingRequestDetails() {
		return this.enableLoggingRequestDetails;
	}

	/**
	 * Called by Spring via {@link ApplicationContextAware} to inject the current
	 * application context. This method allows FrameworkServlets to be registered as
	 * Spring beans inside an existing {@link WebApplicationContext} rather than
	 * {@link #findWebApplicationContext() finding} a
	 * {@link org.springframework.web.context.ContextLoaderListener bootstrapped} context.
	 * <p>Primarily added to support use in embedded servlet containers.
	 * @since 4.0
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		if (this.webApplicationContext == null && applicationContext instanceof WebApplicationContext wac) {
			this.webApplicationContext = wac;
			this.webApplicationContextInjected = true;
		}
	}


	/**
	 * Overridden method of {@link HttpServletBean}, invoked after any bean properties
	 * have been set. Creates this servlet's WebApplicationContext.
	 */
	// {@link HttpServletBean} 的重写方法，在设置所有 bean 属性后调用。创建此 servlet 的 WebApplicationContext。
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring " + getClass().getSimpleName() + " '" + getServletName() + "'");
		if (logger.isInfoEnabled()) {
			logger.info("Initializing Servlet '" + getServletName() + "'");
		}
		long startTime = System.currentTimeMillis();

		try {
			// 初始化并发布此 servlet 的 WebApplicationContext。
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();
		}
		catch (ServletException | RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (logger.isDebugEnabled()) {
			// 显示可能导致不安全地记录潜在敏感数据，屏蔽以防止不安全地记录潜在敏感数据
			String value = this.enableLoggingRequestDetails ?
					"shown which may lead to unsafe logging of potentially sensitive data" :
					"masked to prevent unsafe logging of potentially sensitive data";
			logger.debug("enableLoggingRequestDetails='" + this.enableLoggingRequestDetails +
					"': request parameters and headers will be " + value);
		}

		if (logger.isInfoEnabled()) {
			logger.info("Completed initialization in " + (System.currentTimeMillis() - startTime) + " ms");
		}
	}

	/**
	 * Initialize and publish the WebApplicationContext for this servlet.
	 * <p>Delegates to {@link #createWebApplicationContext} for actual creation
	 * of the context. Can be overridden in subclasses.
	 * @return the WebApplicationContext instance
	 * @see #FrameworkServlet(WebApplicationContext)
	 * @see #setContextClass
	 * @see #setContextConfigLocation
	 */
	// 初始化并发布此 servlet 的 WebApplicationContext。
	// <p>委托 {@link #createWebApplicationContext} 实际创建上下文。可在子类中重写。@return WebApplicationContext 实例
	protected WebApplicationContext initWebApplicationContext() {
		// // 查找此 Web 应用的根 {@code WebApplicationContext}，通常通过 {@link org.springframework.web.context.ContextLoaderListener} 加载。
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it --> 译文：在构造时注入了上下文实例 -> 使用它
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext cwac && !cwac.isActive()) {
				// The context has not yet been refreshed -> provide services such as
				// setting the parent context, setting the application context id, etc
				// --> 译文：上下文还未刷新 -> 提供设置父上下文、设置应用上下文id等服务
				if (cwac.getParent() == null) {
					// The context instance was injected without an explicit parent -> set
					// the root application context (if any; may be null) as the parent
					// --> 译文：上下文实例在没有明确父级的情况下被注入 -> 将根应用程序上下文（如果有；可能为空）设置为父级
					cwac.setParent(rootContext);
				}
				configureAndRefreshWebApplicationContext(cwac);
			}
		}
		if (wac == null) {
			// No context instance was injected at construction time -> see if one
			// has been registered in the servlet context. If one exists, it is assumed
			// that the parent context (if any) has already been set and that the
			// user has performed any initialization such as setting the context id
			// --> 译文：构造时未注入上下文实例 -> 检查 Servlet 上下文中是否已注册。如果存在，则假定父上下文（如果有）已设置，并且用户已执行任何初始化操作，例如设置上下文 ID
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// No context instance is defined for this servlet -> create a local one --> 译文：此 servlet 没有定义上下文实例 -> 创建一个本地实例
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// Either the context is not a ConfigurableApplicationContext with refresh
			// support or the context injected at construction time had already been
			// refreshed -> trigger initial onRefresh manually here.
			// --> 译文：要么上下文不是具有刷新支持的 ConfigurableApplicationContext，要么在构造时注入的上下文已经被刷新 -> 在这里手动触发初始 onRefresh。
			synchronized (this.onRefreshMonitor) {
				onRefresh(wac);
			}
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute. --> 译文：将上下文发布为 Servlet 上下文属性。
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
		}

		return wac;
	}

	/**
	 * Retrieve a {@code WebApplicationContext} from the {@code ServletContext}
	 * attribute with the {@link #setContextAttribute configured name}. The
	 * {@code WebApplicationContext} must have already been loaded and stored in the
	 * {@code ServletContext} before this servlet gets initialized (or invoked).
	 * <p>Subclasses may override this method to provide a different
	 * {@code WebApplicationContext} retrieval strategy.
	 * @return the WebApplicationContext for this servlet, or {@code null} if not found
	 * @see #getContextAttribute()
	 */
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

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
	 * or a {@link #setContextClass custom context class}, if set.
	 * <p>This implementation expects custom contexts to implement the
	 * {@link org.springframework.web.context.ConfigurableWebApplicationContext}
	 * interface. Can be overridden in subclasses.
	 * <p>Do not forget to register this servlet instance as application listener on the
	 * created context (for triggering its {@link #onRefresh callback}), and to call
	 * {@link org.springframework.context.ConfigurableApplicationContext#refresh()}
	 * before returning the context instance.
	 * @param parent the parent ApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 */
	// 实例化此 servlet 的 WebApplicationContext，可以是默认的 {@link org.springframework.web.context.support.XmlWebApplicationContext} 或
	// {@link #setContextClass 自定义上下文类}（如果已设置）。
	// <p>此实现要求自定义上下文实现 {@link org.springframework.web.context.ConfigurableWebApplicationContext} 接口。可在子类中重写。
	// <p>不要忘记将此 servlet 实例注册为创建的上下文上的应用程序监听器（用于触发其 {@link #onRefresh 回调}），
	// 并在返回上下文实例之前调用 {@link org.springframework.context.ConfigurableApplicationContext#refresh()}。
	// @param parent 要使用的父 ApplicationContext，如果没有则返回 {@code null}
	// @return 此 servlet 的 WebApplicationContext
	protected WebApplicationContext createWebApplicationContext(@Nullable ApplicationContext parent) {
		Class<?> contextClass = getContextClass();	// XmlWebApplicationContext.class
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
		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}

	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			// --> 译文:应用程序上下文 id 仍然设置为其原始默认值 -> 根据可用信息分配更有用的 id
			if (this.contextId != null) {
				wac.setId(this.contextId);
			}
			else {
				// Generate default id... --> 译文:生成默认 ID...
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
						ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
			}
		}

		wac.setServletContext(getServletContext()); // 设置此 Web 应用上下文的 ServletContext。
		wac.setServletConfig(getServletConfig());	// 设置此 Web 应用上下文的 ServletConfig。
		wac.setNamespace(getNamespace());			// 设置此 Web 应用上下文的命名空间，用于构建默认上下文配置位置。
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener())); // 为给定的事件源创建一个 SourceFilteringListener。

		// The wac environment's #initPropertySources will be called in any case when the context
		// is refreshed; do it eagerly here to ensure servlet property sources are in place for
		// use in any post-processing or initialization that occurs below prior to #refresh
		//  --> 译文:在任何情况下，当上下文刷新时，都会调用 wac 环境的 #initPropertySources；在这里急切地执行此操作，以确保 servlet 属性源已到位，可用于在 #refresh 之前发生的任何后处理或初始化
		ConfigurableEnvironment env = wac.getEnvironment(); // AbstractRefreshableWebApplicationContext.createEnvironment() -> StandardServletEnvironment
		if (env instanceof ConfigurableWebEnvironment cwe) {
			// 使用给定的参数，将任何用作占位符的存根属性源实例替换为真实的 servlet 上下文/配置属性源。
			cwe.initPropertySources(getServletContext(), getServletConfig());
		}

		postProcessWebApplicationContext(wac); // 在刷新并激活给定的 WebApplicationContext 作为此 servlet 的上下文之前，对其进行后处理。
		applyInitializers(wac);
		wac.refresh();
	}

	/**
	 * Instantiate the WebApplicationContext for this servlet, either a default
	 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
	 * or a {@link #setContextClass custom context class}, if set.
	 * Delegates to #createWebApplicationContext(ApplicationContext).
	 * @param parent the parent WebApplicationContext to use, or {@code null} if none
	 * @return the WebApplicationContext for this servlet
	 * @see org.springframework.web.context.support.XmlWebApplicationContext
	 * @see #createWebApplicationContext(ApplicationContext)
	 */
	// 实例化此 servlet 的 WebApplicationContext，可以是默认的 {@link org.springframework.web.context.support.XmlWebApplicationContext} 或
	// {@link #setContextClass 自定义上下文类}（如果已设置）。委托给 #createWebApplicationContext(ApplicationContext)。
	// @param parent 要使用的父 WebApplicationContext，如果没有则返回 {@code null}
	// @return 此 servlet 的 WebApplicationContext
	protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
		return createWebApplicationContext((ApplicationContext) parent);
	}

	/**
	 * Post-process the given WebApplicationContext before it is refreshed
	 * and activated as context for this servlet.
	 * <p>The default implementation is empty. {@code refresh()} will
	 * be called automatically after this method returns.
	 * <p>Note that this method is designed to allow subclasses to modify the application
	 * context, while {@link #initWebApplicationContext} is designed to allow
	 * end-users to modify the context through the use of
	 * {@link ApplicationContextInitializer ApplicationContextInitializers}.
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * @see #createWebApplicationContext
	 * @see #initWebApplicationContext
	 * @see ConfigurableWebApplicationContext#refresh()
	 */
	// 在刷新并激活给定的 WebApplicationContext 作为此 servlet 的上下文之前，对其进行后处理。
	// <p>默认实现为空。此方法返回后，将自动调用 {@code refresh()}。
	// <p>请注意，此方法旨在允许子类修改应用程序上下文，而 {@link #initWebApplicationContext} 旨在
	// 允许最终用户通过使用 {@link ApplicationContextInitializer ApplicationContextInitializers} 修改上下文。
	// @param wac 已配置的 WebApplicationContext（尚未刷新）
	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
	}

	/**
	 * Delegate the WebApplicationContext before it is refreshed to any
	 * {@link ApplicationContextInitializer} instances specified by the
	 * "contextInitializerClasses" servlet init-param.
	 * <p>See also {@link #postProcessWebApplicationContext}, which is designed to allow
	 * subclasses (as opposed to end-users) to modify the application context, and is
	 * called immediately before this method.
	 * @param wac the configured WebApplicationContext (not refreshed yet)
	 * @see #createWebApplicationContext
	 * @see #postProcessWebApplicationContext
	 * @see ConfigurableApplicationContext#refresh()
	 */
	// 在 WebApplicationContext 刷新之前，将其委托给由 “contextInitializerClasses” servlet 初始化参数指定的任何 {@link ApplicationContextInitializer} 实例。
	// <p>另请参阅 {@link #postProcessWebApplicationContext}，它旨在允许子类（而非最终用户）修改应用程序上下文，并在此方法之前立即调用。
	// @param wac 已配置的 WebApplicationContext（尚未刷新）
	protected void applyInitializers(ConfigurableApplicationContext wac) {
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
			initializer.initialize(wac);
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

	/**
	 * Return the ServletContext attribute name for this servlet's WebApplicationContext.
	 * <p>The default implementation returns
	 * {@code SERVLET_CONTEXT_PREFIX + servlet name}.
	 * @see #SERVLET_CONTEXT_PREFIX
	 * @see #getServletName
	 */
	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}

	/**
	 * Return this servlet's WebApplicationContext.
	 */
	@Nullable
	public final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}


	/**
	 * This method will be invoked after any bean properties have been set and
	 * the WebApplicationContext has been loaded. The default implementation is empty;
	 * subclasses may override this method to perform any initialization they require.
	 * @throws ServletException in case of an initialization exception
	 */
	// 在设置所有 bean 属性并加载 WebApplicationContext 后，将调用此方法。
	// 默认实现为空；子类可以重写此方法以执行所需的任何初始化操作。
	// @throws ServletException 如果发生初始化异常
	protected void initFrameworkServlet() throws ServletException {
	}

	/**
	 * Refresh this servlet's application context, as well as the
	 * dependent state of the servlet.
	 * @see #getWebApplicationContext()
	 * @see org.springframework.context.ConfigurableApplicationContext#refresh()
	 */
	public void refresh() {
		WebApplicationContext wac = getWebApplicationContext();
		if (!(wac instanceof ConfigurableApplicationContext cac)) {
			throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
		}
		cac.refresh();
	}

	/**
	 * Callback that receives refresh events from this servlet's WebApplicationContext.
	 * <p>The default implementation calls {@link #onRefresh},
	 * triggering a refresh of this servlet's context-dependent state.
	 * @param event the incoming ApplicationContext event
	 */
	// 从此 servlet 的 WebApplicationContext 接收刷新事件的回调。
	// <p>默认实现调用 {@link #onRefresh}，触发此 servlet 上下文相关状态的刷新。
	// @param event 传入的 ApplicationContext 事件
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		synchronized (this.onRefreshMonitor) {
			onRefresh(event.getApplicationContext());
		}
	}

	/**
	 * Template method which can be overridden to add servlet-specific refresh work.
	 * Called after successful context refresh.
	 * <p>This implementation is empty.
	 * @param context the current WebApplicationContext
	 * @see #refresh()
	 */
	// 可重写模板方法，以添加特定于 servlet 的刷新功能。上下文刷新成功后调用。
	// <p>此实现为空。
	// @param context 当前的 WebApplicationContext
	protected void onRefresh(ApplicationContext context) {
		// For subclasses: do nothing by default.
	}

	/**
	 * Close the WebApplicationContext of this servlet.
	 * @see org.springframework.context.ConfigurableApplicationContext#close()
	 */
	@Override
	public void destroy() {
		getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
		// Only call close() on WebApplicationContext if locally managed...
		if (!this.webApplicationContextInjected &&
				this.webApplicationContext instanceof ConfigurableApplicationContext cac) {
			cac.close();
		}
	}


	/**
	 * Override the parent class implementation in order to intercept requests
	 * using PATCH or non-standard HTTP methods (WebDAV).
	 */
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (HTTP_SERVLET_METHODS.contains(request.getMethod())) {
			super.service(request, response);
		}
		else {
			processRequest(request, response);
		}
	}

	/**
	 * Delegate GET requests to processRequest/doService.
	 * <p>Will also be invoked by HttpServlet's default implementation of {@code doHead},
	 * with a {@code NoBodyResponse} that just captures the content length.
	 * @see #doService
	 * @see #doHead
	 */
	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate POST requests to {@link #processRequest}.
	 * @see #doService
	 */
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate PUT requests to {@link #processRequest}.
	 * @see #doService
	 */
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate DELETE requests to {@link #processRequest}.
	 * @see #doService
	 */
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate OPTIONS requests to {@link #processRequest}, if desired.
	 * <p>Applies HttpServlet's standard OPTIONS processing otherwise,
	 * and also if there is still no 'Allow' header set after dispatching.
	 * @see #doService
	 */
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchOptionsRequest || CorsUtils.isPreFlightRequest(request)) {
			processRequest(request, response);
			if (response.containsHeader(HttpHeaders.ALLOW)) {
				// Proper OPTIONS response coming from a handler - we're done.
				return;
			}
		}

		// Use response wrapper in order to always add PATCH to the allowed methods
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

	/**
	 * Delegate TRACE requests to {@link #processRequest}, if desired.
	 * <p>Applies HttpServlet's standard TRACE processing otherwise.
	 * @see #doService
	 */
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

	/**
	 * Process this request, publishing an event regardless of the outcome.
	 * <p>The actual event handling is performed by the abstract
	 * {@link #doService} template method.
	 */
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		initContextHolders(request, localeContext, requestAttributes);

		try {
			doService(request, response);
		}
		catch (ServletException | IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new ServletException("Request processing failed: " + ex, ex);
		}

		finally {
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}
			logResult(request, response, failureCause, asyncManager);
			publishRequestHandledEvent(request, response, startTime, failureCause);
		}
	}

	/**
	 * Build a LocaleContext for the given request, exposing the request's
	 * primary locale as current locale.
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext, or {@code null} if none to bind
	 * @see LocaleContextHolder#setLocaleContext
	 */
	@Nullable
	protected LocaleContext buildLocaleContext(HttpServletRequest request) {
		return new SimpleLocaleContext(request.getLocale());
	}

	/**
	 * Build ServletRequestAttributes for the given request (potentially also
	 * holding a reference to the response), taking pre-bound attributes
	 * (and their type) into consideration.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param previousAttributes pre-bound RequestAttributes instance, if any
	 * @return the ServletRequestAttributes to bind, or {@code null} to preserve
	 * the previously bound instance (or not binding any, if none bound before)
	 * @see RequestContextHolder#setRequestAttributes
	 */
	@Nullable
	protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
			@Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {

		if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
			return new ServletRequestAttributes(request, response);
		}
		else {
			return null;  // preserve the pre-bound RequestAttributes instance
		}
	}

	private void initContextHolders(HttpServletRequest request,
			@Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
	}

	private void resetContextHolders(HttpServletRequest request,
			@Nullable LocaleContext prevLocaleContext, @Nullable RequestAttributes previousAttributes) {

		LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
		RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
	}

	private void logResult(HttpServletRequest request, HttpServletResponse response,
			@Nullable Throwable failureCause, WebAsyncManager asyncManager) {

		if (!logger.isDebugEnabled()) {
			return;
		}

		DispatcherType dispatchType = request.getDispatcherType();
		boolean initialDispatch = (dispatchType == DispatcherType.REQUEST);

		if (failureCause != null) {
			if (!initialDispatch) {
				// FORWARD/ERROR/ASYNC: minimal message (there should be enough context already)
				if (logger.isDebugEnabled()) {
					logger.debug("Unresolved failure from \"" + dispatchType + "\" dispatch: " + failureCause);
				}
			}
			else if (logger.isTraceEnabled()) {
				logger.trace("Failed to complete request", failureCause);
			}
			else {
				logger.debug("Failed to complete request: " + failureCause);
			}
			return;
		}

		if (asyncManager.isConcurrentHandlingStarted()) {
			logger.debug("Exiting but response remains open for further handling");
			return;
		}

		int status = response.getStatus();
		String headers = "";  // nothing below trace

		if (logger.isTraceEnabled()) {
			Collection<String> names = response.getHeaderNames();
			if (this.enableLoggingRequestDetails) {
				headers = names.stream().map(name -> name + ":" + response.getHeaders(name))
						.collect(Collectors.joining(", "));
			}
			else {
				headers = names.isEmpty() ? "" : "masked";
			}
			headers = ", headers={" + headers + "}";
		}

		if (!initialDispatch) {
			logger.debug("Exiting from \"" + dispatchType + "\" dispatch, status " + status + headers);
		}
		else {
			logger.debug("Completed " + HttpStatusCode.valueOf(status) + headers);
		}
	}

	private void publishRequestHandledEvent(HttpServletRequest request, HttpServletResponse response,
			long startTime, @Nullable Throwable failureCause) {

		if (this.publishEvents && this.webApplicationContext != null) {
			// Whether or not we succeeded, publish an event.
			long processingTime = System.currentTimeMillis() - startTime;
			this.webApplicationContext.publishEvent(
					new ServletRequestHandledEvent(this,
							request.getRequestURI(), request.getRemoteAddr(),
							request.getMethod(), getServletConfig().getServletName(),
							WebUtils.getSessionId(request), getUsernameForRequest(request),
							processingTime, failureCause, response.getStatus()));
		}
	}

	/**
	 * Determine the username for the given request.
	 * <p>The default implementation takes the name of the UserPrincipal, if any.
	 * Can be overridden in subclasses.
	 * @param request current HTTP request
	 * @return the username, or {@code null} if none found
	 * @see jakarta.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	@Nullable
	protected String getUsernameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return (userPrincipal != null ? userPrincipal.getName() : null);
	}


	/**
	 * Subclasses must implement this method to do the work of request handling,
	 * receiving a centralized callback for GET, POST, PUT and DELETE.
	 * <p>The contract is essentially the same as that for the commonly overridden
	 * {@code doGet} or {@code doPost} methods of HttpServlet.
	 * <p>This class intercepts calls to ensure that exception handling and
	 * event publication takes place.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure
	 * @see jakarta.servlet.http.HttpServlet#doGet
	 * @see jakarta.servlet.http.HttpServlet#doPost
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
			throws Exception;


	/**
	 * ApplicationListener endpoint that receives events from this servlet's WebApplicationContext
	 * only, delegating to {@code onApplicationEvent} on the FrameworkServlet instance.
	 */
	// ApplicationListener 端点仅从此 servlet 的 WebApplicationContext 接收事件，委托给 FrameworkServlet 实例上的 {@code onApplicationEvent}。
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
	}


	/**
	 * CallableProcessingInterceptor implementation that initializes and resets
	 * FrameworkServlet's context holders, i.e. LocaleContextHolder and RequestContextHolder.
	 */
	private class RequestBindingInterceptor implements CallableProcessingInterceptor {

		@Override
		public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
				initContextHolders(request, buildLocaleContext(request),
						buildRequestAttributes(request, response, null));
			}
		}
		@Override
		public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, @Nullable Object concurrentResult) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				resetContextHolders(request, null, null);
			}
		}
	}

}
