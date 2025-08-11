package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    @Column(nullable = false, unique = true)
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Agent> agents = new HashSet<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();
    
    // Constructors
    public Project() {}
    
    public Project(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ProjectStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProjectStatus status) {
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
    
    public Set<Agent> getAgents() {
        return agents;
    }
    
    public void setAgents(Set<Agent> agents) {
        this.agents = agents;
    }
    
    public Set<Task> getTasks() {
        return tasks;
    }
    
    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }
    
    // Helper methods
    public void addAgent(Agent agent) {
        agents.add(agent);
        agent.setProject(this);
    }
    
    public void removeAgent(Agent agent) {
        agents.remove(agent);
        agent.setProject(null);
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
    
    // Additional fields and methods needed by services
    @Column(name = "files_count")
    private Long filesCount = 0L;
    
    @Column(name = "directories_count")
    private Long directoriesCount = 0L;
    
    @Column(name = "technology_stack", length = 1000)
    private String technologyStack;
    
    @Column(name = "last_analysis_date")
    private LocalDateTime lastAnalysisDate;
    
    public Long getFilesCount() {
        return filesCount;
    }
    
    public void setFilesCount(Long filesCount) {
        this.filesCount = filesCount;
    }
    
    public Long getDirectoriesCount() {
        return directoriesCount;
    }
    
    public void setDirectoriesCount(Long directoriesCount) {
        this.directoriesCount = directoriesCount;
    }
    
    public String getTechnologyStack() {
        return technologyStack;
    }
    
    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }
    
    public LocalDateTime getLastAnalysisDate() {
        return lastAnalysisDate;
    }
    
    public void setLastAnalysisDate(LocalDateTime lastAnalysisDate) {
        this.lastAnalysisDate = lastAnalysisDate;
    }
    
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id != null && id.equals(project.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
