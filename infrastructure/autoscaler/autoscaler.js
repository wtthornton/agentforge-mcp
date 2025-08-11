#!/usr/bin/env node

const axios = require('axios');
const { Docker } = require('node-docker-api');
const winston = require('winston');
const cron = require('node-cron');

// Configuration
const CONFIG = {
  prometheus: {
    url: process.env.PROMETHEUS_URL || 'http://localhost:9090',
    queries: {
      cpuUsage: 'avg(rate(container_cpu_usage_seconds_total[5m])) by (container_label_com_docker_compose_service)',
      memoryUsage: 'avg(container_memory_usage_bytes / container_spec_memory_limit_bytes) by (container_label_com_docker_compose_service)',
      requestRate: 'rate(nginx_http_requests_total[5m])',
      responseTime: 'histogram_quantile(0.95, rate(nginx_http_request_duration_seconds_bucket[5m]))',
      errorRate: 'rate(nginx_http_requests_total{status=~"5.."}[5m])'
    }
  },
  scaling: {
    scaleUpThreshold: parseFloat(process.env.SCALE_UP_THRESHOLD) || 80,
    scaleDownThreshold: parseFloat(process.env.SCALE_DOWN_THRESHOLD) || 30,
    minReplicas: parseInt(process.env.MIN_REPLICAS) || 2,
    maxReplicas: parseInt(process.env.MAX_REPLICAS) || 6,
    cooldownPeriod: parseInt(process.env.COOLDOWN_PERIOD) || 300, // 5 minutes
    evaluationPeriod: parseInt(process.env.EVALUATION_PERIOD) || 60, // 1 minute
  },
  services: {
    backend: {
      name: 'agentforge-backend',
      type: 'stateless',
      priority: 'high',
      metrics: ['cpuUsage', 'memoryUsage', 'responseTime'],
      scaleUpThreshold: 75,
      scaleDownThreshold: 25,
    },
    mcp: {
      name: 'agentforge-mcp',
      type: 'stateless',
      priority: 'medium',
      metrics: ['cpuUsage', 'memoryUsage'],
      scaleUpThreshold: 80,
      scaleDownThreshold: 30,
    },
    frontend: {
      name: 'agentforge-frontend',
      type: 'stateless',
      priority: 'low',
      metrics: ['cpuUsage'],
      scaleUpThreshold: 85,
      scaleDownThreshold: 35,
    }
  }
};

// Logger setup
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: '/app/logs/autoscaler-error.log', level: 'error' }),
    new winston.transports.File({ filename: '/app/logs/autoscaler.log' }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
});

class AutoScaler {
  constructor() {
    this.docker = new Docker({ socketPath: '/var/run/docker.sock' });
    this.lastScaleAction = {};
    this.scalingHistory = [];
    this.metrics = {};
    this.isScaling = false;
  }

  async initialize() {
    logger.info('Initializing AgentForge AutoScaler...');
    
    try {
      // Test Docker connection
      const version = await this.docker.version();
      logger.info('Docker connection established', { version: version.Version });

      // Test Prometheus connection
      const response = await axios.get(`${CONFIG.prometheus.url}/api/v1/query?query=up`);
      logger.info('Prometheus connection established', { targets: response.data.data.result.length });

      logger.info('AutoScaler initialized successfully');
      return true;
    } catch (error) {
      logger.error('Failed to initialize AutoScaler', error);
      return false;
    }
  }

  async collectMetrics() {
    const metrics = {};
    
    try {
      for (const [metricName, query] of Object.entries(CONFIG.prometheus.queries)) {
        const response = await axios.get(`${CONFIG.prometheus.url}/api/v1/query`, {
          params: { query },
          timeout: 10000
        });

        if (response.data.status === 'success') {
          metrics[metricName] = this.parsePrometheusResponse(response.data.data.result);
        } else {
          logger.warn(`Failed to collect metric: ${metricName}`);
        }
      }

      this.metrics = metrics;
      logger.debug('Metrics collected successfully', { metricsCount: Object.keys(metrics).length });
      
    } catch (error) {
      logger.error('Failed to collect metrics', error);
    }
  }

  parsePrometheusResponse(result) {
    return result.reduce((acc, item) => {
      const serviceName = item.metric.container_label_com_docker_compose_service || 'unknown';
      const value = parseFloat(item.value[1]);
      acc[serviceName] = value;
      return acc;
    }, {});
  }

