package com.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements org.springframework.security.web.AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        log.error("Unauthorized access attempt: {}", authException.getMessage());
        
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication required to access this resource");
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", request.getRequestURI());
        
        // Add more details for debugging in development
        if (authException.getMessage() != null) {
            errorResponse.put("details", authException.getMessage());
        }
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
