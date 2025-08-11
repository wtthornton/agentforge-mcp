import { Request, Response, Router } from 'express';
import { MCPService } from '../services/mcpService';
import { Logger } from 'winston';

// Enhanced MCP Protocol interfaces
export interface MCPRequest {
  jsonrpc: string;
  id: string | number;
  method: string;
  params?: any;
  metadata?: {
    version?: string;
    timestamp?: string;
    source?: string;
    priority?: 'low' | 'normal' | 'high' | 'critical';
    retryCount?: number;
    correlationId?: string;
  };
}

export interface MCPBatchRequest {
  jsonrpc: string;
  id: string | number;
  method: string;
  params?: any;
  metadata?: {
    version?: string;
    timestamp?: string;
    source?: string;
    priority?: 'low' | 'normal' | 'high' | 'critical';
    retryCount?: number;
    correlationId?: string;
  };
}

export interface MCPResponse {
  jsonrpc: string;
  id: string | number;
  result?: any;
  error?: {
    code: number;
    message: string;
    data?: any;
    retryable?: boolean;
    suggestedAction?: string;
  };
  metadata?: {
    timestamp: string;
    processingTime: number;
    cacheHit?: boolean;
    version: string;
  };
}

export interface MCPBatchResponse {
  jsonrpc: string;
  id: string | number;
  result?: any;
  error?: {
    code: number;
    message: string;
    data?: any;
    retryable?: boolean;
    suggestedAction?: string;
  };
  metadata?: {
    timestamp: string;
    processingTime: number;
    cacheHit?: boolean;
    version: string;
    batchIndex?: number;
  };
}

// Enhanced MCP protocol version support
export interface MCPProtocolVersion {
  version: string;
  supported: boolean;
  deprecated?: boolean;
  features: string[];
  limitations: string[];
  migrationGuide?: string;
}

export class MCPController {
  public router: Router;
  private mcpService: MCPService;
  private logger: Logger;
  private supportedVersions: MCPProtocolVersion[] = [
    {
      version: '2.0',
      supported: true,
      deprecated: false,
      features: ['batch-requests', 'metadata', 'priority-queuing', 'retry-mechanisms'],
      limitations: []
    },
    {
      version: '1.0',
      supported: false,
      deprecated: true,
      features: ['basic-requests'],
      limitations: ['no-batch-support', 'no-metadata', 'no-priority-queuing'],
      migrationGuide: 'Upgrade to version 2.0 for enhanced features'
    }
  ];
  private maxBatchSize: number = 100;
  private maxConcurrentBatches: number = 10;
  private activeBatches: Map<string, { startTime: number; requestCount: number }> = new Map();

  constructor(mcpService: MCPService, logger: Logger) {
    this.mcpService = mcpService;
    this.logger = logger;
    this.router = Router();
    this.initializeRoutes();
  }

  private initializeRoutes(): void {
    // Enhanced MCP Protocol endpoints with advanced features
    this.router.post('/context', this.handleMCPRequest.bind(this, 'getContext'));
    this.router.post('/standards', this.handleMCPRequest.bind(this, 'getStandards'));
    this.router.post('/compliance', this.handleMCPRequest.bind(this, 'validateCompliance'));
    this.router.post('/analyze', this.handleMCPRequest.bind(this, 'analyzeProject'));
    this.router.post('/metrics', this.handleMCPRequest.bind(this, 'getMetrics'));
    
    // Enhanced batch endpoint with priority and correlation support
    this.router.post('/batch', this.handleBatchRequest.bind(this));
    
    // Advanced MCP Protocol endpoints
    this.router.get('/protocol', this.getProtocolInfo.bind(this));
    this.router.get('/capabilities', this.getCapabilities.bind(this));
    this.router.get('/versions', this.getSupportedVersions.bind(this));
    this.router.get('/health/advanced', this.getAdvancedHealth.bind(this));
    this.router.post('/validate', this.validateMCPMessage.bind(this));
    
    // New advanced endpoints
    this.router.post('/priority', this.handlePriorityRequest.bind(this));
    this.router.post('/retry', this.handleRetryRequest.bind(this));
    this.router.get('/stats/batch', this.getBatchStats.bind(this));
  }

