package com.cloudprojectone.imagerecognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({"com.cloudprojectone.imagerecognition.service"})
@EntityScan("com.cloudprojectone.imagerecognition.model")
@EnableJpaRepositories("com.cloudprojectone.imagerecognition.repository")
public class ImagerecognitionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImagerecognitionApplication.class, args);
	}
}
