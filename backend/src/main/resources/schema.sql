-- AgentForge Database Schema
-- PostgreSQL 17 with pgvector extension

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgvector";

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Projects table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    repository_url VARCHAR(500),
    project_path VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    technology_stack TEXT,
    lines_of_code BIGINT,
    last_analysis_date TIMESTAMP,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Analyses table
CREATE TABLE analyses (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    analysis_type VARCHAR(30) NOT NULL DEFAULT 'FULL',
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_seconds BIGINT,
    lines_analyzed BIGINT,
    files_analyzed BIGINT,
    compliance_score DECIMAL(5,2),
    quality_score DECIMAL(5,2),
    security_score DECIMAL(5,2),
    performance_score DECIMAL(5,2),
    total_violations INTEGER,
    critical_violations INTEGER,
    warning_violations INTEGER,
    info_violations INTEGER,
    analysis_summary TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Analysis metrics table (element collection)
CREATE TABLE analysis_metrics (
    analysis_id BIGINT NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    metric_key VARCHAR(100) NOT NULL,
    metric_value TEXT,
    PRIMARY KEY (analysis_id, metric_key)
);

-- Analysis errors table (element collection)
CREATE TABLE analysis_errors (
    analysis_id BIGINT NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    error_message TEXT NOT NULL
);

-- Compliance violations table
CREATE TABLE compliance_violations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    analysis_id BIGINT REFERENCES analyses(id) ON DELETE SET NULL,
    rule_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(200) NOT NULL,
    rule_category VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    line_number INTEGER,
    column_number INTEGER,
    message TEXT NOT NULL,
    code_snippet TEXT,
    suggestion TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    false_positive BOOLEAN DEFAULT false,
    suppressed_until TIMESTAMP,
    suppression_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at);

CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_technology_stack ON projects(technology_stack);
CREATE INDEX idx_projects_last_analysis_date ON projects(last_analysis_date);
CREATE INDEX idx_projects_created_at ON projects(created_at);

CREATE INDEX idx_analyses_project_id ON analyses(project_id);
CREATE INDEX idx_analyses_user_id ON analyses(user_id);
CREATE INDEX idx_analyses_status ON analyses(status);
CREATE INDEX idx_analyses_type ON analyses(analysis_type);
CREATE INDEX idx_analyses_start_time ON analyses(start_time);
CREATE INDEX idx_analyses_created_at ON analyses(created_at);

CREATE INDEX idx_compliance_violations_project_id ON compliance_violations(project_id);
CREATE INDEX idx_compliance_violations_analysis_id ON compliance_violations(analysis_id);
CREATE INDEX idx_compliance_violations_rule_id ON compliance_violations(rule_id);
CREATE INDEX idx_compliance_violations_severity ON compliance_violations(severity);
CREATE INDEX idx_compliance_violations_status ON compliance_violations(status);
CREATE INDEX idx_compliance_violations_file_path ON compliance_violations(file_path);
CREATE INDEX idx_compliance_violations_line_number ON compliance_violations(line_number);
CREATE INDEX idx_compliance_violations_created_at ON compliance_violations(created_at);

-- Composite indexes for common query patterns
CREATE INDEX idx_projects_owner_status ON projects(owner_id, status);
CREATE INDEX idx_projects_owner_created ON projects(owner_id, created_at);
CREATE INDEX idx_analyses_project_status ON analyses(project_id, status);
CREATE INDEX idx_analyses_project_type ON analyses(project_id, analysis_type);
CREATE INDEX idx_analyses_project_created ON analyses(project_id, created_at);
CREATE INDEX idx_violations_project_severity ON compliance_violations(project_id, severity);
CREATE INDEX idx_violations_project_status ON compliance_violations(project_id, status);
CREATE INDEX idx_violations_analysis_severity ON compliance_violations(analysis_id, severity);

-- Full-text search indexes
CREATE INDEX idx_projects_description_fts ON projects USING gin(to_tsvector('english', description));
CREATE INDEX idx_violations_message_fts ON compliance_violations USING gin(to_tsvector('english', message));

