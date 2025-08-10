-- =====================================================
-- AgentForge Advanced Features Migration
-- Version: 1.0.1
-- Description: Advanced features including materialized views, RLS, and maintenance functions
-- =====================================================

-- =====================================================
-- MATERIALIZED VIEWS FOR PERFORMANCE
-- =====================================================

-- Daily compliance summary for fast dashboard queries
CREATE MATERIALIZED VIEW daily_compliance_summary AS
SELECT 
    DATE(created_at) as date,
    project_id,
    p.name as project_name,
    AVG(compliance_score) as avg_score,
    COUNT(*) as total_checks,
    SUM(CASE WHEN compliance_score >= 85 THEN 1 ELSE 0 END) as compliant_projects,
    SUM(CASE WHEN compliance_score < 85 THEN 1 ELSE 0 END) as non_compliant_projects
FROM standards_compliance sc
JOIN projects p ON sc.project_id = p.id
GROUP BY DATE(created_at), project_id, p.name;

-- Weekly performance metrics summary
CREATE MATERIALIZED VIEW weekly_performance_summary AS
SELECT 
    DATE_TRUNC('week', recorded_at) as week_start,
    metric_name,
    AVG(metric_value) as avg_value,
    MIN(metric_value) as min_value,
    MAX(metric_value) as max_value,
    COUNT(*) as sample_count,
    STDDEV(metric_value) as std_dev
FROM performance_metrics
GROUP BY DATE_TRUNC('week', recorded_at), metric_name;

-- Project quality overview
CREATE MATERIALIZED VIEW project_quality_overview AS
SELECT 
    p.id as project_id,
    p.name as project_name,
    p.status as project_status,
    COUNT(DISTINCT fa.id) as total_files,
    AVG(fa.quality_score) as avg_quality_score,
    AVG(fa.complexity_score) as avg_complexity_score,
    AVG(fa.security_score) as avg_security_score,
    COUNT(cv.id) as total_violations,
    COUNT(CASE WHEN cv.severity = 'critical' THEN 1 END) as critical_violations,
    COUNT(CASE WHEN cv.severity = 'high' THEN 1 END) as high_violations,
    COUNT(CASE WHEN cv.severity = 'medium' THEN 1 END) as medium_violations,
    COUNT(CASE WHEN cv.severity = 'low' THEN 1 END) as low_violations,
    p.last_analyzed_at
FROM projects p
LEFT JOIN project_analyses pa ON p.id = pa.project_id
LEFT JOIN file_analyses fa ON pa.id = fa.project_analysis_id
LEFT JOIN code_violations cv ON p.id = cv.project_id
WHERE p.status = 'active'
GROUP BY p.id, p.name, p.status, p.last_analyzed_at;

-- Code similarity patterns
CREATE MATERIALIZED VIEW code_similarity_patterns AS
SELECT 
    ce1.project_id as project1_id,
    p1.name as project1_name,
    ce2.project_id as project2_id,
    p2.name as project2_name,
    ce1.embedding_model,
    AVG(ce1.embedding <=> ce2.embedding) as avg_similarity_score,
    COUNT(*) as comparison_count
FROM code_embeddings ce1
JOIN projects p1 ON ce1.project_id = p1.id
JOIN code_embeddings ce2 ON ce1.embedding_model = ce2.embedding_model
JOIN projects p2 ON ce2.project_id = p2.id
WHERE ce1.project_id < ce2.project_id
GROUP BY ce1.project_id, p1.name, ce2.project_id, p2.name, ce1.embedding_model
HAVING AVG(ce1.embedding <=> ce2.embedding) < 0.3; -- High similarity threshold

-- =====================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- =====================================================

