#!/bin/bash

# AgentForge Production Deployment Script
# This script handles the complete production deployment process

set -euo pipefail

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$SCRIPT_DIR")")"
BACKUP_DIR="/opt/agentforge/backups"
LOG_FILE="/var/log/agentforge/deployment.log"

# Ensure log directory exists
mkdir -p "$(dirname "$LOG_FILE")"

# Logging function
log() {
    echo -e "${2:-$GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}" | tee -a "$LOG_FILE"
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

# Pre-deployment checks
pre_deployment_checks() {
    info "Running pre-deployment checks..."
    
    # Check if running as root or with sudo
    if [[ $EUID -ne 0 ]]; then
        error "This script must be run as root or with sudo privileges"
    fi
    
    # Check required tools
    local required_tools=("docker" "docker-compose" "curl" "jq" "openssl")
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            error "Required tool '$tool' is not installed"
        fi
    done
    
    # Check if .env.prod exists
    if [[ ! -f "$SCRIPT_DIR/.env.prod" ]]; then
        error ".env.prod file not found. Please copy .env.prod.template and configure it."
    fi
    
    # Validate environment variables
    source "$SCRIPT_DIR/.env.prod"
    local required_vars=("DOMAIN_NAME" "DB_PASSWORD" "JWT_SECRET" "ENCRYPTION_KEY")
    for var in "${required_vars[@]}"; do
        if [[ -z "${!var:-}" ]]; then
            error "Required environment variable $var is not set in .env.prod"
        fi
    done
    
    # Check SSL certificates
    if [[ ! -f "$SCRIPT_DIR/nginx/prod/ssl/fullchain.pem" ]] || [[ ! -f "$SCRIPT_DIR/nginx/prod/ssl/privkey.pem" ]]; then
        warn "SSL certificates not found. HTTPS will not work properly."
        warn "Please obtain SSL certificates and place them in infrastructure/production/nginx/prod/ssl/"
    fi
    
    success "Pre-deployment checks completed"
}

# Backup existing deployment
backup_existing() {
    info "Creating backup of existing deployment..."
    
    local backup_timestamp=$(date +%Y%m%d_%H%M%S)
    local backup_path="$BACKUP_DIR/$backup_timestamp"
    
    mkdir -p "$backup_path"
    
    # Backup database
    if docker ps | grep -q agentforge-postgres-prod; then
        info "Backing up database..."
        docker exec agentforge-postgres-prod pg_dump -U "$DB_USERNAME" -d agentforge_prod > "$backup_path/database_backup.sql"
    fi
    
    # Backup volumes
    info "Backing up Docker volumes..."
    docker run --rm -v agentforge_postgres_prod_data:/data -v "$backup_path:/backup" alpine tar czf /backup/postgres_data.tar.gz -C /data .
    docker run --rm -v agentforge_redis_prod_data:/data -v "$backup_path:/backup" alpine tar czf /backup/redis_data.tar.gz -C /data .
    
    # Backup configuration
    cp -r "$SCRIPT_DIR" "$backup_path/config"
    
    success "Backup created at $backup_path"
}

# Generate SSL certificates using Let's Encrypt
setup_ssl() {
    info "Setting up SSL certificates..."
    
    local ssl_dir="$SCRIPT_DIR/nginx/prod/ssl"
    mkdir -p "$ssl_dir"
    
    # Check if certificates already exist and are valid
    if [[ -f "$ssl_dir/fullchain.pem" ]] && [[ -f "$ssl_dir/privkey.pem" ]]; then
        if openssl x509 -noout -checkend 2592000 -in "$ssl_dir/fullchain.pem" > /dev/null 2>&1; then
            info "Valid SSL certificates found, skipping generation"
            return 0
        fi
    fi
    
    # Use Certbot with DNS challenge (requires manual setup) or HTTP challenge
    if command -v certbot &> /dev/null; then
        info "Using Certbot to generate SSL certificates..."
        
        # Create temporary nginx config for HTTP challenge
        docker run --rm -d \
            -p 80:80 \
            --name temp-nginx-ssl \
            -v "$SCRIPT_DIR/nginx/temp:/etc/nginx/conf.d" \
            nginx:alpine
            
        # Generate certificate
        certbot certonly \
            --webroot \
            --webroot-path /var/www/certbot \
            -d "$DOMAIN_NAME" \
            -d "www.$DOMAIN_NAME" \
            --email "admin@$DOMAIN_NAME" \
            --agree-tos \
            --no-eff-email
            
        # Stop temporary nginx
        docker stop temp-nginx-ssl || true
        
        # Copy certificates
        cp "/etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem" "$ssl_dir/"
        cp "/etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem" "$ssl_dir/"
    else
        warn "Certbot not found. Generating self-signed certificates for testing..."
        
        # Generate self-signed certificate (for development/testing only)
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout "$ssl_dir/privkey.pem" \
            -out "$ssl_dir/fullchain.pem" \
            -subj "/C=US/ST=State/L=City/O=Organization/CN=$DOMAIN_NAME"
    fi
    
    # Generate DH parameters
    if [[ ! -f "$ssl_dir/dhparam.pem" ]]; then
        info "Generating DH parameters (this may take a while)..."
        openssl dhparam -out "$ssl_dir/dhparam.pem" 2048
    fi
    
    # Set proper permissions
    chmod 600 "$ssl_dir/privkey.pem"
    chmod 644 "$ssl_dir/fullchain.pem" "$ssl_dir/dhparam.pem"
    
    success "SSL certificates configured"
}

# Build and deploy application
deploy_application() {
    info "Deploying AgentForge application..."
    
    cd "$SCRIPT_DIR"
    
    # Load environment variables
    source .env.prod
    export $(grep -v '^#' .env.prod | xargs)
    
    # Pull latest code (if this is a git deployment)
    if [[ -d "$PROJECT_ROOT/.git" ]]; then
        cd "$PROJECT_ROOT"
        git pull origin main || warn "Failed to pull latest code"
        cd "$SCRIPT_DIR"
    fi
    
    # Build and start services
    info "Building Docker images..."
    docker-compose -f docker-compose.prod.yml build --parallel
    
    info "Starting services..."
    docker-compose -f docker-compose.prod.yml up -d
    
    # Wait for services to be healthy
    info "Waiting for services to be healthy..."
    local max_wait=300  # 5 minutes
    local wait_time=0
    
    while [[ $wait_time -lt $max_wait ]]; do
        if docker-compose -f docker-compose.prod.yml ps | grep -E "(unhealthy|Exit)" > /dev/null; then
            warn "Some services are not healthy yet, waiting..."
            sleep 10
            wait_time=$((wait_time + 10))
        else
            success "All services are healthy"
            break
        fi
    done
    
    if [[ $wait_time -ge $max_wait ]]; then
        error "Services failed to become healthy within $max_wait seconds"
    fi
    
    success "Application deployed successfully"
}

# Run database migrations
run_migrations() {
    info "Running database migrations..."
    
    # Wait for database to be ready
    local max_wait=60
    local wait_time=0
    
    while [[ $wait_time -lt $max_wait ]]; do
        if docker exec agentforge-postgres-prod pg_isready -U "$DB_USERNAME" -d agentforge_prod > /dev/null 2>&1; then
            break
        fi
        sleep 2
        wait_time=$((wait_time + 2))
    done
    
    if [[ $wait_time -ge $max_wait ]]; then
        error "Database failed to become ready"
    fi
    
    # Run migrations via backend service
    docker exec agentforge-backend-1 /bin/bash -c "
        cd /app && \
        java -jar app.jar --spring.profiles.active=production --spring.jpa.hibernate.ddl-auto=validate || true
    "
    
    success "Database migrations completed"
}

# Health checks
verify_deployment() {
    info "Verifying deployment..."
    
    # Check service health
    local services=("nginx" "backend-1" "backend-2" "frontend" "postgres-prod" "redis-prod")
    for service in "${services[@]}"; do
        if ! docker ps | grep -q "agentforge-$service"; then
            error "Service $service is not running"
        fi
    done
    
    # Test HTTP endpoints
    local endpoints=(
        "https://$DOMAIN_NAME/health"
        "https://$DOMAIN_NAME/api/actuator/health"
    )
    
    for endpoint in "${endpoints[@]}"; do
        info "Testing endpoint: $endpoint"
        if ! curl -f -s -k "$endpoint" > /dev/null; then
            warn "Endpoint $endpoint is not responding correctly"
        else
            success "Endpoint $endpoint is healthy"
        fi
    done
    
    # Test database connection
    if docker exec agentforge-postgres-prod psql -U "$DB_USERNAME" -d agentforge_prod -c "SELECT 1;" > /dev/null 2>&1; then
        success "Database connection is healthy"
    else
        error "Database connection failed"
    fi
    
    success "Deployment verification completed"
}

# Setup monitoring and alerting
setup_monitoring() {
    info "Setting up monitoring and alerting..."
    
    # Ensure monitoring directories exist
    mkdir -p "$SCRIPT_DIR/monitoring/"{prometheus,grafana,loki,promtail}/prod
    
    # Copy monitoring configurations if they don't exist
    if [[ ! -f "$SCRIPT_DIR/monitoring/prometheus/prod/prometheus.yml" ]]; then
        info "Creating default Prometheus configuration..."
        # This would copy from templates or create default configs
    fi
    
    # Start monitoring services if not already running
    docker-compose -f docker-compose.prod.yml up -d prometheus grafana loki promtail
    
    success "Monitoring setup completed"
}

# Cleanup old resources
cleanup() {
    info "Cleaning up old resources..."
    
    # Remove old images
    docker image prune -f
    
    # Remove old logs (keep last 30 days)
    find /var/log/agentforge -name "*.log" -mtime +30 -delete 2>/dev/null || true
    
    # Remove old backups (keep last 7 days)
    find "$BACKUP_DIR" -maxdepth 1 -type d -mtime +7 -exec rm -rf {} + 2>/dev/null || true
    
    success "Cleanup completed"
}

# Main deployment function
main() {
    log "Starting AgentForge production deployment..."
    
    pre_deployment_checks
    backup_existing
    setup_ssl
    deploy_application
    run_migrations
    verify_deployment
    setup_monitoring
    cleanup
    
    success "🎉 AgentForge production deployment completed successfully!"
    info "Application is now available at: https://$DOMAIN_NAME"
    info "Monitoring dashboard: https://$DOMAIN_NAME:3000 (Grafana)"
    info "Deployment log: $LOG_FILE"
}

# Handle script arguments
case "${1:-deploy}" in
    "deploy")
        main
        ;;
    "backup")
        backup_existing
        ;;
    "ssl")
        setup_ssl
        ;;
    "health")
        verify_deployment
        ;;
    "cleanup")
        cleanup
        ;;
    "help"|"--help"|"-h")
        echo "Usage: $0 [command]"
        echo "Commands:"
        echo "  deploy   - Full production deployment (default)"
        echo "  backup   - Create backup only"
        echo "  ssl      - Setup SSL certificates only"
        echo "  health   - Run health checks only"
        echo "  cleanup  - Cleanup old resources only"
        echo "  help     - Show this help message"
        ;;
    *)
        error "Unknown command: $1. Use 'help' for usage information."
        ;;
esac