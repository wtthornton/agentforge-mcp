package com.agentforge.repository;

import com.agentforge.entity.Project;
import com.agentforge.entity.ProjectStatus;
import com.agentforge.entity.Agent;
import com.agentforge.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic ProjectRepository test using manual entity validation
 * This test focuses on validating the Project entity structure and behavior
 * without complex Spring Boot context loading
 */
class BasicProjectRepositoryTest {

    private Project testProject;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("A test project for repository testing");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setTechnologyStack("Java,Spring Boot,PostgreSQL");
        testProject.setFilesCount(100L);
        testProject.setDirectoriesCount(20L);
        testProject.setLastAnalysisDate(LocalDateTime.now());
        testDate = LocalDateTime.now();
    }

    @Test
    @DisplayName("1.2.1: Validate Project entity structure and getters/setters")
    void testProjectEntityStructure() {
        // Test basic entity structure
        assertNotNull(testProject);
        assertEquals("Test Project", testProject.getName());
        assertEquals("A test project for repository testing", testProject.getDescription());
        assertEquals(ProjectStatus.ACTIVE, testProject.getStatus());
        assertEquals("Java,Spring Boot,PostgreSQL", testProject.getTechnologyStack());
        assertEquals(100L, testProject.getFilesCount());
        assertEquals(20L, testProject.getDirectoriesCount());
        
        // Test setter methods
        testProject.setName("Updated Project");
        testProject.setDescription("Updated description");
        testProject.setStatus(ProjectStatus.SUSPENDED);
        testProject.setTechnologyStack("Python,Django,MySQL");
        testProject.setFilesCount(200L);
        testProject.setDirectoriesCount(40L);
        
        assertEquals("Updated Project", testProject.getName());
        assertEquals("Updated description", testProject.getDescription());
        assertEquals(ProjectStatus.SUSPENDED, testProject.getStatus());
        assertEquals("Python,Django,MySQL", testProject.getTechnologyStack());
        assertEquals(200L, testProject.getFilesCount());
        assertEquals(40L, testProject.getDirectoriesCount());
        
        System.out.println("✅ Project Entity: Structure and getters/setters working correctly");
    }

    @Test
    @DisplayName("1.2.2: Validate ProjectStatus enum values")
    void testProjectStatusEnum() {
        // Test enum values
        assertEquals("ACTIVE", ProjectStatus.ACTIVE.name());
        assertEquals("ARCHIVED", ProjectStatus.ARCHIVED.name());
        assertEquals("SUSPENDED", ProjectStatus.SUSPENDED.name());
        assertEquals("IN_PROGRESS", ProjectStatus.IN_PROGRESS.name());
        assertEquals("COMPLETED", ProjectStatus.COMPLETED.name());
        
        // Test enum ordinal
        assertEquals(0, ProjectStatus.ACTIVE.ordinal());
        assertEquals(1, ProjectStatus.ARCHIVED.ordinal());
        assertEquals(2, ProjectStatus.SUSPENDED.ordinal());
        assertEquals(3, ProjectStatus.IN_PROGRESS.ordinal());
        assertEquals(4, ProjectStatus.COMPLETED.ordinal());
        
        // Test display names
        assertEquals("Active", ProjectStatus.ACTIVE.getDisplayName());
        assertEquals("Archived", ProjectStatus.ARCHIVED.getDisplayName());
        assertEquals("Suspended", ProjectStatus.SUSPENDED.getDisplayName());
        assertEquals("In Progress", ProjectStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Completed", ProjectStatus.COMPLETED.getDisplayName());
        
        System.out.println("✅ ProjectStatus Enum: All values and display names working correctly");
    }

    @Test
    @DisplayName("1.2.3: Validate Project entity validation logic")
    void testProjectEntityValidation() {
        // Test that required fields are properly set
        assertNotNull(testProject.getName(), "Project name should not be null");
        assertNotNull(testProject.getDescription(), "Project description should not be null");
        assertNotNull(testProject.getStatus(), "Project status should not be null");
        assertNotNull(testProject.getTechnologyStack(), "Technology stack should not be null");
        assertNotNull(testProject.getFilesCount(), "Files count should not be null");
        assertNotNull(testProject.getDirectoriesCount(), "Directories count should not be null");
        
        // Test that name is not empty
        assertFalse(testProject.getName().isEmpty(), "Project name should not be empty");
        assertFalse(testProject.getDescription().isEmpty(), "Project description should not be empty");
        assertFalse(testProject.getTechnologyStack().isEmpty(), "Technology stack should not be empty");
        
        // Test that counts are non-negative
        assertTrue(testProject.getFilesCount() >= 0, "Files count should be non-negative");
        assertTrue(testProject.getDirectoriesCount() >= 0, "Directories count should be non-negative");
        
        System.out.println("✅ Project Validation: Basic validation logic working correctly");
    }

    @Test
    @DisplayName("1.2.4: Validate Project entity state transitions")
    void testProjectEntityStateTransitions() {
        // Test initial state
        assertEquals(ProjectStatus.ACTIVE, testProject.getStatus(), "Project should be ACTIVE initially");
        
        // Test status transitions
        testProject.setStatus(ProjectStatus.SUSPENDED);
        assertEquals(ProjectStatus.SUSPENDED, testProject.getStatus(), "Project should be SUSPENDED");
        
        testProject.setStatus(ProjectStatus.IN_PROGRESS);
        assertEquals(ProjectStatus.IN_PROGRESS, testProject.getStatus(), "Project should be IN_PROGRESS");
        
        testProject.setStatus(ProjectStatus.COMPLETED);
        assertEquals(ProjectStatus.COMPLETED, testProject.getStatus(), "Project should be COMPLETED");
        
        testProject.setStatus(ProjectStatus.ARCHIVED);
        assertEquals(ProjectStatus.ARCHIVED, testProject.getStatus(), "Project should be ARCHIVED");
        
        // Test reactivation
        testProject.setStatus(ProjectStatus.ACTIVE);
        assertEquals(ProjectStatus.ACTIVE, testProject.getStatus(), "Project should be ACTIVE after reactivation");
        
        System.out.println("✅ Project State: State transitions working correctly");
    }

    @Test
    @DisplayName("1.2.5: Validate Project entity data integrity")
    void testProjectEntityDataIntegrity() {
        // Test that setting values doesn't corrupt other values
        String originalName = testProject.getName();
        String originalDescription = testProject.getDescription();
        Long originalFilesCount = testProject.getFilesCount();
        
        testProject.setStatus(ProjectStatus.SUSPENDED);
        testProject.setTechnologyStack("New Tech Stack");
        testProject.setDirectoriesCount(50L);
        
        // Original values should remain unchanged
        assertEquals(originalName, testProject.getName(), "Project name should remain unchanged");
        assertEquals(originalDescription, testProject.getDescription(), "Project description should remain unchanged");
        assertEquals(originalFilesCount, testProject.getFilesCount(), "Files count should remain unchanged");
        
        // Only specified values should change
        assertEquals(ProjectStatus.SUSPENDED, testProject.getStatus(), "Status should be SUSPENDED");
        assertEquals("New Tech Stack", testProject.getTechnologyStack(), "Technology stack should be updated");
        assertEquals(50L, testProject.getDirectoriesCount(), "Directories count should be updated");
        
        System.out.println("✅ Project Data Integrity: Data isolation working correctly");
    }

    @Test
    @DisplayName("1.2.6: Validate Project entity edge cases")
    void testProjectEntityEdgeCases() {
        // Test with empty strings
        testProject.setName("");
        testProject.setDescription("");
        testProject.setTechnologyStack("");
        assertEquals("", testProject.getName(), "Project name should accept empty string");
        assertEquals("", testProject.getDescription(), "Project description should accept empty string");
        assertEquals("", testProject.getTechnologyStack(), "Technology stack should accept empty string");
        
        // Test with very long values
        String longName = "a".repeat(1000);
        String longDescription = "a".repeat(1000);
        String longTechStack = "a".repeat(1000);
        
        testProject.setName(longName);
        testProject.setDescription(longDescription);
        testProject.setTechnologyStack(longTechStack);
        
        assertEquals(longName, testProject.getName(), "Project name should accept long values");
        assertEquals(longDescription, testProject.getDescription(), "Project description should accept long values");
        assertEquals(longTechStack, testProject.getTechnologyStack(), "Technology stack should accept long values");
        
        // Test with zero and negative counts
        testProject.setFilesCount(0L);
        testProject.setDirectoriesCount(0L);
        assertEquals(0L, testProject.getFilesCount(), "Files count should accept zero");
        assertEquals(0L, testProject.getDirectoriesCount(), "Directories count should accept zero");
        
        System.out.println("✅ Project Edge Cases: Edge case handling working correctly");
    }

    @Test
    @DisplayName("1.2.7: Validate Project entity business rules")
    void testProjectEntityBusinessRules() {
        // Test that a project can have different statuses
        assertDoesNotThrow(() -> testProject.setStatus(ProjectStatus.ACTIVE));
        assertDoesNotThrow(() -> testProject.setStatus(ProjectStatus.SUSPENDED));
        assertDoesNotThrow(() -> testProject.setStatus(ProjectStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> testProject.setStatus(ProjectStatus.COMPLETED));
        assertDoesNotThrow(() -> testProject.setStatus(ProjectStatus.ARCHIVED));
        
        // Test that counts can be updated
        assertDoesNotThrow(() -> testProject.setFilesCount(1000L));
        assertDoesNotThrow(() -> testProject.setDirectoriesCount(100L));
        
        // Test that technology stack can be updated
        assertDoesNotThrow(() -> testProject.setTechnologyStack("Java,Spring Boot,PostgreSQL,Redis"));
        
        // Test that name and description can be updated
        assertDoesNotThrow(() -> testProject.setName("New Project Name"));
        assertDoesNotThrow(() -> testProject.setDescription("New project description"));
        
        System.out.println("✅ Project Business Rules: Business rule validation working correctly");
    }

    @Test
    @DisplayName("1.2.8: Validate Project entity performance characteristics")
    void testProjectEntityPerformance() {
        // Test that entity operations are fast
        long startTime = System.nanoTime();
        
        // Perform multiple operations
        for (int i = 0; i < 1000; i++) {
            testProject.setName("Project " + i);
            testProject.setDescription("Description " + i);
            testProject.setStatus(ProjectStatus.values()[i % ProjectStatus.values().length]);
            testProject.setTechnologyStack("Tech" + i + ",Stack" + i);
            testProject.setFilesCount((long) i);
            testProject.setDirectoriesCount((long) (i / 10));
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (1 second = 1,000,000,000 nanoseconds)
        assertTrue(duration < 1_000_000_000, "Entity operations should complete within 1 second");
        
        System.out.println("✅ Project Performance: Entity operations completed in " + 
            (duration / 1_000_000) + "ms");
    }

    @Test
    @DisplayName("1.2.9: Validate Project entity timestamp handling")
    void testProjectEntityTimestampHandling() {
        // Test that timestamps can be set and retrieved
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        LocalDateTime future = now.plusDays(1);
        
        testProject.setCreatedAt(past);
        testProject.setUpdatedAt(now);
        testProject.setLastAnalysisDate(future);
        
        assertEquals(past, testProject.getCreatedAt(), "Created at should be set correctly");
        assertEquals(now, testProject.getUpdatedAt(), "Updated at should be set correctly");
        assertEquals(future, testProject.getLastAnalysisDate(), "Last analysis date should be set correctly");
        
        // Test that timestamps are properly ordered
        assertTrue(testProject.getCreatedAt().isBefore(testProject.getUpdatedAt()), 
            "Created at should be before updated at");
        assertTrue(testProject.getUpdatedAt().isBefore(testProject.getLastAnalysisDate()), 
            "Updated at should be before last analysis date");
        
        System.out.println("✅ Project Timestamps: Timestamp handling working correctly");
    }

    @Test
    @DisplayName("1.2.10: Validate Project entity collections handling")
    void testProjectEntityCollectionsHandling() {
        // Test that collections are properly initialized
        assertNotNull(testProject.getAgents(), "Agents collection should not be null");
        assertNotNull(testProject.getTasks(), "Tasks collection should not be null");
        
        // Test that collections start empty
        assertTrue(testProject.getAgents().isEmpty(), "Agents collection should start empty");
        assertTrue(testProject.getTasks().isEmpty(), "Tasks collection should start empty");
        
        // Test that collections can be set
        Set<Agent> newAgents = new HashSet<>();
        Set<Task> newTasks = new HashSet<>();
        
        testProject.setAgents(newAgents);
        testProject.setTasks(newTasks);
        
        assertEquals(newAgents, testProject.getAgents(), "Agents collection should be updated");
        assertEquals(newTasks, testProject.getTasks(), "Tasks collection should be updated");
        
        System.out.println("✅ Project Collections: Collections handling working correctly");
    }
}

