#!/usr/bin/env node

/**
 * Development Workflow Updater with Context7 Integration
 * Automatically integrates Context7 validation into development workflow
 * 
 * Usage: node .agent-os/tools/development-workflow-updater.js
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class DevelopmentWorkflowUpdater {
  constructor() {
    this.agentOsRoot = path.join(__dirname, '..');
    this.projectRoot = path.join(__dirname, '../../..');
    this.workflowFiles = [
      'standards/enforcement.md',
      'standards/best-practices.md',
      'standards/development-patterns.md',
      'templates/development-workflow-template.md'
    ];
    this.cursorRulesFiles = [
      '.cursorrules',
      '.cursor/rules/README.md',
      '.cursor/rules/context7-integration.mdc'
    ];
  }

  /**
   * Update enforcement standards with Context7 integration
   */
  updateEnforcementStandards() {
    const enforcementPath = path.join(this.agentOsRoot, 'standards/enforcement.md');
    
    try {
      let content = fs.readFileSync(enforcementPath, 'utf8');
      
      // Add Context7 validation to mandatory rules
      if (!content.includes('Context7 Technology Validation')) {
        const context7Rule = `
## Context7 Technology Validation (MANDATORY)

### Pre-Development Context7 Validation
**ALWAYS** validate technology choices against Context7 before implementation:
1. **Technology Selection**: Use Context7 to verify current versions and best practices
2. **Compatibility Check**: Ensure all components work together via Context7
3. **Best Practices**: Follow official Context7 recommendations
4. **Security Validation**: Check Context7 for security considerations
5. **Performance Guidelines**: Apply Context7 performance recommendations

### During Development Context7 Validation
**ALWAYS** reference Context7 during development:
1. **API Usage**: Check Context7 for current API patterns
2. **Code Examples**: Use Context7 code examples as reference
3. **Version Features**: Verify feature availability in current versions
4. **Integration Patterns**: Follow Context7 integration guidelines
5. **Error Handling**: Apply Context7 error handling patterns

### Post-Development Context7 Validation
**ALWAYS** validate implementation against Context7:
1. **Best Practices Compliance**: Ensure implementation follows Context7 guidelines
2. **Security Compliance**: Verify security measures align with Context7
3. **Performance Compliance**: Check performance against Context7 benchmarks
4. **Documentation Compliance**: Ensure documentation matches Context7 standards
`;

        // Insert after the first ## section
        const sections = content.split('## ');
        if (sections.length > 1) {
          sections.splice(1, 0, context7Rule);
          content = sections.join('## ');
        }
      }
      
      fs.writeFileSync(enforcementPath, content);
      console.log('✅ Updated enforcement standards with Context7 integration');
      
    } catch (error) {
      console.error('❌ Error updating enforcement standards:', error.message);
    }
  }

  /**
   * Update best practices with Context7 integration
   */
  updateBestPractices() {
    const bestPracticesPath = path.join(this.agentOsRoot, 'standards/best-practices.md');
    
    try {
      let content = fs.readFileSync(bestPracticesPath, 'utf8');
      
      // Add Context7 best practices section
      if (!content.includes('Context7 Best Practices')) {
        const context7BestPractices = `
## Context7 Best Practices

### Technology Selection Best Practices
1. **Always Check Context7 First**: Use Context7 as primary source for technology decisions
2. **Version Compatibility**: Verify version compatibility via Context7
3. **Security Considerations**: Check Context7 for security best practices
4. **Performance Guidelines**: Apply Context7 performance recommendations
5. **Integration Patterns**: Follow Context7 integration guidelines

### Development Best Practices
1. **API Reference**: Always check Context7 for current API documentation
2. **Code Examples**: Use Context7 code examples as implementation reference
3. **Error Handling**: Follow Context7 error handling patterns
4. **Testing Strategies**: Apply Context7 testing recommendations
5. **Documentation**: Align documentation with Context7 standards

### Maintenance Best Practices
1. **Regular Updates**: Check Context7 for regular updates and security patches
2. **Migration Planning**: Use Context7 for migration planning and strategies
3. **Deprecation Monitoring**: Monitor Context7 for deprecation notices
4. **Performance Monitoring**: Use Context7 benchmarks for performance monitoring
5. **Security Monitoring**: Monitor Context7 for security advisories
`;

        // Insert at the end of the file
        content += context7BestPractices;
      }
      
      fs.writeFileSync(bestPracticesPath, content);
      console.log('✅ Updated best practices with Context7 integration');
      
    } catch (error) {
      console.error('❌ Error updating best practices:', error.message);
    }
  }

  /**
   * Update development patterns with Context7 integration
   */
  updateDevelopmentPatterns() {
    const patternsPath = path.join(this.agentOsRoot, 'standards/development-patterns.md');
    
    try {
      let content = fs.readFileSync(patternsPath, 'utf8');
      
      // Add Context7 development patterns
      if (!content.includes('Context7 Development Patterns')) {
        const context7Patterns = `
## Context7 Development Patterns

### Technology Validation Pattern
\`\`\`javascript
// Before implementing any technology, validate against Context7
async function validateTechnologyChoice(technology, version) {
  // 1. Check Context7 for current version
  const context7Info = await getContext7Info(technology);
  
  // 2. Validate version compatibility
  if (!isVersionCompatible(version, context7Info.recommendedVersion)) {
    throw new Error('Version ' + version + ' not compatible with Context7 recommendations');
  }
  
  // 3. Apply Context7 best practices
  return applyContext7BestPractices(technology, context7Info);
}
\`\`\`

### Best Practices Integration Pattern
\`\`\`javascript
// Integrate Context7 best practices into development
function applyContext7BestPractices(technology, context7Info) {
  return {
    technology,
    version: context7Info.recommendedVersion,
    bestPractices: context7Info.bestPractices,
    securityConsiderations: context7Info.securityConsiderations,
    performanceGuidelines: context7Info.performanceGuidelines,
    integrationPatterns: context7Info.integrationPatterns
  };
}
\`\`\`

### Continuous Validation Pattern
\`\`\`javascript
// Continuously validate against Context7 during development
function setupContext7Validation() {
  // Set up real-time validation
  watchForTechnologyChanges((change) => {
    validateAgainstContext7(change.technology, change.version);
  });
  
  // Set up periodic validation
  setInterval(() => {
    validateAllTechnologiesAgainstContext7();
  }, 24 * 60 * 60 * 1000); // Daily validation
}
\`\`\`
`;

        // Insert at the end of the file
        content += context7Patterns;
      }
      
      fs.writeFileSync(patternsPath, content);
      console.log('✅ Updated development patterns with Context7 integration');
      
    } catch (error) {
      console.error('❌ Error updating development patterns:', error.message);
    }
  }

  /**
   * Create development workflow template with Context7 integration
   */
  createDevelopmentWorkflowTemplate() {
    const templatePath = path.join(this.agentOsRoot, 'templates/development-workflow-template.md');
    
    try {
      const template = `# Development Workflow Template with Context7 Integration

## Pre-Development Phase

### 1. Technology Selection and Validation
- [ ] **Context7 Technology Validation** (MANDATORY)
  - [ ] Check Context7 for current technology versions
  - [ ] Validate version compatibility
  - [ ] Review Context7 best practices
  - [ ] Check security considerations
  - [ ] Review performance guidelines

### 2. Standards Compliance Check
- [ ] Run compliance checker: \`node .agent-os/tools/compliance-checker.js\`
- [ ] Run Context7 technology validator: \`node .agent-os/tools/context7-technology-validator.js\`
- [ ] Ensure compliance score ≥85%
- [ ] Address any critical violations

### 3. Project Setup
- [ ] Load relevant specifications and standards
- [ ] Review Context7 integration guidelines
- [ ] Set up development environment
- [ ] Configure Context7 validation tools

## Development Phase

### 1. Daily Development Workflow
- [ ] **Start of Day**
  - [ ] Run compliance check: \`node .agent-os/tools/compliance-checker.js\`
  - [ ] Check Context7 for any updates: \`node .agent-os/tools/context7-technology-validator.js\`
  - [ ] Review any new Context7 recommendations

- [ ] **During Development**
  - [ ] Reference Context7 for API usage and patterns
  - [ ] Apply Context7 best practices
  - [ ] Follow Context7 integration guidelines
  - [ ] Use Context7 code examples as reference

- [ ] **End of Day**
  - [ ] Update tasks.md with progress
  - [ ] Run final compliance check
  - [ ] Document any Context7 insights learned

### 2. Technology Implementation
- [ ] **Before Adding New Technology**
  - [ ] Validate against Context7: \`node .agent-os/tools/context7-technology-validator.js [technology]\`
  - [ ] Check version compatibility
  - [ ] Review security considerations
  - [ ] Apply Context7 best practices

- [ ] **During Implementation**
  - [ ] Follow Context7 code patterns
  - [ ] Implement Context7 security guidelines
  - [ ] Apply Context7 performance recommendations
  - [ ] Use Context7 integration patterns

### 3. Code Quality Assurance
- [ ] **Static Analysis**
  - [ ] Run linting and formatting
  - [ ] Check for security vulnerabilities
  - [ ] Validate architecture patterns
  - [ ] Ensure Context7 compliance

- [ ] **Testing**
  - [ ] Unit tests with ≥85% coverage
  - [ ] Integration tests for critical paths
  - [ ] Security testing for new features
  - [ ] Performance testing against Context7 benchmarks

## Post-Development Phase

### 1. Final Validation
- [ ] **Compliance Check**
  - [ ] Run full compliance check
  - [ ] Run Context7 technology validation
  - [ ] Ensure all standards are met
  - [ ] Address any remaining violations

### 2. Documentation Update
- [ ] **Update Project Documentation**
  - [ ] Document Context7 insights learned
  - [ ] Update technology stack documentation
  - [ ] Document best practices applied
  - [ ] Update lessons learned

### 3. Knowledge Sharing
- [ ] **Share Context7 Insights**
  - [ ] Document new Context7 patterns discovered
  - [ ] Share Context7 best practices with team
  - [ ] Update team standards based on Context7 findings
  - [ ] Contribute to Context7 knowledge base

## Context7 Integration Commands

### Technology Validation
\`\`\`bash
# Validate specific technology
node .agent-os/tools/context7-technology-validator.js [technology-name]

# Validate entire tech stack
node .agent-os/tools/context7-technology-validator.js

# Check Context7 compliance status
node .agent-os/tools/compliance-checker.js --context7-status
\`\`\`

### Development Workflow
\`\`\`bash
# Start development session
node .agent-os/scripts/setup.js validate

# Real-time monitoring
node .agent-os/tools/real-time-monitor.js

# Compliance checking
node .agent-os/tools/compliance-checker.js --detailed
\`\`\`

## Quality Gates

### Context7 Compliance Gates
- [ ] **Technology Validation Gate**: All technologies validated against Context7
- [ ] **Best Practices Gate**: All Context7 best practices implemented
- [ ] **Security Compliance Gate**: All Context7 security guidelines followed
- [ ] **Performance Compliance Gate**: All Context7 performance recommendations applied
- [ ] **Integration Compliance Gate**: All Context7 integration patterns followed

### Overall Compliance Gates
- [ ] **Compliance Score**: ≥85%
- [ ] **Critical Violations**: 0
- [ ] **Security Violations**: 0
- [ ] **Test Coverage**: ≥85%
- [ ] **Performance Targets**: Met

## Lessons Learned Integration

### Context7 Lessons
- [ ] **Technology Insights**: Document Context7 technology insights
- [ ] **Best Practice Discoveries**: Document new best practices found
- [ ] **Integration Patterns**: Document successful integration patterns
- [ ] **Performance Optimizations**: Document performance improvements
- [ ] **Security Enhancements**: Document security improvements

### Continuous Improvement
- [ ] **Update Standards**: Incorporate Context7 insights into standards
- [ ] **Improve Processes**: Enhance workflows based on Context7 findings
- [ ] **Share Knowledge**: Share Context7 insights with team
- [ ] **Optimize Tools**: Improve Context7 integration tools
- [ ] **Expand Coverage**: Extend Context7 validation coverage

---

**Remember**: Context7 is the primary source for all technology decisions, best practices, and implementation guidance. Always validate against Context7 before making technology choices or implementing new features.
`;

      // Ensure templates directory exists
      const templatesDir = path.dirname(templatePath);
      if (!fs.existsSync(templatesDir)) {
        fs.mkdirSync(templatesDir, { recursive: true });
      }
      
      fs.writeFileSync(templatePath, template);
      console.log('✅ Created development workflow template with Context7 integration');
      
    } catch (error) {
      console.error('❌ Error creating workflow template:', error.message);
    }
  }

  /**
   * Update cursor rules with enhanced Context7 integration
   */
  updateCursorRules() {
    // Update main .cursorrules
    const cursorRulesPath = path.join(this.projectRoot, '.cursorrules');
    
    try {
      if (fs.existsSync(cursorRulesPath)) {
        let content = fs.readFileSync(cursorRulesPath, 'utf8');
        
        // Add enhanced Context7 integration section
        if (!content.includes('Enhanced Context7 Integration')) {
          const enhancedContext7Section = `

## Enhanced Context7 Integration

### Automated Context7 Validation
**ALWAYS** use automated Context7 validation tools:
- **Technology Validator**: \`node .agent-os/tools/context7-technology-validator.js\`
- **Compliance Checker**: \`node .agent-os/tools/compliance-checker.js --context7\`
- **Real-time Monitor**: \`node .agent-os/tools/real-time-monitor.js\`

### Context7 Development Workflow
**ALWAYS** follow Context7-integrated development workflow:
1. **Pre-Development**: Validate technology choices against Context7
2. **During Development**: Reference Context7 for best practices
3. **Post-Development**: Validate implementation against Context7
4. **Continuous**: Monitor Context7 for updates and improvements

### Context7 Quality Gates
**ALWAYS** pass Context7 quality gates:
- Technology validation against Context7
- Best practices compliance with Context7
- Security guidelines from Context7
- Performance recommendations from Context7
- Integration patterns from Context7
`;

          // Insert before the last section
          const sections = content.split('\n## ');
          if (sections.length > 1) {
            sections.splice(sections.length - 1, 0, enhancedContext7Section);
            content = sections.join('\n## ');
          }
        }
        
        fs.writeFileSync(cursorRulesPath, content);
        console.log('✅ Updated main .cursorrules with enhanced Context7 integration');
      }
    } catch (error) {
      console.error('❌ Error updating main .cursorrules:', error.message);
    }
  }

  /**
   * Create Context7 integration status checker
   */
  createContext7StatusChecker() {
    const statusCheckerPath = path.join(this.agentOsRoot, 'tools/context7-status-checker.js');
    
    try {
      const statusChecker = `#!/usr/bin/env node

/**
 * Context7 Integration Status Checker
 * Checks the status of Context7 integration in the project
 * 
 * Usage: node .agent-os/tools/context7-status-checker.js
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

class Context7StatusChecker {
  constructor() {
    this.agentOsRoot = path.join(__dirname, '..');
    this.projectRoot = path.join(__dirname, '../../..');
    this.integrationStatus = {};
  }

  /**
   * Check Context7 integration status
   */
  async checkIntegrationStatus() {
    console.log('🔍 Checking Context7 Integration Status...\n');
    
    // Check cursor rules integration
    this.checkCursorRulesIntegration();
    
    // Check standards integration
    this.checkStandardsIntegration();
    
    // Check tools integration
    this.checkToolsIntegration();
    
    // Check workflow integration
    this.checkWorkflowIntegration();
    
    // Generate status report
    this.generateStatusReport();
  }

  /**
   * Check cursor rules integration
   */
  checkCursorRulesIntegration() {
    console.log('📋 Checking Cursor Rules Integration...');
    
    const cursorRulesPath = path.join(this.projectRoot, '.cursorrules');
    const context7RulesPath = path.join(this.projectRoot, '.cursor/rules/context7-integration.mdc');
    
    if (fs.existsSync(cursorRulesPath)) {
      const content = fs.readFileSync(cursorRulesPath, 'utf8');
      this.integrationStatus.cursorRules = {
        exists: true,
        context7Referenced: content.includes('Context7'),
        context7Priority: content.includes('Context7 Priority'),
        context7Validation: content.includes('Context7 Technology Validation'),
        context7Compliance: content.includes('Context7 Compliance')
      };
      
      console.log('   ✅ Main .cursorrules: Found');
      console.log('   ✅ Context7 referenced: ' + (this.integrationStatus.cursorRules.context7Referenced ? 'Yes' : 'No'));
      console.log('   ✅ Context7 priority: ' + (this.integrationStatus.cursorRules.context7Priority ? 'Yes' : 'No'));
      console.log('   ✅ Context7 validation: ' + (this.integrationStatus.cursorRules.context7Validation ? 'Yes' : 'No'));
      console.log('   ✅ Context7 compliance: ' + (this.integrationStatus.cursorRules.context7Compliance ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.cursorRules = { exists: false };
      console.log('   ❌ Main .cursorrules: Not found');
    }
    
    if (fs.existsSync(context7RulesPath)) {
      const content = fs.readFileSync(context7RulesPath, 'utf8');
      this.integrationStatus.context7Rules = {
        exists: true,
        priorityHierarchy: content.includes('Context7 Priority Hierarchy'),
        mcpTools: content.includes('MCP Tools'),
        technologyValidation: content.includes('Technology Validation'),
        complianceRequirements: content.includes('Compliance Requirements')
      };
      
      console.log('   ✅ Context7 rules file: Found');
      console.log('   ✅ Priority hierarchy: ' + (this.integrationStatus.context7Rules.priorityHierarchy ? 'Yes' : 'No'));
      console.log('   ✅ MCP tools: ' + (this.integrationStatus.context7Rules.mcpTools ? 'Yes' : 'No'));
      console.log('   ✅ Technology validation: ' + (this.integrationStatus.context7Rules.technologyValidation ? 'Yes' : 'No'));
      console.log('   ✅ Compliance requirements: ' + (this.integrationStatus.context7Rules.complianceRequirements ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.context7Rules = { exists: false };
      console.log('   ❌ Context7 rules file: Not found');
    }
  }

  /**
   * Check standards integration
   */
  checkStandardsIntegration() {
    console.log('\n📚 Checking Standards Integration...');
    
    const standardsPath = path.join(this.agentOsRoot, 'standards');
    const techStackPath = path.join(standardsPath, 'tech-stack.md');
    const enforcementPath = path.join(standardsPath, 'enforcement.md');
    const bestPracticesPath = path.join(standardsPath, 'best-practices.md');
    
    if (fs.existsSync(techStackPath)) {
      const content = fs.readFileSync(techStackPath, 'utf8');
      this.integrationStatus.techStack = {
        exists: true,
        context7Priority: content.includes('Context7 Priority'),
        context7Validation: content.includes('Context7 validation'),
        context7Integration: content.includes('Context7 Integration')
      };
      
      console.log('   ✅ Tech stack standards: Found');
      console.log('   ✅ Context7 priority: ' + (this.integrationStatus.techStack.context7Priority ? 'Yes' : 'No'));
      console.log('   ✅ Context7 validation: ' + (this.integrationStatus.techStack.context7Validation ? 'Yes' : 'No'));
      console.log('   ✅ Context7 integration: ' + (this.integrationStatus.techStack.context7Integration ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.techStack = { exists: false };
      console.log('   ❌ Tech stack standards: Not found');
    }
    
    if (fs.existsSync(enforcementPath)) {
      const content = fs.readFileSync(enforcementPath, 'utf8');
      this.integrationStatus.enforcement = {
        exists: true,
        context7Validation: content.includes('Context7 Technology Validation'),
        context7Compliance: content.includes('Context7 compliance')
      };
      
      console.log('   ✅ Enforcement standards: Found');
      console.log('   ✅ Context7 validation: ' + (this.integrationStatus.enforcement.context7Validation ? 'Yes' : 'No'));
      console.log('   ✅ Context7 compliance: ' + (this.integrationStatus.enforcement.context7Compliance ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.enforcement = { exists: false };
      console.log('   ❌ Enforcement standards: Not found');
    }
  }

  /**
   * Check tools integration
   */
  checkToolsIntegration() {
    console.log('\n🛠️  Checking Tools Integration...');
    
    const toolsPath = path.join(this.agentOsRoot, 'tools');
    const context7ValidatorPath = path.join(toolsPath, 'context7-technology-validator.js');
    const complianceCheckerPath = path.join(toolsPath, 'compliance-checker.js');
    
    if (fs.existsSync(context7ValidatorPath)) {
      this.integrationStatus.context7Validator = { exists: true };
      console.log('   ✅ Context7 technology validator: Found');
    } else {
      this.integrationStatus.context7Validator = { exists: false };
      console.log('   ❌ Context7 technology validator: Not found');
    }
    
    if (fs.existsSync(complianceCheckerPath)) {
      const content = fs.readFileSync(complianceCheckerPath, 'utf8');
      this.integrationStatus.complianceChecker = {
        exists: true,
        context7Integration: content.includes('Context7')
      };
      
      console.log('   ✅ Compliance checker: Found');
      console.log('   ✅ Context7 integration: ' + (this.integrationStatus.complianceChecker.context7Integration ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.complianceChecker = { exists: false };
      console.log('   ❌ Compliance checker: Not found');
    }
  }

  /**
   * Check workflow integration
   */
  checkWorkflowIntegration() {
    console.log('\n🔄 Checking Workflow Integration...');
    
    const templatesPath = path.join(this.agentOsRoot, 'templates');
    const workflowTemplatePath = path.join(templatesPath, 'development-workflow-template.md');
    
    if (fs.existsSync(workflowTemplatePath)) {
      const content = fs.readFileSync(workflowTemplatePath, 'utf8');
      this.integrationStatus.workflowTemplate = {
        exists: true,
        context7Integration: content.includes('Context7 Integration'),
        context7Workflow: content.includes('Context7 Development Workflow'),
        context7QualityGates: content.includes('Context7 Quality Gates')
      };
      
      console.log('   ✅ Workflow template: Found');
      console.log('   ✅ Context7 integration: ' + (this.integrationStatus.workflowTemplate.context7Integration ? 'Yes' : 'No'));
      console.log('   ✅ Context7 workflow: ' + (this.integrationStatus.workflowTemplate.context7Workflow ? 'Yes' : 'No'));
      console.log('   ✅ Context7 quality gates: ' + (this.integrationStatus.workflowTemplate.context7QualityGates ? 'Yes' : 'No'));
    } else {
      this.integrationStatus.workflowTemplate = { exists: false };
      console.log('   ❌ Workflow template: Not found');
    }
  }

  /**
   * Generate status report
   */
  generateStatusReport() {
    console.log('\n📊 Context7 Integration Status Report');
    console.log('=====================================');
    
    let totalChecks = 0;
    let passedChecks = 0;
    
    // Calculate overall status
    for (const category of Object.values(this.integrationStatus)) {
      for (const check of Object.values(category)) {
        if (typeof check === 'boolean') {
          totalChecks++;
          if (check) passedChecks++;
        }
      }
    }
    
    const integrationScore = totalChecks > 0 ? Math.round((passedChecks / totalChecks) * 100) : 0;
    
    console.log(`Integration Score: ${passedChecks}/${totalChecks} (${integrationScore}%)`);
    
    if (integrationScore >= 90) {
      console.log('🎉 Excellent Context7 integration!');
    } else if (integrationScore >= 75) {
      console.log('✅ Good Context7 integration with room for improvement');
    } else if (integrationScore >= 50) {
      console.log('⚠️  Fair Context7 integration, needs attention');
    } else {
      console.log('❌ Poor Context7 integration, requires immediate action');
    }
    
    // Save detailed report
    const reportPath = path.join(this.agentOsRoot, 'reports/context7-integration-status.json');
    const report = {
      timestamp: new Date().toISOString(),
      integrationScore,
      totalChecks,
      passedChecks,
      details: this.integrationStatus
    };
    
    try {
      const reportsDir = path.dirname(reportPath);
      if (!fs.existsSync(reportsDir)) {
        fs.mkdirSync(reportsDir, { recursive: true });
      }
      
      fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
      console.log(`\n📄 Detailed report saved to: ${reportPath}`);
    } catch (error) {
      console.error('❌ Error saving report:', error.message);
    }
  }

  /**
   * Main execution method
   */
  async run() {
    try {
      console.log('🤖 Context7 Integration Status Checker');
      console.log('=====================================\n');
      
      await this.checkIntegrationStatus();
      
      return this.integrationStatus;
    } catch (error) {
      console.error('❌ Status check failed:', error.message);
      return null;
    }
  }
}

// CLI execution
if (import.meta.url === `file://${process.argv[1]}`) {
  const checker = new Context7StatusChecker();
  checker.run().catch(console.error);
}

export default Context7StatusChecker;
`;

      fs.writeFileSync(statusCheckerPath, statusChecker);
      console.log('✅ Created Context7 status checker');
    } catch (error) {
      console.error('❌ Error creating status checker:', error.message);
    }
  }

  /**
   * Main execution method
   */
  async run() {
    try {
      console.log('🤖 Development Workflow Updater with Context7 Integration');
      console.log('========================================================\n');
      
      // Update all workflow components
      this.updateEnforcementStandards();
      this.updateBestPractices();
      this.updateDevelopmentPatterns();
      this.createDevelopmentWorkflowTemplate();
      this.updateCursorRules();
      this.createContext7StatusChecker();
      
      console.log('\n✅ Development workflow update complete!');
      console.log('\n🚀 Next steps:');
      console.log('   1. Check Context7 integration status: node .agent-os/tools/context7-status-checker.js');
      console.log('   2. Validate technology stack: node .agent-os/tools/context7-technology-validator.js');
      console.log('   3. Run compliance check: node .agent-os/tools/compliance-checker.js --detailed');
      
      return true;
    } catch (error) {
      console.error('❌ Workflow update failed:', error.message);
      return false;
    }
  }
}

// CLI execution
if (import.meta.url === `file://${process.argv[1]}`) {
  const updater = new DevelopmentWorkflowUpdater();
  updater.run().catch(console.error);
}

export default DevelopmentWorkflowUpdater;
