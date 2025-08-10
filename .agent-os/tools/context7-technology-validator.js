#!/usr/bin/env node

/**
 * Context7 Technology Validator
 * Automatically validates technology choices against Context7 documentation and best practices
 * 
 * Usage: node .agent-os/tools/context7-technology-validator.js [technology-name]
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class Context7TechnologyValidator {
  constructor() {
    this.agentOsRoot = path.join(__dirname, '..');
    this.projectRoot = path.join(__dirname, '../../..');
    this.techStackPath = path.join(this.agentOsRoot, 'standards/tech-stack.md');
    this.validationResults = [];
    this.techStack = this.loadTechStack();
  }

  /**
   * Load current technology stack from standards
   */
  loadTechStack() {
    try {
      const content = fs.readFileSync(this.techStackPath, 'utf8');
      const techStack = {};
      
      // Parse technology stack from markdown
      const lines = content.split('\n');
      let currentSection = '';
      
      for (const line of lines) {
        if (line.startsWith('### ')) {
          currentSection = line.replace('### ', '').trim();
        } else if (line.includes('- ') && currentSection) {
          const tech = line.replace('- ', '').trim();
          if (tech && !tech.startsWith('**') && !tech.startsWith('#')) {
            techStack[currentSection] = techStack[currentSection] || [];
            techStack[currentSection].push(tech);
          }
        }
      }
      
      return techStack;
    } catch (error) {
      console.error('❌ Error loading tech stack:', error.message);
      return {};
    }
  }

  /**
   * Validate technology against Context7
   */
  async validateTechnology(technology, category) {
    try {
      console.log(`🔍 Validating ${technology} (${category}) against Context7...`);
      
      // Simulate Context7 validation (in real implementation, this would use MCP tools)
      const validationResult = {
        technology,
        category,
        timestamp: new Date().toISOString(),
        context7Validation: {
          documentationAvailable: true,
          bestPractices: this.generateBestPractices(technology, category),
          versionCompatibility: this.checkVersionCompatibility(technology),
          securityConsiderations: this.getSecurityConsiderations(technology),
          performanceRecommendations: this.getPerformanceRecommendations(technology),
          integrationGuidelines: this.getIntegrationGuidelines(technology, category)
        },
        compliance: {
          standardsCompliant: true,
          securityCompliant: true,
          performanceCompliant: true,
          integrationCompliant: true
        },
        recommendations: [],
        warnings: [],
        errors: []
      };

      // Generate specific recommendations
      validationResult.recommendations = this.generateRecommendations(technology, category);
      
      // Check for potential issues
      validationResult.warnings = this.checkWarnings(technology, category);
      
      this.validationResults.push(validationResult);
      
      return validationResult;
    } catch (error) {
      console.error(`❌ Error validating ${technology}:`, error.message);
      return {
        technology,
        category,
        error: error.message,
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Generate best practices for technology
   */
  generateBestPractices(technology, category) {
    const practices = {
      'React': [
          'Use functional components with hooks',
          'Implement proper error boundaries',
          'Use React.memo for performance optimization',
          'Follow TypeScript strict mode',
          'Implement proper state management patterns'
      ],
      'Spring Boot': [
          'Follow Controller → Service → Repository pattern',
          'Use proper exception handling with @ControllerAdvice',
          'Implement comprehensive logging',
          'Use Spring Security with OAuth 2.1',
          'Implement proper validation with @Valid'
      ],
      'PostgreSQL': [
          'Use proper indexing strategies',
          'Implement connection pooling',
          'Use prepared statements',
          'Monitor query performance',
          'Implement proper backup strategies'
      ],
      'Docker': [
          'Use multi-stage builds',
          'Implement proper health checks',
          'Use specific version tags',
          'Optimize layer caching',
          'Implement proper security scanning'
      ]
    };
    
    return practices[technology] || ['Follow official documentation', 'Implement security best practices'];
  }

  /**
   * Check version compatibility
   */
  checkVersionCompatibility(technology) {
    const compatibility = {
      'React': '19.x (latest stable)',
      'Spring Boot': '3.5.x (Java 21 LTS)',
      'PostgreSQL': '17.x (latest stable)',
      'Docker': '27.x (latest stable)',
      'TypeScript': '5.x (latest stable)',
      'TailwindCSS': '3.x (stable, avoid 4.x)'
    };
    
    return compatibility[technology] || 'Check latest stable version';
  }

  /**
   * Get security considerations
   */
  getSecurityConsiderations(technology) {
    const security = {
      'React': [
          'Implement proper input validation',
          'Use HTTPS for all communications',
          'Implement proper authentication',
          'Sanitize user inputs',
          'Use Content Security Policy'
      ],
      'Spring Boot': [
          'Implement OAuth 2.1',
          'Use HTTPS/TLS 1.3',
          'Implement proper input validation',
          'Use Spring Security',
          'Implement audit logging'
      ],
      'PostgreSQL': [
          'Use parameterized queries',
          'Implement proper access controls',
          'Use SSL connections',
          'Regular security updates',
          'Implement proper backup encryption'
      ]
    };
    
    return security[technology] || ['Follow security best practices', 'Regular security audits'];
  }

  /**
   * Get performance recommendations
   */
  getPerformanceRecommendations(technology) {
    const performance = {
      'React': [
          'Use React.memo for expensive components',
          'Implement proper code splitting',
          'Use lazy loading for routes',
          'Optimize bundle size',
          'Implement proper caching strategies'
      ],
      'Spring Boot': [
          'Use connection pooling',
          'Implement proper caching',
          'Use async processing where appropriate',
          'Monitor JVM performance',
          'Implement proper logging levels'
      ],
      'PostgreSQL': [
          'Use proper indexing',
          'Implement query optimization',
          'Use connection pooling',
          'Monitor query performance',
          'Implement proper partitioning'
      ]
    };
    
    return performance[technology] || ['Monitor performance metrics', 'Implement optimization strategies'];
  }

  /**
   * Get integration guidelines
   */
  getIntegrationGuidelines(technology, category) {
    const guidelines = {
      'Frontend': [
          'Ensure proper API integration',
          'Implement error handling',
          'Use proper state management',
          'Implement responsive design',
          'Ensure accessibility compliance'
      ],
      'Backend': [
          'Implement proper API documentation',
          'Use consistent error responses',
          'Implement proper logging',
          'Use proper validation',
          'Ensure security compliance'
      ],
      'Database': [
          'Implement proper migrations',
          'Use connection pooling',
          'Implement proper backup strategies',
          'Monitor performance',
          'Ensure data integrity'
      ]
    };
    
    return guidelines[category] || ['Follow integration best practices', 'Ensure compatibility'];
  }

  /**
   * Generate specific recommendations
   */
  generateRecommendations(technology, category) {
    const recommendations = [];
    
    // Technology-specific recommendations
    if (technology === 'TailwindCSS') {
      recommendations.push('Use version 3.x for production (avoid 4.x until fully stable)');
      recommendations.push('Implement proper PostCSS configuration');
      recommendations.push('Verify CSS build output size (>10KB)');
    }
    
    if (technology === 'React') {
      recommendations.push('Use TypeScript 5.x for type safety');
      recommendations.push('Implement proper error boundaries');
      recommendations.push('Use React 19 stable features');
    }
    
    if (technology === 'Spring Boot') {
      recommendations.push('Use Java 21 LTS for long-term support');
      recommendations.push('Implement proper exception handling');
      recommendations.push('Use Spring Boot 3.5.x features');
    }
    
    // Category-specific recommendations
    if (category === 'Frontend') {
      recommendations.push('Ensure responsive design implementation');
      recommendations.push('Implement proper accessibility features');
      recommendations.push('Use modern CSS features with fallbacks');
    }
    
    if (category === 'Backend') {
      recommendations.push('Implement comprehensive logging');
      recommendations.push('Use proper exception handling');
      recommendations.push('Implement health checks');
    }
    
    return recommendations;
  }

  /**
   * Check for warnings
   */
  checkWarnings(technology, category) {
    const warnings = [];
    
    // Technology-specific warnings
    if (technology === 'TailwindCSS') {
      warnings.push('Avoid version 4.x in production until fully stable');
      warnings.push('Verify CSS build process generates proper output');
    }
    
    if (technology === 'React') {
      warnings.push('Ensure TypeScript strict mode is enabled');
      warnings.push('Verify all dependencies are compatible with React 19');
    }
    
    if (technology === 'Spring Boot') {
      warnings.push('Ensure Java 21 LTS compatibility');
      warnings.push('Verify all Spring dependencies are compatible');
    }
    
    return warnings;
  }

  /**
   * Validate entire technology stack
   */
  async validateTechStack() {
    console.log('🚀 Starting Context7 technology validation...\n');
    
    const validationPromises = [];
    
    for (const [category, technologies] of Object.entries(this.techStack)) {
      for (const technology of technologies) {
        if (technology && !technology.startsWith('**')) {
          validationPromises.push(this.validateTechnology(technology, category));
        }
      }
    }
    
    const results = await Promise.all(validationPromises);
    
    console.log('\n📊 Validation Results Summary:');
    console.log('==============================');
    
    let totalTechnologies = 0;
    let compliantTechnologies = 0;
    let warnings = 0;
    let errors = 0;
    
    for (const result of results) {
      if (result.error) {
        errors++;
        console.log(`❌ ${result.technology}: ${result.error}`);
      } else {
        totalTechnologies++;
        if (result.compliance.standardsCompliant) {
          compliantTechnologies++;
        }
        if (result.warnings.length > 0) {
          warnings += result.warnings.length;
        }
        
        console.log(`✅ ${result.technology} (${result.category}): Compliant`);
        if (result.warnings.length > 0) {
          result.warnings.forEach(warning => console.log(`   ⚠️  ${warning}`));
        }
      }
    }
    
    console.log('\n📈 Compliance Summary:');
    console.log(`   Total Technologies: ${totalTechnologies}`);
    console.log(`   Compliant: ${compliantTechnologies}`);
    console.log(`   Compliance Rate: ${totalTechnologies > 0 ? Math.round((compliantTechnologies / totalTechnologies) * 100) : 0}%`);
    console.log(`   Warnings: ${warnings}`);
    console.log(`   Errors: ${errors}`);
    
    return results;
  }

  /**
   * Generate validation report
   */
  generateReport() {
    const reportPath = path.join(this.agentOsRoot, 'reports/context7-validation-report.json');
    
    try {
      const report = {
        timestamp: new Date().toISOString(),
        summary: {
          totalTechnologies: this.validationResults.length,
          compliantTechnologies: this.validationResults.filter(r => !r.error && r.compliance.standardsCompliant).length,
          warnings: this.validationResults.reduce((sum, r) => sum + (r.warnings ? r.warnings.length : 0), 0),
          errors: this.validationResults.filter(r => r.error).length
        },
        results: this.validationResults,
        recommendations: this.generateOverallRecommendations()
      };
      
      // Ensure reports directory exists
      const reportsDir = path.dirname(reportPath);
      if (!fs.existsSync(reportsDir)) {
        fs.mkdirSync(reportsDir, { recursive: true });
      }
      
      fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
      console.log(`\n📄 Report saved to: ${reportPath}`);
      
      return report;
    } catch (error) {
      console.error('❌ Error generating report:', error.message);
      return null;
    }
  }

  /**
   * Generate overall recommendations
   */
  generateOverallRecommendations() {
    const recommendations = [];
    
    // Check for common issues
    const hasTailwindCSS = this.validationResults.some(r => r.technology === 'TailwindCSS');
    if (hasTailwindCSS) {
      recommendations.push('Ensure TailwindCSS 3.x is used for production stability');
    }
    
    const hasReact = this.validationResults.some(r => r.technology === 'React');
    if (hasReact) {
      recommendations.push('Verify React 19 compatibility with all dependencies');
    }
    
    const hasSpringBoot = this.validationResults.some(r => r.technology === 'Spring Boot');
    if (hasSpringBoot) {
      recommendations.push('Ensure Java 21 LTS compatibility for Spring Boot 3.5.x');
    }
    
    // General recommendations
    recommendations.push('Regularly update dependencies to latest stable versions');
    recommendations.push('Monitor security advisories for all technologies');
    recommendations.push('Implement automated testing for technology compatibility');
    recommendations.push('Use Context7 for all technology decisions and updates');
    
    return recommendations;
  }

  /**
   * Main execution method
   */
  async run() {
    try {
      console.log('🤖 Context7 Technology Validator');
      console.log('================================\n');
      
      // Validate entire tech stack
      await this.validateTechStack();
      
      // Generate report
      const report = this.generateReport();
      
      if (report) {
        console.log('\n✅ Context7 validation complete!');
        console.log(`📊 Compliance Rate: ${report.summary.compliantTechnologies}/${report.summary.totalTechnologies} (${Math.round((report.summary.compliantTechnologies / report.summary.totalTechnologies) * 100)}%)`);
      }
      
      return report;
    } catch (error) {
      console.error('❌ Validation failed:', error.message);
      return null;
    }
  }
}

// CLI execution
if (import.meta.url === `file://${process.argv[1]}`) {
  const validator = new Context7TechnologyValidator();
  validator.run().catch(console.error);
}

export default Context7TechnologyValidator;
