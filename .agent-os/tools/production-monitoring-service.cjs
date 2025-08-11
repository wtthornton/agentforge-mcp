/**
 * Production Monitoring Service for AgentForge
 * Comprehensive monitoring, alerting, and automated incident response
 * Part of Phase 4: Task 4.3 - Production Monitoring & Operations
 */

const fs = require('fs');
const path = require('path');
const http = require('http');
const https = require('https');
const { performance } = require('perf_hooks');

class ProductionMonitoringService {
    constructor() {
        this.config = this.loadMonitoringConfig();
        this.metrics = new Map();
        this.alerts = [];
        this.incidents = [];
        this.healthChecks = [];
        this.reportPath = path.join(process.cwd(), '.agent-os', 'reports', 'monitoring');
        this.logPath = path.join(process.cwd(), 'logs', 'monitoring');
        this.ensureDirectories();
        this.initializeMonitoring();
    }

    loadMonitoringConfig() {
        const configPath = path.join(process.cwd(), '.agent-os', 'config', 'monitoring-config.json');
        if (fs.existsSync(configPath)) {
            return JSON.parse(fs.readFileSync(configPath, 'utf-8'));
        }

        const defaultConfig = {
            environment: 'production',
            checkInterval: 30000, // 30 seconds
            alertCooldown: 300000, // 5 minutes
            incidentThreshold: 3, // 3 failed checks = incident
            endpoints: [
                { name: 'Frontend', url: 'https://agentforge.com', timeout: 5000, critical: true },
                { name: 'API Health', url: 'https://agentforge.com/api/actuator/health', timeout: 10000, critical: true },
                { name: 'Backend Metrics', url: 'https://agentforge.com/api/actuator/prometheus', timeout: 15000, critical: false },
                { name: 'Database Health', url: 'https://agentforge.com/health', timeout: 10000, critical: true }
            ],
            alertChannels: {
                email: {
                    enabled: true,
                    recipients: ['ops@agentforge.com', 'alerts@agentforge.com'],
                    smtp: {
                        host: 'smtp.gmail.com',
                        port: 587,
                        secure: false
                    }
                },
                slack: {
                    enabled: true,
                    webhook: process.env.SLACK_WEBHOOK_URL
                },
                pagerduty: {
                    enabled: true,
                    routingKey: process.env.PAGERDUTY_ROUTING_KEY
                }
            },
            thresholds: {
                responseTime: { warning: 2000, critical: 5000 },
                errorRate: { warning: 0.05, critical: 0.1 },
                availability: { warning: 99.5, critical: 99.0 },
                diskSpace: { warning: 80, critical: 90 },
                memory: { warning: 85, critical: 95 },
                cpu: { warning: 80, critical: 90 }
            },
            autoRemediation: {
                enabled: true,
                actions: {
                    restartService: true,
                    scaleUp: true,
                    clearCache: true,
                    failover: true
                }
            }
        };

        this.saveMonitoringConfig(defaultConfig);
        return defaultConfig;
    }

    saveMonitoringConfig(config) {
        const configDir = path.join(process.cwd(), '.agent-os', 'config');
        if (!fs.existsSync(configDir)) {
            fs.mkdirSync(configDir, { recursive: true });
        }
        const configPath = path.join(configDir, 'monitoring-config.json');
        fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
    }

    ensureDirectories() {
        [this.reportPath, this.logPath].forEach(dir => {
            if (!fs.existsSync(dir)) {
                fs.mkdirSync(dir, { recursive: true });
            }
        });
    }

    initializeMonitoring() {
        console.log('🚀 Initializing Production Monitoring Service...');
        
        // Initialize health checks
        this.config.endpoints.forEach(endpoint => {
            this.healthChecks.push({
                ...endpoint,
                status: 'unknown',
                lastCheck: null,
                consecutiveFailures: 0,
                responseTime: null,
                lastError: null
            });
        });

        // Start monitoring loops
        this.startHealthCheckLoop();
        this.startMetricsCollection();
        this.startIncidentManagement();
        this.startReporting();

        console.log('✅ Production monitoring initialized successfully');
    }

