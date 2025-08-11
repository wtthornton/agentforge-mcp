import { Pool, PoolConfig } from 'pg';

export interface DatabaseConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  ssl?: boolean;
  max?: number;
  min?: number;
  idleTimeoutMillis?: number;
  connectionTimeoutMillis?: number;
  acquireTimeoutMillis?: number;
  createTimeoutMillis?: number;
  destroyTimeoutMillis?: number;
  reapIntervalMillis?: number;
  createRetryIntervalMillis?: number;
  propagateCreateError?: boolean;
  maxUses?: number;
  validateOnBorrow?: boolean;
  evictionRunIntervalMillis?: number;
  numTestsPerEvictionRun?: number;
  softIdleTimeoutMillis?: number;
  allowExitOnIdle?: boolean;
}

export class DatabaseConnection {
  private pool: Pool;
  private static instance: DatabaseConnection;

  private constructor(config: DatabaseConfig) {
    const poolConfig: PoolConfig = {
      host: config.host,
      port: config.port,
      database: config.database,
      user: config.user,
      password: config.password,
      ssl: config.ssl || false,
      // Optimized connection pool settings
      max: config.max || 30,
      min: config.min || 5,
      idleTimeoutMillis: config.idleTimeoutMillis || 300000, // 5 minutes
      connectionTimeoutMillis: config.connectionTimeoutMillis || 10000, // 10 seconds
      acquireTimeoutMillis: config.acquireTimeoutMillis || 60000,
      createTimeoutMillis: config.createTimeoutMillis || 30000,
      destroyTimeoutMillis: config.destroyTimeoutMillis || 5000,
      reapIntervalMillis: config.reapIntervalMillis || 1000,
      createRetryIntervalMillis: config.createRetryIntervalMillis || 200,
      propagateCreateError: config.propagateCreateError ?? false,
      // Performance optimizations
      application_name: 'agentforge-mcp-server',
      query_timeout: 30000,
      statement_timeout: 60000,
      idle_in_transaction_session_timeout: 30000,
    };

    this.pool = new Pool(poolConfig);

    // Enhanced error handling and monitoring
    this.pool.on('error', (err) => {
      console.error('Unexpected error on idle client', err);
    });

    this.pool.on('connect', (client) => {
      console.debug('New client connected, total count:', this.pool.totalCount);
      // Set session-specific optimizations
      client.query('SET statement_timeout = 60000');
      client.query('SET idle_in_transaction_session_timeout = 30000');
      client.query('SET application_name = \'agentforge-mcp-server\'');
    });

    this.pool.on('acquire', () => {
      console.debug('Client acquired from pool, idle count:', this.pool.idleCount);
    });

    this.pool.on('release', () => {
      console.debug('Client released to pool, idle count:', this.pool.idleCount);
    });
  }

  public static getInstance(config?: DatabaseConfig): DatabaseConnection {
    if (!DatabaseConnection.instance && config) {
      DatabaseConnection.instance = new DatabaseConnection(config);
    } else if (!DatabaseConnection.instance) {
      throw new Error('Database connection not initialized. Call with config first.');
    }
    return DatabaseConnection.instance;
  }

  public getPool(): Pool {
    return this.pool;
  }

  public async testConnection(): Promise<boolean> {
    try {
      const client = await this.pool.connect();
      await client.query('SELECT NOW()');
      client.release();
      return true;
    } catch (error) {
      console.error('Database connection test failed:', error);
      return false;
    }
  }

  public async close(): Promise<void> {
    await this.pool.end();
  }

  public getPoolMetrics() {
    return {
      totalCount: this.pool.totalCount,
      idleCount: this.pool.idleCount,
      waitingCount: this.pool.waitingCount,
    };
  }

