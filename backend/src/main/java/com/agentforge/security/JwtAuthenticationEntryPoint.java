package com.agentforge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point for handling authentication errors
 * 
 * This entry point:
 * - Handles unauthorized access attempts
 * - Returns structured JSON error responses
 * - Logs authentication failures for security monitoring
 * - Provides consistent error format across the API
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        // Log the authentication failure
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        
        // Log security event (you might want to use your LoggingService here)
        System.err.printf("[SECURITY] Unauthorized access attempt: %s %s from %s (User-Agent: %s)%n",
                         method, requestURI, ipAddress, userAgent);
        
        // Prepare error response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "UNAUTHORIZED");
        errorResponse.put("message", "Access denied. Please provide valid authentication credentials.");
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        errorResponse.put("method", method);
        
        // Add specific error details based on the request
        if (requestURI.startsWith("/api/admin/")) {
            errorResponse.put("details", "Administrative privileges required for this resource.");
        } else if (requestURI.startsWith("/api/")) {
            errorResponse.put("details", "Authentication required. Please login and provide a valid Bearer token.");
        } else {
            errorResponse.put("details", "This resource requires authentication.");
        }
        
        // Additional security headers
        response.setHeader("WWW-Authenticate", "Bearer realm=\"AgentForge API\"");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        
        // Write JSON response
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Extract client IP address from request
     * Handles various proxy headers to get the real client IP
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (common in load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header (used by some reverse proxies)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Check X-Original-Forwarded-For header
        String xOriginalForwardedFor = request.getHeader("X-Original-Forwarded-For");
        if (xOriginalForwardedFor != null && !xOriginalForwardedFor.isEmpty()) {
            return xOriginalForwardedFor.split(",")[0].trim();
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
}