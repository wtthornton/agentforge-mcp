package com.agentforge.repository;

import com.agentforge.entity.Embedding;
import com.agentforge.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Embedding entity with vector operations
 * 
 * This repository provides specialized methods for:
 * - Vector similarity search using pgvector
 * - Embedding management and storage
 * - Vector-based analytics and clustering
 * - Performance-optimized vector queries
 * - Multi-dimensional vector operations
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Repository
public interface EmbeddingRepository extends VectorRepository<Embedding, Long> {

    /**
     * Find embeddings by project
     */
    List<Embedding> findByProject(Project project);

    /**
     * Find embeddings by project with pagination
     */
    Page<Embedding> findByProject(Project project, Pageable pageable);

    /**
     * Find embeddings by project and type
     */
    List<Embedding> findByProjectAndType(Project project, String type);

    /**
     * Find embeddings by project and type with pagination
     */
    Page<Embedding> findByProjectAndType(Project project, String type, Pageable pageable);

    /**
     * Find embeddings by type
     */
    List<Embedding> findByType(String type);

    /**
     * Find embeddings by type with pagination
     */
    Page<Embedding> findByType(String type, Pageable pageable);

    /**
     * Find embeddings by model
     */
    List<Embedding> findByModel(String model);

    /**
     * Find embeddings by model with pagination
     */
    Page<Embedding> findByModel(String model, Pageable pageable);

    /**
     * Find embeddings by project and model
     */
    List<Embedding> findByProjectAndModel(Project project, String model);

    /**
     * Find embeddings by project and model with pagination
     */
    Page<Embedding> findByProjectAndModel(Project project, String model, Pageable pageable);

    /**
     * Find embeddings by dimension
     */
    List<Embedding> findByDimension(Integer dimension);

    /**
     * Find embeddings by dimension range
     */
    List<Embedding> findByDimensionBetween(Integer minDimension, Integer maxDimension);

    /**
     * Find embeddings by project and dimension
     */
    List<Embedding> findByProjectAndDimension(Project project, Integer dimension);

    /**
     * Find embeddings by project and dimension range
     */
    List<Embedding> findByProjectAndDimensionBetween(Project project, Integer minDimension, Integer maxDimension);

