package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.service.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final CacheService cacheService;
    
    // User Management
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto.UserResponse>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get all users request received");
        Page<UserDto.UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable String userId) {
        log.info("Admin get user by ID request received: {}", userId);
        UserDto.UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDto.UserResponse> updateUser(@PathVariable String userId,
                                                         @Valid @RequestBody UserDto.UpdateUserRequest request) {
        log.info("Admin update user request received for user ID: {}", userId);
        UserDto.UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<UserDto.MessageResponse> deleteUser(@PathVariable String userId) {
        log.info("Admin delete user request received for user ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(UserDto.MessageResponse.builder()
                .message("User deleted successfully")
                .build());
    }
    
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<UserDto.UserResponse> assignRole(@PathVariable String userId,
                                                         @RequestParam com.ecommerce.model.User.Role role) {
        log.info("Admin assign role request received for user ID: {} with role: {}", userId, role);
        UserDto.UserResponse updatedUser = userService.assignRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<UserDto.UserResponse> removeRole(@PathVariable String userId,
                                                         @RequestParam com.ecommerce.model.User.Role role) {
        log.info("Admin remove role request received for user ID: {} with role: {}", userId, role);
        UserDto.UserResponse updatedUser = userService.removeRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/users/search")
    public ResponseEntity<List<UserDto.UserResponse>> searchUsers(@RequestParam String query) {
        log.info("Admin search users request received with query: {}", query);
        List<UserDto.UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/by-role")
    public ResponseEntity<List<UserDto.UserResponse>> getUsersByRole(@RequestParam com.ecommerce.model.User.Role role) {
        log.info("Admin get users by role request received for role: {}", role);
        List<UserDto.UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    // Product Management
    
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto.ProductResponse>> getAllProducts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get all products request received");
        Page<ProductDto.ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDto.ProductResponse> getProductById(@PathVariable String productId) {
        log.info("Admin get product by ID request received: {}", productId);
        ProductDto.ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping("/products")
    public ResponseEntity<ProductDto.ProductResponse> createProduct(@Valid @RequestBody ProductDto.CreateProductRequest request) {
        log.info("Admin create product request received: {}", request.getName());
        ProductDto.ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductDto.ProductResponse> updateProduct(@PathVariable String productId,
                                                                 @Valid @RequestBody ProductDto.UpdateProductRequest request) {
        log.info("Admin update product request received for product ID: {}", productId);
        ProductDto.ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ProductDto.MessageResponse> deleteProduct(@PathVariable String productId) {
        log.info("Admin delete product request received for product ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.ok(ProductDto.MessageResponse.builder()
                .message("Product deleted successfully")
                .build());
    }
    
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<ProductDto.ProductResponse>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        log.info("Admin get low stock products request received with threshold: {}", threshold);
        List<ProductDto.ProductResponse> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/products/out-of-stock")
    public ResponseEntity<List<ProductDto.ProductResponse>> getOutOfStockProducts() {
        log.info("Admin get out of stock products request received");
        List<ProductDto.ProductResponse> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }
    
    @PostMapping("/products/{productId}/images")
    public ResponseEntity<ProductDto.ProductResponse> uploadProductImages(@PathVariable String productId,
                                                                       @RequestParam("files") List<MultipartFile> files) {
        log.info("Admin upload product images request received for product ID: {}", productId);
        ProductDto.ProductResponse product = productService.uploadProductImages(productId, files);
        return ResponseEntity.ok(product);
    }
    
    // Category Management
    
    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryDto.CategoryResponse>> getAllCategories(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get all categories request received");
        Page<CategoryDto.CategoryResponse> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDto.CategoryResponse> getCategoryById(@PathVariable String categoryId) {
        log.info("Admin get category by ID request received: {}", categoryId);
        CategoryDto.CategoryResponse category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }
    
    @PostMapping("/categories")
    public ResponseEntity<CategoryDto.CategoryResponse> createCategory(@Valid @RequestBody CategoryDto.CreateCategoryRequest request) {
        log.info("Admin create category request received: {}", request.getName());
        CategoryDto.CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDto.CategoryResponse> updateCategory(@PathVariable String categoryId,
                                                                     @Valid @RequestBody CategoryDto.UpdateCategoryRequest request) {
        log.info("Admin update category request received for category ID: {}", categoryId);
        CategoryDto.CategoryResponse category = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(category);
    }
    
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDto.MessageResponse> deleteCategory(@PathVariable String categoryId) {
        log.info("Admin delete category request received for category ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(CategoryDto.MessageResponse.builder()
                .message("Category deleted successfully")
                .build());
    }
    
    @GetMapping("/categories/tree")
    public ResponseEntity<CategoryDto.CategoryTreeResponse> getCategoryTree() {
        log.info("Admin get category tree request received");
        CategoryDto.CategoryTreeResponse tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }
    
    @PutMapping("/categories/{categoryId}/move")
    public ResponseEntity<CategoryDto.CategoryResponse> moveCategory(@PathVariable String categoryId,
                                                                  @RequestParam(required = false) String newParentId) {
        log.info("Admin move category request received for category ID: {} to parent: {}", categoryId, newParentId);
        CategoryDto.CategoryResponse category = categoryService.moveCategory(categoryId, newParentId);
        return ResponseEntity.ok(category);
    }
    
    // Order Management
    
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDto.OrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get all orders request received");
        Page<OrderDto.OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDto.OrderResponse> getOrderById(@PathVariable String orderId) {
        log.info("Admin get order by ID request received: {}", orderId);
        OrderDto.OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderDto.OrderResponse> updateOrderStatus(@PathVariable String orderId,
                                                                 @RequestParam com.ecommerce.model.Order.OrderStatus status) {
        log.info("Admin update order status request received for order ID: {} to status: {}", orderId, status);
        OrderDto.OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/orders/{orderId}/payment-status")
    public ResponseEntity<OrderDto.OrderResponse> updatePaymentStatus(@PathVariable String orderId,
                                                                    @RequestParam com.ecommerce.model.Order.PaymentStatus paymentStatus) {
        log.info("Admin update payment status request received for order ID: {} to status: {}", orderId, paymentStatus);
        OrderDto.OrderResponse order = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/orders/{orderId}/tracking")
    public ResponseEntity<OrderDto.OrderResponse> addTrackingInfo(@PathVariable String orderId,
                                                               @RequestParam String trackingNumber,
                                                               @RequestParam String carrier) {
        log.info("Admin add tracking info request received for order ID: {}", orderId);
        OrderDto.OrderResponse order = orderService.addTrackingInfo(orderId, trackingNumber, carrier);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/orders/by-status")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByStatus(@RequestParam com.ecommerce.model.Order.OrderStatus status) {
        log.info("Admin get orders by status request received for status: {}", status);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/by-payment-status")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByPaymentStatus(@RequestParam com.ecommerce.model.Order.PaymentStatus paymentStatus) {
        log.info("Admin get orders by payment status request received for status: {}", paymentStatus);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/by-date-range")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByDateRange(@RequestParam LocalDateTime startDate,
                                                                          @RequestParam LocalDateTime endDate) {
        log.info("Admin get orders by date range request received: {} to {}", startDate, endDate);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }
    
    // Review Management
    
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDto.ReviewResponse>> getAllReviews(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get all reviews request received");
        Page<ReviewDto.ReviewResponse> reviews = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/reviews/pending")
    public ResponseEntity<Page<ReviewDto.ReviewResponse>> getPendingReviews(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get pending reviews request received");
        Page<ReviewDto.ReviewResponse> reviews = reviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/reviews/approved")
    public ResponseEntity<Page<ReviewDto.ReviewResponse>> getApprovedReviews(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get approved reviews request received");
        Page<ReviewDto.ReviewResponse> reviews = reviewService.getApprovedReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/reviews/rejected")
    public ResponseEntity<Page<ReviewDto.ReviewResponse>> getRejectedReviews(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Admin get rejected reviews request received");
        Page<ReviewDto.ReviewResponse> reviews = reviewService.getRejectedReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @PutMapping("/reviews/{reviewId}/moderate")
    public ResponseEntity<ReviewDto.ReviewResponse> moderateReview(@PathVariable String reviewId,
                                                                @RequestParam boolean approved,
                                                                @RequestParam(required = false) String moderatorComment) {
        log.info("Admin moderate review request received for review ID: {} - approved: {}", reviewId, approved);
        ReviewDto.ReviewResponse review = reviewService.moderateReview(reviewId, approved, moderatorComment);
        return ResponseEntity.ok(review);
    }
    
    // Analytics and Reporting
    
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        log.info("Admin get dashboard analytics request received");
        // TODO: Implement dashboard analytics
        Map<String, Object> analytics = Map.of(
            "totalUsers", 0,
            "totalProducts", 0,
            "totalOrders", 0,
            "totalRevenue", 0.0,
            "recentOrders", List.of(),
            "topProducts", List.of(),
            "userGrowth", Map.of(),
            "revenueGrowth", Map.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/products")
    public ResponseEntity<Map<String, Object>> getProductAnalytics(@RequestParam(required = false) String productId) {
        log.info("Admin get product analytics request received for product ID: {}", productId);
        if (productId != null) {
            Map<String, Object> analytics = productService.getProductAnalytics(productId);
            return ResponseEntity.ok(analytics);
        }
        // TODO: Implement overall product analytics
        Map<String, Object> analytics = Map.of(
            "totalProducts", 0,
            "activeProducts", 0,
            "lowStockProducts", 0,
            "outOfStockProducts", 0,
            "topSellingProducts", List.of(),
            "categoryDistribution", Map.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/orders")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics() {
        log.info("Admin get order analytics request received");
        // TODO: Implement order analytics
        Map<String, Object> analytics = Map.of(
            "totalOrders", 0,
            "totalRevenue", 0.0,
            "averageOrderValue", 0.0,
            "ordersByStatus", Map.of(),
            "revenueByPeriod", Map.of(),
            "topCustomers", List.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics() {
        log.info("Admin get user analytics request received");
        // TODO: Implement user analytics
        Map<String, Object> analytics = Map.of(
            "totalUsers", 0,
            "activeUsers", 0,
            "newUsers", 0,
            "usersByRole", Map.of(),
            "userGrowth", Map.of(),
            "topUsers", List.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    // Cache Management
    
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        log.info("Admin clear all caches request received");
        cacheService.clearAllCaches();
        return ResponseEntity.ok(Map.of("message", "All caches cleared successfully"));
    }
    
    @PostMapping("/cache/clear/{pattern}")
    public ResponseEntity<Map<String, String>> clearCacheByPattern(@PathVariable String pattern) {
        log.info("Admin clear cache by pattern request received for pattern: {}", pattern);
        cacheService.clearCacheByPattern(pattern);
        return ResponseEntity.ok(Map.of("message", "Cache cleared for pattern: " + pattern));
    }
    
    @GetMapping("/cache/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        log.info("Admin get cache health request received");
        boolean isAvailable = cacheService.isCacheAvailable();
        Map<String, Object> health = Map.of(
            "available", isAvailable,
            "status", isAvailable ? "healthy" : "unhealthy",
            "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(health);
    }
    
    // System Management
    
    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        log.info("Admin get system status request received");
        Map<String, Object> status = Map.of(
            "status", "operational",
            "timestamp", LocalDateTime.now(),
            "version", "1.0.0",
            "uptime", "N/A"
        );
        return ResponseEntity.ok(status);
    }
}
