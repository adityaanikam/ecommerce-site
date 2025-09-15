package com.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Rate limiting configuration
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_ATTEMPT_WINDOW = Duration.ofMinutes(15);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String requestPath = request.getRequestURI();
        
        // Skip rate limiting for static resources
        if (isStaticResource(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Apply different rate limits based on endpoint
        if (isLoginEndpoint(requestPath)) {
            if (!checkLoginRateLimit(clientIp)) {
                handleRateLimitExceeded(response, "Too many login attempts. Please try again later.");
                return;
            }
        } else if (isApiEndpoint(requestPath)) {
            if (!checkApiRateLimit(clientIp)) {
                handleRateLimitExceeded(response, "Rate limit exceeded. Please try again later.");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean checkLoginRateLimit(String clientIp) {
        String key = "rate_limit:login:" + clientIp;
        String countStr = (String) redisTemplate.opsForValue().get(key);
        
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        if (count >= MAX_LOGIN_ATTEMPTS) {
            log.warn("Login rate limit exceeded for IP: {}", clientIp);
            return false;
        }
        
        // Increment counter
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOGIN_ATTEMPT_WINDOW);
        
        return true;
    }
    
    private boolean checkApiRateLimit(String clientIp) {
        // Check per-minute limit
        String minuteKey = "rate_limit:minute:" + clientIp + ":" + (System.currentTimeMillis() / 60000);
        String minuteCountStr = (String) redisTemplate.opsForValue().get(minuteKey);
        
        int minuteCount = minuteCountStr != null ? Integer.parseInt(minuteCountStr) : 0;
        if (minuteCount >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Per-minute rate limit exceeded for IP: {}", clientIp);
            return false;
        }
        
        // Check per-hour limit
        String hourKey = "rate_limit:hour:" + clientIp + ":" + (System.currentTimeMillis() / 3600000);
        String hourCountStr = (String) redisTemplate.opsForValue().get(hourKey);
        
        int hourCount = hourCountStr != null ? Integer.parseInt(hourCountStr) : 0;
        if (hourCount >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Per-hour rate limit exceeded for IP: {}", clientIp);
            return false;
        }
        
        // Increment counters
        redisTemplate.opsForValue().increment(minuteKey);
        redisTemplate.expire(minuteKey, Duration.ofMinutes(1));
        
        redisTemplate.opsForValue().increment(hourKey);
        redisTemplate.expire(hourKey, Duration.ofHours(1));
        
        return true;
    }
    
    private void handleRateLimitExceeded(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Rate Limit Exceeded");
        errorResponse.put("message", message);
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
    
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
    
    private boolean isStaticResource(String path) {
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.startsWith("/favicon.ico");
    }
    
    private boolean isLoginEndpoint(String path) {
        return path.equals("/api/auth/login") || 
               path.equals("/api/auth/register") ||
               path.equals("/api/auth/forgot-password");
    }
    
    private boolean isApiEndpoint(String path) {
        return path.startsWith("/api/");
    }
    
    public void resetLoginAttempts(String clientIp) {
        String key = "rate_limit:login:" + clientIp;
        redisTemplate.delete(key);
    }
}
