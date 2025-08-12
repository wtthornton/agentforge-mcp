package com.agentforge.config;

import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import com.agentforge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data Initializer for AgentForge
 * 
 * Creates default admin user and sample data for development and testing
 * Only runs when no users exist in the database
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only initialize if no users exist
            if (userRepository.count() == 0) {
                createDefaultUsers(userRepository, passwordEncoder);
                System.out.println("✓ Default users created successfully");
            } else {
                System.out.println("✓ Users already exist, skipping initialization");
            }
        };
    }

    private void createDefaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@agentforge.com");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole(UserRole.ADMIN);
        admin.setIsActive(true);
        admin.addPermission("SYSTEM_ADMIN");
        admin.addPermission("MANAGE_USERS");
        admin.addPermission("MANAGE_PROJECTS");
        admin.addPermission("MANAGE_AGENTS");
        userRepository.save(admin);

        // Create developer user
        User developer = new User();
        developer.setUsername("developer");
        developer.setEmail("dev@agentforge.com");
        developer.setPasswordHash(passwordEncoder.encode("password123"));
        developer.setRole(UserRole.DEVELOPER);
        developer.setIsActive(true);
        developer.addPermission("READ_PROJECTS");
        developer.addPermission("WRITE_PROJECTS");
        developer.addPermission("MANAGE_AGENTS");
        userRepository.save(developer);

        // Create viewer user
        User viewer = new User();
        viewer.setUsername("viewer");
        viewer.setEmail("viewer@agentforge.com");
        viewer.setPasswordHash(passwordEncoder.encode("password123"));
        viewer.setRole(UserRole.VIEWER);
        viewer.setIsActive(true);
        viewer.addPermission("READ_PROJECTS");
        userRepository.save(viewer);

        // Create additional test users
        User testUser1 = new User();
        testUser1.setUsername("johndoe");
        testUser1.setEmail("john.doe@example.com");
        testUser1.setPasswordHash(passwordEncoder.encode("password123"));
        testUser1.setRole(UserRole.DEVELOPER);
        testUser1.setIsActive(true);
        testUser1.addPermission("READ_PROJECTS");
        testUser1.addPermission("WRITE_PROJECTS");
        userRepository.save(testUser1);

        User testUser2 = new User();
        testUser2.setUsername("janesmith");
        testUser2.setEmail("jane.smith@example.com");
        testUser2.setPasswordHash(passwordEncoder.encode("password123"));
        testUser2.setRole(UserRole.VIEWER);
        testUser2.setIsActive(true);
        testUser2.addPermission("READ_PROJECTS");
        userRepository.save(testUser2);

        // Create inactive test user
        User inactiveUser = new User();
        inactiveUser.setUsername("testuser");
        inactiveUser.setEmail("test@example.com");
        inactiveUser.setPasswordHash(passwordEncoder.encode("password123"));
        inactiveUser.setRole(UserRole.DEVELOPER);
        inactiveUser.setIsActive(false);
        userRepository.save(inactiveUser);

        System.out.println("✓ Created 6 test users:");
        System.out.println("  - admin/admin@agentforge.com (ADMIN) - password: password123");
        System.out.println("  - developer/dev@agentforge.com (DEVELOPER) - password: password123");
        System.out.println("  - viewer/viewer@agentforge.com (VIEWER) - password: password123");
        System.out.println("  - johndoe/john.doe@example.com (DEVELOPER) - password: password123");
        System.out.println("  - janesmith/jane.smith@example.com (VIEWER) - password: password123");
        System.out.println("  - testuser/test@example.com (DEVELOPER, INACTIVE) - password: password123");
    }
}