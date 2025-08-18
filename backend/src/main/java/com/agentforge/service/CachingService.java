package com.agentforge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Advanced Caching Service for AgentForge
 * 
 * This service provides:
 * - Redis-based caching with intelligent TTL management
 * - Cache invalidation strategies
 * - Performance optimization through caching
 * - Cache hit/miss analytics
 * - Automatic cache warming
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
@ConditionalOnBean(RedisTemplate.class)
public class CachingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final LoggingService loggingService;
    
    // Cache configuration
    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long defaultTtl;
    
    @Value("${performance.cache.max-size:1000}")
    private int maxCacheSize;
    
    // Cache statistics
    private final AtomicLong totalCacheHits = new AtomicLong(0);
    private final AtomicLong totalCacheMisses = new AtomicLong(0);
    private final AtomicLong totalCachePuts = new AtomicLong(0);
    private final AtomicLong totalCacheEvictions = new AtomicLong(0);
    
    // Cache metadata for intelligent TTL management
    private final Map<String, CacheMetadata> cacheMetadata = new ConcurrentHashMap<>();
    private final Map<String, CachePattern> cachePatterns = new ConcurrentHashMap<>();
    
    // Cache warming and optimization
    private final AtomicReference<LocalDateTime> lastCacheWarming = new AtomicReference<>(LocalDateTime.now());
    private final Map<String, Integer> accessFrequency = new ConcurrentHashMap<>();

    public CachingService(RedisTemplate<String, Object> redisTemplate, LoggingService loggingService) {
        this.redisTemplate = redisTemplate;
        this.loggingService = loggingService;
        initializeCachePatterns();
        loggingService.logInfo("CACHING", "INIT", "Caching service initialized", new HashMap<>());
    }

    /**
     * Initialize cache patterns for different data types
     */
    private void initializeCachePatterns() {
        // User data caching pattern
        cachePatterns.put("user", new CachePattern(
            "user:*",
            Duration.ofMinutes(30),
            Duration.ofMinutes(5),
            CacheStrategy.READ_HEAVY,
            Arrays.asList("user:profile:*", "user:preferences:*")
        ));
        
        // Project data caching pattern
        cachePatterns.put("project", new CachePattern(
            "project:*",
            Duration.ofMinutes(60),
            Duration.ofMinutes(10),
            CacheStrategy.READ_HEAVY,
            Arrays.asList("project:metadata:*", "project:analysis:*")
        ));
        
        // Analysis results caching pattern
        cachePatterns.put("analysis", new CachePattern(
            "analysis:*",
            Duration.ofHours(2),
            Duration.ofMinutes(15),
            CacheStrategy.WRITE_HEAVY,
            Arrays.asList("analysis:results:*", "analysis:compliance:*")
        ));
        
        // Vector embeddings caching pattern
        cachePatterns.put("embedding", new CachePattern(
            "embedding:*",
            Duration.ofHours(24),
            Duration.ofMinutes(30),
            CacheStrategy.READ_HEAVY,
            Arrays.asList("embedding:vector:*", "embedding:similarity:*")
        ));
        
        // Compliance data caching pattern
        cachePatterns.put("compliance", new CachePattern(
            "compliance:*",
            Duration.ofHours(12),
            Duration.ofMinutes(20),
            CacheStrategy.BALANCED,
            Arrays.asList("compliance:rules:*", "compliance:violations:*")
        ));
    }

    /**
     * Get value from cache with intelligent TTL management
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value != null) {
                totalCacheHits.incrementAndGet();
                updateAccessFrequency(key);
                updateCacheMetadata(key, true);
                
                if (type.isInstance(value)) {
                    return Optional.of(type.cast(value));
                } else {
                    loggingService.logWarning("CACHING", "TYPE_MISMATCH", 
                        "Cache type mismatch for key: " + key, Map.of("expected", type.getName(), "actual", value.getClass().getName()));
                    return Optional.empty();
                }
            } else {
                totalCacheMisses.incrementAndGet();
                updateCacheMetadata(key, false);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            totalCacheMisses.incrementAndGet();
            loggingService.logError("CACHING", "GET_ERROR", 
                "Error retrieving from cache: " + key, Map.of("error", e.getMessage()), e);
            return Optional.empty();
        }
    }

    /**
     * Put value in cache with intelligent TTL management
     */
    public void put(String key, Object value) {
        put(key, value, null);
    }

    /**
     * Put value in cache with custom TTL
     */
    public void put(String key, Object value, Duration customTtl) {
        try {
            Duration ttl = determineOptimalTtl(key, customTtl);
            
            redisTemplate.opsForValue().set(key, value, ttl);
            totalCachePuts.incrementAndGet();
            
            // Update cache metadata
            updateCacheMetadata(key, true);
            updateAccessFrequency(key);
            
            // Check cache size limits
            enforceCacheSizeLimits();
            
            loggingService.logDebug("CACHING", "PUT_SUCCESS", 
                "Value cached successfully", Map.of("key", key, "ttl", ttl.toMinutes()));
            
        } catch (Exception e) {
            loggingService.logError("CACHING", "PUT_ERROR", 
                "Error putting value in cache: " + key, Map.of("error", e.getMessage()), e);
        }
    }

    /**
     * Determine optimal TTL based on cache pattern and access frequency
     */
    private Duration determineOptimalTtl(String key, Duration customTtl) {
        if (customTtl != null) {
            return customTtl;
        }
        
        // Find matching cache pattern
        CachePattern pattern = findMatchingPattern(key);
        if (pattern != null) {
            Duration baseTtl = pattern.getBaseTtl();
            
            // Adjust TTL based on access frequency
            Integer frequency = accessFrequency.get(key);
            if (frequency != null && frequency > 10) {
                // High access frequency - extend TTL
                return baseTtl.plus(Duration.ofMinutes(30));
            } else if (frequency != null && frequency < 3) {
                // Low access frequency - reduce TTL
                return baseTtl.minus(Duration.ofMinutes(10));
            }
            
            return baseTtl;
        }
        
        return Duration.ofMillis(defaultTtl);
    }

    /**
     * Find matching cache pattern for a key
     */
    private CachePattern findMatchingPattern(String key) {
        return cachePatterns.values().stream()
            .filter(pattern -> key.matches(pattern.getKeyPattern().replace("*", ".*")))
            .findFirst()
            .orElse(null);
    }

    /**
     * Update access frequency for a key
     */
    private void updateAccessFrequency(String key) {
        accessFrequency.merge(key, 1, Integer::sum);
    }

    /**
     * Update cache metadata
     */
    private void updateCacheMetadata(String key, boolean wasHit) {
        CacheMetadata metadata = cacheMetadata.computeIfAbsent(key, k -> new CacheMetadata());
        
        if (wasHit) {
            metadata.incrementHits();
        } else {
            metadata.incrementMisses();
        }
        
        metadata.setLastAccess(LocalDateTime.now());
    }

    /**
     * Enforce cache size limits
     */
    private void enforceCacheSizeLimits() {
        if (cacheMetadata.size() > maxCacheSize) {
            // Evict least recently used items
            List<Map.Entry<String, CacheMetadata>> sortedEntries = new ArrayList<>(cacheMetadata.entrySet());
            sortedEntries.sort(Comparator.comparing(entry -> entry.getValue().getLastAccess()));
            
            int itemsToEvict = cacheMetadata.size() - maxCacheSize + 100; // Keep some buffer
            for (int i = 0; i < itemsToEvict && i < sortedEntries.size(); i++) {
                String keyToEvict = sortedEntries.get(i).getKey();
                evict(keyToEvict);
            }
        }
    }

    /**
     * Evict a specific key from cache
     */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            cacheMetadata.remove(key);
            accessFrequency.remove(key);
            totalCacheEvictions.incrementAndGet();
            
            loggingService.logDebug("CACHING", "EVICT_SUCCESS", 
                "Key evicted from cache", Map.of("key", key));
            
        } catch (Exception e) {
            loggingService.logError("CACHING", "EVICT_ERROR", 
                "Error evicting key from cache: " + key, Map.of("error", e.getMessage()), e);
        }
    }

    /**
     * Evict all keys matching a pattern
     */
    public void evictPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                
                // Remove metadata for evicted keys
                keys.forEach(key -> {
                    cacheMetadata.remove(key);
                    accessFrequency.remove(key);
                });
                
                totalCacheEvictions.addAndGet(keys.size());
                
                loggingService.logInfo("CACHING", "EVICT_PATTERN_SUCCESS", 
                    "Pattern eviction completed", Map.of("pattern", pattern, "keysEvicted", keys.size()));
            }
            
        } catch (Exception e) {
            loggingService.logError("CACHING", "EVICT_PATTERN_ERROR", 
                "Error evicting pattern from cache: " + pattern, Map.of("error", e.getMessage()), e);
        }
    }

    /**
     * Clear all cache data
     */
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            cacheMetadata.clear();
            accessFrequency.clear();
            
            loggingService.logInfo("CACHING", "CLEAR_ALL_SUCCESS", 
                "All cache data cleared", new HashMap<>());
            
        } catch (Exception e) {
            loggingService.logError("CACHING", "CLEAR_ALL_ERROR", 
                "Error clearing all cache data", Map.of("error", e.getMessage()), e);
        }
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRequests = totalCacheHits.get() + totalCacheMisses.get();
        double hitRate = totalRequests > 0 ? (double) totalCacheHits.get() / totalRequests * 100 : 0.0;
        
        stats.put("totalHits", totalCacheHits.get());
        stats.put("totalMisses", totalCacheMisses.get());
        stats.put("totalPuts", totalCachePuts.get());
        stats.put("totalEvictions", totalCacheEvictions.get());
        stats.put("hitRate", hitRate);
        stats.put("cacheSize", cacheMetadata.size());
        stats.put("maxCacheSize", maxCacheSize);
        stats.put("lastCacheWarming", lastCacheWarming.get());
        
        return stats;
    }

    /**
     * Get cache performance analysis
     */
    public Map<String, Object> getCachePerformanceAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // Hit rate analysis
        double hitRate = (double) totalCacheHits.get() / (totalCacheHits.get() + totalCacheMisses.get()) * 100;
        String hitRateStatus = hitRate >= 80 ? "EXCELLENT" : hitRate >= 60 ? "GOOD" : hitRate >= 40 ? "FAIR" : "POOR";
        
        analysis.put("hitRate", hitRate);
        analysis.put("hitRateStatus", hitRateStatus);
        
        // Cache efficiency analysis
        Map<String, Object> efficiency = analyzeCacheEfficiency();
        analysis.put("efficiency", efficiency);
        
        // Optimization recommendations
        List<String> recommendations = getOptimizationRecommendations(hitRate, efficiency);
        analysis.put("recommendations", recommendations);
        
        return analysis;
    }

    /**
     * Analyze cache efficiency
     */
    private Map<String, Object> analyzeCacheEfficiency() {
        Map<String, Object> efficiency = new HashMap<>();
        
        // Memory usage efficiency
        double memoryEfficiency = (double) cacheMetadata.size() / maxCacheSize * 100;
        efficiency.put("memoryEfficiency", memoryEfficiency);
        efficiency.put("memoryStatus", memoryEfficiency > 90 ? "HIGH" : memoryEfficiency > 70 ? "MEDIUM" : "LOW");
        
        // Access pattern efficiency
        Map<String, Object> accessPatterns = analyzeAccessPatterns();
        efficiency.put("accessPatterns", accessPatterns);
        
        // TTL efficiency
        Map<String, Object> ttlEfficiency = analyzeTtlEfficiency();
        efficiency.put("ttlEfficiency", ttlEfficiency);
        
        return efficiency;
    }

    /**
     * Analyze access patterns
     */
    private Map<String, Object> analyzeAccessPatterns() {
        Map<String, Object> patterns = new HashMap<>();
        
        // Find most and least accessed keys
        Optional<Map.Entry<String, Integer>> maxAccess = accessFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue());
        Optional<Map.Entry<String, Integer>> minAccess = accessFrequency.entrySet().stream()
            .min(Map.Entry.comparingByValue());
        
        patterns.put("maxAccessKey", maxAccess.map(entry -> entry.getKey()).orElse("N/A"));
        patterns.put("maxAccessCount", maxAccess.map(entry -> entry.getValue()).orElse(0));
        patterns.put("minAccessKey", minAccess.map(entry -> entry.getKey()).orElse("N/A"));
        patterns.put("minAccessCount", minAccess.map(entry -> entry.getValue()).orElse(0));
        
        // Calculate access distribution
        double avgAccess = accessFrequency.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        patterns.put("averageAccess", avgAccess);
        
        return patterns;
    }

    /**
     * Analyze TTL efficiency
     */
    private Map<String, Object> analyzeTtlEfficiency() {
        Map<String, Object> ttlAnalysis = new HashMap<>();
        
        // Count keys by TTL ranges
        Map<String, Long> ttlDistribution = new HashMap<>();
        ttlDistribution.put("short", 0L); // < 1 hour
        ttlDistribution.put("medium", 0L); // 1-6 hours
        ttlDistribution.put("long", 0L); // > 6 hours
        
        // This is a simplified analysis - in production, you'd track actual TTL values
        ttlAnalysis.put("ttlDistribution", ttlDistribution);
        ttlAnalysis.put("recommendation", "Monitor TTL effectiveness and adjust based on access patterns");
        
        return ttlAnalysis;
    }

    /**
     * Get optimization recommendations
     */
    private List<String> getOptimizationRecommendations(double hitRate, Map<String, Object> efficiency) {
        List<String> recommendations = new ArrayList<>();
        
        if (hitRate < 60) {
            recommendations.add("Low cache hit rate detected. Consider increasing TTL for frequently accessed data.");
        }
        
        Map<String, Object> memoryEfficiency = (Map<String, Object>) efficiency.get("memoryEfficiency");
        String memoryStatus = (String) memoryEfficiency.get("memoryStatus");
        
        if ("HIGH".equals(memoryStatus)) {
            recommendations.add("Cache memory usage is high. Consider evicting less frequently used items.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Cache performance is optimal. Continue monitoring for degradation.");
        }
        
        return recommendations;
    }

    /**
     * Warm up cache with frequently accessed data
     */
    public void warmCache() {
        try {
            loggingService.logInfo("CACHING", "WARM_START", 
                "Starting cache warming process", new HashMap<>());
            
            // Warm up user data
            warmUserCache();
            
            // Warm up project data
            warmProjectCache();
            
            // Warm up analysis data
            warmAnalysisCache();
            
            lastCacheWarming.set(LocalDateTime.now());
            
            loggingService.logInfo("CACHING", "WARM_SUCCESS", 
                "Cache warming completed successfully", new HashMap<>());
            
        } catch (Exception e) {
            loggingService.logError("CACHING", "WARM_ERROR", 
                "Cache warming failed", Map.of("error", e.getMessage()), e);
        }
    }

    /**
     * Warm up user-related cache
     */
    private void warmUserCache() {
        // Placeholder for user cache warming logic
        // In production, this would pre-load frequently accessed user data
        loggingService.logDebug("CACHING", "WARM_USER", 
            "User cache warming completed", new HashMap<>());
    }

    /**
     * Warm up project-related cache
     */
    private void warmProjectCache() {
        // Placeholder for project cache warming logic
        // In production, this would pre-load frequently accessed project data
        loggingService.logDebug("CACHING", "WARM_PROJECT", 
            "Project cache warming completed", new HashMap<>());
    }

    /**
     * Warm up analysis-related cache
     */
    private void warmAnalysisCache() {
        // Placeholder for analysis cache warming logic
        // In production, this would pre-load frequently accessed analysis data
        loggingService.logDebug("CACHING", "WARM_ANALYSIS", 
            "Analysis cache warming completed", new HashMap<>());
    }

    /**
     * Check if cache is healthy
     */
    public boolean isCacheHealthy() {
        try {
            // Test Redis connection
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            loggingService.logError("CACHING", "HEALTH_CHECK_FAILED", 
                "Cache health check failed", Map.of("error", e.getMessage()), e);
            return false;
        }
    }

    /**
     * Cache metadata for tracking performance
     */
    private static class CacheMetadata {
        private long hits = 0;
        private long misses = 0;
        private LocalDateTime lastAccess = LocalDateTime.now();
        
        public void incrementHits() { hits++; }
        public void incrementMisses() { misses++; }
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public LocalDateTime getLastAccess() { return lastAccess; }
        public void setLastAccess(LocalDateTime lastAccess) { this.lastAccess = lastAccess; }
    }

    /**
     * Cache pattern configuration
     */
    private static class CachePattern {
        private final String keyPattern;
        private final Duration baseTtl;
        private final Duration refreshTtl;
        private final CacheStrategy strategy;
        private final List<String> relatedPatterns;
        
        public CachePattern(String keyPattern, Duration baseTtl, Duration refreshTtl, 
                          CacheStrategy strategy, List<String> relatedPatterns) {
            this.keyPattern = keyPattern;
            this.baseTtl = baseTtl;
            this.refreshTtl = refreshTtl;
            this.strategy = strategy;
            this.relatedPatterns = relatedPatterns;
        }
        
        public String getKeyPattern() { return keyPattern; }
        public Duration getBaseTtl() { return baseTtl; }
        public Duration getRefreshTtl() { return refreshTtl; }
        public CacheStrategy getStrategy() { return strategy; }
        public List<String> getRelatedPatterns() { return relatedPatterns; }
    }

    /**
     * Cache strategy types
     */
    public enum CacheStrategy {
        READ_HEAVY,    // Optimized for frequent reads
        WRITE_HEAVY,   // Optimized for frequent writes
        BALANCED       // Balanced read/write optimization
    }
}
