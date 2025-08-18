package com.agentforge.repository;

import com.agentforge.entity.ComplianceViolation;
import com.agentforge.entity.Project;
import com.agentforge.entity.Analysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic ComplianceViolationRepository test using manual entity validation
 * This test focuses on validating the ComplianceViolation entity structure and behavior
 * without complex Spring Boot context loading
 */
class BasicComplianceViolationRepositoryTest {

    private ComplianceViolation testViolation;
    private Project testProject;
    private Analysis testAnalysis;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("A test project for compliance violation testing");
        
        // Create test analysis
        testAnalysis = new Analysis();
        testAnalysis.setId(1L);
        testAnalysis.setProject(testProject);
        
        // Create test violation
        testViolation = new ComplianceViolation();
        testViolation.setProject(testProject);
        testViolation.setAnalysis(testAnalysis);
        testViolation.setRuleId("RULE-001");
        testViolation.setRuleName("Security Best Practice");
        testViolation.setRuleCategory("Security");
        testViolation.setSeverity(ComplianceViolation.ViolationSeverity.HIGH);
        testViolation.setFilePath("src/main/java/TestClass.java");
        testViolation.setLineNumber(42);
        testViolation.setColumnNumber(15);
        testViolation.setMessage("Potential security vulnerability detected");
        testViolation.setCodeSnippet("password = \"hardcoded\"");
        testViolation.setSuggestion("Use environment variables or secure configuration");
        testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN);
        testViolation.setFalsePositive(false);
        
        testDate = LocalDateTime.now();
    }

    @Test
    @DisplayName("1.4.1: Validate ComplianceViolation entity structure and getters/setters")
    void testComplianceViolationEntityStructure() {
        // Test basic entity structure
        assertNotNull(testViolation);
        assertEquals(testProject, testViolation.getProject());
        assertEquals(testAnalysis, testViolation.getAnalysis());
        assertEquals("RULE-001", testViolation.getRuleId());
        assertEquals("Security Best Practice", testViolation.getRuleName());
        assertEquals("Security", testViolation.getRuleCategory());
        assertEquals(ComplianceViolation.ViolationSeverity.HIGH, testViolation.getSeverity());
        assertEquals("src/main/java/TestClass.java", testViolation.getFilePath());
        assertEquals(42, testViolation.getLineNumber());
        assertEquals(15, testViolation.getColumnNumber());
        assertEquals("Potential security vulnerability detected", testViolation.getMessage());
        assertEquals("password = \"hardcoded\"", testViolation.getCodeSnippet());
        assertEquals("Use environment variables or secure configuration", testViolation.getSuggestion());
        assertEquals(ComplianceViolation.ViolationStatus.OPEN, testViolation.getStatus());
        assertFalse(testViolation.getFalsePositive());
        
        // Test setter methods
        testViolation.setRuleId("RULE-002");
        testViolation.setRuleName("Code Quality Rule");
        testViolation.setRuleCategory("Quality");
        testViolation.setSeverity(ComplianceViolation.ViolationSeverity.MEDIUM);
        testViolation.setFilePath("src/main/java/AnotherClass.java");
        testViolation.setLineNumber(100);
        testViolation.setColumnNumber(25);
        testViolation.setMessage("Code style violation");
        testViolation.setCodeSnippet("if(condition){");
        testViolation.setSuggestion("Add space after if keyword");
        testViolation.setStatus(ComplianceViolation.ViolationStatus.IN_PROGRESS);
        testViolation.setFalsePositive(true);
        
        assertEquals("RULE-002", testViolation.getRuleId());
        assertEquals("Code Quality Rule", testViolation.getRuleName());
        assertEquals("Quality", testViolation.getRuleCategory());
        assertEquals(ComplianceViolation.ViolationSeverity.MEDIUM, testViolation.getSeverity());
        assertEquals("src/main/java/AnotherClass.java", testViolation.getFilePath());
        assertEquals(100, testViolation.getLineNumber());
        assertEquals(25, testViolation.getColumnNumber());
        assertEquals("Code style violation", testViolation.getMessage());
        assertEquals("if(condition){", testViolation.getCodeSnippet());
        assertEquals("Add space after if keyword", testViolation.getSuggestion());
        assertEquals(ComplianceViolation.ViolationStatus.IN_PROGRESS, testViolation.getStatus());
        assertTrue(testViolation.getFalsePositive());
        
        System.out.println("✅ ComplianceViolation Entity: Structure and getters/setters working correctly");
    }

    @Test
    @DisplayName("1.4.2: Validate ViolationSeverity enum values")
    void testViolationSeverityEnum() {
        // Test enum values
        assertEquals("CRITICAL", ComplianceViolation.ViolationSeverity.CRITICAL.name());
        assertEquals("HIGH", ComplianceViolation.ViolationSeverity.HIGH.name());
        assertEquals("MEDIUM", ComplianceViolation.ViolationSeverity.MEDIUM.name());
        assertEquals("LOW", ComplianceViolation.ViolationSeverity.LOW.name());
        assertEquals("INFO", ComplianceViolation.ViolationSeverity.INFO.name());
        
        // Test enum ordinal
        assertEquals(0, ComplianceViolation.ViolationSeverity.CRITICAL.ordinal());
        assertEquals(1, ComplianceViolation.ViolationSeverity.HIGH.ordinal());
        assertEquals(2, ComplianceViolation.ViolationSeverity.MEDIUM.ordinal());
        assertEquals(3, ComplianceViolation.ViolationSeverity.LOW.ordinal());
        assertEquals(4, ComplianceViolation.ViolationSeverity.INFO.ordinal());
        
        // Test display names
        assertEquals("Critical", ComplianceViolation.ViolationSeverity.CRITICAL.getDisplayName());
        assertEquals("High", ComplianceViolation.ViolationSeverity.HIGH.getDisplayName());
        assertEquals("Medium", ComplianceViolation.ViolationSeverity.MEDIUM.getDisplayName());
        assertEquals("Low", ComplianceViolation.ViolationSeverity.LOW.getDisplayName());
        assertEquals("Info", ComplianceViolation.ViolationSeverity.INFO.getDisplayName());
        
        // Test priority values
        assertEquals(1, ComplianceViolation.ViolationSeverity.CRITICAL.getPriority());
        assertEquals(2, ComplianceViolation.ViolationSeverity.HIGH.getPriority());
        assertEquals(3, ComplianceViolation.ViolationSeverity.MEDIUM.getPriority());
        assertEquals(4, ComplianceViolation.ViolationSeverity.LOW.getPriority());
        assertEquals(5, ComplianceViolation.ViolationSeverity.INFO.getPriority());
        
        System.out.println("✅ ViolationSeverity Enum: All values, display names, and priorities working correctly");
    }

    @Test
    @DisplayName("1.4.3: Validate ViolationStatus enum values")
    void testViolationStatusEnum() {
        // Test enum values
        assertEquals("OPEN", ComplianceViolation.ViolationStatus.OPEN.name());
        assertEquals("IN_PROGRESS", ComplianceViolation.ViolationStatus.IN_PROGRESS.name());
        assertEquals("RESOLVED", ComplianceViolation.ViolationStatus.RESOLVED.name());
        assertEquals("FALSE_POSITIVE", ComplianceViolation.ViolationStatus.FALSE_POSITIVE.name());
        assertEquals("SUPPRESSED", ComplianceViolation.ViolationStatus.SUPPRESSED.name());
        assertEquals("WONT_FIX", ComplianceViolation.ViolationStatus.WONT_FIX.name());
        
        // Test enum ordinal
        assertEquals(0, ComplianceViolation.ViolationStatus.OPEN.ordinal());
        assertEquals(1, ComplianceViolation.ViolationStatus.IN_PROGRESS.ordinal());
        assertEquals(2, ComplianceViolation.ViolationStatus.RESOLVED.ordinal());
        assertEquals(3, ComplianceViolation.ViolationStatus.FALSE_POSITIVE.ordinal());
        assertEquals(4, ComplianceViolation.ViolationStatus.SUPPRESSED.ordinal());
        assertEquals(5, ComplianceViolation.ViolationStatus.WONT_FIX.ordinal());
        
        // Test display names
        assertEquals("Open", ComplianceViolation.ViolationStatus.OPEN.getDisplayName());
        assertEquals("In Progress", ComplianceViolation.ViolationStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Resolved", ComplianceViolation.ViolationStatus.RESOLVED.getDisplayName());
        assertEquals("False Positive", ComplianceViolation.ViolationStatus.FALSE_POSITIVE.getDisplayName());
        assertEquals("Suppressed", ComplianceViolation.ViolationStatus.SUPPRESSED.getDisplayName());
        assertEquals("Won't Fix", ComplianceViolation.ViolationStatus.WONT_FIX.getDisplayName());
        
        System.out.println("✅ ViolationStatus Enum: All values and display names working correctly");
    }

    @Test
    @DisplayName("1.4.4: Validate ComplianceViolation entity validation logic")
    void testComplianceViolationEntityValidation() {
        // Test that required fields are properly set
        assertNotNull(testViolation.getProject(), "Violation project should not be null");
        assertNotNull(testViolation.getRuleId(), "Rule ID should not be null");
        assertNotNull(testViolation.getRuleName(), "Rule name should not be null");
        assertNotNull(testViolation.getRuleCategory(), "Rule category should not be null");
        assertNotNull(testViolation.getSeverity(), "Violation severity should not be null");
        assertNotNull(testViolation.getMessage(), "Violation message should not be null");
        assertNotNull(testViolation.getStatus(), "Violation status should not be null");
        assertNotNull(testViolation.getFalsePositive(), "False positive flag should not be null");
        
        // Test that required fields are not empty
        assertFalse(testViolation.getRuleId().isEmpty(), "Rule ID should not be empty");
        assertFalse(testViolation.getRuleName().isEmpty(), "Rule name should not be empty");
        assertFalse(testViolation.getRuleCategory().isEmpty(), "Rule category should not be empty");
        assertFalse(testViolation.getMessage().isEmpty(), "Violation message should not be empty");
        
        // Test that line and column numbers are non-negative when set
        if (testViolation.getLineNumber() != null) {
            assertTrue(testViolation.getLineNumber() >= 0, "Line number should be non-negative");
        }
        if (testViolation.getColumnNumber() != null) {
            assertTrue(testViolation.getColumnNumber() >= 0, "Column number should be non-negative");
        }
        
        System.out.println("✅ ComplianceViolation Validation: Basic validation logic working correctly");
    }

    @Test
    @DisplayName("1.4.5: Validate ComplianceViolation entity state transitions")
    void testComplianceViolationEntityStateTransitions() {
        // Test initial state
        assertEquals(ComplianceViolation.ViolationStatus.OPEN, testViolation.getStatus(), "Violation should be OPEN initially");
        
        // Test status transitions
        testViolation.setStatus(ComplianceViolation.ViolationStatus.IN_PROGRESS);
        assertEquals(ComplianceViolation.ViolationStatus.IN_PROGRESS, testViolation.getStatus(), "Violation should be IN_PROGRESS");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.RESOLVED);
        assertEquals(ComplianceViolation.ViolationStatus.RESOLVED, testViolation.getStatus(), "Violation should be RESOLVED");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.FALSE_POSITIVE);
        assertEquals(ComplianceViolation.ViolationStatus.FALSE_POSITIVE, testViolation.getStatus(), "Violation should be FALSE_POSITIVE");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.SUPPRESSED);
        assertEquals(ComplianceViolation.ViolationStatus.SUPPRESSED, testViolation.getStatus(), "Violation should be SUPPRESSED");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.WONT_FIX);
        assertEquals(ComplianceViolation.ViolationStatus.WONT_FIX, testViolation.getStatus(), "Violation should be WONT_FIX");
        
        // Test reactivation
        testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN);
        assertEquals(ComplianceViolation.ViolationStatus.OPEN, testViolation.getStatus(), "Violation should be OPEN after reactivation");
        
        System.out.println("✅ ComplianceViolation State: State transitions working correctly");
    }

    @Test
    @DisplayName("1.4.6: Validate ComplianceViolation entity data integrity")
    void testComplianceViolationEntityDataIntegrity() {
        // Test that setting values doesn't corrupt other values
        Project originalProject = testViolation.getProject();
        String originalRuleId = testViolation.getRuleId();
        String originalMessage = testViolation.getMessage();
        ComplianceViolation.ViolationSeverity originalSeverity = testViolation.getSeverity();
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.IN_PROGRESS);
        testViolation.setFilePath("new/path/File.java");
        testViolation.setLineNumber(200);
        testViolation.setFalsePositive(true);
        
        // Original values should remain unchanged
        assertEquals(originalProject, testViolation.getProject(), "Violation project should remain unchanged");
        assertEquals(originalRuleId, testViolation.getRuleId(), "Rule ID should remain unchanged");
        assertEquals(originalMessage, testViolation.getMessage(), "Violation message should remain unchanged");
        assertEquals(originalSeverity, testViolation.getSeverity(), "Violation severity should remain unchanged");
        
        // Only specified values should change
        assertEquals(ComplianceViolation.ViolationStatus.IN_PROGRESS, testViolation.getStatus(), "Status should be IN_PROGRESS");
        assertEquals("new/path/File.java", testViolation.getFilePath(), "File path should be updated");
        assertEquals(200, testViolation.getLineNumber(), "Line number should be updated");
        assertTrue(testViolation.getFalsePositive(), "False positive flag should be updated");
        
        System.out.println("✅ ComplianceViolation Data Integrity: Data isolation working correctly");
    }

    @Test
    @DisplayName("1.4.7: Validate ComplianceViolation entity edge cases")
    void testComplianceViolationEntityEdgeCases() {
        // Test with null values for optional fields
        testViolation.setAnalysis(null);
        testViolation.setFilePath(null);
        testViolation.setLineNumber(null);
        testViolation.setColumnNumber(null);
        testViolation.setCodeSnippet(null);
        testViolation.setSuggestion(null);
        testViolation.setResolvedBy(null);
        testViolation.setResolvedAt(null);
        testViolation.setResolutionNotes(null);
        testViolation.setSuppressedUntil(null);
        testViolation.setSuppressionReason(null);
        
        assertNull(testViolation.getAnalysis(), "Analysis should accept null");
        assertNull(testViolation.getFilePath(), "File path should accept null");
        assertNull(testViolation.getLineNumber(), "Line number should accept null");
        assertNull(testViolation.getColumnNumber(), "Column number should accept null");
        assertNull(testViolation.getCodeSnippet(), "Code snippet should accept null");
        assertNull(testViolation.getSuggestion(), "Suggestion should accept null");
        assertNull(testViolation.getResolvedBy(), "Resolved by should accept null");
        assertNull(testViolation.getResolvedAt(), "Resolved at should accept null");
        assertNull(testViolation.getResolutionNotes(), "Resolution notes should accept null");
        assertNull(testViolation.getSuppressedUntil(), "Suppressed until should accept null");
        assertNull(testViolation.getSuppressionReason(), "Suppression reason should accept null");
        
        // Test with empty strings
        testViolation.setRuleId("");
        testViolation.setRuleName("");
        testViolation.setRuleCategory("");
        testViolation.setMessage("");
        
        assertEquals("", testViolation.getRuleId(), "Rule ID should accept empty string");
        assertEquals("", testViolation.getRuleName(), "Rule name should accept empty string");
        assertEquals("", testViolation.getRuleCategory(), "Rule category should accept empty string");
        assertEquals("", testViolation.getMessage(), "Violation message should accept empty string");
        
        // Test with very long values
        String longString = "a".repeat(1000);
        testViolation.setRuleId(longString);
        testViolation.setRuleName(longString);
        testViolation.setRuleCategory(longString);
        testViolation.setMessage(longString);
        
        assertEquals(longString, testViolation.getRuleId(), "Rule ID should accept long values");
        assertEquals(longString, testViolation.getRuleName(), "Rule name should accept long values");
        assertEquals(longString, testViolation.getRuleCategory(), "Rule category should accept long values");
        assertEquals(longString, testViolation.getMessage(), "Violation message should accept long values");
        
        System.out.println("✅ ComplianceViolation Edge Cases: Edge case handling working correctly");
    }

    @Test
    @DisplayName("1.4.8: Validate ComplianceViolation entity business rules")
    void testComplianceViolationEntityBusinessRules() {
        // Test that violation can have different severities
        assertDoesNotThrow(() -> testViolation.setSeverity(ComplianceViolation.ViolationSeverity.CRITICAL));
        assertDoesNotThrow(() -> testViolation.setSeverity(ComplianceViolation.ViolationSeverity.HIGH));
        assertDoesNotThrow(() -> testViolation.setSeverity(ComplianceViolation.ViolationSeverity.MEDIUM));
        assertDoesNotThrow(() -> testViolation.setSeverity(ComplianceViolation.ViolationSeverity.LOW));
        assertDoesNotThrow(() -> testViolation.setSeverity(ComplianceViolation.ViolationSeverity.INFO));
        
        // Test that violation can have different statuses
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN));
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.RESOLVED));
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.FALSE_POSITIVE));
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.SUPPRESSED));
        assertDoesNotThrow(() -> testViolation.setStatus(ComplianceViolation.ViolationStatus.WONT_FIX));
        
        // Test that counts can be updated
        assertDoesNotThrow(() -> testViolation.setLineNumber(1000));
        assertDoesNotThrow(() -> testViolation.setColumnNumber(100));
        
        // Test that boolean flags can be updated
        assertDoesNotThrow(() -> testViolation.setFalsePositive(true));
        assertDoesNotThrow(() -> testViolation.setFalsePositive(false));
        
        System.out.println("✅ ComplianceViolation Business Rules: Business rule validation working correctly");
    }

    @Test
    @DisplayName("1.4.9: Validate ComplianceViolation entity performance characteristics")
    void testComplianceViolationEntityPerformance() {
        // Test that entity operations are fast
        long startTime = System.nanoTime();
        
        // Perform multiple operations
        for (int i = 0; i < 1000; i++) {
            testViolation.setSeverity(ComplianceViolation.ViolationSeverity.values()[i % ComplianceViolation.ViolationSeverity.values().length]);
            testViolation.setStatus(ComplianceViolation.ViolationStatus.values()[i % ComplianceViolation.ViolationStatus.values().length]);
            testViolation.setLineNumber(i);
            testViolation.setColumnNumber(i % 100);
            testViolation.setFalsePositive(i % 2 == 0);
            testViolation.setRuleId("RULE-" + i);
            testViolation.setRuleName("Rule " + i);
            testViolation.setRuleCategory("Category " + (i % 5));
            testViolation.setMessage("Message " + i);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (1 second = 1,000,000,000 nanoseconds)
        assertTrue(duration < 1_000_000_000, "Entity operations should complete within 1 second");
        
        System.out.println("✅ ComplianceViolation Performance: Entity operations completed in " + 
            (duration / 1_000_000) + "ms");
    }

    @Test
    @DisplayName("1.4.10: Validate ComplianceViolation entity timestamp handling")
    void testComplianceViolationEntityTimestampHandling() {
        // Test that timestamps can be set and retrieved
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        LocalDateTime future = now.plusDays(1);
        
        testViolation.setCreatedAt(past);
        testViolation.setUpdatedAt(now);
        testViolation.setResolvedAt(future);
        testViolation.setSuppressedUntil(future);
        
        assertEquals(past, testViolation.getCreatedAt(), "Created at should be set correctly");
        assertEquals(now, testViolation.getUpdatedAt(), "Updated at should be set correctly");
        assertEquals(future, testViolation.getResolvedAt(), "Resolved at should be set correctly");
        assertEquals(future, testViolation.getSuppressedUntil(), "Suppressed until should be set correctly");
        
        // Test that timestamps are properly ordered
        assertTrue(testViolation.getCreatedAt().isBefore(testViolation.getUpdatedAt()), 
            "Created at should be before updated at");
        assertTrue(testViolation.getUpdatedAt().isBefore(testViolation.getResolvedAt()), 
            "Updated at should be before resolved at");
        
        System.out.println("✅ ComplianceViolation Timestamps: Timestamp handling working correctly");
    }

    @Test
    @DisplayName("1.4.11: Validate ComplianceViolation entity business methods")
    void testComplianceViolationEntityBusinessMethods() {
        // Test resolve method
        testViolation.resolve("testuser", "Issue has been fixed");
        assertEquals(ComplianceViolation.ViolationStatus.RESOLVED, testViolation.getStatus(), "Status should be RESOLVED after resolution");
        assertEquals("testuser", testViolation.getResolvedBy(), "Resolved by should be set");
        assertNotNull(testViolation.getResolvedAt(), "Resolved at should be set");
        assertEquals("Issue has been fixed", testViolation.getResolutionNotes(), "Resolution notes should be set");
        
        // Test markAsFalsePositive method
        testViolation.markAsFalsePositive("This is not a real violation");
        assertEquals(ComplianceViolation.ViolationStatus.FALSE_POSITIVE, testViolation.getStatus(), "Status should be FALSE_POSITIVE");
        assertTrue(testViolation.getFalsePositive(), "False positive flag should be true");
        assertEquals("This is not a real violation", testViolation.getResolutionNotes(), "Resolution notes should be updated");
        assertNotNull(testViolation.getResolvedAt(), "Resolved at should be updated");
        
        // Test suppress method
        LocalDateTime suppressUntil = LocalDateTime.now().plusDays(30);
        testViolation.suppress(suppressUntil, "Temporarily suppressed for refactoring");
        assertEquals(ComplianceViolation.ViolationStatus.SUPPRESSED, testViolation.getStatus(), "Status should be SUPPRESSED");
        assertEquals(suppressUntil, testViolation.getSuppressedUntil(), "Suppressed until should be set");
        assertEquals("Temporarily suppressed for refactoring", testViolation.getSuppressionReason(), "Suppression reason should be set");
        
        System.out.println("✅ ComplianceViolation Business Methods: Business methods working correctly");
    }

    @Test
    @DisplayName("1.4.12: Validate ComplianceViolation entity utility methods")
    void testComplianceViolationEntityUtilityMethods() {
        // Test isActive method
        testViolation.setStatus(ComplianceViolation.ViolationStatus.OPEN);
        assertTrue(testViolation.isActive(), "Open violations should be active");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.RESOLVED);
        assertFalse(testViolation.isActive(), "Resolved violations should not be active");
        
        testViolation.setStatus(ComplianceViolation.ViolationStatus.SUPPRESSED);
        testViolation.setSuppressedUntil(LocalDateTime.now().plusDays(1));
        assertTrue(testViolation.isActive(), "Suppressed violations with future date should be active");
        
        testViolation.setSuppressedUntil(LocalDateTime.now().minusDays(1));
        assertFalse(testViolation.isActive(), "Suppressed violations with past date should not be active");
        
        // Test getLocation method
        testViolation.setFilePath("src/main/java/Test.java");
        testViolation.setLineNumber(100);
        testViolation.setColumnNumber(25);
        assertEquals("src/main/java/Test.java:100:25", testViolation.getLocation(), "Location should include file, line, and column");
        
        testViolation.setColumnNumber(null);
        assertEquals("src/main/java/Test.java:100", testViolation.getLocation(), "Location should include file and line when column is null");
        
        testViolation.setLineNumber(null);
        assertEquals("src/main/java/Test.java", testViolation.getLocation(), "Location should include only file when line is null");
        
        testViolation.setFilePath(null);
        assertEquals("Unknown location", testViolation.getLocation(), "Location should be unknown when file path is null");
        
        System.out.println("✅ ComplianceViolation Utility Methods: Utility methods working correctly");
    }

    @Test
    @DisplayName("1.4.13: Validate ComplianceViolation entity relationships")
    void testComplianceViolationEntityRelationships() {
        // Test project relationship
        Project newProject = new Project();
        newProject.setId(2L);
        newProject.setName("Another Project");
        
        testViolation.setProject(newProject);
        assertEquals(newProject, testViolation.getProject(), "Project relationship should be updated");
        assertEquals(2L, testViolation.getProject().getId(), "Project ID should be accessible through relationship");
        
        // Test analysis relationship
        Analysis newAnalysis = new Analysis();
        newAnalysis.setId(2L);
        newAnalysis.setProject(newProject);
        
        testViolation.setAnalysis(newAnalysis);
        assertEquals(newAnalysis, testViolation.getAnalysis(), "Analysis relationship should be updated");
        assertEquals(2L, testViolation.getAnalysis().getId(), "Analysis ID should be accessible through relationship");
        
        // Test that relationships don't interfere with each other
        testViolation.setProject(testProject);
        testViolation.setAnalysis(testAnalysis);
        
        assertEquals(testProject, testViolation.getProject(), "Project should remain set after analysis change");
        assertEquals(testAnalysis, testViolation.getAnalysis(), "Analysis should remain set after project change");
        
        System.out.println("✅ ComplianceViolation Relationships: Entity relationships working correctly");
    }
}
