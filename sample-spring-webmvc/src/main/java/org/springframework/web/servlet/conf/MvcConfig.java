package org.springframework.web.servlet.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@EnableWebMvc
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    /**
     * 视图解析器
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.clear();
		converters.add(new ByteArrayHttpMessageConverter()); // support byte[].class
		converters.add(new StringHttpMessageConverter());    // support String.class

		// 如果需要解析 String 返回值；这个 messageConverter 要放到 StringHttpMessageConverter 之后；原因：supports() 方法总是返回 true
		converters.add(new MappingJackson2HttpMessageConverter()); // AbstractGenericHttpMessageConverter.supports() -> return true
	}

}