  async getServiceReplicas(serviceName) {
    try {
      const containers = await this.docker.container.list({
        filters: {
          label: [`com.docker.compose.service=${serviceName}`],
          status: ['running']
        }
      });
      return containers.length;
    } catch (error) {
      logger.error(`Failed to get replicas for service ${serviceName}`, error);
      return 0;
    }
  }

  shouldScale(serviceName, serviceConfig) {
    const now = Date.now();
    const lastAction = this.lastScaleAction[serviceName];
    
    // Cooldown period check
    if (lastAction && (now - lastAction.timestamp) < CONFIG.scaling.cooldownPeriod * 1000) {
      logger.debug(`Service ${serviceName} in cooldown period`);
      return { action: 'none', reason: 'cooldown' };
    }

    // Check if we have enough metrics data
    const requiredMetrics = serviceConfig.metrics;
    const availableMetrics = requiredMetrics.filter(metric => 
      this.metrics[metric] && this.metrics[metric][serviceName] !== undefined
    );

    if (availableMetrics.length === 0) {
      logger.debug(`No metrics available for service ${serviceName}`);
      return { action: 'none', reason: 'no-metrics' };
    }

    // Calculate average metric value
    const metricValues = availableMetrics.map(metric => this.metrics[metric][serviceName]);
    const avgValue = metricValues.reduce((sum, val) => sum + val, 0) / metricValues.length;
    const avgPercentage = avgValue * 100; // Convert to percentage

    logger.debug(`Service ${serviceName} metrics`, { 
      avgPercentage, 
      scaleUpThreshold: serviceConfig.scaleUpThreshold,
      scaleDownThreshold: serviceConfig.scaleDownThreshold 
    });

    if (avgPercentage > serviceConfig.scaleUpThreshold) {
      return { action: 'up', reason: 'high-load', value: avgPercentage };
    } else if (avgPercentage < serviceConfig.scaleDownThreshold) {
      return { action: 'down', reason: 'low-load', value: avgPercentage };
    }

    return { action: 'none', reason: 'stable', value: avgPercentage };
  }

  async scaleService(serviceName, action, currentReplicas) {
    if (this.isScaling) {
      logger.info('Scaling operation already in progress, skipping...');
      return false;
    }

    this.isScaling = true;

    try {
      let targetReplicas = currentReplicas;

      if (action === 'up') {
        targetReplicas = Math.min(currentReplicas + 1, CONFIG.scaling.maxReplicas);
        if (targetReplicas === currentReplicas) {
          logger.info(`Service ${serviceName} already at max replicas (${CONFIG.scaling.maxReplicas})`);
          return false;
        }
      } else if (action === 'down') {
        targetReplicas = Math.max(currentReplicas - 1, CONFIG.scaling.minReplicas);
        if (targetReplicas === currentReplicas) {
          logger.info(`Service ${serviceName} already at min replicas (${CONFIG.scaling.minReplicas})`);
          return false;
        }
      }

      logger.info(`Scaling ${serviceName} from ${currentReplicas} to ${targetReplicas} replicas`);

      // Execute scaling command
      await this.executeScalingCommand(serviceName, targetReplicas, action);

      // Record the scaling action
      this.lastScaleAction[serviceName] = {
        timestamp: Date.now(),
        action,
        fromReplicas: currentReplicas,
        toReplicas: targetReplicas
      };

      // Add to history
      this.scalingHistory.push({
        timestamp: new Date().toISOString(),
        service: serviceName,
        action,
        fromReplicas: currentReplicas,
        toReplicas: targetReplicas,
        reason: 'autoscale'
      });

      // Keep only last 100 scaling actions
      if (this.scalingHistory.length > 100) {
        this.scalingHistory.shift();
      }

      logger.info(`Successfully scaled ${serviceName} to ${targetReplicas} replicas`);
      return true;

    } catch (error) {
      logger.error(`Failed to scale service ${serviceName}`, error);
      return false;
    } finally {
      this.isScaling = false;
    }
  }

  async executeScalingCommand(serviceName, targetReplicas, action) {
    // This would execute the actual scaling command
    // For Docker Compose, this might involve calling docker-compose up --scale
    // For Kubernetes, this would be kubectl scale
    // For Docker Swarm, this would be docker service update --replicas
    
    const { exec } = require('child_process');
    const util = require('util');
    const execAsync = util.promisify(exec);

    try {
      // Example for Docker Compose scaling
      const command = `docker-compose -f /app/docker-compose.scaling.yml up -d --scale ${serviceName}=${targetReplicas}`;
      
      logger.info(`Executing scaling command: ${command}`);
      const { stdout, stderr } = await execAsync(command);
      
      if (stderr && !stderr.includes('WARNING')) {
        throw new Error(stderr);
      }
      
      logger.info('Scaling command executed successfully', { stdout: stdout.trim() });
      
      // Wait for services to be ready
      await this.waitForServiceHealth(serviceName, targetReplicas);
      
    } catch (error) {
      logger.error('Scaling command failed', error);
      throw error;
    }
  }

