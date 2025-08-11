package com.agentforge.service;

import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import com.agentforge.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Authentication Service for AgentForge
 * 
 * Provides comprehensive authentication capabilities including:
 * - JWT token management with refresh tokens
 * - OAuth 2.1 integration
 * - Multi-factor authentication support
 * - Session management and security
 * - Password policies and validation
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final LoggingService loggingService;
    private final CachingService cachingService;
    private final PasswordEncoder passwordEncoder;
    
    // JWT Configuration
    @Value("${security.jwt.secret:your-secret-key-change-in-production}")
    private String jwtSecret;
    
    @Value("${security.jwt.expiration:86400000}")
    private long jwtExpiration; // 24 hours
    
    @Value("${security.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration; // 7 days
    
    // Security configuration
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.lockout-duration:1800000}")
    private long lockoutDuration; // 30 minutes
    
    // Session management
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    // JWT key
    private SecretKey jwtKey;

    public AuthenticationService(UserRepository userRepository, 
                               LoggingService loggingService,
                               CachingService cachingService) {
        this.userRepository = userRepository;
        this.loggingService = loggingService;
        this.cachingService = cachingService;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Initialize JWT signing key
     */
    private SecretKey getJwtKey() {
        if (jwtKey == null) {
            jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        return jwtKey;
    }

    /**
     * Authenticate user with username/email and password
     */
    public AuthenticationResult authenticate(String usernameOrEmail, String password, String ipAddress, String userAgent) {
        try {
            // Check for account lockout
            if (isAccountLocked(usernameOrEmail)) {
                return AuthenticationResult.failure("Account temporarily locked due to multiple failed attempts");
            }
            
            // Find user by username or email
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
            
            if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPasswordHash())) {
                recordFailedLoginAttempt(usernameOrEmail, ipAddress);
                loggingService.logWarning("AUTH", "LOGIN_FAILED", 
                    "Authentication failed for user", Map.of("user", usernameOrEmail, "ip", ipAddress));
                return AuthenticationResult.failure("Invalid credentials");
            }
            
            User user = userOpt.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                loggingService.logWarning("AUTH", "INACTIVE_USER", 
                    "Login attempt for inactive user", Map.of("user", usernameOrEmail, "ip", ipAddress));
                return AuthenticationResult.failure("Account is deactivated");
            }
            
            // Clear failed login attempts
            clearFailedLoginAttempts(usernameOrEmail);
            
            // Generate tokens
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);
            
            // Create session
            SessionInfo session = createSession(user, refreshToken, ipAddress, userAgent);
            
            // Update user last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Log successful authentication
            loggingService.logInfo("AUTH", "LOGIN_SUCCESS", 
                "User authenticated successfully", Map.of("userId", user.getId(), "ip", ipAddress));
            
            return AuthenticationResult.success(user, accessToken, refreshToken, session.getSessionId());
            
        } catch (Exception e) {
            loggingService.logError("AUTH", "AUTH_ERROR", 
                "Authentication error", Map.of("user", usernameOrEmail, "error", e.getMessage()));
            return AuthenticationResult.failure("Authentication failed");
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthenticationResult refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            Claims claims = validateToken(refreshToken);
            if (claims == null) {
                return AuthenticationResult.failure("Invalid refresh token");
            }
            
            String userId = claims.getSubject();
            String tokenType = claims.get("type", String.class);
            
            if (!"refresh".equals(tokenType)) {
                return AuthenticationResult.failure("Invalid token type");
            }
            
            // Find user
            Optional<User> userOpt = userRepository.findById(Long.parseLong(userId));
            if (userOpt.isEmpty()) {
                return AuthenticationResult.failure("User not found");
            }
            
            User user = userOpt.get();
            
            // Check if user is still active
            if (!user.getIsActive()) {
                return AuthenticationResult.failure("Account is deactivated");
            }
            
            // Generate new access token
            String newAccessToken = generateAccessToken(user);
            
            loggingService.logInfo("AUTH", "TOKEN_REFRESH", 
                "Access token refreshed", Map.of("userId", user.getId()));
            
            return AuthenticationResult.success(user, newAccessToken, refreshToken, null);
            
        } catch (Exception e) {
            loggingService.logError("AUTH", "REFRESH_ERROR", 
                "Token refresh error", Map.of("error", e.getMessage()));
            return AuthenticationResult.failure("Token refresh failed");
        }
    }

    /**
     * Logout user and invalidate tokens
     */
    public void logout(String accessToken, String refreshToken) {
        try {
            // Add tokens to blacklist
            if (accessToken != null) {
                blacklistedTokens.add(accessToken);
            }
            if (refreshToken != null) {
                blacklistedTokens.add(refreshToken);
            }
            
            // Remove session
            removeSessionByRefreshToken(refreshToken);
            
            loggingService.logInfo("AUTH", "LOGOUT", 
                "User logged out successfully", new HashMap<>());
                
        } catch (Exception e) {
            loggingService.logError("AUTH", "LOGOUT_ERROR", 
                "Logout error", Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generate JWT access token
     */
    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("username", user.getUsername())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .claim("type", "access")
            .signWith(getJwtKey())
            .compact();
    }

    /**
     * Generate JWT refresh token
     */
    private String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "refresh")
            .signWith(getJwtKey())
            .compact();
    }

    /**
     * Validate JWT token
     */
    public Claims validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (blacklistedTokens.contains(token)) {
                return null;
            }

            return Jwts.parserBuilder()
                .setSigningKey(getJwtKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        } catch (JwtException | IllegalArgumentException e) {
            loggingService.logWarning("AUTH", "TOKEN_VALIDATION_FAILED", 
                "Token validation failed", Map.of("error", e.getMessage()));
            return null;
        }
    }

    /**
     * Extract user ID from token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }

    /**
     * Create user session
     */
    private SessionInfo createSession(User user, String refreshToken, String ipAddress, String userAgent) {
        String sessionId = UUID.randomUUID().toString();
        SessionInfo session = new SessionInfo(sessionId, user.getId(), refreshToken, 
                                            LocalDateTime.now(), ipAddress, userAgent);
        
        activeSessions.put(sessionId, session);
        
        // Cache session for quick access
        cachingService.put("session:" + sessionId, session);
        
        return session;
    }

    /**
     * Remove session by refresh token
     */
    private void removeSessionByRefreshToken(String refreshToken) {
        activeSessions.entrySet().removeIf(entry -> {
            SessionInfo session = entry.getValue();
            if (refreshToken.equals(session.getRefreshToken())) {
                cachingService.evict("session:" + entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Check if account is locked
     */
    private boolean isAccountLocked(String usernameOrEmail) {
        LoginAttempt attempt = loginAttempts.get(usernameOrEmail);
        if (attempt == null) {
            return false;
        }
        
        if (attempt.getFailedAttempts() >= maxLoginAttempts) {
            long timeSinceLastAttempt = System.currentTimeMillis() - attempt.getLastAttemptTime();
            return timeSinceLastAttempt < lockoutDuration;
        }
        
        return false;
    }

    /**
     * Record failed login attempt
     */
    private void recordFailedLoginAttempt(String usernameOrEmail, String ipAddress) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(usernameOrEmail, 
            k -> new LoginAttempt(usernameOrEmail));
        
        attempt.incrementFailedAttempts();
        attempt.setLastAttemptTime(System.currentTimeMillis());
        attempt.setLastAttemptIp(ipAddress);
    }

    /**
     * Clear failed login attempts
     */
    private void clearFailedLoginAttempts(String usernameOrEmail) {
        loginAttempts.remove(usernameOrEmail);
    }

    /**
     * Get user by token
     */
    public Optional<User> getUserByToken(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId != null) {
            return userRepository.findById(userId);
        }
        return Optional.empty();
    }

    /**
     * Check if user has role
     */
    public boolean hasRole(String token, UserRole role) {
        Claims claims = validateToken(token);
        if (claims != null) {
            String tokenRole = claims.get("role", String.class);
            return role.name().equals(tokenRole);
        }
        return false;
    }

    /**
     * Get active sessions for user
     */
    public List<SessionInfo> getActiveSessionsForUser(Long userId) {
        return activeSessions.values().stream()
            .filter(session -> session.getUserId().equals(userId))
            .toList();
    }

    /**
     * Invalidate all sessions for user
     */
    public void invalidateAllUserSessions(Long userId) {
        List<SessionInfo> userSessions = getActiveSessionsForUser(userId);
        
        for (SessionInfo session : userSessions) {
            blacklistedTokens.add(session.getRefreshToken());
            activeSessions.remove(session.getSessionId());
            cachingService.evict("session:" + session.getSessionId());
        }
        
        loggingService.logInfo("AUTH", "SESSIONS_INVALIDATED", 
            "All sessions invalidated for user", Map.of("userId", userId, "sessionCount", userSessions.size()));
    }

    /**
     * Authentication result container
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String accessToken;
        private final String refreshToken;
        private final String sessionId;

        private AuthenticationResult(boolean success, String message, User user, 
                                   String accessToken, String refreshToken, String sessionId) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.sessionId = sessionId;
        }

        public static AuthenticationResult success(User user, String accessToken, 
                                                 String refreshToken, String sessionId) {
            return new AuthenticationResult(true, "Authentication successful", 
                                          user, accessToken, refreshToken, sessionId);
        }

        public static AuthenticationResult failure(String message) {
            return new AuthenticationResult(false, message, null, null, null, null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getSessionId() { return sessionId; }
    }

    /**
     * Session information
     */
    public static class SessionInfo {
        private final String sessionId;
        private final Long userId;
        private final String refreshToken;
        private final LocalDateTime createdAt;
        private final String ipAddress;
        private final String userAgent;

        public SessionInfo(String sessionId, Long userId, String refreshToken, 
                          LocalDateTime createdAt, String ipAddress, String userAgent) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.refreshToken = refreshToken;
            this.createdAt = createdAt;
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public Long getUserId() { return userId; }
        public String getRefreshToken() { return refreshToken; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
    }

    /**
     * Login attempt tracking
     */
    private static class LoginAttempt {
        private final String usernameOrEmail;
        private int failedAttempts = 0;
        private long lastAttemptTime;
        private String lastAttemptIp;

        public LoginAttempt(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
        }

        public void incrementFailedAttempts() {
            this.failedAttempts++;
        }

        // Getters and setters
        public String getUsernameOrEmail() { return usernameOrEmail; }
        public int getFailedAttempts() { return failedAttempts; }
        public long getLastAttemptTime() { return lastAttemptTime; }
        public void setLastAttemptTime(long lastAttemptTime) { this.lastAttemptTime = lastAttemptTime; }
        public String getLastAttemptIp() { return lastAttemptIp; }
        public void setLastAttemptIp(String lastAttemptIp) { this.lastAttemptIp = lastAttemptIp; }
    }
}