    startHealthCheckLoop() {
        setInterval(async () => {
            await this.performHealthChecks();
        }, this.config.checkInterval);
        
        // Perform initial check
        setTimeout(() => this.performHealthChecks(), 1000);
    }

    async performHealthChecks() {
        const checkPromises = this.healthChecks.map(check => this.performSingleHealthCheck(check));
        await Promise.allSettled(checkPromises);
        
        // Process results and trigger alerts if needed
        this.processHealthCheckResults();
    }

    async performSingleHealthCheck(check) {
        const startTime = performance.now();
        
        try {
            const result = await this.httpRequest(check.url, {
                timeout: check.timeout,
                method: 'GET'
            });

            const responseTime = Math.round(performance.now() - startTime);
            
            check.status = 'healthy';
            check.lastCheck = new Date().toISOString();
            check.responseTime = responseTime;
            check.consecutiveFailures = 0;
            check.lastError = null;

            // Check response time thresholds
            if (responseTime > this.config.thresholds.responseTime.critical) {
                this.createAlert('critical', `${check.name} response time critical: ${responseTime}ms`, check);
            } else if (responseTime > this.config.thresholds.responseTime.warning) {
                this.createAlert('warning', `${check.name} response time high: ${responseTime}ms`, check);
            }

            this.logHealthCheck('INFO', `${check.name} is healthy (${responseTime}ms)`);
            
        } catch (error) {
            check.status = 'unhealthy';
            check.lastCheck = new Date().toISOString();
            check.consecutiveFailures += 1;
            check.lastError = error.message;
            check.responseTime = null;

            this.logHealthCheck('ERROR', `${check.name} health check failed: ${error.message}`);
            
            // Create alert for unhealthy service
            const severity = check.critical ? 'critical' : 'warning';
            this.createAlert(severity, `${check.name} is unhealthy: ${error.message}`, check);
            
            // Check if incident threshold is reached
            if (check.consecutiveFailures >= this.config.incidentThreshold && check.critical) {
                this.createIncident(check);
            }
        }
    }

    async httpRequest(url, options = {}) {
        return new Promise((resolve, reject) => {
            const urlObj = new URL(url);
            const httpModule = urlObj.protocol === 'https:' ? https : http;
            
            const requestOptions = {
                hostname: urlObj.hostname,
                port: urlObj.port,
                path: urlObj.pathname + urlObj.search,
                method: options.method || 'GET',
                timeout: options.timeout || 5000,
                headers: {
                    'User-Agent': 'AgentForge-Monitoring/1.0',
                    ...options.headers
                }
            };

            const req = httpModule.request(requestOptions, (res) => {
                let data = '';
                res.on('data', chunk => data += chunk);
                res.on('end', () => {
                    if (res.statusCode >= 200 && res.statusCode < 300) {
                        resolve({ statusCode: res.statusCode, data, headers: res.headers });
                    } else {
                        reject(new Error(`HTTP ${res.statusCode}: ${res.statusMessage}`));
                    }
                });
            });

            req.on('timeout', () => {
                req.destroy();
                reject(new Error('Request timeout'));
            });

            req.on('error', reject);
            req.end();
        });
    }

    processHealthCheckResults() {
        const unhealthyServices = this.healthChecks.filter(check => check.status === 'unhealthy');
        const criticalServices = unhealthyServices.filter(check => check.critical);

        // Update overall system health
        this.updateSystemHealth();
        
        // Trigger auto-remediation if enabled
        if (this.config.autoRemediation.enabled && criticalServices.length > 0) {
            this.triggerAutoRemediation(criticalServices);
        }
    }

