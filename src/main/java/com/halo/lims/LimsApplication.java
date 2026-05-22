package com.halo.lims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class LimsApplication {

	private static final Logger log = LoggerFactory.getLogger(LimsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LimsApplication.class, args);
	}

	/**
	 * Logs a startup confirmation via the proper SLF4J logger once the application
	 * context is fully initialized and all beans are ready.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		log.info("=======================================");
		log.info("  LIMS APPLICATION FULLY STARTED!      ");
		log.info("  Environment: {}", System.getenv("ENVIRONMENT") != null ? System.getenv("ENVIRONMENT") : "development");
		log.info("=======================================");
	}
}
