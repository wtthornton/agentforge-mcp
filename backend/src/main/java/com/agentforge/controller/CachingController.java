package com.agentforge.controller;

import com.agentforge.service.CachingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Cache Management
 * 
 * This controller provides endpoints for:
 * - Cache statistics and performance metrics
 * - Cache management operations (clear, evict, warm)
 * - Cache health monitoring
 * - Cache optimization recommendations
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/cache")
@ConditionalOnBean(CachingService.class)
public class CachingController {

    private final CachingService cachingService;

    public CachingController(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    /**
     * Get comprehensive cache statistics
     * 
     * @return Cache performance metrics and statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> stats = cachingService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get cache performance analysis and recommendations
     * 
     * @return Performance analysis with optimization recommendations
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getCachePerformance() {
        Map<String, Object> performance = cachingService.getCachePerformanceAnalysis();
        return ResponseEntity.ok(performance);
    }

    /**
     * Check cache health status
     * 
     * @return Health status of Redis cache
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        boolean isHealthy = cachingService.isCacheHealthy();
        
        Map<String, Object> health = Map.of(
            "status", isHealthy ? "UP" : "DOWN",
            "description", isHealthy ? "Cache is operational" : "Cache is not responding"
        );
        
        return ResponseEntity.ok(health);
    }

    /**
     * Warm up cache with frequently accessed data
     * 
     * @return Status of cache warming operation
     */
    @PostMapping("/warm")
    public ResponseEntity<Map<String, Object>> warmCache() {
        cachingService.warmCache();
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "Cache warming initiated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Evict a specific key from cache
     * 
     * @param key Cache key to evict
     * @return Status of eviction operation
     */
    @DeleteMapping("/evict/{key}")
    public ResponseEntity<Map<String, Object>> evictKey(@PathVariable String key) {
        cachingService.evict(key);
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "Key evicted successfully",
            "key", key
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Evict all keys matching a pattern
     * 
     * @param pattern Pattern to match for eviction
     * @return Status of pattern eviction operation
     */
    @DeleteMapping("/evict-pattern")
    public ResponseEntity<Map<String, Object>> evictPattern(@RequestParam String pattern) {
        cachingService.evictPattern(pattern);
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "Pattern evicted successfully",
            "pattern", pattern
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear all cache data (use with caution)
     * 
     * @return Status of clear operation
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllCache() {
        cachingService.clearAll();
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "All cache data cleared",
            "warning", "This operation affects all cached data"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test cache functionality with a sample key-value pair
     * 
     * @param testData Data to test caching with
     * @return Test results
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testCache(@RequestBody Map<String, Object> testData) {
        String testKey = "test:cache:" + System.currentTimeMillis();
        
        // Put test data in cache
        cachingService.put(testKey, testData.getOrDefault("value", "test-value"));
        
        // Retrieve test data from cache
        var cachedValue = cachingService.get(testKey, Object.class);
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "testKey", testKey,
            "originalValue", testData.getOrDefault("value", "test-value"),
            "cachedValue", cachedValue.orElse(null),
            "cacheHit", cachedValue.isPresent()
        );
        
        // Clean up test key
        cachingService.evict(testKey);
        
        return ResponseEntity.ok(response);
    }
}