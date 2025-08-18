package com.agentforge.repository;

import com.agentforge.entity.Analysis;
import com.agentforge.entity.Project;
import com.agentforge.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic AnalysisRepository test using manual entity validation
 * This test focuses on validating the Analysis entity structure and behavior
 * without complex Spring Boot context loading
 */
class BasicAnalysisRepositoryTest {

    private Analysis testAnalysis;
    private Project testProject;
    private User testUser;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        // Create test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("A test project for analysis testing");
        
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        // Create test analysis
        testAnalysis = new Analysis();
        testAnalysis.setProject(testProject);
        testAnalysis.setUser(testUser);
        testAnalysis.setStatus(Analysis.AnalysisStatus.PENDING);
        testAnalysis.setType(Analysis.AnalysisType.FULL);
        testAnalysis.setStartTime(LocalDateTime.now());
        testAnalysis.setLinesAnalyzed(1000L);
        testAnalysis.setFilesAnalyzed(50L);
        testAnalysis.setComplianceScore(85.0);
        testAnalysis.setQualityScore(90.0);
        testAnalysis.setSecurityScore(95.0);
        testAnalysis.setPerformanceScore(88.0);
        testAnalysis.setTotalViolations(5);
        testAnalysis.setCriticalViolations(1);
        testAnalysis.setWarningViolations(3);
        testAnalysis.setInfoViolations(1);
        testAnalysis.setAnalysisSummary("Test analysis completed successfully");
        
