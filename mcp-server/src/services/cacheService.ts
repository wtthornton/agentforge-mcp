import Redis from 'ioredis';
import { createHash } from 'crypto';
import { logger } from '../utils/logger';

export interface CacheConfig {
  redisUrl?: string;
  defaultTtl?: number;
  keyPrefix?: string;
  enableCompression?: boolean;
  enableStats?: boolean;
}

export interface CacheStats {
  hits: number;
  misses: number;
  sets: number;
  deletes: number;
  errors: number;
  hitRate: number;
}

export class CacheService {
  private redis: Redis | null = null;
  private memoryCache: Map<string, { value: any; expires: number }> = new Map();
  private config: Required<CacheConfig>;
  private stats: CacheStats = {
    hits: 0,
    misses: 0,
    sets: 0,
    deletes: 0,
    errors: 0,
    hitRate: 0,
  };

  constructor(config: CacheConfig = {}) {
    this.config = {
      redisUrl: config.redisUrl || process.env.REDIS_URL || 'redis://localhost:6379',
      defaultTtl: config.defaultTtl || 3600, // 1 hour
      keyPrefix: config.keyPrefix || 'agentforge:mcp:',
      enableCompression: config.enableCompression ?? true,
      enableStats: config.enableStats ?? true,
    };

    this.initializeRedis();
    this.startStatsCalculation();
  }

  private initializeRedis() {
    try {
      this.redis = new Redis(this.config.redisUrl, {
        retryDelayOnFailover: 100,
        enableReadyCheck: false,
        maxRetriesPerRequest: 3,
        lazyConnect: true,
        keepAlive: 30000,
        family: 4,
        connectTimeout: 10000,
        commandTimeout: 5000,
      });

      this.redis.on('connect', () => {
        logger.info('Redis cache connected successfully');
      });

      this.redis.on('error', (err) => {
        logger.warn('Redis cache error, falling back to memory cache:', err.message);
        this.stats.errors++;
      });

      this.redis.on('ready', () => {
        logger.info('Redis cache ready for operations');
      });
    } catch (error) {
      logger.warn('Failed to initialize Redis, using memory cache only:', error);
      this.redis = null;
    }
  }

  private startStatsCalculation() {
    if (!this.config.enableStats) return;
    
    setInterval(() => {
      const total = this.stats.hits + this.stats.misses;
      this.stats.hitRate = total > 0 ? (this.stats.hits / total) * 100 : 0;
    }, 10000); // Calculate hit rate every 10 seconds
  }

  private buildKey(key: string): string {
    return `${this.config.keyPrefix}${key}`;
  }

  private generateHash(data: any): string {
    return createHash('md5').update(JSON.stringify(data)).digest('hex');
  }

  private compressValue(value: any): string {
    if (!this.config.enableCompression) {
      return JSON.stringify(value);
    }
    
    const jsonString = JSON.stringify(value);
    // Simple compression simulation - in real implementation, use gzip
    return Buffer.from(jsonString).toString('base64');
  }

  private decompressValue(compressedValue: string): any {
    if (!this.config.enableCompression) {
      return JSON.parse(compressedValue);
    }
    
    const jsonString = Buffer.from(compressedValue, 'base64').toString();
    return JSON.parse(jsonString);
  }

  async get<T = any>(key: string): Promise<T | null> {
    const cacheKey = this.buildKey(key);
    
    try {
      // Try Redis first
      if (this.redis) {
        const value = await this.redis.get(cacheKey);
        if (value !== null) {
          this.stats.hits++;
          return this.decompressValue(value);
        }
      }

      // Fallback to memory cache
      const memoryEntry = this.memoryCache.get(cacheKey);
      if (memoryEntry && memoryEntry.expires > Date.now()) {
        this.stats.hits++;
        return memoryEntry.value;
      }

      // Remove expired memory cache entry
      if (memoryEntry) {
        this.memoryCache.delete(cacheKey);
      }

      this.stats.misses++;
      return null;
    } catch (error) {
      logger.error('Cache get error:', error);
      this.stats.errors++;
      return null;
    }
  }

  async set(key: string, value: any, ttl: number = this.config.defaultTtl): Promise<boolean> {
    const cacheKey = this.buildKey(key);
    const compressedValue = this.compressValue(value);
    
    try {
      // Set in Redis
      if (this.redis) {
        await this.redis.setex(cacheKey, ttl, compressedValue);
      }

      // Set in memory cache as backup
      const expires = Date.now() + (ttl * 1000);
      this.memoryCache.set(cacheKey, { value, expires });

      // Limit memory cache size
      if (this.memoryCache.size > 1000) {
        const firstKey = this.memoryCache.keys().next().value;
        this.memoryCache.delete(firstKey);
      }

      this.stats.sets++;
      return true;
    } catch (error) {
      logger.error('Cache set error:', error);
      this.stats.errors++;
      return false;
    }
  }

  async del(key: string): Promise<boolean> {
    const cacheKey = this.buildKey(key);
    
    try {
      // Delete from Redis
      if (this.redis) {
        await this.redis.del(cacheKey);
      }

      // Delete from memory cache
      this.memoryCache.delete(cacheKey);

      this.stats.deletes++;
      return true;
    } catch (error) {
      logger.error('Cache delete error:', error);
      this.stats.errors++;
      return false;
    }
  }

