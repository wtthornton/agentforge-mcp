# AgentForge MCP Server

A Model Context Protocol (MCP) server for AgentForge static analysis and project setup utilities.

## 🚀 Features

- **MCP Protocol Implementation**: Full JSON-RPC 2.0 compliance
- **Project Analysis**: Comprehensive project structure and dependency analysis
- **Standards Compliance**: Agent OS standards validation and reporting
- **Metrics Collection**: Performance, quality, and compliance metrics
- **Health Monitoring**: Built-in health checks and readiness probes
- **Comprehensive Logging**: Winston-based logging with file and console output

## 📋 Prerequisites

- Node.js 18.0.0 or higher
- npm 8.0.0 or higher
- TypeScript 5.3.0 or higher

## 🛠️ Installation

1. **Clone the repository** (if not already done):
   ```bash
   cd mcp-server
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Set up environment**:
   ```bash
   cp env.example .env
   # Edit .env with your configuration
   ```

4. **Build the project**:
   ```bash
   npm run build
   ```

## 🚀 Usage

### Development Mode
```bash
npm run dev
```

### Production Mode
```bash
npm run build
npm start
```

### Testing
```bash
npm test
npm run test:watch
```

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `3001` | Server port |
| `NODE_ENV` | `development` | Environment mode |
| `PROJECT_PATH` | `.` | Path to analyze |
| `LOG_LEVEL` | `info` | Logging level |
| `CORS_ORIGIN` | `http://localhost:3000` | CORS origin |

### Logging Configuration

The server uses Winston for logging with the following transports:
- **Console**: Colored output for development
- **File**: `logs/combined.log` for all logs
- **File**: `logs/error.log` for error logs only

## 📡 API Endpoints

### Health Checks
- `GET /health` - Basic health check
- `GET /ready` - Readiness probe for Kubernetes

### MCP Protocol Endpoints
- `POST /mcp/context` - Get project context
- `POST /mcp/standards` - Get standards information
- `POST /mcp/compliance` - Validate compliance
- `POST /mcp/analyze` - Analyze project structure
- `POST /mcp/metrics` - Get project metrics

### Information Endpoints
- `GET /mcp/protocol` - MCP protocol information
- `GET /mcp/capabilities` - Server capabilities

## 🔌 MCP Protocol

The server implements the Model Context Protocol (MCP) with the following methods:

### getContext
Retrieves comprehensive project context including:
- Project structure
- Dependencies
- Standards
- Compliance status
- Metrics

**Request**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "getContext",
  "params": {}
}
```

**Response**:
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "projectPath": "/path/to/project",
    "projectStructure": { ... },
    "dependencies": { ... },
    "standards": { ... },
    "compliance": { ... },
    "metrics": { ... }
  }
}
```

### getStandards
Retrieves Agent OS standards and compliance rules.

### validateCompliance
Validates project compliance against standards.

### analyzeProject
Analyzes project structure and quality.

### getMetrics
Retrieves performance, quality, and compliance metrics.

## 🏗️ Architecture

```
src/
├── server.ts          # Main server entry point
├── controllers/       # Request handlers
│   └── mcpController.ts
├── services/          # Business logic
│   └── mcpService.ts
├── types/            # Type definitions
│   └── mcp.ts
└── __tests__/        # Test files
    └── mcpService.test.ts
```

## 🧪 Testing

The project includes comprehensive tests:

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Generate coverage report
npm test -- --coverage
```

## 📊 Monitoring

### Health Checks
- **Health**: Basic server health
- **Ready**: Service readiness for traffic

### Metrics
- Performance metrics (uptime, memory, CPU)
- Quality metrics (code analysis, documentation)
- Compliance metrics (standards validation)
- System metrics (Node.js version, platform)

## 🔒 Security

- CORS configuration
- Request size limits
- Input validation
- Error handling without information leakage

## 🚀 Deployment

### Docker
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY dist ./dist
EXPOSE 3001
CMD ["npm", "start"]
```

### Kubernetes
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: mcp-server
  template:
    metadata:
      labels:
        app: mcp-server
    spec:
      containers:
      - name: mcp-server
        image: mcp-server:latest
        ports:
        - containerPort: 3001
        livenessProbe:
          httpGet:
            path: /health
            port: 3001
        readinessProbe:
          httpGet:
            path: /ready
            port: 3001
```

## 🤝 Contributing

1. Follow the existing code style
2. Add tests for new functionality
3. Update documentation as needed
4. Ensure all tests pass

## 📝 License

MIT License - see LICENSE file for details.

## 🆘 Support

For issues and questions:
1. Check the logs in `logs/` directory
2. Verify environment configuration
3. Run tests to ensure functionality
4. Check health endpoints for service status
