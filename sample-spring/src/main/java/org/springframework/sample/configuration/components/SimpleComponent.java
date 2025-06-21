package org.springframework.sample.configuration.components;

import org.springframework.stereotype.Component;

@Component
public class SimpleComponent {
	public void sayHello() {
		System.out.println("Hello SimpleComponent");
	}
}
