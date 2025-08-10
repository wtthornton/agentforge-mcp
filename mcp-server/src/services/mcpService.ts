import { Logger } from 'winston';
import * as fs from 'fs';
import * as path from 'path';
import { databaseService } from './databaseService';
import { Project, ProjectStatus } from '../models/Project';

export interface MCPContext {
  projectPath: string;
  projectStructure: any;
  dependencies: any;
  standards: any;
  compliance: any;
  metrics: any;
}

export interface MCPStandards {
  standards: any[];
  categories: string[];
  compliance: any;
}

export interface MCPCompliance {
  violations: any[];
  score: number;
  recommendations: string[];
}

export interface MCPAnalysis {
  projectPath: string;
  structure: any;
  quality: any;
  metrics: any;
}

export interface MCPMetrics {
  performance: any;
  quality: any;
  compliance: any;
  system: any;
}

export class MCPService {
  private logger: Logger;
  private isInitialized: boolean = false;
  private projectPath: string = process.cwd();

  constructor(logger: Logger) {
    this.logger = logger;
  }

  async initialize(): Promise<void> {
    try {
      this.logger.info('Initializing MCP Service...');
      
      // Set project path
      this.projectPath = process.env['PROJECT_PATH'] || process.cwd();
      
      // Verify project path exists
      if (!fs.existsSync(this.projectPath)) {
        throw new Error(`Project path does not exist: ${this.projectPath}`);
      }

      // Check for .agent-os directory
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      if (!fs.existsSync(agentOsPath)) {
        this.logger.warn('.agent-os directory not found, some features may be limited');
      }

      this.isInitialized = true;
      this.logger.info('MCP Service initialized successfully', { projectPath: this.projectPath });
    } catch (error) {
      this.logger.error('Failed to initialize MCP Service:', error);
      throw error;
    }
  }

  isReady(): boolean {
    return this.isInitialized;
  }

  async getContext(params: any): Promise<MCPContext> {
    this.logger.info('Getting project context', { params });
    
    try {
      const projectStructure = await this.analyzeProjectStructure();
      const dependencies = await this.analyzeDependencies();
      const standards = await this.getStandardsData();
      const compliance = await this.getComplianceData();
      const metrics = await this.getMetricsData();

      return {
        projectPath: this.projectPath,
        projectStructure,
        dependencies,
        standards,
        compliance,
        metrics
      };
    } catch (error) {
      this.logger.error('Error getting context:', error);
      throw error;
    }
  }

  async getStandards(params: any): Promise<MCPStandards> {
    this.logger.info('Getting standards', { params });
    
    try {
      const standards = await this.getStandardsData();
      const categories = await this.getStandardsCategories();
      const compliance = await this.getComplianceData();

      return {
        standards,
        categories,
        compliance
      };
    } catch (error) {
      this.logger.error('Error getting standards:', error);
      throw error;
    }
  }

  async validateCompliance(params: any): Promise<MCPCompliance> {
    this.logger.info('Validating compliance', { params });
    
    try {
      const violations = await this.checkComplianceViolations();
      const score = await this.calculateComplianceScore();
      const recommendations = await this.generateComplianceRecommendations();

      return {
        violations,
        score,
        recommendations
      };
    } catch (error) {
      this.logger.error('Error validating compliance:', error);
      throw error;
    }
  }

  async analyzeProject(params: any): Promise<MCPAnalysis> {
    this.logger.info('Analyzing project', { params });
    
    try {
      const structure = await this.analyzeProjectStructure();
      const quality = await this.analyzeCodeQuality();
      const metrics = await this.getMetricsData();

      // Save project analysis to database
      await this.saveProjectToDatabase(structure, quality, metrics);

      return {
        projectPath: this.projectPath,
        structure,
        quality,
        metrics
      };
    } catch (error) {
      this.logger.error('Error analyzing project:', error);
      throw error;
    }
  }

