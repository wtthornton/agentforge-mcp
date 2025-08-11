package com.agentforge;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Database Setup Runner for AgentForge
 * 
 * This class provides a simple way to:
 * - Initialize database schema
 * - Validate database connectivity
 * - Test basic operations
 * - Run outside of the test framework
 */
@SpringBootApplication
@Profile("setup")
public class DatabaseSetupRunner {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseSetupRunner.class, args);
    }

    @Bean
    @Profile("setup")
    public CommandLineRunner databaseSetup(DataSource dataSource) {
        return args -> {
            System.out.println("🚀 Starting AgentForge Database Setup...");
            
            try {
                // Test basic connection
                testDatabaseConnection(dataSource);
                
                // Test schema
                testDatabaseSchema(dataSource);
                
                // Test pgvector
                testPgVectorExtension(dataSource);
                
                System.out.println("✅ Database setup completed successfully!");
                
            } catch (Exception e) {
                System.err.println("❌ Database setup failed: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        };
    }

    private void testDatabaseConnection(DataSource dataSource) throws Exception {
        System.out.println("🔍 Testing database connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            if (isValid) {
                System.out.println("✅ Database connection established successfully");
                
                // Get database info
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT version()");
                    if (rs.next()) {
                        System.out.println("📊 Database: " + rs.getString(1));
                    }
                }
            } else {
                throw new RuntimeException("Database connection is not valid");
            }
        }
    }

    private void testDatabaseSchema(DataSource dataSource) throws Exception {
        System.out.println("🔍 Testing database schema...");
        
        try (Connection connection = dataSource.getConnection()) {
            String[] coreTables = {
                "users", "projects", "project_analyses", "file_analyses",
                "standards_compliance", "code_violations", "code_embeddings"
            };
            
            int existingTables = 0;
            for (String tableName : coreTables) {
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + tableName + "'"
                    );
                    if (rs.next() && rs.getInt(1) > 0) {
                        existingTables++;
                        System.out.println("✅ Table " + tableName + " exists");
                    } else {
                        System.out.println("⚠️  Table " + tableName + " not found");
                    }
                }
            }
            
            System.out.println("📊 Schema validation: " + existingTables + "/" + coreTables.length + " core tables exist");
            
            if (existingTables < coreTables.length) {
                System.out.println("⚠️  Some tables are missing. Run Flyway migrations to create them.");
            }
        }
    }

    private void testPgVectorExtension(DataSource dataSource) throws Exception {
        System.out.println("🔍 Testing pgvector extension...");
        
        try (Connection connection = dataSource.getConnection()) {
            // Check if pgvector extension exists
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT extname FROM pg_extension WHERE extname = 'vector'");
                if (rs.next()) {
                    System.out.println("✅ pgvector extension is available");
                    
                    // Test vector operations
                    try {
                        stmt.execute("SELECT '[1,2,3]'::vector");
                        System.out.println("✅ Vector type creation successful");
                        
                        rs = stmt.executeQuery("SELECT '[1,2,3]'::vector <-> '[4,5,6]'::vector as distance");
                        if (rs.next()) {
                            double distance = rs.getDouble("distance");
                            System.out.println("✅ Vector similarity calculation successful (distance: " + distance + ")");
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️  Vector operations failed: " + e.getMessage());
                    }
                } else {
                    System.out.println("⚠️  pgvector extension not found");
                    System.out.println("💡 Install pgvector extension: CREATE EXTENSION IF NOT EXISTS vector;");
                }
            }
        }
    }
}
