package com.ecommerce.controller;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductController {
    
    private final ProductService productService;
    
    // Public Product Operations
    
    @GetMapping
    public ResponseEntity<Page<ProductDto.ProductResponse>> getAllProducts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Get all products request received");
        Page<ProductDto.ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.ProductResponse> getProductById(@PathVariable String productId) {
        log.info("Get product by ID request received: {}", productId);
        ProductDto.ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.ProductResponse>> searchProducts(@RequestParam String query,
                                                                         @PageableDefault(size = 20) Pageable pageable) {
        log.info("Search products request received with query: {}", query);
        Page<ProductDto.ProductResponse> products = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getProductsByCategory(@PathVariable String categoryId,
                                                                                @PageableDefault(size = 20) Pageable pageable) {
        log.info("Get products by category request received for category ID: {}", categoryId);
        Page<ProductDto.ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/brand/{brand}")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getProductsByBrand(@PathVariable String brand,
                                                                             @PageableDefault(size = 20) Pageable pageable) {
        log.info("Get products by brand request received for brand: {}", brand);
        Page<ProductDto.ProductResponse> products = productService.getProductsByBrand(brand, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getProductsByPriceRange(@RequestParam Double minPrice,
                                                                                   @RequestParam Double maxPrice,
                                                                                   @PageableDefault(size = 20) Pageable pageable) {
        log.info("Get products by price range request received: {} - {}", minPrice, maxPrice);
        Page<ProductDto.ProductResponse> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<ProductDto.ProductResponse>> getFeaturedProducts(@RequestParam(defaultValue = "10") int limit) {
        log.info("Get featured products request received with limit: {}", limit);
        List<ProductDto.ProductResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductDto.ProductResponse>> getTopRatedProducts(@RequestParam(defaultValue = "10") int limit) {
        log.info("Get top rated products request received with limit: {}", limit);
        List<ProductDto.ProductResponse> products = productService.getTopRatedProducts(limit);
        return ResponseEntity.ok(products);
    }
    
    @PostMapping("/search/advanced")
    public ResponseEntity<Page<ProductDto.ProductResponse>> searchProductsWithFilters(@Valid @RequestBody ProductDto.ProductSearchRequest searchRequest,
                                                                                     @PageableDefault(size = 20) Pageable pageable) {
        log.info("Advanced product search request received");
        Page<ProductDto.ProductResponse> products = productService.searchProductsWithFilters(searchRequest, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Product Management (Seller/Admin)
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> createProduct(@Valid @RequestBody ProductDto.CreateProductRequest request) {
        log.info("Create product request received: {}", request.getName());
        ProductDto.ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> updateProduct(@PathVariable String productId,
                                                                  @Valid @RequestBody ProductDto.UpdateProductRequest request) {
        log.info("Update product request received for product ID: {}", productId);
        ProductDto.ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto.MessageResponse> deleteProduct(@PathVariable String productId) {
        log.info("Delete product request received for product ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ProductDto.MessageResponse.builder()
                .message("Product deleted successfully")
                .build());
    }
    
    // Image Management
    
    @PostMapping("/{productId}/images")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> uploadProductImages(@PathVariable String productId,
                                                                        @RequestParam("files") List<MultipartFile> files) {
        log.info("Upload product images request received for product ID: {}", productId);
        ProductDto.ProductResponse product = productService.uploadProductImages(productId, files);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> removeProductImage(@PathVariable String productId,
                                                                       @RequestParam String imageUrl) {
        log.info("Remove product image request received for product ID: {}", productId);
        ProductDto.ProductResponse product = productService.removeProductImage(productId, imageUrl);
        return ResponseEntity.ok(product);
    }
    
    // Inventory Management
    
    @PutMapping("/{productId}/stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> updateStock(@PathVariable String productId,
                                                                @RequestParam Integer stock) {
        log.info("Update stock request received for product ID: {} to {}", productId, stock);
        ProductDto.ProductResponse product = productService.updateStock(productId, stock);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/{productId}/stock/reduce")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> reduceStock(@PathVariable String productId,
                                                                @RequestParam Integer quantity) {
        log.info("Reduce stock request received for product ID: {} by {}", productId, quantity);
        ProductDto.ProductResponse product = productService.reduceStock(productId, quantity);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/{productId}/stock/increase")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ProductDto.ProductResponse> increaseStock(@PathVariable String productId,
                                                                  @RequestParam Integer quantity) {
        log.info("Increase stock request received for product ID: {} by {}", productId, quantity);
        ProductDto.ProductResponse product = productService.increaseStock(productId, quantity);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<List<ProductDto.ProductResponse>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        log.info("Get low stock products request received with threshold: {}", threshold);
        List<ProductDto.ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<List<ProductDto.ProductResponse>> getOutOfStockProducts() {
        log.info("Get out of stock products request received");
        List<ProductDto.ProductResponse> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    // Analytics and Reporting
    
    @GetMapping("/{productId}/analytics")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProductAnalytics(@PathVariable String productId) {
        log.info("Get product analytics request received for product ID: {}", productId);
        Map<String, Object> analytics = productService.getProductAnalytics(productId);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/seller/{sellerId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getProductsBySeller(@PathVariable String sellerId,
                                                                              @PageableDefault(size = 20) Pageable pageable) {
        log.info("Get products by seller request received for seller ID: {}", sellerId);
        Page<ProductDto.ProductResponse> products = productService.getProductsBySeller(sellerId, pageable);
        return ResponseEntity.ok(products);
    }
    
    // Utility Endpoints
    
    @GetMapping("/{productId}/available")
    public ResponseEntity<Map<String, Object>> checkProductAvailability(@PathVariable String productId,
                                                                       @RequestParam Integer quantity) {
        log.info("Check product availability request received for product ID: {} with quantity: {}", productId, quantity);
        boolean available = productService.isProductAvailable(productId, quantity);
        Double price = productService.getProductPrice(productId);
        
        Map<String, Object> response = Map.of(
            "available", available,
            "price", price,
            "productId", productId,
            "requestedQuantity", quantity
        );
        
        return ResponseEntity.ok(response);
    }
}