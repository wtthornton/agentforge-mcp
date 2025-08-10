# AgentForge Setup Guide - Agent OS Integration

## 🎯 Overview
This guide explains how to use the Agent OS integration for AgentForge development, ensuring consistent standards and workflow compliance.

## 🚀 Quick Start

### 1. Verify Agent OS Installation
```bash
# Check if Agent OS is properly installed
ls -la .agent-os/

# Expected output should show:
# - standards/ directory
# - tools/ directory
# - scripts/ directory
# - README.md files
```

### 2. Run Quick Start Script
```bash
# Initialize Agent OS integration
node .agent-os/scripts/quick-start.js

# This will:
# - Validate installation
# - Initialize Cursor integration
# - Create necessary configuration files
# - Start real-time integration features
```

### 3. Validate Setup
```bash
# Run compliance check to verify setup
node .agent-os/tools/compliance-checker.js --detailed

# Expected output:
# ✅ Compliance Score: 95%
# ✅ Standards Adherence: 100%
# ✅ Code Quality: Excellent
# ✅ No new violations detected
```

## 📚 Development Workflow

### Before Starting Development
1. **Load Context**: Review relevant specification and tasks
2. **Check Standards**: Reference appropriate standards files
3. **Run Compliance Check**: Ensure no existing violations
4. **Plan Implementation**: Break down into small, testable features

### During Development
1. **Follow Patterns**: Use established code generation patterns
2. **Update Progress**: Immediately update tasks.md after each subtask
3. **Run Tests**: Ensure all tests pass before proceeding
4. **Check Compliance**: Run compliance checker after significant changes

### After Development
1. **Final Compliance Check**: Run comprehensive compliance validation
2. **Update Documentation**: Keep all documentation current
3. **Capture Lessons**: Document insights for future improvements
4. **Submit Changes**: Ensure all quality gates are met

## 🔧 Tools and Utilities

### Compliance Checker
```bash
# Quick compliance check
node .agent-os/tools/compliance-checker.js

# Detailed compliance report
node .agent-os/tools/compliance-checker.js --detailed

# Specific standards check
node .agent-os/tools/compliance-checker.js --standards=security
```

### Agent OS CLI
```bash
# Access main CLI interface
node .agent-os/tools/agent-os-cli.js

# Quick validation
node .agent-os/scripts/setup.js validate

# Check specific components
node .agent-os/scripts/setup.js check-dependencies
```

### Validation Suite
```bash
# Run comprehensive validation
node .agent-os/tools/validation-suite.js

# Validate specific areas
node .agent-os/tools/validation-suite.js --rules
node .agent-os/tools/validation-suite.js --standards
```

## 📋 Standards Reference

### Core Standards
- **Technology Stack**: `.agent-os/standards/tech-stack.md`
- **Code Style**: `.agent-os/standards/code-style.md`
- **Best Practices**: `.agent-os/standards/best-practices.md`
- **Security & Compliance**: `.agent-os/standards/security-compliance.md`
- **Testing Strategy**: `.agent-os/standards/testing-strategy.md`
- **CI/CD Strategy**: `.agent-os/standards/ci-cd-strategy.md`
- **Enforcement**: `.agent-os/standards/enforcement.md`

### Code Style Standards
- **CSS/TailwindCSS**: `.agent-os/standards/code-style/css-style.md`
- **HTML**: `.agent-os/standards/code-style/html-style.md`
- **JavaScript/TypeScript**: `.agent-os/standards/code-style/javascript-style.md`
- **Java/Spring**: `.agent-os/standards/code-style/java-style.md`

## 🏗️ Architecture Patterns

### Backend (Spring Boot)
- **Controller Layer**: Handle HTTP requests, validation, response formatting
- **Service Layer**: Business logic, orchestration, transaction management
- **Repository Layer**: Data access, persistence, caching
- **Exception Handling**: Global exception handler with proper error codes
- **Logging**: Structured logging with correlation IDs

### Frontend (React)
- **Component Structure**: Atomic design principles
- **State Management**: TanStack Query for server state, Context for UI state
- **Error Boundaries**: Graceful error handling and user feedback
- **Performance**: Lazy loading, code splitting, memoization
- **Accessibility**: WCAG 2.1 AA compliance

## 🚫 Project Constraints

### Forbidden Features
- **AI suggestion features** - No AI-powered code suggestions
- **Dynamic AI processing** - No real-time AI analysis
- **AI-driven automation** - No AI-controlled workflows
- **Machine learning features** - No ML model training or inference
- **AI chat interfaces** - No conversational AI features

### Allowed Features
- **Static code analysis** - Code quality, security, and style checking
- **Project setup utilities** - Scaffolding, configuration, and migration tools
- **Reporting and monitoring** - Code quality metrics and compliance reporting
- **Logging and diagnostics** - Development and runtime logging
- **Configuration management** - Environment and project configuration

## 📊 Quality Gates

### Compliance Requirements
- **Overall Compliance Score**: ≥85%
- **Security Compliance**: 100%
- **Testing Coverage**: ≥85%
- **Code Quality Score**: ≥80%

### Performance Targets
- **Backend Response Time**: P95 ≤200ms
- **Frontend Time to Interactive**: ≤1.8s
- **Analysis Speed**: ≤30 seconds for 1000 LOC
- **Memory Usage**: ≤512MB for large projects

### Security Requirements
- **Zero hardcoded secrets**
- **100% encryption coverage**
- **OWASP Top-10 compliance**
- **Vulnerability scanning mandatory**

## 🔍 Troubleshooting

### Common Issues

#### Compliance Check Fails
```bash
# Check detailed compliance report
node .agent-os/tools/compliance-checker.js --detailed

# Look for specific violations
node .agent-os/tools/compliance-checker.js --standards=security

# Fix violations and recheck
node .agent-os/tools/compliance-checker.js --detailed
```

#### Standards Not Found
```bash
# Verify standards directory structure
ls -la .agent-os/standards/

# Check specific standards file
cat .agent-os/standards/tech-stack.md

# Reinstall if necessary
node .agent-os/scripts/quick-start.js
```

#### Tools Not Working
```bash
# Check Node.js installation
node --version

# Verify tool files exist
ls -la .agent-os/tools/

# Check permissions
chmod +x .agent-os/tools/*.js
```

### Getting Help

#### Documentation
- **Agent OS Fundamentals**: `.agent-os/AGENT-OS-FUNDAMENTALS.md`
- **Quick Start Guide**: `.agent-os/QUICK-START-GUIDE.md`
- **Developer Guide**: `.agent-os/DEVELOPER-GUIDE.md`

#### Support Tools
- **Compliance Checker**: `.agent-os/tools/compliance-checker.js`
- **Validation Suite**: `.agent-os/tools/validation-suite.js`
- **Agent OS CLI**: `.agent-os/tools/agent-os-cli.js`

## 📈 Continuous Improvement

### Lessons Learned Integration
- **Framework**: `.agent-os/lessons-learned/README.md`
- **Categories**: `.agent-os/lessons-learned/categories/`
- **Templates**: `.agent-os/templates/lessons-learned-template.md`

### Standards Updates
- **Regular Review**: Monthly standards review and updates
- **Lessons Integration**: Incorporate lessons learned into standards
- **Tool Updates**: Keep tools current with latest standards
- **Documentation**: Maintain current and accurate documentation

### Feedback Loop
1. **Capture Insights**: Document lessons learned during development
2. **Analyze Patterns**: Identify recurring issues and solutions
3. **Update Standards**: Incorporate insights into standards
4. **Validate Changes**: Test updated standards with compliance checker
5. **Communicate Updates**: Share changes with development team