  async saveProjectToDatabase(structure: any, quality: any, metrics: any): Promise<void> {
    try {
      // Initialize database service
      await databaseService.initialize();

      // Check if project already exists
      const existingProject = await databaseService.getProjectByPath(this.projectPath);
      
      if (existingProject) {
        // Update existing project
        await databaseService.updateProject(existingProject.id, {
          totalFiles: structure.totalFiles,
          totalDirectories: structure.totalDirectories,
          totalLinesOfCode: metrics.quality?.totalLines || 0,
          lastAnalyzedAt: new Date(),
          status: ProjectStatus.COMPLETED
        });
        this.logger.info('Project updated in database', { id: existingProject.id });
      } else {
        // Create new project
        const projectData = {
          name: path.basename(this.projectPath),
          description: `Project analyzed at ${new Date().toISOString()}`,
          path: this.projectPath,
          technologyStack: await this.detectTechnologyStack(),
          totalFiles: structure.totalFiles,
          totalDirectories: structure.totalDirectories,
          totalLinesOfCode: metrics.quality?.totalLines || 0
        };
        
        const newProject = await databaseService.createProject(projectData);
        this.logger.info('Project created in database', { id: newProject.id });
      }
    } catch (error) {
      this.logger.error('Failed to save project to database:', error);
      // Don't throw error to avoid breaking the main analysis flow
    }
  }

  private async detectTechnologyStack(): Promise<string[]> {
    const technologies: string[] = [];
    
    try {
      // Check for package.json (Node.js)
      if (fs.existsSync(path.join(this.projectPath, 'package.json'))) {
        technologies.push('Node.js');
        technologies.push('npm');
      }

      // Check for pom.xml (Java/Maven)
      if (fs.existsSync(path.join(this.projectPath, 'pom.xml'))) {
        technologies.push('Java');
        technologies.push('Maven');
      }

      // Check for build.gradle (Java/Gradle)
      if (fs.existsSync(path.join(this.projectPath, 'build.gradle'))) {
        technologies.push('Java');
        technologies.push('Gradle');
      }

      // Check for requirements.txt (Python)
      if (fs.existsSync(path.join(this.projectPath, 'requirements.txt'))) {
        technologies.push('Python');
        technologies.push('pip');
      }

      // Check for Dockerfile
      if (fs.existsSync(path.join(this.projectPath, 'Dockerfile'))) {
        technologies.push('Docker');
      }

      // Check for docker-compose.yml
      if (fs.existsSync(path.join(this.projectPath, 'docker-compose.yml')) || 
          fs.existsSync(path.join(this.projectPath, 'docker-compose.yaml'))) {
        technologies.push('Docker Compose');
      }

      // Check for TypeScript
      if (fs.existsSync(path.join(this.projectPath, 'tsconfig.json'))) {
        technologies.push('TypeScript');
      }

      // Check for React
      if (fs.existsSync(path.join(this.projectPath, 'package.json'))) {
        try {
          const packageJson = JSON.parse(fs.readFileSync(path.join(this.projectPath, 'package.json'), 'utf8'));
          if (packageJson.dependencies?.react || packageJson.devDependencies?.react) {
            technologies.push('React');
          }
        } catch (e) {
          // Ignore JSON parsing errors
        }
      }

      return technologies;
    } catch (error) {
      this.logger.error('Error detecting technology stack:', error);
      return ['Unknown'];
    }
  }

  async getMetrics(params: any): Promise<MCPMetrics> {
    this.logger.info('Getting metrics', { params });
    
    try {
      const performance = await this.getPerformanceMetrics();
      const quality = await this.getQualityMetrics();
      const compliance = await this.getComplianceMetrics();
      const system = await this.getSystemMetrics();

      return {
        performance,
        quality,
        compliance,
        system
      };
    } catch (error) {
      this.logger.error('Error getting metrics:', error);
      throw error;
    }
  }

