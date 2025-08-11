# 🎯 Product Folder Priority - Quick Reference Guide

## 🚨 MANDATORY: Always Start Here First

**Before making ANY development decisions, ALWAYS reference these files in order:**

### 1. **`.agent-os/product/tech-stack.md`** - TECHNOLOGY CONSTRAINTS
- **React**: 19.0.0 (exact version, not 19.x)
- **TypeScript**: 5.5.3 (exact version, not 5.x)
- **TailwindCSS**: 3.4.0 (exact version, not 3.x)
- **Spring Boot**: 3.5.0 (exact version, not 3.5.x)
- **PostgreSQL**: 17 with pgvector

### 2. **`.agent-os/product/mission.md`** - PROJECT GOALS
- **Purpose**: Static analyzer and project setup utility
- **Constraint**: NO AI suggestion features, NO dynamic AI processing
- **Focus**: Code quality, security, and style checking

### 3. **`.agent-os/product/FEATURES.md`** - REQUIRED FUNCTIONALITY
- **Core Components**: Logging, Monitoring, Reporting, Project Analysis
- **Architecture**: Controller → Service → Repository pattern
- **Database**: User management, project storage, compliance tracking

### 4. **`.agent-os/product/decisions.md`** - ARCHITECTURE CHOICES
- **Backend**: Spring Boot with Java 21 LTS
- **Frontend**: React 19 + TypeScript 5 + Functional Components
- **Database**: PostgreSQL 17 + pgvector extension

## 📋 Development Workflow

### Before Starting ANY Work
```bash
# 1. Check technology constraints
"According to .agent-os/product/tech-stack.md, we use React 19.0.0 and TypeScript 5.5.3"

# 2. Review project goals
"Based on .agent-os/product/mission.md, this project is for static analysis only"

# 3. Check required features
"Following .agent-os/product/FEATURES.md, we need to implement logging service first"

# 4. Reference architecture decisions
"According to .agent-os/product/decisions.md, we use Controller → Service → Repository pattern"
```

### Technology Decision Process
1. **FIRST**: Check `.agent-os/product/tech-stack.md` for exact versions
2. **SECOND**: Validate against Context7 for best practices
3. **THIRD**: Apply standards for implementation details

## 🚫 What NOT to Do

- ❌ **Don't** start with standards folder for technology versions
- ❌ **Don't** use version ranges (19.x, 5.x) instead of exact versions
- ❌ **Don't** ignore product constraints and mission
- ❌ **Don't** implement features not in FEATURES.md

## ✅ What TO Do

- ✅ **ALWAYS** start with product folder for project context
- ✅ **ALWAYS** use exact versions from product tech-stack.md
- ✅ **ALWAYS** validate against Context7 after product folder reference
- ✅ **ALWAYS** follow standards for implementation details

## 🔍 Quick Commands

```bash
# View product tech stack
cat .agent-os/product/tech-stack.md

# View project mission
cat .agent-os/product/mission.md

# View required features
cat .agent-os/product/FEATURES.md

# View architecture decisions
cat .agent-os/product/decisions.md
```

## 📊 Priority Summary

| Priority | Folder | Purpose | Usage |
|----------|--------|---------|-------|
| **1st** | `.agent-os/product/` | Project specifications | **MANDATORY FIRST** |
| **2nd** | `.cursor/rules/` | Development rules | After product folder |
| **3rd** | `.agent-os/standards/` | Implementation patterns | After product folder |

---

**Remember**: Product folder = **WHAT** to build, Standards folder = **HOW** to build it.

**Always start with WHAT before HOW!**
