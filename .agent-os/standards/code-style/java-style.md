# Java/Spring Code Style Standards

## Overview
This document defines the coding standards for Java and Spring Boot development in the AgentForge project, ensuring consistency, maintainability, and adherence to best practices.

## Java Language Standards

### Java Version
- **Target Version**: Java 21 LTS
- **Language Features**: Use modern Java features (records, pattern matching, text blocks)
- **Preview Features**: Avoid preview features in production code

### Naming Conventions
- **Classes**: PascalCase (e.g., `UserService`, `DataProcessor`)
- **Interfaces**: PascalCase, no "I" prefix (e.g., `UserRepository`, not `IUserRepository`)
- **Methods**: camelCase (e.g., `getUserById`, `processData`)
- **Variables**: camelCase (e.g., `userName`, `dataProcessor`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase (e.g., `com.agentforge.service`, `com.agentforge.repository`)

### Code Structure
- **File Organization**: One public class per file, filename matches class name
- **Package Structure**: Follow domain-driven design principles
- **Import Statements**: Organize imports (static, java, javax, org, com)
- **Line Length**: Maximum 120 characters per line

### Documentation
- **JavaDoc**: Required for all public classes, methods, and interfaces
- **Comments**: Explain complex business logic, not obvious code
- **API Documentation**: Use OpenAPI annotations for REST endpoints

## Spring Boot Standards

### Application Structure
- **Main Class**: `@SpringBootApplication` with clear package structure
- **Configuration**: Use `@Configuration` classes for complex configurations
- **Profiles**: Use `@Profile` for environment-specific configurations
- **Properties**: Use `@ConfigurationProperties` for typed configuration

### Controller Layer
- **Annotations**: Use `@RestController` for REST APIs
- **Request Mapping**: Use specific HTTP method annotations (`@GetMapping`, `@PostMapping`)
- **Validation**: Use `@Valid` for request body validation
- **Response**: Use consistent response formats with proper HTTP status codes
- **Exception Handling**: Use `@ControllerAdvice` for global exception handling

### Service Layer
- **Annotations**: Use `@Service` for business logic components
- **Transaction Management**: Use `@Transactional` with appropriate propagation
- **Dependency Injection**: Use constructor injection for required dependencies
- **Interface Segregation**: Define focused service interfaces

### Repository Layer
- **Annotations**: Use `@Repository` for data access components
- **JPA Usage**: Use Spring Data JPA repositories when possible
- **Custom Queries**: Use `@Query` for complex queries
- **Transaction Boundaries**: Keep repository methods focused and atomic

### Entity Design
- **Annotations**: Use appropriate JPA annotations (`@Entity`, `@Table`, `@Column`)
- **Validation**: Use Bean Validation annotations (`@NotNull`, `@Size`, `@Email`)
- **Relationships**: Define clear relationships with proper cascade options
- **Auditing**: Use `@EntityListeners` for audit fields when needed

## Code Quality Standards

### Error Handling
- **Exception Types**: Use appropriate exception types for different scenarios
- **Custom Exceptions**: Create domain-specific exceptions when needed
- **Logging**: Log exceptions with appropriate log levels and context
- **User Messages**: Provide user-friendly error messages

### Logging
- **Framework**: Use SLF4J with Logback
- **Log Levels**: Use appropriate log levels (ERROR, WARN, INFO, DEBUG, TRACE)
- **Structured Logging**: Use MDC for correlation IDs and context
- **Performance**: Avoid string concatenation in log statements

### Testing
- **Framework**: Use JUnit 5 with Mockito
- **Test Naming**: Descriptive test names that explain the scenario
- **Test Structure**: Arrange-Act-Assert pattern
- **Coverage**: Aim for 90%+ code coverage
- **Integration Tests**: Use `@SpringBootTest` for integration testing

### Performance
- **Database Queries**: Optimize queries and use appropriate indexes
- **Caching**: Use Spring Cache for frequently accessed data
- **Async Processing**: Use `@Async` for long-running operations
- **Connection Pooling**: Configure appropriate connection pool sizes

## Security Standards

### Authentication & Authorization
- **Spring Security**: Use Spring Security for authentication and authorization
- **JWT Tokens**: Implement secure JWT token handling
- **Password Security**: Use BCrypt for password hashing
- **Role-Based Access**: Implement proper role-based access control

### Input Validation
- **Bean Validation**: Use Bean Validation annotations for input validation
- **Sanitization**: Sanitize user inputs to prevent injection attacks
- **File Uploads**: Validate file types and sizes
- **SQL Injection**: Use parameterized queries and JPA

### Data Protection
- **Sensitive Data**: Never log sensitive information
- **Encryption**: Encrypt sensitive data at rest and in transit
- **Audit Logging**: Log all security-relevant events
- **Access Control**: Implement principle of least privilege

## Best Practices

### Dependency Management
- **Maven**: Use Maven for dependency management
- **Version Management**: Use dependency management for consistent versions
- **Security Updates**: Regularly update dependencies for security patches
- **Scope**: Use appropriate dependency scopes (compile, runtime, test)

### Configuration Management
- **Environment Variables**: Use environment variables for sensitive configuration
- **Configuration Files**: Use YAML for configuration files
- **Default Values**: Provide sensible default values
- **Validation**: Validate configuration values at startup

### Monitoring & Observability
- **Health Checks**: Implement health check endpoints
- **Metrics**: Use Micrometer for application metrics
- **Tracing**: Implement distributed tracing for microservices
- **Alerting**: Set up appropriate alerting for critical issues

## Code Examples

### Controller Example
```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable @Valid Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

### Service Example
```java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return UserMapper.toResponse(user);
    }
    
    public UserResponse createUser(CreateUserRequest request) {
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }
}
```

### Repository Example
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.active = true AND u.createdAt >= :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);
    
    boolean existsByEmail(String email);
}
```

## Compliance Requirements

### Mandatory Standards
- Follow all naming conventions
- Implement comprehensive error handling
- Use appropriate logging levels
- Include JavaDoc for all public APIs
- Follow Spring Boot best practices

### Code Review Checklist
- [ ] Naming conventions followed
- [ ] Proper exception handling
- [ ] Appropriate logging
- [ ] JavaDoc included
- [ ] Tests written and passing
- [ ] Security considerations addressed
- [ ] Performance optimized
- [ ] Error messages user-friendly

### Automated Checks
- Use Checkstyle for code style validation
- Use SpotBugs for bug detection
- Use PMD for code quality analysis
- Use JaCoCo for code coverage reporting
- Use OWASP dependency check for security vulnerabilities