  // Private helper methods
  private async analyzeProjectStructure(): Promise<any> {
    try {
      const structure: {
        files: string[];
        directories: string[];
        totalFiles: number;
        totalDirectories: number;
      } = {
        files: [],
        directories: [],
        totalFiles: 0,
        totalDirectories: 0
      };

      const scanDirectory = (dirPath: string, relativePath: string = ''): void => {
        const items = fs.readdirSync(dirPath);
        
        for (const item of items) {
          if (item.startsWith('.') && item !== '.agent-os') continue; // Skip hidden files except .agent-os
          
          const fullPath = path.join(dirPath, item);
          const relativeItemPath = path.join(relativePath, item);
          const stats = fs.statSync(fullPath);
          
          if (stats.isDirectory()) {
            structure.directories.push(relativeItemPath);
            structure.totalDirectories++;
            scanDirectory(fullPath, relativeItemPath);
          } else {
            structure.files.push(relativeItemPath);
            structure.totalFiles++;
          }
        }
      };

      scanDirectory(this.projectPath);
      return structure;
    } catch (error) {
      this.logger.error('Error analyzing project structure:', error);
      return { error: 'Failed to analyze project structure' };
    }
  }

  private async analyzeDependencies(): Promise<any> {
    try {
      const dependencies: any = {
        node: null,
        java: null,
        python: null,
        other: [] as string[]
      };

      // Check for package.json (Node.js)
      const packageJsonPath = path.join(this.projectPath, 'package.json');
      if (fs.existsSync(packageJsonPath)) {
        const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
        dependencies.node = {
          name: packageJson.name,
          version: packageJson.version,
          dependencies: packageJson.dependencies || {},
          devDependencies: packageJson.devDependencies || {}
        };
      }

      // Check for pom.xml (Java/Maven)
      const pomXmlPath = path.join(this.projectPath, 'pom.xml');
      if (fs.existsSync(pomXmlPath)) {
        // Basic XML parsing for pom.xml
        const pomContent = fs.readFileSync(pomXmlPath, 'utf8');
        const versionMatch = pomContent.match(/<version>(.*?)<\/version>/);
        const artifactIdMatch = pomContent.match(/<artifactId>(.*?)<\/artifactId>/);
        
        if (artifactIdMatch) {
          dependencies.java = {
            name: artifactIdMatch[1],
            version: versionMatch ? versionMatch[1] : 'unknown',
            type: 'maven'
          };
        }
      }

      return dependencies;
    } catch (error) {
      this.logger.error('Error analyzing dependencies:', error);
      return { error: 'Failed to analyze dependencies' };
    }
  }

  private async getStandardsData(): Promise<any> {
    try {
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      if (!fs.existsSync(agentOsPath)) {
        return { standards: [], message: '.agent-os directory not found' };
      }

      const standards = [];
      const standardsPath = path.join(agentOsPath, 'standards');
      
      if (fs.existsSync(standardsPath)) {
        const items = fs.readdirSync(standardsPath);
        for (const item of items) {
          const itemPath = path.join(standardsPath, item);
          const stats = fs.statSync(itemPath);
          
          if (stats.isDirectory()) {
            standards.push({
              category: item,
              path: itemPath,
              type: 'directory'
            });
          } else if (item.endsWith('.md') || item.endsWith('.yml') || item.endsWith('.yaml')) {
            standards.push({
              category: path.basename(item, path.extname(item)),
              path: itemPath,
              type: 'file',
              extension: path.extname(item)
            });
          }
        }
      }

      return { standards, total: standards.length };
    } catch (error) {
      this.logger.error('Error getting standards data:', error);
      return { standards: [], error: 'Failed to get standards data' };
    }
  }

