package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "models")
public class Model {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Model name is required")
    @jakarta.validation.constraints.Size(min = 1, max = 255, message = "Model name must be between 1 and 255 characters")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Model version is required")
    @jakarta.validation.constraints.Size(min = 1, max = 100, message = "Model version must be between 1 and 100 characters")
    @Column(nullable = false)
    private String version;
    
    @jakarta.validation.constraints.Size(max = 2000, message = "Configuration cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String configuration;
    
    @Column(name = "performance_metrics", columnDefinition = "TEXT")
    private String performanceMetrics;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(name = "usage_count")
    private Long usageCount = 0L;
    
    @ElementCollection
    @CollectionTable(name = "model_metadata", joinColumns = @JoinColumn(name = "model_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    // Constructors
    public Model() {}
    
    public Model(String name, String version) {
        this.name = name;
        this.version = version;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
    
    public String getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    public void setPerformanceMetrics(String performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public Long getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    // Helper methods
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    public String getMetadataValue(String key) {
        return this.metadata.get(key);
    }
    
    public void removeMetadata(String key) {
        this.metadata.remove(key);
    }
    
    public void incrementUsage() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
    }
    
    public String getFullName() {
        return name + " v" + version;
    }
    
    @Override
    public String toString() {
        return "Model{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", usageCount=" + usageCount +
                ", lastUsed=" + lastUsed +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return id != null && id.equals(model.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    /**
     * Model size enumeration
     */
    public enum Size {
        TINY("Tiny"),
        SMALL("Small"),
        MEDIUM("Medium"),
        LARGE("Large"),
        HUGE("Huge");
        
        private final String displayName;
        
        Size(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
