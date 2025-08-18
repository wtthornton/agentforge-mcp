package com.agentforge.repository;

import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import com.agentforge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced integration tests for UserRepository
 * Tests CRUD operations, custom query methods, and performance with real database
 * Uses Testcontainers for isolated PostgreSQL testing environment
 */
@DataJpaTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17-alpine")
            .asCompatibleSubstituteFor("postgres")
    )
    .withDatabaseName("agentforge_test")
    .withUsername("test")
    .withPassword("test")
    .withInitScript("init-test-db.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        
        // JPA Configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("spring.jpa.properties.hibernate.dialect", 
            () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private User testUser4;

    @BeforeEach
    void setUpTestData() {
        // Clean up any existing test data
        userRepository.deleteAll();
        
        // Create test users with comprehensive data
        testUser1 = createTestUser("testuser1", "testuser1@example.com", UserRole.ADMIN, true);
        testUser2 = createTestUser("testuser2", "testuser2@example.com", UserRole.DEVELOPER, true);
        testUser3 = createTestUser("testuser3", "testuser3@example.com", UserRole.VIEWER, false);
        testUser4 = createTestUser("testuser4", "testuser4@example.com", UserRole.DEVELOPER, true);
    }
    
    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User createTestUser(String username, String email, UserRole role, boolean isActive) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("$2a$12$hashedpassword" + username);
        user.setRole(role);
        user.setIsActive(isActive);
        user.setLastLogin(LocalDateTime.now().minusDays(isActive ? 1 : 10));
        return user;
    }

    @Test
    @DisplayName("1.1.1: Test UserRepository CREATE operation with performance validation")
    void testCreateOperation() {
        long startTime = System.nanoTime();
        
        // CREATE - Test save
        User savedUser = userRepository.save(testUser1);
        
        long duration = System.nanoTime() - startTime;
        
        // Validate creation
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser1");
        assertThat(savedUser.getEmail()).isEqualTo("testuser1@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedUser.getIsActive()).isTrue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        
        // Performance validation - should complete within 100ms
        assertThat(duration).isLessThan(100_000_000); // 100ms in nanoseconds
        
        System.out.println("✅ CREATE: User saved successfully with ID: " + savedUser.getId());
        System.out.println("⏱️  Performance: " + Duration.ofNanos(duration).toMillis() + "ms");
    }

    @Test
    @DisplayName("1.1.2: Test UserRepository READ operations with performance validation")
    void testReadOperations() {
        // Save test user first
        User savedUser = userRepository.save(testUser1);
        
        // Test findById with performance measurement
        long startTime = System.nanoTime();
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        long duration = System.nanoTime() - startTime;
        
        // Validate findById
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser1");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser1@example.com");
        
        // Performance validation
        assertThat(duration).isLessThan(100_000_000); // 100ms
        
        // Test findByUsername
        startTime = System.nanoTime();
        Optional<User> foundByUsername = userRepository.findByUsername("testuser1");
        duration = System.nanoTime() - startTime;
        
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getEmail()).isEqualTo("testuser1@example.com");
        assertThat(duration).isLessThan(100_000_000);
        
        // Test findByEmail
        startTime = System.nanoTime();
        Optional<User> foundByEmail = userRepository.findByEmail("testuser1@example.com");
        duration = System.nanoTime() - startTime;
        
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getUsername()).isEqualTo("testuser1");
        assertThat(duration).isLessThan(100_000_000);
        
        System.out.println("✅ READ: All read operations completed successfully");
        System.out.println("⏱️  Performance: All queries under 100ms");
    }

    @Test
    @DisplayName("1.1.3: Test UserRepository UPDATE operation with performance validation")
    void testUpdateOperation() {
        // Save test user first
        User savedUser = userRepository.save(testUser1);
        
        // UPDATE - Test save (update)
        savedUser.setEmail("updated@example.com");
        savedUser.setRole(UserRole.DEVELOPER);
        savedUser.setLastLogin(LocalDateTime.now());
        
        long startTime = System.nanoTime();
        User updatedUser = userRepository.save(savedUser);
        long duration = System.nanoTime() - startTime;
        
        // Validate update
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(updatedUser.getLastLogin()).isNotNull();
        assertThat(updatedUser.getUpdatedAt()).isAfter(updatedUser.getCreatedAt());
        
        // Performance validation
        assertThat(duration).isLessThan(100_000_000); // 100ms
        
        System.out.println("✅ UPDATE: User updated successfully");
        System.out.println("⏱️  Performance: " + Duration.ofNanos(duration).toMillis() + "ms");
    }

    @Test
    @DisplayName("1.1.4: Test UserRepository DELETE operation with performance validation")
    void testDeleteOperation() {
        // Save test user first
        User savedUser = userRepository.save(testUser1);
        Long userId = savedUser.getId();
        
        // Verify user exists
        assertThat(userRepository.findById(userId)).isPresent();
        
        // DELETE - Test deleteById
        long startTime = System.nanoTime();
        userRepository.deleteById(userId);
        long duration = System.nanoTime() - startTime;
        
        // Verify deletion
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
        
        // Performance validation
        assertThat(duration).isLessThan(100_000_000); // 100ms
        
        System.out.println("✅ DELETE: User deleted successfully");
        System.out.println("⏱️  Performance: " + Duration.ofNanos(duration).toMillis() + "ms");
    }

    @Test
    @DisplayName("1.1.5: Test UserRepository custom query methods with performance validation")
    void testCustomQueryMethods() {
        // Save multiple test users
        userRepository.save(testUser1); // active ADMIN
        userRepository.save(testUser2); // active DEVELOPER
        userRepository.save(testUser3); // inactive VIEWER
        userRepository.save(testUser4); // active DEVELOPER
        
        // Test findByIsActiveTrue
        long startTime = System.nanoTime();
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        long duration = System.nanoTime() - startTime;
        
        assertThat(activeUsers).hasSize(3);
        activeUsers.forEach(user -> assertThat(user.getIsActive()).isTrue());
        assertThat(duration).isLessThan(100_000_000);
        
        // Test findByRole
        startTime = System.nanoTime();
        List<User> developers = userRepository.findByRole(UserRole.DEVELOPER);
        duration = System.nanoTime() - startTime;
        
        assertThat(developers).hasSize(2);
        developers.forEach(user -> assertThat(user.getRole()).isEqualTo(UserRole.DEVELOPER));
        assertThat(duration).isLessThan(100_000_000);
        
        // Test findByUsernameOrEmail
        startTime = System.nanoTime();
        Optional<User> foundByUsername = userRepository.findByUsernameOrEmail("testuser1", "different");
        duration = System.nanoTime() - startTime;
        
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getUsername()).isEqualTo("testuser1");
        assertThat(duration).isLessThan(100_000_000);
        
        System.out.println("✅ Custom Queries: All custom query methods working correctly");
        System.out.println("⏱️  Performance: All queries under 100ms");
    }

    @Test
    @DisplayName("1.1.6: Test UserRepository pagination and sorting with performance validation")
    void testPaginationAndSorting() {
        // Save multiple test users
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);
        
        // Test pagination
        Pageable pageable = PageRequest.of(0, 2);
        long startTime = System.nanoTime();
        Page<User> userPage = userRepository.findAll(pageable);
        long duration = System.nanoTime() - startTime;
        
        assertThat(userPage.getContent()).hasSize(2);
        assertThat(userPage.getTotalElements()).isEqualTo(4);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
        assertThat(duration).isLessThan(100_000_000);
        
        System.out.println("✅ Pagination: Working correctly with " + userPage.getTotalPages() + " pages");
        System.out.println("⏱️  Performance: " + Duration.ofNanos(duration).toMillis() + "ms");
    }

    @Test
    @DisplayName("1.1.7: Test UserRepository error handling and edge cases")
    void testErrorHandlingAndEdgeCases() {
        // Test finding non-existent user
        Optional<User> notFound = userRepository.findById(999999L);
        assertThat(notFound).isEmpty();
        
        // Test finding by non-existent username
        Optional<User> notFoundByUsername = userRepository.findByUsername("nonexistent");
        assertThat(notFoundByUsername).isEmpty();
        
        // Test finding by non-existent email
        Optional<User> notFoundByEmail = userRepository.findByEmail("nonexistent@example.com");
        assertThat(notFoundByEmail).isEmpty();
        
        // Test empty result sets
        List<User> emptyActiveUsers = userRepository.findByIsActiveTrue();
        assertThat(emptyActiveUsers).isEmpty();
        
        System.out.println("✅ Error Handling: All edge cases handled correctly");
    }

    @Test
    @DisplayName("1.1.8: Test UserRepository bulk operations with performance validation")
    void testBulkOperations() {
        // Test bulk save
        List<User> usersToSave = List.of(testUser1, testUser2, testUser3, testUser4);
        
        long startTime = System.nanoTime();
        List<User> savedUsers = userRepository.saveAll(usersToSave);
        long duration = System.nanoTime() - startTime;
        
        assertThat(savedUsers).hasSize(4);
        savedUsers.forEach(user -> assertThat(user.getId()).isNotNull());
        
        // Performance validation for bulk operations (allow more time)
        assertThat(duration).isLessThan(500_000_000); // 500ms for bulk operations
        
        // Test bulk delete
        startTime = System.nanoTime();
        userRepository.deleteAll();
        duration = System.nanoTime() - startTime;
        
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(duration).isLessThan(500_000_000);
        
        System.out.println("✅ Bulk Operations: Save and delete working correctly");
        System.out.println("⏱️  Performance: All operations under 500ms");
    }
}
