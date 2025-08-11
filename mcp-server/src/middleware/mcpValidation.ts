import { Request, Response, NextFunction } from 'express';
import { Logger } from 'winston';

// Enhanced MCP validation interfaces
export interface MCPValidationContext {
  requestId: string;
  timestamp: string;
  ip: string;
  userAgent?: string;
  method: string;
  priority: 'low' | 'normal' | 'high' | 'critical';
  correlationId?: string;
  retryCount: number;
  source?: string;
  version?: string;
}

export interface MCPValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  context: MCPValidationContext;
  suggestions: string[];
}

export interface MCPRateLimitInfo {
  current: number;
  limit: number;
  resetTime: number;
  remaining: number;
}

// Enhanced validation rules
interface ValidationRule {
  name: string;
  validator: (data: any, context: MCPValidationContext) => boolean | string;
  severity: 'error' | 'warning';
  description: string;
}

export class MCPValidationMiddleware {
  private logger: Logger;
  private rateLimitStore: Map<string, { count: number; resetTime: number; limit: number }> = new Map();
  private validationRules: ValidationRule[] = [];
  private maxRetryCount: number = 5;
  private maxBatchSize: number = 100;
  private maxConcurrentRequests: number = 50;
  private activeRequests: Set<string> = new Set();

  constructor(logger: Logger) {
    this.logger = logger;
    this.initializeValidationRules();
  }

