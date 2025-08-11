import { Logger } from 'winston';
import { DatabaseService } from './databaseService';
import { ProjectRepository } from '../repositories/ProjectRepository';

// Enhanced MCP request context with advanced features
export interface MCPRequestContext {
  requestId: string;
  timestamp: string;
  ip?: string;
  userAgent?: string;
  method: string;
  metadata: {
    version?: string;
    source?: string;
    priority: 'low' | 'normal' | 'high' | 'critical';
    retryCount: number;
    correlationId?: string;
    [key: string]: any;
  };
  correlationId?: string;
  priority: 'low' | 'normal' | 'high' | 'critical';
  retryCount: number;
}

// Enhanced MCP request with context support
export interface EnhancedMCPRequest {
  method: string;
  params?: any;
  context: MCPRequestContext;
  version: string;
  compatibility: {
    minVersion: string;
    maxVersion: string;
    supportedVersions: string[];
  };
}

// MCP Protocol Version Management
export interface MCPProtocolVersion {
  version: string;
  features: string[];
  deprecatedFeatures: string[];
  breakingChanges: string[];
  migrationGuide?: string;
  releaseDate: string;
  endOfLife?: string;
}

// Version compatibility result
export interface VersionCompatibilityResult {
  isCompatible: boolean;
  requestedVersion: string;
  supportedVersions: string[];
  compatibilityLevel: 'full' | 'partial' | 'none';
  warnings: string[];
  suggestions: string[];
  fallbackVersion?: string;
}

// Enhanced MCP response with version info
export interface EnhancedMCPResponse {
  jsonrpc: string;
  id: string | number;
  result?: any;
  error?: {
    code: number;
    message: string;
    data?: any;
  };
  metadata: {
    version: string;
    timestamp: string;
    requestId: string;
    processingTime: number;
    compatibility: VersionCompatibilityResult;
  };
}

// Priority queue item
interface PriorityQueueItem {
  request: EnhancedMCPRequest;
  priority: number;
  timestamp: number;
  retryCount: number;
}

// Cache entry with TTL
interface CacheEntry {
  data: any;
  timestamp: number;
  ttl: number;
  accessCount: number;
}

export class EnhancedMCPService {
  private logger: Logger;
  private databaseService: DatabaseService;
  private projectRepository: ProjectRepository;
  
  // Enhanced features
  private priorityQueue: PriorityQueueItem[] = [];
  private cache: Map<string, CacheEntry> = new Map();
  private maxCacheSize: number = 1000;
  private defaultTTL: number = 300000; // 5 minutes
  private maxRetryCount: number = 5;
  private retryDelays: number[] = [1000, 2000, 5000, 10000, 30000]; // Exponential backoff
  
  // Performance tracking
  private requestCounts: Map<string, number> = new Map();
  private averageProcessingTimes: Map<string, number> = new Map();
  private errorCounts: Map<string, number> = new Map();
  
  // Batch processing
  private activeBatches: Map<string, { startTime: number; requestCount: number; results: any[] }> = new Map();
  private maxConcurrentBatches: number = 10;
  private activeRequests: Set<string> = new Set();

  // Protocol version management
  private readonly CURRENT_VERSION = '2.0.0';
  private readonly SUPPORTED_VERSIONS: MCPProtocolVersion[] = [
    {
      version: '1.0.0',
      features: ['basic-mcp', 'project-analysis', 'standards-validation'],
      deprecatedFeatures: [],
      breakingChanges: [],
      releaseDate: '2024-01-01',
      endOfLife: '2025-01-01'
    },
    {
      version: '1.5.0',
      features: ['basic-mcp', 'project-analysis', 'standards-validation', 'batch-processing', 'caching'],
      deprecatedFeatures: ['legacy-validation'],
      breakingChanges: [],
      releaseDate: '2024-06-01',
      endOfLife: '2025-06-01'
    },
    {
      version: '2.0.0',
      features: [
        'basic-mcp', 'project-analysis', 'standards-validation', 'batch-processing', 'caching',
        'priority-queuing', 'retry-mechanism', 'advanced-metrics', 'protocol-versioning',
        'enhanced-validation', 'correlation-tracking'
      ],
      deprecatedFeatures: ['legacy-validation', 'basic-batch-processing'],
      breakingChanges: ['validation-middleware-changes', 'rate-limiting-updates'],
      releaseDate: '2024-12-01'
    }
  ];

  // Enhanced MCP error handling system
  private readonly ERROR_CODES = {
    // JSON-RPC 2.0 standard errors
    PARSE_ERROR: -32700,
    INVALID_REQUEST: -32600,
    METHOD_NOT_FOUND: -32601,
    INVALID_PARAMS: -32602,
    INTERNAL_ERROR: -32603,
    
    // MCP-specific errors
    VERSION_NOT_SUPPORTED: -32002,
    RATE_LIMIT_EXCEEDED: -32000,
    CONCURRENT_LIMIT_EXCEEDED: -32001,
    VALIDATION_FAILED: -32003,
    AUTHENTICATION_FAILED: -32004,
    AUTHORIZATION_FAILED: -32005,
    RESOURCE_NOT_FOUND: -32006,
    RESOURCE_CONFLICT: -32007,
    SERVICE_UNAVAILABLE: -32008,
    TIMEOUT_ERROR: -32009,
    QUOTA_EXCEEDED: -32010,
    
    // AgentForge-specific errors
    PROJECT_ANALYSIS_FAILED: -32100,
    STANDARDS_VALIDATION_FAILED: -32101,
    COMPLIANCE_CHECK_FAILED: -32102,
    METRICS_COLLECTION_FAILED: -32103,
    CACHE_OPERATION_FAILED: -32104,
    DATABASE_OPERATION_FAILED: -32105,
    BATCH_PROCESSING_FAILED: -32106,
    PRIORITY_QUEUE_FULL: -32107,
    RETRY_LIMIT_EXCEEDED: -32108
  };

  // Error categories for better error handling
  private readonly ERROR_CATEGORIES = {
    CLIENT_ERRORS: [
      this.ERROR_CODES.PARSE_ERROR,
      this.ERROR_CODES.INVALID_REQUEST,
      this.ERROR_CODES.METHOD_NOT_FOUND,
      this.ERROR_CODES.INVALID_PARAMS,
      this.ERROR_CODES.VERSION_NOT_SUPPORTED,
      this.ERROR_CODES.VALIDATION_FAILED,
      this.ERROR_CODES.AUTHENTICATION_FAILED,
      this.ERROR_CODES.AUTHORIZATION_FAILED
    ],
    RATE_LIMIT_ERRORS: [
      this.ERROR_CODES.RATE_LIMIT_EXCEEDED,
      this.ERROR_CODES.CONCURRENT_LIMIT_EXCEEDED,
      this.ERROR_CODES.QUOTA_EXCEEDED
    ],
    RETRYABLE_ERRORS: [
      this.ERROR_CODES.SERVICE_UNAVAILABLE,
      this.ERROR_CODES.TIMEOUT_ERROR,
      this.ERROR_CODES.DATABASE_OPERATION_FAILED,
      this.ERROR_CODES.CACHE_OPERATION_FAILED
    ],
    SYSTEM_ERRORS: [
      this.ERROR_CODES.INTERNAL_ERROR,
      this.ERROR_CODES.PROJECT_ANALYSIS_FAILED,
      this.ERROR_CODES.STANDARDS_VALIDATION_FAILED,
      this.ERROR_CODES.COMPLIANCE_CHECK_FAILED
    ]
  };

