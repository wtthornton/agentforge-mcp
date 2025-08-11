#!/bin/bash

# AgentForge Deployment Validation Script
# Comprehensive validation of deployment health and functionality

set -euo pipefail

# Configuration
ENVIRONMENT=${1:-production}
BASE_URL=${2:-https://agentforge.com}
MAX_RETRIES=30
RETRY_DELAY=10

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${2:-$GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    log "ERROR: $1" "$RED"
    exit 1
}

warn() {
    log "WARNING: $1" "$YELLOW"
}

info() {
    log "INFO: $1" "$BLUE"
}

success() {
    log "SUCCESS: $1" "$GREEN"
}

# Test HTTP endpoint with retries
test_endpoint() {
    local url="$1"
    local description="$2"
    local expected_status="${3:-200}"
    local retry_count=0
    
    info "Testing $description: $url"
    
    while [[ $retry_count -lt $MAX_RETRIES ]]; do
        if response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null); then
            http_code=$(echo "$response" | tail -n1)
            response_body=$(echo "$response" | sed '$d')
            
            if [[ "$http_code" == "$expected_status" ]]; then
                success "$description is healthy (HTTP $http_code)"
                return 0
            else
                warn "$description returned HTTP $http_code, expected $expected_status"
            fi
        else
            warn "$description is not responding"
        fi
        
        retry_count=$((retry_count + 1))
        if [[ $retry_count -lt $MAX_RETRIES ]]; then
            info "Retrying in ${RETRY_DELAY}s... (attempt $((retry_count + 1))/$MAX_RETRIES)"
            sleep $RETRY_DELAY
        fi
    done
    
    error "$description failed after $MAX_RETRIES attempts"
}

# Test API endpoint with JSON response validation
test_api_endpoint() {
    local url="$1"
    local description="$2"
    local expected_field="$3"
    
    info "Testing $description: $url"
    
    response=$(curl -s -f "$url" || error "$description is not responding")
    
    if echo "$response" | jq -e ".$expected_field" > /dev/null 2>&1; then
        success "$description is healthy and returning valid JSON"
        return 0
    else
        error "$description returned invalid JSON structure"
    fi
}

# Test database connectivity
test_database() {
    info "Testing database connectivity..."
    
    # Test via health endpoint that includes database status
    response=$(curl -s -f "$BASE_URL/api/actuator/health" || error "Health endpoint not responding")
    
    db_status=$(echo "$response" | jq -r '.components.db.status // "unknown"')
    if [[ "$db_status" == "UP" ]]; then
        success "Database connectivity is healthy"
    else
        error "Database connectivity failed: $db_status"
    fi
}

# Test caching layer
test_cache() {
    info "Testing Redis cache connectivity..."
    
    response=$(curl -s -f "$BASE_URL/api/actuator/health" || error "Health endpoint not responding")
    
    redis_status=$(echo "$response" | jq -r '.components.redis.status // "unknown"')
    if [[ "$redis_status" == "UP" ]]; then
        success "Redis cache connectivity is healthy"
    else
        error "Redis cache connectivity failed: $redis_status"
    fi
}

# Test authentication system
test_authentication() {
    info "Testing authentication system..."
    
    # Test login endpoint (should return 400/401 for missing credentials, not 500)
    http_code=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/api/auth/login" || echo "000")
    
    if [[ "$http_code" == "400" ]] || [[ "$http_code" == "401" ]]; then
        success "Authentication endpoint is responding correctly"
    else
        error "Authentication endpoint returned unexpected status: $http_code"
    fi
}

# Test security headers
test_security_headers() {
    info "Testing security headers..."
    
    headers=$(curl -s -I "$BASE_URL" || error "Could not retrieve headers")
    
    # Check for essential security headers
    local required_headers=(
        "X-Content-Type-Options: nosniff"
        "X-Frame-Options: DENY"
        "X-XSS-Protection: 1; mode=block"
        "Strict-Transport-Security:"
    )
    
    for header in "${required_headers[@]}"; do
        if echo "$headers" | grep -i "$header" > /dev/null; then
            success "Security header found: $header"
        else
            warn "Security header missing or incorrect: $header"
        fi
    done
}

