import { BaseRepository } from './BaseRepository';
import { Project, ProjectStatus } from '../models/Project';
import { Pool } from 'pg';

export interface ProjectFilters {
  status?: ProjectStatus;
  technologyStack?: string[];
  complianceScoreMin?: number;
  complianceScoreMax?: number;
  createdAfter?: Date;
  createdBefore?: Date;
  lastAnalyzedAfter?: Date;
  lastAnalyzedBefore?: Date;
}

export interface ProjectCreateData {
  name: string;
  description?: string;
  path: string;
  technologyStack: string[];
  totalFiles: number;
  totalDirectories: number;
  totalLinesOfCode: number;
}

export interface ProjectUpdateData {
  name?: string;
  description?: string;
  technologyStack?: string[];
  status?: ProjectStatus;
  complianceScore?: number;
  totalFiles?: number;
  totalDirectories?: number;
  totalLinesOfCode?: number;
  lastAnalyzedAt?: Date;
}

export class ProjectRepository extends BaseRepository<Project> {
  constructor(pool: Pool) {
    super(pool, { tableName: 'projects', primaryKey: 'id' });
  }

  async create(data: ProjectCreateData): Promise<Project> {
    try {
      const query = `
        INSERT INTO ${this.tableName} (
          name, description, path, technology_stack, 
          total_files, total_directories, total_lines_of_code
        ) VALUES ($1, $2, $3, $4, $5, $6, $7)
        RETURNING *
      `;

      const params = [
        data.name,
        data.description,
        data.path,
        data.technologyStack,
        data.totalFiles,
        data.totalDirectories,
        data.totalLinesOfCode
      ];

      const result = await this.queryOne(query, params);
      if (!result) {
        throw new Error('Failed to create project');
      }

      this.logOperation('create', { name: data.name, path: data.path });
      return this.mapRowToProject(result);
    } catch (error) {
      this.handleError(error, 'create');
    }
  }

  async findById(id: string): Promise<Project | null> {
    try {
      const query = `SELECT * FROM ${this.tableName} WHERE id = $1`;
      const result = await this.queryOne(query, [id]);
      return result ? this.mapRowToProject(result) : null;
    } catch (error) {
      this.handleError(error, 'findById');
    }
  }

  async findByPath(path: string): Promise<Project | null> {
    try {
      const query = `SELECT * FROM ${this.tableName} WHERE path = $1`;
      const result = await this.queryOne(query, [path]);
      return result ? this.mapRowToProject(result) : null;
    } catch (error) {
      this.handleError(error, 'findByPath');
    }
  }

  async findAll(filters: ProjectFilters = {}, orderBy?: Record<string, 'ASC' | 'DESC'>, limit?: number, offset?: number): Promise<Project[]> {
    try {
      const { whereClause, params } = this.buildWhereClause(this.mapFiltersToColumns(filters));
      const orderByClause = this.buildOrderByClause(orderBy);
      const paginationClause = this.buildPaginationClause(limit, offset);

      const query = `
        SELECT * FROM ${this.tableName} 
        ${whereClause} 
        ${orderByClause} 
        ${paginationClause}
      `;

      const results = await this.queryMany(query, params);
      return results.map(row => this.mapRowToProject(row));
    } catch (error) {
      this.handleError(error, 'findAll');
    }
  }

  async update(id: string, data: ProjectUpdateData): Promise<Project | null> {
    try {
      const updateFields: string[] = [];
      const params: any[] = [];
      let paramIndex = 1;

      // Build dynamic update query
      if (data.name !== undefined) {
        updateFields.push(`name = $${paramIndex++}`);
        params.push(data.name);
      }
      if (data.description !== undefined) {
        updateFields.push(`description = $${paramIndex++}`);
        params.push(data.description);
      }
      if (data.technologyStack !== undefined) {
        updateFields.push(`technology_stack = $${paramIndex++}`);
        params.push(data.technologyStack);
      }
      if (data.status !== undefined) {
        updateFields.push(`status = $${paramIndex++}`);
        params.push(data.status);
      }
      if (data.complianceScore !== undefined) {
        updateFields.push(`compliance_score = $${paramIndex++}`);
        params.push(data.complianceScore);
      }
      if (data.totalFiles !== undefined) {
        updateFields.push(`total_files = $${paramIndex++}`);
        params.push(data.totalFiles);
      }
      if (data.totalDirectories !== undefined) {
        updateFields.push(`total_directories = $${paramIndex++}`);
        params.push(data.totalDirectories);
      }
      if (data.totalLinesOfCode !== undefined) {
        updateFields.push(`total_lines_of_code = $${paramIndex++}`);
        params.push(data.totalLinesOfCode);
      }
      if (data.lastAnalyzedAt !== undefined) {
        updateFields.push(`last_analyzed_at = $${paramIndex++}`);
        params.push(data.lastAnalyzedAt);
      }

      if (updateFields.length === 0) {
        return this.findById(id);
      }

      // Add updated_at timestamp
      updateFields.push(`updated_at = CURRENT_TIMESTAMP`);

      params.push(id);
      const query = `
        UPDATE ${this.tableName} 
        SET ${updateFields.join(', ')} 
        WHERE id = $${paramIndex}
        RETURNING *
      `;

      const result = await this.queryOne(query, params);
      if (result) {
        this.logOperation('update', { id, updatedFields: Object.keys(data) });
        return this.mapRowToProject(result);
      }
      return null;
    } catch (error) {
      this.handleError(error, 'update');
    }
  }