  // Enhanced validation middleware
  validateRequest = (req: Request, res: Response, next: NextFunction): void => {
    const startTime = Date.now();
    
    try {
      // Create validation context
      const context = this.createValidationContext(req);
      
      // Perform comprehensive validation
      const validationResult = this.performValidation(req.body, context);
      
      if (!validationResult.isValid) {
        this.logger.warn('MCP request validation failed', {
          requestId: context.requestId,
          method: context.method,
          errors: validationResult.errors,
          warnings: validationResult.warnings
        });

        return res.status(400).json({
          jsonrpc: '2.0',
          id: req.body.id || 'unknown',
          error: {
            code: -32600,
            message: 'Invalid Request',
            data: {
              errors: validationResult.errors,
              warnings: validationResult.warnings,
              suggestions: validationResult.suggestions,
              context: {
                requestId: context.requestId,
                timestamp: context.timestamp,
                priority: context.priority
              }
            }
          }
        });
      }

      // Check rate limiting
      const rateLimitResult = this.checkRateLimit(context);
      if (!rateLimitResult.allowed) {
        this.logger.warn('Rate limit exceeded', {
          requestId: context.requestId,
          method: context.method,
          ip: context.ip,
          current: rateLimitResult.info.current,
          limit: rateLimitResult.info.limit
        });

        return res.status(429).json({
          jsonrpc: '2.0',
          id: req.body.id || 'unknown',
          error: {
            code: -32000,
            message: 'Rate limit exceeded',
            data: {
              retryAfter: Math.ceil((rateLimitResult.info.resetTime - Date.now()) / 1000),
              limit: rateLimitResult.info.limit,
              current: rateLimitResult.info.current,
              resetTime: new Date(rateLimitResult.info.resetTime).toISOString()
            }
          }
        });
      }

      // Check concurrent request limits
      if (!this.checkConcurrentLimit(context)) {
        this.logger.warn('Concurrent request limit exceeded', {
          requestId: context.requestId,
          method: context.method,
          activeRequests: this.activeRequests.size,
          maxConcurrent: this.maxConcurrentRequests
        });

        return res.status(503).json({
          jsonrpc: '2.0',
          id: req.body.id || 'unknown',
          error: {
            code: -32001,
            message: 'Service temporarily unavailable',
            data: {
              reason: 'Concurrent request limit exceeded',
              activeRequests: this.activeRequests.size,
              maxConcurrent: this.maxConcurrentRequests,
              retryAfter: 30
            }
          }
        });
      }

      // Add validation context to request
      req.mcpContext = context;
      
      // Track active request
      this.activeRequests.add(context.requestId);
      
      // Log successful validation
      const validationTime = Date.now() - startTime;
      this.logger.info('MCP request validation successful', {
        requestId: context.requestId,
        method: context.method,
        priority: context.priority,
        validationTime,
        warnings: validationResult.warnings.length
      });

      next();

    } catch (error) {
      this.logger.error('MCP validation middleware error:', error);
      
      return res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id || 'unknown',
        error: {
          code: -32603,
          message: 'Internal error',
          data: {
            reason: 'Validation middleware error',
            error: error instanceof Error ? error.message : 'Unknown error'
          }
        }
      });
    }
  };

  // Enhanced batch validation middleware
  validateBatchRequest = (req: Request, res: Response, next: NextFunction): void => {
    const startTime = Date.now();
    
    try {
      const batchData = req.body;
      
      // Validate batch structure
      if (!Array.isArray(batchData)) {
        return res.status(400).json({
          jsonrpc: '2.0',
          id: 'batch',
          error: {
            code: -32600,
            message: 'Invalid Request',
            data: {
              reason: 'Batch request must be an array',
              expected: 'array',
              received: typeof batchData
            }
          }
        });
      }

      // Check batch size limits
      if (batchData.length > this.maxBatchSize) {
        return res.status(400).json({
          jsonrpc: '2.0',
          id: 'batch',
          error: {
            code: -32600,
            message: 'Invalid Request',
            data: {
              reason: 'Batch size exceeds limit',
              current: batchData.length,
              maxAllowed: this.maxBatchSize,
              suggestion: 'Split into smaller batches'
            }
          }
        });
      }

      // Validate each request in the batch
      const batchValidationResults: MCPValidationResult[] = [];
      const batchErrors: string[] = [];
      
      for (let i = 0; i < batchData.length; i++) {
        const request = batchData[i];
        const context = this.createValidationContext(req, i.toString());
        
        const validationResult = this.performValidation(request, context);
        batchValidationResults.push(validationResult);
        
        if (!validationResult.isValid) {
          batchErrors.push(`Request ${i}: ${validationResult.errors.join(', ')}`);
        }
      }

      // If any request in batch is invalid, reject entire batch
      if (batchErrors.length > 0) {
        this.logger.warn('Batch request validation failed', {
          batchSize: batchData.length,
          errorCount: batchErrors.length,
          errors: batchErrors
        });

        return res.status(400).json({
          jsonrpc: '2.0',
          id: 'batch',
          error: {
            code: -32600,
            message: 'Invalid Request',
            data: {
              reason: 'One or more requests in batch failed validation',
              batchSize: batchData.length,
              errorCount: batchErrors.length,
              errors: batchErrors,
              suggestions: [
                'Fix validation errors in individual requests',
                'Ensure all requests follow MCP protocol',
                'Check parameter requirements for each method'
              ]
            }
          }
        });
      }

      // Add batch validation context to request
      req.mcpBatchContext = {
        batchSize: batchData.length,
        validationResults: batchValidationResults,
        timestamp: new Date().toISOString()
      };

      // Log successful batch validation
      const validationTime = Date.now() - startTime;
      this.logger.info('Batch request validation successful', {
        batchSize: batchData.length,
        validationTime,
        totalWarnings: batchValidationResults.reduce((sum, r) => sum + r.warnings.length, 0)
      });

      next();

    } catch (error) {
      this.logger.error('Batch validation middleware error:', error);
      
      return res.status(500).json({
        jsonrpc: '2.0',
        id: 'batch',
        error: {
          code: -32603,
          message: 'Internal error',
          data: {
            reason: 'Batch validation middleware error',
            error: error instanceof Error ? error.message : 'Unknown error'
          }
        }
      });
    }
  };

  // Cleanup middleware to remove tracked requests
  cleanupRequest = (req: Request, res: Response, next: NextFunction): void => {
    res.on('finish', () => {
      if (req.mcpContext?.requestId) {
        this.activeRequests.delete(req.mcpContext.requestId);
      }
    });
    next();
  };

  // Enhanced validation logic
  private performValidation(data: any, context: MCPValidationContext): MCPValidationResult {
    const errors: string[] = [];
    const warnings: string[] = [];
    const suggestions: string[] = [];

    // Run all validation rules
    for (const rule of this.validationRules) {
      try {
        const result = rule.validator(data, context);
        
        if (typeof result === 'string') {
          if (rule.severity === 'error') {
            errors.push(`${rule.name}: ${result}`);
          } else {
            warnings.push(`${rule.name}: ${result}`);
          }
        } else if (!result) {
          if (rule.severity === 'error') {
            errors.push(`${rule.name}: ${rule.description}`);
          } else {
            warnings.push(`${rule.name}: ${rule.description}`);
          }
        }
      } catch (error) {
        this.logger.error(`Validation rule error for ${rule.name}:`, error);
        errors.push(`${rule.name}: Validation rule execution failed`);
      }
    }

    // Generate suggestions based on errors
    if (errors.length > 0) {
      suggestions.push(...this.generateSuggestions(errors, data));
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings,
      context,
      suggestions
    };
  }

  // Enhanced rate limiting with priority-based limits
  private checkRateLimit(context: MCPValidationContext): { allowed: boolean; info: MCPRateLimitInfo } {
    const key = `rate_limit:${context.method}:${context.ip}`;
    const now = Date.now();
    
    // Get or create rate limit entry
    let entry = this.rateLimitStore.get(key);
    if (!entry || now > entry.resetTime) {
      // Priority-based rate limits
      const limits = {
        'low': 10,      // 10 requests per minute
        'normal': 30,   // 30 requests per minute
        'high': 60,     // 60 requests per minute
        'critical': 100 // 100 requests per minute
      };
      
      entry = {
        count: 0,
        resetTime: now + 60000, // 1 minute
        limit: limits[context.priority] || limits.normal
      };
      this.rateLimitStore.set(key, entry);
    }

    // Check if limit exceeded
    if (entry.count >= entry.limit) {
      return {
        allowed: false,
        info: {
          current: entry.count,
          limit: entry.limit,
          resetTime: entry.resetTime,
          remaining: 0
        }
      };
    }

    // Increment count
    entry.count++;

    return {
      allowed: true,
      info: {
        current: entry.count,
        limit: entry.limit,
        resetTime: entry.resetTime,
        remaining: entry.limit - entry.count
      }
    };
  }

  // Concurrent request limit checking
  private checkConcurrentLimit(context: MCPValidationContext): boolean {
    // Critical requests bypass concurrent limits
    if (context.priority === 'critical') {
      return true;
    }

    return this.activeRequests.size < this.maxConcurrentRequests;
  }

  // Context creation with enhanced metadata
  private createValidationContext(req: Request, requestIndex?: string): MCPValidationContext {
    const requestId = req.headers['x-request-id'] as string || 
                     `val_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    
    const priority = this.extractPriority(req) || 'normal';
    const correlationId = req.headers['x-correlation-id'] as string;
    const retryCount = parseInt(req.headers['x-retry-count'] as string) || 0;
    const source = req.headers['x-source'] as string;
    const version = req.headers['x-mcp-version'] as string || '2.0';

    return {
      requestId: requestIndex ? `${requestId}_${requestIndex}` : requestId,
      timestamp: new Date().toISOString(),
      ip: req.ip || req.connection.remoteAddress || 'unknown',
      userAgent: req.get('User-Agent'),
      method: req.body?.method || 'unknown',
      priority,
      correlationId,
      retryCount: Math.min(retryCount, this.maxRetryCount),
      source,
      version
    };
  }

  // Priority extraction from request
  private extractPriority(req: Request): 'low' | 'normal' | 'high' | 'critical' | undefined {
    const priorityHeader = req.headers['x-priority'] as string;
    const validPriorities = ['low', 'normal', 'high', 'critical'];
    
    if (priorityHeader && validPriorities.includes(priorityHeader)) {
      return priorityHeader as 'low' | 'normal' | 'high' | 'critical';
    }

    // Extract from request body if available
    if (req.body?.metadata?.priority && validPriorities.includes(req.body.metadata.priority)) {
      return req.body.metadata.priority;
    }

    return undefined;
  }

  // Suggestion generation based on validation errors
  private generateSuggestions(errors: string[], data: any): string[] {
    const suggestions: string[] = [];

    if (errors.some(e => e.includes('jsonrpc'))) {
      suggestions.push('Ensure jsonrpc field is set to "2.0"');
    }

    if (errors.some(e => e.includes('method'))) {
      suggestions.push('Check that method field contains a valid MCP method name');
    }

    if (errors.some(e => e.includes('id'))) {
      suggestions.push('Include a unique id field for request tracking');
    }

    if (errors.some(e => e.includes('params'))) {
      suggestions.push('Verify parameter structure matches method requirements');
    }

    if (errors.some(e => e.includes('priority'))) {
      suggestions.push('Set priority to one of: low, normal, high, critical');
    }

    if (errors.some(e => e.includes('retry'))) {
      suggestions.push('Ensure retry count does not exceed maximum allowed');
    }

    return suggestions;
  }

  // Initialize comprehensive validation rules
  private initializeValidationRules(): void {
    this.validationRules = [
      // JSON-RPC 2.0 compliance
      {
        name: 'JSON-RPC Version',
        validator: (data: any) => data.jsonrpc === '2.0',
        severity: 'error',
        description: 'jsonrpc field must be "2.0"'
      },
      {
        name: 'Request ID',
        validator: (data: any) => data.id !== undefined && data.id !== null,
        severity: 'error',
        description: 'id field is required'
      },
      {
        name: 'Method',
        validator: (data: any) => typeof data.method === 'string' && data.method.length > 0,
        severity: 'error',
        description: 'method field must be a non-empty string'
      },
      {
        name: 'Valid Method Names',
        validator: (data: any) => {
          const validMethods = [
            'getContext', 'getStandards', 'validateCompliance', 
            'analyzeProject', 'getMetrics', 'getBatchStats', 
            'getPerformanceMetrics'
          ];
          return validMethods.includes(data.method);
        },
        severity: 'error',
        description: 'method must be a valid MCP method'
      },

      // Enhanced MCP protocol features
      {
        name: 'Priority Validation',
        validator: (data: any) => {
          if (!data.metadata?.priority) return true; // Optional
          const validPriorities = ['low', 'normal', 'high', 'critical'];
          return validPriorities.includes(data.metadata.priority);
        },
        severity: 'error',
        description: 'priority must be one of: low, normal, high, critical'
      },
      {
        name: 'Retry Count Validation',
        validator: (data: any) => {
          if (!data.metadata?.retryCount) return true; // Optional
          const retryCount = parseInt(data.metadata.retryCount);
          return !isNaN(retryCount) && retryCount >= 0 && retryCount <= this.maxRetryCount;
        },
        severity: 'error',
        description: `retry count must be between 0 and ${this.maxRetryCount}`
      },
      {
        name: 'Correlation ID Format',
        validator: (data: any) => {
          if (!data.metadata?.correlationId) return true; // Optional
          return typeof data.metadata.correlationId === 'string' && data.metadata.correlationId.length > 0;
        },
        severity: 'warning',
        description: 'correlation ID should be a non-empty string'
      },
      {
        name: 'Source Validation',
        validator: (data: any) => {
          if (!data.metadata?.source) return true; // Optional
          return typeof data.metadata.source === 'string' && data.metadata.source.length > 0;
        },
        severity: 'warning',
        description: 'source should be a non-empty string'
      },
      {
        name: 'Version Format',
        validator: (data: any) => {
          if (!data.metadata?.version) return true; // Optional
          return typeof data.metadata.version === 'string' && /^\d+\.\d+(\.\d+)?$/.test(data.metadata.version);
        },
        severity: 'warning',
        description: 'version should follow semantic versioning format (e.g., "1.0.0")'
      },

      // Parameter validation
      {
        name: 'Parameters Type',
        validator: (data: any) => {
          if (data.params === undefined) return true; // Optional
          return typeof data.params === 'object' && data.params !== null;
        },
        severity: 'error',
        description: 'params must be an object if provided'
      },
      {
        name: 'Required Parameters',
        validator: (data: any) => {
          // This would be method-specific validation
          // For now, just ensure params is valid if method requires it
          return true;
        },
        severity: 'warning',
        description: 'check method-specific parameter requirements'
      },

      // Metadata structure validation
      {
        name: 'Metadata Structure',
        validator: (data: any) => {
          if (!data.metadata) return true; // Optional
          return typeof data.metadata === 'object' && data.metadata !== null;
        },
        severity: 'error',
        description: 'metadata must be an object if provided'
      }
    ];
  }

  // Public methods for external access
  getValidationStats(): {
    activeRequests: number;
    maxConcurrent: number;
    rateLimitEntries: number;
    validationRules: number;
  } {
    return {
      activeRequests: this.activeRequests.size,
      maxConcurrent: this.maxConcurrentRequests,
      rateLimitEntries: this.rateLimitStore.size,
      validationRules: this.validationRules.length
    };
  }

  clearRateLimits(): void {
    this.rateLimitStore.clear();
    this.logger.info('Rate limit store cleared');
  }

  updateRateLimitConfig(maxRetryCount: number, maxBatchSize: number, maxConcurrentRequests: number): void {
    this.maxRetryCount = maxRetryCount;
    this.maxBatchSize = maxBatchSize;
    this.maxConcurrentRequests = maxConcurrentRequests;
    
    this.logger.info('Rate limit configuration updated', {
      maxRetryCount,
      maxBatchSize,
      maxConcurrentRequests
    });
  }
}

// Extend Express Request interface
declare global {
  namespace Express {
    interface Request {
      mcpContext?: MCPValidationContext;
      mcpBatchContext?: {
        batchSize: number;
        validationResults: MCPValidationResult[];
        timestamp: string;
      };
    }
  }
}

export default MCPValidationMiddleware;
