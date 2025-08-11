# Agent OS Enforcement Rules

## 🚨 **MANDATORY COMPLIANCE REQUIREMENTS**

### **CRITICAL: Cursor Agent Management (MANDATORY)**
**BEFORE** starting any development work, you **MUST**:
1. **Clear Context**: Press `Ctrl+Shift+C` to clear previous conversation
2. **New Conversation**: Press `Ctrl+Shift+N` to start fresh
3. **Select Agent**: Choose appropriate agent type for the task
4. **Define Scope**: State single, focused objective clearly

**VIOLATION**: Any development without fresh AI agent will result in immediate compliance failure.

### **CRITICAL: Quick Context Gathering (MANDATORY)**
**BEFORE** starting any development work, you **MUST** run:
```bash
node .agent-os/tools/quick-context-gatherer.js
```

This tool provides **full project context in <100ms** and is **REQUIRED** for:
- Understanding current implementation status
- Identifying missing components
- Compliance status overview
- Project structure analysis
- Recent changes tracking

**VIOLATION**: Any development without running this tool first will result in immediate compliance failure.

### **Agent Management Workflow (MANDATORY)**
1. **ALWAYS** clear context: Press `Ctrl+Shift+C`
2. **ALWAYS** start fresh: Press `Ctrl+Shift+N`
3. **ALWAYS** select agent: Choose appropriate agent type for the task
4. **ALWAYS** define scope: State single, focused objective clearly

### **Context Gathering Workflow (MANDATORY)**
1. **ALWAYS** start with: `node .agent-os/tools/quick-context-gatherer.js`
2. **Review** the generated context summary
3. **Reference** the saved context file: `.agent-os/reports/quick-context.json`
4. **Follow** the .agent-os framework structure:
   - **Specs**: `.agent-os/specs/TASKS.md` - Implementation tasks
   - **Product**: `.agent-os/product/ROADMAP.md` - Product roadmap
   - **Standards**: `.agent-os/standards/` - Framework standards
5. **Proceed** with development using full context

### **Efficiency Standards (MANDATORY)**
- **Context Gathering**: ≤100ms for full project understanding
- **Tool Usage**: Use existing Agent OS tools before manual file reading
- **Status Checks**: Use `--summary` flags for quick overviews
- **File Reading**: Only read specific files when context is insufficient

### **Prohibited Inefficient Practices**
- ❌ Reading files individually without context
- ❌ Running compliance checks multiple times
- ❌ Manual directory exploration
- ❌ Repeated status checks
- ❌ Starting development without full context

### **Required Efficient Practices**
- ✅ Use quick context gatherer first
- ✅ Reference saved context files
- ✅ Use summary flags for tools
- ✅ Leverage existing Agent OS infrastructure
- ✅ Start with complete project understanding

## 🔒 **Compliance Enforcement**

### **Pre-Development Check (MANDATORY)**
```bash
# 1. Agent Management (REQUIRED)
#    - Clear context: Ctrl+Shift+C
#    - New conversation: Ctrl+Shift+N
#    - Select appropriate agent type
#    - Define single, focused objective

# 2. Gather full context (REQUIRED)
node .agent-os/tools/quick-context-gatherer.js

# 3. Check compliance (if needed)
node .agent-os/tools/compliance-checker.js --summary

# 4. Proceed with development
```

### **Violation Consequences**
- **Immediate**: Development blocked until context gathered
- **Compliance**: Score reduced by 25% for each violation
- **Quality**: Increased risk of implementation errors
- **Efficiency**: Wasted development time

### **Success Metrics**
- **Context Gathering**: 100% of development sessions start with full context
- **Efficiency**: ≤100ms context gathering time
- **Compliance**: ≥95% enforcement adherence
- **Quality**: Reduced implementation errors due to context gaps

## 📋 **Implementation Requirements**

### **For All Development Sessions**
1. **Start**: Run quick context gatherer
2. **Review**: Context summary and saved file
3. **Plan**: Use context to identify next steps
4. **Execute**: Develop with full project understanding
5. **Validate**: Run compliance check after changes

### **For Code Reviews**
1. **Context**: Reference quick context file
2. **Changes**: Review against current implementation status
3. **Compliance**: Verify standards adherence
4. **Quality**: Ensure no context gaps

### **For Documentation Updates**
1. **Context**: Use current project status
2. **Accuracy**: Reflect actual implementation state
3. **Completeness**: Cover all current components
4. **Timeliness**: Update with each change

## 🎯 **Success Indicators**

### **Efficiency Metrics**
- Context gathering time: ≤100ms
- Development start time: ≤30 seconds
- Context completeness: 100%
- Tool usage optimization: Maximum

### **Quality Metrics**
- Implementation accuracy: ≥95%
- Context-driven decisions: 100%
- Reduced rework: ≥50% improvement
- Development velocity: ≥25% improvement

### **Compliance Metrics**
- Enforcement adherence: 100%
- Standards compliance: ≥95%
- Context gathering compliance: 100%
- Tool usage compliance: 100%

## 🚀 **Quick Start Commands**

### **Daily Development**
```bash
# 1. ALWAYS start here (MANDATORY)
node .agent-os/tools/quick-context-gatherer.js

# 2. Check compliance if needed
node .agent-os/tools/compliance-checker.js --summary

# 3. Ready to develop with full context!
```

### **Context Reference**
```bash
# View saved context
cat .agent-os/reports/quick-context.json

# Quick status check
node .agent-os/scripts/setup.js status
```

### **Compliance Validation**
```bash
# Quick compliance check
node .agent-os/tools/compliance-checker.js --summary

# Detailed compliance (when needed)
node .agent-os/tools/compliance-checker.js --detailed
```

---

**Remember**: **ALWAYS** start with `node .agent-os/tools/quick-context-gatherer.js` for full project context in <100ms! 