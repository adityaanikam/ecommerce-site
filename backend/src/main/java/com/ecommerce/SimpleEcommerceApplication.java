package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class SimpleEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleEcommerceApplication.class, args);
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Simple E-commerce backend is running");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/api/products")
    public Map<String, Object> getProducts() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", new Object[]{
            Map.of(
                "id", "1",
                "name", "Sample Product",
                "description", "A sample product for testing",
                "price", 99.99,
                "stock", 10
            )
        });
        return response;
    }
}
