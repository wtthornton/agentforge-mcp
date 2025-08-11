package com.agentforge.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database Configuration for AgentForge
 * 
 * This class configures:
 * - PostgreSQL connection with HikariCP connection pooling
 * - JPA/Hibernate configuration with optimized settings
 * - Transaction management
 * - pgvector support for vector operations
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.agentforge.repository")
public class DatabaseConfig {

    @Autowired
    private DatabaseHealthConfig.DatabaseConnectionValidator connectionValidator;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;

    /**
     * Configure HikariCP DataSource with optimized settings for AgentForge
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(databaseUrl);
        config.setUsername(databaseUsername);
        config.setPassword(databasePassword);
        config.setDriverClassName(driverClassName);
        
        // Connection pool optimization
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // Performance optimizations
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setValidationTimeout(10000); // 10 seconds for Docker environment
        config.setConnectionTestQuery("SELECT 1");
        
        // Docker environment optimizations
        config.setInitializationFailTimeout(-1); // Don't fail on startup
        config.setConnectionInitSql("SELECT 1");
        
        // PostgreSQL specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // pgvector support
        config.addDataSourceProperty("stringtype", "unspecified");
        
        // Additional PostgreSQL connection properties for better compatibility
        config.addDataSourceProperty("ApplicationName", "AgentForge");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("socketTimeout", "30");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        // Wait for database to be ready in Docker environment
        try {
            System.out.println("Waiting for database connection...");
            System.out.println("Database URL: " + databaseUrl);
            System.out.println("Database User: " + databaseUsername);
            System.out.println("Database Password: " + (databasePassword != null ? "***" : "null"));
            
            connectionValidator.waitForDatabase(dataSource, 60000); // Wait up to 60 seconds
            System.out.println("Database connection established successfully!");
        } catch (Exception e) {
            // Log the error but don't fail startup
            System.err.println("Warning: Database connection failed during startup: " + e.getMessage());
            System.err.println("Application will continue and retry connections as needed.");
            System.err.println("Please check:");
            System.err.println("1. Database container is running: docker ps | grep postgres");
            System.err.println("2. Database credentials are correct");
            System.err.println("3. Database port 5432 is accessible");
            System.err.println("4. Database user 'agentforge' exists and has proper permissions");
        }
        
        return dataSource;
    }

    /**
     * Configure EntityManagerFactory with Hibernate optimizations
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.agentforge.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // We use Flyway migrations
        vendorAdapter.setShowSql(false); // Controlled via application.yml
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        
        // Hibernate performance optimizations
        properties.setProperty("hibernate.jdbc.batch_size", "50");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        // Second-level cache configuration
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.cache.region.factory_class", 
            "org.hibernate.cache.jcache.JCacheRegionFactory");
        
        // Connection pool validation
        properties.setProperty("hibernate.connection.provider_class", 
            "org.hibernate.hikari.internal.HikariConnectionProvider");
        
        // SQL logging (controlled via application.yml)
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "false");
        
        // Statistics and monitoring
        properties.setProperty("hibernate.generate_statistics", "false");
        properties.setProperty("hibernate.session.events.log", "false");
        
        em.setJpaProperties(properties);
        
        return em;
    }

    /**
     * Configure TransactionManager for JPA operations
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
