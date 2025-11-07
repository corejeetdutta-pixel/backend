package com.recruitment.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)     // Important: bootstraps existing DB only if no history exists
                .baselineVersion("1")        // Must match the already existing baseline
                .load();
    }

    @Bean
    public CommandLineRunner runFlyway(Flyway flyway) {
        return args -> flyway.migrate();
    }
}