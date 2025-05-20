package org.springframework.sample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
//@PropertySource("classpath:my.properties")
public class AppConfig {

	@Autowired
	private Environment env;

	@Bean
	public M m(){
		System.out.println("=================================");
		System.out.println(env.getProperty("name"));
		return new M();
	}

	public static class M {
		public void m(){
			System.out.println("I am M");
		}
	}

}