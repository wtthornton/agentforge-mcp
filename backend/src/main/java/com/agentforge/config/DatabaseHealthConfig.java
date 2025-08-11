package com.agentforge.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Health Configuration for AgentForge
 * 
 * This class provides:
 * - Database connection retry logic for Docker environments
 * - Health check configuration
 * - Connection validation utilities
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Configuration
@EnableRetry
public class DatabaseHealthConfig {

    /**
     * Configure retry template for database operations
     */
    @Bean
    public RetryTemplate databaseRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(16000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Retry policy: max 5 attempts
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }

    /**
     * Database connection validator
     */
    @Bean
    public DatabaseConnectionValidator databaseConnectionValidator() {
        return new DatabaseConnectionValidator();
    }

    /**
     * Database connection validator utility class
     */
    public static class DatabaseConnectionValidator {
        
        /**
         * Test database connection with retry logic
         */
        public boolean testConnection(DataSource dataSource) {
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                if (isValid) {
                    System.out.println("Database connection test successful");
                } else {
                    System.out.println("Database connection test failed - connection not valid");
                }
                return isValid;
            } catch (SQLException e) {
                System.err.println("Database connection test failed with SQLException: " + e.getMessage());
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Error Code: " + e.getErrorCode());
                return false;
            } catch (Exception e) {
                System.err.println("Database connection test failed with unexpected error: " + e.getMessage());
                return false;
            }
        }
        
        /**
         * Wait for database to be ready
         */
        public void waitForDatabase(DataSource dataSource, long maxWaitTimeMs) {
            long startTime = System.currentTimeMillis();
            long waitTime = 1000; // Start with 1 second
            int attemptCount = 0;
            
            System.out.println("Starting database connection validation...");
            
            while (System.currentTimeMillis() - startTime < maxWaitTimeMs) {
                attemptCount++;
                System.out.println("Database connection attempt " + attemptCount + "...");
                
                if (testConnection(dataSource)) {
                    System.out.println("Database is ready after " + attemptCount + " attempts");
                    return; // Database is ready
                }
                
                long elapsed = System.currentTimeMillis() - startTime;
                long remaining = maxWaitTimeMs - elapsed;
                
                if (remaining <= 0) {
                    break;
                }
                
                System.out.println("Database not ready, waiting " + waitTime + "ms before retry... (remaining: " + remaining + "ms)");
                
                try {
                    Thread.sleep(waitTime);
                    waitTime = Math.min(waitTime * 2, 10000); // Exponential backoff, max 10s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Database connection wait interrupted");
                    break;
                }
            }
            
            throw new RuntimeException("Database connection timeout after " + maxWaitTimeMs + "ms and " + attemptCount + " attempts");
        }
    }
}
