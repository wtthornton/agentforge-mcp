# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**AgentForge** is a comprehensive AI-powered development platform with Agent-OS integration, designed as a static analyzer and project setup utility. The project follows enterprise patterns with a microservices-oriented architecture using Java Spring Boot backend, React frontend, and MCP (Model Context Protocol) server.

**CRITICAL**: AgentForge is a **STATIC ANALYZER ONLY** project. NO AI suggestion features, NO dynamic AI processing, NO AI-driven automation.

## Commands

### Development Commands
```bash
# Start development (frontend + backend)
npm run dev

# Start individual services
npm run dev:frontend    # React dev server on port 5173
npm run dev:backend     # Spring Boot on port 8081

# Build entire project
npm run build

# Run all tests
npm run test

# Lint all code
npm run lint

# Infrastructure
npm start               # Start Docker stack (PostgreSQL, Redis, monitoring)
npm stop                # Stop Docker stack
npm logs                # View Docker logs

# Agent-OS compliance and validation
npm run compliance      # Run Agent-OS compliance checks
npm run validate        # Full validation pipeline (compliance + tests + build)
```

### Frontend-Specific Commands
```bash
cd frontend/

npm run dev            # Vite dev server with HMR
npm run build          # TypeScript compilation + Vite build
npm run test           # Vitest test runner
npm run test:ui        # Vitest UI interface
npm run test:coverage  # Run tests with coverage report
npm run lint           # ESLint with strict TypeScript rules
npm run lint:fix       # Auto-fix ESLint issues
npm run type-check     # TypeScript type checking
npm run format         # Prettier formatting
```

### Backend-Specific Commands
```bash
cd backend/

mvn spring-boot:run    # Development server with hot reload
mvn test               # JUnit 5 + Testcontainers
mvn clean package      # Build JAR file
mvn checkstyle:check   # Code style validation
```

### MCP Server Commands
```bash
cd mcp-server/

npm run dev            # ts-node development server
npm run build          # TypeScript compilation to dist/
npm run start          # Production server
npm run test           # Jest test suite
npm run test:watch     # Jest watch mode
npm run lint           # ESLint for TypeScript
```

### Agent-OS Framework Commands
```bash
# Quick start for new developers
node .agent-os/scripts/setup.js

# Daily development validation
node .agent-os/tools/compliance-checker.js --detailed

# Real-time monitoring dashboard
node .agent-os/tools/real-time-monitor.js

# Enhanced metrics dashboard
node .agent-os/tools/enhanced-dashboard.js

# Feature scoring (mandatory before development)
node .agent-os/tools/feature-scoring/feature-scorer.js

# Validate refactoring compliance
node .agent-os/tools/refactoring-validator.js --phase=1 --validate
```

## Architecture

### High-Level Structure
```
agentforge/
├── backend/              # Spring Boot 3.5 (Java 21)
├── frontend/             # React 19 + TypeScript 5
├── mcp-server/           # Model Context Protocol server
├── infrastructure/       # Docker Compose + monitoring
├── .agent-os/           # Agent-OS framework integration
└── implementation/       # Project specifications
```

### Technology Stack

**Backend (Spring Boot)**
- Java 21 LTS with Spring Boot 3.5
- PostgreSQL 17 with pgvector extension for vector operations
- Redis for caching and session management
- Spring Security with JWT authentication
- Spring JPA with Hibernate for ORM
- Comprehensive observability (Micrometer + Prometheus)

**Frontend (React)**
- React 19 with TypeScript 5 (strict mode)
- Vite 5.0 for build tooling with HMR
- TailwindCSS 3.4 for styling (avoid 4.x until stable)
- TanStack Query 5 for server state management
- React Router DOM 6.20 for routing
- Comprehensive testing with Vitest + Testing Library

**MCP Server**
- Node.js 18+ with TypeScript 5.3
- Express.js with security middleware (helmet, cors, rate limiting)
- SQLite for MCP-specific data storage
- Winston for structured logging
- Comprehensive Jest testing suite

**Infrastructure**
- Docker 24 with multi-stage builds
- PostgreSQL 17 with pgvector extension
- Redis with persistence configuration
- Full monitoring stack: Prometheus, Grafana, InfluxDB, Jaeger
- ELK stack for log aggregation

### Architecture Patterns

**Backend Pattern**: Controller → Service → Repository
- Controllers handle HTTP requests and validation
- Services contain business logic and transactions
- Repositories handle data access and database operations
- Comprehensive error handling and logging throughout

**Frontend Pattern**: Component-based with hooks
- Functional components with React hooks
- TanStack Query for server state management
- Context API for global application state
- Route-based code splitting for performance

**Database Design**:
- Core entities: User, Project, Analysis, Task, Agent
- Vector embeddings support with pgvector
- Comprehensive audit logging
- Performance-optimized queries with connection pooling

## Development Standards