  constructor(logger: Logger, databaseService: DatabaseService) {
    this.logger = logger;
    this.databaseService = databaseService;
    this.projectRepository = new ProjectRepository(databaseService);
    
    // Start background tasks
    this.startBackgroundTasks();
  }

  // Enhanced MCP method with version support
  async processMCPRequest(request: any, context: MCPRequestContext): Promise<EnhancedMCPResponse> {
    const startTime = Date.now();
    const requestId = context.requestId;

    try {
      // Check version compatibility
      const versionCompatibility = this.checkVersionCompatibility(request.version || '1.0.0');
      
      if (!versionCompatibility.isCompatible) {
        this.logger.warn('Version compatibility check failed', {
          requestId,
          requestedVersion: request.version,
          compatibility: versionCompatibility
        });

        return this.createErrorResponse(request.id, -32002, 'Version not supported', {
          compatibility: versionCompatibility,
          suggestions: [
            'Upgrade to a supported version',
            'Use backward compatibility mode',
            'Check version requirements'
          ]
        }, context, startTime);
      }

      // Add to active requests
      this.activeRequests.add(requestId);

      // Process request based on method
      let result: any;
      switch (request.method) {
        case 'getContext':
          result = await this.getContext(request.params, context);
          break;
        case 'getStandards':
          result = await this.getStandards(request.params, context);
          break;
        case 'validateCompliance':
          result = await this.validateCompliance(request.params, context);
          break;
        case 'analyzeProject':
          result = await this.analyzeProject(request.params, context);
          break;
        case 'getMetrics':
          result = await this.getMetrics(request.params, context);
          break;
        case 'getBatchStats':
          result = await this.getBatchStats(context);
          break;
        case 'getPerformanceMetrics':
          result = await this.getPerformanceMetrics(context);
          break;
        case 'getVersionInfo':
          result = await this.getVersionInfo(request.params, context);
          break;
        case 'checkCompatibility':
          result = await this.checkCompatibility(request.params, context);
          break;
        default:
          throw new Error(`Method '${request.method}' not supported`);
      }

      // Update performance metrics
      this.updatePerformanceMetrics(request.method, Date.now() - startTime, false);

      // Create success response
      return this.createSuccessResponse(request.id, result, context, startTime, versionCompatibility);

    } catch (error) {
      this.logger.error('Error processing MCP request', {
        requestId,
        method: request.method,
        error: error instanceof Error ? error.message : 'Unknown error',
        stack: error instanceof Error ? error.stack : undefined
      });

      return this.createErrorResponse(
        request.id,
        -32603,
        'Internal error',
        {
          error: error instanceof Error ? error.message : 'Unknown error',
          requestId,
          method: request.method
        },
        context,
        startTime
      );

    } finally {
      // Remove from active requests
      this.activeRequests.delete(requestId);
    }
  }

