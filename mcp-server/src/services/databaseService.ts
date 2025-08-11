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
      const allProjects = await this.getAllProjects();
      const compliantProjects = allProjects.filter(p => (p.complianceScore || 0) >= 85);
      const nonCompliantProjects = allProjects.filter(p => (p.complianceScore || 0) < 85);
      const averageScore = allProjects.reduce((sum, p) => sum + (p.complianceScore || 0), 0) / allProjects.length;
      
      const topProjects = allProjects
        .sort((a, b) => (b.complianceScore || 0) - (a.complianceScore || 0))
        .slice(0, 5);
      
      const needsAttention = allProjects
        .filter(p => (p.complianceScore || 0) < 70)
        .sort((a, b) => (a.complianceScore || 0) - (b.complianceScore || 0))
        .slice(0, 5);

      return {
        compliantProjects: compliantProjects.length,
        nonCompliantProjects: nonCompliantProjects.length,
        averageScore: Math.round(averageScore * 100) / 100,
        topProjects,
        needsAttention
      };
    } catch (error) {
      logger.error('Failed to get compliance report:', error);
      throw error;
    }
  }

  // Task 2.2: Advanced Project Analysis Capabilities
  async analyzeProject(params: any): Promise<any> {
    try {
      logger.info('Starting advanced project analysis', { params });
      
      const projectPath = params.path || params.projectPath || process.cwd();
      const analysis = {
        projectPath,
        timestamp: new Date().toISOString(),
        structure: await this.analyzeProjectStructure(projectPath),
        technologyStack: await this.detectAdvancedTechnologyStack(projectPath),
        codeQuality: await this.analyzeAdvancedCodeQuality(projectPath),
        performance: await this.analyzePerformanceMetrics(projectPath),
        compliance: await this.analyzeComplianceMetrics(projectPath),
        recommendations: []
      };

      // Generate recommendations based on analysis
      analysis.recommendations = await this.generateAnalysisRecommendations(analysis);
      
      logger.info('Advanced project analysis completed', { 
        projectPath, 
        totalFiles: analysis.structure.totalFiles,
        technologies: analysis.technologyStack.primary.length
      });
      
      return analysis;
    } catch (error) {
      logger.error('Failed to analyze project:', error);
      throw error;
    }
  }

  private async analyzeProjectStructure(projectPath: string): Promise<any> {
    try {
      const fs = require('fs');
      const path = require('path');
      
      const structure = {
        files: [] as string[],
        directories: [] as string[],
        totalFiles: 0,
        totalDirectories: 0,
        fileTypes: {} as Record<string, number>,
        directoryDepth: 0,
        largestFiles: [] as Array<{path: string, size: number, lines: number}>,
        emptyDirectories: [] as string[]
      };

      const scanDirectory = (dirPath: string, relativePath: string = '', depth: number = 0): void => {
        structure.directoryDepth = Math.max(structure.directoryDepth, depth);
        
        try {
          const items = fs.readdirSync(dirPath);
          
          for (const item of items) {
            if (item.startsWith('.') && item !== '.agent-os') continue;
            
            const fullPath = path.join(dirPath, item);
            const relativeItemPath = path.join(relativePath, item);
            const stats = fs.statSync(fullPath);
            
            if (stats.isDirectory()) {
              structure.directories.push(relativeItemPath);
              structure.totalDirectories++;
              
              // Check if directory is empty
              try {
                const dirItems = fs.readdirSync(fullPath);
                if (dirItems.length === 0) {
                  structure.emptyDirectories.push(relativeItemPath);
                }
              } catch (error) {
                // Directory might not be accessible
              }
              
              scanDirectory(fullPath, relativeItemPath, depth + 1);
            } else {
              structure.files.push(relativeItemPath);
              structure.totalFiles++;
              
              // Count file types
              const ext = path.extname(item).toLowerCase();
              structure.fileTypes[ext] = (structure.fileTypes[ext] || 0) + 1;
              
              // Track largest files
              try {
                const content = fs.readFileSync(fullPath, 'utf8');
                const lines = content.split('\n').length;
                const size = stats.size;
                
                structure.largestFiles.push({ path: relativeItemPath, size, lines });
                structure.largestFiles.sort((a, b) => b.size - a.size);
                if (structure.largestFiles.length > 10) {
                  structure.largestFiles = structure.largestFiles.slice(0, 10);
                }
              } catch (error) {
                // File might not be readable
              }
            }
          }
        } catch (error) {
          logger.warn(`Cannot scan directory: ${dirPath}`, error);
        }
      };

      scanDirectory(projectPath);
      return structure;
    } catch (error) {
      logger.error('Error analyzing project structure:', error);
      return { error: 'Failed to analyze project structure' };
    }
  }

  private async detectAdvancedTechnologyStack(projectPath: string): Promise<any> {
    try {
      const fs = require('fs');
      const path = require('path');
      
      const technologies = {
        primary: [] as string[],
        frameworks: [] as string[],
        buildTools: [] as string[],
        databases: [] as string[],
        testing: [] as string[],
        deployment: [] as string[],
        monitoring: [] as string[],
        details: {} as Record<string, any>
      };

      // Node.js ecosystem
      const packageJsonPath = path.join(projectPath, 'package.json');
      if (fs.existsSync(packageJsonPath)) {
        try {
          const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
          technologies.primary.push('Node.js');
          
          if (packageJson.dependencies) {
            Object.keys(packageJson.dependencies).forEach((dep: string) => {
              if (dep.includes('react')) technologies.frameworks.push('React');
              if (dep.includes('vue')) technologies.frameworks.push('Vue.js');
              if (dep.includes('angular')) technologies.frameworks.push('Angular');
              if (dep.includes('express')) technologies.frameworks.push('Express.js');
              if (dep.includes('next')) technologies.frameworks.push('Next.js');
              if (dep.includes('jest')) technologies.testing.push('Jest');
              if (dep.includes('mocha')) technologies.testing.push('Mocha');
              if (dep.includes('cypress')) technologies.testing.push('Cypress');
              if (dep.includes('postgres')) technologies.databases.push('PostgreSQL');
              if (dep.includes('mysql')) technologies.databases.push('MySQL');
              if (dep.includes('mongodb')) technologies.databases.push('MongoDB');
              if (dep.includes('redis')) technologies.databases.push('Redis');
            });
          }
          
          technologies.details['nodejs'] = {
            name: packageJson.name,
            version: packageJson.version,
            scripts: packageJson.scripts || {},
            engines: packageJson.engines || {}
          };
        } catch (error) {
          logger.warn('Failed to parse package.json:', error);
        }
      }

      // Java ecosystem
      const pomXmlPath = path.join(projectPath, 'pom.xml');
      if (fs.existsSync(pomXmlPath)) {
        try {
          const pomContent = fs.readFileSync(pomXmlPath, 'utf8');
          technologies.primary.push('Java');
          technologies.buildTools.push('Maven');
          
          // Parse basic Maven info
          const versionMatch = pomContent.match(/<version>(.*?)<\/version>/);
          const artifactIdMatch = pomContent.match(/<artifactId>(.*?)<\/artifactId>/);
          const groupIdMatch = pomContent.match(/<groupId>(.*?)<\/groupId>/);
          
          if (artifactIdMatch) {
            technologies.details['java'] = {
              name: artifactIdMatch[1],
              version: versionMatch ? versionMatch[1] : 'unknown',
              groupId: groupIdMatch ? groupIdMatch[1] : 'unknown',
              type: 'maven'
            };
          }
          
          // Detect Spring Boot
          if (pomContent.includes('spring-boot-starter')) {
            technologies.frameworks.push('Spring Boot');
          }
          
          // Detect testing frameworks
          if (pomContent.includes('junit')) technologies.testing.push('JUnit');
          if (pomContent.includes('mockito')) technologies.testing.push('Mockito');
        } catch (error) {
          logger.warn('Failed to parse pom.xml:', error);
        }
      }

      // Python ecosystem
      const requirementsPath = path.join(projectPath, 'requirements.txt');
      if (fs.existsSync(requirementsPath)) {
        try {
          technologies.primary.push('Python');
          const requirements = fs.readFileSync(requirementsPath, 'utf8').split('\n');
          
          requirements.forEach((req: string) => {
            const packageName = req.split('==')[0].split('>=')[0].split('<=')[0].toLowerCase();
            if (packageName.includes('django')) technologies.frameworks.push('Django');
            if (packageName.includes('flask')) technologies.frameworks.push('Flask');
            if (packageName.includes('fastapi')) technologies.frameworks.push('FastAPI');
            if (packageName.includes('pytest')) technologies.testing.push('pytest');
            if (packageName.includes('postgres')) technologies.databases.push('PostgreSQL');
            if (packageName.includes('mysql')) technologies.databases.push('MySQL');
          });
        } catch (error) {
          logger.warn('Failed to parse requirements.txt:', error);
        }
      }

      // Docker and containerization
      if (fs.existsSync(path.join(projectPath, 'Dockerfile'))) {
        technologies.deployment.push('Docker');
      }
      if (fs.existsSync(path.join(projectPath, 'docker-compose.yml')) || 
          fs.existsSync(path.join(projectPath, 'docker-compose.yaml'))) {
        technologies.deployment.push('Docker Compose');
      }

      // CI/CD
      if (fs.existsSync(path.join(projectPath, '.github'))) {
        technologies.deployment.push('GitHub Actions');
      }
      if (fs.existsSync(path.join(projectPath, '.gitlab-ci.yml'))) {
        technologies.deployment.push('GitLab CI');
      }
      if (fs.existsSync(path.join(projectPath, '.travis.yml'))) {
        technologies.deployment.push('Travis CI');
      }

      // Monitoring and observability
      if (fs.existsSync(path.join(projectPath, 'prometheus.yml')) || 
          fs.existsSync(path.join(projectPath, 'grafana'))) {
        technologies.monitoring.push('Prometheus');
        technologies.monitoring.push('Grafana');
      }

      return technologies;
    } catch (error) {
      logger.error('Error detecting technology stack:', error);
      return { error: 'Failed to detect technology stack' };
    }
  }

  private async analyzeAdvancedCodeQuality(projectPath: string): Promise<any> {
    try {
      const fs = require('fs');
      const path = require('path');
      
      const quality = {
        totalLines: 0,
        codeFiles: 0,
        documentationFiles: 0,
        configurationFiles: 0,
        testFiles: 0,
        buildFiles: 0,
        averageLinesPerFile: 0,
        documentationRatio: 0,
        testCoverage: 0,
        complexityMetrics: {
          cyclomaticComplexity: 0,
          averageComplexity: 0,
          highComplexityFiles: [] as string[]
        },
        codeStyle: {
          hasLinting: false,
          hasFormatting: false,
          lintingTools: [] as string[]
        },
        security: {
          hasSecurityScanning: false,
          securityTools: [] as string[]
        }
      };

      const codeExtensions = ['.js', '.ts', '.jsx', '.tsx', '.java', '.py', '.cpp', '.c', '.cs', '.go', '.rs', '.php'];
      const docExtensions = ['.md', '.txt', '.rst', '.adoc'];
      const configExtensions = ['.json', '.yml', '.yaml', '.xml', '.toml', '.ini', '.env'];
      const testExtensions = ['.test.js', '.test.ts', '.spec.js', '.spec.ts', '.test.java', '.py', '.cpp', '.c'];
      const buildExtensions = ['.gradle', '.xml', '.lock', '.sh', '.bat', '.ps1'];

      const analyzeFile = (filePath: string): void => {
        const ext = path.extname(filePath).toLowerCase();
        const fileName = path.basename(filePath).toLowerCase();
        
        if (codeExtensions.includes(ext)) {
          quality.codeFiles++;
          try {
            const content = fs.readFileSync(filePath, 'utf8');
            const lines = content.split('\n').length;
            quality.totalLines += lines;
            
            // Basic complexity analysis (count control structures)
            const complexity = (content.match(/if|else|for|while|switch|case|catch/g) || []).length;
            if (complexity > 10) {
              quality.complexityMetrics.highComplexityFiles.push(filePath);
            }
            quality.complexityMetrics.cyclomaticComplexity += complexity;
          } catch (error) {
            // Skip files that can't be read
          }
        } else if (docExtensions.includes(ext)) {
          quality.documentationFiles++;
        } else if (configExtensions.includes(ext)) {
          quality.configurationFiles++;
        } else if (testExtensions.some(testExt => fileName.includes(testExt) || fileName.includes('test') || fileName.includes('spec'))) {
          quality.testFiles++;
        } else if (buildExtensions.includes(ext) || fileName.includes('build') || fileName.includes('gradle') || fileName.includes('maven')) {
          quality.buildFiles++;
        }
      };

      // Check for code quality tools
      const checkCodeQualityTools = (dirPath: string): void => {
        const qualityFiles = [
          '.eslintrc', '.eslintrc.js', '.eslintrc.json',
          '.prettierrc', '.prettierrc.js', '.prettierrc.json',
          '.stylelintrc', '.stylelintrc.js', '.stylelintrc.json',
          'tsconfig.json', 'tslint.json',
          'pylint.rc', 'flake8', 'black.toml',
          'checkstyle.xml', 'spotbugs.xml'
        ];

        qualityFiles.forEach(file => {
          if (fs.existsSync(path.join(dirPath, file))) {
            quality.codeStyle.hasLinting = true;
            if (file.includes('eslint')) quality.codeStyle.lintingTools.push('ESLint');
            if (file.includes('prettier')) quality.codeStyle.hasFormatting = true;
            if (file.includes('stylelint')) quality.codeStyle.lintingTools.push('Stylelint');
            if (file.includes('tslint')) quality.codeStyle.lintingTools.push('TSLint');
            if (file.includes('pylint')) quality.codeStyle.lintingTools.push('Pylint');
            if (file.includes('flake8')) quality.codeStyle.lintingTools.push('Flake8');
            if (file.includes('black')) quality.codeStyle.hasFormatting = true;
            if (file.includes('checkstyle')) quality.codeStyle.lintingTools.push('Checkstyle');
            if (file.includes('spotbugs')) quality.codeStyle.lintingTools.push('SpotBugs');
          }
        });

        // Check for security tools
        const securityFiles = [
          'snyk', '.snyk', 'bandit', 'safety', 'npm-audit', 'yarn-audit',
          'owasp-dependency-check', 'trivy', 'grype'
        ];

        securityFiles.forEach(file => {
          if (fs.existsSync(path.join(dirPath, file))) {
            quality.security.hasSecurityScanning = true;
            quality.security.securityTools.push(file);
          }
        });
      };

      // Recursively analyze files
      const analyzeDirectory = (dirPath: string): void => {
        try {
          const items = fs.readdirSync(dirPath);
          
          for (const item of items) {
            if (item.startsWith('.') && item !== '.agent-os') continue;
            
            const fullPath = path.join(dirPath, item);
            const stats = fs.statSync(fullPath);
            
            if (stats.isDirectory()) {
              analyzeDirectory(fullPath);
            } else {
              analyzeFile(fullPath);
            }
          }
        } catch (error) {
          // Directory might not be accessible
        }
      };

      analyzeDirectory(projectPath);
      checkCodeQualityTools(projectPath);

      // Calculate derived metrics
      quality.averageLinesPerFile = quality.codeFiles > 0 ? Math.round(quality.totalLines / quality.codeFiles) : 0;
      quality.documentationRatio = quality.codeFiles > 0 ? Math.round((quality.documentationFiles / quality.codeFiles) * 100) : 0;
      quality.testCoverage = quality.codeFiles > 0 ? Math.round((quality.testFiles / quality.codeFiles) * 100) : 0;
      quality.complexityMetrics.averageComplexity = quality.codeFiles > 0 ? Math.round(quality.complexityMetrics.cyclomaticComplexity / quality.codeFiles) : 0;

      return quality;
    } catch (error) {
      logger.error('Error analyzing code quality:', error);
      return { error: 'Failed to analyze code quality' };
    }
  }

  private async analyzePerformanceMetrics(projectPath: string): Promise<any> {
    try {
      const fs = require('fs');
      const path = require('path');
      
      const performance = {
        system: {
          uptime: process.uptime(),
          memory: process.memoryUsage(),
          cpu: process.cpuUsage(),
          timestamp: new Date().toISOString()
        },
        project: {
          totalSize: 0,
          averageFileSize: 0,
          largestFile: { path: '', size: 0 },
          fileSizeDistribution: {} as Record<string, number>,
          buildTime: 0,
          dependenciesCount: 0
        },
        analysis: {
          scanTime: 0,
          processingTime: 0,
          memoryUsage: 0
        }
      };

      // Calculate project size metrics
      const calculateProjectSize = (dirPath: string): number => {
        let totalSize = 0;
        
        try {
          const items = fs.readdirSync(dirPath);
          
          for (const item of items) {
            if (item.startsWith('.') && item !== '.agent-os') continue;
            
            const fullPath = path.join(dirPath, item);
            const stats = fs.statSync(fullPath);
            
            if (stats.isDirectory()) {
              totalSize += calculateProjectSize(fullPath);
            } else {
              totalSize += stats.size;
              
              // Track file size distribution
              const sizeInKB = Math.floor(stats.size / 1024);
              const sizeCategory = sizeInKB < 1 ? '<1KB' : 
                                 sizeInKB < 10 ? '1-10KB' : 
                                 sizeInKB < 100 ? '10-100KB' : 
                                 sizeInKB < 1000 ? '100KB-1MB' : '>1MB';
              performance.project.fileSizeDistribution[sizeCategory] = (performance.project.fileSizeDistribution[sizeCategory] || 0) + 1;
              
              // Track largest file
              if (stats.size > performance.project.largestFile.size) {
                performance.project.largestFile = { path: fullPath, size: stats.size };
              }
            }
          }
        } catch (error) {
          // Directory might not be accessible
        }
        
        return totalSize;
      };

      performance.project.totalSize = calculateProjectSize(projectPath);
      
      // Count dependencies
      const dependencyFiles = ['package.json', 'pom.xml', 'requirements.txt', 'build.gradle', 'Cargo.toml', 'go.mod'];
      dependencyFiles.forEach(file => {
        if (fs.existsSync(path.join(projectPath, file))) {
          performance.project.dependenciesCount++;
        }
      });

      return performance;
    } catch (error) {
      logger.error('Error analyzing performance metrics:', error);
      return { error: 'Failed to analyze performance metrics' };
    }
  }

  private async analyzeComplianceMetrics(projectPath: string): Promise<any> {
    try {
      const fs = require('fs');
      const path = require('path');
      
      const compliance = {
        standards: {
          hasAgentOS: false,
          hasStandards: false,
          standardsCount: 0,
          categories: [] as string[]
        },
        violations: [] as string[],
        score: 100,
        recommendations: [] as string[]
      };

      // Check for Agent OS standards
      const agentOsPath = path.join(projectPath, '.agent-os');
      if (fs.existsSync(agentOsPath)) {
        compliance.standards.hasAgentOS = true;
        
        const standardsPath = path.join(agentOsPath, 'standards');
        if (fs.existsSync(standardsPath)) {
          compliance.standards.hasStandards = true;
          
          try {
            const items = fs.readdirSync(standardsPath);
            compliance.standards.standardsCount = items.length;
            compliance.standards.categories = items.filter(item => 
              fs.statSync(path.join(standardsPath, item)).isDirectory()
            );
          } catch (error) {
            logger.warn('Cannot read standards directory:', error);
          }
        }
      } else {
        compliance.violations.push('Missing .agent-os directory');
        compliance.score -= 20;
        compliance.recommendations.push('Initialize Agent OS standards directory');
      }

      // Check for essential project files
      const essentialFiles = ['README.md', '.gitignore', 'LICENSE'];
      essentialFiles.forEach((file: string) => {
        if (!fs.existsSync(path.join(projectPath, file))) {
          compliance.violations.push(`Missing ${file}`);
          compliance.score -= 5;
          compliance.recommendations.push(`Create ${file} file`);
        }
      });

      // Check for testing setup
      const testDirs = ['tests', 'test', '__tests__', 'spec'];
      const hasTests = testDirs.some((dir: string) => fs.existsSync(path.join(projectPath, dir)));
      if (!hasTests) {
        compliance.violations.push('No test directory found');
        compliance.score -= 15;
        compliance.recommendations.push('Set up testing framework and test directory');
      }

      // Check for documentation
      const docDirs = ['docs', 'documentation', 'wiki'];
      const hasDocs = docDirs.some((dir: string) => fs.existsSync(path.join(projectPath, dir)));
      if (!hasDocs) {
        compliance.violations.push('No documentation directory found');
        compliance.score -= 10;
        compliance.recommendations.push('Create documentation directory');
      }

      // Ensure score doesn't go below 0
      compliance.score = Math.max(0, compliance.score);

      return compliance;
    } catch (error) {
      logger.error('Error analyzing compliance metrics:', error);
      return { error: 'Failed to analyze compliance metrics' };
    }
  }

  private async generateAnalysisRecommendations(analysis: any): Promise<string[]> {
    const recommendations: string[] = [];

    // Structure recommendations
    if (analysis.structure.emptyDirectories && analysis.structure.emptyDirectories.length > 0) {
      recommendations.push('Consider removing empty directories to clean up project structure');
    }
    if (analysis.structure.directoryDepth > 5) {
      recommendations.push('Project has deep directory nesting - consider flattening structure for better maintainability');
    }

    // Technology stack recommendations
    if (analysis.technologyStack.frameworks.length === 0) {
      recommendations.push('No major framework detected - consider using a framework for better development experience');
    }
    if (analysis.technologyStack.testing.length === 0) {
      recommendations.push('No testing framework detected - implement testing for better code quality');
    }

    // Code quality recommendations
    if (!analysis.codeQuality.codeStyle.hasLinting) {
      recommendations.push('Implement linting tools to maintain code quality and consistency');
    }
    if (!analysis.codeQuality.codeStyle.hasFormatting) {
      recommendations.push('Add code formatting tools (Prettier, Black, etc.) for consistent code style');
    }
    if (analysis.codeQuality.complexityMetrics.averageComplexity > 5) {
      recommendations.push('High cyclomatic complexity detected - consider refactoring complex methods');
    }
    if (analysis.codeQuality.documentationRatio < 20) {
      recommendations.push('Low documentation ratio - increase documentation coverage');
    }

    // Performance recommendations
    if (analysis.performance.project.largestFile.size > 1024 * 1024) {
      recommendations.push('Large files detected - consider splitting large files for better maintainability');
    }

    // Compliance recommendations
    if (analysis.compliance.score < 80) {
      recommendations.push('Low compliance score - review and address identified violations');
    }

    return recommendations;
  }

  // Task 2.2: Enhanced metrics collection
  async getMetrics(params: any): Promise<any> {
    try {
      logger.info('Getting enhanced metrics', { params });
      
      const projectPath = params.path || params.projectPath || process.cwd();
      
      // Get comprehensive metrics
      const metrics = {
        timestamp: new Date().toISOString(),
        project: {
          path: projectPath,
          stats: await this.getProjectStats(),
          compliance: await this.getComplianceReport()
        },
        system: {
          uptime: process.uptime(),
          memory: process.memoryUsage(),
          cpu: process.cpuUsage(),
          platform: process.platform,
          nodeVersion: process.version
        },
        analysis: {
          lastAnalysis: null,
          analysisCount: 0,
          averageAnalysisTime: 0
        },
        performance: {
          databaseConnections: 0,
          cacheHitRate: 0,
          averageResponseTime: 0
        }
      };

      // Get project-specific metrics if available
      try {
        const project = await this.getProjectByPath(projectPath);
        if (project) {
          metrics.project.stats = {
            ...metrics.project.stats,
            currentProject: {
              id: project.id,
              name: project.name,
              status: project.status,
              complianceScore: project.complianceScore,
              lastAnalyzedAt: project.lastAnalyzedAt
            }
          };
        }
      } catch (error) {
        logger.warn('Could not get project-specific metrics:', error);
      }

      return metrics;
    } catch (error) {
      logger.error('Failed to get metrics:', error);
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