  async delete(id: string): Promise<boolean> {
    try {
      const query = `DELETE FROM ${this.tableName} WHERE id = $1`;
      const result = await this.execute(query, [id]);
      
      if (result > 0) {
        this.logOperation('delete', { id });
        return true;
      }
      return false;
    } catch (error) {
      this.handleError(error, 'delete');
    }
  }

  async updateStatus(id: string, status: ProjectStatus): Promise<boolean> {
    try {
      const query = `
        UPDATE ${this.tableName} 
        SET status = $1, updated_at = CURRENT_TIMESTAMP 
        WHERE id = $2
      `;
      
      const result = await this.execute(query, [status, id]);
      if (result > 0) {
        this.logOperation('updateStatus', { id, status });
        return true;
      }
      return false;
    } catch (error) {
      this.handleError(error, 'updateStatus');
    }
  }

  async updateComplianceScore(id: string, score: number): Promise<boolean> {
    try {
      const query = `
        UPDATE ${this.tableName} 
        SET compliance_score = $1, updated_at = CURRENT_TIMESTAMP 
        WHERE id = $2
      `;
      
      const result = await this.execute(query, [score, id]);
      if (result > 0) {
        this.logOperation('updateComplianceScore', { id, score });
        return true;
      }
      return false;
    } catch (error) {
      this.handleError(error, 'updateComplianceScore');
    }
  }

  async getProjectStats(): Promise<{
    totalProjects: number;
    projectsByStatus: Record<ProjectStatus, number>;
    averageComplianceScore: number;
    totalFiles: number;
    totalLinesOfCode: number;
  }> {
    try {
      const query = `
        SELECT 
          COUNT(*) as total_projects,
          COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_count,
          COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending_count,
          COUNT(CASE WHEN status = 'analyzing' THEN 1 END) as analyzing_count,
          COUNT(CASE WHEN status = 'failed' THEN 1 END) as failed_count,
          COUNT(CASE WHEN status = 'archived' THEN 1 END) as archived_count,
          AVG(compliance_score) as avg_compliance_score,
          SUM(total_files) as total_files,
          SUM(total_lines_of_code) as total_lines_of_code
        FROM ${this.tableName}
      `;

      const result = await this.queryOne(query);
      if (!result) {
        throw new Error('Failed to get project stats');
      }

      return {
        totalProjects: parseInt(result.total_projects),
        projectsByStatus: {
          [ProjectStatus.COMPLETED]: parseInt(result.completed_count),
          [ProjectStatus.PENDING]: parseInt(result.pending_count),
          [ProjectStatus.ANALYZING]: parseInt(result.analyzing_count),
          [ProjectStatus.FAILED]: parseInt(result.failed_count),
          [ProjectStatus.ARCHIVED]: parseInt(result.archived_count)
        },
        averageComplianceScore: parseFloat(result.avg_compliance_score) || 0,
        totalFiles: parseInt(result.total_files) || 0,
        totalLinesOfCode: parseInt(result.total_lines_of_code) || 0
      };
    } catch (error) {
      this.handleError(error, 'getProjectStats');
    }
  }

  async searchProjects(searchTerm: string, limit: number = 10): Promise<Project[]> {
    try {
      const query = `
        SELECT * FROM ${this.tableName} 
        WHERE 
          name ILIKE $1 OR 
          description ILIKE $1 OR 
          path ILIKE $1 OR
          technology_stack::text ILIKE $1
        ORDER BY 
          CASE WHEN name ILIKE $1 THEN 1 ELSE 2 END,
          created_at DESC
        LIMIT $2
      `;

      const searchPattern = `%${searchTerm}%`;
      const results = await this.queryMany(query, [searchPattern, limit]);
      return results.map(row => this.mapRowToProject(row));
    } catch (error) {
      this.handleError(error, 'searchProjects');
    }
  }

  private mapFiltersToColumns(filters: ProjectFilters): Record<string, any> {
    const mapped: Record<string, any> = {};
    
    if (filters.status !== undefined) mapped.status = filters.status;
    if (filters.technologyStack !== undefined) mapped.technology_stack = filters.technologyStack;
    if (filters.complianceScoreMin !== undefined) mapped.compliance_score_min = filters.complianceScoreMin;
    if (filters.complianceScoreMax !== undefined) mapped.compliance_score_max = filters.complianceScoreMax;
    if (filters.createdAfter !== undefined) mapped.created_after = filters.createdAfter;
    if (filters.createdBefore !== undefined) mapped.created_before = filters.createdBefore;
    if (filters.lastAnalyzedAfter !== undefined) mapped.last_analyzed_after = filters.lastAnalyzedAfter;
    if (filters.lastAnalyzedBefore !== undefined) mapped.last_analyzed_before = filters.lastAnalyzedBefore;

    return mapped;
  }

  private mapRowToProject(row: any): Project {
    return {
      id: row.id,
      name: row.name,
      description: row.description,
      path: row.path,
      technologyStack: row.technology_stack || [],
      createdAt: new Date(row.created_at),
      updatedAt: new Date(row.updated_at),
      lastAnalyzedAt: row.last_analyzed_at ? new Date(row.last_analyzed_at) : undefined,
      status: row.status as ProjectStatus,
      complianceScore: row.compliance_score ? parseFloat(row.compliance_score) : undefined,
      totalFiles: parseInt(row.total_files),
      totalDirectories: parseInt(row.total_directories),
      totalLinesOfCode: parseInt(row.total_lines_of_code)
    };
  }
}
