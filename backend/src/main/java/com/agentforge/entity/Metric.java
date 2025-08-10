package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "metrics")
public class Metric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @NotBlank(message = "Metric name is required")
    @Size(min = 1, max = 255, message = "Metric name must be between 1 and 255 characters")
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @NotNull(message = "Metric value is required")
    @Column(name = "value", nullable = false)
    private Double value;
    
    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;
    
    @ElementCollection
    @CollectionTable(name = "metric_tags", joinColumns = @JoinColumn(name = "metric_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> tags = new HashMap<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Metric() {}
    
    public Metric(String metricName, Double value, LocalDateTime timestamp) {
        this.metricName = metricName;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    public Metric(String metricName, Double value, LocalDateTime timestamp, String source) {
        this.metricName = metricName;
        this.value = value;
        this.timestamp = timestamp;
        this.source = source;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMetricName() {
        return metricName;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public boolean hasTag(String key) {
        return this.tags.containsKey(key);
    }
    
    @Override
    public String toString() {
        return "Metric{" +
                "id=" + id +
                ", metricName='" + metricName + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return id != null && id.equals(metric.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
