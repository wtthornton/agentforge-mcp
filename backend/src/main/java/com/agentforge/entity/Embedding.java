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