  // Enhanced MCP message handling with advanced validation and processing
  private async handleMCPRequest(method: string, req: Request, res: Response): Promise<void> {
    const startTime = Date.now();
    const requestId = req.body.id || 'unknown';
    
    try {
      this.logger.info(`${method} called`, { 
        body: req.body,
        requestId,
        ip: req.ip,
        userAgent: req.headers['user-agent']
      });
      
      // Enhanced MCP request validation with metadata support
      const validationResult = this.validateEnhancedMCPRequest(req.body);
      if (!validationResult.isValid) {
        this.logger.warn(`Invalid MCP request for ${method}:`, validationResult.errors);
        res.status(400).json(this.createEnhancedErrorResponse(
          requestId, 
          -32600, 
          'Invalid Request', 
          validationResult.errors,
          validationResult.warnings
        ));
        return;
      }

      // Check protocol version compatibility with enhanced support
      const versionInfo = this.getVersionInfo(req.body.jsonrpc);
      if (!versionInfo.supported) {
        this.logger.warn(`Unsupported MCP version: ${req.body.jsonrpc}`, { versionInfo });
        res.status(400).json(this.createEnhancedErrorResponse(
          requestId, 
          -32600, 
          'Invalid Request', 
          `Unsupported JSON-RPC version: ${req.body.jsonrpc}`,
          versionInfo.deprecated ? `Version ${req.body.jsonrpc} is deprecated. ${versionInfo.migrationGuide}` : undefined
        ));
        return;
      }

      // Process the request with enhanced context
      const context = this.createRequestContext(req, method);
      const result = await this.processEnhancedMCPRequest(method, req.body.params || {}, context);
      
      const processingTime = Date.now() - startTime;
      res.json(this.createEnhancedSuccessResponse(requestId, result, processingTime, versionInfo.version));
      
    } catch (error) {
      const processingTime = Date.now() - startTime;
      this.logger.error(`${method} error:`, error);
      res.status(500).json(this.createEnhancedErrorResponse(
        requestId, 
        -32603, 
        'Internal error', 
        error instanceof Error ? error.message : 'Unknown error',
        undefined,
        processingTime,
        true // retryable
      ));
    }
  }

