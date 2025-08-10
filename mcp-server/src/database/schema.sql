-- AgentForge MCP Server Database Schema
-- PostgreSQL 17+ with pgvector extension

-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    path TEXT NOT NULL UNIQUE,
    technology_stack TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_analyzed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    compliance_score DECIMAL(5,2),
    total_files INTEGER NOT NULL DEFAULT 0,
    total_directories INTEGER NOT NULL DEFAULT 0,
    total_lines_of_code INTEGER NOT NULL DEFAULT 0
);

-- Project analyses table
CREATE TABLE IF NOT EXISTS project_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    analysis_type VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    results JSONB,
    errors TEXT[],
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Project compliance table
CREATE TABLE IF NOT EXISTS project_compliance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    standard_id VARCHAR(100) NOT NULL,
    standard_name VARCHAR(255) NOT NULL,
    compliance_level VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    last_checked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    next_check_due TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Compliance violations table
CREATE TABLE IF NOT EXISTS compliance_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compliance_id UUID NOT NULL REFERENCES project_compliance(id) ON DELETE CASCADE,
    rule_id VARCHAR(100) NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    file_path TEXT,
    line_number INTEGER,
    column_number INTEGER,
    context TEXT,
    suggested_fix TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Project files table for detailed file analysis
CREATE TABLE IF NOT EXISTS project_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    file_path TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_extension VARCHAR(50),
    file_size BIGINT NOT NULL,
    lines_of_code INTEGER NOT NULL DEFAULT 0,
    language VARCHAR(100),
    complexity_score DECIMAL(5,2),
    last_modified TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Project directories table
CREATE TABLE IF NOT EXISTS project_directories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    directory_path TEXT NOT NULL,
    directory_name VARCHAR(255) NOT NULL,
    parent_directory_id UUID REFERENCES project_directories(id),
    depth_level INTEGER NOT NULL DEFAULT 0,
    file_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Technology stack analysis table
CREATE TABLE IF NOT EXISTS technology_stack_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    technology_name VARCHAR(255) NOT NULL,
    technology_type VARCHAR(100) NOT NULL,
    version VARCHAR(100),
    confidence_score DECIMAL(5,2) NOT NULL,
    detection_method VARCHAR(100),
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(100),
    resource_id UUID,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);
CREATE INDEX IF NOT EXISTS idx_projects_technology_stack ON projects USING GIN(technology_stack);
CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects(created_at);
CREATE INDEX IF NOT EXISTS idx_projects_compliance_score ON projects(compliance_score);

CREATE INDEX IF NOT EXISTS idx_project_analyses_project_id ON project_analyses(project_id);
CREATE INDEX IF NOT EXISTS idx_project_analyses_type_status ON project_analyses(analysis_type, status);
CREATE INDEX IF NOT EXISTS idx_project_analyses_started_at ON project_analyses(started_at);

CREATE INDEX IF NOT EXISTS idx_project_compliance_project_id ON project_compliance(project_id);
CREATE INDEX IF NOT EXISTS idx_project_compliance_standard_id ON project_compliance(standard_id);
CREATE INDEX IF NOT EXISTS idx_project_compliance_level ON project_compliance(compliance_level);

CREATE INDEX IF NOT EXISTS idx_compliance_violations_compliance_id ON compliance_violations(compliance_id);
CREATE INDEX IF NOT EXISTS idx_compliance_violations_severity ON compliance_violations(severity);
CREATE INDEX IF NOT EXISTS idx_compliance_violations_rule_id ON compliance_violations(rule_id);

CREATE INDEX IF NOT EXISTS idx_project_files_project_id ON project_files(project_id);
CREATE INDEX IF NOT EXISTS idx_project_files_language ON project_files(language);
CREATE INDEX IF NOT EXISTS idx_project_files_extension ON project_files(file_extension);

CREATE INDEX IF NOT EXISTS idx_project_directories_project_id ON project_directories(project_id);
CREATE INDEX IF NOT EXISTS idx_project_directories_parent_id ON project_directories(parent_directory_id);

CREATE INDEX IF NOT EXISTS idx_technology_stack_project_id ON technology_stack_analysis(project_id);
CREATE INDEX IF NOT EXISTS idx_technology_stack_technology_name ON technology_stack_analysis(technology_name);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_project_compliance_updated_at BEFORE UPDATE ON project_compliance
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_project_files_updated_at BEFORE UPDATE ON project_files
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create vector embeddings table for AI-powered analysis (future use)
CREATE TABLE IF NOT EXISTS code_embeddings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    file_id UUID REFERENCES project_files(id) ON DELETE CASCADE,
    content_hash VARCHAR(64) NOT NULL,
    embedding vector(1536), -- OpenAI embedding dimension
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_code_embeddings_project_id ON code_embeddings(project_id);
CREATE INDEX IF NOT EXISTS idx_code_embeddings_content_hash ON code_embeddings(content_hash);
CREATE INDEX IF NOT EXISTS idx_code_embeddings_vector ON code_embeddings USING ivfflat (embedding vector_cosine_ops);

-- Insert initial data
INSERT INTO projects (name, description, path, technology_stack, status, total_files, total_directories, total_lines_of_code)
VALUES (
    'AgentForge MCP Server',
    'MCP Server for AgentForge static analysis and project management',
    '/mcp-server',
    ARRAY['TypeScript', 'Node.js', 'Express', 'PostgreSQL', 'pgvector'],
    'completed',
    15,
    8,
    1200
) ON CONFLICT (path) DO NOTHING;
