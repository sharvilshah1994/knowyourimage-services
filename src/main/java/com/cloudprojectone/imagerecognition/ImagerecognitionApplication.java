package com.cloudprojectone.imagerecognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.cloudprojectone.imagerecognition")
@EnableScheduling
public class ImagerecognitionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagerecognitionApplication.class, args);
	}
}