  public async performanceCheck(): Promise<{
    connectionTime: number;
    queryTime: number;
    poolMetrics: any;
    isHealthy: boolean;
  }> {
    const start = Date.now();
    
    try {
      const client = await this.pool.connect();
      const connectionTime = Date.now() - start;
      
      const queryStart = Date.now();
      await client.query('SELECT 1 as test, NOW() as current_time, version() as db_version');
      const queryTime = Date.now() - queryStart;
      
      client.release();
      
      const poolMetrics = this.getPoolMetrics();
      
      return {
        connectionTime,
        queryTime,
        poolMetrics,
        isHealthy: connectionTime < 5000 && queryTime < 1000,
      };
    } catch (error) {
      console.error('Database performance check failed:', error);
      return {
        connectionTime: -1,
        queryTime: -1,
        poolMetrics: this.getPoolMetrics(),
        isHealthy: false,
      };
    }
  }

  public async getDbStats(): Promise<any> {
    try {
      const client = await this.pool.connect();
      const result = await client.query(`
        SELECT 
          schemaname,
          tablename,
          attname,
          n_distinct,
          correlation
        FROM pg_stats 
        WHERE schemaname = 'public'
        ORDER BY tablename, attname
        LIMIT 50
      `);
      client.release();
      return result.rows;
    } catch (error) {
      console.error('Failed to get database statistics:', error);
      return [];
    }
  }

  public async optimizeDatabase(): Promise<void> {
    try {
      const client = await this.pool.connect();
      
      // Update table statistics
      await client.query('ANALYZE');
      
      // Update all table statistics
      const tables = await client.query(`
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = 'public'
      `);
      
      for (const table of tables.rows) {
        await client.query(`ANALYZE ${table.tablename}`);
      }
      
      client.release();
      console.log('Database optimization completed successfully');
    } catch (error) {
      console.error('Database optimization failed:', error);
      throw error;
    }
  }
}

// Default configuration from environment variables with performance optimizations
export const defaultDatabaseConfig: DatabaseConfig = {
  host: process.env['DB_HOST'] || 'localhost',
  port: parseInt(process.env['DB_PORT'] || '5432'),
  database: process.env['DB_NAME'] || 'agentforge',
  user: process.env['DB_USER'] || 'agentforge',
  password: process.env['DB_PASSWORD'] || 'agentforge',
  ssl: process.env['DB_SSL'] === 'true',
  // Optimized pool configuration
  max: parseInt(process.env['DB_MAX_CONNECTIONS'] || '30'),
  min: parseInt(process.env['DB_MIN_CONNECTIONS'] || '5'),
  idleTimeoutMillis: parseInt(process.env['DB_IDLE_TIMEOUT'] || '300000'),
  connectionTimeoutMillis: parseInt(process.env['DB_CONNECTION_TIMEOUT'] || '10000'),
  acquireTimeoutMillis: parseInt(process.env['DB_ACQUIRE_TIMEOUT'] || '60000'),
  createTimeoutMillis: parseInt(process.env['DB_CREATE_TIMEOUT'] || '30000'),
  destroyTimeoutMillis: parseInt(process.env['DB_DESTROY_TIMEOUT'] || '5000'),
  reapIntervalMillis: parseInt(process.env['DB_REAP_INTERVAL'] || '1000'),
  createRetryIntervalMillis: parseInt(process.env['DB_CREATE_RETRY_INTERVAL'] || '200'),
  propagateCreateError: process.env['DB_PROPAGATE_CREATE_ERROR'] === 'true',
  maxUses: parseInt(process.env['DB_MAX_USES'] || '0'), // 0 = unlimited
  validateOnBorrow: process.env['DB_VALIDATE_ON_BORROW'] !== 'false',
  evictionRunIntervalMillis: parseInt(process.env['DB_EVICTION_RUN_INTERVAL'] || '0'),
  numTestsPerEvictionRun: parseInt(process.env['DB_NUM_TESTS_PER_EVICTION_RUN'] || '3'),
  softIdleTimeoutMillis: parseInt(process.env['DB_SOFT_IDLE_TIMEOUT'] || '0'),
  allowExitOnIdle: process.env['DB_ALLOW_EXIT_ON_IDLE'] === 'true',
};

// Initialize database connection
export const initializeDatabase = (config: DatabaseConfig = defaultDatabaseConfig): DatabaseConnection => {
  return DatabaseConnection.getInstance(config);
};