  async exists(key: string): Promise<boolean> {
    const cacheKey = this.buildKey(key);
    
    try {
      // Check Redis first
      if (this.redis) {
        const exists = await this.redis.exists(cacheKey);
        if (exists) return true;
      }

      // Check memory cache
      const memoryEntry = this.memoryCache.get(cacheKey);
      return memoryEntry ? memoryEntry.expires > Date.now() : false;
    } catch (error) {
      logger.error('Cache exists error:', error);
      this.stats.errors++;
      return false;
    }
  }

  async flush(): Promise<void> {
    try {
      if (this.redis) {
        await this.redis.flushdb();
      }
      this.memoryCache.clear();
      logger.info('Cache flushed successfully');
    } catch (error) {
      logger.error('Cache flush error:', error);
      this.stats.errors++;
    }
  }

  async getMultiple<T = any>(keys: string[]): Promise<Map<string, T | null>> {
    const results = new Map<string, T | null>();
    
    try {
      if (this.redis && keys.length > 1) {
        const cacheKeys = keys.map(key => this.buildKey(key));
        const values = await this.redis.mget(...cacheKeys);
        
        keys.forEach((originalKey, index) => {
          const value = values[index];
          if (value !== null) {
            results.set(originalKey, this.decompressValue(value));
            this.stats.hits++;
          } else {
            results.set(originalKey, null);
            this.stats.misses++;
          }
        });
      } else {
        // Fallback to individual gets
        for (const key of keys) {
          const value = await this.get<T>(key);
          results.set(key, value);
        }
      }
    } catch (error) {
      logger.error('Cache getMultiple error:', error);
      this.stats.errors++;
      
      // Return empty results for all keys
      keys.forEach(key => results.set(key, null));
    }
    
    return results;
  }

  async setMultiple(entries: Array<{ key: string; value: any; ttl?: number }>): Promise<boolean> {
    try {
      if (this.redis && entries.length > 1) {
        const pipeline = this.redis.pipeline();
        
        entries.forEach(({ key, value, ttl = this.config.defaultTtl }) => {
          const cacheKey = this.buildKey(key);
          const compressedValue = this.compressValue(value);
          pipeline.setex(cacheKey, ttl, compressedValue);
        });
        
        await pipeline.exec();
        this.stats.sets += entries.length;
      } else {
        // Fallback to individual sets
        for (const { key, value, ttl } of entries) {
          await this.set(key, value, ttl);
        }
      }
      
      return true;
    } catch (error) {
      logger.error('Cache setMultiple error:', error);
      this.stats.errors++;
      return false;
    }
  }

  getStats(): CacheStats {
    return { ...this.stats };
  }

  async getInfo(): Promise<any> {
    try {
      if (this.redis) {
        const info = await this.redis.info();
        return {
          redisInfo: info,
          memoryCacheSize: this.memoryCache.size,
          stats: this.getStats(),
        };
      }
      
      return {
        redisInfo: 'Redis not available',
        memoryCacheSize: this.memoryCache.size,
        stats: this.getStats(),
      };
    } catch (error) {
      logger.error('Cache getInfo error:', error);
      return {
        error: error.message,
        memoryCacheSize: this.memoryCache.size,
        stats: this.getStats(),
      };
    }
  }

  async healthCheck(): Promise<{ isHealthy: boolean; details: any }> {
    const details: any = {
      redis: false,
      memoryCache: true,
      stats: this.getStats(),
    };

    try {
      if (this.redis) {
        await this.redis.ping();
        details.redis = true;
      }

      const isHealthy = details.redis || details.memoryCache;
      return { isHealthy, details };
    } catch (error) {
      logger.error('Cache health check error:', error);
      return {
        isHealthy: details.memoryCache,
        details: { ...details, error: error.message },
      };
    }
  }

  async close(): Promise<void> {
    try {
      if (this.redis) {
        await this.redis.quit();
      }
      this.memoryCache.clear();
      logger.info('Cache service closed successfully');
    } catch (error) {
      logger.error('Cache close error:', error);
    }
  }
}

// Create singleton instance
export const cacheService = new CacheService();

// Cache decorators for easy usage
export function Cache(ttl?: number) {
  return function (target: any, propertyName: string, descriptor: PropertyDescriptor) {
    const method = descriptor.value;
    
    descriptor.value = async function (...args: any[]) {
      const cacheKey = `${target.constructor.name}:${propertyName}:${JSON.stringify(args)}`;
      
      // Try to get from cache first
      const cachedResult = await cacheService.get(cacheKey);
      if (cachedResult !== null) {
        return cachedResult;
      }
      
      // Execute method and cache result
      const result = await method.apply(this, args);
      await cacheService.set(cacheKey, result, ttl);
      
      return result;
    };
    
    return descriptor;
  };
}

export function CacheInvalidate(pattern: string) {
  return function (target: any, propertyName: string, descriptor: PropertyDescriptor) {
    const method = descriptor.value;
    
    descriptor.value = async function (...args: any[]) {
      const result = await method.apply(this, args);
      
      // Invalidate cache based on pattern
      const cacheKey = pattern.replace(/\{(\d+)\}/g, (match, index) => args[index]);
      await cacheService.del(cacheKey);
      
      return result;
    };
    
    return descriptor;
  };
}