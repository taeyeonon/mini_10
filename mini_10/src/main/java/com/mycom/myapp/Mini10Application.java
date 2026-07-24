package com.mycom.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Mini10Application {

	public static void main(String[] args) {
		SpringApplication.run(Mini10Application.class, args);
	}

}
