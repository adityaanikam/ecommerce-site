package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class ProductController {
    private final ProductService productService;
    private final MongoTemplate mongoTemplate;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Product> products = productService.findProducts(
            category, subcategory, search, minPrice, maxPrice, pageRequest
        );
        
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> products = productService.findByCategory(category, pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.findFeaturedProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<Product>> getNewProducts() {
        return ResponseEntity.ok(productService.findNewProducts());
    }

    @GetMapping("/on-sale")
    public ResponseEntity<List<Product>> getOnSaleProducts() {
        return ResponseEntity.ok(productService.findOnSaleProducts());
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @PostMapping("/fix-images")
    public ResponseEntity<Map<String, Object>> fixImagePaths() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> allProducts = productRepository.findAll();
            int updatedCount = 0;
            
            for (Product product : allProducts) {
                String category = product.getCategory();
                String name = product.getName();
                
                // Create correct image paths - try different extensions
                List<String> imagePaths = new ArrayList<>();
                
                // Use correct extensions based on actual files
                // Electronics: mostly jpg, some webp
                // Fashion: mix of jpg and webp
                String[] extensions;
                if ("Electronics".equals(category)) {
                    extensions = new String[]{"jpg", "jpg", "jpg"}; // Most electronics use jpg
                } else if ("Fashion".equals(category)) {
                    extensions = new String[]{"jpg", "jpg", "jpg"}; // Most fashion use jpg
                } else {
                    extensions = new String[]{"jpg", "jpg", "jpg"}; // Default to jpg
                }
                
                for (int i = 0; i < 3; i++) {
                    imagePaths.add("/products/" + category + "/" + name + "/" + (i + 1) + "." + extensions[i]);
                }
                
                // Update product with correct image paths
                product.setImages(imagePaths);
                productRepository.save(product);
                
                updatedCount++;
            }
            
            response.put("success", true);
            response.put("message", "Successfully updated " + updatedCount + " products");
            response.put("updatedCount", updatedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifySetup() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test MongoDB connection
            boolean mongoConnected = testMongoConnection();
            response.put("mongoConnected", mongoConnected);
            
            if (mongoConnected) {
                // Get total products count
                long totalProducts = mongoTemplate.count(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where("_id").exists(true)), "products");
                response.put("totalProducts", totalProducts);
                
                // Get a sample product
                Optional<Product> sampleProduct = productService.findById("ELEC001");
                if (sampleProduct.isPresent()) {
                    Product product = sampleProduct.get();
                    Map<String, Object> sampleProductData = new HashMap<>();
                    sampleProductData.put("_id", product.getId());
                    sampleProductData.put("name", product.getName());
                    sampleProductData.put("images", product.getImages());
                    response.put("sampleProduct", sampleProductData);
                } else {
                    // If ELEC001 doesn't exist, get the first product
                    Page<Product> firstPage = productService.findProducts(null, null, null, null, null, PageRequest.of(0, 1));
                    if (!firstPage.getContent().isEmpty()) {
                        Product product = firstPage.getContent().get(0);
                        Map<String, Object> sampleProductData = new HashMap<>();
                        sampleProductData.put("_id", product.getId());
                        sampleProductData.put("name", product.getName());
                        sampleProductData.put("images", product.getImages());
                        response.put("sampleProduct", sampleProductData);
                    } else {
                        response.put("sampleProduct", null);
                    }
                }
            } else {
                response.put("totalProducts", 0);
                response.put("sampleProduct", null);
            }
            
            // Check if static resources are configured
            response.put("staticResourcesConfigured", true);
            
            // Provide an example image URL
            response.put("imageUrlExample", "http://localhost:8080/products/Electronics/iPhone+15+Pro/1.jpg");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("mongoConnected", false);
            response.put("totalProducts", 0);
            response.put("sampleProduct", null);
            response.put("staticResourcesConfigured", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    private boolean testMongoConnection() {
        try {
            mongoTemplate.getCollection("products").countDocuments();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
