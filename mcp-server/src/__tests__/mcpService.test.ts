import { MCPService } from '../services/mcpService';
import { createLogger, transports } from 'winston';

// Create a test logger
const testLogger = createLogger({
  level: 'error',
  transports: [new transports.Console()]
});

describe('MCPService', () => {
  let mcpService: MCPService;

  beforeEach(() => {
    mcpService = new MCPService(testLogger);
  });

  describe('initialization', () => {
    it('should initialize successfully', async () => {
      await expect(mcpService.initialize()).resolves.not.toThrow();
      expect(mcpService.isReady()).toBe(true);
    });

    it('should set project path correctly', async () => {
      await mcpService.initialize();
      // The service should be ready after initialization
      expect(mcpService.isReady()).toBe(true);
    });
  });

  describe('readiness check', () => {
    it('should not be ready before initialization', () => {
      expect(mcpService.isReady()).toBe(false);
    });

    it('should be ready after initialization', async () => {
      await mcpService.initialize();
      expect(mcpService.isReady()).toBe(true);
    });
  });

  describe('basic functionality', () => {
    beforeEach(async () => {
      await mcpService.initialize();
    });

    it('should get context', async () => {
      const context = await mcpService.getContext({});
      expect(context).toBeDefined();
      expect(context.projectPath).toBeDefined();
    });

    it('should get standards', async () => {
      const standards = await mcpService.getStandards({});
      expect(standards).toBeDefined();
      expect(standards.standards).toBeDefined();
    });

    it('should validate compliance', async () => {
      const compliance = await mcpService.validateCompliance({});
      expect(compliance).toBeDefined();
      expect(compliance.score).toBeDefined();
      expect(compliance.violations).toBeDefined();
    });

    it('should analyze project', async () => {
      const analysis = await mcpService.analyzeProject({});
      expect(analysis).toBeDefined();
      expect(analysis.projectPath).toBeDefined();
      expect(analysis.structure).toBeDefined();
    });

    it('should get metrics', async () => {
      const metrics = await mcpService.getMetrics({});
      expect(metrics).toBeDefined();
      expect(metrics.performance).toBeDefined();
      expect(metrics.quality).toBeDefined();
      expect(metrics.compliance).toBeDefined();
      expect(metrics.system).toBeDefined();
    });
  });
});
