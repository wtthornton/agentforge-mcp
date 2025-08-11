package com.agentforge;

import com.agentforge.service.LoggingService;
import com.agentforge.service.MonitoringService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

/**
 * Main application class for AgentForge backend service.
 * 
 * AgentForge is a static analyzer and project setup/migration utility
 * that follows Agent OS development standards.
 */
@SpringBootApplication
@EnableScheduling
public class AgentForgeApplication {

    private final LoggingService loggingService;
    private final MonitoringService monitoringService;

    public AgentForgeApplication(LoggingService loggingService, MonitoringService monitoringService) {
        this.loggingService = loggingService;
        this.monitoringService = monitoringService;
    }

    public static void main(String[] args) {
        SpringApplication.run(AgentForgeApplication.class, args);
    }

    /**
     * Initialize services after application context is ready
     */
    @PostConstruct
    public void initialize() {
        loggingService.logStartup();
        
        // Log initial system metrics
        var systemMetrics = monitoringService.getSystemMetrics();
        loggingService.logInfo("SYSTEM", "INIT", "System metrics initialized", systemMetrics);
    }

    /**
     * Configure CORS for frontend integration
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
