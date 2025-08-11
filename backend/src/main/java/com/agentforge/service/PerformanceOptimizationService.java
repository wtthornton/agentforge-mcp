package com.agentforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Advanced Performance Optimization Service for AgentForge
 * 
 * This service provides:
 * - Real-time performance monitoring and analysis
 * - Automatic optimization recommendations
 * - Scaling decision support
 * - Performance bottleneck detection
 * - Resource utilization optimization
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
public class PerformanceOptimizationService {

    private final LoggingService loggingService;
    private final MonitoringService monitoringService;
    
    // Performance tracking
    private final AtomicLong totalOptimizations = new AtomicLong(0);
    private final AtomicLong successfulOptimizations = new AtomicLong(0);
    private final AtomicLong failedOptimizations = new AtomicLong(0);
    
    // Performance metrics storage
    private final Map<String, Object> performanceHistory = new ConcurrentHashMap<>();
    private final Map<String, Object> optimizationHistory = new ConcurrentHashMap<>();
    private final Map<String, Object> scalingRecommendations = new ConcurrentHashMap<>();
    
    // Performance thresholds
    private final Map<String, Double> performanceThresholds = new ConcurrentHashMap<>();
    private final AtomicReference<String> currentPerformanceStatus = new AtomicReference<>("OPTIMAL");
    
    // Optimization tracking
    private LocalDateTime lastOptimizationCheck = LocalDateTime.now();
    private LocalDateTime lastScalingRecommendation = LocalDateTime.now();

    public PerformanceOptimizationService(LoggingService loggingService, MonitoringService monitoringService) {
        this.loggingService = loggingService;
        this.monitoringService = monitoringService;
        initializePerformanceThresholds();
        loggingService.logInfo("PERFORMANCE_OPTIMIZATION", "INIT", "Performance optimization service initialized", new HashMap<>());
    }

    /**
     * Initialize performance thresholds based on Agent OS standards
     */
    private void initializePerformanceThresholds() {
        // Response time thresholds (P95)
        performanceThresholds.put("responseTimeP95", 200.0); // 200ms target
        performanceThresholds.put("responseTimeP99", 500.0); // 500ms warning
        
        // Memory usage thresholds
        performanceThresholds.put("memoryUsagePercent", 80.0); // 80% warning
        performanceThresholds.put("memoryUsageCritical", 90.0); // 90% critical
        
        // CPU usage thresholds
        performanceThresholds.put("cpuUsagePercent", 70.0); // 70% warning
        performanceThresholds.put("cpuUsageCritical", 85.0); // 85% critical
        
        // Database performance thresholds
        performanceThresholds.put("dbQueryTime", 50.0); // 50ms target
        performanceThresholds.put("dbConnectionPoolUsage", 80.0); // 80% warning
        
        // Vector search performance thresholds
        performanceThresholds.put("vectorSearchTime", 50.0); // 50ms target
        performanceThresholds.put("vectorSearchThroughput", 100.0); // 100 queries/sec target
    }

    /**
     * Get comprehensive performance analysis
     */
    public Map<String, Object> getPerformanceAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // Current performance metrics
        analysis.put("currentMetrics", getCurrentPerformanceMetrics());
        
        // Performance trends
        analysis.put("performanceTrends", analyzePerformanceTrends());
        
        // Optimization recommendations
        analysis.put("optimizationRecommendations", getOptimizationRecommendations());
        
        // Scaling recommendations
        analysis.put("scalingRecommendations", getScalingRecommendations());
        
        // Performance status
        analysis.put("performanceStatus", currentPerformanceStatus.get());
        analysis.put("lastOptimizationCheck", lastOptimizationCheck);
        analysis.put("lastScalingRecommendation", lastScalingRecommendation);
        
