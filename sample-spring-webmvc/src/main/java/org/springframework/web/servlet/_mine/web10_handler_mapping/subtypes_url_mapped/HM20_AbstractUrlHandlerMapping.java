package org.springframework.web.servlet._mine.web10_handler_mapping.subtypes_url_mapped;

import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

/**
 * AbstractUrlHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping
 */
public class HM20_AbstractUrlHandlerMapping {
	/**
	 * @see AbstractUrlHandlerMapping#setRootHandler(Object)
	 * @see AbstractUrlHandlerMapping#getRootHandler()
	 * @see AbstractUrlHandlerMapping#setUseTrailingSlashMatch(boolean)
	 * @see AbstractUrlHandlerMapping#useTrailingSlashMatch()
	 * @see AbstractUrlHandlerMapping#setLazyInitHandlers(boolean)
	 * @see AbstractUrlHandlerMapping#getHandlerMap()
	 * @see AbstractUrlHandlerMapping#getPathPatternHandlerMap()
	 */
	public static void main(String[] args) {

	}
}
/*
********************************* Class API Docs *********************************
URL 映射 HandlerMapping 实现的抽象基类。

<p>支持文字匹配和模式匹配，例如“/test/”、“/test/”等。有关模式语法的详细信息，
请参阅 PathPattern（如果已解析模式已启用 AbstractHandlerMapping#usesPathPatterns()），否则请参阅 AntPathMatcher。
* 语法大致相同，但 PathPattern 语法更适合 Web 应用程序，并且其实现效率更高。

<p>所有路径模式都会被检查，以便找到与当前请求路径最精确的匹配，其中“最精确”是指与当前请求路径匹配的最长路径模式。

********************************* Class Definition *********************************
public abstract class AbstractUrlHandlerMapping extends AbstractHandlerMapping implements MatchableHandlerMapping {
	private Object rootHandler;
	private boolean useTrailingSlashMatch = false;
	private boolean lazyInitHandlers = false;
	private final Map<String, Object> handlerMap = new LinkedHashMap<>();
	private final Map<PathPattern, Object> pathPatternHandlerMap = new LinkedHashMap<>();
	// ....
}
**/