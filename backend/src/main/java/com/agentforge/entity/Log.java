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
@Table(name = "logs")
public class Log {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @NotNull(message = "Log level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private LogLevel level;
    
    @NotBlank(message = "Log message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Size(max = 1000, message = "Context cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String context;
    
    @Size(max = 100, message = "Correlation ID cannot exceed 100 characters")
    @Column(name = "correlation_id")
    private String correlationId;
    
    @Size(max = 100, message = "Source cannot exceed 100 characters")
    private String source;
    
    @Size(max = 100, message = "Thread name cannot exceed 100 characters")
    @Column(name = "thread_name")
    private String threadName;
    
    @ElementCollection
    @CollectionTable(name = "log_metadata", joinColumns = @JoinColumn(name = "log_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public Log() {}
    
    public Log(LogLevel level, String message, LocalDateTime timestamp) {
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public Log(LogLevel level, String message, LocalDateTime timestamp, String correlationId) {
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
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
    
    public LogLevel getLevel() {
        return level;
    }
    
    public void setLevel(LogLevel level) {
        this.level = level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public boolean isError() {
        return level == LogLevel.ERROR;
    }
    
    public boolean isWarning() {
        return level == LogLevel.WARN;
    }
    
    public boolean isInfo() {
        return level == LogLevel.INFO;
    }
    
    public boolean isDebug() {
        return level == LogLevel.DEBUG;
    }
    
    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", correlationId='" + correlationId + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return id != null && id.equals(log.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
