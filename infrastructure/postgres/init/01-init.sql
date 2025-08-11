-- AgentForge Database Initialization
-- This script sets up the initial database structure

-- Create extensions (pgvector should already be available in pgvector/pgvector image)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgvector";

-- Ensure the agentforge user exists and has proper permissions
DO $$
BEGIN
    -- Create user if it doesn't exist
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'agentforge') THEN
        CREATE USER agentforge WITH PASSWORD 'agentforge';
        RAISE NOTICE 'Created user agentforge';
    ELSE
        RAISE NOTICE 'User agentforge already exists';
    END IF;
    
    -- Grant necessary permissions
    ALTER USER agentforge WITH CREATEDB;
    GRANT ALL PRIVILEGES ON DATABASE agentforge TO agentforge;
    GRANT ALL PRIVILEGES ON SCHEMA public TO agentforge;
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO agentforge;
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO agentforge;
    GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO agentforge;
    
    -- Set default privileges for future objects
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO agentforge;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO agentforge;
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO agentforge;
    
    RAISE NOTICE 'Granted all privileges to user agentforge';
END
$$;

-- Create a test table to verify the setup
CREATE TABLE IF NOT EXISTS agentforge_test (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert test data
INSERT INTO agentforge_test (name) VALUES ('AgentForge Test Entry') ON CONFLICT DO NOTHING;

-- Verify the setup
SELECT 'Database initialization completed successfully' as status;
