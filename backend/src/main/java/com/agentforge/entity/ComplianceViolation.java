package com.agentforge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ComplianceViolation entity for AgentForge system.
 * Tracks compliance violations and their resolution status.
 */
@Entity
@Table(name = "compliance_violations")
public class ComplianceViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private Analysis analysis;

    @NotBlank(message = "Rule ID is required")
    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @NotBlank(message = "Rule name is required")
    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @NotBlank(message = "Rule category is required")
    @Column(name = "rule_category", nullable = false)
    private String ruleCategory;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViolationSeverity severity;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "column_number")
    private Integer columnNumber;

    @NotBlank(message = "Violation message is required")
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ViolationStatus status = ViolationStatus.OPEN;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "false_positive")
    private Boolean falsePositive = false;

    @Column(name = "suppressed_until")
    private LocalDateTime suppressedUntil;

    @Column(name = "suppression_reason", columnDefinition = "TEXT")
    private String suppressionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ComplianceViolation() {}

    public ComplianceViolation(Project project, String ruleId, String ruleName, String ruleCategory, 
                             ViolationSeverity severity, String message) {
        this.project = project;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleCategory = ruleCategory;
        this.severity = severity;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Analysis getAnalysis() { return analysis; }
    public void setAnalysis(Analysis analysis) { this.analysis = analysis; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getRuleCategory() { return ruleCategory; }
    public void setRuleCategory(String ruleCategory) { this.ruleCategory = ruleCategory; }

    public ViolationSeverity getSeverity() { return severity; }
    public void setSeverity(ViolationSeverity severity) { this.severity = severity; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public Integer getColumnNumber() { return columnNumber; }
    public void setColumnNumber(Integer columnNumber) { this.columnNumber = columnNumber; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public ViolationStatus getStatus() { return status; }
    public void setStatus(ViolationStatus status) { this.status = status; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public Boolean getFalsePositive() { return falsePositive; }
    public void setFalsePositive(Boolean falsePositive) { this.falsePositive = falsePositive; }

    public LocalDateTime getSuppressedUntil() { return suppressedUntil; }
    public void setSuppressedUntil(LocalDateTime suppressedUntil) { this.suppressedUntil = suppressedUntil; }

    public String getSuppressionReason() { return suppressionReason; }
    public void setSuppressionReason(String suppressionReason) { this.suppressionReason = suppressionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business methods
    public void resolve(String resolvedBy, String resolutionNotes) {
        this.status = ViolationStatus.RESOLVED;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public void markAsFalsePositive(String reason) {
        this.falsePositive = true;
        this.status = ViolationStatus.FALSE_POSITIVE;
        this.resolutionNotes = reason;
        this.resolvedAt = LocalDateTime.now();
    }

    public void suppress(LocalDateTime until, String reason) {
        this.status = ViolationStatus.SUPPRESSED;
        this.suppressedUntil = until;
        this.suppressionReason = reason;
    }

    public boolean isActive() {
        return status == ViolationStatus.OPEN || 
               (status == ViolationStatus.SUPPRESSED && 
                (suppressedUntil == null || LocalDateTime.now().isBefore(suppressedUntil)));
    }

    public String getLocation() {
        if (filePath != null && lineNumber != null) {
            return filePath + ":" + lineNumber + (columnNumber != null ? ":" + columnNumber : "");
        } else if (filePath != null) {
            return filePath;
        }
        return "Unknown location";
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplianceViolation that = (ComplianceViolation) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ComplianceViolation{" +
                "id=" + id +
                ", ruleId='" + ruleId + '\'' +
                ", severity=" + severity +
                ", status=" + status +
                ", filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }

    /**
     * Violation severity enumeration
     */
    public enum ViolationSeverity {
        CRITICAL("Critical", 1),
        HIGH("High", 2),
        MEDIUM("Medium", 3),
        LOW("Low", 4),
        INFO("Info", 5);

        private final String displayName;
        private final int priority;

        ViolationSeverity(String displayName, int priority) {
            this.displayName = displayName;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * Violation status enumeration
     */
    public enum ViolationStatus {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        FALSE_POSITIVE("False Positive"),
        SUPPRESSED("Suppressed"),
        WONT_FIX("Won't Fix");

        private final String displayName;

        ViolationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