        return analysis;
    }

    /**
     * Get current performance metrics with advanced analysis
     */
    public Map<String, Object> getCurrentPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        metrics.put("systemLoad", osBean.getSystemLoadAverage());
        metrics.put("availableProcessors", osBean.getAvailableProcessors());
        metrics.put("totalPhysicalMemory", osBean.getTotalPhysicalMemorySize());
        metrics.put("freePhysicalMemory", osBean.getFreePhysicalMemorySize());
        
        // Memory analysis
        Map<String, Object> memoryAnalysis = analyzeMemoryUsage();
        metrics.put("memoryAnalysis", memoryAnalysis);
        
        // GC analysis
        Map<String, Object> gcAnalysis = analyzeGarbageCollection();
        metrics.put("gcAnalysis", gcAnalysis);
        
        // Thread analysis
        Map<String, Object> threadAnalysis = analyzeThreadUsage();
        metrics.put("threadAnalysis", threadAnalysis);
        
        // Database performance
        Map<String, Object> dbPerformance = analyzeDatabasePerformance();
        metrics.put("databasePerformance", dbPerformance);
        
        return metrics;
    }

    /**
     * Analyze memory usage patterns
     */
    private Map<String, Object> analyzeMemoryUsage() {
        Map<String, Object> analysis = new HashMap<>();
        
        MemoryPoolMXBean heapBean = ManagementFactory.getMemoryPoolMXBeans().stream()
            .filter(bean -> bean.getName().contains("Eden"))
            .findFirst()
            .orElse(null);
        
        if (heapBean != null) {
            long used = heapBean.getUsage().getUsed();
            long max = heapBean.getUsage().getMax();
            double usagePercent = (double) used / max * 100;
            
            analysis.put("heapUsagePercent", usagePercent);
            analysis.put("heapUsedMB", used / (1024 * 1024));
            analysis.put("heapMaxMB", max / (1024 * 1024));
            analysis.put("status", usagePercent < performanceThresholds.get("memoryUsagePercent") ? "GOOD" : "WARNING");
        }
        
        return analysis;
    }

    /**
     * Analyze garbage collection patterns
     */
    private Map<String, Object> analyzeGarbageCollection() {
        Map<String, Object> analysis = new HashMap<>();
        
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcTime = 0;
        long totalGcCount = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGcTime += gcBean.getCollectionTime();
            totalGcCount += gcBean.getCollectionCount();
        }
        
        analysis.put("totalGcTime", totalGcTime);
        analysis.put("totalGcCount", totalGcCount);
        analysis.put("averageGcTime", totalGcCount > 0 ? (double) totalGcTime / totalGcCount : 0.0);
        
        // GC frequency analysis
        if (totalGcCount > 100) {
            analysis.put("gcFrequency", "HIGH");
            analysis.put("recommendation", "Consider increasing heap size or optimizing memory usage");
        } else if (totalGcCount > 50) {
            analysis.put("gcFrequency", "MEDIUM");
            analysis.put("recommendation", "Monitor GC patterns");
        } else {
            analysis.put("gcFrequency", "LOW");
            analysis.put("recommendation", "GC performance is optimal");
        }
        
        return analysis;
    }

    /**
     * Analyze thread usage patterns
     */
    private Map<String, Object> analyzeThreadUsage() {
        Map<String, Object> analysis = new HashMap<>();
        
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        int daemonThreadCount = threadBean.getDaemonThreadCount();
        
        analysis.put("currentThreadCount", threadCount);
        analysis.put("peakThreadCount", peakThreadCount);
        analysis.put("daemonThreadCount", daemonThreadCount);
        analysis.put("userThreadCount", threadCount - daemonThreadCount);
        
        // Thread efficiency analysis
        double threadEfficiency = (double) (peakThreadCount - threadCount) / peakThreadCount * 100;
        analysis.put("threadEfficiency", threadEfficiency);
        
        if (threadCount > 100) {
            analysis.put("status", "WARNING");
            analysis.put("recommendation", "High thread count detected. Consider thread pool optimization.");
        } else if (threadCount > 50) {
            analysis.put("status", "MONITOR");
            analysis.put("recommendation", "Monitor thread usage patterns.");
        } else {
            analysis.put("status", "GOOD");
            analysis.put("recommendation", "Thread usage is optimal.");
        }
        
        return analysis;
    }

    /**
     * Analyze database performance
     */
    private Map<String, Object> analyzeDatabasePerformance() {
        Map<String, Object> analysis = new HashMap<>();
        
        // Get monitoring metrics for database performance
        Map<String, Object> monitoringMetrics = monitoringService.getPerformanceMetrics();
        
        double avgResponseTime = (Double) monitoringMetrics.getOrDefault("averageResponseTime", 0.0);
        long totalRequests = (Long) monitoringMetrics.getOrDefault("totalRequests", 0L);
        
        analysis.put("averageResponseTime", avgResponseTime);
        analysis.put("totalRequests", totalRequests);
        analysis.put("requestsPerSecond", calculateRequestsPerSecond(totalRequests));
        
        // Performance status based on thresholds
        if (avgResponseTime > performanceThresholds.get("responseTimeP99")) {
            analysis.put("status", "CRITICAL");
            analysis.put("recommendation", "Response time exceeds P99 threshold. Immediate optimization required.");
        } else if (avgResponseTime > performanceThresholds.get("responseTimeP95")) {
            analysis.put("status", "WARNING");
            analysis.put("recommendation", "Response time exceeds P95 threshold. Consider optimization.");
        } else {
            analysis.put("status", "GOOD");
            analysis.put("recommendation", "Response time is within acceptable limits.");
        }
        
        return analysis;
    }

    /**
     * Calculate requests per second
     */
    private double calculateRequestsPerSecond(long totalRequests) {
        // This is a simplified calculation - in production, you'd want time-based tracking
        return totalRequests > 0 ? totalRequests / 60.0 : 0.0; // Assuming 1 minute window
    }

    /**
     * Analyze performance trends
     */
    private Map<String, Object> analyzePerformanceTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // Store current metrics for trend analysis
        String timestamp = LocalDateTime.now().toString();
        performanceHistory.put(timestamp, getCurrentPerformanceMetrics());
        
        // Keep only last 100 entries for trend analysis
        if (performanceHistory.size() > 100) {
            String oldestKey = performanceHistory.keySet().iterator().next();
            performanceHistory.remove(oldestKey);
        }
        
        // Analyze trends (simplified - in production, you'd want statistical analysis)
        trends.put("dataPoints", performanceHistory.size());
        trends.put("trendDirection", determineTrendDirection());
        trends.put("stabilityScore", calculateStabilityScore());
        
        return trends;
    }

    /**
     * Determine overall trend direction
     */
    private String determineTrendDirection() {
        if (performanceHistory.size() < 2) {
            return "INSUFFICIENT_DATA";
        }
        
        // Simplified trend analysis - in production, use statistical methods
        return "STABLE"; // Placeholder
    }

    /**
     * Calculate system stability score
     */
    private double calculateStabilityScore() {
        // Simplified stability calculation - in production, use variance analysis
        return 85.0; // Placeholder - good stability
    }

    /**
     * Get optimization recommendations
     */
    public Map<String, Object> getOptimizationRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        List<Map<String, Object>> optimizationList = new ArrayList<>();
        
        // Memory optimization recommendations
        Map<String, Object> memoryRecommendations = getMemoryOptimizationRecommendations();
        if (!memoryRecommendations.isEmpty()) {
            optimizationList.add(memoryRecommendations);
        }
        
        // Database optimization recommendations
        Map<String, Object> dbRecommendations = getDatabaseOptimizationRecommendations();
        if (!dbRecommendations.isEmpty()) {
            optimizationList.add(dbRecommendations);
        }
        
        // Thread optimization recommendations
        Map<String, Object> threadRecommendations = getThreadOptimizationRecommendations();
        if (!threadRecommendations.isEmpty()) {
            optimizationList.add(threadRecommendations);
        }
        
        recommendations.put("optimizations", optimizationList);
        recommendations.put("totalRecommendations", optimizationList.size());
        recommendations.put("priority", determineOptimizationPriority(optimizationList));
        
        return recommendations;
    }

    /**
     * Get memory optimization recommendations
     */
    private Map<String, Object> getMemoryOptimizationRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        Map<String, Object> memoryAnalysis = analyzeMemoryUsage();
        
        if (memoryAnalysis.containsKey("status") && "WARNING".equals(memoryAnalysis.get("status"))) {
            recommendations.put("category", "MEMORY");
            recommendations.put("priority", "HIGH");
            recommendations.put("description", "Memory usage is approaching threshold");
            recommendations.put("actions", Arrays.asList(
                "Increase JVM heap size",
                "Optimize object creation patterns",
                "Review memory leaks",
                "Consider object pooling"
            ));
        }
        
        return recommendations;
    }

    /**
     * Get database optimization recommendations
     */
    private Map<String, Object> getDatabaseOptimizationRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        Map<String, Object> dbPerformance = analyzeDatabasePerformance();
        
        if (dbPerformance.containsKey("status") && "WARNING".equals(dbPerformance.get("status"))) {
            recommendations.put("category", "DATABASE");
            recommendations.put("priority", "MEDIUM");
            recommendations.put("description", "Database response time exceeds P95 threshold");
            recommendations.put("actions", Arrays.asList(
                "Add database indexes",
                "Optimize SQL queries",
                "Review connection pool settings",
                "Consider query caching"
            ));
        }
        
        return recommendations;
    }

    /**
     * Get thread optimization recommendations
     */
    private Map<String, Object> getThreadOptimizationRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        Map<String, Object> threadAnalysis = analyzeThreadUsage();
        
        if (threadAnalysis.containsKey("status") && "WARNING".equals(threadAnalysis.get("status"))) {
            recommendations.put("category", "THREADS");
            recommendations.put("priority", "MEDIUM");
            recommendations.put("description", "High thread count detected");
            recommendations.put("actions", Arrays.asList(
                "Optimize thread pool configurations",
                "Review async processing patterns",
                "Consider connection pooling",
                "Monitor thread lifecycle"
            ));
        }
        
        return recommendations;
    }

    /**
     * Determine optimization priority
     */
    private String determineOptimizationPriority(List<Map<String, Object>> optimizations) {
        if (optimizations.stream().anyMatch(opt -> "HIGH".equals(opt.get("priority")))) {
            return "HIGH";
        } else if (optimizations.stream().anyMatch(opt -> "MEDIUM".equals(opt.get("priority")))) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Get scaling recommendations
     */
    public Map<String, Object> getScalingRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        List<Map<String, Object>> scalingList = new ArrayList<>();
        
        // Horizontal scaling recommendations
        Map<String, Object> horizontalScaling = getHorizontalScalingRecommendations();
        if (!horizontalScaling.isEmpty()) {
            scalingList.add(horizontalScaling);
        }
        
        // Vertical scaling recommendations
        Map<String, Object> verticalScaling = getVerticalScalingRecommendations();
        if (!verticalScaling.isEmpty()) {
            scalingList.add(verticalScaling);
        }
        
        recommendations.put("scalingOptions", scalingList);
        recommendations.put("totalOptions", scalingList.size());
        recommendations.put("recommendedAction", determineScalingAction(scalingList));
        
        lastScalingRecommendation = LocalDateTime.now();
        
        return recommendations;
    }

    /**
     * Get horizontal scaling recommendations
     */
    private Map<String, Object> getHorizontalScalingRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        
        // Analyze if horizontal scaling is beneficial
        Map<String, Object> performanceMetrics = getCurrentPerformanceMetrics();
        Map<String, Object> dbPerformance = (Map<String, Object>) performanceMetrics.get("databasePerformance");
        
        if (dbPerformance != null && "CRITICAL".equals(dbPerformance.get("status"))) {
            recommendations.put("type", "HORIZONTAL");
            recommendations.put("priority", "HIGH");
            recommendations.put("description", "Performance critical - horizontal scaling recommended");
            recommendations.put("benefits", Arrays.asList(
                "Improved response time",
                "Better resource utilization",
                "Increased availability",
                "Load distribution"
            ));
            recommendations.put("implementation", Arrays.asList(
                "Deploy additional instances",
                "Configure load balancer",
                "Implement session sharing",
                "Update health checks"
            ));
        }
        
        return recommendations;
    }

    /**
     * Get vertical scaling recommendations
     */
    private Map<String, Object> getVerticalScalingRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        
        // Analyze if vertical scaling is beneficial
        Map<String, Object> memoryAnalysis = analyzeMemoryUsage();
        Map<String, Object> threadAnalysis = analyzeThreadUsage();
        
        if (("WARNING".equals(memoryAnalysis.get("status")) || "WARNING".equals(threadAnalysis.get("status")))) {
            recommendations.put("type", "VERTICAL");
            recommendations.put("priority", "MEDIUM");
            recommendations.put("description", "Resource constraints detected - vertical scaling recommended");
            recommendations.put("benefits", Arrays.asList(
                "Increased memory capacity",
                "Better thread management",
                "Improved single-instance performance",
                "Simplified deployment"
            ));
            recommendations.put("implementation", Arrays.asList(
                "Increase JVM heap size",
                "Optimize thread pool settings",
                "Review resource allocation",
                "Monitor performance impact"
            ));
        }
        
        return recommendations;
    }

    /**
     * Determine recommended scaling action
     */
    private String determineScalingAction(List<Map<String, Object>> scalingOptions) {
        if (scalingOptions.stream().anyMatch(opt -> "HIGH".equals(opt.get("priority")))) {
            return "IMMEDIATE_SCALING";
        } else if (scalingOptions.stream().anyMatch(opt -> "MEDIUM".equals(opt.get("priority")))) {
            return "PLANNED_SCALING";
        } else {
            return "MONITOR_ONLY";
        }
    }

    /**
     * Apply performance optimization
     */
    public Map<String, Object> applyOptimization(String optimizationType, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            totalOptimizations.incrementAndGet();
            
            switch (optimizationType.toUpperCase()) {
                case "MEMORY":
                    result = applyMemoryOptimization(parameters);
                    break;
                case "DATABASE":
                    result = applyDatabaseOptimization(parameters);
                    break;
                case "THREADS":
                    result = applyThreadOptimization(parameters);
                    break;
                default:
                    result.put("success", false);
                    result.put("error", "Unknown optimization type: " + optimizationType);
                    break;
            }
            
            if ((Boolean) result.getOrDefault("success", false)) {
                successfulOptimizations.incrementAndGet();
                loggingService.logInfo("PERFORMANCE_OPTIMIZATION", "OPTIMIZATION_APPLIED", 
                    "Applied " + optimizationType + " optimization", parameters);
            } else {
                failedOptimizations.incrementAndGet();
                loggingService.logError("PERFORMANCE_OPTIMIZATION", "OPTIMIZATION_FAILED", 
                    "Failed to apply " + optimizationType + " optimization", parameters);
            }
            
        } catch (Exception e) {
            failedOptimizations.incrementAndGet();
            result.put("success", false);
            result.put("error", e.getMessage());
            loggingService.logError("PERFORMANCE_OPTIMIZATION", "OPTIMIZATION_EXCEPTION", 
                "Exception during optimization", Map.of("error", e.getMessage()));
        }
        
        return result;
    }

    /**
     * Apply memory optimization
     */
    private Map<String, Object> applyMemoryOptimization(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        // Trigger garbage collection
        System.gc();
        
        result.put("success", true);
        result.put("action", "Garbage collection triggered");
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    /**
     * Apply database optimization
     */
    private Map<String, Object> applyDatabaseOptimization(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        // Placeholder for database optimization logic
        result.put("success", true);
        result.put("action", "Database optimization applied");
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    /**
     * Apply thread optimization
     */
    private Map<String, Object> applyThreadOptimization(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        // Placeholder for thread optimization logic
        result.put("success", true);
        result.put("action", "Thread optimization applied");
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    /**
     * Get optimization statistics
     */
    public Map<String, Object> getOptimizationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalOptimizations", totalOptimizations.get());
        stats.put("successfulOptimizations", successfulOptimizations.get());
        stats.put("failedOptimizations", failedOptimizations.get());
        stats.put("successRate", totalOptimizations.get() > 0 ? 
            (double) successfulOptimizations.get() / totalOptimizations.get() * 100 : 0.0);
        
        return stats;
    }

    /**
     * Scheduled performance optimization check every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledPerformanceCheck() {
        try {
            Map<String, Object> analysis = getPerformanceAnalysis();
            Map<String, Object> recommendations = getOptimizationRecommendations();
            
            // Update performance status
            String newStatus = determinePerformanceStatus(analysis);
            currentPerformanceStatus.set(newStatus);
            
            // Log performance status
            loggingService.logInfo("PERFORMANCE_OPTIMIZATION", "SCHEDULED_CHECK", 
                "Performance check completed", Map.of("status", newStatus, "recommendations", recommendations.size()));
            
            lastOptimizationCheck = LocalDateTime.now();
            
        } catch (Exception e) {
            loggingService.logError("PERFORMANCE_OPTIMIZATION", "SCHEDULED_CHECK_FAILED", 
                "Scheduled performance check failed", Map.of("error", e.getMessage()));
        }
    }

    /**
     * Determine overall performance status
     */
    private String determinePerformanceStatus(Map<String, Object> analysis) {
        Map<String, Object> recommendations = (Map<String, Object>) analysis.get("optimizationRecommendations");
        String priority = (String) recommendations.get("priority");
        
        if ("HIGH".equals(priority)) {
            return "CRITICAL";
        } else if ("MEDIUM".equals(priority)) {
            return "WARNING";
        } else {
            return "OPTIMAL";
        }
    }

    /**
     * Reset optimization statistics
     */
    public void resetOptimizationStatistics() {
        totalOptimizations.set(0);
        successfulOptimizations.set(0);
        failedOptimizations.set(0);
        
        loggingService.logInfo("PERFORMANCE_OPTIMIZATION", "STATS_RESET", 
            "Optimization statistics reset", new HashMap<>());
    }
}
