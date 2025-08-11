package com.agentforge.repository;

import com.agentforge.entity.Project;
import com.agentforge.entity.ProjectStatus;
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
 * Repository interface for Project entity.
 * Provides data access methods for project management operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find project by name
     */
    Optional<Project> findByName(String name);

    /**
     * Find projects by owner
     */
    List<Project> findByOwner(User owner);

    /**
     * Find projects by owner with pagination
     */
    Page<Project> findByOwner(User owner, Pageable pageable);

    /**
     * Find projects by status
     */
    List<Project> findByStatus(ProjectStatus status);

    /**
     * Find projects by status with pagination
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    /**
     * Find projects by owner and status
     */
    List<Project> findByOwnerAndStatus(User owner, ProjectStatus status);

    /**
     * Find projects by owner and status with pagination
     */
    Page<Project> findByOwnerAndStatus(User owner, ProjectStatus status, Pageable pageable);

    /**
     * Find projects by technology stack containing the search term
     */
    List<Project> findByTechnologyStackContainingIgnoreCase(String technologyStack);

    /**
     * Find projects by partial name match
     */
    List<Project> findByNameContainingIgnoreCase(String name);

    /**
     * Find projects by partial description match
     */
    List<Project> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Find projects created after a specific date
     */
    List<Project> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find projects created between two dates
     */
    List<Project> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find projects last analyzed before a specific date
     */
    List<Project> findByLastAnalysisDateBeforeOrLastAnalysisDateIsNull(LocalDateTime date);

    /**
     * Find projects by repository URL
     */
    Optional<Project> findByRepositoryUrl(String repositoryUrl);

    /**
     * Find project by project path
     */
    Optional<Project> findByProjectPath(String projectPath);

    /**
     * Find projects with pagination and sorting
     */
    Page<Project> findAll(Pageable pageable);

    /**
     * Find projects by multiple statuses
     */
    List<Project> findByStatusIn(List<ProjectStatus> statuses);

    /**
     * Find projects by multiple statuses with pagination
     */
    Page<Project> findByStatusIn(List<ProjectStatus> statuses, Pageable pageable);

    /**
     * Find projects by owner with pagination
     */
    Page<Project> findByOwner(Pageable pageable);

    /**
     * Count projects by status
     */
    long countByStatus(ProjectStatus status);

    /**
     * Count projects by owner
     */
    long countByOwner(User owner);

    /**
     * Count projects by owner and status
     */
    long countByOwnerAndStatus(User owner, ProjectStatus status);

    /**
     * Find projects by lines of code range
     */
    List<Project> findByLinesOfCodeBetween(Long minLines, Long maxLines);

    /**
     * Find projects by lines of code greater than a threshold
     */
    List<Project> findByLinesOfCodeGreaterThan(Long threshold);

    /**
     * Find projects by lines of code less than a threshold
     */
    List<Project> findByLinesOfCodeLessThan(Long threshold);

    /**
     * Find projects by last analysis date range
     */
    List<Project> findByLastAnalysisDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find projects that haven't been analyzed recently
     */
    @Query("SELECT p FROM Project p WHERE p.lastAnalysisDate IS NULL OR p.lastAnalysisDate < :cutoffDate")
    List<Project> findProjectsNeedingAnalysis(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find projects by owner and technology stack
     */
    List<Project> findByOwnerAndTechnologyStackContainingIgnoreCase(User owner, String technologyStack);

    /**
     * Find projects by owner and partial name match
     */
    List<Project> findByOwnerAndNameContainingIgnoreCase(User owner, String name);

    /**
     * Find projects by owner and partial description match
     */
    List<Project> findByOwnerAndDescriptionContainingIgnoreCase(User owner, String description);

    /**
     * Find projects by owner and creation date range
     */
    List<Project> findByOwnerAndCreatedAtBetween(User owner, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find projects by owner and last analysis date range
     */
    List<Project> findByOwnerAndLastAnalysisDateBetween(User owner, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find projects by owner and status with pagination and sorting
     */
    Page<Project> findByOwnerAndStatusOrderByCreatedAtDesc(User owner, ProjectStatus status, Pageable pageable);

    /**
     * Find projects by owner ordered by last analysis date (most recent first)
     */
    List<Project> findByOwnerOrderByLastAnalysisDateDesc(User owner);

    /**
     * Find projects by owner ordered by creation date (most recent first)
     */
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * Find projects by owner ordered by name
     */
    List<Project> findByOwnerOrderByNameAsc(User owner);

    /**
     * Find projects by owner and status ordered by last analysis date
     */
    List<Project> findByOwnerAndStatusOrderByLastAnalysisDateDesc(User owner, ProjectStatus status);

    /**
     * Find projects by owner and status ordered by creation date
     */
    Page<Project> findByOwnerAndStatusOrderByCreatedAtDesc(User owner, ProjectStatus status);

    /**
     * Check if project exists by name
     */
    boolean existsByName(String name);
}
