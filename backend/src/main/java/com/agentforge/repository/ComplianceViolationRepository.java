package com.agentforge.repository;

import com.agentforge.entity.ComplianceViolation;
import com.agentforge.entity.Project;
import com.agentforge.entity.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ComplianceViolation entity.
 * Provides data access methods for compliance violation operations.
 */
@Repository
public interface ComplianceViolationRepository extends JpaRepository<ComplianceViolation, Long> {

    /**
     * Find violations by project
     */
    List<ComplianceViolation> findByProject(Project project);

    /**
     * Find violations by project with pagination
     */
    Page<ComplianceViolation> findByProject(Project project, Pageable pageable);

    /**
     * Find violations by analysis
     */
    List<ComplianceViolation> findByAnalysis(Analysis analysis);

    /**
     * Find violations by analysis with pagination
     */
    Page<ComplianceViolation> findByAnalysis(Analysis analysis, Pageable pageable);

    /**
     * Find violations by project and analysis
     */
    List<ComplianceViolation> findByProjectAndAnalysis(Project project, Analysis analysis);

    /**
     * Find violations by severity
     */
    List<ComplianceViolation> findBySeverity(ComplianceViolation.ViolationSeverity severity);

    /**
     * Find violations by severity with pagination
     */
    Page<ComplianceViolation> findBySeverity(ComplianceViolation.ViolationSeverity severity, Pageable pageable);

    /**
     * Find violations by status
     */
    List<ComplianceViolation> findByStatus(ComplianceViolation.ViolationStatus status);

    /**
     * Find violations by status with pagination
     */
    Page<ComplianceViolation> findByStatus(ComplianceViolation.ViolationStatus status, Pageable pageable);

    /**
     * Find violations by rule ID
     */
    List<ComplianceViolation> findByRuleId(String ruleId);

    /**
     * Find violations by rule category
     */
    List<ComplianceViolation> findByRuleCategory(String ruleCategory);

    /**
     * Find violations by rule category with pagination
     */
    Page<ComplianceViolation> findByRuleCategory(String ruleCategory, Pageable pageable);

    /**
     * Find violations by project and severity
     */
    List<ComplianceViolation> findByProjectAndSeverity(Project project, ComplianceViolation.ViolationSeverity severity);

    /**
     * Find violations by project and status
     */
    List<ComplianceViolation> findByProjectAndStatus(Project project, ComplianceViolation.ViolationStatus status);

    /**
     * Find violations by project and rule category
     */
    List<ComplianceViolation> findByProjectAndRuleCategory(Project project, String ruleCategory);

    /**
     * Find violations by analysis and severity
     */
    List<ComplianceViolation> findByAnalysisAndSeverity(Analysis analysis, ComplianceViolation.ViolationSeverity severity);

    /**
     * Find violations by analysis and status
     */
    List<ComplianceViolation> findByAnalysisAndStatus(Analysis analysis, ComplianceViolation.ViolationStatus status);

    /**
     * Find violations by analysis and rule category
     */
    List<ComplianceViolation> findByAnalysisAndRuleCategory(Analysis analysis, String ruleCategory);

    /**
     * Find violations by file path
     */
    List<ComplianceViolation> findByFilePath(String filePath);

    /**
     * Find violations by file path containing the search term
     */
    List<ComplianceViolation> findByFilePathContainingIgnoreCase(String filePath);

    /**
     * Find violations by line number
     */
    List<ComplianceViolation> findByLineNumber(Integer lineNumber);

    /**
     * Find violations by line number range
     */
    List<ComplianceViolation> findByLineNumberBetween(Integer minLine, Integer maxLine);

    /**
     * Find violations by line number greater than threshold
     */
    List<ComplianceViolation> findByLineNumberGreaterThan(Integer threshold);

    /**
     * Find violations by line number less than threshold
     */
    List<ComplianceViolation> findByLineNumberLessThan(Integer threshold);

    /**
     * Find violations created after a specific date
     */
    List<ComplianceViolation> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find violations created between two dates
     */
    List<ComplianceViolation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find violations resolved after a specific date
     */
    List<ComplianceViolation> findByResolvedAtAfter(LocalDateTime date);

    /**
     * Find violations resolved between two dates
     */
    List<ComplianceViolation> findByResolvedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find violations by false positive flag
     */
    List<ComplianceViolation> findByFalsePositive(Boolean falsePositive);