-- Enable RLS on sensitive tables
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE project_analyses ENABLE ROW LEVEL SECURITY;
ALTER TABLE file_analyses ENABLE ROW LEVEL SECURITY;
ALTER TABLE code_violations ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE code_embeddings ENABLE ROW LEVEL SECURITY;
ALTER TABLE function_embeddings ENABLE ROW LEVEL SECURITY;

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
        OR EXISTS (
            SELECT 1 FROM users 
            WHERE users.id = current_setting('app.current_user_id')::bigint
            AND users.role = 'admin'
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
                )
                OR EXISTS (
                    SELECT 1 FROM users 
                    WHERE users.id = current_setting('app.current_user_id')::bigint
                    AND users.role = 'admin'
                ))
        )
    );

-- RLS policies for file analyses
CREATE POLICY file_analysis_access_policy ON file_analyses
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM project_analyses pa
            JOIN projects p ON pa.project_id = p.id
            WHERE pa.id = file_analyses.project_analysis_id
            AND (p.created_by = current_setting('app.current_user_id')::bigint
                OR EXISTS (
                    SELECT 1 FROM user_permissions 
                    WHERE user_id = current_setting('app.current_user_id')::bigint
                    AND resource_type = 'project'
                    AND resource_id = p.id
                )
                OR EXISTS (
                    SELECT 1 FROM users 
                    WHERE users.id = current_setting('app.current_user_id')::bigint
                    AND users.role = 'admin'
                ))
        )
    );

-- RLS policies for code violations
CREATE POLICY code_violations_access_policy ON code_violations
    FOR ALL USING (
        project_id IN (
            SELECT p.id FROM projects p
            WHERE p.created_by = current_setting('app.current_user_id')::bigint
            OR EXISTS (
                SELECT 1 FROM user_permissions 
                WHERE user_id = current_setting('app.current_user_id')::bigint
                AND resource_type = 'project'
                AND resource_id = p.id
            )
            OR EXISTS (
                SELECT 1 FROM users 
                WHERE users.id = current_setting('app.current_user_id')::bigint
                AND users.role = 'admin'
            )
        )
    );

-- RLS policies for embeddings
CREATE POLICY embeddings_access_policy ON code_embeddings
    FOR ALL USING (
        project_id IN (
            SELECT p.id FROM projects p
            WHERE p.created_by = current_setting('app.current_user_id')::bigint
            OR EXISTS (
                SELECT 1 FROM user_permissions 
                WHERE user_id = current_setting('app.current_user_id')::bigint
                AND resource_type = 'project'
                AND resource_id = p.id
            )
            OR EXISTS (
                SELECT 1 FROM users 
                WHERE users.id = current_setting('app.current_user_id')::bigint
                AND users.role = 'admin'
            )
        )
    );

-- RLS policies for audit logs (admin only for sensitive operations)
CREATE POLICY audit_logs_access_policy ON audit_logs
    FOR ALL USING (
        user_id = current_setting('app.current_user_id')::bigint
        OR EXISTS (
            SELECT 1 FROM users 
            WHERE users.id = current_setting('app.current_user_id')::bigint
            AND users.role = 'admin'
        )
    );

-- =====================================================
-- MAINTENANCE FUNCTIONS
-- =====================================================

-- Function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY daily_compliance_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY weekly_performance_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY project_quality_overview;
    REFRESH MATERIALIZED VIEW CONCURRENTLY code_similarity_patterns;
END;
$$ LANGUAGE plpgsql;

-- Function to cleanup old data
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
    
    -- Clean up old error logs (keep 1 year)
    DELETE FROM error_logs WHERE created_at < NOW() - INTERVAL '1 year';
    
    -- Clean up old analyses (keep 6 months)
    DELETE FROM project_analyses WHERE completed_at < NOW() - INTERVAL '6 months';
    
    -- Clean up old file analyses (keep 6 months)
    DELETE FROM file_analyses WHERE created_at < NOW() - INTERVAL '6 months';
END;
$$ LANGUAGE plpgsql;

-- Function to analyze table statistics
CREATE OR REPLACE FUNCTION analyze_table_statistics()
RETURNS void AS $$
BEGIN
    ANALYZE users;
    ANALYZE projects;
    ANALYZE project_analyses;
    ANALYZE file_analyses;
    ANALYZE code_violations;
    ANALYZE standards_compliance;
    ANALYZE code_embeddings;
    ANALYZE function_embeddings;
    ANALYZE performance_metrics;
    ANALYZE api_requests;
    ANALYZE audit_logs;
