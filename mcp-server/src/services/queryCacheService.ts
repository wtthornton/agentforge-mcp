import { Pool } from 'pg';
import { createHash } from 'crypto';
import { cacheService } from './cacheService';
import { logger } from '../utils/logger';

export interface QueryCacheConfig {
  enableCache: boolean;
  defaultTtl: number;
  maxCacheSize: number;
  enableQueryStats: boolean;
}

export interface QueryStats {
  totalQueries: number;
  cachedQueries: number;
  cacheHitRate: number;
  avgExecutionTime: number;
  slowQueries: Array<{ query: string; executionTime: number; timestamp: Date }>;
}

export class QueryCacheService {
  private config: QueryCacheConfig;
  private queryStats: QueryStats = {
    totalQueries: 0,
    cachedQueries: 0,
    cacheHitRate: 0,
    avgExecutionTime: 0,
    slowQueries: [],
  };
  private executionTimes: number[] = [];

  constructor(config: Partial<QueryCacheConfig> = {}) {
    this.config = {
      enableCache: config.enableCache ?? true,
      defaultTtl: config.defaultTtl ?? 300, // 5 minutes for queries
      maxCacheSize: config.maxCacheSize ?? 1000,
      enableQueryStats: config.enableQueryStats ?? true,
    };
  }

  private generateQueryKey(query: string, params?: any[]): string {
    const queryHash = createHash('md5').update(query).digest('hex');
    const paramsHash = params ? createHash('md5').update(JSON.stringify(params)).digest('hex') : '';
    return `query:${queryHash}:${paramsHash}`;
  }

  private shouldCacheQuery(query: string): boolean {
    const lowercaseQuery = query.toLowerCase().trim();
    
    // Cache SELECT queries but not INSERT, UPDATE, DELETE
    if (!lowercaseQuery.startsWith('select')) {
      return false;
    }

    // Don't cache queries with functions that return different results
    const nonCacheableFunctions = ['now()', 'current_timestamp', 'random()', 'gen_random_uuid()'];
    return !nonCacheableFunctions.some(func => lowercaseQuery.includes(func));
  }

  private updateStats(executionTime: number, wasFromCache: boolean, query?: string) {
    if (!this.config.enableQueryStats) return;

    this.queryStats.totalQueries++;
    if (wasFromCache) {
      this.queryStats.cachedQueries++;
    }

    this.executionTimes.push(executionTime);
    if (this.executionTimes.length > 1000) {
      this.executionTimes.shift(); // Keep last 1000 execution times
    }

    this.queryStats.cacheHitRate = (this.queryStats.cachedQueries / this.queryStats.totalQueries) * 100;
    this.queryStats.avgExecutionTime = this.executionTimes.reduce((a, b) => a + b, 0) / this.executionTimes.length;

    // Track slow queries (> 1 second)
    if (executionTime > 1000 && query && !wasFromCache) {
      this.queryStats.slowQueries.push({
        query: query.substring(0, 200) + (query.length > 200 ? '...' : ''),
        executionTime,
        timestamp: new Date(),
      });

      // Keep only last 50 slow queries
      if (this.queryStats.slowQueries.length > 50) {
        this.queryStats.slowQueries.shift();
      }
    }
  }

  async executeQuery<T = any>(
    pool: Pool,
    query: string,
    params?: any[],
    options?: { ttl?: number; bypassCache?: boolean }
  ): Promise<T[]> {
    const startTime = Date.now();
    const shouldCache = this.config.enableCache && 
                       this.shouldCacheQuery(query) && 
                       !options?.bypassCache;

    if (shouldCache) {
      const cacheKey = this.generateQueryKey(query, params);
      
      // Try to get from cache
      const cachedResult = await cacheService.get<T[]>(cacheKey);
      if (cachedResult) {
        const executionTime = Date.now() - startTime;
        this.updateStats(executionTime, true);
        logger.debug('Query served from cache', { query: query.substring(0, 100) });
        return cachedResult;
      }
    }

    try {
      // Execute query
      const result = params 
        ? await pool.query(query, params)
        : await pool.query(query);
      
      const executionTime = Date.now() - startTime;
      const rows = result.rows as T[];

      // Cache the result if applicable
      if (shouldCache && rows.length > 0) {
        const ttl = options?.ttl || this.config.defaultTtl;
        const cacheKey = this.generateQueryKey(query, params);
        await cacheService.set(cacheKey, rows, ttl);
        logger.debug('Query result cached', { 
          query: query.substring(0, 100), 
          rowCount: rows.length,
          executionTime 
        });
      }

      this.updateStats(executionTime, false, query);
      return rows;
    } catch (error) {
      const executionTime = Date.now() - startTime;
      this.updateStats(executionTime, false);
      logger.error('Query execution failed', { query, params, error });
      throw error;
    }
  }