    /**
     * Find violations by false positive flag with pagination
     */
    Page<ComplianceViolation> findByFalsePositive(Boolean falsePositive, Pageable pageable);

    /**
     * Find violations by project and false positive flag
     */
    List<ComplianceViolation> findByProjectAndFalsePositive(Project project, Boolean falsePositive);

    /**
     * Find violations by analysis and false positive flag
     */
    List<ComplianceViolation> findByAnalysisAndFalsePositive(Analysis analysis, Boolean falsePositive);

    /**
     * Find violations by suppressed until date
     */
    List<ComplianceViolation> findBySuppressedUntilBefore(LocalDateTime date);

    /**
     * Find violations by suppressed until date range
     */
    List<ComplianceViolation> findBySuppressedUntilBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find violations by project and severity with pagination
     */
    Page<ComplianceViolation> findByProjectAndSeverity(Project project, ComplianceViolation.ViolationSeverity severity, Pageable pageable);

    /**
     * Find violations by project and status with pagination
     */
    Page<ComplianceViolation> findByProjectAndStatus(Project project, ComplianceViolation.ViolationStatus status, Pageable pageable);

    /**
     * Find violations by project and rule category with pagination
     */
    Page<ComplianceViolation> findByProjectAndRuleCategory(Project project, String ruleCategory, Pageable pageable);

    /**
     * Find violations by analysis and severity with pagination
     */
    Page<ComplianceViolation> findByAnalysisAndSeverity(Analysis analysis, ComplianceViolation.ViolationSeverity severity, Pageable pageable);

    /**
     * Find violations by analysis and status with pagination
     */
    Page<ComplianceViolation> findByAnalysisAndStatus(Analysis analysis, ComplianceViolation.ViolationStatus status, Pageable pageable);

    /**
     * Find violations by analysis and rule category with pagination
     */
    Page<ComplianceViolation> findByAnalysisAndRuleCategory(Analysis analysis, String ruleCategory, Pageable pageable);

    /**
     * Find violations by project ordered by severity priority (most critical first)
     */
    List<ComplianceViolation> findByProjectOrderBySeverityAsc(Project project);

    /**
     * Find violations by project ordered by creation date (most recent first)
     */
    List<ComplianceViolation> findByProjectOrderByCreatedAtDesc(Project project);

    /**
     * Find violations by project ordered by line number
     */
    List<ComplianceViolation> findByProjectOrderByLineNumberAsc(Project project);

    /**
     * Find violations by analysis ordered by severity priority (most critical first)
     */
    List<ComplianceViolation> findByAnalysisOrderBySeverityAsc(Analysis analysis);

    /**
     * Find violations by analysis ordered by creation date (most recent first)
     */
    List<ComplianceViolation> findByAnalysisOrderByCreatedAtDesc(Analysis analysis);

    /**
     * Find violations by analysis ordered by line number
     */
    List<ComplianceViolation> findByAnalysisOrderByLineNumberAsc(Analysis analysis);

    /**
     * Find violations by project and severity ordered by line number
     */
    List<ComplianceViolation> findByProjectAndSeverityOrderByLineNumberAsc(Project project, ComplianceViolation.ViolationSeverity severity);

    /**
     * Find violations by project and status ordered by creation date (most recent first)
     */
    List<ComplianceViolation> findByProjectAndStatusOrderByCreatedAtDesc(Project project, ComplianceViolation.ViolationStatus status);

    /**
     * Find violations by analysis and severity ordered by line number
     */
    List<ComplianceViolation> findByAnalysisAndSeverityOrderByLineNumberAsc(Analysis analysis, ComplianceViolation.ViolationSeverity severity);

    /**
     * Find violations by analysis and status ordered by creation date (most recent first)
     */
    List<ComplianceViolation> findByAnalysisAndStatusOrderByCreatedAtDesc(Analysis analysis, ComplianceViolation.ViolationStatus status);

    /**
     * Count violations by project
     */
    long countByProject(Project project);

    /**
     * Count violations by analysis
     */
    long countByAnalysis(Analysis analysis);

    /**
     * Count violations by project and severity
     */
    long countByProjectAndSeverity(Project project, ComplianceViolation.ViolationSeverity severity);

    /**
     * Count violations by project and status
     */
    long countByProjectAndStatus(Project project, ComplianceViolation.ViolationStatus status);

    /**
     * Count violations by analysis and severity
     */
    long countByAnalysisAndSeverity(Analysis analysis, ComplianceViolation.ViolationSeverity severity);

