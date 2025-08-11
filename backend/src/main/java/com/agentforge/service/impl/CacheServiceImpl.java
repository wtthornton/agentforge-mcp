package com.agentforge.service.impl;

import com.agentforge.service.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);
    private static final String CACHE_PREFIX = "agentforge:backend:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public <T> void put(String cacheName, String key, T value) {
        put(cacheName, key, value, Duration.ofHours(1));
    }

    @Override
    public <T> void put(String cacheName, String key, T value, Duration ttl) {
        try {
            String cacheKey = buildKey(cacheName, key);
            String jsonValue = objectMapper.writeValueAsString(value);
            
            redisTemplate.opsForValue().set(cacheKey, jsonValue, ttl);
            logger.debug("Cached value for key: {}", cacheKey);
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize value for caching: {}", key, e);
        } catch (Exception e) {
            logger.error("Failed to cache value: {}", key, e);
        }
    }

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        try {
            String cacheKey = buildKey(cacheName, key);
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedValue != null) {
                String jsonValue = cachedValue.toString();
                T deserializedValue = objectMapper.readValue(jsonValue, type);
                logger.debug("Cache hit for key: {}", cacheKey);
                return Optional.of(deserializedValue);
            }
            
            logger.debug("Cache miss for key: {}", cacheKey);
            return Optional.empty();
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize cached value: {}", key, e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to retrieve cached value: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        try {
            String cacheKey = buildKey(cacheName, key);
            redisTemplate.delete(cacheKey);
            
            // Also evict from Spring cache if it exists
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
            }
            
            logger.debug("Evicted cache entry: {}", cacheKey);
        } catch (Exception e) {
            logger.error("Failed to evict cache entry: {}", key, e);
        }
    }

    @Override
    public void evictAll(String cacheName) {
        try {
            String pattern = buildKey(cacheName, "*");
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Evicted {} cache entries for pattern: {}", keys.size(), pattern);
            }
            
            // Also clear Spring cache
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
            
        } catch (Exception e) {
            logger.error("Failed to evict all cache entries for: {}", cacheName, e);
        }
    }

    @Override
    public boolean exists(String cacheName, String key) {
        try {
            String cacheKey = buildKey(cacheName, key);
            return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
        } catch (Exception e) {
            logger.error("Failed to check cache existence: {}", key, e);
            return false;
        }
    }

    @Override
    public void putMultiple(String cacheName, Map<String, Object> entries, Duration ttl) {
        try {
            entries.forEach((key, value) -> put(cacheName, key, value, ttl));
            logger.debug("Cached {} entries in cache: {}", entries.size(), cacheName);
        } catch (Exception e) {
            logger.error("Failed to cache multiple entries: {}", cacheName, e);
        }
    }

    @Override
    public Map<String, Object> getMultiple(String cacheName, List<String> keys) {
        try {
            List<String> cacheKeys = keys.stream()
                    .map(key -> buildKey(cacheName, key))
                    .toList();
            
            List<Object> values = redisTemplate.opsForValue().multiGet(cacheKeys);
            
            Map<String, Object> result = new java.util.HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                if (values.get(i) != null) {
                    result.put(keys.get(i), values.get(i));
                }
            }
            
            logger.debug("Retrieved {} cached entries from cache: {}", result.size(), cacheName);
            return result;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve multiple cache entries: {}", cacheName, e);
            return Map.of();
        }
    }

    @Override
    public CacheStats getStats(String cacheName) {
        try {
            String pattern = buildKey(cacheName, "*");
            Set<String> keys = redisTemplate.keys(pattern);
            
            long totalEntries = keys != null ? keys.size() : 0;
            
            // Get memory usage (this would need Redis INFO command in real implementation)
            long memoryUsage = 0; // Placeholder
            
            return CacheStats.builder()
                    .cacheName(cacheName)
                    .totalEntries(totalEntries)
                    .memoryUsage(memoryUsage)
                    .hitCount(0) // Would need proper metrics collection
                    .missCount(0)
                    .evictionCount(0)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Failed to get cache stats: {}", cacheName, e);
            return CacheStats.builder()
                    .cacheName(cacheName)
                    .totalEntries(0)
                    .memoryUsage(0)
                    .hitCount(0)
                    .missCount(0)
                    .evictionCount(0)
                    .build();
        }
    }

    @Override
    public void warmUp(String cacheName, Map<String, Object> preloadData) {
        try {
            logger.info("Warming up cache: {} with {} entries", cacheName, preloadData.size());
            putMultiple(cacheName, preloadData, Duration.ofHours(24));
            logger.info("Cache warm-up completed: {}", cacheName);
        } catch (Exception e) {
            logger.error("Failed to warm up cache: {}", cacheName, e);
        }
    }

    @Override
    public void extend(String cacheName, String key, Duration additionalTtl) {
        try {
            String cacheKey = buildKey(cacheName, key);
            redisTemplate.expire(cacheKey, additionalTtl);
            logger.debug("Extended TTL for cache key: {}", cacheKey);
        } catch (Exception e) {
            logger.error("Failed to extend TTL for cache key: {}", key, e);
        }
    }

    private String buildKey(String cacheName, String key) {
        return CACHE_PREFIX + cacheName + ":" + key;
    }

    public static class CacheStats {
        private final String cacheName;
        private final long totalEntries;
        private final long memoryUsage;
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;

        private CacheStats(Builder builder) {
            this.cacheName = builder.cacheName;
            this.totalEntries = builder.totalEntries;
            this.memoryUsage = builder.memoryUsage;
            this.hitCount = builder.hitCount;
            this.missCount = builder.missCount;
            this.evictionCount = builder.evictionCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        // Getters
        public String getCacheName() { return cacheName; }
        public long getTotalEntries() { return totalEntries; }
        public long getMemoryUsage() { return memoryUsage; }
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        public double getHitRate() { 
            return (hitCount + missCount) > 0 ? (double) hitCount / (hitCount + missCount) * 100 : 0; 
        }

        public static class Builder {
            private String cacheName;
            private long totalEntries;
            private long memoryUsage;
            private long hitCount;
            private long missCount;
            private long evictionCount;

            public Builder cacheName(String cacheName) { this.cacheName = cacheName; return this; }
            public Builder totalEntries(long totalEntries) { this.totalEntries = totalEntries; return this; }
            public Builder memoryUsage(long memoryUsage) { this.memoryUsage = memoryUsage; return this; }
            public Builder hitCount(long hitCount) { this.hitCount = hitCount; return this; }
            public Builder missCount(long missCount) { this.missCount = missCount; return this; }
            public Builder evictionCount(long evictionCount) { this.evictionCount = evictionCount; return this; }

            public CacheStats build() {
                return new CacheStats(this);
            }
        }
    }
}