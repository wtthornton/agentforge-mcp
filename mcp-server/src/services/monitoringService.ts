import { EventEmitter } from 'events';
import { PerformanceObserver, performance } from 'perf_hooks';
import { DatabaseConnection } from '../config/database';
import { cacheService } from './cacheService';
import { logger } from '../utils/logger';
import axios from 'axios';

export interface MetricData {
  name: string;
  value: number;
  timestamp: Date;
  labels?: Record<string, string>;
  unit?: string;
}

export interface AlertRule {
  id: string;
  name: string;
  metric: string;
  condition: 'gt' | 'lt' | 'eq' | 'gte' | 'lte';
  threshold: number;
  duration: number; // seconds
  severity: 'critical' | 'high' | 'medium' | 'low';
  enabled: boolean;
  channels: string[];
}

export interface Alert {
  id: string;
  ruleId: string;
  ruleName: string;
  metric: string;
  value: number;
  threshold: number;
  severity: string;
  message: string;
  timestamp: Date;
  resolved?: Date;
  status: 'firing' | 'resolved';
}

export interface SystemHealth {
  overall: 'healthy' | 'degraded' | 'unhealthy';
  components: {
    database: ComponentHealth;
    cache: ComponentHealth;
    memory: ComponentHealth;
    cpu: ComponentHealth;
    disk: ComponentHealth;
  };
  metrics: MetricData[];
  alerts: Alert[];
  timestamp: Date;
}

export interface ComponentHealth {
  status: 'healthy' | 'degraded' | 'unhealthy';
  value?: number;
  threshold?: number;
  message?: string;
}

export class MonitoringService extends EventEmitter {
  private metrics: Map<string, MetricData[]> = new Map();
  private alerts: Map<string, Alert> = new Map();
  private alertRules: Map<string, AlertRule> = new Map();
  private performanceObserver?: PerformanceObserver;
  private monitoringInterval?: NodeJS.Timeout;
  private alertCheckInterval?: NodeJS.Timeout;
  private systemInfo: any = {};

  constructor(private dbConnection?: DatabaseConnection) {
    super();
    this.initializeDefaultRules();
    this.setupPerformanceObserver();
  }

  private initializeDefaultRules() {
    const defaultRules: AlertRule[] = [
      {
        id: 'high_cpu_usage',
        name: 'High CPU Usage',
        metric: 'cpu_usage_percent',
        condition: 'gt',
        threshold: 80,
        duration: 300, // 5 minutes
        severity: 'high',
        enabled: true,
        channels: ['email', 'slack'],
      },
      {
        id: 'high_memory_usage',
        name: 'High Memory Usage',
        metric: 'memory_usage_percent',
        condition: 'gt',
        threshold: 85,
        duration: 300,
        severity: 'high',
        enabled: true,
        channels: ['email', 'slack'],
      },
      {
        id: 'slow_database_queries',
        name: 'Slow Database Queries',
        metric: 'db_query_duration_ms',
        condition: 'gt',
        threshold: 5000,
        duration: 60,
        severity: 'medium',
        enabled: true,
        channels: ['slack'],
      },
      {
        id: 'low_cache_hit_rate',
        name: 'Low Cache Hit Rate',
        metric: 'cache_hit_rate_percent',
        condition: 'lt',
        threshold: 60,
        duration: 600, // 10 minutes
        severity: 'medium',
        enabled: true,
        channels: ['slack'],
      },
      {
        id: 'database_connection_failure',
        name: 'Database Connection Failure',
        metric: 'db_connection_success',
        condition: 'eq',
        threshold: 0,
        duration: 30,
        severity: 'critical',
        enabled: true,
        channels: ['email', 'slack', 'pagerduty'],
      },
    ];

    defaultRules.forEach(rule => {
      this.alertRules.set(rule.id, rule);
    });
  }

