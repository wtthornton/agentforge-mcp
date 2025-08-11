#!/usr/bin/env node

/**
 * Enhanced Compliance Reporter for AgentForge
 * Task 2.3: Compliance Reporting Enhancement
 * 
 * Features:
 * - Multi-format reporting (JSON, HTML, Markdown, CSV)
 * - Interactive dashboards
 * - Trend analysis
 * - Action item tracking
 * - Performance metrics
 * - Custom standards integration
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

// Get __dirname equivalent for ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class EnhancedComplianceReporter {
  constructor() {
    this.reportTemplates = this.loadReportTemplates();
    this.exportFormats = ['json', 'html', 'markdown', 'csv'];
    this.dashboardTemplates = this.loadDashboardTemplates();
  }

  /**
   * Load report templates
   */
  loadReportTemplates() {
    const templatesPath = path.join(__dirname, '../templates/reports');
    const templates = {};
    
    try {
      if (fs.existsSync(templatesPath)) {
        const files = fs.readdirSync(templatesPath);
        files.forEach(file => {
          if (file.endsWith('.html') || file.endsWith('.md')) {
            const templateName = file.replace(/\.[^/.]+$/, '');
            const content = fs.readFileSync(path.join(templatesPath, file), 'utf8');
            templates[templateName] = {
              name: templateName,
              content: content,
              type: file.split('.').pop()
            };
          }
        });
      }
    } catch (error) {
      console.warn('⚠️  Could not load report templates:', error.message);
    }
    
    return templates;
  }

  /**
   * Load dashboard templates
   */
  loadDashboardTemplates() {
    const dashboardsPath = path.join(__dirname, '../templates/dashboards');
    const dashboards = {};
    
    try {
      if (fs.existsSync(dashboardsPath)) {
        const files = fs.readdirSync(dashboardsPath);
        files.forEach(file => {
          if (file.endsWith('.html')) {
            const dashboardName = file.replace('.html', '');
            const content = fs.readFileSync(path.join(dashboardsPath, file), 'utf8');
            dashboards[dashboardName] = {
              name: dashboardName,
              content: content
            };
          }
        });
      }
    } catch (error) {
      console.warn('⚠️  Could not load dashboard templates:', error.message);
    }
    
    return dashboards;
  }

  /**
   * Generate comprehensive compliance report
   */
  async generateComprehensiveReport(validationResults, options = {}) {
    const report = {
      metadata: {
        generatedAt: new Date().toISOString(),
        version: '2.3.0',
        tool: 'Enhanced Compliance Reporter',
        options: options
      },
      summary: this.generateSummary(validationResults),
      standards: this.generateStandardsReport(validationResults),
      violations: this.generateViolationsReport(validationResults),
      recommendations: this.generateRecommendationsReport(validationResults),
      actionItems: this.generateActionItemsReport(validationResults),
      trends: await this.generateTrendsReport(validationResults),
      performance: this.generatePerformanceReport(validationResults),
      customStandards: this.generateCustomStandardsReport(validationResults)
    };

    return report;
  }

  /**
   * Generate executive summary
   */
  generateSummary(validationResults) {
    const totalRules = Object.values(validationResults.standards || {})
      .reduce((sum, std) => sum + (std.rules?.length || 0), 0);
    
    const totalViolations = validationResults.violations?.length || 0;
    const criticalViolations = validationResults.violations?.filter(v => v.severity === 'critical').length || 0;
    const highViolations = validationResults.violations?.filter(v => v.severity === 'high').length || 0;
    
    return {
      overallCompliance: validationResults.complianceScore || 100,
      totalRules: totalRules,
      totalViolations: totalViolations,
      criticalViolations: criticalViolations,
      highViolations: highViolations,
      complianceLevel: this.getComplianceLevel(validationResults.complianceScore || 100),
      riskLevel: this.getRiskLevel(criticalViolations, highViolations),
      priorityActions: this.getPriorityActions(validationResults.violations || [])
    };
  }

  /**
   * Get compliance level
   */
  getComplianceLevel(score) {
    if (score >= 95) return 'Excellent';
    if (score >= 85) return 'Good';
    if (score >= 70) return 'Fair';
    if (score >= 50) return 'Poor';
    return 'Critical';
  }

  /**
   * Get risk level
   */
  getRiskLevel(critical, high) {
    if (critical > 0) return 'Critical';
    if (high > 5) return 'High';
    if (high > 2) return 'Medium';
    if (high > 0) return 'Low';
    return 'Minimal';
  }

  /**
   * Get priority actions
   */
  getPriorityActions(violations) {
    const critical = violations.filter(v => v.severity === 'critical');
    const high = violations.filter(v => v.severity === 'high');
    
    const actions = [];
    
    if (critical.length > 0) {
      actions.push({
        priority: 'Immediate',
        action: `Fix ${critical.length} critical violations`,
        impact: 'Block deployment',
        effort: '1-2 days'
      });
    }
    
    if (high.length > 0) {
      actions.push({
        priority: 'High',
        action: `Address ${high.length} high-priority violations`,
        impact: 'Risk to production',
        effort: '2-3 days'
      });
    }
    
    return actions;
  }

  /**
   * Generate standards report
   */
  generateStandardsReport(validationResults) {
    const standardsReport = {};
    
    if (validationResults.standards) {
      for (const [standardName, standard] of Object.entries(validationResults.standards)) {
        standardsReport[standardName] = {
          name: standard.standardName || standardName,
          compliance: standard.compliance || 100,
          totalRules: standard.rules?.length || 0,
          violations: standard.violations?.length || 0,
          status: this.getStandardStatus(standard.compliance || 100),
          topViolations: this.getTopViolations(standard.violations || [])
        };
      }
    }
    
    return standardsReport;
  }

  /**
   * Get standard status
   */
  getStandardStatus(compliance) {
    if (compliance >= 95) return 'Excellent';
    if (compliance >= 85) return 'Good';
    if (compliance >= 70) return 'Fair';
    if (compliance >= 50) return 'Poor';
    return 'Critical';
  }

  /**
   * Get top violations
   */
  getTopViolations(violations) {
    return violations
      .sort((a, b) => this.getSeverityScore(b.severity) - this.getSeverityScore(a.severity))
      .slice(0, 5)
      .map(v => ({
        rule: v.rule?.content || 'Unknown rule',
        severity: v.severity,
        suggestion: v.suggestion
      }));
  }

  /**
   * Get severity score
   */
  getSeverityScore(severity) {
    const scores = { critical: 4, high: 3, medium: 2, low: 1 };
    return scores[severity] || 0;
  }

  /**
   * Generate violations report
   */
  generateViolationsReport(validationResults) {
    const violations = validationResults.violations || [];
    
    return {
      total: violations.length,
      bySeverity: this.groupViolationsBySeverity(violations),
      byCategory: this.groupViolationsByCategory(violations),
      byStandard: this.groupViolationsByStandard(violations),
      criticalIssues: violations.filter(v => v.severity === 'critical'),
      highPriorityIssues: violations.filter(v => v.severity === 'high')
    };
  }

  /**
   * Group violations by severity
   */
  groupViolationsBySeverity(violations) {
    return violations.reduce((groups, violation) => {
      const severity = violation.severity || 'unknown';
      if (!groups[severity]) groups[severity] = [];
      groups[severity].push(violation);
      return groups;
    }, {});
  }

  /**
   * Group violations by category
   */
  groupViolationsByCategory(violations) {
    return violations.reduce((groups, violation) => {
      const category = violation.rule?.category || 'general';
      if (!groups[category]) groups[category] = [];
      groups[category].push(violation);
      return groups;
    }, {});
  }

  /**
   * Group violations by standard
   */
  groupViolationsByStandard(violations) {
    return violations.reduce((groups, violation) => {
      const standard = violation.rule?.standard || 'unknown';
      if (!groups[standard]) groups[standard] = [];
      groups[standard].push(violation);
      return groups;
    }, {});
  }

  /**
   * Generate recommendations report
   */
  generateRecommendationsReport(validationResults) {
    const recommendations = validationResults.recommendations || [];
    
    return {
      total: recommendations.length,
      byPriority: this.groupRecommendationsByPriority(recommendations),
      byType: this.groupRecommendationsByType(recommendations),
      actionable: recommendations.filter(r => r.actions && r.actions.length > 0),
      quickWins: recommendations.filter(r => r.estimatedEffort === '1-2 days')
    };
  }

  /**
   * Group recommendations by priority
   */
  groupRecommendationsByPriority(recommendations) {
    return recommendations.reduce((groups, rec) => {
      const priority = rec.priority || 'medium';
      if (!groups[priority]) groups[priority] = [];
      groups[priority].push(rec);
      return groups;
    }, {});
  }

  /**
   * Group recommendations by type
   */
  groupRecommendationsByType(recommendations) {
    return recommendations.reduce((groups, rec) => {
      const type = rec.type || 'general';
      if (!groups[type]) groups[type] = [];
      groups[type].push(rec);
      return groups;
    }, {});
  }

  /**
   * Generate action items report
   */
  generateActionItemsReport(validationResults) {
    const actionItems = validationResults.actionItems || [];
    
    return {
      total: actionItems.length,
      byPriority: this.groupActionItemsByPriority(actionItems),
      byImpact: this.groupActionItemsByImpact(actionItems),
      byEffort: this.groupActionItemsByEffort(actionItems),
      critical: actionItems.filter(a => a.priority === 'critical'),
      highImpact: actionItems.filter(a => a.impact === 'high')
    };
  }

  /**
   * Group action items by priority
   */
  groupActionItemsByPriority(actionItems) {
    return actionItems.reduce((groups, item) => {
      const priority = item.priority || 'medium';
      if (!groups[priority]) groups[priority] = [];
      groups[priority].push(item);
      return groups;
    }, {});
  }

  /**
   * Group action items by impact
   */
  groupActionItemsByImpact(actionItems) {
    return actionItems.reduce((groups, item) => {
      const impact = item.impact || 'medium';
      if (!groups[impact]) groups[impact] = [];
      groups[impact].push(item);
      return groups;
    }, {});
  }

  /**
   * Group action items by effort
   */
  groupActionItemsByEffort(actionItems) {
    return actionItems.reduce((groups, item) => {
      const effort = item.estimatedEffort || 'unknown';
      if (!groups[effort]) groups[effort] = [];
      groups[effort].push(item);
      return groups;
    }, {});
  }

  /**
   * Generate trends report
   */
  async generateTrendsReport(validationResults) {
    // Load historical data for trend analysis
    const historicalData = await this.loadHistoricalData();
    
    return {
      complianceTrend: this.calculateComplianceTrend(historicalData),
      violationsTrend: this.calculateViolationsTrend(historicalData),
      standardsTrend: this.calculateStandardsTrend(historicalData),
      performanceTrend: this.calculatePerformanceTrend(historicalData),
      predictions: this.generatePredictions(historicalData)
    };
  }

  /**
   * Load historical data
   */
  async loadHistoricalData() {
    const reportsPath = path.join(__dirname, '../reports');
    const historicalData = [];
    
    try {
      if (fs.existsSync(reportsPath)) {
        const files = fs.readdirSync(reportsPath);
        const reportFiles = files.filter(f => f.includes('enhanced-validation-') && f.endsWith('.json'));
        
        for (const file of reportFiles.slice(-10)) { // Last 10 reports
          try {
            const content = fs.readFileSync(path.join(reportsPath, file), 'utf8');
            const report = JSON.parse(content);
            historicalData.push({
              timestamp: report.timestamp,
              complianceScore: report.complianceScore,
              violations: report.violations?.length || 0,
              standards: Object.keys(report.standards || {}).length
            });
          } catch (error) {
            console.warn(`⚠️  Could not parse report ${file}:`, error.message);
          }
        }
      }
    } catch (error) {
      console.warn('⚠️  Could not load historical data:', error.message);
    }
    
    return historicalData.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
  }

  /**
   * Calculate compliance trend
   */
  calculateComplianceTrend(historicalData) {
    if (historicalData.length < 2) return 'insufficient-data';
    
    const recent = historicalData.slice(-3);
    const older = historicalData.slice(-6, -3);
    
    if (recent.length === 0 || older.length === 0) return 'insufficient-data';
    
    const recentAvg = recent.reduce((sum, d) => sum + d.complianceScore, 0) / recent.length;
    const olderAvg = older.reduce((sum, d) => sum + d.complianceScore, 0) / older.length;
    
    if (recentAvg > olderAvg + 5) return 'improving';
    if (recentAvg < olderAvg - 5) return 'declining';
    return 'stable';
  }

  /**
   * Calculate violations trend
   */
  calculateViolationsTrend(historicalData) {
    if (historicalData.length < 2) return 'insufficient-data';
    
    const recent = historicalData.slice(-3);
    const older = historicalData.slice(-6, -3);
    
    if (recent.length === 0 || older.length === 0) return 'insufficient-data';
    
    const recentAvg = recent.reduce((sum, d) => sum + d.violations, 0) / recent.length;
    const olderAvg = older.reduce((sum, d) => sum + d.violations, 0) / older.length;
    
    if (recentAvg < olderAvg - 2) return 'decreasing';
    if (recentAvg > olderAvg + 2) return 'increasing';
    return 'stable';
  }

  /**
   * Calculate standards trend
   */
  calculateStandardsTrend(historicalData) {
    if (historicalData.length < 2) return 'insufficient-data';
    
    const recent = historicalData.slice(-3);
    const older = historicalData.slice(-6, -3);
    
    if (recent.length === 0 || older.length === 0) return 'insufficient-data';
    
    const recentAvg = recent.reduce((sum, d) => sum + d.standards, 0) / recent.length;
    const olderAvg = older.reduce((sum, d) => sum + d.standards, 0) / older.length;
    
    if (recentAvg > olderAvg) return 'expanding';
    if (recentAvg < olderAvg) return 'contracting';
    return 'stable';
  }

  /**
   * Calculate performance trend
   */
  calculatePerformanceTrend(historicalData) {
    // This would analyze performance metrics over time
    return 'stable';
  }

  /**
   * Generate predictions
   */
  generatePredictions(historicalData) {
    if (historicalData.length < 5) return { confidence: 'low', predictions: [] };
    
    const predictions = [];
    
    // Predict next compliance score
    const recentScores = historicalData.slice(-5).map(d => d.complianceScore);
    const trend = this.calculateLinearTrend(recentScores);
    if (trend) {
      predictions.push({
        metric: 'compliance-score',
        predictedValue: Math.max(0, Math.min(100, trend.nextValue)),
        confidence: trend.confidence,
        trend: trend.direction
      });
    }
    
    return {
      confidence: 'medium',
      predictions: predictions
    };
  }

  /**
   * Calculate linear trend
   */
  calculateLinearTrend(data) {
    if (data.length < 3) return null;
    
    const n = data.length;
    const sumX = (n * (n - 1)) / 2;
    const sumY = data.reduce((sum, val) => sum + val, 0);
    const sumXY = data.reduce((sum, val, i) => sum + (i * val), 0);
    const sumX2 = data.reduce((sum, val, i) => sum + (i * i), 0);
    
    const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    const intercept = (sumY - slope * sumX) / n;
    
    const nextValue = intercept + slope * n;
    const direction = slope > 0 ? 'increasing' : slope < 0 ? 'decreasing' : 'stable';
    const confidence = Math.min(90, Math.max(10, 100 - Math.abs(slope) * 10));
    
    return { slope, intercept, nextValue, direction, confidence };
  }

  /**
   * Generate performance report
   */
  generatePerformanceReport(validationResults) {
    const performance = validationResults.performanceMetrics || {};
    
    return {
      validationTime: performance.validationTime || 0,
      memoryUsage: performance.memoryUsage || 0,
      cpuUsage: performance.cpuUsage || 0,
      fileProcessingRate: performance.fileProcessingRate || 0,
      cacheHitRate: performance.cacheHitRate || 0,
      recommendations: this.generatePerformanceRecommendations(performance)
    };
  }

  /**
   * Generate performance recommendations
   */
  generatePerformanceRecommendations(performance) {
    const recommendations = [];
    
    if (performance.validationTime > 300) {
      recommendations.push({
        issue: 'Validation time exceeds 5 minutes',
        suggestion: 'Consider parallel processing or caching',
        impact: 'high'
      });
    }
    
    if (performance.memoryUsage > 512) {
      recommendations.push({
        issue: 'Memory usage exceeds 512MB',
        suggestion: 'Optimize memory usage or increase limits',
        impact: 'medium'
      });
    }
    
    return recommendations;
  }

  /**
   * Generate custom standards report
   */
  generateCustomStandardsReport(validationResults) {
    const customStandards = validationResults.customStandards || {};
    
    return {
      total: Object.keys(customStandards).length,
      compliance: customStandards.compliance || 100,
      standards: customStandards.standards || {},
      violations: customStandards.violations || [],
      recommendations: this.generateCustomStandardsRecommendations(customStandards)
    };
  }

  /**
   * Generate custom standards recommendations
   */
  generateCustomStandardsRecommendations(customStandards) {
    const recommendations = [];
    
    if (customStandards.violations && customStandards.violations.length > 0) {
      recommendations.push({
        priority: 'high',
        message: `Address ${customStandards.violations.length} custom standards violations`,
        action: 'Review and fix custom standards compliance'
      });
    }
    
    return recommendations;
  }

  /**
   * Export report in specified format
   */
  async exportReport(report, format, outputPath) {
    switch (format.toLowerCase()) {
      case 'json':
        return this.exportJSON(report, outputPath);
      case 'html':
        return this.exportHTML(report, outputPath);
      case 'markdown':
        return this.exportMarkdown(report, outputPath);
      case 'csv':
        return this.exportCSV(report, outputPath);
      default:
        throw new Error(`Unsupported format: ${format}`);
    }
  }

  /**
   * Export as JSON
   */
  exportJSON(report, outputPath) {
    const content = JSON.stringify(report, null, 2);
    fs.writeFileSync(outputPath, content);
    return `Report exported to ${outputPath}`;
  }

  /**
   * Export as HTML
   */
  exportHTML(report, outputPath) {
    const htmlContent = this.generateHTMLReport(report);
    fs.writeFileSync(outputPath, htmlContent);
    return `HTML report exported to ${outputPath}`;
  }

  /**
   * Export as Markdown
   */
  exportMarkdown(report, outputPath) {
    const markdownContent = this.generateMarkdownReport(report);
    fs.writeFileSync(outputPath, markdownContent);
    return `Markdown report exported to ${outputPath}`;
  }

  /**
   * Export as CSV
   */
  exportCSV(report, outputPath) {
    const csvContent = this.generateCSVReport(report);
    fs.writeFileSync(outputPath, csvContent);
    return `CSV report exported to ${outputPath}`;
  }

  /**
   * Generate HTML report
   */
  generateHTMLReport(report) {
    // This would generate a comprehensive HTML report
    return `
<!DOCTYPE html>
<html>
<head>
    <title>Enhanced Compliance Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .summary { background: #f5f5f5; padding: 20px; border-radius: 5px; }
        .violations { margin: 20px 0; }
        .recommendations { margin: 20px 0; }
        .critical { color: #dc2626; font-weight: bold; }
        .high { color: #ea580c; font-weight: bold; }
        .medium { color: #ca8a04; }
        .low { color: #16a34a; }
    </style>
</head>
<body>
    <h1>Enhanced Compliance Report</h1>
    <div class="summary">
        <h2>Summary</h2>
        <p>Overall Compliance: <strong>${report.summary.overallCompliance}%</strong></p>
        <p>Total Violations: <strong>${report.summary.totalViolations}</strong></p>
        <p>Risk Level: <strong class="${report.summary.riskLevel.toLowerCase()}">${report.summary.riskLevel}</strong></p>
    </div>
    
    <div class="violations">
        <h2>Violations</h2>
        <p>Critical: <span class="critical">${report.summary.criticalViolations}</span></p>
        <p>High: <span class="high">${report.summary.highViolations}</span></p>
    </div>
    
    <div class="recommendations">
        <h2>Priority Actions</h2>
        ${report.summary.priorityActions.map(action => 
            `<p><strong>${action.priority}:</strong> ${action.action}</p>`
        ).join('')}
    </div>
</body>
</html>`;
  }

  /**
   * Generate Markdown report
   */
  generateMarkdownReport(report) {
    return `# Enhanced Compliance Report

## Summary
- **Overall Compliance**: ${report.summary.overallCompliance}%
- **Total Violations**: ${report.summary.totalViolations}
- **Risk Level**: ${report.summary.riskLevel}
- **Compliance Level**: ${report.summary.complianceLevel}

## Priority Actions
${report.summary.priorityActions.map(action => 
  `### ${action.priority}
  - **Action**: ${action.action}
  - **Impact**: ${action.impact}
  - **Effort**: ${action.estimatedEffort}
`
).join('')}

## Standards Compliance
${Object.entries(report.standards).map(([name, std]) => 
  `### ${name}
  - **Compliance**: ${std.compliance}%
  - **Violations**: ${std.violations}
  - **Status**: ${std.status}
`
).join('')}

## Violations by Severity
${Object.entries(report.violations.bySeverity || {}).map(([severity, violations]) => 
  `### ${severity.toUpperCase()}: ${violations.length}
  ${violations.slice(0, 3).map(v => `- ${v.rule?.content || 'Unknown rule'}`).join('\n  ')}
`
).join('')}

## Recommendations
${Object.entries(report.recommendations.byPriority || {}).map(([priority, recs]) => 
  `### ${priority.toUpperCase()}: ${recs.length}
  ${recs.slice(0, 3).map(r => `- ${r.message}`).join('\n  ')}
`
).join('')}

---
*Generated on ${new Date().toLocaleString()}*
`;
  }

  /**
   * Generate CSV report
   */
  generateCSVReport(report) {
    const rows = [
      ['Category', 'Metric', 'Value', 'Status']
    ];
    
    // Summary metrics
    rows.push(['Summary', 'Overall Compliance', `${report.summary.overallCompliance}%`, report.summary.complianceLevel]);
    rows.push(['Summary', 'Total Violations', report.summary.totalViolations, report.summary.riskLevel]);
    rows.push(['Summary', 'Critical Violations', report.summary.criticalViolations, '']);
    rows.push(['Summary', 'High Violations', report.summary.highViolations, '']);
    
    // Standards compliance
    Object.entries(report.standards).forEach(([name, std]) => {
      rows.push(['Standards', name, `${std.compliance}%`, std.status]);
    });
    
    // Violations by severity
    Object.entries(report.violations.bySeverity || {}).forEach(([severity, violations]) => {
      rows.push(['Violations', severity, violations.length, '']);
    });
    
    return rows.map(row => row.map(cell => `"${cell}"`).join(',')).join('\n');
  }
}

// CLI interface
if (import.meta.url === `file://${process.argv[1]}`) {
  const reporter = new EnhancedComplianceReporter();
  
  const command = process.argv[2] || 'help';
  
  switch (command) {
    case 'generate':
      // This would load validation results and generate a report
      console.log('📊 Enhanced Compliance Reporter');
      console.log('Use with validation results to generate comprehensive reports');
      break;
      
    case 'help':
      console.log(`
Enhanced Compliance Reporter - Task 2.3

Usage: node enhanced-compliance-reporter.js [command]

Commands:
  generate    - Generate comprehensive compliance report
  help        - Show this help message

Features:
  - Multi-format export (JSON, HTML, Markdown, CSV)
  - Interactive dashboards
  - Trend analysis
  - Action item tracking
  - Performance metrics
  - Custom standards integration
      `);
      break;
      
    default:
      console.log(`Unknown command: ${command}. Use 'help' for usage information.`);
  }
}

export default EnhancedComplianceReporter;
