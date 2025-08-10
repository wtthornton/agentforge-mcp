package com.agentforge.repository;

import com.agentforge.entity.Analysis;
import com.agentforge.entity.Project;
import com.agentforge.entity.User;
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
 * Repository interface for Analysis entity.
 * Provides data access methods for analysis operations.
 */
@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    /**
     * Find analyses by project
     */
    List<Analysis> findByProject(Project project);

    /**
     * Find analyses by project with pagination
     */
    Page<Analysis> findByProject(Project project, Pageable pageable);

    /**
     * Find analyses by user
     */
    List<Analysis> findByUser(User user);

    /**
     * Find analyses by user with pagination
     */
    Page<Analysis> findByUser(User user, Pageable pageable);

    /**
     * Find analyses by project and user
     */
    List<Analysis> findByProjectAndUser(Project project, User user);

    /**
     * Find analyses by status
     */
    List<Analysis> findByStatus(Analysis.AnalysisStatus status);

    /**
     * Find analyses by status with pagination
     */
    Page<Analysis> findByStatus(Analysis.AnalysisStatus status, Pageable pageable);

    /**
     * Find analyses by type
     */
    List<Analysis> findByType(Analysis.AnalysisType type);

    /**
     * Find analyses by type with pagination
     */
    Page<Analysis> findByType(Analysis.AnalysisType type, Pageable pageable);

    /**
     * Find analyses by project and status
     */
    List<Analysis> findByProjectAndStatus(Project project, Analysis.AnalysisStatus status);

    /**
     * Find analyses by project and type
     */
    List<Analysis> findByProjectAndType(Project project, Analysis.AnalysisType type);

    /**
     * Find analyses by user and status
     */
    List<Analysis> findByUserAndStatus(User user, Analysis.AnalysisStatus status);

    /**
     * Find analyses by user and type
     */
    List<Analysis> findByUserAndType(User user, Analysis.AnalysisType type);

    /**
     * Find analyses by project, user, and status
     */
    List<Analysis> findByProjectAndUserAndStatus(Project project, User user, Analysis.AnalysisStatus status);

    /**
     * Find analyses by project, user, and type
     */
    List<Analysis> findByProjectAndUserAndType(Project project, User user, Analysis.AnalysisType type);

    /**
     * Find analyses created after a specific date
     */
    List<Analysis> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find analyses created between two dates
     */
    List<Analysis> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find analyses started after a specific date
     */
    List<Analysis> findByStartTimeAfter(LocalDateTime date);

    /**
     * Find analyses completed after a specific date
     */
    List<Analysis> findByEndTimeAfter(LocalDateTime date);

    /**
     * Find analyses by duration range (in seconds)
     */
    List<Analysis> findByDurationSecondsBetween(Long minDuration, Long maxDuration);

    /**
     * Find analyses by duration greater than threshold (in seconds)
     */
    List<Analysis> findByDurationSecondsGreaterThan(Long threshold);

    /**
     * Find analyses by duration less than threshold (in seconds)
     */
    List<Analysis> findByDurationSecondsLessThan(Long threshold);

    /**
     * Find analyses by compliance score range
     */
    List<Analysis> findByComplianceScoreBetween(Double minScore, Double maxScore);

    /**
     * Find analyses by quality score range
     */
    List<Analysis> findByQualityScoreBetween(Double minScore, Double maxScore);

    /**
     * Find analyses by security score range
     */
    List<Analysis> findBySecurityScoreBetween(Double minScore, Double maxScore);

    /**
     * Find analyses by performance score range
     */
    List<Analysis> findByPerformanceScoreBetween(Double minScore, Double maxScore);

    /**
     * Find analyses by total violations range
     */
    List<Analysis> findByTotalViolationsBetween(Integer minViolations, Integer maxViolations);

    /**
     * Find analyses by critical violations range
     */
    List<Analysis> findByCriticalViolationsBetween(Integer minViolations, Integer maxViolations);

    /**
     * Find analyses by lines analyzed range
     */
    List<Analysis> findByLinesAnalyzedBetween(Long minLines, Long maxLines);

    /**
     * Find analyses by files analyzed range
     */
    List<Analysis> findByFilesAnalyzedBetween(Long minFiles, Long maxFiles);

    /**
     * Find the latest analysis for a project
     */
    Optional<Analysis> findFirstByProjectOrderByCreatedAtDesc(Project project);

    /**
     * Find the latest analysis for a project by type
     */
    Optional<Analysis> findFirstByProjectAndTypeOrderByCreatedAtDesc(Project project, Analysis.AnalysisType type);

    /**
     * Find the latest analysis for a project by status
     */
    Optional<Analysis> findFirstByProjectAndStatusOrderByCreatedAtDesc(Project project, Analysis.AnalysisStatus status);

    /**
     * Find analyses by project ordered by creation date (most recent first)
     */
    List<Analysis> findByProjectOrderByCreatedAtDesc(Project project);

    /**
     * Find analyses by project and type ordered by creation date (most recent first)
     */
    List<Analysis> findByProjectAndTypeOrderByCreatedAtDesc(Project project, Analysis.AnalysisType type);

    /**
     * Find analyses by project and status ordered by creation date (most recent first)
     */
    List<Analysis> findByProjectAndStatusOrderByCreatedAtDesc(Project project, Analysis.AnalysisStatus status);

    /**
     * Find analyses by user ordered by creation date (most recent first)
     */
    List<Analysis> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find analyses by user and type ordered by creation date (most recent first)
     */
    List<Analysis> findByUserAndTypeOrderByCreatedAtDesc(User user, Analysis.AnalysisType type);

    /**
     * Find analyses by user and status ordered by creation date (most recent first)
     */
    List<Analysis> findByUserAndStatusOrderByCreatedAtDesc(User user, Analysis.AnalysisStatus status);

    /**
     * Count analyses by project
     */
    long countByProject(Project project);

    /**
     * Count analyses by user
     */
    long countByUser(User user);

    /**
     * Count analyses by project and status
     */
    long countByProjectAndStatus(Project project, Analysis.AnalysisStatus status);

    /**
     * Count analyses by project and type
     */
    long countByProjectAndType(Project project, Analysis.AnalysisType type);

    /**
     * Count analyses by user and status
     */
    long countByUserAndStatus(User user, Analysis.AnalysisStatus status);

    /**
     * Count analyses by user and type
     */
    long countByUserAndType(User user, Analysis.AnalysisType type);

    /**
     * Find analyses with pagination and sorting
     */
    Page<Analysis> findAll(Pageable pageable);

    /**
     * Find analyses by multiple statuses
     */
    List<Analysis> findByStatusIn(List<Analysis.AnalysisStatus> statuses);

    /**
     * Find analyses by multiple types
     */
    List<Analysis> findByTypeIn(List<Analysis.AnalysisType> types);

    /**
     * Find analyses by multiple statuses with pagination
     */
    Page<Analysis> findByStatusIn(List<Analysis.AnalysisStatus> statuses, Pageable pageable);

    /**
     * Find analyses by multiple types with pagination
     */
    Page<Analysis> findByTypeIn(List<Analysis.AnalysisType> types, Pageable pageable);

    /**
     * Find analyses that are currently in progress
     */
    List<Analysis> findByStatus(Analysis.AnalysisStatus.IN_PROGRESS);

    /**
     * Find analyses that have failed
     */
    List<Analysis> findByStatus(Analysis.AnalysisStatus.FAILED);

    /**
     * Find analyses that are pending
     */
    List<Analysis> findByStatus(Analysis.AnalysisStatus.PENDING);

    /**
     * Find analyses that have completed successfully
     */
    List<Analysis> findByStatus(Analysis.AnalysisStatus.COMPLETED);

    /**
     * Find analyses by project with pagination and sorting
     */
    Page<Analysis> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);

    /**
     * Find analyses by user with pagination and sorting
     */
    Page<Analysis> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find analyses by project and status with pagination and sorting
     */
    Page<Analysis> findByProjectAndStatusOrderByCreatedAtDesc(Project project, Analysis.AnalysisStatus status, Pageable pageable);

    /**
     * Find analyses by project and type with pagination and sorting
     */
    Page<Analysis> findByProjectAndTypeOrderByCreatedAtDesc(Project project, Analysis.AnalysisType type, Pageable pageable);

    /**
     * Find analyses by user and status with pagination and sorting
     */
    Page<Analysis> findByUserAndStatusOrderByCreatedAtDesc(User user, Analysis.AnalysisStatus status, Pageable pageable);

    /**
     * Find analyses by user and type with pagination and sorting
     */
    Page<Analysis> findByUserAndTypeOrderByCreatedAtDesc(User user, Analysis.AnalysisType type, Pageable pageable);
}
