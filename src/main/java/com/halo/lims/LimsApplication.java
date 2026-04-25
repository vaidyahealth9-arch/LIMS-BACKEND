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
	public CommandLineRunner commandLineRunner(
			com.halo.lims.repository.UserRepository userRepository,
			com.halo.lims.repository.OrganizationRepository organizationRepository,
			org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
		return args -> {
			System.out.println("=======================================");
			System.out.println("  LIMS APPLICATION FULLY STARTED!      ");
			System.out.println("  Listening on Port: " + System.getenv("PORT"));
			System.out.println("=======================================");

			// Seed default organization if none exist
			if (organizationRepository.count() == 0) {
				System.out.println("Seeding default organization...");
				com.halo.lims.model.Organization org = com.halo.lims.model.Organization.builder()
						.organizationName("Hale Labs")
						.orgType("laboratory")
						.localIdentifierSystem("https://vaidyahealth.com")
						.localIdentifierValue("HALE-001")
						.country("IND")
						.build();
				organizationRepository.save(org);
			}

			// Seed default admin user if none exist
			if (userRepository.count() == 0) {
				System.out.println("Seeding default admin user...");
				com.halo.lims.model.Organization org = organizationRepository.findAll().get(0);
				com.halo.lims.model.User admin = com.halo.lims.model.User.builder()
						.username("admin")
						.password(passwordEncoder.encode("admin"))
						.roles(java.util.Set.of("ADMIN"))
						.organization(org)
						.isActive(true)
						.build();
				userRepository.save(admin);
				System.out.println("Default admin created: admin / admin");
			}
		};
	}

}
