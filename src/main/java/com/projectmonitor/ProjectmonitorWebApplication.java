package com.projectmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjectmonitorWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectmonitorWebApplication.class, args);
	}
}
