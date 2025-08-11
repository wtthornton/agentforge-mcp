package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "embeddings")
public class Embedding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @NotBlank(message = "Embedding type is required")
    @Column(nullable = false)
    private String type;
    
    @NotBlank(message = "Content is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "content_length")
    private Long contentLength;
    
    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(nullable = false)
    private Integer dimension;
    
    @Column(name = "similarity_score")
    private Double similarityScore;
    
    @NotBlank(message = "Content hash is required")
    @Column(name = "content_hash", nullable = false, unique = true)
    private String contentHash;
    
    @NotNull(message = "Vector data is required")
    @Column(name = "vector_data", columnDefinition = "TEXT", nullable = false)
    private String vectorData;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @ElementCollection
    @CollectionTable(name = "embedding_tags", joinColumns = @JoinColumn(name = "embedding_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();
    
    // Constructors
    public Embedding() {}
    
    public Embedding(String contentHash, String vectorData) {
        this.contentHash = contentHash;
        this.vectorData = vectorData;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getContentLength() {
        return contentLength;
    }
    
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getModelVersion() {
        return modelVersion;
    }
    
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
    
    public Integer getDimension() {
        return dimension;
    }
    
    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }
    
    public Double getSimilarityScore() {
        return similarityScore;
    }
    
    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }
    
    public String getEmbeddingVector() {
        return vectorData;
    }
    
    public void setEmbeddingVector(String embeddingVector) {
        this.vectorData = embeddingVector;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getContentHash() {
        return contentHash;
    }
    
    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }
    
    public String getVectorData() {
        return vectorData;
    }
    
    public void setVectorData(String vectorData) {
        this.vectorData = vectorData;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    // Helper methods
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }
    
    public String getTagValue(String key) {
        return this.tags.get(key);
    }
    
    public void removeTag(String key) {
        this.tags.remove(key);
    }
    
    @Override
    public String toString() {
        return "Embedding{" +
                "id=" + id +
                ", contentHash='" + contentHash + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Embedding embedding = (Embedding) o;
        return id != null && id.equals(embedding.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
