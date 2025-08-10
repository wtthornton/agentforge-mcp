# AgentForge Database Schema Design

## 🎯 Overview
This document outlines the comprehensive database schema design for AgentForge, following PostgreSQL 17 and pgvector best practices for static analysis, project management, and compliance tracking.

## 🏗️ Architecture Principles
- **Normalization**: 3NF design with strategic denormalization for performance
- **Vector Operations**: Optimized pgvector integration for similarity search
- **Performance**: Indexing strategy for sub-200ms response times
- **Scalability**: Partitioning strategy for large datasets
- **Security**: Row-level security and audit trails

## 📊 Core Tables

### 1. Users & Authentication
```sql
-- User management with role-based access control
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'developer',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login TIMESTAMP WITH TIME ZONE
);

-- User sessions for authentication tracking
CREATE TABLE user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ip_address INET,
    user_agent TEXT
);

-- User permissions for fine-grained access control
CREATE TABLE user_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    permission VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    granted_by BIGINT REFERENCES users(id)
);
```

### 2. Projects & Analysis
```sql
-- Core project information
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    repository_url TEXT,
    local_path TEXT,
    technology_stack JSONB,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_analyzed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) DEFAULT 'active',
    metadata JSONB
);

-- Project analysis results
CREATE TABLE project_analyses (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    analysis_type VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) DEFAULT 'running',
    results JSONB,
    error_message TEXT,
    performance_metrics JSONB
);

-- File-level analysis results
CREATE TABLE file_analyses (
    id BIGSERIAL PRIMARY KEY,
    project_analysis_id BIGINT REFERENCES project_analyses(id) ON DELETE CASCADE,
    file_path TEXT NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT,
    lines_of_code INTEGER,
    complexity_score DECIMAL(5,2),
    quality_score DECIMAL(5,2),
    security_score DECIMAL(5,2),
    analysis_results JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 3. Code Quality & Standards
```sql
-- Standards compliance tracking
CREATE TABLE standards_compliance (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    standard_name VARCHAR(100) NOT NULL,
    standard_version VARCHAR(20),
    compliance_score DECIMAL(5,2),
    total_checks INTEGER,
    passed_checks INTEGER,
    failed_checks INTEGER,
    violations JSONB,
    last_checked_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    next_check_due TIMESTAMP WITH TIME ZONE
);

-- Code quality violations
CREATE TABLE code_violations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    file_analysis_id BIGINT REFERENCES file_analyses(id) ON DELETE SET NULL,
    rule_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    line_number INTEGER,
    column_number INTEGER,
    message TEXT NOT NULL,
    suggestion TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by BIGINT REFERENCES users(id)
);

-- Code quality rules and configurations
CREATE TABLE quality_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_id VARCHAR(100) UNIQUE NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    rule_description TEXT,
    category VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    configuration JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 4. Vector Embeddings & Similarity Search
```sql
-- Code embeddings for similarity search
CREATE TABLE code_embeddings (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    file_analysis_id BIGINT REFERENCES file_analyses(id) ON DELETE CASCADE,
    embedding_model VARCHAR(100) NOT NULL,
    embedding vector(1536), -- OpenAI embedding dimension
    code_hash VARCHAR(64) NOT NULL,
    code_snippet TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Function embeddings for code similarity
CREATE TABLE function_embeddings (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    file_analysis_id BIGINT REFERENCES file_analyses(id) ON DELETE CASCADE,
    function_name VARCHAR(255) NOT NULL,
    function_signature TEXT,
    embedding vector(1536),
    complexity_score DECIMAL(5,2),
    lines_of_code INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Pattern embeddings for code pattern recognition
CREATE TABLE pattern_embeddings (
    id BIGSERIAL PRIMARY KEY,
    pattern_type VARCHAR(50) NOT NULL,
    pattern_name VARCHAR(255) NOT NULL,
    embedding vector(1536),
    description TEXT,
    examples JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 5. Monitoring & Performance
```sql
-- System performance metrics
CREATE TABLE performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(10,4) NOT NULL,
    metric_unit VARCHAR(20),
    tags JSONB,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- API request logging
