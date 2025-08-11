package com.agentforge.controller;

import com.agentforge.config.DatabaseHealthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Test Controller for AgentForge
 * 
 * This controller provides endpoints to test database connectivity
 * and diagnose connection issues during development.
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseHealthConfig.DatabaseConnectionValidator connectionValidator;

    /**
     * Test basic database connectivity
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test basic connection
            boolean isConnected = connectionValidator.testConnection(dataSource);
            response.put("connected", isConnected);
            response.put("status", isConnected ? "SUCCESS" : "FAILED");
            
            if (isConnected) {
                // Get connection details
                try (Connection connection = dataSource.getConnection()) {
                    response.put("database_product_name", connection.getMetaData().getDatabaseProductName());
                    response.put("database_product_version", connection.getMetaData().getDatabaseProductVersion());
                    response.put("driver_name", connection.getMetaData().getDriverName());
                    response.put("driver_version", connection.getMetaData().getDriverVersion());
                    response.put("url", connection.getMetaData().getURL());
                    response.put("username", connection.getMetaData().getUserName());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("connected", false);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test database connection with retry logic
     */
    @GetMapping("/test-with-retry")
    public ResponseEntity<Map<String, Object>> testConnectionWithRetry() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Wait for database with retry logic
            connectionValidator.waitForDatabase(dataSource, 30000); // Wait up to 30 seconds
            
            response.put("connected", true);
            response.put("status", "SUCCESS");
            response.put("message", "Database connection established after retry");
            
            // Get connection details
            try (Connection connection = dataSource.getConnection()) {
                response.put("database_product_name", connection.getMetaData().getDatabaseProductName());
                response.put("database_product_version", connection.getMetaData().getDatabaseProductVersion());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("connected", false);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get database connection pool status
     */
    @GetMapping("/pool-status")
    public ResponseEntity<Map<String, Object>> getPoolStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                com.zaxxer.hikari.HikariPoolMXBean poolMXBean = hikariDS.getHikariPoolMXBean();
                
                if (poolMXBean != null) {
                    response.put("active_connections", poolMXBean.getActiveConnections());
                    response.put("idle_connections", poolMXBean.getIdleConnections());
                    response.put("total_connections", poolMXBean.getTotalConnections());
                    response.put("threads_awaiting_connection", poolMXBean.getThreadsAwaitingConnection());
                    response.put("max_pool_size", hikariDS.getMaximumPoolSize());
                    response.put("min_idle", hikariDS.getMinimumIdle());
                    response.put("status", "SUCCESS");
                } else {
                    response.put("status", "NO_POOL_INFO");
                    response.put("message", "Pool MXBean not available");
                }
            } else {
                response.put("status", "NOT_HIKARI");
                response.put("message", "DataSource is not HikariCP");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("error_type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}