  // Enhanced batch request handling with priority and correlation support
  private async handleBatchRequest(req: Request, res: Response): Promise<void> {
    const startTime = Date.now();
    const batchId = `batch_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
      // Check concurrent batch limits
      if (this.activeBatches.size >= this.maxConcurrentBatches) {
        this.logger.warn('Maximum concurrent batches reached', { 
          activeBatches: this.activeBatches.size,
          maxConcurrent: this.maxConcurrentBatches 
        });
        res.status(429).json(this.createEnhancedErrorResponse(
          'batch_limit_exceeded',
          -32000,
          'Too many concurrent batches',
          'Maximum concurrent batch processing limit reached. Please try again later.',
          undefined,
          Date.now() - startTime,
          true // retryable
        ));
        return;
      }

      // Validate batch request structure
      if (!Array.isArray(req.body)) {
        res.status(400).json(this.createEnhancedErrorResponse(
          'batch_invalid',
          -32600,
          'Invalid batch request',
          'Batch request must be an array of MCP requests'
        ));
        return;
      }

      // Enhanced batch size validation
      if (req.body.length > this.maxBatchSize) {
        res.status(400).json(this.createEnhancedErrorResponse(
          'batch_too_large',
          -32600,
          'Batch too large',
          `Batch size ${req.body.length} exceeds maximum limit of ${this.maxBatchSize}`
        ));
        return;
      }

      // Track batch processing
      this.activeBatches.set(batchId, { 
        startTime: Date.now(), 
        requestCount: req.body.length 
      });

      // Enhanced batch validation with priority sorting
      const batchValidationResults = req.body.map((request: any, index: number) => ({
        index,
        request,
        validation: this.validateEnhancedMCPRequest(request),
        priority: this.extractPriority(request)
      }));

      const invalidRequests = batchValidationResults.filter(r => !r.validation.isValid);
      if (invalidRequests.length > 0) {
        this.activeBatches.delete(batchId);
        this.logger.warn('Invalid requests in batch', { 
          invalidCount: invalidRequests.length,
          totalCount: req.body.length,
          errors: invalidRequests.map(r => ({ index: r.index, errors: r.validation.errors }))
        });
        res.status(400).json(this.createEnhancedErrorResponse(
          'batch_validation_failed',
          -32600,
          'Invalid batch request',
          `One or more requests in batch are invalid: ${invalidRequests.map(r => `Request ${r.index}: ${r.validation.errors.join(', ')}`).join('; ')}`
        ));
        return;
      }

      // Sort requests by priority for optimal processing
      const sortedRequests = batchValidationResults
        .sort((a, b) => this.comparePriority(a.priority, b.priority))
        .map(r => r.request);

      // Process batch with enhanced error handling
      const results = await this.processEnhancedBatchRequest(sortedRequests, batchId);
      
      const processingTime = Date.now() - startTime;
      this.activeBatches.delete(batchId);
      
      res.json({
        jsonrpc: '2.0',
        id: batchId,
        result: results,
        metadata: {
          timestamp: new Date().toISOString(),
          processingTime,
          batchSize: req.body.length,
          version: '2.0',
          batchId
        }
      });

    } catch (error) {
      this.activeBatches.delete(batchId);
      const processingTime = Date.now() - startTime;
      this.logger.error('Batch request error:', error);
      res.status(500).json(this.createEnhancedErrorResponse(
        batchId, 
        -32603, 
        'Internal error', 
        error instanceof Error ? error.message : 'Unknown error',
        undefined,
        processingTime,
        true // retryable
      ));
    }
  }

  // Enhanced MCP request validation with metadata support
  private validateEnhancedMCPRequest(request: any): { isValid: boolean; errors: string[]; warnings?: string[] } {
    const errors: string[] = [];
    const warnings: string[] = [];

    // Basic field validation
    if (!request.jsonrpc) {
      errors.push('Missing jsonrpc field');
    } else if (typeof request.jsonrpc !== 'string') {
      errors.push('jsonrpc field must be a string');
    }

    if (!request.id) {
      errors.push('Missing id field');
    } else if (typeof request.id !== 'string' && typeof request.id !== 'number') {
      errors.push('id field must be a string or number');
    }

    if (!request.method) {
      errors.push('Missing method field');
    } else if (typeof request.method !== 'string') {
      errors.push('method field must be a string');
    } else if (request.method.trim() === '') {
      errors.push('method field cannot be empty');
    }

    // Enhanced metadata validation
    if (request.metadata) {
      if (typeof request.metadata !== 'object' || request.metadata === null) {
        errors.push('metadata field must be an object');
      } else {
        // Validate metadata fields
        if (request.metadata.priority && !['low', 'normal', 'high', 'critical'].includes(request.metadata.priority)) {
          errors.push('metadata.priority must be one of: low, normal, high, critical');
        }
        
        if (request.metadata.retryCount !== undefined) {
          if (typeof request.metadata.retryCount !== 'number' || request.metadata.retryCount < 0) {
            errors.push('metadata.retryCount must be a non-negative number');
          } else if (request.metadata.retryCount > 5) {
            warnings.push('metadata.retryCount exceeds recommended maximum of 5');
          }
        }

        if (request.metadata.correlationId && typeof request.metadata.correlationId !== 'string') {
          errors.push('metadata.correlationId must be a string');
        }
      }
    }

    // Params validation
    if (request.params !== undefined) {
      if (typeof request.params !== 'object' || request.params === null) {
        errors.push('params field must be an object or undefined');
      }
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings: warnings.length > 0 ? warnings : undefined
    };
  }

  // Enhanced version compatibility checking
  private getVersionInfo(version: string): MCPProtocolVersion {
    const versionInfo = this.supportedVersions.find(v => v.version === version);
    if (versionInfo) {
      return versionInfo;
    }
    
    // Return default unsupported version info
    return {
      version,
      supported: false,
      deprecated: false,
      features: [],
      limitations: ['Version not supported', 'No feature information available']
    };
  }

  // Enhanced request context creation
  private createRequestContext(req: Request, method: string): any {
    return {
      requestId: req.body.id || 'unknown',
      timestamp: new Date().toISOString(),
      ip: req.ip,
      userAgent: req.headers['user-agent'],
      method,
      metadata: req.body.metadata || {},
      correlationId: req.body.metadata?.correlationId,
      priority: req.body.metadata?.priority || 'normal',
      retryCount: req.body.metadata?.retryCount || 0
    };
  }

  // Enhanced MCP request processing
  private async processEnhancedMCPRequest(method: string, params: any, context: any): Promise<any> {
    // Add context-aware processing
    if (context.priority === 'critical') {
      this.logger.info('Processing critical priority request', { method, context });
    }

    if (context.retryCount > 0) {
      this.logger.info('Processing retry request', { method, retryCount: context.retryCount, context });
    }

    // Process the request using the existing service
    const result = await this.mcpService.processRequest({ method, params }, context);
    
    // Add context metadata to result
    return {
      ...result,
      _context: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId
      }
    };
  }

  // Enhanced batch request processing
  private async processEnhancedBatchRequest(requests: any[], batchId: string): Promise<any[]> {
    const results = [];
    
    for (let i = 0; i < requests.length; i++) {
      const request = requests[i];
      const startTime = Date.now();
      
      try {
        const context = this.createRequestContext({ body: request } as Request, request.method);
        const result = await this.processEnhancedMCPRequest(request.method, request.params || {}, context);
        
        results.push({
          jsonrpc: '2.0',
          id: request.id,
          result,
          metadata: {
            timestamp: new Date().toISOString(),
            processingTime: Date.now() - startTime,
            version: '2.0',
            batchIndex: i
          }
        });
      } catch (error) {
        this.logger.error(`Error processing batch request ${i}:`, error);
        results.push({
          jsonrpc: '2.0',
          id: request.id,
          error: {
            code: -32603,
            message: 'Internal error',
            data: error instanceof Error ? error.message : 'Unknown error',
            retryable: true,
            suggestedAction: 'Retry the request'
          },
          metadata: {
            timestamp: new Date().toISOString(),
            processingTime: Date.now() - startTime,
            version: '2.0',
            batchIndex: i
          }
        });
      }
    }
    
    return results;
  }

  // Priority extraction and comparison
  private extractPriority(request: any): string {
    return request.metadata?.priority || 'normal';
  }

  private comparePriority(a: string, b: string): number {
    const priorityOrder = { 'critical': 4, 'high': 3, 'normal': 2, 'low': 1 };
    return (priorityOrder[b as keyof typeof priorityOrder] || 0) - (priorityOrder[a as keyof typeof priorityOrder] || 0);
  }

  // Enhanced response creation methods
  private createEnhancedSuccessResponse(id: string | number, result: any, processingTime: number, version: string): MCPResponse {
    return {
      jsonrpc: '2.0',
      id,
      result,
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime,
        version
      }
    };
  }

  private createEnhancedErrorResponse(
    id: string | number | null, 
    code: number, 
    message: string, 
    data?: any, 
    warnings?: string[], 
    processingTime?: number,
    retryable?: boolean
  ): MCPResponse {
    const response: MCPResponse = {
      jsonrpc: '2.0',
      id: id || 'unknown',
      error: {
        code,
        message,
        data,
        retryable
      },
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: processingTime || 0,
        version: '2.0'
      }
    };

    if (warnings && warnings.length > 0) {
      response.error!.suggestedAction = `Consider addressing these warnings: ${warnings.join(', ')}`;
    }

    return response;
  }

  // Enhanced protocol information endpoint
  private getProtocolInfo(_req: Request, res: Response): void {
    const protocolInfo = {
      name: 'AgentForge MCP Server',
      version: '2.0.0',
      description: 'Enhanced MCP server with advanced features',
      capabilities: [
        'batch-requests',
        'priority-queuing',
        'retry-mechanisms',
        'metadata-support',
        'protocol-versioning',
        'advanced-validation',
        'performance-monitoring',
        'correlation-tracking'
      ],
      supportedVersions: this.supportedVersions,
      maxBatchSize: this.maxBatchSize,
      maxConcurrentBatches: this.maxConcurrentBatches,
      timestamp: new Date().toISOString()
    };

    res.json({
      jsonrpc: '2.0',
      id: 'protocol_info',
      result: protocolInfo,
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  // Enhanced capabilities endpoint
  private getCapabilities(_req: Request, res: Response): void {
    const capabilities = {
      methods: [
        'getContext',
        'getStandards', 
        'validateCompliance',
        'analyzeProject',
        'getMetrics'
      ],
      features: [
        'batch-processing',
        'priority-handling',
        'retry-support',
        'metadata-enrichment',
        'version-compatibility',
        'advanced-validation',
        'performance-tracking',
        'correlation-support'
      ],
      limits: {
        maxBatchSize: this.maxBatchSize,
        maxConcurrentBatches: this.maxConcurrentBatches,
        maxRequestSize: '10MB',
        maxRetryCount: 5
      },
      timestamp: new Date().toISOString()
    };

    res.json({
      jsonrpc: '2.0',
      id: 'capabilities',
      result: capabilities,
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  // Enhanced versions endpoint
  private getSupportedVersions(_req: Request, res: Response): void {
    res.json({
      jsonrpc: '2.0',
      id: 'versions',
      result: {
        supportedVersions: this.supportedVersions,
        recommendedVersion: '2.0',
        migrationGuides: this.supportedVersions
          .filter(v => v.deprecated)
          .map(v => ({ from: v.version, to: '2.0', guide: v.migrationGuide }))
      },
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  // New advanced endpoints
  private async handlePriorityRequest(req: Request, res: Response): Promise<void> {
    // Handle priority-based request processing
    const { priority, requests } = req.body;
    
    if (!['low', 'normal', 'high', 'critical'].includes(priority)) {
      res.status(400).json(this.createEnhancedErrorResponse(
        req.body.id,
        -32600,
        'Invalid priority level',
        `Priority must be one of: low, normal, high, critical`
      ));
      return;
    }

    // Process priority requests
    const results = await this.processPriorityRequests(priority, requests);
    res.json(this.createEnhancedSuccessResponse(req.body.id, results, 0, '2.0'));
  }

  private async handleRetryRequest(req: Request, res: Response): Promise<void> {
    // Handle retry logic for failed requests
    const { originalRequest, retryCount, reason } = req.body;
    
    if (retryCount > 5) {
      res.status(400).json(this.createEnhancedErrorResponse(
        req.body.id,
        -32600,
        'Max retry count exceeded',
        `Maximum retry count of 5 exceeded`
      ));
      return;
    }

    // Process retry request
    const result = await this.processRetryRequest(originalRequest, retryCount, reason);
    res.json(this.createEnhancedSuccessResponse(req.body.id, result, 0, '2.0'));
  }

  private getBatchStats(_req: Request, res: Response): void {
    const stats = {
      activeBatches: this.activeBatches.size,
      maxConcurrentBatches: this.maxConcurrentBatches,
      totalBatchesProcessed: 0, // TODO: Implement counter
      averageBatchSize: 0, // TODO: Implement calculation
      timestamp: new Date().toISOString()
    };

    res.json({
      jsonrpc: '2.0',
      id: 'batch_stats',
      result: stats,
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  private getAdvancedHealth(_req: Request, res: Response): void {
    const health = {
      status: 'healthy',
      timestamp: new Date().toISOString(),
      services: {
        mcp: 'operational',
        database: 'operational',
        cache: 'operational'
      },
      metrics: {
        activeBatches: this.activeBatches.size,
        maxConcurrentBatches: this.maxConcurrentBatches,
        uptime: process.uptime()
      }
    };

    res.json({
      jsonrpc: '2.0',
      id: 'advanced_health',
      result: health,
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  private validateMCPMessage(req: Request, res: Response): void {
    const validationResult = this.validateEnhancedMCPRequest(req.body);
    
    res.json({
      jsonrpc: '2.0',
      id: req.body.id || 'validation',
      result: {
        isValid: validationResult.isValid,
        errors: validationResult.errors,
        warnings: validationResult.warnings,
        suggestions: this.generateValidationSuggestions(validationResult)
      },
      metadata: {
        timestamp: new Date().toISOString(),
        processingTime: 0,
        version: '2.0'
      }
    });
  }

  private generateValidationSuggestions(validationResult: any): string[] {
    const suggestions: string[] = [];
    
    if (validationResult.errors.includes('Missing jsonrpc field')) {
      suggestions.push('Add jsonrpc: "2.0" to your request');
    }
    
    if (validationResult.errors.includes('Missing method field')) {
      suggestions.push('Specify the method you want to call');
    }
    
    if (validationResult.warnings?.includes('metadata.retryCount exceeds recommended maximum of 5')) {
      suggestions.push('Consider reducing retry count to avoid excessive load');
    }
    
    return suggestions;
  }

  // Placeholder methods for new features
  private async processPriorityRequests(priority: string, requests: any[]): Promise<any[]> {
    // TODO: Implement priority-based request processing
    this.logger.info(`Processing ${requests.length} ${priority} priority requests`);
    return requests.map(req => ({ ...req, priority, processed: true }));
  }

  private async processRetryRequest(originalRequest: any, retryCount: number, reason: string): Promise<any> {
    // TODO: Implement retry request processing
    this.logger.info(`Processing retry request (attempt ${retryCount + 1})`, { reason });
    return { ...originalRequest, retryCount, reason, processed: true };
  }

  // Legacy methods for backward compatibility
  private async getContext(req: Request, res: Response): Promise<void> {
    // Legacy endpoint - redirect to new MCP endpoint
    res.redirect(307, '/api/context');
  }

  private async getStandards(req: Request, res: Response): Promise<void> {
    // Legacy endpoint - redirect to new MCP endpoint
    res.redirect(307, '/api/standards');
  }

  private async validateCompliance(req: Request, res: Response): Promise<void> {
    // Legacy endpoint - redirect to new MCP endpoint
    res.redirect(307, '/api/compliance');
  }

  private async analyzeProject(req: Request, res: Response): Promise<void> {
    // Legacy endpoint - redirect to new MCP endpoint
    res.redirect(307, '/api/analyze');
  }

  private async getMetrics(req: Request, res: Response): Promise<void> {
    // Legacy endpoint - redirect to new MCP endpoint
    res.redirect(307, '/api/metrics');
  }
}
