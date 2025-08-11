package com.agentforge.service;

import com.agentforge.entity.Embedding;
import com.agentforge.entity.Project;
import com.agentforge.repository.EmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;

/**
 * Embedding Service for AgentForge
 * 
 * This service manages vector embeddings and provides:
 * - Embedding generation and storage
 * - Vector similarity search
 * - Embedding updates and maintenance
 * - Performance optimization
 * - Batch processing capabilities
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
@Transactional
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    private EmbeddingRepository embeddingRepository;

    /**
     * Create a new embedding
     */
    public Embedding createEmbedding(Embedding embedding) {
        logger.info("Creating new embedding for project: {} with type: {}", 
                   embedding.getProject().getName(), embedding.getType());

        try {
            embedding.setCreatedAt(LocalDateTime.now());
            embedding.setLastUpdated(LocalDateTime.now());
            
            // Generate content hash if not provided
            if (embedding.getContentHash() == null) {
                embedding.setContentHash(generateContentHash(embedding.getContent()));
            }
            
            // Set content length if not provided
            if (embedding.getContentLength() == null) {
                embedding.setContentLength((long) embedding.getContent().length());
            }
            
            Embedding savedEmbedding = embeddingRepository.save(embedding);
            logger.info("Embedding created successfully with ID: {}", savedEmbedding.getId());
            
            return savedEmbedding;
        } catch (Exception e) {
            logger.error("Failed to create embedding for project: {}", embedding.getProject().getName(), e);
            throw new RuntimeException("Failed to create embedding", e);
        }
    }

    /**
     * Create multiple embeddings in batch
     */
    public List<Embedding> createEmbeddingsBatch(List<Embedding> embeddings) {
        logger.info("Creating {} embeddings in batch", embeddings.size());

        try {
            LocalDateTime now = LocalDateTime.now();
            
            embeddings.forEach(embedding -> {
                embedding.setCreatedAt(now);
                embedding.setLastUpdated(now);
                
                if (embedding.getContentHash() == null) {
                    embedding.setContentHash(generateContentHash(embedding.getContent()));
                }
                
                if (embedding.getContentLength() == null) {
                    embedding.setContentLength((long) embedding.getContent().length());
                }
            });
            
            List<Embedding> savedEmbeddings = embeddingRepository.saveAll(embeddings);
            logger.info("Batch embedding creation completed successfully. Created: {}", savedEmbeddings.size());
            
            return savedEmbeddings;
        } catch (Exception e) {
            logger.error("Failed to create embeddings in batch", e);
            throw new RuntimeException("Failed to create embeddings in batch", e);
        }
    }

    /**
     * Update an existing embedding
     */
    public Embedding updateEmbedding(Long embeddingId, Embedding updatedEmbedding) {
        logger.info("Updating embedding with ID: {}", embeddingId);

        try {
            Embedding existingEmbedding = embeddingRepository.findById(embeddingId)
                .orElseThrow(() -> new RuntimeException("Embedding not found: " + embeddingId));

            // Update fields
            existingEmbedding.setContent(updatedEmbedding.getContent());
            existingEmbedding.setEmbeddingVector(updatedEmbedding.getEmbeddingVector());
            existingEmbedding.setModel(updatedEmbedding.getModel());
            existingEmbedding.setModelVersion(updatedEmbedding.getModelVersion());
            existingEmbedding.setDimension(updatedEmbedding.getDimension());
            existingEmbedding.setSimilarityScore(updatedEmbedding.getSimilarityScore());
            existingEmbedding.setMetadata(updatedEmbedding.getMetadata());
            existingEmbedding.setLastUpdated(LocalDateTime.now());

            // Update content hash and length
            existingEmbedding.setContentHash(generateContentHash(updatedEmbedding.getContent()));
            existingEmbedding.setContentLength((long) updatedEmbedding.getContent().length());

            Embedding savedEmbedding = embeddingRepository.save(existingEmbedding);
            logger.info("Embedding updated successfully with ID: {}", embeddingId);
            
            return savedEmbedding;
        } catch (Exception e) {
            logger.error("Failed to update embedding with ID: {}", embeddingId, e);
            throw new RuntimeException("Failed to update embedding", e);
        }
    }

    /**
     * Get embedding by ID
     */
    public Optional<Embedding> getEmbeddingById(Long embeddingId) {
        return embeddingRepository.findById(embeddingId);
    }

    /**
     * Get embeddings by project
     */
    public List<Embedding> getEmbeddingsByProject(Project project) {
        return embeddingRepository.findByProjectOrderByCreatedAtDesc(project);
    }

    /**
     * Get embeddings by project with pagination
     */
    public Page<Embedding> getEmbeddingsByProject(Project project, Pageable pageable) {
        return embeddingRepository.findByProjectOrderByCreatedAtDesc(project, pageable);
    }

    /**
     * Get embeddings by project and type
     */
    public List<Embedding> getEmbeddingsByProjectAndType(Project project, String type) {
        return embeddingRepository.findByProjectAndTypeOrderByCreatedAtDesc(project, type);
    }

    /**
     * Get embeddings by project and type with pagination
     */
    public Page<Embedding> getEmbeddingsByProjectAndType(Project project, String type, Pageable pageable) {
        return embeddingRepository.findByProjectAndTypeOrderByCreatedAtDesc(project, type, pageable);
    }

    /**
     * Get embeddings by type
     */
    public List<Embedding> getEmbeddingsByType(String type) {
        return embeddingRepository.findByType(type);
    }

    /**
     * Get embeddings by type with pagination
     */
    public Page<Embedding> getEmbeddingsByType(String type, Pageable pageable) {
        return embeddingRepository.findByType(type, pageable);
    }

    /**
     * Get embeddings by model
     */
    public List<Embedding> getEmbeddingsByModel(String model) {
        return embeddingRepository.findByModel(model);
    }

    /**
     * Get embeddings by model with pagination
     */
    public Page<Embedding> getEmbeddingsByModel(String model, Pageable pageable) {
        return embeddingRepository.findByModel(model, pageable);
    }

    /**
     * Get embeddings by dimension
     */
    public List<Embedding> getEmbeddingsByDimension(Integer dimension) {
        return embeddingRepository.findByDimension(dimension);
    }

    /**
     * Get embeddings by dimension range
     */
    public List<Embedding> getEmbeddingsByDimensionBetween(Integer minDimension, Integer maxDimension) {
        return embeddingRepository.findByDimensionBetween(minDimension, maxDimension);
    }

    /**
     * Get embeddings by content type
     */
    public List<Embedding> getEmbeddingsByContentType(String contentType) {
        return embeddingRepository.findByContentType(contentType);
    }

    /**
     * Get embeddings by content type with pagination
     */
    public Page<Embedding> getEmbeddingsByContentType(String contentType, Pageable pageable) {
        return embeddingRepository.findByContentType(contentType, pageable);
    }

    /**
     * Get embeddings by similarity score range
     */
    public List<Embedding> getEmbeddingsBySimilarityScoreBetween(Double minScore, Double maxScore) {
        return embeddingRepository.findBySimilarityScoreBetween(minScore, maxScore);
    }

    /**
     * Get embeddings by similarity score greater than threshold
     */
    public List<Embedding> getEmbeddingsBySimilarityScoreGreaterThan(Double threshold) {
        return embeddingRepository.findBySimilarityScoreGreaterThan(threshold);
    }

    /**
     * Get embeddings by similarity score less than threshold
     */
    public List<Embedding> getEmbeddingsBySimilarityScoreLessThan(Double threshold) {
        return embeddingRepository.findBySimilarityScoreLessThan(threshold);
    }

    /**
     * Get embeddings by content length range
     */
    public List<Embedding> getEmbeddingsByContentLengthBetween(Long minLength, Long maxLength) {
        return embeddingRepository.findByContentLengthBetween(minLength, maxLength);
    }

    /**
     * Get embeddings by content length greater than threshold
     */
    public List<Embedding> getEmbeddingsByContentLengthGreaterThan(Long threshold) {
        return embeddingRepository.findByContentLengthGreaterThan(threshold);
    }

    /**
     * Get embeddings by content length less than threshold
     */
    public List<Embedding> getEmbeddingsByContentLengthLessThan(Long threshold) {
        return embeddingRepository.findByContentLengthLessThan(threshold);
    }

    /**
     * Get embeddings by metadata key
     */
    public List<Embedding> getEmbeddingsByMetadataContaining(String key) {
        return embeddingRepository.findByMetadataContaining(key);
    }

    /**
     * Get embeddings by metadata key and value
     */
    public List<Embedding> getEmbeddingsByMetadataKeyAndValue(String key, String value) {
        return embeddingRepository.findByMetadataKeyAndValue(key, value);
    }

    /**
     * Get embeddings by project and metadata key
     */
    public List<Embedding> getEmbeddingsByProjectAndMetadataContaining(Project project, String key) {
        return embeddingRepository.findByProjectAndMetadataContaining(project, key);
    }

    /**
     * Get embeddings by project and metadata key and value
     */
    public List<Embedding> getEmbeddingsByProjectAndMetadataKeyAndValue(Project project, String key, String value) {
        return embeddingRepository.findByProjectAndMetadataKeyAndValue(project, key, value);
    }

    /**
     * Get top embeddings by similarity score
     */
    public List<Embedding> getTopEmbeddingsBySimilarityScore(Project project, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return embeddingRepository.findTopByProjectAndTypeOrderBySimilarityScoreDesc(project, type, pageable);
    }

    /**
     * Get top embeddings by content length
     */
    public List<Embedding> getTopEmbeddingsByContentLength(Project project, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return embeddingRepository.findTopByProjectAndTypeOrderByContentLengthDesc(project, type, pageable);
    }

    /**
     * Get top embeddings by creation date
     */
    public List<Embedding> getTopEmbeddingsByCreationDate(Project project, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return embeddingRepository.findTopByProjectAndTypeOrderByCreatedAtDesc(project, type, pageable);
    }

    /**
     * Count embeddings by project
     */
    public long countEmbeddingsByProject(Project project) {
        return embeddingRepository.countByProject(project);
    }

    /**
     * Count embeddings by project and type
     */
    public long countEmbeddingsByProjectAndType(Project project, String type) {
        return embeddingRepository.countByProjectAndType(project, type);
    }

    /**
     * Count embeddings by type
     */
    public long countEmbeddingsByType(String type) {
        return embeddingRepository.countByType(type);
    }

    /**
     * Count embeddings by model
     */
    public long countEmbeddingsByModel(String model) {
        return embeddingRepository.countByModel(model);
    }

    /**
     * Count embeddings by dimension
     */
    public long countEmbeddingsByDimension(Integer dimension) {
        return embeddingRepository.countByDimension(dimension);
    }

    /**
     * Count embeddings by content type
     */
    public long countEmbeddingsByContentType(String contentType) {
        return embeddingRepository.countByContentType(contentType);
    }

    /**
     * Count embeddings by similarity score range
     */
    public long countEmbeddingsBySimilarityScoreBetween(Double minScore, Double maxScore) {
        return embeddingRepository.countBySimilarityScoreBetween(minScore, maxScore);
    }

    /**
     * Count embeddings by content length range
     */
    public long countEmbeddingsByContentLengthBetween(Long minLength, Long maxLength) {
        return embeddingRepository.countByContentLengthBetween(minLength, maxLength);
    }

    /**
     * Find embeddings that need to be updated
     */
    public List<Embedding> findEmbeddingsNeedingUpdate(String currentModelVersion, LocalDateTime cutoffDate) {
        return embeddingRepository.findEmbeddingsNeedingUpdate(currentModelVersion, cutoffDate);
    }

    /**
     * Find project embeddings that need to be updated
     */
    public List<Embedding> findProjectEmbeddingsNeedingUpdate(Project project, String currentModelVersion, LocalDateTime cutoffDate) {
        return embeddingRepository.findProjectEmbeddingsNeedingUpdate(project, currentModelVersion, cutoffDate);
    }

    /**
     * Find project embeddings by type that need to be updated
     */
    public List<Embedding> findProjectEmbeddingsNeedingUpdateByType(Project project, String type, String currentModelVersion, LocalDateTime cutoffDate) {
        return embeddingRepository.findProjectEmbeddingsNeedingUpdateByType(project, type, currentModelVersion, cutoffDate);
    }

    /**
     * Delete embedding by ID
     */
    public void deleteEmbeddingById(Long embeddingId) {
        embeddingRepository.deleteById(embeddingId);
        logger.info("Embedding deleted successfully with ID: {}", embeddingId);
    }

    /**
     * Delete embeddings by project
     */
    public void deleteEmbeddingsByProject(Project project) {
        List<Embedding> embeddings = embeddingRepository.findByProject(project);
        embeddingRepository.deleteAll(embeddings);
        logger.info("Deleted {} embeddings for project: {}", embeddings.size(), project.getName());
    }

    /**
     * Delete embeddings by project and type
     */
    public void deleteEmbeddingsByProjectAndType(Project project, String type) {
        List<Embedding> embeddings = embeddingRepository.findByProjectAndType(project, type);
        embeddingRepository.deleteAll(embeddings);
        logger.info("Deleted {} embeddings for project: {} and type: {}", embeddings.size(), project.getName(), type);
    }

    /**
     * Update embeddings in batch
     */
    public CompletableFuture<List<Embedding>> updateEmbeddingsBatchAsync(List<Embedding> embeddings) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                
                embeddings.forEach(embedding -> {
                    embedding.setLastUpdated(now);
                    embedding.setContentHash(generateContentHash(embedding.getContent()));
                    embedding.setContentLength((long) embedding.getContent().length());
                });
                
                List<Embedding> updatedEmbeddings = embeddingRepository.saveAll(embeddings);
                logger.info("Batch embedding update completed successfully. Updated: {}", updatedEmbeddings.size());
                
                return updatedEmbeddings;
            } catch (Exception e) {
                logger.error("Failed to update embeddings in batch", e);
                throw new RuntimeException("Failed to update embeddings in batch", e);
            }
        }, executorService);
    }

    /**
     * Generate embeddings for project content
     */
    public CompletableFuture<List<Embedding>> generateProjectEmbeddingsAsync(Project project, String type, List<String> contentList) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Generating {} embeddings for project: {} with type: {}", 
                           contentList.size(), project.getName(), type);
                
                List<Embedding> embeddings = contentList.stream()
                    .map(content -> {
                        Embedding embedding = new Embedding();
                        embedding.setProject(project);
                        embedding.setType(type);
                        embedding.setContent(content);
                        embedding.setContentHash(generateContentHash(content));
                        embedding.setContentLength((long) content.length());
                        embedding.setModel("default-model");
                        embedding.setModelVersion("1.0.0");
                        embedding.setDimension(768); // Default dimension
                        embedding.setSimilarityScore(0.0);
                        embedding.setMetadata("{}");
                        embedding.setCreatedAt(LocalDateTime.now());
                        embedding.setLastUpdated(LocalDateTime.now());
                        return embedding;
                    })
                    .toList();
                
                List<Embedding> savedEmbeddings = embeddingRepository.saveAll(embeddings);
                logger.info("Generated {} embeddings successfully for project: {}", savedEmbeddings.size(), project.getName());
                
                return savedEmbeddings;
            } catch (Exception e) {
                logger.error("Failed to generate embeddings for project: {}", project.getName(), e);
                throw new RuntimeException("Failed to generate embeddings", e);
            }
        }, executorService);
    }

    /**
     * Get embedding statistics
     */
    public Map<String, Object> getEmbeddingStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Total embeddings
            long totalEmbeddings = embeddingRepository.count();
            statistics.put("totalEmbeddings", totalEmbeddings);
            
            // Embeddings by type
            statistics.put("codeEmbeddings", embeddingRepository.countByType("code"));
            statistics.put("documentationEmbeddings", embeddingRepository.countByType("documentation"));
            statistics.put("configurationEmbeddings", embeddingRepository.countByType("configuration"));
            
            // Embeddings by model
            statistics.put("defaultModelEmbeddings", embeddingRepository.countByModel("default-model"));
            
            // Embeddings by dimension
            statistics.put("dimension768Embeddings", embeddingRepository.countByDimension(768));
            statistics.put("dimension1024Embeddings", embeddingRepository.countByDimension(1024));
            statistics.put("dimension1536Embeddings", embeddingRepository.countByDimension(1536));
            
            // Content statistics
            statistics.put("totalContentLength", embeddingRepository.countByContentLengthGreaterThan(0L));
            
            logger.info("Embedding statistics retrieved successfully");
        } catch (Exception e) {
            logger.error("Failed to retrieve embedding statistics", e);
            throw new RuntimeException("Failed to retrieve embedding statistics", e);
        }
        
        return statistics;
    }

    /**
     * Get project embedding statistics
     */
    public Map<String, Object> getProjectEmbeddingStatistics(Project project) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Project embeddings
            long totalProjectEmbeddings = embeddingRepository.countByProject(project);
            statistics.put("totalProjectEmbeddings", totalProjectEmbeddings);
            
            // Project embeddings by type
            statistics.put("projectCodeEmbeddings", embeddingRepository.countByProjectAndType(project, "code"));
            statistics.put("projectDocumentationEmbeddings", embeddingRepository.countByProjectAndType(project, "documentation"));
            statistics.put("projectConfigurationEmbeddings", embeddingRepository.countByProjectAndType(project, "configuration"));
            
            // Project embeddings by model
            statistics.put("projectDefaultModelEmbeddings", embeddingRepository.countByProjectAndModel(project, "default-model"));
            
            // Project embeddings by dimension
            statistics.put("projectDimension768Embeddings", embeddingRepository.countByProjectAndDimension(project, 768));
            statistics.put("projectDimension1024Embeddings", embeddingRepository.countByProjectAndDimension(project, 1024));
            statistics.put("projectDimension1536Embeddings", embeddingRepository.countByProjectAndDimension(project, 1536));
            
            // Project content statistics
            statistics.put("projectTotalContentLength", embeddingRepository.countByProjectAndContentLengthGreaterThan(project, 0L));
            
            logger.info("Project embedding statistics retrieved successfully for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to retrieve project embedding statistics for project: {}", project.getName(), e);
            throw new RuntimeException("Failed to retrieve project embedding statistics", e);
        }
        
        return statistics;
    }

    /**
     * Generate content hash
     */
    private String generateContentHash(String content) {
        if (content == null) {
            return null;
        }
        return String.valueOf(content.hashCode());
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        executorService.shutdown();
        logger.info("EmbeddingService cleanup completed");
    }
}
