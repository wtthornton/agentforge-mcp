# Technical Stack

## Application Framework
- **Backend**: Spring Boot 3.5.0 (Java 21 LTS)
- **Frontend**: React 19.0.0 with TypeScript 5.5.3

## Database System
- **Primary Database**: PostgreSQL 17 with pgvector extension
- **Time Series Database**: InfluxDB 6.12.0
- **Caching**: Redis

## JavaScript Framework
- **Frontend Framework**: React 19.0.0
- **Build Tool**: Vite 5.0.0
- **Package Manager**: npm 9.0.0+

## Import Strategy
- **Module System**: ES Modules (ESM)
- **Bundling**: Vite with native ESM support

## CSS Framework
- **Styling**: TailwindCSS 3.4.0
- **PostCSS**: 8.4.32 with autoprefixer

## UI Component Library
- **Component System**: Custom components with shadcn/ui patterns
- **Icon Library**: Lucide React 0.294.0
- **Form Handling**: React Hook Form 7.48.0 with Zod validation

## Fonts Provider
- **Font System**: System fonts with TailwindCSS defaults

## Icon Library
- **Icon System**: Lucide React 0.294.0

## Application Hosting
- **Containerization**: Docker 24 with multi-stage builds
- **Orchestration**: Docker Compose for local development

## Database Hosting
- **Database**: PostgreSQL 17 with pgvector extension
- **Time Series**: InfluxDB 6.12.0
- **Cache**: Redis

## Asset Hosting
- **Static Assets**: Vite build output with optimized bundling
- **CDN**: Configurable for production deployments

## Deployment Solution
- **CI/CD**: GitHub Actions
- **Container Registry**: Docker Hub / GitHub Container Registry
- **Infrastructure**: Docker-based deployment with configurable cloud providers

## Code Repository URL
- **Repository**: GitHub repository (configurable)
- **Version Control**: Git with conventional commit standards

## Additional Technologies

### Backend Dependencies
- **Spring Cloud**: 2024.0.0
- **Spring AI**: 1.0.0
- **OpenAI**: 3.7.0
- **LangChain**: 0.3.0
- **Micrometer**: 1.12.0
- **OpenTelemetry**: 1.36.0

### Frontend Dependencies
- **State Management**: TanStack Query 5.0.0
- **Routing**: React Router DOM 6.20.0
- **HTTP Client**: Axios 1.7.0
- **Utilities**: clsx 2.0.0, tailwind-merge 2.0.0, date-fns 3.0.0

### Development Tools
- **Testing**: Vitest 1.0.0 with Testing Library
- **Linting**: ESLint 9.17.0 with TypeScript support
- **Formatting**: Prettier 3.6.2
- **Type Checking**: TypeScript 5.5.3

### Monitoring & Observability
- **Metrics**: Micrometer with Prometheus support
- **Tracing**: OpenTelemetry integration
- **Health Checks**: Spring Boot Actuator
- **Logging**: Structured logging with configurable backends
