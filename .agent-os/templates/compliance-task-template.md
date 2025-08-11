# Compliance-Integrated Task Template

## Task: [Task Name]
**Status**: [ ] In Progress | [x] Completed  
**Priority**: High | Medium | Low  
**Assigned**: [Developer Name]  
**Due**: [YYYY-MM-DD]
**Agent Used**: [@static-analyzer/@database-agent/@frontend-agent/@backend-agent/@infrastructure-agent]

## MANDATORY: Cursor Agent Management

**CRITICAL**: All compliance tasks require fresh AI agents for optimal validation and tracking.

### Agent Assignment for Compliance Tasks
- **@static-analyzer**: Compliance validation, standards checking, violation detection and resolution
- **@backend-agent**: Backend compliance, API standards, service architecture validation
- **@frontend-agent**: Frontend compliance, UI/UX standards, component architecture validation
- **@database-agent**: Database compliance, schema standards, data integrity validation
- **@infrastructure-agent**: Infrastructure compliance, deployment standards, monitoring validation

### Compliance Workflow
1. **Clear Context**: Press `Ctrl+Shift+C` before compliance work
2. **New Conversation**: Press `Ctrl+Shift+N` for fresh agent
3. **Select Agent**: Choose appropriate agent type for compliance domain
4. **Run Compliance Check**: Use agent expertise for comprehensive validation
5. **Address Violations**: Fix issues with agent guidance
6. **Verify Resolution**: Confirm compliance with agent assistance

### Subtasks

#### 1.1 [Subtask Name]
- [x] **Completed**: [YYYY-MM-DD HH:MM]
- **Progress Note**: [Detailed description of what was accomplished]
- **Compliance Check**: ✅ Score [XX]% - [Status: No violations | Violations detected and fixed]
- **Issues**: [Any issues encountered]
- **Next**: [Next subtask or action]

#### 1.2 [Subtask Name]
- [ ] **In Progress** | [x] **Completed**: [YYYY-MM-DD HH:MM]
- **Progress Note**: [Detailed description of what was accomplished]
- **Compliance Check**: ✅ Score [XX]% - [Status: No violations | Violations detected and fixed]
- **Issues**: [Any issues encountered]
- **Next**: [Next subtask or action]

### Compliance Tracking

#### Overall Compliance Status
- **Current Score**: [XX]%
- **Target Score**: ≥85%
- **Last Check**: [YYYY-MM-DD HH:MM]
- **Status**: ✅ Compliant | ⚠️ Needs Attention | ❌ Non-Compliant

#### Compliance History
- **Task Start**: [XX]% (YYYY-MM-DD)
- **After Subtask 1.1**: [XX]% (YYYY-MM-DD)
- **After Subtask 1.2**: [XX]% (YYYY-MM-DD)
- **Current**: [XX]% (YYYY-MM-DD)

#### Violations Addressed
- [ ] [Violation 1] - [Date Fixed]
- [ ] [Violation 2] - [Date Fixed]
- [ ] [Violation 3] - [Date Fixed]

### Progress Summary

#### ✅ Completed in This Session
- [x] [Subtask 1.1]: [Brief description]
- [x] [Subtask 1.2]: [Brief description]

#### 🔄 Next Priority Tasks
- [ ] [Next subtask]
- [ ] [Next subtask]

#### 📊 Progress Update
- **Overall Progress**: [XX]% Complete
- **Compliance Score**: [XX]% (Target: ≥85%)
- **Subtasks Complete**: [X]/[Y]
- **Remaining Work**: [Brief description]

### Session Summary - [YYYY-MM-DD HH:MM]

#### ✅ Completed in This Session
- [x] [Task description with compliance status]

#### 🔄 Next Priority Tasks
- [ ] [Next task with compliance requirements]

#### 📊 Progress Update
- **Overall Progress**: [XX]% Complete (up from [XX]%)
- **Compliance Score**: [XX]% (Target: ≥85%)
- **Sections Complete**: [X]/[Y] major sections
- **Remaining Work**: [Integration testing, compliance fixes, etc.]

## Compliance Check Commands

### After Each Subtask
```bash
# Run detailed compliance check
node compliance-checker.js --detailed

# Expected output:
# ✅ Compliance Score: 95%
# ✅ Standards Adherence: 100%
# ✅ Code Quality: Excellent
# ✅ No new violations detected
```

### If Violations Detected
```bash
# Fix violations and re-check
node compliance-checker.js --detailed

# Verify score is back to ≥85%
# Document fixes in progress notes
```

### Compliance Failure Protocol
1. **If score < 85%**: Fix violations immediately
2. **If new violations**: Address before next subtask
3. **If standards violations**: Document and resolve as priority
4. **If quality issues**: Refactor code to meet standards 