package org.springframework.web.servlet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
public class SampleRestController {

	// curl --location 'http://localhost:8080/rest'
    @GetMapping("rest")
    public String rest() {
        return "Hello Rest!!";
    }

	@GetMapping("req_body")
	public String req_body(@RequestBody String body) {
		/*
		   curl --location --request GET 'http://localhost:8080/req_body' \
				--header 'Content-Type: application/json' \
				--data '{
					"id": 1,
					"name": "xiaoming"
				}'
		*/
		return body;
	}

	// curl --location 'http://localhost:8080/json'
	@GetMapping(value = "json")
	public User json() {
		return new User("111", 12);
	}

	public static class User {
		private final String name;
		private final int age;

		public User(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}
	}

}
