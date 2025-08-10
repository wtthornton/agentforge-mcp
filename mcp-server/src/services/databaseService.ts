import { DatabaseConnection, defaultDatabaseConfig } from '../config/database';
import { ProjectRepository } from '../repositories/ProjectRepository';
import { Project, ProjectStatus } from '../models/Project';
import { logger } from '../utils/logger';

export class DatabaseService {
  private dbConnection: DatabaseConnection;
  private projectRepository: ProjectRepository;
  private isInitialized: boolean = false;

  constructor() {
    this.dbConnection = DatabaseConnection.getInstance(defaultDatabaseConfig);
    this.projectRepository = new ProjectRepository(this.dbConnection.getPool());
  }

  async initialize(): Promise<void> {
    try {
      if (this.isInitialized) {
        return;
      }

      logger.info('Initializing database service...');
      
      // Test database connection
      const isConnected = await this.dbConnection.testConnection();
      if (!isConnected) {
        throw new Error('Database connection failed');
      }

      logger.info('Database service initialized successfully');
      this.isInitialized = true;
    } catch (error) {
      logger.error('Failed to initialize database service:', error);
      throw error;
    }
  }

  async getProjectRepository(): Promise<ProjectRepository> {
    if (!this.isInitialized) {
      await this.initialize();
    }
    return this.projectRepository;
  }

  async createProject(projectData: {
    name: string;
    description?: string;
    path: string;
    technologyStack: string[];
    totalFiles: number;
    totalDirectories: number;
    totalLinesOfCode: number;
  }): Promise<Project> {
    try {
      const repository = await this.getProjectRepository();
      const project = await repository.create(projectData);
      logger.info(`Project created: ${project.name} (${project.id})`);
      return project;
    } catch (error) {
      logger.error('Failed to create project:', error);
      throw error;
    }
  }

  async getProjectById(id: string): Promise<Project | null> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.findById(id);
    } catch (error) {
      logger.error(`Failed to get project by ID ${id}:`, error);
      throw error;
    }
  }

  async getProjectByPath(path: string): Promise<Project | null> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.findByPath(path);
    } catch (error) {
      logger.error(`Failed to get project by path ${path}:`, error);
      throw error;
    }
  }

  async getAllProjects(limit?: number, offset?: number): Promise<Project[]> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.findAll({}, { createdAt: 'DESC' }, limit, offset);
    } catch (error) {
      logger.error('Failed to get all projects:', error);
      throw error;
    }
  }

  async updateProject(id: string, updateData: {
    name?: string;
    description?: string;
    technologyStack?: string[];
    status?: ProjectStatus;
    complianceScore?: number;
    totalFiles?: number;
    totalDirectories?: number;
    totalLinesOfCode?: number;
    lastAnalyzedAt?: Date;
  }): Promise<Project | null> {
    try {
      const repository = await this.getProjectRepository();
      const project = await repository.update(id, updateData);
      if (project) {
        logger.info(`Project updated: ${project.name} (${id})`);
      }
      return project;
    } catch (error) {
      logger.error(`Failed to update project ${id}:`, error);
      throw error;
    }
  }

  async updateProjectStatus(id: string, status: ProjectStatus): Promise<boolean> {
    try {
      const repository = await this.getProjectRepository();
      const success = await repository.updateStatus(id, status);
      if (success) {
        logger.info(`Project status updated: ${id} -> ${status}`);
      }
      return success;
    } catch (error) {
      logger.error(`Failed to update project status ${id}:`, error);
      throw error;
    }
  }

  async updateProjectComplianceScore(id: string, score: number): Promise<boolean> {
    try {
      const repository = await this.getProjectRepository();
      const success = await repository.updateComplianceScore(id, score);
      if (success) {
        logger.info(`Project compliance score updated: ${id} -> ${score}`);
      }
      return success;
    } catch (error) {
      logger.error(`Failed to update project compliance score ${id}:`, error);
      throw error;
    }
  }

  async deleteProject(id: string): Promise<boolean> {
    try {
      const repository = await this.getProjectRepository();
      const success = await repository.delete(id);
      if (success) {
        logger.info(`Project deleted: ${id}`);
      }
      return success;
    } catch (error) {
      logger.error(`Failed to delete project ${id}:`, error);
      throw error;
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
      const repository = await this.getProjectRepository();
      return await repository.getProjectStats();
    } catch (error) {
      logger.error('Failed to get project stats:', error);
      throw error;
    }
  }

  async searchProjects(searchTerm: string, limit: number = 10): Promise<Project[]> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.searchProjects(searchTerm, limit);
    } catch (error) {
      logger.error(`Failed to search projects with term "${searchTerm}":`, error);
      throw error;
    }
  }

  async getProjectsByStatus(status: ProjectStatus, limit?: number, offset?: number): Promise<Project[]> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.findAll({ status }, { createdAt: 'DESC' }, limit, offset);
    } catch (error) {
      logger.error(`Failed to get projects by status ${status}:`, error);
      throw error;
    }
  }

  async getProjectsByTechnology(technology: string, limit?: number, offset?: number): Promise<Project[]> {
    try {
      const repository = await this.getProjectRepository();
      return await repository.findAll({ technologyStack: [technology] }, { createdAt: 'DESC' }, limit, offset);
    } catch (error) {
      logger.error(`Failed to get projects by technology ${technology}:`, error);
      throw error;
    }
  }

  async getComplianceReport(): Promise<{
    compliantProjects: number;
    nonCompliantProjects: number;
    averageScore: number;
    topProjects: Project[];
    needsAttention: Project[];
  }> {
    try {
      const stats = await this.getProjectStats();
      const repository = await this.getProjectRepository();
      
      // Get top 5 projects by compliance score
      const topProjects = await repository.findAll(
        { complianceScoreMin: 80 },
        { complianceScore: 'DESC' },
        5
      );

      // Get projects that need attention (score < 70)
      const needsAttention = await repository.findAll(
        { complianceScoreMax: 70 },
        { complianceScore: 'ASC' },
        10
      );

      return {
        compliantProjects: stats.projectsByStatus[ProjectStatus.COMPLETED],
        nonCompliantProjects: stats.projectsByStatus[ProjectStatus.FAILED],
        averageScore: stats.averageComplianceScore,
        topProjects,
        needsAttention
      };
    } catch (error) {
      logger.error('Failed to get compliance report:', error);
      throw error;
    }
  }

  async healthCheck(): Promise<{
    status: 'healthy' | 'unhealthy';
    database: boolean;
    message: string;
  }> {
    try {
      const isConnected = await this.dbConnection.testConnection();
      
      if (!isConnected) {
        return {
          status: 'unhealthy',
          database: false,
          message: 'Database connection failed'
        };
      }

      // Test basic repository operations
      const repository = await this.getProjectRepository();
      const stats = await repository.getProjectStats();

      return {
        status: 'healthy',
        database: true,
        message: `Database healthy - ${stats.totalProjects} projects found`
      };
    } catch (error) {
      logger.error('Health check failed:', error);
      return {
        status: 'unhealthy',
        database: false,
        message: `Health check failed: ${error.message}`
      };
    }
  }

  async close(): Promise<void> {
    try {
      await this.dbConnection.close();
      this.isInitialized = false;
      logger.info('Database service closed');
    } catch (error) {
      logger.error('Failed to close database service:', error);
    }
  }
}

// Export singleton instance
export const databaseService = new DatabaseService();