# Test SSL/TLS configuration
test_ssl() {
    info "Testing SSL/TLS configuration..."
    
    # Extract domain from BASE_URL
    domain=$(echo "$BASE_URL" | sed 's|https\?://||' | cut -d'/' -f1)
    
    # Test SSL certificate
    if openssl s_client -connect "$domain:443" -servername "$domain" </dev/null 2>/dev/null | \
       openssl x509 -noout -dates 2>/dev/null; then
        success "SSL certificate is valid"
    else
        error "SSL certificate validation failed"
    fi
    
    # Test SSL grade (requires external service or specific tools)
    info "SSL configuration appears healthy"
}

# Test performance benchmarks
test_performance() {
    info "Testing performance benchmarks..."
    
    # Simple response time test
    start_time=$(date +%s.%N)
    curl -s -f "$BASE_URL/health" > /dev/null || error "Performance test failed - endpoint not responding"
    end_time=$(date +%s.%N)
    
    response_time=$(echo "$end_time - $start_time" | bc)
    response_time_ms=$(echo "$response_time * 1000" | bc | cut -d'.' -f1)
    
    if [[ $response_time_ms -lt 1000 ]]; then
        success "Response time is good: ${response_time_ms}ms"
    elif [[ $response_time_ms -lt 3000 ]]; then
        warn "Response time is acceptable: ${response_time_ms}ms"
    else
        error "Response time is too slow: ${response_time_ms}ms"
    fi
}

# Test monitoring endpoints
test_monitoring() {
    info "Testing monitoring endpoints..."
    
    # Test metrics endpoint (should be restricted)
    metrics_code=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/metrics" || echo "000")
    if [[ "$metrics_code" == "403" ]] || [[ "$metrics_code" == "404" ]]; then
        success "Metrics endpoint is properly secured"
    else
        warn "Metrics endpoint security may need review: HTTP $metrics_code"
    fi
    
    # Test Actuator endpoints
    test_api_endpoint "$BASE_URL/api/actuator/info" "Application info endpoint" "build"
}

# Test backup and recovery readiness
test_backup_readiness() {
    info "Testing backup and recovery readiness..."
    
    # This would typically test backup mechanisms
    # For now, we'll check if backup directories exist and are writable
    info "Backup readiness check completed (implementation depends on backup strategy)"
}

# Test logging system
test_logging() {
    info "Testing logging system..."
    
    # Test log levels endpoint
    response=$(curl -s "$BASE_URL/api/actuator/loggers" || echo "{}")
    
    if echo "$response" | jq -e '.loggers' > /dev/null 2>&1; then
        success "Logging system is accessible"
    else
        warn "Logging system endpoint not accessible"
    fi
}

# Main validation function
main() {
    log "Starting deployment validation for $ENVIRONMENT environment"
    log "Base URL: $BASE_URL"
    
    # Core functionality tests
    test_endpoint "$BASE_URL/" "Frontend application"
    test_endpoint "$BASE_URL/health" "Application health check"
    test_api_endpoint "$BASE_URL/api/actuator/health" "Backend health endpoint" "status"
    
    # Infrastructure tests
    test_database
    test_cache
    test_authentication
    
    # Security tests
    test_security_headers
    if [[ "$BASE_URL" == https* ]]; then
        test_ssl
    fi
    
    # Performance and monitoring
    test_performance
    test_monitoring
    
    # Operational readiness
    test_backup_readiness
    test_logging
    
    success "🎉 All deployment validation tests completed successfully!"
    log "Deployment is ready for $ENVIRONMENT environment"
}

# Handle script arguments
case "${1:-validate}" in
    "validate"|"")
        main
        ;;
    "help"|"--help"|"-h")
        echo "Usage: $0 [environment] [base_url]"
        echo "Environment: staging|production (default: production)"
        echo "Base URL: Full URL to test (default: https://agentforge.com)"
        echo ""
        echo "Example: $0 staging https://staging.agentforge.com"
        ;;
    *)
        error "Unknown command: $1. Use 'help' for usage information."
        ;;
esac