  private setupPerformanceObserver() {
    try {
      this.performanceObserver = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        entries.forEach(entry => {
          this.recordMetric({
            name: `performance_${entry.name}`,
            value: entry.duration,
            timestamp: new Date(),
            unit: 'ms',
            labels: {
              type: entry.entryType,
            },
          });
        });
      });

      this.performanceObserver.observe({ entryTypes: ['measure', 'mark'] });
    } catch (error) {
      logger.error('Failed to setup performance observer', error);
    }
  }

  recordMetric(metric: MetricData) {
    const metricHistory = this.metrics.get(metric.name) || [];
    metricHistory.push(metric);

    // Keep only last 1000 data points per metric
    if (metricHistory.length > 1000) {
      metricHistory.shift();
    }

    this.metrics.set(metric.name, metricHistory);
    this.emit('metric', metric);

    logger.debug('Metric recorded', {
      name: metric.name,
      value: metric.value,
      labels: metric.labels,
    });
  }

  async collectSystemMetrics(): Promise<void> {
    try {
      const now = new Date();

      // Memory metrics
      const memUsage = process.memoryUsage();
      this.recordMetric({
        name: 'memory_usage_bytes',
        value: memUsage.rss,
        timestamp: now,
        unit: 'bytes',
        labels: { type: 'rss' },
      });

      this.recordMetric({
        name: 'memory_usage_bytes',
        value: memUsage.heapUsed,
        timestamp: now,
        unit: 'bytes',
        labels: { type: 'heap_used' },
      });

      this.recordMetric({
        name: 'memory_usage_bytes',
        value: memUsage.heapTotal,
        timestamp: now,
        unit: 'bytes',
        labels: { type: 'heap_total' },
      });

      // Calculate memory usage percentage
      const memUsagePercent = (memUsage.rss / (1024 * 1024 * 1024)) * 100; // Assuming 1GB limit
      this.recordMetric({
        name: 'memory_usage_percent',
        value: memUsagePercent,
        timestamp: now,
        unit: 'percent',
      });

      // CPU metrics (simplified)
      const cpuUsage = process.cpuUsage(this.systemInfo.lastCpuUsage || undefined);
      this.systemInfo.lastCpuUsage = process.cpuUsage();
      
      const cpuPercent = (cpuUsage.user + cpuUsage.system) / 1000000; // Convert to seconds
      this.recordMetric({
        name: 'cpu_usage_percent',
        value: cpuPercent,
        timestamp: now,
        unit: 'percent',
      });

      // Event loop lag
      const start = process.hrtime.bigint();
      setImmediate(() => {
        const lag = Number(process.hrtime.bigint() - start) / 1000000;
        this.recordMetric({
          name: 'event_loop_lag_ms',
          value: lag,
          timestamp: new Date(),
          unit: 'ms',
        });
      });

      // Database metrics
      if (this.dbConnection) {
        await this.collectDatabaseMetrics();
      }

      // Cache metrics
      await this.collectCacheMetrics();

    } catch (error) {
      logger.error('Failed to collect system metrics', error);
    }
  }

  private async collectDatabaseMetrics(): Promise<void> {
    if (!this.dbConnection) return;

    try {
      const start = Date.now();
      const healthCheck = await this.dbConnection.performanceCheck();
      const duration = Date.now() - start;

      this.recordMetric({
        name: 'db_connection_success',
        value: healthCheck.isHealthy ? 1 : 0,
        timestamp: new Date(),
        unit: 'bool',
      });

      this.recordMetric({
        name: 'db_query_duration_ms',
        value: healthCheck.queryTime,
        timestamp: new Date(),
        unit: 'ms',
        labels: { type: 'health_check' },
      });

      this.recordMetric({
        name: 'db_connection_duration_ms',
        value: healthCheck.connectionTime,
        timestamp: new Date(),
        unit: 'ms',
      });

      // Pool metrics
      const poolMetrics = healthCheck.poolMetrics;
      this.recordMetric({
        name: 'db_pool_total_connections',
        value: poolMetrics.totalCount,
        timestamp: new Date(),
        unit: 'count',
      });

      this.recordMetric({
        name: 'db_pool_idle_connections',
        value: poolMetrics.idleCount,
        timestamp: new Date(),
        unit: 'count',
      });

      this.recordMetric({
        name: 'db_pool_waiting_connections',
        value: poolMetrics.waitingCount,
        timestamp: new Date(),
        unit: 'count',
      });

    } catch (error) {
      logger.error('Failed to collect database metrics', error);
      
      this.recordMetric({
        name: 'db_connection_success',
        value: 0,
        timestamp: new Date(),
        unit: 'bool',
      });
    }
  }

  private async collectCacheMetrics(): Promise<void> {
    try {
      const cacheStats = cacheService.getStats();
      
      this.recordMetric({
        name: 'cache_hit_rate_percent',
        value: cacheStats.hitRate,
        timestamp: new Date(),
        unit: 'percent',
      });

      this.recordMetric({
        name: 'cache_hits_total',
        value: cacheStats.hits,
        timestamp: new Date(),
        unit: 'count',
      });

      this.recordMetric({
        name: 'cache_misses_total',
        value: cacheStats.misses,
        timestamp: new Date(),
        unit: 'count',
      });

      this.recordMetric({
        name: 'cache_errors_total',
        value: cacheStats.errors,
        timestamp: new Date(),
        unit: 'count',
      });

    } catch (error) {
      logger.error('Failed to collect cache metrics', error);
    }
  }

  private async checkAlertRules(): Promise<void> {
    for (const rule of this.alertRules.values()) {
      if (!rule.enabled) continue;

      try {
        const metricData = this.metrics.get(rule.metric);
        if (!metricData || metricData.length === 0) continue;

        // Get recent values within duration
        const cutoffTime = new Date(Date.now() - rule.duration * 1000);
        const recentValues = metricData.filter(m => m.timestamp >= cutoffTime);
        
        if (recentValues.length === 0) continue;

        const latestValue = recentValues[recentValues.length - 1].value;
        const shouldTrigger = this.evaluateCondition(latestValue, rule.condition, rule.threshold);

        const existingAlert = Array.from(this.alerts.values())
          .find(a => a.ruleId === rule.id && a.status === 'firing');

        if (shouldTrigger && !existingAlert) {
          // Create new alert
          const alert: Alert = {
            id: `alert_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            ruleId: rule.id,
            ruleName: rule.name,
            metric: rule.metric,
            value: latestValue,
            threshold: rule.threshold,
            severity: rule.severity,
            message: `${rule.name}: ${rule.metric} is ${latestValue} (threshold: ${rule.threshold})`,
            timestamp: new Date(),
            status: 'firing',
          };

          this.alerts.set(alert.id, alert);
          this.emit('alert', alert);
          await this.sendAlert(alert, rule);
          
          logger.warn('Alert triggered', {
            ruleId: rule.id,
            metric: rule.metric,
            value: latestValue,
            threshold: rule.threshold,
          });

        } else if (!shouldTrigger && existingAlert) {
          // Resolve existing alert
          existingAlert.status = 'resolved';
          existingAlert.resolved = new Date();
          
          this.emit('alert_resolved', existingAlert);
          logger.info('Alert resolved', {
            alertId: existingAlert.id,
            ruleId: rule.id,
          });
        }

      } catch (error) {
        logger.error(`Failed to check alert rule ${rule.id}`, error);
      }
    }
  }

  private evaluateCondition(value: number, condition: string, threshold: number): boolean {
    switch (condition) {
      case 'gt': return value > threshold;
      case 'lt': return value < threshold;
      case 'eq': return value === threshold;
      case 'gte': return value >= threshold;
      case 'lte': return value <= threshold;
      default: return false;
    }
  }

  private async sendAlert(alert: Alert, rule: AlertRule): Promise<void> {
    for (const channel of rule.channels) {
      try {
        switch (channel) {
          case 'email':
            await this.sendEmailAlert(alert);
            break;
          case 'slack':
            await this.sendSlackAlert(alert);
            break;
          case 'pagerduty':
            await this.sendPagerDutyAlert(alert);
            break;
          case 'webhook':
            await this.sendWebhookAlert(alert);
            break;
          default:
            logger.warn(`Unknown alert channel: ${channel}`);
        }
      } catch (error) {
        logger.error(`Failed to send alert via ${channel}`, error);
      }
    }
  }

  private async sendEmailAlert(alert: Alert): Promise<void> {
    // Email implementation would go here
    logger.info('Email alert sent (mock)', { alertId: alert.id });
  }

  private async sendSlackAlert(alert: Alert): Promise<void> {
    const webhookUrl = process.env.SLACK_WEBHOOK_URL;
    if (!webhookUrl) {
      logger.warn('Slack webhook URL not configured');
      return;
    }

    const color = this.getSeverityColor(alert.severity);
    const payload = {
      attachments: [{
        color,
        title: `🚨 ${alert.ruleName}`,
        text: alert.message,
        fields: [
          { title: 'Metric', value: alert.metric, short: true },
          { title: 'Value', value: alert.value.toString(), short: true },
          { title: 'Threshold', value: alert.threshold.toString(), short: true },
          { title: 'Severity', value: alert.severity.toUpperCase(), short: true },
        ],
        timestamp: Math.floor(alert.timestamp.getTime() / 1000),
      }],
    };

    await axios.post(webhookUrl, payload);
    logger.info('Slack alert sent', { alertId: alert.id });
  }

  private async sendPagerDutyAlert(alert: Alert): Promise<void> {
    // PagerDuty implementation would go here
    logger.info('PagerDuty alert sent (mock)', { alertId: alert.id });
  }

  private async sendWebhookAlert(alert: Alert): Promise<void> {
    const webhookUrl = process.env.WEBHOOK_URL;
    if (!webhookUrl) return;

    await axios.post(webhookUrl, alert);
    logger.info('Webhook alert sent', { alertId: alert.id });
  }

  private getSeverityColor(severity: string): string {
    switch (severity) {
      case 'critical': return '#ff0000';
      case 'high': return '#ff8c00';
      case 'medium': return '#ffd700';
      case 'low': return '#90ee90';
      default: return '#808080';
    }
  }

  async getSystemHealth(): Promise<SystemHealth> {
    const now = new Date();
    const recentMetrics = Array.from(this.metrics.entries())
      .flatMap(([name, metrics]) => 
        metrics.filter(m => m.timestamp > new Date(now.getTime() - 300000)) // Last 5 minutes
      );

    const activeAlerts = Array.from(this.alerts.values())
      .filter(a => a.status === 'firing');

    // Determine overall health
    let overall: 'healthy' | 'degraded' | 'unhealthy' = 'healthy';
    const criticalAlerts = activeAlerts.filter(a => a.severity === 'critical');
    const highAlerts = activeAlerts.filter(a => a.severity === 'high');

    if (criticalAlerts.length > 0) {
      overall = 'unhealthy';
    } else if (highAlerts.length > 0 || activeAlerts.length > 3) {
      overall = 'degraded';
    }

    return {
      overall,
      components: {
        database: await this.getDatabaseHealth(),
        cache: await this.getCacheHealth(),
        memory: this.getMemoryHealth(),
        cpu: this.getCpuHealth(),
        disk: this.getDiskHealth(),
      },
      metrics: recentMetrics,
      alerts: activeAlerts,
      timestamp: now,
    };
  }

  private async getDatabaseHealth(): Promise<ComponentHealth> {
    try {
      if (!this.dbConnection) {
        return { status: 'unhealthy', message: 'Database connection not configured' };
      }

      const healthCheck = await this.dbConnection.performanceCheck();
      if (!healthCheck.isHealthy) {
        return { 
          status: 'unhealthy', 
          message: 'Database connection failed',
          value: healthCheck.connectionTime,
          threshold: 5000,
        };
      }

      if (healthCheck.queryTime > 2000) {
        return { 
          status: 'degraded', 
          message: 'Slow database queries detected',
          value: healthCheck.queryTime,
          threshold: 2000,
        };
      }

      return { status: 'healthy' };
    } catch (error) {
      return { status: 'unhealthy', message: error.message };
    }
  }

  private async getCacheHealth(): Promise<ComponentHealth> {
    try {
      const healthCheck = await cacheService.healthCheck();
      if (!healthCheck.isHealthy) {
        return { status: 'unhealthy', message: 'Cache connection failed' };
      }

      const stats = cacheService.getStats();
      if (stats.hitRate < 60) {
        return { 
          status: 'degraded', 
          message: 'Low cache hit rate',
          value: stats.hitRate,
          threshold: 60,
        };
      }

      return { status: 'healthy' };
    } catch (error) {
      return { status: 'unhealthy', message: error.message };
    }
  }

  private getMemoryHealth(): ComponentHealth {
    const memUsage = process.memoryUsage();
    const memUsageMB = memUsage.rss / (1024 * 1024);
    
    if (memUsageMB > 1024) {
      return { 
        status: 'unhealthy', 
        message: 'High memory usage',
        value: memUsageMB,
        threshold: 1024,
      };
    } else if (memUsageMB > 512) {
      return { 
        status: 'degraded', 
        message: 'Elevated memory usage',
        value: memUsageMB,
        threshold: 512,
      };
    }

    return { status: 'healthy' };
  }

  private getCpuHealth(): ComponentHealth {
    const cpuMetrics = this.metrics.get('cpu_usage_percent');
    if (!cpuMetrics || cpuMetrics.length === 0) {
      return { status: 'healthy' };
    }

    const latestCpu = cpuMetrics[cpuMetrics.length - 1].value;
    
    if (latestCpu > 90) {
      return { 
        status: 'unhealthy', 
        message: 'Very high CPU usage',
        value: latestCpu,
        threshold: 90,
      };
    } else if (latestCpu > 70) {
      return { 
        status: 'degraded', 
        message: 'High CPU usage',
        value: latestCpu,
        threshold: 70,
      };
    }

    return { status: 'healthy' };
  }

  private getDiskHealth(): ComponentHealth {
    // Simplified disk health check
    return { status: 'healthy' };
  }

  start(interval: number = 30000): void {
    // Start metrics collection
    this.monitoringInterval = setInterval(() => {
      this.collectSystemMetrics();
    }, interval);

    // Start alert checking
    this.alertCheckInterval = setInterval(() => {
      this.checkAlertRules();
    }, 10000); // Check every 10 seconds

    logger.info('Monitoring service started', { interval });
  }

  stop(): void {
    if (this.monitoringInterval) {
      clearInterval(this.monitoringInterval);
    }
    
    if (this.alertCheckInterval) {
      clearInterval(this.alertCheckInterval);
    }

    if (this.performanceObserver) {
      this.performanceObserver.disconnect();
    }

    logger.info('Monitoring service stopped');
  }

  // Public API methods
  addAlertRule(rule: AlertRule): void {
    this.alertRules.set(rule.id, rule);
    logger.info('Alert rule added', { ruleId: rule.id });
  }

  removeAlertRule(ruleId: string): void {
    this.alertRules.delete(ruleId);
    logger.info('Alert rule removed', { ruleId });
  }

  getMetrics(metricName?: string): MetricData[] {
    if (metricName) {
      return this.metrics.get(metricName) || [];
    }
    
    return Array.from(this.metrics.values()).flat();
  }

  getAlerts(status?: 'firing' | 'resolved'): Alert[] {
    const alerts = Array.from(this.alerts.values());
    return status ? alerts.filter(a => a.status === status) : alerts;
  }

  getAlertRules(): AlertRule[] {
    return Array.from(this.alertRules.values());
  }
}

// Create singleton instance
export const monitoringService = new MonitoringService();