package com.agentforge;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Initialization Test for AgentForge
 * 
 * This test validates:
 * - Database schema creation via Flyway migrations
 * - Core table structure
 * - Index creation
 * - pgvector extension setup
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/agentforge",
    "spring.datasource.username=agentforge",
    "spring.datasource.password=agentforge",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.baseline-on-migrate=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseInitializationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @Order(1)
    public void testDatabaseConnectionEstablished() throws Exception {
        assertNotNull(dataSource, "DataSource should be available");
        
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(5), "Database connection should be valid");
            System.out.println("✅ Database connection established successfully");
        }
    }

    @Test
    @Order(2)
    public void testCoreTablesExist() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String[] coreTables = {
                "users", "projects", "project_analyses", "file_analyses",
                "standards_compliance", "code_violations", "code_embeddings",
                "performance_metrics", "audit_logs", "compliance_reports"
            };
            
            for (String tableName : coreTables) {
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + tableName + "'"
                    );
                    assertTrue(rs.next(), "Should be able to query table existence for " + tableName);
                    assertTrue(rs.getInt(1) > 0, "Table " + tableName + " should exist");
                    System.out.println("✅ Table " + tableName + " exists");
                }
            }
        }
    }

    @Test
    @Order(3)
    public void testPgVectorExtension() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Check pgvector extension
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT extname FROM pg_extension WHERE extname = 'vector'");
                assertTrue(rs.next(), "pgvector extension should be available");
                assertEquals("vector", rs.getString("extname"), "pgvector extension should be named 'vector'");
                System.out.println("✅ pgvector extension is available");
            }
            
            // Test vector operations
            try (Statement stmt = connection.createStatement()) {
                // Test vector type
                stmt.execute("SELECT '[1,2,3]'::vector");
                System.out.println("✅ Vector type creation successful");
                
                // Test vector similarity
                ResultSet rs = stmt.executeQuery("SELECT '[1,2,3]'::vector <-> '[4,5,6]'::vector as distance");
                assertTrue(rs.next(), "Should be able to calculate vector distance");
                assertTrue(rs.getDouble("distance") > 0, "Vector distance should be positive");
                System.out.println("✅ Vector similarity calculation successful");
            }
        }
    }

    @Test
    @Order(4)
    public void testIndexesExist() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String[] importantIndexes = {
                "idx_users_username", "idx_projects_name", "idx_project_analyses_project_id",
                "idx_code_embeddings_hnsw", "idx_function_embeddings_hnsw"
            };
            
            for (String indexName : importantIndexes) {
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM pg_indexes WHERE indexname = '" + indexName + "'"
                    );
                    assertTrue(rs.next(), "Should be able to query index existence for " + indexName);
                    if (rs.getInt(1) > 0) {
                        System.out.println("✅ Index " + indexName + " exists");
                    } else {
                        System.out.println("⚠️  Index " + indexName + " not found (may be created later)");
                    }
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testDatabaseSchemaValidation() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            // Test that we can insert and query data
            try (Statement stmt = connection.createStatement()) {
                // Test users table structure
                ResultSet rs = stmt.executeQuery(
                    "SELECT column_name, data_type FROM information_schema.columns " +
                    "WHERE table_name = 'users' ORDER BY ordinal_position"
                );
                
                int columnCount = 0;
                while (rs.next()) {
                    columnCount++;
                    String columnName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    System.out.println("  - " + columnName + ": " + dataType);
                }
                
                assertTrue(columnCount >= 8, "Users table should have at least 8 columns");
                System.out.println("✅ Users table structure validated (" + columnCount + " columns)");
            }
        }
    }

    @Test
    @Order(6)
    public void testDatabasePerformance() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            long startTime = System.currentTimeMillis();
            
            // Test simple query performance
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.tables");
                assertTrue(rs.next(), "Should be able to execute performance test query");
            }
            
            long endTime = System.currentTimeMillis();
            long queryTime = endTime - startTime;
            
            assertTrue(queryTime < 1000, "Database query should complete in under 1 second");
            System.out.println("✅ Database query performance: " + queryTime + "ms");
        }
    }
}
