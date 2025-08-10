import { Request, Response, Router } from 'express';
import { MCPService } from '../services/mcpService';
import { Logger } from 'winston';

export class MCPController {
  public router: Router;
  private mcpService: MCPService;
  private logger: Logger;

  constructor(mcpService: MCPService, logger: Logger) {
    this.mcpService = mcpService;
    this.logger = logger;
    this.router = Router();
    this.initializeRoutes();
  }

  private initializeRoutes(): void {
    // MCP Protocol endpoints
    this.router.post('/context', this.getContext.bind(this));
    this.router.post('/standards', this.getStandards.bind(this));
    this.router.post('/compliance', this.validateCompliance.bind(this));
    this.router.post('/analyze', this.analyzeProject.bind(this));
    this.router.post('/metrics', this.getMetrics.bind(this));
    
    // MCP Protocol info
    this.router.get('/protocol', this.getProtocolInfo.bind(this));
    this.router.get('/capabilities', this.getCapabilities.bind(this));
  }

  private async getContext(req: Request, res: Response): Promise<void> {
    try {
      this.logger.info('getContext called', { body: req.body });
      
      const result = await this.mcpService.getContext(req.body);
      res.json({
        jsonrpc: '2.0',
        id: req.body.id,
        result: result
      });
    } catch (error) {
      this.logger.error('getContext error:', error);
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id,
        error: {
          code: -32603,
          message: 'Internal error',
          data: error instanceof Error ? error.message : 'Unknown error'
        }
      });
    }
  }

  private async getStandards(req: Request, res: Response): Promise<void> {
    try {
      this.logger.info('getStandards called', { body: req.body });
      
      const result = await this.mcpService.getStandards(req.body);
      res.json({
        jsonrpc: '2.0',
        id: req.body.id,
        result: result
      });
    } catch (error) {
      this.logger.error('getStandards error:', error);
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id,
        error: {
          code: -32603,
          message: 'Internal error',
          data: error instanceof Error ? error.message : 'Unknown error'
        }
      });
    }
  }

  private async validateCompliance(req: Request, res: Response): Promise<void> {
    try {
      this.logger.info('validateCompliance called', { body: req.body });
      
      const result = await this.mcpService.validateCompliance(req.body);
      res.json({
        jsonrpc: '2.0',
        id: req.body.id,
        result: result
      });
    } catch (error) {
      this.logger.error('validateCompliance error:', error);
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id,
        error: {
          code: -32603,
          message: 'Internal error',
          data: error instanceof Error ? error.message : 'Unknown error'
        }
      });
    }
  }

  private async analyzeProject(req: Request, res: Response): Promise<void> {
    try {
      this.logger.info('analyzeProject called', { body: req.body });
      
      const result = await this.mcpService.analyzeProject(req.body);
      res.json({
        jsonrpc: '2.0',
        id: req.body.id,
        result: result
      });
    } catch (error) {
      this.logger.error('analyzeProject error:', error);
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id,
        error: {
          code: -32603,
          message: 'Internal error',
          data: error instanceof Error ? error.message : 'Unknown error'
        }
      });
    }
  }

  private async getMetrics(req: Request, res: Response): Promise<void> {
    try {
      this.logger.info('getMetrics called', { body: req.body });
      
      const result = await this.mcpService.getMetrics(req.body);
      res.json({
        jsonrpc: '2.0',
        id: req.body.id,
        result: result
      });
    } catch (error) {
      this.logger.error('getMetrics error:', error);
      res.status(500).json({
        jsonrpc: '2.0',
        id: req.body.id,
        error: {
          code: -32603,
          message: 'Internal error',
          data: error instanceof Error ? error.message : 'Unknown error'
        }
      });
    }
  }

  private getProtocolInfo(_req: Request, res: Response): void {
    res.json({
      name: 'AgentForge MCP Server',
      version: '1.0.0',
      protocol: 'MCP (Model Context Protocol)',
      description: 'MCP server for AgentForge static analysis and project setup'
    });
  }

  private getCapabilities(_req: Request, res: Response): void {
    res.json({
      capabilities: [
        'getContext',
        'getStandards', 
        'validateCompliance',
        'analyzeProject',
        'getMetrics'
      ],
      supportedFormats: ['json'],
      maxRequestSize: '10MB'
    });
  }
}
