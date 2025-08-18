package com.agentforge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple database connectivity test without complex dependencies
 */
@SpringBootTest(classes = {})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/agentforge",
    "spring.datasource.username=agentforge", 
    "spring.datasource.password=agentforge",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=validate"
})
class SimpleDatabaseTest {

    @Test
    @DisplayName("Direct PostgreSQL connection test")
    void testPostgreSQLConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/agentforge";
        String username = "agentforge";
        String password = "agentforge";
        
        try (Connection connection = java.sql.DriverManager.getConnection(url, username, password)) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be open");
            
            // Test basic query
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT version()");
                assertTrue(rs.next(), "Should get PostgreSQL version");
                String version = rs.getString(1);
                assertTrue(version.contains("PostgreSQL"), "Should be PostgreSQL database");
                
                System.out.println("✅ PostgreSQL connectivity successful");
                System.out.println("   Version: " + version);
            }
        }
    }
    
    @Test
    @DisplayName("Test pgvector extension")
    void testPgVectorExtension() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/agentforge";
        String username = "agentforge";
        String password = "agentforge";
        
        try (Connection connection = java.sql.DriverManager.getConnection(url, username, password)) {
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'vector') as has_vector"
                );
                assertTrue(rs.next(), "Query should return result");
                assertTrue(rs.getBoolean("has_vector"), "pgvector extension should be installed");
                
                System.out.println("✅ pgvector extension verified");
            }
        }
    }
    
    @Test
    @DisplayName("Test basic vector operations")
    void testVectorOperations() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/agentforge";
        String username = "agentforge";
        String password = "agentforge";
        
        try (Connection connection = java.sql.DriverManager.getConnection(url, username, password)) {
            try (Statement stmt = connection.createStatement()) {
                // Test vector creation and similarity
                ResultSet rs = stmt.executeQuery(
                    "SELECT '[1,2,3]'::vector <-> '[1,2,4]'::vector as distance"
                );
                assertTrue(rs.next(), "Vector operation should return result");
                double distance = rs.getDouble("distance");
                assertTrue(distance > 0, "Distance should be positive");
                
                System.out.println("✅ Vector operations working");
                System.out.println("   Distance between [1,2,3] and [1,2,4]: " + distance);
            }
        }
    }
    
    @Test
    @DisplayName("Test users table existence")
    void testUsersTableExists() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/agentforge";
        String username = "agentforge";
        String password = "agentforge";
        
        try (Connection connection = java.sql.DriverManager.getConnection(url, username, password)) {
            try (Statement stmt = connection.createStatement()) {
                // Check if users table exists
                ResultSet rs = stmt.executeQuery(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users') as table_exists"
                );
                assertTrue(rs.next(), "Query should return result");
                boolean tableExists = rs.getBoolean("table_exists");
                
                System.out.println("✅ Users table existence check");
                System.out.println("   Users table exists: " + tableExists);
                
                if (tableExists) {
                    // Get table structure
                    ResultSet columns = stmt.executeQuery(
                        "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users' ORDER BY ordinal_position"
                    );
                    System.out.println("   Table structure:");
                    while (columns.next()) {
                        System.out.println("     " + columns.getString("column_name") + " (" + columns.getString("data_type") + ")");
                    }
                }
            }
        }
    }
}