import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import rateLimit from 'express-rate-limit';
import { createLogger, format, transports } from 'winston';
import { MCPController } from './controllers/mcpController';
import { MCPValidationMiddleware } from './middleware/mcpValidation';
import { DatabaseService } from './services/databaseService';
import { EnhancedMCPService } from './services/enhancedMCPService';

// Enhanced MCP Server Configuration
interface MCPServerConfig {
  port: number;
  host: string;
  cors: {
    origin: string | string[];
    credentials: boolean;
  };
  rateLimit: {
    windowMs: number;
    max: number;
    message: string;
  };
  compression: {
    level: number;
    threshold: number;
  };
  logging: {
    level: string;
    format: string;
  };
  mcp: {
    maxBatchSize: number;
    maxConcurrentRequests: number;
    maxRetryCount: number;
    enablePriorityQueuing: boolean;
    enableRetryMechanism: boolean;
    enableAdvancedMetrics: boolean;
  };
}

// Default configuration
const defaultConfig: MCPServerConfig = {
  port: parseInt(process.env.PORT || '3000'),
  host: process.env.HOST || '0.0.0.0',
  cors: {
    origin: process.env.CORS_ORIGIN ? process.env.CORS_ORIGIN.split(',') : ['http://localhost:3000', 'http://localhost:5173'],
    credentials: true
  },
  rateLimit: {
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // limit each IP to 100 requests per windowMs
    message: 'Too many requests from this IP, please try again later.'
  },
  compression: {
    level: 6,
    threshold: 1024
  },
  logging: {
    level: process.env.LOG_LEVEL || 'info',
    format: process.env.LOG_FORMAT || 'json'
  },
  mcp: {
    maxBatchSize: parseInt(process.env.MCP_MAX_BATCH_SIZE || '100'),
    maxConcurrentRequests: parseInt(process.env.MCP_MAX_CONCURRENT || '50'),
    maxRetryCount: parseInt(process.env.MCP_MAX_RETRY || '5'),
    enablePriorityQueuing: process.env.MCP_ENABLE_PRIORITY_QUEUING === 'true',
    enableRetryMechanism: process.env.MCP_ENABLE_RETRY_MECHANISM === 'true',
    enableAdvancedMetrics: process.env.MCP_ENABLE_ADVANCED_METRICS === 'true'
  }
};

// Enhanced logger configuration
const createEnhancedLogger = (config: MCPServerConfig) => {
  const logFormat = config.logging.format === 'json' 
    ? format.json() 
    : format.combine(
        format.timestamp(),
        format.errors({ stack: true }),
        format.splat(),
        format.json()
      );

  return createLogger({
    level: config.logging.level,
    format: logFormat,
    defaultMeta: { 
      service: 'mcp-server',
      version: process.env.npm_package_version || '1.0.0',
      environment: process.env.NODE_ENV || 'development'
    },
    transports: [
      new transports.Console({
        format: format.combine(
          format.colorize(),
          format.timestamp(),
          format.printf(({ timestamp, level, message, ...meta }) => {
            return `${timestamp} [${level}]: ${message} ${Object.keys(meta).length ? JSON.stringify(meta, null, 2) : ''}`;
          })
        )
      }),
      new transports.File({ 
        filename: 'logs/error.log', 
        level: 'error',
        maxsize: 5242880, // 5MB
        maxFiles: 5
      }),
      new transports.File({ 
        filename: 'logs/combined.log',
        maxsize: 5242880, // 5MB
        maxFiles: 5
      })
    ],
    exceptionHandlers: [
      new transports.File({ filename: 'logs/exceptions.log' })
    ],
    rejectionHandlers: [
      new transports.File({ filename: 'logs/rejections.log' })
    ]
  });
};

// Enhanced MCP Server class
class EnhancedMCPServer {
  private app: express.Application;
  private server: any;
  private logger: any;
  private config: MCPServerConfig;
  private mcpValidation: MCPValidationMiddleware;
  private mcpController: MCPController;
  private enhancedMCPService: EnhancedMCPService;
  private databaseService: DatabaseService;
  private isShuttingDown: boolean = false;

