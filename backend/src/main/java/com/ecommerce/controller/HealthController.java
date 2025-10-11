package com.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://ecommerce-site-five-phi.vercel.app"})
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "E-commerce Backend API");
        response.put("version", "1.0.0");
        response.put("message", "Backend is running successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> testApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Backend API is working!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("cors", "Enabled for Vercel frontend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/debug")
    public ResponseEntity<Map<String, Object>> debugInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("backend", "Running successfully");
        response.put("timestamp", System.currentTimeMillis());
        response.put("cors_origins", new String[]{"http://localhost:3000", "https://ecommerce-site-five-phi.vercel.app"});
        response.put("api_base", "https://ecommerce-backend-51pg.onrender.com/api");
        response.put("products_endpoint", "https://ecommerce-backend-51pg.onrender.com/api/products");
        response.put("frontend_url", "https://ecommerce-site-five-phi.vercel.app");
        return ResponseEntity.ok(response);
    }
}