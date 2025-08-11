import { EnhancedMCPService } from '../services/enhancedMCPService';
import { ProjectRepository } from '../repositories/ProjectRepository';
import { BaseRepository } from '../repositories/BaseRepository';
import { createLogger } from 'winston';

// Mock repositories
const mockProjectRepository = {
  findById: jest.fn(),
  create: jest.fn(),
  update: jest.fn(),
  delete: jest.fn(),
  findAll: jest.fn()
} as jest.Mocked<ProjectRepository>;

const mockBaseRepository = {
  testConnection: jest.fn(),
  query: jest.fn(),
  transaction: jest.fn()
} as jest.Mocked<BaseRepository>;

// Mock logger
const mockLogger = createLogger({
  level: 'error',
  transports: []
});

describe('EnhancedMCPService', () => {
  let enhancedMCPService: EnhancedMCPService;

  beforeEach(() => {
    jest.clearAllMocks();
    enhancedMCPService = new EnhancedMCPService(
      mockLogger,
      mockProjectRepository,
      mockBaseRepository
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
});
