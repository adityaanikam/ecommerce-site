package com.ecommerce.controller;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    // Order Operations
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> createOrder(@Valid @RequestBody OrderDto.CreateOrderRequest request,
                                                            Authentication authentication) {
        log.info("Create order request received");
        String userId = authentication.getName();
        OrderDto.OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> getOrderById(@PathVariable String orderId,
                                                            Authentication authentication) {
        log.info("Get order by ID request received: {}", orderId);
        OrderDto.OrderResponse order = orderService.getOrderById(orderId);
        
        // Check if user has access to this order
        String userId = authentication.getName();
        if (!order.getUserId().equals(userId) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> getOrderByOrderNumber(@PathVariable String orderNumber,
                                                                     Authentication authentication) {
        log.info("Get order by order number request received: {}", orderNumber);
        OrderDto.OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        
        // Check if user has access to this order
        String userId = authentication.getName();
        if (!order.getUserId().equals(userId) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<Page<OrderDto.OrderResponse>> getMyOrders(@PageableDefault(size = 20) Pageable pageable,
                                                                 Authentication authentication) {
        log.info("Get my orders request received");
        String userId = authentication.getName();
        Page<OrderDto.OrderResponse> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrderHistory(Authentication authentication) {
        log.info("Get order history request received");
        String userId = authentication.getName();
        List<OrderDto.OrderResponse> orders = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }
    
    // Order Status Management
    
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> updateOrderStatus(@PathVariable String orderId,
                                                                 @RequestParam com.ecommerce.model.Order.OrderStatus status) {
        log.info("Update order status request received for order ID: {} to status: {}", orderId, status);
        OrderDto.OrderResponse order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }
    
    @PutMapping("/{orderId}/payment-status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> updatePaymentStatus(@PathVariable String orderId,
                                                                    @RequestParam com.ecommerce.model.Order.PaymentStatus paymentStatus) {
        log.info("Update payment status request received for order ID: {} to status: {}", orderId, paymentStatus);
        OrderDto.OrderResponse order = orderService.updatePaymentStatus(orderId, paymentStatus);
        return ResponseEntity.ok(order);
    }
    
    // Order Cancellation
    
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> cancelOrder(@PathVariable String orderId,
                                                           @RequestParam(required = false) String reason,
                                                           Authentication authentication) {
        log.info("Cancel order request received for order ID: {} with reason: {}", orderId, reason);
        
        // Check if user has permission to cancel this order
        OrderDto.OrderResponse order = orderService.getOrderById(orderId);
        String userId = authentication.getName();
        if (!order.getUserId().equals(userId) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        OrderDto.OrderResponse cancelledOrder = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(cancelledOrder);
    }
    
    // Order Tracking
    
    @PostMapping("/{orderId}/tracking")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<OrderDto.OrderResponse> addTrackingInfo(@PathVariable String orderId,
                                                               @RequestParam String trackingNumber,
                                                               @RequestParam String carrier) {
        log.info("Add tracking info request received for order ID: {}", orderId);
        OrderDto.OrderResponse order = orderService.addTrackingInfo(orderId, trackingNumber, carrier);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/{orderId}/tracking")
    @PreAuthorize("hasAnyRole('USER', 'SELLER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTrackingInfo(@PathVariable String orderId,
                                                            Authentication authentication) {
        log.info("Get tracking info request received for order ID: {}", orderId);
        OrderDto.OrderResponse order = orderService.getOrderById(orderId);
        
        // Check if user has access to this order
        String userId = authentication.getName();
        if (!order.getUserId().equals(userId) && !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Map<String, Object> trackingInfo = Map.of(
            "orderNumber", order.getOrderNumber(),
            "trackingNumber", order.getTrackingNumber() != null ? order.getTrackingNumber() : "",
            "carrier", order.getCarrier() != null ? order.getCarrier() : "",
            "status", order.getStatus(),
            "shippingAddress", order.getShippingAddress()
        );
        
        return ResponseEntity.ok(trackingInfo);
    }
    
    // Admin Operations
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto.OrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Get all orders request received");
        Page<OrderDto.OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByStatus(@RequestParam com.ecommerce.model.Order.OrderStatus status) {
        log.info("Get orders by status request received for status: {}", status);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/by-payment-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByPaymentStatus(@RequestParam com.ecommerce.model.Order.PaymentStatus paymentStatus) {
        log.info("Get orders by payment status request received for status: {}", paymentStatus);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/by-date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto.OrderResponse>> getOrdersByDateRange(@RequestParam LocalDateTime startDate,
                                                                          @RequestParam LocalDateTime endDate) {
        log.info("Get orders by date range request received: {} to {}", startDate, endDate);
        List<OrderDto.OrderResponse> orders = orderService.getOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }
    
    // Analytics and Reporting
    
    @GetMapping("/analytics/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderAnalytics() {
        log.info("Get order analytics request received");
        // TODO: Implement order analytics
        Map<String, Object> analytics = Map.of(
            "totalOrders", 0,
            "totalRevenue", 0.0,
            "averageOrderValue", 0.0,
            "ordersByStatus", Map.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(@RequestParam(required = false) LocalDateTime startDate,
                                                                 @RequestParam(required = false) LocalDateTime endDate) {
        log.info("Get revenue analytics request received");
        // TODO: Implement revenue analytics
        Map<String, Object> analytics = Map.of(
            "totalRevenue", 0.0,
            "revenueByPeriod", Map.of(),
            "revenueByStatus", Map.of()
        );
        return ResponseEntity.ok(analytics);
    }
    
    // User Analytics
    
    @GetMapping("/analytics/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserOrderAnalytics(@PathVariable String userId) {
        log.info("Get user order analytics request received for user ID: {}", userId);
        double totalValue = orderService.getTotalOrderValue(userId);
        int orderCount = orderService.getOrderCount(userId);
        
        Map<String, Object> analytics = Map.of(
            "userId", userId,
            "totalOrderValue", totalValue,
            "orderCount", orderCount,
            "averageOrderValue", orderCount > 0 ? totalValue / orderCount : 0.0
        );
        
        return ResponseEntity.ok(analytics);
    }
}