  private async getStandardsCategories(): Promise<string[]> {
    try {
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      if (!fs.existsSync(agentOsPath)) {
        return [];
      }

      const standardsPath = path.join(agentOsPath, 'standards');
      if (!fs.existsSync(standardsPath)) {
        return [];
      }

      const items = fs.readdirSync(standardsPath);
      return items.filter(item => {
        const itemPath = path.join(standardsPath, item);
        return fs.statSync(itemPath).isDirectory();
      });
    } catch (error) {
      this.logger.error('Error getting standards categories:', error);
      return [];
    }
  }

  private async getComplianceData(): Promise<any> {
    try {
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      if (!fs.existsSync(agentOsPath)) {
        return { compliance: 'unknown', message: '.agent-os directory not found' };
      }

      // Basic compliance check - check if key directories exist
      const requiredDirs = ['standards', 'tools', 'templates'];
      const existingDirs = requiredDirs.filter(dir => 
        fs.existsSync(path.join(agentOsPath, dir))
      );

      const complianceScore = (existingDirs.length / requiredDirs.length) * 100;

      return {
        compliance: complianceScore >= 80 ? 'compliant' : 'non-compliant',
        score: complianceScore,
        requiredDirectories: requiredDirs,
        existingDirectories: existingDirs,
        missingDirectories: requiredDirs.filter(dir => !existingDirs.includes(dir))
      };
    } catch (error) {
      this.logger.error('Error getting compliance data:', error);
      return { compliance: 'unknown', error: 'Failed to get compliance data' };
    }
  }

  private async checkComplianceViolations(): Promise<any[]> {
    try {
      const violations = [];
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      
      if (!fs.existsSync(agentOsPath)) {
        violations.push({
          type: 'critical',
          message: '.agent-os directory not found',
          recommendation: 'Initialize Agent OS project structure'
        });
        return violations;
      }

      // Check for required files
      const requiredFiles = [
        'standards/tech-stack.md',
        'standards/code-style.md',
        'standards/best-practices.md'
      ];

      for (const file of requiredFiles) {
        const filePath = path.join(agentOsPath, file);
        if (!fs.existsSync(filePath)) {
          violations.push({
            type: 'warning',
            message: `Required file not found: ${file}`,
            recommendation: `Create ${file} with appropriate content`
          });
        }
      }

      return violations;
    } catch (error) {
      this.logger.error('Error checking compliance violations:', error);
      return [{ type: 'error', message: 'Failed to check compliance', recommendation: 'Check system logs' }];
    }
  }

  private async calculateComplianceScore(): Promise<number> {
    try {
      const violations = await this.checkComplianceViolations();
      const criticalViolations = violations.filter(v => v.type === 'critical').length;
      const warningViolations = violations.filter(v => v.type === 'warning').length;
      
      // Base score starts at 100, deduct points for violations
      let score = 100;
      score -= criticalViolations * 25; // Critical violations cost 25 points each
      score -= warningViolations * 10;  // Warning violations cost 10 points each
      
      return Math.max(0, score);
    } catch (error) {
      this.logger.error('Error calculating compliance score:', error);
      return 0;
    }
  }

  private async generateComplianceRecommendations(): Promise<string[]> {
    try {
      const recommendations = [];
      const agentOsPath = path.join(this.projectPath, '.agent-os');
      
      if (!fs.existsSync(agentOsPath)) {
        recommendations.push('Initialize Agent OS project structure by running the setup script');
        recommendations.push('Create .agent-os/standards directory with required standards files');
        recommendations.push('Set up compliance checking tools and validation');
        return recommendations;
      }

      // Check for common missing elements
      const toolsPath = path.join(agentOsPath, 'tools');
      if (!fs.existsSync(toolsPath)) {
        recommendations.push('Create .agent-os/tools directory for compliance checking tools');
      }

      const templatesPath = path.join(agentOsPath, 'templates');
      if (!fs.existsSync(templatesPath)) {
        recommendations.push('Create .agent-os/templates directory for project templates');
      }

      if (recommendations.length === 0) {
        recommendations.push('Project appears to be compliant with Agent OS standards');
        recommendations.push('Consider running periodic compliance checks to maintain standards');
      }

      return recommendations;
    } catch (error) {
      this.logger.error('Error generating compliance recommendations:', error);
      return ['Unable to generate recommendations due to system error'];
    }
  }

