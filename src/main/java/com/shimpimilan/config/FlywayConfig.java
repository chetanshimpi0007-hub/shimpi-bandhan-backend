package com.shimpimilan.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairStrategy() {
        return flyway -> {
            // Repair checksums automatically on startup to recover from modified migrations (MySQL to PostgreSQL conversion)
            flyway.repair();
            // Continue with standard migration execution (V2, V3, etc.)
            flyway.migrate();
        };
    }
}
