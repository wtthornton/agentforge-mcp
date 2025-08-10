import { DatabaseConnection, defaultDatabaseConfig } from '../config/database';
import { readFileSync } from 'fs';
import { join } from 'path';
import { logger } from '../utils/logger';

export class DatabaseInitializer {
  private dbConnection: DatabaseConnection;

  constructor() {
    this.dbConnection = DatabaseConnection.getInstance(defaultDatabaseConfig);
  }

  async initialize(): Promise<void> {
    try {
      logger.info('Starting database initialization...');

      // Test connection
      const isConnected = await this.dbConnection.testConnection();
      if (!isConnected) {
        throw new Error('Failed to connect to database');
      }
      logger.info('Database connection successful');

      // Read and execute schema
      await this.createSchema();
      logger.info('Database schema created successfully');

      // Insert initial data
      await this.insertInitialData();
      logger.info('Initial data inserted successfully');

      logger.info('Database initialization completed successfully');
    } catch (error) {
      logger.error('Database initialization failed:', error);
      throw error;
    }
  }

  private async createSchema(): Promise<void> {
    try {
      const schemaPath = join(__dirname, 'schema.sql');
      const schemaSQL = readFileSync(schemaPath, 'utf8');
      
      // Split SQL into individual statements
      const statements = schemaSQL
        .split(';')
        .map(stmt => stmt.trim())
        .filter(stmt => stmt.length > 0 && !stmt.startsWith('--'));

      const pool = this.dbConnection.getPool();
      
      for (const statement of statements) {
        if (statement.trim()) {
          try {
            await pool.query(statement);
            logger.debug(`Executed SQL statement: ${statement.substring(0, 100)}...`);
          } catch (error) {
            // Ignore "already exists" errors for extensions and triggers
            if (!error.message.includes('already exists') && !error.message.includes('duplicate key')) {
              logger.warn(`SQL statement failed: ${statement.substring(0, 100)}...`, error.message);
            }
          }
        }
      }
    } catch (error) {
      logger.error('Failed to create database schema:', error);
      throw error;
    }
  }

  private async insertInitialData(): Promise<void> {
    try {
      const pool = this.dbConnection.getPool();
      
      // Check if initial data already exists
      const existingProject = await pool.query(
        "SELECT id FROM projects WHERE path = '/mcp-server'"
      );

      if (existingProject.rows.length === 0) {
        // Insert initial project data
        await pool.query(`
          INSERT INTO projects (
            name, description, path, technology_stack, 
            status, total_files, total_directories, total_lines_of_code
          ) VALUES (
            'AgentForge MCP Server',
            'MCP Server for AgentForge static analysis and project management',
            '/mcp-server',
            ARRAY['TypeScript', 'Node.js', 'Express', 'PostgreSQL', 'pgvector'],
            'completed',
            15,
            8,
            1200
          )
        `);
        logger.info('Initial project data inserted');
      } else {
        logger.info('Initial project data already exists');
      }

      // Insert sample compliance standards
      const existingStandards = await pool.query(
        "SELECT id FROM project_compliance WHERE standard_id = 'agent-os-core'"
      );

      if (existingStandards.rows.length === 0) {
        await pool.query(`
          INSERT INTO project_compliance (
            project_id, standard_id, standard_name, compliance_level, score, next_check_due
          ) VALUES (
            (SELECT id FROM projects WHERE path = '/mcp-server'),
            'agent-os-core',
            'Agent OS Core Standards',
            'compliant',
            95.5,
            CURRENT_TIMESTAMP + INTERVAL '30 days'
          )
        `);
        logger.info('Sample compliance data inserted');
      } else {
        logger.info('Sample compliance data already exists');
      }

    } catch (error) {
      logger.error('Failed to insert initial data:', error);
      throw error;
    }
  }

  async validateSchema(): Promise<{
    isValid: boolean;
    errors: string[];
    warnings: string[];
  }> {
    const errors: string[] = [];
    const warnings: string[] = [];

    try {
      const pool = this.dbConnection.getPool();
      
      // Check required tables exist
      const requiredTables = [
        'projects', 'project_analyses', 'project_compliance', 
        'compliance_violations', 'project_files', 'project_directories',
        'technology_stack_analysis', 'audit_logs', 'code_embeddings'
      ];

      for (const table of requiredTables) {
        try {
          const result = await pool.query(
            "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = $1)",
            [table]
          );
          
          if (!result.rows[0].exists) {
            errors.push(`Required table '${table}' does not exist`);
          }
        } catch (error) {
          errors.push(`Failed to check table '${table}': ${error.message}`);
        }
      }

      // Check pgvector extension
      try {
        const result = await pool.query(
          "SELECT EXISTS (SELECT FROM pg_extension WHERE extname = 'vector')"
        );
        
        if (!result.rows[0].exists) {
          warnings.push("pgvector extension not found - vector search functionality will be limited");
        }
      } catch (error) {
        warnings.push(`Failed to check pgvector extension: ${error.message}`);
      }

      // Check indexes
      try {
        const result = await pool.query(`
          SELECT indexname FROM pg_indexes 
          WHERE tablename = 'projects' AND indexname LIKE 'idx_%'
        `);
        
        if (result.rows.length < 4) {
          warnings.push('Some recommended indexes may be missing on projects table');
        }
      } catch (error) {
        warnings.push(`Failed to check indexes: ${error.message}`);
      }

      // Check initial data
      try {
        const result = await pool.query("SELECT COUNT(*) FROM projects");
        const projectCount = parseInt(result.rows[0].count);
        
        if (projectCount === 0) {
          warnings.push('No projects found in database');
        }
      } catch (error) {
        errors.push(`Failed to check initial data: ${error.message}`);
      }

    } catch (error) {
      errors.push(`Schema validation failed: ${error.message}`);
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings
    };
  }

  async cleanup(): Promise<void> {
    try {
      await this.dbConnection.close();
      logger.info('Database connection closed');
    } catch (error) {
      logger.error('Failed to close database connection:', error);
    }
  }
}

// CLI usage
if (require.main === module) {
  const initializer = new DatabaseInitializer();
  
  initializer.initialize()
    .then(() => initializer.validateSchema())
    .then((validation) => {
      console.log('Schema validation result:', validation);
      if (validation.isValid) {
        console.log('✅ Database initialization successful');
        process.exit(0);
      } else {
        console.log('❌ Database initialization failed');
        process.exit(1);
      }
    })
    .catch((error) => {
      console.error('❌ Database initialization failed:', error);
      process.exit(1);
    })
    .finally(() => {
      initializer.cleanup();
    });
}
