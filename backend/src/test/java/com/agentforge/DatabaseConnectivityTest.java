package com.agentforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import com.agentforge.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database connectivity and basic operations test
 * Tests PostgreSQL + pgvector connectivity and basic CRUD operations
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/agentforge",
    "spring.datasource.username=agentforge",
    "spring.datasource.password=agentforge",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
class DatabaseConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Test PostgreSQL database connectivity")
    void testDatabaseConnectivity() throws SQLException {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be open");
            
            String databaseName = connection.getCatalog();
            assertEquals("agentforge", databaseName, "Should connect to agentforge database");
            
            System.out.println("✅ Database connectivity test passed");
            System.out.println("   Database: " + databaseName);
            System.out.println("   URL: " + connection.getMetaData().getURL());
        }
    }

    @Test
    @DisplayName("Test pgvector extension availability")
    void testPgVectorExtension() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery(
                "SELECT EXISTS(SELECT 1 FROM pg_extension WHERE extname = 'vector') as has_vector"
            );
            
            assertTrue(resultSet.next(), "Query should return a result");
            assertTrue(resultSet.getBoolean("has_vector"), "pgvector extension should be available");
            
            System.out.println("✅ pgvector extension test passed");
        }
    }

    @Test
    @DisplayName("Test User entity CRUD operations")
    @Transactional
    void testUserEntityCrud() {
        // Create a test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$12$hashedpassword");
        user.setRole(UserRole.DEVELOPER);
        user.setIsActive(true);

        // Test Create
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId(), "User ID should be generated");
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        
        System.out.println("✅ User CREATE operation successful");
        System.out.println("   User ID: " + savedUser.getId());

        // Test Read
        var foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent(), "User should be found by ID");
        assertEquals("testuser", foundUser.get().getUsername());
        
        System.out.println("✅ User READ operation successful");

        // Test Update
        savedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(savedUser);
        assertEquals("updated@example.com", updatedUser.getEmail());
        
        System.out.println("✅ User UPDATE operation successful");

        // Test Delete
        userRepository.delete(savedUser);
        var deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent(), "User should not be found after deletion");
        
        System.out.println("✅ User DELETE operation successful");
    }

    @Test
    @DisplayName("Test User repository query methods")
    @Transactional
    void testUserRepositoryQueries() {
        // Create test users
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("$2a$12$hashedpassword1");
        user1.setRole(UserRole.ADMIN);
        user1.setIsActive(true);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("$2a$12$hashedpassword2");
        user2.setRole(UserRole.VIEWER);
        user2.setIsActive(false);

        userRepository.save(user1);
        userRepository.save(user2);

        // Test findByUsername
        var foundByUsername = userRepository.findByUsername("user1");
        assertTrue(foundByUsername.isPresent(), "User should be found by username");
        assertEquals("user1@example.com", foundByUsername.get().getEmail());

        // Test findByEmail
        var foundByEmail = userRepository.findByEmail("user2@example.com");
        assertTrue(foundByEmail.isPresent(), "User should be found by email");
        assertEquals("user2", foundByEmail.get().getUsername());

        // Test findByUsernameOrEmail (used by authentication)
        var foundByUsernameOrEmail = userRepository.findByUsernameOrEmail("user1", "user1");
        assertTrue(foundByUsernameOrEmail.isPresent(), "User should be found by username or email");
        assertEquals(UserRole.ADMIN, foundByUsernameOrEmail.get().getRole());

        System.out.println("✅ User repository query methods test passed");
        System.out.println("   Found users by username, email, and usernameOrEmail queries");
    }
}