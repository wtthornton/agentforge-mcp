package com.agentforge.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Analysis entity for AgentForge system.
 * Tracks project analysis results, metrics, and compliance status.
 */
@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(name = "analysis_type")
    @Enumerated(EnumType.STRING)
    private AnalysisType type = AnalysisType.FULL;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "lines_analyzed")
    private Long linesAnalyzed;

    @Column(name = "files_analyzed")
    private Long filesAnalyzed;

    @Column(name = "compliance_score")
    private Double complianceScore;

    @Column(name = "quality_score")
    private Double qualityScore;

    @Column(name = "security_score")
    private Double securityScore;

    @Column(name = "performance_score")
    private Double performanceScore;

    @Column(name = "total_violations")
    private Integer totalViolations;

    @Column(name = "critical_violations")
    private Integer criticalViolations;

    @Column(name = "warning_violations")
    private Integer warningViolations;

    @Column(name = "info_violations")
    private Integer infoViolations;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @ElementCollection
    @CollectionTable(name = "analysis_metrics", joinColumns = @JoinColumn(name = "analysis_id"))
    @MapKeyColumn(name = "metric_key")
    @Column(name = "metric_value")
    private Map<String, String> metrics = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "analysis_errors", joinColumns = @JoinColumn(name = "analysis_id"))
    @Column(name = "error_message", columnDefinition = "TEXT")
    private java.util.List<String> errors = new java.util.ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Analysis() {}

    public Analysis(Project project, User user, AnalysisType type) {
        this.project = project;
        this.user = user;
        this.type = type;
        this.startTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public AnalysisStatus getStatus() { return status; }
    public void setStatus(AnalysisStatus status) { this.status = status; }

    public AnalysisType getType() { return type; }
    public void setType(AnalysisType type) { this.type = type; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Long getLinesAnalyzed() { return linesAnalyzed; }
    public void setLinesAnalyzed(Long linesAnalyzed) { this.linesAnalyzed = linesAnalyzed; }

    public Long getFilesAnalyzed() { return filesAnalyzed; }
    public void setFilesAnalyzed(Long filesAnalyzed) { this.filesAnalyzed = filesAnalyzed; }

    public Double getComplianceScore() { return complianceScore; }
    public void setComplianceScore(Double complianceScore) { this.complianceScore = complianceScore; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public Double getSecurityScore() { return securityScore; }
    public void setSecurityScore(Double securityScore) { this.securityScore = securityScore; }

    public Double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(Double performanceScore) { this.performanceScore = performanceScore; }

    public Integer getTotalViolations() { return totalViolations; }
    public void setTotalViolations(Integer totalViolations) { this.totalViolations = totalViolations; }

    public Integer getCriticalViolations() { return criticalViolations; }
    public void setCriticalViolations(Integer criticalViolations) { this.criticalViolations = criticalViolations; }

    public Integer getWarningViolations() { return warningViolations; }
    public void setWarningViolations(Integer warningViolations) { this.warningViolations = warningViolations; }

    public Integer getInfoViolations() { return infoViolations; }
    public void setInfoViolations(Integer infoViolations) { this.infoViolations = infoViolations; }

    public String getAnalysisSummary() { return analysisSummary; }
    public void setAnalysisSummary(String analysisSummary) { this.analysisSummary = analysisSummary; }

    public Map<String, String> getMetrics() { return metrics; }
    public void setMetrics(Map<String, String> metrics) { this.metrics = metrics; }

    public java.util.List<String> getErrors() { return errors; }
    public void setErrors(java.util.List<String> errors) { this.errors = errors; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public void startAnalysis() {
        this.status = AnalysisStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
    }

    public void completeAnalysis() {
        this.status = AnalysisStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.durationSeconds = java.time.Duration.between(this.startTime, this.endTime).getSeconds();
        }
    }

    public void failAnalysis(String errorMessage) {
        this.status = AnalysisStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errors.add(errorMessage);
        if (this.startTime != null) {
            this.durationSeconds = java.time.Duration.between(this.startTime, this.endTime).getSeconds();
        }
    }

    public void addMetric(String key, String value) {
        this.metrics.put(key, value);
    }

    public void addError(String errorMessage) {
        this.errors.add(errorMessage);
    }

    public void setErrorMessage(String errorMessage) {
        this.errors.clear();
        this.errors.add(errorMessage);
    }

    public void setSecurityViolations(int count) {
        this.securityScore = count > 0 ? 100.0 - (count * 10.0) : 100.0;
        if (this.securityScore < 0) this.securityScore = 0.0;
    }

    public void setPerformanceIssues(int count) {
        this.performanceScore = count > 0 ? 100.0 - (count * 10.0) : 100.0;
        if (this.performanceScore < 0) this.performanceScore = 0.0;
    }

    public void setComplianceViolations(int count) {
        this.complianceScore = count > 0 ? 100.0 - (count * 10.0) : 100.0;
        if (this.complianceScore < 0) this.complianceScore = 0.0;
    }

    public Double getOverallScore() {
        if (complianceScore == null && qualityScore == null && securityScore == null && performanceScore == null) {
            return null;
        }
        
        double total = 0.0;
        int count = 0;
        
        if (complianceScore != null) { total += complianceScore; count++; }
        if (qualityScore != null) { total += qualityScore; count++; }
        if (securityScore != null) { total += securityScore; count++; }
        if (performanceScore != null) { total += performanceScore; count++; }
        
        return count > 0 ? total / count : null;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Analysis analysis = (Analysis) o;
        return id != null && id.equals(analysis.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "id=" + id +
                ", project=" + (project != null ? project.getName() : "null") +
                ", status=" + status +
                ", type=" + type +
                ", complianceScore=" + complianceScore +
                '}';
    }

    /**
     * Analysis status enumeration
     */
    public enum AnalysisStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        CANCELLED("Cancelled");

        private final String displayName;

        AnalysisStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Analysis type enumeration
     */
    public enum AnalysisType {
        FULL("Full Analysis"),
        INCREMENTAL("Incremental Analysis"),
        COMPLIANCE("Compliance Check"),
        SECURITY("Security Scan"),
        PERFORMANCE("Performance Analysis");

        private final String displayName;

        AnalysisType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