  constructor(config: MCPServerConfig = defaultConfig) {
    this.config = config;
    this.logger = createEnhancedLogger(config);
    this.app = express();
    
    // Initialize services
    this.databaseService = new DatabaseService(this.logger);
    this.enhancedMCPService = new EnhancedMCPService(this.logger, this.databaseService);
    this.mcpController = new MCPController(this.enhancedMCPService, this.logger);
    this.mcpValidation = new MCPValidationMiddleware(this.logger);
    
    this.setupMiddleware();
    this.setupRoutes();
    this.setupErrorHandling();
    this.setupGracefulShutdown();
  }

  // Enhanced middleware setup
  private setupMiddleware(): void {
    // Security middleware
    this.app.use(helmet({
      contentSecurityPolicy: {
        directives: {
          defaultSrc: ["'self'"],
          styleSrc: ["'self'", "'unsafe-inline'"],
          scriptSrc: ["'self'"],
          imgSrc: ["'self'", "data:", "https:"],
        },
      },
      crossOriginEmbedderPolicy: false,
      crossOriginResourcePolicy: { policy: "cross-origin" }
    }));

    // CORS configuration
    this.app.use(cors({
      origin: this.config.cors.origin,
      credentials: this.config.cors.credentials,
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
      allowedHeaders: [
        'Content-Type', 
        'Authorization', 
        'X-Request-ID', 
        'X-Correlation-ID', 
        'X-Priority', 
        'X-Retry-Count', 
        'X-Source', 
        'X-MCP-Version'
      ]
    }));

    // Rate limiting
    this.app.use(rateLimit({
      windowMs: this.config.rateLimit.windowMs,
      max: this.config.rateLimit.max,
      message: {
        error: 'Rate limit exceeded',
        message: this.config.rateLimit.message,
        retryAfter: Math.ceil(this.config.rateLimit.windowMs / 1000)
      },
      standardHeaders: true,
      legacyHeaders: false,
      keyGenerator: (req) => {
        // Use IP and user agent for more granular rate limiting
        return `${req.ip}-${req.get('User-Agent') || 'unknown'}`;
      }
    }));

    // Compression
    this.app.use(compression({
      level: this.config.compression.level,
      threshold: this.config.compression.threshold,
      filter: (req, res) => {
        // Don't compress MCP requests/responses if they're already compressed
        if (req.headers['content-encoding'] === 'gzip') {
          return false;
        }
        return compression.filter(req, res);
      }
    }));

    // Body parsing
    this.app.use(express.json({ 
      limit: '10mb',
      verify: (req, res, buf) => {
        // Store raw body for validation
        (req as any).rawBody = buf;
      }
    }));
    this.app.use(express.urlencoded({ extended: true, limit: '10mb' }));

    // Request logging middleware
    this.app.use((req, res, next) => {
      const startTime = Date.now();
      const requestId = req.headers['x-request-id'] as string || 
                       `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
      
      // Add request ID to response headers
      res.setHeader('X-Request-ID', requestId);
      
      // Log request start
      this.logger.info('Incoming request', {
        requestId,
        method: req.method,
        url: req.url,
        ip: req.ip,
        userAgent: req.get('User-Agent'),
        contentType: req.get('Content-Type'),
        contentLength: req.get('Content-Length'),
        timestamp: new Date().toISOString()
      });

      // Override res.end to log response
      const originalEnd = res.end;
      res.end = function(chunk?: any, encoding?: any) {
        const duration = Date.now() - startTime;
        
        this.logger.info('Request completed', {
          requestId,
          method: req.method,
          url: req.url,
          statusCode: res.statusCode,
          duration: `${duration}ms`,
          contentLength: res.get('Content-Length'),
          timestamp: new Date().toISOString()
        });

        originalEnd.call(this, chunk, encoding);
      }.bind(this);

      next();
    });

    // MCP-specific middleware
    this.app.use('/mcp', (req, res, next) => {
      // Add MCP-specific headers
      res.setHeader('X-MCP-Server', 'AgentForge MCP Server');
      res.setHeader('X-MCP-Version', '2.0');
      res.setHeader('X-MCP-Features', [
        'batch-requests',
        'priority-queuing',
        'retry-mechanism',
        'advanced-metrics',
        'protocol-versioning'
      ].join(', '));
      
      next();
    });

    // MCP validation middleware
    this.app.use('/mcp', this.mcpValidation.validateRequest);
    this.app.use('/mcp/batch', this.mcpValidation.validateBatchRequest);
    
    // Cleanup middleware
    this.app.use(this.mcpValidation.cleanupRequest);
  }

  // Enhanced route setup
  private setupRoutes(): void {
    // Health check endpoint
    this.app.get('/health', (req, res) => {
      res.json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        version: process.env.npm_package_version || '1.0.0',
        environment: process.env.NODE_ENV || 'development',
        mcp: {
          features: {
            priorityQueuing: this.config.mcp.enablePriorityQueuing,
            retryMechanism: this.config.mcp.enableRetryMechanism,
            advancedMetrics: this.config.mcp.enableAdvancedMetrics
          },
          limits: {
            maxBatchSize: this.config.mcp.maxBatchSize,
            maxConcurrentRequests: this.config.mcp.maxConcurrentRequests,
            maxRetryCount: this.config.mcp.maxRetryCount
          }
        }
      });
    });

    // MCP server info endpoint
    this.app.get('/mcp/info', (req, res) => {
      res.json({
        jsonrpc: '2.0',
        id: req.query.id || 'info',
        result: {
          server: 'AgentForge MCP Server',
          version: process.env.npm_package_version || '1.0.0',
          protocol: 'MCP 2.0',
          features: [
            'Advanced message handling',
            'Priority-based queuing',
            'Retry mechanisms',
            'Batch request processing',
            'Advanced metrics and monitoring',
            'Protocol versioning support',
            'Enhanced validation and rate limiting'
          ],
          methods: [
            'getContext',
            'getStandards', 
            'validateCompliance',
            'analyzeProject',
            'getMetrics',
            'getBatchStats',
            'getPerformanceMetrics'
          ],
          limits: {
            maxBatchSize: this.config.mcp.maxBatchSize,
            maxConcurrentRequests: this.config.mcp.maxConcurrentRequests,
            maxRetryCount: this.config.mcp.maxRetryCount
          },
          timestamp: new Date().toISOString()
        }
      });
    });

    // MCP validation stats endpoint
    this.app.get('/mcp/stats', (req, res) => {
      const stats = this.mcpValidation.getValidationStats();
      res.json({
        jsonrpc: '2.0',
        id: req.query.id || 'stats',
        result: {
          validation: stats,
          server: {
            uptime: process.uptime(),
            memory: process.memoryUsage(),
            cpu: process.cpuUsage()
          },
          timestamp: new Date().toISOString()
        }
      });
    });

    // MCP routes
    this.app.use('/mcp', this.mcpController.router);

    // 404 handler
    this.app.use('*', (req, res) => {
      res.status(404).json({
        jsonrpc: '2.0',
        id: req.body?.id || 'unknown',
        error: {
          code: -32601,
          message: 'Method not found',
          data: {
            requestedUrl: req.originalUrl,
            availableEndpoints: [
              '/health',
              '/mcp/info',
              '/mcp/stats',
              '/mcp/*'
            ]
          }
        }
      });
    });
  }

  // Enhanced error handling
  private setupErrorHandling(): void {
    // Global error handler
    this.app.use((error: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
      this.logger.error('Unhandled error:', {
        error: error.message,
        stack: error.stack,
        requestId: req.headers['x-request-id'],
        method: req.method,
        url: req.url,
        ip: req.ip
      });

      // Don't leak error details in production
      const isDevelopment = process.env.NODE_ENV === 'development';
      
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body?.id || 'unknown',
        error: {
          code: -32603,
          message: 'Internal error',
          data: isDevelopment ? {
            error: error.message,
            stack: error.stack
          } : {
            message: 'An internal server error occurred'
          }
        }
      });
    });

    // Unhandled promise rejection handler
    process.on('unhandledRejection', (reason, promise) => {
      this.logger.error('Unhandled Rejection at:', {
        promise,
        reason,
        stack: new Error().stack
      });
    });

    // Uncaught exception handler
    process.on('uncaughtException', (error) => {
      this.logger.error('Uncaught Exception:', {
        error: error.message,
        stack: error.stack
      });
      
      // Gracefully shutdown the server
      this.shutdown();
    });
  }

  // Graceful shutdown setup
  private setupGracefulShutdown(): void {
    const gracefulShutdown = (signal: string) => {
      this.logger.info(`Received ${signal}. Starting graceful shutdown...`);
      this.isShuttingDown = true;
      
      this.shutdown();
    };

    process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
    process.on('SIGINT', () => gracefulShutdown('SIGINT'));
  }

  // Enhanced shutdown method
  private async shutdown(): Promise<void> {
    try {
      this.logger.info('Shutting down MCP server...');
      
      // Close HTTP server
      if (this.server) {
        await new Promise<void>((resolve) => {
          this.server.close(() => {
            this.logger.info('HTTP server closed');
            resolve();
          });
        });
      }

      // Close database connections
      if (this.databaseService) {
        await this.databaseService.close();
        this.logger.info('Database connections closed');
      }

      // Clear rate limits and active requests
      if (this.mcpValidation) {
        this.mcpValidation.clearRateLimits();
        this.logger.info('Rate limits cleared');
      }

      this.logger.info('MCP server shutdown complete');
      process.exit(0);
      
    } catch (error) {
      this.logger.error('Error during shutdown:', error);
      process.exit(1);
    }
  }

  // Start the server
  async start(): Promise<void> {
    try {
      // Initialize database
      await this.databaseService.initialize();
      this.logger.info('Database initialized successfully');

      // Update MCP validation configuration
      this.mcpValidation.updateRateLimitConfig(
        this.config.mcp.maxRetryCount,
        this.config.mcp.maxBatchSize,
        this.config.mcp.maxConcurrentRequests
      );

      // Start HTTP server
      this.server = this.app.listen(this.config.port, this.config.host, () => {
        this.logger.info(`MCP Server started successfully`, {
          host: this.config.host,
          port: this.config.port,
          environment: process.env.NODE_ENV || 'development',
          version: process.env.npm_package_version || '1.0.0',
          features: {
            priorityQueuing: this.config.mcp.enablePriorityQueuing,
            retryMechanism: this.config.mcp.enableRetryMechanism,
            advancedMetrics: this.config.mcp.enableAdvancedMetrics
          }
        });
      });

      // Handle server errors
      this.server.on('error', (error: any) => {
        if (error.syscall !== 'listen') {
          throw error;
        }

        const bind = typeof this.config.port === 'string' ? 'Pipe ' + this.config.port : 'Port ' + this.config.port;

        switch (error.code) {
          case 'EACCES':
            this.logger.error(`${bind} requires elevated privileges`);
            process.exit(1);
            break;
          case 'EADDRINUSE':
            this.logger.error(`${bind} is already in use`);
            process.exit(1);
            break;
          default:
            throw error;
        }
      });

    } catch (error) {
      this.logger.error('Failed to start MCP server:', error);
      throw error;
    }
  }

  // Get server status
  getStatus(): {
    isRunning: boolean;
    isShuttingDown: boolean;
    uptime: number;
    config: MCPServerConfig;
  } {
    return {
      isRunning: !!this.server,
      isShuttingDown: this.isShuttingDown,
      uptime: process.uptime(),
      config: this.config
    };
  }
}

// Create and start server
const server = new EnhancedMCPServer();

server.start().catch((error) => {
  console.error('Failed to start server:', error);
  process.exit(1);
});

export default server;
