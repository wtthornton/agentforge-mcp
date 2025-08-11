import { EnhancedMCPService, MCPRequestContext } from '../services/enhancedMCPService';
import { DatabaseService } from '../services/databaseService';
import { ProjectRepository } from '../repositories/ProjectRepository';
import { BaseRepository } from '../repositories/BaseRepository';
import { createLogger } from 'winston';

// Mock the database service
const mockDatabaseService = {
  initialize: jest.fn(),
  analyzeProject: jest.fn(),
  getMetrics: jest.fn()
} as jest.Mocked<DatabaseService>;

// Mock the project repository
const mockProjectRepository = {
  findById: jest.fn(),
  create: jest.fn(),
  update: jest.fn(),
  delete: jest.fn(),
  findAll: jest.fn(),
  findByPath: jest.fn(),
  updateStatus: jest.fn(),
  updateComplianceScore: jest.fn(),
  getProjectStats: jest.fn(),
  searchProjects: jest.fn()
} as jest.Mocked<ProjectRepository>;

// Mock the base repository
const mockBaseRepository = {
  findById: jest.fn(),
  create: jest.fn(),
  update: jest.fn(),
  delete: jest.fn(),
  findAll: jest.fn()
} as jest.Mocked<BaseRepository<any>>;

// Mock logger
const mockLogger = {
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
  debug: jest.fn()
} as any;