-- Constraints
ALTER TABLE projects ADD CONSTRAINT chk_project_status CHECK (status IN ('ACTIVE', 'ARCHIVED', 'SUSPENDED'));
ALTER TABLE analyses ADD CONSTRAINT chk_analysis_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED'));
ALTER TABLE analyses ADD CONSTRAINT chk_analysis_type CHECK (analysis_type IN ('FULL', 'INCREMENTAL', 'COMPLIANCE', 'SECURITY', 'PERFORMANCE'));
ALTER TABLE compliance_violations ADD CONSTRAINT chk_violation_severity CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'));
ALTER TABLE compliance_violations ADD CONSTRAINT chk_violation_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'FALSE_POSITIVE', 'SUPPRESSED', 'WONT_FIX'));
ALTER TABLE users ADD CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'USER', 'ANALYST', 'VIEWER'));

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_analyses_updated_at BEFORE UPDATE ON analyses FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_compliance_violations_updated_at BEFORE UPDATE ON compliance_violations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Views for common queries
CREATE VIEW project_summary AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.status,
    p.technology_stack,
    p.lines_of_code,
    p.last_analysis_date,
    u.username as owner_username,
    u.email as owner_email,
    COUNT(DISTINCT a.id) as total_analyses,
    COUNT(DISTINCT v.id) as total_violations,
    COUNT(DISTINCT CASE WHEN v.severity = 'CRITICAL' THEN v.id END) as critical_violations,
    COUNT(DISTINCT CASE WHEN v.severity = 'HIGH' THEN v.id END) as high_violations,
    COUNT(DISTINCT CASE WHEN v.severity = 'MEDIUM' THEN v.id END) as medium_violations,
    COUNT(DISTINCT CASE WHEN v.severity = 'LOW' THEN v.id END) as low_violations,
    p.created_at,
    p.updated_at
FROM projects p
LEFT JOIN users u ON p.owner_id = u.id
LEFT JOIN analyses a ON p.id = a.project_id
LEFT JOIN compliance_violations v ON p.id = v.project_id
GROUP BY p.id, p.name, p.description, p.status, p.technology_stack, p.lines_of_code, p.last_analysis_date, u.username, u.email, p.created_at, p.updated_at;

CREATE VIEW analysis_summary AS
SELECT 
    a.id,
    a.project_id,
    p.name as project_name,
    a.user_id,
    u.username as user_username,
    a.status,
    a.analysis_type,
    a.start_time,
    a.end_time,
    a.duration_seconds,
    a.lines_analyzed,
    a.files_analyzed,
    a.compliance_score,
    a.quality_score,
    a.security_score,
    a.performance_score,
    a.total_violations,
    a.critical_violations,
    a.warning_violations,
    a.info_violations,
    a.created_at,
    a.updated_at
FROM analyses a
JOIN projects p ON a.project_id = p.id
JOIN users u ON a.user_id = u.id;

CREATE VIEW violation_summary AS
SELECT 
    v.id,
    v.project_id,
    p.name as project_name,
    v.analysis_id,
    v.rule_id,
    v.rule_name,
    v.rule_category,
    v.severity,
    v.file_path,
    v.line_number,
    v.column_number,
    v.message,
    v.status,
    v.false_positive,
    v.suppressed_until,
    v.created_at,
    v.updated_at
FROM compliance_violations v
JOIN projects p ON v.project_id = p.id;

-- Initial data (optional - for development/testing)
INSERT INTO users (username, email, password_hash, first_name, last_name, role) VALUES
('admin', 'admin@agentforge.com', '$2a$10$dummy.hash.for.development', 'System', 'Administrator', 'ADMIN'),
('demo', 'demo@agentforge.com', '$2a$10$dummy.hash.for.development', 'Demo', 'User', 'USER');

-- Comments for documentation
COMMENT ON TABLE users IS 'User accounts for AgentForge system';
COMMENT ON TABLE projects IS 'Software projects for analysis and compliance tracking';
COMMENT ON TABLE analyses IS 'Project analysis results and metrics';
COMMENT ON TABLE compliance_violations IS 'Compliance rule violations found during analysis';
COMMENT ON TABLE analysis_metrics IS 'Key-value metrics for analysis runs';
COMMENT ON TABLE analysis_errors IS 'Error messages from failed analyses';

COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN projects.technology_stack IS 'JSON or comma-separated list of technologies';
COMMENT ON COLUMN analyses.compliance_score IS 'Compliance score from 0.0 to 100.0';
COMMENT ON COLUMN analyses.quality_score IS 'Code quality score from 0.0 to 100.0';
COMMENT ON COLUMN analyses.security_score IS 'Security score from 0.0 to 100.0';
COMMENT ON COLUMN analyses.performance_score IS 'Performance score from 0.0 to 100.0';
COMMENT ON COLUMN compliance_violations.severity IS 'Violation severity level for prioritization';
COMMENT ON COLUMN compliance_violations.status IS 'Current status of the violation';
