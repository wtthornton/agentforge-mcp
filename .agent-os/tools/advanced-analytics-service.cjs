/**
 * Advanced Analytics Service for AgentForge
 * Provides comprehensive analytics, reporting, and predictive capabilities
 * Part of Phase 3: Task 3.2 - Advanced Analytics & Reporting
 */

const fs = require('fs');
const path = require('path');
const { performance } = require('perf_hooks');

class AdvancedAnalyticsService {
    constructor() {
        this.metricsHistory = [];
        this.analyticsConfig = this.loadAnalyticsConfig();
        this.reportPath = path.join(process.cwd(), '.agent-os', 'reports');
        this.ensureReportDirectory();
    }

    loadAnalyticsConfig() {
        const configPath = path.join(process.cwd(), '.agent-os', 'config', 'analytics-config.json');
        if (fs.existsSync(configPath)) {
            return JSON.parse(fs.readFileSync(configPath, 'utf-8'));
        }
        
        const defaultConfig = {
            metricsRetention: 90, // days
            analysisIntervals: ['daily', 'weekly', 'monthly'],
            predictiveModels: ['trend', 'anomaly', 'performance'],
            dashboardRefresh: 300000, // 5 minutes
            alertThresholds: {
                performance: { warning: 200, critical: 500 },
                availability: { warning: 95, critical: 90 },
                errorRate: { warning: 1, critical: 5 }
            }
        };
        
        this.saveAnalyticsConfig(defaultConfig);
        return defaultConfig;
    }

    saveAnalyticsConfig(config) {
        const configDir = path.join(process.cwd(), '.agent-os', 'config');
        if (!fs.existsSync(configDir)) {
            fs.mkdirSync(configDir, { recursive: true });
        }
        const configPath = path.join(configDir, 'analytics-config.json');
        fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
    }

    ensureReportDirectory() {
        if (!fs.existsSync(this.reportPath)) {
            fs.mkdirSync(this.reportPath, { recursive: true });
        }
    }

    async collectAdvancedMetrics() {
        const startTime = performance.now();
        
        try {
            const metrics = {
                timestamp: new Date().toISOString(),
                system: await this.collectSystemMetrics(),
                application: await this.collectApplicationMetrics(),
                business: await this.collectBusinessMetrics(),
                performance: await this.collectPerformanceMetrics(),
                security: await this.collectSecurityMetrics()
            };

            this.metricsHistory.push(metrics);
            this.pruneMetricsHistory();

            const duration = performance.now() - startTime;
            console.log(`✅ Advanced metrics collection completed in ${duration.toFixed(2)}ms`);
            
            return metrics;
        } catch (error) {
            console.error('❌ Error collecting advanced metrics:', error.message);
            throw error;
        }
    }

    async collectSystemMetrics() {
        const memUsage = process.memoryUsage();
        
        return {
            uptime: process.uptime(),
            memoryUsage: {
                rss: memUsage.rss / 1024 / 1024, // MB
                heapTotal: memUsage.heapTotal / 1024 / 1024,
                heapUsed: memUsage.heapUsed / 1024 / 1024,
                external: memUsage.external / 1024 / 1024
            },
            cpuUsage: process.cpuUsage(),
            loadAverage: require('os').loadavg(),
            platform: process.platform,
            version: process.version,
            arch: process.arch
        };
    }

    async collectApplicationMetrics() {
        return {
            requestCount: this.getMetricFromHistory('requestCount', 3600000), // last hour
            responseTime: this.calculateAverageResponseTime(),
            errorRate: this.calculateErrorRate(),
            activeUsers: this.getActiveUserCount(),
            cacheHitRate: this.calculateCacheHitRate(),
            databaseConnections: this.getDatabaseConnectionCount(),
            queueDepth: this.getQueueDepth()
        };
    }

    async collectBusinessMetrics() {
        return {
            projectAnalyses: this.getAnalysisCount(),
            complianceScore: await this.calculateComplianceScore(),
            codeQuality: await this.calculateCodeQualityScore(),
            userSatisfaction: this.getUserSatisfactionScore(),
            featureUsage: this.getFeatureUsageMetrics(),
            conversionRate: this.getConversionRate()
        };
    }

    async collectPerformanceMetrics() {
        return {
            throughput: this.calculateThroughput(),
            latency: {
                p50: this.calculatePercentile(50),
                p90: this.calculatePercentile(90),
                p95: this.calculatePercentile(95),
                p99: this.calculatePercentile(99)
            },
            availability: this.calculateAvailability(),
            resourceUtilization: this.getResourceUtilization(),
            scalingMetrics: this.getScalingMetrics()
        };
    }

