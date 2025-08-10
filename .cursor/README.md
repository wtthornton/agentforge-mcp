# Cursor IDE Integration - AgentForge

## Overview
This directory contains Cursor IDE integration rules and configuration for AgentForge, ensuring consistent development standards and workflow compliance.

## File Structure

### Root Level
- **`.cursorrules`** - Main integration point for Agent OS standards
- **`.cursor/README.md`** - This file

### Rules Directory (`.cursor/rules/`)
- **`standards-compliance.mdc`** - Comprehensive standards reference and validation
- **`development-workflow.mdc`** - Structured development process and workflow
- **`code-generation.mdc`** - Code generation patterns and standards
- **`project-specific.mdc`** - AgentForge-specific constraints and requirements

## Integration Points

### Agent OS Standards
All rules reference the comprehensive standards in `.agent-os/standards/`:
- Technology stack and versions
- Code style and conventions
- Best practices and patterns
- Security and compliance requirements
- Testing and quality standards

### Development Workflow
Rules enforce the mandatory Agent OS development workflow:
1. Standards compliance checking
2. Structured task execution
3. Immediate progress tracking
4. Continuous validation
5. Lessons learned integration

### Project Constraints
Rules enforce AgentForge-specific constraints:
- **NO AI suggestion features** - Static analyzer only
- Focus on core components: logging, reporting, monitoring
- Static analysis only - No dynamic AI features

## Usage

### For Developers
1. **Load Context** - Rules automatically apply when working in Cursor
2. **Follow Standards** - All generated code follows established patterns
3. **Validate Compliance** - Run compliance checker after changes
4. **Reference Standards** - Use linked standards files for detailed guidance

### For AI Assistants
1. **Standards First** - Always check standards before generating code
2. **Pattern Compliance** - Use established code generation patterns
3. **Constraint Adherence** - Respect project-specific constraints
4. **Quality Gates** - Ensure all quality requirements are met

## Compliance Requirements

### Mandatory Checks
- **Before coding**: Check relevant standards files
- **During development**: Follow established patterns
- **After changes**: Run compliance checker
- **Before submission**: Validate all standards compliance

### Quality Gates
- **Compliance Score**: ≥85%
- **Code Coverage**: ≥85% branch coverage
- **Security**: 0 vulnerabilities, 100% compliance
- **Performance**: Meets performance targets

## Tools Integration

### Compliance Checker
```bash
# Run comprehensive compliance check
node .agent-os/tools/compliance-checker.js --detailed

# Expected output:
# ✅ Compliance Score: 95%
# ✅ Standards Adherence: 100%
# ✅ Code Quality: Excellent
# ✅ No new violations detected
```

### Agent OS CLI
```bash
# Access Agent OS tools and utilities
node .agent-os/tools/agent-os-cli.js

# Quick validation
node .agent-os/scripts/setup.js validate
```

## Standards Reference

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

## Maintenance

### Updating Rules
1. **Standards Changes**: Update rules when standards change
2. **Project Evolution**: Adapt rules as project requirements evolve
3. **Lessons Learned**: Integrate new insights into rules
4. **Validation**: Test rules with compliance checker

### Rule Validation
```bash
# Validate rule syntax and references
node .agent-os/tools/validation-suite.js --rules

# Check rule compliance
node .agent-os/tools/compliance-checker.js --rules
```

## Support

### Documentation
- **Agent OS Fundamentals**: `.agent-os/AGENT-OS-FUNDAMENTALS.md`
- **Quick Start Guide**: `.agent-os/QUICK-START-GUIDE.md`
- **Developer Guide**: `.agent-os/DEVELOPER-GUIDE.md`

### Tools
- **Compliance Checker**: `.agent-os/tools/compliance-checker.js`
- **Validation Suite**: `.agent-os/tools/validation-suite.js`
- **Agent OS CLI**: `.agent-os/tools/agent-os-cli.js`

### Lessons Learned
- **Framework**: `.agent-os/lessons-learned/README.md`
- **Categories**: `.agent-os/lessons-learned/categories/`
- **Templates**: `.agent-os/templates/lessons-learned-template.md`
