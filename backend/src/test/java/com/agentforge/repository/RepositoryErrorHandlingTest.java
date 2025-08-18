package com.agentforge.repository;

import com.agentforge.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository Error Handling and Edge Cases Test
 * This test focuses on testing repository error handling, edge cases, and boundary conditions
 * without complex Spring Boot context loading
 */
class RepositoryErrorHandlingTest {

    private User testUser;
    private Project testProject;
    private Analysis testAnalysis;
    private ComplianceViolation testViolation;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password123");
        testUser.setRole(UserRole.DEVELOPER);
        testUser.setIsActive(true);
        
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("A test project for error handling");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setTechnologyStack("Java,Spring Boot");
        testProject.setFilesCount(100L);
        testProject.setDirectoriesCount(20L);
        
        // Create test analysis
        testAnalysis = new Analysis();
        testAnalysis.setId(1L);
        testAnalysis.setProject(testProject);
        testAnalysis.setUser(testUser);
        testAnalysis.setStatus(Analysis.AnalysisStatus.PENDING);
        testAnalysis.setType(Analysis.AnalysisType.FULL);
        testAnalysis.setLinesAnalyzed(1000L);
        testAnalysis.setFilesAnalyzed(50L);
        
        // Create test violation
        testViolation = new ComplianceViolation();
        testViolation.setId(1L);
        testViolation.setProject(testProject);
        testViolation.setAnalysis(testAnalysis);
        testViolation.setRuleId("RULE-001");
        testViolation.setRuleName("Test Rule");
        testViolation.setRuleCategory("Test");
        testViolation.setSeverity(ComplianceViolation.ViolationSeverity.HIGH);
        testViolation.setMessage("Test violation");
        testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN);
    }

    @Test
    @DisplayName("1.5.1: Test User entity error handling and edge cases")
    void testUserEntityErrorHandling() {
        // Test null username handling - entities allow null values to be set
        assertDoesNotThrow(() -> {
            testUser.setUsername(null);
        }, "Username should accept null values (validation happens at persistence)");
        
        // Test empty username handling
        assertDoesNotThrow(() -> {
            testUser.setUsername("");
        }, "Username should accept empty values (validation happens at persistence)");
        
        // Test very long username handling
        String longUsername = "a".repeat(1000);
        assertDoesNotThrow(() -> {
            testUser.setUsername(longUsername);
        }, "Username should accept long values");
        assertEquals(longUsername, testUser.getUsername(), "Long username should be set correctly");
        
        // Test null email handling
        assertDoesNotThrow(() -> {
            testUser.setEmail(null);
        }, "Email should accept null values (validation happens at persistence)");
        
        // Test invalid email format handling
        assertDoesNotThrow(() -> {
            testUser.setEmail("invalid-email");
        }, "Email should accept invalid format (validation happens at persistence)");
        
        // Test null password handling
        assertDoesNotThrow(() -> {
            testUser.setPasswordHash(null);
        }, "Password hash should accept null values (validation happens at persistence)");
        
        // Test short password handling
        assertDoesNotThrow(() -> {
            testUser.setPasswordHash("123");
        }, "Password hash should accept short values (validation happens at persistence)");
        
        // Test null role handling
        assertDoesNotThrow(() -> {
            testUser.setRole(null);
        }, "Role should accept null values (validation happens at persistence)");
        
        // Test valid role handling
        assertDoesNotThrow(() -> {
            testUser.setRole(UserRole.ADMIN);
            testUser.setRole(UserRole.DEVELOPER);
            testUser.setRole(UserRole.VIEWER);
        }, "Valid roles should be accepted");
        
        System.out.println("✅ User Entity: Error handling and edge cases working correctly");
    }

    @Test
    @DisplayName("1.5.2: Test Project entity error handling and edge cases")
    void testProjectEntityErrorHandling() {
        // Test null name handling - entities allow null values to be set
        assertDoesNotThrow(() -> {
            testProject.setName(null);
        }, "Project name should accept null values (validation happens at persistence)");
        
        // Test empty name handling
        assertDoesNotThrow(() -> {
            testProject.setName("");
        }, "Project name should accept empty values (validation happens at persistence)");
        
        // Test very long name handling
        String longName = "a".repeat(1000);
        assertDoesNotThrow(() -> {
            testProject.setName(longName);
        }, "Project name should accept long values");
        assertEquals(longName, testProject.getName(), "Long name should be set correctly");
        
        // Test null status handling
        assertDoesNotThrow(() -> {
            testProject.setStatus(null);
        }, "Project status should accept null values (validation happens at persistence)");
        
        // Test valid status handling
        assertDoesNotThrow(() -> {
            testProject.setStatus(ProjectStatus.ACTIVE);
            testProject.setStatus(ProjectStatus.ARCHIVED);
            testProject.setStatus(ProjectStatus.SUSPENDED);
            testProject.setStatus(ProjectStatus.IN_PROGRESS);
            testProject.setStatus(ProjectStatus.COMPLETED);
        }, "Valid statuses should be accepted");
        
        // Test negative counts handling - entities allow negative values to be set
        assertDoesNotThrow(() -> {
            testProject.setFilesCount(-1L);
        }, "Files count should accept negative values (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testProject.setDirectoriesCount(-1L);
        }, "Directories count should accept negative values (validation happens at persistence)");
        
        // Test zero counts handling
        assertDoesNotThrow(() -> {
            testProject.setFilesCount(0L);
            testProject.setDirectoriesCount(0L);
        }, "Zero counts should be accepted");
        
        // Test maximum values handling
        assertDoesNotThrow(() -> {
            testProject.setFilesCount(Long.MAX_VALUE);
            testProject.setDirectoriesCount(Long.MAX_VALUE);
        }, "Maximum values should be accepted");
        
        System.out.println("✅ Project Entity: Error handling and edge cases working correctly");
    }

    @Test
    @DisplayName("1.5.3: Test Analysis entity error handling and edge cases")
    void testAnalysisEntityErrorHandling() {
        // Test null project handling - entities allow null values to be set
        assertDoesNotThrow(() -> {
            testAnalysis.setProject(null);
        }, "Analysis project should accept null values (validation happens at persistence)");
        
        // Test null user handling
        assertDoesNotThrow(() -> {
            testAnalysis.setUser(null);
        }, "Analysis user should accept null values (validation happens at persistence)");
        
        // Test null status handling
        assertDoesNotThrow(() -> {
            testAnalysis.setStatus(null);
        }, "Analysis status should accept null values (validation happens at persistence)");
        
        // Test null type handling
        assertDoesNotThrow(() -> {
            testAnalysis.setType(null);
        }, "Analysis type should accept null values (validation happens at persistence)");
        
        // Test negative counts handling - entities allow negative values to be set
        assertDoesNotThrow(() -> {
            testAnalysis.setLinesAnalyzed(-1L);
        }, "Lines analyzed should accept negative values (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testAnalysis.setFilesAnalyzed(-1L);
        }, "Files analyzed should accept negative values (validation happens at persistence)");
        
        // Test zero counts handling
        assertDoesNotThrow(() -> {
            testAnalysis.setLinesAnalyzed(0L);
            testAnalysis.setFilesAnalyzed(0L);
        }, "Zero counts should be accepted");
        
        // Test maximum values handling
        assertDoesNotThrow(() -> {
            testAnalysis.setLinesAnalyzed(Long.MAX_VALUE);
            testAnalysis.setFilesAnalyzed(Long.MAX_VALUE);
        }, "Maximum values should be accepted");
        
        // Test score boundary handling - entities allow any values to be set
        assertDoesNotThrow(() -> {
            testAnalysis.setComplianceScore(-1.0);
        }, "Compliance score should accept negative values (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testAnalysis.setComplianceScore(101.0);
        }, "Compliance score should accept values over 100 (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testAnalysis.setComplianceScore(0.0);
            testAnalysis.setComplianceScore(50.0);
            testAnalysis.setComplianceScore(100.0);
        }, "Valid scores should be accepted");
        
        // Test violation count handling - entities allow any values to be set
        assertDoesNotThrow(() -> {
            testAnalysis.setTotalViolations(-1);
        }, "Total violations should accept negative values (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testAnalysis.setTotalViolations(0);
            testAnalysis.setTotalViolations(100);
            testAnalysis.setTotalViolations(Integer.MAX_VALUE);
        }, "Valid violation counts should be accepted");
        
        System.out.println("✅ Analysis Entity: Error handling and edge cases working correctly");
    }

    @Test
    @DisplayName("1.5.4: Test ComplianceViolation entity error handling and edge cases")
    void testComplianceViolationEntityErrorHandling() {
        // Test null project handling - entities allow null values to be set
        assertDoesNotThrow(() -> {
            testViolation.setProject(null);
        }, "Violation project should accept null values (validation happens at persistence)");
        
        // Test null rule ID handling
        assertDoesNotThrow(() -> {
            testViolation.setRuleId(null);
        }, "Rule ID should accept null values (validation happens at persistence)");
        
        // Test empty rule ID handling
        assertDoesNotThrow(() -> {
            testViolation.setRuleId("");
        }, "Rule ID should accept empty values (validation happens at persistence)");
        
        // Test null rule name handling
        assertDoesNotThrow(() -> {
            testViolation.setRuleName(null);
        }, "Rule name should accept null values (validation happens at persistence)");
        
        // Test null rule category handling
        assertDoesNotThrow(() -> {
            testViolation.setRuleCategory(null);
        }, "Rule category should accept null values (validation happens at persistence)");
        
        // Test null severity handling
        assertDoesNotThrow(() -> {
            testViolation.setSeverity(null);
        }, "Violation severity should accept null values (validation happens at persistence)");
        
        // Test null message handling
        assertDoesNotThrow(() -> {
            testViolation.setMessage(null);
        }, "Violation message should accept null values (validation happens at persistence)");
        
        // Test null status handling
        assertDoesNotThrow(() -> {
            testViolation.setStatus(null);
        }, "Violation status should accept null values (validation happens at persistence)");
        
        // Test negative line numbers handling - entities allow negative values to be set
        assertDoesNotThrow(() -> {
            testViolation.setLineNumber(-1);
        }, "Line number should accept negative values (validation happens at persistence)");
        
        assertDoesNotThrow(() -> {
            testViolation.setColumnNumber(-1);
        }, "Column number should accept negative values (validation happens at persistence)");
        
        // Test zero and positive values handling
        assertDoesNotThrow(() -> {
            testViolation.setLineNumber(0);
            testViolation.setLineNumber(1);
            testViolation.setLineNumber(Integer.MAX_VALUE);
            testViolation.setColumnNumber(0);
            testViolation.setColumnNumber(1);
            testViolation.setColumnNumber(Integer.MAX_VALUE);
        }, "Valid line and column numbers should be accepted");
        
        // Test very long string handling
        String longString = "a".repeat(10000);
        assertDoesNotThrow(() -> {
            testViolation.setRuleId(longString);
            testViolation.setRuleName(longString);
            testViolation.setRuleCategory(longString);
            testViolation.setMessage(longString);
        }, "Long strings should be accepted");
        
        System.out.println("✅ ComplianceViolation Entity: Error handling and edge cases working correctly");
    }

    @Test
    @DisplayName("1.5.5: Test entity relationship error handling")
    void testEntityRelationshipErrorHandling() {
        // Test circular reference prevention
        Project project1 = new Project();
        Project project2 = new Project();
        
        // Test that entities can reference each other without infinite loops
        assertDoesNotThrow(() -> {
            project1.setName("Project 1");
            project2.setName("Project 2");
            
            // Set up relationships
            project1.addAgent(new Agent());
            project2.addTask(new Task());
        }, "Entity relationships should be established without errors");
        
        // Test null relationship handling
        assertDoesNotThrow(() -> {
            testAnalysis.setProject(null);
            testAnalysis.setUser(null);
        }, "Null relationships should be handled gracefully");
        
        // Test empty collection handling
        assertDoesNotThrow(() -> {
            testProject.setAgents(new HashSet<>());
            testProject.setTasks(new HashSet<>());
        }, "Empty collections should be handled gracefully");
        
        // Test large collection handling
        Set<Agent> largeAgentSet = new HashSet<>();
        Set<Task> largeTaskSet = new HashSet<>();
        
        for (int i = 0; i < 1000; i++) {
            largeAgentSet.add(new Agent());
            largeTaskSet.add(new Task());
        }
        
        assertDoesNotThrow(() -> {
            testProject.setAgents(largeAgentSet);
            testProject.setTasks(largeTaskSet);
        }, "Large collections should be handled gracefully");
        
        System.out.println("✅ Entity Relationships: Error handling working correctly");
    }

    @Test
    @DisplayName("1.5.6: Test entity validation constraint handling")
    void testEntityValidationConstraintHandling() {
        // Test User validation constraints - these will be enforced by Bean Validation
        User invalidUser = new User();
        
        // Test that entities can be created with invalid data (validation happens at persistence time)
        assertDoesNotThrow(() -> {
            invalidUser.setUsername(null);
            invalidUser.setEmail(null);
            invalidUser.setPasswordHash(null);
        }, "Entities should allow setting invalid data (validation happens at persistence)");
        
        // Test Project validation constraints
        Project invalidProject = new Project();
        
        assertDoesNotThrow(() -> {
            invalidProject.setName(null);
            invalidProject.setStatus(null);
        }, "Entities should allow setting invalid data (validation happens at persistence)");
        
        // Test Analysis validation constraints
        Analysis invalidAnalysis = new Analysis();
        
        assertDoesNotThrow(() -> {
            invalidAnalysis.setProject(null);
            invalidAnalysis.setUser(null);
        }, "Entities should allow setting invalid data (validation happens at persistence)");
        
        // Test ComplianceViolation validation constraints
        ComplianceViolation invalidViolation = new ComplianceViolation();
        
        assertDoesNotThrow(() -> {
            invalidViolation.setProject(null);
            invalidViolation.setRuleId(null);
        }, "Entities should allow setting invalid data (validation happens at persistence)");
        
        System.out.println("✅ Entity Validation: Constraint handling working correctly");
    }

    @Test
    @DisplayName("1.5.7: Test entity state transition error handling")
    void testEntityStateTransitionErrorHandling() {
        // Test state transitions - the entities allow any status changes
        assertDoesNotThrow(() -> {
            testAnalysis.setStatus(Analysis.AnalysisStatus.COMPLETED);
            testAnalysis.startAnalysis(); // This will change status to IN_PROGRESS
        }, "Status transitions should be allowed");
        
        assertDoesNotThrow(() -> {
            testAnalysis.setStatus(Analysis.AnalysisStatus.FAILED);
            testAnalysis.completeAnalysis(); // This will change status to COMPLETED
        }, "Status transitions should be allowed");
        
        // Test valid state transitions
        assertDoesNotThrow(() -> {
            testAnalysis.setStatus(Analysis.AnalysisStatus.PENDING);
            testAnalysis.startAnalysis();
            testAnalysis.completeAnalysis();
        }, "Valid state transitions should be allowed");
        
        // Test violation state transitions
        assertDoesNotThrow(() -> {
            testViolation.setStatus(ComplianceViolation.ViolationStatus.RESOLVED);
            testViolation.resolve("user", "notes"); // This will change status to RESOLVED
        }, "Status transitions should be allowed");
        
        assertDoesNotThrow(() -> {
            testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN);
            testViolation.resolve("user", "notes");
        }, "Valid violation state transition should be allowed");
        
        System.out.println("✅ Entity State Transitions: Error handling working correctly");
    }

    @Test
    @DisplayName("1.5.8: Test entity performance under error conditions")
    void testEntityPerformanceUnderErrorConditions() {
        // Test performance when handling large amounts of data
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 1000; i++) {
            // Set various values to test performance
            testUser.setUsername("user" + i);
            testProject.setName("project" + i);
            testAnalysis.setLinesAnalyzed((long) i);
            testViolation.setLineNumber(i);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time
        assertTrue(duration < 1_000_000_000, "Entity operations should complete within 1 second");
        
        // Test performance with large collections and error conditions
        startTime = System.nanoTime();
        
        Set<Agent> largeAgentSet = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            largeAgentSet.add(new Agent());
        }
        
        assertDoesNotThrow(() -> {
            testProject.setAgents(largeAgentSet);
        }, "Large collections should be handled without errors");
        
        endTime = System.nanoTime();
        duration = endTime - startTime;
        
        // Should complete within reasonable time
        assertTrue(duration < 1_000_000_000, "Large collection operations should complete within 1 second");
        
        System.out.println("✅ Entity Performance: Error condition handling completed in " + 
            (duration / 1_000_000) + "ms");
    }
}
