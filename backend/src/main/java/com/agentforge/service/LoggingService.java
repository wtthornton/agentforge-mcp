package com.agentforge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized logging service for AgentForge backend.
 * Follows Agent OS logging standards with consistent formatting.
 */
@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Log an informational message with standard formatting
     */
    public void logInfo(String component, String operation, String message, Map<String, Object> context) {
        String logEntry = formatLogEntry("INFO", component, operation, message, context, null);
        logger.info(logEntry);
    }

    /**
     * Log a warning message with standard formatting
     */
    public void logWarning(String component, String operation, String message, Map<String, Object> context) {
        String logEntry = formatLogEntry("WARN", component, operation, message, context, null);
        logger.warn(logEntry);
    }

    /**
     * Log an error message with standard formatting
     */
    public void logError(String component, String operation, String message, Map<String, Object> context, Throwable error) {
        String logEntry = formatLogEntry("ERROR", component, operation, message, context, error);
        logger.error(logEntry, error);
    }

    /**
     * Log a debug message with standard formatting
     */
    public void logDebug(String component, String operation, String message, Map<String, Object> context) {
        String logEntry = formatLogEntry("DEBUG", component, operation, message, context, null);
        logger.debug(logEntry);
    }

    /**
     * Log compliance check results
     */
    public void logComplianceCheck(String projectPath, int overallScore, int filesChecked, int criticalIssues) {
        Map<String, Object> context = new HashMap<>();
        context.put("projectPath", projectPath);
        context.put("overallScore", overallScore);
        context.put("filesChecked", filesChecked);
        context.put("criticalIssues", criticalIssues);
        
        logInfo("COMPLIANCE", "CHECK", "Compliance check completed", context);
    }

    /**
     * Log API request
     */
    public void logApiRequest(String endpoint, String method, String clientIp, long responseTime) {
        Map<String, Object> context = new HashMap<>();
        context.put("endpoint", endpoint);
        context.put("method", method);
        context.put("clientIp", clientIp);
        context.put("responseTime", responseTime);
        
        logInfo("API", "REQUEST", "API request processed", context);
    }

    /**
     * Log system health status
     */
    public void logHealthStatus(String status, Map<String, Object> healthMetrics) {
        Map<String, Object> context = new HashMap<>();
        context.put("status", status);
        context.put("metrics", healthMetrics);
        
        logInfo("SYSTEM", "HEALTH", "Health check completed", context);
    }

    /**
     * Format log entry according to Agent OS standards
     */
    private String formatLogEntry(String level, String component, String operation, String message, 
                                 Map<String, Object> context, Throwable error) {
        
        StringBuilder logEntry = new StringBuilder();
        
        // Timestamp
        logEntry.append("[").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("] ");
        
        // Level
        logEntry.append("[").append(level).append("] ");
        
        // Component and Operation
        logEntry.append("[").append(component).append(":").append(operation).append("] ");
        
        // Message
        logEntry.append(message);
        
        // Context (if provided)
        if (context != null && !context.isEmpty()) {
            logEntry.append(" | Context: ").append(formatContext(context));
        }
        
        // Error details (if provided)
        if (error != null) {
            logEntry.append(" | Error: ").append(error.getClass().getSimpleName())
                    .append(": ").append(error.getMessage());
        }
        
        return logEntry.toString();
    }

    /**
     * Format context map for logging
     */
    private String formatContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "{}";
        }
        
        StringBuilder formatted = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (!first) {
                formatted.append(", ");
            }
            formatted.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        formatted.append("}");
        return formatted.toString();
    }

    /**
     * Generate unique request ID for tracing
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Log startup information
     */
    public void logStartup() {
        Map<String, Object> context = new HashMap<>();
        context.put("version", "1.0.0");
        context.put("javaVersion", System.getProperty("java.version"));
        context.put("startupTime", LocalDateTime.now());
        
        logInfo("SYSTEM", "STARTUP", "AgentForge backend service started", context);
    }

    /**
     * Log shutdown information
     */
    public void logShutdown() {
        Map<String, Object> context = new HashMap<>();
        context.put("shutdownTime", LocalDateTime.now());
        
        logInfo("SYSTEM", "SHUTDOWN", "AgentForge backend service shutting down", context);
    }
}