### Mandatory Agent-OS Standards
Always reference these standards before making changes:
- Technology Stack: `.agent-os/standards/tech-stack.md`
- Code Style: `.agent-os/standards/code-style.md`
- Best Practices: `.agent-os/standards/best-practices.md`
- Security Compliance: `.agent-os/standards/security-compliance.md`
- Testing Strategy: `.agent-os/standards/testing-strategy.md`

### Code Quality Requirements
- **Test Coverage**: ≥85% branch coverage
- **TODO Limit**: ≤5 TODO items per service
- **Performance**: P95 ≤200ms response time (backend), TTI ≤1.8s (frontend)
- **Security**: Zero hardcoded secrets, 100% encryption coverage
- **Code Style**: Strict ESLint/Checkstyle compliance

### Feature Development Process
1. **Score Feature**: Use 4-dimension framework (Business Impact, Developer Productivity, Implementation Complexity, Adoption Likelihood)
2. **Validate Technology**: Reference Context7 documentation for best practices
3. **Check Compliance**: Run compliance checker before starting
4. **Develop**: Follow Controller → Service → Repository pattern
5. **Update Tasks**: Update tasks.md immediately after each subtask
6. **Run Compliance**: Check compliance after each subtask
7. **Capture Lessons**: Document lessons learned using templates

### Phase-Based Development
- **Phase 1**: Foundation (Database, Core APIs, Authentication)
- **Phase 2**: Integration (Frontend-Backend, Advanced Features)
- **Phase 3**: Optimization (Performance, Monitoring, Advanced Analytics)

Mandatory refactoring after each phase completion.

## Testing

### Backend Testing
- **Framework**: JUnit 5 + Spring Boot Test
- **Integration**: Testcontainers for PostgreSQL testing
- **Coverage**: JaCoCo with 85% line/branch coverage targets
- **Database**: H2 for unit tests, PostgreSQL containers for integration

### Frontend Testing
- **Framework**: Vitest with jsdom environment
- **Libraries**: Testing Library (React, Jest DOM, User Event)
- **Coverage**: V8 provider with 85% thresholds
- **Setup**: Global test configuration with comprehensive setup files

### MCP Server Testing
- **Framework**: Jest 29.7 with TypeScript support
- **Coverage**: Built-in Jest coverage (text, lcov, html)
- **Timeout**: 10-second test timeout configuration

## Development Environment

### Initial Setup
1. **Prerequisites**: Node.js 18+, Java 21, Docker 24, PostgreSQL 17
2. **Infrastructure**: Run `npm start` to launch Docker stack
3. **Dependencies**: Run `npm install` in root, frontend, and mcp-server directories
4. **Backend**: Maven will download dependencies automatically
5. **Validation**: Run `npm run validate` to ensure everything works

### Daily Development Workflow
1. **Infrastructure**: Ensure Docker stack is running (`npm start`)
2. **Backend**: `npm run dev:backend` (port 8081)
3. **Frontend**: `npm run dev:frontend` (port 5173, proxies API to backend)
4. **Validation**: Regularly run `npm run compliance` and `npm run test`

### Database Management
- **Development**: PostgreSQL 17 with pgvector in Docker
- **Testing**: Testcontainers for integration tests, H2 for unit tests
- **Migrations**: Flyway migrations in `backend/src/main/resources/db/migration/`
- **Connection**: Connection pooling configured with retry logic

## Constraints and Focus Areas

### Forbidden Development Areas
- NO AI suggestion features or dynamic AI processing
- NO real-time AI analysis or AI-controlled workflows
- NO machine learning features or AI chat interfaces
- Focus ONLY on static analysis capabilities

### Required Development Areas
- Static code analysis (quality, security, style)
- Project setup and scaffolding tools
- Compliance reporting and monitoring
- System health and performance metrics
- Comprehensive logging and audit trails

### Performance Targets
- Backend response time: P95 ≤200ms
- Frontend time to interactive: ≤1.8s
- Analysis speed: ≤30 seconds for 1000 LOC
- Memory usage: ≤512MB for large projects

## Key File Locations

### Configuration Files
- `package.json` - Root workspace configuration
- `frontend/vite.config.ts` - Frontend build configuration
- `backend/src/main/resources/application.yml` - Backend configuration
- `infrastructure/docker-compose.yml` - Infrastructure setup
- `.cursorrules` - Cursor AI development rules

### Important Directories
- `.agent-os/standards/` - Development standards and guidelines
- `.agent-os/tools/` - Agent-OS automation tools
- `.agent-os/lessons-learned/` - Knowledge management
- `backend/src/main/java/com/agentforge/` - Main backend code
- `frontend/src/` - Main frontend code
- `mcp-server/src/` - MCP server implementation

## Observability and Monitoring

The project includes comprehensive monitoring:
- **Metrics**: Prometheus + Grafana dashboards
- **Logging**: Structured logging with Winston (Node.js) and Logback (Java)
- **Tracing**: Jaeger for distributed tracing
- **Health Checks**: Spring Boot Actuator endpoints
- **Database Monitoring**: Connection pooling and query performance metrics

Access monitoring at:
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- Application health: http://localhost:8081/actuator/health