  async waitForServiceHealth(serviceName, expectedReplicas, maxWaitTime = 60000) {
    const startTime = Date.now();
    
    while (Date.now() - startTime < maxWaitTime) {
      try {
        const containers = await this.docker.container.list({
          filters: {
            label: [`com.docker.compose.service=${serviceName}`],
            status: ['running'],
            health: ['healthy']
          }
        });

        if (containers.length >= expectedReplicas) {
          logger.info(`Service ${serviceName} scaled successfully, ${containers.length} healthy replicas`);
          return true;
        }

        logger.debug(`Waiting for ${serviceName} to be healthy: ${containers.length}/${expectedReplicas} ready`);
        await new Promise(resolve => setTimeout(resolve, 5000)); // Wait 5 seconds
        
      } catch (error) {
        logger.warn(`Error checking service health for ${serviceName}`, error);
        await new Promise(resolve => setTimeout(resolve, 5000));
      }
    }

    logger.warn(`Timeout waiting for ${serviceName} to be healthy`);
    return false;
  }

  async evaluateScaling() {
    if (this.isScaling) {
      logger.debug('Scaling operation in progress, skipping evaluation');
      return;
    }

    logger.debug('Evaluating scaling decisions...');

    for (const [serviceKey, serviceConfig] of Object.entries(CONFIG.services)) {
      try {
        const currentReplicas = await this.getServiceReplicas(serviceConfig.name);
        
        if (currentReplicas === 0) {
          logger.warn(`Service ${serviceConfig.name} has no running replicas`);
          continue;
        }

        const scalingDecision = this.shouldScale(serviceConfig.name, serviceConfig);
        
        if (scalingDecision.action !== 'none') {
          logger.info(`Scaling decision for ${serviceConfig.name}`, scalingDecision);
          await this.scaleService(serviceConfig.name, scalingDecision.action, currentReplicas);
        }

      } catch (error) {
        logger.error(`Error evaluating scaling for ${serviceKey}`, error);
      }
    }
  }

  async getHealthStatus() {
    try {
      const services = {};
      
      for (const [serviceKey, serviceConfig] of Object.entries(CONFIG.services)) {
        const replicas = await this.getServiceReplicas(serviceConfig.name);
        services[serviceKey] = {
          name: serviceConfig.name,
          replicas,
          lastScaleAction: this.lastScaleAction[serviceConfig.name] || null,
        };
      }

      return {
        status: 'healthy',
        timestamp: new Date().toISOString(),
        services,
        metrics: this.metrics,
        scalingHistory: this.scalingHistory.slice(-10), // Last 10 actions
      };
    } catch (error) {
      logger.error('Failed to get health status', error);
      return {
        status: 'unhealthy',
        timestamp: new Date().toISOString(),
        error: error.message
      };
    }
  }

  async start() {
    const initialized = await this.initialize();
    if (!initialized) {
      process.exit(1);
    }

    // Start metrics collection (every 30 seconds)
    cron.schedule('*/30 * * * * *', async () => {
      await this.collectMetrics();
    });

    // Start scaling evaluation (every minute)
    cron.schedule('0 * * * * *', async () => {
      await this.evaluateScaling();
    });

    // Health check endpoint
    const express = require('express');
    const app = express();
    
    app.get('/health', async (req, res) => {
      const health = await this.getHealthStatus();
      res.json(health);
    });

    app.get('/metrics', (req, res) => {
      res.json(this.metrics);
    });

    app.get('/history', (req, res) => {
      res.json(this.scalingHistory);
    });

    const port = process.env.PORT || 8081;
    app.listen(port, () => {
      logger.info(`AutoScaler health server listening on port ${port}`);
    });

    logger.info('AutoScaler started successfully');

    // Graceful shutdown
    process.on('SIGTERM', () => {
      logger.info('Received SIGTERM, shutting down gracefully');
      process.exit(0);
    });

    process.on('SIGINT', () => {
      logger.info('Received SIGINT, shutting down gracefully');
      process.exit(0);
    });
  }
}

// Start the autoscaler
const autoScaler = new AutoScaler();
autoScaler.start().catch(error => {
  logger.error('Failed to start AutoScaler', error);
  process.exit(1);
});

module.exports = AutoScaler;