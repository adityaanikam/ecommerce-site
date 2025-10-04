package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test MongoDB connection
            boolean mongoConnected = testMongoConnection();
            health.put("mongodb_connected", mongoConnected);
            
            if (mongoConnected) {
                // Get total product count
                long productCount = mongoTemplate.count(new Query(), "products");
                health.put("total_products", productCount);
                
                // Get first product's image paths
                List<Map> products = mongoTemplate.findAll(Map.class, "products");
                if (!products.isEmpty()) {
                    Map<String, Object> firstProduct = products.get(0);
                    health.put("first_product_name", firstProduct.get("name"));
                    health.put("first_product_images", firstProduct.get("images"));
                } else {
                    health.put("first_product_name", "No products found");
                    health.put("first_product_images", "No images available");
                }
            } else {
                health.put("total_products", 0);
                health.put("first_product_name", "Database not connected");
                health.put("first_product_images", "Database not connected");
            }
            
            // Static resource base path
            health.put("static_resource_base_path", "file:./Products/");
            health.put("image_endpoint", "/products/**");
            
            // Server status
            health.put("server_status", "running");
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("error", e.getMessage());
            health.put("mongodb_connected", false);
            health.put("server_status", "error");
            return ResponseEntity.status(500).body(health);
        }
    }
    
    private boolean testMongoConnection() {
        try {
            // Try to ping the database
            mongoTemplate.getCollection("products").countDocuments();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
