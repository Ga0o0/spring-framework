package org.springframework.web.servlet._mine.servlet.servlet10_atenablewebmvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @EnableWebMvc
 *
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 */
public class EWM01_AtEnableWebMvc {

	@EnableWebMvc
	@Configuration
	static class MvcConfig {
		@Bean
		public WebMvcConfigurer webMvcConfigurer() {
			return new CustomWebMvcConfigurer();
		}
	}

	static class CustomWebMvcConfigurer implements WebMvcConfigurer {
		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(new CustomHandlerInterceptor());
		}

		static class CustomHandlerInterceptor implements HandlerInterceptor {
			@Override
			public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
				System.out.println("invoke CustomHandlerInterceptor#preHandle()");
				return HandlerInterceptor.super.preHandle(request, response, handler);
			}
		}
	}
}
