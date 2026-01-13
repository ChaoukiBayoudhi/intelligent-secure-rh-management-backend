package tn.sesame.rh_management_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 * 
 * This is the entry point of the RH Management Backend application.
 * The @SpringBootApplication annotation enables:
 * - Component scanning
 * - Auto-configuration
 * - Configuration properties
 */
@SpringBootApplication
public class RhManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RhManagementBackendApplication.class, args);
	}

}
