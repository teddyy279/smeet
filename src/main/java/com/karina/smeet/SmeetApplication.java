package com.karina.smeet;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class SmeetApplication {

	public static void main(String[] args) {
		//loadDotenv();
		SpringApplication.run(SmeetApplication.class, args);
	}
}
