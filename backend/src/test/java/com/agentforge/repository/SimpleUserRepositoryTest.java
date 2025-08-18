package com.agentforge.repository;

import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple UserRepository test focusing on basic CRUD operations
 * Uses @DataJpaTest with minimal configuration for repository testing only
 */
@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.autoconfigure.exclude=com.agentforge.AgentForgeApplication"
})
class SimpleUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPasswordHash("$2a$12$hashedpassword");
        testUser.setRole(UserRole.DEVELOPER);
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("1.1.1: Test basic User save operation")
    void testSaveUser() {
        User savedUser = userRepository.save(testUser);
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(savedUser.getIsActive()).isTrue();
        
        System.out.println("✅ CREATE: User saved successfully with ID: " + savedUser.getId());
    }

    @Test
    @DisplayName("1.1.2: Test basic User findById operation")
    void testFindById() {
        User savedUser = userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
        
        System.out.println("✅ READ: User found by ID successfully");
    }

    @Test
    @DisplayName("1.1.3: Test basic User update operation")
    void testUpdateUser() {
        User savedUser = userRepository.save(testUser);
        
        savedUser.setEmail("updated@example.com");
        savedUser.setRole(UserRole.ADMIN);
        
        User updatedUser = userRepository.save(savedUser);
        
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ADMIN);
        
        System.out.println("✅ UPDATE: User updated successfully");
    }

    @Test
    @DisplayName("1.1.4: Test basic User delete operation")
    void testDeleteUser() {
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();
        
        userRepository.deleteById(userId);
        
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
        
        System.out.println("✅ DELETE: User deleted successfully");
    }

    @Test
    @DisplayName("1.1.5: Test findByUsername custom query")
    void testFindByUsername() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByUsername("testuser");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
        
        System.out.println("✅ Custom Query: findByUsername working correctly");
    }

    @Test
    @DisplayName("1.1.6: Test findByEmail custom query")
    void testFindByEmail() {
        userRepository.save(testUser);
        
        Optional<User> foundUser = userRepository.findByEmail("testuser@example.com");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        
        System.out.println("✅ Custom Query: findByEmail working correctly");
    }

    @Test
    @DisplayName("1.1.7: Test findByIsActiveTrue custom query")
    void testFindByIsActiveTrue() {
        User inactiveUser = new User();
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPasswordHash("$2a$12$hashedpassword");
        inactiveUser.setRole(UserRole.VIEWER);
        inactiveUser.setIsActive(false);
        
        userRepository.save(testUser); // active
        userRepository.save(inactiveUser); // inactive
        
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getUsername()).isEqualTo("testuser");
        
        System.out.println("✅ Custom Query: findByIsActiveTrue working correctly");
    }

    @Test
    @DisplayName("1.1.8: Test count and exists operations")
    void testCountAndExists() {
        assertThat(userRepository.count()).isEqualTo(0);
        assertThat(userRepository.existsById(1L)).isFalse();
        
        User savedUser = userRepository.save(testUser);
        
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.existsById(savedUser.getId())).isTrue();
        
        System.out.println("✅ Count and Exists: Working correctly");
    }
}
