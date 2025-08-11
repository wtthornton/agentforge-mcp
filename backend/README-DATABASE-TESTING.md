# AgentForge Database Testing Guide

## 🎯 Overview
This guide explains how to test the AgentForge database integration with PostgreSQL 17 + pgvector extension.

## 🚀 Prerequisites

### 1. Docker Infrastructure
Ensure the PostgreSQL container is running:
```bash
cd infrastructure
docker-compose up -d postgres
docker-compose ps postgres  # Should show "healthy" status
```

### 2. Database Credentials
The default credentials are:
- **Host**: localhost:5432
- **Database**: agentforge
- **Username**: agentforge
- **Password**: agentforge

## 🧪 Running Database Tests

### Option 1: JUnit Tests (Recommended)
Run the database integration tests using Maven:

```bash
cd backend

# Run all database tests
mvn test -Dtest=DatabaseConnectionTest,DatabaseInitializationTest

# Run specific test class
mvn test -Dtest=DatabaseConnectionTest

# Run with detailed output
mvn test -Dtest=DatabaseInitializationTest -Dspring.profiles.active=test
```

### Option 2: Database Setup Runner
Run the database setup validation outside of the test framework:

```bash
cd backend

# Run with setup profile
mvn spring-boot:run -Dspring.profiles.active=setup
```

### Option 3: Manual Database Testing
Test the database connection manually using the API endpoints:

```bash
# Start the backend application
mvn spring-boot:run

# Test database connection (in another terminal)
curl http://localhost:8081/api/database/test

# Test with retry logic
curl http://localhost:8081/api/database/test-with-retry

# Check connection pool status
curl http://localhost:8081/api/database/pool-status
```

## 📊 Expected Test Results

### Database Connection Test
- ✅ DataSource should be available
- ✅ Database connection should be valid
- ✅ Basic query execution should work

### PgVector Extension Test
- ✅ pgvector extension should be available
- ✅ Vector type creation should work
- ✅ Vector similarity calculation should work

### Database Schema Test
- ✅ Core tables should exist (users, projects, project_analyses, etc.)
- ✅ Indexes should be created
- ✅ Constraints should be enforced

### Performance Test
- ✅ Database queries should complete in under 1 second
- ✅ Connection pool should be properly configured

## 🔧 Troubleshooting

### Common Issues

#### 1. Connection Refused
```
Error: Connection refused
Solution: Ensure PostgreSQL container is running and healthy
```

#### 2. Authentication Failed
```
Error: FATAL: password authentication failed
Solution: Check database credentials in application.yml
```

#### 3. PgVector Extension Missing
```
Error: type "vector" does not exist
Solution: Ensure pgvector extension is installed in PostgreSQL
```

#### 4. Tables Not Found
```
Error: relation "users" does not exist
Solution: Run Flyway migrations to create schema
```

### Debugging Steps

1. **Check Container Status**:
   ```bash
   docker-compose ps postgres
   docker logs agentforge-postgres
   ```

2. **Test Direct Connection**:
   ```bash
   docker exec -it agentforge-postgres psql -U agentforge -d agentforge
   ```

3. **Check Database Extensions**:
   ```sql
   SELECT * FROM pg_extension;
   ```

4. **Verify Schema**:
   ```sql
   \dt  -- List tables
   \di  -- List indexes
   ```

## 📈 Performance Benchmarks

### Target Metrics
- **Connection Time**: < 100ms
- **Query Response**: < 50ms for simple queries
- **Vector Operations**: < 100ms for similarity calculations
- **Connection Pool**: 20 max connections, 5 min idle

### Monitoring
Use the health endpoints to monitor database performance:
- `/actuator/health` - Overall health status
- `/api/database/pool-status` - Connection pool metrics
- `/actuator/metrics/hikaricp.connections` - Detailed pool metrics

## 🔒 Security Notes

- Database credentials are configured for development only
- Production deployments should use environment variables
- Connection pooling is configured with reasonable limits
- SQL injection protection is enabled via prepared statements

## 📚 Next Steps

After successful database testing:

1. **Repository Testing**: Test CRUD operations on all entities
2. **API Integration**: Test controllers with actual database
3. **Performance Testing**: Validate performance targets
4. **Security Testing**: Test authentication and authorization

## 🆘 Getting Help

If you encounter issues:

1. Check the troubleshooting section above
2. Review Docker container logs
3. Verify database configuration
4. Check Agent OS compliance reports
5. Review the implementation plan for next steps
