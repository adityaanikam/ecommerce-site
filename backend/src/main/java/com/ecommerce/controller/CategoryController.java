package com.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private com.ecommerce.repository.ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> getCategories() {
        try {
            // Return hardcoded categories for now
            List<Map<String, Object>> categories = new ArrayList<>();
            
            Map<String, Object> category1 = new HashMap<>();
            category1.put("name", "Electronics");
            category1.put("slug", "electronics");
            category1.put("subcategories", new ArrayList<>());
            categories.add(category1);
            
            Map<String, Object> category2 = new HashMap<>();
            category2.put("name", "Fashion");
            category2.put("slug", "fashion");
            category2.put("subcategories", new ArrayList<>());
            categories.add(category2);
            
            Map<String, Object> category3 = new HashMap<>();
            category3.put("name", "Sports");
            category3.put("slug", "sports");
            category3.put("subcategories", new ArrayList<>());
            categories.add(category3);
            
            Map<String, Object> category4 = new HashMap<>();
            category4.put("name", "Home & Garden");
            category4.put("slug", "home-garden");
            category4.put("subcategories", new ArrayList<>());
            categories.add(category4);
            
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch categories: " + e.getMessage()));
        }
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getCategoryBySlug(@PathVariable String slug) {
        try {
            Map<String, Object> category = new HashMap<>();
            category.put("name", "Category " + slug);
            category.put("slug", slug);
            category.put("description", "Description for " + slug);
            category.put("image", "https://placehold.co/800x600/6366f1/ffffff?text=Category");
            category.put("subcategories", new ArrayList<>());
            
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch category: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getCategoryStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCategories", 4);
            stats.put("totalProducts", 130);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch category stats: " + e.getMessage()));
        }
    }
}