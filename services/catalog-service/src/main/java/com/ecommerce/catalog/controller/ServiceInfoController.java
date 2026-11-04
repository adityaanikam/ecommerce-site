package com.ecommerce.catalog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root identity endpoint, kept because the monolith answered on / and the Render
 * health check pointed at it. Actuator owns the real health checks.
 */
@RestController
public class ServiceInfoController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "catalog-service");
        body.put("version", "1.0.0");
        return ResponseEntity.ok(body);
    }
}
