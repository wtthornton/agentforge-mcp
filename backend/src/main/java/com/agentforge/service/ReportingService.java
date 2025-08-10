package com.agentforge.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Reporting service for AgentForge backend.
 * Generates compliance reports and project analysis summaries.
 */
@Service
public class ReportingService {

    private final LoggingService loggingService;
    private final MonitoringService monitoringService;
    
    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportingService(LoggingService loggingService, MonitoringService monitoringService) {
        this.loggingService = loggingService;
        this.monitoringService = monitoringService;
    }

    /**
     * Generate comprehensive compliance report
     */
    public Map<String, Object> generateComplianceReport(String projectPath, Map<String, Object> complianceData) {
        String reportId = UUID.randomUUID().toString();
        LocalDateTime reportTime = LocalDateTime.now();
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportId);
        report.put("projectPath", projectPath);
        report.put("generatedAt", reportTime.format(REPORT_DATE_FORMAT));
        report.put("complianceData", complianceData);
        report.put("summary", generateComplianceSummary(complianceData));
        report.put("recommendations", generateRecommendations(complianceData));
        report.put("metadata", generateReportMetadata());
        
        // Log report generation
        Map<String, Object> context = new HashMap<>();
        context.put("reportId", reportId);
        context.put("projectPath", projectPath);
        context.put("overallScore", complianceData.get("overallScore"));
        
        loggingService.logInfo("REPORTING", "GENERATE", "Compliance report generated", context);
        
