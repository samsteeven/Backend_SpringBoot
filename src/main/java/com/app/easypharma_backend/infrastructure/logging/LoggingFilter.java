package com.app.easypharma_backend.infrastructure.logging;

import com.app.easypharma_backend.infrastructure.security.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to populate MDC (Mapped Diagnostic Context) with user information
 * for enhanced logging with user ID and client IP address.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingFilter implements Filter {

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // Extract and set client IP address
            String clientIp = extractClientIp(httpRequest);
            MDC.put("clientIp", clientIp);

            // Extract and set user ID from JWT token if present
            String userId = extractUserId(httpRequest);
            MDC.put("userId", userId != null ? userId : "anonymous");

            // Continue with the filter chain
            chain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /**
     * Extract client IP address from request, considering proxy headers
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If multiple IPs (comma-separated), take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }

    /**
     * Extract user ID from JWT token in Authorization header
     */
    private String extractUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtService.extractEmail(token);

                // You could also extract user ID directly if it's in the JWT claims
                // For now, we'll use email as identifier
                return email;
            }
        } catch (Exception e) {
            // Silent fail - user is not authenticated or token is invalid
            log.debug("Could not extract user from token: {}", e.getMessage());
        }

        return null;
    }
}