        testDate = LocalDateTime.now();
    }

    @Test
    @DisplayName("1.3.1: Validate Analysis entity structure and getters/setters")
    void testAnalysisEntityStructure() {
        // Test basic entity structure
        assertNotNull(testAnalysis);
        assertEquals(testProject, testAnalysis.getProject());
        assertEquals(testUser, testAnalysis.getUser());
        assertEquals(Analysis.AnalysisStatus.PENDING, testAnalysis.getStatus());
        assertEquals(Analysis.AnalysisType.FULL, testAnalysis.getType());
        assertEquals(1000L, testAnalysis.getLinesAnalyzed());
        assertEquals(50L, testAnalysis.getFilesAnalyzed());
        assertEquals(85.0, testAnalysis.getComplianceScore());
        assertEquals(90.0, testAnalysis.getQualityScore());
        assertEquals(95.0, testAnalysis.getSecurityScore());
        assertEquals(88.0, testAnalysis.getPerformanceScore());
        assertEquals(5, testAnalysis.getTotalViolations());
        assertEquals(1, testAnalysis.getCriticalViolations());
        assertEquals(3, testAnalysis.getWarningViolations());
        assertEquals(1, testAnalysis.getInfoViolations());
        assertEquals("Test analysis completed successfully", testAnalysis.getAnalysisSummary());
        
        // Test setter methods
        testAnalysis.setStatus(Analysis.AnalysisStatus.IN_PROGRESS);
        testAnalysis.setType(Analysis.AnalysisType.SECURITY);
        testAnalysis.setLinesAnalyzed(2000L);
        testAnalysis.setFilesAnalyzed(100L);
        testAnalysis.setComplianceScore(95.0);
        testAnalysis.setQualityScore(98.0);
        testAnalysis.setSecurityScore(99.0);
        testAnalysis.setPerformanceScore(92.0);
        testAnalysis.setTotalViolations(2);
        testAnalysis.setCriticalViolations(0);
        testAnalysis.setWarningViolations(1);
        testAnalysis.setInfoViolations(1);
        testAnalysis.setAnalysisSummary("Updated analysis summary");
        
        assertEquals(Analysis.AnalysisStatus.IN_PROGRESS, testAnalysis.getStatus());
        assertEquals(Analysis.AnalysisType.SECURITY, testAnalysis.getType());
        assertEquals(2000L, testAnalysis.getLinesAnalyzed());
        assertEquals(100L, testAnalysis.getFilesAnalyzed());
        assertEquals(95.0, testAnalysis.getComplianceScore());
        assertEquals(98.0, testAnalysis.getQualityScore());
        assertEquals(99.0, testAnalysis.getSecurityScore());
        assertEquals(92.0, testAnalysis.getPerformanceScore());
        assertEquals(2, testAnalysis.getTotalViolations());
        assertEquals(0, testAnalysis.getCriticalViolations());
        assertEquals(1, testAnalysis.getWarningViolations());
        assertEquals(1, testAnalysis.getInfoViolations());
        assertEquals("Updated analysis summary", testAnalysis.getAnalysisSummary());
        
        System.out.println("✅ Analysis Entity: Structure and getters/setters working correctly");
    }

    @Test
    @DisplayName("1.3.2: Validate AnalysisStatus enum values")
    void testAnalysisStatusEnum() {
        // Test enum values
        assertEquals("PENDING", Analysis.AnalysisStatus.PENDING.name());
        assertEquals("IN_PROGRESS", Analysis.AnalysisStatus.IN_PROGRESS.name());
        assertEquals("COMPLETED", Analysis.AnalysisStatus.COMPLETED.name());
        assertEquals("FAILED", Analysis.AnalysisStatus.FAILED.name());
        assertEquals("CANCELLED", Analysis.AnalysisStatus.CANCELLED.name());
        
        // Test enum ordinal
        assertEquals(0, Analysis.AnalysisStatus.PENDING.ordinal());
        assertEquals(1, Analysis.AnalysisStatus.IN_PROGRESS.ordinal());
        assertEquals(2, Analysis.AnalysisStatus.COMPLETED.ordinal());
        assertEquals(3, Analysis.AnalysisStatus.FAILED.ordinal());
        assertEquals(4, Analysis.AnalysisStatus.CANCELLED.ordinal());
        
        // Test display names
        assertEquals("Pending", Analysis.AnalysisStatus.PENDING.getDisplayName());
        assertEquals("In Progress", Analysis.AnalysisStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Completed", Analysis.AnalysisStatus.COMPLETED.getDisplayName());
        assertEquals("Failed", Analysis.AnalysisStatus.FAILED.getDisplayName());
        assertEquals("Cancelled", Analysis.AnalysisStatus.CANCELLED.getDisplayName());
        
        System.out.println("✅ AnalysisStatus Enum: All values and display names working correctly");
    }

    @Test
    @DisplayName("1.3.3: Validate AnalysisType enum values")
    void testAnalysisTypeEnum() {
        // Test enum values
        assertEquals("FULL", Analysis.AnalysisType.FULL.name());
        assertEquals("INCREMENTAL", Analysis.AnalysisType.INCREMENTAL.name());
        assertEquals("COMPLIANCE", Analysis.AnalysisType.COMPLIANCE.name());
        assertEquals("SECURITY", Analysis.AnalysisType.SECURITY.name());
        assertEquals("PERFORMANCE", Analysis.AnalysisType.PERFORMANCE.name());
        
        // Test enum ordinal
        assertEquals(0, Analysis.AnalysisType.FULL.ordinal());
        assertEquals(1, Analysis.AnalysisType.INCREMENTAL.ordinal());
        assertEquals(2, Analysis.AnalysisType.COMPLIANCE.ordinal());
        assertEquals(3, Analysis.AnalysisType.SECURITY.ordinal());
        assertEquals(4, Analysis.AnalysisType.PERFORMANCE.ordinal());
        
        // Test display names
        assertEquals("Full Analysis", Analysis.AnalysisType.FULL.getDisplayName());
        assertEquals("Incremental Analysis", Analysis.AnalysisType.INCREMENTAL.getDisplayName());
        assertEquals("Compliance Check", Analysis.AnalysisType.COMPLIANCE.getDisplayName());
        assertEquals("Security Scan", Analysis.AnalysisType.SECURITY.getDisplayName());
        assertEquals("Performance Analysis", Analysis.AnalysisType.PERFORMANCE.getDisplayName());
        
        System.out.println("✅ AnalysisType Enum: All values and display names working correctly");
    }

    @Test
    @DisplayName("1.3.4: Validate Analysis entity validation logic")
    void testAnalysisEntityValidation() {
        // Test that required fields are properly set
        assertNotNull(testAnalysis.getProject(), "Analysis project should not be null");
        assertNotNull(testAnalysis.getUser(), "Analysis user should not be null");
        assertNotNull(testAnalysis.getStatus(), "Analysis status should not be null");
        assertNotNull(testAnalysis.getType(), "Analysis type should not be null");
        assertNotNull(testAnalysis.getLinesAnalyzed(), "Lines analyzed should not be null");
        assertNotNull(testAnalysis.getFilesAnalyzed(), "Files analyzed should not be null");
        assertNotNull(testAnalysis.getComplianceScore(), "Compliance score should not be null");
        assertNotNull(testAnalysis.getQualityScore(), "Quality score should not be null");
        assertNotNull(testAnalysis.getSecurityScore(), "Security score should not be null");
        assertNotNull(testAnalysis.getPerformanceScore(), "Performance score should not be null");
        assertNotNull(testAnalysis.getTotalViolations(), "Total violations should not be null");
        assertNotNull(testAnalysis.getCriticalViolations(), "Critical violations should not be null");
        assertNotNull(testAnalysis.getWarningViolations(), "Warning violations should not be null");
        assertNotNull(testAnalysis.getInfoViolations(), "Info violations should not be null");
        assertNotNull(testAnalysis.getAnalysisSummary(), "Analysis summary should not be null");
        
        // Test that counts are non-negative
        assertTrue(testAnalysis.getLinesAnalyzed() >= 0, "Lines analyzed should be non-negative");
        assertTrue(testAnalysis.getFilesAnalyzed() >= 0, "Files analyzed should be non-negative");
        assertTrue(testAnalysis.getTotalViolations() >= 0, "Total violations should be non-negative");
        assertTrue(testAnalysis.getCriticalViolations() >= 0, "Critical violations should be non-negative");
        assertTrue(testAnalysis.getWarningViolations() >= 0, "Warning violations should be non-negative");
        assertTrue(testAnalysis.getInfoViolations() >= 0, "Info violations should be non-negative");
        
        // Test that scores are within valid range
        assertTrue(testAnalysis.getComplianceScore() >= 0.0 && testAnalysis.getComplianceScore() <= 100.0, 
            "Compliance score should be between 0 and 100");
        assertTrue(testAnalysis.getQualityScore() >= 0.0 && testAnalysis.getQualityScore() <= 100.0, 
            "Quality score should be between 0 and 100");
        assertTrue(testAnalysis.getSecurityScore() >= 0.0 && testAnalysis.getSecurityScore() <= 100.0, 
            "Security score should be between 0 and 100");
        assertTrue(testAnalysis.getPerformanceScore() >= 0.0 && testAnalysis.getPerformanceScore() <= 100.0, 
            "Performance score should be between 0 and 100");
        
        System.out.println("✅ Analysis Validation: Basic validation logic working correctly");
    }

    @Test
    @DisplayName("1.3.5: Validate Analysis entity state transitions")
    void testAnalysisEntityStateTransitions() {
        // Test initial state
        assertEquals(Analysis.AnalysisStatus.PENDING, testAnalysis.getStatus(), "Analysis should be PENDING initially");
        
        // Test status transitions
        testAnalysis.setStatus(Analysis.AnalysisStatus.IN_PROGRESS);
        assertEquals(Analysis.AnalysisStatus.IN_PROGRESS, testAnalysis.getStatus(), "Analysis should be IN_PROGRESS");
        
        testAnalysis.setStatus(Analysis.AnalysisStatus.COMPLETED);
        assertEquals(Analysis.AnalysisStatus.COMPLETED, testAnalysis.getStatus(), "Analysis should be COMPLETED");
        
        testAnalysis.setStatus(Analysis.AnalysisStatus.FAILED);
        assertEquals(Analysis.AnalysisStatus.FAILED, testAnalysis.getStatus(), "Analysis should be FAILED");
        
        testAnalysis.setStatus(Analysis.AnalysisStatus.CANCELLED);
        assertEquals(Analysis.AnalysisStatus.CANCELLED, testAnalysis.getStatus(), "Analysis should be CANCELLED");
        
        // Test reactivation
        testAnalysis.setStatus(Analysis.AnalysisStatus.PENDING);
        assertEquals(Analysis.AnalysisStatus.PENDING, testAnalysis.getStatus(), "Analysis should be PENDING after reactivation");
        
        System.out.println("✅ Analysis State: State transitions working correctly");
    }

    @Test
    @DisplayName("1.3.6: Validate Analysis entity data integrity")
    void testAnalysisEntityDataIntegrity() {
        // Test that setting values doesn't corrupt other values
        Project originalProject = testAnalysis.getProject();
        User originalUser = testAnalysis.getUser();
        Long originalLinesAnalyzed = testAnalysis.getLinesAnalyzed();
        Double originalComplianceScore = testAnalysis.getComplianceScore();
        
        testAnalysis.setStatus(Analysis.AnalysisStatus.IN_PROGRESS);
        testAnalysis.setType(Analysis.AnalysisType.SECURITY);
        testAnalysis.setFilesAnalyzed(200L);
        testAnalysis.setQualityScore(95.0);
        
        // Original values should remain unchanged
        assertEquals(originalProject, testAnalysis.getProject(), "Analysis project should remain unchanged");
        assertEquals(originalUser, testAnalysis.getUser(), "Analysis user should remain unchanged");
        assertEquals(originalLinesAnalyzed, testAnalysis.getLinesAnalyzed(), "Lines analyzed should remain unchanged");
        assertEquals(originalComplianceScore, testAnalysis.getComplianceScore(), "Compliance score should remain unchanged");
        
        // Only specified values should change
        assertEquals(Analysis.AnalysisStatus.IN_PROGRESS, testAnalysis.getStatus(), "Status should be IN_PROGRESS");
        assertEquals(Analysis.AnalysisType.SECURITY, testAnalysis.getType(), "Type should be SECURITY");
        assertEquals(200L, testAnalysis.getFilesAnalyzed(), "Files analyzed should be updated");
        assertEquals(95.0, testAnalysis.getQualityScore(), "Quality score should be updated");
        
        System.out.println("✅ Analysis Data Integrity: Data isolation working correctly");
    }

    @Test
    @DisplayName("1.3.7: Validate Analysis entity edge cases")
    void testAnalysisEntityEdgeCases() {
        // Test with zero values
        testAnalysis.setLinesAnalyzed(0L);
        testAnalysis.setFilesAnalyzed(0L);
        testAnalysis.setTotalViolations(0);
        testAnalysis.setCriticalViolations(0);
        testAnalysis.setWarningViolations(0);
        testAnalysis.setInfoViolations(0);
        
        assertEquals(0L, testAnalysis.getLinesAnalyzed(), "Lines analyzed should accept zero");
        assertEquals(0L, testAnalysis.getFilesAnalyzed(), "Files analyzed should accept zero");
        assertEquals(0, testAnalysis.getTotalViolations(), "Total violations should accept zero");
        assertEquals(0, testAnalysis.getCriticalViolations(), "Critical violations should accept zero");
        assertEquals(0, testAnalysis.getWarningViolations(), "Warning violations should accept zero");
        assertEquals(0, testAnalysis.getInfoViolations(), "Info violations should accept zero");
        
        // Test with maximum values
        testAnalysis.setLinesAnalyzed(Long.MAX_VALUE);
        testAnalysis.setFilesAnalyzed(Long.MAX_VALUE);
        testAnalysis.setTotalViolations(Integer.MAX_VALUE);
        testAnalysis.setCriticalViolations(Integer.MAX_VALUE);
        testAnalysis.setWarningViolations(Integer.MAX_VALUE);
        testAnalysis.setInfoViolations(Integer.MAX_VALUE);
        
        assertEquals(Long.MAX_VALUE, testAnalysis.getLinesAnalyzed(), "Lines analyzed should accept maximum value");
        assertEquals(Long.MAX_VALUE, testAnalysis.getFilesAnalyzed(), "Files analyzed should accept maximum value");
        assertEquals(Integer.MAX_VALUE, testAnalysis.getTotalViolations(), "Total violations should accept maximum value");
        assertEquals(Integer.MAX_VALUE, testAnalysis.getCriticalViolations(), "Critical violations should accept maximum value");
        assertEquals(Integer.MAX_VALUE, testAnalysis.getWarningViolations(), "Warning violations should accept maximum value");
        assertEquals(Integer.MAX_VALUE, testAnalysis.getInfoViolations(), "Info violations should accept maximum value");
        
        // Test with boundary scores
        testAnalysis.setComplianceScore(0.0);
        testAnalysis.setQualityScore(100.0);
        testAnalysis.setSecurityScore(50.0);
        testAnalysis.setPerformanceScore(75.5);
        
        assertEquals(0.0, testAnalysis.getComplianceScore(), "Compliance score should accept 0.0");
        assertEquals(100.0, testAnalysis.getQualityScore(), "Quality score should accept 100.0");
        assertEquals(50.0, testAnalysis.getSecurityScore(), "Security score should accept 50.0");
        assertEquals(75.5, testAnalysis.getPerformanceScore(), "Performance score should accept 75.5");
        
        System.out.println("✅ Analysis Edge Cases: Edge case handling working correctly");
    }

    @Test
    @DisplayName("1.3.8: Validate Analysis entity business rules")
    void testAnalysisEntityBusinessRules() {
        // Test that analysis can have different statuses
        assertDoesNotThrow(() -> testAnalysis.setStatus(Analysis.AnalysisStatus.PENDING));
        assertDoesNotThrow(() -> testAnalysis.setStatus(Analysis.AnalysisStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> testAnalysis.setStatus(Analysis.AnalysisStatus.COMPLETED));
        assertDoesNotThrow(() -> testAnalysis.setStatus(Analysis.AnalysisStatus.FAILED));
        assertDoesNotThrow(() -> testAnalysis.setStatus(Analysis.AnalysisStatus.CANCELLED));
        
        // Test that analysis can have different types
        assertDoesNotThrow(() -> testAnalysis.setType(Analysis.AnalysisType.FULL));
        assertDoesNotThrow(() -> testAnalysis.setType(Analysis.AnalysisType.INCREMENTAL));
        assertDoesNotThrow(() -> testAnalysis.setType(Analysis.AnalysisType.COMPLIANCE));
        assertDoesNotThrow(() -> testAnalysis.setType(Analysis.AnalysisType.SECURITY));
        assertDoesNotThrow(() -> testAnalysis.setType(Analysis.AnalysisType.PERFORMANCE));
        
        // Test that counts can be updated
        assertDoesNotThrow(() -> testAnalysis.setLinesAnalyzed(10000L));
        assertDoesNotThrow(() -> testAnalysis.setFilesAnalyzed(500L));
        assertDoesNotThrow(() -> testAnalysis.setTotalViolations(100));
        assertDoesNotThrow(() -> testAnalysis.setCriticalViolations(10));
        
        // Test that scores can be updated
        assertDoesNotThrow(() -> testAnalysis.setComplianceScore(75.5));
        assertDoesNotThrow(() -> testAnalysis.setQualityScore(85.0));
        assertDoesNotThrow(() -> testAnalysis.setSecurityScore(92.5));
        assertDoesNotThrow(() -> testAnalysis.setPerformanceScore(88.0));
        
        System.out.println("✅ Analysis Business Rules: Business rule validation working correctly");
    }

    @Test
    @DisplayName("1.3.9: Validate Analysis entity performance characteristics")
    void testAnalysisEntityPerformance() {
        // Test that entity operations are fast
        long startTime = System.nanoTime();
        
        // Perform multiple operations
        for (int i = 0; i < 1000; i++) {
            testAnalysis.setStatus(Analysis.AnalysisStatus.values()[i % Analysis.AnalysisStatus.values().length]);
            testAnalysis.setType(Analysis.AnalysisType.values()[i % Analysis.AnalysisType.values().length]);
            testAnalysis.setLinesAnalyzed((long) i);
            testAnalysis.setFilesAnalyzed((long) (i / 10));
            testAnalysis.setComplianceScore((double) (i % 100));
            testAnalysis.setQualityScore((double) (i % 100));
            testAnalysis.setSecurityScore((double) (i % 100));
            testAnalysis.setPerformanceScore((double) (i % 100));
            testAnalysis.setTotalViolations(i % 100);
            testAnalysis.setCriticalViolations(i % 20);
            testAnalysis.setWarningViolations(i % 50);
            testAnalysis.setInfoViolations(i % 30);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (1 second = 1,000,000,000 nanoseconds)
        assertTrue(duration < 1_000_000_000, "Entity operations should complete within 1 second");
        
        System.out.println("✅ Analysis Performance: Entity operations completed in " + 
            (duration / 1_000_000) + "ms");
    }

    @Test
    @DisplayName("1.3.10: Validate Analysis entity timestamp handling")
    void testAnalysisEntityTimestampHandling() {
        // Test that timestamps can be set and retrieved
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        LocalDateTime future = now.plusDays(1);
        
        testAnalysis.setStartTime(past);
        testAnalysis.setEndTime(now);
        testAnalysis.setCreatedAt(past);
        testAnalysis.setUpdatedAt(now);
        
        assertEquals(past, testAnalysis.getStartTime(), "Start time should be set correctly");
        assertEquals(now, testAnalysis.getEndTime(), "End time should be set correctly");
        assertEquals(past, testAnalysis.getCreatedAt(), "Created at should be set correctly");
        assertEquals(now, testAnalysis.getUpdatedAt(), "Updated at should be set correctly");
        
        // Test that timestamps are properly ordered
        assertTrue(testAnalysis.getStartTime().isBefore(testAnalysis.getEndTime()), 
            "Start time should be before end time");
        assertTrue(testAnalysis.getCreatedAt().isBefore(testAnalysis.getUpdatedAt()), 
            "Created at should be before updated at");
        
        System.out.println("✅ Analysis Timestamps: Timestamp handling working correctly");
    }

    @Test
    @DisplayName("1.3.11: Validate Analysis entity collections handling")
    void testAnalysisEntityCollectionsHandling() {
        // Test that collections are properly initialized
        assertNotNull(testAnalysis.getMetrics(), "Metrics collection should not be null");
        assertNotNull(testAnalysis.getErrors(), "Errors collection should not be null");
        
        // Test that collections start empty
        assertTrue(testAnalysis.getMetrics().isEmpty(), "Metrics collection should start empty");
        assertTrue(testAnalysis.getErrors().isEmpty(), "Errors collection should start empty");
        
        // Test that collections can be set
        Map<String, String> newMetrics = new HashMap<>();
        newMetrics.put("test_key", "test_value");
        newMetrics.put("performance", "85.5");
        
        testAnalysis.setMetrics(newMetrics);
        assertEquals(newMetrics, testAnalysis.getMetrics(), "Metrics collection should be updated");
        
        // Test that collections can be modified
        testAnalysis.addMetric("new_key", "new_value");
        assertTrue(testAnalysis.getMetrics().containsKey("new_key"), "New metric should be added");
        assertEquals("new_value", testAnalysis.getMetrics().get("new_key"), "New metric value should be correct");
        
        System.out.println("✅ Analysis Collections: Collections handling working correctly");
    }

    @Test
    @DisplayName("1.3.12: Validate Analysis entity business methods")
    void testAnalysisEntityBusinessMethods() {
        // Test startAnalysis method
        testAnalysis.startAnalysis();
        assertEquals(Analysis.AnalysisStatus.IN_PROGRESS, testAnalysis.getStatus(), "Status should be IN_PROGRESS after start");
        assertNotNull(testAnalysis.getStartTime(), "Start time should be set after start");
        
        // Test completeAnalysis method
        testAnalysis.completeAnalysis();
        assertEquals(Analysis.AnalysisStatus.COMPLETED, testAnalysis.getStatus(), "Status should be COMPLETED after completion");
        assertNotNull(testAnalysis.getEndTime(), "End time should be set after completion");
        assertNotNull(testAnalysis.getDurationSeconds(), "Duration should be calculated after completion");
        
        // Test failAnalysis method
        testAnalysis.failAnalysis("Test error message");
        assertEquals(Analysis.AnalysisStatus.FAILED, testAnalysis.getStatus(), "Status should be FAILED after failure");
        assertNotNull(testAnalysis.getEndTime(), "End time should be set after failure");
        assertTrue(testAnalysis.getErrors().contains("Test error message"), "Error message should be added");
        
        // Test addMetric method
        testAnalysis.addMetric("test_metric", "test_value");
        assertTrue(testAnalysis.getMetrics().containsKey("test_metric"), "Metric should be added");
        assertEquals("test_value", testAnalysis.getMetrics().get("test_metric"), "Metric value should be correct");
        
        // Test addError method
        testAnalysis.addError("Additional error");
        assertTrue(testAnalysis.getErrors().contains("Additional error"), "Additional error should be added");
        
        // Test setErrorMessage method
        testAnalysis.setErrorMessage("Single error message");
        assertEquals(1, testAnalysis.getErrors().size(), "Should have only one error message");
        assertTrue(testAnalysis.getErrors().contains("Single error message"), "Single error message should be set");
        
        System.out.println("✅ Analysis Business Methods: Business methods working correctly");
    }

    @Test
    @DisplayName("1.3.13: Validate Analysis entity score calculations")
    void testAnalysisEntityScoreCalculations() {
        // Test setSecurityViolations method
        testAnalysis.setSecurityViolations(0);
        assertEquals(100.0, testAnalysis.getSecurityScore(), "Security score should be 100.0 with no violations");
        
        testAnalysis.setSecurityViolations(5);
        assertEquals(50.0, testAnalysis.getSecurityScore(), "Security score should be 50.0 with 5 violations");
        
        testAnalysis.setSecurityViolations(15);
        assertEquals(0.0, testAnalysis.getSecurityScore(), "Security score should be 0.0 with 15+ violations");
        
        // Test setPerformanceIssues method
        testAnalysis.setPerformanceIssues(0);
        assertEquals(100.0, testAnalysis.getPerformanceScore(), "Performance score should be 100.0 with no issues");
        
        testAnalysis.setPerformanceIssues(8);
        assertEquals(20.0, testAnalysis.getPerformanceScore(), "Performance score should be 20.0 with 8 issues");
        
        // Test setComplianceViolations method
        testAnalysis.setComplianceViolations(0);
        assertEquals(100.0, testAnalysis.getComplianceScore(), "Compliance score should be 100.0 with no violations");
        
        testAnalysis.setComplianceViolations(3);
        assertEquals(70.0, testAnalysis.getComplianceScore(), "Compliance score should be 70.0 with 3 violations");
        
        // Test getOverallScore method
        testAnalysis.setComplianceScore(80.0);
        testAnalysis.setQualityScore(90.0);
        testAnalysis.setSecurityScore(95.0);
        testAnalysis.setPerformanceScore(85.0);
        
        Double overallScore = testAnalysis.getOverallScore();
        assertNotNull(overallScore, "Overall score should not be null");
        assertEquals(87.5, overallScore, 0.01, "Overall score should be calculated correctly");
        
        System.out.println("✅ Analysis Score Calculations: Score calculation methods working correctly");
    }
}