  // Enhanced MCP request processing with advanced features
  async processRequest(request: EnhancedMCPRequest, context?: MCPRequestContext): Promise<any> {
    const startTime = Date.now();
    const requestId = context?.requestId || `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    try {
      // Create enhanced context if not provided
      const enhancedContext = context || this.createDefaultContext(request, requestId);
      
      // Check cache first (for non-critical requests)
      if (enhancedContext.priority !== 'critical') {
        const cacheKey = this.generateCacheKey(request);
        const cachedResult = this.getFromCache(cacheKey);
        if (cachedResult) {
          this.logger.info('Cache hit for request', { method: request.method, requestId });
          return this.createCachedResponse(cachedResult, Date.now() - startTime, enhancedContext);
        }
      }

      // Process request based on priority
      let result: any;
      if (enhancedContext.priority === 'critical') {
        // Process critical requests immediately
        result = await this.processCriticalRequest(request, enhancedContext);
      } else {
        // Add to priority queue for non-critical requests
        result = await this.processQueuedRequest(request, enhancedContext);
      }

      // Cache successful results (for non-critical requests)
      if (enhancedContext.priority !== 'critical' && result && !result.error) {
        const cacheKey = this.generateCacheKey(request);
        this.setCache(cacheKey, result);
      }

      // Update performance metrics
      const processingTime = Date.now() - startTime;
      this.updatePerformanceMetrics(request.method, processingTime, false);

      // Create enhanced response
      return this.createEnhancedResponse(result, processingTime, enhancedContext, false);

    } catch (error) {
      const processingTime = Date.now() - startTime;
      this.logger.error('Error processing MCP request:', error);
      
      // Update error metrics
      this.updatePerformanceMetrics(request.method, processingTime, true);
      
      // Handle retry logic
      if (context?.retryCount < this.maxRetryCount) {
        return this.handleRetry(request, context, error, processingTime);
      }

      // Return error response
      return this.createErrorResponse(error, processingTime, enhancedContext, false);
    }
  }

  // Enhanced batch request processing
  async processBatchRequest(requests: EnhancedMCPRequest[], batchId: string): Promise<any[]> {
    const startTime = Date.now();
    
    try {
      // Check concurrent batch limits
      if (this.activeBatches.size >= this.maxConcurrentBatches) {
        throw new Error('Maximum concurrent batches reached');
      }

      // Initialize batch tracking
      this.activeBatches.set(batchId, {
        startTime: Date.now(),
        requestCount: requests.length,
        results: []
      });

      // Sort requests by priority
      const sortedRequests = requests
        .map(req => ({
          request: req,
          priority: this.getPriorityValue(req.context?.priority || 'normal'),
          timestamp: Date.now()
        }))
        .sort((a, b) => b.priority - a.priority)
        .map(item => item.request);

      // Process requests in priority order
      const results = [];
      for (let i = 0; i < sortedRequests.length; i++) {
        const request = sortedRequests[i];
        const requestStartTime = Date.now();
        
        try {
          const context = request.context || this.createDefaultContext(request, `${batchId}_${i}`);
          const result = await this.processRequest(request, context);
          
          results.push({
            index: i,
            requestId: context.requestId,
            result,
            processingTime: Date.now() - requestStartTime
          });
          
          // Update batch progress
          const batch = this.activeBatches.get(batchId);
          if (batch) {
            batch.results.push(result);
          }
          
        } catch (error) {
          this.logger.error(`Error processing batch request ${i}:`, error);
          results.push({
            index: i,
            requestId: `${batchId}_${i}`,
            error: {
              code: -32603,
              message: 'Internal error',
              details: error instanceof Error ? error.message : 'Unknown error',
              retryable: true
            },
            processingTime: Date.now() - requestStartTime
          });
        }
      }

      // Clean up batch tracking
      this.activeBatches.delete(batchId);
      
      const totalProcessingTime = Date.now() - startTime;
      this.logger.info('Batch request completed', { 
        batchId, 
        requestCount: requests.length, 
        processingTime: totalProcessingTime 
      });

      return results;

    } catch (error) {
      this.activeBatches.delete(batchId);
      throw error;
    }
  }

  // Priority-based request processing
  private async processQueuedRequest(request: EnhancedMCPRequest, context: MCPRequestContext): Promise<any> {
    const priorityValue = this.getPriorityValue(context.priority);
    
    // Add to priority queue
    this.priorityQueue.push({
      request,
      priority: priorityValue,
      timestamp: Date.now(),
      retryCount: context.retryCount
    });

    // Sort queue by priority (highest first)
    this.priorityQueue.sort((a, b) => {
      if (a.priority !== b.priority) {
        return b.priority - a.priority;
      }
      // If same priority, process older requests first
      return a.timestamp - b.timestamp;
    });

    // Process queue
    return this.processPriorityQueue();
  }

  // Critical request processing (immediate execution)
  private async processCriticalRequest(request: EnhancedMCPRequest, context: MCPRequestContext): Promise<any> {
    this.logger.info('Processing critical priority request', { 
      method: request.method, 
      requestId: context.requestId 
    });

    // Execute immediately without queuing
    return this.executeRequest(request, context);
  }

  // Priority queue processing
  private async processPriorityQueue(): Promise<any> {
    if (this.priorityQueue.length === 0) {
      return null;
    }

    // Take the highest priority request
    const item = this.priorityQueue.shift();
    if (!item) {
      return null;
    }

    // Process the request
    return this.executeRequest(item.request, {
      ...item.request.context!,
      retryCount: item.retryCount
    });
  }

  // Execute the actual request
  private async executeRequest(request: EnhancedMCPRequest, context: MCPRequestContext): Promise<any> {
    switch (request.method) {
      case 'getContext':
        return await this.getContext(request.params, context);
      case 'getStandards':
        return await this.getStandards(request.params, context);
      case 'validateCompliance':
        return await this.validateCompliance(request.params, context);
      case 'analyzeProject':
        return await this.analyzeProject(request.params, context);
      case 'getMetrics':
        return await this.getMetrics(request.params, context);
      case 'getBatchStats':
        return await this.getBatchStats(context);
      case 'getPerformanceMetrics':
        return await this.getPerformanceMetrics(context);
      default:
        throw new Error(`Unsupported method: ${request.method}`);
    }
  }

  // Enhanced retry handling with exponential backoff
  private async handleRetry(
    request: EnhancedMCPRequest, 
    context: MCPRequestContext, 
    error: any, 
    processingTime: number
  ): Promise<any> {
    const retryCount = context.retryCount + 1;
    const delay = this.retryDelays[Math.min(retryCount - 1, this.retryDelays.length - 1)];
    
    this.logger.info('Scheduling retry for request', { 
      method: request.method, 
      requestId: context.requestId, 
      retryCount, 
      delay 
    });

    // Schedule retry with exponential backoff
    setTimeout(async () => {
      try {
        const retryContext = { ...context, retryCount };
        await this.processRequest(request, retryContext);
      } catch (retryError) {
        this.logger.error('Retry failed for request', { 
          method: request.method, 
          requestId: context.requestId, 
          retryCount, 
          error: retryError 
        });
      }
    }, delay);

    // Return immediate response indicating retry is scheduled
    return this.createRetryResponse(error, processingTime, context, retryCount, delay);
  }

  // Enhanced caching system
  private generateCacheKey(request: EnhancedMCPRequest): string {
    return `${request.method}_${JSON.stringify(request.params)}`;
  }

  private getFromCache(key: string): any {
    const entry = this.cache.get(key);
    if (!entry) {
      return null;
    }

    // Check if expired
    if (Date.now() - entry.timestamp > entry.ttl) {
      this.cache.delete(key);
      return null;
    }

    // Update access count
    entry.accessCount++;
    return entry.data;
  }

  private setCache(key: string, data: any, ttl?: number): void {
    // Implement LRU eviction if cache is full
    if (this.cache.size >= this.maxCacheSize) {
      const oldestKey = this.cache.keys().next().value;
      this.cache.delete(oldestKey);
    }

    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl: ttl || this.defaultTTL,
      accessCount: 1
    });
  }

  // Performance metrics tracking
  private updatePerformanceMetrics(method: string, processingTime: number, isError: boolean): void {
    // Update request counts
    const currentCount = this.requestCounts.get(method) || 0;
    this.requestCounts.set(method, currentCount + 1);

    // Update average processing times
    const currentAvg = this.averageProcessingTimes.get(method) || 0;
    const currentCount2 = this.requestCounts.get(method) || 1;
    const newAvg = (currentAvg * (currentCount2 - 1) + processingTime) / currentCount2;
    this.averageProcessingTimes.set(method, newAvg);

    // Update error counts
    if (isError) {
      const currentErrorCount = this.errorCounts.get(method) || 0;
      this.errorCounts.set(method, currentErrorCount + 1);
    }
  }

  // Enhanced response creation
  private createEnhancedResponse(
    data: any, 
    processingTime: number, 
    context: MCPRequestContext, 
    cacheHit: boolean
  ): EnhancedMCPResponse {
    return {
      success: true,
      data,
      metadata: {
        processingTime,
        cacheHit,
        priority: context.priority,
        correlationId: context.correlationId,
        timestamp: new Date().toISOString(),
        version: '2.0'
      }
    };
  }

  private createCachedResponse(
    data: any, 
    processingTime: number, 
    context: MCPRequestContext
  ): EnhancedMCPResponse {
    return this.createEnhancedResponse(data, processingTime, context, true);
  }

  private createErrorResponse(
    error: any, 
    processingTime: number, 
    context: MCPRequestContext, 
    retryable: boolean
  ): EnhancedMCPResponse {
    return {
      success: false,
      error: {
        code: -32603,
        message: 'Internal error',
        details: error instanceof Error ? error.message : 'Unknown error',
        retryable,
        suggestedAction: retryable ? 'Retry the request' : 'Contact support'
      },
      metadata: {
        processingTime,
        cacheHit: false,
        priority: context.priority,
        correlationId: context.correlationId,
        timestamp: new Date().toISOString(),
        version: '2.0'
      }
    };
  }

  private createRetryResponse(
    error: any, 
    processingTime: number, 
    context: MCPRequestContext, 
    retryCount: number, 
    delay: number
  ): EnhancedMCPResponse {
    return {
      success: false,
      error: {
        code: -32603,
        message: 'Request failed, retry scheduled',
        details: error instanceof Error ? error.message : 'Unknown error',
        retryable: true,
        suggestedAction: `Retry scheduled in ${delay}ms (attempt ${retryCount + 1})`
      },
      metadata: {
        processingTime,
        cacheHit: false,
        priority: context.priority,
        correlationId: context.correlationId,
        timestamp: new Date().toISOString(),
        version: '2.0'
      }
    };
  }

  // Enhanced error handling with retry logic
  private async handleEnhancedError(
    error: any,
    request: any,
    context: MCPRequestContext,
    startTime: number
  ): Promise<EnhancedMCPResponse> {
    const errorCode = this.determineErrorCode(error);
    const retryable = this.isErrorRetryable(errorCode);
    const retryAfter = this.calculateRetryAfter(errorCode, context);
    
    // Log error with context
    this.logger.error('Enhanced error handling', {
      requestId: context.requestId,
      method: request.method,
      errorCode,
      error: error instanceof Error ? error.message : 'Unknown error',
      retryable,
      retryAfter,
      context: {
        priority: context.priority,
        retryCount: context.retryCount,
        source: context.source
      }
    });

    // Update error metrics
    this.updateErrorMetrics(request.method, errorCode);

    // Create enhanced error response
    const errorResponse = this.createEnhancedErrorResponse(
      request.id,
      errorCode,
      this.getErrorMessage(errorCode, error),
      this.getErrorData(error, context),
      context,
      startTime,
      retryable,
      retryAfter
    );

    // If retryable and under retry limit, add to retry queue
    if (retryable && context.retryCount < this.maxRetryCount) {
      await this.queueForRetry(request, context, errorCode);
    }

    return errorResponse;
  }

  // Determine error code from error
  private determineErrorCode(error: any): number {
    if (error.code && typeof error.code === 'number') {
      return error.code;
    }
    
    if (error instanceof Error) {
      const message = error.message.toLowerCase();
      
      if (message.includes('validation') || message.includes('invalid')) {
        return this.ERROR_CODES.VALIDATION_FAILED;
      } else if (message.includes('not found')) {
        return this.ERROR_CODES.RESOURCE_NOT_FOUND;
      } else if (message.includes('timeout') || message.includes('timed out')) {
        return this.ERROR_CODES.TIMEOUT_ERROR;
      } else if (message.includes('unauthorized') || message.includes('authentication')) {
        return this.ERROR_CODES.AUTHENTICATION_FAILED;
      } else if (message.includes('forbidden') || message.includes('authorization')) {
        return this.ERROR_CODES.AUTHORIZATION_FAILED;
      } else if (message.includes('rate limit') || message.includes('too many requests')) {
        return this.ERROR_CODES.RATE_LIMIT_EXCEEDED;
      } else if (message.includes('service unavailable') || message.includes('unavailable')) {
        return this.ERROR_CODES.SERVICE_UNAVAILABLE;
      }
    }
    
    return this.ERROR_CODES.INTERNAL_ERROR;
  }

  // Check if error is retryable
  private isErrorRetryable(errorCode: number): boolean {
    return this.ERROR_CATEGORIES.RETRYABLE_ERRORS.includes(errorCode);
  }

  // Calculate retry after delay
  private calculateRetryAfter(errorCode: number, context: MCPRequestContext): number | undefined {
    if (!this.isErrorRetryable(errorCode)) {
      return undefined;
    }
    
    const baseDelay = this.retryDelays[Math.min(context.retryCount, this.retryDelays.length - 1)];
    
    // Adjust delay based on priority
    const priorityMultiplier = {
      'low': 2.0,
      'normal': 1.0,
      'high': 0.5,
      'critical': 0.25
    };
    
    return Math.floor(baseDelay * (priorityMultiplier[context.priority] || 1.0));
  }

  // Get user-friendly error message
  private getErrorMessage(errorCode: number, error: any): string {
    const defaultMessages = {
      [this.ERROR_CODES.PARSE_ERROR]: 'Invalid JSON in request body',
      [this.ERROR_CODES.INVALID_REQUEST]: 'Request format is invalid',
      [this.ERROR_CODES.METHOD_NOT_FOUND]: 'Method not supported',
      [this.ERROR_CODES.INVALID_PARAMS]: 'Invalid parameters provided',
      [this.ERROR_CODES.VERSION_NOT_SUPPORTED]: 'Protocol version not supported',
      [this.ERROR_CODES.RATE_LIMIT_EXCEEDED]: 'Rate limit exceeded',
      [this.ERROR_CODES.CONCURRENT_LIMIT_EXCEEDED]: 'Too many concurrent requests',
      [this.ERROR_CODES.VALIDATION_FAILED]: 'Request validation failed',
      [this.ERROR_CODES.AUTHENTICATION_FAILED]: 'Authentication required',
      [this.ERROR_CODES.AUTHORIZATION_FAILED]: 'Insufficient permissions',
      [this.ERROR_CODES.RESOURCE_NOT_FOUND]: 'Requested resource not found',
      [this.ERROR_CODES.RESOURCE_CONFLICT]: 'Resource conflict detected',
      [this.ERROR_CODES.SERVICE_UNAVAILABLE]: 'Service temporarily unavailable',
      [this.ERROR_CODES.TIMEOUT_ERROR]: 'Request timed out',
      [this.ERROR_CODES.QUOTA_EXCEEDED]: 'Service quota exceeded',
      [this.ERROR_CODES.PROJECT_ANALYSIS_FAILED]: 'Project analysis failed',
      [this.ERROR_CODES.STANDARDS_VALIDATION_FAILED]: 'Standards validation failed',
      [this.ERROR_CODES.COMPLIANCE_CHECK_FAILED]: 'Compliance check failed',
      [this.ERROR_CODES.METRICS_COLLECTION_FAILED]: 'Metrics collection failed',
      [this.ERROR_CODES.CACHE_OPERATION_FAILED]: 'Cache operation failed',
      [this.ERROR_CODES.DATABASE_OPERATION_FAILED]: 'Database operation failed',
      [this.ERROR_CODES.BATCH_PROCESSING_FAILED]: 'Batch processing failed',
      [this.ERROR_CODES.PRIORITY_QUEUE_FULL]: 'Priority queue is full',
      [this.ERROR_CODES.RETRY_LIMIT_EXCEEDED]: 'Maximum retry attempts exceeded'
    };
    
    return defaultMessages[errorCode] || 'An unexpected error occurred';
  }

  // Get error data for response
  private getErrorData(error: any, context: MCPRequestContext): any {
    const baseData: any = {
      timestamp: new Date().toISOString(),
      requestId: context.requestId,
      priority: context.priority,
      retryCount: context.retryCount
    };
    
    if (error instanceof Error) {
      baseData.errorType = error.constructor.name;
      baseData.message = error.message;
      
      // Include stack trace in development
      if (process.env.NODE_ENV === 'development') {
        baseData.stack = error.stack;
      }
    } else if (typeof error === 'object' && error !== null) {
      baseData.errorType = 'ObjectError';
      baseData.details = error;
    } else {
      baseData.errorType = 'UnknownError';
      baseData.value = error;
    }
    
    return baseData;
  }

  // Queue request for retry
  private async queueForRetry(request: any, context: MCPRequestContext, errorCode: number): Promise<void> {
    const retryContext = {
      ...context,
      retryCount: context.retryCount + 1,
      lastError: errorCode,
      retryTimestamp: new Date().toISOString()
    };
    
    const retryItem: PriorityQueueItem = {
      request: {
        ...request,
        context: retryContext
      },
      priority: this.getPriorityValue(context.priority),
      timestamp: Date.now(),
      retryCount: retryContext.retryCount
    };
    
    // Add to priority queue
    this.priorityQueue.push(retryItem);
    this.priorityQueue.sort((a, b) => b.priority - a.priority);
    
    this.logger.info('Request queued for retry', {
      requestId: context.requestId,
      method: request.method,
      retryCount: retryContext.retryCount,
      priority: context.priority,
      estimatedDelay: this.calculateRetryAfter(errorCode, retryContext)
    });
  }

  // Update error metrics
  private updateErrorMetrics(method: string, errorCode: number): void {
    const key = `${method}_${errorCode}`;
    const currentCount = this.errorCounts.get(key) || 0;
    this.errorCounts.set(key, currentCount + 1);
  }

  // Get error statistics
  getErrorStats(): {
    totalErrors: number;
    errorsByMethod: Record<string, number>;
    errorsByCode: Record<number, number>;
    retryStats: { totalRetries: number; successfulRetries: number; failedRetries: number };
  } {
    const errorsByMethod = new Map<string, number>();
    const errorsByCode = new Map<number, number>();
    
    // Aggregate error counts
    for (const [key, count] of this.errorCounts.entries()) {
      const [method, errorCode] = key.split('_');
      const code = parseInt(errorCode);
      
      errorsByMethod.set(method, (errorsByMethod.get(method) || 0) + count);
      errorsByCode.set(code, (errorsByCode.get(code) || 0) + count);
    }
    
    return {
      totalErrors: Array.from(this.errorCounts.values()).reduce((sum, count) => sum + count, 0),
      errorsByMethod: Object.fromEntries(errorsByMethod),
      errorsByCode: Object.fromEntries(errorsByCode),
      retryStats: {
        totalRetries: this.priorityQueue.filter(item => item.retryCount > 0).length,
        successfulRetries: 0, // Would need to track successful retries
        failedRetries: this.priorityQueue.filter(item => item.retryCount >= this.maxRetryCount).length
      }
    };
  }

  // Context creation and management
  private createDefaultContext(request: EnhancedMCPRequest, requestId: string): MCPRequestContext {
    return {
      requestId,
      timestamp: new Date().toISOString(),
      method: request.method,
      metadata: {
        priority: 'normal',
        retryCount: 0
      },
      priority: 'normal',
      retryCount: 0
    };
  }

  private getPriorityValue(priority: string): number {
    const priorityMap = { 'low': 1, 'normal': 2, 'high': 3, 'critical': 4 };
    return priorityMap[priority as keyof typeof priorityMap] || 2;
  }

  // Background tasks for maintenance
  private startBackgroundTasks(): void {
    // Cache cleanup every 5 minutes
    setInterval(() => {
      this.cleanupCache();
    }, 5 * 60 * 1000);

    // Priority queue processing every 100ms
    setInterval(() => {
      this.processPriorityQueue();
    }, 100);

    // Performance metrics reset every hour
    setInterval(() => {
      this.resetPerformanceMetrics();
    }, 60 * 60 * 1000);
  }

  private cleanupCache(): void {
    const now = Date.now();
    for (const [key, entry] of this.cache.entries()) {
      if (now - entry.timestamp > entry.ttl) {
        this.cache.delete(key);
      }
    }
  }

  private resetPerformanceMetrics(): void {
    this.requestCounts.clear();
    this.averageProcessingTimes.clear();
    this.errorCounts.clear();
  }

  // Enhanced service methods with context support
  async getContext(params: any, context: MCPRequestContext): Promise<any> {
    this.logger.info('Getting context with enhanced processing', { 
      params, 
      requestId: context.requestId,
      priority: context.priority 
    });

    // Enhanced context retrieval with priority-based processing
    const contextData = await this.databaseService.getContext(params);
    
    // Add context metadata
    return {
      ...contextData,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  async getStandards(params: any, context: MCPRequestContext): Promise<any> {
    this.logger.info('Getting standards with enhanced processing', { 
      params, 
      requestId: context.requestId,
      priority: context.priority 
    });

    const standards = await this.databaseService.getStandards(params);
    
    return {
      ...standards,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  async validateCompliance(params: any, context: MCPRequestContext): Promise<any> {
    this.logger.info('Validating compliance with enhanced processing', { 
      params, 
      requestId: context.requestId,
      priority: context.priority 
    });

    const compliance = await this.databaseService.validateCompliance(params);
    
    return {
      ...compliance,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  async analyzeProject(params: any, context: MCPRequestContext): Promise<any> {
    this.logger.info('Analyzing project with enhanced processing', { 
      params, 
      requestId: context.requestId,
      priority: context.priority 
    });

    const analysis = await this.databaseService.analyzeProject(params);
    
    return {
      ...analysis,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  async getMetrics(params: any, context: MCPRequestContext): Promise<any> {
    this.logger.info('Getting metrics with enhanced processing', { 
      params, 
      requestId: context.requestId,
      priority: context.priority 
    });

    const metrics = await this.databaseService.getMetrics(params);
    
    return {
      ...metrics,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  // New enhanced methods
  async getBatchStats(context: MCPRequestContext): Promise<any> {
    return {
      activeBatches: this.activeBatches.size,
      maxConcurrentBatches: this.maxConcurrentBatches,
      priorityQueueLength: this.priorityQueue.length,
      cacheSize: this.cache.size,
      maxCacheSize: this.maxCacheSize,
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  async getPerformanceMetrics(context: MCPRequestContext): Promise<any> {
    return {
      requestCounts: Object.fromEntries(this.requestCounts),
      averageProcessingTimes: Object.fromEntries(this.averageProcessingTimes),
      errorCounts: Object.fromEntries(this.errorCounts),
      cacheStats: {
        size: this.cache.size,
        maxSize: this.maxCacheSize,
        hitRate: this.calculateCacheHitRate()
      },
      _enhanced: {
        processedAt: new Date().toISOString(),
        priority: context.priority,
        correlationId: context.correlationId,
        requestId: context.requestId
      }
    };
  }

  private calculateCacheHitRate(): number {
    // TODO: Implement cache hit rate calculation
    return 0.85; // Placeholder
  }

  // Public methods for external access
  getCacheStats(): { size: number; maxSize: number; hitRate: number } {
    return {
      size: this.cache.size,
      maxSize: this.maxCacheSize,
      hitRate: this.calculateCacheHitRate()
    };
  }

  getPriorityQueueStats(): { length: number; priorities: string[] } {
    const priorities = this.priorityQueue.map(item => 
      Object.keys({ low: 1, normal: 2, high: 3, critical: 4 })
        .find(key => ({ low: 1, normal: 2, high: 3, critical: 4 }[key as keyof typeof { low: 1, normal: 2, high: 3, critical: 4 }]) === item.priority) || 'normal'
    );

    return {
      length: this.priorityQueue.length,
      priorities
    };
  }

  getActiveBatchesStats(): { count: number; maxConcurrent: number } {
    return {
      count: this.activeBatches.size,
      maxConcurrent: this.maxConcurrentBatches
    };
  }

  // Version compatibility checking
  private checkVersionCompatibility(requestedVersion: string): VersionCompatibilityResult {
    const requested = this.parseVersion(requestedVersion);
    const current = this.parseVersion(this.CURRENT_VERSION);
    
    // Find the requested version in supported versions
    const versionInfo = this.SUPPORTED_VERSIONS.find(v => v.version === requestedVersion);
    
    if (!versionInfo) {
      return {
        isCompatible: false,
        requestedVersion,
        supportedVersions: this.SUPPORTED_VERSIONS.map(v => v.version),
        compatibilityLevel: 'none',
        warnings: [`Version ${requestedVersion} is not supported`],
        suggestions: [
          'Use one of the supported versions',
          'Check the latest version documentation',
          'Contact support for migration assistance'
        ]
      };
    }

    // Check if version is end-of-life
    if (versionInfo.endOfLife && new Date() > new Date(versionInfo.endOfLife)) {
      return {
        isCompatible: false,
        requestedVersion,
        supportedVersions: this.SUPPORTED_VERSIONS.map(v => v.version),
        compatibilityLevel: 'none',
        warnings: [`Version ${requestedVersion} has reached end-of-life`],
        suggestions: [
          'Upgrade to a supported version',
          'Check migration guides for the latest version',
          'Contact support for upgrade assistance'
        ]
      };
    }

    // Check compatibility level
    let compatibilityLevel: 'full' | 'partial' | 'none' = 'full';
    const warnings: string[] = [];
    const suggestions: string[] = [];

    // Check for deprecated features
    if (versionInfo.deprecatedFeatures.length > 0) {
      compatibilityLevel = 'partial';
      warnings.push(`Version ${requestedVersion} contains deprecated features`);
      suggestions.push('Consider upgrading to remove deprecated features');
    }

    // Check for breaking changes
    if (versionInfo.breakingChanges.length > 0) {
      compatibilityLevel = 'partial';
      warnings.push(`Version ${requestedVersion} has breaking changes from previous versions`);
      suggestions.push('Review breaking changes documentation');
    }

    // Version comparison
    if (requested.major < current.major) {
      compatibilityLevel = 'partial';
      warnings.push(`Major version difference detected`);
      suggestions.push('Consider upgrading to the latest major version');
    }

    return {
      isCompatible: true,
      requestedVersion,
      supportedVersions: this.SUPPORTED_VERSIONS.map(v => v.version),
      compatibilityLevel,
      warnings,
      suggestions,
      fallbackVersion: this.findBestFallbackVersion(requestedVersion)
    };
  }

  // Parse semantic version
  private parseVersion(version: string): { major: number; minor: number; patch: number } {
    const parts = version.split('.').map(Number);
    return {
      major: parts[0] || 0,
      minor: parts[1] || 0,
      patch: parts[2] || 0
    };
  }

  // Find best fallback version
  private findBestFallbackVersion(requestedVersion: string): string | undefined {
    const requested = this.parseVersion(requestedVersion);
    
    // Find the closest supported version
    let bestVersion = this.CURRENT_VERSION;
    let bestScore = Infinity;
    
    for (const version of this.SUPPORTED_VERSIONS) {
      const supported = this.parseVersion(version.version);
      const score = Math.abs(supported.major - requested.major) * 1000 +
                   Math.abs(supported.minor - requested.minor) * 100 +
                   Math.abs(supported.patch - requested.patch);
      
      if (score < bestScore) {
        bestScore = score;
        bestVersion = version.version;
      }
    }
    
    return bestVersion !== requestedVersion ? bestVersion : undefined;
  }

  // Get version information
  async getVersionInfo(params: any, context: MCPRequestContext): Promise<any> {
    const requestedVersion = params?.version || this.CURRENT_VERSION;
    const compatibility = this.checkVersionCompatibility(requestedVersion);
    
    return {
      currentVersion: this.CURRENT_VERSION,
      requestedVersion,
      supportedVersions: this.SUPPORTED_VERSIONS.map(v => ({
        version: v.version,
        features: v.features,
        deprecatedFeatures: v.deprecatedFeatures,
        breakingChanges: v.breakingChanges,
        releaseDate: v.releaseDate,
        endOfLife: v.endOfLife,
        isCurrent: v.version === this.CURRENT_VERSION,
        isDeprecated: v.endOfLife && new Date() > new Date(v.endOfLife)
      })),
      compatibility,
      migrationGuides: this.getMigrationGuides(requestedVersion),
      featureMatrix: this.generateFeatureMatrix()
    };
  }

  // Check compatibility for specific features
  async checkCompatibility(params: any, context: MCPRequestContext): Promise<any> {
    const { version, features } = params;
    
    if (!version) {
      throw new Error('Version parameter is required');
    }
    
    const compatibility = this.checkVersionCompatibility(version);
    const featureCompatibility: Record<string, boolean> = {};
    
    if (features && Array.isArray(features)) {
      for (const feature of features) {
        featureCompatibility[feature] = this.isFeatureSupported(feature, version);
      }
    }
    
    return {
      version,
      compatibility,
      featureCompatibility,
      recommendations: this.generateCompatibilityRecommendations(version, features)
    };
  }

  // Check if specific feature is supported in version
  private isFeatureSupported(feature: string, version: string): boolean {
    const versionInfo = this.SUPPORTED_VERSIONS.find(v => v.version === version);
    if (!versionInfo) return false;
    
    return versionInfo.features.includes(feature);
  }

  // Generate feature matrix
  private generateFeatureMatrix(): Record<string, Record<string, boolean>> {
    const matrix: Record<string, Record<string, boolean>> = {};
    const allFeatures = new Set<string>();
    
    // Collect all features
    for (const version of this.SUPPORTED_VERSIONS) {
      for (const feature of version.features) {
        allFeatures.add(feature);
      }
    }
    
    // Generate matrix
    for (const version of this.SUPPORTED_VERSIONS) {
      matrix[version.version] = {};
      for (const feature of allFeatures) {
        matrix[version.version][feature] = version.features.includes(feature);
      }
    }
    
    return matrix;
  }

  // Get migration guides
  private getMigrationGuides(fromVersion: string): any[] {
    const guides = [];
    const from = this.SUPPORTED_VERSIONS.find(v => v.version === fromVersion);
    
    if (!from) return guides;
    
    for (const to of this.SUPPORTED_VERSIONS) {
      if (to.version === fromVersion) continue;
      
      const fromParsed = this.parseVersion(fromVersion);
      const toParsed = this.parseVersion(to.version);
      
      if (toParsed.major > fromParsed.major || 
          (toParsed.major === fromParsed.major && toParsed.minor > fromParsed.minor)) {
        guides.push({
          from: fromVersion,
          to: to.version,
          type: toParsed.major > fromParsed.major ? 'major' : 'minor',
          breakingChanges: to.breakingChanges,
          deprecatedFeatures: to.deprecatedFeatures,
          migrationSteps: this.generateMigrationSteps(fromVersion, to.version)
        });
      }
    }
    
    return guides;
  }

  // Generate migration steps
  private generateMigrationSteps(fromVersion: string, toVersion: string): string[] {
    const steps = [];
    
    if (fromVersion === '1.0.0' && toVersion === '2.0.0') {
      steps.push(
        'Update validation middleware usage',
        'Review rate limiting configuration',
        'Update batch processing implementation',
        'Check for deprecated feature usage',
        'Review breaking changes documentation'
      );
    } else if (fromVersion === '1.5.0' && toVersion === '2.0.0') {
      steps.push(
        'Update to new validation system',
        'Review priority queuing configuration',
        'Update retry mechanism implementation',
        'Check for deprecated features'
      );
    }
    
    return steps;
  }

  // Generate compatibility recommendations
  private generateCompatibilityRecommendations(version: string, features?: string[]): string[] {
    const recommendations = [];
    const compatibility = this.checkVersionCompatibility(version);
    
    if (compatibility.compatibilityLevel === 'none') {
      recommendations.push('Upgrade to a supported version');
    } else if (compatibility.compatibilityLevel === 'partial') {
      recommendations.push('Consider upgrading for full compatibility');
    }
    
    if (features && Array.isArray(features)) {
      for (const feature of features) {
        if (!this.isFeatureSupported(feature, version)) {
          recommendations.push(`Feature '${feature}' is not supported in version ${version}`);
        }
      }
    }
    
    if (compatibility.fallbackVersion) {
      recommendations.push(`Consider using version ${compatibility.fallbackVersion} as fallback`);
    }
    
    return recommendations;
  }

  // Create success response with version metadata
  private createSuccessResponse(
    id: string | number,
    result: any,
    context: MCPRequestContext,
    startTime: number,
    compatibility: VersionCompatibilityResult
  ): EnhancedMCPResponse {
    const processingTime = Date.now() - startTime;
    
    return {
      jsonrpc: '2.0',
      id,
      result,
      metadata: {
        version: this.CURRENT_VERSION,
        timestamp: new Date().toISOString(),
        requestId: context.requestId,
        processingTime,
        compatibility
      }
    };
  }

  // Enhanced error response creation
  private createEnhancedErrorResponse(
    id: string | number,
    errorCode: number,
    message: string,
    data: any,
    context: MCPRequestContext,
    startTime: number,
    retryable: boolean = false,
    retryAfter?: number
  ): EnhancedMCPResponse {
    const processingTime = Date.now() - startTime;
    const errorCategory = this.getErrorCategory(errorCode);
    const suggestions = this.generateErrorSuggestions(errorCode, data, context);
    const helpUrl = this.getErrorHelpUrl(errorCode);
    
    return {
      jsonrpc: '2.0',
      id,
      error: {
        code: errorCode,
        message,
        data: {
          ...data,
          category: errorCategory,
          retryable,
          retryAfter,
          suggestions,
          helpUrl,
          requestId: context.requestId,
          timestamp: new Date().toISOString(),
          processingTime
        }
      },
      metadata: {
        version: this.CURRENT_VERSION,
        timestamp: new Date().toISOString(),
        requestId: context.requestId,
        processingTime,
        compatibility: {
          isCompatible: false,
          requestedVersion: 'unknown',
          supportedVersions: this.SUPPORTED_VERSIONS.map(v => v.version),
          compatibilityLevel: 'none',
          warnings: [`Error occurred: ${message}`],
          suggestions: suggestions.slice(0, 3) // Limit suggestions in metadata
        }
      }
    };
  }

  // Get error category
  private getErrorCategory(errorCode: number): string {
    if (this.ERROR_CATEGORIES.CLIENT_ERRORS.includes(errorCode)) {
      return 'client_error';
    } else if (this.ERROR_CATEGORIES.RATE_LIMIT_ERRORS.includes(errorCode)) {
      return 'rate_limit_error';
    } else if (this.ERROR_CATEGORIES.RETRYABLE_ERRORS.includes(errorCode)) {
      return 'retryable_error';
    } else if (this.ERROR_CATEGORIES.SYSTEM_ERRORS.includes(errorCode)) {
      return 'system_error';
    } else {
      return 'unknown_error';
    }
  }

  // Generate error-specific suggestions
  private generateErrorSuggestions(errorCode: number, data: any, context: MCPRequestContext): string[] {
    const suggestions: string[] = [];
    
    switch (errorCode) {
      case this.ERROR_CODES.PARSE_ERROR:
        suggestions.push(
          'Check that your request is valid JSON',
          'Verify the Content-Type header is set to application/json',
          'Ensure no syntax errors in the request body'
        );
        break;
        
      case this.ERROR_CODES.INVALID_REQUEST:
        suggestions.push(
          'Verify all required fields are present',
          'Check field types and formats',
          'Review the MCP protocol specification'
        );
        break;
        
      case this.ERROR_CODES.METHOD_NOT_FOUND:
        suggestions.push(
          'Check the method name spelling',
          'Verify the method is supported in your version',
          'Use /mcp/info to see available methods'
        );
        break;
        
      case this.ERROR_CODES.INVALID_PARAMS:
        suggestions.push(
          'Check parameter types and formats',
          'Verify required parameters are provided',
          'Review method-specific parameter requirements'
        );
        break;
        
      case this.ERROR_CODES.VERSION_NOT_SUPPORTED:
        suggestions.push(
          'Upgrade to a supported version',
          'Check version compatibility',
          'Use backward compatibility mode if available'
        );
        break;
        
      case this.ERROR_CODES.RATE_LIMIT_EXCEEDED:
        suggestions.push(
          'Reduce request frequency',
          'Use batch requests to minimize API calls',
          'Implement exponential backoff in your client'
        );
        break;
        
      case this.ERROR_CODES.VALIDATION_FAILED:
        if (data.errors && Array.isArray(data.errors)) {
          suggestions.push(
            'Fix validation errors listed above',
            'Check parameter requirements',
            'Verify data formats and constraints'
          );
        }
        break;
        
      case this.ERROR_CODES.AUTHENTICATION_FAILED:
        suggestions.push(
          'Check your authentication credentials',
          'Verify API key or token is valid',
          'Ensure proper authorization headers'
        );
        break;
        
      case this.ERROR_CODES.SERVICE_UNAVAILABLE:
        suggestions.push(
          'Try again in a few moments',
          'Check service status page',
          'Contact support if issue persists'
        );
        break;
        
      case this.ERROR_CODES.TIMEOUT_ERROR:
        suggestions.push(
          'Reduce request complexity',
          'Check network connectivity',
          'Consider using async processing for long operations'
        );
        break;
        
      case this.ERROR_CODES.PROJECT_ANALYSIS_FAILED:
        suggestions.push(
          'Check project structure and permissions',
          'Verify project files are accessible',
          'Ensure project meets minimum requirements'
        );
        break;
        
      case this.ERROR_CODES.STANDARDS_VALIDATION_FAILED:
        suggestions.push(
          'Review project against standards',
          'Check for common compliance issues',
          'Use detailed validation reports'
        );
        break;
        
      default:
        suggestions.push(
          'Check the error details above',
          'Review request format and parameters',
          'Contact support if issue persists'
        );
    }
    
    // Add context-specific suggestions
    if (context.priority === 'critical') {
      suggestions.push('Critical requests may have different rate limits and processing rules');
    }
    
    if (context.retryCount > 0) {
      suggestions.push(`This is retry attempt ${context.retryCount + 1}. Consider reviewing the original request.`);
    }
    
    return suggestions;
  }

  // Get help URL for error
  private getErrorHelpUrl(errorCode: number): string {
    const baseUrl = 'https://docs.agentforge.dev/errors';
    
    switch (errorCode) {
      case this.ERROR_CODES.PARSE_ERROR:
        return `${baseUrl}/parse-error`;
      case this.ERROR_CODES.INVALID_REQUEST:
        return `${baseUrl}/invalid-request`;
      case this.ERROR_CODES.METHOD_NOT_FOUND:
        return `${baseUrl}/method-not-found`;
      case this.ERROR_CODES.INVALID_PARAMS:
        return `${baseUrl}/invalid-params`;
      case this.ERROR_CODES.VERSION_NOT_SUPPORTED:
        return `${baseUrl}/version-not-supported`;
      case this.ERROR_CODES.RATE_LIMIT_EXCEEDED:
        return `${baseUrl}/rate-limit-exceeded`;
      case this.ERROR_CODES.VALIDATION_FAILED:
        return `${baseUrl}/validation-failed`;
      case this.ERROR_CODES.AUTHENTICATION_FAILED:
        return `${baseUrl}/authentication-failed`;
      case this.ERROR_CODES.SERVICE_UNAVAILABLE:
        return `${baseUrl}/service-unavailable`;
      case this.ERROR_CODES.TIMEOUT_ERROR:
        return `${baseUrl}/timeout-error`;
      case this.ERROR_CODES.PROJECT_ANALYSIS_FAILED:
        return `${baseUrl}/project-analysis-failed`;
      case this.ERROR_CODES.STANDARDS_VALIDATION_FAILED:
        return `${baseUrl}/standards-validation-failed`;
      default:
        return `${baseUrl}/general`;
    }
  }

  // Enhanced error handling with retry logic
  private async handleEnhancedError(
    error: any,
    request: any,
    context: MCPRequestContext,
    startTime: number
  ): Promise<EnhancedMCPResponse> {
    const errorCode = this.determineErrorCode(error);
    const retryable = this.isErrorRetryable(errorCode);
    const retryAfter = this.calculateRetryAfter(errorCode, context);
    
    // Log error with context
    this.logger.error('Enhanced error handling', {
      requestId: context.requestId,
      method: request.method,
      errorCode,
      error: error instanceof Error ? error.message : 'Unknown error',
      retryable,
      retryAfter,
      context: {
        priority: context.priority,
        retryCount: context.retryCount,
        source: context.source
      }
    });

    // Update error metrics
    this.updateErrorMetrics(request.method, errorCode);

    // Create enhanced error response
    const errorResponse = this.createEnhancedErrorResponse(
      request.id,
      errorCode,
      this.getErrorMessage(errorCode, error),
      this.getErrorData(error, context),
      context,
      startTime,
      retryable,
      retryAfter
    );

    // If retryable and under retry limit, add to retry queue
    if (retryable && context.retryCount < this.maxRetryCount) {
      await this.queueForRetry(request, context, errorCode);
    }

    return errorResponse;
  }

  // Determine error code from error
  private determineErrorCode(error: any): number {
    if (error.code && typeof error.code === 'number') {
      return error.code;
    }
    
    if (error instanceof Error) {
      const message = error.message.toLowerCase();
      
      if (message.includes('validation') || message.includes('invalid')) {
        return this.ERROR_CODES.VALIDATION_FAILED;
      } else if (message.includes('not found')) {
        return this.ERROR_CODES.RESOURCE_NOT_FOUND;
      } else if (message.includes('timeout') || message.includes('timed out')) {
        return this.ERROR_CODES.TIMEOUT_ERROR;
      } else if (message.includes('unauthorized') || message.includes('authentication')) {
        return this.ERROR_CODES.AUTHENTICATION_FAILED;
      } else if (message.includes('forbidden') || message.includes('authorization')) {
        return this.ERROR_CODES.AUTHORIZATION_FAILED;
      } else if (message.includes('rate limit') || message.includes('too many requests')) {
        return this.ERROR_CODES.RATE_LIMIT_EXCEEDED;
      } else if (message.includes('service unavailable') || message.includes('unavailable')) {
        return this.ERROR_CODES.SERVICE_UNAVAILABLE;
      }
    }
    
    return this.ERROR_CODES.INTERNAL_ERROR;
  }

  // Check if error is retryable
  private isErrorRetryable(errorCode: number): boolean {
    return this.ERROR_CATEGORIES.RETRYABLE_ERRORS.includes(errorCode);
  }

  // Calculate retry after delay
  private calculateRetryAfter(errorCode: number, context: MCPRequestContext): number | undefined {
    if (!this.isErrorRetryable(errorCode)) {
      return undefined;
    }
    
    const baseDelay = this.retryDelays[Math.min(context.retryCount, this.retryDelays.length - 1)];
    
    // Adjust delay based on priority
    const priorityMultiplier = {
      'low': 2.0,
      'normal': 1.0,
      'high': 0.5,
      'critical': 0.25
    };
    
    return Math.floor(baseDelay * (priorityMultiplier[context.priority] || 1.0));
  }

  // Get user-friendly error message
  private getErrorMessage(errorCode: number, error: any): string {
    const defaultMessages = {
      [this.ERROR_CODES.PARSE_ERROR]: 'Invalid JSON in request body',
      [this.ERROR_CODES.INVALID_REQUEST]: 'Request format is invalid',
      [this.ERROR_CODES.METHOD_NOT_FOUND]: 'Method not supported',
      [this.ERROR_CODES.INVALID_PARAMS]: 'Invalid parameters provided',
      [this.ERROR_CODES.VERSION_NOT_SUPPORTED]: 'Protocol version not supported',
      [this.ERROR_CODES.RATE_LIMIT_EXCEEDED]: 'Rate limit exceeded',
      [this.ERROR_CODES.CONCURRENT_LIMIT_EXCEEDED]: 'Too many concurrent requests',
      [this.ERROR_CODES.VALIDATION_FAILED]: 'Request validation failed',
      [this.ERROR_CODES.AUTHENTICATION_FAILED]: 'Authentication required',
      [this.ERROR_CODES.AUTHORIZATION_FAILED]: 'Insufficient permissions',
      [this.ERROR_CODES.RESOURCE_NOT_FOUND]: 'Requested resource not found',
      [this.ERROR_CODES.RESOURCE_CONFLICT]: 'Resource conflict detected',
      [this.ERROR_CODES.SERVICE_UNAVAILABLE]: 'Service temporarily unavailable',
      [this.ERROR_CODES.TIMEOUT_ERROR]: 'Request timed out',
      [this.ERROR_CODES.QUOTA_EXCEEDED]: 'Service quota exceeded',
      [this.ERROR_CODES.PROJECT_ANALYSIS_FAILED]: 'Project analysis failed',
      [this.ERROR_CODES.STANDARDS_VALIDATION_FAILED]: 'Standards validation failed',
      [this.ERROR_CODES.COMPLIANCE_CHECK_FAILED]: 'Compliance check failed',
      [this.ERROR_CODES.METRICS_COLLECTION_FAILED]: 'Metrics collection failed',
      [this.ERROR_CODES.CACHE_OPERATION_FAILED]: 'Cache operation failed',
      [this.ERROR_CODES.DATABASE_OPERATION_FAILED]: 'Database operation failed',
      [this.ERROR_CODES.BATCH_PROCESSING_FAILED]: 'Batch processing failed',
      [this.ERROR_CODES.PRIORITY_QUEUE_FULL]: 'Priority queue is full',
      [this.ERROR_CODES.RETRY_LIMIT_EXCEEDED]: 'Maximum retry attempts exceeded'
    };
    
    return defaultMessages[errorCode] || 'An unexpected error occurred';
  }

  // Get error data for response
  private getErrorData(error: any, context: MCPRequestContext): any {
    const baseData: any = {
      timestamp: new Date().toISOString(),
      requestId: context.requestId,
      priority: context.priority,
      retryCount: context.retryCount
    };
    
    if (error instanceof Error) {
      baseData.errorType = error.constructor.name;
      baseData.message = error.message;
      
      // Include stack trace in development
      if (process.env.NODE_ENV === 'development') {
        baseData.stack = error.stack;
      }
    } else if (typeof error === 'object' && error !== null) {
      baseData.errorType = 'ObjectError';
      baseData.details = error;
    } else {
      baseData.errorType = 'UnknownError';
      baseData.value = error;
    }
    
    return baseData;
  }

  // Queue request for retry
  private async queueForRetry(request: any, context: MCPRequestContext, errorCode: number): Promise<void> {
    const retryContext = {
      ...context,
      retryCount: context.retryCount + 1,
      lastError: errorCode,
      retryTimestamp: new Date().toISOString()
    };
    
    const retryItem: PriorityQueueItem = {
      request: {
        ...request,
        context: retryContext
      },
      priority: this.getPriorityValue(context.priority),
      timestamp: Date.now(),
      retryCount: retryContext.retryCount
    };
    
    // Add to priority queue
    this.priorityQueue.push(retryItem);
    this.priorityQueue.sort((a, b) => b.priority - a.priority);
    
    this.logger.info('Request queued for retry', {
      requestId: context.requestId,
      method: request.method,
      retryCount: retryContext.retryCount,
      priority: context.priority,
      estimatedDelay: this.calculateRetryAfter(errorCode, retryContext)
    });
  }

  // Update error metrics
  private updateErrorMetrics(method: string, errorCode: number): void {
    const key = `${method}_${errorCode}`;
    const currentCount = this.errorCounts.get(key) || 0;
    this.errorCounts.set(key, currentCount + 1);
  }

  // Get error statistics
  getErrorStats(): {
    totalErrors: number;
    errorsByMethod: Record<string, number>;
    errorsByCode: Record<number, number>;
    retryStats: { totalRetries: number; successfulRetries: number; failedRetries: number };
  } {
    const errorsByMethod = new Map<string, number>();
    const errorsByCode = new Map<number, number>();
    
    // Aggregate error counts
    for (const [key, count] of this.errorCounts.entries()) {
      const [method, errorCode] = key.split('_');
      const code = parseInt(errorCode);
      
      errorsByMethod.set(method, (errorsByMethod.get(method) || 0) + count);
      errorsByCode.set(code, (errorsByCode.get(code) || 0) + count);
    }
    
    return {
      totalErrors: Array.from(this.errorCounts.values()).reduce((sum, count) => sum + count, 0),
      errorsByMethod: Object.fromEntries(errorsByMethod),
      errorsByCode: Object.fromEntries(errorsByCode),
      retryStats: {
        totalRetries: this.priorityQueue.filter(item => item.retryCount > 0).length,
        successfulRetries: 0, // Would need to track successful retries
        failedRetries: this.priorityQueue.filter(item => item.retryCount >= this.maxRetryCount).length
      }
    };
  }
}
