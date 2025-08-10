-- =====================================================
-- AgentForge Core Database Schema
-- Version: 1.0.0
-- Description: Core tables for static analysis, project management, and compliance tracking
-- =====================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "vector";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- =====================================================
-- 1. USERS & AUTHENTICATION
-- =====================================================

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

-- =====================================================
-- 2. PROJECTS & ANALYSIS
-- =====================================================

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

-- =====================================================
-- 3. CODE QUALITY & STANDARDS
-- =====================================================

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

-- =====================================================
-- 4. VECTOR EMBEDDINGS & SIMILARITY SEARCH
-- =====================================================

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

-- =====================================================
-- 5. MONITORING & PERFORMANCE
-- =====================================================

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

-- =====================================================
-- 6. AUDIT & COMPLIANCE
-- =====================================================

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

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

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

-- Composite indexes for better performance
CREATE INDEX idx_file_analyses_project_quality ON file_analyses(project_id, quality_score DESC);
CREATE INDEX idx_code_violations_project_severity ON code_violations(project_id, severity, created_at DESC);
CREATE INDEX idx_standards_compliance_project_score ON standards_compliance(project_id, compliance_score DESC);
CREATE INDEX idx_performance_metrics_name_tags_time ON performance_metrics(metric_name, tags, recorded_at DESC);

-- =====================================================
-- TRIGGERS FOR AUTOMATIC UPDATES
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at trigger to relevant tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quality_rules_updated_at BEFORE UPDATE ON quality_rules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- INITIAL DATA
-- =====================================================

-- Insert default quality rules
INSERT INTO quality_rules (rule_id, rule_name, rule_description, category, severity, configuration) VALUES
('AGENT_OS_STANDARDS_COMPLIANCE', 'Agent OS Standards Compliance', 'Ensures code follows Agent OS development standards', 'compliance', 'high', '{"enabled": true, "threshold": 85}'),
('CODE_COMPLEXITY', 'Code Complexity Check', 'Monitors cyclomatic complexity of functions and methods', 'quality', 'medium', '{"enabled": true, "max_complexity": 10}'),
('SECURITY_VULNERABILITIES', 'Security Vulnerability Scan', 'Identifies potential security issues in code', 'security', 'critical', '{"enabled": true, "scan_types": ["sql_injection", "xss", "authentication"]}'),
('PERFORMANCE_ANTIPATTERNS', 'Performance Antipattern Detection', 'Detects code patterns that may cause performance issues', 'performance', 'medium', '{"enabled": true, "patterns": ["n_plus_one", "memory_leak", "inefficient_algorithms"]}'),
('DOCUMENTATION_COVERAGE', 'Documentation Coverage Check', 'Ensures adequate documentation coverage', 'documentation', 'low', '{"enabled": true, "min_coverage": 80}');

-- Insert default admin user (password: admin123 - should be changed in production)
INSERT INTO users (username, email, password_hash, role) VALUES
('admin', 'admin@agentforge.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin');

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON TABLE users IS 'User management and authentication information';
COMMENT ON TABLE projects IS 'Project information and metadata for analysis';
COMMENT ON TABLE project_analyses IS 'Analysis execution history and results';
COMMENT ON TABLE file_analyses IS 'File-level analysis results and metrics';
COMMENT ON TABLE code_violations IS 'Code quality and standards violations';
COMMENT ON TABLE code_embeddings IS 'Vector embeddings for code similarity search';
COMMENT ON TABLE performance_metrics IS 'System performance monitoring data';
COMMENT ON TABLE audit_logs IS 'Complete audit trail for compliance and security';

COMMENT ON COLUMN code_embeddings.embedding IS '1536-dimensional vector for OpenAI embeddings';
COMMENT ON COLUMN projects.technology_stack IS 'JSON object containing detected technologies and versions';
COMMENT ON COLUMN file_analyses.analysis_results IS 'Detailed analysis results in JSON format';
COMMENT ON COLUMN code_violations.violations IS 'JSON array of specific violation details';
