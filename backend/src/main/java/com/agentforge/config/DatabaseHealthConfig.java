package com.agentforge.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Health Check Configuration for AgentForge
 * 
 * This class provides:
 * - Database connectivity health checks
 * - Connection pool status monitoring
 * - pgvector extension verification
 * - Performance metrics collection
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Component
public class DatabaseHealthConfig implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            
            // Basic connectivity check
            if (!connection.isValid(5)) {
                return Health.down()
                    .withDetail("error", "Database connection validation failed")
                    .build();
            }

            Map<String, Object> details = new HashMap<>();
            
            // Check PostgreSQL version
            String postgresVersion = getPostgresVersion(connection);
            details.put("postgres_version", postgresVersion);
            
            // Check pgvector extension
            boolean pgvectorAvailable = checkPgvectorExtension(connection);
            details.put("pgvector_available", pgvectorAvailable);
            
            // Check connection pool status
            Map<String, Object> poolStatus = getConnectionPoolStatus();
            details.put("connection_pool", poolStatus);
            
            // Check database size
            long databaseSize = getDatabaseSize(connection);
            details.put("database_size_mb", databaseSize);
            
            // Check active connections
            int activeConnections = getActiveConnections(connection);
            details.put("active_connections", activeConnections);
            
            // Determine health status
            Health.Builder healthBuilder = Health.up()
                .withDetail("database", "PostgreSQL")
                .withDetail("status", "Connected and operational");
            
            // Add all details
            details.forEach(healthBuilder::withDetail);
            
            return healthBuilder.build();
            
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", "Database connection failed: " + e.getMessage())
                .withDetail("exception", e.getClass().getSimpleName())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Unexpected error during health check: " + e.getMessage())
                .withDetail("exception", e.getClass().getSimpleName())
                .build();
        }
    }

    /**
     * Get PostgreSQL version information
     */
    private String getPostgresVersion(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT version()")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "Unknown";
    }

    /**
     * Check if pgvector extension is available
     */
    private boolean checkPgvectorExtension(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT 1 FROM pg_extension WHERE extname = 'vector'")) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Get connection pool status (if using HikariCP)
     */
    private Map<String, Object> getConnectionPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                com.zaxxer.hikari.HikariPoolMXBean poolMXBean = hikariDS.getHikariPoolMXBean();
                
                if (poolMXBean != null) {
                    status.put("active_connections", poolMXBean.getActiveConnections());
                    status.put("idle_connections", poolMXBean.getIdleConnections());
                    status.put("total_connections", poolMXBean.getTotalConnections());
                    status.put("threads_awaiting_connection", poolMXBean.getThreadsAwaitingConnection());
                    status.put("max_pool_size", hikariDS.getMaximumPoolSize());
                    status.put("min_idle", hikariDS.getMinimumIdle());
                }
            }
        } catch (Exception e) {
            status.put("error", "Unable to retrieve pool status: " + e.getMessage());
        }
        
        return status;
    }

    /**
     * Get database size in MB
     */
    private long getDatabaseSize(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT pg_database_size(current_database()) / (1024 * 1024)")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0;
    }

    /**
     * Get number of active connections
     */
    private int getActiveConnections(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT count(*) FROM pg_stat_activity WHERE state = 'active'")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