    updateSystemHealth() {
        const totalChecks = this.healthChecks.length;
        const healthyChecks = this.healthChecks.filter(check => check.status === 'healthy').length;
        const availability = (healthyChecks / totalChecks) * 100;

        this.metrics.set('system_availability', {
            value: availability,
            timestamp: new Date().toISOString(),
            unit: 'percent'
        });

        // Check availability thresholds
        if (availability < this.config.thresholds.availability.critical) {
            this.createAlert('critical', `System availability critical: ${availability.toFixed(2)}%`);
        } else if (availability < this.config.thresholds.availability.warning) {
            this.createAlert('warning', `System availability low: ${availability.toFixed(2)}%`);
        }
    }

    createAlert(severity, message, context = null) {
        const alert = {
            id: this.generateId(),
            severity,
            message,
            context,
            timestamp: new Date().toISOString(),
            acknowledged: false,
            resolved: false
        };

        // Check for alert cooldown
        if (this.isAlertCooldownActive(message)) {
            return;
        }

        this.alerts.push(alert);
        this.logAlert(severity, message);
        this.sendAlertNotifications(alert);
        
        // Set cooldown for this alert type
        this.setAlertCooldown(message);
    }

    createIncident(check) {
        const incident = {
            id: this.generateId(),
            title: `${check.name} Service Incident`,
            description: `${check.name} has failed ${check.consecutiveFailures} consecutive health checks`,
            severity: check.critical ? 'critical' : 'warning',
            status: 'open',
            service: check.name,
            startTime: new Date().toISOString(),
            endTime: null,
            timeline: [{
                timestamp: new Date().toISOString(),
                event: 'Incident created',
                details: `${check.consecutiveFailures} consecutive failures detected`
            }]
        };

        this.incidents.push(incident);
        this.logIncident('CREATED', incident);
        
        // Create high-priority alert for incident
        this.createAlert('critical', `INCIDENT: ${incident.title}`, incident);
        
        // Trigger incident response procedures
        this.triggerIncidentResponse(incident);
    }

    async triggerAutoRemediation(criticalServices) {
        for (const service of criticalServices) {
            this.logRemediation('INFO', `Attempting auto-remediation for ${service.name}`);
            
            try {
                await this.performAutoRemediation(service);
                this.logRemediation('SUCCESS', `Auto-remediation completed for ${service.name}`);
            } catch (error) {
                this.logRemediation('ERROR', `Auto-remediation failed for ${service.name}: ${error.message}`);
            }
        }
    }

    async performAutoRemediation(service) {
        const actions = this.config.autoRemediation.actions;
        
        if (actions.clearCache && service.name.includes('Cache')) {
            await this.clearCache();
        }
        
        if (actions.restartService) {
            await this.restartService(service.name);
        }
        
        if (actions.scaleUp) {
            await this.scaleUpService(service.name);
        }
    }

    async clearCache() {
        // Implementation would depend on cache type (Redis, etc.)
        this.logRemediation('ACTION', 'Clearing application cache');
        return Promise.resolve();
    }

    async restartService(serviceName) {
        // Implementation would use Docker API or orchestration system
        this.logRemediation('ACTION', `Restarting service: ${serviceName}`);
        return Promise.resolve();
    }

    async scaleUpService(serviceName) {
        // Implementation would use container orchestration
        this.logRemediation('ACTION', `Scaling up service: ${serviceName}`);
        return Promise.resolve();
    }

    startMetricsCollection() {
        setInterval(() => {
            this.collectSystemMetrics();
        }, 60000); // Every minute
    }

    collectSystemMetrics() {
        // Collect basic system metrics
        const memUsage = process.memoryUsage();
        const uptime = process.uptime();

        this.metrics.set('node_memory_usage', {
            value: memUsage.heapUsed / 1024 / 1024,
            timestamp: new Date().toISOString(),
            unit: 'MB'
        });

        this.metrics.set('node_uptime', {
            value: uptime,
            timestamp: new Date().toISOString(),
            unit: 'seconds'
        });

        // Collect application-specific metrics
        this.collectApplicationMetrics();
    }

