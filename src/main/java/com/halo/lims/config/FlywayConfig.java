package com.halo.lims.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    /**
     * Custom Flyway migration strategy that runs repair before migration.
     * This automatically resolves checksum mismatches in the flyway_schema_history
     * table (e.g. from line ending differences between Windows and Linux) 
     * without failing application startup.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