        return report;
    }

    /**
     * Generate project analysis report
     */
    public Map<String, Object> generateProjectAnalysisReport(String projectPath, Map<String, Object> analysisData) {
        String reportId = UUID.randomUUID().toString();
        LocalDateTime reportTime = LocalDateTime.now();
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportId);
        report.put("projectPath", projectPath);
        report.put("generatedAt", reportTime.format(REPORT_DATE_FORMAT));
        report.put("analysisData", analysisData);
        report.put("summary", generateAnalysisSummary(analysisData));
        report.put("insights", generateProjectInsights(analysisData));
        report.put("metadata", generateReportMetadata());
        
        // Log report generation
        Map<String, Object> context = new HashMap<>();
        context.put("reportId", reportId);
        context.put("projectPath", projectPath);
        
        loggingService.logInfo("REPORTING", "ANALYSIS", "Project analysis report generated", context);
        
        return report;
    }

    /**
     * Generate system health report
     */
    public Map<String, Object> generateSystemHealthReport() {
        String reportId = UUID.randomUUID().toString();
        LocalDateTime reportTime = LocalDateTime.now();
        
        Map<String, Object> healthStatus = monitoringService.getHealthStatus();
        Map<String, Object> systemMetrics = monitoringService.getSystemMetrics();
        Map<String, Object> performanceMetrics = monitoringService.getPerformanceMetrics();
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportId);
        report.put("generatedAt", reportTime.format(REPORT_DATE_FORMAT));
        report.put("healthStatus", healthStatus);
        report.put("systemMetrics", systemMetrics);
        report.put("performanceMetrics", performanceMetrics);
        report.put("summary", generateHealthSummary(healthStatus));
        report.put("alerts", generateHealthAlerts(healthStatus));
        report.put("metadata", generateReportMetadata());
        
        // Log report generation
        Map<String, Object> context = new HashMap<>();
        context.put("reportId", reportId);
        context.put("healthStatus", healthStatus.get("status"));
        
        loggingService.logInfo("REPORTING", "HEALTH", "System health report generated", context);
        
        return report;
    }

    /**
     * Generate compliance summary
     */
    private Map<String, Object> generateComplianceSummary(Map<String, Object> complianceData) {
        Map<String, Object> summary = new HashMap<>();
        
        int overallScore = (Integer) complianceData.get("overallScore");
        int filesChecked = (Integer) complianceData.get("filesChecked");
        int criticalIssues = (Integer) complianceData.get("criticalIssues");
        
        summary.put("overallScore", overallScore);
        summary.put("filesChecked", filesChecked);
        summary.put("criticalIssues", criticalIssues);
        summary.put("complianceLevel", getComplianceLevel(overallScore));
        summary.put("riskLevel", getRiskLevel(criticalIssues));
        
        return summary;
    }

    /**
     * Generate analysis summary
     */
    private Map<String, Object> generateAnalysisSummary(Map<String, Object> analysisData) {
        Map<String, Object> summary = new HashMap<>();
        
        // TODO: Implement based on actual analysis data structure
        summary.put("totalFiles", analysisData.getOrDefault("totalFiles", 0));
        summary.put("codeQuality", analysisData.getOrDefault("codeQuality", "UNKNOWN"));
        summary.put("complexity", analysisData.getOrDefault("complexity", "UNKNOWN"));
        
        return summary;
    }

    /**
     * Generate health summary
     */
    private Map<String, Object> generateHealthSummary(Map<String, Object> healthStatus) {
        Map<String, Object> summary = new HashMap<>();
        
        String status = (String) healthStatus.get("status");
        Map<String, Object> checks = (Map<String, Object>) healthStatus.get("checks");
        
        summary.put("overallStatus", status);
        summary.put("lastCheck", healthStatus.get("lastCheck"));
        summary.put("checksPerformed", checks.size());
        
        // Count check statuses
        long goodChecks = checks.values().stream()
                .filter(check -> "GOOD".equals(((Map<String, Object>) check).get("status")))
                .count();
        
        summary.put("goodChecks", goodChecks);
        summary.put("warningChecks", checks.size() - goodChecks);
        
        return summary;
    }

    /**
     * Generate compliance recommendations
     */
    private Map<String, Object> generateRecommendations(Map<String, Object> complianceData) {
        Map<String, Object> recommendations = new HashMap<>();
        
        int overallScore = (Integer) complianceData.get("overallScore");
        int criticalIssues = (Integer) complianceData.get("criticalIssues");
        
        if (overallScore < 80) {
            recommendations.put("priority", "HIGH");
            recommendations.put("action", "Immediate attention required for compliance issues");
        } else if (overallScore < 95) {
            recommendations.put("priority", "MEDIUM");
            recommendations.put("action", "Address remaining issues to improve compliance score");
        } else {
            recommendations.put("priority", "LOW");
            recommendations.put("action", "Maintain current compliance level");
        }
        
        if (criticalIssues > 0) {
            recommendations.put("criticalAction", "Address " + criticalIssues + " critical compliance issues immediately");
        }
        
        return recommendations;
    }

    /**
     * Generate project insights
     */
    private Map<String, Object> generateProjectInsights(Map<String, Object> analysisData) {
        Map<String, Object> insights = new HashMap<>();
        
        // TODO: Implement based on actual analysis data
        insights.put("codeQuality", "Code quality analysis provides insights into maintainability");
        insights.put("complexity", "Complexity metrics help identify refactoring opportunities");
        insights.put("standards", "Standards compliance ensures consistent development practices");
        
        return insights;
    }

    /**
     * Generate health alerts
     */
    private Map<String, Object> generateHealthAlerts(Map<String, Object> healthStatus) {
        Map<String, Object> alerts = new HashMap<>();
        
        Map<String, Object> checks = (Map<String, Object>) healthStatus.get("checks");
        boolean hasWarnings = checks.values().stream()
                .anyMatch(check -> "WARNING".equals(((Map<String, Object>) check).get("status")));
        
        if (hasWarnings) {
            alerts.put("level", "WARNING");
            alerts.put("message", "System health checks detected warnings");
            alerts.put("action", "Monitor system metrics and address warnings");
        } else {
            alerts.put("level", "INFO");
            alerts.put("message", "All system health checks passed");
            alerts.put("action", "Continue monitoring");
        }
        
        return alerts;
    }

    /**
     * Generate report metadata
     */
    private Map<String, Object> generateReportMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generator", "AgentForge Reporting Service");
        metadata.put("version", "1.0.0");
        metadata.put("format", "JSON");
        metadata.put("timestamp", System.currentTimeMillis());
        
        return metadata;
    }

    /**
     * Get compliance level based on score
     */
    private String getComplianceLevel(int score) {
        if (score >= 95) return "EXCELLENT";
        if (score >= 85) return "GOOD";
        if (score >= 75) return "FAIR";
        if (score >= 60) return "POOR";
        return "CRITICAL";
    }

    /**
     * Get risk level based on critical issues
     */
    private String getRiskLevel(int criticalIssues) {
        if (criticalIssues == 0) return "LOW";
        if (criticalIssues <= 2) return "MEDIUM";
        if (criticalIssues <= 5) return "HIGH";
        return "CRITICAL";
    }

    /**
     * Export report to different formats
     */
    public String exportReportToMarkdown(Map<String, Object> report) {
        StringBuilder markdown = new StringBuilder();
        
        markdown.append("# AgentForge Report\n\n");
        markdown.append("**Generated:** ").append(report.get("generatedAt")).append("\n");
        markdown.append("**Report ID:** ").append(report.get("reportId")).append("\n\n");
        
        // Add report-specific content
        if (report.containsKey("projectPath")) {
            markdown.append("**Project:** ").append(report.get("projectPath")).append("\n\n");
        }
        
        // Add summary
        if (report.containsKey("summary")) {
            markdown.append("## Summary\n\n");
            Map<String, Object> summary = (Map<String, Object>) report.get("summary");
            for (Map.Entry<String, Object> entry : summary.entrySet()) {
                markdown.append("- **").append(entry.getKey()).append(":** ").append(entry.getValue()).append("\n");
            }
            markdown.append("\n");
        }
        
        return markdown.toString();
    }
}
