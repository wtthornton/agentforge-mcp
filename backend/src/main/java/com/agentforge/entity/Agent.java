package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "agents")
public class Agent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Agent name is required")
    @Size(min = 1, max = 255, message = "Agent name must be between 1 and 255 characters")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Agent type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentType type;
    
    @Size(max = 2000, message = "Configuration cannot exceed 2000 characters")
    @Column(columnDefinition = "TEXT")
    private String configuration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status = AgentStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ElementCollection
    @CollectionTable(name = "agent_metadata", joinColumns = @JoinColumn(name = "agent_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();
    
    // Constructors
    public Agent() {}
    
    public Agent(String name, AgentType type, Project project) {
        this.name = name;
        this.type = type;
        this.project = project;
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
    
    public AgentType getType() {
        return type;
    }
    
    public void setType(AgentType type) {
        this.type = type;
    }
    
    public String getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public AgentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AgentStatus status) {
        this.status = status;
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
    
    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", projectId=" + (project != null ? project.getId() : "null") +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return id != null && id.equals(agent.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