CREATE TABLE api_requests (
    id BIGSERIAL PRIMARY KEY,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    user_id BIGINT REFERENCES users(id),
    request_id VARCHAR(36) UNIQUE NOT NULL,
    request_body JSONB,
    response_status INTEGER,
    response_time_ms INTEGER,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Error logging
CREATE TABLE error_logs (
    id BIGSERIAL PRIMARY KEY,
    error_type VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT,
    user_id BIGINT REFERENCES users(id),
    request_id VARCHAR(36),
    context JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 6. Audit & Compliance
```sql
-- Audit trail for all operations
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Compliance reports
CREATE TABLE compliance_reports (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT REFERENCES projects(id) ON DELETE CASCADE,
    report_type VARCHAR(50) NOT NULL,
    report_period_start DATE NOT NULL,
    report_period_end DATE NOT NULL,
    overall_score DECIMAL(5,2),
    detailed_results JSONB,
    generated_by BIGINT REFERENCES users(id),
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

## 🔍 Indexing Strategy

### Performance Indexes
```sql
-- User authentication indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = true;

-- Project analysis indexes
CREATE INDEX idx_project_analyses_project_id ON project_analyses(project_id);
CREATE INDEX idx_project_analyses_status ON project_analyses(status);
CREATE INDEX idx_project_analyses_completed_at ON project_analyses(completed_at);

-- File analysis indexes
CREATE INDEX idx_file_analyses_project_analysis_id ON file_analyses(project_analysis_id);
CREATE INDEX idx_file_analyses_file_path ON file_analyses(file_path);
CREATE INDEX idx_file_analyses_quality_score ON file_analyses(quality_score);

-- Compliance indexes
CREATE INDEX idx_standards_compliance_project_id ON standards_compliance(project_id);
CREATE INDEX idx_standards_compliance_score ON standards_compliance(compliance_score);
CREATE INDEX idx_code_violations_project_id ON code_violations(project_id);
CREATE INDEX idx_code_violations_severity ON code_violations(severity);
CREATE INDEX idx_code_violations_resolved ON code_violations(resolved_at) WHERE resolved_at IS NULL;

-- Vector similarity indexes (HNSW for fast approximate search)
CREATE INDEX idx_code_embeddings_hnsw ON code_embeddings USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_function_embeddings_hnsw ON function_embeddings USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_pattern_embeddings_hnsw ON pattern_embeddings USING hnsw (embedding vector_cosine_ops);

-- Performance monitoring indexes
CREATE INDEX idx_performance_metrics_name_time ON performance_metrics(metric_name, recorded_at);
CREATE INDEX idx_api_requests_endpoint_time ON api_requests(endpoint, created_at);
CREATE INDEX idx_api_requests_user_time ON api_requests(user_id, created_at);

-- Audit and compliance indexes
CREATE INDEX idx_audit_logs_user_action ON audit_logs(user_id, action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_compliance_reports_project_period ON compliance_reports(project_id, report_period_start, report_period_end);
```

### Composite Indexes
```sql
-- Multi-column performance indexes
CREATE INDEX idx_file_analyses_project_quality ON file_analyses(project_id, quality_score DESC);
CREATE INDEX idx_code_violations_project_severity ON code_violations(project_id, severity, created_at DESC);
CREATE INDEX idx_standards_compliance_project_score ON standards_compliance(project_id, compliance_score DESC);
CREATE INDEX idx_performance_metrics_name_tags_time ON performance_metrics(metric_name, tags, recorded_at DESC);
```

## 🚀 Performance Optimizations

### Partitioning Strategy
```sql
-- Partition performance metrics by month for better query performance
CREATE TABLE performance_metrics_y2024m01 PARTITION OF performance_metrics
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE performance_metrics_y2024m02 PARTITION OF performance_metrics
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Partition audit logs by month
CREATE TABLE audit_logs_y2024m01 PARTITION OF audit_logs
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE audit_logs_y2024m02 PARTITION OF audit_logs
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

### Materialized Views
```sql
-- Daily compliance summary for fast dashboard queries
CREATE MATERIALIZED VIEW daily_compliance_summary AS
SELECT 
    DATE(created_at) as date,
    project_id,
    AVG(compliance_score) as avg_score,
    COUNT(*) as total_checks,
    SUM(CASE WHEN compliance_score >= 85 THEN 1 ELSE 0 END) as compliant_projects
FROM standards_compliance
GROUP BY DATE(created_at), project_id;

-- Weekly performance metrics summary
CREATE MATERIALIZED VIEW weekly_performance_summary AS
SELECT 
    DATE_TRUNC('week', recorded_at) as week_start,
    metric_name,
    AVG(metric_value) as avg_value,
    MIN(metric_value) as min_value,
    MAX(metric_value) as max_value,
    COUNT(*) as sample_count
FROM performance_metrics
GROUP BY DATE_TRUNC('week', recorded_at), metric_name;
```

## 🔒 Security & Access Control

### Row Level Security (RLS)
```sql
-- Enable RLS on sensitive tables
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_analyses ENABLE ROW LEVEL SECURITY;
ALTER TABLE file_analyses ENABLE ROW LEVEL SECURITY;
ALTER TABLE code_violations ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- RLS policies for projects
CREATE POLICY project_access_policy ON projects
    FOR ALL USING (
        created_by = current_setting('app.current_user_id')::bigint
        OR EXISTS (
            SELECT 1 FROM user_permissions 
            WHERE user_id = current_setting('app.current_user_id')::bigint
            AND resource_type = 'project'
            AND resource_id = projects.id
        )
    );

-- RLS policies for analyses
CREATE POLICY analysis_access_policy ON project_analyses
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM projects 
            WHERE projects.id = project_analyses.project_id
            AND (projects.created_by = current_setting('app.current_user_id')::bigint
                OR EXISTS (
                    SELECT 1 FROM user_permissions 
                    WHERE user_id = current_setting('app.current_user_id')::bigint
                    AND resource_type = 'project'
                    AND resource_id = projects.id
                ))
        )
    );
```

### Data Encryption
```sql
-- Encrypt sensitive columns
ALTER TABLE users ALTER COLUMN password_hash SET ENCRYPTED;
ALTER TABLE user_sessions ALTER COLUMN session_token SET ENCRYPTED;
ALTER TABLE audit_logs ALTER COLUMN old_values SET ENCRYPTED;
ALTER TABLE audit_logs ALTER COLUMN new_values SET ENCRYPTED;
```

## 📈 Monitoring & Maintenance

### Automated Maintenance
```sql
-- Create maintenance functions
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY daily_compliance_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY weekly_performance_summary;
END;
$$ LANGUAGE plpgsql;

-- Create cleanup functions
CREATE OR REPLACE FUNCTION cleanup_old_data()
RETURNS void AS $$
BEGIN
    -- Clean up old audit logs (keep 2 years)
    DELETE FROM audit_logs WHERE created_at < NOW() - INTERVAL '2 years';
    
    -- Clean up old performance metrics (keep 1 year)
    DELETE FROM performance_metrics WHERE recorded_at < NOW() - INTERVAL '1 year';
    
    -- Clean up old API requests (keep 6 months)
    DELETE FROM api_requests WHERE created_at < NOW() - INTERVAL '6 months';
    
    -- Clean up expired sessions
    DELETE FROM user_sessions WHERE expires_at < NOW();
END;
$$ LANGUAGE plpgsql;
```

### Performance Monitoring Views
```sql
-- Slow query monitoring
CREATE VIEW slow_queries AS
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows,
    100.0 * shared_blks_hit / nullif(shared_blks_hit + shared_blks_read, 0) AS hit_percent
FROM pg_stat_statements
WHERE mean_time > 100  -- Queries taking more than 100ms
ORDER BY mean_time DESC;

-- Index usage statistics
CREATE VIEW index_usage_stats AS
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## 🔧 Configuration & Tuning

### PostgreSQL Configuration
```sql
-- Optimize for vector operations
SET shared_preload_libraries = 'vector';
SET max_parallel_workers_per_gather = 4;
SET max_parallel_maintenance_workers = 4;
SET maintenance_work_mem = '1GB';
SET work_mem = '256MB';

-- Optimize for pgvector
SET hnsw.ef_search = 100;
SET hnsw.iterative_scan = strict_order;
SET ivfflat.probes = 10;
```

### Connection Pooling
```sql
-- Connection pool settings
SET max_connections = 200;
SET shared_buffers = '256MB';
SET effective_cache_size = '1GB';
```

## 📋 Implementation Checklist

### Phase 1: Core Schema
- [ ] Create database and enable extensions
- [ ] Implement user authentication tables
- [ ] Implement project management tables
- [ ] Implement basic analysis tables
- [ ] Create core indexes

### Phase 2: Vector Operations
- [ ] Implement embedding tables
- [ ] Create vector indexes (HNSW)
- [ ] Implement similarity search functions
- [ ] Test vector performance

### Phase 3: Advanced Features
- [ ] Implement audit logging
- [ ] Create materialized views
- [ ] Implement RLS policies
- [ ] Create maintenance functions

### Phase 4: Performance & Monitoring
- [ ] Implement partitioning
- [ ] Create monitoring views
- [ ] Optimize query performance
- [ ] Implement automated maintenance

## 🎯 Success Metrics
- **Query Performance**: P95 response time ≤200ms
- **Vector Search**: Similarity search ≤50ms
- **Storage Efficiency**: ≤512MB for 1000 LOC projects
- **Scalability**: Support 100+ concurrent users
- **Compliance**: 100% audit trail coverage

## 🔍 Next Steps
1. **Database Setup**: Create PostgreSQL 17 instance with pgvector
2. **Schema Implementation**: Execute schema creation scripts
3. **Data Migration**: Migrate existing project data
4. **Performance Testing**: Validate performance metrics
5. **Integration Testing**: Test with existing application code