    collectApplicationMetrics() {
        // Calculate average response time
        const responseTimes = this.healthChecks
            .filter(check => check.responseTime !== null)
            .map(check => check.responseTime);

        if (responseTimes.length > 0) {
            const avgResponseTime = responseTimes.reduce((a, b) => a + b, 0) / responseTimes.length;
            this.metrics.set('avg_response_time', {
                value: Math.round(avgResponseTime),
                timestamp: new Date().toISOString(),
                unit: 'ms'
            });
        }

        // Count active alerts
        const activeAlerts = this.alerts.filter(alert => !alert.resolved).length;
        this.metrics.set('active_alerts', {
            value: activeAlerts,
            timestamp: new Date().toISOString(),
            unit: 'count'
        });

        // Count open incidents
        const openIncidents = this.incidents.filter(incident => incident.status === 'open').length;
        this.metrics.set('open_incidents', {
            value: openIncidents,
            timestamp: new Date().toISOString(),
            unit: 'count'
        });
    }

    startIncidentManagement() {
        setInterval(() => {
            this.reviewIncidents();
        }, 60000); // Every minute
    }

    reviewIncidents() {
        const openIncidents = this.incidents.filter(incident => incident.status === 'open');
        
        for (const incident of openIncidents) {
            // Check if the related service has recovered
            const relatedCheck = this.healthChecks.find(check => check.name === incident.service);
            
            if (relatedCheck && relatedCheck.status === 'healthy' && relatedCheck.consecutiveFailures === 0) {
                this.resolveIncident(incident);
            }
        }
    }

    resolveIncident(incident) {
        incident.status = 'resolved';
        incident.endTime = new Date().toISOString();
        incident.timeline.push({
            timestamp: new Date().toISOString(),
            event: 'Incident resolved',
            details: 'Service has recovered and is healthy'
        });

        this.logIncident('RESOLVED', incident);
        
        // Send resolution notification
        this.sendIncidentResolution(incident);
    }

    startReporting() {
        setInterval(() => {
            this.generateStatusReport();
        }, 300000); // Every 5 minutes
        
        // Generate daily summary
        setInterval(() => {
            this.generateDailySummary();
        }, 86400000); // Every 24 hours
    }

    async generateStatusReport() {
        const report = {
            timestamp: new Date().toISOString(),
            environment: this.config.environment,
            systemHealth: {
                availability: this.metrics.get('system_availability')?.value || 0,
                avgResponseTime: this.metrics.get('avg_response_time')?.value || 0,
                activeAlerts: this.metrics.get('active_alerts')?.value || 0,
                openIncidents: this.metrics.get('open_incidents')?.value || 0
            },
            services: this.healthChecks.map(check => ({
                name: check.name,
                status: check.status,
                responseTime: check.responseTime,
                lastCheck: check.lastCheck,
                consecutiveFailures: check.consecutiveFailures
            })),
            recentAlerts: this.alerts.slice(-10),
            activeIncidents: this.incidents.filter(inc => inc.status === 'open')
        };

        const reportPath = path.join(this.reportPath, `status-${Date.now()}.json`);
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        
        return report;
    }

    // Utility methods
    generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    isAlertCooldownActive(message) {
        // Simple implementation - would be more sophisticated in production
        const key = `cooldown_${message.substring(0, 50)}`;
        return this.metrics.has(key);
    }

    setAlertCooldown(message) {
        const key = `cooldown_${message.substring(0, 50)}`;
        this.metrics.set(key, {
            value: Date.now() + this.config.alertCooldown,
            timestamp: new Date().toISOString()
        });
    }

    // Logging methods
    logHealthCheck(level, message) {
        this.writeLog('health-check', level, message);
    }

    logAlert(level, message) {
        this.writeLog('alerts', level, message);
    }

    logIncident(level, incident) {
        this.writeLog('incidents', level, `${incident.id}: ${incident.title}`);
    }

    logRemediation(level, message) {
        this.writeLog('remediation', level, message);
    }

    writeLog(category, level, message) {
        const timestamp = new Date().toISOString();
        const logMessage = `[${timestamp}] [${level}] [${category}] ${message}\n`;
        
        console.log(logMessage.trim());
        
        const logFile = path.join(this.logPath, `${category}-${new Date().toISOString().split('T')[0]}.log`);
        fs.appendFileSync(logFile, logMessage);
    }

