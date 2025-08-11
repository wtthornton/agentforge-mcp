package com.agentforge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Vector Repository Interface for AgentForge
 * 
 * This interface provides specialized methods for vector operations:
 * - Vector similarity search using pgvector
 * - Vector storage and retrieval
 * - Vector-based analytics and clustering
 * - Performance-optimized vector queries
 * - Multi-dimensional vector operations
 * 
 * @author AgentForge Team
 * @version 1.0.0
 * @param <T> Entity type that supports vector operations
 * @param <ID> Entity ID type
 */
@Repository
public interface VectorRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Find entities by vector similarity using cosine distance
     * Lower distance = higher similarity
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityCosine(@Param("queryVector") String queryVector,
                                               @Param("tableName") String tableName,
                                               @Param("limit") int limit);

    /**
     * Find entities by vector similarity using Euclidean distance
     * Lower distance = higher similarity
     */
    @Query(value = "SELECT *, (embedding <-> :queryVector) as distance " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <-> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityEuclidean(@Param("queryVector") String queryVector,
                                                  @Param("tableName") String tableName,
                                                  @Param("limit") int limit);

    /**
     * Find entities by vector similarity using Manhattan distance
     * Lower distance = higher similarity
     */
    @Query(value = "SELECT *, (embedding <#> :queryVector) as distance " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <#> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityManhattan(@Param("queryVector") String queryVector,
                                                  @Param("tableName") String tableName,
                                                  @Param("limit") int limit);

    /**
     * Find entities by vector similarity with minimum similarity threshold
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND (1 - (embedding <=> :queryVector)) >= :minSimilarity " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithThreshold(@Param("queryVector") String queryVector,
                                                      @Param("minSimilarity") double minSimilarity,
                                                      @Param("tableName") String tableName,
                                                      @Param("limit") int limit);

    /**
     * Find entities by vector similarity with pagination
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <=> :queryVector " +
                   "OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithPagination(@Param("queryVector") String queryVector,
                                                       @Param("tableName") String tableName,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /**
     * Find entities by vector similarity within a specific category
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND category = :category " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityInCategory(@Param("queryVector") String queryVector,
                                                   @Param("category") String category,
                                                   @Param("tableName") String tableName,
                                                   @Param("limit") int limit);

    /**
     * Find entities by vector similarity with multiple filters
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND (:category IS NULL OR category = :category) " +
                   "AND (:status IS NULL OR status = :status) " +
                   "AND (:minScore IS NULL OR score >= :minScore) " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithFilters(@Param("queryVector") String queryVector,
                                                    @Param("category") String category,
                                                    @Param("status") String status,
                                                    @Param("minScore") Double minScore,
                                                    @Param("tableName") String tableName,
                                                    @Param("limit") int limit);

    /**
     * Find entities by vector similarity using weighted scoring
     */
    @Query(value = "SELECT *, " +
                   "((1 - (embedding <=> :queryVector)) * :similarityWeight + " +
                   "score * :scoreWeight + " +
                   "quality * :qualityWeight) as weightedScore " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY weightedScore DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithWeightedScoring(@Param("queryVector") String queryVector,
                                                            @Param("similarityWeight") double similarityWeight,
                                                            @Param("scoreWeight") double scoreWeight,
                                                            @Param("qualityWeight") double qualityWeight,
                                                            @Param("tableName") String tableName,
                                                            @Param("limit") int limit);

    /**
     * Find entities by vector clustering (K-means approximation)
     */
    @Query(value = "SELECT *, " +
                   "CASE " +
                   "  WHEN (embedding <-> :center1) < (embedding <-> :center2) " +
                   "  AND (embedding <-> :center1) < (embedding <-> :center3) " +
                   "  THEN 1 " +
                   "  WHEN (embedding <-> :center2) < (embedding <-> :center3) " +
                   "  THEN 2 " +
                   "  ELSE 3 " +
                   "END as cluster " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY cluster, (embedding <-> :center1)", nativeQuery = true)
    List<Object[]> findByVectorClustering(@Param("center1") String center1,
                                         @Param("center2") String center2,
                                         @Param("center3") String center3,
                                         @Param("tableName") String tableName);

    /**
     * Find entities by vector similarity with radius search
     */
    @Query(value = "SELECT *, (embedding <-> :queryVector) as distance " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND (embedding <-> :queryVector) <= :radius " +
                   "ORDER BY embedding <-> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorRadiusSearch(@Param("queryVector") String queryVector,
                                          @Param("radius") double radius,
                                          @Param("tableName") String tableName,
                                          @Param("limit") int limit);

    /**
     * Find entities by vector similarity with approximate nearest neighbor search
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorApproximateNearestNeighbor(@Param("queryVector") String queryVector,
                                                         @Param("tableName") String tableName,
                                                         @Param("limit") int limit);

    /**
     * Find entities by vector similarity with hybrid search (vector + text)
     */
    @Query(value = "SELECT *, " +
                   "((1 - (embedding <=> :queryVector)) * :vectorWeight + " +
                   "ts_rank(to_tsvector('english', content), plainto_tsquery('english', :textQuery)) * :textWeight) " +
                   "as hybridScore " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND content IS NOT NULL " +
                   "ORDER BY hybridScore DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorAndTextHybridSearch(@Param("queryVector") String queryVector,
                                                  @Param("textQuery") String textQuery,
                                                  @Param("vectorWeight") double vectorWeight,
                                                  @Param("textWeight") double textWeight,
                                                  @Param("tableName") String tableName,
                                                  @Param("limit") int limit);

    /**
     * Find entities by vector similarity with temporal filtering
     */
    @Query(value = "SELECT *, (1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "AND created_at >= :startDate " +
                   "AND created_at <= :endDate " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityInTimeRange(@Param("queryVector") String queryVector,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate,
                                                    @Param("tableName") String tableName,
                                                    @Param("limit") int limit);

    /**
     * Find entities by vector similarity with batch processing
     */
    @Query(value = "SELECT *, " +
                   "UNNEST(ARRAY[1,2,3,4,5]) as batchId, " +
                   "(1 - (embedding <=> :queryVector)) as similarity " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithBatchProcessing(@Param("queryVector") String queryVector,
                                                           @Param("tableName") String tableName,
                                                           @Param("limit") int limit);

    /**
     * Count entities with vector embeddings
     */
    @Query(value = "SELECT COUNT(*) FROM :tableName WHERE embedding IS NOT NULL", nativeQuery = true)
    long countWithVectorEmbeddings(@Param("tableName") String tableName);

    /**
     * Get vector statistics for analytics
     */
    @Query(value = "SELECT " +
                   "COUNT(*) as total_vectors, " +
                   "AVG(array_length(embedding, 1)) as avg_dimensions, " +
                   "MIN(array_length(embedding, 1)) as min_dimensions, " +
                   "MAX(array_length(embedding, 1)) as max_dimensions " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL", nativeQuery = true)
    Object getVectorStatistics(@Param("tableName") String tableName);

    /**
     * Find entities by vector similarity with performance metrics
     */
    @Query(value = "SELECT *, " +
                   "(1 - (embedding <=> :queryVector)) as similarity, " +
                   "EXTRACT(EPOCH FROM (clock_timestamp() - query_start)) * 1000 as query_time_ms " +
                   "FROM :tableName " +
                   "WHERE embedding IS NOT NULL " +
                   "ORDER BY embedding <=> :queryVector " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByVectorSimilarityWithPerformanceMetrics(@Param("queryVector") String queryVector,
                                                               @Param("tableName") String tableName,
                                                               @Param("limit") int limit);
}
