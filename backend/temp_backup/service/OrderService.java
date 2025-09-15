package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.model.Order;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private final EmailService emailService;
    
    // Order Creation and Management
    
    public OrderDto.OrderResponse createOrder(String userId, OrderDto.CreateOrderRequest request) {
        log.info("Creating order for user ID: {}", userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Get and validate cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("Cart is empty"));
        
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot create order with empty cart");
        }
        
        // Validate all items in cart
        for (Cart.CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));
            
            if (!product.getIsActive()) {
                throw new ValidationException("Product is not available: " + product.getName());
            }
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new ValidationException("Insufficient stock for product: " + product.getName());
            }
        }
        
        // Create order
        Order order = Order.builder()
                .userId(userId)
                .orderNumber(generateOrderNumber())
                .items(cart.getItems().stream()
                        .map(this::mapCartItemToOrderItem)
                        .collect(Collectors.toList()))
                .totalAmount(cart.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Reduce stock for all products
        for (Cart.CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() - cartItem.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }
        }
        
        // Clear cart
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cart.setTaxAmount(0.0);
        cart.setShippingAmount(0.0);
        cart.setDiscountAmount(0.0);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
        
        // Clear cache
        cacheService.evictCartCache(userId);
        cacheService.evictOrderCache(savedOrder.getId());
        
        // Send order confirmation email
        emailService.sendOrderConfirmation(user.getEmail(), savedOrder);
        
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return mapToOrderResponse(savedOrder);
    }
    
    public OrderDto.OrderResponse getOrderById(String orderId) {
        log.debug("Fetching order by ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        return mapToOrderResponse(order);
    }
    
    public OrderDto.OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order by order number: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        
        return mapToOrderResponse(order);
    }
    
    public Page<OrderDto.OrderResponse> getOrdersByUserId(String userId, Pageable pageable) {
        log.debug("Fetching orders for user ID: {}", userId);
        
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    public Page<OrderDto.OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination");
        
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    // Order Status Updates
    
    public OrderDto.OrderResponse updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        log.info("Updating order status for order ID: {} to {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        Order.OrderStatus currentStatus = order.getStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new ValidationException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Update payment status if order is completed
        if (newStatus == Order.OrderStatus.COMPLETED) {
            order.setPaymentStatus(Order.PaymentStatus.COMPLETED);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cache
        cacheService.evictOrderCache(orderId);
        
        // Send status update email
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendOrderStatusUpdate(user.getEmail(), savedOrder);
        }
        
        log.info("Order status updated successfully for order ID: {}", orderId);
        return mapToOrderResponse(savedOrder);
    }
    
    public OrderDto.OrderResponse updatePaymentStatus(String orderId, Order.PaymentStatus paymentStatus) {
        log.info("Updating payment status for order ID: {} to {}", orderId, paymentStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Update order status based on payment status
        if (paymentStatus == Order.PaymentStatus.COMPLETED && order.getStatus() == Order.OrderStatus.PENDING) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cache
        cacheService.evictOrderCache(orderId);
        
        log.info("Payment status updated successfully for order ID: {}", orderId);
        return mapToOrderResponse(savedOrder);
    }
    
    // Order Cancellation
    
    public OrderDto.OrderResponse cancelOrder(String orderId, String reason) {
        log.info("Cancelling order ID: {} with reason: {}", orderId, reason);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        // Check if order can be cancelled
        if (!canCancelOrder(order)) {
            throw new ValidationException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Restore stock for all items
        for (Order.OrderItem orderItem : order.getItems()) {
            Product product = productRepository.findById(orderItem.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + orderItem.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cache
        cacheService.evictOrderCache(orderId);
        
        // Send cancellation email
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendOrderCancellation(user.getEmail(), savedOrder, reason);
        }
        
        log.info("Order cancelled successfully for order ID: {}", orderId);
        return mapToOrderResponse(savedOrder);
    }
    
    // Order History and Analytics
    
    public List<OrderDto.OrderResponse> getOrderHistory(String userId) {
        log.debug("Fetching order history for user ID: {}", userId);
        
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto.OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        log.debug("Fetching orders by status: {}", status);
        
        List<Order> orders = orderRepository.findByStatus(status);
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto.OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching orders by date range: {} to {}", startDate, endDate);
        
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto.OrderResponse> getOrdersByPaymentStatus(Order.PaymentStatus paymentStatus) {
        log.debug("Fetching orders by payment status: {}", paymentStatus);
        
        List<Order> orders = orderRepository.findByPaymentStatus(paymentStatus);
        
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    // Order Tracking
    
    public OrderDto.OrderResponse addTrackingInfo(String orderId, String trackingNumber, String carrier) {
        log.info("Adding tracking info for order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (order.getStatus() != Order.OrderStatus.SHIPPED) {
            throw new ValidationException("Cannot add tracking info for order in status: " + order.getStatus());
        }
        
        order.setTrackingNumber(trackingNumber);
        order.setCarrier(carrier);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cache
        cacheService.evictOrderCache(orderId);
        
        // Send tracking email
        User user = userRepository.findById(order.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendOrderTracking(user.getEmail(), savedOrder);
        }
        
        log.info("Tracking info added successfully for order ID: {}", orderId);
        return mapToOrderResponse(savedOrder);
    }
    
    // Utility Methods
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private Order.OrderItem mapCartItemToOrderItem(Cart.CartItem cartItem) {
        return Order.OrderItem.builder()
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productImage(cartItem.getProductImage())
                .price(cartItem.getPrice())
                .quantity(cartItem.getQuantity())
                .build();
    }
    
    private boolean isValidStatusTransition(Order.OrderStatus current, Order.OrderStatus newStatus) {
        switch (current) {
            case PENDING:
                return newStatus == Order.OrderStatus.CONFIRMED || newStatus == Order.OrderStatus.CANCELLED;
            case CONFIRMED:
                return newStatus == Order.OrderStatus.PROCESSING || newStatus == Order.OrderStatus.CANCELLED;
            case PROCESSING:
                return newStatus == Order.OrderStatus.SHIPPED || newStatus == Order.OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == Order.OrderStatus.DELIVERED || newStatus == Order.OrderStatus.CANCELLED;
            case DELIVERED:
                return newStatus == Order.OrderStatus.COMPLETED;
            case CANCELLED:
            case COMPLETED:
                return false;
            default:
                return false;
        }
    }
    
    private boolean canCancelOrder(Order order) {
        return order.getStatus() == Order.OrderStatus.PENDING || 
               order.getStatus() == Order.OrderStatus.CONFIRMED ||
               order.getStatus() == Order.OrderStatus.PROCESSING;
    }
    
    private OrderDto.OrderResponse mapToOrderResponse(Order order) {
        return OrderDto.OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    public double getTotalOrderValue(String userId) {
        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, Order.OrderStatus.COMPLETED);
        return orders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }
    
    public int getOrderCount(String userId) {
        return orderRepository.countByUserId(userId);
    }
}