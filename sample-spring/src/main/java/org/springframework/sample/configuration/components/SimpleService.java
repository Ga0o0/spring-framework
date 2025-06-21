package org.springframework.sample.configuration.components;

import org.springframework.stereotype.Service;

@Service
public class SimpleService {
	public void sayHello() {
		System.out.println("Hello SimpleService");
	}
}
