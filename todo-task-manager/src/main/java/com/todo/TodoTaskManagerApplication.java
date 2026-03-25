package com.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoTaskManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoTaskManagerApplication.class, args);
		System.out.println("todo is running.....");
	}

}
