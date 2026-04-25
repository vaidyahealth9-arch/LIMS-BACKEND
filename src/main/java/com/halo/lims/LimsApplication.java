package com.halo.lims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LimsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimsApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			System.out.println("=======================================");
			System.out.println("  LIMS APPLICATION FULLY STARTED!      ");
			System.out.println("  Listening on Port: " + System.getenv("PORT"));
			System.out.println("=======================================");
		};
	}

}
