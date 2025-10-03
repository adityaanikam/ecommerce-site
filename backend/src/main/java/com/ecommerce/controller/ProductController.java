package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
public class ProductController {
    private final ProductService productService;

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
}
