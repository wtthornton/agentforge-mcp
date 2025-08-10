package com.agentforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for AgentForge Backend.
 * 
 * This application provides:
 * - RESTful API endpoints for the frontend
 * - AI/ML capabilities through OpenAI and LangChain integration
 * - Database operations with PostgreSQL and pgvector
 * - Time-series data with InfluxDB
 * - Caching with Redis
 * - Observability with Micrometer and OpenTelemetry
 * - Security with Spring Security
 * 
 * @author Agent-OS Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.agentforge.repository")
@EnableCaching
@EnableAsync
@EnableScheduling
public class AgentforgeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentforgeBackendApplication.class, args);
    }
}