    async collectSecurityMetrics() {
        return {
            securityEvents: this.getSecurityEventCount(),
            vulnerabilities: await this.scanVulnerabilities(),
            accessAttempts: this.getAccessAttempts(),
            complianceViolations: this.getComplianceViolations(),
            encryptionCoverage: this.calculateEncryptionCoverage()
        };
    }

    async performPredictiveAnalysis() {
        const startTime = performance.now();
        
        try {
            const analysis = {
                trendAnalysis: this.analyzeTrends(),
                anomalyDetection: this.detectAnomalies(),
                performancePrediction: this.predictPerformance(),
                capacityPlanning: this.planCapacity(),
                riskAssessment: this.assessRisks(),
                recommendations: this.generateRecommendations()
            };

            const duration = performance.now() - startTime;
            console.log(`✅ Predictive analysis completed in ${duration.toFixed(2)}ms`);
            
            return analysis;
        } catch (error) {
            console.error('❌ Error in predictive analysis:', error.message);
            throw error;
        }
    }

    analyzeTrends() {
        if (this.metricsHistory.length < 10) {
            return { status: 'insufficient_data', message: 'Need at least 10 data points for trend analysis' };
        }

        const trends = {
            performance: this.calculateTrend('performance.responseTime'),
            memory: this.calculateTrend('system.memoryUsage.heapUsed'),
            requests: this.calculateTrend('application.requestCount'),
            errors: this.calculateTrend('application.errorRate')
        };

        return {
            trends,
            summary: this.summarizeTrends(trends),
            confidence: this.calculateTrendConfidence(trends)
        };
    }

    detectAnomalies() {
        const anomalies = [];
        const metrics = ['performance.responseTime', 'system.memoryUsage.heapUsed', 'application.errorRate'];

        for (const metric of metrics) {
            const anomaly = this.detectMetricAnomalies(metric);
            if (anomaly.detected) {
                anomalies.push(anomaly);
            }
        }

        return {
            count: anomalies.length,
            anomalies,
            severity: this.calculateAnomalySeverity(anomalies)
        };
    }

    predictPerformance() {
        const performanceData = this.metricsHistory.map(m => ({
            timestamp: new Date(m.timestamp).getTime(),
            responseTime: this.getNestedValue(m, 'application.responseTime') || 0,
            throughput: this.getNestedValue(m, 'performance.throughput') || 0,
            memoryUsage: this.getNestedValue(m, 'system.memoryUsage.heapUsed') || 0
        }));

        if (performanceData.length < 5) {
            return { status: 'insufficient_data' };
        }

        return {
            responseTime: this.predictMetric(performanceData, 'responseTime'),
            throughput: this.predictMetric(performanceData, 'throughput'),
            memoryUsage: this.predictMetric(performanceData, 'memoryUsage'),
            forecast: this.generatePerformanceForecast(performanceData)
        };
    }

    async generateAdvancedReport(type = 'comprehensive') {
        const startTime = performance.now();
        
        try {
            const report = {
                metadata: {
                    type,
                    generatedAt: new Date().toISOString(),
                    version: '1.0.0',
                    dataPoints: this.metricsHistory.length
                },
                summary: await this.generateReportSummary(),
                metrics: await this.collectAdvancedMetrics(),
                analysis: await this.performPredictiveAnalysis(),
                recommendations: this.generateActionableRecommendations(),
                alerts: this.generateAlerts(),
                trends: this.generateTrendReport(),
                forecasts: this.generateForecastReport()
            };

            const reportPath = path.join(this.reportPath, `advanced-analytics-${type}-${Date.now()}.json`);
            fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));

            // Generate HTML dashboard
            const dashboardPath = await this.generateHTMLDashboard(report);

            const duration = performance.now() - startTime;
            console.log(`✅ Advanced report generated in ${duration.toFixed(2)}ms`);
            console.log(`📊 Report saved to: ${reportPath}`);
            console.log(`🌐 Dashboard saved to: ${dashboardPath}`);
            
