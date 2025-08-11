package com.agentforge.controller;

import com.agentforge.service.AuthenticationService;
import com.agentforge.service.AuthenticationService.AuthenticationResult;
import com.agentforge.service.AuthenticationService.SessionInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Authentication Management
 * 
 * Provides endpoints for:
 * - User authentication (login/logout)
 * - Token management (refresh, validation)
 * - Session management
 * - OAuth 2.1 integration
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate user and return JWT tokens
     * 
     * @param loginRequest Login credentials
     * @param request HTTP request for IP address extraction
     * @return Authentication result with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        AuthenticationResult result = authenticationService.authenticate(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword(),
            ipAddress,
            userAgent
        );
        
        if (result.isSuccess()) {
            Map<String, Object> response = Map.of(
                "status", "SUCCESS",
                "message", result.getMessage(),
                "user", Map.of(
                    "id", result.getUser().getId(),
                    "username", result.getUser().getUsername(),
                    "email", result.getUser().getEmail(),
                    "role", result.getUser().getRole().name()
                ),
                "accessToken", result.getAccessToken(),
                "refreshToken", result.getRefreshToken(),
                "sessionId", result.getSessionId()
            );
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = Map.of(
                "status", "FAILED",
                "message", result.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param refreshRequest Refresh token request
     * @return New access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @RequestBody RefreshTokenRequest refreshRequest) {
        
        AuthenticationResult result = authenticationService.refreshToken(
            refreshRequest.getRefreshToken()
        );
        
        if (result.isSuccess()) {
            Map<String, Object> response = Map.of(
                "status", "SUCCESS",
                "message", "Token refreshed successfully",
                "accessToken", result.getAccessToken(),
                "user", Map.of(
                    "id", result.getUser().getId(),
                    "username", result.getUser().getUsername(),
                    "role", result.getUser().getRole().name()
                )
            );
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = Map.of(
                "status", "FAILED",
                "message", result.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Logout user and invalidate tokens
     * 
     * @param logoutRequest Logout request with tokens
     * @return Logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestBody LogoutRequest logoutRequest) {
        
        authenticationService.logout(
            logoutRequest.getAccessToken(),
            logoutRequest.getRefreshToken()
        );
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "Logout successful"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Validate access token
     * 
     * @param token Access token to validate
     * @return Token validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam String token) {
        
        var claims = authenticationService.validateToken(token);
        boolean isValid = claims != null;
        
        Map<String, Object> response = Map.of(
            "valid", isValid,
            "message", isValid ? "Token is valid" : "Token is invalid or expired"
        );
        
        if (isValid) {
            response = Map.of(
                "valid", true,
                "message", "Token is valid",
                "userId", claims.getSubject(),
                "username", claims.get("username", String.class),
                "role", claims.get("role", String.class),
                "expiresAt", claims.getExpiration()
            );
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user information from token
     * 
     * @param authorization Authorization header with Bearer token
     * @return Current user information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader("Authorization") String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Missing or invalid authorization header"));
        }
        
        var userOpt = authenticationService.getUserByToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token or user not found"));
        }
        
        var user = userOpt.get();
        Map<String, Object> response = Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().name(),
            "isActive", user.getIsActive(),
            "createdAt", user.getCreatedAt(),
            "lastLogin", user.getLastLogin()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active sessions for current user
     * 
     * @param authorization Authorization header with Bearer token
     * @return List of active sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getActiveSessions(
            @RequestHeader("Authorization") String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Missing or invalid authorization header"));
        }
        
        Long userId = authenticationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token"));
        }
        
        List<SessionInfo> sessions = authenticationService.getActiveSessionsForUser(userId);
        
        List<Map<String, Object>> sessionData = sessions.stream()
            .map(session -> Map.of(
                "sessionId", session.getSessionId(),
                "createdAt", session.getCreatedAt(),
                "ipAddress", session.getIpAddress(),
                "userAgent", session.getUserAgent() != null ? session.getUserAgent() : "Unknown"
            ))
            .toList();
        
        Map<String, Object> response = Map.of(
            "sessions", sessionData,
            "totalSessions", sessionData.size()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Invalidate all sessions for current user
     * 
     * @param authorization Authorization header with Bearer token
     * @return Session invalidation confirmation
     */
    @PostMapping("/sessions/invalidate-all")
    public ResponseEntity<Map<String, Object>> invalidateAllSessions(
            @RequestHeader("Authorization") String authorization) {
        
        String token = extractTokenFromHeader(authorization);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Missing or invalid authorization header"));
        }
        
        Long userId = authenticationService.getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token"));
        }
        
        authenticationService.invalidateAllUserSessions(userId);
        
        Map<String, Object> response = Map.of(
            "status", "SUCCESS",
            "message", "All sessions invalidated successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extract token from Authorization header
     */
    private String extractTokenFromHeader(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    /**
     * Login request DTO
     */
    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;

        // Constructors
        public LoginRequest() {}

        public LoginRequest(String usernameOrEmail, String password) {
            this.usernameOrEmail = usernameOrEmail;
            this.password = password;
        }

        // Getters and setters
        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Refresh token request DTO
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        // Constructors
        public RefreshTokenRequest() {}

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        // Getters and setters
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    /**
     * Logout request DTO
     */
    public static class LogoutRequest {
        private String accessToken;
        private String refreshToken;

        // Constructors
        public LogoutRequest() {}

        public LogoutRequest(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        // Getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}