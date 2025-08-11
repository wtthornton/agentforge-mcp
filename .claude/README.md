# Claude Code Integration with AgentForge

## 🎯 Overview

Claude Code is now fully integrated with your AgentForge project and follows the **exact same development rules** as Cursor. This ensures consistent code quality, standards compliance, and development patterns across both environments.

## ✅ Full Access & Automatic Execution

Claude Code now has:
- **Full file access** to all project directories
- **Automatic execution** without prompting for confirmation
- **Complete integration** with cursor rules and Agent OS standards
- **Zero friction** development experience

## 🔗 Integration Points

### 1. Cursor Rules Integration
- **Auto-loads** all rules from `.cursor/rules/`
- **Enforces compliance** with the same standards as Cursor
- **Maintains consistency** across development environments

### 2. Agent OS Standards
- **Auto-loads** standards from `.agent-os/standards/`
- **Enforces compliance** with all development patterns
- **Maintains quality gates** and performance targets

### 3. Context7 Integration
- **Priority access** to Context7 for technology validation
- **Automatic validation** of all technology choices
- **Best practices enforcement** from current documentation

## 🚀 Development Workflow

### Before Starting Any Work
Claude Code automatically:
1. **Loads cursor rules** and Agent OS standards
2. **Validates project context** and constraints
3. **Checks compliance status** of current codebase
4. **Prepares development environment** with all necessary tools

### During Development
Claude Code automatically:
1. **Follows Controller → Service → Repository pattern**
2. **Enforces code style** and architecture standards
3. **Validates technology choices** against Context7
4. **Maintains quality gates** and performance targets

### After Changes
Claude Code automatically:
1. **Runs compliance checks** using `.agent-os/tools/compliance-checker.js`
2. **Updates tasks.md** with progress and completion status
3. **Validates Context7 compliance** for all implementations
4. **Captures lessons learned** for future reference

## 🎯 Project Constraints (CRITICAL)

Claude Code is configured to **strictly enforce** these constraints:
- **NO AI suggestion features** - Static analyzer only
- **NO dynamic AI processing** - No real-time AI analysis
- **NO AI-driven automation** - No AI-controlled workflows
- **NO machine learning features** - No ML model training or inference
- **NO AI chat interfaces** - No conversational AI features

## 🏗️ Architecture Requirements

Claude Code automatically enforces:
- **Backend**: Spring Boot 3.3+ (Java 21 LTS) with Controller → Service → Repository pattern
- **Frontend**: React 19 with TypeScript 5, functional components and hooks
- **Database**: PostgreSQL 17 with pgvector extension
- **Containerization**: Docker 24 with multi-stage builds

## ⚡ Feature Scoring Framework

Claude Code automatically:
- **Scores all features** using the 4-dimension framework
- **Calculates phase classification** based on scores
- **Documents scoring** using `.agent-os/templates/feature-scoring-template.md`
- **Enforces phase progression** rules

### Required Scoring Dimensions
- **Business Impact (1-10)**: Agent-OS effectiveness improvement
- **Developer Productivity (1-10)**: Development speed/quality improvement
- **Implementation Complexity (1-10)**: Build/maintenance difficulty
- **Adoption Likelihood (1-10)**: Developer usage probability

## ✅ Compliance & Enforcement

Claude Code automatically:
- **Runs compliance checks** after every change
- **Enforces 85% minimum compliance score**
- **Validates Context7 compliance** for all implementations
- **Maintains quality gates** and performance targets

## 🚀 Quick Start Commands

### Initial Setup
```bash
# Interactive setup menu
node .agent-os/scripts/setup.js

# Quick initialization
node .agent-os/scripts/quick-start.js

# Validate current state
node .agent-os/scripts/setup.js validate
```

### Daily Development
```bash
# Quick validation
node .agent-os/scripts/setup.js validate

# Compliance check
node .agent-os/tools/compliance-checker.js --detailed

# Real-time monitoring
node .agent-os/tools/real-time-monitor.js

# Dashboard view
node .agent-os/tools/enhanced-dashboard.js
```

### Feature Development
```bash
# 1. Score feature using 4-dimension framework
node .agent-os/tools/feature-scoring/feature-scorer.js

# 2. Check compliance before starting
node .agent-os/tools/compliance-checker.js --detailed

# 3. Develop following established patterns
# Claude Code automatically enforces all standards

# 4. Compliance check after completion
node .agent-os/tools/compliance-checker.js --detailed
```

## 🔧 Available Tools

Claude Code has access to all Agent OS tools:
- **Compliance Checker**: `.agent-os/tools/compliance-checker.js`
- **Feature Scorer**: `.agent-os/tools/feature-scoring/`
- **Real-time Monitor**: `.agent-os/tools/real-time-monitor.js`
- **Enhanced Dashboard**: `.agent-os/tools/enhanced-dashboard.js`
- **Validation Suite**: `.agent-os/tools/validation-suite.js`
- **Database Optimizer**: `.agent-os/tools/database-optimizer.js`

## 📊 Quality Gates

Claude Code automatically enforces:
- **Code Quality**: ≤5 TODO items per service
- **Performance**: TTI ≤1.8s frontend, P95 ≤150ms backend
- **Security**: 0 vulnerabilities, 100% compliance
- **Architecture**: Controller → Service → Repository pattern enforced
- **Context7 Compliance**: 100% - All technology choices validated

## 🎯 Success Metrics

Claude Code maintains:
- **Zero Security Vulnerabilities**: No hardcoded secrets or security issues
- **High Code Quality**: ≤5 TODO items per service, ≥85% test coverage
- **Excellent Performance**: P95 ≤200ms response time
- **Comprehensive Documentation**: All public APIs documented
- **Systematic Refactoring**: Refactoring after each phase completion
- **Feature Scoring Compliance**: 100% features scored before development

## 🔄 Continuous Improvement

Claude Code automatically:
- **Runs weekly comprehensive compliance checks**
- **Captures lessons learned** after each development session
- **Analyzes statistical metrics** for optimization
- **Reviews feature scoring** and phase progression
- **Updates standards** based on Context7 best practices

## 🚨 Important Notes

1. **Claude Code is now identical** to Cursor in terms of rules and standards
2. **All development automatically follows** Agent OS standards and cursor rules
3. **No manual intervention required** - everything runs automatically
4. **Full compliance enforcement** with zero friction development
5. **Context7 integration** is mandatory and automatic

## 🆘 Troubleshooting

If you encounter any issues:
1. **Check compliance status**: `node .agent-os/tools/compliance-checker.js --detailed`
2. **Validate cursor rules**: Ensure `.cursor/rules/` files are accessible
3. **Check Agent OS standards**: Verify `.agent-os/standards/` directory structure
4. **Run setup validation**: `node .agent-os/scripts/setup.js validate`

---

**Claude Code is now fully integrated and ready for seamless development with the same standards as Cursor! 🚀**