            return { report, reportPath, dashboardPath };
        } catch (error) {
            console.error('❌ Error generating advanced report:', error.message);
            throw error;
        }
    }

    async generateHTMLDashboard(report) {
        const dashboardHTML = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AgentForge Advanced Analytics Dashboard</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
        .dashboard { max-width: 1200px; margin: 0 auto; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
        .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 20px; }
        .metric-card { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .metric-value { font-size: 2em; font-weight: bold; color: #667eea; }
        .metric-label { color: #666; margin-bottom: 10px; }
        .chart-container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }
        .alert { padding: 15px; border-radius: 5px; margin-bottom: 10px; }
        .alert-warning { background: #fff3cd; border: 1px solid #ffeaa7; color: #856404; }
        .alert-critical { background: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }
        .recommendations { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .recommendation { padding: 10px; margin-bottom: 10px; border-left: 4px solid #667eea; background: #f8f9fa; }
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="header">
            <h1>🚀 AgentForge Advanced Analytics Dashboard</h1>
            <p>Generated: ${new Date().toLocaleString()}</p>
            <p>Data Points: ${report.metadata.dataPoints} | Type: ${report.metadata.type}</p>
        </div>

        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-label">Average Response Time</div>
                <div class="metric-value">${report.metrics?.application?.responseTime || 0}ms</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">System Availability</div>
                <div class="metric-value">${report.metrics?.performance?.availability || 0}%</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Error Rate</div>
                <div class="metric-value">${report.metrics?.application?.errorRate || 0}%</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Compliance Score</div>
                <div class="metric-value">${report.metrics?.business?.complianceScore || 0}%</div>
            </div>
        </div>

        <div class="chart-container">
            <h3>📈 Performance Trends</h3>
            <canvas id="performanceChart" width="400" height="200"></canvas>
        </div>

        <div class="chart-container">
            <h3>💾 Memory Usage</h3>
            <canvas id="memoryChart" width="400" height="200"></canvas>
        </div>

        ${report.alerts && report.alerts.length > 0 ? `
        <div class="chart-container">
            <h3>🚨 Active Alerts</h3>
            ${report.alerts.map(alert => `
                <div class="alert alert-${alert.severity}">
                    <strong>${alert.title}</strong>: ${alert.message}
                </div>
            `).join('')}
        </div>
        ` : ''}

        <div class="recommendations">
            <h3>💡 Recommendations</h3>
            ${report.recommendations ? report.recommendations.map(rec => `
                <div class="recommendation">
                    <strong>${rec.category}</strong>: ${rec.description}
                    <br><small>Priority: ${rec.priority} | Impact: ${rec.impact}</small>
                </div>
            `).join('') : '<p>No recommendations at this time.</p>'}
        </div>
    </div>

    <script>
        // Performance Chart
        const performanceCtx = document.getElementById('performanceChart').getContext('2d');
        new Chart(performanceCtx, {
            type: 'line',
            data: {
                labels: ${JSON.stringify(this.generateTimeLabels())},
                datasets: [{
                    label: 'Response Time (ms)',
                    data: ${JSON.stringify(this.generatePerformanceData())},
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } },
                scales: {
                    y: { beginAtZero: true, title: { display: true, text: 'Response Time (ms)' } },
                    x: { title: { display: true, text: 'Time' } }
                }
            }
        });

        // Memory Chart
        const memoryCtx = document.getElementById('memoryChart').getContext('2d');
        new Chart(memoryCtx, {
            type: 'area',
            data: {
                labels: ${JSON.stringify(this.generateTimeLabels())},
                datasets: [{
                    label: 'Memory Usage (MB)',
                    data: ${JSON.stringify(this.generateMemoryData())},
                    borderColor: '#764ba2',
                    backgroundColor: 'rgba(118, 75, 162, 0.1)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: true } },
                scales: {
                    y: { beginAtZero: true, title: { display: true, text: 'Memory Usage (MB)' } },
                    x: { title: { display: true, text: 'Time' } }
                }
            }
        });
    </script>
</body>
</html>`;

        const dashboardPath = path.join(this.reportPath, `advanced-dashboard-${Date.now()}.html`);
        fs.writeFileSync(dashboardPath, dashboardHTML);
        return dashboardPath;
    }

    // Utility methods for calculations and data processing
    calculateTrend(metricPath) {
        const values = this.metricsHistory.map(m => this.getNestedValue(m, metricPath)).filter(v => v !== null);
        if (values.length < 2) return null;

        const n = values.length;
        const sumX = n * (n + 1) / 2;
        const sumY = values.reduce((a, b) => a + b, 0);
        const sumXY = values.reduce((sum, y, i) => sum + (i + 1) * y, 0);
        const sumX2 = n * (n + 1) * (2 * n + 1) / 6;

        const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        const intercept = (sumY - slope * sumX) / n;

        return { slope, intercept, direction: slope > 0 ? 'increasing' : 'decreasing' };
    }

    getNestedValue(obj, path) {
        return path.split('.').reduce((current, key) => current?.[key], obj);
    }

    generateTimeLabels() {
        return this.metricsHistory.slice(-10).map(m => new Date(m.timestamp).toLocaleTimeString());
    }

    generatePerformanceData() {
        return this.metricsHistory.slice(-10).map(m => this.getNestedValue(m, 'application.responseTime') || 0);
    }

    generateMemoryData() {
        return this.metricsHistory.slice(-10).map(m => this.getNestedValue(m, 'system.memoryUsage.heapUsed') || 0);
    }

    pruneMetricsHistory() {
        const maxAge = this.analyticsConfig.metricsRetention * 24 * 60 * 60 * 1000;
        const cutoffTime = Date.now() - maxAge;
        this.metricsHistory = this.metricsHistory.filter(m => new Date(m.timestamp).getTime() > cutoffTime);
    }

    // Mock data methods - would integrate with real data sources
    getMetricFromHistory(metric, timeWindow) { return Math.floor(Math.random() * 1000); }
    calculateAverageResponseTime() { return Math.floor(Math.random() * 200) + 50; }
    calculateErrorRate() { return Math.random() * 2; }
    getActiveUserCount() { return Math.floor(Math.random() * 100) + 10; }
    calculateCacheHitRate() { return Math.random() * 20 + 80; }
    getDatabaseConnectionCount() { return Math.floor(Math.random() * 10) + 5; }
    getQueueDepth() { return Math.floor(Math.random() * 50); }
    getAnalysisCount() { return Math.floor(Math.random() * 500) + 100; }
    async calculateComplianceScore() { return Math.floor(Math.random() * 10) + 90; }
    async calculateCodeQualityScore() { return Math.floor(Math.random() * 15) + 85; }
    getUserSatisfactionScore() { return Math.random() * 1 + 4; }
    getFeatureUsageMetrics() { return {}; }
    getConversionRate() { return Math.random() * 5 + 2; }
    calculateThroughput() { return Math.floor(Math.random() * 1000) + 500; }
    calculatePercentile(p) { return Math.floor(Math.random() * 100) + p; }
    calculateAvailability() { return Math.random() * 5 + 95; }
    getResourceUtilization() { return {}; }
    getScalingMetrics() { return {}; }
    getSecurityEventCount() { return Math.floor(Math.random() * 10); }
    async scanVulnerabilities() { return []; }
    getAccessAttempts() { return Math.floor(Math.random() * 1000) + 500; }
    getComplianceViolations() { return Math.floor(Math.random() * 3); }
    calculateEncryptionCoverage() { return Math.random() * 5 + 95; }
    detectMetricAnomalies(metric) { return { detected: false, metric }; }
    calculateAnomalySeverity(anomalies) { return 'low'; }
    predictMetric(data, metric) { return { prediction: 'stable', confidence: 0.8 }; }
    generatePerformanceForecast(data) { return {}; }
    async generateReportSummary() { return { status: 'healthy', score: 95 }; }
    generateActionableRecommendations() { 
        return [
            { category: 'Performance', description: 'Optimize database queries', priority: 'high', impact: 'medium' },
            { category: 'Security', description: 'Update dependencies', priority: 'medium', impact: 'high' }
        ]; 
    }
    generateAlerts() { return []; }
    generateTrendReport() { return {}; }
    generateForecastReport() { return {}; }
    summarizeTrends(trends) { return 'Overall trends are stable'; }
    calculateTrendConfidence(trends) { return 0.85; }
    planCapacity() { return {}; }
    assessRisks() { return {}; }
    generateRecommendations() { return []; }
}

module.exports = AdvancedAnalyticsService;

// CLI interface
if (require.main === module) {
    const analyticsService = new AdvancedAnalyticsService();
    
    async function runAnalytics() {
        console.log('🚀 Starting Advanced Analytics Service...');
        
        try {
            const metrics = await analyticsService.collectAdvancedMetrics();
            console.log('📊 Metrics collected successfully');
            
            const analysis = await analyticsService.performPredictiveAnalysis();
            console.log('🔮 Predictive analysis completed');
            
            const result = await analyticsService.generateAdvancedReport('comprehensive');
            console.log('📈 Advanced analytics report generated');
            console.log('🎯 Open the HTML dashboard in your browser to view results');
            
        } catch (error) {
            console.error('❌ Analytics service failed:', error.message);
            process.exit(1);
        }
    }
    
    runAnalytics();
}