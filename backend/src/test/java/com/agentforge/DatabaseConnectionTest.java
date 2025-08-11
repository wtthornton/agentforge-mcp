package com.agentforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Connection Test for AgentForge
 * 
 * This test validates:
 * - PostgreSQL connection establishment
 * - pgvector extension availability
 * - Basic database operations
 * - Schema creation and table access
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/agentforge",
    "spring.datasource.username=agentforge",
    "spring.datasource.password=agentforge",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true"
})
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5), "Database connection should be valid");
            
            // Test basic query
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 as test_value");
                assertTrue(rs.next(), "Should be able to execute basic query");
                assertEquals(1, rs.getInt("test_value"), "Basic query should return expected value");
            }
        }
    }

    @Test
    public void testPgVectorExtension() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Check if pgvector extension is available
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT extname FROM pg_extension WHERE extname = 'vector'");
                assertTrue(rs.next(), "pgvector extension should be available");
                assertEquals("vector", rs.getString("extname"), "pgvector extension should be named 'vector'");
            }
        }
    }

    @Test
    public void testDatabaseSchema() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Check if core tables exist
            try (Statement stmt = connection.createStatement()) {
                // Test users table
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'");
                assertTrue(rs.next(), "Should be able to query information_schema");
                assertTrue(rs.getInt(1) > 0, "users table should exist");
                
                // Test projects table
                rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'projects'");
                assertTrue(rs.next(), "Should be able to query projects table info");
                assertTrue(rs.getInt(1) > 0, "projects table should exist");
                
                // Test code_embeddings table (for pgvector)
                rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'code_embeddings'");
                assertTrue(rs.next(), "Should be able to query code_embeddings table info");
                assertTrue(rs.getInt(1) > 0, "code_embeddings table should exist");
            }
        }
    }

    @Test
    public void testVectorOperations() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Test basic vector operations
            try (Statement stmt = connection.createStatement()) {
                // Test vector type creation
                stmt.execute("SELECT '[1,2,3]'::vector");
                
                // Test vector similarity function
                ResultSet rs = stmt.executeQuery("SELECT '[1,2,3]'::vector <-> '[4,5,6]'::vector as distance");
                assertTrue(rs.next(), "Should be able to calculate vector distance");
                assertTrue(rs.getDouble("distance") > 0, "Vector distance should be positive");
            }
        }
    }
}
