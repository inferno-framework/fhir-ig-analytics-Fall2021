package com.gatech.fhir.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ServletComponentScan
@ComponentScan(basePackages= {"com.gatech.fhir.analytics"})
public class AnalyticsApplication {
	public static final String BASE_DIR = System.getProperty("user.dir");

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsApplication.class, args);
	}
}