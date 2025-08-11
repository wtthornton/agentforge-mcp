package com.agentforge.service;

import com.agentforge.service.impl.CacheServiceImpl.CacheStats;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cache service interface for managing application-level caching
 * Supports both Redis and in-memory caching strategies
 */
public interface CacheService {

    /**
     * Store a value in the cache with default TTL
     */
    <T> void put(String cacheName, String key, T value);

    /**
     * Store a value in the cache with custom TTL
     */
    <T> void put(String cacheName, String key, T value, Duration ttl);

    /**
     * Retrieve a value from the cache
     */
    <T> Optional<T> get(String cacheName, String key, Class<T> type);

    /**
     * Remove a specific entry from the cache
     */
    void evict(String cacheName, String key);

    /**
     * Clear all entries in a cache
     */
    void evictAll(String cacheName);

    /**
     * Check if a cache entry exists
     */
    boolean exists(String cacheName, String key);

    /**
     * Store multiple values in the cache
     */
    void putMultiple(String cacheName, Map<String, Object> entries, Duration ttl);

    /**
     * Retrieve multiple values from the cache
     */
    Map<String, Object> getMultiple(String cacheName, List<String> keys);

    /**
     * Get cache statistics
     */
    CacheStats getStats(String cacheName);

    /**
     * Pre-load cache with data for performance
     */
    void warmUp(String cacheName, Map<String, Object> preloadData);

    /**
     * Extend the TTL of a cache entry
     */
    void extend(String cacheName, String key, Duration additionalTtl);
}