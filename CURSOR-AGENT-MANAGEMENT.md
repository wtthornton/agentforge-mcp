# 🎯 Cursor Agent Management Quick Reference

## **MANDATORY: Fresh Agent for Every Subtask**

To prevent conversation length issues and maintain optimal AI assistance, **every subtask requires a fresh AI agent**.

## 🚀 Quick Start Commands

### **Before Each Task (REQUIRED)**
1. **Clear Context**: `Ctrl+Shift+C`
2. **New Conversation**: `Ctrl+Shift+N`
3. **Select Agent**: Choose appropriate agent type
4. **Define Scope**: State single, focused objective

### **Agent Selection Commands**
- `@backend-agent` - Java/Spring Boot development
- `@database-agent` - PostgreSQL and database operations
- `@frontend-agent` - React 19/TypeScript development
- `@static-analyzer` - Code quality and compliance
- `@infrastructure-agent` - Docker and deployment

## 📋 Task-Agent Mapping

| Task Type | Agent | Example Use |
|-----------|-------|-------------|
| **Java/Spring Boot** | `@backend-agent` | "Help implement this Spring Boot service" |
| **Database/Schema** | `@database-agent` | "Optimize this PostgreSQL query" |
| **React/TypeScript** | `@frontend-agent` | "Review this React component" |
| **Code Analysis** | `@static-analyzer` | "Check compliance with Agent OS standards" |
| **Docker/Deployment** | `@infrastructure-agent` | "Configure this Docker Compose setup" |

## 🔄 Workflow Example

### **Task 1: Implement Spring Boot Service**
1. `Ctrl+Shift+C` (Clear context)
2. `Ctrl+Shift+N` (New conversation)
3. **Agent**: `@backend-agent`
4. **Prompt**: "Help me implement this Spring Boot service method for project analysis"
5. **Complete task**
6. **Close conversation**

### **Task 2: Optimize Database Query**
1. `Ctrl+Shift+C` (Clear context)
2. `Ctrl+Shift+N` (New conversation)
3. **Agent**: `@database-agent`
4. **Prompt**: "Help optimize this PostgreSQL query for better performance"
5. **Complete task**
6. **Close conversation**

## ✅ Benefits

- **Prevents conversation length limitations**
- **Maintains fresh context for each task**
- **Provides specialized expertise for different domains**
- **Improves response quality and relevance**
- **Enables parallel development across different areas**

## 🚫 What NOT to Do

- ❌ **Don't** use the same conversation for multiple tasks
- ❌ **Don't** mix different types of tasks in one conversation
- ❌ **Don't** let conversations get too long
- ❌ **Don't** use wrong agent type for the task

## 🎯 Best Practices

1. **Single Focus**: One clear objective per conversation
2. **Right Agent**: Choose agent type that matches task requirements
3. **Clear Context**: Provide relevant files and specific requirements
4. **Quick Closure**: End conversation after task completion
5. **Document Success**: Note effective agent-task combinations

---

**Remember**: Fresh agent = Better results = No conversation length issues!