END;
$$ LANGUAGE plpgsql;

-- Function to vacuum tables
CREATE OR REPLACE FUNCTION vacuum_tables()
RETURNS void AS $$
BEGIN
    VACUUM ANALYZE users;
    VACUUM ANALYZE projects;
    VACUUM ANALYZE project_analyses;
    VACUUM ANALYZE file_analyses;
    VACUUM ANALYZE code_violations;
    VACUUM ANALYZE standards_compliance;
    VACUUM ANALYZE code_embeddings;
    VACUUM ANALYZE function_embeddings;
    VACUUM ANALYZE performance_metrics;
    VACUUM ANALYZE api_requests;
    VACUUM ANALYZE audit_logs;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- PERFORMANCE MONITORING VIEWS
-- =====================================================

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

-- Table size statistics
CREATE VIEW table_size_stats AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) as index_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Vector index performance
CREATE VIEW vector_index_stats AS
SELECT 
    indexname,
    tablename,
    pg_size_pretty(pg_relation_size(indexname)) as index_size,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE indexname LIKE '%hnsw%' OR indexname LIKE '%ivfflat%'
ORDER BY idx_scan DESC;

-- =====================================================
-- UTILITY FUNCTIONS
-- =====================================================

-- Function to get project statistics
CREATE OR REPLACE FUNCTION get_project_statistics(project_id_param BIGINT)
RETURNS TABLE(
    total_files BIGINT,
    total_lines BIGINT,
    avg_quality_score DECIMAL(5,2),
    avg_complexity_score DECIMAL(5,2),
    avg_security_score DECIMAL(5,2),
    total_violations BIGINT,
    critical_violations BIGINT,
    high_violations BIGINT,
    compliance_score DECIMAL(5,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(DISTINCT fa.id)::BIGINT as total_files,
        COALESCE(SUM(fa.lines_of_code), 0)::BIGINT as total_lines,
        ROUND(AVG(fa.quality_score), 2) as avg_quality_score,
        ROUND(AVG(fa.complexity_score), 2) as avg_complexity_score,
        ROUND(AVG(fa.security_score), 2) as avg_security_score,
        COUNT(cv.id)::BIGINT as total_violations,
        COUNT(CASE WHEN cv.severity = 'critical' THEN 1 END)::BIGINT as critical_violations,
        COUNT(CASE WHEN cv.severity = 'high' THEN 1 END)::BIGINT as high_violations,
        COALESCE(MAX(sc.compliance_score), 0) as compliance_score
    FROM projects p
    LEFT JOIN project_analyses pa ON p.id = pa.project_id
    LEFT JOIN file_analyses fa ON pa.id = fa.project_analysis_id
    LEFT JOIN code_violations cv ON p.id = cv.project_id
    LEFT JOIN standards_compliance sc ON p.id = sc.project_id
    WHERE p.id = project_id_param
    GROUP BY p.id;
END;
$$ LANGUAGE plpgsql;

-- Function to find similar code patterns
CREATE OR REPLACE FUNCTION find_similar_code(
    query_embedding vector(1536),
    similarity_threshold DECIMAL DEFAULT 0.3,
    max_results INTEGER DEFAULT 10
)
RETURNS TABLE(
    project_id BIGINT,
    project_name VARCHAR(255),
    file_path TEXT,
    code_snippet TEXT,
    similarity_score DECIMAL(10,6)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ce.project_id,
        p.name as project_name,
        fa.file_path,
        ce.code_snippet,
        ROUND((ce.embedding <=> query_embedding)::DECIMAL, 6) as similarity_score
    FROM code_embeddings ce
    JOIN projects p ON ce.project_id = p.id
    JOIN file_analyses fa ON ce.file_analysis_id = fa.id
    WHERE ce.embedding <=> query_embedding < similarity_threshold
    ORDER BY ce.embedding <=> query_embedding
    LIMIT max_results;
END;
$$ LANGUAGE plpgsql;

-- Function to get compliance trend
CREATE OR REPLACE FUNCTION get_compliance_trend(
    project_id_param BIGINT,
    days_back INTEGER DEFAULT 30
)
RETURNS TABLE(
    date DATE,
    compliance_score DECIMAL(5,2),
    total_checks INTEGER,
    passed_checks INTEGER,
    failed_checks INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        DATE(sc.created_at) as date,
        sc.compliance_score,
        sc.total_checks,
        sc.passed_checks,
        sc.failed_checks
    FROM standards_compliance sc
    WHERE sc.project_id = project_id_param
    AND sc.created_at >= NOW() - INTERVAL '1 day' * days_back
    ORDER BY DATE(sc.created_at);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- SCHEDULED MAINTENANCE
-- =====================================================

-- Create a function to run scheduled maintenance
CREATE OR REPLACE FUNCTION run_scheduled_maintenance()
RETURNS void AS $$
BEGIN
    -- Refresh materialized views
    PERFORM refresh_materialized_views();
    
    -- Clean up old data
    PERFORM cleanup_old_data();
    
    -- Analyze table statistics
    PERFORM analyze_table_statistics();
    
    -- Log maintenance completion
    INSERT INTO performance_metrics (metric_name, metric_value, metric_unit, tags)
    VALUES ('maintenance_completed', EXTRACT(EPOCH FROM NOW()), 'seconds', '{"type": "scheduled", "status": "success"}');
    
    EXCEPTION
    WHEN OTHERS THEN
        -- Log maintenance failure
        INSERT INTO performance_metrics (metric_name, metric_value, metric_unit, tags)
        VALUES ('maintenance_failed', EXTRACT(EPOCH FROM NOW()), 'seconds', '{"type": "scheduled", "status": "error", "error": "' || SQLERRM || '"}');
        RAISE;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- INDEXES FOR MATERIALIZED VIEWS
-- =====================================================

-- Indexes for materialized view performance
CREATE INDEX idx_daily_compliance_summary_date ON daily_compliance_summary(date);
CREATE INDEX idx_daily_compliance_summary_project ON daily_compliance_summary(project_id);
CREATE INDEX idx_weekly_performance_summary_week ON weekly_performance_summary(week_start);
CREATE INDEX idx_project_quality_overview_project ON project_quality_overview(project_id);
CREATE INDEX idx_code_similarity_patterns_projects ON code_similarity_patterns(project1_id, project2_id);

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON MATERIALIZED VIEW daily_compliance_summary IS 'Daily compliance summary for dashboard performance';
COMMENT ON MATERIALIZED VIEW weekly_performance_summary IS 'Weekly performance metrics summary';
COMMENT ON MATERIALIZED VIEW project_quality_overview IS 'Project quality overview with violation counts';
COMMENT ON MATERIALIZED VIEW code_similarity_patterns IS 'Code similarity patterns between projects';

COMMENT ON FUNCTION refresh_materialized_views() IS 'Refreshes all materialized views concurrently';
COMMENT ON FUNCTION cleanup_old_data() IS 'Cleans up old data based on retention policies';
COMMENT ON FUNCTION find_similar_code(vector, DECIMAL, INTEGER) IS 'Finds similar code patterns using vector similarity';
COMMENT ON FUNCTION get_project_statistics(BIGINT) IS 'Returns comprehensive project statistics';
COMMENT ON FUNCTION get_compliance_trend(BIGINT, INTEGER) IS 'Returns compliance trend over specified days';

COMMENT ON VIEW slow_queries IS 'Shows slow queries for performance monitoring';
COMMENT ON VIEW index_usage_stats IS 'Shows index usage statistics';
COMMENT ON VIEW table_size_stats IS 'Shows table and index sizes';
COMMENT ON VIEW vector_index_stats IS 'Shows vector index performance statistics';
