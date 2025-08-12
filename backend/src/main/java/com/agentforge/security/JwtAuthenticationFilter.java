package com.agentforge.security;

import com.agentforge.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter for processing JWT tokens in requests
 * 
 * This filter:
 * - Extracts JWT tokens from Authorization headers
 * - Validates tokens using AuthenticationService
 * - Sets up Spring Security authentication context
 * - Handles token extraction and validation errors
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    public JwtAuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Validate token
                Claims claims = authenticationService.validateToken(jwt);
                
                if (claims != null) {
                    // Extract user information from claims
                    String userId = claims.getSubject();
                    String username = claims.get("username", String.class);
                    String role = claims.get("role", String.class);
                    String tokenType = claims.get("type", String.class);
                    
                    // Only process access tokens for authentication
                    if ("access".equals(tokenType)) {
                        // Create authorities based on user role
                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                        );
                        
                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        
                        // Set additional details
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Add user information to request attributes for easy access
                        request.setAttribute("userId", userId);
                        request.setAttribute("username", username);
                        request.setAttribute("userRole", role);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot set user authentication: {}", ex);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * @param request HTTP request
     * @return JWT token or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Skip filter for certain paths to improve performance
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip filter for public endpoints
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/refresh") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/actuator/info") ||
               path.startsWith("/actuator/prometheus") ||
               path.startsWith("/static/") ||
               path.equals("/favicon.ico") ||
               path.equals("/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
    }
}