describe('EnhancedMCPService', () => {
  let enhancedMCPService: EnhancedMCPService;

  beforeEach(() => {
    jest.clearAllMocks();
    
    // Mock the database service methods
    mockDatabaseService.analyzeProject.mockResolvedValue({
      projectPath: process.cwd(),
      structure: { totalFiles: 100, totalDirectories: 20 },
      technologyStack: { primary: ['Node.js'], frameworks: ['Express'] },
      codeQuality: { totalLines: 5000, codeFiles: 50 },
      performance: { system: { uptime: 1000 } },
      compliance: { score: 85 },
      recommendations: ['Add tests', 'Improve documentation']
    });

    mockDatabaseService.getMetrics.mockResolvedValue({
      timestamp: new Date().toISOString(),
      project: { path: process.cwd() },
      system: { uptime: 1000, platform: 'win32', nodeVersion: '18.0.0' },
      analysis: {},
      performance: {}
    });

    enhancedMCPService = new EnhancedMCPService(
      mockLogger,
      mockDatabaseService
    );
  });

  describe('Method Registration', () => {
    it('should register all core MCP methods on initialization', () => {
      const methods = enhancedMCPService.getAvailableMethods();
      const methodNames = methods.map(m => m.method);
      
      expect(methodNames).toContain('getContext');
      expect(methodNames).toContain('getStandards');
      expect(methodNames).toContain('validateCompliance');
      expect(methodNames).toContain('analyzeProject');
      expect(methodNames).toContain('getMetrics');
      expect(methodNames).toContain('batchProcess');
      expect(methodNames).toContain('getPerformanceStats');
      expect(methodNames).toContain('clearCache');
      expect(methodNames).toContain('healthCheck');
    });

    it('should allow registering new methods', () => {
      const customMethod = {
        method: 'customMethod',
        handler: jest.fn(),
        description: 'A custom method',
        requiredParams: ['param1'],
        optionalParams: ['param2']
      };

      enhancedMCPService.registerMethod(customMethod);
      const methods = enhancedMCPService.getAvailableMethods();
      const customMethodFound = methods.find(m => m.method === 'customMethod');
      
      expect(customMethodFound).toBeDefined();
      expect(customMethodFound?.description).toBe('A custom method');
    });
  });

  describe('Request Processing', () => {
    const mockContext = {
      requestId: 'test-123',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should process valid MCP requests successfully', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.jsonrpc).toBe('2.0');
      expect(result.id).toBe('req-1');
      expect(result.result).toBeDefined();
      expect(result.error).toBeUndefined();
    });

    it('should return error for non-existent methods', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'nonExistentMethod',
        params: {}
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.jsonrpc).toBe('2.0');
      expect(result.id).toBe('req-1');
      expect(result.error).toBeDefined();
      expect(result.error?.code).toBe(-32603);
      expect(result.error?.message).toBe('Internal error');
    });

    it('should validate required parameters', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getContext',
        params: {} // Missing required projectId
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.error).toBeDefined();
      expect(result.error?.data).toContain('Required parameter');
    });

    it('should handle method execution errors gracefully', async () => {
      mockProjectRepository.findById.mockRejectedValue(new Error('Database error'));
      
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getContext',
        params: { projectId: 'test-id' }
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.error).toBeDefined();
      expect(result.error?.code).toBe(-32603);
      expect(result.error?.data).toBe('Database error');
    });
  });

  describe('Batch Processing', () => {
    const mockContext = {
      requestId: 'batch-123',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should process batch requests successfully', async () => {
      const requests = [
        {
          jsonrpc: '2.0',
          id: 'req-1',
          method: 'getStandards',
          params: {}
        },
        {
          jsonrpc: '2.0',
          id: 'req-2',
          method: 'getMetrics',
          params: {}
        }
      ];

      const results = await enhancedMCPService.processBatchRequest(requests, mockContext);
      
      expect(results).toHaveLength(2);
      expect(results[0].id).toBe('req-1');
      expect(results[1].id).toBe('req-2');
      expect(results[0].error).toBeUndefined();
      expect(results[1].error).toBeUndefined();
    });

    it('should handle errors in batch requests', async () => {
      const requests = [
        {
          jsonrpc: '2.0',
          id: 'req-1',
          method: 'getStandards',
          params: {}
        },
        {
          jsonrpc: '2.0',
          id: 'req-2',
          method: 'nonExistentMethod',
          params: {}
        }
      ];

      const results = await enhancedMCPService.processBatchRequest(requests, mockContext);
      
      expect(results).toHaveLength(2);
      expect(results[0].error).toBeUndefined();
      expect(results[1].error).toBeDefined();
    });

    it('should respect batch size limits', async () => {
      const largeBatch = Array.from({ length: 150 }, (_, i) => ({
        jsonrpc: '2.0',
        id: `req-${i}`,
        method: 'getStandards',
        params: {}
      }));

      await expect(
        enhancedMCPService.processBatchRequest(largeBatch, mockContext)
      ).rejects.toThrow('Batch size cannot exceed 100 requests');
    });
  });

  describe('Caching', () => {
    const mockContext = {
      requestId: 'cache-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should cache results for cacheable methods', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      };

      // First request - should not be cached
      const result1 = await enhancedMCPService.processRequest(request, mockContext);
      expect(result1.cached).toBeUndefined();

      // Second request - should be cached
      const result2 = await enhancedMCPService.processRequest(request, mockContext);
      expect(result2.cached).toBe(true);
    });

    it('should not cache results for non-cacheable methods', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'validateCompliance',
        params: { projectId: 'test-id' }
      };

      const result1 = await enhancedMCPService.processRequest(request, mockContext);
      const result2 = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result1.cached).toBeUndefined();
      expect(result2.cached).toBeUndefined();
    });
  });

  describe('Performance Monitoring', () => {
    const mockContext = {
      requestId: 'perf-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should track performance metrics for methods', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      };

      await enhancedMCPService.processRequest(request, mockContext);
      
      const stats = await enhancedMCPService.processRequest({
        jsonrpc: '2.0',
        id: 'stats-1',
        method: 'getPerformanceStats',
        params: { method: 'getStandards' }
      }, mockContext);

      expect(stats.result).toBeDefined();
      expect(stats.result.totalRequests).toBeGreaterThan(0);
      expect(stats.result.successRate).toBe(100);
    });

    it('should calculate average response times', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      };

      // Make multiple requests to get meaningful averages
      for (let i = 0; i < 3; i++) {
        await enhancedMCPService.processRequest(request, mockContext);
      }

      const stats = await enhancedMCPService.processRequest({
        jsonrpc: '2.0',
        id: 'stats-1',
        method: 'getPerformanceStats',
        params: { method: 'getStandards' }
      }, mockContext);

      expect(stats.result.averageDuration).toBeGreaterThan(0);
      expect(stats.result.totalRequests).toBe(3);
    });
  });

  describe('Rate Limiting', () => {
    const mockContext = {
      requestId: 'rate-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should enforce rate limits for methods with limits', async () => {
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'batchProcess',
        params: { requests: [] }
      };

      // batchProcess has a rate limit of 10 requests per minute
      const promises = Array.from({ length: 12 }, (_, i) => 
        enhancedMCPService.processRequest({
          ...request,
          id: `req-${i}`
        }, mockContext)
      );

      const results = await Promise.all(promises);
      const errors = results.filter(r => r.error);
      
      // Should have some rate limit errors
      expect(errors.length).toBeGreaterThan(0);
      expect(errors.some(r => r.error?.data?.includes('Rate limit exceeded'))).toBe(true);
    });
  });

  describe('Health Check', () => {
    const mockContext = {
      requestId: 'health-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should return health status', async () => {
      mockBaseRepository.testConnection.mockResolvedValue(true);
      
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'healthCheck',
        params: {}
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.result.status).toBe('healthy');
      expect(result.result.services.database).toBe('healthy');
      expect(result.result.services.cache).toBeDefined();
      expect(result.result.services.performance).toBeDefined();
    });

    it('should return detailed health information when requested', async () => {
      mockBaseRepository.testConnection.mockResolvedValue(true);
      
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'healthCheck',
        params: { detailed: true }
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.result.details).toBeDefined();
      expect(result.result.details.methodHandlers).toBeGreaterThan(0);
      expect(result.result.details.cacheSize).toBeDefined();
      expect(result.result.details.performanceMetrics).toBeDefined();
    });
  });

  describe('Cache Management', () => {
    const mockContext = {
      requestId: 'cache-mgmt-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should clear cache for specific methods', async () => {
      // First, populate cache
      await enhancedMCPService.processRequest({
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      }, mockContext);

      // Clear cache for getStandards method
      const clearRequest = {
        jsonrpc: '2.0',
        id: 'clear-1',
        method: 'clearCache',
        params: { method: 'getStandards' }
      };

      const result = await enhancedMCPService.processRequest(clearRequest, mockContext);
      
      expect(result.result.cleared).toBeGreaterThan(0);
      expect(result.result.method).toBe('getStandards');
    });

    it('should clear all cache when no parameters specified', async () => {
      // Populate cache with multiple methods
      await enhancedMCPService.processRequest({
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getStandards',
        params: {}
      }, mockContext);

      await enhancedMCPService.processRequest({
        jsonrpc: '2.0',
        id: 'req-2',
        method: 'getMetrics',
        params: {}
      }, mockContext);

      // Clear all cache
      const clearRequest = {
        jsonrpc: '2.0',
        id: 'clear-all',
        method: 'clearCache',
        params: {}
      };

      const result = await enhancedMCPService.processRequest(clearRequest, mockContext);
      
      expect(result.result.cleared).toBeGreaterThan(0);
      expect(result.result.message).toContain('all');
    });
  });

  describe('Error Handling', () => {
    const mockContext = {
      requestId: 'error-test',
      timestamp: new Date().toISOString(),
      ip: '127.0.0.1'
    };

    it('should handle database connection failures gracefully', async () => {
      mockBaseRepository.testConnection.mockRejectedValue(new Error('Connection failed'));
      
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'healthCheck',
        params: {}
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.result.services.database).toBe('unhealthy');
    });

    it('should handle invalid project IDs gracefully', async () => {
      mockProjectRepository.findById.mockResolvedValue(null);
      
      const request = {
        jsonrpc: '2.0',
        id: 'req-1',
        method: 'getContext',
        params: { projectId: 'invalid-id' }
      };

      const result = await enhancedMCPService.processRequest(request, mockContext);
      
      expect(result.error).toBeDefined();
      expect(result.error?.data).toContain('not found');
    });
  });

  describe('Task 2.2: Advanced Project Analysis Capabilities', () => {
    it('should analyze project structure with enhanced capabilities', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.1',
        timestamp: new Date().toISOString(),
        method: 'analyzeProject',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd(),
        enhanced: true
      };

      const result = await enhancedMCPService.analyzeProject(mockParams, mockContext);
      
      expect(result).toBeDefined();
      expect(result.projectPath).toBe(process.cwd());
      expect(result.structure).toBeDefined();
      expect(result.technologyStack).toBeDefined();
      expect(result.codeQuality).toBeDefined();
      expect(result.performance).toBeDefined();
      expect(result.compliance).toBeDefined();
      expect(result.recommendations).toBeDefined();
      expect(Array.isArray(result.recommendations)).toBe(true);
    });

    it('should detect advanced technology stack', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.2',
        timestamp: new Date().toISOString(),
        method: 'analyzeProject',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd(),
        enhanced: true
      };

      const result = await enhancedMCPService.analyzeProject(mockParams, mockContext);
      
      expect(result.technologyStack.primary).toBeDefined();
      expect(result.technologyStack.frameworks).toBeDefined();
      expect(result.technologyStack.buildTools).toBeDefined();
      expect(result.technologyStack.databases).toBeDefined();
      expect(result.technologyStack.testing).toBeDefined();
      expect(result.technologyStack.deployment).toBeDefined();
      expect(result.technologyStack.monitoring).toBeDefined();
      expect(result.technologyStack.details).toBeDefined();
    });

    it('should analyze advanced code quality metrics', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.3',
        timestamp: new Date().toISOString(),
        method: 'analyzeProject',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd(),
        enhanced: true
      };

      const result = await enhancedMCPService.analyzeProject(mockParams, mockContext);
      
      expect(result.codeQuality.totalLines).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.codeFiles).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.documentationFiles).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.configurationFiles).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.testFiles).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.buildFiles).toBeGreaterThanOrEqual(0);
      expect(result.codeQuality.complexityMetrics).toBeDefined();
      expect(result.codeQuality.codeStyle).toBeDefined();
      expect(result.codeQuality.security).toBeDefined();
    });

    it('should provide performance analysis', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.4',
        timestamp: new Date().toISOString(),
        method: 'analyzeProject',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd(),
        enhanced: true
      };

      const result = await enhancedMCPService.analyzeProject(mockParams, mockContext);
      
      expect(result.performance.system).toBeDefined();
      expect(result.performance.project).toBeDefined();
      expect(result.performance.analysis).toBeDefined();
      expect(result.performance.system.uptime).toBeGreaterThan(0);
      expect(result.performance.system.memory).toBeDefined();
      expect(result.performance.system.cpu).toBeDefined();
    });

    it('should generate actionable recommendations', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.5',
        timestamp: new Date().toISOString(),
        method: 'analyzeProject',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd(),
        enhanced: true
      };

      const result = await enhancedMCPService.analyzeProject(mockParams, mockContext);
      
      expect(result.recommendations).toBeDefined();
      expect(Array.isArray(result.recommendations)).toBe(true);
      
      // Recommendations should be actionable
      result.recommendations.forEach((recommendation: string) => {
        expect(typeof recommendation).toBe('string');
        expect(recommendation.length).toBeGreaterThan(0);
        expect(recommendation).toMatch(/^[A-Z]/); // Should start with capital letter
      });
    });

    it('should provide enhanced metrics', async () => {
      const mockContext: MCPRequestContext = {
        requestId: 'test-2.2.6',
        timestamp: new Date().toISOString(),
        method: 'getMetrics',
        metadata: {
          priority: 'normal',
          retryCount: 0
        },
        priority: 'normal',
        retryCount: 0
      };

      const mockParams = {
        path: process.cwd()
      };

      const result = await enhancedMCPService.getMetrics(mockParams, mockContext);
      
      expect(result).toBeDefined();
      expect(result.timestamp).toBeDefined();
      expect(result.project).toBeDefined();
      expect(result.system).toBeDefined();
      expect(result.analysis).toBeDefined();
      expect(result.performance).toBeDefined();
      expect(result.system.uptime).toBeGreaterThan(0);
      expect(result.system.platform).toBeDefined();
      expect(result.system.nodeVersion).toBeDefined();
    });
  });
});
