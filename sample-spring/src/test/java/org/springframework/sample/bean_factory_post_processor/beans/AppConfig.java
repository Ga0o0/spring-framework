package org.springframework.sample.bean_factory_post_processor.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	@Bean
	public Service service() {
		return new Service();
	}
}