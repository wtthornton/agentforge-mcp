-- AgentForge Database Initialization
-- This script sets up the initial database structure

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgvector";

-- Create database if it doesn't exist
-- Note: This will be handled by the POSTGRES_DB environment variable

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE agentforge TO agentforge;
GRANT ALL PRIVILEGES ON SCHEMA public TO agentforge;

-- Create a test table to verify the setup
CREATE TABLE IF NOT EXISTS agentforge_test (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test data
INSERT INTO agentforge_test (name) VALUES ('AgentForge Test Entry') ON CONFLICT DO NOTHING;
