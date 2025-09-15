package com.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.error("Access denied for request: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Access Denied");
        errorResponse.put("message", "You don't have permission to access this resource");
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getRequestURI());
        
        // Add more details for debugging in development
        if (accessDeniedException.getMessage() != null) {
            errorResponse.put("details", accessDeniedException.getMessage());
        }
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
