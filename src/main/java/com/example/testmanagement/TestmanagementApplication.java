package com.example.testmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestmanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestmanagementApplication.class, args);
	}

}
