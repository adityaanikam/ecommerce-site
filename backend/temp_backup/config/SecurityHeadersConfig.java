package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Security Headers and Rate Limiting Configuration
 * 
 * This configuration adds security headers and implements rate limiting
 * to protect the API from abuse and security vulnerabilities.
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Security Headers Filter
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * Rate Limiting Filter
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    /**
     * Security Headers Filter Implementation
     */
    public static class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Security Headers
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
            
            // Content Security Policy
            response.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' https:; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'");
            
            // Strict Transport Security (HTTPS only)
            if (request.isSecure()) {
                response.setHeader("Strict-Transport-Security", 
                    "max-age=31536000; includeSubDomains; preload");
            }
            
            // Remove server information
            response.setHeader("Server", "E-commerce-API");
            
            // Cache control for API responses
            if (request.getRequestURI().startsWith("/api/")) {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
            
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Rate Limiting Filter Implementation
     */
    public static class RateLimitingFilter extends OncePerRequestFilter {

        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RateLimitingFilter.class);
        
        // Rate limiting storage
        private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
        
        // Rate limiting configuration
        private static final int DEFAULT_RATE_LIMIT = 100; // requests per minute
        private static final int AUTH_RATE_LIMIT = 10; // auth requests per minute
        private static final int SEARCH_RATE_LIMIT = 50; // search requests per minute
        private static final long WINDOW_SIZE = 60 * 1000; // 1 minute in milliseconds

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String clientId = getClientId(request);
            String endpoint = request.getRequestURI();
            
            // Get rate limit for endpoint
            int rateLimit = getRateLimitForEndpoint(endpoint);
            
            // Check rate limit
            if (isRateLimited(clientId, rateLimit)) {
                response.setStatus(429); // Too Many Requests
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}");
                return;
            }
            
            // Add rate limit headers
            RateLimitInfo rateLimitInfo = rateLimitMap.get(clientId);
            if (rateLimitInfo != null) {
                response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit));
                response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimit - rateLimitInfo.getCount()));
                response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitInfo.getResetTime()));
            }
            
            filterChain.doFilter(request, response);
        }

        private String getClientId(HttpServletRequest request) {
            // Use IP address as client identifier
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }

        private int getRateLimitForEndpoint(String endpoint) {
            if (endpoint.contains("/auth/") || endpoint.contains("/login") || endpoint.contains("/register")) {
                return AUTH_RATE_LIMIT;
            } else if (endpoint.contains("/search") || endpoint.contains("/products")) {
                return SEARCH_RATE_LIMIT;
            }
            return DEFAULT_RATE_LIMIT;
        }

        private boolean isRateLimited(String clientId, int rateLimit) {
            long currentTime = System.currentTimeMillis();
            
            RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientId, 
                k -> new RateLimitInfo(currentTime + WINDOW_SIZE));
            
            // Reset if window has expired
            if (currentTime > rateLimitInfo.getResetTime()) {
                rateLimitInfo.reset(currentTime + WINDOW_SIZE);
            }
            
            // Check if rate limit exceeded
            if (rateLimitInfo.getCount() >= rateLimit) {
                logger.warn("Rate limit exceeded for client: {} on endpoint: {}", clientId, rateLimit);
                return true;
            }
            
            // Increment counter
            rateLimitInfo.increment();
            return false;
        }

        /**
         * Rate limit information holder
         */
        private static class RateLimitInfo {
            private final AtomicInteger count = new AtomicInteger(0);
            private final AtomicLong resetTime = new AtomicLong();

            public RateLimitInfo(long resetTime) {
                this.resetTime.set(resetTime);
            }

            public void reset(long newResetTime) {
                count.set(0);
                resetTime.set(newResetTime);
            }

            public int getCount() {
                return count.get();
            }

            public void increment() {
                count.incrementAndGet();
            }

            public long getResetTime() {
                return resetTime.get();
            }
        }
    }

    /**
     * CORS Configuration for additional security
     */
    @Bean
    public FilterRegistrationBean<CorsSecurityFilter> corsSecurityFilter() {
        FilterRegistrationBean<CorsSecurityFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CorsSecurityFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    /**
     * CORS Security Filter
     */
    public static class CorsSecurityFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String origin = request.getHeader("Origin");
            
            // Allow specific origins
            if (isAllowedOrigin(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }
            
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", 
                "Content-Type, Authorization, X-Requested-With, Accept, Origin");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            
            // Handle preflight requests
            if ("OPTIONS".equals(request.getMethod())) {
                response.setStatus(200);
                return;
            }
            
            filterChain.doFilter(request, response);
        }

        private boolean isAllowedOrigin(String origin) {
            if (origin == null) return false;
            
            // Add your allowed origins here
            return origin.startsWith("http://localhost:") || 
                   origin.startsWith("https://localhost:") ||
                   origin.equals("https://yourdomain.com");
        }
    }
}