  async invalidatePattern(pattern: string): Promise<void> {
    // This would require Redis KEYS command or a more sophisticated cache invalidation system
    // For now, we'll implement a simple approach
    logger.info('Cache invalidation requested for pattern:', pattern);
    
    // In a production system, you might maintain a reverse index of cache keys
    // or use Redis pub/sub for cache invalidation
  }

  async invalidateTableCache(tableName: string): Promise<void> {
    const pattern = `query:*:${tableName}:*`;
    await this.invalidatePattern(pattern);
    logger.info('Invalidated cache for table:', tableName);
  }

  getStats(): QueryStats {
    return { ...this.queryStats };
  }

  async getTopQueries(limit: number = 10): Promise<Array<{ query: string; count: number; avgTime: number }>> {
    // This would require tracking query execution in more detail
    // For now, return slow queries as a proxy
    return this.queryStats.slowQueries
      .slice(-limit)
      .map(sq => ({
        query: sq.query,
        count: 1, // Would need proper tracking
        avgTime: sq.executionTime,
      }));
  }

  async optimizeCache(): Promise<void> {
    // Remove expired entries and optimize cache performance
    const stats = cacheService.getStats();
    
    if (stats.hitRate < 50) {
      logger.warn('Low cache hit rate detected:', stats.hitRate);
      // Could implement cache warming strategies here
    }

    logger.info('Cache optimization completed', { stats });
  }

  resetStats(): void {
    this.queryStats = {
      totalQueries: 0,
      cachedQueries: 0,
      cacheHitRate: 0,
      avgExecutionTime: 0,
      slowQueries: [],
    };
    this.executionTimes = [];
  }
}

// Create singleton instance
export const queryCacheService = new QueryCacheService();

// Wrapper for common query patterns
export class CachedQueries {
  constructor(private pool: Pool, private cacheService: QueryCacheService) {}

  async getProjectById(id: string, ttl?: number): Promise<any> {
    return this.cacheService.executeQuery(
      this.pool,
      'SELECT * FROM projects WHERE id = $1',
      [id],
      { ttl }
    );
  }

  async getProjectsByStatus(status: string, ttl?: number): Promise<any[]> {
    return this.cacheService.executeQuery(
      this.pool,
      'SELECT * FROM projects WHERE status = $1 ORDER BY created_at DESC',
      [status],
      { ttl }
    );
  }

  async getProjectStatistics(projectId: string, ttl?: number): Promise<any> {
    return this.cacheService.executeQuery(
      this.pool,
      `SELECT 
        p.id,
        p.name,
        p.status,
        p.compliance_score,
        COUNT(DISTINCT pf.id) as total_files,
        COUNT(DISTINCT pd.id) as total_directories,
        SUM(pf.lines_of_code) as total_lines,
        AVG(pf.complexity_score) as avg_complexity
      FROM projects p
      LEFT JOIN project_files pf ON p.id = pf.project_id
      LEFT JOIN project_directories pd ON p.id = pd.project_id
      WHERE p.id = $1
      GROUP BY p.id, p.name, p.status, p.compliance_score`,
      [projectId],
      { ttl: ttl || 600 } // Cache for 10 minutes
    );
  }

  async getComplianceViolations(projectId: string, severity?: string, ttl?: number): Promise<any[]> {
    let query = `
      SELECT cv.*, pc.standard_name 
      FROM compliance_violations cv
      JOIN project_compliance pc ON cv.compliance_id = pc.id
      WHERE pc.project_id = $1
    `;
    
    const params = [projectId];
    
    if (severity) {
      query += ' AND cv.severity = $2';
      params.push(severity);
    }
    
    query += ' ORDER BY cv.created_at DESC';

    return this.cacheService.executeQuery(this.pool, query, params, { ttl });
  }

  async getRecentAnalyses(projectId: string, limit: number = 10, ttl?: number): Promise<any[]> {
    return this.cacheService.executeQuery(
      this.pool,
      `SELECT * FROM project_analyses 
       WHERE project_id = $1 
       ORDER BY started_at DESC 
       LIMIT $2`,
      [projectId, limit],
      { ttl }
    );
  }

  // Invalidation methods
  async invalidateProject(projectId: string): Promise<void> {
    await Promise.all([
      cacheService.del(`query:*:${projectId}:*`),
      cacheService.del(`project:${projectId}`),
      cacheService.del(`project:stats:${projectId}`),
    ]);
  }

  async invalidateProjectList(): Promise<void> {
    await cacheService.del('query:*:projects:*');
  }
}