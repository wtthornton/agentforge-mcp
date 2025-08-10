import { Pool, QueryResult, QueryConfig } from 'pg';

export interface RepositoryOptions {
  tableName: string;
  primaryKey: string;
}

export abstract class BaseRepository<T> {
  protected pool: Pool;
  protected tableName: string;
  protected primaryKey: string;

  constructor(pool: Pool, options: RepositoryOptions) {
    this.pool = pool;
    this.tableName = options.tableName;
    this.primaryKey = options.primaryKey;
  }

  protected async query(text: string, params?: any[]): Promise<QueryResult> {
    const start = Date.now();
    try {
      const result = await this.pool.query(text, params);
      const duration = Date.now() - start;
      
      // Log slow queries (over 100ms)
      if (duration > 100) {
        console.warn(`Slow query detected: ${duration}ms - ${text.substring(0, 100)}...`);
      }
      
      return result;
    } catch (error) {
      console.error(`Query error: ${text}`, error);
      throw error;
    }
  }

  protected async queryOne(text: string, params?: any[]): Promise<T | null> {
    const result = await this.query(text, params);
    return result.rows.length > 0 ? result.rows[0] : null;
  }

  protected async queryMany(text: string, params?: any[]): Promise<T[]> {
    const result = await this.query(text, params);
    return result.rows;
  }

  protected async execute(text: string, params?: any[]): Promise<number> {
    const result = await this.query(text, params);
    return result.rowCount || 0;
  }

  protected buildWhereClause(filters: Record<string, any>): { whereClause: string; params: any[] } {
    const conditions: string[] = [];
    const params: any[] = [];
    let paramIndex = 1;

    for (const [key, value] of Object.entries(filters)) {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          conditions.push(`${key} = ANY($${paramIndex})`);
          params.push(value);
        } else if (typeof value === 'object') {
          // Handle JSONB queries
          conditions.push(`${key} @> $${paramIndex}`);
          params.push(JSON.stringify(value));
        } else {
          conditions.push(`${key} = $${paramIndex}`);
          params.push(value);
        }
        paramIndex++;
      }
    }

    const whereClause = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
    return { whereClause, params };
  }

  protected buildOrderByClause(orderBy?: Record<string, 'ASC' | 'DESC'>): string {
    if (!orderBy || Object.keys(orderBy).length === 0) {
      return '';
    }

    const clauses = Object.entries(orderBy).map(([column, direction]) => {
      return `${column} ${direction}`;
    });

    return `ORDER BY ${clauses.join(', ')}`;
  }

  protected buildPaginationClause(limit?: number, offset?: number): string {
    let clause = '';
    
    if (limit !== undefined) {
      clause += `LIMIT ${limit}`;
    }
    
    if (offset !== undefined) {
      clause += ` OFFSET ${offset}`;
    }
    
    return clause;
  }

  protected async transaction<R>(callback: (client: any) => Promise<R>): Promise<R> {
    const client = await this.pool.connect();
    try {
      await client.query('BEGIN');
      const result = await callback(client);
      await client.query('COMMIT');
      return result;
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }
  }

  protected async count(filters: Record<string, any> = {}): Promise<number> {
    const { whereClause, params } = this.buildWhereClause(filters);
    const query = `SELECT COUNT(*) FROM ${this.tableName} ${whereClause}`;
    const result = await this.queryOne(query, params);
    return result ? parseInt(Object.values(result)[0] as string) : 0;
  }

  protected async exists(filters: Record<string, any>): Promise<boolean> {
    const { whereClause, params } = this.buildWhereClause(filters);
    const query = `SELECT 1 FROM ${this.tableName} ${whereClause} LIMIT 1`;
    const result = await this.queryOne(query, params);
    return result !== null;
  }

  protected logOperation(operation: string, details: any): void {
    console.log(`[${this.tableName}] ${operation}:`, {
      timestamp: new Date().toISOString(),
      operation,
      details: typeof details === 'object' ? JSON.stringify(details) : details
    });
  }

  protected handleError(error: any, operation: string): never {
    const errorMessage = `Error in ${this.tableName}.${operation}: ${error.message}`;
    console.error(errorMessage, error);
    throw new Error(errorMessage);
  }
}
