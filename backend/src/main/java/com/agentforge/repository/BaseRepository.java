package com.agentforge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Base Repository Interface for AgentForge
 * 
 * This interface provides common functionality for all repositories:
 * - Basic CRUD operations
 * - Pagination and sorting support
 * - Specification-based querying
 * - Common query patterns
 * - Performance optimization methods
 * 
 * @author AgentForge Team
 * @version 1.0.0
 * @param <T> Entity type
 * @param <ID> Entity ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find all entities with custom sorting
     */
    List<T> findAll(Sort sort);

    /**
     * Find all entities with pagination and sorting
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Find entity by ID with optional eager loading
     */
    Optional<T> findById(ID id);

    /**
     * Check if entity exists by ID
     */
    boolean existsById(ID id);

    /**
     * Count all entities
     */
    long count();

    /**
     * Delete entity by ID
     */
    void deleteById(ID id);

    /**
     * Delete multiple entities by IDs
     */
    void deleteAllById(Iterable<? extends ID> ids);

    /**
     * Save entity and return the saved instance
     */
    <S extends T> S save(S entity);

    /**
     * Save multiple entities and return saved instances
     */
    <S extends T> List<S> saveAll(Iterable<S> entities);

    /**
     * Save entity and flush immediately
     */
    <S extends T> S saveAndFlush(S entity);

    /**
     * Flush all pending changes
     */
    void flush();

    /**
     * Refresh entity from database
     */
    void refresh(T entity);

    /**
     * Clear persistence context
     */
    void clear();

    /**
     * Detach entity from persistence context
     */
    void detach(T entity);

    /**
     * Merge entity with persistence context
     */
    <S extends T> S merge(S entity);

    /**
     * Find entities by multiple IDs
     */
    List<T> findAllById(Iterable<ID> ids);

    /**
     * Delete all entities in batch
     */
    void deleteAllInBatch(Iterable<T> entities);

    /**
     * Delete all entities in batch
     */
    void deleteAllInBatch();

    /**
     * Get reference to entity by ID (proxy)
     */
    T getReferenceById(ID id);

    /**
     * Get one entity by ID (throws exception if not found)
     */
    T getById(ID id);

    /**
     * Get one entity by ID (returns null if not found)
     */
    T getOne(ID id);

    /**
     * Find entity by ID or throw exception
     */
    T findByIdOrThrow(ID id);

    /**
     * Find entity by ID or return default value
     */
    T findByIdOrDefault(ID id, T defaultValue);

    /**
     * Find first entity or return empty
     */
    Optional<T> findFirst();

    /**
     * Find first entity with sorting
     */
    Optional<T> findFirst(Sort sort);

    /**
     * Find last entity with sorting
     */
    Optional<T> findLast(Sort sort);

    /**
     * Check if any entities exist
     */
    boolean exists();

    /**
     * Find entities with limit
     */
    List<T> findTop(int limit);

    /**
     * Find entities with limit and sorting
     */
    List<T> findTop(int limit, Sort sort);

    /**
     * Find entities with limit and sorting by specific field
     */
    List<T> findTopByOrderBy(int limit, String fieldName);

    /**
     * Find entities with limit and sorting by specific field descending
     */
    List<T> findTopByOrderByDesc(int limit, String fieldName);

    /**
     * Find entities created after specific date
     */
    List<T> findByCreatedAtAfter(java.time.LocalDateTime date);

    /**
     * Find entities created before specific date
     */
    List<T> findByCreatedAtBefore(java.time.LocalDateTime date);

    /**
     * Find entities created between two dates
     */
    List<T> findByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Find entities updated after specific date
     */
    List<T> findByUpdatedAtAfter(java.time.LocalDateTime date);

    /**
     * Find entities updated before specific date
     */
    List<T> findByUpdatedAtBefore(java.time.LocalDateTime date);

    /**
     * Find entities updated between two dates
     */
    List<T> findByUpdatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Find entities by active status
     */
    List<T> findByActiveTrue();

    /**
     * Find entities by inactive status
     */
    List<T> findByActiveFalse();

    /**
     * Find entities by status
     */
    List<T> findByStatus(String status);

    /**
     * Find entities by multiple statuses
     */
    List<T> findByStatusIn(List<String> statuses);

    /**
     * Count entities by status
     */
    long countByStatus(String status);

    /**
     * Count entities by active status
     */
    long countByActiveTrue();

    /**
     * Count entities by inactive status
     */
    long countByActiveFalse();

    /**
     * Delete entities by status
     */
    void deleteByStatus(String status);

    /**
     * Delete entities by active status
     */
    void deleteByActiveFalse();

    /**
     * Soft delete entity by ID
     */
    void softDeleteById(ID id);

    /**
     * Restore soft deleted entity by ID
     */
    void restoreById(ID id);

    /**
     * Find soft deleted entities
     */
    List<T> findSoftDeleted();

    /**
     * Find entities by partial name match
     */
    List<T> findByNameContainingIgnoreCase(String name);

    /**
     * Find entities by partial description match
     */
    List<T> findByDescriptionContainingIgnoreCase(String description);

    /**
     * Find entities by partial name or description match
     */
    List<T> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    /**
     * Find entities with pagination and sorting by creation date
     */
    Page<T> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find entities with pagination and sorting by update date
     */
    Page<T> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * Find entities with pagination and sorting by name
     */
    Page<T> findAllByOrderByNameAsc(Pageable pageable);

    /**
     * Find entities by status with pagination and sorting
     */
    Page<T> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /**
     * Find entities by active status with pagination and sorting
     */
    Page<T> findByActiveOrderByCreatedAtDesc(boolean active, Pageable pageable);
}