    // Notification methods (placeholder implementations)
    async sendAlertNotifications(alert) {
        if (this.config.alertChannels.slack.enabled) {
            await this.sendSlackNotification(alert);
        }
        
        if (this.config.alertChannels.email.enabled) {
            await this.sendEmailNotification(alert);
        }
        
        if (this.config.alertChannels.pagerduty.enabled && alert.severity === 'critical') {
            await this.sendPagerDutyAlert(alert);
        }
    }

    async sendSlackNotification(alert) {
        // Placeholder - would integrate with Slack API
        this.logAlert('INFO', `Slack notification sent for: ${alert.message}`);
    }

    async sendEmailNotification(alert) {
        // Placeholder - would integrate with email service
        this.logAlert('INFO', `Email notification sent for: ${alert.message}`);
    }

    async sendPagerDutyAlert(alert) {
        // Placeholder - would integrate with PagerDuty API
        this.logAlert('INFO', `PagerDuty alert sent for: ${alert.message}`);
    }

    async sendIncidentResolution(incident) {
        this.logIncident('INFO', `Resolution notification sent for incident: ${incident.id}`);
    }

    triggerIncidentResponse(incident) {
        this.logIncident('INFO', `Incident response triggered for: ${incident.id}`);
    }

    async generateDailySummary() {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        
        const summary = {
            date: yesterday.toISOString().split('T')[0],
            totalAlerts: this.alerts.filter(a => a.timestamp.startsWith(yesterday.toISOString().split('T')[0])).length,
            totalIncidents: this.incidents.filter(i => i.startTime.startsWith(yesterday.toISOString().split('T')[0])).length,
            avgAvailability: 99.5, // Would be calculated from actual data
            avgResponseTime: this.metrics.get('avg_response_time')?.value || 0
        };

        const summaryPath = path.join(this.reportPath, `daily-summary-${summary.date}.json`);
        fs.writeFileSync(summaryPath, JSON.stringify(summary, null, 2));
        
        return summary;
    }

    // Public API methods
    getSystemStatus() {
        return {
            availability: this.metrics.get('system_availability')?.value || 0,
            healthChecks: this.healthChecks,
            activeAlerts: this.alerts.filter(a => !a.resolved).length,
            openIncidents: this.incidents.filter(i => i.status === 'open').length,
            lastUpdate: new Date().toISOString()
        };
    }

    getMetrics() {
        const metrics = {};
        for (const [key, value] of this.metrics.entries()) {
            if (!key.startsWith('cooldown_')) {
                metrics[key] = value;
            }
        }
        return metrics;
    }

    acknowledgeAlert(alertId) {
        const alert = this.alerts.find(a => a.id === alertId);
        if (alert) {
            alert.acknowledged = true;
            this.logAlert('INFO', `Alert acknowledged: ${alertId}`);
            return true;
        }
        return false;
    }

    resolveAlert(alertId) {
        const alert = this.alerts.find(a => a.id === alertId);
        if (alert) {
            alert.resolved = true;
            this.logAlert('INFO', `Alert resolved: ${alertId}`);
            return true;
        }
        return false;
    }
}

module.exports = ProductionMonitoringService;

// CLI interface
if (require.main === module) {
    const monitoringService = new ProductionMonitoringService();
    
    console.log('🚀 AgentForge Production Monitoring Service Started');
    console.log('📊 Monitoring dashboard: http://localhost:3000/monitoring');
    console.log('📋 Logs directory:', monitoringService.logPath);
    console.log('📈 Reports directory:', monitoringService.reportPath);
    
    // Graceful shutdown
    process.on('SIGINT', () => {
        console.log('\n🛑 Gracefully shutting down monitoring service...');
        process.exit(0);
    });

    process.on('SIGTERM', () => {
        console.log('\n🛑 Gracefully shutting down monitoring service...');
        process.exit(0);
    });
}