    /**
     * Count violations by analysis and status
     */
    long countByAnalysisAndStatus(Analysis analysis, ComplianceViolation.ViolationStatus status);

    /**
     * Count violations by severity
     */
    long countBySeverity(ComplianceViolation.ViolationSeverity severity);

    /**
     * Count violations by status
     */
    long countByStatus(ComplianceViolation.ViolationStatus status);

    /**
     * Count violations by rule category
     */
    long countByRuleCategory(String ruleCategory);

    /**
     * Count violations by false positive flag
     */
    long countByFalsePositive(Boolean falsePositive);

    /**
     * Find violations with pagination and sorting
     */
    Page<ComplianceViolation> findAll(Pageable pageable);

    /**
     * Find violations by multiple severities
     */
    List<ComplianceViolation> findBySeverityIn(List<ComplianceViolation.ViolationSeverity> severities);

    /**
     * Find violations by multiple statuses
     */
    List<ComplianceViolation> findByStatusIn(List<ComplianceViolation.ViolationStatus> statuses);

    /**
     * Find violations by multiple rule categories
     */
    List<ComplianceViolation> findByRuleCategoryIn(List<String> ruleCategories);

    /**
     * Find violations by multiple severities with pagination
     */
    Page<ComplianceViolation> findBySeverityIn(List<ComplianceViolation.ViolationSeverity> severities, Pageable pageable);

    /**
     * Find violations by multiple statuses with pagination
     */
    Page<ComplianceViolation> findByStatusIn(List<ComplianceViolation.ViolationStatus> statuses, Pageable pageable);

    /**
     * Find violations by multiple rule categories with pagination
     */
    Page<ComplianceViolation> findByRuleCategoryIn(List<String> ruleCategories, Pageable pageable);

    /**
     * Find active violations (open or suppressed but not expired)
     */
    @Query("SELECT v FROM ComplianceViolation v WHERE v.status = 'OPEN' OR " +
           "(v.status = 'SUPPRESSED' AND (v.suppressedUntil IS NULL OR v.suppressedUntil > :currentDate))")
    List<ComplianceViolation> findActiveViolations(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find active violations by project
     */
    @Query("SELECT v FROM ComplianceViolation v WHERE v.project = :project AND " +
           "(v.status = 'OPEN' OR (v.status = 'SUPPRESSED' AND (v.suppressedUntil IS NULL OR v.suppressedUntil > :currentDate)))")
    List<ComplianceViolation> findActiveViolationsByProject(@Param("project") Project project, @Param("currentDate") LocalDateTime currentDate);

    /**
     * Find active violations by analysis
     */
    @Query("SELECT v FROM ComplianceViolation v WHERE v.analysis = :analysis AND " +
           "(v.status = 'OPEN' OR (v.status = 'SUPPRESSED' AND (v.suppressedUntil IS NULL OR v.suppressedUntil > :currentDate)))")
    List<ComplianceViolation> findActiveViolationsByAnalysis(@Param("analysis") Analysis analysis, @Param("currentDate") LocalDateTime currentDate);

    /**
     * Find violations by project with pagination and sorting
     */
    Page<ComplianceViolation> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);

    /**
     * Find violations by analysis with pagination and sorting
     */
    Page<ComplianceViolation> findByAnalysisOrderByCreatedAtDesc(Analysis analysis, Pageable pageable);

    /**
     * Find violations by project and severity with pagination and sorting
     */
    Page<ComplianceViolation> findByProjectAndSeverityOrderByCreatedAtDesc(Project project, ComplianceViolation.ViolationSeverity severity, Pageable pageable);

    /**
     * Find violations by project and status with pagination and sorting
     */
    Page<ComplianceViolation> findByProjectAndStatusOrderByCreatedAtDesc(Project project, ComplianceViolation.ViolationStatus status, Pageable pageable);

    /**
     * Find violations by analysis and severity with pagination and sorting
     */
    Page<ComplianceViolation> findByAnalysisAndSeverityOrderByCreatedAtDesc(Analysis analysis, ComplianceViolation.ViolationSeverity severity, Pageable pageable);

    /**
     * Find violations by analysis and status with pagination and sorting
     */
    Page<ComplianceViolation> findByAnalysisAndStatusOrderByCreatedAtDesc(Analysis analysis, ComplianceViolation.ViolationStatus status, Pageable pageable);
}
