-- Sample data for AgentForge development and testing
-- Note: This file runs after schema creation and provides initial test data

-- Insert sample users with BCrypt-hashed passwords
-- Password for all users: "password123" (BCrypt with strength 12)
INSERT INTO users (username, email, password_hash, role, is_active, created_at, updated_at) VALUES 
    ('admin', 'admin@agentforge.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'ADMIN', true, NOW(), NOW()),
    ('developer', 'dev@agentforge.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'DEVELOPER', true, NOW(), NOW()),
    ('viewer', 'viewer@agentforge.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'VIEWER', true, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- Insert additional test users
INSERT INTO users (username, email, password_hash, role, is_active, created_at, updated_at) VALUES 
    ('johndoe', 'john.doe@example.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'DEVELOPER', true, NOW(), NOW()),
    ('janesmith', 'jane.smith@example.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'VIEWER', true, NOW(), NOW()),
    ('testuser', 'test@example.com', '$2a$12$LQv3c1yqBw9YEGjhFH4fBu.YkjNZvJzgWr.k5HKkWm.4W.h7OJ8G6', 'DEVELOPER', false, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- Add sample permissions for testing
INSERT INTO user_permissions (user_id, permission) 
SELECT u.id, 'READ_PROJECTS' 
FROM users u 
WHERE u.username IN ('admin', 'developer', 'viewer') 
ON CONFLICT DO NOTHING;

INSERT INTO user_permissions (user_id, permission) 
SELECT u.id, 'WRITE_PROJECTS' 
FROM users u 
WHERE u.username IN ('admin', 'developer') 
ON CONFLICT DO NOTHING;

INSERT INTO user_permissions (user_id, permission) 
SELECT u.id, 'DELETE_PROJECTS' 
FROM users u 
WHERE u.username = 'admin' 
ON CONFLICT DO NOTHING;

INSERT INTO user_permissions (user_id, permission) 
SELECT u.id, 'MANAGE_AGENTS' 
FROM users u 
WHERE u.username IN ('admin', 'developer') 
ON CONFLICT DO NOTHING;

INSERT INTO user_permissions (user_id, permission) 
SELECT u.id, 'SYSTEM_ADMIN' 
FROM users u 
WHERE u.username = 'admin' 
ON CONFLICT DO NOTHING;

-- Sample projects (if projects table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'projects') THEN
        INSERT INTO projects (name, description, status, created_at, updated_at) VALUES 
            ('AgentForge Core', 'Main AgentForge development project', 'ACTIVE', NOW(), NOW()),
            ('Sample Project', 'Test project for development', 'ACTIVE', NOW(), NOW()),
            ('Archived Project', 'Old project for testing', 'ARCHIVED', NOW(), NOW())
        ON CONFLICT (name) DO NOTHING;
    END IF;
END $$;

-- Sample agents (if agents table exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'agents') THEN
        INSERT INTO agents (name, type, status, created_at, updated_at) VALUES 
            ('Code Analyzer', 'ANALYZER', 'RUNNING', NOW(), NOW()),
            ('Performance Monitor', 'MONITOR', 'RUNNING', NOW(), NOW()),
            ('Security Scanner', 'ANALYZER', 'STOPPED', NOW(), NOW()),
            ('Report Generator', 'REPORTER', 'RUNNING', NOW(), NOW())
        ON CONFLICT (name) DO NOTHING;
    END IF;
END $$;