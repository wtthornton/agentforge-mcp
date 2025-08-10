package com.agentforge.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitoring service for AgentForge backend.
 * Tracks system metrics, performance, and health indicators.
 */
@Service
public class MonitoringService {

    private final LoggingService loggingService;
    
    // Performance metrics
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    // System metrics
    private final Map<String, Object> systemMetrics = new ConcurrentHashMap<>();
    private final Map<String, Object> performanceMetrics = new ConcurrentHashMap<>();
    
    // Health status
    private String healthStatus = "UNKNOWN";
    private LocalDateTime lastHealthCheck = LocalDateTime.now();

    public MonitoringService(LoggingService loggingService) {
        this.loggingService = loggingService;
        initializeMetrics();
    }

    /**
     * Initialize monitoring metrics
     */
    private void initializeMetrics() {
        systemMetrics.put("startupTime", LocalDateTime.now());
        systemMetrics.put("version", "1.0.0");
        
        performanceMetrics.put("totalRequests", 0L);
        performanceMetrics.put("successfulRequests", 0L);
        performanceMetrics.put("failedRequests", 0L);
        performanceMetrics.put("averageResponseTime", 0.0);
        
        loggingService.logInfo("MONITORING", "INIT", "Monitoring service initialized", systemMetrics);
    }

    /**
     * Record API request metrics
     */
    public void recordApiRequest(String endpoint, String method, long responseTime, boolean success) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // Update performance metrics
        updatePerformanceMetrics();
        
        // Log the request
        Map<String, Object> context = new HashMap<>();
        context.put("endpoint", endpoint);
        context.put("method", method);
        context.put("responseTime", responseTime);
        context.put("success", success);
        
        loggingService.logApiRequest(endpoint, method, "unknown", responseTime);
    }

    /**
     * Update performance metrics
     */
    private void updatePerformanceMetrics() {
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        long totalTime = totalResponseTime.get();
        
        double avgResponseTime = total > 0 ? (double) totalTime / total : 0.0;
        
        performanceMetrics.put("totalRequests", total);
        performanceMetrics.put("successfulRequests", successful);
        performanceMetrics.put("failedRequests", failed);
        performanceMetrics.put("averageResponseTime", avgResponseTime);
        performanceMetrics.put("successRate", total > 0 ? (double) successful / total * 100 : 0.0);
    }

    /**
     * Get current system metrics
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>(systemMetrics);
        
        // Add current system information
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        metrics.put("currentTime", LocalDateTime.now());
        metrics.put("uptime", System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());
        metrics.put("memoryUsage", memoryBean.getHeapMemoryUsage().getUsed());
        metrics.put("memoryMax", memoryBean.getHeapMemoryUsage().getMax());
        metrics.put("threadCount", threadBean.getThreadCount());
        metrics.put("peakThreadCount", threadBean.getPeakThreadCount());
        
        return metrics;
    }

    /**
     * Get current performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        updatePerformanceMetrics();
        return new HashMap<>(performanceMetrics);
    }

    /**
     * Get comprehensive health status
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        // Check system health
        boolean isHealthy = checkSystemHealth();
        healthStatus = isHealthy ? "HEALTHY" : "UNHEALTHY";
        
        health.put("status", healthStatus);
        health.put("lastCheck", lastHealthCheck);
        health.put("systemMetrics", getSystemMetrics());
        health.put("performanceMetrics", getPerformanceMetrics());
        health.put("checks", performHealthChecks());
        
        lastHealthCheck = LocalDateTime.now();
        
        return health;
    }

    /**
     * Perform health checks
     */
    private Map<String, Object> performHealthChecks() {
        Map<String, Object> checks = new HashMap<>();
        
        // Memory health check
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        checks.put("memory", Map.of(
            "status", memoryUsagePercent < 80 ? "GOOD" : "WARNING",
            "usagePercent", memoryUsagePercent,
            "usedMB", usedMemory / (1024 * 1024),
            "maxMB", maxMemory / (1024 * 1024)
        ));
        
        // Thread health check
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        
        checks.put("threads", Map.of(
            "status", threadCount < 100 ? "GOOD" : "WARNING",
            "currentCount", threadCount,
            "peakCount", peakThreadCount
        ));
        
        // Performance health check
        long total = totalRequests.get();
        long failed = failedRequests.get();
        double failureRate = total > 0 ? (double) failed / total * 100 : 0.0;
        
        checks.put("performance", Map.of(
            "status", failureRate < 5.0 ? "GOOD" : "WARNING",
            "failureRate", failureRate,
            "totalRequests", total,
            "failedRequests", failed
        ));
        
        return checks;
    }

    /**
     * Check if system is healthy
     */
    private boolean checkSystemHealth() {
        Map<String, Object> checks = performHealthChecks();
        
        // Check memory usage
        Map<String, Object> memoryCheck = (Map<String, Object>) checks.get("memory");
        if ("WARNING".equals(memoryCheck.get("status"))) {
            return false;
        }
        
        // Check thread count
        Map<String, Object> threadCheck = (Map<String, Object>) checks.get("threads");
        if ("WARNING".equals(threadCheck.get("status"))) {
            return false;
        }
        
        // Check performance
        Map<String, Object> performanceCheck = (Map<String, Object>) checks.get("performance");
        if ("WARNING".equals(performanceCheck.get("status"))) {
            return false;
        }
        
        return true;
    }

    /**
     * Reset metrics (useful for testing)
     */
    public void resetMetrics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        
        updatePerformanceMetrics();
        
        loggingService.logInfo("MONITORING", "RESET", "Metrics reset", new HashMap<>());
    }

    /**
     * Scheduled health check every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void scheduledHealthCheck() {
        Map<String, Object> health = getHealthStatus();
        loggingService.logHealthStatus(healthStatus, health);
    }

    /**
     * Get monitoring summary
     */
    public Map<String, Object> getMonitoringSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("healthStatus", getHealthStatus());
        summary.put("systemMetrics", getSystemMetrics());
        summary.put("performanceMetrics", getPerformanceMetrics());
        summary.put("lastUpdate", LocalDateTime.now());
        
        return summary;
    }
}