    /**
     * Find embeddings created after a specific date
     */
    List<Embedding> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find embeddings created before a specific date
     */
    List<Embedding> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Find embeddings created between two dates
     */
    List<Embedding> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find embeddings by project and creation date range
     */
    List<Embedding> findByProjectAndCreatedAtBetween(Project project, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find embeddings by project and type and creation date range
     */
    List<Embedding> findByProjectAndTypeAndCreatedAtBetween(Project project, String type, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find embeddings by similarity score range
     */
    List<Embedding> findBySimilarityScoreBetween(Double minScore, Double maxScore);

    /**
     * Find embeddings by similarity score greater than threshold
     */
    List<Embedding> findBySimilarityScoreGreaterThan(Double threshold);

    /**
     * Find embeddings by similarity score less than threshold
     */
    List<Embedding> findBySimilarityScoreLessThan(Double threshold);

    /**
     * Find embeddings by project and similarity score range
     */
    List<Embedding> findByProjectAndSimilarityScoreBetween(Project project, Double minScore, Double maxScore);

    /**
     * Find embeddings by project and similarity score greater than threshold
     */
    List<Embedding> findByProjectAndSimilarityScoreGreaterThan(Project project, Double threshold);

    /**
     * Find embeddings by project and similarity score less than threshold
     */
    List<Embedding> findByProjectAndSimilarityScoreLessThan(Project project, Double threshold);

    /**
     * Find embeddings by content hash
     */
    Optional<Embedding> findByContentHash(String contentHash);

    /**
     * Find embeddings by project and content hash
     */
    Optional<Embedding> findByProjectAndContentHash(Project project, String contentHash);

    /**
     * Find embeddings by content type
     */
    List<Embedding> findByContentType(String contentType);

    /**
     * Find embeddings by project and content type
     */
    List<Embedding> findByProjectAndContentType(Project project, String contentType);

    /**
     * Find embeddings by content type with pagination
     */
    Page<Embedding> findByContentType(String contentType, Pageable pageable);

    /**
     * Find embeddings by project and content type with pagination
     */
    Page<Embedding> findByProjectAndContentType(Project project, String contentType, Pageable pageable);

    /**
     * Find embeddings by content length range
     */
    List<Embedding> findByContentLengthBetween(Long minLength, Long maxLength);

    /**
     * Find embeddings by project and content length range
     */
    List<Embedding> findByProjectAndContentLengthBetween(Project project, Long minLength, Long maxLength);

    /**
     * Find embeddings by content length greater than threshold
     */
    List<Embedding> findByContentLengthGreaterThan(Long threshold);

    /**
     * Find embeddings by project and content length greater than threshold
     */
    List<Embedding> findByProjectAndContentLengthGreaterThan(Project project, Long threshold);

    /**
     * Find embeddings by content length less than threshold
     */
    List<Embedding> findByContentLengthLessThan(Long threshold);

    /**
     * Find embeddings by project and content length less than threshold
     */
    List<Embedding> findByProjectAndContentLengthLessThan(Project project, Long threshold);

    /**
     * Find embeddings by metadata key
     */
    @Query("SELECT e FROM Embedding e WHERE e.metadata LIKE %:key%")
    List<Embedding> findByMetadataContaining(@Param("key") String key);

    /**
     * Find embeddings by project and metadata key
     */
    @Query("SELECT e FROM Embedding e WHERE e.project = :project AND e.metadata LIKE %:key%")
    List<Embedding> findByProjectAndMetadataContaining(@Param("project") Project project, @Param("key") String key);

    /**
     * Find embeddings by metadata key and value
     */
    @Query("SELECT e FROM Embedding e WHERE e.metadata LIKE %:key% AND e.metadata LIKE %:value%")
    List<Embedding> findByMetadataKeyAndValue(@Param("key") String key, @Param("value") String value);

    /**
     * Find embeddings by project and metadata key and value
     */
    @Query("SELECT e FROM Embedding e WHERE e.project = :project AND e.metadata LIKE %:key% AND e.metadata LIKE %:value%")
    List<Embedding> findByProjectAndMetadataKeyAndValue(@Param("project") Project project, @Param("key") String key, @Param("value") String value);

    /**
     * Find embeddings by project ordered by creation date (most recent first)
     */
    List<Embedding> findByProjectOrderByCreatedAtDesc(Project project);

    /**
     * Find embeddings by project and type ordered by creation date (most recent first)
     */
    List<Embedding> findByProjectAndTypeOrderByCreatedAtDesc(Project project, String type);

    /**
     * Find embeddings by project ordered by similarity score (highest first)
     */
    List<Embedding> findByProjectOrderBySimilarityScoreDesc(Project project);

    /**
     * Find embeddings by project and type ordered by similarity score (highest first)
     */
    List<Embedding> findByProjectAndTypeOrderBySimilarityScoreDesc(Project project, String type);

    /**
     * Find embeddings by project ordered by content length (largest first)
     */
    List<Embedding> findByProjectOrderByContentLengthDesc(Project project);

    /**
     * Find embeddings by project and type ordered by content length (largest first)
     */
    List<Embedding> findByProjectAndTypeOrderByContentLengthDesc(Project project, String type);

    /**
     * Find embeddings by project with pagination and sorting by creation date
     */
    Page<Embedding> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);

    /**
     * Find embeddings by project and type with pagination and sorting by creation date
     */
    Page<Embedding> findByProjectAndTypeOrderByCreatedAtDesc(Project project, String type, Pageable pageable);

    /**
     * Find embeddings by project with pagination and sorting by similarity score
     */
    Page<Embedding> findByProjectOrderBySimilarityScoreDesc(Project project, Pageable pageable);

    /**
     * Find embeddings by project and type with pagination and sorting by similarity score
     */
    Page<Embedding> findByProjectAndTypeOrderBySimilarityScoreDesc(Project project, String type, Pageable pageable);

    /**
     * Find embeddings by project with pagination and sorting by content length
     */
    Page<Embedding> findByProjectOrderByContentLengthDesc(Project project, Pageable pageable);

    /**
     * Find embeddings by project and type with pagination and sorting by content length
     */
    Page<Embedding> findByProjectAndTypeOrderByContentLengthDesc(Project project, String type, Pageable pageable);

    /**
     * Count embeddings by project
     */
    long countByProject(Project project);

    /**
     * Count embeddings by project and type
     */
    long countByProjectAndType(Project project, String type);

    /**
     * Count embeddings by type
     */
    long countByType(String type);

    /**
     * Count embeddings by model
     */
    long countByModel(String model);

    /**
     * Count embeddings by project and model
     */
    long countByProjectAndModel(Project project, String model);

    /**
     * Count embeddings by dimension
     */
    long countByDimension(Integer dimension);

    /**
     * Count embeddings by project and dimension
     */
    long countByProjectAndDimension(Project project, Integer dimension);

    /**
     * Count embeddings by content type
     */
    long countByContentType(String contentType);

    /**
     * Count embeddings by project and content type
     */
    long countByProjectAndContentType(Project project, String contentType);

    /**
     * Count embeddings by similarity score range
     */
    long countBySimilarityScoreBetween(Double minScore, Double maxScore);

    /**
     * Count embeddings by project and similarity score range
     */
    long countByProjectAndSimilarityScoreBetween(Project project, Double minScore, Double maxScore);

    /**
     * Count embeddings by content length range
     */
    long countByContentLengthBetween(Long minLength, Long maxLength);

    /**
     * Count embeddings by project and content length range
     */
    long countByProjectAndContentLengthBetween(Project project, Long minLength, Long maxLength);

    /**
     * Find embeddings that need to be updated (based on model version or content changes)
     */
    @Query("SELECT e FROM Embedding e WHERE e.modelVersion != :currentModelVersion OR e.lastUpdated < :cutoffDate")
    List<Embedding> findEmbeddingsNeedingUpdate(@Param("currentModelVersion") String currentModelVersion, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find embeddings by project that need to be updated
     */
    @Query("SELECT e FROM Embedding e WHERE e.project = :project AND (e.modelVersion != :currentModelVersion OR e.lastUpdated < :cutoffDate)")
    List<Embedding> findProjectEmbeddingsNeedingUpdate(@Param("project") Project project, @Param("currentModelVersion") String currentModelVersion, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find embeddings by project and type that need to be updated
     */
    @Query("SELECT e FROM Embedding e WHERE e.project = :project AND e.type = :type AND (e.modelVersion != :currentModelVersion OR e.lastUpdated < :cutoffDate)")
    List<Embedding> findProjectEmbeddingsNeedingUpdateByType(@Param("project") Project project, @Param("type") String type, @Param("currentModelVersion") String currentModelVersion, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find embeddings by project and type ordered by similarity score with limit
     */
    List<Embedding> findTop10ByProjectAndTypeOrderBySimilarityScoreDesc(Project project, String type);

    /**
     * Find embeddings by project and type ordered by content length with limit
     */
    List<Embedding> findTop10ByProjectAndTypeOrderByContentLengthDesc(Project project, String type);

    /**
     * Find embeddings by project and type ordered by creation date with limit
     */
    List<Embedding> findTop10ByProjectAndTypeOrderByCreatedAtDesc(Project project, String type);

    /**
     * Find embeddings by project and type ordered by similarity score with custom limit
     */
    List<Embedding> findTopByProjectAndTypeOrderBySimilarityScoreDesc(Project project, String type, Pageable pageable);

    /**
     * Find embeddings by project and type ordered by content length with custom limit
     */
    List<Embedding> findTopByProjectAndTypeOrderByContentLengthDesc(Project project, String type, Pageable pageable);

    /**
     * Find embeddings by project and type ordered by creation date with custom limit
     */
    List<Embedding> findTopByProjectAndTypeOrderByCreatedAtDesc(Project project, String type, Pageable pageable);

    /**
     * Count embeddings by content length greater than threshold
     */
    long countByContentLengthGreaterThan(Long threshold);

    /**
     * Count embeddings by project and content length greater than threshold
     */
    long countByProjectAndContentLengthGreaterThan(Project project, Long threshold);
}