  private async analyzeCodeQuality(): Promise<any> {
    try {
      // Basic code quality analysis
      const quality = {
        totalLines: 0,
        codeFiles: 0,
        documentationFiles: 0,
        configurationFiles: 0
      };

      const codeExtensions = ['.js', '.ts', '.java', '.py', '.cpp', '.c', '.cs'];
      const docExtensions = ['.md', '.txt', '.rst'];
      const configExtensions = ['.json', '.yml', '.yaml', '.xml', '.toml', '.ini'];

      const analyzeFile = (filePath: string): void => {
        const ext = path.extname(filePath).toLowerCase();
        
        if (codeExtensions.includes(ext)) {
          quality.codeFiles++;
          try {
            const content = fs.readFileSync(filePath, 'utf8');
            quality.totalLines += content.split('\n').length;
          } catch (error) {
            // Skip files that can't be read
          }
        } else if (docExtensions.includes(ext)) {
          quality.documentationFiles++;
        } else if (configExtensions.includes(ext)) {
          quality.configurationFiles++;
        }
      };

      // Recursively analyze files
      const analyzeDirectory = (dirPath: string): void => {
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
      };

      analyzeDirectory(this.projectPath);

      return quality;
    } catch (error) {
      this.logger.error('Error analyzing code quality:', error);
      return { error: 'Failed to analyze code quality' };
    }
  }

  private async getPerformanceMetrics(): Promise<any> {
    try {
      return {
        uptime: process.uptime(),
        memory: process.memoryUsage(),
        cpu: process.cpuUsage(),
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      this.logger.error('Error getting performance metrics:', error);
      return { error: 'Failed to get performance metrics' };
    }
  }

  private async getQualityMetrics(): Promise<any> {
    try {
      const quality = await this.analyzeCodeQuality();
      return {
        ...quality,
        averageLinesPerFile: quality.codeFiles > 0 ? Math.round(quality.totalLines / quality.codeFiles) : 0,
        documentationRatio: quality.codeFiles > 0 ? (quality.documentationFiles / quality.codeFiles) * 100 : 0
      };
    } catch (error) {
      this.logger.error('Error getting quality metrics:', error);
      return { error: 'Failed to get quality metrics' };
    }
  }

  private async getComplianceMetrics(): Promise<any> {
    try {
      const compliance = await this.getComplianceData();
      const violations = await this.checkComplianceViolations();
      
      return {
        ...compliance,
        violationsCount: violations.length,
        criticalViolations: violations.filter(v => v.type === 'critical').length,
        warningViolations: violations.filter(v => v.type === 'warning').length
      };
    } catch (error) {
      this.logger.error('Error getting compliance metrics:', error);
      return { error: 'Failed to get compliance metrics' };
    }
  }

  private async getSystemMetrics(): Promise<any> {
    try {
      return {
        nodeVersion: process.version,
        platform: process.platform,
        architecture: process.arch,
        environment: process.env['NODE_ENV'] || 'development',
        timestamp: new Date().toISOString()
      };
    } catch (error) {
      this.logger.error('Error getting system metrics:', error);
      return { error: 'Failed to get system metrics' };
    }
  }

  // Add missing getMetricsData method
  private async getMetricsData(): Promise<any> {
    try {
      const performance = await this.getPerformanceMetrics();
      const quality = await this.getQualityMetrics();
      const compliance = await this.getComplianceMetrics();
      const system = await this.getSystemMetrics();

      return {
        performance,
        quality,
        compliance,
        system
      };
    } catch (error) {
      this.logger.error('Error getting metrics data:', error);
      return { error: 'Failed to get metrics data' };
    }
  }
}

