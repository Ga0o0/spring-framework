package org.springframework.web.servlet._mine.web10_handler_mapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;

/**
 * MatchableHandlerMapping
 *
 * @see org.springframework.web.servlet.handler.MatchableHandlerMapping
 */
public class HM10_MatchableHandlerMapping {
	/**
	 * @see MatchableHandlerMapping#getPatternParser()
	 * @see MatchableHandlerMapping#match(HttpServletRequest, String)
	 */
	public static void main(String[] args) {

	}
}
/*
********************************* Class API Docs *********************************
HandlerMapping 可以实现的附加接口，用于公开与其内部请求匹配配置和实现一致的请求匹配 API。

********************************* Class Definition *********************************
public interface MatchableHandlerMapping extends HandlerMapping {
	default PathPatternParser getPatternParser() {
		return null;
	}
	RequestMatchResult match(HttpServletRequest request, String pattern);
}
**/