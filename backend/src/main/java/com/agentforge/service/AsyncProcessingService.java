package com.agentforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Advanced Async Processing Service for AgentForge
 * 
 * This service provides:
 * - Background task processing and optimization
 * - Thread pool management and optimization
 * - Async workflow management
 * - Task prioritization and scheduling
 * - Performance monitoring for async operations
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
public class AsyncProcessingService {

    private final LoggingService loggingService;
    
    // Thread pool configuration
    @Value("${performance.async.core-pool-size:10}")
    private int corePoolSize;
    
    @Value("${performance.async.max-pool-size:50}")
    private int maxPoolSize;
    
    @Value("${performance.async.queue-capacity:100}")
    private int queueCapacity;
    
    @Value("${performance.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    // Thread pools for different task types
    private final ThreadPoolExecutor analysisThreadPool;
    private final ThreadPoolExecutor reportingThreadPool;
    private final ThreadPoolExecutor monitoringThreadPool;
    private final ThreadPoolExecutor maintenanceThreadPool;
    
    // Task tracking and statistics
    private final Map<String, TaskInfo> activeTasks = new ConcurrentHashMap<>();
    private final Map<String, TaskStatistics> taskStatistics = new ConcurrentHashMap<>();
    private final AtomicLong totalTasksSubmitted = new AtomicLong(0);
    private final AtomicLong totalTasksCompleted = new AtomicLong(0);
    private final AtomicLong totalTasksFailed = new AtomicLong(0);
    
    // Performance monitoring
    private final AtomicReference<LocalDateTime> lastOptimizationCheck = new AtomicReference<>(LocalDateTime.now());
    private final Map<String, Long> taskTypePerformance = new ConcurrentHashMap<>();

    public AsyncProcessingService(LoggingService loggingService) {
        this.loggingService = loggingService;
        
        // Initialize thread pools with optimized settings
        this.analysisThreadPool = createThreadPool("Analysis", 8, 20, 50);
        this.reportingThreadPool = createThreadPool("Reporting", 4, 15, 30);
        this.monitoringThreadPool = createThreadPool("Monitoring", 6, 25, 40);
        this.maintenanceThreadPool = createThreadPool("Maintenance", 2, 10, 20);
        
        initializeTaskStatistics();
        loggingService.logInfo("ASYNC_PROCESSING", "INIT", "Async processing service initialized", new HashMap<>());
    }

    /**
     * Create optimized thread pool for specific task type
     */
    private ThreadPoolExecutor createThreadPool(String name, int core, int max, int queue) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            core, max, keepAliveSeconds, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queue),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, name + "-" + counter++);
                    thread.setPriority(Thread.NORM_PRIORITY);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Enable monitoring
        executor.setThreadFactory(new MonitoredThreadFactory(name));
        
        return executor;
    }

    /**
     * Initialize task statistics for different task types
     */
    private void initializeTaskStatistics() {
        Arrays.asList("ANALYSIS", "REPORTING", "MONITORING", "MAINTENANCE", "EMBEDDING", "COMPLIANCE")
            .forEach(type -> taskStatistics.put(type, new TaskStatistics()));
    }

    /**
     * Submit analysis task for async processing
     */
    @Async
    public CompletableFuture<Map<String, Object>> submitAnalysisTask(String taskId, Map<String, Object> parameters) {
        return submitTask("ANALYSIS", taskId, parameters, analysisThreadPool);
    }

    /**
     * Submit reporting task for async processing
     */
    @Async
    public CompletableFuture<Map<String, Object>> submitReportingTask(String taskId, Map<String, Object> parameters) {
        return submitTask("REPORTING", taskId, parameters, reportingThreadPool);
    }

    /**
     * Submit monitoring task for async processing
     */
    @Async
    public CompletableFuture<Map<String, Object>> submitMonitoringTask(String taskId, Map<String, Object> parameters) {
        return submitTask("MONITORING", taskId, parameters, monitoringThreadPool);
    }

    /**
     * Submit maintenance task for async processing
     */
    @Async
    public CompletableFuture<Map<String, Object>> submitMaintenanceTask(String taskId, Map<String, Object> parameters) {
        return submitTask("MAINTENANCE", taskId, parameters, maintenanceThreadPool);
    }

    /**
     * Submit task to specified thread pool
     */
    private CompletableFuture<Map<String, Object>> submitTask(String taskType, String taskId, 
                                                             Map<String, Object> parameters, ThreadPoolExecutor executor) {
        try {
            totalTasksSubmitted.incrementAndGet();
            
            // Create task info
            TaskInfo taskInfo = new TaskInfo(taskId, taskType, parameters, LocalDateTime.now());
            activeTasks.put(taskId, taskInfo);
            
            // Update statistics
            TaskStatistics stats = taskStatistics.get(taskType);
            if (stats != null) {
                stats.incrementSubmitted();
            }
            
            // Submit to thread pool
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Execute task based on type
                    Map<String, Object> result = executeTask(taskType, parameters);
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    // Update performance metrics
                    taskTypePerformance.merge(taskType, executionTime, Long::sum);
                    
                    // Update task info
                    taskInfo.setCompleted(true);
                    taskInfo.setExecutionTime(executionTime);
                    taskInfo.setCompletedAt(LocalDateTime.now());
                    taskInfo.setResult(result);
                    
                    // Update statistics
                    if (stats != null) {
                        stats.incrementCompleted();
                        stats.recordExecutionTime(executionTime);
                    }
                    
                    totalTasksCompleted.incrementAndGet();
                    
                    loggingService.logInfo("ASYNC_PROCESSING", "TASK_COMPLETED", 
                        "Task completed successfully", Map.of("taskId", taskId, "taskType", taskType, "executionTime", executionTime));
                    
                    return result;
                    
                } catch (Exception e) {
                    // Handle task failure
                    taskInfo.setFailed(true);
                    taskInfo.setError(e.getMessage());
                    taskInfo.setCompletedAt(LocalDateTime.now());
                    
                    if (stats != null) {
                        stats.incrementFailed();
                    }
                    
                    totalTasksFailed.incrementAndGet();
                    
                    loggingService.logError("ASYNC_PROCESSING", "TASK_FAILED", 
                        "Task failed", Map.of("taskId", taskId, "taskType", taskType, "error", e.getMessage()));
                    
                    throw new CompletionException(e);
                } finally {
                    // Remove from active tasks
                    activeTasks.remove(taskId);
                }
            }, executor);
            
            return future;
            
        } catch (Exception e) {
            totalTasksFailed.incrementAndGet();
            loggingService.logError("ASYNC_PROCESSING", "TASK_SUBMISSION_FAILED", 
                "Failed to submit task", Map.of("taskId", taskId, "taskType", taskType, "error", e.getMessage()));
            
            CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    /**
     * Execute task based on type
     */
    private Map<String, Object> executeTask(String taskType, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        switch (taskType.toUpperCase()) {
            case "ANALYSIS":
                result = executeAnalysisTask(parameters);
                break;
            case "REPORTING":
                result = executeReportingTask(parameters);
                break;
            case "MONITORING":
                result = executeMonitoringTask(parameters);
                break;
            case "MAINTENANCE":
                result = executeMaintenanceTask(parameters);
                break;
            default:
                result.put("success", false);
                result.put("error", "Unknown task type: " + taskType);
                break;
        }
        
        return result;
    }

    /**
     * Execute analysis task
     */
    private Map<String, Object> executeAnalysisTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate analysis processing
            Thread.sleep(1000 + new Random().nextInt(2000)); // 1-3 seconds
            
            result.put("success", true);
            result.put("taskType", "ANALYSIS");
            result.put("result", "Analysis completed successfully");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Task interrupted");
        }
        
        return result;
    }

    /**
     * Execute reporting task
     */
    private Map<String, Object> executeReportingTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate reporting processing
            Thread.sleep(500 + new Random().nextInt(1000)); // 0.5-1.5 seconds
            
            result.put("success", true);
            result.put("taskType", "REPORTING");
            result.put("result", "Report generated successfully");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Task interrupted");
        }
        
        return result;
    }

    /**
     * Execute monitoring task
     */
    private Map<String, Object> executeMonitoringTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate monitoring processing
            Thread.sleep(200 + new Random().nextInt(500)); // 0.2-0.7 seconds
            
            result.put("success", true);
            result.put("taskType", "MONITORING");
            result.put("result", "Monitoring check completed");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Task interrupted");
        }
        
        return result;
    }

    /**
     * Execute maintenance task
     */
    private Map<String, Object> executeMaintenanceTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Simulate maintenance processing
            Thread.sleep(300 + new Random().nextInt(700)); // 0.3-1.0 seconds
            
            result.put("success", true);
            result.put("taskType", "MAINTENANCE");
            result.put("result", "Maintenance completed");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.put("success", false);
            result.put("error", "Task interrupted");
        }
        
        return result;
    }

    /**
     * Get thread pool status and performance metrics
     */
    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("analysisThreadPool", getThreadPoolMetrics(analysisThreadPool, "Analysis"));
        status.put("reportingThreadPool", getThreadPoolMetrics(reportingThreadPool, "Reporting"));
        status.put("monitoringThreadPool", getThreadPoolMetrics(monitoringThreadPool, "Monitoring"));
        status.put("maintenanceThreadPool", getThreadPoolMetrics(maintenanceThreadPool, "Maintenance"));
        
        return status;
    }

    /**
     * Get metrics for a specific thread pool
     */
    private Map<String, Object> getThreadPoolMetrics(ThreadPoolExecutor executor, String name) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("name", name);
        metrics.put("corePoolSize", executor.getCorePoolSize());
        metrics.put("maximumPoolSize", executor.getMaximumPoolSize());
        metrics.put("currentPoolSize", executor.getPoolSize());
        metrics.put("activeThreads", executor.getActiveCount());
        metrics.put("queuedTasks", executor.getQueue().size());
        metrics.put("completedTasks", executor.getCompletedTaskCount());
        metrics.put("totalTasks", executor.getTaskCount());
        
        // Performance metrics
        double utilization = executor.getMaximumPoolSize() > 0 ? 
            (double) executor.getActiveCount() / executor.getMaximumPoolSize() * 100 : 0.0;
        metrics.put("utilization", utilization);
        metrics.put("status", utilization > 80 ? "HIGH" : utilization > 60 ? "MEDIUM" : "LOW");
        
        return metrics;
    }

    /**
     * Get task statistics
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalSubmitted", totalTasksSubmitted.get());
        stats.put("totalCompleted", totalTasksCompleted.get());
        stats.put("totalFailed", totalTasksFailed.get());
        stats.put("activeTasks", activeTasks.size());
        
        // Calculate success rate
        long total = totalTasksSubmitted.get();
        double successRate = total > 0 ? (double) totalTasksCompleted.get() / total * 100 : 0.0;
        stats.put("successRate", successRate);
        
        // Task type statistics
        Map<String, Object> typeStats = new HashMap<>();
        taskStatistics.forEach((type, typeStat) -> {
            Map<String, Object> typeInfo = new HashMap<>();
            typeInfo.put("submitted", typeStat.getSubmitted());
            typeInfo.put("completed", typeStat.getCompleted());
            typeInfo.put("failed", typeStat.getFailed());
            typeInfo.put("averageExecutionTime", typeStat.getAverageExecutionTime());
            typeStats.put(type, typeInfo);
        });
        stats.put("taskTypeStatistics", typeStats);
        
        return stats;
    }

    /**
     * Get active tasks information
     */
    public Map<String, Object> getActiveTasks() {
        Map<String, Object> activeTasksInfo = new HashMap<>();
        
        activeTasksInfo.put("count", activeTasks.size());
        activeTasksInfo.put("tasks", activeTasks.values().stream()
            .map(TaskInfo::toMap)
            .toList());
        
        return activeTasksInfo;
    }

    /**
     * Optimize thread pool configurations based on performance metrics
     */
    public Map<String, Object> optimizeThreadPools() {
        Map<String, Object> optimization = new HashMap<>();
        
        try {
            // Analyze current performance
            Map<String, Object> currentStatus = getThreadPoolStatus();
            
            // Optimize analysis thread pool
            Map<String, Object> analysisOptimization = optimizeThreadPool(analysisThreadPool, "Analysis", currentStatus);
            optimization.put("analysis", analysisOptimization);
            
            // Optimize reporting thread pool
            Map<String, Object> reportingOptimization = optimizeThreadPool(reportingThreadPool, "Reporting", currentStatus);
            optimization.put("reporting", reportingOptimization);
            
            // Optimize monitoring thread pool
            Map<String, Object> monitoringOptimization = optimizeThreadPool(monitoringThreadPool, "Monitoring", currentStatus);
            optimization.put("monitoring", monitoringOptimization);
            
            // Optimize maintenance thread pool
            Map<String, Object> maintenanceOptimization = optimizeThreadPool(maintenanceThreadPool, "Maintenance", currentStatus);
            optimization.put("maintenance", maintenanceOptimization);
            
            lastOptimizationCheck.set(LocalDateTime.now());
            
            loggingService.logInfo("ASYNC_PROCESSING", "THREAD_POOL_OPTIMIZATION", 
                "Thread pool optimization completed", optimization);
            
        } catch (Exception e) {
            loggingService.logError("ASYNC_PROCESSING", "THREAD_POOL_OPTIMIZATION_FAILED", 
                "Thread pool optimization failed", Map.of("error", e.getMessage()));
            optimization.put("error", e.getMessage());
        }
        
        return optimization;
    }

    /**
     * Optimize specific thread pool
     */
    private Map<String, Object> optimizeThreadPool(ThreadPoolExecutor executor, String name, Map<String, Object> currentStatus) {
        Map<String, Object> optimization = new HashMap<>();
        
        try {
            Map<String, Object> poolStatus = (Map<String, Object>) currentStatus.get(name.toLowerCase() + "ThreadPool");
            double utilization = (Double) poolStatus.get("utilization");
            int queuedTasks = (Integer) poolStatus.get("queuedTasks");
            
            // Determine if optimization is needed
            if (utilization > 80 && queuedTasks > 10) {
                // High utilization and queued tasks - increase pool size
                int newMaxSize = Math.min(executor.getMaximumPoolSize() + 5, 100);
                executor.setMaximumPoolSize(newMaxSize);
                
                optimization.put("action", "INCREASED_MAX_POOL_SIZE");
                optimization.put("oldMaxSize", executor.getMaximumPoolSize());
                optimization.put("newMaxSize", newMaxSize);
                optimization.put("reason", "High utilization and queued tasks detected");
                
            } else if (utilization < 30 && executor.getMaximumPoolSize() > executor.getCorePoolSize() + 5) {
                // Low utilization - reduce pool size
                int newMaxSize = Math.max(executor.getCorePoolSize() + 2, executor.getMaximumPoolSize() - 3);
                executor.setMaximumPoolSize(newMaxSize);
                
                optimization.put("action", "DECREASED_MAX_POOL_SIZE");
                optimization.put("oldMaxSize", executor.getMaximumPoolSize());
                optimization.put("newMaxSize", newMaxSize);
                optimization.put("reason", "Low utilization detected");
                
            } else {
                optimization.put("action", "NO_CHANGE");
                optimization.put("reason", "Current configuration is optimal");
            }
            
            optimization.put("success", true);
            
        } catch (Exception e) {
            optimization.put("success", false);
            optimization.put("error", e.getMessage());
        }
        
        return optimization;
    }

    /**
     * Shutdown all thread pools gracefully
     */
    public void shutdown() {
        try {
            loggingService.logInfo("ASYNC_PROCESSING", "SHUTDOWN", 
                "Shutting down async processing service", new HashMap<>());
            
            // Shutdown thread pools
            shutdownThreadPool(analysisThreadPool, "Analysis");
            shutdownThreadPool(reportingThreadPool, "Reporting");
            shutdownThreadPool(monitoringThreadPool, "Monitoring");
            shutdownThreadPool(maintenanceThreadPool, "Maintenance");
            
            loggingService.logInfo("ASYNC_PROCESSING", "SHUTDOWN_COMPLETE", 
                "Async processing service shutdown completed", new HashMap<>());
            
        } catch (Exception e) {
            loggingService.logError("ASYNC_PROCESSING", "SHUTDOWN_ERROR", 
                "Error during shutdown", Map.of("error", e.getMessage()));
        }
    }

    /**
     * Shutdown specific thread pool gracefully
     */
    private void shutdownThreadPool(ThreadPoolExecutor executor, String name) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    loggingService.logWarning("ASYNC_PROCESSING", "SHUTDOWN_FORCED", 
                        "Forced shutdown of " + name + " thread pool", new HashMap<>());
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Scheduled thread pool optimization every 10 minutes
     */
    @Scheduled(fixedRate = 600000)
    public void scheduledThreadPoolOptimization() {
        try {
            Map<String, Object> optimization = optimizeThreadPools();
            loggingService.logDebug("ASYNC_PROCESSING", "SCHEDULED_OPTIMIZATION", 
                "Scheduled thread pool optimization completed", optimization);
            
        } catch (Exception e) {
            loggingService.logError("ASYNC_PROCESSING", "SCHEDULED_OPTIMIZATION_FAILED", 
                "Scheduled thread pool optimization failed", Map.of("error", e.getMessage()));
        }
    }

    /**
     * Task information tracking
     */
    private static class TaskInfo {
        private final String taskId;
        private final String taskType;
        private final Map<String, Object> parameters;
        private final LocalDateTime submittedAt;
        private boolean completed = false;
        private boolean failed = false;
        private long executionTime = 0;
        private LocalDateTime completedAt;
        private Map<String, Object> result;
        private String error;
        
        public TaskInfo(String taskId, String taskType, Map<String, Object> parameters, LocalDateTime submittedAt) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.parameters = parameters;
            this.submittedAt = submittedAt;
        }
        
        // Getters and setters
        public String getTaskId() { return taskId; }
        public String getTaskType() { return taskType; }
        public Map<String, Object> getParameters() { return parameters; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public boolean isFailed() { return failed; }
        public void setFailed(boolean failed) { this.failed = failed; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
        public Map<String, Object> getResult() { return result; }
        public void setResult(Map<String, Object> result) { this.result = result; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", taskId);
            map.put("taskType", taskType);
            map.put("submittedAt", submittedAt);
            map.put("completed", completed);
            map.put("failed", failed);
            map.put("executionTime", executionTime);
            map.put("completedAt", completedAt);
            map.put("result", result);
            map.put("error", error);
            return map;
        }
    }

    /**
     * Task statistics tracking
     */
    private static class TaskStatistics {
        private long submitted = 0;
        private long completed = 0;
        private long failed = 0;
        private final List<Long> executionTimes = new ArrayList<>();
        
        public void incrementSubmitted() { submitted++; }
        public void incrementCompleted() { completed++; }
        public void incrementFailed() { failed++; }
        
        public void recordExecutionTime(long executionTime) {
            executionTimes.add(executionTime);
            // Keep only last 1000 execution times for memory efficiency
            if (executionTimes.size() > 1000) {
                executionTimes.remove(0);
            }
        }
        
        public long getSubmitted() { return submitted; }
        public long getCompleted() { return completed; }
        public long getFailed() { return failed; }
        
        public double getAverageExecutionTime() {
            if (executionTimes.isEmpty()) return 0.0;
            return executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
    }

    /**
     * Monitored thread factory for performance tracking
     */
    private static class MonitoredThreadFactory implements ThreadFactory {
        private final String poolName;
        private int counter = 0;
        
        public MonitoredThreadFactory(String poolName) {
            this.poolName = poolName;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, poolName + "-" + counter++);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
}
